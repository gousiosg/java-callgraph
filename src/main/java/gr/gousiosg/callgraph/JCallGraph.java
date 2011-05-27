
package gr.gousiosg.callgraph;

import java.io.IOException;

import org.objectweb.asm.ClassReader;

/**
 * Constructs a callgraph out of a JAR archive. Can combine multiple archives
 * into a single call graph.
 * 
 * @author Georgios Gousios <gousiosg@gmail.com>
 * 
 */
public class JCallGraph {

    public static void main(String[] args) {
        try {
            ClassPrinter cp = new ClassPrinter(); 
            ClassReader cr = new ClassReader("java.lang.Thread"); 
            cr.accept(cp, 0);
        } catch (IOException e) {
            
        }
    }
}
