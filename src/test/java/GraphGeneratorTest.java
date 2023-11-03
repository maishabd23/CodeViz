import codeViz.GraphGenerator;
import codeViz.entity.ClassEntity;
import codeViz.entity.EntityType;
import org.gephi.graph.api.*;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Test class for EntityGraphGeneratorTest.
 * @author Thanuja Sivaananthan
 */
public class GraphGeneratorTest {

    /**
     * Test a basic graph manually created
     * @author Thanuja Sivaananthan
     */
    @Test
    public void testDirectedGraph(){

        ClassEntity person = new ClassEntity("person");
        ClassEntity student = new ClassEntity("student");
        ClassEntity professor = new ClassEntity("professor");
        ClassEntity address = new ClassEntity("address");

        student.addConnectedEntity(person);
        professor.addConnectedEntity(person);
        person.addConnectedEntity(address);

        GraphGenerator graphGenerator = new GraphGenerator();
        graphGenerator.addEntity(person.getName(), person);
        graphGenerator.addEntity(student.getName(), student);
        graphGenerator.addEntity(professor.getName(), professor);
        graphGenerator.addEntity(address.getName(), address);

        DirectedGraph directedGraph = graphGenerator.entitiesToNodes(EntityType.CLASS);

        assertEquals(4, directedGraph.getNodeCount());
        assertEquals(3, directedGraph.getEdgeCount());

        Node[] nodes = directedGraph.getNodes().toArray();
        Edge[] edges = directedGraph.getEdges().toArray();

        assertEquals(4, nodes.length);
        assertEquals(3, edges.length);

        assertEquals("person", nodes[0].getLabel());
        assertEquals("student", nodes[1].getLabel());
        assertEquals("professor", nodes[2].getLabel());
        assertEquals("address", nodes[3].getLabel());

        assertEquals("person", edges[0].getSource().getLabel());
        assertEquals("address", edges[0].getTarget().getLabel());
        assertEquals("student", edges[1].getSource().getLabel());
        assertEquals("person", edges[1].getTarget().getLabel());
        assertEquals("professor", edges[2].getSource().getLabel());
        assertEquals("person", edges[2].getTarget().getLabel());

        graphGenerator.directedGraphToGexf(directedGraph, "./src/test/gexf/class_gephi.gexf");

    }

    /**
     * Test a basic graph with gexf4j
     * @author Thanuja Sivaananthan
     */
    @Test
    public void testSampleGexf(){

        ClassEntity person = new ClassEntity("person");
        ClassEntity student = new ClassEntity("student");
        ClassEntity professor = new ClassEntity("professor");
        ClassEntity address = new ClassEntity("address");

        student.addConnectedEntity(person);
        professor.addConnectedEntity(person);
        person.addConnectedEntity(address);

        GraphGenerator graphGenerator = new GraphGenerator();
        graphGenerator.addEntity(person.getName(), person);
        graphGenerator.addEntity(student.getName(), student);
        graphGenerator.addEntity(professor.getName(), professor);
        graphGenerator.addEntity(address.getName(), address);

        graphGenerator.entitiesToGexf(EntityType.CLASS, "./src/test/gexf/class_gexf4j.gexf");

    }
}
