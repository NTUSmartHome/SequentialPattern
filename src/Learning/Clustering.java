package Learning;

import org.apache.commons.lang3.ArrayUtils;
import weka.clusterers.ClusterEvaluation;
import weka.clusterers.Clusterer;
import weka.clusterers.EM;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.core.pmml.Array;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

/**
 * Created by g2525_000 on 2016/4/18.
 */
public class Clustering {
    private weka.clusterers.EM clusterer;
    private String fileName;
    private ArrayList<Integer>[] instanceBelongToCluster;
    private String Topic;
    private double[] mean;
    private double[] stdDev;
    private DateFormat dateFormat = new SimpleDateFormat("HH:mm");
    private ArrayList<Integer> outlier;

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

    /*public double predict(String startTime) {
        ArrayList<Attribute> attributes = new ArrayList<>();
        attributes.add(new Attribute("startTime", Attribute.NUMERIC));
        Instances predictSet = new Instances("predict", attributes, 0);
        predictSet.setClassIndex(0);
        DenseInstance inst = new DenseInstance(1);
        String[] time = startTime.split(":");
        int timeInt = Integer.parseInt(time[0]) * 60 + Integer.parseInt(time[1]);
        inst.setDataset(predictSet);
        inst.setValue(attributes.get(0), timeInt);
        double[] prd;
        try {
            prd = clusterer.distributionForInstance(inst);
            return Collections.max(Arrays.asList(ArrayUtils.toObject(prd)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }*/
    public double predict(String startTime) {
        String[] time = startTime.split(":");
        int timeInt = Integer.parseInt(time[0]) * 60 + Integer.parseInt(time[1]);
        double min = Math.abs(timeInt-mean[0]) ;
        for (int i = 1; i < mean.length; i++) {
            double tmp = Math.abs(timeInt-mean[i]);
            if (Double.compare(tmp, min) < 0) {
                min = tmp;
            }
        }
        return min;
    }
    public void train(String fileName) {
        try {
            //ArffLoader loader = new ArffLoader();
            //loader.setFile(new File(fileName));
            Instances trainingData = new Instances(new BufferedReader(new FileReader("report/features/" + this.fileName + ".arff")));
            // structure.setClassIndex(structure.numAttributes() - 1);
            if (fileName != null) this.fileName = fileName;
            clusterer = new EM();
            clusterer.setMaxIterations(500);
            clusterer.setSeed(trainingData.size() / 4 +  (int)(Math.random()*10));
            //clusterer.setMaximumNumberOfClusters(4);
            clusterer.buildClusterer(trainingData);

            double[][][] modelsNumericAtts = clusterer.getClusterModelsNumericAtts();

            System.out.println(Topic);
            System.out.println(clusterer);

            outlier = new ArrayList();
            for (int i = 0; i < trainingData.size(); i++) {
                int cluster = clusterer.clusterInstance(trainingData.get(i));
                if (modelsNumericAtts[cluster][0][2] / trainingData.size() <= 0.05) {
                    outlier.add(i);
                }
            }
            Collections.sort(outlier);
            for (int i = outlier.size() - 1; i >= 0; i--) {
                trainingData.remove((int) outlier.get(i));
            }
            if (outlier.size() > 0) {
                clusterer.buildClusterer(trainingData);

                System.out.println(Topic);
                System.out.println(clusterer);
            }

            modelsNumericAtts = clusterer.getClusterModelsNumericAtts();
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
            for (int i = 0; i < mean.length; i++) {
                mean[i] = modelsNumericAtts[i][0][0];
                stdDev[i] = modelsNumericAtts[i][0][1];
            }
            //calculateMeanNStdDev();
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
            clusterer = (weka.clusterers.EM) weka.core.SerializationHelper.read("report/model/" + fileName + ".sModel");
            mean = new double[clusterer.numberOfClusters()];
            stdDev = new double[clusterer.numberOfClusters()];
            calculateMeanNStdDev();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadModel(String fileName) {
        try {
            clusterer = (weka.clusterers.EM) weka.core.SerializationHelper.read(fileName);
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

            sb.append(dateFormat.format(new Date(((int) (mean[i] - stdDev[i])) * 60000)) + "~"
                    + dateFormat.format(new Date(((int) (mean[i] + stdDev[i])) * 60000)));
        }
        String[] result = sb.toString().split(",");
        return result;
    }

    public ArrayList<Integer> getOutlier() {
        return outlier;
    }

    private void calculateMeanNStdDev() throws Exception {

        double[][][] modelsNumericAtts = clusterer.getClusterModelsNumericAtts();
        mean = new double[clusterer.numberOfClusters()];
        stdDev = new double[clusterer.numberOfClusters()];
        for (int i = 0; i < mean.length; i++) {
            mean[i] = modelsNumericAtts[i][0][0];
            stdDev[i] = modelsNumericAtts[i][0][1];
        }
        /*String result = clusterer.toString();
        int meanIDX = result.indexOf("mean");
        int stdDevIDX = result.indexOf("std.");
        String test = result.substring(meanIDX + 4, stdDevIDX - 1);
        String[] allMean = test.split("\\s+");
        String[] allStdDev = result.substring(stdDevIDX + 9).split("\\s+");
        for (int i = 0; i < mean.length; i++) {
            mean[i] = Double.parseDouble(allMean[i + 1]);
            stdDev[i] = Double.parseDouble(allStdDev[i + 1]);
        }*/

    }

    // public ArrayList<>
}
