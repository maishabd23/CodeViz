package org.example.entity;

import java.util.HashSet;
import java.util.Set;

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
    }

    public Set<ClassEntity> getClasses() {
        return classes;
    }
}
