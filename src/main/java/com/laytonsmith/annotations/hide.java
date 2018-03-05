package com.laytonsmith.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * If present, this annotation indicates that the class, method or field should be undocumented. This generally
 * indicates that the item is likely going to be subject to incompatible changes or removal, or is otherwise unstable or
 * untested. Items generally should not be hidden if they were ever exposed, but instead should be deprecated and still
 * publicly exposed for one release cycle or more, then either hidden or removed.
 */
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE, ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface hide {

	/**
	 * The reason why this element is hidden. This is likely only meant as a type of comment on the element itself, but
	 * could potentially show up in general documentation in some cases.
	 *
	 * @return
	 */
	String value();
}
