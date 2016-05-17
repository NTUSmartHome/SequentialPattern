package Pattern;

import DataStructure.ActivityInstance;
import Learning.Classifier;
import Learning.Clustering;
import Learning.WekaRegression;
import tool.ActivityInstanceParser;
import tool.LogPreProcessing;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by g2525_000 on 2016/4/21.
 */
public class TestLifePattern {
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

    public TestLifePattern() {
        activityList = new ArrayList<>();
        regressors = loadRegressors();
        activityStartTimeClusterer = loadActivityStartTimeClusterer();
        relationer = loadRelationer();
        dateFormat = new SimpleDateFormat("HH:mm");
        durationSetting();
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
        }
        durationSetting();
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
        double max  = relationPredict.get(activityList.get(0));
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
        double max =   relationPredict.get(activityList.get(0)) + 1 / startTimePredict.get(activityList.get(0));
        for (int i = 1; i < activityList.size(); i++) {
            double tmp = relationPredict.get(activityList.get(i)) + 1 / startTimePredict.get(activityList.get(i));
            if (tmp > max) {
                max = tmp;
                activityIdx = i;
            }
        }

        return activityList.get(activityIdx);
    }

    //Only multiple relation(sequential) prediction
    public String sequentialNextActivity(int idx, ActivityInstance lastActivity, ActivityInstance currentActivity) {
        Map<String, Double> relationPredict = multipleRelationer.get(idx).predict(lastActivity, currentActivity);
        int activityIdx = 0;
        double max =   relationPredict.get(activityList.get(0));
        for (int i = 1; i < activityList.size(); i++) {
            double tmp = relationPredict.get(activityList.get(i));
            if (tmp > max) {
                max = tmp;
                activityIdx = i;
            }
        }

        return activityList.get(activityIdx);
    }
    //Only single relation(sequential) prediction

    public String sequentialNextActivity(ActivityInstance lastActivity, ActivityInstance currentActivity) {
        Map<String, Double> relationPredict = relationer.predict(lastActivity, currentActivity);
        int activityIdx = 0;
        double max =   relationPredict.get(activityList.get(0));
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
        double max =   1 / startTimePredict.get(activityList.get(0));
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
}
