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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
    private List<String> methodCalls = new ArrayList<>();

    public MethodVisitor(MethodGen m, JavaClass jc) {
        visitedClass = jc;
        mg = m;
        cp = mg.getConstantPool();
        format = "M:" + visitedClass.getClassName() + ":" + mg.getName() + "(" + argumentList(mg.getArgumentTypes()) + ")"
            + " " + "(%s)%s:%s(%s)";
    }

    private String argumentList(Type[] arguments) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < arguments.length; i++) {
            if (i != 0) {
                sb.append(",");
            }
            sb.append(arguments[i].toString());
        }
        return sb.toString();
    }

    public List<String> start() {
        if (mg.isAbstract() || mg.isNative())
            return Collections.emptyList();

        for (InstructionHandle ih = mg.getInstructionList().getStart(); 
                ih != null; ih = ih.getNext()) {
            Instruction i = ih.getInstruction();
            
            if (!visitInstruction(i))
                i.accept(this);
        }
        return methodCalls;
    }

    private boolean visitInstruction(Instruction i) {
        short opcode = i.getOpcode();
        return ((InstructionConst.getInstruction(opcode) != null)
                && !(i instanceof ConstantPushInstruction) 
                && !(i instanceof ReturnInstruction));
    }

    @Override
    public void visitINVOKEVIRTUAL(INVOKEVIRTUAL i) {
        methodCalls.add(String.format(format,"M",i.getReferenceType(cp),i.getMethodName(cp),argumentList(i.getArgumentTypes(cp))));
    }

    @Override
    public void visitINVOKEINTERFACE(INVOKEINTERFACE i) {
        methodCalls.add(String.format(format,"I",i.getReferenceType(cp),i.getMethodName(cp),argumentList(i.getArgumentTypes(cp))));
    }

    @Override
    public void visitINVOKESPECIAL(INVOKESPECIAL i) {
        methodCalls.add(String.format(format,"O",i.getReferenceType(cp),i.getMethodName(cp),argumentList(i.getArgumentTypes(cp))));
    }

    @Override
    public void visitINVOKESTATIC(INVOKESTATIC i) {
        methodCalls.add(String.format(format,"S",i.getReferenceType(cp),i.getMethodName(cp),argumentList(i.getArgumentTypes(cp))));
    }

    @Override
    public void visitINVOKEDYNAMIC(INVOKEDYNAMIC i) {
        methodCalls.add(String.format(format,"D",i.getType(cp),i.getMethodName(cp),
                argumentList(i.getArgumentTypes(cp))));
    }
}
