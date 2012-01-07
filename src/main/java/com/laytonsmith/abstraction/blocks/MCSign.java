/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.abstraction.blocks;

/**
 *
 * @author layton
 */
public interface MCSign extends MCBlockState{       

    public void setLine(int i, String line1);

    public String getLine(int i);
    
}
