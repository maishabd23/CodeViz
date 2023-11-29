package codeViz;
import codeViz.entity.EntityType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class SampleController {

    private final String folderPath = "./codeviz_backend/target/classes/codeViz/entity";

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


    public void viewLevel(EntityType entityType){
        JavaBytecodeReader javaBytecodeReader = new JavaBytecodeReader();
        javaBytecodeReader.generateEntitiesAndConnections(javaBytecodeReader.getAllFilePaths(folderPath));

        javaBytecodeReader.generateGraph(entityType, "./codeviz_frontend/public/codeviz_demo.gexf");
    }

    @CrossOrigin
    @GetMapping("/api/viewGraphLevel")
    public Map<String, String> viewGraphLevel(@RequestParam(name = "level", required = false, defaultValue = "PACKAGE") String level) {
        Map<String, String> response = new HashMap<>();

        viewLevel(EntityType.valueOf(level));

        response.put("file", "codeviz_demo.gexf");
        return response; //each API call returns a JSON object that the React app parses
    }

}