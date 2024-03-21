package codeViz;
import codeViz.entity.EntityType;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
public class CodeVizController {

    private EntityType currentLevel = EntityType.CLASS;
    private static final String GEXF_FILE = "./codeviz_frontend/public/codeviz_demo.gexf";

    private final CodeVizInterface codeVizInterface;
    private boolean success;
    private boolean gitHistory;

    private boolean isDisplayingGraph = false;

    public CodeVizController() {
        this.codeVizInterface = new CodeVizInterface();
        this.success = true; // Change to false after target can be chosen
        this.gitHistory = false;

        // create empty graph on start-up
        codeVizInterface.generateGraph(currentLevel, GEXF_FILE, gitHistory);
    }

    /**
     * Input a new repo url
     * @author Maisha Abdullah
     * @param requestBody   request body
     * @return              response, whether it was successful
     */
    @CrossOrigin
    @PostMapping("/init")
    public Map<String, String> initController(@RequestBody Map<String, String> requestBody) {
        String repoURL = requestBody.get("repoURL");
        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("ok", "true");

        System.out.println("THE REPO URL WAS SENT TO BACKEND " + repoURL);

        repoURL = codeVizInterface.modifyRepoUrl(repoURL);

        // Call generateEntitiesAndConnections method with repoURL
        String repoUrlError = codeVizInterface.isValidRepoUrl(repoURL);
        if (repoUrlError.isEmpty()) {
            if (codeVizInterface.generateEntitiesAndConnections(repoURL, 50)) {
                codeVizInterface.generateGraph(currentLevel, GEXF_FILE, gitHistory);

                this.isDisplayingGraph = true;
            } else {
                repoUrlError = "ERROR, not a valid Java project";
            }
        }

        if (!repoUrlError.isEmpty()){
            repoUrlError = TextAnnotate.javaToHtml(repoUrlError);
            responseBody.put("ok", "false");
            responseBody.put("error", repoUrlError);
        }

        return responseBody;
    }

    @GetMapping("/")
    public String index() {
        return "Welcome to the CodeViz API";
    }

    @CrossOrigin
    @GetMapping("/api/isDisplayingGraph")
    public Map<String, String> isDisplayingGraph() {
        Map<String, String> response = new HashMap<>();
        response.put("isSuccessful", String.valueOf(isDisplayingGraph));
        return response; //each API call returns a JSON object that the React app parses
    }

    @CrossOrigin
    @GetMapping("/api/displayGraph")
    public Map<String, String> hello() {
        Map<String, String> response = new HashMap<>();
        response.put("file", "src/test/gexf/class_gephi.gexf");
        return response; //each API call returns a JSON object that the React app parses
    }


    /**
     * Select the level of the graph to view at
     * Returns the default code graph at the given level (no annotations, inner graphs, etc.)
     * @param level             either the PACKAGE, CLASS, or METHOD level
     * @return                  file response, name of gexf file
     */
    @CrossOrigin
    @GetMapping("/api/viewGraphLevel")
    public Map<String, String> viewGraphLevel(@RequestParam(name = "level", required = false, defaultValue = "") String level)
    {
        Map<String, String> response = new HashMap<>();

        if (!level.isEmpty()) {
            currentLevel = EntityType.valueOf(level);
        }

        if (success) {

            codeVizInterface.clearSelectedNode(); // clicking the level buttons will clear any filters
            // TODO - also clear searches? (could be useful to keep search results when viewing other levels)
            gitHistory = false; // default git history is false
            codeVizInterface.generateGraph(currentLevel, GEXF_FILE, gitHistory);
        }

        response.put("file", "codeviz_demo.gexf");
        return response; //each API call returns a JSON object that the React app parses
    }

    @CrossOrigin
    @GetMapping("/api/annotateGraph")
    public Map<String, String> annotateGraph(@RequestParam(name = "gitHistory", required = false, defaultValue = "false") boolean gitHistory)
    {
        Map<String, String> response = new HashMap<>();

        if (success) {
            this.gitHistory = gitHistory;
            System.out.println("git history: " + gitHistory);
            codeVizInterface.generateGraph(currentLevel, GEXF_FILE, gitHistory);
        }

        response.put("file", "codeviz_demo.gexf");
        return response; //each API call returns a JSON object that the React app parses
    }


    /**
     * Search at the current level of the graph
     * Updates the displayed code graph
     * @param searchValue       the search value
     * @return                  string response, message of the search result
     */
    @CrossOrigin
    @PostMapping("/api/searchGraph")
    public Map<String, String> searchGraph(@RequestBody Map<String, String> requestBody
    ) {
        String searchValue;
        boolean searchClasses, searchMethods, searchAttributes, searchParameters, searchReturnType, searchConnections;

        searchValue = requestBody.getOrDefault("value", "");
        searchClasses = Boolean.parseBoolean(requestBody.getOrDefault("searchClasses", "false"));
        searchMethods = Boolean.parseBoolean(requestBody.getOrDefault("searchMethods", "false"));
        searchAttributes = Boolean.parseBoolean(requestBody.getOrDefault("searchAttributes", "false"));
        searchParameters = Boolean.parseBoolean(requestBody.getOrDefault("searchParameters", "false"));
        searchReturnType = Boolean.parseBoolean(requestBody.getOrDefault("searchReturnType", "false"));
        searchConnections = Boolean.parseBoolean(requestBody.getOrDefault("searchConnections", "false"));

        Map<String, String> response = new HashMap<>();

        if (success) {
            if (!searchValue.isEmpty()) {
                System.out.println("SEARCHING FOR " + searchValue);
                codeVizInterface.performSearch(searchValue, searchClasses, searchMethods, searchAttributes,
                        searchParameters, searchReturnType, searchConnections, currentLevel);
            }
            codeVizInterface.generateGraph(currentLevel, GEXF_FILE, this.gitHistory);
        }

        String result = codeVizInterface.getSearchResult(currentLevel);
        System.out.println("Search Result: " + result);

        result = TextAnnotate.javaToHtml(result);
        response.put("string", result);
        return response;
    }

