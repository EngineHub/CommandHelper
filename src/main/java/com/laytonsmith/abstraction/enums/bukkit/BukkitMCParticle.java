package com.laytonsmith.abstraction.enums.bukkit;

import com.laytonsmith.abstraction.MCColor;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.MCParticleData;
import com.laytonsmith.abstraction.blocks.MCBlockData;
import com.laytonsmith.abstraction.bukkit.BukkitMCColor;
import com.laytonsmith.abstraction.enums.MCParticle;
import com.laytonsmith.abstraction.enums.MCVersion;
import com.laytonsmith.core.MSLog;
import com.laytonsmith.core.MSLog.Tags;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.Target;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Vibration;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

import java.util.EnumMap;
import java.util.Map;

public class BukkitMCParticle extends MCParticle<Particle> {

	private static final Map<Particle, MCParticle> BUKKIT_MAP = new EnumMap<>(Particle.class);

	public BukkitMCParticle(MCVanillaParticle vanillaParticle, Particle particle) {
		super(vanillaParticle, particle);
	}

	@Override
	public String name() {
		return getAbstracted() == MCVanillaParticle.UNKNOWN ? getConcrete().name() : getAbstracted().name();
	}

	public static MCParticle valueOfConcrete(Particle test) {
		MCParticle type = BUKKIT_MAP.get(test);
		if(type == null) {
			MSLog.GetLogger().e(MSLog.Tags.GENERAL, "Bukkit Particle missing in BUKKIT_MAP: " + test.name(), Target.UNKNOWN);
			return new BukkitMCParticle(MCVanillaParticle.UNKNOWN, test);
		}
		return type;
	}

	public static void build() {
		for(MCVanillaParticle v : MCVanillaParticle.values()) {
			if(v.existsIn(Static.getServer().getMinecraftVersion())) {
				Particle type;
				try {
					type = getBukkitType(v);
				} catch (IllegalArgumentException | NoSuchFieldError ex) {
					MSLog.GetLogger().w(MSLog.Tags.RUNTIME, "Could not find a Bukkit Particle for " + v.name(), Target.UNKNOWN);
					continue;
				}
				BukkitMCParticle wrapper = new BukkitMCParticle(v, type);
				BUKKIT_MAP.put(type, wrapper);
				MAP.put(v.name(), wrapper);
			}
		}
		for(Particle p : Particle.values()) {
			if(!BUKKIT_MAP.containsKey(p)) {
				MSLog.GetLogger().w(Tags.GENERAL, "Could not find MCParticle for " + p.name(), Target.UNKNOWN);
				MCParticle wrapper = new BukkitMCParticle(MCVanillaParticle.UNKNOWN, p);
				MAP.put(p.name(), wrapper);
				BUKKIT_MAP.put(p, wrapper);
			}
		}
	}

	private static Particle getBukkitType(MCVanillaParticle v) {
		// remap name changes
		return Particle.valueOf(v.name());
	}

	public Object getParticleData(MCLocation l, Object data) {
		switch(getAbstracted()) {
			case BLOCK_DUST:
			case BLOCK_CRACK:
			case BLOCK_CRUMBLE:
			case BLOCK_MARKER:
			case DUST_PILLAR:
			case FALLING_DUST:
				if(data instanceof MCBlockData) {
					return ((MCBlockData) data).getHandle();
				} else if(getAbstracted() == MCVanillaParticle.BLOCK_MARKER) {
					// Barrier (and light) particles were replaced by block markers, so this is the best fallback.
					return Material.BARRIER.createBlockData();
				} else {
					return Material.STONE.createBlockData();
				}
			case ITEM_CRACK:
				ItemStack is;
				if(data instanceof MCItemStack) {
					is = (ItemStack) ((MCItemStack) data).getHandle();
				} else {
					is = new ItemStack(Material.STONE, 1);
				}
				return is;
			case SPELL_MOB:
				if(Static.getServer().getMinecraftVersion().gte(MCVersion.MC1_20_6)) {
					if(data instanceof MCColor) {
						return BukkitMCColor.GetColor((MCColor) data);
					} else {
						return Color.WHITE;
					}
				}
				break;
			case REDSTONE:
				if(data instanceof MCColor) {
					return new Particle.DustOptions(BukkitMCColor.GetColor((MCColor) data), 1.0F);
				} else {
					return new Particle.DustOptions(Color.RED, 1.0F);
				}
			case DUST_COLOR_TRANSITION:
				if(data instanceof MCParticleData.DustTransition transition) {
					return new Particle.DustTransition(BukkitMCColor.GetColor(transition.from()),
							BukkitMCColor.GetColor(transition.to()), 1.0F);
				} else {
					return new Particle.DustTransition(Color.TEAL, Color.RED, 1.0F);
				}
			case VIBRATION:
				if(data instanceof MCParticleData.VibrationBlockDestination destination) {
					return new Vibration((Location) l.getHandle(),
							new Vibration.Destination.BlockDestination((Location) destination.location().getHandle()),
							destination.arrivalTime());
				} else if(data instanceof MCParticleData.VibrationEntityDestination destination) {
					return new Vibration((Location) l.getHandle(),
							new Vibration.Destination.EntityDestination((Entity) destination.entity().getHandle()),
							destination.arrivalTime());
				} else {
					return new Vibration((Location) l.getHandle(),
							new Vibration.Destination.BlockDestination((Location) l.getHandle()), 5);
				}
			case SCULK_CHARGE:
				if(data instanceof Float) {
					return data;
				} else {
					return 0.0F;
				}
			case SHRIEK:
				if(data instanceof Integer) {
					return data;
				} else {
					return 0;
				}
			case TRAIL:
				if(data instanceof MCParticleData.TargetColor target) {
					return new Particle.TargetColor((Location) target.location().getHandle(),
							BukkitMCColor.GetColor(target.color()));
				} else {
					return new Particle.TargetColor((Location) l.getHandle(), Color.fromRGB(252, 120, 18));
				}
		}
		return null;
	}
}
