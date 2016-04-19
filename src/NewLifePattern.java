/**
 * Created by MingJe on 2016/4/18.
 */

import DataStructure.ActivityInstance;
import Learning.Clustering;
import Learning.DPMM;
import Learning.WekaRegression;
import SDLE.SDLE;
import smile.regression.Regression;
import tool.ActivityInstanceParser;
import weka.clusterers.Clusterer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
    Map<String, ArrayList<WekaRegression>> regressors;

    //Use Expectation Maximization to cluster each activity start time
    Map<String, Clusterer> activityStartTimeClusterer;
    ArrayList<String> activityList;
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
        activityStartTimeClusterer = new HashMap<>();
        activityList = new ArrayList<>();
        Map<String, Integer> resultMap = new HashMap<>();
        regressors = new HashMap<>();
        for (int i = 0; i < 7; i++) {
            resultMap.put(String.valueOf(i), 0);
        }
        weekResultMap = resultMap;
    }

    public static void main(String[] args) throws InterruptedException, IOException, ParseException {
        NewLifePattern lifePattern = new NewLifePattern();
        //lifePattern.readFile(5, 1, rh, beta);
        //lifePattern.perDayActivityEstimation(66);

        //Activity Instance parse, return an object and write the file;
        ArrayList<ActivityInstance>[][] total = ActivityInstanceParser.yin(84, lifePattern.weekResultMap);
        lifePattern.weekActivityInstances = total[0];
        lifePattern.testWeekActivityInstances = total[1];

        //Activity Start Time Clustering
        lifePattern.activityStartTimeClustering();
        //Activity Duration Estimation
        lifePattern.activityDurationEstimation();
    }

    private void activityRelationLearning() {
        for (int i = 2; i < weekActivityInstances[0].size(); i++) {

        }
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


        } catch (IOException e) {
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

    /**
     * Re-structure activity instances
     * Re-structure activity instances
     *
     * @param weekActivityInstances
     * @return activity instances which is categorized by activity name
     */
    private Map<String, ArrayList<ActivityInstance>>[] eachActivity(ArrayList<ActivityInstance>[] weekActivityInstances) {
        Map<String, ArrayList<ActivityInstance>>[] eachActivity = new HashMap[weekActivityInstances.length];
        for (int i = 0; i < eachActivity.length; i++) {
            eachActivity[i] = new HashMap<>();
        }
        //Map<String, Integer> activities = new HashMap<>();
        for (int i = 0; i < weekActivityInstances.length; i++) {
            for (int j = 0; j < weekActivityInstances[i].size(); j++) {
                ActivityInstance activityInstance = weekActivityInstances[i].get(j);
                String activity = activityInstance.getActivity();
                if (!eachActivity[i].containsKey(activity)) {
                    eachActivity[i].put(activity, new ArrayList<>());
                    activityList.add(activity);
                }
                eachActivity[i].get(activity).add(activityInstance);
            }
        }

        return eachActivity;
    }

    /**
     * Use Expectation Maximization to cluster each activities and write feature for activity duration estimation
     *
     * @throws IOException
     * @throws ParseException
     */
    private void activityStartTimeClustering() throws IOException, ParseException {
        Map<String, ArrayList<ActivityInstance>>[] eachActivity = eachActivity(weekActivityInstances);
        StringBuilder featureString = new StringBuilder();
        SimpleDateFormat startTimeDateFormat = new SimpleDateFormat("HH:mm:ss");
        FileWriter fw = null;
        String fileName = null;
        ActivityInstance activityInstance = null;


        for (int i = 0; i < eachActivity.length; i++) {
            Collection<ArrayList<ActivityInstance>> values = eachActivity[i].values();
            Iterator<ArrayList<ActivityInstance>> activityIterator = values.iterator();
            while (activityIterator.hasNext()) {
                ArrayList<ActivityInstance> activityInstances = activityIterator.next();
                for (int k = 0; k < activityInstances.size(); k++) {
                    activityInstance = activityInstances.get(k);
                    if (k == 0) {
                        fileName = "ActivityStartTime/" + activityInstance.getActivity();
                        fw = new FileWriter("report/features/" + fileName + ".arff");
                        fw.write("@RELATION " + activityInstance.getActivity() + "_startTime\n" +
                                "@ATTRIBUTE startTime" + " Numeric" + "\n" +
                                //"@ATTRIBUTE duration" + " Numeric" + "\n" +
                                "@data\n");
                    }
                    Date date = startTimeDateFormat.parse(activityInstance.getStartTime());

                    long startTime = date.getTime() / 1000 / 60; // unit : minute
                    if (startTime < 0) {
                        System.out.println(startTime);
                    }
                    featureString.append(startTime + /*"," + activityInstance.getDuration() +*/ "\n");
                }
                fw.write(featureString.toString());
                featureString = new StringBuilder();
                fw.flush();
                fw.close();
                Clustering clustering = new Clustering(activityInstance.getActivity(), fileName);
                clustering.train(null);
                clustering.saveModel();
                activityStartTimeClusterer.put(activityInstance.getActivity(), clustering.getClusterer());

                //write feature for activity duration estimation
                ArrayList<Integer>[] instanceBelongToCluster = clustering.getInstanceBelongToCluster();
                for (int k = 0; k < instanceBelongToCluster.length; k++) {
                    fileName = "ActivityDurationEstimation/" + activityInstance.getActivity() + "-" + k;
                    fw = new FileWriter("report/features/" + fileName + ".arff");
                    fw.write("@RELATION " + activityInstance.getActivity() + "-" + k + "_duration\n" +
                            "@ATTRIBUTE startTime" + " Numeric" + "\n" +
                            "@ATTRIBUTE duration" + " Numeric" + "\n" +
                            "@data\n");
                    for (int l = 0; l < instanceBelongToCluster[k].size(); l++) {
                        activityInstance = activityInstances.get(instanceBelongToCluster[k].get(l));
                        Date date = startTimeDateFormat.parse(activityInstance.getStartTime());
                        long startTime = date.getTime() / 1000 / 60;
                        featureString.append(startTime + "," + activityInstance.getDuration() + "\n");
                    }
                    fw.write(featureString.toString());
                    featureString = new StringBuilder();
                    fw.flush();
                    fw.close();
                }

            }
        }
    }

    /**
     * Use Additive Regression(GBRT) to find relation between start time and duration
     */
    private void activityDurationEstimation() {
        for (int i = 0; i < activityList.size(); i++) {
            String activity = activityList.get(i);
            int cluster = 0;
            ArrayList<WekaRegression> eachActivityRegressions = new ArrayList<>();
            System.out.println("Activity:" + activity);
            while (true) {
                String fileName = "ActivityDurationEstimation/" + activity + "-" + cluster;
                WekaRegression regression = new WekaRegression(activity, fileName);
                if (!regression.isFeatureExist()) break;
                regression.train(fileName);
                regression.saveModel();
                eachActivityRegressions.add(regression);
                cluster++;
            }
            regressors.put(activity, eachActivityRegressions);
        }
    }

}
