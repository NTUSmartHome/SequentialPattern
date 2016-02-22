package tool;

import sdle.DB;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

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
            String[] preData = null;
            DB db = new DB(5,1);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (preData != null) {
                    Date preDate = simpleDateFormat.parse(preData[2]);
                    Date curDate = simpleDateFormat.parse(data[1]);
                    int cur = db.belongToWhichSDLE(curDate.getTime() / 1000);
                    int pre = db.belongToWhichSDLE(preDate.getTime() / 1000);
                    if ((cur - pre) > 1) {
                        preDate.setTime(preDate.getTime() + 60*1000*5);
                        fw.write("8," + simpleDateFormat.format(preDate)+"\n");
                        preDate.setTime(curDate.getTime() - 60*1000*5);
                        fw.write("8," + simpleDateFormat.format(preDate)+"\n");

                    }

                }
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
                preData = data;
            }
            br.close();
            fr.close();
            fw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
