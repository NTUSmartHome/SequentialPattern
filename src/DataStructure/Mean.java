package DataStructure;


import smile.regression.Regression;

/**
 * Created by MingJe on 2016/2/29.
 */
public class Mean implements Regression {
    double[] xMean;
    double yMean;
    public Mean(double[][] x, double[]y) {
        xMean = new double[x.length];
        for (int i = 0; i < x.length; i++) {
            xMean[i] = mean(x[i]);
        }
        yMean = mean(y);
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
