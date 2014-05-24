
package com.laytonsmith.abstraction;

/**
 *
 * 
 */
public interface MCTameable extends MCAgeable {
    public boolean isTamed();

    public void setTamed(boolean bln);

    public MCAnimalTamer getOwner();

    public void setOwner(MCAnimalTamer at);    
}
