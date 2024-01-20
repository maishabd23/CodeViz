package codeViz.entity;

import codeViz.gitHistory.CommitInfo;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Class entity
 *
 * @author Thanuja Sivaananthan
 */
public class ClassEntity extends Entity {

    private final PackageEntity packageEntity;
    private final Set<MethodEntity> methods;

    private ClassEntity superClass; // may have methods not defined in this class alone

    public ClassEntity(String name, PackageEntity packageEntity){
        super(name, EntityType.CLASS);
        this.packageEntity = packageEntity;
        this.methods = new HashSet<>();
        this.superClass = null;

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

    public void setSuperClass(ClassEntity superClass) {
        this.superClass = superClass;
    }

    public ClassEntity getSuperClass() {
        return superClass;
    }

    /**
     * Add method to a class if it doesn't already exist
     * @author Thanuja Sivaananthan
     *
     * @param methodEntity method entity to add
     */
    public void addMethod(MethodEntity methodEntity){ // TODO - allow for overloaded methods
        if (getMethod(methodEntity.getName()) == null){
            methods.add(methodEntity);
            incrementSize();
        } else {
            System.out.println("NOTE, class " + getName() + " already contains method " + methodEntity.getName() );
        }
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
        methodName = MethodEntity.getProperName(methodName);
        for (MethodEntity method : methods){
            if (method.getName().equals(methodName)){
                return method;
            }
        }
        return null;
    }

    /**
     * Get parent colour
     * @author Thanuja Sivaananthan
     * @return  parent colour
     */
    public Color getParentColour() {
        if (isHighlighed()){ // being highlighted takes precedence over the parent
            return getHighlighedColour();
        } else if (packageEntity != null){
            return packageEntity.getColour();
        } else {
            return getColour();
        }
    }

    @Override
    public boolean containsSearchValue(String searchValue) {

        if (super.containsSearchValue(searchValue)){
            return true;
        }

        // TODO check attributes

        // check methods
        for (MethodEntity methodEntity : methods){
            if (methodEntity.containsSearchValue(searchValue)){
                return true;
            }
        }

        return false;
    }

    @Override
    public void addCommitInfo(CommitInfo commitInfo){
        super.addCommitInfo(commitInfo);

        // add to method?
    }


    @Override
    public String toString() {
        String methodsString = methodEntitySetToString(methods, "Methods");

        String packageName = Entity.entityToString(packageEntity, "Package");
        String superClassName = Entity.entityToString(superClass, "Superclass");

        return titleToString() +
                packageName +
                superClassName +
                methodsString
                ;
    }
}
