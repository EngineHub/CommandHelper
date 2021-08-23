package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.PureUtilities.Common.ReflectionUtils;
import com.laytonsmith.abstraction.MCPlayerProfile;
import com.laytonsmith.abstraction.MCProfileProperty;

import java.util.Set;
import java.util.UUID;

public class BukkitMCPlayerProfile implements MCPlayerProfile {

	Object pp;

	public BukkitMCPlayerProfile(Object pp) {
		this.pp = pp;
	}

	@Override
	public String getName() {
		return (String) ReflectionUtils.invokeMethod(this.pp, "getName");
	}

	@Override
	public String setName(String name) {
		return (String) ReflectionUtils.invokeMethod(this.pp, "setName", name);
	}

	@Override
	public UUID getId() {
		return (UUID) ReflectionUtils.invokeMethod(this.pp, "getId");
	}

	@Override
	public UUID setId(UUID id) {
		return (UUID) ReflectionUtils.invokeMethod(this.pp, "setId", id);
	}

	@Override
	public MCProfileProperty getProperty(String key) {
		Set<?> properties = (Set<?>) ReflectionUtils.invokeMethod(this.pp, "getProperties");
		for(Object property : properties) {
			if(ReflectionUtils.invokeMethod(property, "getName").equals(key)) {
				String name = (String) ReflectionUtils.invokeMethod(property, "getName");
				String value = (String) ReflectionUtils.invokeMethod(property, "getValue");
				String signature = (String) ReflectionUtils.invokeMethod(property, "getSignature");
				return new MCProfileProperty(name, value, signature);
			}
		}
		return null;
	}

	@Override
	public void setProperty(MCProfileProperty property) {
		Class clz;
		try {
			clz = Class.forName("com.destroystokyo.paper.profile.ProfileProperty");
		} catch (ClassNotFoundException e) {
			return;
		}
		Object profileProperty = ReflectionUtils.newInstance(clz, new Class[]{String.class, String.class, String.class},
				new Object[]{property.getName(), property.getValue(), property.getSignature()});
		ReflectionUtils.invokeMethod(this.pp, "setProperty", profileProperty);
	}

	@Override
	public Object getHandle() {
		return this.pp;
	}

	@Override
	public String toString() {
		return this.pp.toString();
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof BukkitMCPlayerProfile && this.pp.equals(((BukkitMCPlayerProfile) obj).pp);
	}

	@Override
	public int hashCode() {
		return this.pp.hashCode();
	}
}
