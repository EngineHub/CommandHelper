package com.laytonsmith.core.natives.interfaces;

import com.laytonsmith.annotations.nofield;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CNull;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.exceptions.CRE.CRECastException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * An MObject is a definition of an array with a certain configuration. While an MObject can be constructed
 * directly, this is generally discouraged, since the reusability is lower this way. Instead, subclass it,
 * add direct accessors for various properties, and construct that instead. The static methods
 * provided allow for creation of an instance, given a CArray.
 *
 * Subclasses should note that a typical implementation will look like this:
 * <pre>
 * public Integer x;
 * ...
 * public void setX(Integer x, Target t){
 *		super.set("x", x, t);
 * }
 * ...
 * </pre>
 *
 * Additionally, the alias function can be overriden to provide for aliased
 * values, which will be accepted as non-dynamic parameters that point to
 * the real fields. They will not be included during serialization, however,
 * reads and writes to these fields will work correctly.
 *
 * If an object contains fields that should not be handled as instance fields,
 * they must be set to private, and they will be
 * ignored. All other fields should be public. If a field MUST be public, but
 * not accessible, you may annotate it with the {@link nofield} annotation, though
 * this is highly discouraged, since it breaks future compatibility.
 *
 * Note that in the example, an Integer is used. This is appropriate, though Construct
 * types may be used as well. The supported POJO types are only the following,
 * and conversions are automatic: All 8 primitives' object wrappers, String, MList,
 * MObject (and subclasses) and MMap.
 *
 */
public class MObject {

	public static <T extends MObject> T Construct(Class<T> type, CArray data){
		T instance;
		try {
			instance = type.newInstance();
		} catch (InstantiationException ex) {
			throw new RuntimeException(type.getName() + " does not have a default constructor.");
		} catch (IllegalAccessException ex) {
			throw new RuntimeException(type.getName() + "'s default constructor is not public.");
		}
		return null; //TODO
	}

	/**
	 * Constructs a fully dynamic MObject based on the given array. This is discouraged
	 * from direct use.
	 * @param data
	 * @return
	 */
	public static MObject Construct(CArray data){
		return Construct(MObject.class, data);
	}

	private Map<String, Construct> fields = new HashMap<String, Construct>();

	/**
	 * If a field can have an alias, this should return the proper
	 * name given this alias. If this is not an alias, return null, and
	 * the parameter will be added as a dynamic parameter, or if it actually
	 * represents a field, that is set instead. If both the value and the
	 * alias are set, the actual value takes priority, should the two values
	 * be different.
	 * @param field
	 * @return
	 */
	protected String alias(String field){
		return null;
	}

	/**
	 * Sets the field to the given parameter. If the field is a non-dynamic
	 * property, it is actually set in the object (and converted properly),
	 * otherwise it is simply added to the dynamic field list.
	 * @param field
	 * @param value
	 * @param t
	 */
	public final void set(String field, Construct value, Target t){
		String alias = alias(field);
		if(alias != null){
			field = alias;
		}
		for(Field f : this.getClass().getFields()){
			if(f.isAnnotationPresent(nofield.class)){
				//Skip this one
				continue;
			}
			if(f.getName().equals(field)){
				//This is it, so let's set it, (converting if necessary) then break
				Object val;
				Class fType = f.getType();
				if(value instanceof CNull){ //TODO
					//Easy case
					val = null;
				} else {
					if(fType == byte.class){
						val = Static.getInt8(value, t);
					} else if(fType == short.class){
						val = Static.getInt16(value, t);
					} else if(fType == int.class){
						val = Static.getInt32(value, t);
					} else if(fType == long.class){
						val = Static.getInt(value, t);
					} else if(fType == char.class){
						if(value.val().length() == 0){
							val = null;
						} else {
							val = value.val().charAt(0);
						}
					} else if(fType == boolean.class){
						val = Static.getBoolean(value);
					} else if(fType == float.class){
						val = Static.getDouble32(value, t);
					} else if(fType == double.class){
						val = Static.getDouble(value, t);
					} else if(fType == MMap.class){
						CArray ca = Static.getArray(value, t);
						MMap m = new MMap();
						for(String key : ca.stringKeySet()){
							m.put(key, ca.get(key, t));
						}
						val = m;
					} else if(fType == MList.class){
						CArray ca = Static.getArray(value, t);
						MList m = new MList();
						if(ca.inAssociativeMode()){
							throw ConfigRuntimeException.BuildException("Expected non-associative array, but an associative array was found instead.",
									CRECastException.class, t);
						}
						for(int i = 0; i < ca.size(); i++){
							m.add(ca.get(i, t));
						}
						val = m;
					} else if(Construct.class.isAssignableFrom(fType)){
						val = value;
					} else if(MObject.class.isAssignableFrom(fType)){
						CArray ca = Static.getArray(value, t);
						val = MObject.Construct(fType, ca);
					} else {
						//Programming error.
						throw new Error(this.getClass().getName() + " contained the public field "
								+ f.getName() + " of type " + fType.getName() + ", which is an unsupported field type.");
					}
				}
				try {
					//val is now set correctly, guaranteed.
					f.set(this, val);
					//These exceptions cannot happen.
				} catch (IllegalArgumentException ex) {
					throw new Error(ex);
				} catch (IllegalAccessException ex) {
					throw new Error(ex);
				}
			}
		}
		//Always put the dynamic parameter, regardless
		fields.put(field, value);
	}

	/**
	 * Retrieves a Construct from the
	 * @param field
	 * @return
	 */
	public Construct get(String field){
		return null; //TODO
	}
}
