
package com.laytonsmith.core.constructs;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.CHVersion;
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

	/**
	 * Constructs a new CResource, given some underlying object.
	 * @param resource
	 * @param t
	 */
	public CResource(final T resource, Target t){
		this(resource, new ResourceToString() {

			@Override
			public String getString(CResource id) {
				// This is the original implementation of Object.toString()
				String original = id.getResource().getClass().getName() + "@"
						+ Integer.toHexString(id.getResource().hashCode());
				String addendum = "";
				if(!original.equals(id.getResource().toString())){
					addendum = original + ":";
				}
				return "resource@" + id.getId() + ":" 
						+ addendum
						+ id.getResource().toString();
			}
		}, t);
	}

	/**
	 * Constructs a new CResource, given some underlying object. The ResourceToString object allows you to override
	 * how this object is toString'd.
	 * @param resource
	 * @param toString
	 * @param t
	 */
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

	@Override
	public String docs() {
		return "A resource is a value that represents an underlying native object. The object cannot be accessed directly.";
	}

	@Override
	public Version since() {
		return CHVersion.V3_3_1;
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
