package com.laytonsmith.abstraction.enums.bukkit;

import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCCommandMinecart;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCEntity;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCFishHook;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCItem;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCLightningStrike;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCMinecart;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCTNT;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCThrownPotion;
import com.laytonsmith.abstraction.enums.MCEntityType;
import com.laytonsmith.core.CHLog;
import com.laytonsmith.core.constructs.Target;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 *
 */
public class BukkitMCEntityType extends MCEntityType<EntityType> {

	public BukkitMCEntityType(EntityType concreteType, MCVanillaEntityType abstractedType) {
		super(abstractedType, concreteType);
	}

	// This way we don't take up extra memory on non-bukkit implementations
	public static void build() {
		vanilla = new HashMap<>();
		mappings = new HashMap<>();
		NULL = new BukkitMCEntityType(EntityType.UNKNOWN, MCVanillaEntityType.UNKNOWN);
		ArrayList<EntityType> counted = new ArrayList<>();
		for (MCVanillaEntityType v : MCVanillaEntityType.values()) {
			if (v.existsInCurrent()) {
				EntityType type = getBukkitType(v);
				if (type == null) {
					CHLog.GetLogger().e(CHLog.Tags.RUNTIME, "Could not find a matching entity type for " + v.name()
							+ ". This is an error, please report this to the bug tracker.", Target.UNKNOWN);
					continue;
				}
				BukkitMCEntityType wrapper = new BukkitMCEntityType(type, v);
				wrapper.setWrapperClass();
				vanilla.put(v, wrapper);
				mappings.put(v.name(), wrapper);
				counted.add(type);
			}
		}
		for (EntityType b : EntityType.values()) {
			if (!counted.contains(b)) {
				mappings.put(b.name(), new BukkitMCEntityType(b, MCVanillaEntityType.UNKNOWN));
			}
		}
	}

	@Override
	public String name() {
		return getAbstracted() == MCVanillaEntityType.UNKNOWN ? getConcrete().name() : getAbstracted().name();
	}

	@Override
	public String concreteName() {
		return getConcrete().name();
	}

	@Override
	public boolean isSpawnable() {
		return (getAbstracted() == MCVanillaEntityType.UNKNOWN) ? (getConcrete()
				!= EntityType.UNKNOWN) : getAbstracted().isSpawnable();
	}

	public static BukkitMCEntityType valueOfConcrete(EntityType test) {
		for (MCEntityType t : mappings.values()) {
			if (((BukkitMCEntityType) t).getConcrete().equals(test)) {
				return (BukkitMCEntityType) t;
			}
		}
		return (BukkitMCEntityType) NULL;
	}

	public static BukkitMCEntityType valueOfConcrete(String test) {
		try {
			return valueOfConcrete(EntityType.valueOf(test));
		} catch (IllegalArgumentException iae) {
			return (BukkitMCEntityType) NULL;
		}
	}

	// Add exceptions here
	public static EntityType getBukkitType(MCVanillaEntityType v) {
		switch (v) {
			case ENDER_EYE:
				return EntityType.ENDER_SIGNAL;
		}
		try {
			return EntityType.valueOf(v.name());
		} catch (IllegalArgumentException iae) {
			return null;
		}
	}

	// This is here because it shouldn't be getting changed from API
	public void setWrapperClass(Class<? extends MCEntity> clazz) {
		wrapperClass = clazz;
	}

	// run once on setup
	private void setWrapperClass() {
		switch (getAbstracted()) {
			case UNKNOWN:
				wrapperClass = BukkitMCEntity.class;
				break;
			case DROPPED_ITEM:
				wrapperClass = BukkitMCItem.class;
				break;
			case PRIMED_TNT:
				wrapperClass = BukkitMCTNT.class;
				break;
			case LIGHTNING:
				wrapperClass = BukkitMCLightningStrike.class;
				break;
			case SPLASH_POTION:
				wrapperClass = BukkitMCThrownPotion.class;
				break;
			default:
				String[] split = abstracted.name().toLowerCase().split("_");
				if (split.length == 0 || "".equals(split[0])) {
					break;
				}
				String name = "com.laytonsmith.abstraction.bukkit.entities.BukkitMC";
				if ("minecart".equals(split[0])) {
					if (split.length == 1 || !"command".equals(split[1])) {
						wrapperClass = BukkitMCMinecart.class;
						break;
					} else {
						wrapperClass = BukkitMCCommandMinecart.class;
						break;
					}
				}
				if (split[0].startsWith("fish")) { // Bukkit enum matches neither the old class or the new
					wrapperClass = BukkitMCFishHook.class;
					break;
				}
				for (String s : split) {
					name = name.concat(Character.toUpperCase(s.charAt(0)) + s.substring(1));
				}
				try {
					wrapperClass = (Class<? extends MCEntity>) Class.forName(name);
				} catch (ClassNotFoundException e) {
					String url = "https://github.com/sk89q/CommandHelper/tree/master/src/main/java/"
							+ "com/laytonsmith/abstraction/bukkit/entities";
					CHLog.GetLogger().d(CHLog.Tags.RUNTIME, "While trying to find the correct entity class for "
							+ getAbstracted().name() + "(attempted " + name + "), we could not find a wrapper class."
							+ " This is not necessarily an error, we just don't have any special handling for"
							+ " this entity yet, and will treat it generically. If there is a matching file at"
							+ url + ", please alert the developers of this notice.", Target.UNKNOWN);
				}
		}
	}
}
