package org.example.entity;

import org.gephi.graph.api.Node;
import java.util.HashSet;
import java.util.Set;

/**
 * Abstract entity class with common behavior, such as connected components
 *
 * @author Thanuja Sivaananthan
 */
public abstract class Entity {
    private final String name;
    private final EntityType entityType;

    // TODO - store the weight of connections
    private Set<Entity> connectedEntities;

    // FIXME - keeping both Node types for now, until we decide which one to use
    private Node gephiNode;
    private it.uniroma1.dis.wsngroup.gexf4j.core.Node gexf4jNode;

    public Entity(String name, EntityType entityType){
        name = name.replace("<", "").replace(">", "");
        this.name = name;
        this.entityType = entityType;
        this.connectedEntities = new HashSet<>();
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
     * Add a connected entity
     * This method is protected: A package should only be able to add other packages, etc
     * @param entity entity to add
     */
    protected void addConnectedEntity(Entity entity){
        this.connectedEntities.add(entity); // should only add to one list at a time
    }

    public Set<Entity> getConnectedEntities() {
        return connectedEntities;
    }
}
