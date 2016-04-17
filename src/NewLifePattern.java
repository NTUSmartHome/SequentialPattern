/**
 * Created by MingJe on 2016/4/18.
 */

import DataStructure.ActivityInstance;
import Learning.DPMM;

import com.datumbox.common.dataobjects.AssociativeArray;
import com.datumbox.common.dataobjects.Record;
import SDLE.SDLE;
import smile.regression.Regression;
import tool.ActivityInstanceParser;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


public class NewLifePattern {
    static double rh = 0.05, beta = 0.01;
    ArrayList<ArrayList<SDLE>> weekSDLEList;
    ArrayList<SDLE> sdleList;
    ArrayList<ArrayList<String>> instanceLabel;
    ArrayList<ArrayList<SDLE>> newWeekSDLEList;
    Map<String, Integer> weekResultMap;
    Map<String, Integer>[] daySegmentationMap;
    ArrayList<ActivityInstance>[] weekActivityInstances;
    ArrayList<ActivityInstance>[] testWeekActivityInstances;
    Map<String, Map<Integer, Regression>>[] regressors;
    int MaxCluster;
    //----------------------------------WSU M1 data begins on Friday------------------------------------//
    //----------------------------------Ming data begins on Sat------------------------------------//
    final int weekDayStart = 5;
    //-----------------------------------------------------------------------------------------------//

