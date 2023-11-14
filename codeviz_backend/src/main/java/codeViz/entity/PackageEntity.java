package codeViz.entity;

import java.util.HashSet;
import java.util.Set;
import java.awt.Color;

/**
 * Package entity
 *
 * @author Thanuja Sivaananthan
 */
public class PackageEntity extends Entity {

    private Set<ClassEntity> classes;

    public PackageEntity(String name){
        super(name, EntityType.PACKAGE);

        this.classes = new HashSet<>();
    }

    public void addConnectedEntity(PackageEntity packageEntity) {
        super.addConnectedEntity(packageEntity);
    }

    public void addClass(ClassEntity classEntity){
        classes.add(classEntity);
        incrementSize();
    }

    public Set<ClassEntity> getClasses() {
        return classes;
    }

    /**
     * Get parent colour
     * Note: package doesn't have any parent, so it returns itself
     * @author Thanuja Sivaananthan
     * @return  parent colour
     */
    public Color getParentColour() {
        return getColour();
    }
}
