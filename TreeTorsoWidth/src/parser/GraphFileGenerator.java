package parser;

import graph.Edge;
import graph.Graph;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by Verena on 16.03.2017.
 */
public class GraphFileGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(GraphFileGenerator.class);

    public static final String CL_TERM = "0";

    // creates a graph file in DIMACS format that can be used for QuickBB - see http://www.hlt.utdallas.edu/~vgogate/quickbb.html
    public static void createDIMACSFile(String filename, Graph graph) {
        try{
            PrintWriter writer = new PrintWriter(filename + ".txt", "UTF-8");
            StringBuilder sb = new StringBuilder();
            sb.append("c " + filename + " CNF file format");
            sb.append("p cnf " + graph.getNodes().size() + " " + graph.getEdges().size());
            for (Edge edge : graph.getEdges()) {
                sb.append(edge.getNode1().getId() + " " + edge.getNode2().getId() + " " + CL_TERM);
            }
            writer.println(sb.toString());
            writer.close();
        } catch (IOException e) {
            LOGGER.error("", e);
        }
    }

}
