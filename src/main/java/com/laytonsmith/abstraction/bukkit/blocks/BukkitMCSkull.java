package com.laytonsmith.abstraction.bukkit.blocks;

import com.laytonsmith.PureUtilities.Common.ReflectionUtils;
import com.laytonsmith.abstraction.MCOfflinePlayer;
import com.laytonsmith.abstraction.MCPlayerProfile;
import com.laytonsmith.abstraction.blocks.MCSkull;
import com.laytonsmith.abstraction.bukkit.BukkitMCOfflinePlayer;

import com.laytonsmith.abstraction.bukkit.BukkitMCPlayerProfile;
import com.laytonsmith.abstraction.bukkit.BukkitMCServer;
import com.laytonsmith.core.Static;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Skull;

public class BukkitMCSkull extends BukkitMCBlockState implements MCSkull {

	Skull skull;

	public BukkitMCSkull(Skull skull) {
		super(skull);
		this.skull = skull;
	}

	@Override
	public Skull getHandle() {
		return this.skull;
	}

	@Override
	public MCOfflinePlayer getOwningPlayer() {
		OfflinePlayer player = this.skull.getOwningPlayer();
		return (player == null ? null : new BukkitMCOfflinePlayer(this.skull.getOwningPlayer()));
	}

	@Override
	public boolean hasOwner() {
		return this.skull.hasOwner();
	}

	@Override
	public void setOwningPlayer(MCOfflinePlayer player) {

		// Handle resetting the skull owner. The Bukkit API does not allow this and clients will need to refresh chunks
		// to see this change.
		if(player == null) {
			ReflectionUtils.set(this.skull.getClass(), this.skull, "profile", null);
			return;
		}

		// Set the skull owner.
		this.skull.setOwningPlayer((OfflinePlayer) player.getHandle());
	}

	@Override
	public MCPlayerProfile getPlayerProfile() {
		if(((BukkitMCServer) Static.getServer()).isPaper()) {
			Object profile = ReflectionUtils.invokeMethod(Skull.class, this.skull, "getPlayerProfile");
			if(profile != null) {
				return new BukkitMCPlayerProfile(profile);
			}
		}
		return null;
	}

	@Override
	public void setPlayerProfile(MCPlayerProfile profile) {
		ReflectionUtils.invokeMethod(this.skull, "setPlayerProfile", profile.getHandle());
	}
}
