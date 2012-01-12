/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.laytonsmith.puls3.core.constructs;

import java.io.File;

/**
 *
 * @author layton
 */
public class CNull extends Construct implements Cloneable{
    
    public static final long serialVersionUID = 1L;
    public CNull(int line_num, File file){
        super("null", ConstructType.NULL, line_num, file);
    }
    
    @Override
    public CNull clone() throws CloneNotSupportedException{
        return (CNull) super.clone();
    }
}
