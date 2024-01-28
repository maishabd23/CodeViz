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

    private void analyzeCodebase(byte[] codebase) {
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(codebase)) {
            JavaParser javaParser = new JavaParser();

            try (ZipInputStream zipInputStream = new ZipInputStream(byteArrayInputStream)) {
                ZipEntry entry;
                while ((entry = zipInputStream.getNextEntry()) != null) {
                    if (!entry.isDirectory() && entry.getName().endsWith(".java")) {
                        // Read the contents of the entry (Java source file)
                        byte[] entryContent = zipInputStream.readAllBytes();

                        if (entryContent.length > 0) {
                            String code = new String(entryContent, StandardCharsets.UTF_8);

                            ParseResult<CompilationUnit> parseResult = javaParser.parse(new StringReader(code));

                            if (parseResult.isSuccessful()) {
                                CompilationUnit compilationUnit = parseResult.getResult().get();
                                List<PackageEntity> packages = new ArrayList<>();

                                // Iterate over all types (classes, interfaces, enums, etc.) in the compilation unit
                                compilationUnit.getTypes().forEach(type -> {
                                    if (type instanceof ClassOrInterfaceDeclaration classDeclaration) {

                                        // Extract package information
                                        String packageName = compilationUnit.getPackageDeclaration()
                                                .map(pd -> pd.getName().toString())
                                                .orElse("");

                                        // Find or create the package entity
                                        PackageEntity packageEntity = packages.stream()
                                                .filter(p -> p.getName().equals(packageName))
                                                .findFirst()
                                                .orElseGet(() -> {
                                                    PackageEntity newPackage = new PackageEntity(packageName);
                                                    packages.add(newPackage);
                                                    return newPackage;
                                                });

                                        // Create class entity
                                        ClassEntity classEntity = new ClassEntity(classDeclaration.getNameAsString(), packageEntity);

                                        // Iterate over methods in the class
                                        classDeclaration.getMethods().forEach(methodDeclaration -> {
                                            // Create method entity
                                            MethodEntity methodEntity = new MethodEntity(methodDeclaration.getNameAsString(), classEntity);
                                            // TODO - Extract additional information (Arguments) and add it to the methodEntity

                                            // Add the method entity to the class
                                            classEntity.addMethod(methodEntity);
                                        });

                                        // Add the class entity to the package
                                        packageEntity.addClass(classEntity);

                                        System.out.println(packages);
                                        for (ClassEntity classEntity1 : packageEntity.getClasses()) {
                                            System.out.println(classEntity1.getName());
                                            System.out.println(Arrays.toString(classEntity1.getMethods().toArray()));
                                        }
                                    }
                                });

                                // Pass the created entities to the model
                                //model.addAttribute("packages", packages);
                            } else {
                                // Handle parsing errors
                                parseResult.getProblems().forEach(problem -> {
                                    System.err.println("Parsing error: " + problem.getMessage());
                                });
                            }
                        }
                    }
                }
            }

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
