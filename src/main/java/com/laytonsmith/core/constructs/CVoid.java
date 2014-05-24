

package com.laytonsmith.core.constructs;

import com.laytonsmith.annotations.typeof;

/**
 * 
 */
@typeof("void")
public final class CVoid extends Construct implements Cloneable{
    
	/**
	 * Void values do not normally need to be duplicated, since they are
	 * immutable, and for values that have an unknown code target, are
	 * always equal. In cases where a void is generated from inside Java,
	 * this value should be returned, instead of generating a new one.
	 */
	public static final CVoid VOID = new CVoid(Target.UNKNOWN);
    
	/**
	 * Constructs a new CVoid object. Generally speaking, this should
	 * only be used when creating void values that are literally created
	 * by user code, all internal code should simply use {@link #VOID}.
	 * This method DOES check the target however, and if the target is
	 * {@link Target#UNKNOWN}, {@link CVoid#VOID} is returned anyways.
	 * @param t
	 * @return 
	 */
	public static CVoid GenerateCVoid(Target t){
		if(t == Target.UNKNOWN){
			return VOID;
		} else {
			return new CVoid(t);
		}
	}
	
	/**
	 * Private constructor to force usage of {@link #GenerateCVoid(com.laytonsmith.core.constructs.Target)}, which can
	 * return existing objects.
	 * @param t 
	 */
    private CVoid(Target t){
        super("", ConstructType.VOID, t);
    }
    
    @Override
	@SuppressWarnings("CloneDoesntCallSuperClone")
    public CVoid clone() throws CloneNotSupportedException{
        return this;
    }

	@Override
	public boolean equals(Object obj) {
		return obj instanceof CVoid;
	}
	
	@Override
	public int hashCode() {
		int hash = 7;
		return hash;
	}

    @Override
    public boolean isDynamic() {
        return false;
    }

}
