
package com.laytonsmith.database;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Classes marked with this interface are automatically found and added to the
 * DB instance list. Alternatively, classes can call DB.addDBInstance() manually to
 * add (or possibly override) existing class types.
 */
@Target(value = ElementType.TYPE)
@Retention(value = RetentionPolicy.RUNTIME)
public @interface DBConnector {

	/**
	 * This is the sql type that this class represents, for instance "mysql".
	 * @return
	 */
	String value();

}
