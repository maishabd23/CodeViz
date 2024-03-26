package codeViz;
import codeViz.entity.EntityType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.*;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.view.RedirectView;

import java.io.FileWriter;
import java.io.IOException;

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
    private final String clientId;
    private final String clientSecret;
    private final String redirect_uri;


    public CodeVizController() {
        this.codeVizInterface = new CodeVizInterface();
        this.success = true; // Change to false after target can be chosen
        this.gitHistory = false;

        // create empty graph on start-up
        codeVizInterface.generateGraph(currentLevel, GEXF_FILE, gitHistory);

        Dotenv dotenv = Dotenv.configure().load();
        this.clientId = dotenv.get("CLIENT_ID");
        this.clientSecret = dotenv.get("CLIENT_SECRET");
        this.redirect_uri = dotenv.get("REDIRECT_URI");
    }

    /**
     * @author mei
     * @return link to github login/authorize page
     */
    @CrossOrigin
    @GetMapping("/authorize")
    public String login() {
        System.out.println("DEBUG: In /authorize endpoint with clientID " + clientId);
        return "https://github.com/login/oauth/authorize?client_id=" + clientId;
    }

    /**
     * @author mei
     * @param code github code
     * @return access token
     */
    @CrossOrigin
    @GetMapping("/github/callback")
    public RedirectView handleGitHubCallback(@RequestParam("code") String code) {
        System.out.println("DEBUG: In callback endpoint with code " + code);
        String accessToken = exchangeCodeForAccessToken(code);

        writeToEnvFile("ACCESS_TOKEN", accessToken);
        return new RedirectView("http://localhost:3000/");
    }

    public static void writeToEnvFile(String key, String value) {
        try (FileWriter writer = new FileWriter(".env", true)) {
            writer.write(key + "=" + value + "\n");
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }
    }

    /**
     * @author mei
     * @param code github code
     * @return access token
     */
    private String exchangeCodeForAccessToken(String code) {
        String tokenUrl = "https://github.com/login/oauth/access_token";

        // Construct the token request body
        String requestBody = "client_id=" + clientId + "&client_secret=" + clientSecret + "&code=" + code + "&redirect_uri=" + redirect_uri;

        // Set headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // Create request entity
        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

        // Send POST request to exchange code for access token
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(tokenUrl, HttpMethod.POST, requestEntity, String.class);

        // Parse response to extract access token
        // Note: This is a basic parsing example. You should handle error cases and parse the response according to GitHub's documentation.
        String accessToken = response.getBody().split("&")[0].split("=")[1];
        System.out.println("DEBUG: Access token: " + accessToken);

        // Make request to GitHub API to get user's repositories
        String repoUrl = "https://api.github.com/user/repos";
        HttpHeaders authHeaders = new HttpHeaders();
        authHeaders.setBearerAuth(accessToken);
        HttpEntity<String> repoRequestEntity = new HttpEntity<>(null, authHeaders);
        ResponseEntity<String> repoResponse = restTemplate.exchange(repoUrl, HttpMethod.GET, repoRequestEntity, String.class);

// Print out private repositories
        if (repoResponse.getStatusCode() == HttpStatus.OK) {
            String repoData = repoResponse.getBody();
            System.out.println("User's Private Repositories:");

            // Parse JSON response to extract repository details
            ObjectMapper mapper = new ObjectMapper();
            try {
                JsonNode rootNode = mapper.readTree(repoData);
                for (JsonNode repoNode : rootNode) {
                    boolean isPrivate = repoNode.get("private").asBoolean();
                    if (isPrivate) {
                        String repoName = repoNode.get("name").asText();
                        System.out.println(repoName);
                        // You can print more details if needed
                        // For example: System.out.println(repoNode);
                    }
                    System.out.println(repoNode.get("name").asText());
                }
            } catch (IOException e) {
                System.err.println("Error parsing repository data: " + e.getMessage());
            }
        } else {
            System.err.println("Failed to retrieve user's repositories. Status code: " + repoResponse.getStatusCodeValue());
        }

        return accessToken;
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
        responseBody.put("isSuccessful", "true");

        System.out.println("THE REPO URL WAS SENT TO BACKEND " + repoURL);

        // Call generateEntitiesAndConnections method with repoURL
        codeVizInterface.generateEntitiesAndConnections(repoURL, 50);
        codeVizInterface.generateGraph(currentLevel, GEXF_FILE, gitHistory);

        this.isDisplayingGraph = true;
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
     * @param detailed          whether the search is detailed or not
     * @return                  string response, message of the search result
     */
    @CrossOrigin
    @GetMapping("/api/searchGraph")
    public Map<String, String> searchGraph(@RequestParam(name = "searchValue", required = false, defaultValue = "") String searchValue,
                                              @RequestParam(name = "detailed", required = false, defaultValue = "false") boolean detailed)
    {
        Map<String, String> response = new HashMap<>();

        if (success) {
            if (!searchValue.isEmpty()) {
                System.out.println("SEARCHING FOR " + searchValue);
                codeVizInterface.performSearch(searchValue, detailed);
            }
            codeVizInterface.generateGraph(currentLevel, GEXF_FILE, this.gitHistory);
        }

        String result = codeVizInterface.getSearchResult(currentLevel);
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