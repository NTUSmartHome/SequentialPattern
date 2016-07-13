package Learning;

import DataStructure.Mean;
import weka.classifiers.Classifier;
import weka.classifiers.evaluation.Evaluation;
import weka.classifiers.meta.AdditiveRegression;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Created by g2525_000 on 2016/4/18.
 */
public class WekaRegression {
    private weka.classifiers.Classifier regressor;
    private double mean;
    private double variance;
    private String fileName;
    private String duration;
    private Evaluation eval;
    private ArrayList<Integer>[] instanceBelongToCluster;
    private String Topic;
    private String idx;
    private DateFormat dateFormat = new SimpleDateFormat("HH:mm");
    private double normalError;

    public WekaRegression(String activity, String fileName) {
        this.Topic = activity;
        this.fileName = fileName;
    }

    public WekaRegression(String activity) {
        this.Topic = activity;
    }

    public ArrayList<Integer>[] getInstanceBelongToCluster() {
        return instanceBelongToCluster;
    }

    public String getTopic() {
        return Topic;
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
            eval = new Evaluation(trainingData);
            eval.evaluateModel(regressor, trainingData);
            System.out.println(eval.toSummaryString("\nResults\n======\n", false));
            double[] durations = new double[trainingData.size()];
            for (int i = 0; i < trainingData.size(); i++) {
                durations[i] = trainingData.get(i).value(1);
            }
            mean = Mean.staticMean(durations);
            variance = Mean.staticVariance(durations);
            double meanError = 0;
            for (int i = 0; i < trainingData.size(); i++) {
                meanError += Math.abs(mean - trainingData.get(i).value(1));
            }
            normalError = meanError;
            System.out.println("meanError : " + meanError / trainingData.size());
        } catch (IOException e) {
            System.out.println(e);
            //e.printStackTrace();
        } catch (Exception e) {
            System.out.println(e);
            // e.printStackTrace();
        }
    }

    public boolean isFeatureExist() {
        File file = new File("report/features/" + this.fileName + ".arff");
        return file.exists();
    }

    public long predict(String startTime) {
        String[] time = startTime.split(":");
        int timeInt = Integer.parseInt(time[0]) * 60 + Integer.parseInt(time[1]);
        ArrayList<Attribute> attributes = new ArrayList<>();
        attributes.add(new Attribute(String.valueOf("startTime")));
        attributes.add(new Attribute(String.valueOf("duration")));
        Instances predictSet = new Instances("train", attributes, 0);
        predictSet.setClassIndex(predictSet.numAttributes() - 1);
        Instance inst = new DenseInstance(predictSet.numAttributes());
        inst.setDataset(predictSet);
        inst.setValue(0, timeInt);
        double result = 0;
        try {
            result = regressor.classifyInstance(inst);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Math.round(result);
    }

    public boolean isAnomaly(double duration) {
        double shortDuration = mean - 2 * variance;
        double longDuration = mean + 2 * variance;
        if (shortDuration < 0) {
            System.out.println("impossible");
            shortDuration = 0;
        }

        return duration < shortDuration || duration > longDuration;
    }

    public void saveModel() {
        try {
            weka.core.SerializationHelper.write("report/model/" + fileName + ".dModel", regressor);
            FileWriter fw = new FileWriter("report/model/" + "Normal" + fileName + ".normalModel");
            fw.write(mean + "\n" + variance);
            fw.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadModel() {
        try {
            String[] kind = fileName.split("-\\.");
            Topic = kind[0];
            idx = kind[1];
            regressor = (Classifier) weka.core.SerializationHelper.read("report/model/" + fileName + ".dModel");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadModel(String fileName) {
        try {
            if (fileName.contains("dModel")) {
                regressor = (Classifier) weka.core.SerializationHelper.read(fileName);
                FileReader fr = new FileReader(fileName.replace(".dModel", ".normalModel").replace("ActivityDuration", "NormalActivityDuration"));
                BufferedReader br = new BufferedReader(fr);
                mean = Double.parseDouble(br.readLine());
                variance = Double.parseDouble(br.readLine());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public Evaluation getEval() {
        return eval;
    }

    public double getNormalError() {
        return normalError;
    }
}
