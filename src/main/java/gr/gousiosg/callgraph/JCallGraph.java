
package gr.gousiosg.callgraph;

import java.io.IOException;

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
            cp = new ClassParser("/Volumes/Files/Developer/java-callgraph/target/classes/gr/gousiosg/callgraph/ClassVisitor.class");
            ClassVisitor visitor = new ClassVisitor(cp.parse());
            visitor.start();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }   
}
