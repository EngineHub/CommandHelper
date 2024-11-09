package com.laytonsmith.abstraction.bukkit.blocks;

import com.laytonsmith.abstraction.blocks.MCBlockData;
import com.laytonsmith.abstraction.blocks.MCMaterial;
import com.laytonsmith.core.MSLog;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.Target;
import org.bukkit.Material;

import java.util.EnumMap;
import java.util.Map;

public class BukkitMCMaterial extends MCMaterial<Material> {

	private static final Map<Material, MCMaterial> BUKKIT_MAP = new EnumMap<>(Material.class);

	public BukkitMCMaterial(Material type) {
		this(null, type);
	}

	private BukkitMCMaterial(MCVanillaMaterial vanillaMaterial, Material type) {
		super(vanillaMaterial, type);
	}

	@Override
	public String name() {
		return getAbstracted() == null ? getConcrete().name() : getAbstracted().name();
	}

	public static MCMaterial valueOfConcrete(Material test) {
		MCMaterial type = BUKKIT_MAP.get(test);
		if(type == null) {
			MSLog.GetLogger().e(MSLog.Tags.GENERAL, "Bukkit Material missing in BUKKIT_MAP: " + test.name(),
					Target.UNKNOWN);
			return new BukkitMCMaterial(null, test);
		}
		return type;
	}

	public static void build() {
		for(MCVanillaMaterial v : MCVanillaMaterial.values()) {
			if(v.existsIn(Static.getServer().getMinecraftVersion())) {
				Material type;
				try {
					type = Material.valueOf(v.name());
				} catch (IllegalArgumentException | NoSuchFieldError ex) {
					// This means something was removed or changed; MCVanillaMaterial will need an update.
					MSLog.GetLogger().e(MSLog.Tags.GENERAL, "Could not find a Bukkit Material for " + v.name(),
							Target.UNKNOWN);
					continue;
				}
				BukkitMCMaterial wrapper = new BukkitMCMaterial(v, type);
				BY_STRING.put(v.name(), wrapper);
				BUKKIT_MAP.put(type, wrapper);
			}
		}
		// Add missing values from Concrete.
		// These values will still be accepted on an MC server, but will be missing from cmdline.
		for(Material m : Material.values()) {
			if(!m.isLegacy() && !BUKKIT_MAP.containsKey(m)) {
				MSLog.GetLogger().w(MSLog.Tags.GENERAL, "Could not find MCMaterial for " + m.name(), Target.UNKNOWN);
				BukkitMCMaterial wrapper = new BukkitMCMaterial(null, m);
				BY_STRING.put(m.name(), wrapper);
				BUKKIT_MAP.put(m, wrapper);
			}
		}
	}

	@Override
	public MCBlockData createBlockData() {
		return new BukkitMCBlockData(getHandle().createBlockData());
	}

	@Override
	public short getMaxDurability() {
		return getHandle().getMaxDurability();
	}

	@Override
	public int getType() {
		return getHandle().getId();
	}

	@Override
	public String getName() {
		return name();
	}

	@Override
	public int getMaxStackSize() {
		return getHandle().getMaxStackSize();
	}

	@Override
	public boolean hasGravity() {
		return getHandle().hasGravity();
	}

	@Override
	public boolean isBlock() {
		return getHandle().isBlock();
	}

	@Override
	public boolean isItem() {
		return getHandle().isItem();
	}

	@Override
	public boolean isBurnable() {
		return getHandle().isBurnable();
	}

	@Override
	public boolean isEdible() {
		return getHandle().isEdible();
	}

	@Override
	public boolean isFlammable() {
		return getHandle().isFlammable();
	}

	@Override
	public boolean isOccluding() {
		return getHandle().isOccluding();
	}

	@Override
	public boolean isRecord() {
		return getHandle().isRecord();
	}

	@Override
	public boolean isSolid() {
		return getHandle().isSolid();
	}

	@Override
	public boolean isTransparent() {
		return getHandle().isTransparent();
	}

	@Override
	public boolean isInteractable() {
		return getHandle().isInteractable();
	}

	@Override
	public boolean isAir() {
		return getHandle().isAir();
	}

	@Override
	public boolean isLegacy() {
		return getHandle().isLegacy();
	}

	@Override
	public float getHardness() {
		return getHandle().getHardness();
	}

	@Override
	public float getBlastResistance() {
		return getHandle().getBlastResistance();
	}

	@Override
	public Material getHandle() {
		return getConcrete();
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof MCMaterial && getHandle().equals(((MCMaterial) obj).getHandle());
	}

	@Override
	public int hashCode() {
		return getHandle().hashCode();
	}

	@Override
	public String toString() {
		return name();
	}

}
