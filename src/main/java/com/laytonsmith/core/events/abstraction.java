

package com.laytonsmith.core.events;

import com.laytonsmith.abstraction.Implementation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This tag is to denote that a class is a implementation of a specific abstraction interface.
 * This isn't always needed however, only when a reverse lookup may need to be done should
 * this be needed.
 * @author layton
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface abstraction {
    Implementation.Type type();
}
