import Learning.Classifier;
import Learning.Clustering;
import Learning.WekaRegression;
import weka.clusterers.Clusterer;

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
        loadRegressors();
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
            loadRegressors();
        }
    }

    public Map<String, Clustering> loadActivityStartTimeClusterer(){
        File folder = new File("report\\model\\ActivityStartTime");
        String[] fileList = folder.list();
        Map<String, Clustering> activityStartTimeClusterer =new HashMap<>();

        for (int i = 0; i < fileList.length; i++) {
            String[] tmp = fileList[i].split("/.");
            Clustering clustering = new Clustering(tmp[tmp.length - 2]);
            clustering.loadModel(fileList[i]);
            activityStartTimeClusterer.put(tmp[tmp.length - 2], clustering);
        }

        return activityStartTimeClusterer;
    }

    public Classifier loadClassifier() {
        Classifier classifier = new Classifier("Relation");
        classifier.loadModel("report/model/ActivityRelationConstruction.rModel");
        return classifier;
    }

    public Map<String, ArrayList<WekaRegression>> loadRegressors() {
        File folder = new File("report/model/ActivityDurationEstimation");
        String[] fileList = folder.list();
        Map<String, ArrayList<WekaRegression>> regressors = new HashMap();
        for (int i = 0; i < fileList.length; i++) {
            String[] tmp = fileList[i].split("/-");
            String activity = tmp[tmp.length - 2];
            ArrayList<WekaRegression> tmpMap = regressors.get(activity);
            if (tmpMap == null) {
                regressors.put(activity, new ArrayList<>());
                tmpMap = regressors.get(activity);
            }
            WekaRegression wekaRegression = new WekaRegression(activity);
            wekaRegression.loadModel(fileList[i]);
            tmpMap.add(wekaRegression);
        }

        return regressors;
    }

    public static void main(String[] args) {
        TestLifePattern testLifePattern = new TestLifePattern();

    }
}
