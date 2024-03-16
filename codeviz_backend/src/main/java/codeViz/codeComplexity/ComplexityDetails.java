package codeViz.codeComplexity;

import codeViz.entity.Entity;

public class ComplexityDetails {

    private Entity entity;
    private int cyclomaticComplexity;
    private int linesOfCode;

    public ComplexityDetails(){
        this.cyclomaticComplexity = 0;
        this.linesOfCode = 0;
    }

    public void setEntity(Entity entity) {
        this.entity = entity;
    }

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

    /**
     * @return              string value
     */
    public static String metricToString(String metricName, int metric){
        return metricName + ": " + metric + "\n";
    }

    @Override
    public String toString() {
        return entity.titleToString() +
                metricToString("Cyclomatic Complexity", cyclomaticComplexity) +
                metricToString("Lines of Code", linesOfCode) ;
    }
}
