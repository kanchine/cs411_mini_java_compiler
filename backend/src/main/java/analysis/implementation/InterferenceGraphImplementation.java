package analysis.implementation;

import analysis.FlowGraph;
import analysis.InterferenceGraph;
import codegen.assem.A_MOVE;
import ir.temp.Color;
import ir.temp.Temp;
import util.IndentingWriter;
import util.List;
import util.graph.Node;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class InterferenceGraphImplementation<N> extends InterferenceGraph {

    private FlowGraph<N> fg;
    private LivenessImplementation<N> liveness;
    private List<Move> moves = List.empty();

    public InterferenceGraphImplementation(FlowGraph<N> fg) {
        this.fg = fg;
        this.liveness = new LivenessImplementation<N>(fg);

        initInterferenceGraph();
    }

    private void initInterferenceGraph() {
        for (Node<N> node : fg.nodes()) {
            handleNode(node);
        }
    }

    private void handleNode(Node<N> node) {
        if (isMoveNode(node)) {
            addMove(node);
            addMoveEdges(node);
        } else {
            addNonMoveEdges(node);
        }
    }

    private boolean isMoveNode(Node<N> node) {
        return node.wrappee() instanceof A_MOVE;
    }

    private void addMove(Node<N> node) {
        A_MOVE aMove = (A_MOVE) node.wrappee();
        Node<Temp> dst = nodeFor(aMove.dst);
        Node<Temp> src = nodeFor(aMove.src);

        Move move = new Move(dst, src);
        moves.add(move);
    }

    private void addMoveEdges(Node<N> node) {
        A_MOVE aMove = (A_MOVE) node.wrappee();
        Temp dst = aMove.dst;
        Temp src = aMove.src;

        for (Temp out : liveness.liveOut(node)) {
            if (!out.equals(src)) {
                linkTemps(out, dst);
            }
        }
    }

    private void addNonMoveEdges(Node<N> node) {
        for (Temp def : fg.def(node)) {
            for (Temp out : liveness.liveOut(node)) {
                linkTemps(def, out);
            }
        }
    }

    private void linkTemps(Temp x, Temp y) {
        if (x.equals(y)) {
            return;   // never add a self-cycle
        }

        Node<Temp> xNode = nodeFor(x);
        Node<Temp> yNode = nodeFor(y);

        addEdge(xNode, yNode);
        addEdge(yNode, xNode);
    }

    @Override
    public List<Move> moves() {
        return moves;
    }

    @Override
    public void dump(IndentingWriter out) {
        out.println("Liveness info: ");
        out.println(liveness);

        out.println("Interference graph: ");
        super.dump(out);

        out.print("Moves");
        out.println(moves);
    }


    private Color colorOf(Temp t, Map<Temp, Color> xcolorMap) {
        Color c = null;
        if (xcolorMap != null) c = xcolorMap.get(t);
        if (c == null)
            c = t.getColor();
        return c;
    }

    private String colorStringOf(Color c, Map<String, String> colorMap) {
        String s = null;
        if (c != null) s = colorMap.get(c.toString());
        if (s == null)
            return "red";
        else
            return s;
    }

    @Override
    public String dotString(int K, Map<Temp, Color> xcolorMap) {
        Map<String, String> colorMap = new HashMap<String, String>();
        colorMap.put("%rax", "gray");
        colorMap.put("%rbx", "hotpink");
        colorMap.put("%rcx", "mediumpurple");
        colorMap.put("%rdx", "salmon");
        colorMap.put("%rdi", "beige");
        colorMap.put("%rsi", "brown");
        colorMap.put("%rsp", "orange");
        colorMap.put("%rbp", "gold");
        colorMap.put("%r8", "yellow");
        colorMap.put("%r9", "darkgreen");
        colorMap.put("%r10", "green");
        colorMap.put("%r11", "cyan");
        colorMap.put("%r12", "blueviolet");
        colorMap.put("%r13", "skyblue");
        colorMap.put("%r14", "magenta");
        colorMap.put("%r15", "turquoise");
        HashSet<String> missing = new HashSet<String>(colorMap.keySet());
        StringBuilder out = new StringBuilder();
        out.append("graph \"Interference graph\" {\n");
        out.append("labelloc=\"t\";\n");
        out.append("fontsize=").append(Math.max(30, Math.sqrt(nodes().size() + 1) * nodes().size() * 1.2)).append(";\n");
        out.append("label=\"").append(name).append("\";\n");

        out.append("  graph [size=\"6.5, 9\", ratio=fill];\n");
        for (Node<Temp> n : nodes()) {
            if (n.succ().size() > 0) {
                out.append("  \"").append(n).append("\" [fontsize=").append(Math.max(30, Math.sqrt(nodes().size() + 1) * nodes().size()));
                if (colorOf(n.wrappee(), xcolorMap) == null) {
                    out.append(", style=\"setlinewidth(").append(Math.min(10, nodes().size())).append(")\", color=").append(n.outDegree() < K ? "green" : "red");
                } else {
                    Color color = colorOf(n.wrappee(), xcolorMap);
                    out.append(", style=filled, color=").append(colorStringOf(color, colorMap));
                    for (Temp t : n.wrappee().elements()) {
                        missing.remove(t.toString());
                    }
                }
                out.append("]\n");
            }
        }
        for (String m : missing) {
            out.append("  \"").append(m).append("\" [fontsize=").append(Math.max(30, Math.sqrt(nodes().size() + 1) * nodes().size()));
            out.append(", style=filled, color=").append(colorMap.get(m));
            out.append("]\n");
        }
        Set<Pair<Temp, Temp>> done = new HashSet<Pair<Temp, Temp>>();
        for (Node<Temp> n : nodes()) {
            for (Node<Temp> o : n.succ()) {
                Pair<Temp, Temp> p = new Pair<Temp, Temp>(o.wrappee(), n.wrappee());
                if (!done.contains(p)) {
                    out.append("  \"").append(n).append("\" -- \"").append(o).append("\";\n");
                    done.add(new Pair<Temp, Temp>(n.wrappee(), o.wrappee()));
                } // else do nothing
            }
        }

        for (Move m : moves) {
            Node<Temp> s = m.src;
            Node<Temp> d = m.dst;
            out.append("\"").append(s).append("\" -- \"").append(d).append("\" [style=\"dashed, setlinewidth(5)\", color=").append("green").append("];\n");
        }
        out.append("}\n");
        return out.toString();
    }

}
