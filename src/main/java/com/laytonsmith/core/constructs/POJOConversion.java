package com.laytonsmith.core.constructs;

import com.laytonsmith.core.natives.interfaces.Mixed;

/**
 * Mixed objects that @NonInheritImplements this can support seamless conversion in some places, allowing simplified
 * code in some places. In order to be able to construct the object, an invalid instance will be constructed, and then
 * the construct() method will be called, with the type sent along.
 * @param <MyType> The type of this class
 * @param <JavaType> The POJO type that is supported.
 */
public interface POJOConversion<MyType extends Mixed, JavaType> {
	/**
	 * Constructs a valid instance of
	 * @param pojo The plain java type that should be used as the basis.
	 * @param t The code target that this should be constructed with.
	 * @return
	 */
	MyType construct(JavaType pojo, Target t);
	/**
	 * Returns a Java type based on this type.
	 * @return
	 */
	JavaType convert();
}
