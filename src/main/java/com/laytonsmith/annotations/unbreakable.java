package com.laytonsmith.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A function that is unbreakable can't be "broken" from using the break() function. Most functions are ambivalent to
 * breaks travelling up them, but some specifically cause errors. This information is used by the compiler to detect if
 * break is being used correctly in the user code.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SuppressWarnings("checkstyle:typename") // Fixing this violation might break dependents.
public @interface unbreakable {

}
