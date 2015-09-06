import java.io.*;

/**
 * Created by MingJe on 2015/9/6.
 */
public class SDLEResultParser {
    File SDLEResultIn;
    int timeinterval;
    int option;
    StringBuilder resultPath;

    public SDLEResultParser(int timeinterval, int option) {
        this.timeinterval = timeinterval;
        this.option = option;
        resultPath = new StringBuilder();
        resultPath.append("report/db/");
        resultPath.append(timeinterval);
        switch (option) {
            case 0:
                resultPath.append("second/");
                break;
            case 1:
                resultPath.append("minute/");
                break;
            case 2:
                resultPath.append("hour/");
                break;
            case 3:
                resultPath.append("day/");
                break;
        }
        parse();
    }

    public void parse() {

        for (int i = 0; i < 24; i++) {
            SDLEResultIn = new File((resultPath.toString() + "SDLE" + i + "h0m.txt"));
            StringBuilder label = new StringBuilder();
            StringBuilder prob = new StringBuilder();
            if (SDLEResultIn.exists()) {
                try {
                    FileReader fr = new FileReader(SDLEResultIn);
                    BufferedReader br = new BufferedReader(fr);
                    while (br.ready()) {
                        String[] s = br.readLine().split(":");
                        label.append(s[0] + "\t");
                        prob.append(s[1] + "\t");
                    }
                    new File(resultPath.toString() + "Excel/").mkdirs();
                    FileWriter fw = new FileWriter(resultPath.toString() + "Excel/" + "SDLE" + i + "h0m.txt");
                    fw.write(label.substring(0, label.length() - 1) + "\r\n");
                    fw.flush();
                    fw.write(prob.substring(0, prob.length() - 1));
                    fw.flush();
                    fw.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }


    }

    public static void main(String[] args){
        SDLEResultParser resultParser = new SDLEResultParser(1, 2);

    }

}
