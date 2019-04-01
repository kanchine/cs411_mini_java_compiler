package analysis.implementation;

import ir.temp.Temp;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import util.ActiveSet;
import util.List;

import analysis.FlowGraph;
import analysis.Liveness;
import util.graph.Node;


public class LivenessImplementation<N> extends Liveness<N> {

    private Map<Node<N>, ActiveSet<Temp>> liveinMap;
    private Map<Node<N>, ActiveSet<Temp>> liveoutMap;

    public LivenessImplementation(FlowGraph<N> graph) {
        super(graph);

        liveinMap = new HashMap<>();
        liveoutMap = new HashMap<>();

        // initialize live-in and live-out sets for each node
        for (Node<N> node : g.nodes()) {
            liveinMap.put(node, new ActiveSet<>());
            liveoutMap.put(node, new ActiveSet<>());
        }

        // compute liveness information of each node using equations 10.3 on P206 and active sets
        // An alternative implementation is using algorithm 10.4 on P206
        for (Node<N> node : g.nodes()) {
            // add dependencies for out[n] (i.e. sets that out[n] depends on)
            // out[n] = U_{s in succ[n]} in[s]
            for (Node<N> successor : node.succ()) {
                liveoutMap.get(node).addAll(liveinMap.get(successor)); // addAll() sets up dependency
            }

            // add dependencies for in[n] (i.e. sets that in[n] depends on)
            // in[n] = use[n] U (out[n] - def[n])
            ActiveSet<Temp> liveinSet = liveinMap.get(node);
            liveinSet.addAll(g.use(node)); // use[n]
            liveinSet.addAll(liveoutMap.get(node).remove(g.def(node))); // out[n] - def[n]
        }
    }

    @Override
    // Returns a list of Temps that are live *after* the execution of a given node in the FlowGraph.
    public List<Temp> liveOut(Node<N> node) {
        if (node == null) {
            throw new IllegalArgumentException("input node is null");
        }

        if (node.getGraph() != g) {
            throw new IllegalArgumentException("input node is not in this graph");
        }

        return liveoutMap.get(node).getElements();
    }

    // Returns a list of Temps that are live *before* the execution of a given node in the FlowGraph.
    private List<Temp> liveIn(Node<N> node) {
        if (node == null) {
            throw new IllegalArgumentException("input node is null");
        }

        if (node.getGraph() != g) {
            throw new IllegalArgumentException("input node is not in this graph");
        }

        return liveinMap.get(node).getElements();
    }

    private String shortList(List<Temp> l) {
        java.util.List<String> reall = new java.util.ArrayList<String>();
        for (Temp t : l) {
            reall.add(t.toString());
        }
        Collections.sort(reall);
        return String.valueOf(reall);
    }

    private String dotLabel(Node<N> n) {
        return shortList(liveIn(n)) +
                "\\n" +
                n +
                ": " +
                n.wrappee() +
                "\\n" +
                shortList(liveOut(n));
    }

    private double fontSize() {
        return (Math.max(30, Math.sqrt(Math.sqrt(g.nodes().size() + 1)) * g.nodes().size() * 1.2));
    }

    private double lineWidth() {
        return (Math.max(3.0, Math.sqrt(g.nodes().size() + 1) * 1.4));
    }

    private double arrowSize() {
        return Math.max(2.0, Math.sqrt(Math.sqrt(g.nodes().size() + 1)));
    }

    @Override
    public String dotString(String name) {
        StringBuilder out = new StringBuilder();
        out.append("digraph \"Flow graph\" {\n");
        out.append("labelloc=\"t\";\n");
        out.append("fontsize=").append(fontSize()).append(";\n");
        out.append("label=\"").append(name).append("\";\n");

        out.append("  graph [size=\"6.5, 9\", ratio=fill];\n");
        for (Node<N> n : g.nodes()) {
            out.append("  \"").append(dotLabel(n)).append("\" [fontsize=").append(fontSize());
            out.append(", style=\"setlinewidth(").append(lineWidth()).append(")\", color=").append(g.isMove(n) ? "green" : "blue");
            out.append("]\n");
        }
        for (Node<N> n : g.nodes()) {
            for (Node<N> o : n.succ()) {
                out.append("  \"").append(dotLabel(n)).append("\" -> \"").append(dotLabel(o)).append("\" [arrowhead = normal, arrowsize=").append(arrowSize()).append(", style=\"setlinewidth(").append(lineWidth()).append(")\"];\n");
            }
        }

        out.append("}\n");
        return out.toString();
    }

}
