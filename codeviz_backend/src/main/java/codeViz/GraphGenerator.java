package codeViz;

import codeViz.entity.Entity;
import codeViz.entity.EntityType;
import it.uniroma1.dis.wsngroup.gexf4j.core.EdgeType;
import it.uniroma1.dis.wsngroup.gexf4j.core.Gexf;
import it.uniroma1.dis.wsngroup.gexf4j.core.Mode;
import it.uniroma1.dis.wsngroup.gexf4j.core.impl.GexfImpl;
import it.uniroma1.dis.wsngroup.gexf4j.core.impl.StaxGraphWriter;
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

    private float max_x;
    private float max_y;

    /**
     * Create an EntityGraphGenerator
     * @author Thanuja Sivaananthan
     */
    public GraphGenerator(){
        packageEntities = new LinkedHashMap<>();
        classEntities = new LinkedHashMap<>();
        methodEntities = new LinkedHashMap<>();
        // initial values, these are dynamically set based on the graph size
        this.max_x = 500;
        this.max_y = 500;
    }

    /**
     * Add entity with specific key
     * @author Thanuja Sivaananthan
     * @param key       key of entity
     * @param entity    entity
     */
    public boolean addEntity(String key, Entity entity){
        LinkedHashMap<String, Entity> entites;
        switch (entity.getEntityType()) {
            case PACKAGE -> entites = packageEntities;
            case CLASS -> entites = classEntities;
            case METHOD -> entites = methodEntities;
            default -> throw new IllegalStateException("Unexpected value: " + entity.getEntityType());
        }

        // If the key already exists, normally its old value is replaced with a new one
        // Do not want to replace with new value, as any connections that were made could get messed up
        if (entites.containsKey(key)){
            System.out.println("NOTE, list of type " + entity.getEntityType() + " already has name: " + key);
            return false;
        }

        entites.put(key, entity);
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
     * Convert entities to Gexf format using gexf4j
     * NOTE: the produced file is not fully supported in latest gephi
     *
     * @author Thanuja Sivaananthan
     * @param entityType entityType to create graph from
     * @param filename filename to save as
     */
    public void entitiesToGexf(EntityType entityType, String filename){

        // examples from https://github.com/francesco-ficarola/gexf4j/tree/master/src/examples/java/it/uniroma1/dis/wsngroup/gexf4j/examples

        // NOTE: assuming all entities are properly set up with connections already
        Collection<Entity> entities;

        switch (entityType) {
            case PACKAGE -> entities = packageEntities.values();
            case CLASS -> entities =  classEntities.values();
            case METHOD -> entities = methodEntities.values();
            default -> throw new IllegalStateException("Unexpected value: " + entityType);
        }

        Gexf gexf = new GexfImpl();
        Calendar date = Calendar.getInstance();

        gexf.getMetadata()
                .setLastModified(date.getTime())
                .setCreator("Gephi.org")
                .setDescription("A Web network");
        gexf.setVisualization(true);

        it.uniroma1.dis.wsngroup.gexf4j.core.Graph graph =  gexf.getGraph();

        graph.setDefaultEdgeType(EdgeType.DIRECTED).setMode(Mode.STATIC); // not sure how to set as directed

        // 1. create nodes for each entity
        int id = 1; // add id in case there are duplicate names
        for (Entity entity : entities){
            it.uniroma1.dis.wsngroup.gexf4j.core.Node node = graph.createNode(id + entity.getName());
            node.setLabel(entity.getName());
            entity.setGexf4jNode(node);

            id += 1;
        }

        // 2. create edges for each pair
        for (Entity entity : entities){
            for (Entity connectedEntity : entity.getConnectedEntities()){
                it.uniroma1.dis.wsngroup.gexf4j.core.Edge edge = entity.getGexf4jNode().connectTo("1", connectedEntity.getGexf4jNode());
            }
        }


        StaxGraphWriter graphWriter = new StaxGraphWriter();
        File f = new File(filename);
        Writer out;
        try {
            out =  new FileWriter(f, false);
            graphWriter.writeToStream(gexf, out, "UTF-8");
            System.out.println(f.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
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
    public DirectedGraph entitiesToNodes(EntityType entityType){
        // NOTE: assuming all entities are properly set up with connections already
        LinkedHashMap<String, Entity> entities;

        switch (entityType) {
            case PACKAGE -> entities = packageEntities;
            case CLASS -> entities =  classEntities;
            case METHOD -> entities = methodEntities;
            default -> throw new IllegalStateException("Unexpected value: " + entityType);
        }

        //System.out.println("Get coordinates for " + entityType);
        float max_graph_size = getGraphSize(entities);
        max_x = max_graph_size;
        max_y = max_graph_size;

        List<Node> nodes = new ArrayList<>();
        List<Edge> edges = new ArrayList<>();

        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.newProject();
        //Workspace workspace = pc.getCurrentWorkspace();

        //Get a graph model - it exists because we have a workspace
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();

        // 1. create nodes for each entity
        int id = 1; // add id in case there are duplicate names
        for (String entityKey : entities.keySet()){
            Entity entity = entities.get(entityKey);
            String nodeName = entity.getName(); // FIXME - might want this as entityKey, but the name might get really long
            Node node = graphModel.factory().newNode(id + nodeName);
            node.setLabel(nodeName);
            node.setSize(entity.getSize()); // might need to scale up node size so it appears nicely?
            node.setColor(entity.getParentColour());

            Random rand = new Random();
            float pos_x = rand.nextFloat() * max_x;
            float pos_y = rand.nextFloat() * max_y;
            node.setPosition(pos_x, pos_y); // TODO - determine proper coordinates

            entity.setGephiNode(node);
            nodes.add(node);

            id += 1;
        }

        // 2. create edges for each pair
        for (String entityKey : entities.keySet()){
            Entity entity = entities.get(entityKey);
            for (Map.Entry<Entity, Integer> entry : entity.getConnectedEntitiesAndWeights().entrySet()){

                Entity connectedEntity = entry.getKey();
                int weight = entry.getValue();
                int type = (int) 1f; // not sure what the type field should be
                Edge edge = graphModel.factory().newEdge(entity.getGephiNode(), connectedEntity.getGephiNode(), type, weight, true);
                edges.add(edge);
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
                writer.write("\t\t\t<edge source=\"" + edge.getSource().getId() + "\" target=\"" + edge.getTarget().getId() + "\" weight=\"" + edge.getWeight() +"\" />");
                writer.write("\n");
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
    public void clearEntites() {
        this.packageEntities.clear();
        this.classEntities.clear();
        this.methodEntities.clear();
    }


    /**
     * Perform a search on a given entity type
     *
     * @param searchValue      the search value
     * @param entities         the entities to search
     * @param isDetailedSearch if the search is detailed or not
     * @author Thanuja Sivaananthan
     */
    private void performSearch(String searchValue, LinkedHashMap<String, Entity> entities, boolean isDetailedSearch){
        // assume correct case
        for (String entityKey : entities.keySet()) {
            Entity entity = entities.get(entityKey);
            // this should work if graphing a level, where the search is at a higher level
            // will not highlight if graphing a level, where the search is at a lower level
            // TODO - in depth searches - highlight class if it contains an attribute name, method if it contains a parameter name, etc?
            if (entityKey.contains(searchValue) || entity.nameContains(searchValue)) {
                entity.setHighlighed(true);
            } else if (isDetailedSearch && entity.containsSearchValue(searchValue)){
                entity.setHighlighed(true);
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
    private void clearSearch() {
        clearSearch(packageEntities);
        clearSearch(classEntities);
        clearSearch(methodEntities);

    }
}
