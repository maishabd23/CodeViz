package org.example;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.commons.io.FilenameUtils;
import org.example.entity.ClassEntity;
import org.example.entity.EntityType;
import org.example.entity.MethodEntity;
import org.example.entity.PackageEntity;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
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
    public void getAllPackages(String filepath){
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

                innerPackageEntity = new PackageEntity(innerFullPackageName);
                boolean success = graphGenerator.addEntity(innerFullPackageName, innerPackageEntity);

                if (!success){
                    break;
                }

                // sometimes the outerPackageEntity might be a placeholder
                // ex if packages A.B and A.C, package A's object was already added
                // when adding the connection, want to reference the object that is already there
                // TODO - add proper test for this
                if (outerPackageEntity != null){
                    innerPackageEntity.addConnectedEntity((PackageEntity) graphGenerator.getPackageEntities().get(outerFullPackageName));
                }

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
    public void getAllClasses(String filepath){

        ClassParser cp = new ClassParser(filepath);
        JavaClass jc;
        try {
            jc = cp.parse();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // System.out.println(jc.getClassName());
        // System.out.println(jc.getSuperclassName()); // Todo - add these in connections
        // System.out.println(jc.getInterfaceNames()); // Todo - add these in connections

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
    public void getAllMethods(String filepath){
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

                String totalName = jc.getClassName() + "." + method.getName();
                // System.out.println(totalName);
                // System.out.println(Arrays.toString(method.getArgumentTypes())); // Todo - add these in connections
                // System.out.println(method.getReturnType()); // Todo - add these in connections

                MethodEntity methodEntity = new MethodEntity(method.getName(), classEntity);
                boolean success = graphGenerator.addEntity(totalName, methodEntity);

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
    public void getAllConnections(String filepath){

        ClassParser cp = new ClassParser(filepath);
        JavaClass jc;
        try {
            jc = cp.parse();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        ClassEntity classEntity = (ClassEntity) graphGenerator.getClassEntities().get(jc.getClassName());

        System.out.println("classname: " + jc.getClassName());
        if (classEntity != null){

            // check interfaces
            for (String interfaceName : jc.getInterfaceNames()){ //  TODO - test this
                ClassEntity interfaceClassEntity = (ClassEntity)  graphGenerator.getClassEntities().get(interfaceName);

                if (interfaceClassEntity != null){
                    classEntity.addConnectedEntity(interfaceClassEntity);
                } else {
                    System.out.println("ERROR, interface entity should exist " + jc.getSuperclassName());
                }
            }

            // check superclasses
            if (!jc.getSuperclassName().isEmpty()){ // TODO - could there be multiple superclasses
                ClassEntity superClassEntity = (ClassEntity)  graphGenerator.getClassEntities().get(jc.getSuperclassName());

                if (superClassEntity != null){
                    classEntity.addConnectedEntity(superClassEntity);
                } else {
                    System.out.println("ERROR, superclass entity should exist " + jc.getSuperclassName());
                }
            }

            // check fields
            Field[] fields = jc.getFields();
            for (Field field : fields){
                String fieldType = String.valueOf(field.getType());
                System.out.println("field: " + fieldType);

                // TODO - handle List/Set types that hold another class type

                ClassEntity fieldClassEntity = (ClassEntity) graphGenerator.getClassEntities().get(fieldType);

                if (fieldClassEntity != null){
                    if (classEntity.equals(fieldClassEntity)){ // FIXME - investigate this further (occurs with enum)
                        System.out.println("ERROR, circular reference with class " + classEntity.getName() + " field " + fieldType);
                    } else {
                        classEntity.addConnectedEntity(fieldClassEntity);
                    }
                }
            }

            // TODO - check methods argument types

            // TODO - check methods contents (using asm)

            // TODO - update packages based on their classes' connections
            // TODO - update classes based on their methods' connections

        } else {
            System.out.println("ERROR, class entity should exist for " + jc.getClassName());
        }
    }

    /**
     * Generate the entities and connections between them
     * @author Thanuja Sivaananthan
     * @param filePaths     filepaths to get entities from
     */
    public void generateEntitesAndConnections(List<String> filePaths){
        for (String filepath : filePaths){
            getAllPackages(filepath);
            getAllClasses(filepath);
            getAllMethods(filepath);
        }

        for (String filepath : filePaths){
            getAllConnections(filepath);
        }
    }

    public void generateGraph(EntityType entityType, String filename){
        graphGenerator.directedGraphToGexf(graphGenerator.entitiesToNodes(entityType), filename);
    }

    public GraphGenerator getGraphGenerator() {
        return graphGenerator;
    }
}