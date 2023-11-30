package codeViz;
import codeViz.entity.EntityType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class CodeVizController {

    private String currentTarget = "./codeviz_backend/target/classes/codeViz/entity";
    private EntityType currentLevel = EntityType.PACKAGE;

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


    public void viewLevel(String targetLevel, EntityType entityType, String searchValue){
        JavaBytecodeReader javaBytecodeReader = new JavaBytecodeReader();
        List<String> filePaths = javaBytecodeReader.getAllFilePaths(targetLevel);
        if (!filePaths.isEmpty()) {
            currentTarget = targetLevel; // valid path, update target
            javaBytecodeReader.generateEntitiesAndConnections(filePaths);

            if (!searchValue.isEmpty()) {
                System.out.println("SEARCHING FOR " + searchValue);
                javaBytecodeReader.getGraphGenerator().performSearch(searchValue);
            }

            javaBytecodeReader.generateGraph(entityType, "./codeviz_frontend/public/codeviz_demo.gexf");
        }
    }

    @CrossOrigin
    @GetMapping("/api/viewGraphLevel")
    public Map<String, String> viewGraphLevel(@RequestParam(name = "level", required = false, defaultValue = "") String level,
                                              @RequestParam(name = "searchValue", required = false, defaultValue = "") String searchValue,
                                              @RequestParam(name = "targetFolder", required = false, defaultValue = "") String targetFolder) {
        Map<String, String> response = new HashMap<>();

        if (!level.isEmpty()) {
            currentLevel = EntityType.valueOf(level);
        }

        if (!targetFolder.isEmpty()) {
            System.out.println("GENERATING FOR "  + currentTarget);
        } else {
            targetFolder = currentTarget;
        }

        viewLevel(targetFolder, currentLevel, searchValue);

        response.put("file", "codeviz_demo.gexf");
        return response; //each API call returns a JSON object that the React app parses
    }

}