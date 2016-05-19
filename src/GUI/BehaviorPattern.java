package GUI;/**
 * Created by g2525_000 on 2016/4/27.
 */

import GUI.Controller.MainController;
import GUI.Controller.SDLEController;
import Pattern.TestLifePattern;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.DatePicker;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
        final NumberAxis xAxis = new NumberAxis(0, 287, 10);
        final NumberAxis yAxis = new NumberAxis(0, 1, 0.1);
        final AreaChart<Number, Number> sdleAreaChart =
                new AreaChart<Number, Number>(xAxis, yAxis);
        xAxis.setMinorTickCount(0);
        yAxis.setMinorTickCount(0);
        controller.setSDLE_areaChart(sdleAreaChart);

        //用讀進來FXML的作為Scene的root node
        Scene scene = new Scene(root, 1000, 700);

        primaryStage.setScene(scene);

        //顯示Stage
        primaryStage.setTitle("SDLE Pattern");
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
