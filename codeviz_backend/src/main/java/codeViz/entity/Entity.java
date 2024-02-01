package codeViz.entity;

import codeViz.gitHistory.CommitInfo;
import codeViz.TextAnnotate;
import org.gephi.graph.api.Node;
import java.util.*;
import java.awt.Color;

/**
 * Abstract entity class with common behavior, such as connected components
 *
 * @author Thanuja Sivaananthan
 */
public abstract class Entity {
    private final String name;
    private final EntityType entityType;
    private final Map<Entity, Integer> connectedEntitiesAndWeights; //stores the weight of connections

    private int size;
    private final Color colour;
    private static final Color HIGHLIGHED_COLOUR = new Color(255,255,50);
    private boolean isHighlighed;

    private Node gephiNode;

    private float x_pos, y_pos;

    private final ArrayList<CommitInfo> commitInfos; //could store as LinkedHashMap - a file can only be changed once per commit

    /**
     * Set up an Entity
     * @author Thanuja Sivaananthan
     * @author Sabah Samwatin
     * @param name          name of entity
     * @param entityType    entity type
     */
    public Entity(String name, EntityType entityType){
        this.name = name;
        this.entityType = entityType;
        this.connectedEntitiesAndWeights = new LinkedHashMap<>();
        this.size = 1;
        this.colour = getRandomColour();
        this.isHighlighed = false;
        this.commitInfos = new ArrayList<>();
        this.x_pos = 0;
        this.y_pos = 0;
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


    public void incrementSize(){
        this.size += 1;
    }

    public int getSize() {
        return size;
    }

    public void setPosition(float x_pos, float y_pos) {
        this.x_pos = x_pos;
        this.y_pos = y_pos;
    }

    public float getX_pos() {
        return x_pos;
    }

    public float getY_pos() {
        return y_pos;
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
     * Only subclasses should call this method directly
     * @author Thanuja Sivaananthan
     * @return  colour
     */
    protected Color getColour() {
        return colour;
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

    public abstract String toString();

    public abstract String getKey();

    /**
     * Return the formatted title of the entity
     * @return  formatted title
     */
    public String titleToString(){
        String type = entityType.toString();
        type = type.substring(0,1).toUpperCase() + type.substring(1).toLowerCase();
        return TextAnnotate.BOLD.javaText + type + ": " + getName() + TextAnnotate.BOLD_OFF.javaText + "\n";
    }

    /**
     * Static method - in case the entity is null
     * @param entity        entity to make a string
     * @param entityTitle          name of entity
     * @return              string value
     */
    public static String entityToString(Entity entity, String entityTitle){
        String entityName = "";
        if (entity != null) entityName = entityTitle + ": " + entity.getName() + "\n";
        return entityName;
    }

    private String entitySetToString(Set<Entity> set, String setTitle){
        StringBuilder setString = new StringBuilder();

        if (!set.isEmpty()) {
            setString.append(setTitle).append(": ");

            for (Entity entity : set) {
                setString.append(entity.getName()).append(", ");
            }

            setString = new StringBuilder(setString.substring(0, setString.length() - 2));
            setString.append("\n");
        }

        return String.valueOf(setString);
    }

    protected String classEntitySetToString(Set<ClassEntity> set, String setTitle){
        return entitySetToString((Set<Entity>) (Set<? extends Entity>) set, setTitle);
    }

    protected String methodEntitySetToString(Set<MethodEntity> set, String setTitle){
        return entitySetToString((Set<Entity>) (Set<? extends Entity>) set, setTitle);
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

    public ArrayList<CommitInfo> getCommitInfos() {
        return commitInfos;
    }

    protected void addCommitInfo(CommitInfo commitInfo){
        this.commitInfos.add(commitInfo);
    }
}
