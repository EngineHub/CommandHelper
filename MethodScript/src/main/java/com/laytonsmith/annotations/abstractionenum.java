
package com.laytonsmith.annotations;

import com.laytonsmith.abstraction.Implementation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An abstraction enum marks an enum upon which there exists an abstraction connection.
 * At startup, a check is done to see if there are more (or less) enums in the runtime's real layer
 * vs the number that is defined for it. If so, a warning is issued, which should assist
 * in detecting a missing implementation mapping.
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface abstractionenum {
	/**
	 * Returns the Implementation type this is valid for
	 * @return 
	 */
	public Implementation.Type implementation();

	/**
	 * The class of the abstraction layer enum with which this convertor maps between.
	 * For instance, MCAction.class
	 * @return 
	 */
	public Class<? extends Enum> forAbstractEnum();
	
	/**
	 * The class of the implementation layer enum with which this convertor maps between.
	 * For instance, Action.class (in bukkit)
	 * @return 
	 */
	public Class<? extends Enum> forConcreteEnum();
}
