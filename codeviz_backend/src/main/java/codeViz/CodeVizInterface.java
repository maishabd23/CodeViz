package codeViz;

import codeViz.entity.Entity;
import codeViz.entity.EntityType;
import codeViz.gitHistory.GitCommitReader;

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

    private GitHubRepoController gitHubRepoController;

    public CodeVizInterface(){
        this.javaBytecodeReader = new JavaBytecodeReader();
        //this.graphGenerator = javaBytecodeReader.getGraphGenerator();
        this.gitHubRepoController = new GitHubRepoController();
        this.graphGenerator = gitHubRepoController.getGraphGenerator();
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
    public void generateEntitiesAndConnections(String folderName, String localDirectory, int maxNumCommits) {
        String repoURl = "https://github.com/martinmimigames/little-music-player";
        gitHubRepoController.analyzeCodebase(gitHubRepoController.retrieveGitHubCodebase(repoURl));
        gitHubRepoController.generateEntitiesAndConnections();
        String tokenPassword = ""; // empty string for public repos
        gitCommitReader.extractCommitHistory(repoURl, tokenPassword, maxNumCommits);
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

    /**
     * Generate graph
     *
     * @param newLevel   the level to generate the graph at
     * @param filename   the filename to save the gexf file as
     * @param gitHistory whether viewing git history graph or not
     */
    public void generateGraph(EntityType newLevel, String filename, boolean gitHistory){
        if (success) {
            // if already looking at the inner level of a selected node, keep doing that
            if (selectedNode != null && selectedNode.getEntityType().getChild().equals(newLevel)){
                graphGenerator.directedGraphToGexf(selectedNode, newLevel, filename, gitHistory);
            } else {
                selectedNode = null;
                graphGenerator.directedGraphToGexf(newLevel, filename, gitHistory);
            }
        }
    }

    /**
     * Generate graph filtered to a specific node
     * @param nodeName          the name of the node to filter the graph at
     * @param parentLevel       the level of the node
     * @param childLevel        the inner level to generate the graph for
     * @param filename          the filename to save the gexf file as
     */
    public void generateInnerGraph(String nodeName, EntityType parentLevel, EntityType childLevel, String filename, boolean gitHistory){
        selectedNode = graphGenerator.getNode(nodeName, parentLevel);
        if (success) {
            graphGenerator.directedGraphToGexf(selectedNode, childLevel, filename, gitHistory);
        }
    }

    public String getSelectedNodeToString() {
        if (selectedNode != null) {
            return selectedNode.getKey();
        } else {
            return "";
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

    public void clearSelectedNode() {
        selectedNode = null;
    }

    public String getEdgeDetails(String edgeName) {
        return graphGenerator.getEdgeDetails(edgeName);
    }
}
