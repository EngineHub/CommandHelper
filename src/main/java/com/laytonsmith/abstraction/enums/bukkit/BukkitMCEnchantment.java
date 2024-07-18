package com.laytonsmith.abstraction.enums.bukkit;

import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.enums.MCEnchantment;
import com.laytonsmith.abstraction.enums.MCVersion;
import com.laytonsmith.core.MSLog;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.Target;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class BukkitMCEnchantment extends MCEnchantment<Enchantment> {

	private static final Map<Enchantment, MCEnchantment> BUKKIT_MAP = new HashMap<>();

	public BukkitMCEnchantment(MCVanillaEnchantment vanillaType, Enchantment effect) {
		super(vanillaType, effect);
	}

	@Override
	public String name() {
		if(getAbstracted() == MCVanillaEnchantment.UNKNOWN) {
			return getConcrete().getKey().getKey().toUpperCase();
		}
		return getAbstracted().name();
	}

	public static MCEnchantment valueOfConcrete(Enchantment test) {
		MCEnchantment type = BUKKIT_MAP.get(test);
		if(type == null) {
			MSLog.GetLogger().e(MSLog.Tags.GENERAL, "Bukkit Enchantment missing in BUKKIT_MAP: "
					+ test.getKey().getKey().toUpperCase(), Target.UNKNOWN);
			return new BukkitMCEnchantment(MCVanillaEnchantment.UNKNOWN, test);
		}
		return type;
	}

	@Override
	public boolean canEnchantItem(MCItemStack is) {
		return getConcrete().canEnchantItem((ItemStack) is.getHandle());
	}

	@Override
	public int getMaxLevel() {
		return getConcrete().getMaxLevel();
	}

	public static void build() {
		for(MCVanillaEnchantment v : MCVanillaEnchantment.values()) {
			if(v.existsIn(Static.getServer().getMinecraftVersion())) {
				Enchantment type = getBukkitType(v);
				if(type == null) {
					MSLog.GetLogger().w(MSLog.Tags.RUNTIME, "Could not find a Bukkit enchantment for " + v.name(), Target.UNKNOWN);
					continue;
				}
				BukkitMCEnchantment wrapper = new BukkitMCEnchantment(v, type);
				MAP.put(v.name(), wrapper);
				BUKKIT_MAP.put(type, wrapper);
			}
		}
		for(Enchantment pt : Enchantment.values()) {
			if(pt != null && !BUKKIT_MAP.containsKey(pt)) {
				MAP.put(pt.getKey().getKey().toUpperCase(), new BukkitMCEnchantment(MCVanillaEnchantment.UNKNOWN, pt));
				BUKKIT_MAP.put(pt, new BukkitMCEnchantment(MCVanillaEnchantment.UNKNOWN, pt));
			}
		}
	}

	private static Enchantment getBukkitType(MCVanillaEnchantment v) {
		if(Static.getServer().getMinecraftVersion().gte(MCVersion.MC1_20_6)) {
			if(v == MCVanillaEnchantment.SWEEPING) {
				return Enchantment.SWEEPING_EDGE;
			}
		}
		return Enchantment.getByKey(NamespacedKey.minecraft(v.name().toLowerCase(Locale.ROOT)));
	}
}
