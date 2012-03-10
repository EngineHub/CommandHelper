/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.core.constructs;

import java.io.File;

/**
 *
 * @author Layton
 */
public class CString extends Construct implements Cloneable{
    
    public CString(String value, int line_num, File file){
        super(value, ConstructType.STRING, line_num, file);
    }
    
    public CString(char value, int line_num, File file){
        this(Character.toString(value), line_num, file);
    }
    
    @Override
    public CString clone() throws CloneNotSupportedException{
        return (CString) super.clone();
    }
}
