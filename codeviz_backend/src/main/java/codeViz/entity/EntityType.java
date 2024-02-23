package codeViz.entity;

/**
 * Entity type to differentiate entities
 *
 * @author Thanuja Sivaananthan
 */
public enum EntityType {
    METHOD("Method", null),
    CLASS("Class", METHOD),
    PACKAGE("Package", CLASS);

    private final String name;
    private final EntityType child;

    EntityType(String name, EntityType child) {
        this.name = name;
        this.child = child;
    }

    public String getName() {
        return name;
    }

    public EntityType getChild() {
        return child;
    }
}
