/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.abstraction.bukkit.events.drivers;

import com.laytonsmith.core.events.Driver;
import com.laytonsmith.core.events.EventUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityListener;

/**
 *
 * @author Layton
 */
public class BukkitEntityListener extends EntityListener{

    @Override
    public void onEntityDeath(EntityDeathEvent event) {
        if(event.getEntity() instanceof Player){
            EventUtils.TriggerListener(Driver.PLAYER_DEATH, "player_death", event);
        }
    }
    
    
}
