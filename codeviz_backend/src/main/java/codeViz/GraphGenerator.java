package codeViz;

import codeViz.entity.*;
import org.gephi.graph.api.*;
import org.gephi.project.api.ProjectController;
import org.openide.util.Lookup;

import java.io.*;
import java.util.*;
import java.awt.Color;

/**
 * Class that generates the gephi input files
 *
 * @author Thanuja Sivaananthan
 */
public class GraphGenerator {

    // can look at the individual list when making that specific level's view
    // NOTE: kept all List types as Entity to allow for code reuse, might need to specify type as PackageEntity, etc, later on
    private final LinkedHashMap<String, Entity> packageEntities;
    private final LinkedHashMap<String, Entity> classEntities;
    private final LinkedHashMap<String, Entity> methodEntities;
    private String searchValue;

    /**
     * Create an EntityGraphGenerator
     * @author Thanuja Sivaananthan
     */
    public GraphGenerator(){
        packageEntities = new LinkedHashMap<>();
        classEntities = new LinkedHashMap<>();
        methodEntities = new LinkedHashMap<>();
        this.searchValue = "";
    }

    /**
     * Add entity with specific key
     * @author Thanuja Sivaananthan
     * @param key       key of entity
     * @param entity    entity
     */
    public boolean addEntity(String key, Entity entity){
        LinkedHashMap<String, Entity> entities = getEntities(entity.getEntityType());

        // If the key already exists, normally its old value is replaced with a new one
        // Do not want to replace with new value, as any connections that were made could get messed up
        if (entities.containsKey(key)){
            System.out.println("NOTE, list of type " + entity.getEntityType() + " already has name: " + key);
            return false;
        }

        entities.put(key, entity);
        return true;
    }

    public LinkedHashMap<String, Entity> getPackageEntities() {
        return packageEntities;
    }

    public LinkedHashMap<String, Entity> getClassEntities() {
        return classEntities;
    }

    public LinkedHashMap<String, Entity> getMethodEntities() {
        return methodEntities;
    }


    /**
     * Set max size of coordinates based on the nodes in the graph
     * Note: This method could be improved to better fit the graph
     * @param entities      entities to graph
     * @return              size of graph
     * @author Thanuja Sivaananthan
     */
    private float getGraphSize(LinkedHashMap<String, Entity> entities){
        int totalNodeSize = 0;

        for (String entityKey : entities.keySet()) {
            Entity entity = entities.get(entityKey);
            totalNodeSize += entity.getSize();
        }

        //System.out.println("Total node size is " + totalNodeSize + " for " + entities.size() + " nodes");
        int factor = totalNodeSize/100;
        if (factor == 0) factor = 1;
        float max_graph_size = (float) (totalNodeSize * factor);
        //System.out.println("Setting max as " + max_graph_size);
        return max_graph_size;
    }


    /**
     * Convert entities to directedGraph format
     *
     * @author Thanuja Sivaananthan
     * @param entityType    entityType to create graph from
     * @return              directed graph
     */
    public DirectedGraph entitiesToNodes(EntityType entityType) {
        LinkedHashMap<String, Entity> entities = getEntities(entityType);
        return entitiesToNodes(entities);
    }

