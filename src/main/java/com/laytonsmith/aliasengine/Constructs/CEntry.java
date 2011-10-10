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
public class CEntry extends Construct{
    Construct ckey;
    Construct cvalue;
    public CEntry(String value, int line_num, File file){
        super(value, ConstructType.ENTRY, line_num, file);
        throw new UnsupportedOperationException("CEntry Constructs cannot use this constructor");
    }
    public CEntry(Construct key, Construct value, int line_num, File file){
        super(key.val() + ":" + value.val(), ConstructType.ENTRY, line_num, file);
        this.ckey = key;
        this.cvalue = value;
    }
}
