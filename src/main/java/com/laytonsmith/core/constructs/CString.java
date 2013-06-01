

package com.laytonsmith.core.constructs;

import com.laytonsmith.annotations.typename;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.Exceptions;
import com.laytonsmith.core.natives.interfaces.ArrayAccess;
import com.laytonsmith.core.natives.interfaces.Mixed;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Layton
 */
@typename("string")
public class CString extends CPrimitive implements Cloneable, ArrayAccess {
	
    public CString(String value, Target t){
        super(value==null?"":value, t);
    }
    
    public CString(char value, Target t){
        this(Character.toString(value), t);
    }
    
    public CString(CharSequence value, Target t){
        this(value.toString(), t);
    }
    
	public static CString asString(Mixed m){
		return new CString(m.val(), m.getTarget());
	}
	
    @Override
    public CString doClone(){
        return this;
    }

    @Override
    public boolean isDynamic() {
        return false;
    }

    public Construct get(String index, Target t) {
        try{
            int i = (int)Integer.parseInt(index);
            return new CString(this.val().charAt(i), t);
        } catch(NumberFormatException e){
            throw new ConfigRuntimeException("Expecting numerical index, but recieved " + index, Exceptions.ExceptionType.FormatException, t);
        }
    }

    public int size() {
        return val().length();
    }

    public boolean canBeAssociative() {
        return false;
    }

    public Construct slice(int begin, int end, Target t) {
        if(begin >= end){
            return new CString("", t);
        }
        return new CString(this.val().substring(begin, end), t);
    }
	
	/**
	 * Returns the underlying string, quoted. So, if the value were
	 * {@code This is 'the value'}, then {@code 'This is \'the value\''} would
	 * be returned. (That is, characters needing escapes will be escaped.) It includes
	 * the outer quotes as well.S
	 * @return 
	 */
	public String getQuote(){
		return "'" + val().replace("\\", "\\\\").replace("'", "\\'") + "'";
	}

	public String typeName() {
		return "string";
	}

	@Override
	public String castToString() {
		return val();
	}

	@Override
	public double castToDouble(Target t) {
		try{
			return Double.valueOf(val());
		} catch(NumberFormatException e){
			throw new ConfigRuntimeException("Could not convert " + val() + " to a double.", Exceptions.ExceptionType.CastException, t);
		}
	}

	@Override
	public long castToInt(Target t) {
		try{
			return Long.valueOf(val());
		} catch(NumberFormatException e){
			throw new ConfigRuntimeException("Could not convert " + val() + " to an int.", Exceptions.ExceptionType.CastException, t);
		}
	}

	@Override
	public boolean castToBoolean() {
		return val().isEmpty() ? false : true;
	}

	@Override
	public boolean containsKey(String index) {
		try{
			int i = Integer.parseInt(index);
			return i < size();
		} catch(NumberFormatException e){
			return false;
		}
	}

	public Set<String> keySet() {
		Set<String> list = new HashSet<String>();
		for(int i = 0; i < size(); i++){
			list.add(Integer.toString(i));
		}
		return list;
	}
}
