package codeViz;

import codeViz.entity.Entity;
import codeViz.entity.EntityType;
import codeViz.gitHistory.CommitInfo;
import codeViz.gitHistory.GitCommitReader;

import java.util.ArrayList;

public class CodeVizInterface {
    private JavaBytecodeReader javaBytecodeReader;
    private GraphGenerator graphGenerator;
    private GitCommitReader gitCommitReader;
    private boolean success;

    public CodeVizInterface(){
        this.javaBytecodeReader = new JavaBytecodeReader();
        this.graphGenerator = javaBytecodeReader.getGraphGenerator();
        this.gitCommitReader = new GitCommitReader(graphGenerator);
        this.success = false;
    }

    /**
     * Generate the entities and connections between them AND annotate with local git history
     * @author Thanuja Sivaananthan
     * @param folderName      folder name to get file paths from
     * @return boolean, whether the entity generation was successful
     */
    public boolean generateEntitiesAndConnections(String folderName, String localDirectory, int maxNumCommits) {
        boolean success = javaBytecodeReader.generateEntitiesAndConnections(folderName);
        if (success){
            gitCommitReader.extractCommitHistory(localDirectory, maxNumCommits);
        }
        return success;
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
}
