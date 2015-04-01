
package com.laytonsmith.abstraction.enums.bukkit;

import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.bukkit.blocks.BukkitMCFallingBlock;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCCommandMinecart;
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
public class BukkitMCEntityType extends MCEntityType {

	private EntityType concrete;

	public BukkitMCEntityType(EntityType concreteType, MCVanillaEntityType abstractedType) {
		super(abstractedType);
		concrete = concreteType;
	}

	// This way we don't take up extra memory on non-bukkit implementations
	public static void build() {
		vanilla = new HashMap<>();
		mappings = new HashMap<>();
		classList = new HashMap<>();
		NULL = new BukkitMCEntityType(EntityType.UNKNOWN, MCVanillaEntityType.UNKNOWN);
		ArrayList<EntityType> counted = new ArrayList<>();
		for (MCVanillaEntityType v : MCVanillaEntityType.values()) {
			if (v.existsInCurrent()) {
				EntityType type = EntityType.valueOf(getBukkitName(v));
				BukkitMCEntityType wrapper = new BukkitMCEntityType(type, v);
				vanilla.put(v, wrapper);
				mappings.put(v.name(), wrapper);
				counted.add(type);

				switch (v) {
					case UNKNOWN:
						classList.put(v, null);
						break;
					case DROPPED_ITEM:
						classList.put(v, BukkitMCItem.class);
						break;
					case PRIMED_TNT:
						classList.put(v, BukkitMCTNT.class);
						break;
					case LIGHTNING:
						classList.put(v, BukkitMCLightningStrike.class);
						break;
					case FALLING_BLOCK:
						classList.put(v, BukkitMCFallingBlock.class);
						break;
					case SPLASH_POTION:
						classList.put(v, BukkitMCThrownPotion.class);
						break;
					default:
						String[] split = v.name().toLowerCase().split("_");
						if (split.length == 0 || "".equals(split[0])) {
							break;
						}
						String name = "com.laytonsmith.abstraction.bukkit.entities.BukkitMC";
						if ("minecart".equals(split[0])) {
							if (split.length == 1 || !"command".equals(split[1])) {
								classList.put(v, BukkitMCMinecart.class);
								break;
							} else {
								classList.put(v, BukkitMCCommandMinecart.class);
								break;
							}
						}
						if (split[0].startsWith("fish")) { // Bukkit enum matches neither the old class or the new
							classList.put(v, BukkitMCFishHook.class);
							break;
						}
						for (String s : split) {
							name = name.concat(Character.toUpperCase(s.charAt(0)) + s.substring(1));
						}
						try {
							classList.put(v, (Class<? extends MCEntity>) Class.forName(name));
						} catch (ClassNotFoundException e) {
							String url = "https://github.com/sk89q/CommandHelper/tree/master/src/main/java/"
										 + "com/laytonsmith/abstraction/bukkit/entities";
							CHLog.GetLogger().d(CHLog.Tags.RUNTIME, "While trying to find the correct entity class for "
									+ v.name() + "(attempted " + name + "), we were unable to find a wrapper class."
									+ " This is not necessarily an error, we just don't have any special handling for"
									+ " this entity yet, and will treat it generically. If there is no matching file at"
									+ url + ", please alert the developers of this notice.", Target.UNKNOWN);
						}
				}
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
		return abstracted == MCVanillaEntityType.UNKNOWN ? concrete.name() : abstracted.name();
	}

	@Override
	public String concreteName() {
		return concrete.name();
	}

	@Override
	public EntityType getConcrete() {
		return concrete;
	}

	@Override
	public boolean isSpawnable() {
		return (abstracted == MCVanillaEntityType.UNKNOWN) ? (concrete
															  != EntityType.UNKNOWN) : abstracted.isSpawnable();
	}

	public static MCEntityType valueOfConcrete(EntityType test) {
		for (MCEntityType t : mappings.values()) {
			if (((BukkitMCEntityType) t).getConcrete().equals(test)) {
				return t;
			}
		}
		return NULL;
	}

	public static MCEntityType valueOfConcrete(String test) {
		EntityType type;
		try {
			type = EntityType.valueOf(test);
		} catch (IllegalArgumentException iae) {
			return NULL;
		}
		return valueOfConcrete(type);
	}

	// Add exceptions here
	public static String getBukkitName(MCVanillaEntityType v) {
		return v.name();
	}

	public static Class<? extends MCEntity> getWrapperClass(EntityType type) {
		return classList.get(valueOfConcrete(type).getAbstracted());
	}
}
