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

package gr.gousiosg.javacg.dyn;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.NotFoundException;

public class Instrumenter implements ClassFileTransformer {

    static List<Pattern> pkgIncl = new ArrayList<Pattern>();
    static List<Pattern> pkgExcl = new ArrayList<Pattern>();

    public static void premain(String argument,
            Instrumentation instrumentation) {
        
        //incl=com.foo.*,gr.bar.foo;excl=com.bar.foo.*
        
        String[] tokens = argument.split(";");
        
        if (tokens.length <= 1) {
            System.err.print("Missing delimeter ; from argument:" + tokens);
            return;
        }
        
        for (String token : tokens) {
            String[] args = token.split("="); 
            if (args.length <= 2) {
                System.err.print("Missing argument delimeter =:" + token);
                return;
            }
            
            String argtype = args[0];
            
            if (!argtype.equals("inc") || !argtype.equals("excl")) {
                System.err.print("wrong argument:" + argtype);
                return;
            }
            
            String[] patterns = args[1].split(",");
            
            for (String pattern : patterns) {
                Pattern p = null;
                try {
                    p = Pattern.compile(pattern);
                } catch (PatternSyntaxException pse) {
                    System.err.print("pattern: " + pattern + " not valid, ignoring");
                }
                if (argtype.equals("inc"))
                    pkgIncl.add(p);
                else 
                    pkgExcl.add(p);
            }
        }
        
        instrumentation.addTransformer(new Instrumenter());
    }

    public byte[] transform(ClassLoader loader, String className, Class clazz,
            java.security.ProtectionDomain domain, byte[] bytes) {
        boolean enhanceClass = false;
        
        for (Pattern p : pkgIncl) {
            Matcher m = p.matcher(clazz.getCanonicalName());
            if (m.matches()) {
                enhanceClass = true;
                break;
            }
        }
        
        for (Pattern p : pkgExcl) {
            Matcher m = p.matcher(clazz.getCanonicalName());
            if (m.matches()) {
                enhanceClass = false;
                break;
            }
        }

        if (enhanceClass) {
            return enhanceClass(className, bytes);
        } else {
            return bytes;
        }
    }

    private byte[] enhanceClass(String name, byte[] b) {
        ClassPool pool = ClassPool.getDefault();
        CtClass clazz = null;
        try {
            clazz = pool.makeClass(new java.io.ByteArrayInputStream(b));
            if (!clazz.isInterface()) {
                CtBehavior[] methods = clazz.getDeclaredBehaviors();
                for (int i = 0; i < methods.length; i++) {
                    if (!methods[i].isEmpty()) {
                        enhanceMethod(methods[i], clazz.getName());
                    }
                }
                b = clazz.toBytecode();
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Could not instrument  " + name
                    + ",  exception : " + e.getMessage());
        } finally {
            if (clazz != null) {
                clazz.detach();
            }
        }
        return b;
    }

    private void enhanceMethod(CtBehavior method, String className)
            throws NotFoundException, CannotCompileException {
        method.insertBefore("gr.gousiosg.javacg.dyn.push(\"" + className
                + "\",\"" + method.getName() + "\");");
        method.insertAfter("gr.gousiosg.javacg.dyn.pop();");
    }
}