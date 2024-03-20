package codeViz.entity;

import codeViz.codeComplexity.ComplexityDetails;

import java.awt.Color;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

/**
 * Method entity
 *
 * @author Thanuja Sivaananthan
 */
public class MethodEntity extends Entity {

    private final ClassEntity classEntity;

    // cannot easily represent with the class connections alone
    private final HashMap<String, ClassEntity> arguments;
    private final HashMap<String, ClassEntity> localVariables;
    private ClassEntity returnType;

    public MethodEntity(String name, ClassEntity classEntity){
        super(getProperName(name), EntityType.METHOD, new ComplexityDetails());
        this.classEntity = classEntity;

        // should classEntity store its methods (easier to reference), or is that too much coupling?
        classEntity.addMethod(this);

        arguments = new LinkedHashMap<>();
        localVariables = new LinkedHashMap<>();
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
        for (ClassEntity argument : arguments.values()){
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


    public void addArgument(String argumentName, ClassEntity argument) {
        this.arguments.put(argumentName, argument);
        incrementSize();
    }

    public void addLocalVariable(String variableName, ClassEntity className) {
        this.localVariables.put(variableName, className);
        incrementSize();
    }

    public HashMap<String, ClassEntity> getArguments() {
        return arguments;
    }

    public HashMap<String, ClassEntity> getLocalVariables() {
        return localVariables;
    }

    public void setReturnType(ClassEntity returnType) {
        this.returnType = returnType;
        if (!returnType.getName().equals("void")){
            incrementSize();
        }
    }


    @Override
    public String toString() {
        String classString = Entity.entityToString(classEntity, "Class");
        String argumentsString = classEntitySetToString(new LinkedHashSet<>(arguments.values()), "Arguments");
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
