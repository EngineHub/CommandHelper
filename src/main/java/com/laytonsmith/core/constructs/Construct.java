

package com.laytonsmith.core.constructs;

import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.exceptions.MarshalException;
import com.laytonsmith.core.natives.interfaces.Mixed;
import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

/**
 *
 *
 */
public abstract class Construct implements Cloneable, Comparable<Construct>, Mixed{

    public enum ConstructType {

        TOKEN, COMMAND, FUNCTION, VARIABLE, LITERAL, ARRAY, MAP, ENTRY, INT,
        DOUBLE, BOOLEAN, NULL, STRING, VOID, IVARIABLE, CLOSURE, LABEL, SLICE,
        SYMBOL, IDENTIFIER, BRACE, BRACKET, BYTE_ARRAY, RESOURCE, LOCK, MUTABLE_PRIMITIVE,
		CLASS_TYPE;
    }
    private final ConstructType ctype;
    private final String value;

    private Target target;
	private transient boolean wasIdentifier = false;

    public ConstructType getCType() {
        return ctype;
    }

    /**
     * This method should only be used by Script when setting the children's target, if it's an ivariable.
     * @param target
     */
    void setTarget(Target target) {
        this.target = target;
    }

    public final String getValue() {
        return val();
    }

    public int getLineNum() {
        return target.line();
    }

    public File getFile() {
        return target.file();
    }

    public int getColumn(){
        return target.col();
    }

    public Target getTarget(){
        return target;
    }

    public Construct(String value, ConstructType ctype, int line_num, File file, int column) {
        this.value = value;
		Static.AssertNonNull(value, "The string value may not be null.");
        this.ctype = ctype;
        this.target = new Target(line_num, file, column);
    }

    public Construct(String value, ConstructType ctype, Target t){
        this.value = value;
		Static.AssertNonNull(value, "The string value may not be null.");
        this.ctype = ctype;
        this.target = t;
    }

    /**
     * Returns the standard string representation of this Construct.
	 * This will never return null.
     * @return
     */
	@Override
    public String val() {
        return value;
    }

	public void setWasIdentifier(boolean b) {
		wasIdentifier = b;
	}

	public boolean wasIdentifier(){
		return wasIdentifier;
	}


    /**
     * Returns the standard string representation of this Construct, except
     * in the case that the construct is a CNull, in which case it returns
     * java null.
     * @return
     */
    public String nval(){
        return val();
    }


    @Override
    public String toString() {
        return value;
    }

    @Override
    public Construct clone() throws CloneNotSupportedException {
        return (Construct) super.clone();
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
        if (c instanceof CString || c instanceof Command) {
            return c.val();
        } else if (c instanceof CVoid) {
            return "";
        } else if (c instanceof CInt) {
            return ((CInt) c).getInt();
        } else if (c instanceof CDouble) {
            return ((CDouble) c).getDouble();
        } else if (c instanceof CBoolean) {
            return ((CBoolean) c).getBoolean();
        } else if (c instanceof CNull) {
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
                for(String key : ca.stringKeySet()){
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
			return CNull.NULL;
		}
		if("".equals(s.trim())){
			throw new MarshalException();
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
                ca.set(convertJSON(key, t),
                        convertJSON(obj.get(key), t), t);
            }
            return ca;
        } else if (s.startsWith("[")) {
            //It's an array
            JSONArray array = (JSONArray) JSONValue.parse(s);
			if(array == null){
				throw new MarshalException();
			}
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
				return CNull.NULL;
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
            return CBoolean.get((Boolean) o);
        } else if (o instanceof java.util.List) {
            java.util.List l = (java.util.List) o;
            CArray ca = new CArray(t);
			for (Object l1 : l) {
				ca.push(convertJSON(l1, t));
			}
            return ca;
        } else if (o == null) {
            return CNull.NULL;
        } else if(o instanceof java.util.Map){
            CArray ca = CArray.GetAssociativeArray(t);
            for(Object key : ((java.util.Map)o).keySet()){
                ca.set(convertJSON(key, t),
                        convertJSON(((java.util.Map)o).get(key), t), t);
            }
            return ca;
        } else {
            throw new MarshalException(o.getClass().getSimpleName() + " are not currently supported");
        }
    }

	@Override
    public int compareTo(Construct c) {
        if(this.value.contains(" ") || this.value.contains("\t")
                || c.value.contains(" ") || c.value.contains("\t")){
            return this.value.compareTo(c.value);
        }
        try {
            Double d1 = Double.valueOf(this.value);
            Double d2 = Double.valueOf(c.value);
            return d1.compareTo(d2);
        } catch (NumberFormatException e) {
            return this.value.compareTo(c.value);
        }
    }

    /**
     * Converts a POJO to a Construct, if the type is convertable. This accepts many types of
     * objects, and should be expanded if a type does fit into the overall type scheme.
     * @param o
     * @return
     * @throws ClassCastException
     */
    public static Construct GetConstruct(Object o) throws ClassCastException{
        if(o == null){
            return CNull.NULL;
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
            return CBoolean.get((Boolean) o);
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
        if(c instanceof CNull){
            return null;
        } else if(c instanceof CString){
            return c.val();
        } else if(c instanceof CBoolean){
            return Boolean.valueOf(((CBoolean)c).getBoolean());
        } else if(c instanceof CInt){
            return Long.valueOf(((CInt)c).getInt());
        } else if(c instanceof CDouble){
            return Double.valueOf(((CDouble)c).getDouble());
        } else if(c instanceof CArray){
            CArray ca = (CArray)c;
            if(ca.inAssociativeMode()){
                //HashMap
                HashMap<String, Object> map = new HashMap<String, Object>((int)ca.size());
                for(String key : ca.stringKeySet()){
                    map.put(key, GetPOJO(ca.get(key, Target.UNKNOWN)));
                }
                return map;
            } else {
                //ArrayList
                ArrayList<Object> list = new ArrayList<Object>((int)ca.size());
                for(int i = 0; i < ca.size(); i++){
                    list.add(GetPOJO(ca.get(i, Target.UNKNOWN)));
                }
                return list;
            }
        } else {
            throw new ClassCastException(c.getClass().getName() + " cannot be cast to a POJO");
        }
    }

	public CString asString(){
		return new CString(val(), target);
	}

    /**
     * If this type of construct is dynamic, that is to say, if it isn't a constant.
     * Things like 9, and 's' are constant. Things like {@code @value} are dynamic.
     * @return
     */
    public abstract boolean isDynamic();

	/**
	 * Returns the underlying value, as a value that can be directly
	 * inserted into code. So, if the value were
	 * {@code This is 'the value'}, then {@code 'This is \'the value\''} would
	 * be returned. (That is, characters needing escapes will be escaped.) It includes
	 * the outer quotes as well. Numbers and other primitives may be able to override
	 * this to return a valid value as well. By default, this assumes a string, and
	 * returns appropriately.
	 * @return
	 */
	protected String getQuote(){
		return "'" + val().replace("\\", "\\\\").replace("'", "\\'") + "'";
	}

	/**
	 * Returns the typeof this Construct, as a string. Not all constructs are annotated with
	 * the @typeof annotation, in which case this is considered a "private" object, which
	 * can't be directly accessed via MethodScript. In this case, an IllegalArgumentException
	 * is thrown.
	 * @return
	 * @throws IllegalArgumentException If the class isn't public facing.
	 */
	public final String typeof(){
		typeof ann = this.getClass().getAnnotation(typeof.class);
		if(ann == null){
			throw new IllegalArgumentException();
		}
		return ann.value();
	}
}
