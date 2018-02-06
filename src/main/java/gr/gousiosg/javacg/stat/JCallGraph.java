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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.bcel.classfile.ClassParser;

/**
 * Constructs a callgraph out of a JAR archive. Can combine multiple archives
 * into a single call graph.
 * 
 * @author Georgios Gousios <gousiosg@gmail.com>
 * 
 */
public class JCallGraph {

    public static void main(String[] args) {
        FormatEnumaration format = FormatEnumaration.TXT;
        PrintStream outputStream = System.out;
        List<String> jars = new ArrayList<>(args.length);
        if (args.length==0) {
            printHelp();
        }
        for (int i =0;i<args.length;i++) {
            String arg = args[i];
            if (arg.startsWith("-")) {
                //additional parameters
                switch (arg) {
                    case "-out" :
                    case "-o" :
                        File file = new File(args[++i]);
                        if (file.exists()) {
                            if (file.isFile()==false) {
                                System.err.println(args[i]+" is not a file");
                                System.exit(-1);
                            }
                            if (file.canWrite()==false) {
                                System.err.println("Cannot write to "+args[i]);
                                System.exit(-1);
                            }
                        } else {
                            try {
                                file.createNewFile();
                            } catch (IOException e) {
                                System.err.println("Cannot create file "+args[i]);
                                System.err.println(e.getMessage());
                                System.exit(-1);
                            }
                        }
                        try {
                            outputStream = new PrintStream(file);
                        } catch (FileNotFoundException e) {
                            System.err.println("Cannot create file "+args[i]);
                            System.err.println(e.getMessage());
                            System.exit(-1);
                        }
                        break;
                    case "-f" :
                    case "-format" :
                        if (i+1<args.length) {
                            String form = args[++i].toLowerCase();
                            switch (form) {
                                case "txt":
                                    format = FormatEnumaration.TXT;
                                    break;
                                case "xml":
                                    format = FormatEnumaration.XML;
                                    break;
                                default:
                                    System.err.println("Unsupported format value "+args[i]);
                                    printHelp();
                                    System.exit(-1);
                            }
                        } else {
                            System.err.println("Format value is not provided");
                            printHelp();
                            System.exit(-1);
                        }
                        break;
                    case "-h" :
                    case "-help" :
                    case "-?" :
                        printHelp();
                        break;
                    default:
                        System.err.println("Unknown parameter "+arg);
                        System.exit(-1);
                }
            } else {
                jars.add(arg);
            }
        }
        ClassParser cp;
        try {
            switch (format) {
                case XML:
                    outputStream.println("<?xml version=\"1.1\" encoding=\"UTF-8\"?>");
                    outputStream.println("<root>");
                    break;
            }
            for (String arg : jars) {

                File f = new File(arg);
                
                if (!f.exists()) {
                    System.err.println("Jar file " + arg + " does not exist");
                }
                
                JarFile jar = new JarFile(f);

                Enumeration<JarEntry> entries = jar.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    if (entry.isDirectory())
                        continue;

                    if (!entry.getName().endsWith(".class"))
                        continue;

                    cp = new ClassParser(arg,entry.getName());
                    ClassVisitor visitor = new ClassVisitor(cp.parse(),outputStream,format);
                    visitor.start();
                }
            }
            switch (format) {
                case XML:
                    outputStream.println("</root>");
                    break;
            }
        } catch (IOException e) {
            System.err.println("Error while processing jar: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void printHelp() {
        System.out.println("Constructs a callgraph out of a JAR archive. Can combine multiple archives into a single call graph");
        System.out.println("See https://github.com/gousiosg/java-callgraph for more details");
        System.out.println("Options:");
        System.out.println("-h, -help, -? This help message");
        System.out.println("-f, -format Set output format. Possible values 'xml' or 'txt' (default)");
        System.out.println("-o, -out File for output. Default - to console");
    }
}
