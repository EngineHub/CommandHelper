
package com.laytonsmith.core.constructs;

import com.laytonsmith.annotations.typeof;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A resource is a large or mutable data structure that is kept in memory with
 * external resource management. This makes certain things more efficient, like
 * string builders, xml parser, streams, etc, at the cost of making user code slightly more
 * complicated. Therefore, this is a stopgap measure that WILL be removed at some point,
 * once Objects are created.
 */
@typeof("resource")
public class CResource<T> extends Construct {
	private static final AtomicLong resourcePool = new AtomicLong(0);
	
	private final long id;
	private final T resource;
	private final ResourceToString toString;
	
	public CResource(final T resource, Target t){
		this(resource, new ResourceToString() {

			public String getString(CResource id) {
				return "resource@" + id.getId() + ":" + id.getResource().toString();
			}
		}, t);
	}
	
	public CResource(T resource, ResourceToString toString, Target t){
		super("", ConstructType.RESOURCE, t);
		this.resource = resource;
		if(toString == null){
			throw new NullPointerException();
		}
		this.toString = toString;
		id = resourcePool.incrementAndGet();
	}
	
	public long getId(){
		return id;
	}
	
	public T getResource(){
		return resource;
	}

	@Override
	public String val() {
		return toString.getString(this);
	}

	@Override
	public String toString() {
		return val();
	}

	@Override
	public boolean isDynamic() {
		return true;
	}
	
	public static interface ResourceToString {
		/**
		 * Returns a toString for the underlying object.
		 * @param self The actual resource being toString'd.
		 * @return 
		 */
		String getString(CResource self);
	}
	
}
