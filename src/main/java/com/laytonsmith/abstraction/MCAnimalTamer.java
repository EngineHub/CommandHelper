/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.abstraction;

/**
 *
 * @author layton
 */
public interface MCAnimalTamer {

    public MCOfflinePlayer getOfflinePlayer();

    public boolean isOfflinePlayer();

    public boolean isHumanEntity();

    public MCHumanEntity getHumanEntity();
    
}
