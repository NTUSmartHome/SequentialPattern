package wsu;

import LifePattern.ActivityInstance;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by MingJe on 2016/2/8.
 */
public class ActivityInstanceParser {
    public static void main(String[] args) throws IOException, ParseException {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
        SimpleDateFormat weekFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        FileReader fr = new FileReader("db/data");
        BufferedReader br = new BufferedReader(fr);
        String line;
        String startTime = "";
        String startDate = "";
        String activity = "";
        ArrayList<ActivityInstance>[] week = new ArrayList[8];
        for (int i = 0; i < week.length; i++) {
            week[i] = new ArrayList<>();
        }
        Calendar calendar = Calendar.getInstance();
        while ((line = br.readLine()) != null) {
            String[] data = line.split("\\s+");
            if (data.length == 6) {
                if (data[5].equals("begin")) {
                    activity = data[4];
                    startDate = data[0];
                    startTime = data[1];
                } else if (data[5].equals("end")) {
                    calendar.setTime(weekFormat.parse(startDate + " " + startTime));
                    int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
                    long duration = weekFormat.parse(data[0] + " " + data[1]).getTime()
                            - weekFormat.parse(startDate + " " + startTime).getTime();

                    week[dayOfWeek].add(new ActivityInstance(activity, startTime, duration));
                }
            }
        }
        br.close();
        fr.close();

        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < 8; i++) {
            FileWriter fw = new FileWriter("report/ActivityInstance/" + i);
            for (int j = 0; j < week[i].size(); j++) {
                ActivityInstance activityInstance = week[i].get(j);
                sb.append(activityInstance.toSting() + "\n");
            }
            fw.write(sb.toString());
            fw.close();
            sb = new StringBuilder();
        }
    }
}
