/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCLightningStrike;
import org.bukkit.entity.LightningStrike;

/**
 *
 * @author Jim
 */
public class BukkitMCLightningStrike extends BukkitMCEntity implements MCLightningStrike{

	LightningStrike ls;
	
	public BukkitMCLightningStrike(LightningStrike ls){
		super(ls);
		this.ls = ls;
	}
	
	public boolean isEffect(){
		return ls.isEffect();
	}
}
