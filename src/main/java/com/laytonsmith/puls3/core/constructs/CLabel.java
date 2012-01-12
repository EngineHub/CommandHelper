/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.puls3.core.constructs;

import com.laytonsmith.puls3.core.constructs.Construct.ConstructType;

/**
 *
 * @author Layton
 */
public class CLabel extends Construct{
    Construct label;
    public CLabel(Construct value){
        super(value.val(), ConstructType.LABEL, value.line_num, value.file); 
        label = value;
    }
    
    public Construct cVal(){
        return label;
    }
}
