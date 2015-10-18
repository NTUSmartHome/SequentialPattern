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
        allSeenActivity.add(new ArrayList<>());
        allMaxLength.add(alz.getMaxLength());
        allActiveLzTrees.add(alz);
    }

    public static Map<String, Double> prediction(int idx) {
        ActiveLzTree alz = allActiveLzTrees.get(idx);
        ArrayDeque pathNode = alz.findActivityNodePath(allSeenActivity.get(idx));
        List<String> allActivity = alz.getAllActivity();
        Map<String,Double> prediction = new HashMap<>();
        double prob = 0;
        ActiveLzTree.Node prev =null;
        for (int i = 0; i < allActivity.size(); i++) {
            ArrayDeque copy = (ArrayDeque)pathNode.clone();
            while (!copy.isEmpty()) {
                ActiveLzTree.Node x = (ActiveLzTree.Node) copy.poll();
                prob *= (1 - (double)x.outFre / x.inFre);
                for (int j = 0; j < x.children.size(); j++) {
                    ActiveLzTree.Node child = x.children.get(j);

                    if (child.activity.equals(allActivity.get(i))) {
                        prob += (double)child.inFre / x.inFre;
                        break;
                    }
                }
            }
            prediction.put(allActivity.get(i),prob);
            prob = 0;
        }

        return prediction;

    }

    public static void addSeenActivity(String activity, int idx) {
        List<String> seenActivity = allSeenActivity.get(idx);
        if (allMaxLength.get(idx) == seenActivity.size())
            seenActivity.remove(0);
        seenActivity.add(activity);

    }
}
