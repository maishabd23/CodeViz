import codeViz.GraphGenerator;
import codeViz.entity.ClassEntity;
import codeViz.entity.EntityType;
import codeViz.entity.MethodEntity;
import codeViz.entity.PackageEntity;
import org.gephi.graph.api.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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

    private final boolean GENERATE_GEXF = false;

    @BeforeEach
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

        DirectedGraph directedGraph = graphGenerator.entitiesToNodes(EntityType.CLASS, false);

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

        if (GENERATE_GEXF) {
            graphGenerator.directedGraphToGexf(directedGraph, "./src/test/gexf/class_gephi.gexf");
        }

    }

    /**
     * Test simple searches
     * @author Thanuja Sivaananthan
     */
    @Test
    public void testSimpleSearch(){
        boolean isDetailed = false;

        // searching a class name will only highlight that class - not it's methods
        graphGenerator.performSearch("Person", isDetailed, isDetailed, isDetailed, isDetailed, isDetailed, isDetailed, EntityType.CLASS);
        assertTrue(person.isHighlighted());
        assertFalse(student.isHighlighted());
        assertFalse(professor.isHighlighted());
        assertFalse(address.isHighlighted());
        assertFalse(personInit.isHighlighted());
        assertFalse(getName.isHighlighted());
        assertFalse(getAge.isHighlighted());

        // searching a method name will only highlight the methods - not the classes they are in
        graphGenerator.performSearch("getName", isDetailed, isDetailed, isDetailed, isDetailed, isDetailed, isDetailed, EntityType.METHOD);
        assertFalse(person.isHighlighted());
        assertTrue(getName.isHighlighted());

        graphGenerator.performSearch("toString", isDetailed, isDetailed, isDetailed, isDetailed, isDetailed, isDetailed, EntityType.METHOD);
        assertFalse(person.isHighlighted());
        assertFalse(student.isHighlighted());
        assertFalse(professor.isHighlighted());
        assertFalse(address.isHighlighted());

        assertTrue(personToString.isHighlighted());
        assertTrue(studentToString.isHighlighted());
        assertTrue(professorToString.isHighlighted());
        assertTrue(addressToString.isHighlighted());

    }

    /**
     * Test detailed searches
     * @author Thanuja Sivaananthan
     */
    @Test
    public void testDetailedSearch(){
        boolean isDetailed = true;

        // searching a class name will highlight the other classes that reference it
        graphGenerator.performSearch("Person", isDetailed, isDetailed, isDetailed, isDetailed, isDetailed, isDetailed, EntityType.CLASS);
        assertTrue(person.isHighlighted());
        assertTrue(student.isHighlighted());
        assertTrue(professor.isHighlighted());
        assertFalse(address.isHighlighted());
        //assertTrue(personInit.isHighlighed()); FIXME used to be highlighted with old search

        graphGenerator.performSearch("Address", isDetailed, isDetailed, isDetailed, isDetailed, isDetailed, isDetailed, EntityType.CLASS);
        assertTrue(person.isHighlighted());
        assertFalse(student.isHighlighted());
        assertFalse(professor.isHighlighted());
        assertTrue(address.isHighlighted());

        // searching a method name will highlight the methods and the classes they're in
        graphGenerator.performSearch("getName", isDetailed, isDetailed, isDetailed, isDetailed, isDetailed, isDetailed, EntityType.METHOD);
        assertTrue(person.isHighlighted());
        assertTrue(getName.isHighlighted());

        graphGenerator.performSearch("toString", isDetailed, isDetailed, isDetailed, isDetailed, isDetailed, isDetailed, EntityType.METHOD);
        assertTrue(person.isHighlighted());
        assertTrue(student.isHighlighted());
        assertTrue(professor.isHighlighted());
        assertTrue(address.isHighlighted());

        assertTrue(personToString.isHighlighted());
        assertTrue(studentToString.isHighlighted());
        assertTrue(professorToString.isHighlighted());
        assertTrue(addressToString.isHighlighted());
    }

    @Test
    public void testGetNodeDetails(){

        // incorrect format
        assertEquals("", graphGenerator.getNodeDetails("invalid", EntityType.PACKAGE));

        // key doesn't exist
        assertEquals("", graphGenerator.getNodeDetails("1_invalid", EntityType.PACKAGE));
        assertEquals("", graphGenerator.getNodeDetails("1_student", EntityType.PACKAGE));

        String personDetails = graphGenerator.getNodeDetails("1_person", EntityType.PACKAGE);
        System.out.println(personDetails);
        assertTrue(personDetails.contains("Package:"));
        assertTrue(personDetails.contains("Person"));
        assertTrue(personDetails.contains("Classes:"));
        assertTrue(personDetails.contains("Student"));
        assertTrue(personDetails.contains("Professor"));

        // more tests for other levels...
    }

}
