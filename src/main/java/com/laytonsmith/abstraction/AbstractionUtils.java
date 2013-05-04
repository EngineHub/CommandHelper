
package com.laytonsmith.abstraction;

import com.laytonsmith.PureUtilities.ClassDiscovery;
import com.laytonsmith.annotations.WrappedItem;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This class provides various utilities for managing the abstraction layer
 * in a decoupled fashion.
 */
public class AbstractionUtils {
	
	private AbstractionUtils(){}
	
	/**
	 * This maps from the wrapper to the wrapped type
	 */
	private static Map<Class<? extends AbstractionObject>, Class> abstractionClasses = null;
	/**
	 * This maps from the wrapped type to the wrapper
	 */
	private static Map<Class, Class<? extends AbstractionObject>> abstractionTypes = null;
	
	private static Set<Class> highestLevelTypes = null;
	
	static {
		init();
	}
	/**
	 * Finds the closest match for an object in the abstraction layer, and
	 * returns that object instead. If {@code item} is null, null is simply returned.
	 * @param <T>
	 * @param item
	 * @throws AbstractionException Thrown if this object doesn't match ANY of the object wrappers
	 * in the abstraction layer.
	 * @return 
	 */
	public static <T extends AbstractionObject> T wrap(Object item) throws AbstractionException {
		if(item == null){
			return null;
		}
		Class search = item.getClass();
		
		while(search.getSuperclass() != null){
			for(Field f : ClassDiscovery.GetFieldsWithAnnotation(WrappedItem.class)){
				//
			}
		}
		throw new AbstractionException("Could not find a match for " + item.getClass().getName() + " in the abstraction library.");
	}
	
	/**
	 * Returns a list of the highest level types in the abstraction layer. These are the base types
	 * in the library, and so long as the object sent to wrap() extends at least one of these objects,
	 * it is guaranteed to be found.
	 * @return 
	 */
	public static Class[] getHighestLevelTypes(){
		return highestLevelTypes.toArray(new Class[highestLevelTypes.size()]);
	}
	
	private static void init() {
		if(abstractionClasses == null){
			initialize();
		}
	}
	
	/**
	 * Scans all the classes for abstraction classes, and caches them locally
	 * to speed up future operations.
	 */
	public static void initialize() {
		abstractionClasses = new HashMap<Class<? extends AbstractionObject>, Class>();
		abstractionTypes = new HashMap<Class, Class<? extends AbstractionObject>>();
		for(Field f : ClassDiscovery.GetFieldsWithAnnotation(WrappedItem.class)){
			//There's a unit test for this to make sure that this cast will actually work at runtime.
			abstractionClasses.put((Class<? extends AbstractionObject>)f.getDeclaringClass(), f.getType());
			abstractionTypes.put(f.getType(), (Class<? extends AbstractionObject>)f.getDeclaringClass());			
		}
		highestLevelTypes = new HashSet<Class>();
		Set<Class> types = abstractionTypes.keySet();
		outer: for(Class c : types){
			if(c.isInterface()){
				//Slightly different handling here, since going up is a tree,
				//we have to recurse instead of loop.
				highestLevelTypes.addAll(getAllSuperInterfaces(c, new HashSet<Class>()));
			} else {
				Class cc = c;
				while((cc = cc.getSuperclass()) != null){
					if(types.contains(cc)){
						//Nope, one of the supertypes is accounted for,
						//so this isn't a highest level type.
						continue outer;
					}
				}
				//Ah, ok, it is a highest level type
				highestLevelTypes.add(c);
			}
		}
	}
	
	private static Set<Class> getAllSuperInterfaces(Class base, Set<Class> types){
		if(base.getInterfaces().length == 0){
			types.add(base);
		} else {
			//This isn't, but lets look at the parents
			for(Class s : base.getInterfaces()){
				getAllSuperInterfaces(s, types);
			}
		}
		return types;
	}
	
	/**
	 * Given a class type for an object that has a given type, returns the outer wrapping class
	 * for that type. This is not to be confused with the {@link #wrap(java.lang.Object)} method, which
	 * actually returns an instance of the nearest appropriate class.
	 * @param c
	 * @return 
	 */
	public static Class<? extends AbstractionObject> getExactClass(Class c){
		return abstractionTypes.get(c);
	}
	
	public static class AbstractionException extends RuntimeException {

		public AbstractionException(String message) {
			super(message);
		}
		
	}
}
