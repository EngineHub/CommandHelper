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

@MDynamicEnum("com.commandhelper.Profession")
public abstract class MCProfession<Concrete> extends DynamicEnum<MCProfession.MCVanillaProfession, Concrete> {

	protected static final Map<String, MCProfession> MAP = new HashMap<>();

	public MCProfession(MCVanillaProfession mcVanillaProfession, Concrete concrete) {
		super(mcVanillaProfession, concrete);
	}

	public static MCProfession valueOf(String test) throws IllegalArgumentException {
		MCProfession ret = MAP.get(test);
		if(ret == null) {
			throw new IllegalArgumentException("Unknown villager profession type: " + test);
		}
		return ret;
	}

	public static Set<String> types() {
		if(MAP.isEmpty()) { // docs mode
			Set<String> dummy = new HashSet<>();
			for(final MCVanillaProfession s : MCVanillaProfession.values()) {
				if(s.existsIn(MCVersion.CURRENT)) {
					dummy.add(s.name());
				}
			}
			return dummy;
		}
		return new TreeSet<>(MAP.keySet());
	}

	public static List<MCProfession> values() {
		if(MAP.isEmpty()) { // docs mode
			ArrayList<MCProfession> dummy = new ArrayList<>();
			for(final MCVanillaProfession s : MCVanillaProfession.values()) {
				if(!s.existsIn(MCVersion.CURRENT)) {
					continue;
				}
				dummy.add(new MCProfession<Object>(s, null) {
					@Override
					public String name() {
						return s.name();
					}

					@Override
					public String concreteName() {
						return s.name();
					}
				});
			}
			return dummy;
		}
		return new ArrayList<>(MAP.values());
	}

	public enum MCVanillaProfession {
		BLACKSMITH(MCVersion.MC1_12_X, MCVersion.MC1_13_X),
		NORMAL(MCVersion.MC1_12_X, MCVersion.MC1_13_X),
		PRIEST(MCVersion.MC1_12_X, MCVersion.MC1_13_X),
		BUTCHER,
		FARMER,
		LIBRARIAN,
		NITWIT,
		ARMORER(MCVersion.MC1_14),
		CARTOGRAPHER(MCVersion.MC1_14),
		CLERIC(MCVersion.MC1_14),
		FISHERMAN(MCVersion.MC1_14),
		FLETCHER(MCVersion.MC1_14),
		LEATHERWORKER(MCVersion.MC1_14),
		MASON(MCVersion.MC1_14),
		NONE(MCVersion.MC1_14),
		SHEPHERD(MCVersion.MC1_14),
		TOOLSMITH(MCVersion.MC1_14),
		WEAPONSMITH(MCVersion.MC1_14),
		UNKNOWN(MCVersion.NEVER);

		private final MCVersion from;
		private final MCVersion to;

		MCVanillaProfession() {
			this.from = MCVersion.MC1_12_X;
			this.to = MCVersion.FUTURE;
		}

		MCVanillaProfession(MCVersion version) {
			this.from = version;
			this.to = MCVersion.FUTURE;
		}

		MCVanillaProfession(MCVersion fromVersion, MCVersion toVersion) {
			this.from = fromVersion;
			this.to = toVersion;
		}

		public boolean existsIn(MCVersion version) {
			return version.gte(from) && version.lte(to);
		}
	}
}
