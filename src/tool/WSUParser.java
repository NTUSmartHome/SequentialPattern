package tool;

import DataStructure.ActivityInstance;
import SDLE.DB;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by YaHung on 2015/8/27.
 */
public class WSUParser {
    int timeInterval;
    int option;
    FileReader fr;
    BufferedReader br;

    public WSUParser(int timeInterval, int option, int option2, int whichDataset) throws IOException, ParseException {
        this.timeInterval = timeInterval;
        this.option = option;

        switch (option2) {
            case 0:
                toSDLE();
                break;
            case 1:
                toSDLEMing();
                break;
            case 2:

                Map<String, Integer> resultMap = new HashMap<>();
                for (int i = 0; i < 7; i++) {
                    resultMap.put(String.valueOf(i), 0);
                }
                //Activity Instance parse, return an object and write the file;
                ArrayList<ActivityInstance>[][] total = null;
                switch (whichDataset) {
                    case 0:
                        total = ActivityInstanceParser.original(400, resultMap);
                        break;
                    case 1:
                        total = ActivityInstanceParser.M2Original(400, resultMap);
                        break;
                    case 2:
                        total = ActivityInstanceParser.MingOriginal(400, resultMap);
                }

                ArrayList<ActivityInstance>[] weekActivityInstances;
                weekActivityInstances = total[0];
                while (true) {
                    int size = weekActivityInstances[0].size();
                    weekActivityInstances[0] = LogPreProcessing.preProcessing(weekActivityInstances[0]);
                    if (size == weekActivityInstances[0].size())
                        break;
                }

                toSDLEFromActivityInstance(weekActivityInstances[0].get(0).getStartDay(),
                        weekActivityInstances[0].get(weekActivityInstances[0].size() - 1).getEndDay(),
                        ActivityInstanceParser.eachActivity(weekActivityInstances));
                break;
        }
    }

    public static void main(String[] args) throws IOException, ParseException {
        /**Generate Database
         * timeInterval set time interval
         * option 0:second, 1:minute, 2:hour, 3:day*/

        /*TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
        SimpleDateFormat df = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
        try {
            Date start = df.parse("2010.11.04.00.03.50");
            Date end = df.parse("2011.06.11.23.58.10");
            long diff = (end.getTime() - start.getTime())/(24*60*60*1000);
            System.out.println();
        } catch (ParseException e) {
            e.printStackTrace();
        }*/
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
        new WSUParser(5, 1, 2, 0);

    }


