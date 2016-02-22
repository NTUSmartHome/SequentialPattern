import smile.regression.LASSO;
import smile.regression.Regression;
import smile.regression.RegressionTree;
import smile.validation.RMSE;

import java.util.ArrayList;

/**
 * Created by MingJe on 2016/2/21.
 */
public class Smile {
    public static Regression RegressionTree(ArrayList<Long[]> x, ArrayList<Long> y) {
        double[][] doubleX = new double[x.size()][1];
        double[] doubleY = new double[y.size()];
        double[][] testX = new double[x.size()][1];
        double[] testY = new double[y.size()];
        for (int i = 0; i < x.size(); i++) {
            doubleX[i][0] = x.get(i)[0];
            doubleY[i] = y.get(i);
            testX[i][0] = doubleX[i][0];
            testY[i] = doubleY[i];
        }
        RegressionTree regressionTree = new RegressionTree(doubleX,doubleY,100);
        int errorMinute = 0;
        int count = 0;
        double[] truth = new double[testX.length];
        double[] predicted = new double[testX.length];
        double maxValue = 0;
        for (int i = 0; i < testX.length; i++) {
           // System.out.println("Real Y : " + testY[i] + "   Predicted Y : " + regressionTree.predict(testX[i]) );
            errorMinute += Math.abs(testY[i] - regressionTree.predict(testX[i]));
            count++;
            truth[i] = testY[i];
            predicted[i] = regressionTree.predict(testX[i]);
            if (testY[i] > maxValue)
                maxValue = testY[i];
        }
        System.out.println((double) errorMinute);
        RMSE rmse = new RMSE();
        System.out.println(rmse.measure(truth, predicted) / maxValue);
        return regressionTree;
    }

    public static Regression LASSO(ArrayList<Long[]> x, ArrayList<Long> y) {
        double[][] doubleX = new double[x.size()][1];
        double[] doubleY = new double[y.size()];
        double[][] testX = new double[x.size()][1];
        double[] testY = new double[y.size()];
        for (int i = 0; i < x.size(); i++) {
            doubleX[i][0] = x.get(i)[0];
            doubleY[i] = y.get(i);
            testX[i][0] = doubleX[i][0];
            testY[i] = doubleY[i];
        }
        LASSO LASSO = new LASSO(doubleX,doubleY,100);
        int errorMinute = 0;
        int count = 0;
        double[] truth = new double[testX.length];
        double[] predicted = new double[testX.length];
        double maxValue = 0;
        for (int i = 0; i < testX.length; i++) {
            // System.out.println("Real Y : " + testY[i] + "   Predicted Y : " + regressionTree.predict(testX[i]) );
            errorMinute += Math.abs(testY[i] - LASSO.predict(testX[i]));
            count++;
            truth[i] = testY[i];
            predicted[i] = LASSO.predict(testX[i]);
            if (testY[i] > maxValue)
                maxValue = testY[i];
        }
        System.out.println((double) errorMinute);
        RMSE rmse = new RMSE();
        System.out.println(rmse.measure(truth, predicted) / maxValue);
        return LASSO;
    }
}
