package codeViz.entity;

import codeViz.codeComplexity.ComplexityDetails;
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
    private final Map<Entity, Float> connectedEntitiesAndWeights; //stores the weight of connections

    private int size;
    private static final int SIZE_INCREMENT = 2;

    private final Color colour;
    private static final Color HIGHLIGHTED_COLOUR = new Color(255,255,50);
    private boolean isHighlighed;

    private Node gephiNode;

    private float x_pos, y_pos;

    private final ArrayList<CommitInfo> commitInfos; // stored in order of most recent to the least recent
    private final Map<Entity, Float> gitConnectedEntitiesAndWeights; //stores the weight of connections

    private final ComplexityDetails complexityDetails;
    private final Entity parent;

    /**
     * Set up an Entity
     *
     * @param name              name of entity
     * @param entityType        entity type
     * @param complexityDetails complexity details
     * @param parent            parent node if it exists
     * @author Thanuja Sivaananthan
     * @author Sabah Samwatin
     */
    public Entity(String name, EntityType entityType, ComplexityDetails complexityDetails, Entity parent){
        this.name = name;
        this.entityType = entityType;
        this.parent = parent;
        this.connectedEntitiesAndWeights = new LinkedHashMap<>();
        this.size = SIZE_INCREMENT;
        this.colour = getRandomColour();
        this.isHighlighed = false;
        // NOTE: might want to move within ClassEntity
        this.commitInfos = new ArrayList<>();
        this.gitConnectedEntitiesAndWeights = new LinkedHashMap<>();
        this.x_pos = 0;
        this.y_pos = 0;

        this.complexityDetails = complexityDetails;
        complexityDetails.setEntity(this);
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


    protected void incrementSize(){
        this.size += SIZE_INCREMENT;
    }

    public static int getSizeIncrement() {
        return SIZE_INCREMENT;
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
    public static Color getHighlightedColour() {
        return HIGHLIGHTED_COLOUR;
    }

    public boolean isHighlighted() {
        return isHighlighed;
    }

    public void setHighlighted(boolean highlighted) {
        isHighlighed = highlighted;
    }

    /**
     * Get parent colour
     * @author Thanuja Sivaananthan
     * @return  parent colour
     */
    public Color getParentColour(){
        if (isHighlighted()){ // being highlighted takes precedence over the parent
            return getHighlightedColour();
        } else if (parent != null){
            return parent.getColour();
        } else {
            return getColour();
        }
    }

    /**
     * Get parent name
     * @author Thanuja Sivaananthan
     * @return  parent name
     */
    public String getParentName(){
        if (parent != null){
            return parent.getName();
        } else {
            return getName();
        }
    }

    public abstract String toString();

    public abstract String getKey();

    /**
     * Return the formatted title of the entity
     * @return  formatted title
     */
    public String titleToString(){
        String type = entityType.getName();
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
        if (entity != null) {
            entityName += TextAnnotate.BOLD.javaText;
            entityName += entityTitle + ": ";
            entityName += TextAnnotate.BOLD_OFF.javaText;
            entityName += entity.getName() + "\n";
        }
        return entityName;
    }

    private String entitySetToString(Set<Entity> set, String setTitle){
        StringBuilder setString = new StringBuilder();

        if (!set.isEmpty()) {
            setString.append(TextAnnotate.BOLD.javaText);
            setString.append(setTitle).append(": ");
            setString.append(TextAnnotate.BOLD_OFF.javaText);

            for (Entity entity : set) {
                setString.append(entity.getName()).append(", ");
            }

            setString = new StringBuilder(setString.substring(0, setString.length() - 2));
            setString.append("\n");
        }

        return String.valueOf(setString);
    }

    protected String classEntityMapToString(HashMap<String, ClassEntity> classMap, String setTitle){
        StringBuilder setString = new StringBuilder();

        if (!classMap.isEmpty()) {
            setString.append(TextAnnotate.BOLD.javaText);
            setString.append(setTitle).append(": ");
            setString.append(TextAnnotate.BOLD_OFF.javaText);

            for (String objectName : classMap.keySet()) {
                setString.append(classMap.get(objectName).getName()).append(" ").append(objectName).append(", ");
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
        if (entity == null){
            return;
        }
        float initialWeight = connectedEntitiesAndWeights.getOrDefault(entity, (float) 0);
        //System.out.println(initialWeight);
        connectedEntitiesAndWeights.put(entity, initialWeight + 1);
    }

    protected void addGitConnectedEntity(Entity entity, float weight){
        // only add to git history if they are connected in dependency graph
        if (connectedEntitiesAndWeights.containsKey(entity)) {
            gitConnectedEntitiesAndWeights.put(entity, weight);
        }
    }

    public Set<Entity> getConnectedEntities() {
        return connectedEntitiesAndWeights.keySet();
    }
    public Map <Entity, Float> getConnectedEntitiesAndWeights(){
        return connectedEntitiesAndWeights;
    }

    public Map<Entity, Float> getGitConnectedEntitiesAndWeights() {
        return gitConnectedEntitiesAndWeights;
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

    public ComplexityDetails getComplexityDetails() {
        return complexityDetails;
    }
}
