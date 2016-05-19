package Pattern; /**
 * Created by MingJe on 2016/4/18.
 */

import DataStructure.ActivityInstance;
import Learning.Classifier;
import Learning.Clustering;
import Learning.WekaRegression;
import SDLE.SDLE;
import tool.ActivityInstanceParser;
import tool.LogPreProcessing;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


public class TrainLifePattern {
    static double rh = 0.05, beta = 0.01;
    //----------------------------------WSU M1 data begins on Friday------------------------------------//
    //----------------------------------Ming data begins on Sat------------------------------------//
    final int weekDayStart = 5;
    List<String> sdleAct = new ArrayList<>();
    ArrayList<Map<String, ArrayList<SDLE>>> weekSDLEList;
    Map<String, ArrayList<SDLE>> sdleList;
    Map<String, ArrayList<ArrayList<String>>> instanceLabel;
    ArrayList<ArrayList<SDLE>> newWeekSDLEList;
    Map<String, Integer> weekResultMap;
    ArrayList<ActivityInstance>[] weekActivityInstances;
    ArrayList<ActivityInstance>[] testWeekActivityInstances;
    Map<String, ArrayList<ActivityInstance>>[] eachActivity;
    //Use GBRT to learn the relation between start time and duration. And this model use to infer activity duration
    Map<String, ArrayList<WekaRegression>> regressors;
    //Use BayesNet to train relationship between activity
    Classifier relationer;
    Classifier[] multipleRelationer;
    //Use Expectation Maximization to cluster each activity start time
    Map<String, Clustering> activityStartTimeClusterer;
    //Store activity name
    ArrayList<String> activityList;
    //-----------------------------------------------------------------------------------------------//


    public TrainLifePattern() throws IOException, ParseException {
        sdleList = new HashMap<>();
        instanceLabel = new HashMap<>();
        weekSDLEList = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            weekSDLEList.add(new HashMap<>());
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
        sdleAct.add("true");
        sdleAct.add("false");

        //test sdle
        readSDLEFile(5, 1, rh, beta);
        SDLEAccumulate(100);
        writeSDLE();
        System.out.println("Thread test");

        //Activity Start Time Clustering
        activityStartTimeClustering();
        //Activity Duration Estimation
        activityDurationEstimation();
        //Activity Relation Construction
        activityRelationConstruction();

        //Multiple Activity Relation Construction
        activityRelationConstructionMutiple();
    }

    public static void main(String[] args) throws InterruptedException, IOException, ParseException {
        TrainLifePattern lifePattern = new TrainLifePattern();
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
        //lifePattern.readSDLEFile(5, 1, rh, beta);

    }


