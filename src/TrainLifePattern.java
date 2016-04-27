/**
 * Created by MingJe on 2016/4/18.
 */

import DataStructure.ActivityInstance;
import Learning.Classifier;
import Learning.Clustering;
import Learning.WekaRegression;
import SDLE.SDLE;
import tool.ActivityInstanceParser;
import tool.LogPreProcessing;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


public class TrainLifePattern {
    static double rh = 0.05, beta = 0.01;
    ArrayList<ArrayList<SDLE>> weekSDLEList;
    ArrayList<SDLE> sdleList;
    ArrayList<ArrayList<String>> instanceLabel;
    ArrayList<ArrayList<SDLE>> newWeekSDLEList;
    Map<String, Integer> weekResultMap;
    Map<String, Integer>[] daySegmentationMap;
    ArrayList<ActivityInstance>[] weekActivityInstances;
    ArrayList<ActivityInstance>[] testWeekActivityInstances;

    //Use GBRT to learn the relation between start time and duration. And this model use to infer activity duration
    Map<String, ArrayList<WekaRegression>> regressors;
    //Use BayesNet to train relationship between activity
    Classifier relationer;
    //Use Expectation Maximization to cluster each activity start time
    Map<String, Clustering> activityStartTimeClusterer;
    //Store activity name
    ArrayList<String> activityList;
    //----------------------------------WSU M1 data begins on Friday------------------------------------//
    //----------------------------------Ming data begins on Sat------------------------------------//
    final int weekDayStart = 5;
    //-----------------------------------------------------------------------------------------------//



    public TrainLifePattern() throws IOException, ParseException {
        sdleList = new ArrayList<>();
        instanceLabel = new ArrayList<>();
        weekSDLEList = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            weekSDLEList.add(new ArrayList<>());
        }


        Map<String, Integer> resultMap = new HashMap<>();
        for (int i = 0; i < 7; i++) {
            resultMap.put(String.valueOf(i), 0);
        }
        weekResultMap = resultMap;

        activityStartTimeClusterer = new HashMap<>();
        activityList = new ArrayList<>();
        regressors = new HashMap<>();

        //Activity Instance parse, return an object and write the file;
        ArrayList<ActivityInstance>[][] total = ActivityInstanceParser.original(400, weekResultMap);
        weekActivityInstances = total[0];
        testWeekActivityInstances = total[1];
        while (true) {
            int size = weekActivityInstances[0].size();
            weekActivityInstances[0] = LogPreProcessing.preProcessing(weekActivityInstances[0]);
            if (size == weekActivityInstances[0].size())
                break;
        }

