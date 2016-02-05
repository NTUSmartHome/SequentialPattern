import com.datumbox.common.dataobjects.AssociativeArray;
import com.datumbox.common.dataobjects.Dataset;
import com.datumbox.common.dataobjects.Record;
import com.datumbox.common.dataobjects.TypeInference;
import com.datumbox.common.persistentstorage.ConfigurationFactory;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConfiguration;
import com.datumbox.common.utilities.PHPfunctions;
import com.datumbox.common.utilities.RandomGenerator;
import com.datumbox.framework.machinelearning.clustering.MultinomialDPMM;
import com.datumbox.framework.machinelearning.common.bases.basemodels.BaseDPMM;
import com.datumbox.framework.machinelearning.datatransformation.DummyXMinMaxNormalizer;


import java.io.*;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Created by MingJe on 2016/2/4.
 */

public class DPMM {

    public static Map<String, Integer> train(String file, double alpha, double alphaWords, int iter) throws URISyntaxException, IOException {
        //Initialization
        //--------------
        RandomGenerator.setGlobalSeed(42L); //optionally set a specific seed for all Random objects
        DatabaseConfiguration dbConf = ConfigurationFactory.INMEMORY.getConfiguration(); //in-memory maps
        //DatabaseConfiguration dbConf = ConfigurationFactory.MAPDB.getConfiguration(); //mapdb maps
        FileReader fr = new FileReader(new File(file));
        BufferedReader br = new BufferedReader(fr);
        String line;
        String[] data;
        /*Map<String, TypeInference.DataType> headerDataTypes = new HashMap<>();
        line = br.readLine();
        String[] data = line.split(",");
        for (int i = 0; i < data.length; i++) {
            headerDataTypes.put(data[i], TypeInference.DataType.NUMERICAL);
        }
        headerDataTypes.put("Class", TypeInference.DataType.CATEGORICAL);*/
        Dataset trainingDataset = new Dataset(dbConf);
        Reader fileReader = new FileReader(file);
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


        MultinomialDPMM cluster = new MultinomialDPMM("Test", dbConf);

        MultinomialDPMM.TrainingParameters param = new MultinomialDPMM.TrainingParameters();
        param.setAlpha(alpha);
        param.setAlphaWords(alphaWords);
        param.setInitializationMethod(BaseDPMM.TrainingParameters.Initialization.ONE_CLUSTER_PER_RECORD);
        param.setMaxIterations(iter);

        cluster.fit(trainingDataset, param);

        //Denormalize trainingDataset (optional)
        dataTransformer.denormalize(trainingDataset);

        System.out.println("Cluster assignments (Record Ids):");
        for (Map.Entry<Integer, MultinomialDPMM.Cluster> entry : cluster.getClusters().entrySet()) {
            Integer clusterId = entry.getKey();
            MultinomialDPMM.Cluster cl = entry.getValue();

            System.out.println("Cluster " + clusterId + ": " + cl.getRecordIdSet());
        }


        //Use the clusterer
        //-----------------

        //Apply the same transformations on testingDataset
        dataTransformer.transform(testingDataset);

        //Get validation metrics on the training set
        MultinomialDPMM.ValidationMetrics vm = cluster.validate(testingDataset);
        cluster.setValidationMetrics(vm); //store them in the model for future reference

        //Denormalize testingDataset (optional)
        dataTransformer.denormalize(testingDataset);


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
        dataTransformer.erase();
        cluster.erase();

        //Erase datasets.
        trainingDataset.erase();
        testingDataset.erase();
        return resultMap;
    }
}
