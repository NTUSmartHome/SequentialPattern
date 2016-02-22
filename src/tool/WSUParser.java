package tool;

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
                toSDLEMing();
                break;
            case 2:
                toSDLER();
                break;
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

        new WSUParser(5, 1, 1);

    }



    private void toSDLER() {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
        DB db = new DB(timeInterval, option);
        int interval = db.getTimeInterval();
        try {
            fr = new FileReader("db/Seq.txt");
            br = new BufferedReader(fr);


            ArrayList<String> instance = new ArrayList<String>();
            int preBelongToWhichSDLE = -1;
            String line;
            String[] rawData;

            while ((line = br.readLine()) != null) {

                rawData = line.split(",|:");
                String label = rawData[1];
                SimpleDateFormat df = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
                long endTime = df.parse(rawData[5]).getTime() / 1000;
                long startTime = df.parse(rawData[3]).getTime() / 1000;

                int bound = 0;
                /*if (db.belongToWhichSDLE(endTime) != db.belongToWhichSDLE(startTime))
                    bound = interval;*/
                int endSDLE = db.belongToWhichSDLE(endTime);
                int startSDLE = db.belongToWhichSDLE(startTime);
                while (endSDLE >= startSDLE) {
                    if (preBelongToWhichSDLE == 287) {
                        System.out.println();
                    }

                    if (endSDLE == db.getSDLEQuantity() - 1 && startSDLE != endSDLE)
                        endSDLE = -1;

                    if (startSDLE == preBelongToWhichSDLE) {
                        instance.add(label);
                    } else {
                        if (instance.size() > 0) {
                            String[] instanceLable = new String[instance.size()];
                            for (int i = 0; i < instance.size(); i++) {
                                instanceLable[i] = instance.get(i);
                            }
                            db.addInstance(instanceLable, preBelongToWhichSDLE);
                            instance.clear();
                        }

                        instance.add(label);
                    }
                    preBelongToWhichSDLE = startSDLE;
                    startTime += interval;
                    startSDLE = db.belongToWhichSDLE(startTime);
                    //startSDLE = db.belongToWhichSDLE(startTime);
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
                            //System.out.println("\r\nMore than one sdle idle, " + tmpNoSDLE + " -> " + belongToWhichSDLE);
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
                            //System.out.println("More than one sdle idle, " + tmpNoSDLE + " -> " + belongToWhichSDLE);
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
            //For DB_M1, preLabel = 12, DB_M2, preLabel = 14;
            String preLabel = "12";
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
                            //System.out.println("\r\nMore than one sdle idle, " + tmpNoSDLE + " -> " + belongToWhichSDLE);
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
                            //System.out.println("More than one sdle idle, " + tmpNoSDLE + " -> " + belongToWhichSDLE);
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
            System.out.println("test");
            br.close();
            fr.close();
            db.printDB();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
