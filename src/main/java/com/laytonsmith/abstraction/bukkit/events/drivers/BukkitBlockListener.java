

package com.laytonsmith.abstraction.bukkit.events.drivers;

import com.laytonsmith.abstraction.bukkit.events.BukkitBlockEvents;
import com.laytonsmith.core.events.Driver;
import com.laytonsmith.core.events.EventUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;


/**
 *
 * @author Layton
 */
public class BukkitBlockListener implements Listener{
	@EventHandler(priority=EventPriority.LOWEST)
    public void onSignChange(SignChangeEvent e){
		BukkitBlockEvents.BukkitMCSignChangeEvent mce = new BukkitBlockEvents.BukkitMCSignChangeEvent(e);
		EventUtils.TriggerExternal(mce);
        EventUtils.TriggerListener(Driver.SIGN_CHANGED, "sign_changed", mce);
    }
	
	@EventHandler(priority=EventPriority.LOWEST)
    public void onBlockPlace(BlockPlaceEvent e){
		BukkitBlockEvents.BukkitMCBlockPlaceEvent bpe = new BukkitBlockEvents.BukkitMCBlockPlaceEvent(e);
		EventUtils.TriggerExternal(bpe);
        EventUtils.TriggerListener(Driver.BLOCK_PLACE, "block_place", bpe);
    }
	
	@EventHandler(priority=EventPriority.LOWEST)
    public void onBlockBreak(BlockBreakEvent e){
		BukkitBlockEvents.BukkitMCBlockBreakEvent bbe = new BukkitBlockEvents.BukkitMCBlockBreakEvent(e);
		EventUtils.TriggerExternal(bbe);
        EventUtils.TriggerListener(Driver.BLOCK_BREAK, "block_break", bbe);
    }

	@EventHandler(priority = EventPriority.LOWEST)
	public void onBlockDispense(BlockDispenseEvent e) {
		BukkitBlockEvents.BukkitMCBlockDispenseEvent bde = new BukkitBlockEvents.BukkitMCBlockDispenseEvent(e);
		EventUtils.TriggerExternal(bde);
		EventUtils.TriggerListener(Driver.BLOCK_DISPENSE, "block_dispense", bde);
	}
}
