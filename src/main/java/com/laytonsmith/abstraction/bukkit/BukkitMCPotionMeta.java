package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.PureUtilities.Common.ReflectionUtils;
import com.laytonsmith.abstraction.MCColor;
import com.laytonsmith.abstraction.MCLivingEntity.MCEffect;
import com.laytonsmith.abstraction.MCPotionData;
import com.laytonsmith.abstraction.MCPotionMeta;
import com.laytonsmith.abstraction.enums.MCPotionEffectType;
import com.laytonsmith.abstraction.enums.MCPotionType;
import com.laytonsmith.abstraction.enums.MCVersion;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCPotionEffectType;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCPotionType;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.Target;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

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
		return new BukkitMCPotionData(ReflectionUtils.invokeMethod(PotionMeta.class, pm, "getBasePotionData"));
	}

	@Override
	public void setBasePotionData(MCPotionData bpd) {
		ReflectionUtils.invokeMethod(pm, "setBasePotionData", bpd.getHandle());
	}

	@Override
	public MCPotionType getBasePotionType() {
		PotionType type = pm.getBasePotionType();
		if(type == null) {
			return null;
		}
		return BukkitMCPotionType.valueOfConcrete(pm.getBasePotionType());
	}

	@Override
	public void setBasePotionType(MCPotionType pt) {
		if(pt == null) {
			pm.setBasePotionType(null);
		} else {
			pm.setBasePotionType((PotionType) pt.getConcrete());
		}
	}

	@Override
	public boolean addCustomEffect(MCPotionEffectType type, int strength, int ticks, boolean ambient, boolean particles, boolean icon, boolean force, Target t) {
		if(ticks < 0) {
			if(Static.getServer().getMinecraftVersion().gte(MCVersion.MC1_19_4)) {
				ticks = -1;
			} else {
				ticks = Integer.MAX_VALUE;
			}
		}
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
