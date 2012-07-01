/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

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
    public boolean isDynamic() {
        return false;
    }
    
    @Override
    public String nval(){
        return null;
    }

    @Override
    public String val(){
        return "null";
    }
    
}
