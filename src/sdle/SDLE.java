package sdle;

import java.io.*;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by YaHung on 2015/8/24.
 */

public class SDLE {

    String InputFile;
    String OutputFile;
    final double printThreshold = 0.0;
    Activity A = new Activity();
    int t = 1;
    double rh;
    double beta;
    double k;

    double sumOfActivityEvent;

    public SDLE(String InputFile, String OutputFile, double rh, double beta) {
        this.InputFile = InputFile;
        this.OutputFile = OutputFile;
        this.rh = rh;
        this.beta = beta;
        t = 1;
        k = A.getPossibleActSet();
        parameterUpdating();
        printResult();
    }
    public SDLE(double rh, double beta) {
        this.rh = rh;
        this.beta = beta;
        t= 1;
        k = A.getPossibleActSet();
    }
    public void parameterUpdating(String[] Acts) {
        updateDiscountingOfT();
        A.setTOfActs(Acts, A.getTOfActs(Acts) + 1);
        sumOfActivityEvent++;
        t++;
        updateDiscountingOfQ();
    }
    private void parameterUpdating() {
        try {
            FileReader fr = new FileReader(InputFile);
            BufferedReader br = new BufferedReader(fr);
            String line;
            try {
                while ((line = br.readLine()) != null) {

                    updateDiscountingOfT();

                    String[] Acts = line.split(",");

                    A.setTOfActs(Acts, A.getTOfActs(Acts) + 1);
                    sumOfActivityEvent++;
                    t++;
                }
                updateDiscountingOfQ();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    public List<Map.Entry<String, Double>> getMaxProbabiltyAct() {
        return A.getActsnProb();
    }
    public void printResult() {
        try {
            FileWriter fw = new FileWriter(OutputFile);
            double value = A.getQOfActs(1);
            double sum;
            if (rh != 0) sum = beta / (((1 - Math.pow(1 - rh, t)) / rh) + k * beta);
            else sum = 0;

            for (int i = 1; i < A.getPossibleActSet(); i++) {
                value = A.getQOfActs(i);
                if (value > printThreshold) {
                    NumberFormat nf = NumberFormat.getInstance();
                    nf.setMaximumFractionDigits(8);

                    String[] occurActs = A.getNameOfActs(i);
                    fw.write(Arrays.toString(occurActs));

                    fw.write(": ");
                    fw.write(nf.format(value) + "\n");
                }
            }

            for (int i = 1; i < A.getPossibleActSet(); i++) {
                value = A.getQOfActs(i);
                sum += value;
            }

            fw.write("Sum:" + sum + "\r\n");
            fw.flush();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateDiscountingOfT() {
        for (int i = 1; i < A.getPossibleActSet(); i++) {
            A.setTOfActs(i, (1 - rh) * A.getTOfActs(i));
        }
    }

    private void updateDiscountingOfQ() {
        t--;
        for (int i = 1; i < A.getPossibleActSet(); i++) {

            if (rh != 0)
                A.setQOfActs(i, (A.getTOfActs(i) + beta) / (((1 - Math.pow(1 - rh, t)) / rh) + k * beta));
            else
                A.setQOfActs(i, A.getTOfActs(i) / sumOfActivityEvent);
        }
    }

    public void setOutputFile(String outputFile) {
        this.OutputFile = outputFile;
    }

    public static void main(String[] args) {
        new SDLE("db/SDLE1.txt", "result.txt", 0.01, 0.0001);
    }
}
