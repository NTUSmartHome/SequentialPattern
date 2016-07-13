package Learning;

import DataStructure.ActivityInstance;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.BayesNet;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by MingJe on 2016/4/18.
 */
public class Classifier {
    private Evaluation eval;
    private BayesNet cModel;
    private String fileName;
    private String Topic;
    private ArrayList<Attribute> attributes;

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
            attributes = new ArrayList<>();
            for (int i = 0; i < trainingData.numAttributes(); i++) {
                attributes.add(trainingData.attribute(i));
            }
            trainingData.setClassIndex(trainingData.numAttributes() - 1);
            cModel = new BayesNet();
            cModel.setBIFFile("BayesNet/test1.xml");
            cModel.buildClassifier(trainingData);
            eval = new Evaluation(trainingData);
            System.out.println(Topic);
            eval.evaluateModel(cModel, trainingData);
            System.out.println(eval.toSummaryString("\nResults\n======\n", false));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public Map<String, Double> predict(ActivityInstance preActivity, ActivityInstance currentActivity, ActivityInstance nextActivity) {
        /*Attribute act_last_two = new Attribute("act_last_two");
        Attribute act_last = new Attribute("act_last");
        Attribute startTime = new Attribute("startTime");
        Attribute act = new Attribute("act");*/
        Instances predictSet = new Instances("predict", attributes, 0);
        predictSet.setClassIndex(predictSet.numAttributes() - 1);
        DenseInstance inst = new DenseInstance(predictSet.numAttributes());
        inst.setDataset(predictSet);
        inst.setValue(attributes.get(0), preActivity.getActivity());
        inst.setValue(attributes.get(1), currentActivity.getActivity());

        String[] currentStartTime = currentActivity.getStartTime().split(":");
        int currentStartTimeHour = Integer.parseInt(currentStartTime[0]);
        inst.setValue(attributes.get(2), currentStartTimeHour);

        String[] nextStartTime = nextActivity.getStartTime().split(":");
        int nextStartTimeHour = Integer.parseInt(nextStartTime[0]);
        inst.setValue(attributes.get(3), currentStartTimeHour);

        inst.setValue(attributes.get(4), currentActivity.getDuration());
        inst.setValue(attributes.get(5), currentActivity.getDayOfWeek());


        try {
            //double prd = cModel.classifyInstance(inst);
            //String test = inst.classAttribute().value((int) prd);
            Map<String, Double> resultSet = new HashMap<>();
            double[] distributionForInstance = cModel.distributionForInstance(inst);
            for (int i = 0; i < distributionForInstance.length; i++) {
                resultSet.put(attributes.get(attributes.size() - 1).value(i), distributionForInstance[i]);
            }
            return resultSet;

        } catch (Exception e) {
            e.printStackTrace();
        }
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
            cModel = (BayesNet) weka.core.SerializationHelper.read(fileName);
            Instances trainingData = new Instances(new BufferedReader(new FileReader("report/features/ActivityRelationConstruction.arff")));
            attributes = new ArrayList<>();
            for (int i = 0; i < trainingData.numAttributes(); i++) {
                attributes.add(trainingData.attribute(i));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadModel() {
        try {
            cModel = (BayesNet) weka.core.SerializationHelper.read("report/model/" + this.fileName + ".rModel");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Evaluation getEval() {
        return eval;
    }
}
