package com.laytonsmith.abstraction.bukkit;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.laytonsmith.abstraction.MCPlayerProfile;
import com.laytonsmith.abstraction.MCProfileProperty;

import java.util.Set;
import java.util.UUID;

public class BukkitMCPlayerProfile implements MCPlayerProfile {

	PlayerProfile pp;

	public BukkitMCPlayerProfile(Object pp) {
		this.pp = (PlayerProfile) pp;
	}

	@Override
	public String getName() {
		return this.pp.getName();
	}

	@Override
	public UUID getId() {
		return this.pp.getId();
	}

	@Override
	public MCProfileProperty getProperty(String key) {
		Set<ProfileProperty> properties = this.pp.getProperties();
		for(ProfileProperty p : properties) {
			if(p.getName().equals(key)) {
				return new MCProfileProperty(p.getName(), p.getValue(), p.getSignature());
			}
		}
		return null;
	}

	@Override
	public void setProperty(MCProfileProperty property) {
		this.pp.setProperty(new ProfileProperty(property.getName(), property.getValue(), property.getValue()));
	}

	@Override
	public boolean removeProperty(String key) {
		return this.pp.removeProperty(key);
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
