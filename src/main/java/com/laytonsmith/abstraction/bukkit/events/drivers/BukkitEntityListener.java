package com.laytonsmith.abstraction.bukkit.events.drivers;

import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.bukkit.events.BukkitEntityEvents.BukkitMCCreatureSpawnEvent;
import com.laytonsmith.abstraction.bukkit.events.BukkitEntityEvents.BukkitMCEntityChangeBlockEvent;
import com.laytonsmith.abstraction.bukkit.events.BukkitEntityEvents.BukkitMCEntityDamageByEntityEvent;
import com.laytonsmith.abstraction.bukkit.events.BukkitEntityEvents.BukkitMCEntityDamageEvent;
import com.laytonsmith.abstraction.bukkit.events.BukkitEntityEvents.BukkitMCEntityDeathEvent;
import com.laytonsmith.abstraction.bukkit.events.BukkitEntityEvents.BukkitMCEntityEnterPortalEvent;
import com.laytonsmith.abstraction.bukkit.events.BukkitEntityEvents.BukkitMCEntityExplodeEvent;
import com.laytonsmith.abstraction.bukkit.events.BukkitEntityEvents.BukkitMCEntityInteractEvent;
import com.laytonsmith.abstraction.bukkit.events.BukkitEntityEvents.BukkitMCHangingBreakEvent;
import com.laytonsmith.abstraction.bukkit.events.BukkitEntityEvents.BukkitMCItemDespawnEvent;
import com.laytonsmith.abstraction.bukkit.events.BukkitEntityEvents.BukkitMCItemSpawnEvent;
import com.laytonsmith.abstraction.bukkit.events.BukkitEntityEvents.BukkitMCPlayerDropItemEvent;
import com.laytonsmith.abstraction.bukkit.events.BukkitEntityEvents.BukkitMCPlayerInteractAtEntityEvent;
import com.laytonsmith.abstraction.bukkit.events.BukkitEntityEvents.BukkitMCPlayerInteractEntityEvent;
import com.laytonsmith.abstraction.bukkit.events.BukkitEntityEvents.BukkitMCPlayerPickupItemEvent;
import com.laytonsmith.abstraction.bukkit.events.BukkitEntityEvents.BukkitMCProjectileHitEvent;
import com.laytonsmith.abstraction.bukkit.events.BukkitEntityEvents.BukkitMCProjectileLaunchEvent;
import com.laytonsmith.abstraction.bukkit.events.BukkitEntityEvents.BukkitMCTargetEvent;
import com.laytonsmith.abstraction.bukkit.events.BukkitPlayerEvents;
import com.laytonsmith.annotations.EventIdentifier;
import com.laytonsmith.core.events.Driver;
import com.laytonsmith.core.events.EventUtils;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

/**
 *
 * 
 */
public class BukkitEntityListener implements Listener {

	@EventIdentifier(event = Driver.CREATURE_SPAWN, className = "org.bukkit.event.entity.CreatureSpawnEvent")
	public void onSpawn(Event event) {
		BukkitMCCreatureSpawnEvent cse = new BukkitMCCreatureSpawnEvent(event);
		EventUtils.TriggerListener(Driver.CREATURE_SPAWN, "creature_spawn", cse);
	}

	@EventIdentifier(event = Driver.PLAYER_INTERACT_ENTITY,
			className = "org.bukkit.event.player.PlayerInteractEntityEvent")
	public void onClickEnt(Event event) {
		BukkitMCPlayerInteractEntityEvent piee = new BukkitMCPlayerInteractEntityEvent(event);
		EventUtils.TriggerListener(Driver.PLAYER_INTERACT_ENTITY, "player_interact_entity", piee);
	}

	@EventIdentifier(event = Driver.PLAYER_INTERACT_AT_ENTITY,
			className = "org.bukkit.event.player.PlayerInteractAtEntityEvent")
	public void onClickAtEnt(Event event) {
		BukkitMCPlayerInteractAtEntityEvent piaee = new BukkitMCPlayerInteractAtEntityEvent(event);
		EventUtils.TriggerListener(Driver.PLAYER_INTERACT_AT_ENTITY, "player_interact_at_entity", piaee);
	}

	@EventIdentifier(event = Driver.ITEM_DROP, className = "org.bukkit.event.player.PlayerDropItemEvent")
	public void onItemDrop(Event event) {
		BukkitMCPlayerDropItemEvent pdie = new BukkitMCPlayerDropItemEvent(event);
		EventUtils.TriggerListener(Driver.ITEM_DROP, "item_drop", pdie);
    }

	@EventIdentifier(event = Driver.ITEM_PICKUP, className = "org.bukkit.event.player.PlayerPickupItemEvent")
	public void onItemPickup(Event event) {
		BukkitMCPlayerPickupItemEvent ppie = new BukkitMCPlayerPickupItemEvent(event);
		EventUtils.TriggerListener(Driver.ITEM_PICKUP, "item_pickup", ppie);
	}

