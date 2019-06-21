package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.MCLivingEntity;
import com.laytonsmith.abstraction.MCPotionData;
import com.laytonsmith.abstraction.bukkit.BukkitMCPotionData;
import com.laytonsmith.abstraction.entities.MCArrow;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCPotionEffectType;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

public class BukkitMCArrow extends BukkitMCProjectile implements MCArrow {

	private final Arrow arrow;

	public BukkitMCArrow(Entity arrow) {
		super(arrow);
		this.arrow = (Arrow) arrow;
	}

	@Override
	public int getKnockbackStrength() {
		return this.arrow.getKnockbackStrength();
	}

	@Override
	public void setKnockbackStrength(int strength) {
		this.arrow.setKnockbackStrength(strength);
	}

	@Override
	public boolean isCritical() {
		return this.arrow.isCritical();
	}

	@Override
	public void setCritical(boolean critical) {
		this.arrow.setCritical(critical);
	}

	@Override
	public double getDamage() {
		return this.arrow.getDamage();
	}

	@Override
	public void setDamage(double damage) {
		this.arrow.setDamage(damage);
	}

	@Override
	public MCPotionData getBasePotionData() {
		return new BukkitMCPotionData(arrow.getBasePotionData());
	}

	@Override
	public List<MCLivingEntity.MCEffect> getCustomEffects() {
		List<MCLivingEntity.MCEffect> list = new ArrayList<>();
		for(PotionEffect pe : arrow.getCustomEffects()) {
			list.add(new MCLivingEntity.MCEffect(BukkitMCPotionEffectType.valueOfConcrete(pe.getType()),
					pe.getAmplifier(), pe.getDuration(), pe.isAmbient(), pe.hasParticles(), pe.hasIcon()));
		}
		return list;
	}

	@Override
	public void addCustomEffect(MCLivingEntity.MCEffect effect) {
		PotionEffect pe = new PotionEffect((PotionEffectType) effect.getPotionEffectType().getConcrete(),
				effect.getTicksRemaining(), effect.getStrength(), effect.isAmbient(), effect.hasParticles(),
				effect.showIcon());
		arrow.addCustomEffect(pe, true);
	}

	@Override
	public void clearCustomEffects() {
		arrow.clearCustomEffects();
	}

	@Override
	public void setBasePotionData(MCPotionData pd) {
		arrow.setBasePotionData((PotionData) pd.getHandle());
	}
}
