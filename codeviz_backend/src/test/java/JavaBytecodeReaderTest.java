import codeViz.GraphGenerator;
import codeViz.JavaBytecodeReader;
import codeViz.entity.*;
import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Test class for JavaBytecodeReader.
 * @author Thanuja Sivaananthan
 */
public class JavaBytecodeReaderTest {


    // FIXME - want these tests to use hardcoded files that will not change (can have more robust tests)

    private final String folderPath = "./target/classes/codeViz/entity";
    private final String name = "codeViz/entity";


    /**
     * Test that the filepaths are correct
     * @author Thanuja Sivaananthan
     */
    @Test
    public void testGetFilePathNames(){
        JavaBytecodeReader javaBytecodeReader = new JavaBytecodeReader();

        List<String> filePaths = javaBytecodeReader.getAllFilePaths(folderPath);

        for (String filepath : filePaths){
            System.out.println(filepath);
        }

        assertTrue(filePaths.contains(".\\target\\classes\\codeViz\\entity\\ClassEntity.class"));
        assertTrue(filePaths.contains(".\\target\\classes\\codeViz\\entity\\Entity.class"));
        assertTrue(filePaths.contains(".\\target\\classes\\codeViz\\entity\\EntityType.class"));
        assertTrue(filePaths.contains(".\\target\\classes\\codeViz\\entity\\MethodEntity.class"));
        assertTrue(filePaths.contains(".\\target\\classes\\codeViz\\entity\\PackageEntity.class"));

    }

    /**
     * Test that the package entities are correct
     * @author Thanuja Sivaananthan
     */
    @Test
    public void testGeneratePackages(){
        JavaBytecodeReader javaBytecodeReader = new JavaBytecodeReader();

        List<String> filePaths = javaBytecodeReader.getAllFilePaths(folderPath);
        javaBytecodeReader.generateEntitiesAndConnections(filePaths);

        GraphGenerator graphGenerator = javaBytecodeReader.getGraphGenerator();
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
        JavaBytecodeReader javaBytecodeReader = new JavaBytecodeReader();

        List<String> filePaths = javaBytecodeReader.getAllFilePaths(folderPath);
        javaBytecodeReader.generateEntitiesAndConnections(filePaths);

        GraphGenerator graphGenerator = javaBytecodeReader.getGraphGenerator();
        LinkedHashMap<String, Entity> classEntities = graphGenerator.getClassEntities();

        Set<String> keys = classEntities.keySet();

        for (String key : keys){
            Entity entity = classEntities.get(key);
            System.out.println(key + ": " +  entity.getName());
        }

        assertTrue(classEntities.containsKey("codeViz.entity.ClassEntity"));
        assertTrue(classEntities.containsKey("codeViz.entity.Entity"));
        assertTrue(classEntities.containsKey("codeViz.entity.EntityType"));
        assertTrue(classEntities.containsKey("codeViz.entity.MethodEntity"));
        assertTrue(classEntities.containsKey("codeViz.entity.PackageEntity"));
    }

    /**
     * Test that the method entities are correct
     * @author Thanuja Sivaananthan
     */
    @Test
    public void testGenerateMethods(){
        JavaBytecodeReader javaBytecodeReader = new JavaBytecodeReader();

        List<String> filePaths = javaBytecodeReader.getAllFilePaths(folderPath);
        javaBytecodeReader.generateEntitiesAndConnections(filePaths);

        GraphGenerator graphGenerator = javaBytecodeReader.getGraphGenerator();
        LinkedHashMap<String, Entity> methodEntities = graphGenerator.getMethodEntities();

        Set<String> keys = methodEntities.keySet();

        for (String key : keys){
            Entity entity = methodEntities.get(key);
            System.out.println(key + ": " +  entity.getName());
        }

        assertTrue(methodEntities.containsKey("codeViz.entity.ClassEntity.addConnectedEntity"));
        assertTrue(methodEntities.containsKey("codeViz.entity.Entity.addConnectedEntity"));
        assertTrue(methodEntities.containsKey("codeViz.entity.Entity.getEntityType"));
//        assertTrue(methodEntities.containsKey("codeViz.entity.EntityType"));
        assertTrue(methodEntities.containsKey("codeViz.entity.MethodEntity.getClassEntity"));
        assertTrue(methodEntities.containsKey("codeViz.entity.MethodEntity.addConnectedEntity"));
        assertTrue(methodEntities.containsKey("codeViz.entity.PackageEntity.addConnectedEntity"));

        // NOTE: <init> and <clinit> are used for the constructors, manually changed those
        // https://www.baeldung.com/jvm-init-clinit-methods

        assertTrue(methodEntities.containsKey("codeViz.entity.ClassEntity.init"));
        assertTrue(methodEntities.containsKey("codeViz.entity.Entity.init"));
        assertTrue(methodEntities.containsKey("codeViz.entity.EntityType.clinit"));
        assertTrue(methodEntities.containsKey("codeViz.entity.MethodEntity.init"));
        assertTrue(methodEntities.containsKey("codeViz.entity.MethodEntity.init"));
        assertTrue(methodEntities.containsKey("codeViz.entity.PackageEntity.init"));
    }

