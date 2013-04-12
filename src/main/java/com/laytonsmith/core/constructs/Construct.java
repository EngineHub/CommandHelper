

package com.laytonsmith.core.constructs;

import com.laytonsmith.annotations.immutable;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.exceptions.MarshalException;
import com.laytonsmith.core.functions.Exceptions;
import com.laytonsmith.core.natives.interfaces.Mixed;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

/**
 *
 * @author layton
 */
public abstract class Construct implements Cloneable, Comparable<Construct>, Mixed{

    private final Object value;
    private final Target target;
    
    public Target getTarget(){
        return target;
    }
	
    public Construct(Object value, Target t){
        this.value = value;
        this.target = t;
    }
	
	/**
	 * Returns true if the underlying value is null.
	 * @return 
	 */
	public boolean isNull(){
		return value == null;
	}

    /**
     * Returns the standard string representation of this Construct.
	 * This will never return null.
     * @return 
     */
    public final String val() {
        return toString()==null?"null":toString();
    }
	
	/**
	 * Returns the standard string representation of this Construct,
	 * or a Java null if it is null.
	 * @return 
	 */
	public final String nval(){
		return toString()==null?null:toString();
	}
	
    public final Object value() {
        return value;
    }    
    
    @Override
    public String toString() {
        return value==null?"null":value.toString();
    }

    @Override
    public Construct clone() throws CloneNotSupportedException {
        return (Construct) super.clone();
    }
	
		
	public CString asString(){
		return new CString(toString(), target);
	}
    
    /**
     * If this type of construct is dynamic, that is to say, if it isn't a constant.
     * If the underlying value is mutable, it is dynamic.
     * @return 
     */
    public abstract boolean isDynamic();
	
	/**
	 * Subclasses are free to implement this as needed. This is called
	 * if a value goes out of scope. By default, nothing happens.
	 */
	public void destructor(){
		//
	}

    /**
     * This function takes a Construct, and turns it into a JSON value. If the construct is
     * not one of the following, a MarshalException is thrown: CArray, CBoolean, CDouble, CInt, CNull, 
     * CString, CVoid, Command. Currently unsupported, but will be in the future are: CClosure/CFunction
     * The following map is applied when encoding and decoding:
     * <table border='1'>
     * <tr><th>JSON</th><th>MethodScript</th></tr>
     * <tr><td>string</td><td>CString, CVoid, Command, but all are decoded into CString</td></tr>
     * <tr><td>number</td><td>CInt, CDouble, and it is decoded intelligently</td></tr>
     * <tr><td>boolean</td><td>CBoolean</td></tr>
     * <tr><td>null</td><td>CNull</td></tr>
     * <tr><td>array/object</td><td>CArray</td></tr>
     * </table>
     * @param c
     * @return 
     */
    public static String json_encode(Construct c, Target t) throws MarshalException{
        return JSONValue.toJSONString(json_encode0(c, t));
    }
    
    private static Object json_encode0(Construct c, Target t) throws MarshalException{
        if (c instanceof CString) {
            return c.val();
        } else if (c instanceof CVoid) {
            return "";
        } else if (c instanceof CInt) {
            return ((CInt) c).castToInt(t);
        } else if (c instanceof CDouble) {
            return ((CDouble) c).castToCDouble(t);
        } else if (c instanceof CBoolean) {
            return ((CBoolean) c).castToBoolean();
        } else if (c.isNull()) {
            return null;
        } else if (c instanceof CArray) {
            CArray ca = (CArray) c;
            if (!ca.inAssociativeMode()) {
                List<Object> list = new ArrayList<Object>();
                for(int i = 0; i < ca.size(); i++){
                    list.add(json_encode0(ca.get(i, t), t));
                }
                return list;
            } else {
                Map<String, Object> map = new HashMap<String, Object>();
                for(String key : ca.keySet()){
                    map.put(key, json_encode0(ca.get(key, t), t));
                }
                return map;
            }
        } else {
            throw new MarshalException("The type of " + c.getClass().getSimpleName() + " is not currently supported", c);
        }
    }
    /**
     * Takes a string and converts it into a Construct
     * @param s
     * @return 
     */
    public static Construct json_decode(String s, Target t) throws MarshalException {
		if(s == null){
			return GetNullConstruct(t);
		}
        if (s.startsWith("{")) {
            //Object
            JSONObject obj = (JSONObject) JSONValue.parse(s);
            CArray ca = CArray.GetAssociativeArray(t);
			if(obj == null){
				//From what I can tell, this happens when the json object is improperly formatted,
				//so go ahead and throw an exception
				throw new MarshalException();
			}
            for(Object key : obj.keySet()){
                ca.set(convertJSON(key, t).primitive(t), 
                        convertJSON(obj.get(key), t), t);
            }
            return ca;
        } else if (s.startsWith("[")) {
            //It's an array
            JSONArray array = (JSONArray) JSONValue.parse(s);
            CArray carray = new CArray(t);
            for (int i = 0; i < array.size(); i++) {
                carray.push(convertJSON(array.get(i), t));
            }
            return carray;
        } else {
            //It's a single value, but we're gonna wrap it in an array, then deconstruct it
            s = "[" + s + "]";
            JSONArray array = (JSONArray) JSONValue.parse(s);
			if(array == null){
				//It's a null value
				return GetNullConstruct(t);
			}
            Object o = array.get(0);
            return convertJSON(o, t);
        }
    }

