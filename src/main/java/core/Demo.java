package core;

/*
 * Created on Jan 2, 2004
 */

import core.model.Edge;
import edu.uci.ics.jung.algorithms.generators.random.EppsteinPowerLawGenerator;
import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import org.apache.commons.collections15.Factory;
import org.apache.commons.collections15.Transformer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.List;

/**
 * Demonstrates use of the shortest path algorithm and visualization of the
 * results.
 *
 * @author danyelf
 */
public class Demo extends JPanel {

    /**
     *
     */
    private static final long serialVersionUID = 7526217664458188502L;

    /**
     * Starting vertex
     */
    private String mFrom;

    /**
     * Ending vertex
     */
    private String mTo;
    private Graph<String, Edge> mGraph;
    private Set<String> mPred;
    private JTextField weightField;

    private DefaultComboBoxModel<String> fromModel = new DefaultComboBoxModel<String>();
    private DefaultComboBoxModel<String> toModel = new DefaultComboBoxModel<String>();
    private JComboBox selectFromBox;
    private JComboBox selectToBox;
    private List<Edge> shortestPath;

    public Demo() {
        setLayout(new BorderLayout());
        add(setUpControls(), BorderLayout.SOUTH);
        add(createMenu(), BorderLayout.NORTH);
    }

    private void initialize() {

        Component center = ((BorderLayout) getLayout()).getLayoutComponent(BorderLayout.CENTER);
        if (center != null) {
            remove(center);
        }
        //this.mGraph = getGraph();
        setBackground(Color.WHITE);
        // show graph
        final Layout<String, Edge> layout = new FRLayout<String, Edge>(mGraph);
        final VisualizationViewer<String, Edge> vv = new VisualizationViewer<String, Edge>(layout);
        vv.setBackground(Color.WHITE);

        vv.getRenderContext().setVertexDrawPaintTransformer(new MyVertexDrawPaintFunction<String>());
        vv.getRenderContext().setVertexFillPaintTransformer(new MyVertexFillPaintFunction<String>());
        vv.getRenderContext().setEdgeDrawPaintTransformer(new MyEdgePaintFunction());
        vv.getRenderContext().setEdgeStrokeTransformer(new MyEdgeStrokeFunction());
        vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller<String>());
        vv.getRenderContext().setEdgeLabelTransformer(new MyEdgeLabeller());
        vv.setGraphMouse(new DefaultModalGraphMouse<String, Edge>());
        vv.addPostRenderPaintable(new VisualizationViewer.Paintable() {

            public boolean useTransform() {
                return true;
            }

            public void paint(Graphics g) {
                if (mPred == null) return;

                // for all edges, paint edges that are in shortest path
                for (Edge e : layout.getGraph().getEdges()) {
                    if (isBlessed(e)) {
                        String v1 = mGraph.getEndpoints(e).getFirst();
                        String v2 = mGraph.getEndpoints(e).getSecond();
                        Point2D p1 = layout.transform(v1);
                        Point2D p2 = layout.transform(v2);
                        p1 = vv.getRenderContext().getMultiLayerTransformer().transform(Layer.LAYOUT, p1);
                        p2 = vv.getRenderContext().getMultiLayerTransformer().transform(Layer.LAYOUT, p2);
                        edu.uci.ics.jung.visualization.renderers.Renderer<String, Edge> renderer = vv.getRenderer();
                        renderer.renderEdge(
                                vv.getRenderContext(),
                                layout,
                                e);
                    }
                }
            }
        });

