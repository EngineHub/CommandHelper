package com.laytonsmith.abstraction.bukkit.events.drivers;

import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.bukkit.events.BukkitEntityEvents.BukkitEntityPotionEffectEvent;
import com.laytonsmith.abstraction.bukkit.events.BukkitEntityEvents.BukkitMCCreatureSpawnEvent;
import com.laytonsmith.abstraction.bukkit.events.BukkitEntityEvents.BukkitMCEntityChangeBlockEvent;
import com.laytonsmith.abstraction.bukkit.events.BukkitEntityEvents.BukkitMCEntityDamageByEntityEvent;
import com.laytonsmith.abstraction.bukkit.events.BukkitEntityEvents.BukkitMCEntityDamageEvent;
import com.laytonsmith.abstraction.bukkit.events.BukkitEntityEvents.BukkitMCEntityDeathEvent;
import com.laytonsmith.abstraction.bukkit.events.BukkitEntityEvents.BukkitMCEntityEnterPortalEvent;
import com.laytonsmith.abstraction.bukkit.events.BukkitEntityEvents.BukkitMCEntityExplodeEvent;
import com.laytonsmith.abstraction.bukkit.events.BukkitEntityEvents.BukkitMCEntityInteractEvent;
import com.laytonsmith.abstraction.bukkit.events.BukkitEntityEvents.BukkitMCEntityPortalEvent;
import com.laytonsmith.abstraction.bukkit.events.BukkitEntityEvents.BukkitMCEntityRegainHealthEvent;
import com.laytonsmith.abstraction.bukkit.events.BukkitEntityEvents.BukkitMCEntityToggleGlideEvent;
import com.laytonsmith.abstraction.bukkit.events.BukkitEntityEvents.BukkitMCEntityUnleashEvent;
import com.laytonsmith.abstraction.bukkit.events.BukkitEntityEvents.BukkitMCFireworkExplodeEvent;
import com.laytonsmith.abstraction.bukkit.events.BukkitEntityEvents.BukkitMCHangingBreakEvent;
import com.laytonsmith.abstraction.bukkit.events.BukkitEntityEvents.BukkitMCHangingPlaceEvent;
import com.laytonsmith.abstraction.bukkit.events.BukkitEntityEvents.BukkitMCItemDespawnEvent;
import com.laytonsmith.abstraction.bukkit.events.BukkitEntityEvents.BukkitMCItemSpawnEvent;
import com.laytonsmith.abstraction.bukkit.events.BukkitEntityEvents.BukkitMCPlayerDropItemEvent;
import com.laytonsmith.abstraction.bukkit.events.BukkitEntityEvents.BukkitMCPlayerInteractAtEntityEvent;
import com.laytonsmith.abstraction.bukkit.events.BukkitEntityEvents.BukkitMCPlayerInteractEntityEvent;
import com.laytonsmith.abstraction.bukkit.events.BukkitEntityEvents.BukkitMCPlayerPickupItemEvent;
import com.laytonsmith.abstraction.bukkit.events.BukkitEntityEvents.BukkitMCPotionSplashEvent;
import com.laytonsmith.abstraction.bukkit.events.BukkitEntityEvents.BukkitMCProjectileHitEvent;
import com.laytonsmith.abstraction.bukkit.events.BukkitEntityEvents.BukkitMCProjectileLaunchEvent;
import com.laytonsmith.abstraction.bukkit.events.BukkitEntityEvents.BukkitMCTargetEvent;
import com.laytonsmith.abstraction.bukkit.events.BukkitPlayerEvents;
import com.laytonsmith.commandhelper.CommandHelperPlugin;
import com.laytonsmith.core.events.Driver;
import com.laytonsmith.core.events.EventUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.entity.EntityUnleashEvent;
import org.bukkit.event.entity.FireworkExplodeEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class BukkitEntityListener implements Listener {

	@EventHandler(priority = EventPriority.LOWEST)
	public void onSpawn(CreatureSpawnEvent event) {
		BukkitMCCreatureSpawnEvent cse = new BukkitMCCreatureSpawnEvent(event);
		EventUtils.TriggerListener(Driver.CREATURE_SPAWN, "creature_spawn", cse, CommandHelperPlugin.getCore().getLastLoadedEnv());
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onClickEnt(PlayerInteractEntityEvent event) {
		BukkitMCPlayerInteractEntityEvent piee = new BukkitMCPlayerInteractEntityEvent(event);
		EventUtils.TriggerListener(Driver.PLAYER_INTERACT_ENTITY, "player_interact_entity", piee, CommandHelperPlugin.getCore().getLastLoadedEnv());
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onClickAtEnt(PlayerInteractAtEntityEvent event) {
		BukkitMCPlayerInteractAtEntityEvent piaee = new BukkitMCPlayerInteractAtEntityEvent(event);
		EventUtils.TriggerListener(Driver.PLAYER_INTERACT_AT_ENTITY, "player_interact_at_entity", piaee, CommandHelperPlugin.getCore().getLastLoadedEnv());
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onItemDrop(PlayerDropItemEvent event) {
		BukkitMCPlayerDropItemEvent pdie = new BukkitMCPlayerDropItemEvent(event);
		EventUtils.TriggerListener(Driver.ITEM_DROP, "item_drop", pdie, CommandHelperPlugin.getCore().getLastLoadedEnv());
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onItemPickup(EntityPickupItemEvent event) {
		if(((EntityEvent) event).getEntity() instanceof Player) {
			BukkitMCPlayerPickupItemEvent ppie = new BukkitMCPlayerPickupItemEvent(event);
			EventUtils.TriggerListener(Driver.ITEM_PICKUP, "item_pickup", ppie, CommandHelperPlugin.getCore().getLastLoadedEnv());
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onEntityDeath(EntityDeathEvent event) {
		BukkitMCEntityDeathEvent ede;
		if(event instanceof PlayerDeathEvent) {
			ede = new BukkitPlayerEvents.BukkitMCPlayerDeathEvent(event);
		} else {
			ede = new BukkitMCEntityDeathEvent(event);
		}
		EventUtils.TriggerListener(Driver.ENTITY_DEATH, "entity_death", ede, CommandHelperPlugin.getCore().getLastLoadedEnv());
		if(event instanceof PlayerDeathEvent) {
			EventUtils.TriggerListener(Driver.PLAYER_DEATH, "player_death", ede, CommandHelperPlugin.getCore().getLastLoadedEnv());
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onTargetLiving(EntityTargetEvent event) {
		BukkitMCTargetEvent ete = new BukkitMCTargetEvent(event);
		MCEntity target = ete.getTarget();
		if(target == null || !(target instanceof MCPlayer)) {
			return;
		}
		EventUtils.TriggerListener(Driver.TARGET_ENTITY, "target_player", ete, CommandHelperPlugin.getCore().getLastLoadedEnv());
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onEntityDamage(EntityDamageEvent event) {
		BukkitMCEntityDamageEvent ede;
		if(event instanceof EntityDamageByEntityEvent) {
			ede = new BukkitMCEntityDamageByEntityEvent(event);
			EventUtils.TriggerListener(Driver.ENTITY_DAMAGE, "entity_damage", ede, CommandHelperPlugin.getCore().getLastLoadedEnv());
			if(ede.getEntity() instanceof MCPlayer) {
				EventUtils.TriggerListener(Driver.ENTITY_DAMAGE_PLAYER, "entity_damage_player", ede, CommandHelperPlugin.getCore().getLastLoadedEnv());
			}
		} else {
			ede = new BukkitMCEntityDamageEvent(event);
			EventUtils.TriggerListener(Driver.ENTITY_DAMAGE, "entity_damage", ede, CommandHelperPlugin.getCore().getLastLoadedEnv());
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPHit(ProjectileHitEvent event) {
		BukkitMCProjectileHitEvent phe = new BukkitMCProjectileHitEvent(event);
		EventUtils.TriggerListener(Driver.PROJECTILE_HIT, "projectile_hit", phe, CommandHelperPlugin.getCore().getLastLoadedEnv());
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onProjectileLaunch(ProjectileLaunchEvent event) {
		BukkitMCProjectileLaunchEvent ple = new BukkitMCProjectileLaunchEvent(event);
		EventUtils.TriggerListener(Driver.PROJECTILE_LAUNCH, "projectile_launch", ple, CommandHelperPlugin.getCore().getLastLoadedEnv());
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPortalEnter(EntityPortalEnterEvent event) {
		BukkitMCEntityEnterPortalEvent pe = new BukkitMCEntityEnterPortalEvent(event);
		EventUtils.TriggerListener(Driver.ENTITY_ENTER_PORTAL, "entity_enter_portal", pe, CommandHelperPlugin.getCore().getLastLoadedEnv());
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onExplode(EntityExplodeEvent event) {
		BukkitMCEntityExplodeEvent ee = new BukkitMCEntityExplodeEvent(event);
		EventUtils.TriggerListener(Driver.ENTITY_EXPLODE, "entity_explode", ee, CommandHelperPlugin.getCore().getLastLoadedEnv());
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onItemDespawn(ItemDespawnEvent event) {
		BukkitMCItemDespawnEvent id = new BukkitMCItemDespawnEvent(event);
		EventUtils.TriggerListener(Driver.ITEM_DESPAWN, "item_despawn", id, CommandHelperPlugin.getCore().getLastLoadedEnv());
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onItemSpawn(ItemSpawnEvent event) {
		BukkitMCItemSpawnEvent is = new BukkitMCItemSpawnEvent(event);
		EventUtils.TriggerListener(Driver.ITEM_SPAWN, "item_spawn", is, CommandHelperPlugin.getCore().getLastLoadedEnv());
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onChangeBlock(EntityChangeBlockEvent event) {
		BukkitMCEntityChangeBlockEvent ecbe = new BukkitMCEntityChangeBlockEvent(event);
		EventUtils.TriggerListener(Driver.ENTITY_CHANGE_BLOCK, "entity_change_block", ecbe, CommandHelperPlugin.getCore().getLastLoadedEnv());
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onInteract(EntityInteractEvent event) {
		BukkitMCEntityInteractEvent eie = new BukkitMCEntityInteractEvent(event);
		EventUtils.TriggerListener(Driver.ENTITY_INTERACT, "entity_interact", eie, CommandHelperPlugin.getCore().getLastLoadedEnv());
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onHangingBreak(HangingBreakEvent event) {
		BukkitMCHangingBreakEvent hbe = new BukkitMCHangingBreakEvent(event);
		EventUtils.TriggerListener(Driver.HANGING_BREAK, "hanging_break", hbe, CommandHelperPlugin.getCore().getLastLoadedEnv());
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onHangingPlace(HangingPlaceEvent event) {
		BukkitMCHangingPlaceEvent hpe = new BukkitMCHangingPlaceEvent(event);
		EventUtils.TriggerListener(Driver.HANGING_PLACE, "hanging_place", hpe, CommandHelperPlugin.getCore().getLastLoadedEnv());
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onEntityToggleGlide(EntityToggleGlideEvent event) {
		BukkitMCEntityToggleGlideEvent etge = new BukkitMCEntityToggleGlideEvent(event);
		EventUtils.TriggerListener(Driver.ENTITY_TOGGLE_GLIDE, "entity_toggle_glide", etge, CommandHelperPlugin.getCore().getLastLoadedEnv());
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onFireworkExplode(FireworkExplodeEvent event) {
		BukkitMCFireworkExplodeEvent fee = new BukkitMCFireworkExplodeEvent(event);
		EventUtils.TriggerListener(Driver.FIREWORK_EXPLODE, "firework_explode", fee, CommandHelperPlugin.getCore().getLastLoadedEnv());
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onRegainHealth(EntityRegainHealthEvent event) {
		BukkitMCEntityRegainHealthEvent erhe = new BukkitMCEntityRegainHealthEvent(event);
		EventUtils.TriggerListener(Driver.ENTITY_REGAIN_HEALTH, "entity_regain_health", erhe, CommandHelperPlugin.getCore().getLastLoadedEnv());
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPortalTravel(EntityPortalEvent event) {
		BukkitMCEntityPortalEvent epe = new BukkitMCEntityPortalEvent(event);
		EventUtils.TriggerListener(Driver.ENTITY_PORTAL_TRAVEL, "entity_portal_travel", epe, CommandHelperPlugin.getCore().getLastLoadedEnv());
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onEntityUnleash(EntityUnleashEvent event) {
		BukkitMCEntityUnleashEvent epe = new BukkitMCEntityUnleashEvent(event);
		EventUtils.TriggerListener(Driver.ENTITY_UNLEASH, "entity_unleash", epe, CommandHelperPlugin.getCore().getLastLoadedEnv());
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPotionSplash(PotionSplashEvent event) {
		BukkitMCPotionSplashEvent pse = new BukkitMCPotionSplashEvent(event);
		EventUtils.TriggerListener(Driver.POTION_SPLASH, "potion_splash", pse, CommandHelperPlugin.getCore().getLastLoadedEnv());
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onEntityPotionEffectEvent(EntityPotionEffectEvent event) {
		BukkitEntityPotionEffectEvent potion = new BukkitEntityPotionEffectEvent(event);
		EventUtils.TriggerListener(Driver.ENTITY_POTION_EFFECT, "entity_potion", potion, CommandHelperPlugin.getCore().getLastLoadedEnv());
	}
}
