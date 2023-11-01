import org.example.GraphGenerator;
import org.example.JavaBytecodeReader;
import org.example.entity.*;
import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test class for JavaBytecodeReader.
 * @author Thanuja Sivaananthan
 */
public class JavaBytecodeReaderTest {


    // FIXME - want these tests to use hardcoded files that will not change (can have more robust tests)

    private final String folderPath = ".\\target\\classes\\org\\example\\entity";
    private final String name = "codeviz/entity";


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

        assertTrue(filePaths.contains(".\\target\\classes\\org\\example\\entity\\ClassEntity.class"));
        assertTrue(filePaths.contains(".\\target\\classes\\org\\example\\entity\\Entity.class"));
        assertTrue(filePaths.contains(".\\target\\classes\\org\\example\\entity\\EntityType.class"));
        assertTrue(filePaths.contains(".\\target\\classes\\org\\example\\entity\\MethodEntity.class"));
        assertTrue(filePaths.contains(".\\target\\classes\\org\\example\\entity\\PackageEntity.class"));

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

        assertTrue(packageEntities.containsKey("org"));
        assertTrue(packageEntities.containsKey("org.example"));
        assertTrue(packageEntities.containsKey("org.example.entity"));
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

        assertTrue(classEntities.containsKey("org.example.entity.ClassEntity"));
        assertTrue(classEntities.containsKey("org.example.entity.Entity"));
        assertTrue(classEntities.containsKey("org.example.entity.EntityType"));
        assertTrue(classEntities.containsKey("org.example.entity.MethodEntity"));
        assertTrue(classEntities.containsKey("org.example.entity.PackageEntity"));
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

        assertTrue(methodEntities.containsKey("org.example.entity.ClassEntity.addConnectedEntity"));
        assertTrue(methodEntities.containsKey("org.example.entity.Entity.addConnectedEntity"));
        assertTrue(methodEntities.containsKey("org.example.entity.Entity.getEntityType"));
//        assertTrue(methodEntities.containsKey("org.example.entity.EntityType"));
        assertTrue(methodEntities.containsKey("org.example.entity.MethodEntity.getClassEntity"));
        assertTrue(methodEntities.containsKey("org.example.entity.MethodEntity.addConnectedEntity"));
        assertTrue(methodEntities.containsKey("org.example.entity.PackageEntity.addConnectedEntity"));

        // NOTE: <init> and <clinit> are used for the constructors, manually changed those
        // https://www.baeldung.com/jvm-init-clinit-methods

        assertTrue(methodEntities.containsKey("org.example.entity.ClassEntity.init"));
        assertTrue(methodEntities.containsKey("org.example.entity.Entity.init"));
        assertTrue(methodEntities.containsKey("org.example.entity.EntityType.clinit"));
        assertTrue(methodEntities.containsKey("org.example.entity.MethodEntity.init"));
        assertTrue(methodEntities.containsKey("org.example.entity.MethodEntity.init"));
        assertTrue(methodEntities.containsKey("org.example.entity.PackageEntity.init"));
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

        PackageEntity entityPackage = (PackageEntity) packageEntities.get("org.example.entity");

        ClassEntity entityClass = (ClassEntity) classEntities.get("org.example.entity.Entity");
        ClassEntity packageEntityClass = (ClassEntity) classEntities.get("org.example.entity.PackageEntity");
        ClassEntity classEntityClass = (ClassEntity) classEntities.get("org.example.entity.ClassEntity");
        ClassEntity methodEntityClass = (ClassEntity) classEntities.get("org.example.entity.MethodEntity");

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

        ClassEntity entityClass = (ClassEntity) classEntities.get("org.example.entity.Entity");
        ClassEntity packageEntityClass = (ClassEntity) classEntities.get("org.example.entity.PackageEntity");
        ClassEntity classEntityClass = (ClassEntity) classEntities.get("org.example.entity.ClassEntity");
        ClassEntity methodEntityClass = (ClassEntity) classEntities.get("org.example.entity.MethodEntity");

        MethodEntity entityAddConnectedEntityMethod = (MethodEntity) methodEntities.get("org.example.entity.Entity.addConnectedEntity");
        MethodEntity packageAddConnectedEntityMethod = (MethodEntity) methodEntities.get("org.example.entity.PackageEntity.addConnectedEntity");
        MethodEntity classAddConnectedEntityMethod = (MethodEntity) methodEntities.get("org.example.entity.ClassEntity.addConnectedEntity");
        MethodEntity methodGetClassEntityMethod = (MethodEntity) methodEntities.get("org.example.entity.MethodEntity.getClassEntity");
        MethodEntity entityGetEntityTypeMethod = (MethodEntity) methodEntities.get("org.example.entity.Entity.getEntityType");

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

        Entity packageEntityClass = classEntities.get("org.example.entity.PackageEntity");
        Entity classEntityClass = classEntities.get("org.example.entity.ClassEntity");
        Entity methodEntityClass = classEntities.get("org.example.entity.MethodEntity");
        Entity entityClass = classEntities.get("org.example.entity.Entity");

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

        Entity packageEntityClass = classEntities.get("org.example.entity.PackageEntity");
        Entity classEntityClass = classEntities.get("org.example.entity.ClassEntity");
        Entity methodEntityClass = classEntities.get("org.example.entity.MethodEntity");
        Entity entityTypeClass = classEntities.get("org.example.entity.EntityType");
        Entity entityClass = classEntities.get("org.example.entity.Entity");

        assertTrue(entityClass.getConnectedEntities().contains(entityTypeClass));

        assertTrue(classEntityClass.getConnectedEntities().contains(packageEntityClass));
        assertTrue(classEntityClass.getConnectedEntities().contains(methodEntityClass)); // is a type Set, but also in a method's arguments

        assertTrue(packageEntityClass.getConnectedEntities().contains(classEntityClass)); // is a type Set, but also in a method's arguments
        assertTrue(methodEntityClass.getConnectedEntities().contains(classEntityClass));
    }

    // TODO - tests for edges/connections
}
