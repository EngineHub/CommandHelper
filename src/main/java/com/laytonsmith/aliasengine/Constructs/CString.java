/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine.Constructs;

import java.io.File;

/**
 *
 * @author Layton
 */
public class CString extends Construct implements Cloneable{
    
    public static final long serialVersionUID = 1L;
    public CString(String value, int line_num, File file){
        super(value, ConstructType.STRING, line_num, file);
    }
    
    @Override
    public CString clone() throws CloneNotSupportedException{
        return (CString) super.clone();
    }
}
