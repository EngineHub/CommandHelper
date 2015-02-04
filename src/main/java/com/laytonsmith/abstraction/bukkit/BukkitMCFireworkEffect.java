package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCColor;
import com.laytonsmith.abstraction.MCFireworkEffect;
import com.laytonsmith.abstraction.enums.MCFireworkType;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCFireworkType;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;

import java.util.ArrayList;
import java.util.List;

public class BukkitMCFireworkEffect implements MCFireworkEffect {

	FireworkEffect effect;
	public BukkitMCFireworkEffect(FireworkEffect eff) {
		effect = eff;
	}

	public FireworkEffect getEffect() {
		return effect;
	}

	@Override
	public List<MCColor> getColors() {
		List<MCColor> ret = new ArrayList<>();
		for (Color c : effect.getColors()) {
			ret.add(BukkitMCColor.GetMCColor(c));
		}
		return ret;
	}

	@Override
	public List<MCColor> getFadeColors() {
		List<MCColor> ret = new ArrayList<>();
		for (Color c : effect.getFadeColors()) {
			ret.add(BukkitMCColor.GetMCColor(c));
		}
		return ret;
	}

	@Override
	public MCFireworkType getShape() {
		return BukkitMCFireworkType.getConvertor().getAbstractedEnum(effect.getType());
	}

	@Override
	public boolean hasFlicker() {
		return effect.hasFlicker();
	}

	@Override
	public boolean hasTrail() {
		return effect.hasTrail();
	}
}
