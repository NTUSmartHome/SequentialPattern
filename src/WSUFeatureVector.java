import java.io.*;

/**
 * Created by MingJe on 2015/10/3.
 */
public class WSUFeatureVector {
    private final int numOfFeatures = 12;
    private double[] features;
    private String label;

    WSUFeatureVector(String path, String inputFile) {
        features = new double[numOfFeatures];
        this.label = inputFile;
        init(path + "\\" + inputFile);
    }

    private void init(String inputFile) {
        try {
            FileReader fr = new FileReader(new File(inputFile));
            BufferedReader br = new BufferedReader(fr);
            String line = null;
            String[] feature = null;
            String[] actLabel;
            while ((line = br.readLine()) != null) {
                if(line.contains("Sum")) break;
                line = line.replace("[", "");
                line = line.replace("]", "");
                feature = line.split(":");
                actLabel = feature[0].split(",");
                for (int i = 0; i < actLabel.length; i++) {
                    features[Integer.parseInt(actLabel[i].trim()) - 1] +=
                            Double.valueOf(feature[1]) / actLabel.length;
                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public double[] getVector() {
        return features;
    }

    public String getLabel() {
        return label;
    }

    public int getNumOfFeatures() {
        return numOfFeatures;
    }
}
