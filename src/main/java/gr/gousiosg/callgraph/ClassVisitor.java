package gr.gousiosg.callgraph;

import org.apache.bcel.classfile.EmptyVisitor;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.MethodGen;

public class ClassVisitor extends EmptyVisitor {

    JavaClass visitedClass;
    private ConstantPoolGen cp;

    public ClassVisitor(JavaClass jc) {
        visitedClass = jc;
        cp = new ConstantPoolGen(visitedClass.getConstantPool());
    }

    public void visitJavaClass(JavaClass jc) {
        Method[] methods = jc.getMethods();
        for (int i = 0; i < methods.length; i++)
            methods[i].accept(this);
    }

    public void visitMethod(Method method) {
        MethodGen mg = new MethodGen(method, visitedClass.getClassName(), cp);
        MethodVisitor visitor = new MethodVisitor(mg, visitedClass);
        visitor.start(); 
    }
    
    public void start() {
        visitJavaClass(visitedClass);
    }
}
