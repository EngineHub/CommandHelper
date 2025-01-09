package com.laytonsmith.abstraction.enums.bukkit;

import com.laytonsmith.PureUtilities.Common.ReflectionUtils;
import com.laytonsmith.abstraction.enums.MCArt;
import com.laytonsmith.core.MSLog;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.Target;
import org.bukkit.Art;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class BukkitMCArt extends MCArt<Art> {

	private static final Map<Art, MCArt> BUKKIT_MAP = new HashMap<>();

	public BukkitMCArt(MCVanillaArt vanillaArtType, Art art) {
		super(vanillaArtType, art);
	}

	@Override
	public String name() {
		if(getAbstracted() == MCVanillaArt.UNKNOWN) {
			// changed from enum to interface in 1.21.3
			NamespacedKey key = ReflectionUtils.invokeMethod(Keyed.class, getConcrete(), "getKey");
			return key.getKey().toUpperCase(Locale.ROOT);
		}
		return getAbstracted().name();
	}

	public static MCArt valueOfConcrete(Art test) {
		MCArt type = BUKKIT_MAP.get(test);
		if(type == null) {
			// changed from enum to interface in 1.21.3
			NamespacedKey key = ReflectionUtils.invokeMethod(Keyed.class, test, "getKey");
			MSLog.GetLogger().e(MSLog.Tags.GENERAL, "Bukkit Art missing in BUKKIT_MAP: "
					+ key.getKey().toUpperCase(Locale.ROOT), Target.UNKNOWN);
			return new BukkitMCArt(MCVanillaArt.UNKNOWN, test);
		}
		return type;
	}

	public static void build() {
		for(MCVanillaArt v : MCVanillaArt.values()) {
			if(v.existsIn(Static.getServer().getMinecraftVersion())) {
				Art type;
				try {
					type = (Art) Art.class.getDeclaredField(v.name()).get(null);
				} catch (IllegalAccessException | NoSuchFieldException e) {
					MSLog.GetLogger().w(MSLog.Tags.GENERAL, "Could not find a Bukkit Art for " + v.name(),
							Target.UNKNOWN);
					continue;
				}
				BukkitMCArt wrapper = new BukkitMCArt(v, type);
				MAP.put(v.name(), wrapper);
				BUKKIT_MAP.put(type, wrapper);
			}
		}
		for(Field f : Art.class.getFields()) {
			try {
				Art a = (Art) f.get(null);
				if(!BUKKIT_MAP.containsKey(a)) {
					MSLog.GetLogger().w(MSLog.Tags.GENERAL, "Could not find an MCArt for " + f.getName(),
							Target.UNKNOWN);
					MCArt wrapper = new BukkitMCArt(MCVanillaArt.UNKNOWN, a);
					MAP.put(f.getName(), wrapper);
					BUKKIT_MAP.put(a, wrapper);
				}
			} catch (IllegalAccessException | ClassCastException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
