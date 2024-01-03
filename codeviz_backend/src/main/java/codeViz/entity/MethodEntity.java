package codeViz.entity;

import java.awt.Color;
import java.util.HashSet;

/**
 * Method entity
 *
 * @author Thanuja Sivaananthan
 */
public class MethodEntity extends Entity {

    private final ClassEntity classEntity;

    // cannot easily represent with the class connections alone
    private final HashSet<ClassEntity> arguments;
    private ClassEntity returnType;

    public MethodEntity(String name, ClassEntity classEntity){
        super(getProperName(name), EntityType.METHOD);
        this.classEntity = classEntity;

        // should classEntity store its methods (easier to reference), or is that too much coupling?
        classEntity.addMethod(this);

        arguments = new HashSet<>();
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
}
