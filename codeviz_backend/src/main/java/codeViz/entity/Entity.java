package codeViz.entity;

import org.gephi.graph.api.Node;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Set;

/**
 * Abstract entity class with common behavior, such as connected components
 *
 * @author Thanuja Sivaananthan
 */
public abstract class Entity {
    private final String name;
    private final EntityType entityType;
    private Map<Entity, Integer> connectedEntitiesAndWeights; //stores the weight of connections

    // FIXME - keeping both Node types for now, until we decide which one to use
    private Node gephiNode;
    private it.uniroma1.dis.wsngroup.gexf4j.core.Node gexf4jNode;

    public Entity(String name, EntityType entityType){
        this.name = name.replace("<", "").replace(">", "");
        this.entityType = entityType;
        this.connectedEntitiesAndWeights = new LinkedHashMap<>();
    }

    public String getName() {
        return name;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public void setGephiNode(Node node) {
        this.gephiNode = node;
    }

    public Node getGephiNode() {
        return gephiNode;
    }

    public void setGexf4jNode(it.uniroma1.dis.wsngroup.gexf4j.core.Node gexf4jNode) {
        this.gexf4jNode = gexf4jNode;
    }

    public it.uniroma1.dis.wsngroup.gexf4j.core.Node getGexf4jNode() {
        return gexf4jNode;
    }

    /**
     * Add a connected entity with weight
     * This method is protected: A package should only be able to add other packages, etc
     * @param entity entity to add
     */
    protected void addConnectedEntity(Entity entity){
        int initialWeight = connectedEntitiesAndWeights.getOrDefault(entity, 0);
        //System.out.println(initialWeight);
        connectedEntitiesAndWeights.put(entity, initialWeight + 1);
    }

    public Set<Entity> getConnectedEntities() {
        return connectedEntitiesAndWeights.keySet();
    }
    public Map <Entity, Integer> getConnectedEntitiesAndWeights(){
        return connectedEntitiesAndWeights;
    }
}
