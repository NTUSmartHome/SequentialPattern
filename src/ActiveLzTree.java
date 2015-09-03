import java.util.ArrayList;
import java.util.List;

/**
 * Created by g2525_000 on 2015/8/31.
 */
public class ActiveLzTree {
    Node root;

    List<String> window;
    List<String> phase;
    List<String> seenActivity;
    int maxLength;

    class Node {
        List<Node> children;
        int numOfChildren;
        int fre;
        String activity;

        Node(String activity) {
            this.activity = activity;
        }

        Node() {

        }
    }

    public void init() {
        window = new ArrayList<>();
        phase = new ArrayList<>();
        seenActivity = new ArrayList<>();
        root = new Node("root");
    }

    public void step(String activity) {
        if (!seenActivity.contains(activity))
            seenActivity.add(activity);

        if (findActivityNode(phase) == null) {
            phase.add(activity);
        } else {
            addActivity(phase);
            if (phase.size() > maxLength)
                ++maxLength;
            phase.clear();
        }

        window.add(activity);
        if(window.size() > maxLength)
            window.remove(0);

        incrementAllSuffixes();
    }
    private void incrementAllSuffixes(){
        List<List<String>> suffixList = getAllSuffixes();

        for (int i = 0; i < suffixList.size(); i++) {
                incrementSuffix(suffixList.get(i));
        }
    }
    private void incrementSuffix(List<String> suffix){
        String lastActivity = suffix.remove(suffix.size()-1);
        Node parentNode = findActivityNode(suffix);
        Node node;
        for (int i = 0; i < parentNode.numOfChildren; i++) {

        }
    }
    private List<List<String>> getAllSuffixes() {
        List<List<String>> allSuffixes = new ArrayList<>();
        List<String> suffixes;
        for (int i = 0; i < window.size(); i++) {
            suffixes = new ArrayList<>();
            for (int j = i; j < window.size() ; j++) {
                suffixes.add(window.get(i));
            }
            allSuffixes.add(suffixes);
        }
        return allSuffixes;
    }
    private void addActivity(List<String> phase) {
        if (phase.size() == 1) {
            root.children.add(new Node(phase.get(0)));
            ++root.numOfChildren;
        } else {
            String lastActivity = phase.remove(phase.size() - 1);
            Node x = findActivityNode(phase);
            x.children.add(new Node(lastActivity));
            ++x.numOfChildren;
        }

    }

    private Node findActivityNode(List<String> phase) {
        Node x = root;
        for (int i = 0; i < phase.size(); i++) {
            for (int j = 0; j < x.numOfChildren; j++) {
                Node child = x.children.get(i);
                if (child.equals(phase.get(i))) {
                    x = child;
                    break;
                }
            }
            if (x == null) return null;
        }
        return x;
    }

}
