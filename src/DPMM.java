import com.datumbox.common.dataobjects.AssociativeArray;
import com.datumbox.common.dataobjects.Dataset;
import com.datumbox.common.dataobjects.Record;
import com.datumbox.common.persistentstorage.ConfigurationFactory;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConfiguration;
import com.datumbox.common.utilities.PHPfunctions;
import com.datumbox.common.utilities.RandomGenerator;
import com.datumbox.framework.machinelearning.clustering.GaussianDPMM;
import com.datumbox.framework.machinelearning.clustering.HierarchicalAgglomerative;
import com.datumbox.framework.machinelearning.clustering.MultinomialDPMM;
import com.datumbox.framework.machinelearning.common.bases.basemodels.BaseDPMM;
import com.datumbox.framework.machinelearning.datatransformation.DummyXMinMaxNormalizer;
import com.datumbox.framework.machinelearning.datatransformation.XYMinMaxNormalizer;
import com.datumbox.framework.machinelearning.regression.MatrixLinearRegression;
import com.datumbox.framework.machinelearning.regression.NLMS;
import com.datumbox.framework.machinelearning.regression.StepwiseRegression;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by MingJe on 2016/2/4.
 */

public class DPMM {


    private static BaseDPMM staticCluster;

    public static BaseDPMM getCluster() {
        return staticCluster;
    }

