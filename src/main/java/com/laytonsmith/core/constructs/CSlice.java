package com.laytonsmith.core.constructs;

import com.laytonsmith.annotations.typename;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.Exceptions;
import com.laytonsmith.core.natives.interfaces.Mixed;

/**
 *
 * @author layton
 */
@typename("slice")
public class CSlice extends CArray {
    private int start;
    private int finish;
	private int direction;
	private int max;
	private int size;
    public CSlice(String slice, Target t) throws ConfigCompileException{
        super(t);
        String [] split = slice.split("\\.\\.");
        if(split.length > 2){
            throw new ConfigCompileException("Invalid slice notation! (" + slice + ")", t);
        }
        
        String sstart;
        String sfinish;
        if(split.length == 1){
            sstart = slice.trim().substring(0, slice.trim().length() - 2);
            sfinish = "-1";
        } else {
            if(slice.trim().startsWith("..")){
                sstart = "0";
                sfinish = slice.trim().substring(2);
            } else { 
            sstart = split[0];
            sfinish = split[1];
            }
        }
        try{
            start = Integer.parseInt(sstart.trim());
            finish = Integer.parseInt(sfinish.trim());
        } catch(NumberFormatException e){
            throw new ConfigRuntimeException("Expecting integer in a slice, but was given " + sstart + " and " + sfinish, Exceptions.ExceptionType.CastException,  t);
        }
		calculateCaches();
    }
    
    public CSlice(int from, int to, Target t){
        super(t);
        this.start = from;
        this.finish = to;
		calculateCaches();
    }
	
	private void calculateCaches(){
		direction = start < finish?1:start==finish?0:-1;
		max = Math.abs(finish - start);
		size = max + 1;
	}
    
    public int getStart(){
        return start;
    }
    
    public int getFinish(){
        return finish;
    }

    @Override
    public boolean isDynamic() {
        return false;
    }

	@Override
	public String toString() {
		return start + ".." + finish;
	}

	@Override
	public boolean inAssociativeMode() {
		return false;
	}

	@Override
	public void set(CPrimitive index, Mixed c, Target t) {
		throw new ConfigRuntimeException("slices cannot set values", Exceptions.ExceptionType.CastException, t);
	}

	@Override
	public Construct get(CPrimitive index, Target t) {
		long i = index.castToInt(t);
		if(i > max){
			throw new ConfigRuntimeException("Index out of bounds. Index: " + i + " Size: " + max, Exceptions.ExceptionType.RangeException, t);
		}
		return new CInt(start + (direction * i), t);
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public boolean contains(Construct c) {
		try{
			long i = ((CPrimitive)c).castToInt(Target.UNKNOWN);
			if(start < finish){
				return start <= i && i <= finish;
			} else {
				return start >= i && i <= finish;
			}
		} catch(Exception e){
			return false;
		}
	}

	@Override
	public boolean containsKey(String c) {
		try{
			long i = Long.parseLong(c);
			return i >= 0 && i < size;
		} catch(NumberFormatException e){
			return false;
		}
	}

	@Override
	public String typeName() {
		return "slice";
	}
	
}
