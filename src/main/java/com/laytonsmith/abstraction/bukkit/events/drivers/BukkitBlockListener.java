package com.laytonsmith.abstraction.bukkit.events.drivers;

import com.laytonsmith.abstraction.bukkit.events.BukkitBlockEvents;
import com.laytonsmith.commandhelper.CommandHelperPlugin;
import com.laytonsmith.core.events.Driver;
import com.laytonsmith.core.events.EventUtils;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;

/**
 *
 * 
 */
public class BukkitBlockListener implements Listener{
	// Track piston state, because Bukkit tends to multi-fire it's piston events.
	// HAX!
	List<Block> pistonsOut = new ArrayList<>();
	List<Block> pistonsIn = new ArrayList<>();
	
	@EventHandler(priority=EventPriority.LOWEST)
    public void onPistonExtend(final BlockPistonExtendEvent e){
		pistonsIn.remove(e.getBlock());
		if (pistonsOut.contains(e.getBlock())) {
			return;
		}
		
		Bukkit.getScheduler().runTaskLater(CommandHelperPlugin.self, new Runnable() {
			@Override
			public void run() {
				pistonsOut.remove(e.getBlock());
			}
		}, 20);
		
		pistonsOut.add(e.getBlock());
		
		BukkitBlockEvents.BukkitMCBlockPistonExtendEvent mce = new BukkitBlockEvents.BukkitMCBlockPistonExtendEvent(e);
        EventUtils.TriggerListener(Driver.PISTON_EXTEND, "piston_extend", mce);
    }
	
	@EventHandler(priority=EventPriority.LOWEST)
    public void onPistonRetract(final BlockPistonRetractEvent e){
		pistonsOut.remove(e.getBlock());
		if (pistonsIn.contains(e.getBlock())) {
			return;
		}
		
		Bukkit.getScheduler().runTaskLater(CommandHelperPlugin.self, new Runnable() {
			@Override
			public void run() {
				pistonsIn.remove(e.getBlock());
			}
		}, 20);
		
		pistonsIn.add(e.getBlock());
		
		BukkitBlockEvents.BukkitMCBlockPistonRetractEvent mce = new BukkitBlockEvents.BukkitMCBlockPistonRetractEvent(e);
        EventUtils.TriggerListener(Driver.PISTON_RETRACT, "piston_retract", mce);
    }
	
	@EventHandler(priority=EventPriority.LOWEST)
    public void onSignChange(SignChangeEvent e){
		BukkitBlockEvents.BukkitMCSignChangeEvent mce = new BukkitBlockEvents.BukkitMCSignChangeEvent(e);
        EventUtils.TriggerListener(Driver.SIGN_CHANGED, "sign_changed", mce);
    }
	
	@EventHandler(priority=EventPriority.LOWEST)
    public void onBlockPlace(BlockPlaceEvent e){
		BukkitBlockEvents.BukkitMCBlockPlaceEvent bpe = new BukkitBlockEvents.BukkitMCBlockPlaceEvent(e);
        EventUtils.TriggerListener(Driver.BLOCK_PLACE, "block_place", bpe);
    }
	
	@EventHandler(priority=EventPriority.LOWEST)
    public void onBlockBreak(BlockBreakEvent e){
		BukkitBlockEvents.BukkitMCBlockBreakEvent bbe = new BukkitBlockEvents.BukkitMCBlockBreakEvent(e);
        EventUtils.TriggerListener(Driver.BLOCK_BREAK, "block_break", bbe);
    }

	@EventHandler(priority = EventPriority.LOWEST)
	public void onBlockDispense(BlockDispenseEvent e) {
		BukkitBlockEvents.BukkitMCBlockDispenseEvent bde = new BukkitBlockEvents.BukkitMCBlockDispenseEvent(e);
		EventUtils.TriggerListener(Driver.BLOCK_DISPENSE, "block_dispense", bde);
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onBlockBurn(BlockBurnEvent e){
		BukkitBlockEvents.BukkitMCBlockBurnEvent bbe = new BukkitBlockEvents.BukkitMCBlockBurnEvent(e);
		EventUtils.TriggerListener(Driver.BLOCK_BURN, "block_burn", bbe);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onBlockIgnite(BlockIgniteEvent e) {
		BukkitBlockEvents.BukkitMCBlockIgniteEvent bie = new BukkitBlockEvents.BukkitMCBlockIgniteEvent(e);
		EventUtils.TriggerListener(Driver.BLOCK_IGNITE, "block_ignite", bie);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onBlockGrow(BlockGrowEvent e) {
		BukkitBlockEvents.BukkitMCBlockGrowEvent bge = new BukkitBlockEvents.BukkitMCBlockGrowEvent(e);
		EventUtils.TriggerListener(Driver.BLOCK_GROW, "block_grow", bge);
	}
}
