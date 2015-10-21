package alz;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
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

    public static class Node implements Comparable<Node> {
        List<Node> children;
        Node parent;
        int inFre;
        int outFre;
        String activity;
        int duration;
        int level;
        Node(String activity, int level) {
            this.activity = activity;
            children = new ArrayList<>();
            this.level = level;
        }


        @Override
        public int compareTo(Node o) {
            int c = this.activity.compareTo(o.activity);
            if (c != 0) return c;
            c = this.duration - o.duration;
            return c;
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
        root = new Node("root", 1);
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
            node = new Node(lastActivity, parentNode.level+1);
            node.parent = parentNode;
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
        Node child = new Node(lastActivity,x.level+1);
        child.parent = x;
        //x.children.add(new Node(lastActivity));
        x.children.add(child);
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

        for (String d : phase) {

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

    public void levelOrderPrint() {
        Queue<Node> level = new ArrayDeque<>();
        level.add(root);

        int lv = 1;
        boolean isEnd = false;
        while (!level.isEmpty()) {
            Node node = level.poll();
            if (node.activity.equals("null")) {
                System.out.print("0");
                isEnd = false;
                continue;
            }
            if (node.level > lv) {
                System.out.println();
                ++lv;
            }
            if (node.activity.equals("end") && isEnd) {
                break;
            } else isEnd = false;
            if(node.activity.equals("end")) {
                System.out.print("|");
                isEnd = true;
            } else {
                System.out.print(node.activity + "(" + node.inFre + ")" +  ",");
                if (node.children.size() == 0) {
                    level.add(new Node("null", node.level + 1));
                }
                for (int i = 0; i < node.children.size(); i++) {
                    level.add(node.children.get(i));
                }
                level.add(new Node("end", node.level + 1));
            }


        }
    }
    public void finish(){
        while (window.size() > 0 ) {
            window.remove(0);
            incrementAllSuffixes();
        }
    }
    public static void wsuOneDay() {
        ActiveLzTree alz = new ActiveLzTree();
        alz.init();
        ArrayList<String> actSeq = new ArrayList<>();
        try {
            FileReader fr = new FileReader("db/SeqOneDay.txt");
            BufferedReader br = new BufferedReader(fr);
            String line;
            String[] rawData;
            while ((line = br.readLine()) != null) {
                rawData = line.split(",");
                String[] act = rawData[0].split(":");
                actSeq.add(act[1].trim());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < actSeq.size(); i++) {
            alz.step(actSeq.get(i));
        }
        alz.levelOrderPrint();
        PPM.init(alz);
        PPM.addSeenActivity("1", 0);
        PPM.addSeenActivity("1", 0);
       // Map<String, Double> pre = PPM.prediction(0);
    }

    public static void main(String[] args) {
        ActiveLzTree alz = new ActiveLzTree();
        alz.init();
        String s = "a,a,a,b,a,b,b,b,b,b,a,a,b,c,c,d,d,c,b,a,a,a,a";
        String[] ss = s.split(",");
        for (int i = 0; i < ss.length; i++) {
            alz.step(ss[i]);
        }
        //alz.finish();

        wsuOneDay();

    }


}
