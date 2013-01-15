
package com.laytonsmith.abstraction;

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
