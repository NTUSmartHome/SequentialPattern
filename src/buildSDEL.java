import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

/**
 * Created by YaHung on 2015/8/31.
 */
public class buildSDEL {

    int timeInterval;

    public buildSDEL(double rh, double beta){
        DB db = new DB();
        this.timeInterval = db.getTimeInterval();
        int id = 0;
        String inputFile = "db/SDLE" + id + ".txt";
        File file = new File(inputFile);
        while(file.exists()){

            String outputFile = getOutputFilename(id);

            SDLE sdle = new SDLE(inputFile,outputFile,rh,beta);
            id++;
            inputFile = "db/SDLE" + id + ".txt";
            file = new File(inputFile);
        }

    }

    private String getOutputFilename(int id){
        String time = "";
        int currentTime = id*timeInterval;
        int s = currentTime%60;
        currentTime -= s;
        currentTime /= 60;
        int m = currentTime%60;
        currentTime -= m;
        currentTime /= 60;
        int h = currentTime;
        return "report/SDLE_" + h + "h" + m + "m.txt";
    }

    public static void main(String[] args) {
        new buildSDEL(0.01, 0.01);
    }


}
