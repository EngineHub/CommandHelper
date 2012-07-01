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

    public String getLine(int i);

    public void setLine(int i, String line1);
    
}
