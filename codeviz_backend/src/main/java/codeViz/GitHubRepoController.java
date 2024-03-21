package codeViz;

import codeViz.codeComplexity.ClassComplexityDetails;
import codeViz.codeComplexity.CyclomaticComplexity.CyclomaticComplexityVisitor;
import codeViz.entity.*;
import com.github.javaparser.*;

import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import org.apache.commons.io.FilenameUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.springframework.stereotype.Controller;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;

import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


@Controller
public class GitHubRepoController {

    private final GraphGenerator graphGenerator;

    private List<byte[]> entryContentsList = new ArrayList<>();

    public GitHubRepoController(){
        this.graphGenerator = new GraphGenerator(); // only set this once (clear entities each time a new graph is made)
        this.entryContentsList = new ArrayList<>();
    }

    public boolean isRepoUrlReachable(String repoUrl){
        HttpClient httpClient = HttpClients.createDefault();
        HttpGet request = new HttpGet(repoUrl + "/archive/main.zip"); // Assuming main branch

        try {
            HttpResponse response = httpClient.execute(request);

            // Check the HTTP status code
            int statusCode = response.getStatusLine().getStatusCode();
            return statusCode == HttpStatus.SC_OK;
        } catch (IOException e) {
            return false;
        }
    }

    public byte[] retrieveGitHubCodebase(String repoUrl) {
        HttpClient httpClient = HttpClients.createDefault();
        HttpGet request = new HttpGet(repoUrl + "/archive/main.zip"); // Assuming main branch

        try {
            HttpResponse response = httpClient.execute(request);

            // Check the HTTP status code
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                throw new IOException("Failed to retrieve codebase. HTTP Status Code: " + statusCode);
            }

            // Read the response content as a byte array
            HttpEntity entity = response.getEntity();
            return entity.getContent().readAllBytes();
        } catch (IOException e) {
            e.printStackTrace();
            // Log or throw a custom exception with more details
            throw new RuntimeException("Error retrieving codebase", e);
        }
    }

    private boolean parseJavaFilesFromZip(InputStream byteArrayInputStream) throws IOException {
        boolean isValidJavaProject = false;
        Set<PackageEntity> packages = new HashSet<>();
        JavaParser javaParser = new JavaParser();

        ZipInputStream zipInputStream = new ZipInputStream(byteArrayInputStream);

        ZipEntry entry;
        while ((entry = zipInputStream.getNextEntry()) != null) {
            if (!entry.isDirectory() && entry.getName().endsWith(".java")) {
                isValidJavaProject = true;
                byte[] entryContent = zipInputStream.readAllBytes();

                if (entryContent.length > 0) {
                    entryContentsList.add(entryContent);

                    String code = new String(entryContent, StandardCharsets.UTF_8);
                    ParseResult<CompilationUnit> parseResult = javaParser.parse(new StringReader(code));

                    if (parseResult.isSuccessful()) {
                        CompilationUnit compilationUnit = parseResult.getResult().get();
                        packages.addAll(createEntities(compilationUnit));
                    } else {
                        // Handle parsing errors
                        parseResult.getProblems().forEach(problem -> {
                            System.err.println("Parsing error: " + problem.getMessage());
                        });
                    }
                }
                zipInputStream.closeEntry();
            }
        }
        return isValidJavaProject;
    }

    private void setLinesOfCode(Optional<Position> startPosition, Optional<Position> endPosition, Entity entity){
        // Complexity Metrics: Lines of Code
        // includes blank lines and comments (not JavaDoc)
        if (startPosition.isPresent() && endPosition.isPresent()){
            int startLine = startPosition.get().line;
            int endLine = endPosition.get().line;
            int linesOfCode = endLine - startLine + 1;

            System.out.println(entity.getEntityType().getName() + ": " + entity.getName() + ", Lines of Code: " + linesOfCode);
            entity.getComplexityDetails().setLinesOfCode(linesOfCode);
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
            //String[] connectedClassNames = connectedClassName.split("\\.");
            //connectedClassName = connectedClassNames[connectedClassNames.length - 1];
            connectedClassEntity = new ClassEntity(connectedClassName);
        } else {
            classEntity.addConnectedEntity(connectedClassEntity);
        }

        if (classEntity.equals(connectedClassEntity)){ // FIXME - investigate this further (occurs with enum)
            System.out.println("ERROR, circular reference with class " + classEntity.getName() + " and connected class " + connectedClassName);
        }
        return connectedClassEntity;
    }

    private Set<PackageEntity> createEntities(CompilationUnit compilationUnit) {
        Set<PackageEntity> packages = new HashSet<>();
        ConnectionVisitor connectionVisitor = new ConnectionVisitor();
        compilationUnit.accept(connectionVisitor, null);

        // Visit the compilation unit
        connectionVisitor.visit(compilationUnit, null);

        // Get the collected information
        Set<String> connectedClasses = connectionVisitor.getClasses();
        Set<String> fieldTypes = connectionVisitor.getFieldTypes();
        Set<String> methodInvocations = connectionVisitor.getMethodInvocations();

        // Get the package name from the compilation unit
        String basePackageName = compilationUnit.getPackageDeclaration()
                .map(pd -> pd.getName().toString())
                .orElse("");

        // Iterate over all types (classes, interfaces, enums, etc.) in the compilation unit
        compilationUnit.getTypes().forEach(type -> {
            if (type instanceof ClassOrInterfaceDeclaration classDeclaration) {

                // Get or create the package entity
                PackageEntity packageEntity = getOrCreatePackage(packages, basePackageName, connectedClasses);

                // Add class entity to the package
                ClassEntity classEntity = new ClassEntity(classDeclaration.getNameAsString(), packageEntity);
                boolean classSuccess = graphGenerator.addEntity(classEntity.getName(), classEntity);
                packageEntity.addClass(classEntity);

                classDeclaration.getMethods().forEach(methodDeclaration -> {
                    MethodEntity methodEntity = new MethodEntity(methodDeclaration.getNameAsString(), classEntity);
                    boolean methodSuccess = graphGenerator.addEntity(methodEntity.getName(), methodEntity);
                    classEntity.addMethod(methodEntity);

                    System.out.println("METHOD " + methodDeclaration.getNameAsString() + " PARAMETERS: " + methodDeclaration.getParameters());
                    methodDeclaration.getParameters().forEach(parameter -> {
                        String stringArgumentType = String.valueOf(parameter.getType());
                        ClassEntity argumentClassEntity = getAndStoreConnectedClassEntity(classEntity, stringArgumentType);
                        methodEntity.addArgument(argumentClassEntity);
                    });

                    System.out.println("METHOD " + methodDeclaration.getNameAsString() + " RETURN TYPE: " + methodDeclaration.getType());
                    String stringReturnType = String.valueOf(methodDeclaration.getType());
                    ClassEntity returnClassEntity = getAndStoreConnectedClassEntity(classEntity, stringReturnType);
                    methodEntity.setReturnType(returnClassEntity);

                    setLinesOfCode(methodDeclaration.getBegin(), methodDeclaration.getEnd(), methodEntity);
                });

                System.out.println("CLASS " + classDeclaration.getNameAsString() + " FIELDS: " + classDeclaration.getFields());
                classDeclaration.getFields().forEach(fieldDeclaration -> {
                    String fieldType = String.valueOf(fieldDeclaration.getElementType());
                    ClassEntity fieldClassEntity = getAndStoreConnectedClassEntity(classEntity, fieldType);
                    classEntity.addField(fieldClassEntity);
                });

                System.out.println("Class: " + classDeclaration.getNameAsString());
                setLinesOfCode(classDeclaration.getBegin(), classDeclaration.getEnd(), classEntity);
            }
        });

        // Print out classes within each package
        packages.forEach(packageEntity -> {
            System.out.println("Package: " + packageEntity.getName() + " Connected Packages: " + packageEntity.getConnectedEntities());
            packageEntity.getClasses().forEach(classEntity -> {
                System.out.println("\tClass: " + classEntity.getName());
                classEntity.getMethods().forEach(methodEntity -> {
                    System.out.println("\t\tMethod: " + methodEntity.getName());
                });
            });
        });

        return packages;
    }

    // Method to create or retrieve package entities for nested packages
    private PackageEntity getOrCreatePackage(Set<PackageEntity> packages, String packageName,  Set<String> connectedClasses) {
        String[] packageNames = packageName.split("\\.");
        PackageEntity parentPackage = null;

        for (String name : packageNames) {
            String fullPackageName = (parentPackage == null || parentPackage.getName().isEmpty()) ? name : parentPackage.getName() + "." + name;
            PackageEntity finalParentPackage = parentPackage;
            PackageEntity packageEntity = (PackageEntity) graphGenerator.getPackageEntities().get(fullPackageName);

            if (packageEntity == null){ // not in graph generator yet, need to create a new package
                PackageEntity newPackage = new PackageEntity(fullPackageName);
                packages.add(newPackage);

                boolean packageSuccess = graphGenerator.addEntity(fullPackageName, newPackage);
                if (finalParentPackage != null && !newPackage.equals(finalParentPackage) && graphGenerator.getPackageEntities().get(finalParentPackage.getName()) != null){
                    String className = Arrays.toString(connectedClasses.toArray());
                    newPackage.addConnectedEntity((PackageEntity) graphGenerator.getPackageEntities().get(finalParentPackage.getName()));
                }
                packageEntity = newPackage;
            }
            parentPackage = packageEntity; // Update parent package for next iteration
        }

        assert parentPackage != null;


        return parentPackage;
    }



    private void createClassConnections() throws IOException {
        JavaParser javaParser = new JavaParser();

        HashMap<ClassEntity, Set<Entity>> connectedClasses = new HashMap<>();
        Set<ClassEntity> classEntityList = new HashSet<>();

        for (byte[] entryContent : entryContentsList) {
            String code = new String(entryContent, StandardCharsets.UTF_8);
            ParseResult<CompilationUnit> parseResult = javaParser.parse(new StringReader(code));

            if (parseResult.isSuccessful()) {
                CompilationUnit compilationUnit = parseResult.getResult().get();
                ConnectionVisitor connectionVisitor = new ConnectionVisitor();
                compilationUnit.accept(connectionVisitor, null);
                connectionVisitor.visit(compilationUnit, null);


                compilationUnit.getTypes().forEach(type -> {
                    if (type instanceof ClassOrInterfaceDeclaration classDeclaration) {
                        classDeclaration.getFields().forEach(fieldDeclaration -> {
                            // Check if the field type is a class or interface
                            if (fieldDeclaration.getElementType().isClassOrInterfaceType()) {
                                ClassOrInterfaceType fieldType = fieldDeclaration.getElementType().asClassOrInterfaceType();

                                // Retrieve the corresponding ClassEntity from the GraphGenerator
                                ClassEntity fieldClassEntity = (ClassEntity) graphGenerator.getClassEntities().get(fieldType.getNameAsString());

                                if (fieldClassEntity != null) {
                                    graphGenerator.changeInterfaceToClassEntity(classDeclaration).addConnectedEntity(fieldClassEntity);
                                    connectedClasses.put(graphGenerator.changeInterfaceToClassEntity(classDeclaration), graphGenerator.changeInterfaceToClassEntity(classDeclaration).getConnectedEntities());
                                }
                            }

                            // Check if the field type is a parameterized type (List, Set, etc.)
                            if (fieldDeclaration.getElementType().isClassOrInterfaceType() && fieldDeclaration.getElementType().asClassOrInterfaceType().getTypeArguments().isPresent()) {
                                fieldDeclaration.getElementType().asClassOrInterfaceType().getTypeArguments().get().forEach(typeArg -> {
                                    // Check if the type argument is a class or interface
                                    if (typeArg.isClassOrInterfaceType()) {
                                        ClassOrInterfaceType genericType = typeArg.asClassOrInterfaceType();

                                        // Retrieve the corresponding ClassEntity from the GraphGenerator
                                        ClassEntity genericClassEntity = (ClassEntity) graphGenerator.getClassEntities().get(genericType.getNameAsString());

                                        if (genericClassEntity != null) {
                                            graphGenerator.changeInterfaceToClassEntity(classDeclaration).addConnectedEntity(genericClassEntity);
                                            connectedClasses.put(graphGenerator.changeInterfaceToClassEntity(classDeclaration), graphGenerator.changeInterfaceToClassEntity(classDeclaration).getConnectedEntities());
                                        }
                                    }
                                });
                            }
                        });

                        classDeclaration.getMethods().forEach(methodDeclaration -> {
                            // Connect method entities based on method invocations
                            MethodEntity methodEntity = (MethodEntity) graphGenerator.getMethodEntities().get(methodDeclaration.getNameAsString());
                            methodDeclaration.accept(new VoidVisitorAdapter<Void>() {
                                @Override
                                public void visit(MethodCallExpr methodCallExpr, Void arg) {
                                    super.visit(methodCallExpr, arg);
                                    String calledMethodName = methodCallExpr.getNameAsString();
                                    // Assuming the method entity is available in the graph generator
                                    MethodEntity calledMethodEntity = (MethodEntity) graphGenerator.getMethodEntities().get(calledMethodName);
                                    if (calledMethodEntity != null) {
                                        // Add called method entity to the connected entities of the current method entity
                                        methodEntity.addConnectedEntity(calledMethodEntity);
                                    }
                                }
                            }, null);
                        });
                    }
                });


                // Create a visitor to compute cyclomatic complexity
                CyclomaticComplexityVisitor visitor = new CyclomaticComplexityVisitor();
                visitor.visit(compilationUnit, null);

                // Cyclomatic complexity
                // CC = E – N + 2P where E = the number of edges in the control flow graph
                //N = the number of nodes in the control flow graph
                //P = the number of connected components                System.out.println("Cyclomatic complexity for methods:");
                visitor.getMethodComplexities().forEach((methodName, complexity) -> {
                    System.out.println(methodName + ": " + complexity);
                    MethodEntity methodEntity = (MethodEntity) graphGenerator.getMethodEntities().get(methodName);
                    if (methodEntity != null) {
                        methodEntity.getComplexityDetails().setCyclomaticComplexity(complexity);
                        // Cyclomatic complexity of class = sum of cyclomatic complexities of the methods.
                        ((ClassComplexityDetails) methodEntity.getClassEntity().getComplexityDetails()).incrementCyclomaticComplexity(complexity);
                    } else {
                        System.out.println("ERROR, Method is null for methodName: " + methodName);
                    }
                });

            } else {
                // Handle parsing errors
                parseResult.getProblems().forEach(problem -> {
                    System.err.println("Parsing error: " + problem.getMessage());
                });
            }
        }
        for (Map.Entry<ClassEntity, Set<Entity>> entry : connectedClasses.entrySet()) {
            System.out.println("TESTING CONNECTED CLASSES");
            System.out.println("----------------------------------------------");
            String key = entry.getKey().getName();
            String value = entry.getValue().toString();
            System.out.println("Class=" + key + ", Connected Classes=" + value);
            System.out.println("----------------------------------------------");
        }
    }

    public boolean analyzeCodebase(byte[] codebase) {

        // reset when analyzing new codebase
        this.entryContentsList = new ArrayList<>();

        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(codebase)) {
            if (parseJavaFilesFromZip(byteArrayInputStream)) {
                createClassConnections();
                //createPackageConnections();
                // Pass the created entities to the model or perform other actions
                // model.addAttribute("packages", packages);
                return true;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

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

    public void generateEntitiesAndConnections(){
        //graphGenerator.clearEntities();
        updatePackageConnections();
        updateClassConnections();
        //updatePackageConnections();

        graphGenerator.setEntitiesCoordinates();
        // TODO - test if updated connections are needed

    }

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


    private void updatePackageConnections(){
        Collection<Entity> packageEntities = graphGenerator.getPackageEntities().values();
        for (Entity packageEntity : packageEntities){

            System.out.println(((PackageEntity) packageEntity).getClasses());

            // for each class in the package
            for (ClassEntity classEntity : ((PackageEntity) packageEntity).getClasses()){

                // get the connected class
                for (Entity connectedClass : classEntity.getConnectedEntities()){
                    // and connect the package to the connected class's package (if it exists)
                    PackageEntity connectedPackage = ((ClassEntity) connectedClass).getPackageEntity();
                    if (connectedPackage != null && !connectedPackage.equals(packageEntity)){
                        ((PackageEntity) packageEntity).addConnectedEntity(connectedPackage);
                    }
                }
            }
        }
    }
    public GraphGenerator getGraphGenerator() {
        return graphGenerator;
    }

    public static void main(String[] args) {
        GitHubRepoController gitHubRepoController = new GitHubRepoController();

        byte[] result = gitHubRepoController.retrieveGitHubCodebase("https://github.com/maishabd23/online-bookstore");
        //System.out.println(Arrays.toString(result));
        gitHubRepoController.analyzeCodebase(result);
    }

}