    private void toSDLE() {

        DB db = new DB(timeInterval, option);
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
        try {

            fr = new FileReader("db/DB_M1_app.txt");
            br = new BufferedReader(fr);
            int lastNoSDLE = -1;
            long lastUnixTimestamp = 0;
            ArrayList<String> instance = new ArrayList<String>();
            ArrayList<String> preInstance = new ArrayList<String>();
            String line;

            int preSDLEth = -1;
            //For DB_M1, preLabel = 12, DB_M2, preLabel = 14;
            String preLabel = "12";

            while ((line = br.readLine()) != null) {
                line = line.replace("{", "").replace("\"", "").replace(" ", "");
                String[] rawData = line.split("[:}]+");

                String label = rawData[rawData.length - 1];
                int actLabel = Integer.valueOf(label);
                // For DB_M1
                if (actLabel > 12)
                    label = "12";
                //System.out.println(label);

                long unixTimestamp = Integer.valueOf(rawData[rawData.length - 2].substring(0, 10));
                //System.out.println(unixTimestamp);

                String date = new java.text.SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date(unixTimestamp * 1000));
                //System.out.println(date);

                int belongToWhichSDLE = db.belongToWhichSDLE(unixTimestamp);


                if (lastNoSDLE == -1) {
                    instance.add(label);
                } else if (lastNoSDLE != belongToWhichSDLE && lastNoSDLE != -1) {
                    //if(!nonOccurNewAct) {

                    String[] instanceLable = new String[instance.size()];
                    for (int i = 0; i < instance.size(); i++) {
                        instanceLable[i] = instance.get(i);
                    }
                    db.addInstance(instanceLable, lastUnixTimestamp);
                    preInstance = instance;
                    instance.clear();
                    instance.add(label);

                } else {
                    boolean exist = false;
                    for (int i = 0; i < instance.size(); i++) {
                        if (label.equals(instance.get(i))) {
                            exist = true;
                            break;
                        }
                    }
                    if (!exist)
                        instance.add(label);
                }


                int tmpNoSDLE = lastNoSDLE + 1;
                if (tmpNoSDLE == db.getSDLEQuantity())
                    tmpNoSDLE = 0;

                if (lastUnixTimestamp != 0) {
                    if (belongToWhichSDLE != lastNoSDLE) {
                        if (belongToWhichSDLE < tmpNoSDLE) {
                            //System.out.println("\r\nMore than one SDLE idle, " + tmpNoSDLE + " -> " + belongToWhichSDLE);
                            String[] instanceLable = new String[preInstance.size()];
                            for (int i = 0; i < preInstance.size(); i++) {
                                instanceLable[i] = preInstance.get(i);
                            }

                            while (belongToWhichSDLE < tmpNoSDLE) {
                                db.addInstance(instanceLable, tmpNoSDLE);
                                //System.out.print("*");
                                tmpNoSDLE++;
                                if (tmpNoSDLE == db.getSDLEQuantity()) {
                                    tmpNoSDLE = 0;
                                }

                            }
                            while ((belongToWhichSDLE - tmpNoSDLE) > 0) {
                                db.addInstance(instanceLable, tmpNoSDLE);
                                //System.out.print("*");
                                tmpNoSDLE++;

                            }
                        } else if ((belongToWhichSDLE - tmpNoSDLE) > 0) {
                            //System.out.println("More than one SDLE idle, " + tmpNoSDLE + " -> " + belongToWhichSDLE);
                            String[] instanceLable = new String[preInstance.size()];
                            for (int i = 0; i < preInstance.size(); i++) {
                                instanceLable[i] = preInstance.get(i);
                            }

                            while ((belongToWhichSDLE - tmpNoSDLE) > 0) {
                                db.addInstance(instanceLable, tmpNoSDLE);

                                tmpNoSDLE++;

                            }
                        }
                    }
                }
                lastNoSDLE = belongToWhichSDLE;
                lastUnixTimestamp = unixTimestamp;
            }

            br.close();
            fr.close();
            db.printDB();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void toSDLEMing() {

        DB db = new DB(timeInterval, option);
        try {

            fr = new FileReader("db/MingT.csv");
            br = new BufferedReader(fr);
            int lastNoSDLE = -1;
            long lastUnixTimestamp = 0;
            ArrayList<String> instance = new ArrayList<String>();
            ArrayList<String> preInstance = new ArrayList<String>();
            String line;

            int preSDLEth = -1;
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
            while ((line = br.readLine()) != null) {
                String[] rawData = line.split(",");

                String label = rawData[0];

                long unixTimestamp = simpleDateFormat.parse(rawData[1]).getTime() / 1000;
                //System.out.println(unixTimestamp);

                int belongToWhichSDLE = db.belongToWhichSDLE(unixTimestamp);

                if (lastNoSDLE == -1) {
                    instance.add(label);
                } else if (lastNoSDLE != belongToWhichSDLE && lastNoSDLE != -1) {

                    String[] instanceLable = new String[instance.size()];
                    for (int i = 0; i < instance.size(); i++) {
                        instanceLable[i] = instance.get(i);
                    }
                    db.addInstance(instanceLable, lastUnixTimestamp);
                    preInstance = instance;
                    instance.clear();
                    instance.add(label);

                } else {
                    boolean exist = false;
                    for (int i = 0; i < instance.size(); i++) {
                        if (label.equals(instance.get(i))) {
                            exist = true;
                            break;
                        }
                    }
                    if (!exist)
                        instance.add(label);
                }


                int tmpNoSDLE = lastNoSDLE + 1;
                if (tmpNoSDLE == db.getSDLEQuantity())
                    tmpNoSDLE = 0;

                if (lastUnixTimestamp != 0) {
                    if (belongToWhichSDLE != lastNoSDLE) {
                        if (belongToWhichSDLE < tmpNoSDLE) {
                            //System.out.println("\r\nMore than one SDLE idle, " + tmpNoSDLE + " -> " + belongToWhichSDLE);
                            String[] instanceLable = new String[preInstance.size()];
                            for (int i = 0; i < preInstance.size(); i++) {
                                instanceLable[i] = preInstance.get(i);
                            }

                            while (belongToWhichSDLE < tmpNoSDLE) {
                                db.addInstance(instanceLable, tmpNoSDLE);
                                //System.out.print("*");
                                tmpNoSDLE++;
                                if (tmpNoSDLE == db.getSDLEQuantity()) {
                                    tmpNoSDLE = 0;
                                }
                            }
                            while ((belongToWhichSDLE - tmpNoSDLE) > 0) {
                                db.addInstance(instanceLable, tmpNoSDLE);
                                //System.out.print("*");
                                tmpNoSDLE++;

                            }
                        } else if ((belongToWhichSDLE - tmpNoSDLE) > 0) {
                            //System.out.println("More than one SDLE idle, " + tmpNoSDLE + " -> " + belongToWhichSDLE);
                            String[] instanceLable = new String[preInstance.size()];
                            for (int i = 0; i < preInstance.size(); i++) {
                                instanceLable[i] = preInstance.get(i);
                            }

                            while ((belongToWhichSDLE - tmpNoSDLE) > 0) {
                                db.addInstance(instanceLable, tmpNoSDLE);
                                tmpNoSDLE++;

                            }
                        }
                    }
                }
                lastNoSDLE = belongToWhichSDLE;
                lastUnixTimestamp = unixTimestamp;
                if (rawData[1].contains("2016/1/14 18:")) {
                    System.out.println();
                }
            }

            br.close();
            fr.close();
            db.printDB();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //For building SDLE file to 1 or 0 (happen or not)
    private void toSDLEFromActivityInstance(String startDay, String endDay, Map<String, ArrayList<ActivityInstance>>[] eachActivityInstance) throws ParseException, IOException {
        SimpleDateFormat timeDateFormat = new SimpleDateFormat("HH:mm:ss");
        SimpleDateFormat dayDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        DB db = new DB(timeInterval, option);
        long ll = (dayDateFormat.parse(endDay).getTime() - dayDateFormat.parse(startDay).getTime()) / (24 * 60 * 60 * 1000) + 1;

        for (int i = 0; i < eachActivityInstance.length; i++) {
            Set<String> activitySet = eachActivityInstance[i].keySet();

            for (String activity : activitySet) {
                int count = 0;
                ArrayList<ActivityInstance> activityInstanceList = eachActivityInstance[i].get(activity);
                boolean[] happen = new boolean[db.getSDLEQuantity()];
                StringBuilder[] sb = new StringBuilder[db.getSDLEQuantity()];
                for (int j = 0; j < sb.length; j++) {
                    sb[j] = new StringBuilder();
                }
                ActivityInstance preActivityInstance = null;
                ActivityInstance activityInstance = null;
                for (int j = 0; j < activityInstanceList.size(); j++) {
                    activityInstance = activityInstanceList.get(j);
                    if (j == 0) {
                        long start = dayDateFormat.parse(startDay).getTime() / (24 * 60 * 60 * 1000);
                        long curDay = dayDateFormat.parse(activityInstance.getStartDay()).getTime() / (24 * 60 * 60 * 1000);
                        for (long k = start; k < curDay; k++) {
                            for (int i1 = 0; i1 < happen.length; i1++) {
                                if (sb[i1].length() == 0) {
                                    sb[i1].append(happen[i1]);
                                } else {
                                    sb[i1].append("\n" + happen[i1]);
                                }
                            }
                            count++;
                            happen = new boolean[happen.length];
                        }
                    }

                    if (preActivityInstance != null) {

                        long preDay = dayDateFormat.parse(preActivityInstance.getEndDay()).getTime() / (24 * 60 * 60 * 1000);
                        long curDay = dayDateFormat.parse(activityInstance.getStartDay()).getTime() / (24 * 60 * 60 * 1000);
                        if (preDay < curDay) {
                            for (long k = preDay; k < curDay; k++) {
                                for (int i1 = 0; i1 < happen.length; i1++) {
                                    if (sb[i1].length() == 0) {
                                        sb[i1].append(happen[i1]);
                                    } else {
                                        sb[i1].append("\n" + happen[i1]);
                                    }
                                }
                                count++;
                                happen = new boolean[happen.length];

                                long start = dayDateFormat.parse(startDay).getTime() / (24 * 60 * 60 * 1000);
                                long diff = preDay - start + 1;
                                // System.out.println(count + " : " + diff);

                            }
                        }
                    }
                    long startTime = timeDateFormat.parse(activityInstance.getStartTime()).getTime();
                    long endTime = timeDateFormat.parse(activityInstance.getEndTime()).getTime();
                    if (startTime <= endTime) {
                        int startSDLE = db.belongToWhichSDLE(startTime / 1000);
                        int endSDLE = db.belongToWhichSDLE(endTime / 1000);
                        for (int k = startSDLE; k <= endSDLE; k++) {
                            happen[k] = true;
                        }
                    } else {
                        int startSDLE = db.belongToWhichSDLE(startTime / 1000);
                        int endSDLE = db.belongToWhichSDLE(endTime / 1000);
                        for (int k = startSDLE; k < happen.length; k++) {
                            happen[k] = true;
                        }
                        for (int k = 0; k < happen.length; k++) {
                            if (sb[k].length() == 0) {
                                sb[k].append(happen[k]);
                            } else {
                                sb[k].append("\n" + happen[k]);
                            }
                        }
                        happen = new boolean[happen.length];
                        count++;
                        long start = dayDateFormat.parse(startDay).getTime() / (24 * 60 * 60 * 1000);
                        long diff = dayDateFormat.parse(activityInstance.getStartDay()).getTime() / (24 * 60 * 60 * 1000) - start + 1;
                        //System.out.println(count + " : " + diff);
                        for (int k = 0; k <= endSDLE; k++) {
                            happen[k] = true;
                        }

                    }
                    preActivityInstance = activityInstance;

                }
                long start = dayDateFormat.parse(startDay).getTime() / (24 * 60 * 60 * 1000);
                long diff = dayDateFormat.parse(activityInstance.getStartDay()).getTime() / (24 * 60 * 60 * 1000) - start + 1;
                // System.out.println(count + " : " + diff);

                long curDay = dayDateFormat.parse(activityInstance.getEndDay()).getTime() / (24 * 60 * 60 * 1000);
                long end = dayDateFormat.parse(endDay).getTime() / (24 * 60 * 60 * 1000);
                for (long j = curDay; j <= end; j++) {
                    for (int i1 = 0; i1 < happen.length; i1++) {
                        if (sb[i1].length() == 0) {
                            sb[i1].append(happen[i1]);
                        } else {
                            sb[i1].append("\n" + happen[i1]);
                        }
                    }
                    count++;
                    happen = new boolean[happen.length];
                }
                StringBuilder SDLEDBName = new StringBuilder();
                SDLEDBName.append("db/");
                //System.out.println(count);
                switch (option) {
                    case 0:
                        SDLEDBName.append(timeInterval);
                        SDLEDBName.append("second/");
                        break;
                    case 1:
                        SDLEDBName.append(timeInterval);
                        SDLEDBName.append("minute/");
                        break;
                    case 2:
                        SDLEDBName.append(timeInterval);
                        SDLEDBName.append("hour/");
                        break;
                    case 3:
                        SDLEDBName.append(timeInterval);
                        SDLEDBName.append("day/");
                        break;

                }
                for (int j = 0; j < happen.length; j++) {
                    File file = new File(SDLEDBName.toString() + "/" + activity);
                    file.mkdirs();
                    FileWriter fileWriter = new FileWriter(file + "/" + j + ".txt");
                    fileWriter.write(sb[j].toString());
                    fileWriter.close();
                }

            }
        }
    }


}
