package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.PureUtilities.Common.ReflectionUtils;
import com.laytonsmith.abstraction.MCLivingEntity;
import com.laytonsmith.abstraction.MCPotionData;
import com.laytonsmith.abstraction.bukkit.BukkitMCPotionData;
import com.laytonsmith.abstraction.entities.MCArrow;
import com.laytonsmith.abstraction.enums.MCPotionType;
import com.laytonsmith.abstraction.enums.MCVersion;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCPotionEffectType;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCPotionType;
import com.laytonsmith.core.Static;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

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
		return new BukkitMCPotionData(ReflectionUtils.invokeMethod(arrow, "getBasePotionData"));
	}

	@Override
	public MCPotionType getBasePotionType() {
		PotionType type = this.arrow.getBasePotionType();
		if(type == null) {
			return null;
		}
		return BukkitMCPotionType.valueOfConcrete(type);
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
		int ticks = effect.getTicksRemaining();
		if(ticks < 0) {
			if(Static.getServer().getMinecraftVersion().gte(MCVersion.MC1_19_4)) {
				ticks = -1;
			} else {
				ticks = Integer.MAX_VALUE;
			}
		}
		PotionEffect pe = new PotionEffect((PotionEffectType) effect.getPotionEffectType().getConcrete(),
				ticks, effect.getStrength(), effect.isAmbient(), effect.hasParticles(),
				effect.showIcon());
		arrow.addCustomEffect(pe, true);
	}

	@Override
	public void clearCustomEffects() {
		arrow.clearCustomEffects();
	}

	@Override
	public void setBasePotionData(MCPotionData pd) {
		ReflectionUtils.invokeMethod(arrow, "setBasePotionData", pd.getHandle());
	}

	@Override
	public void setBasePotionType(MCPotionType type) {
		if(type == null) {
			this.arrow.setBasePotionType(null);
		} else {
			this.arrow.setBasePotionType((PotionType) type.getConcrete());
		}
	}

	@Override
	public int getPierceLevel() {
		return arrow.getPierceLevel();
	}

	@Override
	public void setPierceLevel(int level) {
		arrow.setPierceLevel(level);
	}

	@Override
	public MCArrow.PickupStatus getPickupStatus() {
		return MCArrow.PickupStatus.valueOf(arrow.getPickupStatus().name());
	}

	@Override
	public void setPickupStatus(MCArrow.PickupStatus status) {
		arrow.setPickupStatus(AbstractArrow.PickupStatus.valueOf(status.name()));
	}
}
