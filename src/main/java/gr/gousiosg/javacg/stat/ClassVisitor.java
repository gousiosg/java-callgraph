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

import org.apache.bcel.classfile.*;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.MethodGen;

import java.io.PrintStream;

/**
 * The simplest of class visitors, invokes the method visitor class for each
 * method found.
 */
public class ClassVisitor extends EmptyVisitor {

    private JavaClass clazz;
    private ConstantPoolGen constants;
    private String classReferenceFormat;
    private PrintStream output;
    private FormatEnumaration format;
    private ConstantPool currentConstantPool;

    public ClassVisitor(JavaClass jc, PrintStream outputStream, FormatEnumaration format) {
        clazz = jc;
        this.format = format;
        this.output = outputStream;
        constants = new ConstantPoolGen(clazz.getConstantPool());
        switch (format) {
            case TXT:
                classReferenceFormat = "C:" + clazz.getClassName() + " %s";
                break;
            case XML:
                classReferenceFormat = "<constant tag=\"%s\">%s</constant>";
                break;
            default:
                throw new RuntimeException("Unsupported format "+format);
        }

    }

    public void visitJavaClass(JavaClass jc) {
        switch (format) {
            case XML:
                output.println("<class name=\""+jc.getClassName()+"\">");
                String[] names = jc.getInterfaceNames();
                if (names != null && names.length>0) {
                    output.println("<interfaces>");
                    for (String name:names) {
                        output.print("<interface>");
                        output.print(name);
                        output.println("</interface>");
                    }
                    output.println("</interfaces>");
                }
                break;
        }
        jc.getConstantPool().accept(this);
        Method[] methods = jc.getMethods();
        for (int i = 0; i < methods.length; i++) {
            methods[i].accept(this);
        }
        switch (format) {
            case XML:
                output.println("</class>");
                break;
        }
    }

    public void visitConstantPool(ConstantPool constantPool) {
        switch (format) {
            case TXT:
                for (int i = 0; i < constantPool.getLength(); i++) {
                    Constant constant = constantPool.getConstant(i);
                    if (constant == null)
                        continue;
                    if (constant.getTag() == 7) {
                        String referencedClass =
                                constantPool.constantToString(constant);
                        output.println(String.format(classReferenceFormat,
                                referencedClass));
                    }
                }
            case XML:
                for (int i = 0; i < constantPool.getLength(); i++) {
                    Constant constant = constantPool.getConstant(i);
                    if (constant == null)
                        continue;
                    if (constant.getTag() == 7) {
                        output.print("<constant index=\"");
                        output.print(i);
                        output.print("\">");
                        String referencedClass =
                                constantPool.constantToString(constant);
                        output.print(xmlEscapeText(referencedClass));
                        output.println("</constant>");
                    }
                }
        }
    }

    public void visitMethod(Method method) {
        MethodGen mg = new MethodGen(method, clazz.getClassName(), constants);
        MethodVisitor visitor = new MethodVisitor(mg, clazz,output,format);
        visitor.start(); 
    }

    public void start() {
        visitJavaClass(clazz);
    }

    /**
     * Encode special charaters for XML
     * @param t
     * @return
     */
    static String xmlEscapeText(String t) {
        StringBuilder sb = null;
        for(int i = 0; i < t.length(); i++){
            char c = t.charAt(i);
            switch(c){
                case '<':
                    if (sb==null) {
                        sb = new StringBuilder(t.length()+10);
                        sb.append(t.substring(0,i));
                    }
                    sb.append("&lt;");
                    break;
                case '>':
                    if (sb==null) {
                        sb = new StringBuilder(t.length()+10);
                        sb.append(t.substring(0,i));
                    }
                    sb.append("&gt;");
                    break;
                case '\"':
                    if (sb==null) {
                        sb = new StringBuilder(t.length()+10);
                        sb.append(t.substring(0,i));
                    }
                    sb.append("&quot;");
                    break;
                case '&':
                    if (sb==null) {
                        sb = new StringBuilder(t.length()+10);
                        sb.append(t.substring(0,i));
                    }
                    sb.append("&amp;");
                    break;
                case '\'':
                    if (sb==null) {
                        sb = new StringBuilder(t.length()+10);
                        sb.append(t.substring(0,i));
                    }
                    sb.append("&apos;");
                    break;
                default:
                    if(c>0x7e || c<0x20) {
                        if (sb==null) {
                            sb = new StringBuilder(t.length()+10);
                            sb.append(t.substring(0,i));
                        }
                        sb.append("&#"+((int)c)+";");
                    }else if (sb != null) {
                        sb.append(c);
                    }
            }
        }
        if (sb==null) {
            //re-use original string
            return t;
        } else {
            return sb.toString();
        }
    }
}
