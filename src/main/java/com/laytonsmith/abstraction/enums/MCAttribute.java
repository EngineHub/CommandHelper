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
import java.util.TreeSet;

@MDynamicEnum("com.commandhelper.Attribute")
public abstract class MCAttribute<Concrete> extends DynamicEnum<MCAttribute.MCVanillaAttribute, Concrete> {

	protected static final Map<String, MCAttribute> MAP = new HashMap<>();

	public MCAttribute(MCVanillaAttribute mcVanillaAttribute, Concrete concrete) {
		super(mcVanillaAttribute, concrete);
	}

	public static MCAttribute valueOf(String test) throws IllegalArgumentException {
		MCAttribute ret = MAP.get(test);
		if(ret == null) {
			if(test.equals("HORSE_JUMP_STRENGTH")) {
				MSLog.GetLogger().e(MSLog.Tags.GENERAL,
						"HORSE_JUMP_STRENGTH attribute changed to GENERIC_JUMP_STRENGTH.", Target.UNKNOWN);
				return MAP.get("GENERIC_JUMP_STRENGTH");
			}
			throw new IllegalArgumentException("Unknown attribute: " + test);
		}
		return ret;
	}

	public static Set<String> types() {
		if(MAP.isEmpty()) { // docs mode
			Set<String> dummy = new HashSet<>();
			for(final MCVanillaAttribute s : MCVanillaAttribute.values()) {
				if(s.existsIn(MCVersion.CURRENT)) {
					dummy.add(s.name());
				}
			}
			return dummy;
		}
		return new TreeSet<>(MAP.keySet());
	}

	public static List<MCAttribute> values() {
		if(MAP.isEmpty()) { // docs mode
			ArrayList<MCAttribute> dummy = new ArrayList<>();
			for(final MCVanillaAttribute s : MCVanillaAttribute.values()) {
				if(!s.existsIn(MCVersion.CURRENT)) {
					continue;
				}
				dummy.add(new MCAttribute<>(s, null) {
					@Override
					public String name() {
						return s.name();
					}
				});
			}
			return dummy;
		}
		return new ArrayList<>(MAP.values());
	}

	public enum MCVanillaAttribute {
		GENERIC_ARMOR,
		GENERIC_ARMOR_TOUGHNESS,
		GENERIC_ATTACK_DAMAGE,
		GENERIC_ATTACK_KNOCKBACK,
		GENERIC_ATTACK_SPEED,
		GENERIC_FLYING_SPEED,
		GENERIC_FOLLOW_RANGE,
		GENERIC_KNOCKBACK_RESISTANCE,
		GENERIC_LUCK,
		GENERIC_MAX_HEALTH,
		GENERIC_MOVEMENT_SPEED,
		HORSE_JUMP_STRENGTH(MCVersion.MC1_6, MCVersion.MC1_20_4), // changed to GENERIC_JUMP_STRENGTH
		ZOMBIE_SPAWN_REINFORCEMENTS,
		GENERIC_MAX_ABSORPTION(MCVersion.MC1_20_2),
		GENERIC_FALL_DAMAGE_MULTIPLIER(MCVersion.MC1_20_6),
		GENERIC_GRAVITY(MCVersion.MC1_20_6),
		GENERIC_JUMP_STRENGTH(MCVersion.MC1_20_6),
		GENERIC_SAFE_FALL_DISTANCE(MCVersion.MC1_20_6),
		GENERIC_SCALE(MCVersion.MC1_20_6),
		GENERIC_STEP_HEIGHT(MCVersion.MC1_20_6),
		PLAYER_BLOCK_BREAK_SPEED(MCVersion.MC1_20_6),
		PLAYER_BLOCK_INTERACTION_RANGE(MCVersion.MC1_20_6),
		PLAYER_ENTITY_INTERACTION_RANGE(MCVersion.MC1_20_6),
		GENERIC_BURNING_TIME(MCVersion.MC1_21),
		GENERIC_EXPLOSION_KNOCKBACK_RESISTANCE(MCVersion.MC1_21),
		GENERIC_MOVEMENT_EFFICIENCY(MCVersion.MC1_21),
		GENERIC_OXYGEN_BONUS(MCVersion.MC1_21),
		GENERIC_WATER_MOVEMENT_EFFICIENCY(MCVersion.MC1_21),
		PLAYER_MINING_EFFICIENCY(MCVersion.MC1_21),
		PLAYER_SNEAKING_SPEED(MCVersion.MC1_21),
		PLAYER_SUBMERGED_MINING_SPEED(MCVersion.MC1_21),
		PLAYER_SWEEPING_DAMAGE_RATIO(MCVersion.MC1_21),
		TEMPT_RANGE(MCVersion.MC1_21_3),

		UNKNOWN(MCVersion.NEVER);

		private final MCVersion since;
		private final MCVersion until;

		MCVanillaAttribute() {
			this(MCVersion.MC1_6);
		}

		MCVanillaAttribute(MCVersion since) {
			this.since = since;
			this.until = MCVersion.FUTURE;
		}

		MCVanillaAttribute(MCVersion since, MCVersion until) {
			this.since = since;
			this.until = until;
		}

		public boolean existsIn(MCVersion version) {
			return version.gte(since) && version.lte(until);
		}
	}
}