    public NewLifePattern() {
        sdleList = new ArrayList<>();
        instanceLabel = new ArrayList<>();
        weekSDLEList = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            weekSDLEList.add(new ArrayList<>());
        }
    }

    public static void main(String[] args) throws InterruptedException, IOException, ParseException {
        NewLifePattern lifePattern = new NewLifePattern();
        lifePattern.readFile(5, 1, rh, beta);
        lifePattern.perDayActivityEstimation(66);
        //Activity Instance Grouping
        lifePattern.activityInstanceModelling();
    }


    private void perDayActivityEstimation(int trainedDays) {

        try {
            // perDayActivityEstimation for day merge
            String file = "report/WeekSDLE/Features/";
            newWeekSDLEList = newWeekSDLEList(7);
            int day = weekDayStart;
            for (int i = 0; i < trainedDays; i++) {
                for (int j = 0; j < instanceLabel.size(); j++) {
                    String[] acts = instanceLabel.get(j).get(i).split(",");
                    newWeekSDLEList.get(day).get(j).parameterUpdating(acts);
                    day = (day + 1) % 7;
                }
            }
            //write feature for day merge
            FileWriter fw = new FileWriter(file + "WeekSDLEFeatures.csv");
            StringBuilder stringBuilder = new StringBuilder(); //initStringBuilder(3168);
            for (int i = 0; i < 7; i++) {
                for (int j = 0; j < instanceLabel.size(); j++) {
                    ArrayList<Double> cellDistribution = newWeekSDLEList.get(i).get(j).getDistribution();
                    for (int k = 1; k < cellDistribution.size() - 1; k++) {
                        stringBuilder.append(String.valueOf(cellDistribution.get(k).doubleValue() * 100) + ",");
                    }
                }
                stringBuilder.append("\n");
            }
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
            fw.write(stringBuilder.toString());
            fw.close();
            weekResultMap = contextDayMerge(file + "WeekSDLEFeatures.csv");

            //perDayActivityEstimation for day segmentation
            int numOfGroup = weekResultMap.get("size");
            daySegmentationMap = new HashMap[numOfGroup];
            for (int i = 0; i < daySegmentationMap.length; i++) {
                daySegmentationMap[i] = new HashMap<>();
            }
            newWeekSDLEList = newWeekSDLEList(numOfGroup);
            day = weekDayStart;
            for (int i = 0; i < trainedDays; i++) {
                for (int j = 0; j < instanceLabel.size(); j++) {
                    String[] acts = instanceLabel.get(j).get(i).split(",");
                    newWeekSDLEList.get(weekResultMap.get(String.valueOf(day))).get(j).parameterUpdating(acts);
                    day = (day + 1) % 7;
                }
            }
            //write feature for day segmentation
            for (int i = 0; i < numOfGroup; i++) {
                fw = new FileWriter(file + "Segmentation/" + i + ".csv");
                stringBuilder = new StringBuilder();//initStringBuilder(11);
                for (int j = 0; j < instanceLabel.size(); j++) {
                    ArrayList<Double> cellDistribution = newWeekSDLEList.get(weekResultMap.get(String.valueOf(i))).get(j).getDistribution();
                    stringBuilder.append(String.valueOf(cellDistribution.get(1).doubleValue() * 8));
                    for (int k = 2; k < cellDistribution.size() - 1; k++) {
                        stringBuilder.append("," + String.valueOf(cellDistribution.get(k).doubleValue() * 8));
                    }
                    stringBuilder.append("\n");
                }
                fw.write(stringBuilder.toString());
                fw.close();
            }
            contextDaySegmentation(file + "Segmentation/");

            //Activity Instance parse, return an object and write the file;
            ArrayList<ActivityInstance>[][] total = ActivityInstanceParser.yin(7, weekResultMap);
            weekActivityInstances = total[0];
            weekActivityInstances = total[1];


        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }


    private Map<String, Integer> contextDayMerge(String file) {

        try {
            //return Learning.DPMM.MDPMMTrain("Model/dayMerge", file, 0.5, 1, 500);
            return DPMM.oneCluster(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;

    }

    private void contextDaySegmentation(String path) {
        int idx = 0;
        String fileName = path + idx++ + ".csv";
        File file = new File(fileName);
        while (file.exists()) {

            try {
                System.out.println("\n\n");
                daySegmentationMap[idx - 1] = DPMM.GDPMMTrain("/Model/" + idx + "Seg", fileName, 0.9, 1, 500);
                //daySegmentationMap[idx-1] = Learning.DPMM.HierarchicalAgglomerativeTrain(fileName);
                //daySegmentationMap[idx - 1] = Learning.DPMM.oneCluster(fileName);
                System.out.println(fileName + "\n\n");

            } catch (IOException e) {
                e.printStackTrace();
            }
            fileName = path + idx++ + ".csv";
            file = new File(fileName);

        }
    }

    private ArrayList<SDLE> newSDLEList() {
        ArrayList<SDLE> newSDLEList = new ArrayList<>();
        for (int i = 0; i < sdleList.size(); i++) {
            newSDLEList.add(new SDLE(rh, beta));
        }
        return newSDLEList;
    }

    private ArrayList<ArrayList<SDLE>> newWeekSDLEList(int weekDays) {
        ArrayList<ArrayList<SDLE>> weekSDLEList = new ArrayList<>();
        for (int i = 0; i < weekDays; i++) {
            weekSDLEList.add(newSDLEList());

        }
        return weekSDLEList;
    }

    private ArrayList<ArrayList<ActivityInstance>>[] eachActivity(ArrayList<ActivityInstance>[] weekActivityInstances) {
        ArrayList<ArrayList<ActivityInstance>>[] eachActivity = new ArrayList[weekActivityInstances.length];
        for (int i = 0; i < eachActivity.length; i++) {
            eachActivity[i] = new ArrayList<>();
        }
        Map<String, Integer> activities = new HashMap<>();
        for (int i = 0; i < weekActivityInstances.length; i++) {
            for (int j = 0; j < weekActivityInstances[i].size(); j++) {
                ActivityInstance activityInstance = weekActivityInstances[i].get(j);
                String activity = activityInstance.getActivity();
                if (!activities.containsKey(activity)) {
                    eachActivity[i].add(new ArrayList<>());
                    activities.put(activity, activities.size());
                }
                eachActivity[i].get(activities.get(activity)).add(activityInstance);
            }
            activities.clear();
        }

        return eachActivity;
    }

    private void activityInstanceModelling() {
        ArrayList<ArrayList<ActivityInstance>>[] eachActivity = eachActivity(weekActivityInstances);
        ArrayList<ArrayList<ActivityInstance>>[] testEachActivity = eachActivity(testWeekActivityInstances);
        StringBuilder featureString = new StringBuilder();
        SimpleDateFormat startTimeDateFormat = new SimpleDateFormat("HH:mm:ss");
        FileWriter fw = null;
        String fileName = null;
        ActivityInstance activityInstance = null;
        regressors = new HashMap[eachActivity.length];
        for (int i = 0; i < regressors.length; i++) {
            regressors[i] = new HashMap<>();
        }
        try {
            for (int k = 0; k < eachActivity.length; k++) {
                for (int i = 0; i < eachActivity[k].size(); i++) {
                    for (int j = 0; j < eachActivity[k].get(i).size(); j++) {
                        activityInstance = eachActivity[k].get(i).get(j);
                        if (j == 0) {
                            fileName = "report/ActivityInstance/Grouping/" + "group" + k + "-"
                                    + activityInstance.getActivity() + "-" + j + ".csv";

                            fw = new FileWriter(fileName);

                        }
                        Date date = startTimeDateFormat.parse(activityInstance.getStartTime());
                        long startTime = date.getTime() / 1000 / 60; // unit : minute
                        if (startTime < 0) {
                            System.out.println(startTime);
                        }
                        featureString.append(startTime + "," + activityInstance.getDuration() + "\n");
                    }

                    fw.write(featureString.toString());
                    featureString = new StringBuilder();
                    fw.flush();
                    fw.close();

                    //if (!activityInstance.getActivity().equals("6")) continue;
                    //Map<String, Integer> activityResultMap = Learning.DPMM.GDPMMTrain("Model/" + "group" + k + "_" + activityInstance.getActivity(), fileName, 0.1, 0, 500);
                    //Map<String, Integer> activityResultMap = Learning.DPMM.MDPMMTrain("Model/" + "group" + k + "_" + activityInstance.getActivity(), fileName, 0.1, 1, 500);
                    Map<String, Integer> activityResultMap = DPMM.oneCluster(fileName);
                    //Map<String, Integer> activityResultMap = Learning.DPMM.HierarchicalAgglomerativeTrain(fileName);
                    featureString = new StringBuilder();
                    ArrayList<Record>[] records = new ArrayList[activityResultMap.get("size")];
                    ArrayList<Long[]>[] xRecordsSmile = new ArrayList[activityResultMap.get("size")];
                    ArrayList<Long>[] yRecordsSmile = new ArrayList[activityResultMap.get("size")];


                    //if we want use idx [0, size] to access activityResultMap,
                    //activityResultMap.size should -1 because of key "size"
                    ActivityInstance tmpAI = null;
                    for (int j = 0; j < activityResultMap.size() - 1; j++) {
                        AssociativeArray record = new AssociativeArray();
                        Long[] xRecordSmile = new Long[1];
                        tmpAI = eachActivity[k].get(i).get(j);
                        Date date = startTimeDateFormat.parse(tmpAI.getStartTime());
                        long startTime = date.getTime() / 1000 / 60; // unit : minute
                        /*record.put(0, startTime);
                        record.put(1, startTime * startTime);
                        record.put(2, startTime * startTime * startTime); */
                        record.put(4, startTime / (1 + Math.exp(1)));
                        xRecordSmile[0] = startTime;
                        int trueClusterIdx = activityResultMap.get(String.valueOf(j));
                        try {
                            records[trueClusterIdx].add(new Record(record, tmpAI.getDuration()));
                            xRecordsSmile[trueClusterIdx].add(xRecordSmile);
                            yRecordsSmile[trueClusterIdx].add(tmpAI.getDuration());
                        } catch (NullPointerException e) {
                            records[trueClusterIdx] = new ArrayList<>();
                            records[trueClusterIdx].add(new Record(record, tmpAI.getDuration()));
                            xRecordsSmile[trueClusterIdx] = new ArrayList<>();
                            yRecordsSmile[trueClusterIdx] = new ArrayList<>();
                            xRecordsSmile[trueClusterIdx].add(xRecordSmile);
                            yRecordsSmile[trueClusterIdx].add(tmpAI.getDuration());
                        }
                    }
                    System.out.println(tmpAI.getActivity());
                    Map<Integer, Regression> regressionMap = new HashMap<>();
                    //if (!tmpAI.getActivity().equals("1") || !tmpAI.getActivity().equals("12")) {
                    for (int j = 0; j < activityResultMap.get("size"); j++) {
                        System.out.println("group" + k + "-"
                                + "-" + j);
                        //Learning.DPMM.NLMS(records[j]);
                        Regression regression = Smile.Mean(xRecordsSmile[j], yRecordsSmile[j]);
                        regressionMap.put(j, regression);
                        System.out.println();
                    }
                    regressors[k].put(tmpAI.getActivity(), regressionMap);
                    //}
                }
            }

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
            for (int k = 0; k < testEachActivity.length; k++) {
                for (int i = 0; i < testEachActivity[k].size(); i++) {
                    ArrayList<Record> records = new ArrayList<>();
                    ActivityInstance testActivityInstance = null;
                    for (int j = 0; j < testEachActivity[k].get(i).size(); j++) {
                        AssociativeArray array = new AssociativeArray();
                        testActivityInstance = testEachActivity[k].get(i).get(j);
                        array.put(0, new Double(simpleDateFormat.parse(testActivityInstance.getStartTime()).getTime()) / 1000 / 60); // unit : minute)
                        array.put(1, new Double(testActivityInstance.getDuration()));
                        records.add(new Record(array, ""));
                    }
                    DPMM.loadGaussianDPMMModel("Model/" + "group" + k
                            + "_" + testActivityInstance.getActivity());
                    Map<String, Integer> resultMap = DPMM.OneClusterPredict(records);
                    double errorMinute = 0;
                    int numOfTestRecords = 0;
                    for (int j = 0; j < (numOfTestRecords = testEachActivity[k].get(i).size()); j++) {
                        testActivityInstance = testEachActivity[k].get(i).get(j);
                        Map<Integer, Regression> regressionMap = regressors[k].get(testActivityInstance.getActivity());
                        Regression regression = regressionMap.get(resultMap.get(String.valueOf(j)));
                        double[] textX = new double[1];
                        textX[0] = Double.valueOf(simpleDateFormat.parse(testActivityInstance.getStartTime()).getTime() / 1000 / 60);
                        try {
                            errorMinute += Math.abs(Double.valueOf(testActivityInstance.getDuration()) -
                                    regression.predict(textX));

                        } catch (NullPointerException e) {
                            System.out.println();
                        }
                    }
                    System.out.println(testActivityInstance.getActivity() + " : " + errorMinute / numOfTestRecords);
                }

            }


        } catch (ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void readFile(int timeInterval, int option, double rh, double beta) {
        int id = 0;
        StringBuilder inputFile = new StringBuilder("db/");
        timeInterval = getTimeInterval(timeInterval, option);
        switch (option) {
            case 0:
                inputFile.append((int) (timeInterval / Math.pow(60, option)));
                inputFile.append("second/");
                break;
            case 1:
                inputFile.append((int) (timeInterval / Math.pow(60, option)));
                inputFile.append("minute/");
                break;
            case 2:
                inputFile.append((int) (timeInterval / Math.pow(60, option)));
                inputFile.append("hour/");
                break;
            case 3:
                inputFile.append((int) (timeInterval / Math.pow(60, option)));
                inputFile.append("day/");
                break;
        }
        //String inputFile = "db/SDLE" + id + ".txt";
        String fileString = inputFile.toString() + id + ".txt";
        File file = new File(fileString);

        while (file.exists()) {

            try {
                FileReader fr = new FileReader(file);
                BufferedReader br = new BufferedReader(fr);
                String line;
                instanceLabel.add(new ArrayList<>());
                sdleList.add(new SDLE(rh, beta));
                //---------------------Day of Week-----------------//
                for (int i = 0; i < weekSDLEList.size(); i++) {
                    weekSDLEList.get(i).add(new SDLE(rh, beta));
                }
                //------------------------------------------------//
                while ((line = br.readLine()) != null) {
                    instanceLabel.get(id).add(line);
                }
                fr.close();
                br.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            id++;
            fileString = inputFile.toString() + id + ".txt";
            file = new File(fileString);
        }
    }

    private int getTimeInterval(int timeInterval, int option) {
        switch (option) {
            case 1:
                timeInterval *= 60;
                break;
            case 2:
                timeInterval *= 60 * 60;
                break;
            case 3:
                timeInterval *= 60 * 60 * 24;
                break;

        }
        return timeInterval;
    }

    private Map<Integer, Integer> weekMap() {
        Map<Integer, Integer> weekMap = new HashMap<>();
        try {
            FileReader fr = new FileReader("report/WeekSDLE/Learning.DPMM/WeekSDLEResult");
            BufferedReader br = new BufferedReader(fr);
            String line;
            int i = 0;
            while ((line = br.readLine()) != null) {
                weekMap.put(i, Integer.parseInt(line.split("\\s+")[1]));
                i++;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        int MaxCluster = 0;
        for (int i = 0; i < weekMap.size(); i++) {
            if (weekMap.get(i) > MaxCluster)
                MaxCluster = weekMap.get(i);
        }
        this.MaxCluster = MaxCluster + 1;
        return weekMap;
    }

    private ArrayList<ArrayList<SDLE>> renewWeekSDLE() {
        ArrayList<ArrayList<SDLE>> tmpWeekSDLEList = new ArrayList<>();
        for (int i = 0; i < MaxCluster; i++) {
            tmpWeekSDLEList.add(new ArrayList<>());
            for (int j = 0; j < weekSDLEList.get(0).size(); j++) {
                tmpWeekSDLEList.get(i).add(new SDLE(rh, beta));
            }
        }
        return tmpWeekSDLEList;
    }
}
