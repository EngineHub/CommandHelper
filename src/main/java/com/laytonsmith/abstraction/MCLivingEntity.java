/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.abstraction;

/**
 *
 * @author layton
 */
public interface MCLivingEntity extends MCEntity {
    public int getHealth();

    public void setHealth(int i);

    public int getMaxHealth();

    public void damage(int i);
}
