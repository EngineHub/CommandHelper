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
    public void damage(int i);

    public int getHealth();

    public int getMaxHealth();

    public void setHealth(int i);
}
