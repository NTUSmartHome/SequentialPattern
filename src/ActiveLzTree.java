import java.util.*;

/**
 * Created by g2525_000 on 2015/8/31.
 */
public class ActiveLzTree {
    private Node root;

    private List<String> window;
    private List<String> phase;
    private List<String> allActivity;
    int maxLength;

    public static class Node {
        List<Node> children;
        Node parent;
        int inFre;
        int outFre;
        String activity;

        Node(String activity) {
            this.activity = activity;
            children = new ArrayList<>();
        }

    }

    public List<String> getAllActivity() {
        return allActivity;
    }

    public int getMaxLength() {
        return maxLength;
    }

    public void init() {
        window = new ArrayList<>();
        phase = new ArrayList<>();
        root = new Node("root");
        allActivity = new ArrayList<>();
    }


    public void step(String activity) {

        if (!allActivity.contains(activity)) allActivity.add(activity);

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
        Node node = getChild(parentNode, lastActivity);
        if (node == null) {
            node = new Node(lastActivity);
            parentNode.children.add(node);
        }
        ++parentNode.outFre;
        ++node.inFre;
        root.inFre = root.outFre;
    }

    private Node getChild(Node parent, String childActivity) {
        Node node = null;
        for (int i = 0; i < parent.children.size(); i++) {
            Node tmp = parent.children.get(i);
            if (tmp.activity.equals(childActivity)) {
                node = tmp;
                break;
            }
        }
        return node;
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
        String lastActivity = phase.remove(phase.size() - 1);
        Node x = findActivityNode(phase);
        Node child = new Node(lastActivity);
        child.parent = x;
        x.children.add(new Node(lastActivity));
        phase.add(lastActivity);
        return child;


    }

    public Node findActivityNode(List<String> phase) {
        if (phase.size() == 0) return root;
        Node x = root;
        boolean isFind = false;

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

    public ArrayDeque<Node> findActivityNodePath(List<String> phase) {
        ArrayDeque<Node> s = new ArrayDeque<>();
        s.add(root);
        if (phase.size() == 0) return s;

        for(String d : phase) {

        }
        Node x = root;
        boolean isFind = false;

        for (int i = 0; i < phase.size(); i++) {
            for (int j = 0; j < x.children.size(); j++) {
                Node child = x.children.get(j);
                s.add(child);
                if (child.activity.equals(phase.get(i))) {
                    x = child;
                    isFind = true;
                    break;
                }
            }
            if (!isFind) {
                s.clear();
                s.add(root);
                return s;
            }
            isFind = false;
        }
        return s;

    }

    public static void main(String[] args) {
        ActiveLzTree alz = new ActiveLzTree();
        alz.init();
        String s = "a,a,a,b,a,b,b,b,b,b,a,a,b,c,c,d,d,c,b,a,a,a,a";
        String[] ss = s.split(",");
        for (int i = 0; i < ss.length; i++) {
            alz.step(ss[i]);
        }

        PPM.init(alz);
        PPM.addSeenActivity("a", 0);
        PPM.addSeenActivity("a", 0);
        Map<String, Double> pre = PPM.prediction(0);
    }


}
