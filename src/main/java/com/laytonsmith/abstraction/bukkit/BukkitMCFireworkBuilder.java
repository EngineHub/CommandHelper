package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCColor;
import com.laytonsmith.abstraction.MCFireworkBuilder;
import com.laytonsmith.abstraction.MCFireworkEffect;
import com.laytonsmith.abstraction.enums.MCFireworkType;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCFireworkType;
import org.bukkit.FireworkEffect;

public class BukkitMCFireworkBuilder implements MCFireworkBuilder {
	private FireworkEffect.Builder builder;

	public BukkitMCFireworkBuilder(){
		builder = FireworkEffect.builder();
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
	public MCFireworkEffect build(){
		return new BukkitMCFireworkEffect(builder.build());
	}

}
