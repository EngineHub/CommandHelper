

package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.AbstractionUtils;
import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.MCOfflinePlayer;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.annotations.WrappedItem;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

/**
 *
 * @author layton
 */
public class BukkitMCOfflinePlayer extends BukkitMCAnimalTamer implements MCOfflinePlayer{

    @WrappedItem OfflinePlayer op;

    public boolean isOnline() {
        return op.isOnline();
    }

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
            return AbstractionUtils.wrap(((Player)op));
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
        return AbstractionUtils.wrap(op.getBedSpawnLocation());
    }
}
