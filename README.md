java-callgraph: Java Call Graph Utilities
=========================================

A suite of programs for generating static and dynamic call graphs in Java.

* javacg-static: Reads classes from a jar file, walks down the method bodies and
   prints a table of caller-caller relationships.
* javacg-dynamic: Runs as a [Java agent](http://download.oracle.com/javase/6/docs/api/index.html?java/lang/instrument/package-summary.html) and instruments
  the methods of a user-defined set of classes in order to track their invocations.
  At JVM exit, prints a table of caller-callee relationships, along with a number
  of calls

#### Compile

The java-callgraph package is build with maven. Install maven and do:

```
mvn install
```

This will produce a `target` directory with the following three jars:
- javacg-0.1-SNAPSHOT.jar: This is the standard maven packaged jar with static and dynamic call graph generator classes
- `javacg-0.1-SNAPSHOT-static.jar`: This is an executable jar which includes the static call graph generator
- `javacg-0.1-SNAPSHOT-dycg-agent.jar`: This is an executable jar which includes the dynamic call graph generator

#### Run

Instructions for running the callgraph generators

##### Static

`javacg-static` accepts as arguments the jars to analyze.

```
java -jar javacg-0.1-SNAPSHOT-static.jar lib1.jar lib2.jar...
```

`javacg-static` produces combined output in the following format:

###### For methods

```
  M:class1:<method1>(arg_types) (typeofcall)class2:<method2>(arg_types)
```
###### For static options

`javacg-static` includes option -m or --modifier which will provide the comma seperated modifier(s) (public, static, private, etc...) for the methods and classes

for example:
```
  java -jar javacg-0.1-SNAPSHOT-static.jar -m lib1.jar lib2.jar...
  
  M:<modifiers>:class1:<method1>(arg_types) (typeofcall)class2:<method2>(arg_types)
```

The line means that `method1` of `class1` called `method2` of `class2`.
The type of call can have one of the following values (refer to
the [JVM specification](http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.doc6.html)
for the meaning of the calls):

 * `M` for `invokevirtual` calls
 * `I` for `invokeinterface` calls
 * `O` for `invokespecial` calls
 * `S` for `invokestatic` calls
 * `D` for `invokedynamic` calls

For `invokedynamic` calls, it is not possible to infer the argument types.

###### For classes

```
  C:class1 class2
```

This means that some method(s) in `class1` called some method(s) in `class2`.

##### Dynamic

`javacg-dynamic` uses
[javassist](http://www.csg.is.titech.ac.jp/~chiba/javassist/) to insert probes
at method entry and exit points. To be able to analyze a class `javassist` must
resolve all dependent classes at instrumentation time. To do so, it reads
classes from the JVM's boot classloader. By default, the JVM sets the boot
classpath to use Java's default classpath implementation (`rt.jar` on
Win/Linux, `classes.jar` on the Mac). The boot classpath can be extended using
the `-Xbootclasspath` option, which works the same as the traditional
`-classpath` option. It is advisable for `javacg-dynamic` to work as expected,
to set the boot classpath to the same, or an appropriate subset, entries as the
normal application classpath.

Moreover, since instrumenting all methods will produce huge callgraphs which
are not necessarily helpful (e.g. it will include Java's default classpath
entries), `javacg-dynamic` includes support for restricting the set of classes
to be instrumented through include and exclude statements. The options are
appended to the `-javaagent` argument and has the following format

```
-javaagent:javacg-dycg-agent.jar="incl=mylib.*,mylib2.*,java.nio.*;excl=java.nio.charset.*"
```

The example above will instrument all classes under the the `mylib`, `mylib2` and
`java.nio` namespaces, except those that fall under the `java.nio.charset` namespace.

```
java
-Xbootclasspath:/System/Library/Java/JavaVirtualMachines/1.6.0.jdk/Contents/Classes/classes.jar:mylib.jar
-javaagent:javacg-0.1-SNAPSHOT-dycg-agent.jar="incl=mylib.*;"
-classpath mylib.jar mylib.Mainclass
```

`javacg-dynamic` produces two kinds of output. On the standard output, it
writes method call pairs as shown below:

```
class1:method1 class2:method2 numcalls
```

It also produces a file named `calltrace.txt` in which it writes the entry
and exit timestamps for methods, thereby turning `javacg-dynamic` into
a poor man's profiler. The format is the following:

```
<>[stack_depth][thread_id]fqdn.class:method=timestamp_nanos
```

The output line starts with a `<` or `>` depending on whether it is a method
entry or exit. It then writes the stack depth, thread id and the class and
method name, followed by a timestamp. The provided `process_trace.rb`
script processes the callgraph output to generate total time per method
information.

#### Examples

The following examples instrument the
[Dacapo benchmark suite](http://dacapobench.org/) to produce dynamic call graphs.
The Dacapo benchmarks come in a single big jar archive that contains all dependency
libraries. To build the boot class path required for the javacg-dyn program,
extract the `dacapo.jar` to a directory: all the required libraries can be found
in the `jar` directory.

Running the batik Dacapo benchmark:

```
java -Xbootclasspath:/System/Library/Java/JavaVirtualMachines/1.6.0.jdk/Contents/Classes/classes.jar:jar/batik-all.jar:jar/xml-apis-ext.jar -javaagent:target/javacg-0.1-SNAPSHOT-dycg-agent.jar="incl=org.apache.batik.*,org.w3c.*;" -jar dacapo-9.12-bach.jar batik -s small |tail -n 10
```
<br/>

```
[...]
org.apache.batik.dom.AbstractParentNode:appendChild org.apache.batik.dom.AbstractParentNode:fireDOMNodeInsertedEvent 6270<br/>
org.apache.batik.dom.AbstractParentNode:fireDOMNodeInsertedEvent org.apache.batik.dom.AbstractDocument:getEventsEnabled 6280<br/>
org.apache.batik.dom.AbstractParentNode:checkAndRemove org.apache.batik.dom.AbstractNode:getOwnerDocument 6280<br/>
org.apache.batik.dom.util.DoublyIndexedTable:put org.apache.batik.dom.util.DoublyIndexedTable$Entry:DoublyIndexedTable$Entry 6682<br/>
org.apache.batik.dom.util.DoublyIndexedTable:put org.apache.batik.dom.util.DoublyIndexedTable:hashCode 6693<br/>
org.apache.batik.dom.AbstractElement:invalidateElementsByTagName org.apache.batik.dom.AbstractElement:getNodeType 7198<br/>
org.apache.batik.dom.AbstractElement:invalidateElementsByTagName org.apache.batik.dom.AbstractDocument:getElementsByTagName 14396<br/>
org.apache.batik.dom.AbstractElement:invalidateElementsByTagName org.apache.batik.dom.AbstractDocument:getElementsByTagNameNS 28792<br/>
```

Running the lucene Dacapo benchmark:

```
java -Xbootclasspath:/System/Library/Java/JavaVirtualMachines/1.6.0.jdk/Contents/Classes/classes.jar:jar/lucene-core-2.4.jar:jar/luindex.jar -javaagent:target/javacg-0.1-SNAPSHOT-dycg-agent.jar="incl=org.apache.lucene.*;" -jar dacapo-9.12-bach.jar luindex -s small |tail -n 10
```
<br/><br/>

```
[...]
org.apache.lucene.analysis.Token:setTermBuffer org.apache.lucene.analysis.Token:growTermBuffer 43449<br/>
org.apache.lucene.analysis.CharArraySet:getSlot org.apache.lucene.analysis.CharArraySet:getHashCode 43472<br/>
org.apache.lucene.analysis.CharArraySet:getSlot org.apache.lucene.analysis.CharArraySet:equals 46107<br/>
org.apache.lucene.index.FreqProxTermsWriter:appendPostings org.apache.lucene.store.IndexOutput:writeVInt 46507<br/>
org.apache.lucene.store.IndexInput:readVInt org.apache.lucene.index.ByteSliceReader:readByte 63927<br/>
org.apache.lucene.index.TermsHashPerField:writeVInt org.apache.lucene.index.TermsHashPerField:writeByte 63927<br/>
org.apache.lucene.store.IndexOutput:writeVInt org.apache.lucene.store.BufferedIndexOutput:writeByte 94239<br/>
org.apache.lucene.index.TermsHashPerField:quickSort org.apache.lucene.index.TermsHashPerField:comparePostings 107343<br/>
org.apache.lucene.analysis.Token:termBuffer org.apache.lucene.analysis.Token:initTermBuffer 162115<br/>
org.apache.lucene.analysis.Token:termLength org.apache.lucene.analysis.Token:initTermBuffer 205554<br/>
```

#### Known Restrictions

* The static call graph generator does not account for methods invoked via
  reflection.
* The dynamic call graph generator will not work reliably (or at all) for
  multithreaded programs
* The dynamic call graph generator does not handle exceptions very well, so some
methods might appear as having never returned

#### Author

Georgios Gousios <gousiosg@gmail.com>

#### License

[2-clause BSD](http://www.opensource.org/licenses/bsd-license.php)
