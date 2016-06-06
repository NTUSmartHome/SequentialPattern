package GUI.Controller;

import Pattern.TestLifePattern;
import SDLE.SDLE;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.time.LocalDate;
import java.util.ArrayList;

/**
 * Created by g2525_000 on 2016/5/19.
 */
public class SDLEController {


    @FXML
    protected AnchorPane SDLE_KLDChartPane;
    @FXML
    protected AnchorPane SDLE_chartPane;
    @FXML
    protected TextField SDLE_trainedDayText;
    @FXML
    protected DatePicker SDLE_startDate;
    @FXML
    protected DatePicker SDLE_endDate;
    @FXML
    protected ComboBox SDLE_activityComboBox;
    @FXML
    protected Button SDLE_generatePatternButton;
    @FXML
    protected Button SDLE_generateDifferenceButton;
    @FXML
    protected Button SDLE_clearButton;

    private TestLifePattern testLifePattern;
    private Stage stage;
    private AreaChart<Number, Number> sdleChart;
    private LineChart<Number, Number> kldChart;

    public LineChart<Number, Number> getKldChart() {
        return kldChart;
    }

    public void setKldChart(LineChart<Number, Number> kldChart) {
        this.kldChart = kldChart;
    }

    public DatePicker getSDLE_endDate() {
        return SDLE_endDate;
    }

    public DatePicker getSDLE_startDate() {
        return SDLE_startDate;
    }

    public ComboBox getSDLE_activityComboBox() {
        return SDLE_activityComboBox;
    }

    public Button getSDLE_generatePatternButton() {
        return SDLE_generatePatternButton;
    }

    public AnchorPane getSDLE_KLDChartPane() {
        return SDLE_KLDChartPane;
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

    public AnchorPane getSDLE_chartPane() {
        return SDLE_chartPane;
    }

    public AreaChart<Number, Number> getSdleChart() {
        return sdleChart;
    }

    public void setSdleChart(AreaChart<Number, Number> sdleChart) {
        this.sdleChart = sdleChart;
    }

    public void generatePattern() {
        if (sdleChart == null) return;
        if (testLifePattern == null) return;
        if (SDLE_startDate.getValue() != null && SDLE_endDate.getValue() != null) {
            Timeline tl = new Timeline();
            tl.getKeyFrames().add(new KeyFrame(Duration.millis(500),
                    new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent actionEvent) {
                            testLifePattern.SDLEAccumulate(SDLE_startDate.getValue(), SDLE_endDate.getValue());
                            ArrayList<SDLE> sdleEstimationList = testLifePattern.getSdleList().get(SDLE_activityComboBox.getValue());
                            XYChart.Series sdleSeries = new XYChart.Series();
                            sdleSeries.setName(SDLE_activityComboBox.getValue() + "_" + SDLE_trainedDayText.getText());
                            for (int i = 0; i < sdleEstimationList.size(); i++) {
                                sdleSeries.getData().add(new XYChart.Data(i, sdleEstimationList.get(i).getDistribution().get(0)));
                            }
                            sdleChart.getData().add(sdleSeries);
                        }
                    }));
            tl.play();
        } else {
            Timeline tl = new Timeline();
            tl.getKeyFrames().add(new KeyFrame(Duration.millis(500),
                    new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent actionEvent) {
                            testLifePattern.SDLEAccumulate(Integer.parseInt(SDLE_trainedDayText.getText()));
                            ArrayList<SDLE> sdleEstimationList = testLifePattern.getSdleList().get(SDLE_activityComboBox.getValue());
                            XYChart.Series sdleSeries = new XYChart.Series();
                            sdleSeries.setName(SDLE_activityComboBox.getValue() + "_" + SDLE_trainedDayText.getText());
                            for (int i = 0; i < sdleEstimationList.size(); i++) {
                                sdleSeries.getData().add(new XYChart.Data(i, sdleEstimationList.get(i).getDistribution().get(0)));
                            }
                            sdleChart.getData().add(sdleSeries);
                        }
                    }));
            tl.play();
        }


    }

    public void generateDifference() {
        Timeline tl = new Timeline();
        tl.getKeyFrames().add(new KeyFrame(Duration.millis(500),
                new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent actionEvent) {
                        LocalDate startDate = SDLE_startDate.getValue();
                        LocalDate endDate = SDLE_endDate.getValue();
                        ArrayList<Double> base = new ArrayList<Double>();
                        testLifePattern.SDLEAccumulate(SDLE_startDate.getValue(), SDLE_startDate.getValue().plusDays(30));
                        ArrayList<SDLE> sdleEstimationList = testLifePattern.getSdleList().get(SDLE_activityComboBox.getValue());
                        for (int i = 0; i < sdleEstimationList.size(); i++) {
                            base.add(sdleEstimationList.get(i).getDistribution().get(0));
                        }
                        XYChart.Series kldSeries = new XYChart.Series();
                        for (LocalDate i = startDate.plusDays(30); i.isBefore(SDLE_endDate.getValue()); i.plusDays(7)) {
                            testLifePattern.SDLEAccumulate(SDLE_startDate.getValue(), SDLE_endDate.getValue());
                            sdleEstimationList = testLifePattern.getSdleList().get(SDLE_activityComboBox.getValue());
                            ArrayList<Double> comparator = new ArrayList<Double>();
                            for (int j = 0; j < sdleEstimationList.size(); j++) {
                                comparator.add(sdleEstimationList.get(j).getDistribution().get(0));
                            }
                            double kld = testLifePattern.KLDivergence(base, comparator);

                        }

                        XYChart.Series sdleSeries = new XYChart.Series();
                        sdleSeries.setName(SDLE_activityComboBox.getValue() + "_" + SDLE_trainedDayText.getText());
                        for (int i = 0; i < sdleEstimationList.size(); i++) {
                            sdleSeries.getData().add(new XYChart.Data(i, sdleEstimationList.get(i).getDistribution().get(0)));
                        }
                        sdleChart.getData().add(sdleSeries);
                    }
                }));
        tl.play();
    }

    public void clear() {
        for (int i = sdleChart.getData().size() - 1; i >= 0; i--) {
            sdleChart.getData().remove(i);
        }
    }

}
