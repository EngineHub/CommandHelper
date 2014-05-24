


package com.laytonsmith.core.constructs;

import com.laytonsmith.annotations.typeof;

/**
 * Represents a MethodScript null value.
 */
@typeof("null")
public final class CNull extends Construct implements Cloneable{
    
    public static final long serialVersionUID = 1L;
	
	/**
	 * Null values do not normally need to be duplicated, since they are
	 * immutable, and for values that have an unknown code target, are
	 * always equal. In cases where a null is generated from inside Java,
	 * this value should be returned, instead of generating a new one.
	 */
	public static final CNull NULL = new CNull(Target.UNKNOWN);
    
	/**
	 * Constructs a new CNull object. Generally speaking, this should
	 * only be used when creating null values that are literally created
	 * by user code, all internal code should simply use {@link #NULL}.
	 * This method DOES check the target however, and if the target is
	 * {@link Target#UNKNOWN}, {@link CNull#NULL} is returned anyways.
	 * @param t
	 * @return 
	 */
	public static CNull GenerateCNull(Target t){
		if(t == Target.UNKNOWN){
			return NULL;
		} else {
			return new CNull(t);
		}
	}
	
	/**
	 * Private constructor to force usage of {@link #GenerateCNull(com.laytonsmith.core.constructs.Target)}, which can
	 * return existing objects.
	 * @param t 
	 */
    private CNull(Target t){
        super("null", ConstructType.NULL, t);
    }
    
    @Override
	@SuppressWarnings("CloneDoesntCallSuperClone")
    public CNull clone() throws CloneNotSupportedException {
        return this;
    }
    
    @Override
    public String val(){
        return "null";
    }
    
    @Override
    public String nval(){
        return null;
    }

	@Override
	public boolean equals(Object obj) {
		return obj instanceof CNull;
	}

    @Override
    public boolean isDynamic() {
        return false;
    }

	@Override
	public int hashCode() {
		int hash = 7;
		return hash;
	}
    
}