    /**
     * Learn the relationships between activities
     */
    private void activityRelationConstructionMutiple() {

        try {
            String fileName = "ActivityRelationConstruction";
            FileWriter[] fw = new FileWriter[5];
            multipleRelationer = new Classifier[5];
            StringBuilder featureString = new StringBuilder("@RELATION activityRelation \n");

           /* featureString.append("@ATTRIBUTE act_last_two {");
            for (int i = 0; i < activityList.size(); i++) {
                if (i == 0) featureString.append(activityList.get(i));
                else featureString.append("," + activityList.get(i));
            }*/

            featureString.append("\n@ATTRIBUTE currentActivity {");
            for (int i = 0; i < activityList.size(); i++) {
                if (i == 0) featureString.append(activityList.get(i));
                else featureString.append("," + activityList.get(i));
            }

            featureString.append("}\n@ATTRIBUTE nextStartTime {");
            for (int i = 0; i < 24; i++) {
                if (i == 0) featureString.append(i);
                else featureString.append("," + i);
            }

            /*featureString.append("}\n@ATTRIBUTE dayofweek {");
            for (int i = 0; i < 7; i++) {
                if (i == 0) featureString.append(i);
                else featureString.append("," + i);
            }*/

            featureString.append("}\n@ATTRIBUTE nextActivity {");
            for (int i = 0; i < activityList.size(); i++) {
                if (i == 0) featureString.append(activityList.get(i));
                else featureString.append("," + activityList.get(i));
            }
            //write attribute
            FileWriter attWriter = new FileWriter("report/features/relationAtt.arff");
            attWriter.write(featureString.toString() + "} \n @data");
            attWriter.close();

            featureString.append("}\n@data\n");

            for (int i = 0; i < 5; i++) {
                fw[i] = new FileWriter("report/features/" + fileName + "_" + i + ".arff");
                fw[i].write(featureString.toString());
            }

            for (int i = 2; i < weekActivityInstances[0].size(); i++) {
                ActivityInstance activityInstance = weekActivityInstances[0].get(i);
                String[] startTime = activityInstance.getStartTime().split(":");
                int startTimeHour = Integer.parseInt(startTime[0]);
                int startTimeMinute = Integer.parseInt(startTime[1]);
                String[] preEndTime = weekActivityInstances[0].get(i - 1).getEndTime().split(":");
                int preEndTimeHour = Integer.parseInt(preEndTime[0]);
                int segment = startTimeHour * 12 + startTimeMinute / 5;
                if (segment >= 4 && segment <= 64) {
                    fw[4].write(/*weekActivityInstances[0].get(i - 2).getActivity() + "," +*/ weekActivityInstances[0].get(i - 1).getActivity()
                            + "," + preEndTimeHour + /*"," + activityInstance.getDayOfWeek() +*/ "," + activityInstance.getActivity() + "\n");
                    segment = 4;
                } else if ((segment > 64 && segment <= 81) || (segment >= 270 && segment <= 287) || segment < 4) {
                    fw[3].write(/*weekActivityInstances[0].get(i - 2).getActivity() + "," +*/ weekActivityInstances[0].get(i - 1).getActivity()
                            + "," + preEndTimeHour + /*"," + activityInstance.getDayOfWeek() +*/ "," + activityInstance.getActivity() + "\n");
                    segment = 3;
                } else if (segment > 81 && segment <= 100) {
                    fw[0].write(/*weekActivityInstances[0].get(i - 2).getActivity() + "," +*/ weekActivityInstances[0].get(i - 1).getActivity()
                            + "," + preEndTimeHour + /*"," + activityInstance.getDayOfWeek() +*/ "," + activityInstance.getActivity() + "\n");
                    segment = 0;
                } else if (segment > 100 && segment <= 225) {
                    fw[1].write(/*weekActivityInstances[0].get(i - 2).getActivity() + "," +*/ weekActivityInstances[0].get(i - 1).getActivity()
                            + "," + preEndTimeHour + /*"," + activityInstance.getDayOfWeek() +*/ "," + activityInstance.getActivity() + "\n");
                    segment = 1;
                } else {
                    fw[2].write(/*weekActivityInstances[0].get(i - 2).getActivity() + "," +*/ weekActivityInstances[0].get(i - 1).getActivity()
                            + "," + preEndTimeHour + /*"," + activityInstance.getDayOfWeek() +*/ "," + activityInstance.getActivity() + "\n");
                    segment = 2;
                }

            }
            double correct = 0;
            double total = 0;
            for (int i = 0; i < 5; i++) {
                fw[i].flush();
                fw[i].close();
                multipleRelationer[i] = new Classifier("Relation" + "i", fileName + "_" + i);
                multipleRelationer[i].train(null);
                multipleRelationer[i].saveModel();
                correct += multipleRelationer[i].getEval().correct();
                total += multipleRelationer[i].getEval().numInstances();
            }
            System.out.println("Results\n======\n\n" +
                    "Correctly Classified Instances " + correct + "\n                 " + correct / total + "\n" +
                    "Total Number of Instances " + total);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Learn the relationships between activities
     */
    private void activityRelationConstruction() {

        try {
            String fileName = "ActivityRelationConstruction";
            FileWriter fw = new FileWriter("report/features/" + fileName + ".arff");
            StringBuilder featureString = new StringBuilder("@RELATION activityRelation \n");

            /*featureString.append("@ATTRIBUTE act_last_two {");
            for (int i = 0; i < activityList.size(); i++) {
                if (i == 0) featureString.append(activityList.get(i));
                else featureString.append("," + activityList.get(i));
            }*/

            featureString.append("\n@ATTRIBUTE currentActivity {");
            for (int i = 0; i < activityList.size(); i++) {
                if (i == 0) featureString.append(activityList.get(i));
                else featureString.append("," + activityList.get(i));
            }

            /*featureString.append("}\n@ATTRIBUTE prestartTime {");
            for (int i = 0; i < 24; i++) {
                if (i == 0) featureString.append(i);
                else featureString.append("," + i);
            }*/

            featureString.append("}\n@ATTRIBUTE currentStartTime {");
            for (int i = 0; i < 24; i++) {
                if (i == 0) featureString.append(i);
                else featureString.append("," + i);
            }

            /*featureString.append("}\n@ATTRIBUTE dayofweek {");
            for (int i = 0; i < 7; i++) {
                if (i == 0) featureString.append(i);
                else featureString.append("," + i);
            }*/

            featureString.append("}\n@ATTRIBUTE nextActivity {");
            for (int i = 0; i < activityList.size(); i++) {
                if (i == 0) featureString.append(activityList.get(i));
                else featureString.append("," + activityList.get(i));
            }
            //write attribute
            FileWriter attWriter = new FileWriter("report/features/relationAtt.arff");
            attWriter.write(featureString.toString() + "} \n @data");
            attWriter.close();

            featureString.append("}\n@data\n");
            for (int i = 2; i < weekActivityInstances[0].size(); i++) {

                ActivityInstance activityInstance = weekActivityInstances[0].get(i);
                String[] startTime = activityInstance.getStartTime().split(":");
                int startTimeHour = Integer.parseInt(startTime[0]);
                int startTimeMinute = Integer.parseInt(startTime[1]);

                String[] preEndTime = weekActivityInstances[0].get(i - 1).getEndTime().split(":");
                int preEndTimeHour = Integer.parseInt(preEndTime[0]);


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

                featureString.append(/*weekActivityInstances[0].get(i - 2).getActivity() + "," + */weekActivityInstances[0].get(i - 1).getActivity()
                        + "," /*+ prestartTimeHour + "," */ + preEndTimeHour +/* "," + activityInstance.getDayOfWeek() + "," +*/ activityInstance.getActivity() + "\n");
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
     *
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
        eachActivity = eachActivity(weekActivityInstances);
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
                ArrayList<Integer>[] instanceBelongToCluster = clustering.getInstanceBelongToCluster();
                /*ArrayList<Integer>[] instanceBelongToCluster = new ArrayList[1];
                instanceBelongToCluster[0] = new ArrayList<>();
                for (int j = 0; j < activityInstances.size(); j++) {
                    instanceBelongToCluster[0].add(j);
                }*/
                for (int j = clustering.getOutlier().size() - 1; j >= 0; j--) {
                    activityInstances.remove((int) clustering.getOutlier().get(j));
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
            double absoluteError = 0;
            int numOfInstances = 0;
            while (true) {
                String fileName = "ActivityDurationEstimation/" + activity + "-" + cluster;
                WekaRegression regression = new WekaRegression(activity, fileName);
                if (!regression.isFeatureExist()) break;
                regression.train(fileName);
                regression.saveModel();
                eachActivityRegressions.add(regression);
                cluster++;
                absoluteError += regression.getEval().numInstances() * regression.getEval().meanAbsoluteError();
                numOfInstances += regression.getEval().numInstances();
            }
            System.out.println("Overall nean absolute error " + (absoluteError /= numOfInstances));
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


    private void SDLEAccumulate(int trainedDays) {
        int realTrainedDays = Math.min(trainedDays, instanceLabel.get(activityList.get(0)).get(0).size());
        for (int i = 0; i < activityList.size(); i++) {
            for (int j = 0; j < instanceLabel.get(activityList.get(i)).size(); j++) {
                for (int k = 0; k < realTrainedDays; k++) {
                    String[] acts = instanceLabel.get(activityList.get(i)).get(j).get(k).split(",");
                    sdleList.get(activityList.get(i)).get(j).parameterUpdating(acts);
                }
            }
        }
        System.out.println("SDLE accumulate " + realTrainedDays + " days");
        writeSDLE();
        for (int i = 0; i < activityList.size(); i++) {
            sdleList.put(activityList.get(i), newSDLEList(i));
        }


    }

    public void readSDLEFile(int timeInterval, int option, double rh, double beta) {


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
        for (int i = 0; i < activityList.size(); i++) {
            int id = 0;
            String fileString = inputFile.toString() + activityList.get(i) + "/" + id + ".txt";
            File file = new File(fileString);

            while (file.exists()) {

                try {
                    FileReader fr = new FileReader(file);
                    BufferedReader br = new BufferedReader(fr);
                    String line;
                    if (!instanceLabel.containsKey(activityList.get(i))) {
                        instanceLabel.put(activityList.get(i), new ArrayList<>());
                        sdleList.put(activityList.get(i), new ArrayList<>());
                        for (int j = 0; j < weekSDLEList.size(); j++) {
                            weekSDLEList.get(j).put(activityList.get(i), new ArrayList<>());
                        }
                    }
                    instanceLabel.get(activityList.get(i)).add(new ArrayList<>());
                    sdleList.get(activityList.get(i)).add(new SDLE(rh, beta, sdleAct));
                    //---------------------Day of Week-----------------//
                    for (int j = 0; j < weekSDLEList.size(); j++) {
                        weekSDLEList.get(j).get(activityList.get(i)).add(new SDLE(rh, beta, sdleAct));
                    }
                    //------------------------------------------------//
                    while ((line = br.readLine()) != null) {
                        instanceLabel.get(activityList.get(i)).get(id).add(line);
                    }
                    fr.close();
                    br.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                id++;
                fileString = inputFile.toString() + activityList.get(i) + "/" + id + ".txt";
                file = new File(fileString);
            }
        }

    }

    public void writeSDLE() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < activityList.size(); i++) {
            for (int j = 0; j < sdleList.get(activityList.get(i)).size(); j++) {
                if (j == 0) {
                    sb.append(sdleList.get(activityList.get(i)).get(j).getDistribution().get(0));
                } else {
                    sb.append("," + sdleList.get(activityList.get(i)).get(j).getDistribution().get(0));
                }
            }
            sb.append("\n");
            try {
                FileWriter fw = new FileWriter("report/SDLE/" + activityList.get(i) + ".txt");
                fw.write(sb.toString());
                fw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            sb.setLength(0);
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


   /* private ArrayList<ArrayList<SDLE>> newWeekSDLEList(int weekDays) {
        ArrayList<ArrayList<SDLE>> weekSDLEList = new ArrayList<>();
        for (int i = 0; i < weekDays; i++) {
            weekSDLEList.add(newSDLEList());

        }
        return weekSDLEList;
    }*/

    private ArrayList<SDLE> newSDLEList(int idx) {
        ArrayList<SDLE> newSDLEList = new ArrayList<>();
        for (int i = 0; i < sdleList.get(activityList.get(idx)).size(); i++) {
            newSDLEList.add(new SDLE(rh, beta, sdleAct));
        }
        return newSDLEList;
    }


}
