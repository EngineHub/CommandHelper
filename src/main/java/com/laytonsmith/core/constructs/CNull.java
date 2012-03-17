/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.laytonsmith.core.constructs;

import java.io.File;

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
        return (CNull) super.clone();
    }
    
    @Override
    public String val(){
        return "null";
    }
    
    @Override
    public String nval(){
        return null;
    }
    
}