        //Activity Start Time Clustering
        activityStartTimeClustering();
        //Activity Duration Estimation
        activityDurationEstimation();
        //Activity Relation Construction
        activityRelationConstruction();
    }

    public static void main(String[] args) throws InterruptedException, IOException, ParseException {
        TrainLifePattern lifePattern = new TrainLifePattern();
        //lifePattern.readFile(5, 1, rh, beta);
        //lifePattern.perDayActivityEstimation(66);

      /*  //Activity Instance parse, return an object and write the file;
        ArrayList<ActivityInstance>[][] total = ActivityInstanceParser.original(84, lifePattern.weekResultMap);
        lifePattern.weekActivityInstances = total[0];
        lifePattern.testWeekActivityInstances = total[1];*/

      /*  //Activity Start Time Clustering
        lifePattern.activityStartTimeClustering();
        //Activity Duration Estimation
        lifePattern.activityDurationEstimation();
        //Activity Relation Construction
        lifePattern.activityRelationConstruction();*/

    }

    /*private void perDayActivityEstimation(int trainedDays) {

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
    }*/

    /**
     * Learn the relationships between activities
     */
    private void activityRelationConstruction() {

        try {
            String fileName = "ActivityRelationConstruction";
            FileWriter fw = new FileWriter("report/features/" + fileName + ".arff");
            StringBuilder featureString = new StringBuilder("@RELATION activityRelation \n");

            featureString.append("@ATTRIBUTE act_last_two {");
            for (int i = 0; i < activityList.size(); i++) {
                if (i == 0) featureString.append(activityList.get(i));
                else featureString.append("," + activityList.get(i));
            }

            featureString.append("}\n@ATTRIBUTE act_last {");
            for (int i = 0; i < activityList.size(); i++) {
                if (i == 0) featureString.append(activityList.get(i));
                else featureString.append("," + activityList.get(i));
            }

            featureString.append("}\n@ATTRIBUTE startTime {");
            for (int i = 0; i < 24; i++) {
                if (i == 0) featureString.append(i);
                else featureString.append("," + i);
            }

            /*featureString.append("}\n@ATTRIBUTE dayofweek {");
            for (int i = 1; i < 8; i++) {
                if (i == 0) featureString.append(i);
                else featureString.append("," + i);
            }*/

            featureString.append("}\n@ATTRIBUTE act {");
            for (int i = 0; i < activityList.size(); i++) {
                if (i == 0) featureString.append(activityList.get(i));
                else featureString.append("," + activityList.get(i));
            }

            featureString.append("}\n@data\n");
            for (int i = 2; i < weekActivityInstances[0].size(); i++) {
                ActivityInstance activityInstance = weekActivityInstances[0].get(i);
                String[] startTime = activityInstance.getStartTime().split(":");
                int startTimeHour = Integer.parseInt(startTime[0]);
                int startTimeMinute = Integer.parseInt(startTime[1]);
                int segment = startTimeHour * 12 + startTimeMinute / 5;
                if (segment >= 4 && segment <= 64)
                    segment = 4;
                else if ((segment > 64 && segment <= 81) || (segment >= 270 && segment <= 287) || segment < 4)
                    segment = 3;
                else if (segment > 81 && segment <= 100)
                    segment = 0;
                else if (segment > 100 && segment <= 225)
                    segment = 1;
                else
                    segment = 2;

                featureString.append(weekActivityInstances[0].get(i - 2).getActivity() + "," + weekActivityInstances[0].get(i - 1).getActivity()
                        + "," + startTimeHour + "," + /*activityInstance.getDayOfWeek() + "," +*/ activityInstance.getActivity() + "\n");
            }
            fw.write(featureString.toString());
            fw.flush();
            fw.close();
            relationer = new Classifier("Relation", fileName);
            relationer.train(null);
            relationer.saveModel();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Re-structure activity instances
     * @param weekActivityInstances
     * @return activity instances which is categorized by activity name
     */
    private Map<String, ArrayList<ActivityInstance>>[] eachActivity(ArrayList<ActivityInstance>[]
                                                                            weekActivityInstances) {
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
                activityStartTimeClusterer.put(activityInstance.getActivity(), clustering);

                //write feature for activity duration estimation
                //ArrayList<Integer>[] instanceBelongToCluster = clustering.getInstanceBelongToCluster();
                ArrayList<Integer>[] instanceBelongToCluster = new ArrayList[1];
                instanceBelongToCluster[0] = new ArrayList<>();
                for (int j = 0; j < activityInstances.size(); j++) {
                    instanceBelongToCluster[0].add(j);
                }

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



    public Classifier getRelationer() {
        return relationer;
    }

    public void setRelationer(Classifier relationer) {
        this.relationer = relationer;
    }

    public Map<String, Clustering> getActivityStartTimeClusterer() {
        return activityStartTimeClusterer;
    }

    public void setActivityStartTimeClusterer(Map<String, Clustering> activityStartTimeClusterer) {
        this.activityStartTimeClusterer = activityStartTimeClusterer;
    }

    public ArrayList<String> getActivityList() {
        return activityList;
    }

    public void setActivityList(ArrayList<String> activityList) {
        this.activityList = activityList;
    }

    public Map<String, ArrayList<WekaRegression>> getRegressors() {
        return regressors;
    }

    public void setRegressors(Map<String, ArrayList<WekaRegression>> regressors) {
        this.regressors = regressors;
    }

}
