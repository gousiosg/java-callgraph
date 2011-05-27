package gr.gousiosg.callgraph;

import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.generic.ArrayInstruction;
import org.apache.bcel.generic.CHECKCAST;
import org.apache.bcel.generic.CodeExceptionGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.ConstantPushInstruction;
import org.apache.bcel.generic.EmptyVisitor;
import org.apache.bcel.generic.FieldInstruction;
import org.apache.bcel.generic.INSTANCEOF;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionConstants;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.LocalVariableInstruction;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.ReturnInstruction;
import org.apache.bcel.generic.Type;

public class MethodVisitor extends EmptyVisitor {

    JavaClass visitedClass;
    private MethodGen mg;
    private ConstantPoolGen cp;

    public MethodVisitor(MethodGen m, JavaClass jc) {
        visitedClass = jc;
        mg = m;
        cp = mg.getConstantPool();
    }

    public void start() {
        if (!mg.isAbstract() && !mg.isNative()) {
            for (InstructionHandle ih = mg.getInstructionList().getStart(); ih != null; ih = ih.getNext()) {
                Instruction i = ih.getInstruction();

                if (!visitInstruction(i))
                    i.accept(this);
            }
            updateExceptionHandlers();
        }
    }

    /** Visit a single instruction. */
    private boolean visitInstruction(Instruction i) {
        short opcode = i.getOpcode();

        return ((InstructionConstants.INSTRUCTIONS[opcode] != null)
                && !(i instanceof ConstantPushInstruction) && !(i instanceof ReturnInstruction));
    }

    /** Local variable use. */
    public void visitLocalVariableInstruction(LocalVariableInstruction i) {
        //if (i.getOpcode() != Constants.IINC)
            //cv.registerCoupling(i.getType(cp));
    }

    /** Array use. */
    public void visitArrayInstruction(ArrayInstruction i) {
        //cv.registerCoupling(i.getType(cp));
    }

    /** Field access. */
    public void visitFieldInstruction(FieldInstruction i) {
        //cv.registerFieldAccess(i.getClassName(cp), i.getFieldName(cp));
        //cv.registerCoupling(i.getFieldType(cp));
    }

    /** Method invocation. */
    public void visitInvokeInstruction(InvokeInstruction i) {
        System.out.println(visitedClass.getClassName() + ":" + mg.getName() + "->" +  i.getReferenceType(cp) + ":"+ i.getMethodName(cp));
        //Type[] argTypes = i.getArgumentTypes(cp);
        //for (int j = 0; j < argTypes.length; j++)
        //    cv.registerCoupling(argTypes[j]);
        //cv.registerCoupling(i.getReturnType(cp));
        /* Measuring decision: measure overloaded methods separately */
        //cv.registerMethodInvocation(i.getClassName(cp), i.getMethodName(cp),
        //        argTypes);
    }

    /** Visit an instanceof instruction. */
    public void visitINSTANCEOF(INSTANCEOF i) {
        
    }

    /** Visit checklast instruction. */
    public void visitCHECKCAST(CHECKCAST i) {
       
    }

    /** Visit return instruction. */
    public void visitReturnInstruction(ReturnInstruction i) {
    }

    /** Visit the method's exception handlers. */
    private void updateExceptionHandlers() {
        CodeExceptionGen[] handlers = mg.getExceptionHandlers();

        /* Measuring decision: couple exceptions */
        for (int i = 0; i < handlers.length; i++) {
            Type t = handlers[i].getCatchType();
            //if (t != null)
                //cv.registerCoupling(t);
        }
    }
}
