package codeViz;

import codeViz.entity.ClassEntity;
import codeViz.entity.MethodEntity;
import codeViz.entity.PackageEntity;
import com.github.javaparser.ParseResult;

import com.github.javaparser.ast.type.ClassOrInterfaceType;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Set;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


@Controller
public class GitHubRepoController {

    private final GraphGenerator graphGenerator;
    private CompilationUnit compilationUnit;

    private  ZipEntry zipEntry;

    private  ZipInputStream zipInputStream;

    private List<byte[]> entryContentsList = new ArrayList<>();

    public GitHubRepoController(){
        this.graphGenerator = new GraphGenerator();
        this.compilationUnit = new CompilationUnit();
        this.entryContentsList = new ArrayList<>();
        //this.zipEntry = new ZipEntry();
    }
    private byte[] retrieveGitHubCodebase(String repoUrl) {
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

    private List<PackageEntity> parseJavaFilesFromZip(InputStream byteArrayInputStream) throws IOException {
        List<PackageEntity> packages = new ArrayList<>();
        JavaParser javaParser = new JavaParser();

        this.zipInputStream = new ZipInputStream(byteArrayInputStream);

        ZipEntry entry;
        while ((entry = zipInputStream.getNextEntry()) != null) {
            if (!entry.isDirectory() && entry.getName().endsWith(".java")) {
                byte[] entryContent = zipInputStream.readAllBytes();

                if (entryContent.length > 0) {
                    entryContentsList.add(entryContent);

                    String code = new String(entryContent, StandardCharsets.UTF_8);
                    ParseResult<CompilationUnit> parseResult = javaParser.parse(new StringReader(code));

                    if (parseResult.isSuccessful()) {
                        CompilationUnit compilationUnit = parseResult.getResult().get();
                        this.compilationUnit = compilationUnit;
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
        return packages;
    }

    private List<PackageEntity> createEntities(CompilationUnit compilationUnit) {
        List<PackageEntity> packages = new ArrayList<>();
        ConnectionVisitor connectionVisitor = new ConnectionVisitor();
        compilationUnit.accept(connectionVisitor, null);

//        Set<String> packagesConnections = connectionVisitor.getPackages();
//        Set<String> classes = connectionVisitor.getClasses();
//        Set<String> methods = connectionVisitor.getMethods();
//        Set<String> methodCalls = connectionVisitor.getMethodCalls(); //TODO - add this later

        // Visit the compilation unit
        connectionVisitor.visit(compilationUnit, null);

        // Get the collected information
        Set<String> connectedClasses = connectionVisitor.getClasses();
        Set<String> fieldTypes = connectionVisitor.getFieldTypes();
        Set<String> methodInvocations = connectionVisitor.getMethodInvocations();


        // Iterate over all types (classes, interfaces, enums, etc.) in the compilation unit
        compilationUnit.getTypes().forEach(type -> {
            if (type instanceof ClassOrInterfaceDeclaration classDeclaration) {
                String packageName = compilationUnit.getPackageDeclaration()
                        .map(pd -> pd.getName().toString())
                        .orElse("");

                PackageEntity packageEntity = packages.stream()
                        .filter(p -> p.getName().equals(packageName))
                        .findFirst()
                        .orElseGet(() -> {
                            PackageEntity newPackage = new PackageEntity(packageName);
                            packages.add(newPackage);
                            return newPackage;
                        });

                boolean packageSuccess = graphGenerator.addEntity(packageName, packageEntity);

                ClassEntity classEntity = new ClassEntity(classDeclaration.getNameAsString(), packageEntity);
                boolean classSuccess = graphGenerator.addEntity(classEntity.getName(), classEntity);
                //classEntity.addConnectedEntity(classEntity); //TODO - TEST

                classDeclaration.getMethods().forEach(methodDeclaration -> {
                    MethodEntity methodEntity = new MethodEntity(methodDeclaration.getNameAsString(), classEntity);
                    boolean methodSuccess = graphGenerator.addEntity(methodEntity.getName(), methodEntity);
                    //methodEntity.addConnectedEntity(methodEntity); //TODO - TEST

                    classEntity.addMethod(methodEntity);
                });


                System.out.println("Class: " + classDeclaration.getNameAsString());

                System.out.println("------");

                packageEntity.addClass(classEntity);
                //packageEntity.addConnectedEntity(packageEntity); //TODO - TEST

                //TESTING THE NODES
                for (ClassEntity classEntity1 : packageEntity.getClasses()) {
                    System.out.println("------------------------------------------------------");
                    System.out.println("TESTING");
                    System.out.println("CLASS: " + classEntity1.getName());
                    System.out.println("METHODS IN THIS CLASS: " + Arrays.toString(classEntity1.getMethods().toArray()));
                    System.out.println("------------------------------------------------------");
                }
            }
        });

        return packages;
    }

    private void createClassConnections() throws IOException {
        JavaParser javaParser = new JavaParser();

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
                                    System.out.println("Class: " + classDeclaration.getNameAsString());
                                    System.out.println("Field: " + fieldDeclaration.getVariable(0).getNameAsString());
                                    System.out.println("Connected Class: " + fieldClassEntity.getName());
                                    System.out.println("----------------------------------------------");

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
                                            System.out.println("Class: " + classDeclaration.getNameAsString());
                                            System.out.println("Field: " + fieldDeclaration.getVariable(0).getNameAsString());
                                            System.out.println("Connected Class in Generic Type: " + genericClassEntity.getName());
                                            System.out.println("----------------------------------------------");
                                        }
                                    }
                                });
                            }
                        });
                    }
                });

            } else {
                // Handle parsing errors
                parseResult.getProblems().forEach(problem -> {
                    System.err.println("Parsing error: " + problem.getMessage());
                });
            }
        }
    }

    private void analyzeCodebase(byte[] codebase) {
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(codebase)) {
            List<PackageEntity> packages = parseJavaFilesFromZip(byteArrayInputStream);
            createClassConnections();
            // Pass the created entities to the model or perform other actions
            // model.addAttribute("packages", packages);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        GitHubRepoController gitHubRepoController = new GitHubRepoController();

        byte[] result = gitHubRepoController.retrieveGitHubCodebase("https://github.com/maishabd23/online-bookstore");
        //System.out.println(Arrays.toString(result));
        gitHubRepoController.analyzeCodebase(result);
    }

}
