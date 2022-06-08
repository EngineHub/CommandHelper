package com.laytonsmith.abstraction.enums.bukkit;

import com.laytonsmith.abstraction.MCColor;
import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.blocks.MCBlockData;
import com.laytonsmith.abstraction.bukkit.BukkitMCColor;
import com.laytonsmith.abstraction.bukkit.BukkitMCVibration;
import com.laytonsmith.abstraction.enums.MCParticle;
import com.laytonsmith.core.MSLog;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.CDouble;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.constructs.Target;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.data.BlockData;
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

	// This way we don't take up extra memory on non-bukkit implementations
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
		for(Particle b : Particle.values()) {
			if(!BUKKIT_MAP.containsKey(b)) {
				MAP.put(b.name(), new BukkitMCParticle(MCVanillaParticle.UNKNOWN, b));
				BUKKIT_MAP.put(b, new BukkitMCParticle(MCVanillaParticle.UNKNOWN, b));
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
			case BLOCK_MARKER:
			case FALLING_DUST:
				BlockData bd;
				if(data instanceof MCBlockData) {
					bd = (BlockData) ((MCBlockData) data).getHandle();
				} else if(getAbstracted() == MCVanillaParticle.BLOCK_MARKER) {
					// Barrier (and light) particles were replaced by block markers, so this is the best fallback.
					bd = Material.BARRIER.createBlockData();
				} else {
					bd = Material.STONE.createBlockData();
				}
				return bd;
			case ITEM_CRACK:
				ItemStack is;
				if(data instanceof MCItemStack) {
					is = (ItemStack) ((MCItemStack) data).getHandle();
				} else {
					is = new ItemStack(Material.STONE, 1);
				}
				return is;
			case REDSTONE:
				Particle.DustOptions color;
				if(data instanceof MCColor) {
					color = new Particle.DustOptions(BukkitMCColor.GetColor((MCColor) data), 1.0F);
				} else {
					color =  new Particle.DustOptions(Color.RED, 1.0F);
				}
				return color;
			case DUST_COLOR_TRANSITION:
				Particle.DustTransition dust;
				if(data instanceof MCColor[]) {
					MCColor[] c = (MCColor[]) data;
					dust = new Particle.DustTransition(BukkitMCColor.GetColor(c[0]), BukkitMCColor.GetColor(c[1]), 1.0F);
				} else {
					dust = new Particle.DustTransition(Color.TEAL, Color.RED, 1.0F);
				}
				return dust;
			case VIBRATION:
				BukkitMCVibration vibe;
				if(data instanceof MCLocation) {
					vibe = new BukkitMCVibration(l, (MCLocation) data, 5);
				} else if(data instanceof MCEntity) {
					vibe = new BukkitMCVibration(l, (MCEntity) data, 5);
				} else {
					vibe = new BukkitMCVibration(l, l, 5);
				}
				return vibe.getHandle();
			case SCULK_CHARGE:
				Float f;
				if(data instanceof CDouble) {
					f = (float) ((CDouble) data).getDouble();
				} else {
					f = 1.0F;
				}
				return f;
			case SHRIEK:
				Integer i;
				if(data instanceof CInt) {
					i = (int) ((CInt) data).getInt();
				} else {
					i = 0;
				}
				return i;
		}
		return null;
	}
}
