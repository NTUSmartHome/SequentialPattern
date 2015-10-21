//import dpmm.MDPMMTrain;

import alz.ActiveLzTree;
import alz.PPM;
import sdle.SDLE;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by MingJe on 2015/10/4.
 */
public class LifePattern {

    ArrayList<SDLE> sdleList;
    ArrayList<ArrayList<String>> instanceLabel;

    public LifePattern() {
        sdleList = new ArrayList<>();
        instanceLabel = new ArrayList<>();
    }

    public void runALZ(int trainedDays) {
        ActiveLzTree activeLzTree = new ActiveLzTree();
        activeLzTree.init();
        PPM.init(activeLzTree);
        int sum = 0;
        int right = 0;

        for (int i = 0; i < trainedDays; i++) {
            for (int j = 0; j < instanceLabel.size(); j++) {
                String[] acts = instanceLabel.get(j).get(i).split(",");
                Arrays.sort(acts);
                StringBuilder sb = new StringBuilder(acts[0]);
                for (int k = 1; k < acts.length; k++) {
                    sb.append("," + acts[k]);
                }
                activeLzTree.step(sb.toString());
            }
            activeLzTree.finish();
        }

        for (int i = trainedDays; i < instanceLabel.get(0).size(); i++) {
            //System.out.println(Thread.currentThread() + "  " + i);
            for (int j = 0; j < instanceLabel.size(); j++) {
                String[] acts = instanceLabel.get(j).get(i).split(",");
                Arrays.sort(acts);
                StringBuilder sb = new StringBuilder(acts[0]);
                for (int k = 1; k < acts.length; k++) {
                    sb.append("," + acts[k]);
                }
                List<Map.Entry<String, Double>> predictActs = PPM.prediction(0);
                if (predictActs.get(0).getKey().equals(sb.toString())) {
                    ++right;
                }
                activeLzTree.step(sb.toString());
                PPM.addSeenActivity(sb.toString(), 0);
                ++sum;
            }
            activeLzTree.finish();
            PPM.clearSeenActivity(0);


        }
        System.out.println(trainedDays + "  " + (double) right / sum);
    }

    public void runSDLE(int trainedDays) {


        int sum = 0;
        int right = 0;

        //readFile(5, 1, 0.01, 0.01);
        for (int i = 0; i < trainedDays; i++) {
            for (int j = 0; j < instanceLabel.size(); j++) {
                String[] acts = instanceLabel.get(j).get(i).split(",");
                sdleList.get(j).parameterUpdating(acts);
                /*for (int k = 0; k < acts.length; k++) {
                    sdleList.get(j).parameterUpdating(acts[k].split(""));
                    //String[] asdas = acts[k].split("");
                    //alz.step(acts[k]);
                }*/

            }
        }

        for (int i = trainedDays; i < instanceLabel.get(0).size(); i++) {
            //System.out.println(Thread.currentThread() + "  " + i);
            for (int j = 0; j < instanceLabel.size(); j++) {
                String[] acts = instanceLabel.get(j).get(i).split(",");
                String[] predictedActs = sdleList.get(j).getMaxProbabiltyAct();
                if (acts.length == predictedActs.length) {
                    boolean isEqual = false;
                    Arrays.sort(acts);
                    Arrays.sort(predictedActs);
                    for (int k = 0; k < acts.length; k++) {
                        if (acts[k].equals(predictedActs[k])) {
                            isEqual = true;
                            break;
                        }
                    }
                    if (isEqual) ++right;

                }


                /*boolean isEqual = false;
                for (int k = 0; k < acts.length; k++) {
                    if (acts[k].equals(predictedActs[0])) {
                        isEqual = true;
                        break;
                    }
                }
                if (isEqual) ++right;*/


                sdleList.get(j).parameterUpdating(acts);
                /*for (int k = 0; k < acts.length; k++) {
                    sdleList.get(j).parameterUpdating(acts[k].split(""));
                }*/


                ++sum;
            }


        }
        System.out.println(trainedDays + "  " + (double) right / sum);
    }



    public void readFile(int timeInterval, int option, double rh, double beta) {
        int id = 0;
        StringBuilder inputFile = new StringBuilder("db/");
        timeInterval = getTimeInterval(timeInterval, option);
        switch (option) {
            case 0:
                inputFile.append((int) (timeInterval / Math.pow(60, option)));
                inputFile.append("second/");
                break;
            case 1:
                inputFile.append((int) (timeInterval / Math.pow(60, option)));
                inputFile.append("minute/");
                break;
            case 2:
                inputFile.append((int) (timeInterval / Math.pow(60, option)));
                inputFile.append("hour/");
                break;
            case 3:
                inputFile.append((int) (timeInterval / Math.pow(60, option)));
                inputFile.append("day/");
                break;
        }
        //String inputFile = "db/SDLE" + id + ".txt";
        String fileString = inputFile.toString() + id + ".txt";
        File file = new File(fileString);

        while (file.exists()) {

            try {
                FileReader fr = new FileReader(file);
                BufferedReader br = new BufferedReader(fr);
                String line;
                instanceLabel.add(new ArrayList<>());
                sdleList.add(new SDLE(rh, beta));
                while ((line = br.readLine()) != null) {
                    instanceLabel.get(id).add(line);
                }
                fr.close();
                br.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            id++;
            fileString = inputFile.toString() + id + ".txt";
            file = new File(fileString);
        }
    }

    private int getTimeInterval(int timeInterval, int option) {
        switch (option) {
            case 1:
                timeInterval *= 60;
                break;
            case 2:
                timeInterval *= 60 * 60;
                break;
            case 3:
                timeInterval *= 60 * 60 * 24;
                break;

        }
        return timeInterval;
    }

    public static void main(String[] args) throws InterruptedException {

        //new MDPMMTrain("report/WSU", "WSU", 0.5, 10, 100);

        LifePattern olp = new LifePattern();
        olp.readFile(5, 1, 0.01, 0.01);
        //olp.runALZ(200);
        olp.runSDLE(220);
        /*ExecutorService pool = Executors.newFixedThreadPool(5);
        LifePattern olp = new LifePattern();
        olp.readFile(5, 1, 0.01, 0.01);
        long start = System.currentTimeMillis();
        for (int i = 10; i <= 50; i += 5) {
            final int idx = i;
            Thread th = new Thread(() -> {
                LifePattern lp = new LifePattern();
                lp.instanceLabel = olp.instanceLabel;
                for (int j = 0; j < olp.instanceLabel.size(); j++) {
                    lp.sdleList.add(new SDLE(0.01, 0.01));
                }
                //System.out.println(Thread.currentThread().getName());
                lp.runSDLE(idx);
            });
            pool.execute(th);

        }
        pool.shutdown();
        pool.awaitTermination(30, TimeUnit.SECONDS);
        long end = System.currentTimeMillis();
        System.out.println((end - start) / 6000);*/

    }
}
