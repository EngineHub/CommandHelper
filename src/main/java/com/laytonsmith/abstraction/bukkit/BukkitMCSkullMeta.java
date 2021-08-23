package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.PureUtilities.Common.ReflectionUtils;
import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.MCOfflinePlayer;
import com.laytonsmith.abstraction.MCPlayerProfile;
import com.laytonsmith.abstraction.MCSkullMeta;
import com.laytonsmith.core.Static;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.meta.SkullMeta;

public class BukkitMCSkullMeta extends BukkitMCItemMeta implements MCSkullMeta {

	SkullMeta sm;

	public BukkitMCSkullMeta(SkullMeta im) {
		super(im);
		this.sm = im;
	}

	public BukkitMCSkullMeta(AbstractionObject o) {
		super(o);
		this.sm = (SkullMeta) o;
	}

	@Override
	public boolean hasOwner() {
		// Spigot only returns true of profile has a name, ignoring UUID.
		return sm.hasOwner();
	}

	@Override
	public String getOwner() {
		return sm.getOwner();
	}

	@Override
	public MCOfflinePlayer getOwningPlayer() {
		// Spigot will return null if profile doesn't have a name.
		// This might be a bug.
		OfflinePlayer ofp = sm.getOwningPlayer();
		if(ofp != null) {
			return new BukkitMCOfflinePlayer(sm.getOwningPlayer());
		}
		return null;
	}

	@Override
	public boolean setOwner(String owner) {
		return sm.setOwner(owner);
	}

	@Override
	public void setOwningPlayer(MCOfflinePlayer player) {
		sm.setOwningPlayer((OfflinePlayer) player.getHandle());
	}

	@Override
	public MCPlayerProfile getProfile() {
		if(((BukkitMCServer) Static.getServer()).isPaper()) {
			Object profile = ReflectionUtils.invokeMethod(SkullMeta.class, sm, "getPlayerProfile");
			if(profile != null) {
				return new BukkitMCPlayerProfile(profile);
			}
		}
		return null;
	}

	@Override
	public void setProfile(MCPlayerProfile profile) {
		// Completes the profile from user cache.
		ReflectionUtils.invokeMethod(sm, "setPlayerProfile", profile.getHandle());
	}
}
