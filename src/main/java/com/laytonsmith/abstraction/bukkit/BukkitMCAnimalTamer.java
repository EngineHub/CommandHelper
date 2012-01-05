/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCAnimalTamer;
import org.bukkit.entity.AnimalTamer;

/**
 *
 * @author layton
 */
public class BukkitMCAnimalTamer implements MCAnimalTamer{
    AnimalTamer at;
    public BukkitMCAnimalTamer(AnimalTamer at){
        this.at = at;
    }
}
