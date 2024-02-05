package codeViz.entity;

import java.awt.Color;
import java.util.HashSet;
import java.util.Set;

/**
 * Method entity
 *
 * @author Thanuja Sivaananthan
 */
public class MethodEntity extends Entity {

    private final ClassEntity classEntity;

    // cannot easily represent with the class connections alone
    private final Set<ClassEntity> arguments;
    private ClassEntity returnType;

    public MethodEntity(String name, ClassEntity classEntity){
        super(getProperName(name), EntityType.METHOD);
        this.classEntity = classEntity;

        // should classEntity store its methods (easier to reference), or is that too much coupling?
        classEntity.addMethod(this);

        arguments = new HashSet<>();
        returnType = null;
    }

    public static String getProperName(String name){
        return name.replace("<", "").replace(">", "");
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
        if (isHighlighed()){ // being highlighted takes precedence over the parent
            return getHighlighedColour();
        }
        return classEntity.getColour();
    }


    @Override
    public boolean containsSearchValue(String searchValue) {

        if (super.containsSearchValue(searchValue)){
            return true;
        }

        // check arguments
        for (ClassEntity argument : arguments){
            if (argument.nameContains(searchValue)){
                return true;
            }
        }

        // check return type
        if (returnType != null && returnType.nameContains(searchValue)){
            return true;
        }

        return false;
    }


    public void addArgument(ClassEntity argument) {
        this.arguments.add(argument);
    }

    public void setReturnType(ClassEntity returnType) {
        this.returnType = returnType;
    }


    @Override
    public String toString() {
        String classString = Entity.entityToString(classEntity, "Class");
        String argumentsString = classEntitySetToString(arguments, "Arguments");
        String returnTypeName = Entity.entityToString(returnType, "Return Type");

        return titleToString() +
                classString +
                argumentsString +
                returnTypeName
                ;
    }

    @Override
    public String getKey() {
        String name = getName();
        return classEntity.getKey() + "." + name;
    }
}
