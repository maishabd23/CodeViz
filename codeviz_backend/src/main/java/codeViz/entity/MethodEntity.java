package codeViz.entity;

import java.awt.Color;

/**
 * Method entity
 *
 * @author Thanuja Sivaananthan
 */
public class MethodEntity extends Entity {

    private final ClassEntity classEntity;

    public MethodEntity(String name, ClassEntity classEntity){
        super(name, EntityType.METHOD);
        this.classEntity = classEntity;

        // should classEntity store its methods (easier to reference), or is that too much coupling?
        classEntity.addMethod(this);
    }

    public void addConnectedEntity(MethodEntity methodEntity) {
        super.addConnectedEntity(methodEntity);
    }

    public ClassEntity getClassEntity() {
        return classEntity;
    }

    /**
     * Get parent colour
     * @author Thanuja Sivaananthan
     * @return  parent colour
     */
    public Color getParentColour() {
        return classEntity.getColour();
    }
}
