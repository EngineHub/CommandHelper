package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.MCColor;
import com.laytonsmith.abstraction.MCLivingEntity;
import com.laytonsmith.abstraction.MCPotionData;
import com.laytonsmith.abstraction.MCProjectileSource;
import com.laytonsmith.abstraction.blocks.MCBlockProjectileSource;
import com.laytonsmith.abstraction.bukkit.BukkitConvertor;
import com.laytonsmith.abstraction.bukkit.BukkitMCColor;
import com.laytonsmith.abstraction.bukkit.BukkitMCPotionData;
import com.laytonsmith.abstraction.bukkit.blocks.BukkitMCBlockProjectileSource;
import com.laytonsmith.abstraction.entities.MCAreaEffectCloud;
import com.laytonsmith.abstraction.enums.MCParticle;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCParticle;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Entity;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.BlockProjectileSource;
import org.bukkit.projectiles.ProjectileSource;

import java.util.ArrayList;
import java.util.List;

public class BukkitMCAreaEffectCloud extends BukkitMCEntity implements MCAreaEffectCloud {

	AreaEffectCloud aec;

	public BukkitMCAreaEffectCloud(Entity aec) {
		super(aec);
		this.aec = (AreaEffectCloud) aec;
	}

	@Override
	public MCPotionData getBasePotionData() {
		return new BukkitMCPotionData(aec.getBasePotionData());
	}

	@Override
	public MCColor getColor() {
		return BukkitMCColor.GetMCColor(aec.getColor());
	}

	@Override
	public List<MCLivingEntity.MCEffect> getCustomEffects() {
		List<MCLivingEntity.MCEffect> list = new ArrayList<>();
		for (PotionEffect pe : aec.getCustomEffects()) {
			list.add(new MCLivingEntity.MCEffect(pe.getType().getId(), pe.getAmplifier(),
					pe.getDuration(), pe.isAmbient(), pe.hasParticles()));
		}
		return list;
	}

	@Override
	public int getDuration() {
		return aec.getDuration();
	}

	@Override
	public int getDurationOnUse() {
		return aec.getDurationOnUse();
	}

	@Override
	public MCParticle getParticle() {
		return BukkitMCParticle.getConvertor().getAbstractedEnum(aec.getParticle());
	}

	@Override
	public float getRadius() {
		return aec.getRadius();
	}

	@Override
	public float getRadiusOnUse() {
		return aec.getRadiusOnUse();
	}

	@Override
	public float getRadiusPerTick() {
		return aec.getRadiusPerTick();
	}

	@Override
	public int getReapplicationDelay() {
		return aec.getReapplicationDelay();
	}

	@Override
	public MCProjectileSource getSource() {
		ProjectileSource source = aec.getSource();
		if (source instanceof BlockProjectileSource) {
			return new BukkitMCBlockProjectileSource((BlockProjectileSource) source);
		}
		return (MCProjectileSource) BukkitConvertor.BukkitGetCorrectEntity((Entity) source);
	}

	@Override
	public int getWaitTime() {
		return aec.getWaitTime();
	}

	@Override
	public void addCustomEffect(MCLivingEntity.MCEffect effect) {
		PotionEffect pe = new PotionEffect(PotionEffectType.getById(effect.getPotionID()),
				effect.getTicksRemaining(), effect.getStrength(), effect.isAmbient(), effect.hasParticles());
		aec.addCustomEffect(pe, true);
	}

	@Override
	public void clearCustomEffects() {
		aec.clearCustomEffects();
	}

	@Override
	public void setBasePotionData(MCPotionData pd) {
		aec.setBasePotionData((PotionData) pd.getHandle());
	}

	@Override
	public void setColor(MCColor color) {
		aec.setColor(BukkitMCColor.GetColor(color));
	}

	@Override
	public void setDuration(int ticks) {
		aec.setDuration(ticks);
	}

	@Override
	public void setDurationOnUse(int ticks) {
		aec.setDurationOnUse(ticks);
	}

	@Override
	public void setParticle(MCParticle particle) {
		aec.setParticle(BukkitMCParticle.getConvertor().getConcreteEnum(particle));
	}

	@Override
	public void setRadius(float radius) {
		aec.setRadius(radius);
	}

	@Override
	public void setRadiusOnUse(float radius) {
		aec.setRadiusOnUse(radius);
	}

	@Override
	public void setRadiusPerTick(float radius) {
		aec.setRadiusPerTick(radius);
	}

	@Override
	public void setReapplicationDelay(int ticks) {
		aec.setReapplicationDelay(ticks);
	}

	@Override
	public void setSource(MCProjectileSource source) {
		if (source == null) {
			aec.setSource(null);
		} else if (source instanceof MCBlockProjectileSource) {
			aec.setSource((BlockProjectileSource) source.getHandle());
		} else {
			aec.setSource((ProjectileSource) source.getHandle());
		}
	}

	@Override
	public void setWaitTime(int ticks) {
		aec.setWaitTime(ticks);
	}

}
