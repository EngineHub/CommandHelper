package com.laytonsmith.abstraction.enums.bukkit;

import com.laytonsmith.PureUtilities.Common.ReflectionUtils;
import com.laytonsmith.abstraction.enums.MCProfession;
import com.laytonsmith.core.MSLog;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.Target;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.entity.Villager;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class BukkitMCProfession extends MCProfession<Villager.Profession> {

	private static final Map<Villager.Profession, MCProfession> BUKKIT_MAP = new HashMap<>();

	public BukkitMCProfession(MCVanillaProfession vanillaProfession, Villager.Profession profession) {
		super(vanillaProfession, profession);
	}

	@Override
	public String name() {
		if(getAbstracted() == MCVanillaProfession.UNKNOWN) {
			// changed from enum to interface in 1.21
			NamespacedKey key = ReflectionUtils.invokeMethod(Keyed.class, getConcrete(), "getKey");
			return key.getKey().toUpperCase(Locale.ROOT);
		}
		return getAbstracted().name();
	}

	public static MCProfession valueOfConcrete(Villager.Profession test) {
		MCProfession profession = BUKKIT_MAP.get(test);
		if(profession == null) {
			NamespacedKey key = ReflectionUtils.invokeMethod(Keyed.class, test, "getKey");
			MSLog.GetLogger().e(MSLog.Tags.GENERAL, "Bukkit Villager Profession missing in BUKKIT_MAP: "
					+ key.getKey().toUpperCase(Locale.ROOT), Target.UNKNOWN);
			return new BukkitMCProfession(MCVanillaProfession.UNKNOWN, test);
		}
		return profession;
	}

	public static void build() {
		for(MCVanillaProfession v : MCVanillaProfession.values()) {
			if(v.existsIn(Static.getServer().getMinecraftVersion())) {
				Villager.Profession profession;
				try {
					profession = getBukkitType(v);
				} catch (IllegalArgumentException | NoSuchFieldError ex) {
					MSLog.GetLogger().w(MSLog.Tags.RUNTIME, "Could not find a Bukkit villager profession type for "
							+ v.name(), Target.UNKNOWN);
					continue;
				}
				BukkitMCProfession wrapper = new BukkitMCProfession(v, profession);
				MAP.put(v.name(), wrapper);
				BUKKIT_MAP.put(profession, wrapper);
			}
		}
		for(Villager.Profession pr : Registry.VILLAGER_PROFESSION) {
			if(pr != null && !BUKKIT_MAP.containsKey(pr)) {
				NamespacedKey key = ReflectionUtils.invokeMethod(Keyed.class, pr, "getKey");
				MAP.put(key.getKey().toUpperCase(Locale.ROOT), new BukkitMCProfession(MCVanillaProfession.UNKNOWN, pr));
				BUKKIT_MAP.put(pr, new BukkitMCProfession(MCVanillaProfession.UNKNOWN, pr));
			}
		}
	}

	private static Villager.Profession getBukkitType(MCVanillaProfession v) {
		return Registry.VILLAGER_PROFESSION.get(NamespacedKey.minecraft(v.name().toLowerCase(Locale.ROOT)));
	}
}
