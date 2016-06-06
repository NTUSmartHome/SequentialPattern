package Pattern;

import DataStructure.ActivityInstance;
import Learning.Classifier;
import Learning.Clustering;
import Learning.WekaRegression;
import SDLE.SDLE;
import sun.util.resources.LocaleData;
import tool.ActivityInstanceParser;
import tool.LogPreProcessing;

import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Created by g2525_000 on 2016/4/21.
 */
public class TestLifePattern {
    final LocalDate startDay = LocalDate.of(2010, 11,4);
    final LocalDate endDay = LocalDate.of(2011, 6,11);

    final double rh = 0.05, beta = 0.01;
    //Use GBRT to learn the relation between start time and duration. And this model use to infer activity duration
    Map<String, ArrayList<WekaRegression>> regressors;
    //Use BayesNet to train relationship between activity
    Classifier relationer;
    List<Classifier> multipleRelationer;
    //Use Expectation Maximization to cluster each activity start time
    Map<String, Clustering> activityStartTimeClusterer;
    //Store activity name
    ArrayList<String> activityList;
    //Transform start time
    DateFormat dateFormat;


    List<String> sdleAct = new ArrayList<>();
    ArrayList<Map<String, ArrayList<SDLE>>> weekSDLEList;
    Map<String, ArrayList<SDLE>> sdleList;
    Map<String, ArrayList<ArrayList<String>>> instanceLabel;

