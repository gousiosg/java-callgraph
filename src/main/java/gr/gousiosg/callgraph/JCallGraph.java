package gr.gousiosg.callgraph;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.bcel.Repository;
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
        ClassParser cp;
        try {
            
            JarFile jar = new JarFile(new File("/System/Library/Java/JavaVirtualMachines/1.6.0.jdk/Contents/Classes/classes.jar"));
            
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.isDirectory())
                    continue;
                
                if (!entry.getName().endsWith(".class"))
                    continue;
                
                cp = new ClassParser("/System/Library/Java/JavaVirtualMachines/1.6.0.jdk/Contents/Classes/classes.jar", entry.getName());
                ClassVisitor visitor = new ClassVisitor(cp.parse());
                visitor.start();
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }   
}
