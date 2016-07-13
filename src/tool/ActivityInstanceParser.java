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
    // Parse out activity instance using original wsu M1 data format.
    // This way without others
    public static ArrayList<ActivityInstance>[][] original(int trainedDays, Map<String, Integer> resultMap) throws IOException, ParseException {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
        SimpleDateFormat weekFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        FileReader fr = new FileReader("db/DB_M1_ori.txt");
        BufferedReader br = new BufferedReader(fr);
        String line;
        String startTime = "";
        String startDate = "";
        String activity = "";
        String startDay = null;
        ArrayList<ActivityInstance>[][] total = new ArrayList[2][resultMap.size()];
        ArrayList<ActivityInstance>[] week = new ArrayList[resultMap.size()];
        ArrayList<ActivityInstance>[] testWeek = new ArrayList[resultMap.size()];
        for (int i = 0; i < week.length; i++) {
            week[i] = new ArrayList<>();
            testWeek[i] = new ArrayList<>();
        }
        total[0] = week;
        total[1] = testWeek;

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
                    if (duration < 0) {
                        System.out.println("test");
                    }
                    week[dayOfGroup].add(new ActivityInstance(activity, startTime, data[1], duration, dayOfWeek - 1, startDate, data[0]));

                    long differenceOfDays = (weekFormat.parse(data[0] + " " + data[1]).getTime()
                            - weekFormat.parse(startDay).getTime()) / 86400000;
                    if (differenceOfDays >= trainedDays) {
                        break;
                    }
                }
            }
        }

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

                    testWeek[dayOfGroup].add(new ActivityInstance(activity, startTime, data[1], duration, dayOfWeek - 1, startDate, data[0]));

                }
            }
        }

        br.close();
        fr.close();

        StringBuilder sb = new StringBuilder();
        StringBuilder sbTest = new StringBuilder();
        for (int i = 0; i < week.length; i++) {
            FileWriter fw = new FileWriter("report/ActivityInstance/" + i);
            FileWriter fwTest = new FileWriter("report/ActivityInstance/" + i + "_Test");
            for (int j = 0; j < week[i].size(); j++) {
                ActivityInstance activityInstance = week[i].get(j);
                sb.append(activityInstance.toSting() + "\n");
            }
            for (int j = 0; j < testWeek[i].size(); j++) {
                ActivityInstance activityInstance = testWeek[i].get(j);
                sbTest.append(activityInstance.toSting() + "\n");
            }
            fw.write(sb.toString());
            fwTest.write(sbTest.toString());
            fw.close();
            fwTest.close();
            sb = new StringBuilder();
            sbTest = new StringBuilder();
        }
        return total;
    }

    // Parse out activity instance using original wsu M2 data format.
    // This way without others
    public static ArrayList<ActivityInstance>[][] M2Original(int trainedDays, Map<String, Integer> resultMap) throws IOException, ParseException {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
        SimpleDateFormat weekFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        FileReader fr = new FileReader("db/DB_M2_ori.txt");
        BufferedReader br = new BufferedReader(fr);
        String line;
        String startTime = "";
        String startDate = "";
        String activity = "";
        String startDay = null;
        ArrayList<ActivityInstance>[][] total = new ArrayList[2][resultMap.size()];
        ArrayList<ActivityInstance>[] week = new ArrayList[resultMap.size()];
        ArrayList<ActivityInstance>[] testWeek = new ArrayList[resultMap.size()];
        for (int i = 0; i < week.length; i++) {
            week[i] = new ArrayList<>();
            testWeek[i] = new ArrayList<>();
        }
        total[0] = week;
        total[1] = testWeek;

        Calendar calendar = Calendar.getInstance();
        while ((line = br.readLine()) != null) {
            String[] data = line.split("\\s+");

            if (data.length == 6) {
                if (data[4].contains("R1_Housekeeping")) continue;
                if (startDay == null) {
                    startDay = data[0] + " " + data[1];
                }
                if (data[5].equals("begin") && data[4].contains("R1")) {
                    activity = data[4];
                    startDate = data[0];
                    startTime = data[1];
                } else if (data[5].equals("end") && data[4].contains("R1")) {
                    calendar.setTime(weekFormat.parse(startDate + " " + startTime));
                    int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
                    int dayOfGroup = resultMap.get(String.valueOf(dayOfWeek - 1));
                    // unit: minute
                    long duration = (weekFormat.parse(data[0] + " " + data[1]).getTime()
                            - weekFormat.parse(startDate + " " + startTime).getTime()) / 60000;

                    week[dayOfGroup].add(new ActivityInstance(activity, startTime, data[1], duration, dayOfWeek - 1, startDate, data[0]));

                    long differenceOfDays = (weekFormat.parse(data[0] + " " + data[1]).getTime()
                            - weekFormat.parse(startDay).getTime()) / 86400000;
                    if (differenceOfDays >= trainedDays) {
                        break;
                    }
                }
            }
        }

        while ((line = br.readLine()) != null) {
            String[] data = line.split("\\s+");
            if (data.length == 6) {
                if (startDay == null) {
                    startDay = data[0] + " " + data[1];
                }
                if (data[5].equals("begin") && data[4].contains("R1")) {
                    activity = data[4];
                    startDate = data[0];
                    startTime = data[1];
                } else if (data[5].equals("end") && data[4].contains("R1")) {
                    calendar.setTime(weekFormat.parse(startDate + " " + startTime));
                    int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
                    int dayOfGroup = resultMap.get(String.valueOf(dayOfWeek - 1));
                    // unit: minute
                    long duration = (weekFormat.parse(data[0] + " " + data[1]).getTime()
                            - weekFormat.parse(startDate + " " + startTime).getTime()) / 60000;

                    testWeek[dayOfGroup].add(new ActivityInstance(activity, startTime, data[1], duration, dayOfWeek - 1, startDate, data[0]));

                }
            }
        }

        br.close();
        fr.close();

        StringBuilder sb = new StringBuilder();
        StringBuilder sbTest = new StringBuilder();
        for (int i = 0; i < week.length; i++) {
            FileWriter fw = new FileWriter("report/ActivityInstance/" + i);
            FileWriter fwTest = new FileWriter("report/ActivityInstance/" + i + "_Test");
            for (int j = 0; j < week[i].size(); j++) {
                ActivityInstance activityInstance = week[i].get(j);
                sb.append(activityInstance.toSting() + "\n");
            }
            for (int j = 0; j < testWeek[i].size(); j++) {
                ActivityInstance activityInstance = testWeek[i].get(j);
                sbTest.append(activityInstance.toSting() + "\n");
            }
            fw.write(sb.toString());
            fwTest.write(sbTest.toString());
            fw.close();
            fwTest.close();
            sb = new StringBuilder();
            sbTest = new StringBuilder();
        }
        return total;
    }

    // Parse out activity instance using yichongzeng wsu data format.
    // This way with others which idx > 11
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
                    week[dayOfGroup].add(new ActivityInstance(preActivity, startTime.split("-")[1], date.split("-")[1], duration, dayOfWeek - 1, startTime.split("-")[0], date.split("-")[0]));
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
                    testWeek[dayOfGroup].add(new ActivityInstance(preActivity, startTime.split("-")[1], date.split("-")[1], duration, dayOfWeek - 1, startTime.split("-")[0], date.split("-")[0]));
                startTime = date;
                //startTimestamp = unixTimestamp;
                preActivity = activity;

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
    public static ArrayList<ActivityInstance>[][] MingOriginal(int trainedDays, Map<String, Integer> resultMap) throws IOException, ParseException {

        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
        FileReader fr = new FileReader("db/pei-hsuan_final.csv");
        BufferedReader br = new BufferedReader(fr);
        String line;
        String startTime = "";
        String startDay = "";
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat original = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        SimpleDateFormat weekFormat = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
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
        boolean isFirst = true;
        while ((line = br.readLine()) != null) {

            String[] rawData = line.split(",");
            String activity = rawData[0];

            long startUnixTimestamp = original.parse(rawData[1]).getTime() / 1000;
            startTime = weekFormat.format(new java.util.Date(startUnixTimestamp * 1000 + 5000));

            long EndUnixTimestamp = original.parse(rawData[2]).getTime() / 1000;
            String endDate = weekFormat.format(new java.util.Date(EndUnixTimestamp * 1000 + 5000));

            if (isFirst) {
                startDay = startTime;
                isFirst = false;
            }

            long duration = (weekFormat.parse(endDate).getTime() - weekFormat.parse(startTime).getTime()) / 60000;


            calendar.setTime(weekFormat.parse(startTime));
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            int dayOfGroup = resultMap.get(String.valueOf(dayOfWeek - 1));
            if (duration >= 0)
                week[dayOfGroup].add(new ActivityInstance(activity, startTime.split("_")[1], endDate.split("_")[1],
                        duration, dayOfWeek - 1, startTime.split("_")[0], endDate.split("_")[0]));

            long differenceOfDays = (weekFormat.parse(endDate).getTime() - weekFormat.parse(startDay).getTime()) / 86400000;
            if (differenceOfDays >= trainedDays) {
                break;
            }

        }


        while ((line = br.readLine()) != null) {
            String[] rawData = line.split(",");
            String activity = rawData[0];

            long startUnixTimestamp = original.parse(rawData[1]).getTime() / 1000;
            String startDate = weekFormat.format(new java.util.Date(startUnixTimestamp * 1000 + 5000));

            long EndUnixTimestamp = original.parse(rawData[1]).getTime() / 1000;
            String endDate = weekFormat.format(new java.util.Date(EndUnixTimestamp * 1000 + 5000));

            if (isFirst) {
                startDay = startDate;
            }

            long duration = (weekFormat.parse(endDate).getTime() - weekFormat.parse(startTime).getTime()) / 60000;


            calendar.setTime(weekFormat.parse(startTime));
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            int dayOfGroup = resultMap.get(String.valueOf(dayOfWeek - 1));
            if (duration >= 0)
                week[dayOfGroup].add(new ActivityInstance(activity, startTime.split("_")[1], endDate.split("_")[1],
                        duration, dayOfWeek - 1, startTime.split("_")[0], endDate.split("_")[0]));

            long differenceOfDays = (weekFormat.parse(endDate).getTime() - weekFormat.parse(startDay).getTime()) / 86400000;
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
                    week[dayOfGroup].add(new ActivityInstance(preActivity, startTime.split("-")[1], date.split("-")[1], duration, dayOfWeek - 1, startTime.split("-")[0], date.split("-")[0]));
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
                    testWeek[dayOfGroup].add(new ActivityInstance(preActivity, startTime.split("-")[1], date.split("-")[0], duration, dayOfWeek - 1, startTime.split("-")[0], date.split("-")[0]));
                startTime = date;
                //startTimestamp = unixTimestamp;
                preActivity = activity;


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

    public static void generateTrainingSampleForBN(int trainedDays) {
        Map<String, Integer> resultMap = new HashMap<>();
        for (int i = 0; i < 7; i++) {
            resultMap.put(String.valueOf(i), 0);
        }
        try {
            ArrayList<ActivityInstance>[][] total = original(trainedDays, resultMap);
            FileWriter fw = new FileWriter("trainingDataBN.arff");
            StringBuilder sb = new StringBuilder("@RELATION predict\n" +
                    "\n" +
                    "@ATTRIBUTE preStartTime  {0,1,2,3,4,5}\n" +
                    "@ATTRIBUTE act_last   {Meal_Preparation,Relax,Eating,Work,Sleeping,Wash_Dishes,Bed_to_Toilet,Enter_Home,Leave_Home,Housekeeping,Resperate}\n" +
                    "@ATTRIBUTE startTime  {0,1,2,3,4,5}\n" +
                    "@ATTRIBUTE dayofweek  {1,2,3,4,5,6,7}\n" +
                    "@ATTRIBUTE act   {Meal_Preparation,Relax,Eating,Work,Sleeping,Wash_Dishes,Bed_to_Toilet,Enter_Home,Leave_Home,Housekeeping,Resperate}\n" +
                    "@data\n");

            ArrayList<ActivityInstance> arrayList = total[0][0];
            while (true) {
                int size = arrayList.size();
                arrayList = LogPreProcessing.preProcessing(arrayList);
                if (size == arrayList.size())
                    break;
            }

            for (int i = 2; i < arrayList.size(); i++) {

                String[] startTime = arrayList.get(i).getStartTime().split(":");
                int startTimeHour = Integer.parseInt(startTime[0]);
                int startTimeMinute = Integer.parseInt(startTime[1]);
                int segment = startTimeHour * 12 + startTimeMinute / 5;

                String[] preStartTime = arrayList.get(i).getStartTime().split(":");
                int preStartTimeHour = Integer.parseInt(startTime[0]);
                int preStartTimeMinute = Integer.parseInt(startTime[1]);
                int preSegment = startTimeHour * 12 + startTimeMinute / 5;


                if (preStartTimeHour < 4) {
                    preSegment = 0;
                } else if (preStartTimeHour < 8) {
                    preSegment = 1;
                } else if (preStartTimeHour < 12) {
                    preSegment = 2;
                } else if (preStartTimeHour < 16) {
                    preSegment = 3;
                } else if (preStartTimeHour < 20) {
                    preSegment = 4;
                } else {
                    preSegment = 5;
                }

                /*if (segment >= 4 && segment <= 64)
                    segment = 4;
                else if ((segment > 64 && segment <= 81) || (segment >= 270 && segment <= 287) || segment < 4)
                    segment = 3;
                else if (segment > 81 && segment <= 100)
                    segment = 0;
                else if (segment > 100 && segment <= 225)
                    segment = 1;
                else
                    segment = 2;*/

                if (startTimeHour < 4) {
                    segment = 0;
                } else if (startTimeHour < 8) {
                    segment = 1;
                } else if (startTimeHour < 12) {
                    segment = 2;
                } else if (startTimeHour < 16) {
                    segment = 3;
                } else if (startTimeHour < 20) {
                    segment = 4;
                } else {
                    segment = 5;
                }

                //Activity(i-2), Activity(i-1), startTime(i), Activity(i)
                int dayOfWeek = arrayList.get(i).getDayOfWeek();
                /*if (dayOfWeek < 6)
                    dayOfWeek = 0;
                else
                    dayOfWeek = 1;*/
                //dayOfWeek = 0;
                sb.append(preSegment + "," + arrayList.get(i - 1).getActivity()
                        + "," + segment + "," + dayOfWeek + "," + arrayList.get(i).getActivity() + "\n");
            }
            fw.write(sb.toString());
            fw.close();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public static void generateTrainingSampleForBNSeg(int trainedDays) {
        Map<String, Integer> resultMap = new HashMap<>();
        for (int i = 0; i < 7; i++) {
            resultMap.put(String.valueOf(i), 0);
        }
        try {
            ArrayList<ActivityInstance>[][] total = original(trainedDays, resultMap);
            FileWriter[] fw = new FileWriter[6];
            StringBuilder[] sb = new StringBuilder[6];
            for (int i = 0; i < 6; i++) {
                fw[i] = new FileWriter("trainingDataBN" + i + ".arff");
                sb[i] = new StringBuilder("@RELATION predict\n" +
                        "\n" +
                        "@ATTRIBUTE act_last   {Meal_Preparation,Relax,Eating,Work,Sleeping,Wash_Dishes,Bed_to_Toilet,Enter_Home,Leave_Home,Housekeeping,Resperate}\n" +
                        "@ATTRIBUTE act   {Meal_Preparation,Relax,Eating,Work,Sleeping,Wash_Dishes,Bed_to_Toilet,Enter_Home,Leave_Home,Housekeeping,Resperate}\n" +
                        "@data\n");
            }

            ArrayList<ActivityInstance> arrayList = total[0][0];
            while (true) {
                int size = arrayList.size();
                arrayList = LogPreProcessing.preProcessing(arrayList);
                if (size == arrayList.size())
                    break;
            }
            for (int i = 3; i < arrayList.size(); i++) {
                String[] startTime = arrayList.get(i).getStartTime().split(":");
                int startTimeHour = Integer.parseInt(startTime[0]);
                int startTimeMinute = Integer.parseInt(startTime[1]);
                int segment = startTimeHour * 12 + startTimeMinute / 5;
                if (arrayList.get(i - 1).getActivity().equals(arrayList.get(i).getActivity())) continue;
                if (segment >= 4 && segment <= 64) {
                    sb[4].append(arrayList.get(i - 1).getActivity()
                            + "," + arrayList.get(i).getActivity() + "\n");
                } else if ((segment > 64 && segment <= 81) || (segment >= 270 && segment <= 287) || segment < 4) {
                    sb[3].append(arrayList.get(i - 1).getActivity()
                            + "," + arrayList.get(i).getActivity() + "\n");
                } else if (segment > 81 && segment <= 100) {
                    sb[0].append(arrayList.get(i - 1).getActivity()
                            + "," + arrayList.get(i).getActivity() + "\n");
                } else if (segment > 100 && segment <= 165) {
                    sb[5].append(arrayList.get(i - 1).getActivity()
                            + "," + arrayList.get(i).getActivity() + "\n");
                } else if (segment > 160 && segment <= 225) {
                    sb[1].append(arrayList.get(i - 1).getActivity()
                            + "," + arrayList.get(i).getActivity() + "\n");
                } else {
                    sb[2].append(arrayList.get(i - 1).getActivity()
                            + "," + arrayList.get(i).getActivity() + "\n");
                }

            }
            for (int i = 0; i < 6; i++) {
                fw[i].write(sb[i].toString());
                fw[i].close();
            }


        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    /**
     * Re-structure activity instances
     *
     * @param weekActivityInstances
     * @return activity instances which is categorized by activity name
     */
    public static Map<String, ArrayList<ActivityInstance>>[] eachActivity(ArrayList<ActivityInstance>[]
                                                                                  weekActivityInstances) {
        Map<String, ArrayList<ActivityInstance>>[] eachActivity = new HashMap[weekActivityInstances.length];
        for (int i = 0; i < eachActivity.length; i++) {
            eachActivity[i] = new HashMap<>();
        }
        //Map<String, Integer> activities = new HashMap<>();
        for (int i = 0; i < weekActivityInstances.length; i++) {
            for (int j = 0; j < weekActivityInstances[i].size(); j++) {
                ActivityInstance activityInstance = weekActivityInstances[i].get(j);
                String activity = activityInstance.getActivity();
                if (!eachActivity[i].containsKey(activity)) {
                    eachActivity[i].put(activity, new ArrayList<>());
                }
                eachActivity[i].get(activity).add(activityInstance);
            }
        }

        return eachActivity;
    }

    public static void main(String[] args) {
        generateTrainingSampleForBN(400);
        /*try {
            Map<String, Integer> resultMap = new HashMap<>();
            for (int i = 0; i < 7; i++) {
                resultMap.put(String.valueOf(i), i);
            }
            yin(7, resultMap);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }*/
    }

}
