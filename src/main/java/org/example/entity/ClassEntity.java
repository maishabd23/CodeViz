package org.example.entity;

import java.util.HashSet;
import java.util.Set;

/**
 * Class entity
 *
 * @author Thanuja Sivaananthan
 */
public class ClassEntity extends Entity {

    private final PackageEntity packageEntity;
    private Set<MethodEntity> methods;

    public ClassEntity(String name, PackageEntity packageEntity){
        super(name, EntityType.CLASS);
        this.packageEntity = packageEntity;
        this.methods = new HashSet<>();

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

    public Set<MethodEntity> getMethods() {
        return methods;
    }

    /**
     * Get a classEntity's methodEntity
     * @author Thanuja Sivaananthan
     *
     * @param methodName    name of method
     * @return              methodEntity, or null if the method doesn't exist
     */
    public MethodEntity getMethod(String methodName){
        // could simplify this if methods is changed from Set to something like HashMap
        methodName = methodName.replace("<", "").replace(">","");
        for (MethodEntity method : methods){
            if (method.getName().equals(methodName)){
                return method;
            }
        }
        return null;
    }

}
