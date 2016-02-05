
import com.datumbox.common.dataobjects.Dataset;
import com.datumbox.common.dataobjects.Record;

import com.datumbox.common.utilities.RandomValue;
import com.datumbox.configuration.MemoryConfiguration;
import com.datumbox.framework.machinelearning.clustering.MultinomialDPMM;
import com.datumbox.framework.machinelearning.common.bases.basemodels.BaseDPMM;



import java.io.*;


import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by MingJe on 2016/2/4.
 */

public class DPMM {



    public static Map<String, Integer> oldTrain(String file, double alpha, double alphaWords, int iter) {
        RandomValue.randomGenerator = new Random(42);

        MemoryConfiguration memoryConfiguration = new MemoryConfiguration();
        Dataset trainingData, validationData;
        trainingData = generateDatasetFeature(file);
        validationData = trainingData;

        MultinomialDPMM instance = new MultinomialDPMM("Test");

        MultinomialDPMM.TrainingParameters param = instance.getEmptyTrainingParametersObject();
        param.setAlpha(alpha);
        param.setMaxIterations(iter);
        param.setInitializationMethod(BaseDPMM.TrainingParameters.Initialization.RANDOM_ASSIGNMENT);
        param.setAlphaWords(alphaWords);

        instance.initializeTrainingConfiguration(memoryConfiguration, param);

        instance.train(trainingData, validationData);

        com.datumbox.framework.machinelearning.clustering.MultinomialDPMM.ValidationMetrics VM = instance.getValidationMetrics();

        com.datumbox.framework.machinelearning.clustering.MultinomialDPMM.ModelParameters TrainedParameters = instance.getModelParameters();

        System.out.println("DB name:\t" + instance.getDBname());
        System.out.println("Number of cluster:\t" + TrainedParameters.getC().toString());
        System.out.println("Number of features:\t" + TrainedParameters.getD().toString());
        System.out.println("Number of instances:\t" + TrainedParameters.getN().toString());

        System.out.println("Parameter NMI:\t" + VM.getNMI());
        System.out.println("Parameter Purity:\t" + VM.getPurity());

        instance = null;
        instance = new MultinomialDPMM("Test");

        instance.setMemoryConfiguration(memoryConfiguration);
        instance.predict(validationData);


        Map<String, Integer> resultMap = new HashMap<>();
        resultMap.put("size", instance.getClusters().size());
        int count = 0;
        for (Record r : validationData) {
            Integer clusterId = (Integer) r.getYPredicted();

            resultMap.put(String.valueOf(count++), clusterId);

            System.out.println( "Label: " + r.getY() + ", predict: " + r.getYPredicted());
        }
        return resultMap;
    }

    private static Dataset generateDatasetFeature(String filename) {
        Dataset trainingData = new Dataset();
        try {
            FileReader fr = new FileReader(filename);
            BufferedReader br = new BufferedReader(fr);
            String line = "";
            while ((line = br.readLine()) != null) {
                String[] tmp = line.split(",");
                int featureNum = tmp.length;
                Object[] feature = new Object[featureNum];
                for (int i = 0; i < feature.length; i++) {
                    feature[i] = Math.abs(Double.valueOf(tmp[i]));
                }
                String label = "";
                trainingData.add(Record.newDataVector(feature, label));
            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return trainingData;
    }
}
