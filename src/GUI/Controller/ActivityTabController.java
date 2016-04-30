package GUI.Controller;

import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;

/**
 * Created by g2525_000 on 2016/4/28.
 */
public class ActivityTabController {
    @FXML
    protected AnchorPane MainPane;
    @FXML
    protected ScrollPane MainScrollPane;
    @FXML
    protected TableView contentTable;

    public TableView getContentTable() {
        return contentTable;
    }

    public ScrollPane getMainScrollPane() {
        return MainScrollPane;
    }

    public AnchorPane getMainPane() {
        return MainPane;
    }
}
