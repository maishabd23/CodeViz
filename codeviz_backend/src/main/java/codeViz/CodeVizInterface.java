package codeViz;

import codeViz.entity.Entity;
import codeViz.entity.EntityType;
import codeViz.gitHistory.CommitInfo;
import codeViz.gitHistory.GitCommitReader;

import java.util.ArrayList;

/**
 * CodeViz Interface that connects the CodeViz classes to the CodeVizController
 * Note: This class should not introduce any new behavior, only follow existing behavior set by the CodeViz classes
 */
public class CodeVizInterface {
    private JavaBytecodeReader javaBytecodeReader;
    private GraphGenerator graphGenerator;
    private GitCommitReader gitCommitReader;
    private boolean success;

    private GitHubRepoController gitHubRepoController;

    public CodeVizInterface(){
        this.javaBytecodeReader = new JavaBytecodeReader();
        //this.graphGenerator = javaBytecodeReader.getGraphGenerator();
        this.gitHubRepoController = new GitHubRepoController();
        this.graphGenerator = gitHubRepoController.getGraphGenerator();
        this.gitCommitReader = new GitCommitReader(graphGenerator);
        this.success = true; // FIXME - change back to false once stuff are working

    }

    /**
     * Generate the entities and connections between them AND annotate with local git history
     * @author Thanuja Sivaananthan
     * @param folderName      folder name to get file paths from
     * @return boolean, whether the entity generation was successful
     */
    public void generateEntitiesAndConnections(String folderName, String localDirectory, int maxNumCommits) {
        String repoURl = "https://github.com/maishabd23/online-bookstore";
        gitHubRepoController.analyzeCodebase(gitHubRepoController.retrieveGitHubCodebase(repoURl));
        gitHubRepoController.generateEntitiesAndConnections();
//        boolean success = javaBytecodeReader.generateEntitiesAndConnections(folderName);
//        if (success){
//            gitCommitReader.extractCommitHistory(localDirectory, maxNumCommits);
//        }
//        return success;
    }

    /**
     * Generate the entities and connections between them AND annotate with remote git history
     * @author Thanuja Sivaananthan
     * @param folderName      folder name to get file paths from
     * @return boolean, whether the entity generation was successful
     */
    public boolean generateEntitiesAndConnections(String folderName, String gitHubURI, String tokenPassword, int maxNumCommits) {
        boolean success = javaBytecodeReader.generateEntitiesAndConnections(folderName);
        if (success){
            gitCommitReader.extractCommitHistory(gitHubURI, tokenPassword, maxNumCommits);
        }
        return success;
    }


    public void performSearch(String searchValue, boolean isDetailedSearch) {
        if (success) {
            graphGenerator.performSearch(searchValue, isDetailedSearch);
        }
    }

    public void generateGraph(EntityType entityType, String filename){
        if (success) {
            graphGenerator.directedGraphToGexf(graphGenerator.entitiesToNodes(entityType), filename);
        }
    }

    public String getNodeDetails(String nodeName, EntityType currentLevel) {
        return graphGenerator.getNodeDetails(nodeName, currentLevel);
    }

    public String getSearchResult(EntityType entityType) {
        return graphGenerator.getSearchResult(entityType);
    }

    public void clearSearch() {
        graphGenerator.clearSearch();
    }
}
