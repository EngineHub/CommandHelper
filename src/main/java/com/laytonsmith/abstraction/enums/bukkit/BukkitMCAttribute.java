package com.laytonsmith.abstraction.enums.bukkit;

import com.laytonsmith.PureUtilities.Common.ReflectionUtils;
import com.laytonsmith.abstraction.enums.MCAttribute;
import com.laytonsmith.abstraction.enums.MCVersion;
import com.laytonsmith.core.MSLog;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.Target;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class BukkitMCAttribute extends MCAttribute<Attribute> {

	private static final Map<Attribute, MCAttribute> BUKKIT_MAP = new HashMap<>();

	public BukkitMCAttribute(MCVanillaAttribute vanillaAttributeType, Attribute attribute) {
		super(vanillaAttributeType, attribute);
	}

	@Override
	public String name() {
		if(getAbstracted() == MCVanillaAttribute.UNKNOWN) {
			// changed from enum to interface in 1.21.3
			NamespacedKey key = ReflectionUtils.invokeMethod(Keyed.class, getConcrete(), "getKey");
			return key.getKey().toUpperCase(Locale.ROOT);
		}
		return getAbstracted().name();
	}

	public static MCAttribute valueOfConcrete(Attribute test) {
		MCAttribute type = BUKKIT_MAP.get(test);
		if(type == null) {
			// changed from enum to interface in 1.21.3
			NamespacedKey key = ReflectionUtils.invokeMethod(Keyed.class, test, "getKey");
			MSLog.GetLogger().e(MSLog.Tags.GENERAL, "Bukkit Attribute missing in BUKKIT_MAP: "
					+ key.getKey().toUpperCase(Locale.ROOT), Target.UNKNOWN);
			return new BukkitMCAttribute(MCVanillaAttribute.UNKNOWN, test);
		}
		return type;
	}

	public static void build() {
		for(MCVanillaAttribute v : MCVanillaAttribute.values()) {
			if(v.existsIn(Static.getServer().getMinecraftVersion())) {
				String name = v.name();
				if(Static.getServer().getMinecraftVersion().gte(MCVersion.MC1_21_3)) {
					// remove prefixes in MC 1.21.3 and later
					name = name.replace("GENERIC_", "");
					name = name.replace("PLAYER_", "");
					name = name.replace("ZOMBIE_", "");
				}
				Attribute type;
				try {
					type = (Attribute) Attribute.class.getDeclaredField(name).get(null);
				} catch (IllegalAccessException | NoSuchFieldException e) {
					MSLog.GetLogger().w(MSLog.Tags.GENERAL, "Could not find a Bukkit Attribute for " + v.name(),
							Target.UNKNOWN);
					continue;
				}
				BukkitMCAttribute wrapper = new BukkitMCAttribute(v, type);
				MAP.put(v.name(), wrapper);
				BUKKIT_MAP.put(type, wrapper);
			}
		}
		for(Field f : Attribute.class.getFields()) {
			try {
				if(f.get(null) instanceof Attribute a && !BUKKIT_MAP.containsKey(a)) {
					MSLog.GetLogger().w(MSLog.Tags.GENERAL, "Could not find an MCAttribute for " + f.getName(),
							Target.UNKNOWN);
					MCAttribute wrapper = new BukkitMCAttribute(MCVanillaAttribute.UNKNOWN, a);
					MAP.put(f.getName(), wrapper);
					BUKKIT_MAP.put(a, wrapper);
				}
			} catch (IllegalAccessException ignore) {}
		}
	}
}
