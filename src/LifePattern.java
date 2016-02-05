//import dpmm.MDPMMTrain;

import alz.ActiveLzTree;
import alz.PPM;
import sdle.SDLE;

import java.io.*;
import java.net.URISyntaxException;
import java.util.*;

/**
 * Created by MingJe on 2015/10/4.
 */
public class LifePattern {
    static double rh = 0.05, beta = 0.01;
    ArrayList<ArrayList<SDLE>> weekSDLEList;
    ArrayList<SDLE> sdleList;
    ArrayList<ArrayList<String>> instanceLabel;
    int MaxCluster;
    //----------------------------------WSU data begins on Thursday------------------------------------//
    final int weekDayStart = 3;
    //-----------------------------------------------------------------------------------------------//

    public LifePattern() {
        sdleList = new ArrayList<>();
        instanceLabel = new ArrayList<>();
        weekSDLEList = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            weekSDLEList.add(new ArrayList<>());
        }
    }

    public static void main(String[] args) throws InterruptedException {

        //Original : 0.5, 10, 100
        //new MDPMMTrain("report/WSU", "WSU", 0.5, 5, 100);

        LifePattern olp = new LifePattern();
        olp.readFile(5, 1, rh, beta);
        olp.perDayActivityEstimation(112);
        //olp.runSDLE(10);
        //olp.runAZSDLESimple(21);
        //olp.improvedALZ(2);
        //olp.runAZSDLE(1);


    }

    public void improvedALZ(int trainedDays) {

        int oneDayChange = 0;
        int allChange = 0;

        int alzRight = 0, alzOneDayRight = 0;
        int alzIdx = 0;
        int[] alzTimeIntervalAccuracy = new int[instanceLabel.size()];

        ActiveLzTree[] activeLzTreeArray = new ActiveLzTree[5];
        for (int i = 0; i < activeLzTreeArray.length; i++) {
            activeLzTreeArray[i] = new ActiveLzTree();
            activeLzTreeArray[i].init();
            PPM.init(activeLzTreeArray[i]);
        }

        //-----------------------------------ALZ focus on changing of activity-------------------------------//
        String preActivity = "-1", currentActivity;
        //--------------------------------------------------------------------------------------------------//
        for (int i = 0; i < trainedDays; i++) {
            for (int j = 0; j < instanceLabel.size(); j++) {
                String[] acts = instanceLabel.get(j).get(i).split(",");
                Arrays.sort(acts);
                /* Old version
                if (j >= 3 && j <= 67)
                    alzIdx = 0;
                else if ((j > 67 && j <= 80) || (j >= 272 && j <= 287) || j < 3)
                    alzIdx = 3;
                else if (j > 80 && j <= 97)
                    alzIdx = 4;
                else if (j > 97 && j <= 224)
                    alzIdx = 1;
                else
                    alzIdx = 2;
                */

               /* if (j >= 4 && j <= 64)
                    alzIdx = 4;
                else if ((j > 64 && j <= 81) || (j >= 270 && j <= 287) || j < 4)
                    alzIdx = 3;
                else if (j > 81 && j <= 100)
                    alzIdx = 0;
                else if (j > 100 && j <= 225)
                    alzIdx = 1;
                else
                    alzIdx = 2;*/


                currentActivity = acts[0];
                if (!currentActivity.equals(preActivity) || activeLzTreeArray[alzIdx].getWindow().size() == 0)
                    activeLzTreeArray[alzIdx].step(currentActivity);

                preActivity = currentActivity;
            }

            for (int j = 0; j < activeLzTreeArray.length; j++) {
                //activeLzTreeArray[j].finish();
            }

        }

        preActivity = "-1";
        StringBuilder result = new StringBuilder();
        StringBuilder sequenceResult = new StringBuilder();
        for (int i = trainedDays; i < instanceLabel.get(0).size(); i++) {
            for (int j = 0; j < instanceLabel.size(); j++) {
                String[] acts = instanceLabel.get(j).get(i).split(",");
                Arrays.sort(acts);
                currentActivity = acts[0];
                /*Old Version
                if (j >= 3 && j <= 67)
                    alzIdx = 0;
                else if ((j > 67 && j <= 80) || (j >= 272 && j <= 287) || j < 3)
                    alzIdx = 3;
                else if (j > 80 && j <= 97)
                    alzIdx = 4;
                else if (j > 97 && j <= 224)
                    alzIdx = 1;
                else
                    alzIdx = 2;
                */

               /* if (j >= 4 && j <= 64)
                    alzIdx = 4;
                else if ((j > 64 && j <= 81) || (j >= 270 && j <= 287) || j < 4)
                    alzIdx = 3;
                else if (j > 81 && j <= 100)
                    alzIdx = 0;
                else if (j > 100 && j <= 225)
                    alzIdx = 1;
                else
                    alzIdx = 2;*/

                if (!currentActivity.equals(preActivity)) {
                    List<Map.Entry<String, Double>> predictedActsByALZ = PPM.prediction(alzIdx);

                    result.append("Time : " + j / 12 + ":" + (j * 5) % 60 + "\tActivity : " + acts[0] + "\n");
                    if (predictedActsByALZ.size() == 1)
                        result.append("ALZ : " + predictedActsByALZ.get(0).getKey() + "\t" + predictedActsByALZ.get(0).getValue()
                                + "\n\n");
                    else if (predictedActsByALZ.size() == 2)
                        result.append("ALZ : " + predictedActsByALZ.get(0).getKey() + "\t" + predictedActsByALZ.get(0).getValue()
                                + " " + predictedActsByALZ.get(1).getKey() + " " + predictedActsByALZ.get(1).getValue() + "\n\n");
                    else
                        result.append("ALZ : " + predictedActsByALZ.get(0).getKey() + "\t" + predictedActsByALZ.get(0).getValue()
                                + " " + predictedActsByALZ.get(1).getKey() + " " + predictedActsByALZ.get(1).getValue()
                                + " " + predictedActsByALZ.get(2).getKey() + " " + predictedActsByALZ.get(2).getValue() + "\n\n");

                    if (predictedActsByALZ.get(0).getKey().equals(currentActivity)
                            || ((predictedActsByALZ.size() > 1) && (predictedActsByALZ.get(1).getKey().equals(currentActivity)) && !predictedActsByALZ.get(1).getValue().equals(0.0))
                            || ((predictedActsByALZ.size() > 2) && (predictedActsByALZ.get(2).getKey().equals(currentActivity)) && !predictedActsByALZ.get(2).getValue().equals(0.0))) {
                        alzOneDayRight++;
                        alzRight++;
                    }
                    allChange++;
                    oneDayChange++;
                    activeLzTreeArray[alzIdx].step(acts[0]);

                }
                preActivity = currentActivity;


            }
            System.out.println(i + " ALZ: " + (double) alzOneDayRight / oneDayChange);
            sequenceResult.append((double) alzOneDayRight / oneDayChange + " ");
            alzOneDayRight = 0;
            oneDayChange = 0;
            for (int j = 0; j < activeLzTreeArray.length; j++) {
                activeLzTreeArray[j].finish();
            }
        }

        System.out.println(" ALZ: " + (double) alzRight / allChange);
        System.out.println(sequenceResult.toString());
        try {
            FileWriter fw = new FileWriter("ResultImprovrdALZ.txt");
            fw.write(result.toString());

            fw.write("\n\n" + Arrays.toString(alzTimeIntervalAccuracy));

            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void perDayActivityEstimation(int trainedDays) {

        try {
            // perDayActivityEstimation for day merge
            String file = "report/WeekSDLE/Features/";
            ArrayList<ArrayList<SDLE>> newWeekSDLEList = newWeekSDLEList(7);
            int day = weekDayStart;
            for (int i = 0; i < trainedDays; i++) {
                for (int j = 0; j < instanceLabel.size(); j++) {
                    String[] acts = instanceLabel.get(j).get(i).split(",");
                    newWeekSDLEList.get(day).get(j).parameterUpdating(acts);
                    day = (day + 1) % 7;
                }
            }
            //write feature for day merge
            FileWriter fw = new FileWriter(file + "WeekSDLEFeatures.csv");
            StringBuilder stringBuilder = initStringBuilder(3168);
            for (int i = 0; i < 7; i++) {
                for (int j = 0; j < instanceLabel.size(); j++) {
                    ArrayList<Double> cellDistribution = newWeekSDLEList.get(i).get(j).getDistribution();
                    for (int k = 1; k < cellDistribution.size() - 1; k++) {
                        stringBuilder.append(String.valueOf(cellDistribution.get(k).doubleValue() * 100) + ",");
                    }
                }
                stringBuilder.append("\n");
            }
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
            fw.write(stringBuilder.toString());
            fw.close();
            Map<String, Integer> resultMap = contextDayMerge(file + "WeekSDLEFeatures.csv");

            //perDayActivityEstimation for day segmentation
            int numOfGroup = resultMap.get("size");
            newWeekSDLEList = newWeekSDLEList(numOfGroup);
            day = weekDayStart;
            for (int i = 0; i < trainedDays; i++) {
                for (int j = 0; j < instanceLabel.size(); j++) {
                    String[] acts = instanceLabel.get(j).get(i).split(",");
                    newWeekSDLEList.get(resultMap.get(String.valueOf(day))).get(j).parameterUpdating(acts);
                    day = (day + 1) % 7;
                }
            }
            //write feature for day segmentation
            for (int i = 0; i < numOfGroup; i++) {
                fw = new FileWriter(file + "Segmentation/" + i + ".csv");
                stringBuilder = initStringBuilder(11);
                for (int j = 0; j < instanceLabel.size(); j++) {
                    ArrayList<Double> cellDistribution = newWeekSDLEList.get(resultMap.get(String.valueOf(i))).get(j).getDistribution();
                    stringBuilder.append(String.valueOf(cellDistribution.get(1).doubleValue()));
                    for (int k = 2; k < cellDistribution.size() - 1; k++) {
                        stringBuilder.append("," + String.valueOf(cellDistribution.get(k).doubleValue()));
                    }
                    stringBuilder.append("\n");
                }
                fw.write(stringBuilder.toString());
                fw.close();
            }
            contextDaySegmentation(file + "Segmentation/");


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private StringBuilder initStringBuilder(int numOfClass) {
        StringBuilder stringBuilder = new StringBuilder();
        //header
        for (int i = 0; i < numOfClass; i++) {
            stringBuilder.append(i + ",");
        }
        //stringBuilder.append("Class");
        stringBuilder.append("\n");
        return stringBuilder;
    }

    private Map<String, Integer> contextDayMerge(String file) {
        try {
            return DPMM.train(file, 0.01, 1, 100);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void contextDaySegmentation(String path) {
        int idx = 0;
        String fileName = path + idx++ + ".csv";
        File file = new File(fileName);
        while (file.exists()) {
            try {
                DPMM.train(fileName, 0.5, 5, 100);
                fileName = path + idx++ + ".csv";
                file = new File(fileName);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private ArrayList<SDLE> newSDLEList() {
        ArrayList<SDLE> newSDLEList = new ArrayList<>();
        for (int i = 0; i < sdleList.size(); i++) {
            newSDLEList.add(new SDLE(rh, beta));
        }
        return newSDLEList;
    }

    private ArrayList<ArrayList<SDLE>> newWeekSDLEList(int weekDays) {
        ArrayList<ArrayList<SDLE>> weekSDLEList = new ArrayList<>();
        for (int i = 0; i < weekDays; i++) {
            weekSDLEList.add(newSDLEList());
        }
        return weekSDLEList;
    }


    public void runAZSDLESimple(int trainedDays) {
        int numOfTimeInterval = instanceLabel.size();
        int totalPredictedInstance = numOfTimeInterval * (instanceLabel.get(0).size() - trainedDays);

        int alzRight = 0, alzOneDayRight = 0;
        int sdleRight = 0, sdleOneDayRight = 0;
        int keepPredictRight = 0, keepPredictOneDayRight = 0;

        int weekRight = 0, weekOneDayRight = 0;
        int alzIdx = 0;
        int[] sdleTimeIntervalAccuracy = new int[instanceLabel.size()];
        int[] alzTimeIntervalAccuracy = new int[instanceLabel.size()];
        int[] keepTimeIntervalAccuracy = new int[instanceLabel.size()];

        ActiveLzTree[] activeLzTreeArray = new ActiveLzTree[5];
        for (int i = 0; i < activeLzTreeArray.length; i++) {
            activeLzTreeArray[i] = new ActiveLzTree();
            activeLzTreeArray[i].init();
            PPM.init(activeLzTreeArray[i]);
        }

        //-----------------------------------ALZ focus on changing of activity-------------------------------//
        String preActivity = "-1", currentActivity;
        //--------------------------------------------------------------------------------------------------//
        //----------------------------------WSU data begins on Thursday------------------------------------//
        int day = 3;
        //-----------------------------------------------------------------------------------------------//
        for (int i = 0; i < trainedDays; i++) {
            for (int j = 0; j < instanceLabel.size(); j++) {
                String[] acts = instanceLabel.get(j).get(i).split(",");
                Arrays.sort(acts);

                if (j >= 3 && j <= 67)
                    alzIdx = 0;
                else if ((j > 67 && j <= 80) || (j >= 272 && j <= 287) || j < 3)
                    alzIdx = 3;
                else if (j > 80 && j <= 97)
                    alzIdx = 4;
                else if (j > 97 && j <= 224)
                    alzIdx = 1;
                else
                    alzIdx = 2;
                //---------------------------------------Different Here with AZSDLE-------------------------------------------//
                String[] oneActs = new String[1];
                oneActs[0] = acts[0];
                //---------------------------------------Week SDLE-----------------------------------------------------------//
                weekSDLEList.get(day).get(j).parameterUpdating(oneActs);

                //-----------------------------------------------------------------------------------------------------------//
                sdleList.get(j).parameterUpdating(oneActs);
                //StringBuilder sb = new StringBuilder(acts[0]);
                currentActivity = acts[0];
                if (!currentActivity.equals(preActivity) || activeLzTreeArray[alzIdx].getWindow().size() == 0)
                    activeLzTreeArray[alzIdx].step(currentActivity);
                //-----------------------------------------------------------------------------------------------------------//
                preActivity = currentActivity;
            }
            day = (day + 1) % 7;
        }

        preActivity = "-1";
        StringBuilder result = new StringBuilder();
        for (int i = trainedDays; i < instanceLabel.get(0).size(); i++) {
            for (int j = 0; j < instanceLabel.size(); j++) {
                String[] acts = instanceLabel.get(j).get(i).split(",");
                Arrays.sort(acts);

                if (j >= 3 && j <= 67)
                    alzIdx = 0;
                else if ((j > 67 && j <= 80) || (j >= 272 && j <= 287) || j < 3)
                    alzIdx = 3;
                else if (j > 80 && j <= 97)
                    alzIdx = 4;
                else if (j > 97 && j <= 224)
                    alzIdx = 1;
                else
                    alzIdx = 2;

                //---------------------------------------Different Here with AZSDLE-------------------------------------------//
                //StringBuilder sb = new StringBuilder(acts[0]);
                String[] oneActs = new String[1];
                oneActs[0] = acts[0];
                currentActivity = acts[0];


                result.append("Time : " + j / 12 + ":" + (j * 5) % 60 + "\tActivity : " + acts[0] + "\n");
                List<Map.Entry<String, Double>> predictedActsBySDLE = sdleList.get(j).getMaxProbabiltyAct();
                List<Map.Entry<String, Double>> predictedActsByALZ = PPM.prediction(alzIdx);
                List<Map.Entry<String, Double>> predictedActsByWeeKSDLE;
                if (day < 5)
                    predictedActsByWeeKSDLE = weekSDLEList.get(0).get(j).getMaxProbabiltyAct();
                else
                    predictedActsByWeeKSDLE = weekSDLEList.get(1).get(j).getMaxProbabiltyAct();


                result.append("SDLE : " + predictedActsBySDLE.get(0).getKey() + "\t" + predictedActsBySDLE.get(0).getValue()
                        + " " + predictedActsBySDLE.get(1).getKey() + " " + predictedActsBySDLE.get(1).getValue() + "\n");
                if (predictedActsByALZ.size() == 1)
                    result.append("ALZ : " + predictedActsByALZ.get(0).getKey() + "\t" + predictedActsByALZ.get(0).getValue()
                            + "\n\n");
                else
                    result.append("ALZ : " + predictedActsByALZ.get(0).getKey() + "\t" + predictedActsByALZ.get(0).getValue()
                            + " " + predictedActsByALZ.get(1).getKey() + " " + predictedActsByALZ.get(1).getValue() + "\n\n");


                if (predictedActsByALZ.get(0).getKey().equals(acts[0])) {
                    ++alzOneDayRight;
                    ++alzRight;
                    ++alzTimeIntervalAccuracy[j];
                }

                if (predictedActsBySDLE.get(0).getKey().equals(acts[0])) {
                    ++sdleOneDayRight;
                    ++sdleRight;
                    ++sdleTimeIntervalAccuracy[j];
                }

                if (currentActivity.equals(preActivity)) {
                    ++keepPredictOneDayRight;
                    ++keepPredictRight;
                    ++keepTimeIntervalAccuracy[j];
                }

                if (predictedActsByWeeKSDLE.get(0).getKey().equals(acts[0])) {
                    ++weekOneDayRight;
                    ++weekRight;
                }

                //---------------------------------------Week SDLE-----------------------------------------------------------//
                weekSDLEList.get(day).get(j).parameterUpdating(oneActs);
                day = (day + 1) % 7;
                //-----------------------------------------------------------------------------------------------------------//
                sdleList.get(j).parameterUpdating(oneActs);
                if (!currentActivity.equals(preActivity))
                    activeLzTreeArray[alzIdx].step(acts[0]);
                preActivity = currentActivity;

            }
            System.out.println(i + "Week SDLE: " + (double) weekOneDayRight / numOfTimeInterval + " SDLE: " + (double) sdleOneDayRight / numOfTimeInterval
                    + " ALZ: " + (double) alzOneDayRight / numOfTimeInterval + " Keep: " + (double) keepPredictOneDayRight / numOfTimeInterval);

            sdleOneDayRight = 0;
            alzOneDayRight = 0;
            keepPredictOneDayRight = 0;
            weekOneDayRight = 0;
        }
        StringBuilder entropySB = new StringBuilder();
        StringBuilder bool = new StringBuilder();
        for (int i = instanceLabel.get(0).size() - 2; i < instanceLabel.get(0).size() - 1; i++) {
            for (int j = 0; j < instanceLabel.size(); j++) {
                String[] acts = instanceLabel.get(j).get(i).split(",");
                Arrays.sort(acts);
                String[] oneActs = new String[1];
                oneActs[0] = acts[0];
                currentActivity = acts[0];
                if (currentActivity.equals(preActivity)) {
                    double entropy = sdleList.get(j).getEntropySimple();
                    System.out.println("0 " + entropy);
                    entropySB.append(entropy + " ");
                    bool.append("0 ");
                } else {
                    double entropy = sdleList.get(j).getEntropySimple();
                    System.out.println("1 " + entropy);
                    entropySB.append(entropy + " ");
                    bool.append("1 ");
                }
                preActivity = currentActivity;
            }
        }

        StringBuilder sdleSB = new StringBuilder();


        for (int j = 1; j <= 12; j++) {
            for (int i = 0; i < sdleList.size(); i++) {
                String[] acts = new String[1];
                acts[0] = String.valueOf(j);
                sdleSB.append(sdleList.get(i).getActivity().getQOfActs(acts) + " ");

            }
            sdleSB.append("\n");
        }


        try {
            FileWriter fw = new FileWriter("Result.txt");
            fw.write(result.toString());
            fw.write("\n\n" + Arrays.toString(sdleTimeIntervalAccuracy));
            fw.write("\n\n" + Arrays.toString(alzTimeIntervalAccuracy));
            fw.write("\n\n" + Arrays.toString(keepTimeIntervalAccuracy));
            fw.write("\n\n" + entropySB.toString());
            fw.write("\n\n" + bool.toString());
            fw.write("\n\n" + sdleSB.toString());
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(trainedDays + "  Week: " + (double) weekRight / totalPredictedInstance + " SDLE: " + (double) sdleRight / totalPredictedInstance
                + " ALZ: " + (double) alzRight / totalPredictedInstance + " Keep: " + (double) keepPredictRight / totalPredictedInstance);

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
                //---------------------Day of Week-----------------//
                for (int i = 0; i < weekSDLEList.size(); i++) {
                    weekSDLEList.get(i).add(new SDLE(rh, beta));
                }
                //------------------------------------------------//
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

    private Map<Integer, Integer> weekMap() {
        Map<Integer, Integer> weekMap = new HashMap<>();
        try {
            FileReader fr = new FileReader("report/WeekSDLE/DPMM/WeekSDLEResult");
            BufferedReader br = new BufferedReader(fr);
            String line;
            int i = 0;
            while ((line = br.readLine()) != null) {
                weekMap.put(i, Integer.parseInt(line.split("\\s+")[1]));
                i++;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        int MaxCluster = 0;
        for (int i = 0; i < weekMap.size(); i++) {
            if (weekMap.get(i) > MaxCluster)
                MaxCluster = weekMap.get(i);
        }
        this.MaxCluster = MaxCluster + 1;
        return weekMap;
    }

    private ArrayList<ArrayList<SDLE>> renewWeekSDLE() {
        ArrayList<ArrayList<SDLE>> tmpWeekSDLEList = new ArrayList<>();
        for (int i = 0; i < MaxCluster; i++) {
            tmpWeekSDLEList.add(new ArrayList<>());
            for (int j = 0; j < weekSDLEList.get(0).size(); j++) {
                tmpWeekSDLEList.get(i).add(new SDLE(rh, beta));
            }
        }
        return tmpWeekSDLEList;
    }
}
