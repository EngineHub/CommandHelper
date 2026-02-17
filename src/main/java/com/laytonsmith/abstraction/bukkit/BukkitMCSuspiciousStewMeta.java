package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCLivingEntity.MCEffect;
import com.laytonsmith.abstraction.MCSuspiciousStewMeta;
import com.laytonsmith.abstraction.enums.MCPotionEffectType;
import com.laytonsmith.abstraction.enums.MCVersion;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCPotionEffectType;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.Target;
import org.bukkit.inventory.meta.SuspiciousStewMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

public class BukkitMCSuspiciousStewMeta extends BukkitMCItemMeta implements MCSuspiciousStewMeta {

	SuspiciousStewMeta ssm;

	public BukkitMCSuspiciousStewMeta(SuspiciousStewMeta ssm) {
		super(ssm);
		this.ssm = ssm;
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
		return this.ssm.addCustomEffect(pe, force);
	}

	@Override
	public List<MCEffect> getCustomEffects() {
		List<MCEffect> list = new ArrayList<>();
		for(PotionEffect pe : this.ssm.getCustomEffects()) {
			list.add(new MCEffect(BukkitMCPotionEffectType.valueOfConcrete(pe.getType()), pe.getAmplifier(),
					pe.getDuration(), pe.isAmbient(), pe.hasParticles(), pe.hasIcon()));
		}
		return list;
	}
}
