package Learning;

import weka.classifiers.bayes.BayesNet;
import weka.core.Instances;
import weka.core.converters.ArffLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * Created by MingJe on 2016/4/18.
 */
public class Classifier {

    private weka.classifiers.Classifier cModel;


    public void train(String fileName, String testFileName) {

        try {
            ArffLoader loader = new ArffLoader();
            loader.setFile(new File("/some/where/data.arff"));
            Instances structure = loader.getStructure();
            structure.setClassIndex(structure.numAttributes() - 1);
            cModel = new BayesNet();
            cModel.buildClassifier(structure);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public String predict(String data) {

        return data;
    }

    public int readModel() {
        if (cModel != null) return 0;
        // Read model
        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(
                    new FileInputStream("Model/ar.model"));
            cModel = (weka.classifiers.Classifier) ois.readObject();
            return 3;
        } catch (IOException e) {
            return 2;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public weka.classifiers.Classifier getcModel() {
        return cModel;
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


}
