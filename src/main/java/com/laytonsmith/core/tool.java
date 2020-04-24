/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * CommandLineTools must be tagged with this annotation in order to be fully entered into the tool ecosystem.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@SuppressWarnings("checkstyle:typename")
public @interface tool {

	/**
	 * This is the tool name, i.e. when the user types mscript -- &lt;tool&gt;, what this should be. This may not
	 * contain any spaces. To prevent conflicts, it is highly recommended that when this is added from an extension,
	 * it start with {@code x-<extension-name>}. There is a guarantee that no tools defined directly in MethodScript
	 * will start with {@code x-}, and hopefully all extension authors follow the same protocol.
	 * @return
	 */
	String value();

	/**
	 * If the tool has aliases, then they can be returned here. If there are none, that is allowed too.
	 * @return
	 */
	String[] aliases() default {};

	/**
	 * Default to false, but if set to true, does not show up in the help file, though will still be shown if
	 * specifically requested. This is meant for incubating features.
	 * @return
	 */
	boolean undocumented() default false;

}
