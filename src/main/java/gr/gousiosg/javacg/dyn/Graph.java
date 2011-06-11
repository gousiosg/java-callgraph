package gr.gousiosg.javacg.dyn;

import java.util.Stack;

public class Graph {

    private static Stack<String> stack = new Stack<String>();

    public static void pushNode(String className, String methodName) {
        String entry = className + ":" + methodName;
        System.err.println(stack.peek() + " " + entry);
        stack.push(entry);
    }

    public static void popNode() {
        stack.pop();
    }
}