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
        graphGenerator.addEntity(calculatorPackage.getName(), calculatorPackage);
        graphGenerator.addEntity(calculatorPackageTest.getName(), calculatorPackageTest);

        graphGenerator.addEntity(bufferedImageCustom.getName(), bufferedImageCustom);
        graphGenerator.addEntity(calculator.getName(), calculator);
        graphGenerator.addEntity(simpleJavaCalculator.getName(), simpleJavaCalculator);
        graphGenerator.addEntity(ui.getName(), ui);
        graphGenerator.addEntity(calculatorTest.getName(), calculatorTest);

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

        // there may be more commmits (ex. merge commits)
        assertTrue(4 <= bufferedImageCustom.getCommitInfos().size());
        assertTrue(16 <= calculator.getCommitInfos().size());
        assertTrue((3+1) <= simpleJavaCalculator.getCommitInfos().size());
        assertTrue((31+2) <= ui.getCommitInfos().size());
        assertTrue(2 <= calculatorTest.getCommitInfos().size());

        // check most recent commits in simpleJavaCalculator
        CommitInfo simpleCalculatorCommit = simpleJavaCalculator.getCommitInfos().get(0);
        assertEquals("24530f4376ce1f8b325cc501805ab3b28e586fb4", simpleCalculatorCommit.getId());
        assertEquals("Update SimpleJavaCalculator.java", simpleCalculatorCommit.getMessage());
        assertEquals(1, simpleCalculatorCommit.getClasses().size());
        assertTrue(simpleCalculatorCommit.getClasses().contains(simpleJavaCalculator));

        simpleCalculatorCommit = simpleJavaCalculator.getCommitInfos().get(1);
        assertEquals("ed0fc7c381b4bffbfd474b0c0a34538ba62434e1", simpleCalculatorCommit.getId());
        assertEquals("Fix indentation", simpleCalculatorCommit.getMessage());
        assertEquals(3, simpleCalculatorCommit.getClasses().size());
        assertTrue(simpleCalculatorCommit.getClasses().contains(calculator));
        assertTrue(simpleCalculatorCommit.getClasses().contains(simpleJavaCalculator));
        assertTrue(simpleCalculatorCommit.getClasses().contains(ui));

        simpleCalculatorCommit = simpleJavaCalculator.getCommitInfos().get(2);
        assertEquals("4803a27a370a32f50d4ea628b7bb7540778d8c77", simpleCalculatorCommit.getId());
        assertEquals("Project was migrated to Netbeans", simpleCalculatorCommit.getMessage());
        assertEquals(2, simpleCalculatorCommit.getClasses().size());
        assertTrue(simpleCalculatorCommit.getClasses().contains(simpleJavaCalculator));
        assertTrue(simpleCalculatorCommit.getClasses().contains(ui));

        // check the initial commit (also checks that the package/class renames were stored with the correct classes)
        CommitInfo initialCommit = simpleJavaCalculator.getCommitInfos().get(simpleJavaCalculator.getCommitInfos().size()-1);
        assertEquals("d1396a2d26495ce900c516042607c1f3031dd4c4", initialCommit.getId());
        assertEquals("Initial Commit", initialCommit.getMessage());
        assertEquals(2, initialCommit.getClasses().size());
        assertTrue(initialCommit.getClasses().contains(simpleJavaCalculator));
        assertTrue(initialCommit.getClasses().contains(ui));
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
    }
}
