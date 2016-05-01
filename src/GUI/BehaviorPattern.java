package GUI;/**
 * Created by g2525_000 on 2016/4/27.
 */

import GUI.Controller.MainController;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.TimeZone;

public class BehaviorPattern extends Application {


    public static void main(String[] args)
    { TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        init(primaryStage);
    }

    public void init(Stage primaryStage) {
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

        //用讀進來FXML的作為Scene的root node
        Scene scene = new Scene(root, 1200, 800);
        primaryStage.setScene(scene);

        //顯示Stage
        primaryStage.setTitle("Behavior Pattern");
        primaryStage.show();
    }


}
