package ntu;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by MingJe on 2016/2/22.
 */
public class MingParser {
    public static void main(String[] args) {
        try {
            FileReader fr = new FileReader("db/Ming.csv");
            BufferedReader br = new BufferedReader(fr);
            String line;
            FileWriter fw = new FileWriter("db/MingT.csv");
            while((line = br.readLine()) != null) {
                String[] data = line.split(",");
                switch (data[0]) {
                    case "Sleeping":
                        data[0] = "1";
                        break;
                    case "Bathing":
                        data[0] = "2";
                        break;
                    case "UsingPC":
                        data[0] = "3";
                        break;
                    case "Leave":
                        data[0] = "4";
                        break;
                    case "Enter":
                        data[0] = "5";
                        break;
                    case "Eating":
                        data[0] = "6";
                        break;
                    case "Meeting":
                        data[0] = "7";
                        break;


                }
                fw.write(data[0] + "," + data[1] + "\n");
                fw.write(data[0] + "," + data[2] + "\n");
            }
            br.close();
            fr.close();
            fw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
