/*
 * Copyright (c) 2011 - Georgios Gousios <gousiosg@gmail.com>
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *
 *     * Redistributions in binary form must reproduce the above
 *       copyright notice, this list of conditions and the following
 *       disclaimer in the documentation and/or other materials provided
 *       with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package gr.gousiosg.javacg.stat;

import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.generic.*;

import java.io.PrintStream;

/**
 * The simplest of method visitors, prints any invoked method
 * signature for all method invocations.
 * 
 * Class copied with modifications from CJKM: http://www.spinellis.gr/sw/ckjm/
 */
public class MethodVisitor extends EmptyVisitor {

    JavaClass visitedClass;
    private MethodGen mg;
    private ConstantPoolGen cp;
    private String format;
    private PrintStream output;
    private FormatEnumaration outputFormat;

    public MethodVisitor(MethodGen m, JavaClass jc, PrintStream output, FormatEnumaration format) {
        visitedClass = jc;
        mg = m;
        cp = mg.getConstantPool();
        this.outputFormat = format;
        this.output = output;
        switch (outputFormat) {
        case TXT:
            this.format = "M:" + visitedClass.getClassName() + ":" + mg.getName() + "(" + argumentList(mg.getArgumentTypes(),false) + ")"
                    + " " + "(%s)%s:%s(%s)";
            break;
        case XML:
            this.format = "<invoke type=\"%s\" reference=\"%s\" call=\"%s\" signature=\"%s\"/>";
            break;
        default:
            throw new RuntimeException("Unsupported format "+outputFormat);
        }
    }

    private String argumentList(Type[] arguments,boolean flat) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < arguments.length; i++) {
            if (flat || outputFormat==FormatEnumaration.TXT) {
                if (i != 0) {
                    sb.append(",");
                }
                sb.append(arguments[i].toString());
            } else {
                switch (outputFormat) {
                    case XML:
                        sb.append("<arg>");
                        sb.append(arguments[i].getSignature());
                        sb.append("</arg>");
                        break;
                    default:
                        throw new RuntimeException("Unsupported format "+outputFormat);
                }
            }
        }
        return sb.toString();
    }

    public void start() {
        if (mg.isAbstract() || mg.isNative())
            return;
        switch (outputFormat) {
            case XML:
                output.print("<method name=\""+ClassVisitor.xmlEscapeText(mg.getName())+"\" signature=\"");
                output.print(argumentList(mg.getArgumentTypes(),true));
                output.println("\">");
                break;
        }
        for (InstructionHandle ih = mg.getInstructionList().getStart();
                ih != null; ih = ih.getNext()) {
            Instruction i = ih.getInstruction();
//            if (outputFormat== FormatEnumaration.XML && i instanceof ConstantPushInstruction) {
//                ConstantPushInstruction pushInstruction = (ConstantPushInstruction)i;
//                output.println("<ConstantPushInstruction>");
//                output.println(pushInstruction.getValue());
//                output.println("</ConstantPushInstruction>");
//            }
            if (!visitInstruction(i))
                i.accept(this);
        }
        switch (outputFormat) {
            case XML:
                output.println("</method>");
                break;
        }
    }

    @Override
    public void visitLCONST(LCONST obj) {
        super.visitLCONST(obj);
    }

    private boolean visitInstruction(Instruction i) {
        short opcode = i.getOpcode();
        return ((InstructionConst.getInstruction(opcode) != null)
                && !(i instanceof ConstantPushInstruction) 
                && !(i instanceof ReturnInstruction));
    }

//    @Override
//    public void visitLocalVariableInstruction(LocalVariableInstruction obj) {
//        switch (outputFormat) {
//            case XML:
//                output.println("<local name=\""+obj.getName()+"\">"+obj.toString(true)+"</local>");
//                break;
//        }
//    }

    @Override
    public void visitINVOKEVIRTUAL(INVOKEVIRTUAL i) {
        output.println(String.format(format,"M",i.getReferenceType(cp), ClassVisitor.xmlEscapeText(i.getMethodName(cp)),argumentList(i.getArgumentTypes(cp),true)));
    }

    @Override
    public void visitINVOKEINTERFACE(INVOKEINTERFACE i) {
        output.println(String.format(format,"I",i.getReferenceType(cp),ClassVisitor.xmlEscapeText(i.getMethodName(cp)),argumentList(i.getArgumentTypes(cp),true)));
    }

    @Override
    public void visitINVOKESPECIAL(INVOKESPECIAL i) {
        output.println(String.format(format,"O",i.getReferenceType(cp),ClassVisitor.xmlEscapeText(i.getMethodName(cp)),argumentList(i.getArgumentTypes(cp),true)));
    }

    @Override
    public void visitINVOKESTATIC(INVOKESTATIC i) {
        output.println(String.format(format,"S",i.getReferenceType(cp),ClassVisitor.xmlEscapeText(i.getMethodName(cp)),argumentList(i.getArgumentTypes(cp),true)));
    }

    @Override
    public void visitINVOKEDYNAMIC(INVOKEDYNAMIC i) {
        output.println(String.format(format,"D",i.getType(cp),ClassVisitor.xmlEscapeText(i.getMethodName(cp)),
                argumentList(i.getArgumentTypes(cp),true)));
    }
}
