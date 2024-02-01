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
    private Entity selectedNode;

    private boolean success;

    public CodeVizInterface(){
        this.javaBytecodeReader = new JavaBytecodeReader();
        this.graphGenerator = javaBytecodeReader.getGraphGenerator();
        this.gitCommitReader = new GitCommitReader(graphGenerator);
        this.success = true; // FIXME - change back to false once stuff are working
        this.selectedNode = null;
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
        boolean canCreateInner = false;
        if (success) {
            if (selectedNode != null){
                System.out.println("Try generating inner graph");
                canCreateInner = generateInnerGraph(entityType, filename);
                if (!canCreateInner){
                    System.out.println("ERROR, GENERATING DEFAULT GRAPH");
                }
            }

            if (selectedNode == null || !canCreateInner){
                selectedNode = null; // couldn't create inner graph, so clear it proactively?
                graphGenerator.directedGraphToGexf(entityType, filename);
            }
        }
    }

    private boolean generateInnerGraph(EntityType currentLevel, String filename){
        boolean canCreateInner = false;
        if (success) {
            canCreateInner = graphGenerator.directedGraphToGexf(selectedNode, currentLevel, filename);

        }
        return canCreateInner;
    }

    public String getSelectedNodeToString() {
        if (selectedNode != null) {
            return selectedNode.getKey();
        } else {
            return "";
        }
    }

    public String getNodeDetails(String nodeName, EntityType currentLevel) {
        selectedNode = graphGenerator.getNode(nodeName, currentLevel);
        return graphGenerator.getNodeDetails(nodeName, currentLevel);
    }

    public String getSearchResult(EntityType entityType) {
        return graphGenerator.getSearchResult(entityType);
    }

    public void clearSearch() {
        graphGenerator.clearSearch();
    }

    public void clearSelectedNode() {
        selectedNode = null;
    }

}
