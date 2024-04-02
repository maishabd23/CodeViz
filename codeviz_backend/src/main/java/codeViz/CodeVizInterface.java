package codeViz;

import codeViz.entity.Entity;
import codeViz.entity.EntityType;
import codeViz.gitHistory.GitCommitReader;

/**
 * CodeViz Interface that connects the CodeViz classes to the CodeVizController
 * Note: This class should not introduce any new behavior, only follow existing behavior set by the CodeViz classes
 */
public class CodeVizInterface {
    private final GraphGenerator graphGenerator;
    private final GitCommitReader gitCommitReader;
    private final GitHubRepoController gitHubRepoController;

    private Entity selectedNode;
    private boolean success;


    public CodeVizInterface(){
        this.gitHubRepoController = new GitHubRepoController();
        this.graphGenerator = gitHubRepoController.getGraphGenerator();
        this.gitCommitReader = new GitCommitReader(graphGenerator);
        this.success = true; // FIXME - change back to false once stuff are working
        this.selectedNode = null;
    }

    /**
     * Generate the entities and connections between them AND annotate with local git history
     * @author Thanuja Sivaananthan
     * @author Maisha Abdullah
     * @return boolean, whether entities/connections were created
     */
    public String generateEntitiesAndConnections(String repoURL, int maxNumCommits) {

        repoURL = modifyRepoUrl(repoURL);

        // initial error checking, before generating entities/connections
        String errorMessage = gitHubRepoController.isValidRepoUrl(repoURL);
        if (!errorMessage.isEmpty()){
            return errorMessage;
        }

        //String repoURl = "https://github.com/martinmimigames/little-music-player";
        System.out.println("THE REPO URL WAS SENT TO BACKEND IN CODE VIZ INTERFACE " + repoURL);
        graphGenerator.clearEntities(); //  making a new graph, clear all entities
        boolean success = gitHubRepoController.analyzeCodebase(repoURL);
        if (success) {
            String tokenPassword = ""; // empty string for public repos
            gitCommitReader.extractCommitHistory(repoURL, tokenPassword, maxNumCommits);
            return "";
        } else {
            return "ERROR, not a valid Java project";
        }
    }

    private String modifyRepoUrl(String repoURL) {
        if (repoURL.endsWith(".git")){
            repoURL = repoURL.replace(".git", "");
        }
        return repoURL;
    }


    /*
    public void performSearch(String searchValue, boolean isDetailedSearch) {
        if (success) {
            graphGenerator.performSearch(searchValue, isDetailedSearch);
        }
    }
*/
    public void performSearch(String searchValue, boolean searchClasses, boolean searchMethods, boolean searchAttributes,
                              boolean searchParameters, boolean searchReturnType, boolean searchConnections, EntityType currentLevel) {
        if (success) {
            graphGenerator.performSearch(searchValue, searchClasses, searchMethods, searchAttributes,
                    searchParameters, searchReturnType, searchConnections, currentLevel);
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
            return selectedNode.getName();
        } else {
            return "";
        }
    }

    public String getNodeDetails(String nodeName, EntityType currentLevel) {
        return graphGenerator.getNodeDetails(nodeName, currentLevel);
    }

    public String getComplexityDetails(String nodeName, EntityType currentLevel) {
        return graphGenerator.getComplexityDetails(nodeName, currentLevel);
    }

    public String getSearchResult() {
        return graphGenerator.getSearchResult();
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
