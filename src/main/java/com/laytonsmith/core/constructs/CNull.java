


package com.laytonsmith.core.constructs;

/**
 *
 * @author layton
 */
public class CNull extends Construct implements Cloneable{
    
    public static final long serialVersionUID = 1L;
    
    //Nulls are often manufactured
    public CNull(){
        this(Target.UNKNOWN);
    }
    
    public CNull(Target t){
        super("null", ConstructType.NULL, t);
    }
    
    @Override
    public CNull clone() throws CloneNotSupportedException{
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
    public boolean isDynamic() {
        return false;
    }
    
}
