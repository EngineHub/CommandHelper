package com.laytonsmith.annotations;

import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.Documentation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Some elements are best suited for using an annotation instead of a runtime function
 * for providing the information provided by the Documentation interface.
 * @author lsmith
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD})
public @interface documentation {
	
	/**
	 * The name, as would be returned by {@link Documentation#getName()}.
	 * By default, returns empty string, which is usually used to signal to the
	 * annotation user that the field/method/class name should be used instead.
	 * @return 
	 */
	String name() default "";
	
	/**
	 * The docs, as would be returned by {@link Documentation#docs()}.
	 * @return 
	 */
	String docs();
	
	/**
	 * The version, as would be returned by {@link Documentation#since()}.
	 * By default, returns {@link CHVersion#V0_0_0}, which is usually used to signal
	 * to the annotation user that the default value should be used.
	 * @return 
	 */
	CHVersion since() default CHVersion.V0_0_0;
	
}
