package main.java.libtw;

import main.java.graph.Graph;
import main.java.graph.Node;
import nl.uu.cs.treewidth.input.GraphInput;
import nl.uu.cs.treewidth.input.InputException;
import nl.uu.cs.treewidth.ngraph.ListGraph;
import nl.uu.cs.treewidth.ngraph.ListVertex;
import nl.uu.cs.treewidth.ngraph.NGraph;
import nl.uu.cs.treewidth.ngraph.NVertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by Verena on 09.03.2017.
 */
public class LPGraphInput implements GraphInput {

    private static final Logger LOGGER = LoggerFactory.getLogger(LPGraphInput.class);

    private Graph graph;

    public LPGraphInput(Graph graph) {
        this.graph = graph;
    }

    @Override
    public NGraph<InputData> get() throws InputException {
        ListGraph<InputData> resultGraph = new ListGraph<>();

        Hashtable<String, NVertex<InputData>> vertices = createVertices(resultGraph);
        createEdges(resultGraph, vertices);
        createComponents(resultGraph);

        return resultGraph;
    }

    private void createComponents(ListGraph<InputData> resultGraph) {
        ArrayList<NGraph<InputData>> components = new ArrayList<>();

        int verticesFound = 0;
        while (verticesFound != graph.getNodes().size()) {
            for (NVertex<InputData> vertex : resultGraph) {
                if (!vertexInSomeComponent(components, vertex)) {
                    ArrayList<NVertex<InputData>> verticesOfNewComponent = getVerticesOfNewComponent(vertex);
                    verticesFound += verticesOfNewComponent.size();
                    NGraph<InputData> componentGraph = createComponentGraph(verticesOfNewComponent);
                    components.add(componentGraph);
                }
            }
        }
        resultGraph.setComponents(components);
    }

    private NGraph<InputData> createComponentGraph(ArrayList<NVertex<InputData>> verticesOfNewComponent) {
        NGraph<InputData> gSub = new ListGraph<>();
        ((ListGraph) gSub).vertices = verticesOfNewComponent;
        return gSub;
    }

    private ArrayList<NVertex<InputData>> getVerticesOfNewComponent(NVertex<InputData> vertex) {
        ArrayList<NVertex<InputData>> handledVertices = new ArrayList<>();
        DFSTree(vertex, handledVertices);
        return handledVertices;
    }

    private boolean vertexInSomeComponent(List<NGraph<InputData>> components, NVertex<InputData> vertex) {
        for (NGraph<InputData> subGraph: components) {
            for (NVertex nVertex : subGraph) {
                if (nVertex.equals(vertex)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void createEdges(ListGraph<InputData> resultGraph, Hashtable<String, NVertex<InputData>> vertices) {

        for (Map.Entry<String, List<Node>> nodeNeighboursPair : graph.getNeighbourNodes().entrySet()) {

            NVertex<InputData> v1, v2;
            String curNodeName = nodeNeighboursPair.getKey();

            v1 = vertices.get(curNodeName);

            for (Node neighbour : nodeNeighboursPair.getValue()) {
                v2 = vertices.get(neighbour.getName());

                boolean edgeExists = v1.isNeighbor(v2);
                if (edgeExists && !v2.isNeighbor(v1)) {
                    LOGGER.error("Directed edge found for node " + v1.data.name);
                }

                if (!edgeExists) {
                    // add (undirected) edge, i.e. add v1 as neighbour of v2 and the other way around
                    resultGraph.addEdge(v1, v2);
                }
            }
        }
    }

    private Hashtable<String, NVertex<InputData>> createVertices(NGraph<InputData> resultGraph) {
        Hashtable<String, NVertex<InputData>> vertices = new Hashtable<>();
        NVertex<LPInputData> vertexPrototype = new ListVertex<>();

        // create vertices for NGraph
        for (Node node : graph.getNodes()) {
            if( !vertices.containsKey(node.getName()) ) {
                //If there vertex isn't created yet, create it where InputData as additional data for a vertex (id, name)
                NVertex<InputData> v = vertexPrototype.newOfSameType(new LPInputData(node.getId(), node.getName(), node.isInteger()));
                vertices.put(node.getName(), v);
                resultGraph.addVertex(v);
            }
        }
        return vertices;
    }

    /*
     * Input is the root node, returns distance from the lowest descendant of the rootNode, i.e. the current
     * height of the tree
     */
    private static int DFSTree(NVertex rootNode, List<NVertex<InputData>> handledVertices) {

        handledVertices.add(rootNode);
        // System.out.print(rootNode.data.name + " ");

        int height = 0;
        for (Iterator<NVertex> it = rootNode.getNeighbors(); it.hasNext(); ) {
            NVertex neighbor = it.next();
            if (!handledVertices.contains(neighbor)) {
                int heightSubtree = DFSTree(neighbor, handledVertices);
                if (heightSubtree > height) {
                    height = heightSubtree;
                }
            }
        }
        return ++height;
    }
}
