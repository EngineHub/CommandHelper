/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCColor;
import com.laytonsmith.abstraction.MCFireworkBuilder;
import com.laytonsmith.abstraction.MCFireworkMeta;
import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCFirework;
import com.laytonsmith.abstraction.entities.MCFirework;
import com.laytonsmith.abstraction.enums.MCFireworkType;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCFireworkType;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;

/**
 *
 *
 */
public class BukkitMCFireworkBuilder implements MCFireworkBuilder {
	private FireworkEffect.Builder builder;
	private int strength;
	public BukkitMCFireworkBuilder(){
		builder = FireworkEffect.builder();
		strength = 0;
	}

	@Override
	public MCFireworkBuilder setFlicker(boolean flicker) {
		builder.flicker(flicker);
		return this;
	}

	@Override
	public MCFireworkBuilder setTrail(boolean trail) {
		builder.trail(trail);
		return this;
	}

	@Override
	public MCFireworkBuilder addColor(MCColor color) {
		builder.withColor(BukkitMCColor.GetColor(color));
		return this;
	}

	@Override
	public MCFireworkBuilder addFadeColor(MCColor color) {
		builder.withFade(BukkitMCColor.GetColor(color));
		return this;
	}

	@Override
	public MCFireworkBuilder setType(MCFireworkType type){
		builder.with(BukkitMCFireworkType.getConvertor().getConcreteEnum(type));
		return this;
	}

	@Override
	public MCFireworkBuilder setStrength(int i) {
		strength = i;
		return this;
	}

	@Override
	public MCFirework launch(MCLocation l) {
		FireworkEffect fe = builder.build();
		Location ll = ((BukkitMCLocation)l).asLocation();
		Firework fw = (Firework)ll.getWorld().spawnEntity(ll, EntityType.FIREWORK);
		FireworkMeta fwmeta = fw.getFireworkMeta();
		fwmeta.addEffect(fe);
		fwmeta.setPower(strength);
		fw.setFireworkMeta(fwmeta);
		return new BukkitMCFirework(fw);
	}

	@Override
	public void createFireworkMeta(MCFireworkMeta meta) {
		FireworkMeta m = ((BukkitMCFireworkMeta)meta).fm;
		FireworkEffect fem = builder.build();
		m.clearEffects();
		m.addEffect(fem);
		m.setPower(strength);
	}


}
