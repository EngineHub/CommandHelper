/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.puls3.abstraction;

/**
 *
 * @author layton
 */
public interface MCTameable extends MCEntity{
    public boolean isTamed();

    public void setTamed(boolean bln);

    public MCAnimalTamer getOwner();

    public void setOwner(MCAnimalTamer at);    
}
