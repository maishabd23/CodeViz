package org.example.entity;

import java.util.ArrayList;
import java.util.List;

public class PackageEntity extends Entity {

    private List<ClassEntity> classes;

    public PackageEntity(String name){
        super(name, EntityType.PACKAGE);

        this.classes = new ArrayList<>();
    }

    public void addConnectedEntity(PackageEntity packageEntity) {
        super.addConnectedEntity(packageEntity);
    }

    public void addClass(ClassEntity classEntity){
        classes.add(classEntity);
    }

    public List<ClassEntity> getClasses() {
        return classes;
    }
}
