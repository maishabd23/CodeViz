import codeViz.GraphGenerator;
import codeViz.entity.ClassEntity;
import codeViz.entity.PackageEntity;
import codeViz.gitHistory.CommitInfo;
import codeViz.gitHistory.GitCommitReader;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.*;


/**
 * Test git history connections with a sample public gitHub repo
 * @author Thanuja Sivaananthan
 */
public class GitHistoryTest {

    // either the uri or ul should work
    // use own fork to have a stable version
    // private static final String github_uri = "https://github.com/thanujasiva/Simple-Java-Calculator.git";
    private static final String github_url = "https://github.com/thanujasiva/Simple-Java-Calculator";

    private static ClassEntity bufferedImageCustom, calculator, simpleJavaCalculator, ui, calculatorTest;
    private static GitCommitReader gitCommitReader;

    @BeforeAll
    public static void setupDependencyConnections() {

        // create entities
        PackageEntity calculatorPackage, calculatorPackageTest;
        calculatorPackage = new PackageEntity("simplejavacalculator");
        bufferedImageCustom = new ClassEntity("BufferedImageCustom", calculatorPackage);
        calculator = new ClassEntity("Calculator", calculatorPackage);
        simpleJavaCalculator = new ClassEntity("SimpleJavaCalculator", calculatorPackage);
        ui = new ClassEntity("UI", calculatorPackage);
        calculatorPackageTest = new PackageEntity("simplejavacalculatorTest");
        calculatorTest = new ClassEntity("CalculatorTest", calculatorPackageTest);

        // add entities to graph generator
        GraphGenerator graphGenerator = new GraphGenerator();
        graphGenerator.addEntity(calculatorPackage.getKey(), calculatorPackage);
        graphGenerator.addEntity(bufferedImageCustom.getKey(), bufferedImageCustom);
        graphGenerator.addEntity(calculator.getKey(), calculator);
        graphGenerator.addEntity(simpleJavaCalculator.getKey(), simpleJavaCalculator);
        graphGenerator.addEntity(ui.getKey(), ui);
        graphGenerator.addEntity(calculatorPackageTest.getKey(), calculatorPackageTest);
        graphGenerator.addEntity(calculatorTest.getKey(), calculatorTest);

        // create connections
        ui.addConnectedEntity(calculator);
        ui.addConnectedEntity(bufferedImageCustom);
        calculatorTest.addConnectedEntity(calculator);

        // git commits / git history
        gitCommitReader = new GitCommitReader(graphGenerator);
        gitCommitReader.extractCommitHistory(github_url, "", -1); // public repo, don't need tokenPassword
    }

    /**
     * Minimal test to check that each class contains commits
     */
    @Test
    public void testGitCommitStorages(){
        // the list of commits should not be empty
        assertNotEquals(0, bufferedImageCustom.getCommitInfos().size());
        assertNotEquals(0, calculator.getCommitInfos().size());
        assertNotEquals(0, simpleJavaCalculator.getCommitInfos().size());
        assertNotEquals(0, ui.getCommitInfos().size());
        assertNotEquals(0, calculatorTest.getCommitInfos().size());

        System.out.println("bufferedImageCustom: " + bufferedImageCustom.getCommitInfos().size());
        System.out.println("calculator: " + calculator.getCommitInfos().size());
        System.out.println("simpleJavaCalculator: " + simpleJavaCalculator.getCommitInfos().size());
        System.out.println("ui: " + ui.getCommitInfos().size());
        System.out.println("calculatorTest: " + calculatorTest.getCommitInfos().size());

        // TODO - check the commit / class contains each other
        assertTrue(4 <= bufferedImageCustom.getCommitInfos().size());
        assertTrue(16 <= calculator.getCommitInfos().size());
//        assertTrue((3+1) <= simpleJavaCalculator.getCommitInfos().size());
        assertTrue((31+2) <= ui.getCommitInfos().size());
        assertTrue(2 <= calculatorTest.getCommitInfos().size());
    }

    /**
     * Check that renamed classes are as expected
     */
    @Test
    public void testRenames(){
        LinkedHashMap<String, String> renamedClasses = gitCommitReader.getRenamedClassEntityNames();
        assertNotNull(renamedClasses);

        // classes that were renamed
        assertTrue(renamedClasses.containsValue("src/simplejavacalculator/SimpleJavaCalculator.java"));
        assertTrue(renamedClasses.containsValue("src/simplejavacalculator/UI.java"));

        // pairs: key = old name, value = new name
        assertEquals("src/simplejavacalculator/SimpleJavaCalculator.java", renamedClasses.get("Calculator/src/ph/calculator/Main.java"));
        assertEquals("src/simplejavacalculator/UI.java", renamedClasses.get("Calculator/src/ph/calculator/UI.java"));

        // TODO test that the renames actually work
    }

    /**
     * Test the git history connections
     */
    @Test
    public void testGitConnections(){
        // class pairs that are connected via dependency graph
        assertTrue(ui.getConnectedEntities().contains(calculator));
        assertTrue(ui.getConnectedEntities().contains(bufferedImageCustom));
        assertTrue(calculatorTest.getConnectedEntities().contains(calculator));

        // connected with 59936e9facf0b737ee1ff72abd3253f0887c07b8, etc
        assertTrue(ui.getGitConnectedEntitiesAndWeights().containsKey(calculator));

        // connected with c72e1476798406b685e37ea08be37c39f811e5f2.
        assertTrue(calculatorTest.getGitConnectedEntitiesAndWeights().containsKey(calculator));

        // no git connections (BufferedImageCustom has few connections)
        assertFalse(ui.getGitConnectedEntitiesAndWeights().containsKey(bufferedImageCustom));

        // TODO - test association rule mining
    }
}
