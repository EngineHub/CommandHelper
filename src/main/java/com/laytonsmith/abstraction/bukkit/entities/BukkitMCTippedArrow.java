package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.MCLivingEntity;
import com.laytonsmith.abstraction.MCPotionData;
import com.laytonsmith.abstraction.bukkit.BukkitMCPotionData;
import com.laytonsmith.abstraction.entities.MCTippedArrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.TippedArrow;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

public class BukkitMCTippedArrow extends BukkitMCArrow implements MCTippedArrow {

	TippedArrow ta;

	public BukkitMCTippedArrow(Entity ta) {
		super(ta);
		this.ta = (TippedArrow) ta;
	}

	@Override
	public MCPotionData getBasePotionData() {
		return new BukkitMCPotionData(ta.getBasePotionData());
	}

	@Override
	public List<MCLivingEntity.MCEffect> getCustomEffects() {
		List<MCLivingEntity.MCEffect> list = new ArrayList<>();
		for(PotionEffect pe : ta.getCustomEffects()) {
			list.add(new MCLivingEntity.MCEffect(pe.getType().getId(), pe.getAmplifier(),
					pe.getDuration(), pe.isAmbient(), pe.hasParticles()));
		}
		return list;
	}

	@Override
	public void addCustomEffect(MCLivingEntity.MCEffect effect) {
		PotionEffect pe = new PotionEffect(PotionEffectType.getById(effect.getPotionID()),
				effect.getTicksRemaining(), effect.getStrength(), effect.isAmbient(), effect.hasParticles());
		ta.addCustomEffect(pe, true);
	}

	@Override
	public void clearCustomEffects() {
		ta.clearCustomEffects();
	}

	@Override
	public void setBasePotionData(MCPotionData pd) {
		ta.setBasePotionData((PotionData) pd.getHandle());
	}

}
