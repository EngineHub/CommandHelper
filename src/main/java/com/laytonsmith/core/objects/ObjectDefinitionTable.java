package com.laytonsmith.core.objects;

import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscovery;
import com.laytonsmith.PureUtilities.Common.FileUtil;
import com.laytonsmith.PureUtilities.Common.StackTraceUtils;
import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.PureUtilities.ZipReader;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.FullyQualifiedClassName;
import com.laytonsmith.core.MethodScriptCompiler;
import com.laytonsmith.core.compiler.TokenStream;
import com.laytonsmith.core.constructs.NativeTypeList;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigCompileGroupException;
import com.laytonsmith.core.natives.interfaces.Mixed;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Spliterator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
/**
 * Contains the underlying definitions of all classes in the ecosystem. Both user created classes and native classes are
 * added to this list, with the exception that native classes are parsed and optimized at Java compile time, to save
 * startup time. The native classes must be defined within the src/methodscript folder. Eventually, all references to
 * the native classes using the {@link NativeTypeList} will go away, and be replaced by this object, which will contain
 * all known classes, including "native" classes written purely in MethodScript, as well as user classes (at runtime.)
 */
public final class ObjectDefinitionTable implements Iterable<ObjectDefinition> {

	/**
	 * The String is the fully qualified class name. There are convenience methods for accepting a FQCN to get the
	 * value, but the underlying item is a String.
	 */
	private final Map<String, ObjectDefinition> classList;
	private final AtomicBoolean nativeTypesAdded = new AtomicBoolean(false);

	private ObjectDefinitionTable() {
		// Go ahead and set the size to that of the native class list, whether or not it actually gets added
		classList = new java.util.concurrent.ConcurrentHashMap<>(NativeTypeList.getNativeTypeList().size());
	}

	/**
	 * Returns a new instance of the ObjectDefinitionTable with the native classes pre-populated. This is the usual
	 * use case for creating a new instance.
	 * @param env The environment. It MUST include a CompilerEnvironment. The GlobalEnv may be a mock environment.
	 * @param envs The target runtime environments
	 * @return
	 */
	public static ObjectDefinitionTable GetNewInstance(Environment env,
			Set<Class<? extends Environment.EnvironmentImpl>> envs) {
		ObjectDefinitionTable table = new ObjectDefinitionTable();
		table.addNativeTypes(env, envs);
		return table;
	}

	/**
	 * Unless you have a special use case, use {@link #GetNewInstance()} instead of this method.
	 * <p>
	 * In normal use cases, the native types should all be added. However, for testing and other meta use cases, it
	 * may be desirable to have a totally blank table. This method returns such an instance.
	 * @return A new, empty table.
	 */
	public static ObjectDefinitionTable GetBlankInstance() {
		return new ObjectDefinitionTable();
	}

	/**
	 * Adds the native types to the instance. If this has already been called, it is not an error to call it again,
	 * but no changes will be made.
	 * @param env The environment. It MUST include a CompilerEnvironment. The GlobalEnv may be a mock environment.
	 * @param envs The target runtime environments
	 */
	public void addNativeTypes(Environment env, Set<Class<? extends Environment.EnvironmentImpl>> envs) {
		if(nativeTypesAdded.compareAndSet(false, true)) {
			// We have two types of onboarding, normal onboarding, and fallback onboarding.
			// Normal onboarding is when the native code was properly compiled in, and we
			// hydrate from a pre-compiled version of the native code. This should be the
			// normal operation, but if for whatever reason that file isn't present, we
			// still want to properly manage. Fallback onboarding is when we scan the
			// native source files and compile them ourselves. This is actually the normal
			// code path when compiling the java code, so we do need to support this anyhow.
			List<Exception> oops = new ArrayList<>();
			if(ObjectDefinitionTable.class.getResource("/nativeSource.ser") != null) {
				// Normal onboarding
				throw new UnsupportedOperationException();
			} else {
				// Fallback onboarding
				Queue<File> msFiles = new LinkedList<>();
				try {
					Queue<File> q = new LinkedList<>();
					File root = new File(ObjectDefinitionTable.class.getResource("/nativeSource").toExternalForm());
					ZipReader reader = new ZipReader(root);
					q.addAll(Arrays.asList(reader.listFiles()));
					// Flatten the structure, so we get a full list of files (no directories)
					while(q.peek() != null) {
						ZipReader r = new ZipReader(q.peek());
						if(r.isDirectory()) {
							q.addAll(Arrays.asList(r.listFiles()));
							q.poll();
						} else {
							msFiles.add(q.poll());
						}
					}
				} catch (IOException ex) {
					oops.add(ex);
				}
				msFiles.stream()
					.parallel()
					.forEach((file) -> {
						try {
							String script = FileUtil.read(file);
							TokenStream ts = MethodScriptCompiler.lex(script, env, file, true);
							MethodScriptCompiler.compile(ts, env, envs);
						} catch (ConfigCompileGroupException g) {
							oops.addAll(g.getList());
						} catch (IOException | ConfigCompileException ex) {
							oops.add(ex);
						}
					});
				if(!oops.isEmpty()) {
					List<String> b = new ArrayList<>();
					for(Exception e : oops) {
						if(e instanceof ConfigCompileException) {
							ConfigCompileException cce = (ConfigCompileException) e;
							b.add(cce.getMessage() + " " + cce.getFile() + ":"
									+ cce.getLineNum() + "." + cce.getColumn());
						} else {
							b.add(e.getMessage() + "\n" + StackTraceUtils.GetStacktrace(e));
						}
					}
					throw new Error("One or more exceptions occured while trying to compile the native files!\n"
						+ StringUtils.Join(b, "\n"));
				}
			}
		}
	}

