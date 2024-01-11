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

    private String currentTarget = "./codeviz_backend/target/classes/codeViz/entity";
    private EntityType currentLevel = EntityType.CLASS;

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


    public void viewLevel(String targetLevel, EntityType entityType, String searchValue, boolean detailed){
        JavaBytecodeReader javaBytecodeReader = new JavaBytecodeReader();
        boolean success = javaBytecodeReader.generateEntitiesAndConnections(targetLevel);
        if (success) {
            currentTarget = targetLevel; // only if it's a valid path, update target

            if (!searchValue.isEmpty()) {
                System.out.println("SEARCHING FOR " + searchValue);
                javaBytecodeReader.getGraphGenerator().performSearch(searchValue, detailed);
            }

            javaBytecodeReader.generateGraph(entityType, "./codeviz_frontend/public/codeviz_demo.gexf");
        }
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

        if (!targetFolder.isEmpty()) {
            System.out.println("GENERATING FOR "  + targetFolder); // TODO - allow user to enter own path / github url
        } else {
            targetFolder = currentTarget;
        }

        viewLevel(targetFolder, currentLevel, searchValue, detailed);

        response.put("file", "codeviz_demo.gexf");
        return response; //each API call returns a JSON object that the React app parses
    }

}