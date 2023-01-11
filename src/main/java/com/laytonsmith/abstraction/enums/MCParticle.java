package com.laytonsmith.abstraction.enums;

import com.laytonsmith.PureUtilities.ClassLoading.DynamicEnum;
import com.laytonsmith.annotations.MDynamicEnum;
import com.laytonsmith.core.MSLog;
import com.laytonsmith.core.constructs.Target;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@MDynamicEnum("com.commandhelper.Particle")
public abstract class MCParticle<Concrete> extends DynamicEnum<MCParticle.MCVanillaParticle, Concrete> {

	protected static final Map<String, MCParticle> MAP = new HashMap<>();

	public MCParticle(MCVanillaParticle mcVanillaParticle, Concrete concrete) {
		super(mcVanillaParticle, concrete);
	}

	public static MCParticle valueOf(String test) throws IllegalArgumentException {
		MCParticle ret = MAP.get(test);
		if(ret == null) {
			switch(test) {
				case "BARRIER":
					MSLog.GetLogger().e(MSLog.Tags.GENERAL,
							"BARRIER particle type was changed in 1.18. Converted to BLOCK_MARKER.", Target.UNKNOWN);
					return MAP.get("BLOCK_MARKER");
				case "LIGHT":
					MSLog.GetLogger().e(MSLog.Tags.GENERAL,
							"LIGHT particle type was changed in 1.18. Converted to BLOCK_MARKER.", Target.UNKNOWN);
					return MAP.get("BLOCK_MARKER");
			}
			throw new IllegalArgumentException("Unknown particle type: " + test);
		}
		return ret;
	}

	/**
	 * @return Names of available particles
	 */
	public static Set<String> types() {
		if(MAP.isEmpty()) { // docs mode
			Set<String> dummy = new HashSet<>();
			for(final MCVanillaParticle t : MCVanillaParticle.values()) {
				if(t.existsIn(MCVersion.CURRENT)) {
					dummy.add(t.name());
				}
			}
			return dummy;
		}
		return MAP.keySet();
	}

	/**
	 * @return Our own MCParticle list
	 */
	public static List<MCParticle> values() {
		if(MAP.isEmpty()) { // docs mode
			ArrayList<MCParticle> dummy = new ArrayList<>();
			for(final MCParticle.MCVanillaParticle p : MCParticle.MCVanillaParticle.values()) {
				if(!p.existsIn(MCVersion.CURRENT)) {
					continue;
				}
				dummy.add(new MCParticle<>(p, null) {
					@Override
					public String name() {
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
		BARRIER(MCVersion.MC1_8, MCVersion.MC1_17_X),
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
		SNEEZE(MCVersion.MC1_14),
		CAMPFIRE_COSY_SMOKE(MCVersion.MC1_14),
		CAMPFIRE_SIGNAL_SMOKE(MCVersion.MC1_14),
		COMPOSTER(MCVersion.MC1_14),
		FLASH(MCVersion.MC1_14),
		FALLING_LAVA(MCVersion.MC1_14),
		LANDING_LAVA(MCVersion.MC1_14),
		FALLING_WATER(MCVersion.MC1_14),
		DRIPPING_HONEY(MCVersion.MC1_15),
		FALLING_HONEY(MCVersion.MC1_15),
		FALLING_NECTAR(MCVersion.MC1_15),
		LANDING_HONEY(MCVersion.MC1_15),
		SOUL_FIRE_FLAME(MCVersion.MC1_16),
		ASH(MCVersion.MC1_16),
		CRIMSON_SPORE(MCVersion.MC1_16),
		WARPED_SPORE(MCVersion.MC1_16),
		SOUL(MCVersion.MC1_16),
		DRIPPING_OBSIDIAN_TEAR(MCVersion.MC1_16),
		FALLING_OBSIDIAN_TEAR(MCVersion.MC1_16),
		LANDING_OBSIDIAN_TEAR(MCVersion.MC1_16),
		REVERSE_PORTAL(MCVersion.MC1_16),
		WHITE_ASH(MCVersion.MC1_16),
		LIGHT(MCVersion.MC1_17, MCVersion.MC1_17_X),
		DUST_COLOR_TRANSITION(MCVersion.MC1_17),
		VIBRATION(MCVersion.MC1_17),
		FALLING_SPORE_BLOSSOM(MCVersion.MC1_17),
		SPORE_BLOSSOM_AIR(MCVersion.MC1_17),
		SMALL_FLAME(MCVersion.MC1_17),
		SNOWFLAKE(MCVersion.MC1_17),
		DRIPPING_DRIPSTONE_LAVA(MCVersion.MC1_17),
		FALLING_DRIPSTONE_LAVA(MCVersion.MC1_17),
		DRIPPING_DRIPSTONE_WATER(MCVersion.MC1_17),
		FALLING_DRIPSTONE_WATER(MCVersion.MC1_17),
		GLOW_SQUID_INK(MCVersion.MC1_17),
		GLOW(MCVersion.MC1_17),
		WAX_ON(MCVersion.MC1_17),
		WAX_OFF(MCVersion.MC1_17),
		ELECTRIC_SPARK(MCVersion.MC1_17),
		SCRAPE(MCVersion.MC1_17),
		BLOCK_MARKER(MCVersion.MC1_18),
		SHRIEK(MCVersion.MC1_19),
		SCULK_CHARGE(MCVersion.MC1_19),
		SCULK_CHARGE_POP(MCVersion.MC1_19),
		SCULK_SOUL(MCVersion.MC1_19),
		SONIC_BOOM(MCVersion.MC1_19),
		UNKNOWN(MCVersion.NEVER);

		private final MCVersion since;
		private final MCVersion until;

		MCVanillaParticle() {
			this(MCVersion.MC1_0);
		}

		MCVanillaParticle(MCVersion since) {
			this(since, MCVersion.CURRENT);
		}

		MCVanillaParticle(MCVersion since, MCVersion until) {
			this.since = since;
			this.until = until;
		}

		public boolean existsIn(MCVersion version) {
			return version.gte(since) && version.lte(until);
		}
	}
}
