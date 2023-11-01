package org.example;

import org.objectweb.asm.*;

/*
Useful links:
https://stackoverflow.com/questions/61807758/how-to-read-a-java-class-method-annotation-value-with-asm
https://asm.ow2.io/javadoc/org/objectweb/asm/MethodVisitor.html
 */


public class ConnectedClassVisitor extends ClassVisitor {
    public ConnectedClassVisitor() {
        super(Opcodes.ASM8);
    }

    static class ConnectedMethodVisitor extends MethodVisitor {
        ConnectedMethodVisitor() {
            super(Opcodes.ASM8);
        }

//        @Override // not high priority, can add missing connections/increase weights after
//        public void visitLocalVariable(String name, String descriptor, String signature, Label start, Label end, int index) {
//            System.out.println("HELLO, local variable " + name + " has descriptor : " + descriptor );
//            super.visitLocalVariable(name, descriptor, signature, start, end, index);
//        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
//            System.out.println("HELLO, method " + opcode + " " + owner + " " + name + " " + descriptor);
            // opcode meaning? 182 = Opcodes.INVOKEVIRTUAL
            if (opcode == Opcodes.INVOKEVIRTUAL){
                System.out.println("methodInsn + " + opcode + " :class: " + owner + " :name: " + name );
            }
            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
        }
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc,
                                     String signature, String[] exceptions) {
        System.out.println("method: name = " + name);
        return new ConnectedMethodVisitor();
    }
}
