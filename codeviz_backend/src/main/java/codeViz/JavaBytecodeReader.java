package codeViz;

import codeViz.entity.*;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.Type;
import org.apache.commons.io.FilenameUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;

/**
 * Reads java bytecode from the .class files
 * @author Thanuja Sivaananthan
 */
public class JavaBytecodeReader {

    private final GraphGenerator graphGenerator;

    public JavaBytecodeReader(){
        this.graphGenerator = new GraphGenerator();
    }


    /**
     * Recursively all files from a folder path
     * @author Thanuja Sivaananthan
     *
     * @param files         files to search
     * @param filePaths     existing file paths that were stored
     */
    private void getAllFilePaths(File[] files, List<String> filePaths){

        for (File file : files) {
            if (!file.isHidden()) {
                //System.out.println(file.getPath());

                String extension = FilenameUtils.getExtension(file.getName());

                // only add .class files, and search any inner directories
                if (extension.equals("class")){
                    filePaths.add(file.getPath());
                } else if (file.isDirectory()) {
                    File[] innerFiles = file.listFiles();
                    if (innerFiles != null) getAllFilePaths(innerFiles, filePaths);
                }
            }


        }
    }


    /**
     * Get all files from a folder path
     * @author Thanuja Sivaananthan
     *
     * @param folderName      folder name to get file paths from
     */
    public List<String> getAllFilePaths(String folderName){
        File folder = new File(folderName);

        // Check if folder exists
        if (!folder.exists()) {
            try {
                throw new Exception("Folder does not exist");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        // Get list of files in folder
        File[] files = folder.listFiles();

        List<String> filePaths = new ArrayList<>();

        if (files != null){
            getAllFilePaths(files, filePaths);
        }

        // // System.out.println("print all paths");
        // for (String path : filePaths){
        //     System.out.println(path);
        // }

        return filePaths;
    }

    /**
     * Get any package(s) from the file
     * @author Thanuja Sivaananthan
     *
     * @param filepath      file to get from
     */
    private void getAllPackages(String filepath){
        ClassParser cp = new ClassParser(filepath);
        JavaClass jc;
        try {
            jc = cp.parse();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (!jc.getPackageName().isEmpty()){
            // System.out.println(jc.getPackageName());

            String[] names = jc.getPackageName().split("\\.");
            String outerFullPackageName = "";
            String innerFullPackageName = "";

            PackageEntity outerPackageEntity = null;
            PackageEntity innerPackageEntity;

            for (String name : names){
                innerFullPackageName += name;
                // System.out.println(innerFullPackageName);


                // sometimes the outerPackageEntity might be a placeholder
                // ex if packages A.B and A.C, package A's object was already added
                // when adding the connection, want to reference the object that is already there
                // TODO - add proper test for this
                if (outerPackageEntity == null) {
                    innerPackageEntity = new PackageEntity(name);
                } else {
                    innerPackageEntity = new PackageEntity(name, outerPackageEntity);
                    innerPackageEntity.addConnectedEntity((PackageEntity) graphGenerator.getPackageEntities().get(outerFullPackageName));
                }

                boolean success = graphGenerator.addEntity(innerFullPackageName, innerPackageEntity);
                outerPackageEntity = innerPackageEntity;
                outerFullPackageName = innerFullPackageName;

                innerFullPackageName += ".";

            }
        }
    }

    /**
     * Get class from the file
     * @author Thanuja Sivaananthan
     *
     * @param filepath      file to get from
     */
    private void getAllClasses(String filepath){

        ClassParser cp = new ClassParser(filepath);
        JavaClass jc;
        try {
            jc = cp.parse();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // System.out.println(jc.getClassName());

        if (!jc.getPackageName().isEmpty()) {
            String[] names = jc.getClassName().split("\\.");
            String className = names[names.length-1];


            PackageEntity packageEntity = (PackageEntity) graphGenerator.getPackageEntities().get(jc.getPackageName());

            if (packageEntity != null){
                ClassEntity classEntity = new ClassEntity(className, packageEntity);
                boolean success = graphGenerator.addEntity(jc.getClassName(), classEntity);
            }else {
                System.out.println("ERROR, package should not be null for " + jc.getPackageName());
            }

        } else {
            ClassEntity classEntity = new ClassEntity(jc.getClassName());
            boolean success = graphGenerator.addEntity(jc.getClassName(), classEntity);
        }

    }

    /**
     * Get all methods from the file
     * @author Thanuja Sivaananthan
     *
     * @param filepath      file to get from
     */
    private void getAllMethods(String filepath){
        ClassParser cp = new ClassParser(filepath);
        JavaClass jc;
        try {
            jc = cp.parse();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        ClassEntity classEntity = (ClassEntity) graphGenerator.getClassEntities().get(jc.getClassName());

        if (classEntity != null){

            List<Method> methodList = Arrays.stream(jc.getMethods()).toList();
            for (Method method : methodList){

                // gephi cannot read names with character '<'
                // NOTE: <init> and <clinit> are used for the constructors, manually changed those
                // https://www.baeldung.com/jvm-init-clinit-methods
                MethodEntity methodEntity = new MethodEntity(method.getName(), classEntity);
                String methodName = methodEntity.getName();
                String totalName = jc.getClassName() + "." + methodName;

                // System.out.println(totalName);

                boolean success = graphGenerator.addEntity(totalName, methodEntity);

                // TODO - a subclass could call a superclass method (even if not explicitly there) - should the subclass have all of the superclass methods then?

                // TODO - how to handle overloaded methods? (class has more than one method of the same name)
                // ex multiple constructors
                if (!success){
                    System.out.println("TODO, already contains method with name " + totalName);
                    // could add a count at then end, but how are they differentiated afterward?
                    // could have the same number of arguments, etc
                }

            }

        }else {
            System.out.println("ERROR, class should not be null for " + jc.getClassName());
        }

    }

    /**
     * Get all connections for the entities
     * @author Thanuja Sivaananthan
     *
     * @param filepath      file to get from
     */
    private void getAllConnections(String filepath){

        ClassParser cp = new ClassParser(filepath);
        JavaClass jc;
        try {
            jc = cp.parse();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        ClassEntity classEntity = (ClassEntity) graphGenerator.getClassEntities().get(jc.getClassName());

        // System.out.println("classname: " + jc.getClassName());
        if (classEntity != null){

            // check interfaces and connect classes
            for (String interfaceName : jc.getInterfaceNames()){ //  TODO - test this
                ClassEntity interfaceClassEntity = (ClassEntity)  graphGenerator.getClassEntities().get(interfaceName);

                if (interfaceClassEntity != null){
                    classEntity.addConnectedEntity(interfaceClassEntity);
                } else {
                    // might be an imported class - not an error
                    System.out.println("ERROR, interface entity for " + jc.getClassName() + " does not exist: " + jc.getSuperclassName());
                }
            }

            // check superclasses and connect classes
            if (!jc.getSuperclassName().isEmpty()){ // TODO - could there be multiple superclasses
                ClassEntity superClassEntity = (ClassEntity)  graphGenerator.getClassEntities().get(jc.getSuperclassName());

                if (superClassEntity != null){
                    classEntity.addConnectedEntity(superClassEntity);
                    classEntity.setSuperClass(superClassEntity);
                } else {
                    // might be an imported class - not an error
                    System.out.println("ERROR, superclass entity for " + jc.getClassName() + " does not exist: " + jc.getSuperclassName());
                }
            }

            // check fields and connect classes
            for (Field field : jc.getFields()){
                String fieldType = String.valueOf(field.getType());
                ClassEntity fieldClassEntity = getAndStoreConnectedClassEntity(classEntity, fieldType);
                classEntity.addField("", fieldClassEntity);
            }

            // check methods argument/return types and connect classes
            for (Method method : jc.getMethods()){

                MethodEntity methodEntity = (MethodEntity) graphGenerator.getMethodEntities().get(jc.getClassName() + "." + MethodEntity.getProperName(method.getName()));

                for (Type argumentType : method.getArgumentTypes()){
                    String stringArgumentType = String.valueOf(argumentType);
                    ClassEntity argumentClassEntity = getAndStoreConnectedClassEntity(classEntity, stringArgumentType);
                    methodEntity.addArgument("", argumentClassEntity);
                }

                String stringReturnType = String.valueOf(method.getReturnType());
                ClassEntity returnClassEntity = getAndStoreConnectedClassEntity(classEntity, stringReturnType);
                methodEntity.setReturnType(returnClassEntity);

            }

            generateMethodConnections(filepath, classEntity);

        } else {
            System.out.println("ERROR, class entity should exist for " + jc.getClassName());
        }
    }

    /**
     * Get the connected class entity for a given name
     * If a valid entity, will add a connection
     * @param classEntity               class that has the connection destination
     * @param connectedClassName        class name of the connection source
     * @return                          the connected class (or dummy class if not valid)
     */
    private ClassEntity getAndStoreConnectedClassEntity(ClassEntity classEntity, String connectedClassName) {
        // TODO - handle List/Set types that hold another class type
        ClassEntity connectedClassEntity = (ClassEntity) graphGenerator.getClassEntities().get(connectedClassName);

        if (connectedClassEntity == null){
            String[] connectedClassNames = connectedClassName.split("\\.");
            connectedClassName = connectedClassNames[connectedClassNames.length - 1];
            connectedClassEntity = new ClassEntity(connectedClassName);
        } else {
            classEntity.addConnectedEntity(connectedClassEntity);
        }

        if (classEntity.equals(connectedClassEntity)){ // FIXME - investigate this further (occurs with enum)
            System.out.println("ERROR, circular reference with class " + classEntity.getName() + " and connected class " + connectedClassName);
        }
        return connectedClassEntity;
    }

    /**
     * Get all connections for the methods
     * @author Thanuja Sivaananthan
     *
     * @param filepath      file to get from
     * @param classEntity   the class entity for the corresponding file
     */
    private void generateMethodConnections(String filepath, ClassEntity classEntity){
        try {
            InputStream in =
                    Files.newInputStream(Paths.get(filepath));
            ClassReader reader = new ClassReader(in);

            ClassVisitor tcv = new ConnectedClassVisitor(graphGenerator, classEntity);
            reader.accept(tcv, 0);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     *  Update class connections based on their methods' connections
     * @author Thanuja Sivaananthan
     */
    private void updateClassConnections(){
        Collection<Entity> classEntities = graphGenerator.getClassEntities().values();
        for (Entity classEntity : classEntities){
            // for each method in the class
            for (MethodEntity methodEntity : ((ClassEntity) classEntity).getMethods()){
                // get the connected method
                for (Entity connectedMethod : methodEntity.getConnectedEntities()){
                    // and connect the class to the connected method's class
                    ClassEntity connectedClass = ((MethodEntity) connectedMethod).getClassEntity();
                    ((ClassEntity) classEntity).addConnectedEntity(connectedClass);
                }
            }
        }
    }

    /**
     * Update package connections based on their classes' connections
     * @author Thanuja Sivaananthan
     */
    private void updatePackageConnections(){
        Collection<Entity> packageEntities = graphGenerator.getPackageEntities().values();
        for (Entity packageEntity : packageEntities){
            // for each class in the package
            for (ClassEntity classEntity : ((PackageEntity) packageEntity).getClasses()){
                // get the connected class
                for (Entity connectedClass : classEntity.getConnectedEntities()){
                    // and connect the package to the connected class's package (if it exists)
                    PackageEntity connectedPackage = ((ClassEntity) connectedClass).getPackageEntity();
                    if (connectedPackage != null){
                        ((PackageEntity) packageEntity).addConnectedEntity(connectedPackage);
                    }
                }
            }
        }
    }

    /**
     * Generate the entities and connections between them
     * @author Thanuja Sivaananthan
     * @param folderName      folder name to get file paths from
     * @return boolean, whether the entity generation was successful
     */
    public boolean generateEntitiesAndConnections(String folderName) {
        List<String> filePaths = getAllFilePaths(folderName);
        if (!filePaths.isEmpty()) {
            generateEntitiesAndConnections(filePaths);
            return true;
        }
        return false;
    }

    /**
     * Generate the entities and connections between them
     * @author Thanuja Sivaananthan
     * @param filePaths     filepaths to get entities from
     */
    private void generateEntitiesAndConnections(List<String> filePaths){
        graphGenerator.clearEntities();
        for (String filepath : filePaths){
            getAllPackages(filepath);
            getAllClasses(filepath);
            getAllMethods(filepath);
        }

        for (String filepath : filePaths){
            getAllConnections(filepath);
        }

        // TODO - test if updated connections are needed
        updateClassConnections();
        updatePackageConnections();

        graphGenerator.setEntitiesCoordinates();
    }

    public void generateGraph(EntityType entityType, String filename){
        graphGenerator.directedGraphToGexf(entityType, filename, false);
    }

    public GraphGenerator getGraphGenerator() {
        return graphGenerator;
    }
}