    /**
     * Generate an inner graph for a specific parent node.
     * The following level pairs are supported:
     * parentEntity: PACKAGE, childLevel: CLASS
     * parentEntity: PACKAGE, childLevel: METHOD
     * parentEntity: CLASS, childLevel: METHOD
     * @param parentEntity  the entity to generate the inner graph for
     * @param childLevel    the level of the inner graph
     * @return              resulting directed graph
     */
    private DirectedGraph entitiesToNodes(Entity parentEntity, EntityType childLevel) {

        // combinations that do not work
        if (parentEntity.getEntityType().equals(EntityType.METHOD) // 3 - method - any
                || parentEntity.getEntityType().equals(EntityType.CLASS) && childLevel.equals(EntityType.PACKAGE) // 1 - class - package
                || parentEntity.getEntityType().equals(childLevel)) // 2 - x - x
        {
            return null;
        }

        System.out.println("get entities within " + parentEntity.getKey());
        LinkedHashMap<String, Entity> entities = new LinkedHashMap<>();
        if (parentEntity != null) { // keep this check just in case
            if (parentEntity.getEntityType().equals(EntityType.PACKAGE)) {
                if (childLevel.equals(EntityType.CLASS)) { // package - class
                    PackageEntity packageEntity = (PackageEntity) parentEntity;
                    Set<ClassEntity> classEntities1 = packageEntity.getClasses();
                    for (Entity entityInner : classEntities1) {
                        entities.put(entityInner.getKey(), entityInner);
                    }
                } else if (childLevel.equals(EntityType.METHOD)) { // package - method
                    PackageEntity packageEntity = (PackageEntity) parentEntity;
                    Set<ClassEntity> classEntities1 = packageEntity.getClasses();
                    for (ClassEntity classEntityInner : classEntities1) {
                        Set<MethodEntity> methodEntities1 = classEntityInner.getMethods();
                        for (Entity entityInner : methodEntities1) {
                            entities.put(entityInner.getKey(), entityInner);
                        }
                    }
                }
            } else if (parentEntity.getEntityType().equals(EntityType.CLASS)) { // class - method
                ClassEntity classEntity = (ClassEntity) parentEntity;
                Set<MethodEntity> methodEntities1 = classEntity.getMethods();
                for (Entity entityInner : methodEntities1) {
                    entities.put(entityInner.getKey(), entityInner);
                }
            }
        } else {
            System.out.println("ERROR, parentEntity null ");
        }
        if (entities.isEmpty()){
            System.out.println("EMPTY entities list");
        }
        return entitiesToNodes(entities);
    }



    private DirectedGraph entitiesToNodes(LinkedHashMap<String, Entity> entities){
        // NOTE: assuming all entities are properly set up with connections already

        if (entities.isEmpty()){
            return null;
        }

        List<Node> nodes = new ArrayList<>();
        List<Edge> edges = new ArrayList<>();

        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.newProject();
        //Workspace workspace = pc.getCurrentWorkspace();

        //Get a graph model - it exists because we have a workspace
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();


        EntityType entityType = null; // TODO - fix the way this EntityType variable is created

        // 1. create nodes for each entity
        int id = 1; // add id in case there are duplicate names
        for (String entityKey : entities.keySet()){
            Entity entity = entities.get(entityKey);
            entityType = entity.getEntityType();
            String nodeName = entity.getName();
            Node node = graphModel.factory().newNode(id + "_" + entityKey); // want this as entityKey, is okay because only label is displayed
            node.setLabel(nodeName);
            node.setSize(entity.getSize()); // might need to scale up node size so it appears nicely?
            node.setColor(entity.getParentColour());

            float pos_x = entity.getX_pos();
            float pos_y = entity.getY_pos();
            node.setPosition(pos_x, pos_y); // TODO - determine proper coordinates

            entity.setGephiNode(node);
            nodes.add(node);

            id += 1;
        }

        // 2. create edges for each pair
        for (String entityKey : entities.keySet()){
            Entity entity = entities.get(entityKey);

            Set<Map.Entry<Entity, Integer>> connectedEntities = entity.getConnectedEntitiesAndWeights().entrySet();
            if (entityType.equals(EntityType.CLASS)){
                connectedEntities = entity.getGitConnectedEntitiesAndWeights().entrySet(); // TODO - move to diff location
            }

            for (Map.Entry<Entity, Integer> entry : connectedEntities){

                Entity connectedEntity = entry.getKey();
                // FIXME what if connected entities doesn't exist in inner graph?
                //  could add immediate connections
                //  could simply not include those nodes/edges that aren't in the inner graph

                if (nodes.contains(connectedEntity.getGephiNode())) { // only add edge if the other node exists
                    int weight = entry.getValue();
                    int type = (int) 1f; // not sure what the type field should be
                    Edge edge = graphModel.factory().newEdge(entity.getGephiNode(), connectedEntity.getGephiNode(), type, weight, true);
                    edges.add(edge);
                }
            }
        }

        DirectedGraph directedGraph = graphModel.getDirectedGraph();
        for (Node node : nodes){
            directedGraph.addNode(node);
        }

        for (Edge edge : edges){
            directedGraph.addEdge(edge);
        }

        return directedGraph;

    }


