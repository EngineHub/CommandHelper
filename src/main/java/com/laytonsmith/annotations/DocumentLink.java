package com.laytonsmith.annotations;

import com.laytonsmith.core.functions.AbstractFunction;
import com.laytonsmith.core.functions.DocumentLinkProvider;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A function tagged with DocumentLink has a parameter that, if a constant string, links to a file that could
 * or must be in the local file system. This is used by the language server to display a link to the file.
 *
 * This annotation can only be used if the function extends {@link AbstractFunction}, implements
 * {@link DocumentLinkProvider}, and the given parameter locations are always the same. If the location of the file
 * parameters can change based on the type of arguments, this cannot be used, and instead the method in
 * DocumentLinkProvider must be overwritten.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DocumentLink {
	/**
	 * The location(s) of parameters which are links to local files. Zero indexed. If the argument index is not present,
	 * it is not an error, but no child will be returned.
	 * @return
	 */
	int[] value();
}
