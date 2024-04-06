import codeViz.SourcecodeReader;
import codeViz.GraphGenerator;
import codeViz.entity.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class SourcecodeReaderTest {

    private static GraphGenerator graphGenerator;

    @BeforeAll
    public static void setupGraphGenerator() {
        SourcecodeReader sourcecodeReaderCodeViz = new SourcecodeReader();
        sourcecodeReaderCodeViz.analyzeCodebase("https://github.com/maishabd23/CodeViz");
        graphGenerator = sourcecodeReaderCodeViz.getGraphGenerator();
    }

    @Test
    public void testisValidRepoUrl(){
        // should work for main or master branches
        SourcecodeReader sourcecodeReader = new SourcecodeReader();
        assertEquals("", sourcecodeReader.isValidRepoUrl("https://github.com/thanujasiva/sysc-3110-project"));
        assertEquals("", sourcecodeReader.isValidRepoUrl("https://github.com/maishabd23/CodeViz"));

        assertTrue(sourcecodeReader.analyzeCodebase("https://github.com/maishabd23/CodeViz"));
    }

    /**
     * Test that the package entities are correct
     * @author Thanuja Sivaananthan
     */
    @Test
    public void testGeneratePackages(){
        LinkedHashMap<String, Entity> packageEntities = graphGenerator.getPackageEntities();

        Set<String> keys = packageEntities.keySet();

        for (String key : keys){
            Entity entity = packageEntities.get(key);
            System.out.println(key + ": " +  entity.getName());
        }

        assertTrue(packageEntities.containsKey("codeViz"));
        assertTrue(packageEntities.containsKey("codeViz.entity"));
    }



    /**
     * Test that the class entities are correct
     * @author Thanuja Sivaananthan
     */
    @Test
    public void testGenerateClasses(){
        LinkedHashMap<String, Entity> classEntities = graphGenerator.getClassEntities();

        Set<String> keys = classEntities.keySet();

        for (String key : keys){
            Entity entity = classEntities.get(key);
            System.out.println(key + ": " +  entity.getName());
        }

        assertTrue(classEntities.containsKey("ClassEntity"));
        assertTrue(classEntities.containsKey("Entity"));
        assertTrue(classEntities.containsKey("EntityType"));
        assertTrue(classEntities.containsKey("MethodEntity"));
        assertTrue(classEntities.containsKey("PackageEntity"));
    }

    /**
     * Test that the method entities are correct
     * @author Thanuja Sivaananthan
     */
    @Test
    public void testGenerateMethods(){
        LinkedHashMap<String, Entity> methodEntities = graphGenerator.getMethodEntities();

        Set<String> keys = methodEntities.keySet();

        for (String key : keys){
            Entity entity = methodEntities.get(key);
            System.out.println(key + ": " +  entity.getName());
        }

        assertTrue(methodEntities.containsKey("ClassEntity.addConnectedEntity"));
        assertTrue(methodEntities.containsKey("Entity.addConnectedEntity"));
        assertTrue(methodEntities.containsKey("Entity.getEntityType"));
//        assertTrue(methodEntities.containsKey("EntityType"));
        assertTrue(methodEntities.containsKey("MethodEntity.getClassEntity"));
        assertTrue(methodEntities.containsKey("MethodEntity.addConnectedEntity"));
        assertTrue(methodEntities.containsKey("PackageEntity.addConnectedEntity"));

        assertTrue(methodEntities.containsKey("ClassEntity.ClassEntity"));
        assertTrue(methodEntities.containsKey("Entity.Entity"));
        assertTrue(methodEntities.containsKey("EntityType.EntityType"));
        assertTrue(methodEntities.containsKey("MethodEntity.MethodEntity"));
        assertTrue(methodEntities.containsKey("MethodEntity.MethodEntity"));
        assertTrue(methodEntities.containsKey("PackageEntity.PackageEntity"));
    }



    /**
     * Test the package class connections
     * @author Thanuja Sivaananthan
     */
    @Test
    public void testGenerateLevelPackageClassConnections(){
        LinkedHashMap<String, Entity> packageEntities = graphGenerator.getPackageEntities();
        LinkedHashMap<String, Entity> classEntities = graphGenerator.getClassEntities();

        // check package and class connections

        PackageEntity entityPackage = (PackageEntity) packageEntities.get("codeViz.entity");

        ClassEntity entityClass = (ClassEntity) classEntities.get("Entity");
        ClassEntity packageEntityClass = (ClassEntity) classEntities.get("PackageEntity");
        ClassEntity classEntityClass = (ClassEntity) classEntities.get("ClassEntity");
        ClassEntity methodEntityClass = (ClassEntity) classEntities.get("MethodEntity");

        assertEquals(entityPackage, entityClass.getPackageEntity());
        assertEquals(entityPackage, packageEntityClass.getPackageEntity());
        assertEquals(entityPackage, classEntityClass.getPackageEntity());
        assertEquals(entityPackage, methodEntityClass.getPackageEntity());

        assertTrue(entityPackage.getClasses().contains(entityClass));
        assertTrue(entityPackage.getClasses().contains(packageEntityClass));
        assertTrue(entityPackage.getClasses().contains(classEntityClass));
        assertTrue(entityPackage.getClasses().contains(methodEntityClass));
    }

    /**
     * Test the class methods connections
     * @author Thanuja Sivaananthan
     */
    @Test
    public void testGenerateLevelClassMethodConnections(){
        LinkedHashMap<String, Entity> classEntities = graphGenerator.getClassEntities();
        LinkedHashMap<String, Entity> methodEntities = graphGenerator.getMethodEntities();

        // check class and method connections

        ClassEntity entityClass = (ClassEntity) classEntities.get("Entity");
        ClassEntity packageEntityClass = (ClassEntity) classEntities.get("PackageEntity");
        ClassEntity classEntityClass = (ClassEntity) classEntities.get("ClassEntity");
        ClassEntity methodEntityClass = (ClassEntity) classEntities.get("MethodEntity");

        MethodEntity entityAddConnectedEntityMethod = (MethodEntity) methodEntities.get("Entity.addConnectedEntity");
        MethodEntity packageAddConnectedEntityMethod = (MethodEntity) methodEntities.get("PackageEntity.addConnectedEntity");
        MethodEntity classAddConnectedEntityMethod = (MethodEntity) methodEntities.get("ClassEntity.addConnectedEntity");
        MethodEntity methodGetClassEntityMethod = (MethodEntity) methodEntities.get("MethodEntity.getClassEntity");
        MethodEntity entityGetEntityTypeMethod = (MethodEntity) methodEntities.get("Entity.getEntityType");

        assertNotNull(entityClass);
        assertNotNull(packageEntityClass);
        assertNotNull(classEntityClass);
        assertNotNull(methodEntityClass);

        assertNotNull(entityAddConnectedEntityMethod);
        assertNotNull(packageAddConnectedEntityMethod);
        assertNotNull(classAddConnectedEntityMethod);
        assertNotNull(methodGetClassEntityMethod);
        assertNotNull(entityGetEntityTypeMethod);

        assertEquals(entityClass, entityAddConnectedEntityMethod.getClassEntity());
        assertEquals(packageEntityClass, packageAddConnectedEntityMethod.getClassEntity());
        assertEquals(classEntityClass, classAddConnectedEntityMethod.getClassEntity());
        assertEquals(methodEntityClass, methodGetClassEntityMethod.getClassEntity());
        assertEquals(entityClass, entityGetEntityTypeMethod.getClassEntity());

        assertTrue(entityClass.getMethods().contains(entityAddConnectedEntityMethod));
        assertTrue(packageEntityClass.getMethods().contains(packageAddConnectedEntityMethod));
        assertTrue(classEntityClass.getMethods().contains(classAddConnectedEntityMethod));
        assertTrue(methodEntityClass.getMethods().contains(methodGetClassEntityMethod));
        assertTrue(entityClass.getMethods().contains(entityGetEntityTypeMethod));
    }


    /**
     * Test superclass connections
     * @author Thanuja Sivaananthan
     */
    @Test
    public void testGenerateSuperclassEdges(){
        LinkedHashMap<String, Entity> classEntities = graphGenerator.getClassEntities();

        Set<String> keys = classEntities.keySet();

        for (String key : keys){
            Entity entity = classEntities.get(key);
            System.out.println(key + ": " +  entity.getName());
        }

        Entity packageEntityClass = classEntities.get("PackageEntity");
        Entity classEntityClass = classEntities.get("ClassEntity");
        Entity methodEntityClass = classEntities.get("MethodEntity");
        Entity entityClass = classEntities.get("Entity");

        assertTrue(packageEntityClass.getConnectedEntities().contains(entityClass));
        assertTrue(classEntityClass.getConnectedEntities().contains(entityClass));
        assertTrue(methodEntityClass.getConnectedEntities().contains(entityClass));
    }


    /**
     * Test field connections
     * @author Thanuja Sivaananthan
     */
    @Test
    public void testGenerateFieldEdges(){
        LinkedHashMap<String, Entity> classEntities = graphGenerator.getClassEntities();

        Set<String> keys = classEntities.keySet();

        for (String key : keys){
            Entity entity = classEntities.get(key);
            System.out.println(key + ": " +  entity.getName());
        }

        Entity packageEntityClass = classEntities.get("PackageEntity");
        Entity classEntityClass = classEntities.get("ClassEntity");
        Entity methodEntityClass = classEntities.get("MethodEntity");
        Entity entityTypeClass = classEntities.get("EntityType");
        Entity entityClass = classEntities.get("Entity");

        //FIXME assertTrue(entityClass.getConnectedEntities().contains(entityTypeClass));

        assertTrue(classEntityClass.getConnectedEntities().contains(packageEntityClass));
        assertTrue(classEntityClass.getConnectedEntities().contains(methodEntityClass)); // is a type Set, but also in a method's arguments

        assertTrue(packageEntityClass.getConnectedEntities().contains(classEntityClass)); // is a type Set, but also in a method's arguments
        assertTrue(methodEntityClass.getConnectedEntities().contains(classEntityClass));
    }

    /**
     * Test method connections
     * @author Thanuja Sivaananthan
     */
    @Test
    public void testGenerateMethodEdges() {
        LinkedHashMap<String, Entity> methodEntities = graphGenerator.getMethodEntities();

        Entity entityAddConnectedEntity = methodEntities.get("Entity.addConnectedEntity");
        Entity packageAddConnectedEntity = methodEntities.get("PackageEntity.addConnectedEntity");
        Entity classAddConnectedEntity = methodEntities.get("ClassEntity.addConnectedEntity");
        Entity methodAddConnectedEntity = methodEntities.get("MethodEntity.addConnectedEntity");

        // test that subclass methods call superclass method
        assertTrue(packageAddConnectedEntity.getConnectedEntities().contains(entityAddConnectedEntity));
        assertTrue(classAddConnectedEntity.getConnectedEntities().contains(entityAddConnectedEntity));
        assertTrue(methodAddConnectedEntity.getConnectedEntities().contains(entityAddConnectedEntity));

        Entity entityInit = methodEntities.get("Entity.Entity");
        Entity packageInit = methodEntities.get("PackageEntity.PackageEntity");
        Entity classInit = methodEntities.get("ClassEntity.ClassEntity"); // FIXME - multiple constructors
        Entity methodInit = methodEntities.get("MethodEntity.MethodEntity");

        Entity packageAddClass = methodEntities.get("PackageEntity.addClass");
        Entity classAddMethod = methodEntities.get("ClassEntity.addMethod");

//        // test that subclass methods call superclass method FIXME
//        assertTrue(packageInit.getConnectedEntities().contains(entityInit));
//        assertTrue(classInit.getConnectedEntities().contains(entityInit));
//        assertTrue(methodInit.getConnectedEntities().contains(entityInit));

        assertTrue(classInit.getConnectedEntities().contains(packageAddClass));
        assertTrue(methodInit.getConnectedEntities().contains(classAddMethod));

        // superclass method connection - ClassEntity.getMethod should be connected to Entity.getName
        Entity getMethod = methodEntities.get("ClassEntity.getMethod");
        Entity getName = methodEntities.get("Entity.getName");
        //FIXME assertTrue(getMethod.getConnectedEntities().contains(getName));
    }



    /**
     * Test search of a class name
     * @author Thanuja Sivaananthan
     */
    @Test
    public void testSearchClassColours() {

        String prefix = "Method";
        boolean isDetailed = false;
        graphGenerator.performSearch(prefix, isDetailed, isDetailed, isDetailed, isDetailed, isDetailed, isDetailed, EntityType.CLASS);

        LinkedHashMap<String, Entity> packageEntities = graphGenerator.getPackageEntities();
        LinkedHashMap<String, Entity> classEntities = graphGenerator.getClassEntities();
        LinkedHashMap<String, Entity> methodEntities = graphGenerator.getMethodEntities();

        assertNotEquals(Entity.getHighlightedColour(), packageEntities.get("codeViz.entity").getParentColour());
        assertNotEquals(Entity.getHighlightedColour(), classEntities.get("PackageEntity").getParentColour());
        assertEquals(Entity.getHighlightedColour(), classEntities.get("MethodEntity").getParentColour());
        assertEquals(Entity.getHighlightedColour(), methodEntities.get("ClassEntity.addMethod").getParentColour());
        assertEquals(Entity.getHighlightedColour(), methodEntities.get("ClassEntity.getMethods").getParentColour());
        assertEquals(Entity.getHighlightedColour(), methodEntities.get("ClassEntity.getMethod").getParentColour());

        // methods in MethodEntity class don't contain "Method" in their name
        //FIXME assertNotEquals(Entity.getHighlighedColour(), methodEntities.get("MethodEntity.MethodEntity").getParentColour());
        assertNotEquals(Entity.getHighlightedColour(), methodEntities.get("MethodEntity.getClassEntity").getParentColour());
        assertNotEquals(Entity.getHighlightedColour(), methodEntities.get("MethodEntity.addConnectedEntity").getParentColour());
    }

    /**
     * Test search of a class name
     * @author Thanuja Sivaananthan
     */
    @Test
    public void testSearchMethodColours() {
        String prefix = "addConnectedEntity";
        boolean isDetailed = false;
        graphGenerator.performSearch(prefix , isDetailed, isDetailed, isDetailed, isDetailed, isDetailed, isDetailed, EntityType.CLASS);

        LinkedHashMap<String, Entity> methodEntities = graphGenerator.getMethodEntities();

        assertEquals(Entity.getHighlightedColour(), methodEntities.get("Entity.addConnectedEntity").getParentColour());
        assertEquals(Entity.getHighlightedColour(), methodEntities.get("PackageEntity.addConnectedEntity").getParentColour());
        assertEquals(Entity.getHighlightedColour(), methodEntities.get("ClassEntity.addConnectedEntity").getParentColour());
        assertEquals(Entity.getHighlightedColour(), methodEntities.get("MethodEntity.addConnectedEntity").getParentColour());
    }

    /**
     * Test search of a class name
     * @author Thanuja Sivaananthan
     */
    @Test
    public void testSearchDetailed() {
        LinkedHashMap<String, Entity> packageEntities, classEntities, methodEntities;

        boolean isDetailed = true;
        graphGenerator.performSearch("addMethod", isDetailed, isDetailed, isDetailed, isDetailed, isDetailed, isDetailed, EntityType.CLASS);
        methodEntities = graphGenerator.getMethodEntities();
        classEntities = graphGenerator.getClassEntities();
        assertNotEquals(Entity.getHighlightedColour(), classEntities.get("PackageEntity").getParentColour());
        assertEquals(Entity.getHighlightedColour(), classEntities.get("ClassEntity").getParentColour()); // class that contains addMethod
        assertEquals(Entity.getHighlightedColour(), classEntities.get("MethodEntity").getParentColour()); // class that calls addMethod
        assertEquals(Entity.getHighlightedColour(), methodEntities.get("ClassEntity.addMethod").getParentColour());
        //FIXME assertEquals(Entity.getHighlighedColour(), methodEntities.get("MethodEntity.MethodEntity").getParentColour()); // calls addMethod

        graphGenerator.performSearch("MethodEntity", isDetailed, isDetailed, isDetailed, isDetailed, isDetailed, isDetailed, EntityType.CLASS);
        packageEntities = graphGenerator.getPackageEntities();
        classEntities = graphGenerator.getClassEntities();
        assertEquals(Entity.getHighlightedColour(), packageEntities.get("codeViz.entity").getParentColour()); // package that contains the class
        assertNotEquals(Entity.getHighlightedColour(), classEntities.get("PackageEntity").getParentColour());
        assertEquals(Entity.getHighlightedColour(), classEntities.get("ClassEntity").getParentColour());
        assertEquals(Entity.getHighlightedColour(), classEntities.get("MethodEntity").getParentColour());
        assertEquals(Entity.getHighlightedColour(), methodEntities.get("ClassEntity.addMethod").getParentColour()); // MethodEntity argument
        assertEquals(Entity.getHighlightedColour(), methodEntities.get("ClassEntity.getMethod").getParentColour()); // MethodEntity return type
        //assertEquals(Entity.getHighlightedColour(), methodEntities.get("ClassEntity.getMethods").getParentColour()); // TODO - MethodEntity list return type

        // methods in MethodEntity class are highlighted for detailed search
        assertEquals(Entity.getHighlightedColour(), methodEntities.get("MethodEntity.MethodEntity").getParentColour());
        //FIXME assertEquals(Entity.getHighlightedColour(), methodEntities.get("MethodEntity.getClassEntity").getParentColour());
        assertEquals(Entity.getHighlightedColour(), methodEntities.get("MethodEntity.addConnectedEntity").getParentColour());
    }

}
