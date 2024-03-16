package codeViz.codeComplexity;

public class ClassComplexityDetails extends ComplexityDetails {
    public ClassComplexityDetails() {
        super();
    }

    public void incrementCyclomaticComplexity(int cyclomaticComplexity) {
        super.setCyclomaticComplexity(getCyclomaticComplexity() + cyclomaticComplexity);
    }
}
