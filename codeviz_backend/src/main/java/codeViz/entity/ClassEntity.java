package codeViz.entity;

import codeViz.codeComplexity.ClassComplexityDetails;
import codeViz.gitHistory.CommitInfo;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Class entity
 *
 * @author Thanuja Sivaananthan
 */
public class ClassEntity extends Entity {

    private final PackageEntity packageEntity;
    private final HashMap<String, ClassEntity> fields;
    private final HashMap<String, MethodEntity> methods;

    private ClassEntity superClass; // may have methods not defined in this class alone

    public ClassEntity(String name, PackageEntity packageEntity){
        super(name, EntityType.CLASS, new ClassComplexityDetails(), packageEntity);
        this.packageEntity = packageEntity;
        this.fields = new LinkedHashMap<>();
        this.methods = new LinkedHashMap<>();
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

    public void addGitConnectedEntity(ClassEntity classEntity, float weight) {
        super.addGitConnectedEntity(classEntity, weight);
    }

    public PackageEntity getPackageEntity() {
        return packageEntity;
    }

    public void setSuperClass(ClassEntity superClass) {
        this.superClass = superClass;
        superClass.incrementSize();
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
    protected void addMethod(MethodEntity methodEntity){ // TODO - allow for overloaded methods
        if (getMethod(methodEntity.getName()) == null){
            methods.put(methodEntity.getName(), methodEntity);
            incrementSize();
        } else {
            System.out.println("NOTE, class " + getName() + " already contains method " + methodEntity.getName() );
        }
    }

    public Set<MethodEntity> getMethods() {
        return new LinkedHashSet<>(methods.values());
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
        return methods.getOrDefault(methodName, null);
    }

    @Override
    public boolean containsSearchValue(String searchValue) {

        if (super.containsSearchValue(searchValue)){
            return true;
        }

        // TODO check attributes

        // check methods
        for (MethodEntity methodEntity : methods.values()){
            if (methodEntity.containsSearchValue(searchValue)){
                return true;
            }
        }

        return false;
    }

    /**
     * Only class entities can add commit info
     * @param commitInfo    commit info to add
     */
    @Override
    public void addCommitInfo(CommitInfo commitInfo){
        super.addCommitInfo(commitInfo);

        // add to method?
    }

    public void addField(String objectName, ClassEntity field) {
        this.fields.put(objectName, field);
        incrementSize();
    }

    public HashMap<String, ClassEntity> getFields() {
        return fields;
    }

    @Override
    public String toString() {
        String methodsString = methodEntitySetToString(new LinkedHashSet<>(methods.values()), "Methods");

        String packageName = Entity.entityToString(packageEntity, "Package");
        String superClassName = Entity.entityToString(superClass, "Superclass");
        String fieldsString = classEntityMapToString(fields, "Fields");

        return titleToString() +
                packageName +
                superClassName +
                fieldsString +
                methodsString
                ;
    }

    @Override
    public String getKey() {
        String name = getName();
        if (packageEntity == null){
            return getName();
        } else {
            return packageEntity.getKey() + "." + name;
        }
    }
    /**
     * Check if the class has a field with the given name.
     *
     * @param name Name of the attribute to check for.
     * @return true if the attribute exists, false otherwise.
     */
    public boolean hasAttributeWithName(String name) {
        if (fields.containsKey(name)){
            return true;
        }
        for (ClassEntity field : fields.values()) {
            if (field.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }
}
