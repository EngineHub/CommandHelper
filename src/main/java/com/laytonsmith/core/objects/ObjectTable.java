package com.laytonsmith.core.objects;

import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.FullyQualifiedClassName;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.NativeTypeList;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.natives.interfaces.Mixed;
import java.io.File;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Contains the underlying definitions of all classes in the ecosystem. Both user created classes and native classes are
 * added to this list, with the exception that native classes are persisted during Java compile time, so that there is a
 * smaller startup time.
 */
public class ObjectTable implements Iterable<ObjectDefinition> {

	/**
	 * The String is the fully qualified class name. There are convenience methods for accepting a FQCN to get the
	 * value, but the underlying item is a String.
	 */
	private final Map<String, ObjectDefinition> classList = new java.util.concurrent.ConcurrentHashMap<>();

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
			return get(clazz.getAnnotation(typeof.class).value());
		} catch(ObjectDefinitionNotFoundException ex) {
			throw new Error("Missing ObjectDefinition for native class: " + clazz.getName(), ex);
		}
	}

	/**
	 * Adds a new ObjectDefinition to the ObjectTable.
	 * @param object The ObjectDefinition to add.
	 * @throws DuplicateObjectDefintionException If an object with this name already exists.
	 */
	public void add(ObjectDefinition object) throws DuplicateObjectDefintionException {
		if(this.classList.containsKey(object.getClassName())) {
			String msg = "The class " + object.getClassName() + " was attempted to be redefined.";
			boolean isCopy = false;
			if(this.classList.get(object.getClassName()).equals(object)) {
				msg += " The object appears to be an identical definition, is code running twice that shouldn't?";
				isCopy = true;
			}
			throw new DuplicateObjectDefintionException(msg, isCopy);
		}
		this.classList.put(object.getClassName(), object);
	}


}
