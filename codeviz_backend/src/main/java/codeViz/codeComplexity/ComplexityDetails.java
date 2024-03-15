package codeViz.codeComplexity;

public class ComplexityDetails {

    private int linesOfCode;
    private int cyclomaticComplexity;

    public void setLinesOfCode(int linesOfCode) {
        this.linesOfCode = linesOfCode;
    }

    public int getLinesOfCode() {
        return linesOfCode;
    }

    public void setCyclomaticComplexity(int cyclomaticComplexity) {
        this.cyclomaticComplexity = cyclomaticComplexity;
    }

    public int getCyclomaticComplexity() {
        return cyclomaticComplexity;
    }
}
