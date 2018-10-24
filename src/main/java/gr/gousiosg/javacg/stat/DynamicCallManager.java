/*
 * Written in 2018 by Matthieu Vergne <matthieu.vergne@gmail.com>
 *
 *     To the extent possible under law, the author(s) have dedicated all
 *     copyright and related and neighboring rights to this software to
 *     the public domain worldwide. This software is distributed without
 *     any warranty.
 *
 *     You should have received a copy of the CC0 Public Domain Dedication
 *     along with this software. If not,
 *     see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package gr.gousiosg.javacg.stat;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.bcel.classfile.Attribute;
import org.apache.bcel.classfile.BootstrapMethod;
import org.apache.bcel.classfile.BootstrapMethods;
import org.apache.bcel.classfile.ConstantCP;
import org.apache.bcel.classfile.ConstantMethodHandle;
import org.apache.bcel.classfile.ConstantNameAndType;
import org.apache.bcel.classfile.ConstantPool;
import org.apache.bcel.classfile.ConstantUtf8;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;

/**
 * {@link DynamicCallManager} provides facilities to retrieve information about
 * dynamic calls statically.
 * <p>
 * Most of the time, call relationships are explicit, which allows to properly
 * build the call graph statically. But in the case of dynamic linking, i.e.
 * <code>invokedynamic</code> instructions, this relationship might be unknown
 * until the code is actually executed. Indeed, bootstrap methods are used to
 * dynamically link the code at first call. One can read details about the
 * <a href=
 * "https://docs.oracle.com/javase/8/docs/technotes/guides/vm/multiple-language-support.html#invokedynamic"><code>invokedynamic</code>
 * instruction</a> to know more about this mechanism.
 * <p>
 * Nested lambdas are particularly subject to such absence of concrete caller,
 * which lead us to produce method names like <code>lambda$null$0</code>, which
 * breaks the call graph. This information can however be retrieved statically
 * through the code of the bootstrap method called.
 * <p>
 * In {@link #retrieveCalls(Method, JavaClass)}, we retrieve the (called,
 * caller) relationships by analyzing the code of the caller {@link Method}.
 * This information is then used in {@link #linkCalls(Method)} to rename the
 * called {@link Method} properly.
 *
 * @author Matthieu Vergne <matthieu.vergne@gmail.com>
 */
public class DynamicCallManager {
    private static final Pattern BOOTSTRAP_CALL_PATTERN = Pattern
            .compile("invokedynamic\t(\\d+):\\S+ \\S+ \\(\\d+\\)");
    private static final int CALL_HANDLE_INDEX_ARGUMENT = 1;

    private final Map<String, String> dynamicCallers = new HashMap<>();

    /**
     * Retrieve dynamic call relationships based on the code of the provided
     * {@link Method}.
     *
     * @param method {@link Method} to analyze the code
     * @param jc     {@link JavaClass} info, which contains the bootstrap methods
     * @see #linkCalls(Method)
     */
    public void retrieveCalls(Method method, JavaClass jc) {
        if (method.isAbstract() || method.isNative()) {
            // No code to consider
            return;
        }
        ConstantPool cp = method.getConstantPool();
        BootstrapMethod[] boots = getBootstrapMethods(jc);
        String code = method.getCode().toString();
        Matcher matcher = BOOTSTRAP_CALL_PATTERN.matcher(code);
        while (matcher.find()) {
            int bootIndex = Integer.parseInt(matcher.group(1));
            BootstrapMethod bootMethod = boots[bootIndex];
            int calledIndex = bootMethod.getBootstrapArguments()[CALL_HANDLE_INDEX_ARGUMENT];
            String calledName = getMethodNameFromHandleIndex(cp, calledIndex);
            String callerName = method.getName();
            dynamicCallers.put(calledName, callerName);
        }
    }

    private String getMethodNameFromHandleIndex(ConstantPool cp, int callIndex) {
        ConstantMethodHandle handle = (ConstantMethodHandle) cp.getConstant(callIndex);
        ConstantCP ref = (ConstantCP) cp.getConstant(handle.getReferenceIndex());
        ConstantNameAndType nameAndType = (ConstantNameAndType) cp.getConstant(ref.getNameAndTypeIndex());
        return nameAndType.getName(cp);
    }

    /**
     * Link the {@link Method}'s name to its concrete caller if required.
     *
     * @param method {@link Method} to analyze
     * @see #retrieveCalls(Method, JavaClass)
     */
    public void linkCalls(Method method) {
        int nameIndex = method.getNameIndex();
        ConstantPool cp = method.getConstantPool();
        String methodName = ((ConstantUtf8) cp.getConstant(nameIndex)).getBytes();
        String linkedName = methodName;
        String callerName = methodName;
        while (linkedName.matches("(lambda\\$)+null(\\$\\d+)+")) {
            callerName = dynamicCallers.get(callerName);
            linkedName = linkedName.replace("null", callerName);
        }
        cp.setConstant(nameIndex, new ConstantUtf8(linkedName));
    }

    private BootstrapMethod[] getBootstrapMethods(JavaClass jc) {
        for (Attribute attribute : jc.getAttributes()) {
            if (attribute instanceof BootstrapMethods) {
                return ((BootstrapMethods) attribute).getBootstrapMethods();
            }
        }
        return new BootstrapMethod[]{};
    }
}
