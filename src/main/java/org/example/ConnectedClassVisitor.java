package org.example;

import org.example.entity.ClassEntity;
import org.example.entity.Entity;
import org.example.entity.MethodEntity;
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

            // the following opcodes should definitely be checked (could restrict to just these opcodes if performance becomes an issue)
            // 182 - Opcodes.INVOKEVIRTUAL
            // 183 - Opcodes.INVOKESPECIAL
            // 184 - Opcodes.INVOKESTATIC

            // NOTE: bcel uses . in the class name; asm uses / in the owner name; chose bcel as the standard
            String connectedClassName = owner.replace("/", ".");
            String connectedMethodName = connectedClassName + "." + name.replace("<", "").replace(">", "");
            // System.out.println("methodInsn : " + opcode + " :method: " + connectedMethodName);

            Entity connectedClass = graphGenerator.getClassEntities().get(connectedClassName);
            // if the class exists, the method should probably also exist
            if (connectedClass != null) {
                // System.out.println("Connected class exists! " + connectedClassName);
                MethodEntity connectedMethod = (MethodEntity) graphGenerator.getMethodEntities().get(connectedMethodName);
                if (connectedMethod != null) {
                    // System.out.println("Connected method exists! " + opcode + " " + connectedMethodName);
                    if (methodEntity != null) {
                        methodEntity.addConnectedEntity(connectedMethod);
                    }
                } else {
                    // TODO - might be from an inherited method
                    System.out.println("ERROR, Method is null: " + connectedMethodName);
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
