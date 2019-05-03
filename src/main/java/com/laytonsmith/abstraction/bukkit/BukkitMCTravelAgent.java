package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.PureUtilities.Common.ReflectionUtils;
import com.laytonsmith.abstraction.MCTravelAgent;

public class BukkitMCTravelAgent implements MCTravelAgent {

	Object a;

	public BukkitMCTravelAgent(Object a) {
		this.a = a;
	}

	@Override
	public String toString() {
		return a.toString();
	}

	@Override
	@SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
	public boolean equals(Object obj) {
		return a.equals(obj);
	}

	@Override
	public int hashCode() {
		return a.hashCode();
	}

	@Override
	public int getCreationRadius() {
		return (int) ReflectionUtils.invokeMethod(a, "getCreationRadius");
	}

	@Override
	public void setCreationRadius(int radius) {
		ReflectionUtils.invokeMethod(a, "setCreationRadius", new Class[]{int.class}, new Object[]{radius});
	}

	@Override
	public int getSearchRadius() {
		return (int) ReflectionUtils.invokeMethod(a, "getSearchRadius");
	}

	@Override
	public void setSearchRadius(int radius) {
		ReflectionUtils.invokeMethod(a, "setSearchRadius", new Class[]{int.class}, new int[]{radius});
	}

	@Override
	public Object getHandle() {
		return a;
	}
}
