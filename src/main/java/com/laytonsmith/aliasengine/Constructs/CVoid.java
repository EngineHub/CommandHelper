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
public class CVoid extends Construct{
    public CVoid(int line_num, File file){
        super("", ConstructType.VOID, line_num, file);
    }
}
