/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCItem;
import org.bukkit.entity.Item;

/**
 *
 * @author Layton
 */
public class BukkitMCItem extends BukkitMCEntity implements MCItem{
	
	Item i;
	
	public BukkitMCItem(Item i){
		super(i);
		this.i = i;
	}
}
