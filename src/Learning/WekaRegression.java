package Learning;

import weka.classifiers.Classifier;
import weka.classifiers.evaluation.Evaluation;
import weka.classifiers.meta.AdditiveRegression;
import weka.core.Instances;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by g2525_000 on 2016/4/18.
 */
public class WekaRegression {
    private weka.classifiers.Classifier regressor;
    private String fileName;

    public ArrayList<Integer>[] getInstanceBelongToCluster() {
        return instanceBelongToCluster;
    }

    public String getActivity() {
        return Activity;
    }

    private ArrayList<Integer>[] instanceBelongToCluster;
    private String Activity;

    public WekaRegression(String activity, String fileName) {
        this.Activity = activity;
        this.fileName = fileName;
    }

    public Classifier getRegressor() {
        return regressor;
    }


    public void train(String fileName) {
        try {
            //ArffLoader loader = new ArffLoader();
            //loader.setFile(new File(fileName));
            if (fileName != null) this.fileName = fileName;
            Instances trainingData = new Instances(new BufferedReader(new FileReader("report/features/" + this.fileName + ".arff")));
            trainingData.setClassIndex(trainingData.numAttributes() - 1);
            regressor = new AdditiveRegression();
            regressor.buildClassifier(trainingData);
            Evaluation eval = new Evaluation(trainingData);
            eval.evaluateModel(regressor, trainingData);
            System.out.println(eval.toSummaryString("\nResults\n======\n", false));

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isFeatureExist() {
        File file = new File("report/features/" + this.fileName + ".arff");
        return file.exists();
    }

    public void saveModel() {
        try {
            weka.core.SerializationHelper.write("report/model/" + fileName + ".rModel", regressor);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadModel() {
        try {
            regressor = (Classifier) weka.core.SerializationHelper.read("report/model/" + fileName + ".rModel");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
