package com.laytonsmith.abstraction.enums.bukkit;

import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCCommandMinecart;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCEnderSignal;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCFishHook;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCHopperMinecart;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCItem;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCLightningStrike;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCLlama;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCPigZombie;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCStorageMinecart;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCTNT;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCThrownPotion;
import com.laytonsmith.abstraction.enums.MCEntityType;
import com.laytonsmith.core.MSLog;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.Target;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Projectile;

import java.util.HashMap;
import java.util.Map;

public class BukkitMCEntityType extends MCEntityType<EntityType> {

	protected static final Map<EntityType, MCEntityType> BUKKIT_MAP = new HashMap<>();

	public BukkitMCEntityType(EntityType concreteType, MCVanillaEntityType abstractedType) {
		super(abstractedType, concreteType);
	}

	// This way we don't take up extra memory on non-bukkit implementations
	public static void build() {
		NULL = new BukkitMCEntityType(EntityType.UNKNOWN, MCVanillaEntityType.UNKNOWN);
		for(MCVanillaEntityType v : MCVanillaEntityType.values()) {
			if(v.existsIn(Static.getServer().getMinecraftVersion())) {
				EntityType type;
				try {
					type = getBukkitType(v);
				} catch (IllegalArgumentException | NoSuchFieldError ex) {
					MSLog.GetLogger().w(MSLog.Tags.RUNTIME, "Could not find a Bukkit EntityType for " + v.name(), Target.UNKNOWN);
					continue;
				}
				BukkitMCEntityType wrapper = new BukkitMCEntityType(type, v);
				wrapper.setWrapperClass();
				VANILLA_MAP.put(v, wrapper);
				MAP.put(v.name(), wrapper);
				BUKKIT_MAP.put(type, wrapper);
			}
		}
		for(EntityType b : EntityType.values()) {
			if(!BUKKIT_MAP.containsKey(b)) {
				MAP.put(b.name(), new BukkitMCEntityType(b, MCVanillaEntityType.UNKNOWN));
				BUKKIT_MAP.put(b, new BukkitMCEntityType(b, MCVanillaEntityType.UNKNOWN));
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
		if(getAbstracted() == MCVanillaEntityType.UNKNOWN) {
			return getConcrete() != EntityType.UNKNOWN && getConcrete().isSpawnable();
		} else {
			return getAbstracted().isSpawnable() || getConcrete().isSpawnable();
		}
	}

	@Override
	public boolean isProjectile() {
		return getConcrete().getEntityClass() != null
				&& Projectile.class.isAssignableFrom(getConcrete().getEntityClass());
	}

	public static BukkitMCEntityType valueOfConcrete(EntityType test) {
		MCEntityType type = BUKKIT_MAP.get(test);
		if(type != null) {
			return (BukkitMCEntityType) type;
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
	private static EntityType getBukkitType(MCVanillaEntityType v) {
		switch(v) {
			case ENDER_EYE:
				return EntityType.ENDER_SIGNAL;
		}
		return EntityType.valueOf(v.name());
	}

	// This is here because it shouldn't be getting changed from API
	public void setWrapperClass(Class<? extends MCEntity> clazz) {
		wrapperClass = clazz;
	}

	// run once on setup
	private void setWrapperClass() {
		switch(getAbstracted()) {
			case DROPPED_ITEM:
				wrapperClass = BukkitMCItem.class;
				break;
			case ENDER_EYE:
				wrapperClass = BukkitMCEnderSignal.class;
				break;
			case FISHING_HOOK:
				wrapperClass = BukkitMCFishHook.class;
				break;
			case LIGHTNING:
				wrapperClass = BukkitMCLightningStrike.class;
				break;
			case LINGERING_POTION:
			case SPLASH_POTION:
				wrapperClass = BukkitMCThrownPotion.class;
				break;
			case MINECART_CHEST:
				wrapperClass = BukkitMCStorageMinecart.class;
				break;
			case MINECART_COMMAND:
				wrapperClass = BukkitMCCommandMinecart.class;
				break;
			case MINECART_HOPPER:
				wrapperClass = BukkitMCHopperMinecart.class;
				break;
			case PRIMED_TNT:
				wrapperClass = BukkitMCTNT.class;
				break;
			case TRADER_LLAMA:
				wrapperClass = BukkitMCLlama.class;
				break;
			case ZOMBIFIED_PIGLIN:
				wrapperClass = BukkitMCPigZombie.class;
				break;
			case UNKNOWN:
				wrapperClass = null;
				break;
			default:
				String[] split = abstracted.name().toLowerCase().split("_");
				if(split.length == 0 || "".equals(split[0])) {
					break;
				}
				String name = "com.laytonsmith.abstraction.bukkit.entities.BukkitMC";
				for(String s : split) {
					name = name.concat(Character.toUpperCase(s.charAt(0)) + s.substring(1));
				}
				try {
					wrapperClass = (Class<? extends MCEntity>) Class.forName(name);
				} catch (ClassNotFoundException e) {
					String url = "https://github.com/sk89q/CommandHelper/tree/master/src/main/java/"
							+ "com/laytonsmith/abstraction/bukkit/entities";
					MSLog.GetLogger().d(MSLog.Tags.RUNTIME, "While trying to find the correct entity class for "
							+ getAbstracted().name() + "(attempted " + name + "), we could not find a wrapper class."
							+ " This is not necessarily an error, we just don't have any special handling for"
							+ " this entity yet, and will treat it generically. If there is a matching file at"
							+ url + ", please alert the developers of this notice.", Target.UNKNOWN);
				}
		}
	}
}
