package com.laytonsmith.core.clientsupport;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @author Cailin
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@SuppressWarnings("checkstyle:typename")
public @interface permission {
	/**
	 * Returns the permission required by this function. All client functions must be tagged with this, with a
	 * minimum of the NONE permission, or it will not be able to run at all. If multiple permissions are required,
	 * ALL of the list must be granted for this function to have access.
	 * @return
	 */
	ClientPermission[] value();
}
