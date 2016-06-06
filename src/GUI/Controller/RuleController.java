package GUI.Controller;

import Pattern.TestLifePattern;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;

import java.util.ArrayList;

/**
 * Created by g2525_000 on 2016/6/6.
 */
public class RuleController {
    @FXML
    protected ComboBox Rule_startTimeCondition;
    @FXML
    protected ComboBox Rule_startTimeActivityComboBox;
    @FXML
    protected ComboBox Rule_durationConditionComboBox;
    @FXML
    protected ComboBox Rule_durationActivityComboBox;
    @FXML
    protected ComboBox Rule_durationLengthComboBox;
    @FXML
    protected ComboBox Rule_nextOfNextActivityComboBox;
    @FXML
    protected ComboBox Rule_nextOfConditionComboBox;
    @FXML
    protected ComboBox Rule_nextOfActivityComboBox;
    @FXML
    protected ComboBox Rule_nextActivityComboBox;
    @FXML
    protected Button Rule_insertButton;
    @FXML
    protected ComboBox Rule_rulesComboBox;
    @FXML
    protected Button Rule_removeButton;

    private Stage stage;
    private TestLifePattern testLifePattern;

    public ComboBox getRule_durationConditionComboBox() {
        return Rule_durationConditionComboBox;
    }

    public ComboBox getRule_durationActivityComboBox() {
        return Rule_durationActivityComboBox;
    }

    public ComboBox getRule_durationLengthComboBox() {
        return Rule_durationLengthComboBox;
    }

    public ComboBox getRule_nextOfNextActivityComboBox() {
        return Rule_nextOfNextActivityComboBox;
    }

    public ComboBox getRule_nextOfActivityComboBox() {
        return Rule_nextOfActivityComboBox;
    }

    public ComboBox getRule_nextOfConditionComboBox() {
        return Rule_nextOfConditionComboBox;
    }

    public ComboBox getRule_nextActivityComboBox() {
        return Rule_nextActivityComboBox;
    }

    public Button getRule_insertButton() {
        return Rule_insertButton;
    }

    public ComboBox getRule_rulesComboBox() {
        return Rule_rulesComboBox;
    }

    public Button getRule_removeButton() {
        return Rule_removeButton;
    }

    public Stage getStage() {
        return stage;
    }

    public ComboBox getRule_startTimeCondition() {
        return Rule_startTimeCondition;
    }

    public ComboBox getRule_startTimeActivityComboBox() {
        return Rule_startTimeActivityComboBox;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public TestLifePattern getTestLifePattern() {
        return testLifePattern;
    }

    public void setTestLifePattern(TestLifePattern testLifePattern) {
        this.testLifePattern = testLifePattern;
    }

    public void setComboBox(ArrayList<String> activityNStartTime, ArrayList<String> activityList) {
        Rule_durationActivityComboBox.getItems().addAll(activityNStartTime);
        Rule_nextOfNextActivityComboBox.getItems().addAll(activityList);
        Rule_nextActivityComboBox.getItems().addAll(activityList);
        Rule_nextOfActivityComboBox.getItems().addAll(activityNStartTime);
        Rule_durationConditionComboBox.getItems().addAll("larger than", "shorter than");
        Rule_nextOfConditionComboBox.getItems().addAll("is", "isn't");
        Rule_startTimeActivityComboBox.getItems().addAll(activityNStartTime);
        Rule_startTimeCondition.getItems().addAll("outside", "inside");
    }
}