        add(vv, BorderLayout.CENTER);
    }

    private void addEdges() {

        JPanel panel = new JPanel();

        ArrayList<String> sortedList = new ArrayList<String>(mGraph.getVertices());
        Collections.sort(sortedList);
        Object[] vertices = sortedList.toArray();

        JComboBox fromBox = new JComboBox(vertices);
        JComboBox toBox = new JComboBox(vertices);
        JTextField weightField = new JTextField("10");
        weightField.setMinimumSize(new Dimension(100, 100));

        fromBox.setSelectedIndex(-1);
        toBox.setSelectedIndex(-1);

        panel.add(new JLabel("From:"));
        panel.add(fromBox);
        panel.add(Box.createHorizontalStrut(15)); // a spacer
        panel.add(new JLabel("To:"));
        panel.add(toBox);
        panel.add(Box.createHorizontalStrut(15)); // a spacer
        panel.add(new JLabel("Weight:"));
        panel.add(weightField);

        while (true) {
            int result = JOptionPane.showConfirmDialog(this, panel, "Enter edge parameters", JOptionPane.OK_CANCEL_OPTION);

            if (result == JOptionPane.OK_OPTION) {
                int weightValue = 0;
                try {
                    weightValue = Integer.parseInt(weightField.getText());
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "Incorrect weight value! Please try again.");
                    weightField.setText("10");
                    continue;
                }

                String from = (String) fromBox.getSelectedItem();
                String to = (String) toBox.getSelectedItem();
                Edge edge = new Edge(weightValue, from, to);

                if (!mGraph.containsEdge(edge)) {
                    mGraph.addEdge(edge, from, to);
                    repaint();
                }

            } else {
                return;
            }
        }
    }

    @Override
    public void repaint() {
        super.repaint();
    }

    private void updateModel() {
        if (toModel != null) {
            toModel.removeAllElements();
        }
        if (fromModel != null) {
            fromModel.removeAllElements();
        }

        if (mGraph != null) {
            String[] vertices = new String[mGraph.getVertexCount()];
            ArrayList<String> sortedList = new ArrayList<String>(mGraph.getVertices());
            Collections.sort(sortedList);
            vertices = sortedList.toArray(vertices);

            for (String vertex : vertices) {
                toModel.addElement(vertex);
                fromModel.addElement(vertex);
                //model.setSelectedItem(null);
            }
            selectFromBox.setSelectedIndex(-1);
            selectToBox.setSelectedIndex(-1);
        }
    }

    private JMenuBar createMenu() {
        JMenuBar mBar = new JMenuBar();

        JMenu mainMenu = new JMenu("Main menu");
        JMenu editMenu = new JMenu("Edit");

        JMenuItem createGraph = new JMenuItem("Create test graph");
        createGraph.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mGraph = getGraph(25, 20);
                initialize();
                updateModel();
                repaint();
                updateUI();
            }
        });

        JMenuItem create = new JMenuItem("Create graph");
        create.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                createGraph();
            }
        });
        JMenuItem addEdges = new JMenuItem("Add edges");
        addEdges.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addEdges();
            }
        });

        mainMenu.add(createGraph);

        editMenu.add(create);
        editMenu.add(addEdges);

        mBar.add(mainMenu);
        mBar.add(editMenu);

        return mBar;
    }

    private void createGraph() {
        int count = 0;
        while (true) {
            String vertexCount = JOptionPane.showInputDialog(this, "Vertex count:", 10);
            if (vertexCount == null) {
                return;
            }
            try {
                count = Integer.parseInt(vertexCount);
            } catch (NumberFormatException e) {
                continue;
            }
            break;
        }
        mGraph = getGraph(count);
        updateModel();

        initialize();
        addEdges();
        repaint();
    }

    boolean isBlessed(Edge e) {
        if (shortestPath == null) {
            return false;
        }

        if (shortestPath.contains(e)) {
            return true;
        }
        return false;
//        Pair<String> endpoints = mGraph.getEndpoints(e);
//        if (endpoints == null) return false;
//        String v1 = endpoints.getFirst();
//        String v2 = endpoints.getSecond();
//        return v1.equals(v2) == false && mPred.contains(v1) && mPred.contains(v2);
    }

    /**
     * @author danyelf
     */
    public class MyEdgePaintFunction implements Transformer<Edge, Paint> {

        public Paint transform(Edge e) {
            if (mPred == null || mPred.size() == 0) return Color.BLACK;
            if (isBlessed(e)) {
                return new Color(0.0f, 0.0f, 1.0f, 0.5f);//Color.BLUE;
            } else {
                return Color.LIGHT_GRAY;
            }
        }
    }

    public class WeightTransformer implements Transformer<Edge, Number> {

        @Override
        public Number transform(Edge edge) {
            return edge.getWeight();
        }
    }

    public class MyEdgeLabeller implements Transformer<Edge, String> {

        @Override
        public String transform(Edge edge) {
            return String.valueOf(edge.getWeight());
        }
    }

    public class MyEdgeStrokeFunction implements Transformer<Edge, Stroke> {
        protected final Stroke THIN = new BasicStroke(1);
        protected final Stroke THICK = new BasicStroke(1);

        public Stroke transform(Edge e) {
            if (mPred == null || mPred.size() == 0) return THIN;
            if (isBlessed(e)) {
                return THICK;
            } else
                return THIN;
        }

    }

    /**
     * @author danyelf
     */
    public class MyVertexDrawPaintFunction<V> implements Transformer<V, Paint> {

        public Paint transform(V v) {
            return Color.black;
        }

    }

    public class MyVertexFillPaintFunction<V> implements Transformer<V, Paint> {

        public Paint transform(V v) {
            if (v == mFrom) {
                return Color.BLUE;
            }
            if (v == mTo) {
                return Color.BLUE;
            }
            if (mPred == null) {
                return Color.LIGHT_GRAY;
            } else {
                if (mPred.contains(v)) {
                    return Color.RED;
                } else {
                    return Color.LIGHT_GRAY;
                }
            }
        }

    }

    /**
     *
     */
    private JPanel setUpControls() {
        JPanel jp = new JPanel();
        jp.setBackground(Color.WHITE);
        jp.setLayout(new BoxLayout(jp, BoxLayout.PAGE_AXIS));
        jp.setBorder(BorderFactory.createLineBorder(Color.black, 3));
        jp.add(
                new JLabel("Select a pair of vertices for which a shortest path will be displayed"));
        JPanel jp2 = new JPanel();
        jp2.add(new JLabel("vertex from", SwingConstants.LEFT));
        selectFromBox = getSelectionBox(true);
        jp2.add(selectFromBox);
        jp2.setBackground(Color.white);
        JPanel jp3 = new JPanel();
        jp3.add(new JLabel("vertex to", SwingConstants.LEFT));
        selectToBox = getSelectionBox(false);
        jp3.add(selectToBox);
        jp3.setBackground(Color.white);
        jp.add(jp2);
        jp.add(jp3);
        return jp;
    }

    private JComboBox getSelectionBox(final boolean from) {

//        Set<String> s = new TreeSet<String>();
//
//        for (String v : mGraph.getVertices()) {
//            s.add(v);
//        }
        final JComboBox choices = new JComboBox(from ? fromModel : toModel);
        choices.setSelectedIndex(-1);
        choices.setBackground(Color.WHITE);
        choices.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                String v = (String) choices.getSelectedItem();
                if (from) {
                    mFrom = v;
                } else {
                    mTo = v;
                }
                drawShortest();
                repaint();
                showDistance();
            }
        });
        return choices;
    }

    private void showDistance() {
        int shortestDistance = getDistance(shortestPath);
        if (shortestDistance > 0) {
            JOptionPane.showMessageDialog(this, "Shortest distance: " + shortestDistance, "Answer", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     *
     */
    protected void drawShortest() {
        if (mFrom == null || mTo == null) {
            return;
        }
        mPred = new HashSet<String>();

        DijkstraShortestPath<String, Edge> alg = new DijkstraShortestPath<String, Edge>(mGraph, new WeightTransformer());
        shortestPath = alg.getPath(mFrom, mTo);

        // grab a predecessor
        String v = mTo;
        Set<String> prd = new HashSet(shortestPath);
        for (Edge edge : shortestPath) {
            String first = mGraph.getEndpoints(edge).getFirst();
            String second = mGraph.getEndpoints(edge).getSecond();
            prd.add(first);
            prd.add(second);
        }
        mPred.addAll(prd);

    }

    private int getDistance(List<Edge> edges) {
        if (edges == null) {
            return 0;
        }
        int count = 0;

        for (Edge edge : edges) {
            count += edge.getWeight();
        }
        return count;
    }

    public static void main(String[] s) {
        JFrame jf = new JFrame();
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jf.getContentPane().add(new Demo());
        jf.pack();
        jf.setVisible(true);
    }

    /**
     * @param numVertices
     * @param numEdges
     * @return the graph for this demo
     */
    Graph<String, Edge> getGraph(int numVertices, int numEdges) {

        Graph<String, Edge> g =
                new EppsteinPowerLawGenerator<String, Edge>(
                        new GraphFactory(), new VertexFactory(), new EdgeFactory(VertexFactory.createRange(numVertices)), numVertices, numEdges, 50).create();
        Set<String> removeMe = new HashSet<String>();
        for (String v : g.getVertices()) {
            if (g.degree(v) == 0) {
                removeMe.add(v);
            }
        }
        for (String v : removeMe) {
            g.removeVertex(v);
        }
        return g;
    }

    Graph<String, Edge> getGraph(int vertexCount) {
        Graph<String, Edge> g =
                new EppsteinPowerLawGenerator<String, Edge>(
                        new GraphFactory(), new VertexFactory(), new EdgeFactory(VertexFactory.createRange(vertexCount)),
                        vertexCount, 0, 0).create();
        return g;
    }

    static class GraphFactory implements Factory<Graph<String, Edge>> {
        public Graph<String, Edge> create() {
            return new SparseMultigraph<String, Edge>();
        }
    }

    static class VertexFactory implements Factory<String> {
        char a = 'A';

        public String create() {
            return Character.toString(a++);
        }

        public static List<String> createRange(int count) {
            List<String> result = new ArrayList<String>(count);

            char start = 'A';
            for (int i = 0; i < count; i++) {
                result.add(Character.toString(start++));
            }
            return result;
        }

    }

    static class EdgeFactory implements Factory<Edge> {
        int count;
        private List<String> vertices;
        private List<Edge> edges = new ArrayList<Edge>();

        EdgeFactory(List<String> vertices) {
            this.vertices = vertices;
        }

        public Edge create() {
            int weight = (int) (Math.random() * 100);


            while (true) {
                int indexFrom = (int) (Math.random() * vertices.size());
                int indexTo = (int) (Math.random() * vertices.size());

                Edge edge = new Edge(weight, vertices.get(indexFrom), vertices.get(indexTo));
                if (!edges.contains(edge)) {
                    edges.add(edge);
                    return edge;
                }
                continue;
            }
        }

    }

}

