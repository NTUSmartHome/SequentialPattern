package alz;

import java.util.*;

/**
 * Created by MingJe on 2015/9/4.
 */
public class PPM {
    private static List<List<String>> allSeenActivity = new ArrayList<>();
    private static List<Integer> allMaxLength = new ArrayList<>();
    private static List<ActiveLzTree> allActiveLzTrees = new ArrayList<>();

    public static void init(ActiveLzTree alz) {
        //allSeenActivity.add(new ArrayList<>());
        allSeenActivity.add(alz.getWindow());
        allMaxLength.add(alz.getMaxLength());
        allActiveLzTrees.add(alz);
    }

    public static List<Map.Entry<String, Double>> prediction(int idx) {
        ActiveLzTree alz = allActiveLzTrees.get(idx);
        ArrayDeque pathNode = null;
        List<String> copySeenActivity = new ArrayList<>();
        List<String> seenActivity = allSeenActivity.get(idx);
        for (int i = 2; i < allSeenActivity.get(idx).size(); i++) {
            copySeenActivity.add(seenActivity.get(i));
        }

        pathNode = alz.findActivityNodePath(copySeenActivity);


        /*for (int i = copySeenActivity.size(); i >= 0 ; i--) {
            pathNode = alz.findActivityNodePath(copySeenActivity);
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

    public static void clearSeenActivity(int idx) {
        allSeenActivity.get(idx).clear();
    }

    public static void addSeenActivity(String activity, int idx) {
        List<String> seenActivity = allSeenActivity.get(idx);
        if (seenActivity.size() != 0 && allMaxLength.get(idx) == seenActivity.size())
            seenActivity.remove(0);
        seenActivity.add(activity);

    }
}
