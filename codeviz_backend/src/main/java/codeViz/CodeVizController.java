package codeViz;
import codeViz.entity.EntityType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class CodeVizController {

    private static final String currentSrc = "./"; // weird things happen in intelliJ's project/vcs if I try setting this as anything else like ./codeviz_backend, ./codeviz_backend/src, etc
    private String currentTarget = "./codeviz_backend/target/classes/codeViz/entity";
    private EntityType currentLevel = EntityType.CLASS;

    private CodeVizInterface codeVizInterface;
    private boolean success;
    public CodeVizController(){
        this.codeVizInterface = new CodeVizInterface();
        this.success = false;

        // TODO - only call this method when a new target is chosen
        success = codeVizInterface.generateEntitiesAndConnections(currentTarget, currentSrc, 10);
    }

    @GetMapping("/")
    public String index() {
        return "Welcome to the CodeViz API";
    }

    @CrossOrigin
    @GetMapping("/api/displayGraph")
    public Map<String, String> hello() {
        Map<String, String> response = new HashMap<>();
        response.put("file", "src/test/gexf/class_gephi.gexf");
        return response; //each API call returns a JSON object that the React app parses
    }


    @CrossOrigin
    @GetMapping("/api/viewGraphLevel")
    public Map<String, String> viewGraphLevel(@RequestParam(name = "level", required = false, defaultValue = "") String level,
                                              @RequestParam(name = "searchValue", required = false, defaultValue = "") String searchValue,
                                              @RequestParam(name = "targetFolder", required = false, defaultValue = "") String targetFolder,
                                              @RequestParam(name = "detailed", required = false, defaultValue = "false") boolean detailed) {
        Map<String, String> response = new HashMap<>();

        if (!level.isEmpty()) {
            currentLevel = EntityType.valueOf(level);
        }

        if (!targetFolder.isEmpty() && !targetFolder.equals(currentTarget)){
            System.out.println("GENERATING FOR "  + targetFolder); // TODO - allow user to enter own path / github url
            success = codeVizInterface.generateEntitiesAndConnections(targetFolder, currentSrc, 10);
        }

        if (success) {
            if (!targetFolder.isEmpty()) {
                currentTarget = targetFolder; // only if it's a valid non-empty path, update target
            }

            if (!searchValue.isEmpty()) {
                System.out.println("SEARCHING FOR " + searchValue);
                codeVizInterface.performSearch(searchValue, detailed);
            }
            codeVizInterface.generateGraph(currentLevel, "./codeviz_frontend/public/codeviz_demo.gexf");
        }

        response.put("file", "codeviz_demo.gexf");
        return response; //each API call returns a JSON object that the React app parses
    }

}