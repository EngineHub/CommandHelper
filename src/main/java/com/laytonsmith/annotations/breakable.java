package com.laytonsmith.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A function that is breakable can be "broken" from using the break() function. Most functions are ambivalent to breaks
 * travelling up them, but some specifically support them. This information is used by the compiler to detect if break
 * is being used correctly in the user code.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface breakable {

}
