/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.core.constructs;

/**
 *
 * @author Layton
 */
public class CString extends Construct implements Cloneable{
    
    public CString(String value, Target t){
        super(value, ConstructType.STRING, t);
    }
    
    public CString(char value, Target t){
        this(Character.toString(value), t);
    }
    
    public CString(CharSequence value, Target t){
        this(value.toString(), t);
    }
    
    @Override
    public CString clone() throws CloneNotSupportedException{
        return this;
    }

    @Override
    public boolean isDynamic() {
        return false;
    }
}
