


import com.datumbox.common.dataobjects.Dataset;
import com.datumbox.common.dataobjects.Record;
import com.datumbox.common.persistentstorage.ConfigurationFactory;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConfiguration;
import com.datumbox.common.utilities.PHPfunctions;
import com.datumbox.common.utilities.RandomGenerator;
import com.datumbox.framework.machinelearning.datatransformation.XYMinMaxNormalizer;
import com.datumbox.framework.machinelearning.regression.MatrixLinearRegression;

import java.util.Map;

/**
 * Created by MingJe on 2016/2/4.
 */

public class DPMM {

    public static void regression(ArrayList<>) {
        //Initialization
        //--------------
        RandomGenerator.setGlobalSeed(42L); //optionally set a specific seed for all Random objects
        DatabaseConfiguration dbConf = ConfigurationFactory.INMEMORY.getConfiguration(); //in-memory maps
        //DatabaseConfiguration dbConf = ConfigurationFactory.MAPDB.getConfiguration(); //mapdb maps




        Dataset trainingDataset = new Dataset(dbConf);

        Dataset testingDataset = trainingDataset.copy();


        //Transform Dataset
        //-----------------

        //Normalize continuous variables
        XYMinMaxNormalizer dataTransformer = new XYMinMaxNormalizer("LaborStatistics", dbConf);
        dataTransformer.fit_transform(trainingDataset, new XYMinMaxNormalizer.TrainingParameters());


        //Fit the regressor
        //-----------------

        MatrixLinearRegression regressor = new MatrixLinearRegression("LaborStatistics", dbConf);

        MatrixLinearRegression.TrainingParameters param = new MatrixLinearRegression.TrainingParameters();

        regressor.fit(trainingDataset, param);

        //Denormalize trainingDataset (optional)
        dataTransformer.denormalize(trainingDataset);


        //Use the regressor
        //------------------

        //Apply the same data transformations on testingDataset
        dataTransformer.transform(testingDataset);

        //Get validation metrics on the training set
        MatrixLinearRegression.ValidationMetrics vm = regressor.validate(testingDataset);
        regressor.setValidationMetrics(vm); //store them in the model for future reference

        //Denormalize testingDataset (optional)
        dataTransformer.denormalize(testingDataset);

        System.out.println("Results:");
        for (Integer rId : testingDataset) {
            Record r = testingDataset.get(rId);
            System.out.println("Record " + rId + " - Real Y: " + r.getY() + ", Predicted Y: " + r.getYPredicted());
        }

        System.out.println("Regressor Statistics: " + PHPfunctions.var_export(vm));


        //Clean up
        //--------

        //Erase data transformer, featureselector and regressor.
        dataTransformer.erase();
        regressor.erase();

        //Erase datasets.
        trainingDataset.erase();
        testingDataset.erase();

    }

    public static Map<String, Integer> oldTrain(String file, double alpha, double alphaWords, int iter) {
        /*RandomValue.randomGenerator = new Random(42);

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
        return resultMap;*/
        return null;
    }

    /*private static Dataset generateDatasetFeature(String filename) {
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
    }*/
}
