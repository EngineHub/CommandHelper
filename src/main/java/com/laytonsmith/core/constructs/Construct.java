/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.core.constructs;

import com.laytonsmith.core.exceptions.MarshalException;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

/**
 *
 * @author layton
 */
public abstract class Construct implements Cloneable, Comparable<Construct> {


    public enum ConstructType {

        TOKEN, COMMAND, FUNCTION, VARIABLE, LITERAL, ARRAY, MAP, ENTRY, INT, 
        DOUBLE, BOOLEAN, NULL, STRING, VOID, IVARIABLE, CLOSURE, LABEL, SLICE,
        SYMBOL
    }
    private ConstructType ctype;
    private String value;

    private Target target;

    public ConstructType getCType() {
        return ctype;
    }

    public String getValue() {
        return value;
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
        this.ctype = ctype;
        this.target = new Target(line_num, file, column);
    }
    
    public Construct(String value, ConstructType ctype, Target t){
        this.value = value;
        this.ctype = ctype;
        this.target = t;
    }

    /**
     * Returns the standard string representation of this Construct.
     * @return 
     */
    public String val() {
        return value;
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
        return json_encode(c, false, t);
    }

    /**
     * Use the other one.
     * @deprecated 
     * @param c
     * @param raw
     * @param line_num
     * @param f
     * @return
     * @throws MarshalException
     * @deprecated
     */
    @Deprecated
    public static String json_encode(Construct c, boolean raw, Target t) throws MarshalException {
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
        if (s.startsWith("{")) {
            //Object
            JSONObject obj = (JSONObject) JSONValue.parse(s);
            CArray ca = new CArray(t);
            ca.forceAssociativeMode();
            for(Object key : obj.keySet()){
                ca.set(convertJSON(key, t), 
                        convertJSON(obj.get(key), t));
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
            return new CNull();
        } else if(o instanceof java.util.Map){
            CArray ca = new CArray(t);
            ca.forceAssociativeMode();
            for(Object key : ((java.util.Map)o).keySet()){
                ca.set(convertJSON(key, t), 
                        convertJSON(((java.util.Map)o).get(key), t));
            }
            return ca;
        } else {
            throw new MarshalException(o.getClass().getSimpleName() + " are not currently supported");
        }
    }

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
     * If this type of construct is dynamic, that is to say, if it isn't a constant.
     * @return 
     */
    public abstract boolean isDynamic();
}
