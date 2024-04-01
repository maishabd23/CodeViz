package codeViz.entity;

import codeViz.codeComplexity.ComplexityDetails;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Package entity
 *
 * @author Thanuja Sivaananthan
 */
public class PackageEntity extends Entity {

    private final Set<ClassEntity> classes;
    private final PackageEntity superPackage;

    public PackageEntity(String name, PackageEntity superPackage){
        super(name, EntityType.PACKAGE, new ComplexityDetails(), null); // TODO might not use complexity details

        this.classes = new LinkedHashSet<>();
        this.superPackage = superPackage;
        if (superPackage != null) {
            superPackage.incrementSize();
        }
    }

    public PackageEntity(String name){
        this(name, null);
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

    @Override
    public boolean containsSearchValue(String searchValue) {
        if (super.containsSearchValue(searchValue)){
            return true;
        }

        // check classes
        for (ClassEntity classEntity : classes){
            if (classEntity.containsSearchValue(searchValue)){
                return true;
            }
        }

        return false;
    }

    @Override
    public String toString() {
        String superPackageName = Entity.entityToString(superPackage, "Superpackage");
        String classesString = classEntitySetToString(classes, "Classes");

        return titleToString() +
                superPackageName +
                classesString
                ;
    }

    @Override
    public String getKey() {
        String name = getName();
        if (superPackage == null){
            return getName();
        } else {
            return superPackage.getKey() + "." + name;
        }
    }
}