	@Override
	public Iterator<ObjectDefinition> iterator() {
		return classList.values().iterator();
	}

	@Override
	public void forEach(Consumer<? super ObjectDefinition> action) {
		classList.values().forEach(action);
	}

	@Override
	public Spliterator<ObjectDefinition> spliterator() {
		return classList.values().spliterator();
	}

	private ObjectDefinition get(String fullyQualifiedClassName) throws ObjectDefinitionNotFoundException {
		if(classList.containsKey(fullyQualifiedClassName)) {
			return classList.get(fullyQualifiedClassName);
		} else {
			throw new ObjectDefinitionNotFoundException(fullyQualifiedClassName + " could not be found.");
		}
	}

	/**
	 * Returns the ObjectDefinition for the given type. If the type cannot be found, an exception is thrown.
	 * @param fullyQualifiedClassName The FQCN to find
	 * @return The ObjectDefinition
	 * @throws ObjectDefinitionNotFoundException If the type cannot be found, because it has not yet been registered.
	 */
	public ObjectDefinition get(FullyQualifiedClassName fullyQualifiedClassName)
			throws ObjectDefinitionNotFoundException {
		return get(fullyQualifiedClassName.getFQCN());
	}

	/**
	 * Returns the ObjectDefinition for a native class. It is generally impossible for this to be missing an
	 * ObjectDefintion, since that is generated automatically, so the underlying
	 * {@link ObjectDefinitionNotFoundException} that would normally be thrown is re-wrapped as an Error.
	 * @param clazz The Java Class to get an ObjectDefinition for
	 * @return The underlying ObjectDefinition
	 */
	public ObjectDefinition get(Class<? extends Mixed> clazz) {
		try {
			return get(ClassDiscovery.GetClassAnnotation(clazz, typeof.class).value());
		} catch (ObjectDefinitionNotFoundException ex) {
			throw new Error("Missing ObjectDefinition for native class: " + clazz.getName(), ex);
		}
	}

	/**
	 * Adds a new ObjectDefinition to the ObjectTable.
	 * @param object The ObjectDefinition to add.
	 * @param t
	 * @throws DuplicateObjectDefintionException If an object with this name already exists.
	 */
	public void add(ObjectDefinition object, Target t) throws DuplicateObjectDefintionException {
		if(this.classList.containsKey(object.getClassName())) {
			String msg = "The class " + object.getClassName() + " was attempted to be redefined.";
			boolean isCopy = false;
			if(this.classList.get(object.getClassName()).exactlyEquals(object)) {
				msg += " The object appears to be an identical definition, is code running twice that shouldn't?";
				isCopy = true;
			}
			throw new DuplicateObjectDefintionException(msg, t, isCopy);
		}
		this.classList.put(object.getClassName(), object);
	}

	/**
	 * Considered a "meta" operation, this should never be called in the course of normal operation, as it is an
	 * expensive operation. It should only be called if the user code specifically uses reflection mechanisms.
	 * <p>
	 * While a copy is returned, it is intentionally shallow. It is not expected that new classes are added or any
	 * removed, but the internal references may be changed, within the allowed limits of ObjectDefinition.
	 * @return A copy of the ObjectDefinitionTable
	 */
	public Set<ObjectDefinition> getObjectDefinitionSet() {
		return new HashSet<>(classList.values());
	}


}
