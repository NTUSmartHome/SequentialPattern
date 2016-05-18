package DataStructure;


/**
 * Created by MingJe on 2016/2/8.
 */
public class ActivityInstance {
    private String activity;
    private String startTime;
    private String endTime;
    private String startDay;
    private String endDay;
    private long duration;
    private int dayOfWeek;

    @Deprecated
    public ActivityInstance(String activity, String startTime, long duration) {
        this.activity = activity;
        this.startTime = startTime;
        this.duration = duration;

    }

    public ActivityInstance(ActivityInstance old) {
        this.activity = old.getActivity();
        this.startTime = old.getStartTime();
        this.endTime = old.getEndTime();
        this.duration = old.getDuration();
        this.dayOfWeek = old.getDayOfWeek();
        this.startDay = old.getStartDay();
        this.endDay = old.getEndDay();
    }

    @Deprecated
    public ActivityInstance(String activity, String startTime, long duration, int dayOfWeek) {
        this.activity = activity;
        this.startTime = startTime;
        this.duration = duration;
        this.dayOfWeek = dayOfWeek;

    }

    public ActivityInstance(String activity, String startTime, String endTime, long duration, int dayOfWeek, String startDay, String endDay) {
        this.activity = activity;
        this.startTime = startTime;
        this.endTime = endTime;
        this.duration = duration;
        this.dayOfWeek = dayOfWeek;
        this.startDay = startDay;
        this.endDay = endDay;

    }

    public void setActivity(String activity) {
        this.activity = activity;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public void setDayOfWeek(int dayOfWeek) {
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

    public String getEndTime() {
        return endTime;
    }

    public String getStartDay() {
        return startDay;
    }

    public String getEndDay() {
        return endDay;
    }

    public String toSting() {
        return activity + " " + startTime + " " + duration;
    }

}
