//import dpmm.MDPMMTrain;

import alz.ActiveLzTree;
import alz.PPM;
import dpmm.MDPMMTrain;
import sdle.SDLE;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by MingJe on 2015/10/4.
 */
public class LifePattern {

    ArrayList<ArrayList<SDLE>> weekSdleList;
    ArrayList<SDLE> sdleList;
    ArrayList<ArrayList<String>> instanceLabel;

    public LifePattern() {
        sdleList = new ArrayList<>();
        instanceLabel = new ArrayList<>();
        weekSdleList = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            weekSdleList.add(new ArrayList<>());
        }
    }

    public static void main(String[] args) throws InterruptedException {

        //Original : 0.5, 10, 100
        //new MDPMMTrain("report/WSU", "WSU", 0.5, 5, 100);

        LifePattern olp = new LifePattern();
        olp.readFile(5, 1, 0.05, 0.01);
        olp.runSDLE(28);
        //olp.runALZ(1);
        //olp.runAZSDLESimple(21);
        //olp.improvedALZ(10);
        //olp.runAZSDLE(1);


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

        StringBuilder result = new StringBuilder();
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
                result.append("Time : " + j / 12 + ":" + (j * 5) % 60 + "\tActivity : " + sb.toString() + "\n");
                result.append("ALZ : " + predictActs.get(0).getKey() + "\t" + predictActs.get(0).getValue() + "\n\n");
                if (predictActs.get(0).getKey().equals(sb.toString())) {
                    ++right;
                }


                activeLzTree.step(sb.toString());
                //PPM.addSeenActivity(sb.toString(), 0);

                ++sum;
            }

            activeLzTree.finish();
            //PPM.clearSeenActivity(0);


        }
        try {
            FileWriter fw = new FileWriter("ResultALZ.txt");
            fw.write(result.toString());
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println(trainedDays + "  " + (double) right / sum);
    }

    public void runSDLE(int trainedDays) {


        int numOfTimeInterval = instanceLabel.size();
        int totalPredictedInstance = numOfTimeInterval * (instanceLabel.get(0).size() - trainedDays);

        int sdleRight = 0, sdleOneDayRight = 0;
        int weekRight = 0, weekOneDayRight = 0;

        int[] sdleTimeIntervalAccuracy = new int[instanceLabel.size()];
        int[] keepTimeIntervalAccuracy = new int[instanceLabel.size()];


        //----------------------------------WSU data begins on Thursday------------------------------------//
        int day = 3;
        //-----------------------------------------------------------------------------------------------//
        for (int i = 0; i < trainedDays; i++) {
            for (int j = 0; j < instanceLabel.size(); j++) {
                String[] acts = instanceLabel.get(j).get(i).split(",");
                Arrays.sort(acts);

                //---------------------------------------Different Here with AZSDLE-------------------------------------------//
                String[] oneActs = new String[1];
                oneActs[0] = acts[0];
                //---------------------------------------Week SDLE-----------------------------------------------------------//
                weekSdleList.get(day).get(j).parameterUpdating(oneActs);
                day = (day + 1) % 7;
                //-----------------------------------------------------------------------------------------------------------//
                sdleList.get(j).parameterUpdating(oneActs);
                //StringBuilder sb = new StringBuilder(acts[0]);

            }
        }

        new File("report/WeekSDLE/Features/").mkdirs();
        new File("report/WeekSDLE/Features/WeekSDLEFeature.txt").delete();
        try {
            StringBuilder sb = new StringBuilder();
            int count = 0;
            for (int i = 0; i < weekSdleList.size(); i++) {
                for (int j = 0; j < weekSdleList.get(i).size(); j++) {
                    ArrayList<Double> tmp = weekSdleList.get(i).get(j).getDistribution();
                    for (int k = 1; k < tmp.size(); k++) {
                        sb.append(tmp.get(k) + ",");
                        count++;
                    }
                }
                sb.append("WeekDay" + (i + 1));
                sb.append("\r\n");
                System.out.println(count);
                count = 0;
               // System.out.println(sb.toString());
            }

            FileWriter fw = new FileWriter("report/WeekSDLE/Features/WeekSDLEFeature.txt");
            fw.write(sb.toString());
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        new MDPMMTrain("report/WeekSDLE", "WeekSDLE", 0.5, 5, 100);
        StringBuilder result = new StringBuilder();
        for (int i = trainedDays; i < instanceLabel.get(0).size(); i++) {
            for (int j = 0; j < instanceLabel.size(); j++) {
                String[] acts = instanceLabel.get(j).get(i).split(",");
                Arrays.sort(acts);

                //---------------------------------------Different Here with AZSDLE-------------------------------------------//
                //StringBuilder sb = new StringBuilder(acts[0]);
                String[] oneActs = new String[1];
                oneActs[0] = acts[0];


                result.append("Time : " + j / 12 + ":" + (j * 5) % 60 + "\tActivity : " + acts[0] + "\n");
                List<Map.Entry<String, Double>> predictedActsBySDLE = sdleList.get(j).getMaxProbabiltyAct();
                List<Map.Entry<String, Double>> predictedActsByWeeKSDLE = weekSdleList.get(day).get(j).getMaxProbabiltyAct();


                result.append("SDLE : " + predictedActsBySDLE.get(0).getKey() + "\t" + predictedActsBySDLE.get(0).getValue()
                        + " " + predictedActsBySDLE.get(1).getKey() + " " + predictedActsBySDLE.get(1).getValue() + "\n");
                result.append("Week : " + predictedActsByWeeKSDLE.get(0).getKey() + "\t" + predictedActsByWeeKSDLE.get(0).getValue()
                        + " " + predictedActsByWeeKSDLE.get(1).getKey() + " " + predictedActsByWeeKSDLE.get(1).getValue() + "\n");


                if (predictedActsBySDLE.get(0).getKey().equals(acts[0])) {
                    ++sdleOneDayRight;
                    ++sdleRight;
                    ++sdleTimeIntervalAccuracy[j];
                }

                if (predictedActsByWeeKSDLE.get(0).getKey().equals(acts[0])) {
                    ++weekOneDayRight;
                    ++weekRight;
                }

                //---------------------------------------Week SDLE-----------------------------------------------------------//
                weekSdleList.get(day).get(j).parameterUpdating(oneActs);
                day = (day + 1) % 7;
                //-----------------------------------------------------------------------------------------------------------//
                sdleList.get(j).parameterUpdating(oneActs);

            }
            System.out.println(i + "Week SDLE: " + (double) weekOneDayRight / numOfTimeInterval
                    + " SDLE: " + (double) sdleOneDayRight / numOfTimeInterval);

            sdleOneDayRight = 0;
            weekOneDayRight = 0;
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
            fw.write("\n\n" + sdleSB.toString());
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(trainedDays + "  Week: " + (double) weekRight / totalPredictedInstance
                + " SDLE: " + (double) sdleRight / totalPredictedInstance);
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

                if (j >= 4 && j <= 64)
                    alzIdx = 4;
                else if ((j > 64 && j <= 81) || (j >= 270 && j <= 287) || j < 4)
                    alzIdx = 3;
                else if (j > 81 && j <= 100)
                    alzIdx = 0;
                else if (j > 100 && j <= 225)
                    alzIdx = 1;
                else
                    alzIdx = 2;


                currentActivity = acts[0];
                if (!currentActivity.equals(preActivity) || activeLzTreeArray[alzIdx].getWindow().size() == 0)
                    activeLzTreeArray[alzIdx].step(currentActivity);

                preActivity = currentActivity;
            }
            for (int j = 0; j < activeLzTreeArray.length; j++) {
                activeLzTreeArray[j].finish();
            }
        }

        preActivity = "-1";
        StringBuilder result = new StringBuilder();
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

                if (j >= 4 && j <= 64)
                    alzIdx = 4;
                else if ((j > 64 && j <= 81) || (j >= 270 && j <= 287) || j < 4)
                    alzIdx = 3;
                else if (j > 81 && j <= 100)
                    alzIdx = 0;
                else if (j > 100 && j <= 225)
                    alzIdx = 1;
                else
                    alzIdx = 2;

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
            alzOneDayRight = 0;
            oneDayChange = 0;
            for (int j = 0; j < activeLzTreeArray.length; j++) {
                activeLzTreeArray[j].finish();
            }
        }

        System.out.println(" ALZ: " + (double) alzRight / allChange);
        try {
            FileWriter fw = new FileWriter("ResultImprovrdALZ.txt");
            fw.write(result.toString());

            fw.write("\n\n" + Arrays.toString(alzTimeIntervalAccuracy));

            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void runAZSDLE(int trainedDays) {
        int sum = 0, oneDaySum = 0;
        int right = 0, oneDayRight = 0;
        ActiveLzTree activeLzTree = new ActiveLzTree();
        activeLzTree.init();
        PPM.init(activeLzTree);

        for (int i = 0; i < trainedDays; i++) {
            for (int j = 0; j < instanceLabel.size(); j++) {
                String[] acts = instanceLabel.get(j).get(i).split(",");
                Arrays.sort(acts);
                sdleList.get(j).parameterUpdating(acts);
                StringBuilder sb = new StringBuilder(acts[0]);
                for (int k = 1; k < acts.length; k++) {
                    sb.append("," + acts[k]);
                }
                activeLzTree.step(sb.toString());

            }
            activeLzTree.finish();
        }

        StringBuilder result = new StringBuilder();
        for (int i = trainedDays; i < instanceLabel.get(0).size(); i++) {
            for (int j = 0; j < instanceLabel.size(); j++) {
                String[] acts = instanceLabel.get(j).get(i).split(",");
                Arrays.sort(acts);
                StringBuilder sb = new StringBuilder(acts[0]);
                for (int k = 1; k < acts.length; k++) {
                    sb.append("," + acts[k]);
                }


                result.append("Time : " + j / 12 + ":" + (j * 5) % 60 + "\tActivity : " + sb.toString() + "\n");
                List<Map.Entry<String, Double>> predictedActsBySDLE = sdleList.get(j).getMaxProbabiltyAct();
                List<Map.Entry<String, Double>> predictedActsByALZ = PPM.prediction(0);

                result.append("SDLE : " + predictedActsBySDLE.get(0).getKey() + "\t" + predictedActsBySDLE.get(0).getValue()
                        + " " + predictedActsBySDLE.get(1).getKey() + " " + predictedActsBySDLE.get(1).getValue() + "\n");
                result.append("ALZ : " + predictedActsByALZ.get(0).getKey() + "\t" + predictedActsByALZ.get(0).getValue()
                        + " " + predictedActsByALZ.get(1).getKey() + " " + predictedActsByALZ.get(1).getValue() + "\n\n");

                if (predictedActsByALZ.get(0).getKey().equals(sb.toString()) ||
                        predictedActsBySDLE.get(0).getKey().equals(sb.toString())) {
                    ++right;
                    ++oneDayRight;
                }


                sdleList.get(j).parameterUpdating(acts);

                activeLzTree.step(sb.toString());
                //PPM.addSeenActivity(sb.toString(), 0);
                //++count;
                ++oneDaySum;
                ++sum;
            }
            System.out.println(i + " " + (double) oneDayRight / oneDaySum);
            oneDayRight = 0;
            oneDaySum = 0;
            activeLzTree.finish();
            //PPM.clearSeenActivity(0);


        }
        /*for (int i = 0; i < sdleList.size(); i++) {
            System.out.println(sdleList.get(i).getSum());
        }*/
        try {
            FileWriter fw = new FileWriter("Result.txt");
            fw.write(result.toString());
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(trainedDays + "  " + (double) right / sum);
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
                weekSdleList.get(day).get(j).parameterUpdating(oneActs);
                day = (day + 1) % 7;
                //-----------------------------------------------------------------------------------------------------------//
                sdleList.get(j).parameterUpdating(oneActs);
                //StringBuilder sb = new StringBuilder(acts[0]);
                currentActivity = acts[0];
                if (!currentActivity.equals(preActivity) || activeLzTreeArray[alzIdx].getWindow().size() == 0)
                    activeLzTreeArray[alzIdx].step(currentActivity);
                //-----------------------------------------------------------------------------------------------------------//
                preActivity = currentActivity;
            }
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
                    predictedActsByWeeKSDLE = weekSdleList.get(0).get(j).getMaxProbabiltyAct();
                else
                    predictedActsByWeeKSDLE = weekSdleList.get(1).get(j).getMaxProbabiltyAct();


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
                weekSdleList.get(day).get(j).parameterUpdating(oneActs);
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
                for (int i = 0; i < weekSdleList.size(); i++) {
                    weekSdleList.get(i).add(new SDLE(rh, beta));
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
}
