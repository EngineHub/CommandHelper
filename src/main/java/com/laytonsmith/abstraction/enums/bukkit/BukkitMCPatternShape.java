package com.laytonsmith.abstraction.enums.bukkit;

import com.laytonsmith.PureUtilities.Common.ReflectionUtils;
import com.laytonsmith.PureUtilities.Common.ReflectionUtils.ReflectionException;
import com.laytonsmith.abstraction.enums.MCPatternShape;
import com.laytonsmith.core.MSLog;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.Target;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.block.banner.PatternType;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class BukkitMCPatternShape extends MCPatternShape<PatternType> {

	private static final Map<PatternType, MCPatternShape> BUKKIT_MAP = new HashMap<>();

	public BukkitMCPatternShape(MCVanillaPatternShape vanillaPatternShape, PatternType pattern) {
		super(vanillaPatternShape, pattern);
	}

	@Override
	public String name() {
		if(getAbstracted() == MCVanillaPatternShape.UNKNOWN) {
			// changed from enum to interface in 1.21, so cannot call methods from PatternType
			try {
				NamespacedKey key = ReflectionUtils.invokeMethod(Keyed.class, getConcrete(), "getKey");
				return key.getKey().toUpperCase(Locale.ROOT);
			} catch(ReflectionException ex) {
				// probably before 1.20.4, so something went wrong
				MSLog.GetLogger().e(MSLog.Tags.GENERAL, "Could not resolve unknown PatternType", Target.UNKNOWN);
			}
		}
		return getAbstracted().name();
	}

	public static MCPatternShape valueOfConcrete(PatternType test) {
		MCPatternShape type = BUKKIT_MAP.get(test);
		if(type == null) {
			MSLog.GetLogger().w(MSLog.Tags.GENERAL, "PatternType missing in BUKKIT_MAP: " + test, Target.UNKNOWN);
			return new BukkitMCPatternShape(MCVanillaPatternShape.UNKNOWN, test);
		}
		return type;
	}

	public static void build() {
		for(MCVanillaPatternShape v : MCVanillaPatternShape.values()) {
			if(v.existsIn(Static.getServer().getMinecraftVersion())) {
				PatternType type;
				try {
					type = getBukkitType(v);
				} catch (IllegalArgumentException ex) {
					MSLog.GetLogger().w(MSLog.Tags.GENERAL, "Could not find Bukkit PatternType for " + v.name(),
							Target.UNKNOWN);
					continue;
				}
				BukkitMCPatternShape wrapper = new BukkitMCPatternShape(v, type);
				BUKKIT_MAP.put(type, wrapper);
				MAP.put(v.name(), wrapper);
			}
		}
		try {
			for(PatternType type : Registry.BANNER_PATTERN) {
				if(!BUKKIT_MAP.containsKey(type)) {
					String name = type.getKey().getKey().toUpperCase(Locale.ROOT);
					MSLog.GetLogger().w(MSLog.Tags.GENERAL, "Could not find MCPatternShape for " + name,
							Target.UNKNOWN);
					MCPatternShape wrapper = new BukkitMCPatternShape(MCVanillaPatternShape.UNKNOWN, type);
					MAP.put(name, wrapper);
					BUKKIT_MAP.put(type, wrapper);
				}
			}
		} catch (IncompatibleClassChangeError ignore) {
			// probably before 1.20.4 so we do not have to check for new missing values
		}
	}

	private static PatternType getBukkitType(MCVanillaPatternShape v) throws IllegalArgumentException {
		// changed from enum to interface in 1.21, so cannot call methods from PatternType
		try {
			String typeName = v.name();
			typeName = switch(typeName) {
				case "DIAGONAL_RIGHT_MIRROR" -> "diagonal_right";
				case "DIAGONAL_RIGHT" -> "diagonal_up_right";
				case "STRIPE_SMALL" -> "small_stripes";
				case "DIAGONAL_LEFT_MIRROR" -> "diagonal_up_left";
				case "CIRCLE_MIDDLE" -> "circle";
				case "RHOMBUS_MIDDLE" -> "rhombus";
				case "HALF_VERTICAL_MIRROR" -> "half_vertical_right";
				case "HALF_HORIZONTAL_MIRROR" -> "half_horizontal_bottom";
				default -> typeName.toLowerCase(Locale.ROOT);
			};
			PatternType t = Registry.BANNER_PATTERN.get(NamespacedKey.minecraft(typeName));
			if(t == null) {
				throw new IllegalArgumentException();
			}
			return t;
		} catch(NoSuchFieldError ex) {
			// probably before 1.20.4 when registry field was added
			try {
				Class cls = Class.forName("org.bukkit.block.banner.PatternType");
				return ReflectionUtils.invokeMethod(cls, null, "valueOf",
						new Class[]{String.class}, new Object[]{v.name()});
			} catch (ClassNotFoundException exc) {
				throw new IllegalArgumentException();
			}
		}
	}
}
