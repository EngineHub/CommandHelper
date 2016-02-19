
package com.laytonsmith.annotations;

import com.laytonsmith.core.events.BindableEvent;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * External extension points with an event handler should tag the methods that receive
 * an event. The event handlers will receive an abstracted event object. The method
 * must take one parameter, an object that extends {@link BindableEvent}. If the event
 * handler is an extension point that does not include abstracted objects, the extension
 * must make other arrangements to trigger itself. See {@link startup}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Deprecated // Use @api class MyClass extends AbstractEvent instead!
public @interface event {
	
}
