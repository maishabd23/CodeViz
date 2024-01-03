package codeViz.entity;

import org.gephi.graph.api.Node;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Set;
import java.awt.Color;
import java.util.Random;

/**
 * Abstract entity class with common behavior, such as connected components
 *
 * @author Thanuja Sivaananthan
 */
public abstract class Entity {
    private final String name;
    private final EntityType entityType;
    private Map<Entity, Integer> connectedEntitiesAndWeights; //stores the weight of connections

    private int size;
    private final Color colour;
    private static final Color HIGHLIGHED_COLOUR = new Color(255,255,50);
    private boolean isHighlighed;

    // FIXME - keeping both Node types for now, until we decide which one to use
    private Node gephiNode;
    private it.uniroma1.dis.wsngroup.gexf4j.core.Node gexf4jNode;

    /**
     * Set up an Entity
     * @author Thanuja Sivaananthan
     * @author Sabah Samwatin
     * @param name          name of entity
     * @param entityType    entity type
     */
    public Entity(String name, EntityType entityType){
        this.name = name.replace("<", "").replace(">", "");
        this.entityType = entityType;
        this.connectedEntitiesAndWeights = new LinkedHashMap<>();
        this.size = 1;
        this.colour = getRandomColour();
        this.isHighlighed = false;
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

    public void incrementSize(){
        this.size += 1;
    }

    public int getSize() {
        return size;
    }

    /**
     * Set a random colour
     * @author Thanuja Sivaananthan
     */
    private Color getRandomColour(){
        Random rand = new Random(name.hashCode()); // enforce a seed for some colour consistency

        while (true) {

            // Will produce only bright / light colours:
            float r = (float) (rand.nextFloat() / 2f + 0.5);
            float g = (float) (rand.nextFloat() / 2f + 0.5);
            float b = (float) (rand.nextFloat() / 2f + 0.5);

            // avoid colours too similar to HIGHLIGHED_COLOUR (for ex, should not be r="244" g="252" b="130")
            if (! (r > ((float) 240 /255) && g > ((float) 240 /255) && b < ((float) 150 /255))) {
                return new Color(r, g, b);
            }
        }
    }

    /**
     * Get colour
     * @author Thanuja Sivaananthan
     * @return  colour
     */
    public Color getColour() {
        if (isHighlighed){
            return HIGHLIGHED_COLOUR;
        } else {
            return colour;
        }
    }

    /**
     * Get highlighted colour
     * @author Thanuja Sivaananthan
     * @return  colour
     */
    public static Color getHighlighedColour() {
        return HIGHLIGHED_COLOUR;
    }

    public boolean isHighlighed() {
        return isHighlighed;
    }

    public void setHighlighed(boolean highlighed) {
        isHighlighed = highlighed;
    }

    /**
     * Get parent colour
     * @author Thanuja Sivaananthan
     * @return  parent colour
     */
    public abstract Color getParentColour();

    /**
     * Add a connected entity with weight
     * This method is protected: A package should only be able to add other packages, etc
     * @param entity entity to add
     */
    protected void addConnectedEntity(Entity entity){
        int initialWeight = connectedEntitiesAndWeights.getOrDefault(entity, 0);
        //System.out.println(initialWeight);
        connectedEntitiesAndWeights.put(entity, initialWeight + 1);
        incrementSize();
    }

    public Set<Entity> getConnectedEntities() {
        return connectedEntitiesAndWeights.keySet();
    }
    public Map <Entity, Integer> getConnectedEntitiesAndWeights(){
        return connectedEntitiesAndWeights;
    }

    public boolean nameContains(String searchValue){
        return name.contains(searchValue);
    }

    public boolean containsSearchValue(String searchValue){
        if (nameContains(searchValue)){
            return true;
        }

        for (Entity connectedEntity : connectedEntitiesAndWeights.keySet()){
            // just do a simple search on the connected entity (don't call recursively, could highlight too much)
            if (connectedEntity.nameContains(searchValue)){
                return true;
            }
        }

        return false;
    }
}
