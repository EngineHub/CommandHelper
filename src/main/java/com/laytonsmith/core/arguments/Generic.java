package com.laytonsmith.core.arguments;

import com.laytonsmith.PureUtilities.StringUtils;
import com.laytonsmith.annotations.typename;
import com.laytonsmith.core.natives.interfaces.Mixed;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author lsmith
 */
public class Generic {
	
	/**
	 * Provides a single instance that can be reused the unbounded wildcard type, i.e.
	 * Mixed&lt;?&gt;
	 */
	public static final Generic ANY = new Generic("?");
	/**
	 * The types that this generic can accept. In Java
	 * notation, it would be something like this:
	 * List&lt;string|int&gt;
	 */
	private List<Class<? extends Mixed>> type;
	/**
	 * If this is a bounded type, that is if 
	 * Type&lt;identifier extends type&gt;, this is true
	 */
	private boolean bounded = false;
	
	/**
	 * The identifier used in this generic, matching [a-zA-Z_][a-zA-Z_0-9]+ or a
	 * literal question mark. This may be null if this is not being used as a class
	 * definition generic.
	 */
	private String identifier = null;
	
	/**
	 * Creates a new unbounded Generic representing a Generic with a
	 * concrete type.
	 * @param clazz 
	 */
	public Generic(Class<? extends Mixed> clazz){
		this(new Class[]{clazz});
	}
	
	/**
	 * Creates a new unbounded Generic representing a Generic with
	 * exactly two disjoint concrete types.
	 * @param clazz1
	 * @param clazz2 
	 */
	public Generic(Class<? extends Mixed> clazz1, Class<? extends Mixed> clazz2){
		this(new Class[]{clazz1, clazz2});		
	}
	
	/**
	 * Creates a new unbounded Generic representing a Generic with
	 * disjoint concrete types.
	 * @param classes 
	 */
	public Generic(Class<? extends Mixed>[] classes){
		this.type = Arrays.asList(classes);
	}
	
	/**
	 * Creates a new unbounded Generic definition.
	 * @param identifier 
	 */
	public Generic(String identifier){
		this(identifier, new Class[]{});
	}
	
	/**
	 * Creates a new bounded Generic definition.
	 * @param identifier
	 * @param clazz 
	 */
	public Generic(String identifier, Class<? extends Mixed> clazz){
		this(identifier, new Class[]{clazz});
	}
	
	/**
	 * Creates a new bounded Generic definition, with exactly two disjoint
	 * types.
	 * @param identifier
	 * @param clazz1
	 * @param clazz2 
	 */
	public Generic(String identifier, Class<? extends Mixed> clazz1, Class<? extends Mixed> clazz2){
		this(identifier, new Class[]{clazz1, clazz2});		
	}
	
	/**
	 * Creates a new disjoint bounded Generic definition.
	 * @param identifier
	 * @param classes 
	 */
	public Generic(String identifier, Class<? extends Mixed>[] classes){
		this.identifier = identifier;
		this.type = Arrays.asList(classes);
		this.bounded = true;
		if(!identifier.matches("[a-zA-Z_][a-zA-Z_0-9]+|\\?")){
			throw new IllegalArgumentException(identifier + " is incorrectly formatted");
		}
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		if(identifier != null){
			b.append(identifier);
			if(bounded){
				b.append(" extends ");
			}
		}
		if(!type.isEmpty()){
			List<String> types = new ArrayList<String>();
			for(Class<? extends Mixed> t : type){
				String tt = t.getSimpleName();
				if(t.getAnnotation(typename.class) != null){
					typename tn = t.getAnnotation(typename.class);
					tt = tn.value();
				}
				types.add(tt);
			}
			b.append(StringUtils.Join(types, "|"));
		}
		return b.toString();
	}
	
}
