/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.core.constructs;

import com.laytonsmith.core.Env;
import com.laytonsmith.core.GenericTreeNode;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Layton
 */
public class CEntry extends Construct{
    Construct ckey;
    Construct construct;

    public CEntry(String value, int line_num, File file){
        super(value, ConstructType.ENTRY, line_num, file);
        throw new UnsupportedOperationException("CEntry Constructs cannot use this constructor");
    }
    public CEntry(Construct key, Construct value, int line_num, File file){
        super(key.val() + ":(CEntry)", ConstructType.ENTRY, line_num, file);
        this.ckey = key;
        this.construct = value;
    }
    
    @Override
    public String val(){
        return construct.val();
    }
    
    public Construct construct(){
        return this.construct;
    }
}
