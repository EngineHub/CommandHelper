package com.laytonsmith.abstraction.enums.bukkit;

import com.laytonsmith.abstraction.enums.MCTrimPattern;
import com.laytonsmith.core.MSLog;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.Target;
import org.bukkit.inventory.meta.trim.TrimPattern;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class BukkitMCTrimPattern extends MCTrimPattern<TrimPattern> {

	private static final Map<TrimPattern, MCTrimPattern> BUKKIT_MAP = new HashMap<>();

	public BukkitMCTrimPattern(MCVanillaTrimPattern vanillaTrimPattern, TrimPattern trimPattern) {
		super(vanillaTrimPattern, trimPattern);
	}

	@Override
	public String name() {
		if(getAbstracted() == MCVanillaTrimPattern.UNKNOWN) {
			getConcrete().getKey().getKey().toUpperCase();
		}
		return getAbstracted().name();
	}

	public static MCTrimPattern valueOfConcrete(TrimPattern test) {
		MCTrimPattern trimPattern = BUKKIT_MAP.get(test);
		if(trimPattern == null) {
			MSLog.GetLogger().e(MSLog.Tags.GENERAL, "Bukkit Trim Pattern missing in BUKKIT_MAP: "
					+ test, Target.UNKNOWN);
			return new BukkitMCTrimPattern(MCVanillaTrimPattern.UNKNOWN, test);
		}
		return trimPattern;
	}

	public static void build() {
		for(MCVanillaTrimPattern v : MCVanillaTrimPattern.values()) {
			if(v == MCVanillaTrimPattern.UNKNOWN || !v.existsIn(Static.getServer().getMinecraftVersion())) {
				continue;
			}
			TrimPattern trimPattern;
			try {
				trimPattern = getBukkitType(v);
			} catch (IllegalAccessException | NoSuchFieldException ex) {
				MSLog.GetLogger().w(MSLog.Tags.GENERAL, "Could not find a Bukkit trim pattern type for "
						+ v.name(), Target.UNKNOWN);
				continue;
			}
			BukkitMCTrimPattern wrapper = new BukkitMCTrimPattern(v, trimPattern);
			MAP.put(v.name(), wrapper);
			BUKKIT_MAP.put(trimPattern, wrapper);
		}
		for(Field field : TrimPattern.class.getFields()) {
			try {
				TrimPattern trimPattern = (TrimPattern) field.get(null);
				if(!BUKKIT_MAP.containsKey(trimPattern)) {
					MSLog.GetLogger().w(MSLog.Tags.GENERAL, "Could not find MCTrimPattern for "
							+ field.getName(), Target.UNKNOWN);
					MCTrimPattern wrapper = new BukkitMCTrimPattern(MCVanillaTrimPattern.UNKNOWN, trimPattern);
					MAP.put(field.getName(), wrapper);
					BUKKIT_MAP.put(trimPattern, wrapper);
				}
			} catch (IllegalAccessException | ClassCastException ignore) {}
		}
	}

	private static TrimPattern getBukkitType(MCVanillaTrimPattern v) throws NoSuchFieldException, IllegalAccessException {
		return (TrimPattern) TrimPattern.class.getField(v.name()).get(null);
	}
}
