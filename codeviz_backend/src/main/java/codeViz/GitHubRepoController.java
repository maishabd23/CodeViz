package codeViz;

import codeViz.entity.ClassEntity;
import codeViz.entity.MethodEntity;
import codeViz.entity.PackageEntity;
import com.github.javaparser.ParseResult;
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
import java.util.stream.Collectors;

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

    public GitHubRepoController(){
        this.graphGenerator = new GraphGenerator();
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

        ZipInputStream zipInputStream = new ZipInputStream(byteArrayInputStream);

        ZipEntry entry;
        while ((entry = zipInputStream.getNextEntry()) != null) {
            if (!entry.isDirectory() && entry.getName().endsWith(".java")) {
                byte[] entryContent = zipInputStream.readAllBytes();

                if (entryContent.length > 0) {
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
            }
        }
        return packages;
    }

    private List<PackageEntity> createEntities(CompilationUnit compilationUnit) {
        List<PackageEntity> packages = new ArrayList<>();
        ConnectionVisitor connectionVisitor = new ConnectionVisitor();
        compilationUnit.accept(connectionVisitor, null);

        Set<String> packagesConnections = connectionVisitor.getPackages();
        Set<String> classes = connectionVisitor.getClasses();
        Set<String> methods = connectionVisitor.getMethods();
        Set<String> methodCalls = connectionVisitor.getMethodCalls(); //TODO - add this later


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

                //TESTING THE CONNECTIONS
                for (ClassEntity classEntity2 : packageEntity.getClasses()) {
                    System.out.println("///////////////////////////////////////////////////////");
                    System.out.println("TESTING CLASS" + classes);
                    System.out.println("THIS CLASS IS CONNECTED TO THE FOLLOWING PACKAGES: " + packagesConnections);
                    System.out.println("THIS CLASS IS CONNECTED TO THE FOLLOWING METHODS: " + methods);
                    System.out.println("------------------------------------------------------");
                }
            }
        });

        return packages;
    }

    private void analyzeCodebase(byte[] codebase) {
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(codebase)) {
            List<PackageEntity> packages = parseJavaFilesFromZip(byteArrayInputStream);

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
