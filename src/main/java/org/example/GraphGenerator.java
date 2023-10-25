package org.example;

import it.uniroma1.dis.wsngroup.gexf4j.core.EdgeType;
import it.uniroma1.dis.wsngroup.gexf4j.core.Gexf;
import it.uniroma1.dis.wsngroup.gexf4j.core.Mode;
import it.uniroma1.dis.wsngroup.gexf4j.core.impl.GexfImpl;
import it.uniroma1.dis.wsngroup.gexf4j.core.impl.StaxGraphWriter;
import org.example.entity.*;
import org.gephi.graph.api.*;
import org.gephi.project.api.ProjectController;
import org.openide.util.Lookup;

import java.io.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Class that generates the gephi input files
 *
 * @author Thanuja Sivaananthan
 */
public class GraphGenerator {

    // can look at the individual list when making that specific level's view
    // NOTE: kept all List types as Entity to allow for code reuse, might need to specify type as PackageEntity, etc, later on
    //private List<Entity> allEntities;
    private List<Entity> packageEntities;
    private List<Entity> classEntities;
    private List<Entity> methodEntities;

    /**
     * Create an EntityGraphGenerator
     * @author Thanuja Sivaananthan
     */
    public GraphGenerator(){
        //allEntities = new ArrayList<>();
        packageEntities = new ArrayList<>();
        classEntities = new ArrayList<>();
        methodEntities = new ArrayList<>();
    }

    /**
     * Add entity
     * @author Thanuja Sivaananthan
     * @param entity    entity
     */
    public void addEntity(Entity entity){
        //allEntities.add(entity);
        switch (entity.getEntityType()) {
            case PACKAGE -> packageEntities.add(entity);
            case CLASS -> classEntities.add(entity);
            case METHOD -> methodEntities.add(entity);
            default -> throw new IllegalStateException("Unexpected value: " + entity.getEntityType());
        }
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
        List<Entity> entities;

        switch (entityType) {
            case PACKAGE -> entities = packageEntities;
            case CLASS -> entities = classEntities;
            case METHOD -> entities = methodEntities;
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
     * Convert entities to directedGraph format
     *
     * @author Thanuja Sivaananthan
     * @param entityType    entityType to create graph from
     * @return              directed graph
     */
    public DirectedGraph entitiesToNodes(EntityType entityType){
        // NOTE: assuming all entities are properly set up with connections already
        List<Entity> entities;

        switch (entityType) {
            case PACKAGE -> entities = packageEntities;
            case CLASS -> entities = classEntities;
            case METHOD -> entities = methodEntities;
            default -> throw new IllegalStateException("Unexpected value: " + entityType);
        }

        List<Node> nodes = new ArrayList<>();
        List<Edge> edges = new ArrayList<>();

        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.newProject();
        //Workspace workspace = pc.getCurrentWorkspace();

        //Get a graph model - it exists because we have a workspace
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();

        // 1. create nodes for each entity
        int id = 1; // add id in case there are duplicate names
        for (Entity entity : entities){
            Node node = graphModel.factory().newNode(id + entity.getName());
            node.setLabel(entity.getName());
            entity.setGephiNode(node);
            nodes.add(node);

            id += 1;
        }

        // 2. create edges for each pair
        for (Entity entity : entities){
            for (Entity connectedEntity : entity.getConnectedEntities()){
                // https://gephi.org/gephi/0.9.2/apidocs/org/gephi/graph/api/GraphFactory.html
                // can have other parameters like type, weight, directed
                Edge edge = graphModel.factory().newEdge(entity.getGephiNode(), connectedEntity.getGephiNode(), true);
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
                writer.write("\t\t\t<node id=\"" + node.getId() + "\" label=\"" + node.getLabel() + "\" />");
                writer.write("\n");
            }
            writer.write("\t\t</nodes>\n");

            writer.write("\t\t<edges>\n");
            for (Edge edge: edges){
                // System.out.println(edge.getSource().getLabel() + "->" + edge.getTarget().getLabel());
                writer.write("\t\t\t<edge source=\"" + edge.getSource().getId() + "\" target=\"" + edge.getTarget().getId() +"\" />");
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


}
