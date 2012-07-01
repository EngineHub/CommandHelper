/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.abstraction.blocks;

/**
 *
 * @author layton
 */
public interface MCMaterial {
    short getMaxDurability();

    public int getMaxStackSize();

    public int getType();
}
