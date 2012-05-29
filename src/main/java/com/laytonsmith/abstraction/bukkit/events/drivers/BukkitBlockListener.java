/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.abstraction.bukkit.events.drivers;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

import com.laytonsmith.abstraction.bukkit.events.BukkitBlockEvents;
import com.laytonsmith.core.events.Driver;
import com.laytonsmith.core.events.EventUtils;


/**
 *
 * @author Layton
 */
public class BukkitBlockListener implements Listener{
	@EventHandler(priority=EventPriority.LOWEST)
    public void onSignChange(SignChangeEvent e){
        EventUtils.TriggerListener(Driver.SIGN_CHANGED, "sign_changed", new BukkitBlockEvents.BukkitMCSignChangeEvent(e));
    }
}