    /**
     * Convert entities to Gexf format manually
     *
     * @author Thanuja Sivaananthan
     *
     * @param directedGraph directed graph to use
     * @param filename filename to save as
     */
    public void directedGraphToGexf(DirectedGraph directedGraph, String filename){

        // following this format:
        // https://gexf.net/basic.html

        Node[] nodes = directedGraph.getNodes().toArray();
        Edge[] edges = directedGraph.getEdges().toArray();

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(filename));

            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            writer.write("<gexf xmlns=\"http://gexf.net/1.3\" version=\"1.3\">\n");
            writer.write("\t<graph mode=\"static\" defaultedgetype=\"directed\">\n");

            writer.write("\t\t<nodes>\n");
            for (Node node: nodes){
                // System.out.println(node.getLabel());
                writer.write("\t\t\t<node id=\"" + node.getId() + "\" label=\"" + node.getLabel() + "\" >\n");

                writer.write("\t\t\t\t<size value=\"" + node.size() + "\"></size>");
                writer.write("\n");

                writer.write("\t\t\t\t <position x=\"" + node.x() + "\" y=\"" + node.y() + "\" z=\"0.0\"></position>");
                writer.write("\n");

                Color colour = node.getColor();
                writer.write("\t\t\t\t<color r=\"" + colour.getRed() + "\" g=\"" + colour.getGreen() + "\" b=\""+ colour.getBlue() +"\"></color>");
                writer.write("\n");

                writer.write("\t\t\t</node>\n");
            }
            writer.write("\t\t</nodes>\n");

            writer.write("\t\t<edges>\n");
            for (Edge edge: edges){
                // System.out.println(edge.getSource().getLabel() + "->" + edge.getTarget().getLabel());
                writer.write("\t\t\t<edge source=\"" + edge.getSource().getId() + "\" target=\"" + edge.getTarget().getId() + "\" weight=\"" + edge.getWeight() +"\" >");
                writer.write("\n");

                writer.write("\t\t\t\t<size value=\"" + edge.getWeight() + "\"></size>");
                writer.write("\n");

                writer.write("\t\t\t</edge>\n");
            }
            writer.write("\t\t</edges>\n");

            writer.write("\t</graph>\n");
            writer.write("</gexf>\n");

