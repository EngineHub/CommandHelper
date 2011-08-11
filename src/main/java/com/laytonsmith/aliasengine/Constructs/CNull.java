/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.laytonsmith.aliasengine.Constructs;

import java.io.File;

/**
 *
 * @author layton
 */
public class CNull extends Construct{
    public CNull(int line_num, File file){
        super("null", ConstructType.NULL, line_num, file);
    }
}
