package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.PureUtilities.Common.ReflectionUtils;
import com.laytonsmith.abstraction.MCPotionData;
import com.laytonsmith.abstraction.enums.MCPotionType;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCPotionType;

public class BukkitMCPotionData implements MCPotionData {

	Object pd;

	public BukkitMCPotionData(Object poda) {
		pd = poda;
	}

	@Override
	public MCPotionType getType() {
		return BukkitMCPotionType.valueOfConcrete(ReflectionUtils.invokeMethod(pd, "getType"));
	}

	@Override
	public boolean isExtended() {
		return ReflectionUtils.invokeMethod(pd, "isExtended");
	}

	@Override
	public boolean isUpgraded() {
		return ReflectionUtils.invokeMethod(pd, "isUpgraded");
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
