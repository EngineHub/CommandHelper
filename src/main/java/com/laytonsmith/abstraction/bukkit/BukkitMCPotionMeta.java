package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCLivingEntity.MCEffect;
import com.laytonsmith.abstraction.MCPotionData;
import com.laytonsmith.abstraction.MCPotionMeta;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.exceptions.CRE.CRERangeException;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

public class BukkitMCPotionMeta extends BukkitMCItemMeta implements MCPotionMeta {

	PotionMeta pm;
	public BukkitMCPotionMeta(PotionMeta pomet) {
		super(pomet);
		pm = pomet;
	}

	@Override
	public MCPotionData getBasePotionData() {
		return new BukkitMCPotionData(pm.getBasePotionData());
	}

	@Override
	public void setBasePotionData(MCPotionData bpd){
		pm.setBasePotionData((PotionData) bpd.getHandle());
	}
	
	@Override
	public boolean addCustomEffect(int potionID, int strength, int seconds, boolean ambient, boolean overwrite, Target t) {
		int maxID = PotionEffectType.values().length;
		if (potionID < 1 || potionID > maxID) {
			throw new CRERangeException("Invalid effect ID, must be from 1-" + maxID, t);
		}
		PotionEffect pe = new PotionEffect(PotionEffectType.getById(potionID), seconds * 20, strength, ambient);
		return pm.addCustomEffect(pe, overwrite);
	}

	@Override
	public boolean clearCustomEffects() {
		return pm.clearCustomEffects();
	}

	@Override
	public List<MCEffect> getCustomEffects() {
		List<MCEffect> list = new ArrayList<MCEffect>();
		for (PotionEffect pe : pm.getCustomEffects()) {
			list.add(new MCEffect(pe.getType().getId(), pe.getAmplifier(), pe.getDuration() / 20, pe.isAmbient()));
		}
		return list;
	}

	@Override
	public boolean hasCustomEffect(int id) {
		return pm.hasCustomEffect(PotionEffectType.getById(id));
	}

	@Override
	public boolean hasCustomEffects() {
		return pm.hasCustomEffects();
	}

	@Override
	public boolean removeCustomEffect(int id) {
		return pm.removeCustomEffect(PotionEffectType.getById(id));
	}

	@Override
	public boolean setMainEffect(int id) {
		return pm.setMainEffect(PotionEffectType.getById(id));
	}

}
