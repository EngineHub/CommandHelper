/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.abstraction;

/**
 *
 * @author layton
 */
public interface MCTameable extends MCEntity{
    public MCAnimalTamer getOwner();

    public boolean isTamed();

    public void setOwner(MCAnimalTamer at);

    public void setTamed(boolean bln);    
}
