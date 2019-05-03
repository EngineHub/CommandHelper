package com.laytonsmith.abstraction.enums.bukkit;

import com.laytonsmith.abstraction.enums.MCProfession;
import com.laytonsmith.core.MSLog;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.Target;
import org.bukkit.entity.Villager;

import java.util.HashMap;
import java.util.Map;

public class BukkitMCProfession extends MCProfession<Villager.Profession> {

	private static final Map<Villager.Profession, MCProfession> BUKKIT_MAP = new HashMap<>();

	public BukkitMCProfession(MCVanillaProfession vanillaProfession, Villager.Profession profession) {
		super(vanillaProfession, profession);
	}

	@Override
	public String name() {
		return getAbstracted() == MCVanillaProfession.NONE ? concreteName() : getAbstracted().name();
	}

	@Override
	public String concreteName() {
		return getConcrete().name();
	}

	public static MCProfession valueOfConcrete(Villager.Profession test) {
		return BUKKIT_MAP.get(test);
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
		for(Villager.Profession pr : Villager.Profession.values()) {
			if(pr != null && !BUKKIT_MAP.containsKey(pr)) {
				MAP.put(pr.name(), new BukkitMCProfession(MCVanillaProfession.UNKNOWN, pr));
				BUKKIT_MAP.put(pr, new BukkitMCProfession(MCVanillaProfession.UNKNOWN, pr));
			}
		}
	}

	private static Villager.Profession getBukkitType(MCVanillaProfession v) {
		return Villager.Profession.valueOf(v.name());
	}
}
