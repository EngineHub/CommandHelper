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
	public void setBasePotionData(MCPotionData bpd) {
		pm.setBasePotionData((PotionData) bpd.getHandle());
	}

	@Override
	public boolean addCustomEffect(int id, int strength, int ticks, boolean ambient, boolean particles, boolean icon, boolean force, Target t) {
		int maxID = PotionEffectType.values().length;
		if(id < 1 || id > maxID) {
			throw new CRERangeException("Invalid effect ID, must be from 1-" + maxID, t);
		}
		PotionEffect pe = new PotionEffect(PotionEffectType.getById(id), ticks, strength, ambient, particles, icon);
		return pm.addCustomEffect(pe, force);
	}

	@Override
	public boolean clearCustomEffects() {
		return pm.clearCustomEffects();
	}

	@Override
	public List<MCEffect> getCustomEffects() {
		List<MCEffect> list = new ArrayList<>();
		for(PotionEffect pe : pm.getCustomEffects()) {
			list.add(new MCEffect(pe.getType().getId(), pe.getAmplifier(), pe.getDuration(),
					pe.isAmbient(), pe.hasParticles(), pe.hasIcon()));
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

}
