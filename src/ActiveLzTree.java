import java.util.ArrayList;

/**
 * Created by g2525_000 on 2015/8/31.
 */
public class ActiveLzTree {
    class Node{
        ArrayList<Node> children;
        int numOfChildren;
        int fre;
        String activity;

        Node(String activity) {
            this.activity = activity;
        }
        Node(){

        }
    }
    void init(String activityList){
        ArrayList<String> window = new ArrayList<>();
        ArrayList<String> phase = new ArrayList<>();
        int maxLength = 0;
        String[] activity = activityList.split(",");

    }

}
