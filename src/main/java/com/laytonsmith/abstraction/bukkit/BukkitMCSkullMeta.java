package com.laytonsmith.abstraction.bukkit;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.MCOfflinePlayer;
import com.laytonsmith.abstraction.MCPlayerProfile;
import com.laytonsmith.abstraction.MCSkullMeta;
import com.laytonsmith.core.Static;
import org.bukkit.NamespacedKey;
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
			PlayerProfile profile = this.sm.getPlayerProfile();
			if(profile != null) {
				return new BukkitMCPlayerProfile(profile);
			}
		}
		return null;
	}

	@Override
	public void setProfile(MCPlayerProfile profile) {
		// Completes the profile from user cache.
		this.sm.setPlayerProfile((PlayerProfile) profile.getHandle());
	}

	@Override
	public String getNoteBlockSound() {
		NamespacedKey sound = this.sm.getNoteBlockSound();
		if(sound == null) {
			return null;
		}
		return sound.toString();
	}

	@Override
	public void setNoteBlockSound(String noteBlockSound) {
		this.sm.setNoteBlockSound(NamespacedKey.fromString(noteBlockSound));
	}
}