    public TestLifePattern() {
        activityList = new ArrayList<>();
        regressors = loadRegressors();
        activityStartTimeClusterer = loadActivityStartTimeClusterer();
        relationer = loadRelationer();
        dateFormat = new SimpleDateFormat("HH:mm");
        durationSetting();

        sdleList = new HashMap<>();
        instanceLabel = new HashMap<>();
        weekSDLEList = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            weekSDLEList.add(new HashMap<>());
        }
        sdleAct.add("true");
        sdleAct.add("false");
        readSDLEFile(5, 1, rh, beta);
    }

    public TestLifePattern(boolean isTrain) {
        dateFormat = new SimpleDateFormat("HH:mm");
        if (isTrain) {
            try {
                TrainLifePattern trainLifePattern = new TrainLifePattern();
                regressors = trainLifePattern.regressors;
                relationer = trainLifePattern.relationer;
                activityStartTimeClusterer = trainLifePattern.activityStartTimeClusterer;
                activityList = trainLifePattern.activityList;
                sdleList = new HashMap<>();
                instanceLabel = new HashMap<>();
                weekSDLEList = new ArrayList<>();
                for (int i = 0; i < 7; i++) {
                    weekSDLEList.add(new HashMap<>());
                }
                sdleAct.add("true");
                sdleAct.add("false");
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }

        } else {
            activityList = new ArrayList<>();
            regressors = loadRegressors();
            activityStartTimeClusterer = loadActivityStartTimeClusterer();
            relationer = loadRelationer();
            multipleRelationer = loadMultipleRelationer();
            sdleList = new HashMap<>();
            instanceLabel = new HashMap<>();
            weekSDLEList = new ArrayList<>();
            for (int i = 0; i < 7; i++) {
                weekSDLEList.add(new HashMap<>());
            }
            sdleAct.add("true");
            sdleAct.add("false");
        }
        durationSetting();
    }

    public static void main(String[] args) throws IOException, ParseException {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
        TestLifePattern testLifePattern = new TestLifePattern(false);
        //testLifePattern.testRelation();
        testLifePattern.testRelation();

       /* Map<String, Integer> resultMap = new HashMap<>();
        for (int i = 0; i < 7; i++) {
            resultMap.put(String.valueOf(i), 0);
        }
        ArrayList<ActivityInstance>[][] total = ActivityInstanceParser.original(90, resultMap);
        ArrayList<ActivityInstance>[] weekActivityInstances = total[0];
        ArrayList<ActivityInstance>[] testWeekActivityInstances = total[1];

        ActivityInstance last = weekActivityInstances[0].get(weekActivityInstances[0].size() - 2);
        ActivityInstance current = weekActivityInstances[0].get(weekActivityInstances[0].size() - 1);

        String nextActivity = testLifePattern.nextActivity(last, current);
        float right = 0;
        for (int i = 0; i < testWeekActivityInstances[0].size(); i++) {
            String realNextActivity = testWeekActivityInstances[0].get(i).getActivity();
            if (nextActivity.equals(realNextActivity)) {
                right++;
            }
            last = current;
            current = testWeekActivityInstances[0].get(i);
            nextActivity = testLifePattern.nextActivity(last, current);
        }
        System.out.println(right / testWeekActivityInstances[0].size());*/


    }

    public Map<String, Clustering> loadActivityStartTimeClusterer() {
        String fileParentFolder = "report/model/ActivityStartTime";
        File folder = new File(fileParentFolder);
        String[] fileList = folder.list();
        Map<String, Clustering> activityStartTimeClusterer = new HashMap<>();

        for (int i = 0; i < fileList.length; i++) {
            String[] tmp = fileList[i].split("\\.");
            activityList.add(tmp[0]);
            Clustering clustering = new Clustering(tmp[0]);
            clustering.loadModel(fileParentFolder + "/" + fileList[i]);
            activityStartTimeClusterer.put(tmp[0], clustering);
        }

        return activityStartTimeClusterer;
    }

    public List<Classifier> loadMultipleRelationer() {
        List<Classifier> multipleRelationer = new ArrayList<>();
        File file = new File("report/model/ActivityRelationConstruction_0.rModel");
        int i = 0;
        while (file.exists()) {
            Classifier classifier = new Classifier("Relation_" + i);
            classifier.loadModel(file.getPath());
            multipleRelationer.add(classifier);
            file = new File("report/model/ActivityRelationConstruction_" + (++i) + ".rModel");
        }
        return multipleRelationer;
    }

    public Classifier loadRelationer() {
        Classifier classifier = new Classifier("Relation");
        classifier.loadModel("report/model/ActivityRelationConstruction.rModel");
        return classifier;
    }

    public Map<String, ArrayList<WekaRegression>> loadRegressors() {
        String fileParentFolder = "report/model/ActivityDurationEstimation";
        File folder = new File(fileParentFolder);
        String[] fileList = folder.list();
        Map<String, ArrayList<WekaRegression>> regressors = new HashMap();
        for (int i = 0; i < fileList.length; i++) {
            String[] tmp = fileList[i].split("-");
            String activity = tmp[0];
            ArrayList<WekaRegression> tmpMap = regressors.get(activity);
            if (tmpMap == null) {
                regressors.put(activity, new ArrayList<>());
                tmpMap = regressors.get(activity);
            }
            WekaRegression wekaRegression = new WekaRegression(activity);
            wekaRegression.loadModel(fileParentFolder + "/" + fileList[i]);
            tmpMap.add(wekaRegression);
        }

        return regressors;
    }

    public String nextActivity(ActivityInstance lastActivity, ActivityInstance currentActivity) {
        Map<String, Double> relationPredict = relationer.predict(lastActivity, currentActivity);
        int activityIdx = 0;
        double max = relationPredict.get(activityList.get(0));
        for (int i = 0; i < activityList.size(); i++) {
            double tmp = relationPredict.get(activityList.get(i));
            if (tmp > max) {
                max = tmp;
                activityIdx = i;
            }
        }

        return activityList.get(activityIdx);
    }

    public String nextActivity(int idx, ActivityInstance lastActivity, ActivityInstance currentActivity) {
        Map<String, Double> startTimePredict = new HashMap<>();
        for (int i = 0; i < activityList.size(); i++) {
            // Date date = dateFormat.parse(currentActivity.getStartTime());
            startTimePredict.put(activityList.get(i),
                    activityStartTimeClusterer.get(activityList.get(i)).predict(currentActivity.getEndTime()));
        }
        Map<String, Double> relationPredict = multipleRelationer.get(idx).predict(lastActivity, currentActivity);
        int activityIdx = 0;
        double max = relationPredict.get(activityList.get(0)) + 1 / startTimePredict.get(activityList.get(0));
        for (int i = 1; i < activityList.size(); i++) {
            double tmp = relationPredict.get(activityList.get(i)) + 1 / startTimePredict.get(activityList.get(i));
            if (tmp > max) {
                max = tmp;
                activityIdx = i;
            }
        }

        return activityList.get(activityIdx);
    }
    //Only single relation(sequential) prediction

    //Only multiple relation(sequential) prediction
    public String sequentialNextActivity(int idx, ActivityInstance lastActivity, ActivityInstance currentActivity) {
        Map<String, Double> relationPredict = multipleRelationer.get(idx).predict(lastActivity, currentActivity);
        int activityIdx = 0;
        double max = relationPredict.get(activityList.get(0));
        for (int i = 1; i < activityList.size(); i++) {
            double tmp = relationPredict.get(activityList.get(i));
            if (tmp > max) {
                max = tmp;
                activityIdx = i;
            }
        }

        return activityList.get(activityIdx);
    }

    public String sequentialNextActivity(ActivityInstance lastActivity, ActivityInstance currentActivity) {
        Map<String, Double> relationPredict = relationer.predict(lastActivity, currentActivity);
        int activityIdx = 0;
        double max = relationPredict.get(activityList.get(0));
        for (int i = 1; i < activityList.size(); i++) {
            double tmp = relationPredict.get(activityList.get(i));
            if (tmp > max) {
                max = tmp;
                activityIdx = i;
            }
        }

        return activityList.get(activityIdx);
    }

    //Only start time(periodic) predict
    public String periodicNextActivity(int idx, ActivityInstance lastActivity, ActivityInstance currentActivity) {
        Map<String, Double> startTimePredict = new HashMap<>();
        for (int i = 0; i < activityList.size(); i++) {
            // Date date = dateFormat.parse(currentActivity.getStartTime());
            startTimePredict.put(activityList.get(i),
                    activityStartTimeClusterer.get(activityList.get(i)).predict(currentActivity.getEndTime()));
        }

        int activityIdx = 0;
        double max = 1 / startTimePredict.get(activityList.get(0));
        for (int i = 1; i < activityList.size(); i++) {
            double tmp = 1 / startTimePredict.get(activityList.get(i));
            if (tmp > max) {
                max = tmp;
                activityIdx = i;
            }
        }

        return activityList.get(activityIdx);
    }

    public Classifier getRelationer() {
        return relationer;
    }

    public Map<String, ArrayList<WekaRegression>> getRegressors() {
        return regressors;
    }

    public Map<String, Clustering> getActivityStartTimeClusterer() {
        return activityStartTimeClusterer;
    }

    public ArrayList<String> getActivityList() {
        return activityList;
    }

    public DateFormat getDateFormat() {
        return dateFormat;
    }

    private int whichSegment(String[] startTime) {
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

        return segment;
    }

    private void testMultipleRelation() throws IOException, ParseException {
        Map<String, Integer> resultMap = new HashMap<>();
        for (int i = 0; i < 7; i++) {
            resultMap.put(String.valueOf(i), 0);
        }
        ArrayList<ActivityInstance>[][] total = ActivityInstanceParser.original(90, resultMap);
        ArrayList<ActivityInstance>[] weekActivityInstances = total[0];
        ArrayList<ActivityInstance>[] testWeekActivityInstances = total[1];

        while (true) {
            int size = testWeekActivityInstances[0].size();
            testWeekActivityInstances[0] = LogPreProcessing.preProcessing(testWeekActivityInstances[0]);
            if (size == testWeekActivityInstances[0].size())
                break;
        }

        ActivityInstance last = weekActivityInstances[0].get(weekActivityInstances[0].size() - 2);
        ActivityInstance current = weekActivityInstances[0].get(weekActivityInstances[0].size() - 1);

        String[] startTime = current.getStartTime().split(":");
        int segment = whichSegment(startTime);

        String nextActivity = sequentialNextActivity(segment, last, current);
        float right = 0;
        for (int i = 0; i < testWeekActivityInstances[0].size(); i++) {
            String realNextActivity = testWeekActivityInstances[0].get(i).getActivity();
            if (nextActivity.equals(realNextActivity)) {
                right++;
            }
            last = current;
            current = testWeekActivityInstances[0].get(i);
            startTime = current.getStartTime().split(":");
            segment = whichSegment(startTime);
            nextActivity = sequentialNextActivity(segment, last, current);
        }
        System.out.println(right / testWeekActivityInstances[0].size());
    }

    private void testRelation() throws IOException, ParseException {

        Map<String, Integer> resultMap = new HashMap<>();
        for (int i = 0; i < 7; i++) {
            resultMap.put(String.valueOf(i), 0);
        }
        ArrayList<ActivityInstance>[][] total = ActivityInstanceParser.original(90, resultMap);
        ArrayList<ActivityInstance>[] weekActivityInstances = total[0];
        ArrayList<ActivityInstance>[] testWeekActivityInstances = total[1];

        while (true) {
            int size = testWeekActivityInstances[0].size();
            testWeekActivityInstances[0] = LogPreProcessing.preProcessing(testWeekActivityInstances[0]);
            if (size == testWeekActivityInstances[0].size())
                break;
        }


        ActivityInstance last = weekActivityInstances[0].get(weekActivityInstances[0].size() - 2);
        ActivityInstance current = weekActivityInstances[0].get(weekActivityInstances[0].size() - 1);

        String nextActivity = sequentialNextActivity(last, current);
        float right = 0;
        for (int i = 0; i < testWeekActivityInstances[0].size(); i++) {
            String realNextActivity = testWeekActivityInstances[0].get(i).getActivity();
            if (nextActivity.equals(realNextActivity)) {
                right++;
            }
            last = current;
            current = testWeekActivityInstances[0].get(i);
            nextActivity = sequentialNextActivity(last, current);
        }
        System.out.println(right / testWeekActivityInstances[0].size());
    }


    private void durationSetting() {
        for (int i = 0; i < activityList.size(); i++) {
            ArrayList<WekaRegression> durations = regressors.get(activityList.get(i));
            Clustering allStartTime = activityStartTimeClusterer.get(activityList.get(i));
            for (int j = 0; j < allStartTime.getMean().length; j++) {
                double[] means = allStartTime.getMean();
                double[] stdDev = allStartTime.getStdDev();
                long durationShort = durations.get(j).predict(String.valueOf(means[j] - stdDev[j]));
                long durationLong = durations.get(j).predict(String.valueOf(means[j] + stdDev[j]));
                if (durationShort > durationLong) {
                    long tmp = durationLong;
                    durationLong = durationShort;
                    durationShort = tmp;
                }
                String durationShortString;
                String durationLongString;
                durationShortString = durationShort / 60 + " Hour and " + durationShort % 60 + " minute";
                durationLongString = durationLong / 60 + " Hour and " + durationLong % 60 + " minute";
                durations.get(j).setDuration(durationShortString + " ~ " + durationLongString);
            }

        }
    }

    private void readSDLEFile(int timeInterval, int option, double rh, double beta) {


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

    public void SDLEAccumulate(int trainedDays) {
        for (int i = 0; i < activityList.size(); i++) {
            sdleList.put(activityList.get(i), newSDLEList(i));
        }
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

    }
    public void SDLEAccumulate(LocalDate startDay, LocalDate endDay) {
        for (int i = 0; i < activityList.size(); i++) {
            sdleList.put(activityList.get(i), newSDLEList(i));
        }
        long start = ChronoUnit.DAYS.between(this.startDay, startDay);
        long end = ChronoUnit.DAYS.between(endDay, this.endDay);

        //int realTrainedDays = Math.min(trainedDays, instanceLabel.get(activityList.get(0)).get(0).size());
        for (int i = 0; i < activityList.size(); i++) {
            for (int j = 0; j < instanceLabel.get(activityList.get(i)).size(); j++) {
                for (long k = start; k < instanceLabel.get(activityList.get(0)).get(0).size() - end; k++) {
                    String[] acts = instanceLabel.get(activityList.get(i)).get(j).get((int) k).split(",");
                    sdleList.get(activityList.get(i)).get(j).parameterUpdating(acts);
                }
            }
        }
        System.out.println("SDLE accumulate " + ChronoUnit.DAYS.between(startDay, endDay) + " days");
        writeSDLE();

    }


    private ArrayList<SDLE> newSDLEList(int idx) {
        ArrayList<SDLE> newSDLEList = new ArrayList<>();
        for (int i = 0; i < sdleList.get(activityList.get(idx)).size(); i++) {
            newSDLEList.add(new SDLE(rh, beta, sdleAct));
        }
        return newSDLEList;
    }

    private void writeSDLE() {
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

    public Map<String, ArrayList<SDLE>> getSdleList() {
        return sdleList;
    }

    public double KLDivergence(ArrayList<SDLE> x, ArrayList<SDLE> y) {
        double KLD = 0;
        for (int i = 0; i < x.size(); i++) {
            KLD += x.get(i).getDistribution().get(0) * Math.log(x.get(i).getDistribution().get(0) / y.get(i).getDistribution().get(0));
        }
        return KLD;
    }
}
