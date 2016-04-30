package GUI.Controller;

import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;

/**
 * Created by g2525_000 on 2016/4/28.
 */
public class ActivityTabController {
    @FXML
    protected AnchorPane MainPane;
    @FXML
    protected ScrollPane MainScrollPane;


    public ScrollPane getMainScrollPane() {
        return MainScrollPane;
    }

    public AnchorPane getMainPane() {
        return MainPane;
    }
}
