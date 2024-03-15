package codeViz;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.util.Optional;

// Visitor to compute cyclomatic complexity
public class CyclomaticComplexityVisitor extends VoidVisitorAdapter<Void> {
    private final MethodComplexities methodComplexities = new MethodComplexities();

    @Override
    public void visit(MethodDeclaration n, Void arg) {
        int complexity = calculateCyclomaticComplexity(n.getBody());
        methodComplexities.put(n.getNameAsString(), complexity);
    }

    private int calculateCyclomaticComplexity(Optional<BlockStmt> node) {
        if (node.isPresent()) {
            ComplexityVisitor complexityVisitor = new ComplexityVisitor();
            complexityVisitor.visit(node.get(), null);
            return complexityVisitor.getComplexity();
        }
        return 0; // if case it's empty
    }

    public MethodComplexities getMethodComplexities() {
        return methodComplexities;
    }
}