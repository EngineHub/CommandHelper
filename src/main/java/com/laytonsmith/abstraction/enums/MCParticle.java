package com.laytonsmith.abstraction.enums;

import com.laytonsmith.PureUtilities.ClassLoading.DynamicEnum;
import com.laytonsmith.annotations.MDynamicEnum;
import com.laytonsmith.core.Static;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@MDynamicEnum("com.commandhelper.Particle")
public abstract class MCParticle<Concrete> extends DynamicEnum<MCParticle.MCVanillaParticle, Concrete> {

	protected static final Map<String, MCParticle> MAP = new HashMap<>();

	@SuppressWarnings("checkstyle:staticvariablename") // Fixing this violation might break dependents.
	public static MCParticle NULL = null;

	public MCParticle(MCVanillaParticle mcVanillaParticle, Concrete concrete) {
		super(mcVanillaParticle, concrete);
	}

	public static MCParticle valueOf(String test) throws IllegalArgumentException {
		MCParticle ret = MAP.get(test);
		if(ret == null) {
			throw new IllegalArgumentException("Unknown particle type: " + test);
		}
		return ret;
	}

	/**
	 * @return Names of available particles
	 */
	public static Set<String> types() {
		if(NULL == null) { // docs mode
			Set<String> dummy = new HashSet<>();
			for(final MCVanillaParticle t : MCVanillaParticle.values()) {
				dummy.add(t.name());
			}
			return dummy;
		}
		return MAP.keySet();
	}

	/**
	 * @return Our own MCParticle list
	 */
	public static List<MCParticle> values() {
		if(NULL == null) { // docs mode
			ArrayList<MCParticle> dummy = new ArrayList<>();
			for(final MCParticle.MCVanillaParticle p : MCParticle.MCVanillaParticle.values()) {
				dummy.add(new MCParticle<Object>(p, null) {
					@Override
					public String name() {
						return p.name();
					}

					@Override
					public String concreteName() {
						return p.name();
					}
				});
			}
			return dummy;
		}
		return new ArrayList<>(MAP.values());
	}

	public enum MCVanillaParticle {
		EXPLOSION_NORMAL,
		EXPLOSION_LARGE,
		EXPLOSION_HUGE,
		FIREWORKS_SPARK,
		WATER_BUBBLE,
		WATER_SPLASH,
		WATER_WAKE,
		SUSPENDED,
		SUSPENDED_DEPTH,
		CRIT,
		CRIT_MAGIC,
		SMOKE_NORMAL,
		SMOKE_LARGE,
		SPELL,
		SPELL_INSTANT,
		SPELL_MOB,
		SPELL_MOB_AMBIENT,
		SPELL_WITCH,
		DRIP_WATER,
		DRIP_LAVA,
		VILLAGER_ANGRY,
		VILLAGER_HAPPY,
		TOWN_AURA,
		NOTE,
		PORTAL,
		ENCHANTMENT_TABLE,
		FLAME,
		LAVA,
		CLOUD,
		REDSTONE,
		SNOWBALL,
		SNOW_SHOVEL,
		SLIME,
		HEART,
		BARRIER,
		ITEM_CRACK,
		BLOCK_CRACK,
		BLOCK_DUST,
		WATER_DROP,
		MOB_APPEARANCE,
		DRAGON_BREATH,
		END_ROD,
		DAMAGE_INDICATOR,
		SWEEP_ATTACK,
		FALLING_DUST,
		TOTEM,
		SPIT,
		SQUID_INK,
		BUBBLE_POP,
		CURRENT_DOWN,
		BUBBLE_COLUMN_UP,
		NAUTILUS,
		DOLPHIN,
		UNKNOWN(MCVersion.NEVER);

		private final MCVersion since;

		MCVanillaParticle() {
			this(MCVersion.MC1_0);
		}

		MCVanillaParticle(MCVersion since) {
			this.since = since;
		}

		public boolean existsInCurrent() {
			return Static.getServer().getMinecraftVersion().gte(since);
		}
	}
}
