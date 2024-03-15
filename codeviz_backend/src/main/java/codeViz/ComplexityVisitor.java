package codeViz;

import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;


// Visitor to calculate cyclomatic complexity
public class ComplexityVisitor extends VoidVisitorAdapter<Void> {
    private int complexity = 1; // Start with 1 for the method entry point

    @Override
    public void visit(IfStmt n, Void arg) {
        complexity++;
        super.visit(n, arg);
    }

    @Override
    public void visit(WhileStmt n, Void arg) {
        complexity++;
        super.visit(n, arg);
    }

    @Override
    public void visit(ForStmt n, Void arg) {
        complexity++;
        super.visit(n, arg);
    }

    @Override
    public void visit(ForEachStmt n, Void arg) {
        complexity++;
        super.visit(n, arg);
    }

    @Override
    public void visit(SwitchStmt n, Void arg) {
        complexity++;
        super.visit(n, arg);
    }

    public int getComplexity() {
        return complexity;
    }
}