    public static BaseDPMM loadGaussianDPMMModel(String modelName) {
        DatabaseConfiguration dbConf = ConfigurationFactory.INMEMORY.getConfiguration(); //in-memory maps
        staticCluster = new GaussianDPMM(modelName, dbConf);
        return staticCluster;
    }
    public static BaseDPMM loadMultinomialDPMMModel(String modelName) {
        DatabaseConfiguration dbConf = ConfigurationFactory.INMEMORY.getConfiguration(); //in-memory maps
        staticCluster = new MultinomialDPMM(modelName, dbConf);
        return staticCluster;
    }
    public static void MatrixLinearRegression(ArrayList<Record> records) {
        //Initialization
        //--------------
        RandomGenerator.setGlobalSeed(42L); //optionally set a specific seed for all Random objects
        DatabaseConfiguration dbConf = ConfigurationFactory.INMEMORY.getConfiguration(); //in-memory maps
        //DatabaseConfiguration dbConf = ConfigurationFactory.MAPDB.getConfiguration(); //mapdb maps


        Dataset trainingDataset = new Dataset(dbConf);
        for (int i = 0; i < records.size(); i++) {
            trainingDataset.add(records.get(i));
        }
        Dataset testingDataset = trainingDataset.copy();


        //Transform Dataset
        //-----------------

        //Normalize continuous variables
        XYMinMaxNormalizer dataTransformer = new XYMinMaxNormalizer("Regression", dbConf);
        dataTransformer.fit_transform(trainingDataset, new XYMinMaxNormalizer.TrainingParameters());


        //Fit the regressor
        //-----------------

        MatrixLinearRegression regressor = new MatrixLinearRegression("Regression", dbConf);

        MatrixLinearRegression.TrainingParameters param = new MatrixLinearRegression.TrainingParameters();

        regressor.fit(trainingDataset, param);

        //Denormalize trainingDataset (optional)
        //dataTransformer.denormalize(trainingDataset);


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

    public static void StepwiseRegression(ArrayList<Record> records) {
        //Initialization
        //--------------
        RandomGenerator.setGlobalSeed(42L); //optionally set a specific seed for all Random objects
        DatabaseConfiguration dbConf = ConfigurationFactory.INMEMORY.getConfiguration(); //in-memory maps
        //DatabaseConfiguration dbConf = ConfigurationFactory.MAPDB.getConfiguration(); //mapdb maps


        Dataset trainingDataset = new Dataset(dbConf);
        for (int i = 0; i < records.size(); i++) {
            trainingDataset.add(records.get(i));
        }
        Dataset testingDataset = trainingDataset.copy();


        //Transform Dataset
        //-----------------

        //Normalize continuous variables
        //XYMinMaxNormalizer dataTransformer = new XYMinMaxNormalizer("Regression", dbConf);
        //dataTransformer.fit_transform(trainingDataset, new XYMinMaxNormalizer.TrainingParameters());


        //Fit the regressor
        //-----------------

        StepwiseRegression regressor = new StepwiseRegression("Regression", dbConf);

        StepwiseRegression.TrainingParameters param = new StepwiseRegression.TrainingParameters();
        param.setAout(0.05);
        param.setRegressionClass(MatrixLinearRegression.class);


        MatrixLinearRegression.TrainingParameters trainingParams = new MatrixLinearRegression.TrainingParameters();
        param.setRegressionTrainingParameters(trainingParams);
        regressor.fit(trainingDataset, param);

        //Denormalize trainingDataset (optional)
        //dataTransformer.denormalize(trainingDataset);


        //Use the regressor
        //------------------

        //Apply the same data transformations on testingDataset
        //dataTransformer.transform(testingDataset);

        //Get validation metrics on the training set
        StepwiseRegression.ValidationMetrics vm = regressor.validate(testingDataset);
        regressor.setValidationMetrics(vm); //store them in the model for future reference

        //Denormalize testingDataset (optional)
        //dataTransformer.denormalize(testingDataset);

        System.out.println("Results:");
        for (Integer rId : testingDataset) {
            Record r = testingDataset.get(rId);
            System.out.println("Record " + rId + " - Real Y: " + r.getY() + ", Predicted Y: " + r.getYPredicted());
        }

        System.out.println("Regressor Statistics: " + PHPfunctions.var_export(vm));

        //Clean up
        //--------

        //Erase data transformer, featureselector and regressor.
        //dataTransformer.erase();
        regressor.erase();

        //Erase datasets.
        trainingDataset.erase();
        testingDataset.erase();
    }

    public static void NLMS(ArrayList<Record> records) {
        //Initialization
        //--------------
        RandomGenerator.setGlobalSeed(42L); //optionally set a specific seed for all Random objects
        DatabaseConfiguration dbConf = ConfigurationFactory.INMEMORY.getConfiguration(); //in-memory maps
        //DatabaseConfiguration dbConf = ConfigurationFactory.MAPDB.getConfiguration(); //mapdb maps


        Dataset trainingDataset = new Dataset(dbConf);
        for (int i = 0; i < records.size(); i++) {
            trainingDataset.add(records.get(i));
        }
        Dataset testingDataset = trainingDataset.copy();


        //Transform Dataset
        //-----------------

        //Normalize continuous variables
        XYMinMaxNormalizer dataTransformer = new XYMinMaxNormalizer("Regression", dbConf);
        dataTransformer.fit_transform(trainingDataset, new XYMinMaxNormalizer.TrainingParameters());


        //Fit the regressor
        //-----------------

        NLMS regressor = new NLMS("Regression", dbConf);

        NLMS.TrainingParameters param = new NLMS.TrainingParameters();
        param.setTotalIterations(1600);


        regressor.fit(trainingDataset, param);

        //Denormalize trainingDataset (optional)
        dataTransformer.denormalize(trainingDataset);


        //Use the regressor
        //------------------

        //Apply the same data transformations on testingDataset
        dataTransformer.transform(testingDataset);

        //Get validation metrics on the training set
        NLMS.ValidationMetrics vm = regressor.validate(testingDataset);
        regressor.setValidationMetrics(vm); //store them in the model for future reference

        //Denormalize testingDataset (optional)
        dataTransformer.denormalize(testingDataset);
        int errorMinute = 0;
        int count = 0;
        System.out.println("Results:");
        for (Integer rId : testingDataset) {
            Record r = testingDataset.get(rId);
            System.out.println("Record " + rId + " - Real Y: " + r.getY() + ", Predicted Y: " + r.getYPredicted());
            errorMinute += Math.abs((Double) r.getY() - (Double) r.getYPredicted());
            count++;
        }
        System.out.println((double) errorMinute / count);
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

    public static Map<String, Integer> MDPMMTrain(String modelName, String file, double alpha, double alphaWords, int iter) throws IOException {
        //Initialization
        //--------------
        RandomGenerator.setGlobalSeed(42L); //optionally set a specific seed for all Random objects

        DatabaseConfiguration dbConf = ConfigurationFactory.INMEMORY.getConfiguration();//in-memory maps
        //DatabaseConfiguration dbConf = ConfigurationFactory.MAPDB.getConfiguration(); //mapdb maps
        FileReader fr = new FileReader(new File(file));
        BufferedReader br = new BufferedReader(fr);
        String line;
        String[] data;
        Dataset trainingDataset = new Dataset(dbConf);

        while ((line = br.readLine()) != null) {
            data = line.split(",");
            AssociativeArray record = new AssociativeArray();
            for (int i = 0; i < data.length; i++) {
                record.put(i, new Double(data[i]));
            }
            trainingDataset.add(new Record(record, ""));
        }
        Dataset testingDataset = trainingDataset.copy();
        br.close();
        fr.close();
        //Transform Dataset
        //-----------------

        //Convert Categorical variables to dummy variables (boolean) and normalize continuous variables
       // DummyXMinMaxNormalizer dataTransformer = new DummyXMinMaxNormalizer("Test", dbConf);
        //dataTransformer.fit_transform(trainingDataset, new DummyXMinMaxNormalizer.TrainingParameters());


        MultinomialDPMM cluster = new MultinomialDPMM(modelName, dbConf);
        staticCluster = cluster;
        MultinomialDPMM.TrainingParameters param = new MultinomialDPMM.TrainingParameters();
        param.setAlpha(alpha);
        param.setAlphaWords(alphaWords);
        param.setMaxIterations(iter);
        param.setInitializationMethod(BaseDPMM.TrainingParameters.Initialization.ONE_CLUSTER_PER_RECORD);

        cluster.fit(trainingDataset, param);

        //Denormalize trainingDataset (optional)
        //dataTransformer.denormalize(trainingDataset);

        System.out.println("Cluster assignments (Record Ids):");
        for (Map.Entry<Integer, MultinomialDPMM.Cluster> entry : cluster.getClusters().entrySet()) {
            Integer clusterId = entry.getKey();
            MultinomialDPMM.Cluster cl = entry.getValue();

            System.out.println("Cluster " + clusterId + ": " + cl.getRecordIdSet());
        }


        //Use the clusterer
        //-----------------

        //Apply the same transformations on testingDataset
        //dataTransformer.transform(testingDataset);

        //Get validation metrics on the training set
        MultinomialDPMM.ValidationMetrics vm = cluster.validate(testingDataset);
        cluster.setValidationMetrics(vm); //store them in the model for future reference

        //Denormalize testingDataset (optional)
        //dataTransformer.denormalize(testingDataset);


        //Result map
        HashMap<String, Integer> resultMap = new HashMap<>();
        resultMap.put("size", cluster.getClusters().size());
        System.out.println("Results:");
        for (Integer rId : testingDataset) {
            Record r = testingDataset.get(rId);
            System.out.println("Record " + rId + " - Original Y: " + r.getY() + ", Predicted Cluster Id: " + r.getYPredicted());
            resultMap.put(String.valueOf(rId), (Integer) r.getYPredicted());
        }


        System.out.println("Clusterer Statistics: " + PHPfunctions.var_export(vm));


        //Clean up
        //--------

        //Erase data transformer, clusterer.
        //dataTransformer.erase();
        //cluster.erase();

        //Erase datasets.
        trainingDataset.erase();
        testingDataset.erase();
        return resultMap;
    }

    public static Map<String, Integer> DPMMPredict(ArrayList<Record> records) {

        DatabaseConfiguration dbConf = ConfigurationFactory.INMEMORY.getConfiguration();//in-memory maps
        Dataset testingDataset = new Dataset(dbConf);
        for (int i = 0; i < records.size(); i++) {
            testingDataset.add(records.get(i));
        }
        staticCluster.predict(testingDataset);
        //Result map
        HashMap<String, Integer> resultMap = new HashMap<>();
        resultMap.put("size", staticCluster.getClusters().size());
        //System.out.println("Results:");
        for (Integer rId : testingDataset) {
            Record r = testingDataset.get(rId);
            //System.out.println("Record " + rId + " - Original Y: " + r.getY() + ", Predicted Cluster Id: " + r.getYPredicted());
            resultMap.put(String.valueOf(rId), (Integer) r.getYPredicted());
        }

        return resultMap;
    }

    public static Map<String, Integer> OneClusterPredict(ArrayList<Record> records) {


        HashMap<String, Integer> resultMap = new HashMap<>();
        resultMap.put("size", 1);
        //System.out.println("Results:");
        for (int i = 0; i < records.size(); i++) {
            resultMap.put(String.valueOf(i), 0);
        }

        return resultMap;
    }

    public static Map<String, Integer> GDPMMTrain(String modelName, String file, double alpha, double alphaWords, int iter) throws IOException {
        //Initialization
        //--------------
        RandomGenerator.setGlobalSeed(42L); //optionally set a specific seed for all Random objects

        DatabaseConfiguration dbConf = ConfigurationFactory.INMEMORY.getConfiguration();//in-memory maps
        //DatabaseConfiguration dbConf = ConfigurationFactory.MAPDB.getConfiguration(); //mapdb maps
        FileReader fr = new FileReader(new File(file));
        BufferedReader br = new BufferedReader(fr);
        String line;
        String[] data;
        Dataset trainingDataset = new Dataset(dbConf);

        //Reader fileReader = new FileReader(file);
        //Dataset trainingDataset = Dataset.Builder.parseCSVFile(fileReader, "Class", headerDataTypes, ',', '"', "\r\n", dbConf);
        while ((line = br.readLine()) != null) {
            data = line.split(",");
            AssociativeArray record = new AssociativeArray();
            for (int i = 0; i < data.length; i++) {
                record.put(i, new Double(data[i]));
            }
            trainingDataset.add(new Record(record, ""));
        }
        Dataset testingDataset = trainingDataset.copy();
        br.close();
        fr.close();
        //Transform Dataset
        //-----------------

        //Convert Categorical variables to dummy variables (boolean) and normalize continuous variables
        //DummyXMinMaxNormalizer dataTransformer = new DummyXMinMaxNormalizer("Test", dbConf);
        //dataTransformer.fit_transform(trainingDataset, new DummyXMinMaxNormalizer.TrainingParameters());


        GaussianDPMM cluster = new GaussianDPMM(modelName, dbConf);
        staticCluster = cluster;
        GaussianDPMM.TrainingParameters param = new GaussianDPMM.TrainingParameters();
        param.setAlpha(alpha);
        param.setMaxIterations(iter);
        param.setInitializationMethod(BaseDPMM.TrainingParameters.Initialization.ONE_CLUSTER_PER_RECORD);
        param.setKappa0(0);
        param.setNu0(1);
        int varibleNumber = trainingDataset.getVariableNumber();
        param.setMu0(new double[varibleNumber]);
        double[][] Psi0 = new double[varibleNumber][varibleNumber];
        for (int i = 0; i < varibleNumber; i++) {
            for (int j = 0; j < varibleNumber; j++) {
                if (i == j) Psi0[i][j] = 1;
            }
        }
        param.setPsi0(Psi0);
        cluster.fit(trainingDataset, param);

        //Denormalize trainingDataset (optional)
        //dataTransformer.denormalize(trainingDataset);

        System.out.println("Cluster assignments (Record Ids):");
        for (Map.Entry<Integer, GaussianDPMM.Cluster> entry : cluster.getClusters().entrySet()) {
            Integer clusterId = entry.getKey();
            GaussianDPMM.Cluster cl = entry.getValue();

            //System.out.println("Cluster " + clusterId + ": " + cl.getRecordIdSet());
        }


        //Use the clusterer
        //-----------------

        //Apply the same transformations on testingDataset
        //dataTransformer.transform(testingDataset);

        //Get validation metrics on the training set
        GaussianDPMM.ValidationMetrics vm = cluster.validate(testingDataset);
        cluster.setValidationMetrics(vm); //store them in the model for future reference

        //Denormalize testingDataset (optional)
        //dataTransformer.denormalize(testingDataset);


        //Result map
        HashMap<String, Integer> resultMap = new HashMap<>();
        resultMap.put("size", cluster.getClusters().size());
        System.out.println("Results:");
        for (Integer rId : testingDataset) {
            Record r = testingDataset.get(rId);
            System.out.println("Record " + rId + " - Original Y: " + r.getY() + ", Predicted Cluster Id: " + r.getYPredicted());
            resultMap.put(String.valueOf(rId), (Integer) r.getYPredicted());
        }


        System.out.println("Clusterer Statistics: " + PHPfunctions.var_export(vm));


        //Clean up
        //--------

        //Erase data transformer, clusterer.
        //dataTransformer.erase();
        //cluster.erase();

        //Erase datasets.
        trainingDataset.erase();
        testingDataset.erase();
        return resultMap;
    }


    public static Map<String, Integer> oneCluster(String file) throws IOException {
        DatabaseConfiguration dbConf = ConfigurationFactory.INMEMORY.getConfiguration();//in-memory maps
        FileReader fr = new FileReader(new File(file));
        BufferedReader br = new BufferedReader(fr);
        String line;
        int size = 0;


        while ((line = br.readLine()) != null) {
            size++;
        }
        br.close();
        fr.close();
        //Result map
        HashMap<String, Integer> resultMap = new HashMap<>();
        resultMap.put("size", 1);
        System.out.println("Results:");
        for (int i = 0; i < size; i++) {
            resultMap.put(String.valueOf(i), 0);
        }

        return resultMap;
    }

    public static Map<String, Integer> HierarchicalAgglomerativeTrain(String file) throws IOException {
        //Initialization
        //--------------
        RandomGenerator.setGlobalSeed(42L); //optionally set a specific seed for all Random objects

        DatabaseConfiguration dbConf = ConfigurationFactory.INMEMORY.getConfiguration();//in-memory maps
        //DatabaseConfiguration dbConf = ConfigurationFactory.MAPDB.getConfiguration(); //mapdb maps
        FileReader fr = new FileReader(new File(file));
        BufferedReader br = new BufferedReader(fr);
        String line;
        String[] data;
        Dataset trainingDataset = new Dataset(dbConf);

        //Reader fileReader = new FileReader(file);
        //Dataset trainingDataset = Dataset.Builder.parseCSVFile(fileReader, "Class", headerDataTypes, ',', '"', "\r\n", dbConf);
        while ((line = br.readLine()) != null) {
            data = line.split(",");
            AssociativeArray record = new AssociativeArray();
            for (int i = 0; i < data.length; i++) {
                record.put(i, new Double(data[i]));
            }
            trainingDataset.add(new Record(record, ""));
        }
        Dataset testingDataset = trainingDataset.copy();

        //Transform Dataset
        //-----------------

        //Convert Categorical variables to dummy variables (boolean) and normalize continuous variables
        DummyXMinMaxNormalizer dataTransformer = new DummyXMinMaxNormalizer("Test", dbConf);
        dataTransformer.fit_transform(trainingDataset, new DummyXMinMaxNormalizer.TrainingParameters());


        HierarchicalAgglomerative cluster = new HierarchicalAgglomerative("Test", dbConf);

        HierarchicalAgglomerative.TrainingParameters param = new HierarchicalAgglomerative.TrainingParameters();
        param.setDistanceMethod(HierarchicalAgglomerative.TrainingParameters.Distance.EUCLIDIAN);
        param.setLinkageMethod(HierarchicalAgglomerative.TrainingParameters.Linkage.AVERAGE);
        param.setMinClustersThreshold(5);
        param.setMaxDistanceThreshold(Double.MAX_VALUE);

        cluster.fit(trainingDataset, param);

        //Denormalize trainingDataset (optional)
        //dataTransformer.denormalize(trainingDataset);

        System.out.println("Cluster assignments (Record Ids):");
        for (Map.Entry<Integer, HierarchicalAgglomerative.Cluster> entry : cluster.getClusters().entrySet()) {
            Integer clusterId = entry.getKey();
            HierarchicalAgglomerative.Cluster cl = entry.getValue();

            System.out.println("Cluster " + clusterId + ": " + cl.getRecordIdSet());
        }


        //Use the clusterer
        //-----------------

        //Apply the same transformations on testingDataset
        dataTransformer.transform(testingDataset);

        //Get validation metrics on the training set
        HierarchicalAgglomerative.ValidationMetrics vm = cluster.validate(testingDataset);
        cluster.setValidationMetrics(vm); //store them in the model for future reference

        //Denormalize testingDataset (optional)
        //dataTransformer.denormalize(testingDataset);


        //Result map
        HashMap<String, Integer> resultMap = new HashMap<>();
        resultMap.put("size", cluster.getClusters().size());
        //Map<Integer, HierarchicalAgglomerative.Cluster> test = cluster.getClusters();
        System.out.println("Results:");
        for (Integer rId : testingDataset) {
            Record r = testingDataset.get(rId);
            System.out.println("Record " + rId + " - Original Y: " + r.getY() + ", Predicted Cluster Id: " + r.getYPredicted());
            resultMap.put(String.valueOf(rId), (Integer) r.getYPredicted());
        }


        System.out.println("Clusterer Statistics: " + PHPfunctions.var_export(vm));


        //Clean up
        //--------

        //Erase data transformer, clusterer.
        dataTransformer.erase();
        cluster.erase();

        //Erase datasets.
        trainingDataset.erase();
        testingDataset.erase();
        return resultMap;
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

        instance.MDPMMTrain(trainingData, validationData);

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


        Map<String, Integer> weekResultMap = new HashMap<>();
        weekResultMap.put("size", instance.getClusters().size());
        int count = 0;
        for (Record r : validationData) {
            Integer clusterId = (Integer) r.getYPredicted();

            weekResultMap.put(String.valueOf(count++), clusterId);

            System.out.println( "Label: " + r.getY() + ", predict: " + r.getYPredicted());
        }
        return weekResultMap;*/
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