    /**
     * Test the package class connections
     * @author Thanuja Sivaananthan
     */
    @Test
    public void testGenerateLevelPackageClassConnections(){
        JavaBytecodeReader javaBytecodeReader = new JavaBytecodeReader();

        List<String> filePaths = javaBytecodeReader.getAllFilePaths(folderPath);
        javaBytecodeReader.generateEntitiesAndConnections(filePaths);

        GraphGenerator graphGenerator = javaBytecodeReader.getGraphGenerator();

        LinkedHashMap<String, Entity> packageEntities = graphGenerator.getPackageEntities();
        LinkedHashMap<String, Entity> classEntities = graphGenerator.getClassEntities();

        // check package and class connections

        PackageEntity entityPackage = (PackageEntity) packageEntities.get("codeViz.entity");

        ClassEntity entityClass = (ClassEntity) classEntities.get("codeViz.entity.Entity");
        ClassEntity packageEntityClass = (ClassEntity) classEntities.get("codeViz.entity.PackageEntity");
        ClassEntity classEntityClass = (ClassEntity) classEntities.get("codeViz.entity.ClassEntity");
        ClassEntity methodEntityClass = (ClassEntity) classEntities.get("codeViz.entity.MethodEntity");

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
        JavaBytecodeReader javaBytecodeReader = new JavaBytecodeReader();

        List<String> filePaths = javaBytecodeReader.getAllFilePaths(folderPath);
        javaBytecodeReader.generateEntitiesAndConnections(filePaths);

        GraphGenerator graphGenerator = javaBytecodeReader.getGraphGenerator();

        LinkedHashMap<String, Entity> classEntities = graphGenerator.getClassEntities();
        LinkedHashMap<String, Entity> methodEntities = graphGenerator.getMethodEntities();

        // check class and method connections

        ClassEntity entityClass = (ClassEntity) classEntities.get("codeViz.entity.Entity");
        ClassEntity packageEntityClass = (ClassEntity) classEntities.get("codeViz.entity.PackageEntity");
        ClassEntity classEntityClass = (ClassEntity) classEntities.get("codeViz.entity.ClassEntity");
        ClassEntity methodEntityClass = (ClassEntity) classEntities.get("codeViz.entity.MethodEntity");

        MethodEntity entityAddConnectedEntityMethod = (MethodEntity) methodEntities.get("codeViz.entity.Entity.addConnectedEntity");
        MethodEntity packageAddConnectedEntityMethod = (MethodEntity) methodEntities.get("codeViz.entity.PackageEntity.addConnectedEntity");
        MethodEntity classAddConnectedEntityMethod = (MethodEntity) methodEntities.get("codeViz.entity.ClassEntity.addConnectedEntity");
        MethodEntity methodGetClassEntityMethod = (MethodEntity) methodEntities.get("codeViz.entity.MethodEntity.getClassEntity");
        MethodEntity entityGetEntityTypeMethod = (MethodEntity) methodEntities.get("codeViz.entity.Entity.getEntityType");

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
     * Test generation of gexf files
     * @author Thanuja Sivaananthan
     */
    @Test
    public void testGenerateGexfGraphs(){
        JavaBytecodeReader javaBytecodeReader = new JavaBytecodeReader();

        List<String> filePaths = javaBytecodeReader.getAllFilePaths(folderPath);
        javaBytecodeReader.generateEntitiesAndConnections(filePaths);

        javaBytecodeReader.generateGraph(EntityType.PACKAGE, "./src/test/gexf/" + name + "/package.gexf");
        javaBytecodeReader.generateGraph(EntityType.CLASS, "./src/test/gexf/" + name + "/class.gexf");
        javaBytecodeReader.generateGraph(EntityType.METHOD, "./src/test/gexf/" + name + "/method.gexf");

//        String folderPath2 = "./target/classes/codeViz";
//        String name2 = "codeViz";
//
//        filePaths = javaBytecodeReader.getAllFilePaths(folderPath2);
//        javaBytecodeReader.generateEntitiesAndConnections(filePaths);
//
//        javaBytecodeReader.generateGraph(EntityType.PACKAGE, "./src/test/gexf/" + name2 + "/package.gexf");
//        javaBytecodeReader.generateGraph(EntityType.CLASS, "./src/test/gexf/" + name2 + "/class.gexf");
//        javaBytecodeReader.generateGraph(EntityType.METHOD, "./src/test/gexf/" + name2 + "/method.gexf");
    }

    /**
     * Test superclass connections
     * @author Thanuja Sivaananthan
     */
    @Test
    public void testGenerateSuperclassEdges(){
        JavaBytecodeReader javaBytecodeReader = new JavaBytecodeReader();

        List<String> filePaths = javaBytecodeReader.getAllFilePaths(folderPath);
        javaBytecodeReader.generateEntitiesAndConnections(filePaths);

        GraphGenerator graphGenerator = javaBytecodeReader.getGraphGenerator();
        LinkedHashMap<String, Entity> classEntities = graphGenerator.getClassEntities();

        Set<String> keys = classEntities.keySet();

        for (String key : keys){
            Entity entity = classEntities.get(key);
            System.out.println(key + ": " +  entity.getName());
        }

        Entity packageEntityClass = classEntities.get("codeViz.entity.PackageEntity");
        Entity classEntityClass = classEntities.get("codeViz.entity.ClassEntity");
        Entity methodEntityClass = classEntities.get("codeViz.entity.MethodEntity");
        Entity entityClass = classEntities.get("codeViz.entity.Entity");

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
        JavaBytecodeReader javaBytecodeReader = new JavaBytecodeReader();

        List<String> filePaths = javaBytecodeReader.getAllFilePaths(folderPath);
        javaBytecodeReader.generateEntitiesAndConnections(filePaths);

        GraphGenerator graphGenerator = javaBytecodeReader.getGraphGenerator();
        LinkedHashMap<String, Entity> classEntities = graphGenerator.getClassEntities();

        Set<String> keys = classEntities.keySet();

        for (String key : keys){
            Entity entity = classEntities.get(key);
            System.out.println(key + ": " +  entity.getName());
        }

        Entity packageEntityClass = classEntities.get("codeViz.entity.PackageEntity");
        Entity classEntityClass = classEntities.get("codeViz.entity.ClassEntity");
        Entity methodEntityClass = classEntities.get("codeViz.entity.MethodEntity");
        Entity entityTypeClass = classEntities.get("codeViz.entity.EntityType");
        Entity entityClass = classEntities.get("codeViz.entity.Entity");

        assertTrue(entityClass.getConnectedEntities().contains(entityTypeClass));

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
        JavaBytecodeReader javaBytecodeReader = new JavaBytecodeReader();

        List<String> filePaths = javaBytecodeReader.getAllFilePaths(folderPath);
        javaBytecodeReader.generateEntitiesAndConnections(filePaths);

        GraphGenerator graphGenerator = javaBytecodeReader.getGraphGenerator();
        LinkedHashMap<String, Entity> methodEntities = graphGenerator.getMethodEntities();

        Entity entityAddConnectedEntity = methodEntities.get("codeViz.entity.Entity.addConnectedEntity");
        Entity packageAddConnectedEntity = methodEntities.get("codeViz.entity.PackageEntity.addConnectedEntity");
        Entity classAddConnectedEntity = methodEntities.get("codeViz.entity.ClassEntity.addConnectedEntity");
        Entity methodAddConnectedEntity = methodEntities.get("codeViz.entity.MethodEntity.addConnectedEntity");

        // test that subclass methods call superclass method
        assertTrue(packageAddConnectedEntity.getConnectedEntities().contains(entityAddConnectedEntity));
        assertTrue(classAddConnectedEntity.getConnectedEntities().contains(entityAddConnectedEntity));
        assertTrue(methodAddConnectedEntity.getConnectedEntities().contains(entityAddConnectedEntity));

        Entity entityInit = methodEntities.get("codeViz.entity.Entity.init");
        Entity packageInit = methodEntities.get("codeViz.entity.PackageEntity.init");
        Entity classInit = methodEntities.get("codeViz.entity.ClassEntity.init"); // FIXME - multiple constructors
        Entity methodInit = methodEntities.get("codeViz.entity.MethodEntity.init");

        Entity packageAddClass = methodEntities.get("codeViz.entity.PackageEntity.addClass");
        Entity classAddMethod = methodEntities.get("codeViz.entity.ClassEntity.addMethod");

        // test that subclass methods call superclass method
        assertTrue(packageInit.getConnectedEntities().contains(entityInit));
        assertTrue(classInit.getConnectedEntities().contains(entityInit));
        assertTrue(methodInit.getConnectedEntities().contains(entityInit));

        assertTrue(classInit.getConnectedEntities().contains(packageAddClass));
        assertTrue(methodInit.getConnectedEntities().contains(classAddMethod));

        // FIXME - MethodEntity.getMethod should be connected to Entity.getName or a MethodEntity.getName
    }


    /**
     * Test search of a class name
     * @author Thanuja Sivaananthan
     */
    @Test
    public void testSearchClassColours() {
        JavaBytecodeReader javaBytecodeReader = new JavaBytecodeReader();

        List<String> filePaths = javaBytecodeReader.getAllFilePaths(folderPath);
        javaBytecodeReader.generateEntitiesAndConnections(filePaths);
        String prefix = "MethodEntity";
        javaBytecodeReader.getGraphGenerator().performSearch(prefix, false);

        GraphGenerator graphGenerator = javaBytecodeReader.getGraphGenerator();
        LinkedHashMap<String, Entity> packageEntities = graphGenerator.getPackageEntities();
        LinkedHashMap<String, Entity> classEntities = graphGenerator.getClassEntities();
        LinkedHashMap<String, Entity> methodEntities = graphGenerator.getMethodEntities();

        assertNotEquals(Entity.getHighlighedColour(), packageEntities.get("codeViz.entity").getColour());
        assertEquals(Entity.getHighlighedColour(), classEntities.get("codeViz.entity.MethodEntity").getColour());
        assertEquals(Entity.getHighlighedColour(), methodEntities.get("codeViz.entity.MethodEntity.init").getColour());

        javaBytecodeReader.generateGraph(EntityType.PACKAGE, "./src/test/gexf/" + name + "/search_" + prefix + "_package.gexf");
        javaBytecodeReader.generateGraph(EntityType.CLASS, "./src/test/gexf/" + name + "/search_" + prefix + "_class.gexf");
        javaBytecodeReader.generateGraph(EntityType.METHOD, "./src/test/gexf/" + name + "/search_" + prefix + "_method.gexf");
    }

    /**
     * Test search of a class name
     * @author Thanuja Sivaananthan
     */
    @Test
    public void testSearchMethodColours() {
        JavaBytecodeReader javaBytecodeReader = new JavaBytecodeReader();

        List<String> filePaths = javaBytecodeReader.getAllFilePaths(folderPath);
        javaBytecodeReader.generateEntitiesAndConnections(filePaths);
        String prefix = "addConnectedEntity";
        javaBytecodeReader.getGraphGenerator().performSearch(prefix, false);

        GraphGenerator graphGenerator = javaBytecodeReader.getGraphGenerator();
        LinkedHashMap<String, Entity> methodEntities = graphGenerator.getMethodEntities();

        assertEquals(Entity.getHighlighedColour(), methodEntities.get("codeViz.entity.Entity.addConnectedEntity").getColour());
        assertEquals(Entity.getHighlighedColour(), methodEntities.get("codeViz.entity.PackageEntity.addConnectedEntity").getColour());
        assertEquals(Entity.getHighlighedColour(), methodEntities.get("codeViz.entity.ClassEntity.addConnectedEntity").getColour());
        assertEquals(Entity.getHighlighedColour(), methodEntities.get("codeViz.entity.MethodEntity.addConnectedEntity").getColour());

        javaBytecodeReader.generateGraph(EntityType.METHOD, "./src/test/gexf/" + name + "/search_" + prefix + "_method.gexf");
    }
}
