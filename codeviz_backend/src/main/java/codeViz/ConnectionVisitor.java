package codeViz;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.util.HashSet;
import java.util.Set;

public class ConnectionVisitor extends VoidVisitorAdapter<Void> {

    private final Set<String> classes = new HashSet<>();
    private final Set<String> fieldTypes = new HashSet<>();
    private final Set<String> methodInvocations = new HashSet<>();

    @Override
    public void visit(CompilationUnit n, Void arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(ClassOrInterfaceDeclaration n, Void arg) {
        // Collect class names
        classes.add(n.getNameAsString());

        // Collect field types
        n.getFields().forEach(fieldDeclaration -> {
            if (fieldDeclaration.getElementType().isClassOrInterfaceType()) {
                ClassOrInterfaceType fieldType = fieldDeclaration.getElementType().asClassOrInterfaceType();
                fieldTypes.add(fieldType.getNameAsString());
            }
        });

        // Collect method invocations
        n.findAll(MethodCallExpr.class).forEach(methodCallExpr -> {
            methodInvocations.add(methodCallExpr.getNameAsString());
        });

        super.visit(n, arg);
    }

    public Set<String> getClasses() {
        return classes;
    }

    public Set<String> getFieldTypes() {
        return fieldTypes;
    }

    public Set<String> getMethodInvocations() {
        return methodInvocations;
    }
}
