package com.laytonsmith.abstraction.enums.bukkit;

import com.laytonsmith.abstraction.enums.MCTrimMaterial;
import com.laytonsmith.core.MSLog;
import com.laytonsmith.core.constructs.Target;
import org.bukkit.inventory.meta.trim.TrimMaterial;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class BukkitMCTrimMaterial extends MCTrimMaterial<TrimMaterial> {

	private static final Map<TrimMaterial, MCTrimMaterial> BUKKIT_MAP = new HashMap<>();

	public BukkitMCTrimMaterial(MCVanillaTrimMaterial vanillaTrimMaterial, TrimMaterial trimMaterial) {
		super(vanillaTrimMaterial, trimMaterial);
	}

	@Override
	public String name() {
		if(getAbstracted() == MCVanillaTrimMaterial.UNKNOWN) {
			return getConcrete().getKey().getKey().toUpperCase();
		}
		return getAbstracted().name();
	}

	public static MCTrimMaterial valueOfConcrete(TrimMaterial test) {
		MCTrimMaterial trimMaterial = BUKKIT_MAP.get(test);
		if(trimMaterial == null) {
			MSLog.GetLogger().e(MSLog.Tags.GENERAL, "Bukkit Trim Material missing in BUKKIT_MAP: "
					+ test, Target.UNKNOWN);
			return new BukkitMCTrimMaterial(MCVanillaTrimMaterial.UNKNOWN, test);
		}
		return trimMaterial;
	}

	public static void build() {
		for(MCVanillaTrimMaterial v : MCVanillaTrimMaterial.values()) {
			if(v == MCVanillaTrimMaterial.UNKNOWN) {
				continue;
			}
			TrimMaterial trimMaterial;
			try {
				trimMaterial = (TrimMaterial) TrimMaterial.class.getField(v.name()).get(null);
			} catch (IllegalAccessException | NoSuchFieldException ex) {
				MSLog.GetLogger().w(MSLog.Tags.RUNTIME, "Could not find a Bukkit trim material type for "
						+ v.name(), Target.UNKNOWN);
				continue;
			}
			BukkitMCTrimMaterial wrapper = new BukkitMCTrimMaterial(v, trimMaterial);
			MAP.put(v.name(), wrapper);
			BUKKIT_MAP.put(trimMaterial, wrapper);
		}
		for(Field field : TrimMaterial.class.getFields()) {
			try {
				TrimMaterial trimMaterial = (TrimMaterial) field.get(null);
				if(!BUKKIT_MAP.containsKey(trimMaterial)) {
					MAP.put(field.getName(), new BukkitMCTrimMaterial(MCVanillaTrimMaterial.UNKNOWN, trimMaterial));
					BUKKIT_MAP.put(trimMaterial, new BukkitMCTrimMaterial(MCVanillaTrimMaterial.UNKNOWN, trimMaterial));
				}
			} catch (IllegalAccessException | ClassCastException ignore) {}
		}
	}
}
