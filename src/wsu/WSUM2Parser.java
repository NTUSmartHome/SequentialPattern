package wsu;

import javafx.scene.input.DataFormat;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by MingJe on 2015/11/27.
 */
public class WSUM2Parser {
    public static void main(String[] args) {
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
            FileReader fr = new FileReader(new File("db/DB_M2_ori.txt"));
            BufferedReader br = new BufferedReader(fr);
            String line;
            StringBuilder result = new StringBuilder();
            //14 means no activity in DB_M2, because they have 13 activities
            int label = 14;
            boolean isEnd = false;
            String[] pre = new String[0];
            Date preDate = null;
            int preLabel = -1;
            while ((line = br.readLine()) != null) {
                String[] data = line.split("\\s+");
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                if (data.length < 2) {
                    continue;
                }
                String dateString = data[0] + " " + data[1];
                Date date = null;
                try {
                    date = sdf.parse(dateString);
                } catch (ParseException e) {
                    //e.printStackTrace();
                    dateString += ".000831";
                    try {
                        date = sdf.parse(dateString);
                    } catch (ParseException e1) {
                        e1.printStackTrace();
                    }
                }

                if (isEnd) {
                    result.append("{\"deviceName\": \"" + pre[2] + "\", \"message\":\""
                            + pre[3] + "\",\"date\":" + preDate.getTime() + "} ");
                    result.append(label + "\n");

                    isEnd = false;
                    label = 14;


                }


                if (data.length > 4) {
                    if (data[4].contains("R2_Bathing") && data[5].equals("begin")) {
                        label = 1;
                    } else if (data[4].contains("R2_Bed_Toilet_Transition") && data[5].equals("begin")) {
                        label = 2;
                    } else if (data[4].contains("R2_Eating") && data[5].equals("begin")) {
                        label = 3;
                    } else if (data[4].contains("R2_Enter_Home") && data[5].equals("begin")) {
                        label = 4;
                    } else if (data[4].contains("R2_Housekeeping") && data[5].equals("begin")) {
                        label = 5;
                    } else if (data[4].contains("R2_Leave_Home") && data[5].equals("begin")) {
                        label = 6;
                    } else if (data[4].contains("R2_Meal_Preparation") && data[5].equals("begin")) {
                        label = 7;
                    } else if (data[4].contains("R2_Personal_Hygiene") && data[5].equals("begin")) {
                        label = 8;
                    } else if (data[4].contains("R2_Sleep") && data[5].equals("begin")) {
                        label = 9;
                    } else if (data[4].contains("R2_Sleeping_Not_in_Bed") && data[5].equals("begin")) {
                        label = 10;
                    } else if (data[4].contains("R2_Wandering_in_room") && data[5].equals("begin")) {
                        label = 11;
                    } else if (data[4].contains("R2_Watch_TV") && data[5].equals("begin")) {
                        label = 12;
                    } else if (data[4].contains("R2_Work") && data[5].equals("begin")) {
                        label = 13;
                    } else if (data[4].contains("R2") && data[5].equals("end")) {
                        isEnd = true;
                    }
                }

                if (preLabel != label) {
                    result.append("{\"deviceName\": \"" + data[2] + "\", \"message\":\""
                            + data[3] + "\",\"date\":" + date.getTime() + "} ");
                    result.append(label + "\n");
                }
                pre = data;
                preDate = date;
                preLabel = label;
            }
            FileWriter fw = new FileWriter("db/DB_M2_appR2.txt");
            fw.write(result.toString());
            fw.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {

            e.printStackTrace();
        }
    }
}
