java-callgraph: Java Call Graph Utils
=====================================

A suite of programs for generating static and dynamic call graphs in Java.

* javacg-static: Reads classes from a jar file, walks down the method bodies and
   prints a table of caller-caller relationships.
* javacg-dynamic: Runs as a [Java agent](http://download.oracle.com/javase/6/docs/api/index.html?java/lang/instrument/package-summary.html) and instruments
  the methods of a user-defined set of classes in order to track their invocations.
  At JVM exit, prints a table of caller-callee relationships.

#### Compile

The java-callgraph package is build with maven. Install maven and do: 

<code>
mvn install
</code>

This will produce a `target` directory with two executable jars:

#### Examples

The following examples instrument the 
[Dacapo benchmark suite](http://dacapobench.org/) to produce dynamic call graphs. 
The Dacapo benchmarks come in a single big jar archive that contains all dependency
libraries. To build the boot class path required for the javacg-dyn program, 
extract the `dacapo.jar` to a directory: all the required libraries can be found
in the `jar` directory.

<code>
java 
    -Xbootclasspath:/System/Library/Java/JavaVirtualMachines/1.6.0.jdk/Contents/Classes/classes.jar:jar/batik-all.jar:jar/xml-apis-ext.jar 
    -javaagent:target/javacg-0.1-SNAPSHOT-dycg-agent.jar="incl=org.apache.batik.*,org.w3c.*;" 
    -jar dacapo-9.12-bach.jar batik -s small
</code>

<code>
java -Xbootclasspath:/System/Library/Java/JavaVirtualMachines/1.6.0.jdk/Contents/Classes/classes.jar:jar/lucene-core-2.4.jar:jar/luindex.jar -javaagent:target/javacg-0.1-SNAPSHOT-dycg-agent.jar="incl=org.apache.lucene.*;" -jar dacapo-9.12-bach.jar luindex  -s small
</code>

#### Known Restrictions

* The static call graph generator does not account for methods invoked via
  reflection.
  
#### Author

Georgios Gousios <gousiosg@gmail.com>

#### License

2-clause BSD
