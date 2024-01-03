package codeViz;

import codeViz.entity.MethodEntity;
import codeViz.entity.ClassEntity;
import codeViz.entity.Entity;
import org.objectweb.asm.*;

/**
 * Extension of the ASM ClassVisitor class
 * Updates the methodEntity connections for a given graphGenerator
 * @author Thanuja Sivaananthan
 */
public class ConnectedClassVisitor extends ClassVisitor {
    private final GraphGenerator graphGenerator;
    private final ClassEntity classEntity;

    /**
     * Create new ConnectedClassVisitor
     * @author Thanuja Sivaananthan
     *
     * @param graphGenerator    the graphGenerator to update
     * @param classEntity       the classEntity of the ClassVisitor
     */
    public ConnectedClassVisitor(GraphGenerator graphGenerator, ClassEntity classEntity) {
        super(Opcodes.ASM8);
        this.graphGenerator = graphGenerator;
        this.classEntity = classEntity;
    }

    /**
     * Extension of the ASM ClassMethod class
     * Updates the methodEntity connections for a given graphGenerator
     * @author Thanuja Sivaananthan
     */
     static class ConnectedMethodVisitor extends MethodVisitor {

        private final GraphGenerator graphGenerator;
        private final MethodEntity methodEntity;

        /**
         * Create new ConnectedMethodVisitor
         * @author Thanuja Sivaananthan
         *
         * @param graphGenerator    the graphGenerator to update
         * @param methodEntity       the methodEntity of the MethodVisitor
         */
        ConnectedMethodVisitor(GraphGenerator graphGenerator, MethodEntity methodEntity) {
            super(Opcodes.ASM8);
            this.graphGenerator = graphGenerator;
            this.methodEntity = methodEntity;
        }

        /**
         * Visits a method instruction, and adds connected methodEntities together
         * @author Thanuja Sivaananthan
         */
        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {

            boolean addedMethod = false;

            // the following opcodes should definitely be checked (could restrict to just these opcodes if performance becomes an issue)
            // 182 - Opcodes.INVOKEVIRTUAL
            // 183 - Opcodes.INVOKESPECIAL
            // 184 - Opcodes.INVOKESTATIC

            // NOTE: bcel uses . in the class name; asm uses / in the owner name; chose bcel as the standard
            String connectedClassName = owner.replace("/", ".");
            String connectedMethodName = MethodEntity.getProperName(name);
            String fullConnectedMethodName = connectedClassName + "." + connectedMethodName;
            // System.out.println("methodInsn : " + opcode + " :method: " + fullConnectedMethodName);

            Entity connectedClass = graphGenerator.getClassEntities().get(connectedClassName);
            // if the class exists, the method should probably also exist
            if (connectedClass != null) {
                // System.out.println("Connected class exists! " + connectedClassName);
                MethodEntity connectedMethod = (MethodEntity) graphGenerator.getMethodEntities().get(fullConnectedMethodName);
                if (connectedMethod != null) {
                    // System.out.println("Connected method exists! " + opcode + " " + fullConnectedMethodName);
                    if (methodEntity != null) {
                        if (methodEntity.equals(connectedMethod)){ // FIXME - figure out how to handle this (ex. a method recursively calling itself)
                            System.out.println("NOTE, circular reference with class " + methodEntity.getName() + " calling " + fullConnectedMethodName);
                        }
                        methodEntity.addConnectedEntity(connectedMethod);
                        addedMethod = true;
                    }
                } else {
                    // might be from an inherited method, check superclass
                    ClassEntity superclass = methodEntity.getClassEntity().getSuperClass();
                    if (superclass != null){
                        connectedMethod = superclass.getMethod(connectedMethodName);
                        if (connectedMethod != null) {
                            // System.out.println("Connected method exists! " + opcode + " " + fullConnectedMethodName);
                            if (methodEntity.equals(connectedMethod)) { // FIXME - figure out how to handle this (ex. a method recursively calling itself)
                                System.out.println("NOTE, circular reference with class " + methodEntity.getName() + " calling " + fullConnectedMethodName);
                            }
                            methodEntity.addConnectedEntity(connectedMethod);
                            addedMethod = true;
                            System.out.println("Note, Method is from superclass: " + fullConnectedMethodName);
                        }
                    }
                }

                if (!addedMethod){
                    System.out.println("ERROR, Method is null: " + fullConnectedMethodName);
                }

            }

            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
        }
    }

    /**
     * Visits a method of a class
     * @author Thanuja Sivaananthan
     */
    @Override
    public MethodVisitor visitMethod(int access, String name, String desc,
                                     String signature, String[] exceptions) {
        MethodEntity methodEntity = classEntity.getMethod(name);
        if (methodEntity == null){ // this should never happen (the methods should have already been created)
            System.out.println("ERROR, METHOD ENTITY IS NULL: " + classEntity.getName() + " " + name);
        }
        return new ConnectedMethodVisitor(graphGenerator, classEntity.getMethod(name));
    }
}
