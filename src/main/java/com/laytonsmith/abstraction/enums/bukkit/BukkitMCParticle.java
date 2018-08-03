package com.laytonsmith.abstraction.enums.bukkit;

import com.laytonsmith.abstraction.enums.MCParticle;
import com.laytonsmith.core.CHLog;
import com.laytonsmith.core.constructs.Target;
import org.bukkit.Particle;

import java.util.HashMap;
import java.util.Map;

public class BukkitMCParticle extends MCParticle<Particle> {

	private static final Map<Particle, MCParticle> BUKKIT_MAP = new HashMap<>();

	public BukkitMCParticle(MCVanillaParticle vanillaParticle, Particle particle) {
		super(vanillaParticle, particle);
	}

	@Override
	public String name() {
		return getAbstracted() == MCVanillaParticle.UNKNOWN ? concreteName() : getAbstracted().name();
	}

	@Override
	public String concreteName() {
		Particle b = getConcrete();
		if(b == null) {
			return "null";
		}
		return b.name();
	}

	public static MCParticle valueOfConcrete(Particle test) {
		MCParticle type = BUKKIT_MAP.get(test);
		if(type == null) {
			return NULL;
		}
		return type;
	}

	// This way we don't take up extra memory on non-bukkit implementations
	public static void build() {
		NULL = new BukkitMCParticle(MCVanillaParticle.UNKNOWN, null);
		for(MCVanillaParticle v : MCVanillaParticle.values()) {
			if(v.existsInCurrent()) {
				Particle type;
				try {
					type = getBukkitType(v);
				} catch (IllegalArgumentException | NoSuchFieldError ex) {
					CHLog.GetLogger().w(CHLog.Tags.RUNTIME, "Could not find a Bukkit Particle for " + v.name(), Target.UNKNOWN);
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
}
