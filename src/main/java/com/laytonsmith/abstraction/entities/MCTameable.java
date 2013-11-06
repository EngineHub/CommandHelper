package com.laytonsmith.abstraction.entities;

import com.laytonsmith.abstraction.MCAnimalTamer;

/**
 *
 * @author layton
 */
public interface MCTameable extends MCAgeable {

    public boolean isTamed();

    public void setTamed(boolean bln);

    public MCAnimalTamer getOwner();

    public void setOwner(MCAnimalTamer at);    
}