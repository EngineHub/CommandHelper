package com.laytonsmith.abstraction.bukkit.events.drivers;

import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.bukkit.events.BukkitBlockEvents;
import com.laytonsmith.core.events.Driver;
import com.laytonsmith.core.events.EventUtils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.NotePlayEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;

public class BukkitBlockListener implements Listener {

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPistonExtend(final BlockPistonExtendEvent e) {
		BukkitBlockEvents.BukkitMCBlockPistonExtendEvent mce = new BukkitBlockEvents.BukkitMCBlockPistonExtendEvent(e);
		EventUtils.TriggerListener(Driver.PISTON_EXTEND, "piston_extend", mce);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPistonRetract(final BlockPistonRetractEvent e) {
		BukkitBlockEvents.BukkitMCBlockPistonRetractEvent mce =
				new BukkitBlockEvents.BukkitMCBlockPistonRetractEvent(e);
		EventUtils.TriggerListener(Driver.PISTON_RETRACT, "piston_retract", mce);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onSignChange(SignChangeEvent e) {
		BukkitBlockEvents.BukkitMCSignChangeEvent mce = new BukkitBlockEvents.BukkitMCSignChangeEvent(e);
		EventUtils.TriggerListener(Driver.SIGN_CHANGED, "sign_changed", mce);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onBlockPlace(BlockPlaceEvent e) {
		BukkitBlockEvents.BukkitMCBlockPlaceEvent bpe = new BukkitBlockEvents.BukkitMCBlockPlaceEvent(e);
		EventUtils.TriggerListener(Driver.BLOCK_PLACE, "block_place", bpe);
	}

	private static boolean ignorebreak = false;

	@EventHandler(priority = EventPriority.LOWEST)
	public void onBlockBreak(BlockBreakEvent e) {
		if(ignorebreak) {
			return;
		}
		BukkitBlockEvents.BukkitMCBlockBreakEvent bbe = new BukkitBlockEvents.BukkitMCBlockBreakEvent(e);
		EventUtils.TriggerListener(Driver.BLOCK_BREAK, "block_break", bbe);
		if(bbe.isModified() && !e.isCancelled()) {
			e.setCancelled(true);
			// If we've modified the drops, create a new event for other plugins (eg. block loggers, region protection)
			BlockBreakEvent chevent = new BlockBreakEvent(e.getBlock(), e.getPlayer());
			chevent.setExpToDrop(bbe.getExpToDrop());
			PluginManager manager = Bukkit.getServer().getPluginManager();
			ignorebreak = true;
			try {
				manager.callEvent(chevent);
			} finally {
				ignorebreak = false;
			}
			if(!chevent.isCancelled()) {
				Block block = chevent.getBlock();
				block.setType(Material.AIR);
				Location loc = block.getLocation();
				loc.add(0.5, 0.5, 0.5);
				for(MCItemStack item : bbe.getDrops()) {
					block.getWorld().dropItemNaturally(loc, (ItemStack) item.getHandle());
				}
				int amt = chevent.getExpToDrop();
				if(amt > 0) {
					ExperienceOrb exp = (ExperienceOrb) block.getWorld().spawnEntity(loc, EntityType.EXPERIENCE_ORB);
					exp.setExperience(amt);
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onBlockDispense(BlockDispenseEvent e) {
		BukkitBlockEvents.BukkitMCBlockDispenseEvent bde = new BukkitBlockEvents.BukkitMCBlockDispenseEvent(e);
		EventUtils.TriggerListener(Driver.BLOCK_DISPENSE, "block_dispense", bde);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onBlockBurn(BlockBurnEvent e) {
		BukkitBlockEvents.BukkitMCBlockBurnEvent bbe = new BukkitBlockEvents.BukkitMCBlockBurnEvent(e);
		EventUtils.TriggerListener(Driver.BLOCK_BURN, "block_burn", bbe);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onBlockFromTo(BlockFromToEvent e) {
		BukkitBlockEvents.BukkitMCBlockFromToEvent bbe = new BukkitBlockEvents.BukkitMCBlockFromToEvent(e);
		EventUtils.TriggerListener(Driver.BLOCK_FROM_TO, "block_from_to", bbe);
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

	@EventHandler(priority = EventPriority.LOWEST)
	public void onNotePlay(NotePlayEvent e) {
		BukkitBlockEvents.BukkitMCNotePlayEvent npe = new BukkitBlockEvents.BukkitMCNotePlayEvent(e);
		EventUtils.TriggerListener(Driver.NOTE_PLAY, "note_play", npe);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onBlockFade(BlockFadeEvent e) {
		BukkitBlockEvents.BukkitMCBlockFadeEvent bfe = new BukkitBlockEvents.BukkitMCBlockFadeEvent(e);
		EventUtils.TriggerListener(Driver.BLOCK_FADE, "block_fade", bfe);
	}
}
