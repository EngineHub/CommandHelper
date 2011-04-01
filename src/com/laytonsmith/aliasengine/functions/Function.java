/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.laytonsmith.aliasengine.functions;

import com.laytonsmith.aliasengine.Constructs.Construct;
import com.laytonsmith.aliasengine.CancelCommandException;

/**
 *
 * @author layton
 */
public interface Function {
    public String getName();
    public Integer[] numArgs();
    public Construct exec(int line_num, Construct ... args) throws CancelCommandException;
}
