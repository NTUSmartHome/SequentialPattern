package sdle;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Created by YaHung on 2015/8/24.
 */
public class Activity {
    private final int numberOfActivities;
    private final int possibleSets;
    ArrayList<String> Activities = new ArrayList<String>();
    ArrayList<Double> TOfActs = new ArrayList<Double>();
    ArrayList<Double> QOfActs = new ArrayList<Double>();

    Activity() {
        //For M2 i <= 14, M1 <= 12
        for (int i = 1; i <= 12; i++) {
            String act = String.valueOf(i);
            Activities.add(act);
        }

        // Contain the activity of "Others"
        numberOfActivities = Activities.size();
        possibleSets = numberOfActivities + 1;
        // Multiple Activity in same time slot;
        // possibleSets = (int) Math.pow(2, numberOfActivities);
        initializePOfActs();
    }

    public static void main(String[] args) {
        new Activity().randomGenerateTest();
    }

    public List<Map.Entry<String, Double>> getActsnProb() {

        Map<String, Double> actnProb = new HashMap<>();
        for (int i = 1; i < QOfActs.size(); i++) {
            double prob = QOfActs.get(i);
            String[] tmp = getNameOfActs(i);
            //Arrays.sort(tmp);
            StringBuilder sb = new StringBuilder();
            sb.append(tmp[0]);
            for (int j = 1; j < tmp.length; j++) {
                sb.append("," + tmp[j]);
            }
            actnProb.put(sb.toString(), prob);
        }

        List<Map.Entry<String, Double>> list = new ArrayList<>(actnProb.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<String, Double>>() {
            @Override
            public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {

                return -Double.compare(o1.getValue(), o2.getValue());
            }

        });
        return list;
    }

    public int getActNum() {
        return numberOfActivities;
    }

    public String getAct(int index) {
        return Activities.get(index);
    }

    public String getAct(String act) {
        for (int i = 0; i < Activities.size(); i++)
            if (Activities.get(i).contains(act)) {
                return Activities.get(i);
            }
        return "Others";
    }

    public int getActValue(int index) {
        if (index == 0)
            return 0;
        return (int) Math.pow(2, index - 1);
    }

    //Single activity in one time slot
    private int getActValue(String act) {
        return Integer.parseInt(act);
    }

    //Original one. Now is pure single activity
    private int getActValueMultiActivity(String act) {
        for (int i = 0; i < Activities.size(); i++)
            if (Activities.get(i).contains(act)) {
                return (int) Math.pow(2, i);
            }
        return (int) Math.pow(2, Activities.size());
    }

    public int getPossibleActSet() {
        return possibleSets;
    }

    private void initializePOfActs() {
        for (int i = 0; i < getPossibleActSet(); i++) {
            TOfActs.add(0.0);
            QOfActs.add(0.0);
        }
    }
    // Single Activity version
    public String[] getNameOfActs(int index) {

        return new String[]{String.valueOf(index)};
    }
    // original one. Mutiple activity in one time slot
    public String[] getNameOfActsMultipleActivity(int index ) {
        boolean[] occurActs = new boolean[Activities.size()];

        int quantityOfOccurActs = 0;
        int record = index;

        for (int i = (int) Math.pow(2, Activities.size() - 1), id = Activities.size() - 1; i > 0; i /= 2, id--) {
            if (record >= i) {
                occurActs[id] = true;
                record -= i;
                quantityOfOccurActs++;
            }
        }
        String[] occurActsStr = new String[quantityOfOccurActs];
        for (int i = 0, j = 0; i < Activities.size(); i++) {
            if (occurActs[i]) {
                occurActsStr[j] = Activities.get(i);
                j++;
            }
        }
        return occurActsStr;
    }

    public int getIndexOfActs(String[] OccurActs) {
        int Index = 0;
        for (int i = 0; i < OccurActs.length; i++) {
            Index += getActValue(OccurActs[i]);
        }
        return Index;
    }
    //2016/2/4 new
    public int getIndexOfActs(String OccurActs) {
        int Index = 0;
        Index += getActValue(OccurActs);
        return Index;
    }

    public double getTOfActs(int index) {
        return TOfActs.get(index);
    }

    public double getTOfActs(String[] OccurActs) {
        int index = getIndexOfActs(OccurActs);
        return TOfActs.get(index);
    }
    //2016/2/4 new
    public double getTOfActs(String OccurActs) {
        int index = getIndexOfActs(OccurActs);
        return TOfActs.get(index);
    }
    public void setTOfActs(int index, double value) {
        TOfActs.set(index, value);
    }

    public void setTOfActs(String[] OccurActs, double value) {
        for (int i = 0; i < OccurActs.length; i++) {
            int index = getIndexOfActs(OccurActs);
            TOfActs.set(index, value);
        }
    }
    //2016/2/4 new
    public void setTOfActs(String OccurActs, double value) {

        int index = getIndexOfActs(OccurActs);
        TOfActs.set(index, value);

    }

    public double getQOfActs(int index) {
        return QOfActs.get(index);
    }

    public double getQOfActs(String[] OccurActs) {
        int index = getIndexOfActs(OccurActs);
        return QOfActs.get(index);
    }

    public void setQOfActs(int index, double value) {
        QOfActs.set(index, value);
    }

    public void setQOfActs(String[] OccurActs, double value) {
        int index = getIndexOfActs(OccurActs);
        QOfActs.set(index, value);
    }

    public int getQuantityOfOccurActs(int index) {
        boolean[] occurActs = new boolean[Activities.size()];
        for (int i = 0; i < occurActs.length; i++) {
            occurActs[i] = false;
        }
        int quantityOfOccurActs = 0;
        int record = index;

        for (int i = (int) Math.pow(2, Activities.size() - 1), id = Activities.size() - 1; i > 0; i /= 2, id--) {
            if (record >= i) {
                occurActs[id] = true;
                record -= i;
                quantityOfOccurActs++;
            }
        }
        String[] occurActsStr = new String[quantityOfOccurActs];
        for (int i = 0, j = 0; i < Activities.size(); i++) {
            if (occurActs[i]) {
                occurActsStr[j] = Activities.get(i);
                j++;
            }
        }
        return occurActsStr.length;
    }

    private void randomGenerateTest() {
        FileWriter fw = null;
        try {
            fw = new FileWriter("random_data_set.txt", false);
            for (int i = 0; i < 100; i++) {
                String activity = Activities.get((int) (Math.random() * (numberOfActivities - 1)));

                fw.write(activity + "\n");

            }
            for (int i = 0; i < 50; i++) {
                fw.write(Activities.get(Activities.size() - 1) + "\n");
            }
            fw.flush();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
