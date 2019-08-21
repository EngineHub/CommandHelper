package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCColor;
import com.laytonsmith.abstraction.MCLivingEntity.MCEffect;
import com.laytonsmith.abstraction.MCPotionData;
import com.laytonsmith.abstraction.MCPotionMeta;
import com.laytonsmith.abstraction.enums.MCPotionEffectType;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCPotionEffectType;
import com.laytonsmith.core.constructs.Target;
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
	public boolean addCustomEffect(MCPotionEffectType type, int strength, int ticks, boolean ambient, boolean particles, boolean icon, boolean force, Target t) {
		PotionEffect pe = new PotionEffect((PotionEffectType) type.getConcrete(), ticks, strength, ambient, particles, icon);
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
			list.add(new MCEffect(BukkitMCPotionEffectType.valueOfConcrete(pe.getType()), pe.getAmplifier(),
					pe.getDuration(), pe.isAmbient(), pe.hasParticles(), pe.hasIcon()));
		}
		return list;
	}

	@Override
	public boolean hasCustomEffect(MCPotionEffectType type) {
		return pm.hasCustomEffect((PotionEffectType) type.getConcrete());
	}

	@Override
	public boolean hasCustomEffects() {
		return pm.hasCustomEffects();
	}

	@Override
	public boolean removeCustomEffect(MCPotionEffectType type) {
		return pm.removeCustomEffect((PotionEffectType) type.getConcrete());
	}

	public boolean hasColor() {
		return pm.hasColor();
	}

	public MCColor getColor() {
		return BukkitMCColor.GetMCColor(pm.getColor());
	}

	@Override
	public void setColor(MCColor color) {
		pm.setColor(BukkitMCColor.GetColor(color));
	}
}
