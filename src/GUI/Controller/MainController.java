package GUI.Controller;

import Pattern.TestLifePattern;
import SDLE.Activity;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import weka.datagenerators.Test;

import java.io.*;

/**
 * Created by g2525_000 on 2016/4/27.
 */
public class MainController {
    private Stage stage;
    private TestLifePattern testLifePattern;

    @FXML
    protected AnchorPane MainPane;
    @FXML
    protected TabPane MainTabPane;

    @FXML
    protected VBox VBox;


    @FXML
    protected void exit(ActionEvent event) {
        System.exit(0);
    }

    @FXML
    protected void open(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File("E:\\Users\\g2525_000\\IdeaProjects\\" +
                "SequentialPattern\\report\\model"));
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Activity List Files", "*.list"),
                new FileChooser.ExtensionFilter("Model Files", "*.rModel", "*.dModel", "*.sModel"));
        fileChooser.setTitle("Open File");
        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            String fileType = selectedFile.getName().split("\\.")[1];
            if (fileType.equals("list")) {
                try {
                    FileReader fr = new FileReader(selectedFile);
                    BufferedReader br = new BufferedReader(fr);
                    String line;
                    while ((line = br.readLine()) != null) {
                        Tab tab = new Tab(line);
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/GUI/View/ActivityTab.fxml"));
                        tab.setContent(loader.load());
                        ActivityTabController activityTabController = loader.getController();
                        MainTabPane.heightProperty().addListener(new ChangeListener<Number>() {
                            @Override
                            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                                activityTabController.getMainPane().setPrefHeight(newValue.doubleValue() - 20);
                            }
                        });
                        MainTabPane.widthProperty().addListener(new ChangeListener<Number>() {
                            @Override
                            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                                activityTabController.getMainPane().setPrefWidth(newValue.doubleValue() - 10);
                            }
                        });
                        MainTabPane.getTabs().add(tab);
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {

        }
    }
    @FXML
    protected void load (ActionEvent event) {
        testLifePattern = new TestLifePattern();
    }

    @FXML
    protected void resizeTabPane(ActionEvent event) {
        System.out.println(MainTabPane.getHeight());
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }


    public TabPane getMainTabPane() {
        return MainTabPane;
    }

    public Stage getStage() {
        return stage;
    }

    public Pane getMainPane() {
        return MainPane;
    }

    public javafx.scene.layout.VBox getVBox() {
        return VBox;
    }

}