    /**
     * Get details of a highlighted node
     * @param nodeName      name of the highlighted node
     * @return              string response of the node details
     */
    @CrossOrigin
    @GetMapping("/api/getNodeDetails")
    public Map<String, String> getNodeDetails(@RequestParam(name = "nodeName", defaultValue = "") String nodeName) {
        Map<String, String> response = new HashMap<>();

        String results = codeVizInterface.getNodeDetails(nodeName, currentLevel);
        results = TextAnnotate.javaToHtml(results);

        response.put("string", results);
        return response; //each API call returns a JSON object that the React app parses
    }

    /**
     * Get complexity details of a highlighted node
     * @param nodeName      name of the highlighted node
     * @return              string response of the complexity details
     */
    @CrossOrigin
    @GetMapping("/api/getComplexityDetails")
    public Map<String, String> getComplexityDetails(@RequestParam(name = "nodeName", defaultValue = "") String nodeName) {
        Map<String, String> response = new HashMap<>();

        String results = codeVizInterface.getComplexityDetails(nodeName, currentLevel);
        results = TextAnnotate.javaToHtml(results);

        response.put("string", results);
        return response; //each API call returns a JSON object that the React app parses
    }

    /**
     * Get the details of an edge
     * Note: Only works for git history annotations
     * @param edgeName  name of the edge
     * @return          string response of the edge details
     */
    @CrossOrigin
    @GetMapping("/api/getEdgeDetails")
    public Map<String, String> getEdgeDetails(@RequestParam(name = "edgeName", defaultValue = "") String edgeName) {
        Map<String, String> response = new HashMap<>();

        if (!gitHistory){
            response.put("string", "");
            return response;
        }

        String results = codeVizInterface.getEdgeDetails(edgeName);
        results = TextAnnotate.javaToHtml(results);

        response.put("string", results);
        return response; //each API call returns a JSON object that the React app parses
    }

    /**
     * Generate an inner graph for the selected node
     * Note: Only works when the selected node is at the PACKAGE or CLASS level
     * @param nodeName   the selected node
     */
    @CrossOrigin
    @GetMapping("/api/generateInnerGraph")
    public void generateInnerGraph(@RequestParam(name = "nodeName", defaultValue = "") String nodeName) {
        // go inside one level (if possible)
        if (currentLevel.getChild() != null){
            EntityType newLevel = currentLevel.getChild();
            System.out.println("Generate inner graph for " + nodeName + " at " + currentLevel);
            gitHistory = false; // default git history is false
            codeVizInterface.generateInnerGraph(nodeName, currentLevel, newLevel, GEXF_FILE, gitHistory);
            currentLevel = newLevel;
        }
    }

    /**
     * Get the current level of the code graph
     * @return  string response, current level
     */
    @CrossOrigin
    @GetMapping("/api/getCurrentLevel")
    public Map<String, String> getCurrentLevel() {
        Map<String, String> response = new HashMap<>();

        String currentLevelString = currentLevel.getName();
        response.put("string", currentLevelString);
        return response;
    }

    /**
     * Get the current level of the code graph + any other annotation details
     * @return  string response, current level + any other annotation details
     */
    @CrossOrigin
    @GetMapping("/api/getCurrentGraphName")
    public Map<String, String> getCurrentGraphName() {
        Map<String, String> response = new HashMap<>();

        String currentLevelString = currentLevel.getName();

        String selectedNodeName = codeVizInterface.getSelectedNodeToString();
        if (!selectedNodeName.isEmpty()) {
            currentLevelString += " at " + selectedNodeName;
        }

        response.put("string", currentLevelString);
        return response;
    }

    /**
     * Get the current annotation details of the code graph
     * @return  string response, annotation details
     */
    @CrossOrigin
    @GetMapping("/api/getCurrentMilestone")
    public Map<String, String> getCurrentMilestone() {
        Map<String, String> response = new HashMap<>();
        String milestone;
        if (!gitHistory){
            milestone = "m1"; // code dependency graph
        } else {
            milestone = "m2"; // git history annotations at class level
        }
        response.put("string", milestone);

        return response;
    }

    @CrossOrigin
    @GetMapping("/api/clearSearch")
    public void clearSearch() {
        codeVizInterface.clearSearch();

        // update code graph without search value
        codeVizInterface.generateGraph(currentLevel, GEXF_FILE, gitHistory);
    }
}