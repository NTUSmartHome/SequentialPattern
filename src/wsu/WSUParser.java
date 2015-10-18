package wsu;

import sdle.DB;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.TimeZone;

/**
 * Created by YaHung on 2015/8/27.
 */
public class WSUParser {
    int timeInterval;
    int option;
    FileReader fr;
    BufferedReader br;

    public WSUParser(int timeInterval, int option, int option2) {
        this.timeInterval = timeInterval;
        this.option = option;

        switch (option2) {
            case 0:
                toSDLE();
                break;
            case 1:
                toSequential();
                break;
            case 2:
                toSDLER();
                break;
        }
    }

    private void toSequential() {
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
            fr = new FileReader("db/DB_M1_app.txt");
            br = new BufferedReader(fr);
            FileWriter fw = new FileWriter("db/Seq.txt");
            String line;
            int currentActivity = 0;
            boolean isFirst = true;
            long startTime = -1;
            long preEndTime = -1;
            while ((line = br.readLine()) != null) {
                String[] rawData = line.split("[}{,:]+");

                if (currentActivity != Integer.valueOf(rawData[7].trim())) {
                    currentActivity = Integer.valueOf(rawData[7].trim());

                    if (!isFirst && startTime != -1) {
                        preEndTime = Long.valueOf(rawData[6]);
                        fw.write("EndTime:"
                                + new java.text.SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date(Long.valueOf(rawData[6])))
                                + ",Duration:" + (Long.valueOf(rawData[6]) - startTime) + "\r\n");
                        startTime = -1;

                    }
                    if (currentActivity < 12) {
                        startTime = Long.valueOf(rawData[6]);
                        long diff = startTime - preEndTime;
                        while (preEndTime != -1 && diff > 300000) {
                            String time = new java.text.SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date(preEndTime += 300000L));
                            String endTime = new java.text.SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date(preEndTime + 1000));
                            fw.write("activity:12,BeginTime:" + time + ",EndTime:" + endTime + ",Duration:1" + "\r\n");
                            diff -= 300000L;
                        }
                        fw.write("activity:" + currentActivity + ",");
                        fw.write("BeginTime:"
                                + new java.text.SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date(startTime)) + ",");

                        isFirst = false;
                    }

                }
                //preTime = Long.valueOf(rawData[6]);
            }

            fw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void toSDLER() {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
        DB db = new DB(timeInterval, option);
        int interval = db.getTimeInterval();
        try {
            fr = new FileReader("db/Seq.txt");
            br = new BufferedReader(fr);

            int lastNoSDLE = -1;
            long lastUnixTimestamp = 0;
            ArrayList<String> instance = new ArrayList<String>();
            ArrayList<String> preInstance = new ArrayList<String>();
            String line;
            String[] rawData;

            while ((line = br.readLine()) != null) {
                rawData = line.split(",|:");
                String label = rawData[1];
                SimpleDateFormat df = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
                long endTime = df.parse(rawData[5]).getTime() / 1000;
                long startTime = df.parse(rawData[3]).getTime() / 1000;
                //long diff = endTime - startTime;
                while (endTime - startTime > 0) {
                    int belongToWhichSDLE = db.belongToWhichSDLE(startTime);
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

                    if (lastUnixTimestamp != 0) {
                        if ((belongToWhichSDLE - tmpNoSDLE) > 0) {
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
                    lastNoSDLE = belongToWhichSDLE;
                    lastUnixTimestamp = startTime;
                    startTime += interval;
                }
            }
            br.close();
            fr.close();
            db.printDB();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
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

            while ((line = br.readLine()) != null) {
                line = line.replace("{", "").replace("\"", "").replace(" ", "");
                String[] rawData = line.split("[:}]+");

                String label = rawData[rawData.length - 1];
                int actLabel = Integer.valueOf(label);
                if (actLabel > 12)
                    label = "12";
                //System.out.println(label);

                long unixTimestamp = Integer.valueOf(rawData[rawData.length - 2].substring(0, 10));
                //System.out.println(unixTimestamp);

                String date = new java.text.SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date(unixTimestamp * 1000));
                System.out.println(date);

                int belongToWhichSDLE = db.belongToWhichSDLE(unixTimestamp);
                //System.out.println("Belong to "+belongToWhichSDLE+"th SDLE.");
                //System.out.println(belongToWhichSDLE);

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

                if (lastUnixTimestamp != 0) {
                    if ((belongToWhichSDLE - tmpNoSDLE) > 0) {
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
                lastNoSDLE = belongToWhichSDLE;
                lastUnixTimestamp = unixTimestamp;
            }

            br.close();
            fr.close();
            db.printDB();
        } catch (Exception e) {

        }
    }

    public static void main(String[] args) {
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


        new WSUParser(5, 1, 0);

    }

}
