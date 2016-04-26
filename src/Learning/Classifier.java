package Learning;

import DataStructure.ActivityInstance;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.BayesNet;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by MingJe on 2016/4/18.
 */
public class Classifier {

    private weka.classifiers.Classifier cModel;
    private String fileName;
    private String Topic;

    public Classifier(String topic, String fileName) {
        this.Topic = topic;
        this.fileName = fileName;
    }


    public Classifier(String topic) {
        this.Topic = topic;
    }

    public void train(String fileName) {

        try {
            if (fileName != null) this.fileName = fileName;
            Instances trainingData = new Instances(new BufferedReader(new FileReader("report/features/" + this.fileName + ".arff")));
            trainingData.setClassIndex(trainingData.numAttributes() - 1);
            cModel = new BayesNet();
            cModel.buildClassifier(trainingData);
            Evaluation eval = new Evaluation(trainingData);
            System.out.println(Topic);
            eval.evaluateModel(cModel, trainingData);
            System.out.println(eval.toSummaryString("\nResults\n======\n", false));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public String predict(ActivityInstance currentActivity) {
        DenseInstance act = new DenseInstance(2);
        //act.setValue();
        //cModel.classifyInstance();
        return null;
    }


    public void saveModel() {
        try {
            weka.core.SerializationHelper.write("report/model/" + fileName + ".rModel", cModel);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveModel(String fileName) {
        try {
            weka.core.SerializationHelper.write(fileName, cModel);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadModel(String fileName) {
        try {
            cModel = (weka.classifiers.Classifier) weka.core.SerializationHelper.read(fileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadModel() {
        try {
            cModel = (weka.classifiers.Classifier) weka.core.SerializationHelper.read("report/model/" + this.fileName + ".cModel");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
