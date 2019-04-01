package util.graph;

import util.List;

import static util.List.*;

/**
 * This is a wrapper class that turns any type of object into a graph node.
 * <p>
 * It is used by the Graph class to represent its nodes.
 */
public class Node<N> {

    Graph<N> mygraph; // tell which graph this node is in

    private int mykey; // each graph node has a unique key in its graph
    private N info; // the object represented by this graph node

    public Node(Graph<N> g, N content) {
        this.info = content;
        mygraph = g;
        mykey = g.nodecount++;
        try {
            g.mynodes.add(this);
        } catch (Error e) {
            g.mynodes = g.mynodes.append(list(this));
        }
    }

    List<Node<N>> succs = theEmpty();
    List<Node<N>> preds = theEmpty();

    public List<Node<N>> succ() {
        return succs;
    }

    public List<Node<N>> pred() {
        return preds;
    }

    public List<Node<N>> adj() {
        return succ().append(pred());
    }

    public int inDegree() {
        return pred().size();
    }

    public int outDegree() {
        return succ().size();
    }

    public int degree() {
        return inDegree() + outDegree();
    }

    public boolean goesTo(Node<N> n) {
        return succ().contains(n);
    }

    public boolean comesFrom(Node<N> n) {
        return pred().contains(n);
    }

    public boolean adj(Node<N> n) {
        return goesTo(n) || comesFrom(n);
    }

    public String toString() {
        return String.valueOf(mykey);
    }

    public int getKey() {
        return mykey;
    }

    public N wrappee() {
        return info;
    }

    public Graph<N> getGraph() {
        return mygraph;
    }

}
