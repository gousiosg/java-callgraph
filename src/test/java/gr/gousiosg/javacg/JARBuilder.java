package gr.gousiosg.javacg;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

public class JARBuilder {
	private static final String TEMP_DIR = System.getProperty("java.io.tmpdir");

	private final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
	private final DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
	private final StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);
	private final LinkedList<JavaFileObject> compilationUnits = new LinkedList<>();
	private final Collection<File> classFiles = new LinkedList<>();

	public JARBuilder() throws IOException {
		fileManager.setLocation(StandardLocation.CLASS_OUTPUT, Arrays.asList(new File(TEMP_DIR)));
	}

	public void add(String className, String classCode) throws IOException {
		compilationUnits.add(createJavaFile(className, classCode));
		classFiles.add(new File(TEMP_DIR, className + ".class"));
	}

	public File build() throws FileNotFoundException, IOException {
		CompilationTask task = compiler.getTask(null, fileManager, diagnostics, null, null, compilationUnits);
		boolean success = task.call();
		if (!success) {
			displayDiagnostic(diagnostics);
			throw new RuntimeException("Cannot compile classes for the JAR");
		}

		File file = File.createTempFile("test", ".jar");
		JarOutputStream jar = new JarOutputStream(new FileOutputStream(file), createManifest());
		for (File classFile : classFiles) {
			add(classFile, jar);
		}
		jar.close();
		return file;
	}

	private void displayDiagnostic(DiagnosticCollector<JavaFileObject> diagnostics) {
		for (Diagnostic<?> diagnostic : diagnostics.getDiagnostics()) {
			JavaSourceFromString sourceClass = (JavaSourceFromString) diagnostic.getSource();
			System.err.println("-----");
			System.err.println("Source: " + sourceClass.getName());
			System.err.println("Message: " + diagnostic.getMessage(null));
			System.err.println("Position: " + diagnostic.getPosition());
			System.err.println(diagnostic.getKind() + " " + diagnostic.getCode());
		}
	}

	private JavaFileObject createJavaFile(String className, String classCode) throws IOException {
		StringWriter writer = new StringWriter();
		writer.append(classCode);
		writer.close();
		JavaFileObject file = new JavaSourceFromString(className, writer.toString());
		return file;
	}

	private Manifest createManifest() {
		Manifest manifest = new Manifest();
		manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
		return manifest;
	}

	private void add(File classFile, JarOutputStream jar) throws IOException {
		JarEntry entry = new JarEntry(classFile.getPath().replace("\\", "/"));
		jar.putNextEntry(entry);
		try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(classFile))) {
			byte[] buffer = new byte[1024];
			while (true) {
				int count = in.read(buffer);
				if (count == -1)
					break;
				jar.write(buffer, 0, count);
			}
			jar.closeEntry();
		}
	}
}

class JavaSourceFromString extends SimpleJavaFileObject {
	final String code;

	JavaSourceFromString(String name, String code) throws IOException {
		super(URI.create("string:///" + name.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
		this.code = code;
	}

	@Override
	public CharSequence getCharContent(boolean ignoreEncodingErrors) {
		return code;
	}
}