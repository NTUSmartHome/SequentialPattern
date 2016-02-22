package tool;

import DataStructure.ActivityInstance;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by MingJe on 2016/2/8.
 */
public class ActivityInstanceParser {
    // Parse out activity instance using original wsu data format.
    // This way without others
    public static ArrayList<ActivityInstance>[] original(int trainedDays, Map<String, Integer> resultMap) throws IOException, ParseException {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
        SimpleDateFormat weekFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        FileReader fr = new FileReader("db/data");
        BufferedReader br = new BufferedReader(fr);
        String line;
        String startTime = "";
        String startDate = "";
        String activity = "";
        String startDay = null;
        ArrayList<ActivityInstance>[] week = new ArrayList[resultMap.size()];
        for (int i = 0; i < week.length; i++) {
            week[i] = new ArrayList<>();
        }
        Calendar calendar = Calendar.getInstance();
        while ((line = br.readLine()) != null) {
            String[] data = line.split("\\s+");
            if (data.length == 6) {
                if (startDay == null) {
                    startDay = data[0] + " " + data[1];
                }
                if (data[5].equals("begin")) {
                    activity = data[4];
                    startDate = data[0];
                    startTime = data[1];
                } else if (data[5].equals("end")) {
                    calendar.setTime(weekFormat.parse(startDate + " " + startTime));
                    int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
                    int dayOfGroup = resultMap.get(String.valueOf(dayOfWeek - 1));
                    // unit: minute
                    long duration = (weekFormat.parse(data[0] + " " + data[1]).getTime()
                            - weekFormat.parse(startDate + " " + startTime).getTime()) / 60000;

                    week[dayOfGroup].add(new ActivityInstance(activity, startTime, duration));

                    long differenceOfDays = (weekFormat.parse(data[0] + " " + data[1]).getTime()
                            - weekFormat.parse(startDay).getTime()) / 86400000;
                    if (differenceOfDays >= trainedDays) {
                        break;
                    }
                }
            }
        }
        br.close();
        fr.close();

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < week.length; i++) {
            FileWriter fw = new FileWriter("report/ActivityInstance/" + i);
            for (int j = 0; j < week[i].size(); j++) {
                ActivityInstance activityInstance = week[i].get(j);
                sb.append(activityInstance.toSting() + "\n");
            }
            fw.write(sb.toString());
            fw.close();
            sb = new StringBuilder();
        }
        return week;
    }

    // Parse out activity instance using yichongzeng wsu data format.
    // This way with others. all activity > 11
    public static ArrayList<ActivityInstance>[][] yin(int trainedDays, Map<String, Integer> resultMap) throws IOException, ParseException {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
        FileReader fr = new FileReader("db/DB_M1_app.txt");
        BufferedReader br = new BufferedReader(fr);
        String line;
        String preActivity = "";
        String startTime = null;
        String startDay = null;
        SimpleDateFormat weekFormat = new SimpleDateFormat("yyyy.MM.dd-HH:mm:ss");
        Calendar calendar = Calendar.getInstance();
        ArrayList<ActivityInstance>[][] total = new ArrayList[2][resultMap.size()];
        ArrayList<ActivityInstance>[] week = new ArrayList[resultMap.size()];
        ArrayList<ActivityInstance>[] testWeek = new ArrayList[resultMap.size()];
        //long startTimestamp = 1010;
        for (int i = 0; i < week.length; i++) {
            week[i] = new ArrayList<>();
            testWeek[i] = new ArrayList<>();
        }
        total[0] = week;
        total[1] = testWeek;

        while ((line = br.readLine()) != null) {
            line = line.replace("{", "").replace("\"", "").replace(" ", "");
            String[] rawData = line.split("[:}]+");
            String activity = rawData[rawData.length - 1];
            if (Integer.parseInt(activity) > 12) {
                activity = "12";
            }
            if (activity.equals("7")) {
                activity = "7";
            }
            long unixTimestamp = Integer.valueOf(rawData[rawData.length - 2].substring(0, 10));
            //System.out.println(unixTimestamp);
            String date = weekFormat.format(new java.util.Date(unixTimestamp * 1000));
            if (preActivity.equals("")) {
                preActivity = activity;
                startTime = date;
                startDay = date;
                //startTimestamp = unixTimestamp;
            }
            if (!preActivity.equals(activity)) {
                //unit:minute
                long duration = (weekFormat.parse(date).getTime() - weekFormat.parse(startTime).getTime()) / 60000;


                calendar.setTime(weekFormat.parse(startTime));
                int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
                int dayOfGroup = resultMap.get(String.valueOf(dayOfWeek - 1));
                if (duration >= 0)
                    week[dayOfGroup].add(new ActivityInstance(preActivity, startTime.split("-")[1], duration));
                startTime = date;
                //startTimestamp = unixTimestamp;
                preActivity = activity;
                long differenceOfDays = (weekFormat.parse(date).getTime() - weekFormat.parse(startDay).getTime()) / 86400000;
                if (differenceOfDays >= trainedDays) {
                    break;
                }
            }
        }


        while ((line = br.readLine()) != null) {
            line = line.replace("{", "").replace("\"", "").replace(" ", "");
            String[] rawData = line.split("[:}]+");
            String activity = rawData[rawData.length - 1];
            if (Integer.parseInt(activity) > 12) {
                activity = "12";
            }
            long unixTimestamp = Integer.valueOf(rawData[rawData.length - 2].substring(0, 10));
            //System.out.println(unixTimestamp);
            String date = weekFormat.format(new java.util.Date(unixTimestamp * 1000));
            if (preActivity.equals("")) {
                preActivity = activity;
                startTime = date;
                startDay = date;
                //startTimestamp = unixTimestamp;
            }
            if (!preActivity.equals(activity)) {
                //unit:minute
                long duration = (weekFormat.parse(date).getTime() - weekFormat.parse(startTime).getTime()) / 60000;


                calendar.setTime(weekFormat.parse(startTime));
                int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
                int dayOfGroup = resultMap.get(String.valueOf(dayOfWeek - 1));
                if (duration >= 0)
                    testWeek[dayOfGroup].add(new ActivityInstance(preActivity, startTime.split("-")[1], duration));
                startTime = date;
                //startTimestamp = unixTimestamp;
                preActivity = activity;
                long differenceOfDays = (weekFormat.parse(date).getTime() - weekFormat.parse(startDay).getTime()) / 86400000;

            }
        }


        br.close();
        fr.close();

        StringBuilder train = new StringBuilder();
        StringBuilder test = new StringBuilder();
        for (int i = 0; i < week.length; i++) {
            FileWriter fw = new FileWriter("report/ActivityInstance/" + i);
            FileWriter fwTest = new FileWriter("report/ActivityInstance/" + i + "_Test");
            for (int j = 0; j < week[i].size(); j++) {
                ActivityInstance activityInstance = week[i].get(j);
                train.append(activityInstance.toSting() + "\n");
            }
            for (int j = 0; j < testWeek[i].size(); j++) {
                ActivityInstance activityInstance = testWeek[i].get(j);
                test.append(activityInstance.toSting() + "\n");
            }
            fw.write(train.toString());
            fw.close();
            fwTest.write(test.toString());
            train = new StringBuilder();
            test = new StringBuilder();
        }

        return total;
    }

    // Parse out activity instance using MING data format.
    // This way with others
    public static ArrayList<ActivityInstance>[][] Ming(int trainedDays, Map<String, Integer> resultMap) throws IOException, ParseException {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
        FileReader fr = new FileReader("db/MingT.csv");
        BufferedReader br = new BufferedReader(fr);
        String line;
        String startTime = "";
        String startDay = null;
        String preActivity = "";
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat original = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        SimpleDateFormat weekFormat = new SimpleDateFormat("yyyy.MM.dd-HH:mm:ss");
        ArrayList<ActivityInstance>[][] total = new ArrayList[2][resultMap.size()];
        ArrayList<ActivityInstance>[] week = new ArrayList[resultMap.size()];
        ArrayList<ActivityInstance>[] testWeek = new ArrayList[resultMap.size()];

        //long startTimestamp = 1010;
        for (int i = 0; i < week.length; i++) {
            week[i] = new ArrayList<>();
            testWeek[i] = new ArrayList<>();
        }
        total[0] = week;
        total[1] = testWeek;
        String preDate = "";
        while ((line = br.readLine()) != null) {
            String[] rawData = line.split(",");
            String activity = rawData[0];

            long unixTimestamp = original.parse(rawData[1]).getTime() / 1000;
            //System.out.println(unixTimestamp);
            String date = weekFormat.format(new java.util.Date(unixTimestamp * 1000 + 5000));
            if (preActivity.equals("")) {
                preActivity = activity;
                startTime = date;
                startDay = date;
                //startTimestamp = unixTimestamp;
            }
            if (!preActivity.equals(activity)) {
                //unit:minute
                long duration = (weekFormat.parse(preDate).getTime() - weekFormat.parse(startTime).getTime()) / 60000;


                calendar.setTime(weekFormat.parse(startTime));
                int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
                int dayOfGroup = resultMap.get(String.valueOf(dayOfWeek - 1));
                if (duration >= 0)
                    week[dayOfGroup].add(new ActivityInstance(preActivity, startTime.split("-")[1], duration));
                startTime = date;
                //startTimestamp = unixTimestamp;
                preActivity = activity;
                long differenceOfDays = (weekFormat.parse(date).getTime() - weekFormat.parse(startDay).getTime()) / 86400000;
                if (differenceOfDays >= trainedDays) {
                    break;
                }
            }
            preDate = date;
        }


        while ((line = br.readLine()) != null) {
            String[] rawData = line.split(",");
            String activity = rawData[0];

            long unixTimestamp = original.parse(rawData[1]).getTime() / 1000;
            //System.out.println(unixTimestamp);
            String date = weekFormat.format(new java.util.Date(unixTimestamp * 1000 + 5000));
            if (preActivity.equals("")) {
                preActivity = activity;
                startTime = date;
                startDay = date;
                //startTimestamp = unixTimestamp;
            }
            if (!preActivity.equals(activity)) {
                //unit:minute
                long duration = (weekFormat.parse(preDate).getTime() - weekFormat.parse(startTime).getTime()) / 60000;


                calendar.setTime(weekFormat.parse(startTime));
                int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
                int dayOfGroup = resultMap.get(String.valueOf(dayOfWeek - 1));
                if (duration >= 0)
                    testWeek[dayOfGroup].add(new ActivityInstance(preActivity, startTime.split("-")[1], duration));
                startTime = date;
                //startTimestamp = unixTimestamp;
                preActivity = activity;
                long differenceOfDays = (weekFormat.parse(date).getTime() - weekFormat.parse(startDay).getTime()) / 86400000;

            }
            preDate = date;
        }


        br.close();
        fr.close();

        StringBuilder train = new StringBuilder();
        StringBuilder test = new StringBuilder();
        for (int i = 0; i < week.length; i++) {
            FileWriter fw = new FileWriter("report/ActivityInstance/" + i);
            FileWriter fwTest = new FileWriter("report/ActivityInstance/" + i + "_Test");
            for (int j = 0; j < week[i].size(); j++) {
                ActivityInstance activityInstance = week[i].get(j);
                train.append(activityInstance.toSting() + "\n");
            }
            for (int j = 0; j < testWeek[i].size(); j++) {
                ActivityInstance activityInstance = testWeek[i].get(j);
                test.append(activityInstance.toSting() + "\n");
            }
            fw.write(train.toString());
            fw.close();
            fwTest.write(test.toString());
            train = new StringBuilder();
            test = new StringBuilder();
        }

        return total;
    }

    public static void main(String[] args) {
        try {
            Map<String, Integer> resultMap = new HashMap<>();
            for (int i = 0; i < 7; i++) {
                resultMap.put(String.valueOf(i), i);
            }
            yin(7, resultMap);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

}