            writer.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }


    /**
     * Clear existing entities from the graph generator
     * @author Thanuja Sivaananthan
     */
    public void clearEntities() {
        this.packageEntities.clear();
        this.classEntities.clear();
        this.methodEntities.clear();
    }


    /**
     * Perform a search on a given entity type
     * Note: is case-sensitive
     *
     * @param searchValue      the search value
     * @param entities         the entities to search
     * @param isDetailedSearch if the search is detailed or not
     * @author Thanuja Sivaananthan
     */
    private void performSearch(String searchValue, LinkedHashMap<String, Entity> entities, boolean isDetailedSearch){
        for (String entityKey : entities.keySet()) {
            Entity entity = entities.get(entityKey);
            if (entity.nameContains(searchValue)) { // simple search - only checks the node name, not full name
                entity.setHighlighed(true);
            } else if (isDetailedSearch) {
                if (entityKey.contains(searchValue) || entity.containsSearchValue(searchValue)) {
                    entity.setHighlighed(true);
                }
            }
        }
    }

    /**
     * Perform a search
     *
     * @param searchValue      the search value
     * @param isDetailedSearch if the search is detailed or not
     * @author Thanuja Sivaananthan
     */
    public void performSearch(String searchValue, boolean isDetailedSearch) {
        // start a fresh search
        clearSearch();

        this.searchValue = searchValue;
        performSearch(searchValue, packageEntities, isDetailedSearch);
        performSearch(searchValue, classEntities, isDetailedSearch);
        performSearch(searchValue, methodEntities, isDetailedSearch);
    }

    /**
     * Clear any previous searches
     * @param entities          the entities to clear
     * @author Thanuja Sivaananthan
     */
    private void clearSearch(LinkedHashMap<String, Entity> entities){
        for (String entityKey : entities.keySet()) {
            Entity entity = entities.get(entityKey);
            entity.setHighlighed(false);
        }

    }

    /**
     * Clear any previous searches
     * @author Thanuja Sivaananthan
     */
    public void clearSearch() {
        this.searchValue = "";
        clearSearch(packageEntities);
        clearSearch(classEntities);
        clearSearch(methodEntities);

    }

    private void setEntitiesCoordinates(LinkedHashMap<String, Entity> entities){
        //System.out.println("Get coordinates for " + entityType);
        float max_graph_size = getGraphSize(entities);
        float max_x = max_graph_size;
        float max_y = max_graph_size;

        Random rand = new Random();
        for (Entity entity : entities.values()){
            float pos_x = rand.nextFloat() * max_x;
            float pos_y = rand.nextFloat() * max_y;
            entity.setPosition(pos_x, pos_y); // TODO - determine proper coordinates
        }
    }

    public void setEntitiesCoordinates() {
        setEntitiesCoordinates(packageEntities);
        setEntitiesCoordinates(classEntities);
        setEntitiesCoordinates(methodEntities);
    }

    private LinkedHashMap<String, Entity> getEntities(EntityType entityType){
        LinkedHashMap<String, Entity> entities;

        switch (entityType) {
            case PACKAGE -> entities = packageEntities;
            case CLASS -> entities =  classEntities;
            case METHOD -> entities = methodEntities;
            default -> throw new IllegalStateException("Unexpected value: " + entityType);
        }

        return entities;
    }

    public Entity getNode(String nodeName, EntityType entityType) {
        LinkedHashMap<String, Entity> entities = getEntities(entityType);

        String[] newNodeNames = nodeName.split("_", 2);
        if (newNodeNames.length != 2){
            System.out.println("INVALID NAME, " + nodeName + " LENGTH IS " + newNodeNames.length);
            return null;
        }

        nodeName = newNodeNames[1];
        Entity entity = entities.getOrDefault(nodeName, null);

        if (entity == null){
            System.out.println("KEY DOESN'T EXIST FOR " + nodeName);
            return null;
        }

        return entity;
    }

    public String getNodeDetails(String nodeName, EntityType entityType) {
        // FIXME - duplicated code
        Entity entity = getNode(nodeName, entityType);
        if (entity == null){
            return "";
        } else {
            return entity.toString();
        }
    }

    /**
     * Check if search value found for certain level
     * @param entityType        level
     * @return                  string message
     */
    public String getSearchResult(EntityType entityType) {

        if (searchValue.isEmpty()){
            return "";
        }

        LinkedHashMap<String, Entity> entities = getEntities(entityType);

        boolean isFound = false;

        for (Entity entity : entities.values()){
            if (entity.isHighlighed()){
                isFound = true;
            }
        }

        String result = TextAnnotate.BOLD.javaText;

        if (isFound) {
            result += TextAnnotate.GREEN.javaText + "Found results for: " + searchValue;
        } else {
            result += TextAnnotate.RED.javaText + "No results for: " + searchValue;
        }
        result += TextAnnotate.RESET.javaText + TextAnnotate.BOLD_OFF.javaText;
        return result;
    }

    /**
     * Generate a code graph at a specific level
     * @param newLevel the level to generate the code graph at
     * @param filename the filename to save the gexf file as
     */
    public void directedGraphToGexf(EntityType newLevel, String filename) {
        DirectedGraph directedGraph = entitiesToNodes(newLevel);
        if (directedGraph != null) {
            directedGraphToGexf(directedGraph, filename);
        }
    }

    /**
     * Generate a filtered code graph at a specific level
     * @param parentEntity the parent entity to filter the graph to
     * @param childLevel the level to generate the code graph at
     * @param filename the filename to save the gexf file as
     * @return  boolean, whether the filtered graph was successfully generated
     */
    public boolean directedGraphToGexf(Entity parentEntity, EntityType childLevel, String filename) {
        DirectedGraph directedGraph = entitiesToNodes(parentEntity, childLevel);
        if (directedGraph != null) {
            directedGraphToGexf(directedGraph, filename);
            return true;
        }
        return false;
    }
}
