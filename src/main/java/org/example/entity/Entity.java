package org.example.entity;

import java.util.ArrayList;
import java.util.List;

public abstract class Entity {
    private final String name;
    private final EntityType entityType;
    private List<Entity> connectedEntities;

    public Entity(String name, EntityType entityType){
        this.name = name;
        this.entityType = entityType;
        this.connectedEntities = new ArrayList<>();
    }

    /**
     * A package should only be able to add other packages, etc
     * @param entity
     */
    protected void addConnectedEntity(Entity entity){
        this.connectedEntities.add(entity); // should only add to one list at a time
    }

    public List<Entity> getConnectedEntities() {
        return connectedEntities;
    }
}
