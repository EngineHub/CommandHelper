/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine.Constructs;

/**
 *
 * @author Layton
 */
public class CString extends Construct{
    public CString(String value, int line_num){
        super(value, ConstructType.STRING, line_num);
    }
}
