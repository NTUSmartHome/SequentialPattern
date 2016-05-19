package GUI.Controller;

import Pattern.TestLifePattern;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.util.ArrayList;

/**
 * Created by g2525_000 on 2016/4/27.
 */
public class MainController {
    @FXML
    protected AnchorPane MainPane;
    @FXML
    protected TabPane MainTabPane;
    @FXML
    protected VBox VBox;
    private Stage stage;
    private TestLifePattern testLifePattern;

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
                        setSizetPropertyListener(true, MainTabPane, activityTabController.getMainPane(), -20);
                        setSizetPropertyListener(false, MainTabPane, activityTabController.getMainPane(), -10);
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
    protected void load(ActionEvent event) throws IOException {
        //testLifePattern = new TestLifePattern();
        ArrayList<String> activityList = testLifePattern.getActivityList();
        for (int i = 0; i < activityList.size(); i++) {
            Tab tab = new Tab(activityList.get(i));
            tab.setStyle("-fx-font-size: 16;");
            TableView<ActivityPerformHobby> table = new TableView();
            table.setFixedCellSize(80);
            table.setStyle("-fx-font-size: 16;");
            table.setEditable(true);
            TableColumn startTimeCol = new TableColumn("Start Time");
            TableColumn durationCol = new TableColumn("Duration");
            startTimeCol.setPrefWidth(500);
            startTimeCol.setCellValueFactory(
                    new PropertyValueFactory<ActivityPerformHobby, String>("startTime"));
            durationCol.setPrefWidth(500);
            durationCol.setCellValueFactory(
                    new PropertyValueFactory<ActivityPerformHobby, String>("duration"));

            ObservableList<ActivityPerformHobby> data = FXCollections.observableArrayList();
            String[] startTime = testLifePattern.getActivityStartTimeClusterer().get(activityList.get(i)).getStartTime();
            for (int j = 0; j < startTime.length; j++) {
                data.add(new ActivityPerformHobby(startTime[j], testLifePattern.getRegressors().get(activityList.get(i)).get(j).getDuration()));
            }
            tab.setContent(table);
            table.setItems(data);
            table.getColumns().addAll(startTimeCol, durationCol);


            MainTabPane.getTabs().add(tab);
        }
    }

    @FXML
    protected void resizeTabPane(ActionEvent event) {
        System.out.println(MainTabPane.getHeight());
    }

    public TabPane getMainTabPane() {
        return MainTabPane;
    }

    public Stage getStage() {
        return stage;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public Pane getMainPane() {
        return MainPane;
    }

    public javafx.scene.layout.VBox getVBox() {
        return VBox;
    }


    private void setSizetPropertyListener(boolean isHeight, Pane root, Pane follow, int offset) {
        if (isHeight) {
            root.heightProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                    follow.setPrefHeight(newValue.doubleValue() + offset);
                }
            });
        } else {
            root.widthProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                    follow.setPrefWidth(newValue.doubleValue() + offset);
                }
            });
        }

    }

    private void setSizetPropertyListener(boolean isHeight, Pane root, Control follow, int offset) {
        if (isHeight) {
            root.heightProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                    follow.setPrefHeight(newValue.doubleValue() + offset);
                }
            });
        } else {
            root.widthProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                    follow.setPrefWidth(newValue.doubleValue() + offset);
                }
            });
        }

    }

    private void setSizetPropertyListener(boolean isHeight, Control root, Pane follow, int offset) {
        if (isHeight) {
            root.heightProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                    follow.setPrefHeight(newValue.doubleValue() + offset);
                }
            });
        } else {
            root.widthProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                    follow.setPrefWidth(newValue.doubleValue() + offset);
                }
            });
        }

    }

    private void setSizetPropertyListener(boolean isHeight, Control root, Control follow, int offset) {
        if (isHeight) {
            root.heightProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                    follow.setPrefHeight(newValue.doubleValue() + offset);
                }
            });
        } else {
            root.widthProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                    follow.setPrefWidth(newValue.doubleValue() + offset);
                }
            });
        }

    }

    public TestLifePattern getTestLifePattern() {
        return testLifePattern;
    }

    public void setTestLifePattern(TestLifePattern testLifePattern) {
        this.testLifePattern = testLifePattern;
    }

    public static class ActivityPerformHobby {
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
}
