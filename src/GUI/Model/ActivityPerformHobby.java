package GUI.Model;

import javafx.beans.property.SimpleStringProperty;

/**
 * Created by g2525_000 on 2016/4/30.
 */
public class ActivityPerformHobby {
    private final SimpleStringProperty startTime;
    private final SimpleStringProperty duration;

    public ActivityPerformHobby(String startTime, String duration) {
        this.startTime = new SimpleStringProperty(startTime);
        this.duration = new SimpleStringProperty(duration);
    }

    public String getStartTime() {
        return startTime.get();
    }

    public void setStartTime(String startTime) {
        this.startTime.set(startTime);
    }

    public SimpleStringProperty startTimeProperty() {
        return startTime;
    }

    public String getDuration() {
        return duration.get();
    }

    public void setDuration(String duration) {
        this.duration.set(duration);
    }

    public SimpleStringProperty durationProperty() {
        return duration;
    }
}
