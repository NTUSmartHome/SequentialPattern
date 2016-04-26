import DataStructure.ActivityInstance;
import Learning.Classifier;
import Learning.Clustering;
import Learning.WekaRegression;
import tool.ActivityInstanceParser;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by g2525_000 on 2016/4/21.
 */
public class TestLifePattern {
    //Use GBRT to learn the relation between start time and duration. And this model use to infer activity duration
    Map<String, ArrayList<WekaRegression>> regressors;
    //Use BayesNet to train relationship between activity
    Classifier relationer;
    //Use Expectation Maximization to cluster each activity start time
    Map<String, Clustering> activityStartTimeClusterer;
    //Store activity name
    ArrayList<String> activityList;

    public TestLifePattern() {
        regressors = loadRegressors();
        activityStartTimeClusterer = loadActivityStartTimeClusterer();
        relationer = loadRelationer();
    }

    public TestLifePattern(boolean isTrain) {
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
            regressors = loadRegressors();
            activityStartTimeClusterer = loadActivityStartTimeClusterer();
            relationer = loadRelationer();
        }
    }

    public Map<String, Clustering> loadActivityStartTimeClusterer(){
        String fileParentFolder = "report/model/ActivityStartTime";
        File folder = new File(fileParentFolder);
        String[] fileList = folder.list();
        Map<String, Clustering> activityStartTimeClusterer =new HashMap<>();

        for (int i = 0; i < fileList.length; i++) {
            String[] tmp = fileList[i].split("\\.");
            Clustering clustering = new Clustering(tmp[0]);
            clustering.loadModel(fileParentFolder + "/" + fileList[i]);
            activityStartTimeClusterer.put(tmp[0], clustering);
        }

        return activityStartTimeClusterer;
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

    public String nextActivity(ActivityInstance currentActivity) {
        //relationer.predict()
        return null;
    }

    public static void main(String[] args) throws IOException, ParseException {
        TestLifePattern testLifePattern = new TestLifePattern(true);
        Map<String, Integer> resultMap = new HashMap<>();
        for (int i = 0; i < 7; i++) {
            resultMap.put(String.valueOf(i), 0);
        }
        ArrayList<ActivityInstance>[][] total = ActivityInstanceParser.original(84, resultMap);
        ArrayList<ActivityInstance>[] weekActivityInstances = total[0];
        ArrayList<ActivityInstance>[] testWeekActivityInstances = total[1];
        for (int i = 0; i < testWeekActivityInstances[0].size(); i++) {

        }



    }
}
