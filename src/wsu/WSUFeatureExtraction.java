package wsu;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by MingJe on 2015/10/3.
 */
public class WSUFeatureExtraction {
    private final int timeInterval;
    private final int option;
    ArrayList<WSUFeatureVector> featureVectors;
    private int numOfInstances;
    private String path;

    WSUFeatureExtraction(int timeInterval, int option, String path) {
        switch (option) {
            case 1:
                path += timeInterval + "minute/";
                timeInterval *= 60;
                numOfInstances = 86400 / timeInterval;
                break;
            case 2:
                path += timeInterval + "hour/";
                timeInterval *= 60 * 60;
                numOfInstances = 86400 / timeInterval;
                break;
            case 3:
                path += timeInterval + "day/";
                timeInterval *= 60 * 60 * 24;
                numOfInstances = 86400 / timeInterval;
                break;
        }
        this.timeInterval = timeInterval;
        this.option = option;
        this.path = path;
        featureVectors = new ArrayList();

        init();


    }

    public static void main(String[] args) {
        new WSUFeatureExtraction(5, 1, "report/db/");
    }

    private void init() {
        final FileWriter fw;
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        try {
            new File("report/WSU/Features/").mkdirs();
            new File("report/WSU/Features/WSUFeature.txt").delete();
            fw = new FileWriter("report/WSU/Features/WSUFeature.txt", true);
            for (int i = 0; i < numOfInstances; i++) {
                WSUFeatureVector vector = new WSUFeatureVector(path, getInputFilename(i));
                double[] feature = vector.getVector();
                for (int j = 0; j < feature.length; j++) {
                    fw.write(String.valueOf(feature[j] * 100));
                    fw.write(",");
                }
                fw.write(vector.getLabel());
                fw.write("\r\n");

            }
            fw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private void output() {

    }

    private String getInputFilename(int id) {
        String time = "";
        int currentTime = id * timeInterval;
        int s = currentTime % 60;
        currentTime -= s;
        currentTime /= 60;
        int m = currentTime % 60;
        currentTime -= m;
        currentTime /= 60;
        int h = currentTime;

        return "SDLE" + h + "h" + m + "m.txt";
    }
}
