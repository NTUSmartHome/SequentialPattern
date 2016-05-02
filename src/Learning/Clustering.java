package Learning;

import weka.clusterers.ClusterEvaluation;
import weka.clusterers.Clusterer;
import weka.clusterers.EM;
import weka.core.Instances;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by g2525_000 on 2016/4/18.
 */
public class Clustering {
    private weka.clusterers.Clusterer clusterer;
    private String fileName;
    private ArrayList<Integer>[] instanceBelongToCluster;
    private String Topic;
    private double[] mean;
    private double[] stdDev;
    private DateFormat dateFormat = new SimpleDateFormat("HH:mm");

    public Clustering(String topic, String fileName) {
        this.Topic = topic;
        this.fileName = fileName;
    }

    public Clustering(String topic) {
        this.Topic = topic;
    }


    public ArrayList<Integer>[] getInstanceBelongToCluster() {
        return instanceBelongToCluster;
    }

    public String getTopic() {
        return Topic;
    }

    public Clusterer getClusterer() {
        return clusterer;
    }


    public void train(String fileName) {
        try {
            //ArffLoader loader = new ArffLoader();
            //loader.setFile(new File(fileName));
            Instances trainingData = new Instances(new BufferedReader(new FileReader("report/features/" + this.fileName + ".arff")));
            // structure.setClassIndex(structure.numAttributes() - 1);
            if (fileName != null) this.fileName = fileName;
            clusterer = new EM();
            clusterer.buildClusterer(trainingData);

            System.out.println(Topic);
            System.out.println(clusterer);


            //Specify each instance(use index instead) belong to which cluster
            instanceBelongToCluster = new ArrayList[clusterer.numberOfClusters()];
            for (int i = 0; i < instanceBelongToCluster.length; i++) {
                instanceBelongToCluster[i] = new ArrayList<>();
            }
            for (int i = 0; i < trainingData.size(); i++) {
                instanceBelongToCluster[clusterer.clusterInstance(trainingData.get(i))].add(i);
            }
            mean = new double[clusterer.numberOfClusters()];
            stdDev = new double[clusterer.numberOfClusters()];
            calculateMeanNStdDev();
            //getStartTime();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void test(String fileName) {
        //ArffLoader loader = new ArffLoader();
        //loader.setFile(new File(fileName));
        Instances testingData = null;
        try {
            testingData = new Instances(new BufferedReader(new FileReader(fileName)));
            ClusterEvaluation eval = new ClusterEvaluation();
            eval.setClusterer(clusterer);                                   // the cluster to evaluate
            eval.evaluateClusterer(testingData);                                // data to evaluate the clusterer on
            System.out.println(eval.clusterResultsToString() + "\n\n");
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
            weka.core.SerializationHelper.write("report/model/" + fileName + ".sModel", clusterer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadModel() {
        try {
            clusterer = (weka.clusterers.Clusterer) weka.core.SerializationHelper.read("report/model/" + fileName + ".sModel");
            mean = new double[clusterer.numberOfClusters()];
            stdDev = new double[clusterer.numberOfClusters()];
            calculateMeanNStdDev();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadModel(String fileName) {
        try {
            clusterer = (weka.clusterers.Clusterer) weka.core.SerializationHelper.read(fileName);
            mean = new double[clusterer.numberOfClusters()];
            stdDev = new double[clusterer.numberOfClusters()];
            calculateMeanNStdDev();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public double[] getMean() {
        return mean;
    }

    public double[] getStdDev() {
        return stdDev;
    }

    public String[] getStartTime() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < mean.length; i++) {
            if (i > 0) sb.append(",");

            sb.append(dateFormat.format(new Date(((int) (mean[i] - 0.5 * stdDev[i])) * 60000)) + "~"
                    + dateFormat.format(new Date(((int) (mean[i] + 0.5 * stdDev[i])) * 60000)));
        }
        String[] result = sb.toString().split(",");
        return result;
    }

    private void calculateMeanNStdDev() {
        String result = clusterer.toString();
        int meanIDX = result.indexOf("mean");
        int stdDevIDX = result.indexOf("std.");
        String test = result.substring(meanIDX + 4, stdDevIDX - 1);
        String[] allMean = test.split("\\s+");
        String[] allStdDev = result.substring(stdDevIDX + 9).split("\\s+");
        for (int i = 0; i < mean.length; i++) {
            mean[i] = Double.parseDouble(allMean[i + 1]);
            stdDev[i] = Double.parseDouble(allStdDev[i + 1]);
        }

    }

    // public ArrayList<>
}
