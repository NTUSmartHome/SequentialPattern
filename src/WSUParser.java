import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

/**
 * Created by YaHung on 2015/8/27.
 */
public class WSUParser {
    int timeInterval;
    int option;
    WSUParser(int timeInterval, int option){
        this.timeInterval = timeInterval;
        this.option = option;
        DB db = new DB(timeInterval,option);

        FileReader fr;

        try{
            fr = new FileReader("db/DB_M1_app.txt");
            BufferedReader br = new BufferedReader(fr);

            int lastNoSDLE = -1;
            long lastUnixTimestamp = 0;
            ArrayList<String> instance = new ArrayList<String>();

            String line;
            while((line = br.readLine())!=null){
                line = line.replace("{","").replace("\"","").replace(" ","");
                String[] rawdata = line.split("[:}]+");

                String label = rawdata[rawdata.length-1];
                int actLable = Integer.valueOf(label);
                if(actLable>12)
                    label = "12";
                //System.out.println(label);

                long unixTimestamp = Integer.valueOf(rawdata[rawdata.length-2].substring(0,10));
                //System.out.println(unixTimestamp);

                String date = new java.text.SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date(unixTimestamp * 1000));
                //System.out.println(date);

                int belongToWhichSDLE = db.belongToWhichSDLE(unixTimestamp);
                //System.out.println("Belong to "+belongToWhichSDLE+"th SDLE.");
                //System.out.println(belongToWhichSDLE);

                if(lastNoSDLE==-1){
                    instance.add(label);
                }
                else if(lastNoSDLE != belongToWhichSDLE && lastNoSDLE!=-1){
                    String[] instanceLable = new String[instance.size()];
                    for (int i=0; i<instance.size(); i++){
                        instanceLable[i] = instance.get(i);
                    }
                    db.addInstance(instanceLable, lastUnixTimestamp);
                    instance.clear();
                    instance.add(label);
                }
                else{
                    boolean exist = false;
                    for(int i=0; i<instance.size(); i++){
                        if(label.equals(instance.get(i))) {
                            exist = true;
                            break;
                        }
                    }
                    if(!exist)
                        instance.add(label);
                }
                lastNoSDLE = belongToWhichSDLE;
                lastUnixTimestamp = unixTimestamp;
            }

            br.close();
            fr.close();
            db.printDB();
        }
        catch(Exception e){

        }
    }


    public static void main(String[] args) {
        /**Generate Database
         * timeInterval set time interval
         * option 0:second, 1:minute, 2:hour, 3:day*/
        new WSUParser(5,1);
    }

}
