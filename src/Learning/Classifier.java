package Learning;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import weka.classifiers.Evaluation;
import weka.classifiers.bayes.BayesNet;
import weka.core.*;

/**
 * Created by MingJe on 2016/4/18.
 */
public class Classifier {
    private ArrayList<Attribute> attributes;
    private weka.classifiers.Classifier cModel;
    private String[] classes;


    private int trainState;

    private Map<String, String> labelMapping;

    public void setTrainState(int trainState) {
        this.trainState = trainState;
    }

    public String train() {

        try {
            trainState = 0;
            // Read training data
            Instances trainingSet = readTrainingData(attributes);
            cModel = new BayesNet();
            trainState = 20;
            // Training
            long startTime = System.currentTimeMillis();
            cModel.buildClassifier(trainingSet);
            //trainState = 40;
            Evaluation eTest = new Evaluation(trainingSet);
            // CrossValidate
            eTest.crossValidateModel(cModel, trainingSet, 10, new Debug.Random(1));
            System.out.println((System.currentTimeMillis() - startTime));
            // Print the statistics result :
            String strSummary = eTest.toSummaryString();
            System.out.println(strSummary);

            // Get the confusion matrix
            StringBuilder confusionMatrix = new StringBuilder();
            double[][] cmMatrix = eTest.confusionMatrix();
            for (int row_i = 0; row_i < cmMatrix.length; row_i++) {
                for (int col_i = 0; col_i < cmMatrix.length; col_i++) {
                    System.out.print(cmMatrix[row_i][col_i]);
                    System.out.print("|");
                    confusionMatrix.append(cmMatrix[row_i][col_i] + "|");
                }
                confusionMatrix.append("\n");
                System.out.println();
            }
            trainState = 90;
            // save the model
            new File("Model/").mkdir();
            weka.core.SerializationHelper.write("Model/ar.model", cModel);
            FileWriter fw = new FileWriter("Model/ar.class");
            fw.write(trainingSet.classAttribute().toString());
            fw.close();
            trainState = 100;
            return confusionMatrix.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

    public Instances readTrainingData(ArrayList<Attribute> attributes) throws Exception {
        Instances trainingSet = new Instances("train", attributes, 0);
        trainingSet.setClassIndex(trainingSet.numAttributes() - 1);
        FileReader fr = new FileReader(new File("trainingData.txt"));
        BufferedReader br = new BufferedReader(fr);
        String dataString;
        while ((dataString = br.readLine()) != null) {
            String[] data = dataString.split("\\s+");
            Instance inst = new DenseInstance(trainingSet.numAttributes() + 1);
            int i = 0;
            for (; i < data.length - 1; i++) {
                try {
                    inst.setValue(attributes.get(i), Double.valueOf(data[i]));
                } catch (Exception e) {
                    inst.setValue(attributes.get(i), data[i]);
                }
            }
            inst.setValue(attributes.get(attributes.size() - 1), data[i]);
            trainingSet.add(inst);
        }
        br.close();
        fr.close();
        return trainingSet;

    }

    public String recognize(String data) {

        // Read model
        readModel();
        // Init instance
        Instances predictSet = new Instances("train", attributes, 0);
        predictSet.setClassIndex(predictSet.numAttributes() - 1);
        String[] datum = data.split("\\s+");
        Instance inst = new DenseInstance(predictSet.numAttributes() + 1);
        inst.setDataset(predictSet);
        for (int i = 0; i < datum.length; i++) {
            try {
                inst.setValue(attributes.get(i), Double.valueOf(datum[i]));
            } catch (Exception e) {
                inst.setValue(attributes.get(i), datum[i]);
            }
        }
        // Predict
        try {
            double prd = cModel.classifyInstance(inst);
            return attributes.get(attributes.size() - 1).value((int) prd);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
        //System.out.println(attributes.get(attributes.size() - 1).value((int) prd));
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

    public int init() {
        // Read feature config
        try {
            attributes = readAtt();
        } catch (IOException e) {
            return 1;
        }
        // Read Label
        labelMapping = labelMappingReader();

        Instances predictSet = new Instances("train", attributes, 0);
        predictSet.setClassIndex(predictSet.numAttributes() - 1);
        return readModel();

    }

    public Map<String, String> labelMappingReader() {
        try {
            FileReader fr = new FileReader(new File("LabelMapping.txt"));
            BufferedReader br = new BufferedReader(fr);
            Map<String, String> labelMapping = new HashMap<>();
            String line;
            while ((line = br.readLine()) != null) {
                String[] labelNMap = line.split("\\s+");
                if (labelNMap.length < 2) continue;
                labelMapping.put(labelNMap[0], labelNMap[1]);
            }
            fr.close();
            br.close();
            return labelMapping;
        } catch (FileNotFoundException e) {
            return new HashMap<>();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String[] getClasses() {
        return classes;
    }

    public ArrayList<Attribute> readAtt() throws IOException {

        FileReader fr = new FileReader("featureConfig.txt");
        BufferedReader br = new BufferedReader(fr);
        ArrayList<Attribute> attributes = new ArrayList<>();
        String att;
        int count = 0;
        while ((att = br.readLine()) != null) {
            if (att.contains("Numeric")) {
                attributes.add(new Attribute(String.valueOf(count)));
            } else if (att.contains("Nominal")) {
                String[] values = att.split(",");
                FastVector fastVector = new FastVector(values.length - 1);
                for (int i = 1; i < values.length; i++) {
                    fastVector.addElement(values[i]);
                }
                attributes.add(new Attribute(String.valueOf(count), fastVector));
            } else if (att.contains("class")) {
                String[] values = att.split(",");
                // Init classes
                classes = new String[values.length - 1];
                FastVector fastVector = new FastVector(values.length - 1);
                for (int i = 1; i < values.length; i++) {
                    fastVector.addElement(values[i]);
                    classes[i - 1] = values[i];

                }
                attributes.add(new Attribute("class", fastVector));
            }
            count++;
        }
        br.close();
        fr.close();
        return attributes;
    }

    public Map<String, String> getLabelMapping() {
        return labelMapping;
    }

    public weka.classifiers.Classifier getcModel() {
        return cModel;
    }


    public ArrayList<Attribute> getAttributes() {
        return attributes;
    }

    public int getTrainState() {
        return trainState;
    }
}
