

package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.bukkit.entities.BukkitMCPlayer;
import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.MCOfflinePlayer;
import com.laytonsmith.abstraction.entities.MCPlayer;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

/**
 *
 * @author layton
 */
public class BukkitMCOfflinePlayer extends BukkitMCAnimalTamer implements MCOfflinePlayer{

    OfflinePlayer op;

    public BukkitMCOfflinePlayer(OfflinePlayer offlinePlayer) {
        super(offlinePlayer);
        this.op = offlinePlayer;
    }

    public boolean isOnline() {
        return op.isOnline();
    }

	@Override
    public String getName() {
        return op.getName();
    }

    public boolean isBanned() {
        return op.isBanned();
    }

    public void setBanned(boolean banned) {
        op.setBanned(banned);
    }

    public boolean isWhitelisted() {
        return op.isWhitelisted();
    }

    public void setWhitelisted(boolean value) {
        op.setWhitelisted(value);
    }

    public MCPlayer getPlayer() {
        if(op instanceof Player) {
            return new BukkitMCPlayer(((Player)op));
        }
        return null;
    }

	public long getFirstPlayed() {
		return op.getFirstPlayed();
	}

	public long getLastPlayed() {
		return op.getLastPlayed();
	}

	public boolean hasPlayedBefore() {
		return op.hasPlayedBefore();
	}

	public MCLocation getBedSpawnLocation() {
        return new BukkitMCLocation(op.getBedSpawnLocation());
    }
}
