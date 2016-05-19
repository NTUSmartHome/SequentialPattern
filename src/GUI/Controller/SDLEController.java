package GUI.Controller;

import Pattern.TestLifePattern;
import javafx.fxml.FXML;
import javafx.scene.chart.AreaChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Created by g2525_000 on 2016/5/19.
 */
public class SDLEController {

    @FXML
    protected AreaChart SDLE_areaChart;
    @FXML
    protected TextField SDLE_trainedDayText;
    @FXML
    protected DatePicker SDLE_startDate;
    @FXML
    protected DatePicker SDLE_endDate;
    @FXML
    protected ComboBox SDLE_activityComboBox;
    @FXML
    protected Button SDLE_generateButton;
    private TestLifePattern testLifePattern;
    private Stage stage;

    public DatePicker getSDLE_endDate() {
        return SDLE_endDate;
    }

    public DatePicker getSDLE_startDate() {
        return SDLE_startDate;
    }

    public ComboBox getSDLE_activityComboBox() {
        return SDLE_activityComboBox;
    }

    public Button getSDLE_generateButton() {
        return SDLE_generateButton;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public TextField getSDLE_trainedDayText() {
        return SDLE_trainedDayText;
    }

    public TestLifePattern getTestLifePattern() {
        return testLifePattern;
    }

    public void setTestLifePattern(TestLifePattern testLifePattern) {
        this.testLifePattern = testLifePattern;
    }

    public AreaChart getSDLE_areaChart() {
        return SDLE_areaChart;
    }

    public void setSDLE_areaChart(AreaChart SDLE_areaChart) {
        this.SDLE_areaChart = SDLE_areaChart;
    }
}