    private static Construct convertJSON(Object o, Target t) throws MarshalException {
        if (o instanceof String) {
            return new CString((String) o, Target.UNKNOWN);
        } else if (o instanceof Number) {
            Number n = (Number) o;
            if (n.longValue() == n.doubleValue()) {
                //It's an int
                return new CInt(n.longValue(), Target.UNKNOWN);
            } else {
                //It's a double
                return new CDouble(n.doubleValue(), Target.UNKNOWN);
            }
        } else if (o instanceof Boolean) {
            return new CBoolean(((Boolean) o).booleanValue(), Target.UNKNOWN);
        } else if (o instanceof java.util.List) {
            java.util.List l = (java.util.List) o;
            CArray ca = new CArray(t);
            for (int i = 0; i < l.size(); i++) {
                ca.push(convertJSON(l.get(i), t));
            }
            return ca;
        } else if (o == null) {
            return GetNullConstruct(t);
        } else if(o instanceof java.util.Map){
            CArray ca = CArray.GetAssociativeArray(t);
            for(Object key : ((java.util.Map)o).keySet()){
                ca.set(convertJSON(key, t).primitive(t), 
                        convertJSON(((java.util.Map)o).get(key), t), t);
            }
            return ca;
        } else {
            throw new MarshalException(o.getClass().getSimpleName() + " are not currently supported");
        }
    }

    public int compareTo(Construct c) {
        if(this.toString().contains(" ") || this.toString().contains("\t") 
                || c.toString().contains(" ") || c.toString().contains("\t")){
            return this.toString().compareTo(c.toString());
        }
        try {
            Double d1 = Double.valueOf(this.toString());
            Double d2 = Double.valueOf(c.toString());
            return d1.compareTo(d2);
        } catch (NumberFormatException e) {
            return this.toString().compareTo(c.toString());
        }
    }
	
	/**
	 * Returns a null construct. The Construct object
	 * returned is a special object that is of type Construct 
	 * (that is, mixed in ms typing)
	 * but returns true for isNull.
	 * @param t
	 * @return 
	 */
	public static Construct GetNullConstruct(final Target t){
		//TODO: Change this return type to Mixed once the exec function
		//has been updated
		return new Construct(null, t) {

			@Override
			public boolean isDynamic() {
				return false;
			}

			public String typeName() {
				return "null";
			}

			@Override
			public boolean isImmutable() {
				throw new ConfigRuntimeException("Cannot dereference a null object", Exceptions.ExceptionType.NullPointerException, t);
			}
			
		};
	}

