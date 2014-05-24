package com.laytonsmith.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to tag data store implementations.
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface datasource {
    /**
     * Returns the protocol handler associated with this data source.
     * @return 
     */
    String value();
}
