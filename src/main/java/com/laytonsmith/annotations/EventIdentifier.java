package com.laytonsmith.annotations;

import com.laytonsmith.core.events.Driver;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by jb_aero on 3/22/2015.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EventIdentifier {

	String className();

	String priority();

	Driver event();
}