	public boolean isImmutable() {
		Class<?> c = this.getClass();
		do {
			if(c.getAnnotation(immutable.class) != null){
				return true;
			}
			c = c.getSuperclass();
		} while(c != null);
		return false;
	}
    /** It is generally best to use this factory method, instead of creating a new Construct() directly. */
	public static Construct GetConstruct(CharSequence c){return GetConstruct((Object)c);}
    /** It is generally best to use this factory method, instead of creating a new Construct() directly. */
	public static Construct GetConstruct(Integer c){return GetConstruct((Object)c);}
    /** It is generally best to use this factory method, instead of creating a new Construct() directly. */
	public static Construct GetConstruct(Long c){return GetConstruct((Object)c);}
    /** It is generally best to use this factory method, instead of creating a new Construct() directly. */
	public static Construct GetConstruct(Byte c){return GetConstruct((Object)c);}
    /** It is generally best to use this factory method, instead of creating a new Construct() directly. */
	public static Construct GetConstruct(BigInteger c){return GetConstruct((Object)c);}
    /** It is generally best to use this factory method, instead of creating a new Construct() directly. */
	public static Construct GetConstruct(AtomicInteger c){return GetConstruct((Object)c);}
    /** It is generally best to use this factory method, instead of creating a new Construct() directly. */
	public static Construct GetConstruct(Short c){return GetConstruct((Object)c);}
    /** It is generally best to use this factory method, instead of creating a new Construct() directly. */
	public static Construct GetConstruct(Boolean c){return GetConstruct((Object)c);}
    /** It is generally best to use this factory method, instead of creating a new Construct() directly. */
	public static Construct GetConstruct(Map c) throws ClassCastException {return GetConstruct((Object)c);}
    /** It is generally best to use this factory method, instead of creating a new Construct() directly. */
	public static Construct GetConstruct(Collection c){return GetConstruct((Object)c);}
    /**
     * Converts a POJO to a Construct, if the type is convertable. This accepts many types of
     * objects, and should be expanded if a type does fit into the overall type scheme.
     * @param o
     * @return
     * @throws ClassCastException 
     */
    private static Construct GetConstruct(Object o) throws ClassCastException{
        if(o == null){
            return GetNullConstruct(Target.UNKNOWN);
        } else if(o instanceof CharSequence){
            return new CString((CharSequence)o, Target.UNKNOWN);
        } else if(o instanceof Number){
            if(o instanceof Integer || o instanceof Long || o instanceof Byte || o instanceof BigInteger
                    || o instanceof AtomicInteger || o instanceof Short){
                //integral
                return new CInt(((Number)o).longValue(), Target.UNKNOWN);
            } else {
                //floating point
                return new CDouble(((Number)o).doubleValue(), Target.UNKNOWN);
            }            
        } else if(o instanceof Boolean){
            return new CBoolean(((Boolean)o).booleanValue(), Target.UNKNOWN);
        } else if(o instanceof Map){
            //associative array
            CArray a = CArray.GetAssociativeArray(Target.UNKNOWN);           
            Map m = (Map)o;
            for(Object key : m.keySet()){
                a.set(key.toString(), GetConstruct(m.get(key)), Target.UNKNOWN);
            }
            return a;
        } else if(o instanceof Collection){
            //normal array
            CArray a = new CArray(Target.UNKNOWN);
            Collection l = (Collection)o;
            for(Object obj : l){
                a.push(GetConstruct(obj));
            }
            return a;
        } else {
            throw new ClassCastException(o.getClass().getName() + " cannot be cast to a Construct type");
        }
    }
    
    /**
     * Converts a Construct to a POJO, if the type is convertable. The types returned from
     * this method are set, unlike GetConstruct which is more flexible. The mapping is precisely
     * as follows:
     * boolean -> Boolean
     * integer -> Long
     * double -> Double
     * string -> String
     * normal array -> ArrayList<Object>
     * associative array -> HashMap<String, Object>
     * null -> null
     * @param c
     * @return
     * @throws ClassCastException 
     */
    public static Object GetPOJO(Construct c) throws ClassCastException{
        if(c.isNull()){
            return null;
        } else if(c instanceof CString){
            return c.val();
        } else if(c instanceof CBoolean){
            return Boolean.valueOf(((CBoolean)c).castToBoolean());
        } else if(c instanceof CInt){
            return Long.valueOf(((CInt)c).castToInt(Target.UNKNOWN));
        } else if(c instanceof CDouble){
            return Double.valueOf(((CDouble)c).castToDouble(Target.UNKNOWN));
        } else if(c instanceof CArray){
            CArray ca = (CArray)c;
            if(ca.inAssociativeMode()){
                //HashMap
                HashMap<String, Object> map = new HashMap<String, Object>((int)ca.size());
                for(String key : ca.keySet()){
                    map.put(key, GetPOJO(ca.get(key)));
                }
                return map;
            } else {
                //ArrayList
                ArrayList<Object> list = new ArrayList<Object>((int)ca.size());
                for(int i = 0; i < ca.size(); i++){
                    list.add(GetPOJO(ca.get(i)));
                }
                return list;
            }            
        } else {
            throw new ClassCastException(c.getClass().getName() + " cannot be cast to a POJO");
        }
    }

	public CPrimitive primitive(Target t) throws ConfigRuntimeException {
		throw new Exceptions.CastException("Cannot cast " + typeName() + " to primitive", t);
	}

}
