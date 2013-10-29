package com.laytonsmith.abstraction.bukkit.events.drivers;

import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.bukkit.events.BukkitEntityEvents;
import com.laytonsmith.abstraction.bukkit.events.BukkitPlayerEvents;
import com.laytonsmith.core.events.Driver;
import com.laytonsmith.core.events.EventUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;

/**
 *
 * @author Layton
 */
public class BukkitEntityListener implements Listener{

	@EventHandler(priority=EventPriority.LOWEST)
	public void onSpawn(CreatureSpawnEvent event) {
		BukkitEntityEvents.BukkitMCCreatureSpawnEvent cse = new BukkitEntityEvents.BukkitMCCreatureSpawnEvent(event);
		EventUtils.TriggerExternal(cse);
		EventUtils.TriggerListener(Driver.CREATURE_SPAWN, "creature_spawn", cse);
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onClickEnt(PlayerInteractEntityEvent event) {
		BukkitEntityEvents.BukkitMCPlayerInteractEntityEvent piee = new BukkitEntityEvents.BukkitMCPlayerInteractEntityEvent(event);
		EventUtils.TriggerExternal(piee);
		EventUtils.TriggerListener(Driver.PLAYER_INTERACT_ENTITY, "player_interact_entity", piee);
	}
	
    @EventHandler(priority=EventPriority.LOWEST)
    public void onItemDrop(PlayerDropItemEvent event) {
		BukkitEntityEvents.BukkitMCPlayerDropItemEvent pdie = new BukkitEntityEvents.BukkitMCPlayerDropItemEvent(event);
        EventUtils.TriggerExternal(pdie);
		EventUtils.TriggerListener(Driver.ITEM_DROP, "item_drop", pdie);
    }
    
	@EventHandler(priority=EventPriority.LOWEST)
    public void onItemPickup(PlayerPickupItemEvent event) {
		BukkitEntityEvents.BukkitMCPlayerPickupItemEvent ppie = new BukkitEntityEvents.BukkitMCPlayerPickupItemEvent(event);
		EventUtils.TriggerExternal(ppie);
		EventUtils.TriggerListener(Driver.ITEM_PICKUP, "item_pickup", ppie);
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onEntityDeath(EntityDeathEvent event) {
		BukkitEntityEvents.BukkitMCEntityDeathEvent ede;
		if (event instanceof PlayerDeathEvent) {
			ede = new BukkitPlayerEvents.BukkitMCPlayerDeathEvent((PlayerDeathEvent) event);
		} else {
			ede = new BukkitEntityEvents.BukkitMCEntityDeathEvent(event);
		}
		EventUtils.TriggerExternal(ede);
		EventUtils.TriggerListener(Driver.ENTITY_DEATH, "entity_death", ede);
		if (event instanceof PlayerDeathEvent) {
			EventUtils.TriggerListener(Driver.PLAYER_DEATH, "player_death", ede);
		}
	}
    
    @EventHandler(priority=EventPriority.LOWEST)
    public void onTargetLiving(EntityTargetEvent event) {
		BukkitEntityEvents.BukkitMCTargetEvent ete = new BukkitEntityEvents.BukkitMCTargetEvent(event);
		EventUtils.TriggerExternal(ete);
        EventUtils.TriggerListener(Driver.TARGET_ENTITY, "target_player", ete);
    }
    
	@EventHandler(priority=EventPriority.LOWEST)
	public void onEntityDamage(EntityDamageEvent event) {
		BukkitEntityEvents.BukkitMCEntityDamageEvent ede;
		if (event instanceof EntityDamageByEntityEvent) {
			ede = new BukkitEntityEvents.BukkitMCEntityDamageByEntityEvent((EntityDamageByEntityEvent) event);
			EventUtils.TriggerExternal(ede);
			EventUtils.TriggerListener(Driver.ENTITY_DAMAGE, "entity_damage", ede);
			if (ede.getEntity() instanceof MCPlayer) {
				EventUtils.TriggerListener(Driver.ENTITY_DAMAGE_PLAYER, "entity_damage_player", ede);
			}
		} else {
			ede = new BukkitEntityEvents.BukkitMCEntityDamageEvent(event);
			EventUtils.TriggerExternal(ede);
			EventUtils.TriggerListener(Driver.ENTITY_DAMAGE, "entity_damage", ede);
		}
	}

	@EventHandler(priority=EventPriority.LOWEST)
	public void onPHit(ProjectileHitEvent event) {
		BukkitEntityEvents.BukkitMCProjectileHitEvent phe = 
				new BukkitEntityEvents.BukkitMCProjectileHitEvent(event);
		EventUtils.TriggerExternal(phe);
		EventUtils.TriggerListener(Driver.PROJECTILE_HIT, "projectile_hit", phe);
	}

	@EventHandler(priority=EventPriority.LOWEST)
	public void onProjectileLaunch(ProjectileLaunchEvent event) {
		BukkitEntityEvents.BukkitMCProjectileLaunchEvent ple =  new BukkitEntityEvents.BukkitMCProjectileLaunchEvent(event);
		EventUtils.TriggerExternal(ple);
		EventUtils.TriggerListener(Driver.PROJECTILE_LAUNCH, "projectile_launch", ple);
	}

	@EventHandler(priority= EventPriority.LOWEST)
	public void onPortalEnter(EntityPortalEnterEvent event) {
		BukkitEntityEvents.BukkitMCEntityEnterPortalEvent pe = new BukkitEntityEvents.BukkitMCEntityEnterPortalEvent(event);
		EventUtils.TriggerExternal(pe);
		EventUtils.TriggerListener(Driver.ENTITY_ENTER_PORTAL, "entity_enter_portal", pe);
	}
	
	@EventHandler(priority= EventPriority.LOWEST)
	public void onExplode(EntityExplodeEvent event) {
		BukkitEntityEvents.BukkitMCEntityExplodeEvent ee = new BukkitEntityEvents.BukkitMCEntityExplodeEvent(event);
		EventUtils.TriggerExternal(ee);
		EventUtils.TriggerListener(Driver.ENTITY_EXPLODE, "entity_explode", ee);
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onItemSpawn(ItemSpawnEvent event) {
		BukkitEntityEvents.BukkitMCItemSpawnEvent is = new BukkitEntityEvents.BukkitMCItemSpawnEvent(event);
		EventUtils.TriggerExternal(is);
		EventUtils.TriggerListener(Driver.ITEM_SPAWN, "item_spawn", is);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onChangeBlock(EntityChangeBlockEvent event) {
		BukkitEntityEvents.BukkitMCEntityChangeBlockEvent ecbe = new BukkitEntityEvents.BukkitMCEntityChangeBlockEvent(event);
		EventUtils.TriggerExternal(ecbe);
		EventUtils.TriggerListener(Driver.ENTITY_CHANGE_BLOCK, "entity_change_block", ecbe);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onHangingBreak(HangingBreakEvent event) {
		BukkitEntityEvents.BukkitMCHangingBreakEvent hbe = new BukkitEntityEvents.BukkitMCHangingBreakEvent(event);
		EventUtils.TriggerExternal(hbe);
		EventUtils.TriggerListener(Driver.HANGING_BREAK, "hanging_break", hbe);
	}
}
