package codeViz.gitHistory;

import java.util.ArrayList;

public class GitDiffCorrelationAnalysis {
    private final ArrayList<Integer> classDiffsX, classDiffsY;

    public GitDiffCorrelationAnalysis(){
        this.classDiffsX = new ArrayList<>();
        this.classDiffsY = new ArrayList<>();
    }

    public void addPair(int amountX, int amountY){
        classDiffsX.add(amountX);
        classDiffsY.add(amountY);
    }

    public float getCorrelationCoefficient(){
        return correlationCoefficient(classDiffsX, classDiffsY, classDiffsX.size());
    }

    /**
     * Source: https://www.geeksforgeeks.org/program-find-correlation-coefficient/
     * @param X     list of x values
     * @param Y     list of y values
     * @param n     number of elements
     * @return      correlation coefficient
     */
    private static float correlationCoefficient(ArrayList<Integer> X, ArrayList<Integer> Y, int n)
    {

        if (n<3){
            System.out.println("NOTE: not enough data points, returning 0");
            return 0;
        }

        int sum_X = 0, sum_Y = 0, sum_XY = 0;
        int squareSum_X = 0, squareSum_Y = 0;

        for (int i = 0; i < n; i++)
        {
            int amountX = X.get(i);
            int amountY = Y.get(i);

            // sum of elements of array X.
            sum_X = sum_X + amountX;

            // sum of elements of array Y.
            sum_Y = sum_Y + amountY;

            // sum of  amountX * amountY.
            sum_XY = sum_XY + amountX * amountY;

            // sum of square of array elements.
            squareSum_X = squareSum_X + amountX * amountX;
            squareSum_Y = squareSum_Y + amountY * amountY;
        }

        // use formula for calculating correlation
        // coefficient.
        float numerator = (float)(n * sum_XY - sum_X * sum_Y);

        float denominatorX = (float) (n * squareSum_X - sum_X * sum_X);
        float denominatorY = (float) (n * squareSum_Y - sum_Y * sum_Y);
        float denominator = (float) Math.sqrt(denominatorX * denominatorY);
        float corr = numerator / denominator;

        if (denominator == 0){
            System.out.println("\nNaN ERROR"); //happens when n == 1 or 2 (not enough data)
            System.out.println("n: " + n);
            System.out.println("X: " + X);
            System.out.println("Y: " + Y);
            System.out.println("squareSum_X: " + squareSum_X);
            System.out.println("sum_X: " + sum_X);
            System.out.println("squareSum_Y: " + squareSum_Y);
            System.out.println("sum_Y: " + sum_Y);
            System.out.println("numerator: " + numerator);
            System.out.println("denominatorX: " + denominatorX);
            System.out.println("denominatorY: " + denominatorY);
            return 0;
        }
        return corr;
    }

}
