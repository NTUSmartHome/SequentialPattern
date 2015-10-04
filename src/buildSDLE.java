import java.io.File;

/**
 * Created by YaHung on 2015/8/31.
 */
public class buildSDLE {

    int timeInterval;

    public buildSDLE(int interval, int option, double rh, double beta) {
        DB db = new DB(interval, option, true);
        this.timeInterval = db.getTimeInterval();
       // int option = db.getOption();
        int id = 0;
        StringBuilder inputFile = new StringBuilder("db/");

        switch (db.getOption()) {
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
            String outputFile = getOutputFilename(id, inputFile.toString());
            SDLE sdle = new SDLE(fileString, outputFile, rh, beta);
            id++;
            fileString = inputFile.toString() + id + ".txt";
            file = new File(fileString);
        }

    }

    private String getOutputFilename(int id, String base) {
        String time = "";
        int currentTime = id * timeInterval;
        int s = currentTime % 60;
        currentTime -= s;
        currentTime /= 60;
        int m = currentTime % 60;
        currentTime -= m;
        currentTime /= 60;
        int h = currentTime;
        //return "report/SDLE_" + h + "h" + m + "m.txt";
        new File("report/" + base).mkdirs();
        return "report/" + base + "SDLE" + h + "h" + m + "m.txt";
    }

    public static void main(String[] args) {
        final int option = 1;
        final int timeInterval = 5;
        final double rh = 0.01;
        final double beta = 0.01;
       // new WSUParser(timeInterval, option);
        new buildSDLE(timeInterval, option, rh, beta);
    }


}
