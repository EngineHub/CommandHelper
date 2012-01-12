/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.puls3.abstraction.bukkit;

import com.laytonsmith.puls3.abstraction.MCOfflinePlayer;
import com.laytonsmith.puls3.abstraction.MCPlayer;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

/**
 *
 * @author layton
 */
public class BukkitMCOfflinePlayer extends BukkitMCAnimalTamer implements MCOfflinePlayer{

    OfflinePlayer op;
    BukkitMCOfflinePlayer(OfflinePlayer offlinePlayer) {
        super(offlinePlayer);
        this.op = offlinePlayer;
    }

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
        if(op instanceof Player){
            return new BukkitMCPlayer(((Player)op));
        }
        return null;
    }
    
}
