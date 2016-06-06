package GUI;/**
 * Created by g2525_000 on 2016/4/27.
 */

import GUI.Controller.MainController;
import GUI.Controller.RuleController;
import GUI.Controller.SDLEController;
import Pattern.TestLifePattern;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.TimeZone;

public class BehaviorPattern extends Application {
    private final String pattern = "yyyy-MM-dd";
    private TestLifePattern testLifePattern;

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        testLifePattern = new TestLifePattern();
        initMain(primaryStage);
        javafx.application.Platform.runLater(new Runnable() {

            @Override
            public void run() {
                Stage SDLE = new Stage();
                initSDLE(SDLE);
            }
        });
        javafx.application.Platform.runLater(new Runnable() {

            @Override
            public void run() {
                Stage rule = new Stage();
                initRuleDef(rule);
            }
        });


        // initSDLE(SDLE);
    }

    public void initMain(Stage primaryStage) {
        Parent root = null;
        FXMLLoader loader = null;
        try {
            loader = new FXMLLoader(getClass().getResource("View/Main.fxml"));
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //幫controller設定stage
        MainController controller = loader.getController();
        controller.setStage(primaryStage);
        controller.getVBox().heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                controller.getMainTabPane().setPrefHeight(newValue.doubleValue());
            }
        });
        controller.getVBox().widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                controller.getMainTabPane().setPrefWidth(newValue.doubleValue());
            }
        });
        controller.setTestLifePattern(testLifePattern);

        //用讀進來FXML的作為Scene的root node
        Scene scene = new Scene(root, 1000, 700);
        primaryStage.setScene(scene);

        //顯示Stage
        primaryStage.setTitle("Behavior Pattern");
        primaryStage.show();
    }

    public void initSDLE(Stage primaryStage) {
        Parent root = null;
        FXMLLoader loader = null;
        try {
            loader = new FXMLLoader(getClass().getResource("View/SDLE.fxml"));
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        SDLEController controller = loader.getController();
        controller.setStage(primaryStage);
        setDateFormat(controller.getSDLE_startDate());
        setDateFormat(controller.getSDLE_endDate());
        controller.setTestLifePattern(testLifePattern);
        controller.getSDLE_activityComboBox().getItems().addAll(testLifePattern.getActivityList());
        //init SDLE chart
        final NumberAxis SDLE_xAxis = new NumberAxis(0, 287, 10);
        final NumberAxis SDLE_yAxis = new NumberAxis(0, 1, 0.1);
        final AreaChart<Number, Number> sdleAreaChart =
                new AreaChart<Number, Number>(SDLE_xAxis, SDLE_yAxis);
        SDLE_xAxis.setMinorTickCount(0);
        SDLE_yAxis.setMinorTickCount(0);
        controller.getSDLE_chartPane().getChildren().add(sdleAreaChart);
        AnchorPane.setTopAnchor(sdleAreaChart, 2.0);
        AnchorPane.setBottomAnchor(sdleAreaChart, 2.0);
        AnchorPane.setLeftAnchor(sdleAreaChart, 2.0);
        AnchorPane.setRightAnchor(sdleAreaChart, 2.0);
        controller.setSdleChart(sdleAreaChart);
        //init KLD chart
        final NumberAxis KLD_xAxis = new NumberAxis(0, 287, 10);
        final NumberAxis KLD_yAxis = new NumberAxis(0, 1, 0.1);
        final LineChart<Number, Number> KLDLineChart =
                new LineChart<Number, Number>(KLD_xAxis, KLD_yAxis);
        KLD_xAxis.setMinorTickCount(0);
        KLD_yAxis.setMinorTickCount(0);
        controller.getSDLE_KLDChartPane().getChildren().add(KLDLineChart);
        AnchorPane.setTopAnchor(KLDLineChart, 2.0);
        AnchorPane.setBottomAnchor(KLDLineChart, 2.0);
        AnchorPane.setLeftAnchor(KLDLineChart, 2.0);
        AnchorPane.setRightAnchor(KLDLineChart, 2.0);
        controller.setKldChart(KLDLineChart);
        //Disable days
        DatePicker startDayDatePicker = controller.getSDLE_startDate();
        final Callback<DatePicker, DateCell> startDayCellFactory =
                new Callback<DatePicker, DateCell>() {
                    @Override
                    public DateCell call(final DatePicker datePicker) {
                        return new DateCell() {
                            @Override
                            public void updateItem(LocalDate item, boolean empty) {
                                super.updateItem(item, empty);

                                if (item.isBefore(LocalDate.of(2010, 11, 4))) {
                                    setDisable(true);
                                    setStyle("-fx-background-color: #ffc0cb;");
                                }
                                if (item.isAfter(LocalDate.of(2011, 06, 11))) {
                                    setDisable(true);
                                    setStyle("-fx-background-color: #ffc0cb;");
                                }
                            }
                        };
                    }
                };
        startDayDatePicker.setDayCellFactory(startDayCellFactory);
        DatePicker endDayDatePicker = controller.getSDLE_endDate();
        final Callback<DatePicker, DateCell> endDayCellFactory =
                new Callback<DatePicker, DateCell>() {
                    @Override
                    public DateCell call(final DatePicker datePicker) {
                        return new DateCell() {
                            @Override
                            public void updateItem(LocalDate item, boolean empty) {
                                super.updateItem(item, empty);
                                if (item.isBefore(LocalDate.of(2010, 11, 4))) {
                                    setDisable(true);
                                    setStyle("-fx-background-color: #ffc0cb;");
                                }
                                if (item.isAfter(LocalDate.of(2011, 06, 11))) {
                                    setDisable(true);
                                    setStyle("-fx-background-color: #ffc0cb;");
                                }
                                if (startDayDatePicker.getValue() == null) {
                                    setDisable(true);
                                    setStyle("-fx-background-color: #ffc0cb;");
                                } else if (item.isBefore(startDayDatePicker.getValue().plusDays(1))) {
                                    setDisable(true);
                                    setStyle("-fx-background-color: #ffc0cb;");
                                }
                            }
                        };
                    }
                };
        endDayDatePicker.setDayCellFactory(endDayCellFactory);
        //用讀進來FXML的作為Scene的root node
        Scene scene = new Scene(root, 1000, 700);

        primaryStage.setScene(scene);

        //顯示Stage
        primaryStage.setTitle("SDLE Pattern");
        primaryStage.show();

    }

    public void initRuleDef(Stage primaryStage) {
        Parent root = null;
        FXMLLoader loader = null;
        try {
            loader = new FXMLLoader(getClass().getResource("View/RuleDef.fxml"));
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        RuleController controller = loader.getController();
        controller.setStage(primaryStage);
        ArrayList<String> activityNStartTime = new ArrayList<>();
        ArrayList<String> activityList = testLifePattern.getActivityList();
        for (int i = 0; i < activityList.size(); i++) {
            String[] startTime = testLifePattern.getActivityStartTimeClusterer().get(activityList.get(i)).getStartTime();
            for (int j = 0; j < startTime.length; j++) {
                activityNStartTime.add(activityList.get(i) + " " + startTime[j]);
            }
        }
        //Set all combobox
        controller.setComboBox(activityNStartTime, activityList);

        //用讀進來FXML的作為Scene的root node
        Scene scene = new Scene(root, 1000, 700);

        primaryStage.setScene(scene);

        //顯示Stage
        primaryStage.setTitle("Rule Def");
        primaryStage.show();

    }

    private void setDateFormat(DatePicker datePicker) {
        StringConverter converter = new StringConverter<LocalDate>() {
            DateTimeFormatter dateFormatter =
                    DateTimeFormatter.ofPattern(pattern);

            @Override
            public String toString(LocalDate date) {
                if (date != null) {
                    return dateFormatter.format(date);
                } else {
                    return "";
                }
            }

            @Override
            public LocalDate fromString(String string) {
                if (string != null && !string.isEmpty()) {
                    return LocalDate.parse(string, dateFormatter);
                } else {
                    return null;
                }
            }
        };

        datePicker.setConverter(converter);
        datePicker.setPromptText(pattern.toLowerCase());
    }


}