	@EventIdentifier(event = Driver.ENTITY_DEATH, className = "org.bukkit.event.entity.EntityDeathEvent")
	public void onEntityDeath(Event event) {
		BukkitMCEntityDeathEvent ede;
		if (event instanceof PlayerDeathEvent) {
			ede = new BukkitPlayerEvents.BukkitMCPlayerDeathEvent(event);
		} else {
			ede = new BukkitMCEntityDeathEvent(event);
		}
		EventUtils.TriggerListener(Driver.ENTITY_DEATH, "entity_death", ede);
		if (event instanceof PlayerDeathEvent) {
			EventUtils.TriggerListener(Driver.PLAYER_DEATH, "player_death", ede);
		}
	}

	@EventIdentifier(event = Driver.TARGET_ENTITY, className = "org.bukkit.event.entity.EntityTargetEvent")
	public void onTargetLiving(Event event) {
		BukkitMCTargetEvent ete = new BukkitMCTargetEvent(event);
        EventUtils.TriggerListener(Driver.TARGET_ENTITY, "target_player", ete);
    }

	@EventIdentifier(event = Driver.ENTITY_DAMAGE, className = "org.bukkit.event.entity.EntityDamageEvent")
	public void onEntityDamage(Event event) {
		BukkitMCEntityDamageEvent ede;
		if (event instanceof EntityDamageByEntityEvent) {
			ede = new BukkitMCEntityDamageByEntityEvent(event);
			EventUtils.TriggerListener(Driver.ENTITY_DAMAGE, "entity_damage", ede);
			if (ede.getEntity() instanceof MCPlayer) {
				EventUtils.TriggerListener(Driver.ENTITY_DAMAGE_PLAYER, "entity_damage_player", ede);
			}
		} else {
			ede = new BukkitMCEntityDamageEvent(event);
			EventUtils.TriggerListener(Driver.ENTITY_DAMAGE, "entity_damage", ede);
		}
	}

	@EventIdentifier(event = Driver.PROJECTILE_HIT, className = "org.bukkit.event.entity.ProjectileHitEvent")
	public void onPHit(Event event) {
		BukkitMCProjectileHitEvent phe = new BukkitMCProjectileHitEvent(event);
		EventUtils.TriggerExternal(phe);
		EventUtils.TriggerListener(Driver.PROJECTILE_HIT, "projectile_hit", phe);
	}

	@EventIdentifier(event = Driver.PROJECTILE_LAUNCH, className = "org.bukkit.event.entity.ProjectileLaunchEvent")
	public void onProjectileLaunch(Event event) {
		BukkitMCProjectileLaunchEvent ple = new BukkitMCProjectileLaunchEvent(event);
		EventUtils.TriggerListener(Driver.PROJECTILE_LAUNCH, "projectile_launch", ple);
	}

	@EventIdentifier(event = Driver.ENTITY_ENTER_PORTAL, className = "org.bukkit.event.entity.EntityPortalEnterEvent")
	public void onPortalEnter(Event event) {
		BukkitMCEntityEnterPortalEvent pe = new BukkitMCEntityEnterPortalEvent(event);
		EventUtils.TriggerListener(Driver.ENTITY_ENTER_PORTAL, "entity_enter_portal", pe);
	}

	@EventIdentifier(event = Driver.ENTITY_EXPLODE, className = "org.bukkit.event.entity.EntityExplodeEvent")
	public void onExplode(Event event) {
		BukkitMCEntityExplodeEvent ee = new BukkitMCEntityExplodeEvent(event);
		EventUtils.TriggerListener(Driver.ENTITY_EXPLODE, "entity_explode", ee);
	}

	@EventIdentifier(event = Driver.ITEM_DESPAWN, className = "org.bukkit.event.entity.ItemDespawnEvent")
	public void onItemDespawn(Event event) {
		BukkitMCItemDespawnEvent id = new BukkitMCItemDespawnEvent(event);
		EventUtils.TriggerListener(Driver.ITEM_DESPAWN, "item_despawn", id);
	}

	@EventIdentifier(event = Driver.ITEM_SPAWN, className = "org.bukkit.event.entity.ItemSpawnEvent")
	public void onItemSpawn(Event event) {
		BukkitMCItemSpawnEvent is = new BukkitMCItemSpawnEvent(event);
		EventUtils.TriggerListener(Driver.ITEM_SPAWN, "item_spawn", is);
	}

	@EventIdentifier(event = Driver.ENTITY_CHANGE_BLOCK, className = "org.bukkit.event.entity.EntityChangeBlockEvent")
	public void onChangeBlock(Event event) {
		BukkitMCEntityChangeBlockEvent ecbe = new BukkitMCEntityChangeBlockEvent(event);
		EventUtils.TriggerListener(Driver.ENTITY_CHANGE_BLOCK, "entity_change_block", ecbe);
	}

	@EventIdentifier(event = Driver.ENTITY_INTERACT, className = "org.bukkit.event.entity.EntityInteractEvent")
	public void onInteract(Event event) {
		BukkitMCEntityInteractEvent eie = new BukkitMCEntityInteractEvent(event);
		EventUtils.TriggerListener(Driver.ENTITY_INTERACT, "entity_interact", eie);
	}

	@EventIdentifier(event = Driver.HANGING_BREAK, className = "org.bukkit.event.hanging.HangingBreakEvent")
	public void onHangingBreak(Event event) {
		BukkitMCHangingBreakEvent hbe = new BukkitMCHangingBreakEvent(event);
		EventUtils.TriggerListener(Driver.HANGING_BREAK, "hanging_break", hbe);
	}
}
