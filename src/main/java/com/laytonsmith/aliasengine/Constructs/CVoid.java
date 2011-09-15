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
public class CVoid extends Construct implements Cloneable{
    
    public static final long serialVersionUID = 1L;
    public CVoid(int line_num, File file){
        super("", ConstructType.VOID, line_num, file);
    }
    
    @Override
    public CVoid clone() throws CloneNotSupportedException{
        return (CVoid) super.clone();
    }
}
