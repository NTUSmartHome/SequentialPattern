import java.util.ArrayList;
import java.util.List;

/**
 * Created by g2525_000 on 2015/8/31.
 */
public class ActiveLzTree {
    private Node root;

    private List<String> window;
    private List<String> phase;
    int maxLength;

    private class Node {
        List<Node> children;
        int fre;
        int sumFreOfSon;
        String activity;
        Node(String activity) {
            this.activity = activity;
            children = new ArrayList<>();
        }

    }
    public int getMaxLength(){
        return maxLength;
    }
    public void init() {
        window = new ArrayList<>();
        phase = new ArrayList<>();
        root = new Node("root");
    }
    public void finish(){
        --maxLength;
        window.clear();
        phase.clear();
        calSumFre(root);
    }
    private void calSumFre(Node x) {
        if (x == null) return;
        for (int i = 0; i < x.children.size(); i++) {
            Node child = x.children.get(i);
            x.sumFreOfSon += child.fre;
        }
        for (int i = 0; i < x.children.size(); i++) {
            Node child = x.children.get(i);
           new Thread(()->calSumFre(child)).start();
        }
    }
    public void step(String activity) {

        List<String> newPattern = new ArrayList<>(phase);
        newPattern.add(activity);
        if (findActivityNode(newPattern) != root) {
            phase = newPattern;
        } else {
            addActivity(newPattern);
            if (newPattern.size() > maxLength)
                ++maxLength;
            phase.clear();
        }

        window.add(activity);
        if (window.size() > maxLength)
            window.remove(0);

        incrementAllSuffixes();
    }

    private void incrementAllSuffixes() {
        List<List<String>> suffixList = getAllSuffixes();

        for (int i = 0; i < suffixList.size(); i++) {
            incrementSuffix(suffixList.get(i));
        }
    }

    private void incrementSuffix(List<String> suffix) {
        String lastActivity = suffix.remove(suffix.size() - 1);
        Node parentNode = findActivityNode(suffix);
        Node node = null;
        for (int i = 0; i < parentNode.children.size(); i++) {
            Node tmp = parentNode.children.get(i);
            if (tmp.activity.equals(lastActivity)) {
                node = tmp;
            }
        }
        if (node == null) {
            parentNode.children.add(new Node(lastActivity));
            node = parentNode.children.get(parentNode.children.size() - 1);
        }
        node.fre++;
    }

    private List<List<String>> getAllSuffixes() {
        List<List<String>> allSuffixes = new ArrayList<>();
        List<String> suffixes;
        for (int i = 0; i < window.size(); i++) {
            suffixes = new ArrayList<>();
            for (int j = i; j < window.size(); j++) {
                suffixes.add(window.get(j));
            }
            allSuffixes.add(suffixes);
        }
        return allSuffixes;
    }

    private Node addActivity(List<String> phase) {
        if (phase.size() == 1) {
            root.children.add(new Node(phase.get(0)));
            return root.children.get(root.children.size() - 1);
        } else {
            String lastActivity = phase.remove(phase.size() - 1);
            Node x = findActivityNode(phase);
            x.children.add(new Node(lastActivity));
            phase.add(lastActivity);
            return x.children.get(x.children.size() - 1);
        }

    }

    private Node findActivityNode(List<String> phase, boolean update) {
        Node x = root;
        boolean isFind = false;
        List<Node> updateNodes;
        if (update) updateNodes = new ArrayList<>();
        for (int i = 0; i < phase.size(); i++) {
            for (int j = 0; j < x.children.size(); j++) {
                Node child = x.children.get(j);
                if (child.activity.equals(phase.get(i))) {
                    x = child;
                    isFind = true;
                    break;
                }
            }
            if (!isFind) return root;
            isFind = false;
        }
        return x;
    }

    public static void main(String[] args) {
        ActiveLzTree alz = new ActiveLzTree();
        alz.init();
        String s = "a,a,a,b,a,b,b,b,b,b,a,a,b,c,c,d,d,c,b,a,a,a,a";
        String[] ss = s.split(",");
        for (int i = 0; i < ss.length; i++) {
            alz.step(ss[i]);
        }
        alz.finish();

    }


}
