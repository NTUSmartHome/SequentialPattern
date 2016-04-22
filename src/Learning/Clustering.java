package Learning;

import weka.clusterers.ClusterEvaluation;
import weka.clusterers.Clusterer;
import weka.clusterers.EM;
import weka.core.Instances;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by g2525_000 on 2016/4/18.
 */
public class Clustering {
    private weka.clusterers.Clusterer clusterer;
    private String fileName;
    private ArrayList<Integer>[] instanceBelongToCluster;
    private String Topic;


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
            ClusterEvaluation eval = new ClusterEvaluation();
            eval.setClusterer(clusterer);                                   // the cluster to evaluate
            eval.evaluateClusterer(trainingData);                                // data to evaluate the clusterer on
            System.out.println(Topic);
            System.out.println(eval.clusterResultsToString() + "\n\n");

            //Specify each instance(use index instead) belong to which cluster
            instanceBelongToCluster = new ArrayList[clusterer.numberOfClusters()];
            for (int i = 0; i < instanceBelongToCluster.length; i++) {
                instanceBelongToCluster[i] = new ArrayList<>();
            }
            for (int i = 0; i < trainingData.size(); i++) {
                instanceBelongToCluster[clusterer.clusterInstance(trainingData.get(i))].add(i);
            }

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
            weka.core.SerializationHelper.write("report/model/" + fileName + ".cModel", clusterer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadModel() {
        try {
            clusterer = (weka.clusterers.Clusterer) weka.core.SerializationHelper.read("report/model/" + fileName + ".cModel");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadModel(String fileName) {
        try {
            clusterer = (weka.clusterers.Clusterer) weka.core.SerializationHelper.read(fileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // public ArrayList<>
}
