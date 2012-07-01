/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.core.constructs;

/**
 *
 * @author Layton
 */
public class CEntry extends Construct{
    Construct ckey;
    Construct construct;

    public CEntry(Construct key, Construct value, Target t){
        super(key.val() + ":(CEntry)", ConstructType.ENTRY, t);
        this.ckey = key;
        this.construct = value;
    }
    public CEntry(String value, Target t){
        super(value, ConstructType.ENTRY, t);
        throw new UnsupportedOperationException("CEntry Constructs cannot use this constructor");
    }
    
    public Construct construct(){
        return this.construct;
    }
    
    @Override
    public boolean isDynamic() {
        return false;
    }

    @Override
    public String val(){
        return construct.val();
    }
    
}
