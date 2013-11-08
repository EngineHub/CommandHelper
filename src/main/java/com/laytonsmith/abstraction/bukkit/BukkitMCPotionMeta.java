package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCLivingEntity.MCEffect;
import com.laytonsmith.abstraction.MCPotionMeta;
import com.laytonsmith.abstraction.bukkit.BukkitMCItemMeta;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class BukkitMCPotionMeta extends BukkitMCItemMeta implements MCPotionMeta {

	PotionMeta pm;
	public BukkitMCPotionMeta(PotionMeta pomet) {
		super(pomet);
		pm = pomet;
	}
	
	public boolean addCustomEffect(int potionID, int strength, int seconds, boolean ambient, boolean overwrite, Target t) {
		int maxID = PotionEffectType.values().length;
		if (potionID < 1 || potionID > maxID) {
			throw new ConfigRuntimeException("Invalid effect ID, must be from 1-" + maxID, ExceptionType.RangeException, t);
		}
		PotionEffect pe = new PotionEffect(PotionEffectType.getById(potionID), 
				(int) Static.msToTicks(seconds * 1000), strength, ambient);
		return pm.addCustomEffect(pe, overwrite);
	}

	public boolean clearCustomEffects() {
		return pm.clearCustomEffects();
	}

	public List<MCEffect> getCustomEffects() {
		List<MCEffect> list = new ArrayList<MCEffect>();
		for (PotionEffect pe : pm.getCustomEffects()) {
			list.add(new MCEffect(pe.getType().getId(), pe.getAmplifier(), 
					(int)(Static.ticksToMs(pe.getDuration()) / 1000), pe.isAmbient()));
		}
		return list;
	}

	public boolean hasCustomEffect(int id) {
		return pm.hasCustomEffect(PotionEffectType.getById(id));
	}

	public boolean hasCustomEffects() {
		return pm.hasCustomEffects();
	}

	public boolean removeCustomEffect(int id) {
		return pm.removeCustomEffect(PotionEffectType.getById(id));
	}

	public boolean setMainEffect(int id) {
		return pm.setMainEffect(PotionEffectType.getById(id));
	}

}
