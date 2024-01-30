package codeViz;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.util.HashSet;
import java.util.Set;

public class ConnectionVisitor extends VoidVisitorAdapter<Void> {

    private final Set<String> packages = new HashSet<>();
    private final Set<String> classes = new HashSet<>();
    private final Set<String> methods = new HashSet<>();
    private final Set<String> methodCalls = new HashSet<>();

    @Override
    public void visit(CompilationUnit n, Void arg) {
        // Extract package information
        n.getPackageDeclaration().ifPresent(packageDeclaration ->
                packages.add(packageDeclaration.getNameAsString()));

        super.visit(n, arg);
    }

    @Override
    public void visit(ClassOrInterfaceDeclaration n, Void arg) {
        classes.add(n.getNameAsString());
        super.visit(n, arg);
    }

    @Override
    public void visit(MethodDeclaration n, Void arg) {
        methods.add(n.getNameAsString());
        super.visit(n, arg);
    }

    // Additional visit methods can be added for other entities like fields, etc.
    @Override
    public void visit(MethodCallExpr n, Void arg) {
        methodCalls.add(n.getNameAsString());
        super.visit(n, arg);
    }

    public Set<String> getPackages() {
        return packages;
    }

    public Set<String> getClasses() {
        return classes;
    }

    public Set<String> getMethods() {
        return methods;
    }

    public Set<String> getMethodCalls() {
        return methodCalls;
    }
}
