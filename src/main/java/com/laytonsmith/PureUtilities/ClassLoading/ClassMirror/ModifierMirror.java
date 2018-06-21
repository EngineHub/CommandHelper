package com.laytonsmith.PureUtilities.ClassLoading.ClassMirror;

import com.laytonsmith.PureUtilities.Common.StringUtils;
import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import org.objectweb.asm.Opcodes;

/**
 * This is a mirror for the {@link java.lang.reflect.Modifier} class. This provides is* methods for all the modifiers,
 * instead of requiring bitwise logic to determine if various modifiers are present.
 */
public class ModifierMirror implements Serializable {

	private static final long serialVersionUID = 1L;
	private final int access;
	private int modifiers = 0;
	/**
	 * This is the canonical order of modifiers, used in the toString method.
	 */
	private static final transient Object[] ORDER = new Object[]{
		Modifier.PUBLIC, "public",
		Modifier.PRIVATE, "private",
		Modifier.PROTECTED, "protected",
		Modifier.STATIC, "static",
		Modifier.FINAL, "final",
		Modifier.SYNCHRONIZED, "synchronized",
		Modifier.VOLATILE, "volatile",
		Modifier.TRANSIENT, "transient",
		Modifier.NATIVE, "native",
		Modifier.INTERFACE, "interface",
		Modifier.ABSTRACT, "abstract",
		Modifier.STRICT, "strictfp"};

	/**
	 * This constructor is used when mirroring an already loaded class. The modifier is just the modifier returned by
	 * Class.
	 *
	 * @param modifier
	 */
	public ModifierMirror(int modifier) {
		this.access = 0;
		this.modifiers = modifier;
	}

	/**
	 * Creates a new ModifierMirror. Type is needed to determine which flags are applicable, and access is the access
	 * modifier stored with the class file.
	 *
	 * @param type Since asm encodes the parameters slightly differently for various types, the type must also be
	 * provided.
	 * @param access
	 */
	public ModifierMirror(Type type, int access) {
		this.access = access;
		//public, private, protected, final are all valid on all three types
		if(type == Type.CLASS || type == Type.METHOD || type == Type.FIELD) {
			if(hasFlag(Opcodes.ACC_PRIVATE)) {
				modifiers |= Modifier.PRIVATE;
			}
			if(hasFlag(Opcodes.ACC_PROTECTED)) {
				modifiers |= Modifier.PROTECTED;
			}
			if(hasFlag(Opcodes.ACC_PUBLIC)) {
				modifiers |= Modifier.PUBLIC;
			}
			if(hasFlag(Opcodes.ACC_FINAL)) {
				modifiers |= Modifier.FINAL;
			}
		}
		//static is only valid on fields and methods
		if(type == Type.FIELD || type == Type.METHOD) {
			if(hasFlag(Opcodes.ACC_STATIC)) {
				modifiers |= Modifier.STATIC;
			}
		}
		//interface is only valid on a class
		if(type == Type.CLASS) {
			if(hasFlag(Opcodes.ACC_INTERFACE)) {
				modifiers |= Modifier.INTERFACE;
			}
		}
		//abstract is only valid on classes or methods
		if(type == Type.CLASS || type == Type.METHOD) {
			if(hasFlag(Opcodes.ACC_ABSTRACT)) {
				modifiers |= Modifier.ABSTRACT;
			}
		}
		//native, strict, and synchronized are only valid on methods
		if(type == Type.METHOD) {
			if(hasFlag(Opcodes.ACC_NATIVE)) {
				modifiers |= Modifier.NATIVE;
			}
			if(hasFlag(Opcodes.ACC_STRICT)) {
				modifiers |= Modifier.STRICT;
			}
			if(hasFlag(Opcodes.ACC_SYNCHRONIZED)) {
				modifiers |= Modifier.SYNCHRONIZED;
			}
		}
		//transient and volatile are only valid on fields
		if(type == Type.FIELD) {
			if(hasFlag(Opcodes.ACC_TRANSIENT)) {
				modifiers |= Modifier.TRANSIENT;
			}
			if(hasFlag(Opcodes.ACC_VOLATILE)) {
				modifiers |= Modifier.VOLATILE;
			}
		}
	}

	private boolean hasFlag(int flag) {
		return (access & flag) > 0;
	}

	/**
	 * Returns true if the abstract modifier was present.
	 *
	 * @return
	 */
	public boolean isAbstract() {
		return (modifiers & Modifier.ABSTRACT) > 0;
	}

	/**
	 * Returns true if the final modifier was present
	 *
	 * @return
	 */
	public boolean isFinal() {
		return (modifiers & Modifier.FINAL) > 0;
	}

	/**
	 * Returns true if the class is an interface
	 *
	 * @return
	 */
	public boolean isInterface() {
		return (modifiers & Modifier.ABSTRACT) > 0;
	}

	/**
	 * Returns true if the native modifier was present
	 *
	 * @return
	 */
	public boolean isNative() {
		return (modifiers & Modifier.NATIVE) > 0;
	}

	/**
	 * Returns true if the private modifier was present
	 *
	 * @return
	 */
	public boolean isPrivate() {
		return (modifiers & Modifier.PRIVATE) > 0;
	}

	/**
	 * Returns true if the protected modifier was present
	 *
	 * @return
	 */
	public boolean isProtected() {
		return (modifiers & Modifier.PROTECTED) > 0;
	}

	/**
	 * Returns true if the public modifier was present
	 *
	 * @return
	 */
	public boolean isPublic() {
		return (modifiers & Modifier.PUBLIC) > 0;
	}

	/**
	 * Returns true if this is package private, that is, it is not public, private, or protected. No access modifier is
	 * present, rather this is the lack of the other three modifiers.
	 *
	 * @return
	 */
	public boolean isPackagePrivate() {
		return !(isPrivate()
				|| isProtected()
				|| isPublic());
	}

	/**
	 * Returns true if the static modifier was present
	 *
	 * @return
	 */
	public boolean isStatic() {
		return (modifiers & Modifier.STATIC) > 0;
	}

	/**
	 * Returns true if the strictfp modifier was present
	 *
	 * @return
	 */
	public boolean isStrict() {
		return (modifiers & Modifier.STRICT) > 0;
	}

	/**
	 * Returns true if the synchronized modifier was present
	 *
	 * @return
	 */
	public boolean isSynchronized() {
		return (modifiers & Modifier.SYNCHRONIZED) > 0;
	}

	/**
	 * Returns true if the transient modifier was present
	 *
	 * @return
	 */
	public boolean isTransient() {
		return (modifiers & Modifier.TRANSIENT) > 0;
	}

	/**
	 * Returns true if the volatile modifier was present
	 *
	 * @return
	 */
	public boolean isVolatile() {
		return (modifiers & Modifier.VOLATILE) > 0;
	}

	/**
	 * Returns an int compatible with the {@link java.lang.reflect.Modifier} class.
	 *
	 * @return
	 */
	public int getModifiers() {
		return modifiers;
	}

	@Override
	public String toString() {
		List<String> build = new ArrayList<String>();
		for(int i = 0; i < ORDER.length; i++) {
			int type = (Integer) ORDER[i];
			String name = (String) ORDER[++i];
			if((modifiers & type) > 0) {
				build.add(name);
			}
		}
		return StringUtils.Join(build, " ");
	}

	public static enum Type {
		CLASS, METHOD, FIELD;
	}

}
