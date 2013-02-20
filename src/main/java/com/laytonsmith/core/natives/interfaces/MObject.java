package com.laytonsmith.core.natives.interfaces;

import com.laytonsmith.PureUtilities.StringUtils;
import com.laytonsmith.annotations.immutable;
import com.laytonsmith.annotations.nofield;
import com.laytonsmith.annotations.typename;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CBoolean;
import com.laytonsmith.core.constructs.CDouble;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.constructs.CPrimitive;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.Exceptions;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
 * public void setX(Construct x, Target t){
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
 * @author lsmith
 */
@typename("Object")
public class MObject implements Mixed {
	
	public static <T extends MObject> T Construct(Class<T> type, CArray data, Target t){
		T instance;
		try {
			instance = type.newInstance();
		} catch (InstantiationException ex) {
			throw new RuntimeException(type.getName() + " does not have a default constructor.");
		} catch (IllegalAccessException ex) {
			throw new RuntimeException(type.getName() + "'s default constructor is not public.");
		}
		for(String key : data.keySet()){
			instance.set(key, data.get(key), t);
		}
		return instance;
	}
	
	/**
	 * Constructs a fully dynamic MObject based on the given array. This is discouraged
	 * from direct use.
	 * @param data
	 * @return 
	 */
	public static MObject Construct(CArray data, Target t){
		return Construct(MObject.class, data, t);
	}
	
	private Map<String, Mixed> fields = new HashMap<String, Mixed>();
	
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
	public final void set(String field, Mixed value, Target t){
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
				if(value.isNull()){
					//Easy case
					val = null;
				} else {
					if(fType == byte.class){
						val = value.primitive(t).castToInt8(t);
					} else if(fType == short.class){
						val = value.primitive(t).castToInt16(t);
					} else if(fType == int.class){
						val = value.primitive(t).castToInt32(t);
					} else if(fType == long.class){
						val = value.primitive(t).castToInt(t);
					} else if(fType == char.class){
						if(value.val().length() == 0){
							val = null;
						} else {
							val = value.val().charAt(0);
						}
					} else if(fType == boolean.class){
						val = value.primitive(t).castToBoolean();
					} else if(fType == float.class){
						val = value.primitive(t).castToDouble32(t);
					} else if(fType == double.class){
						val = value.primitive(t).castToDouble(t);
					} else if(fType == MMap.class){
						CArray ca = Static.getArray(value, t);
						MMap m = new MMap();
						for(String key : ca.keySet()){
							m.put(key, ca.get(key));
						}
						val = m;
					} else if(fType == MList.class){
						CArray ca = Static.getArray(value, t);
						MList m = new MList();
						if(ca.inAssociativeMode()){
							throw new ConfigRuntimeException("Expected non-associative array, but an associative array was found instead.", 
									Exceptions.ExceptionType.CastException, t);
						}
						for(int i = 0; i < ca.size(); i++){
							m.add(ca.get(i));
						}
						val = m;
					} else if(Construct.class.isAssignableFrom(fType)){
						val = value;
					} else if(MObject.class.isAssignableFrom(fType)){
						CArray ca = Static.getArray(value, t);
						val = MObject.Construct(fType, ca, t);
					} else {
						//Programming error.
						throw new Error(this.getClass().getName() + " contained the public field " 
								+ f.getName() + " of type " + fType.getName() + ", which is an unsupported field type.");
					}
				}
				try {
					//val is now set correctly, guaranteed.
					f.set(this, val);
					return;
					//These exceptions cannot happen.
				} catch (IllegalArgumentException ex) {
					throw new Error(ex);
				} catch (IllegalAccessException ex) {
					throw new Error(ex);
				}
			}
		}
		//Put the dynamic parameter in, it wasn't found
		fields.put(field, value);
	}
	
	/**
	 * Retrieves a Construct from the underlying object, or
	 * a null construct if it doesn't exist. 
	 * @param field
	 * @return 
	 */
	public Mixed get(String field){
		//TODO: This won't work. It needs to scan the object first, THEN look for
		//the parameter
		if(alias(field) != null){
			field = alias(field);
		}
		for(Field f : this.getClass().getFields()){
			if(f.getName().equals(field)){
				Object val;
				try {
					val = f.get(this);
				} catch (IllegalArgumentException ex) {
					throw new Error(ex);
				} catch (IllegalAccessException ex) {
					throw new Error(ex);
				}
				if(val == null){
					return Construct.GetNullConstruct(Target.UNKNOWN);
				} else {
					Class fType = val.getClass();
					if(fType == byte.class){
						val = new CInt((Byte)val, Target.UNKNOWN);
					} else if(fType == short.class){
						val = new CInt((Short)val, Target.UNKNOWN);
					} else if(fType == int.class){
						val = new CInt((Integer)val, Target.UNKNOWN);
					} else if(fType == long.class){
						val = new CInt((Long)val, Target.UNKNOWN);
					} else if(fType == char.class){
						val = new CString((Character)val, Target.UNKNOWN);
					} else if(fType == boolean.class){
						val = new CBoolean((Boolean)val, Target.UNKNOWN);
					} else if(fType == float.class){
						val = new CDouble((Float)val, Target.UNKNOWN);
					} else if(fType == double.class){
						val = new CDouble((Double)val, Target.UNKNOWN);
					} else if(fType == MMap.class){
						CArray ca = CArray.GetAssociativeArray(Target.UNKNOWN);
						MMap m = (MMap)val;
						for(String key : m.keySet()){
							ca.set(key, (Construct)m.get(key), Target.UNKNOWN);
						}
						val = m;
					} else if(fType == MList.class){
						CArray ca = new CArray(Target.UNKNOWN);
						MList m = (MList)val;
						for(int i = 0; i < m.size(); i++){
							ca.push(ca.get(i));
						}
						val = m;
					} else if(Construct.class.isAssignableFrom(fType)){
						//Done.
					} else if(MObject.class.isAssignableFrom(fType)){
						//TODO
					} else {
						//Programming error.
						throw new Error(this.getClass().getName() + " contained the public field " 
								+ f.getName() + " of type " + fType.getName() + ", which is an unsupported field type.");
					}
				}
				//At this point, it has been cast to a Construct of some form.
				return (Construct)val;
			}
		}
		if(fields.containsKey(field)){
			return fields.get(field);
		} else {
			return Construct.GetNullConstruct(Target.UNKNOWN);
		}
	}

	/**
	 * For MObjects, the underlying object is not useful to java code, so null
	 * is returned, and subclasses are prevented from further subclassing.
	 * @return 
	 */
	public final Object value() {
		return null;
	}

	/**
	 * By default, val is the listing of all the fields in the object, though this can be overridden by
	 * subclasses.
	 * @return 
	 */
	public String val() {
		Map<String, Mixed> values = new HashMap<String, Mixed>();
		for(String key : fields.keySet()){
			values.put(key, fields.get(key));
		}
		for(Field f : this.getClass().getFields()){
			values.put(f.getName(), get(f.getName()));
		}
		List<String> v = new ArrayList<String>();
		for(String key : values.keySet()){
			v.add(key + ": " + values.get(key).val());
		}
		return "{" + StringUtils.Join(v, ", ") + "}";
	}

	/**
	 * MObjects cannot be null, otherwise they wouldn't exist.
	 * @return 
	 */
	public boolean isNull() {
		return false;
	}

	/**
	 * This should be overriden by each subclass, or the subclass should
	 * tag itself with @typename
	 * @return 
	 */
	public String typeName() {
		if(this.getClass().getAnnotation(typename.class) != null){
			return this.getClass().getAnnotation(typename.class).value();
		} else {
			throw new Error(this.getClass() + " does not have the @typename annotation, nor does it override typeName()");
		}
	}

	/**
	 * Throws an Error, since MObjects are not primitives. Subclasses cannot
	 * override this method either, since no object can ever be a primitive.
	 * @param t
	 * @return
	 * @throws ConfigRuntimeException 
	 */
	public final CPrimitive primitive(Target t) throws ConfigRuntimeException {
		throw new Error("Cannot convert a MObject to a primitive.");
	}

	/**
	 * Returns true if this object is tagged with @immutable. This method cannot
	 * be overridden by subclasses.
	 * @return 
	 */
	public final boolean isImmutable() {
		return this.getClass().getAnnotation(immutable.class) != null;
	}
}
