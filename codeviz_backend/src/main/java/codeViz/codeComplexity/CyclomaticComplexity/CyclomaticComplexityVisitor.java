package codeViz.codeComplexity.CyclomaticComplexity;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.util.HashMap;
import java.util.Optional;

// Visitor to compute cyclomatic complexity
public class CyclomaticComplexityVisitor extends VoidVisitorAdapter<Void> {
    private final HashMap<String, Integer> methodComplexities = new HashMap<>();

    @Override
    public void visit(MethodDeclaration n, Void arg) {
        int complexity = calculateCyclomaticComplexity(n.getBody());
        Optional<ClassOrInterfaceDeclaration> containingClassNode = n.findAncestor(ClassOrInterfaceDeclaration.class);
        if (containingClassNode.isPresent()) {
            String className = containingClassNode.get().getNameAsString();
            String methodName = n.getNameAsString();
            methodComplexities.put(className + "." + methodName, complexity);
        }
    }

    private int calculateCyclomaticComplexity(Optional<BlockStmt> node) {
        if (node.isPresent()) {
            ComplexityVisitor complexityVisitor = new ComplexityVisitor();
            complexityVisitor.visit(node.get(), null);
            return complexityVisitor.getComplexity();
        }
        return 0; // if case it's empty
    }

    public HashMap<String, Integer> getMethodComplexities() {
        return methodComplexities;
    }
}