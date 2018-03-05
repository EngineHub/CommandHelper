package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCPotionData;
import com.laytonsmith.abstraction.enums.MCPotionType;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCPotionType;
import org.bukkit.potion.PotionData;

public class BukkitMCPotionData implements MCPotionData {

	PotionData pd;

	public BukkitMCPotionData(PotionData poda) {
		pd = poda;
	}

	@Override
	public MCPotionType getType() {
		return BukkitMCPotionType.getConvertor().getAbstractedEnum(pd.getType());
	}

	@Override
	public boolean isExtended() {
		return pd.isExtended();
	}

	@Override
	public boolean isUpgraded() {
		return pd.isUpgraded();
	}

	@Override
	public Object getHandle() {
		return pd;
	}

	@Override
	public boolean equals(Object obj) {
		return pd.equals(obj);
	}

	@Override
	public int hashCode() {
		return pd.hashCode();
	}

	@Override
	public String toString() {
		return pd.toString();
	}
}
