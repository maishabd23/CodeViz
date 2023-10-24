package org.example.entity;

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
}
