package ALZ;

import java.util.*;

/**
 * Created by MingJe on 2015/9/4.
 */
public class PPM {
    private static Map<String, List<String>> allSeenActivity = new HashMap<>();
    private static HashMap<String, Integer> allMaxLength = new HashMap<>();
    private static Map<String, ActiveLzTree> allActiveLzTrees = new HashMap<>();

    public static void init(String id, ActiveLzTree alz) {
        //allSeenActivity.add(new ArrayList<>());
        allSeenActivity.put(id, alz.getWindow());
        allMaxLength.put(id, alz.getMaxLength());
        allActiveLzTrees.put(id, alz);
    }

    public static List<Map.Entry<String, Double>> prediction(String id) {
        ActiveLzTree alz = allActiveLzTrees.get(id);
        ArrayDeque pathNode;
        List<String> copySeenActivity = new ArrayList<>();
        List<String> seenActivity = allSeenActivity.get(id);
        for (int i = 2; i < allSeenActivity.get(id).size(); i++) {
            copySeenActivity.add(seenActivity.get(i));
        }

        pathNode = alz.findActivityNodePath(copySeenActivity);


        /*for (int i = copySeenActivity.size(); i >= 0 ; i--) {
            pathNode = ALZ.findActivityNodePath(copySeenActivity);
            if (pathNode.size() != 1 || copySeenActivity.size() == 0) {
                break;
            }
            copySeenActivity.remove(0);
        }*/

        List<String> allActivity = alz.getAllActivity();
        Map<String, Double> prediction = new HashMap<>();
        double prob = 0;
        ActiveLzTree.Node prev = null;
        for (int i = 0; i < allActivity.size(); i++) {
            ArrayDeque copy = pathNode.clone();
            while (!copy.isEmpty()) {
                ActiveLzTree.Node x = (ActiveLzTree.Node) copy.poll();
                //----------------------------------For debugging--------------------------------------//
                if (x.outFre > x.inFre) {
                    x.outFre++;
                    x.outFre--;
                }
                //------------------------------------------------------------------------------------//
                prob *= (1 - (double) x.outFre / x.inFre);
                for (int j = 0; j < x.children.size(); j++) {
                    ActiveLzTree.Node child = x.children.get(j);

                    if (child.activity.equals(allActivity.get(i))) {
                        prob += (double) child.inFre / x.inFre;
                        break;
                    }
                }
            }
            prediction.put(allActivity.get(i), prob);
            prob = 0;
        }
        List<Map.Entry<String, Double>> list = new ArrayList<>(prediction.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<String, Double>>() {
            @Override
            public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {

                return -Double.compare(o1.getValue(), o2.getValue());
            }

        });
        return list;

    }

    public static void clearSeenActivity(String id) {
        allSeenActivity.get(id).clear();
    }

    public static void addSeenActivity(String activity, String id) {
        List<String> seenActivity = allSeenActivity.get(id);
        if (seenActivity.size() != 0 && allMaxLength.get(id) == seenActivity.size())
            seenActivity.remove(0);
        seenActivity.add(activity);

    }
}
