package com.laytonsmith.abstraction.enums;

import com.laytonsmith.PureUtilities.ClassLoading.DynamicEnum;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.annotations.MDynamicEnum;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

@MDynamicEnum("com.commandhelper.Enchantment")
public abstract class MCEnchantment<Concrete> extends DynamicEnum<MCEnchantment.MCVanillaEnchantment, Concrete> {

	protected static final Map<String, MCEnchantment> MAP = new HashMap<>();

	public MCEnchantment(MCVanillaEnchantment mcVanillaType, Concrete concrete) {
		super(mcVanillaType, concrete);
	}

	public static MCEnchantment valueOf(String test) throws IllegalArgumentException {
		MCEnchantment ret = MAP.get(test);
		if(ret == null) {
			MCVanillaEnchantment oldType = MCVanillaEnchantment.valueOf(test);
			if(oldType.rename != null) {
				return MAP.get(oldType.rename);
			}
			throw new IllegalArgumentException("Unknown enchantment type: " + test);
		}
		return ret;
	}

	public abstract boolean canEnchantItem(MCItemStack is);

	public abstract int getMaxLevel();

	public static Set<String> types() {
		if(MAP.isEmpty()) { // docs mode
			Set<String> dummy = new HashSet<>();
			for(final MCVanillaEnchantment s : MCVanillaEnchantment.values()) {
				if(s.existsIn(MCVersion.CURRENT)) {
					dummy.add(s.name());
				}
			}
			return dummy;
		}
		return new TreeSet<>(MAP.keySet());
	}

	public static List<MCEnchantment> values() {
		if(MAP.isEmpty()) { // docs mode
			ArrayList<MCEnchantment> dummy = new ArrayList<>();
			for(final MCVanillaEnchantment s : MCVanillaEnchantment.values()) {
				if(!s.existsIn(MCVersion.CURRENT)) {
					continue;
				}
				dummy.add(new MCEnchantment<>(s, null) {
					@Override
					public boolean canEnchantItem(MCItemStack is) {
						return false;
					}

					@Override
					public int getMaxLevel() {
						return 1;
					}

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

	public enum MCVanillaEnchantment {
		DAMAGE_ALL(MCVersion.MC1_0, MCVersion.MC1_12_X, "SHARPNESS"),
		DAMAGE_ARTHROPODS(MCVersion.MC1_0, MCVersion.MC1_12_X, "BANE_OF_ARTHROPODS"),
		DAMAGE_UNDEAD(MCVersion.MC1_0, MCVersion.MC1_12_X, "SMITE"),
		DIG_SPEED(MCVersion.MC1_0, MCVersion.MC1_12_X, "EFFICIENCY"),
		DURABILITY(MCVersion.MC1_0, MCVersion.MC1_12_X, "UNBREAKING"),
		LOOT_BONUS_BLOCKS(MCVersion.MC1_0, MCVersion.MC1_12_X, "FORTUNE"),
		LOOT_BONUS_MOBS(MCVersion.MC1_0, MCVersion.MC1_12_X, "LOOTING"),
		LUCK(MCVersion.MC1_0, MCVersion.MC1_12_X, "LUCK_OF_THE_SEA"),
		OXYGEN(MCVersion.MC1_0, MCVersion.MC1_12_X, "RESPIRATION"),
		PROTECTION_ENVIRONMENTAL(MCVersion.MC1_0, MCVersion.MC1_12_X, "PROTECTION"),
		PROTECTION_FIRE(MCVersion.MC1_0, MCVersion.MC1_12_X, "FIRE_PROTECTION"),
		PROTECTION_FALL(MCVersion.MC1_0, MCVersion.MC1_12_X, "FALL_PROTECTION"),
		PROTECTION_EXPLOSIONS(MCVersion.MC1_0, MCVersion.MC1_12_X, "BLAST_PROTECTION"),
		PROTECTION_PROJECTILE(MCVersion.MC1_0, MCVersion.MC1_12_X, "PROJECTILE_PROTECTION"),
		WATER_WORKER(MCVersion.MC1_0, MCVersion.MC1_12_X, "AQUA_AFFINITY"),
		ARROW_DAMAGE(MCVersion.MC1_1, MCVersion.MC1_12_X, "POWER"),
		ARROW_KNOCKBACK(MCVersion.MC1_1, MCVersion.MC1_12_X, "PUNCH"),
		ARROW_FIRE(MCVersion.MC1_1, MCVersion.MC1_12_X, "FLAME"),
		ARROW_INFINITE(MCVersion.MC1_1, MCVersion.MC1_12_X, "INFINITY"),
		SWEEPING_EDGE(MCVersion.MC1_11_X, MCVersion.MC1_12_X, "SWEEPING"),
		AQUA_AFFINITY,
		BANE_OF_ARTHROPODS,
		BINDING_CURSE,
		BLAST_PROTECTION,
		CHANNELING,
		DEPTH_STRIDER,
		EFFICIENCY,
		FEATHER_FALLING,
		FIRE_ASPECT,
		FIRE_PROTECTION,
		FLAME,
		FORTUNE,
		FROST_WALKER,
		IMPALING,
		INFINITY,
		KNOCKBACK,
		LOOTING,
		LOYALTY,
		LUCK_OF_THE_SEA,
		LURE,
		MENDING,
		POWER,
		PROJECTILE_PROTECTION,
		PROTECTION,
		PUNCH,
		RESPIRATION,
		RIPTIDE,
		SHARPNESS,
		SILK_TOUCH,
		SMITE,
		THORNS,
		UNBREAKING,
		VANISHING_CURSE,
		SWEEPING,
		MULTISHOT(MCVersion.MC1_14),
		PIERCING(MCVersion.MC1_14),
		QUICK_CHARGE(MCVersion.MC1_14),
		SOUL_SPEED(MCVersion.MC1_16_1),
		SWIFT_SNEAK(MCVersion.MC1_19),
		BREACH(MCVersion.MC1_20_6),
		DENSITY(MCVersion.MC1_20_6),
		WIND_BURST(MCVersion.MC1_20_6),
		UNKNOWN(MCVersion.NEVER);

		private final MCVersion since;
		private final MCVersion until;
		private final String rename;

		MCVanillaEnchantment() {
			this(MCVersion.MC1_13);
		}

		MCVanillaEnchantment(MCVersion since) {
			this(since, MCVersion.FUTURE);
		}

		MCVanillaEnchantment(MCVersion since, MCVersion until) {
			this(since, until, null);
		}

		MCVanillaEnchantment(MCVersion since, MCVersion until, String rename) {
			this.since = since;
			this.until = until;
			this.rename = rename;
		}

		public boolean existsIn(MCVersion version) {
			return version.gte(since) && version.lte(until);
		}
	}
}
