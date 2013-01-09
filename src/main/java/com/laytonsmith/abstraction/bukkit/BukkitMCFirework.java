/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCColor;
import com.laytonsmith.abstraction.MCFirework;
import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.enums.MCFireworkType;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCFireworkType;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;

/**
 *
 * @author Layton
 */
public class BukkitMCFirework implements MCFirework {
	private FireworkEffect.Builder builder;
	private int strength;
	public BukkitMCFirework(){
		builder = FireworkEffect.builder();
		strength = 0;
	}
	
	public MCFirework setFlicker(boolean flicker) {
		builder.flicker(flicker);
		return this;
	}

	public MCFirework setTrail(boolean trail) {
		builder.trail(trail);
		return this;
	}

	public MCFirework addColor(MCColor color) {
		builder.withColor(BukkitMCColor.GetColor(color));
		return this;
	}

	public MCFirework addFadeColor(MCColor color) {
		builder.withFade(BukkitMCColor.GetColor(color));
		return this;
	}
	
	public MCFirework setType(MCFireworkType type){
		builder.with(BukkitMCFireworkType.getConvertor().getConcreteEnum(type));
		return this;
	}

	public MCFirework setStrength(int i) {
		strength = i;
		return this;
	}
	
	public void launch(MCLocation l) {
		FireworkEffect fe = builder.build();
		Location ll = ((BukkitMCLocation)l).asLocation();
		Firework fw = (Firework)ll.getWorld().spawnEntity(ll, EntityType.FIREWORK);
		FireworkMeta fwmeta = fw.getFireworkMeta();
		fwmeta.addEffect(fe);
		fwmeta.setPower(strength);
		fw.setFireworkMeta(fwmeta);
	}

	
}
