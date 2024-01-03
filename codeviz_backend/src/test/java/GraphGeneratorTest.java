import codeViz.GraphGenerator;
import codeViz.entity.ClassEntity;
import codeViz.entity.EntityType;
import codeViz.entity.MethodEntity;
import codeViz.entity.PackageEntity;
import org.gephi.graph.api.*;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test class for EntityGraphGeneratorTest.
 * @author Thanuja Sivaananthan
 */
public class GraphGeneratorTest {


    private PackageEntity personPackage, attributesPackage;
    private ClassEntity person, student, professor, address;

    private MethodEntity personInit, getName, getAge, personToString,
            studentInit,  studentToString,
            professorInit, professorToString,
            addressInit, addressToString;

    private GraphGenerator graphGenerator;


    @Before
    public void setupGraphGenerator() {
        // loosely following these uml/java examples:
        // https://agilemodeling.com/wp-content/uploads/2023/07/classDiagramInheritance.jpg
        // https://javablog2013.files.wordpress.com/2013/11/java-lab-5.pdf

        graphGenerator = new GraphGenerator();

        // create packages
        personPackage = new PackageEntity("person");
        attributesPackage = new PackageEntity("attributes");
        attributesPackage.addConnectedEntity(personPackage);
        graphGenerator.addEntity(personPackage.getName(), personPackage);
        graphGenerator.addEntity(attributesPackage.getName(), attributesPackage);

        // create classes
        person = new ClassEntity("Person", personPackage);
        student = new ClassEntity("Student", personPackage);
        professor = new ClassEntity("Professor", personPackage);
        address = new ClassEntity("Address", attributesPackage);

        // create class connections
        student.addConnectedEntity(person);
        professor.addConnectedEntity(person);
        person.addConnectedEntity(address);

        // create methods
        personInit = new MethodEntity("init", person);
        getName = new MethodEntity("getName", person);
        getAge = new MethodEntity("getAge", person);
        personToString = new MethodEntity("toString", person);

        studentInit = new MethodEntity("init", student);
        studentToString = new MethodEntity("toString", student);

        professorInit = new MethodEntity("init", professor);
        professorToString = new MethodEntity("toString", professor);

        addressInit = new MethodEntity("init", address);
        addressToString = new MethodEntity("toString", address);

        // create method connections

        // connections for overriding
        studentInit.addConnectedEntity(personInit);
        professorInit.addConnectedEntity(personInit);
        studentToString.addConnectedEntity(personToString);
        professorToString.addConnectedEntity(personToString);
        personToString.addConnectedEntity(addressToString);

        MethodEntity[] methodList = {personInit, getName, getAge, personToString,
                studentInit,  studentToString,
                professorInit, professorToString,
                addressInit, addressToString};

        graphGenerator.addEntity(person.getName(), person);
        graphGenerator.addEntity(student.getName(), student);
        graphGenerator.addEntity(professor.getName(), professor);
        graphGenerator.addEntity(address.getName(), address);

        for (MethodEntity method : methodList){
            graphGenerator.addEntity(method.getClassEntity().getName() + "." + method.getName(), method);
        }
    }

    /**
     * Test a basic graph manually created
     * @author Thanuja Sivaananthan
     */
    @Test
    public void testDirectedGraph(){

        DirectedGraph directedGraph = graphGenerator.entitiesToNodes(EntityType.CLASS);

        assertEquals(4, directedGraph.getNodeCount());
        assertEquals(3, directedGraph.getEdgeCount());

        Node[] nodes = directedGraph.getNodes().toArray();
        Edge[] edges = directedGraph.getEdges().toArray();

        assertEquals(4, nodes.length);
        assertEquals(3, edges.length);

        assertEquals("Person", nodes[0].getLabel());
        assertEquals("Student", nodes[1].getLabel());
        assertEquals("Professor", nodes[2].getLabel());
        assertEquals("Address", nodes[3].getLabel());

        assertEquals("Person", edges[0].getSource().getLabel());
        assertEquals("Address", edges[0].getTarget().getLabel());
        assertEquals("Student", edges[1].getSource().getLabel());
        assertEquals("Person", edges[1].getTarget().getLabel());
        assertEquals("Professor", edges[2].getSource().getLabel());
        assertEquals("Person", edges[2].getTarget().getLabel());

        graphGenerator.directedGraphToGexf(directedGraph, "./src/test/gexf/class_gephi.gexf");

    }

    /**
     * Test simple searches
     * @author Thanuja Sivaananthan
     */
    @Test
    public void testSimpleSearch(){
        boolean isDetailed = false;

        // searching a class name will only highlight that class
        graphGenerator.performSearch("Person", isDetailed);
        assertTrue(person.isHighlighed());
        assertFalse(student.isHighlighed());
        assertFalse(professor.isHighlighed());
        assertFalse(address.isHighlighed());

        // searching a method name will only highlight the methods - not the classes they are in
        graphGenerator.performSearch("getName", isDetailed);
        assertFalse(person.isHighlighed());
        assertTrue(getName.isHighlighed());

        graphGenerator.performSearch("toString", isDetailed);
        assertFalse(person.isHighlighed());
        assertFalse(student.isHighlighed());
        assertFalse(professor.isHighlighed());
        assertFalse(address.isHighlighed());

        assertTrue(personToString.isHighlighed());
        assertTrue(studentToString.isHighlighed());
        assertTrue(professorToString.isHighlighed());
        assertTrue(addressToString.isHighlighed());

    }

    /**
     * Test detailed searches
     * @author Thanuja Sivaananthan
     */
    @Test
    public void testDetailedSearch(){
        boolean isDetailed = true;

        // searching a class name will highlight the other classes that reference it
        graphGenerator.performSearch("Person", isDetailed);
        assertTrue(person.isHighlighed());
        assertTrue(student.isHighlighed());
        assertTrue(professor.isHighlighed());
        assertFalse(address.isHighlighed());

        graphGenerator.performSearch("Address", isDetailed);
        assertTrue(person.isHighlighed());
        assertFalse(student.isHighlighed());
        assertFalse(professor.isHighlighed());
        assertTrue(address.isHighlighed());

        // searching a method name will highlight the methods and the classes they're in
        graphGenerator.performSearch("getName", isDetailed);
        assertTrue(person.isHighlighed());
        assertTrue(getName.isHighlighed());

        graphGenerator.performSearch("toString", isDetailed);
        assertTrue(person.isHighlighed());
        assertTrue(student.isHighlighed());
        assertTrue(professor.isHighlighed());
        assertTrue(address.isHighlighed());

        assertTrue(personToString.isHighlighed());
        assertTrue(studentToString.isHighlighed());
        assertTrue(professorToString.isHighlighed());
        assertTrue(addressToString.isHighlighed());
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
