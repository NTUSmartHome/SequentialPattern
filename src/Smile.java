import smile.math.kernel.GaussianKernel;
import smile.regression.*;
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

    public static Regression RandomForest(ArrayList<Long[]> x, ArrayList<Long> y) {
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
        RandomForest randomForest = new RandomForest(doubleX,doubleY,100);
        int errorMinute = 0;
        int count = 0;
        double[] truth = new double[testX.length];
        double[] predicted = new double[testX.length];
        double maxValue = 0;
        for (int i = 0; i < testX.length; i++) {
            // System.out.println("Real Y : " + testY[i] + "   Predicted Y : " + regressionTree.predict(testX[i]) );
            errorMinute += Math.abs(testY[i] - randomForest.predict(testX[i]));
            count++;
            truth[i] = testY[i];
            predicted[i] = randomForest.predict(testX[i]);
            if (testY[i] > maxValue)
                maxValue = testY[i];
        }
        System.out.println((double) errorMinute);
        RMSE rmse = new RMSE();
        System.out.println(rmse.measure(truth, predicted) / maxValue);
        return randomForest;
    }

    public static Regression GradientTreeBoost(ArrayList<Long[]> x, ArrayList<Long> y) {
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
        GradientTreeBoost gradientTreeBoost = new GradientTreeBoost(doubleX,doubleY,500);
        int errorMinute = 0;
        int count = 0;
        double[] truth = new double[testX.length];
        double[] predicted = new double[testX.length];
        double maxValue = 0;
        for (int i = 0; i < testX.length; i++) {
            // System.out.println("Real Y : " + testY[i] + "   Predicted Y : " + regressionTree.predict(testX[i]) );
            errorMinute += Math.abs(testY[i] - gradientTreeBoost.predict(testX[i]));
            count++;
            truth[i] = testY[i];
            predicted[i] = gradientTreeBoost.predict(testX[i]);
            if (testY[i] > maxValue)
                maxValue = testY[i];
        }
        System.out.println((double) errorMinute);
        RMSE rmse = new RMSE();
        System.out.println(rmse.measure(truth, predicted) / maxValue);
        return gradientTreeBoost;
    }

    public static Regression Mean(ArrayList<Long[]> x, ArrayList<Long> y) {
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
        RidgeRegression ridgeRegression = new RidgeRegression(doubleX,doubleY,0.1);
        int errorMinute = 0;
        int count = 0;
        double[] truth = new double[testX.length];
        double[] predicted = new double[testX.length];
        double maxValue = 0;
        for (int i = 0; i < testX.length; i++) {
            // System.out.println("Real Y : " + testY[i] + "   Predicted Y : " + regressionTree.predict(testX[i]) );
            errorMinute += Math.abs(testY[i] - ridgeRegression.predict(testX[i]));
            count++;
            truth[i] = testY[i];
            predicted[i] = ridgeRegression.predict(testX[i]);
            if (testY[i] > maxValue)
                maxValue = testY[i];
        }
        System.out.println((double) errorMinute);
        RMSE rmse = new RMSE();
        System.out.println(rmse.measure(truth, predicted) / maxValue);
        return ridgeRegression;
    }

    public static Regression RidgeRegression(ArrayList<Long[]> x, ArrayList<Long> y) {
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
        RidgeRegression ridgeRegression = new RidgeRegression(doubleX,doubleY,0.1);
        int errorMinute = 0;
        int count = 0;
        double[] truth = new double[testX.length];
        double[] predicted = new double[testX.length];
        double maxValue = 0;
        for (int i = 0; i < testX.length; i++) {
            // System.out.println("Real Y : " + testY[i] + "   Predicted Y : " + regressionTree.predict(testX[i]) );
            errorMinute += Math.abs(testY[i] - ridgeRegression.predict(testX[i]));
            count++;
            truth[i] = testY[i];
            predicted[i] = ridgeRegression.predict(testX[i]);
            if (testY[i] > maxValue)
                maxValue = testY[i];
        }
        System.out.println((double) errorMinute);
        RMSE rmse = new RMSE();
        System.out.println(rmse.measure(truth, predicted) / maxValue);
        return ridgeRegression;
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
        LASSO LASSO = new LASSO(doubleX,doubleY,0.5);
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

    public static Regression SVR(ArrayList<Long[]> x, ArrayList<Long> y) {
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
        SVR svr = new SVR(doubleX,doubleY,new GaussianKernel(0.5),0.1d,1.0d);
        int errorMinute = 0;
        int count = 0;
        double[] truth = new double[testX.length];
        double[] predicted = new double[testX.length];
        double maxValue = 0;
        for (int i = 0; i < testX.length; i++) {
            // System.out.println("Real Y : " + testY[i] + "   Predicted Y : " + regressionTree.predict(testX[i]) );
            errorMinute += Math.abs(testY[i] - svr.predict(testX[i]));
            count++;
            truth[i] = testY[i];
            predicted[i] = svr.predict(testX[i]);
            if (testY[i] > maxValue)
                maxValue = testY[i];
        }
        System.out.println((double) errorMinute);
        RMSE rmse = new RMSE();
        System.out.println(rmse.measure(truth, predicted) / maxValue);
        return svr;
    }


}
