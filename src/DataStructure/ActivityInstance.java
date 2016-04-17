package DataStructure;


/**
 * Created by MingJe on 2016/2/8.
 */
public class ActivityInstance {
    private String activity;
    private String startTime;
    private long duration;
    private int dayOfWeek;



    public ActivityInstance(String activity, String startTime, long duration) {
        this.activity = activity;
        this.startTime = startTime;
        this.duration = duration;

    }

    public ActivityInstance(String activity, String startTime, long duration, int dayOfWeek) {
        this.activity = activity;
        this.startTime = startTime;
        this.duration = duration;
        this.dayOfWeek = dayOfWeek;

    }
    public String getActivity() {
        return activity;
    }

    public String getStartTime() {
        return startTime;
    }

    public long getDuration() {
        return duration;
    }
    public int getDayOfWeek() {
        return dayOfWeek;
    }

    public String toSting() {
        return activity + " " + startTime + " " + duration;
    }
}
