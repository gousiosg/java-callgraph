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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class Graph {

    private static Stack<String> stack = new Stack<String>();
    private static Map<Pair<String, String>, Integer> callgraph = new HashMap<Pair<String,String>, Integer>();
    
    static {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                //Sort by number of calls
                List<Pair<String, String>> keys = new ArrayList<Pair<String, String>>();
                keys.addAll(callgraph.keySet());
                Collections.sort(keys, new Comparator<Object>() {
                    public int compare(Object o1, Object o2) {
                        Integer v1 = callgraph.get(o1);
                        Integer v2 = callgraph.get(o2);
                        return v1.compareTo(v2);
                    }
                });
                
                for (Pair<String, String> key : keys) {
                    System.err.println(key + " " + callgraph.get(key));
                }
            }
        });
    }

    public static void push(String callname) {
        if (!stack.isEmpty()) {
            Pair<String, String> p = new Pair<String, String>(stack.peek(), callname); 
            if (callgraph.containsKey(p))
                callgraph.put(p, callgraph.get(p) + 1);
            else
                callgraph.put(p, 1);
        }
        stack.push(callname);
    }

    public static void pop() {
        stack.pop();
    }
}