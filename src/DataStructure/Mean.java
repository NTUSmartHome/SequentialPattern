package DataStructure;


import smile.regression.Regression;

/**
 * Created by MingJe on 2016/2/29.
 */
public class Mean implements Regression {
    double[] xMean;
    double yMean;

    public Mean(double[][] x, double[] y) {
        xMean = new double[x.length];
        for (int i = 0; i < x.length; i++) {
            xMean[i] = mean(x[i]);
        }
        yMean = mean(y);
    }

    public static double staticMean(double[] x) {
        double mean = 0;
        for (int i = 0; i < x.length; i++) {
            mean += x[i];
        }
        return mean / x.length;
    }

    public static double staticVariance(double[] x) {
        double mean = 0;
        double meansqure = 0;
        double variance = 0;
        for (int i = 0; i < x.length; i++) {
            mean += x[i];
            meansqure += Math.pow(x[i],2);
        }
        for (int i = 0; i < x.length; i++) {
            variance += Math.pow((x[i] - mean / x.length),2);
        }
        return Math.sqrt(variance / x.length);
    }

    public double mean(double[] x) {
        double mean = 0;
        for (int i = 0; i < x.length; i++) {
            mean += x[i];
        }
        return mean / x.length;
    }

    @Override
    public double predict(Object o) {
        return yMean;
    }
}
