package com.laytonsmith.core.objects;

import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.FullyQualifiedClassName;
import com.laytonsmith.core.constructs.NativeTypeList;
import com.laytonsmith.core.natives.interfaces.Mixed;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;
/**
 * Contains the underlying definitions of all classes in the ecosystem. Both user created classes and native classes are
 * added to this list, with the exception that native classes are persisted during Java compile time, so that there is a
 * smaller startup time.
 */
public final class ObjectDefinitionTable implements Iterable<ObjectDefinition> {

	/**
	 * The String is the fully qualified class name. There are convenience methods for accepting a FQCN to get the
	 * value, but the underlying item is a String.
	 */
	private final Map<String, ObjectDefinition> classList;
	private volatile boolean nativeTypesAdded = false;

	private ObjectDefinitionTable() {
		// Go ahead and set the size to that of the native class list, whether or not it actually gets added
		classList = new java.util.concurrent.ConcurrentHashMap<>(NativeTypeList.getNativeTypeList().size());
	}

	/**
	 * Returns a new instance of the ObjectDefinitionTable with the native classes pre-populated. This is the usual
	 * use case for creating a new instance.
	 * @return
	 */
	public static ObjectDefinitionTable GetNewInstance() {
		ObjectDefinitionTable table = new ObjectDefinitionTable();
		table.addNativeTypes();
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
	 */
	public void addNativeTypes() {
		if(nativeTypesAdded) {
			return;
		}
		for(FullyQualifiedClassName fqcn : NativeTypeList.getNativeTypeList()) {
			try {
				if(fqcn.getFQCN().equals("void") || fqcn.getFQCN().equals("null")) {
					continue;
				}
				add(new ObjectDefinition(fqcn));
			} catch (ClassNotFoundException | DuplicateObjectDefintionException ex) {
				throw new Error(ex);
			}
		}
		nativeTypesAdded = true;
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
			return get(clazz.getAnnotation(typeof.class).value());
		} catch (ObjectDefinitionNotFoundException ex) {
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
			if(this.classList.get(object.getClassName()).exactlyEquals(object)) {
				msg += " The object appears to be an identical definition, is code running twice that shouldn't?";
				isCopy = true;
			}
			throw new DuplicateObjectDefintionException(msg, isCopy);
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
