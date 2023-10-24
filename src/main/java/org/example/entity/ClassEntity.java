package org.example.entity;

import java.util.ArrayList;
import java.util.List;

public class ClassEntity extends Entity {

    private final PackageEntity packageEntity;
    private List<MethodEntity> methods;

    public ClassEntity(String name, PackageEntity packageEntity){
        super(name, EntityType.CLASS);
        this.packageEntity = packageEntity;
        this.methods = new ArrayList<>();

        if (packageEntity != null){
            packageEntity.addClass(this);
        }
    }

    public ClassEntity(String name){
        this(name, null);
    }

    public void addConnectedEntity(ClassEntity classEntity) {
        super.addConnectedEntity(classEntity);
    }

    public PackageEntity getPackageEntity() {
        return packageEntity;
    }

    public void addMethod(MethodEntity methodEntity){
        methods.add(methodEntity);
    }

    public List<MethodEntity> getMethods() {
        return methods;
    }
}
