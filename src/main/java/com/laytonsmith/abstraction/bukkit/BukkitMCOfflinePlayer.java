

package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.MCOfflinePlayer;
import com.laytonsmith.abstraction.MCPlayer;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 *
 * 
 */
public class BukkitMCOfflinePlayer extends BukkitMCAnimalTamer implements MCOfflinePlayer{

    OfflinePlayer op;
    BukkitMCOfflinePlayer(OfflinePlayer offlinePlayer) {
        super(offlinePlayer);
        this.op = offlinePlayer;
    }

	@Override
    public boolean isOnline() {
        return op.isOnline();
    }

	@Override
    public String getName() {
        return op.getName();
    }

	@Override
    public boolean isBanned() {
        return op.isBanned();
    }

	@Override
    public void setBanned(boolean banned) {
        op.setBanned(banned);
    }

	@Override
    public boolean isWhitelisted() {
        return op.isWhitelisted();
    }

	@Override
    public void setWhitelisted(boolean value) {
        op.setWhitelisted(value);
    }

	@Override
    public MCPlayer getPlayer() {
        if(op instanceof Player) {
            return new BukkitMCPlayer(((Player)op));
        }
        return null;
    }

	@Override
	public long getFirstPlayed() {
		return op.getFirstPlayed();
	}

	@Override
	public long getLastPlayed() {
		return op.getLastPlayed();
	}

	@Override
	public boolean hasPlayedBefore() {
		return op.hasPlayedBefore();
	}

	@Override
	public MCLocation getBedSpawnLocation() {
		Location loc = op.getBedSpawnLocation();
		return loc == null ? null : new BukkitMCLocation(loc);
    }

	@Override
	public UUID getUniqueID() {
		return op.getUniqueId();
	}
}
