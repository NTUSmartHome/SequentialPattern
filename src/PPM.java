import java.util.ArrayList;
import java.util.List;

/**
 * Created by MingJe on 2015/9/4.
 */
public class PPM {
    static List<List<Integer>> allOrderSun;
    static List<List<String>> seenActivity;
    public PPM(){
        allOrderSun = new ArrayList<>();
    }

    public static void init(ActiveLzTree alz) {
        allOrderSun.add(new ArrayList<>());
        seenActivity.add(new ArrayList<>());
    }

    public static void prediction(int idx){

    }

    public static void addSeenActivity(String activity, int idx){
        seenActivity.get(idx)
    }
}
