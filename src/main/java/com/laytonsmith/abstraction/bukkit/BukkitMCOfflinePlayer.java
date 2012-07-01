/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCOfflinePlayer;
import com.laytonsmith.abstraction.MCPlayer;
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

    public String getName() {
        return op.getName();
    }

    public MCPlayer getPlayer() {
        if(op instanceof Player){
            return new BukkitMCPlayer(((Player)op));
        }
        return null;
    }

    public boolean isBanned() {
        return op.isBanned();
    }

    public boolean isOnline() {
        return op.isOnline();
    }

    public boolean isWhitelisted() {
        return op.isWhitelisted();
    }

    public void setBanned(boolean banned) {
        op.setBanned(banned);
    }

    public void setWhitelisted(boolean value) {
        op.setWhitelisted(value);
    }
    
}
