package com.laytonsmith.abstraction.enums;

import com.laytonsmith.PureUtilities.ClassLoading.DynamicEnum;
import com.laytonsmith.annotations.MDynamicEnum;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

@MDynamicEnum("com.commandhelper.TrimMaterial")
public abstract class MCTrimMaterial<Concrete> extends DynamicEnum<MCTrimMaterial.MCVanillaTrimMaterial, Concrete> {

	protected static final Map<String, MCTrimMaterial> MAP = new HashMap<>();

	public MCTrimMaterial(MCVanillaTrimMaterial mcVanillaTrimMaterial, Concrete concrete) {
		super(mcVanillaTrimMaterial, concrete);
	}

	public static MCTrimMaterial valueOf(String test) throws IllegalArgumentException {
		MCTrimMaterial ret = MAP.get(test);
		if(ret == null) {
			throw new IllegalArgumentException("Unknown trim material type: " + test);
		}
		return ret;
	}

	public static Set<String> types() {
		if(MAP.isEmpty()) { // docs mode
			Set<String> dummy = new HashSet<>();
			for(final MCVanillaTrimMaterial s : MCVanillaTrimMaterial.values()) {
				if(s != MCVanillaTrimMaterial.UNKNOWN) {
					dummy.add(s.name());
				}
			}
			return dummy;
		}
		return new TreeSet<>(MAP.keySet());
	}

	public static List<MCTrimMaterial> values() {
		if(MAP.isEmpty()) { // docs mode
			ArrayList<MCTrimMaterial> dummy = new ArrayList<>();
			for(final MCVanillaTrimMaterial s : MCVanillaTrimMaterial.values()) {
				if(s == MCVanillaTrimMaterial.UNKNOWN) {
					continue;
				}
				dummy.add(new MCTrimMaterial<>(s, null) {
					@Override
					public String name() {
						return s.name();
					}
				});
			}
			return dummy;
		}
		return new ArrayList<>(MAP.values());
	}

	public enum MCVanillaTrimMaterial {
		AMETHYST,
		COPPER,
		DIAMOND,
		EMERALD,
		GOLD,
		IRON,
		LAPIS,
		NETHERITE,
		QUARTZ,
		REDSTONE,
		UNKNOWN
	}
}
