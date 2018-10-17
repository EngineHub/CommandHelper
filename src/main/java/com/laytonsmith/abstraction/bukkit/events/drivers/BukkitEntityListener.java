package com.laytonsmith.abstraction.bukkit.events.drivers;

import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.bukkit.events.BukkitEntityEvents.BukkitMCCreatureSpawnEvent;
import com.laytonsmith.abstraction.bukkit.events.BukkitEntityEvents.BukkitMCEntityChangeBlockEvent;
import com.laytonsmith.abstraction.bukkit.events.BukkitEntityEvents.BukkitMCEntityDamageEvent;
import com.laytonsmith.abstraction.bukkit.events.BukkitEntityEvents.BukkitMCEntityDamageByEntityEvent;
import com.laytonsmith.abstraction.bukkit.events.BukkitEntityEvents.BukkitMCEntityDeathEvent;
import com.laytonsmith.abstraction.bukkit.events.BukkitEntityEvents.BukkitMCEntityEnterPortalEvent;
import com.laytonsmith.abstraction.bukkit.events.BukkitEntityEvents.BukkitMCEntityExplodeEvent;
import com.laytonsmith.abstraction.bukkit.events.BukkitEntityEvents.BukkitMCEntityInteractEvent;
import com.laytonsmith.abstraction.bukkit.events.BukkitEntityEvents.BukkitMCEntityPortalEvent;
import com.laytonsmith.abstraction.bukkit.events.BukkitEntityEvents.BukkitMCEntityRegainHealthEvent;
import com.laytonsmith.abstraction.bukkit.events.BukkitEntityEvents.BukkitMCEntityToggleGlideEvent;
import com.laytonsmith.abstraction.bukkit.events.BukkitEntityEvents.BukkitMCFireworkExplodeEvent;
import com.laytonsmith.abstraction.bukkit.events.BukkitEntityEvents.BukkitMCHangingBreakEvent;
import com.laytonsmith.abstraction.bukkit.events.BukkitEntityEvents.BukkitMCItemDespawnEvent;
import com.laytonsmith.abstraction.bukkit.events.BukkitEntityEvents.BukkitMCItemSpawnEvent;
import com.laytonsmith.abstraction.bukkit.events.BukkitEntityEvents.BukkitMCPlayerDropItemEvent;
import com.laytonsmith.abstraction.bukkit.events.BukkitEntityEvents.BukkitMCPlayerInteractAtEntityEvent;
import com.laytonsmith.abstraction.bukkit.events.BukkitEntityEvents.BukkitMCPlayerInteractEntityEvent;
import com.laytonsmith.abstraction.bukkit.events.BukkitEntityEvents.BukkitMCPlayerPickupItemEvent;
import com.laytonsmith.abstraction.bukkit.events.BukkitEntityEvents.BukkitMCProjectileHitEvent;
import com.laytonsmith.abstraction.bukkit.events.BukkitEntityEvents.BukkitMCProjectileLaunchEvent;
import com.laytonsmith.abstraction.bukkit.events.BukkitEntityEvents.BukkitMCAreaEffectCloudApplyEvent;
import com.laytonsmith.abstraction.bukkit.events.BukkitEntityEvents.BukkitMCCreeperPowerEvent;
import com.laytonsmith.abstraction.bukkit.events.BukkitEntityEvents.BukkitMCEnderdragonChangePhaseEvent;
import com.laytonsmith.abstraction.bukkit.events.BukkitEntityEvents.BukkitMCEntityAirChangeEvent;
import com.laytonsmith.abstraction.bukkit.events.BukkitEntityEvents.BukkitMCEntityBreedEvent;
import com.laytonsmith.abstraction.bukkit.events.BukkitEntityEvents.BukkitMCEntityCreatePortalEvent;
import com.laytonsmith.abstraction.bukkit.events.BukkitEntityEvents.BukkitMCEntityDropItemEvent;
import com.laytonsmith.abstraction.bukkit.events.BukkitEntityEvents.BukkitMCEntityResurrectEvent;
import com.laytonsmith.abstraction.bukkit.events.BukkitEntityEvents.BukkitMCEntityShootBowEvent;
import com.laytonsmith.abstraction.bukkit.events.BukkitEntityEvents.BukkitMCEntityTameEvent;
import com.laytonsmith.abstraction.bukkit.events.BukkitEntityEvents.BukkitMCEntityTeleportEvent;
import com.laytonsmith.abstraction.bukkit.events.BukkitEntityEvents.BukkitMCEntityToggleSwimEvent;
import com.laytonsmith.abstraction.bukkit.events.BukkitEntityEvents.BukkitMCEntityUnleashEvent;
import com.laytonsmith.abstraction.bukkit.events.BukkitEntityEvents.BukkitMCExplosionPrimeEvent;
import com.laytonsmith.abstraction.bukkit.events.BukkitEntityEvents.BukkitMCHorseJumpEvent;
import com.laytonsmith.abstraction.bukkit.events.BukkitEntityEvents.BukkitMCItemMergeEvent;
import com.laytonsmith.abstraction.bukkit.events.BukkitEntityEvents.BukkitMCPigZapEvent;
import com.laytonsmith.abstraction.bukkit.events.BukkitEntityEvents.BukkitMCPigZombieAngerEvent;
import com.laytonsmith.abstraction.bukkit.events.BukkitEntityEvents.BukkitMCSheepDyeWoolEvent;
import com.laytonsmith.abstraction.bukkit.events.BukkitEntityEvents.BukkitMCSheepRegrowWoolEvent;
import com.laytonsmith.abstraction.bukkit.events.BukkitEntityEvents.BukkitMCSlimeSplitEvent;
import com.laytonsmith.abstraction.bukkit.events.BukkitEntityEvents.BukkitMCVillagerAcquireTradeEvent;
import com.laytonsmith.abstraction.bukkit.events.BukkitEntityEvents.BukkitMCVillagerReplenishTradeEvent;
import com.laytonsmith.abstraction.bukkit.events.BukkitEntityEvents.BukkitMCTargetEvent;
import com.laytonsmith.abstraction.bukkit.events.BukkitPlayerEvents;
import com.laytonsmith.annotations.EventIdentifier;
import com.laytonsmith.core.events.Driver;
import com.laytonsmith.core.events.EventUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

public class BukkitEntityListener implements Listener {

	@EventIdentifier(event = Driver.CREATURE_SPAWN, className = "org.bukkit.event.entity.CreatureSpawnEvent")
	public void onSpawn(Event event) {
		BukkitMCCreatureSpawnEvent cse = new BukkitMCCreatureSpawnEvent(event);
		EventUtils.TriggerListener(Driver.CREATURE_SPAWN, "creature_spawn", cse);
	}

	@EventIdentifier(event = Driver.PLAYER_INTERACT_ENTITY, className = "org.bukkit.event.player.PlayerInteractEntityEvent")
	public void onClickEnt(Event event) {
		BukkitMCPlayerInteractEntityEvent piee = new BukkitMCPlayerInteractEntityEvent(event);
		EventUtils.TriggerListener(Driver.PLAYER_INTERACT_ENTITY, "player_interact_entity", piee);
	}

	@EventIdentifier(event = Driver.PLAYER_INTERACT_AT_ENTITY, className = "org.bukkit.event.player.PlayerInteractAtEntityEvent")
	public void onClickAtEnt(Event event) {
		BukkitMCPlayerInteractAtEntityEvent piaee = new BukkitMCPlayerInteractAtEntityEvent(event);
		EventUtils.TriggerListener(Driver.PLAYER_INTERACT_AT_ENTITY, "player_interact_at_entity", piaee);
	}

	@EventIdentifier(event = Driver.ITEM_DROP, className = "org.bukkit.event.player.PlayerDropItemEvent")
	public void onItemDrop(Event event) {
		BukkitMCPlayerDropItemEvent pdie = new BukkitMCPlayerDropItemEvent(event);
		EventUtils.TriggerListener(Driver.ITEM_DROP, "item_drop", pdie);
	}

	@EventIdentifier(event = Driver.ITEM_PICKUP, className = "org.bukkit.event.entity.EntityPickupItemEvent")
	public void onItemPickup(Event event) {
		if(((EntityEvent) event).getEntity() instanceof Player) {
			BukkitMCPlayerPickupItemEvent ppie = new BukkitMCPlayerPickupItemEvent(event);
			EventUtils.TriggerListener(Driver.ITEM_PICKUP, "item_pickup", ppie);
		}
	}

	@EventIdentifier(event = Driver.ENTITY_DEATH, className = "org.bukkit.event.entity.EntityDeathEvent")
	public void onEntityDeath(Event event) {
		BukkitMCEntityDeathEvent ede;
		if(event instanceof PlayerDeathEvent) {
			ede = new BukkitPlayerEvents.BukkitMCPlayerDeathEvent(event);
		} else {
			ede = new BukkitMCEntityDeathEvent(event);
		}
		EventUtils.TriggerListener(Driver.ENTITY_DEATH, "entity_death", ede);
		if(event instanceof PlayerDeathEvent) {
			EventUtils.TriggerListener(Driver.PLAYER_DEATH, "player_death", ede);
		}
	}

	@EventIdentifier(event = Driver.TARGET_ENTITY, className = "org.bukkit.event.entity.EntityTargetEvent")
	public void onTargetLiving(Event event) {
		BukkitMCTargetEvent ete = new BukkitMCTargetEvent(event);
		MCEntity target = ete.getTarget();
		if(target == null || !(target instanceof MCPlayer)) {
			return;
		}
		EventUtils.TriggerListener(Driver.TARGET_ENTITY, "target_player", ete);
	}

	@EventIdentifier(event = Driver.ENTITY_DAMAGE, className = "org.bukkit.event.entity.EntityDamageEvent")
	public void onEntityDamage(Event event) {
		BukkitMCEntityDamageEvent ede;
		if(event instanceof EntityDamageByEntityEvent) {
			ede = new BukkitMCEntityDamageByEntityEvent(event);
			EventUtils.TriggerListener(Driver.ENTITY_DAMAGE, "entity_damage", ede);
			if(ede.getEntity() instanceof MCPlayer) {
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

	@EventIdentifier(event = Driver.ENTITY_TOGGLE_GLIDE, className = "org.bukkit.event.entity.EntityToggleGlideEvent")
	public void onEntityToggleGlide(Event event) {
		BukkitMCEntityToggleGlideEvent etge = new BukkitMCEntityToggleGlideEvent(event);
		EventUtils.TriggerListener(Driver.ENTITY_TOGGLE_GLIDE, "entity_toggle_glide", etge);
	}

	@EventIdentifier(event = Driver.FIREWORK_EXPLODE, className = "org.bukkit.event.entity.FireworkExplodeEvent")
	public void onFireworkExplode(Event event) {
		BukkitMCFireworkExplodeEvent fee = new BukkitMCFireworkExplodeEvent(event);
		EventUtils.TriggerListener(Driver.FIREWORK_EXPLODE, "firework_explode", fee);
	}

	@EventIdentifier(event = Driver.ENTITY_REGAIN_HEALTH, className = "org.bukkit.event.entity.EntityRegainHealthEvent")
	public void onRegainHealth(Event event) {
		BukkitMCEntityRegainHealthEvent erhe = new BukkitMCEntityRegainHealthEvent(event);
		EventUtils.TriggerListener(Driver.ENTITY_REGAIN_HEALTH, "entity_regain_health", erhe);
	}

	@EventIdentifier(event = Driver.ENTITY_PORTAL_TRAVEL, className = "org.bukkit.event.entity.EntityPortalEvent")
	public void onPortalTravel(Event event) {
		BukkitMCEntityPortalEvent epe = new BukkitMCEntityPortalEvent(event);
		EventUtils.TriggerListener(Driver.ENTITY_PORTAL_TRAVEL, "entity_portal_travel", epe);
	}

	@EventIdentifier(event = Driver.AREA_EFFECT_CLOUD_APPLY, className = "org.bukkit.event.entity.AreaEffectCloudApplyEvent")
	public void onAreaEffectCloudApply(Event event) {
		BukkitMCAreaEffectCloudApplyEvent aeca = new BukkitMCAreaEffectCloudApplyEvent(event);
		EventUtils.TriggerListener(Driver.AREA_EFFECT_CLOUD_APPLY, "area_effect_cloud_apply", aeca);
	}

	@EventIdentifier(event = Driver.CREEPER_POWER, className = "org.bukkit.event.entity.CreeperPowerEvent")
	public void onCreeperPower(Event event) {
		BukkitMCCreeperPowerEvent cpe = new BukkitMCCreeperPowerEvent(event);
		EventUtils.TriggerListener(Driver.CREEPER_POWER, "creeper_power", cpe);
	}

	@EventIdentifier(event = Driver.ENDERDRAGON_CHANGE_PAHSE, className = "org.bukkit.event.entity.EnderDragonChangePhaseEvent")
	public void onEnderdragonChangePhase(Event event) {
		BukkitMCEnderdragonChangePhaseEvent ecpe = new BukkitMCEnderdragonChangePhaseEvent(event);
		EventUtils.TriggerListener(Driver.ENDERDRAGON_CHANGE_PAHSE, "enderdragon_change_phase", ecpe);
	}

	@EventIdentifier(event = Driver.ENTITY_AIR_CHANGE, className = "org.bukkit.event.entity.EntityAirChangeEvent")
	public void onEntityAirChange(Event event) {
		BukkitMCEntityAirChangeEvent eace = new BukkitMCEntityAirChangeEvent(event);
		EventUtils.TriggerListener(Driver.ENTITY_AIR_CHANGE, "entity_air_change", eace);
	}

	@EventIdentifier(event = Driver.ENTITY_BREED, className = "org.bukkit.event.entity.EntityBreedEvent")
	public void onEntityBreed(Event event) {
		BukkitMCEntityBreedEvent ebe = new BukkitMCEntityBreedEvent(event);
		EventUtils.TriggerListener(Driver.ENTITY_BREED, "entity_breed", ebe);
	}

	@EventIdentifier(event = Driver.ENTITY_CREATE_PORTAL, className = "org.bukkit.event.entity.EntityCreatePortalEvent")
	public void onEntityCreatePortal(Event event) {
		BukkitMCEntityCreatePortalEvent ecpe = new BukkitMCEntityCreatePortalEvent(event);
		EventUtils.TriggerListener(Driver.ENTITY_CREATE_PORTAL, "entity_create_portal", ecpe);
	}

	@EventIdentifier(event = Driver.ENTITY_DROP_ITEM, className = "org.bukkit.event.entity.EntityDropItemEvent")
	public void onEntityDropItem(Event event) {
		BukkitMCEntityDropItemEvent edie = new BukkitMCEntityDropItemEvent(event);
		EventUtils.TriggerListener(Driver.ENTITY_DROP_ITEM, "entity_drop_item", edie);
	}

	@EventIdentifier(event = Driver.ENTITY_RESURRECT, className = "org.bukkit.event.entity.EntityResurrectEvent")
	public void onEntityResurrect(Event event) {
		BukkitMCEntityResurrectEvent ere = new BukkitMCEntityResurrectEvent(event);
		EventUtils.TriggerListener(Driver.ENTITY_RESURRECT, "entity_resurrect", ere);
	}

	@EventIdentifier(event = Driver.ENTITY_SHOOT_BOW, className = "org.bukkit.event.entity.EntityShootBowEvent")
	public void onEntityShootBow(Event event) {
		BukkitMCEntityShootBowEvent esbe = new BukkitMCEntityShootBowEvent(event);
		EventUtils.TriggerListener(Driver.ENTITY_SHOOT_BOW, "entity_shoot_bow", esbe);
	}

	@EventIdentifier(event = Driver.ENTITY_TAME, className = "org.bukkit.event.entity.EntityTameEvent")
	public void onEntityTame(Event event) {
		BukkitMCEntityTameEvent ete = new BukkitMCEntityTameEvent(event);
		EventUtils.TriggerListener(Driver.ENTITY_TAME, "entity_tame", ete);
	}

	@EventIdentifier(event = Driver.ENTITY_TELEPORT, className = "org.bukkit.event.entity.EntityTeleportEvent")
	public void onEntityTeleport(Event event) {
		BukkitMCEntityTeleportEvent ete = new BukkitMCEntityTeleportEvent(event);
		EventUtils.TriggerListener(Driver.ENTITY_TELEPORT, "entity_teleport", ete);
	}

	@EventIdentifier(event = Driver.ENTITY_TOGGLE_SWIM, className = "org.bukkit.event.entity.EntityToggleSwimEvent")
	public void onEntityToggleSwim(Event event) {
		BukkitMCEntityToggleSwimEvent etse = new BukkitMCEntityToggleSwimEvent(event);
		EventUtils.TriggerListener(Driver.ENTITY_TOGGLE_SWIM, "entity_toggle_swim", etse);
	}

	@EventIdentifier(event = Driver.ENTITY_UNLEASH, className = "org.bukkit.event.entity.EntityUnleashEvent")
	public void onEntityUnlesah(Event event) {
		BukkitMCEntityUnleashEvent eue = new BukkitMCEntityUnleashEvent(event);
		EventUtils.TriggerListener(Driver.ENTITY_UNLEASH, "entity_unleash", eue);
	}

	@EventIdentifier(event = Driver.EXPLOSION_PRIME, className = "org.bukkit.event.entity.ExplosionPrimeEvent")
	public void onExplosionPrime(Event event) {
		BukkitMCExplosionPrimeEvent epe = new BukkitMCExplosionPrimeEvent(event);
		EventUtils.TriggerListener(Driver.EXPLOSION_PRIME, "explosion_prime", epe);
	}

	@EventIdentifier(event = Driver.HORSE_JUMP, className = "org.bukkit.event.entity.HorseJumpEvent")
	public void onHorseJump(Event event) {
		BukkitMCHorseJumpEvent hje = new BukkitMCHorseJumpEvent(event);
		EventUtils.TriggerListener(Driver.HORSE_JUMP, "horse_jump", hje);
	}

	@EventIdentifier(event = Driver.ITEM_MERGE, className = "org.bukkit.event.entity.ItemMergeEvent")
	public void onItemMerge(Event event) {
		BukkitMCItemMergeEvent ime = new BukkitMCItemMergeEvent(event);
		EventUtils.TriggerListener(Driver.ITEM_MERGE, "item_merge", ime);
	}

	@EventIdentifier(event = Driver.PIG_ZAP, className = "org.bukkit.event.entity.PigZapEvent")
	public void onPigZap(Event event) {
		BukkitMCPigZapEvent pze = new BukkitMCPigZapEvent(event);
		EventUtils.TriggerListener(Driver.PIG_ZAP, "pig_zap", pze);
	}

	@EventIdentifier(event = Driver.PIG_ZOMBIE_ANGER, className = "org.bukkit.event.entity.PigZombieAngerEvent")
	public void onPigZombieAnger(Event event) {
		BukkitMCPigZombieAngerEvent pzae = new BukkitMCPigZombieAngerEvent(event);
		EventUtils.TriggerListener(Driver.PIG_ZOMBIE_ANGER, "pig_zombie_anger", pzae);
	}

	@EventIdentifier(event = Driver.SHEEP_DYE_WOOL, className = "org.bukkit.event.entity.SheepDyeWoolEvent")
	public void onSheepDyeWool(Event event) {
		BukkitMCSheepDyeWoolEvent sdwe = new BukkitMCSheepDyeWoolEvent(event);
		EventUtils.TriggerListener(Driver.SHEEP_DYE_WOOL, "sheep_dye_wool", sdwe);
	}

	@EventIdentifier(event = Driver.SHEEP_REGROW_WOOL, className = "org.bukkit.event.entity.SheepRegrowWoolEvent")
	public void onSheepRegrowWool(Event event) {
		BukkitMCSheepRegrowWoolEvent srwe = new BukkitMCSheepRegrowWoolEvent(event);
		EventUtils.TriggerListener(Driver.SHEEP_REGROW_WOOL, "sheep_regrow_wool", srwe);
	}

	@EventIdentifier(event = Driver.SLIME_SPLIT, className = "org.bukkit.event.entity.SlimeSplitEvent")
	public void onSlimeSplit(Event event) {
		BukkitMCSlimeSplitEvent sse = new BukkitMCSlimeSplitEvent(event);
		EventUtils.TriggerListener(Driver.SLIME_SPLIT, "slime_split", sse);
	}

	@EventIdentifier(event = Driver.VILLAGER_ACQUIRE_TRADE, className = "org.bukkit.event.entity.VillagerAcquireTradeEvent")
	public void onVillagerAcquireTrade(Event event) {
		BukkitMCVillagerAcquireTradeEvent vate = new BukkitMCVillagerAcquireTradeEvent(event);
		EventUtils.TriggerListener(Driver.VILLAGER_ACQUIRE_TRADE, "villager_acquire_trade", vate);
	}

	@EventIdentifier(event = Driver.VILLAGER_REPLENISH_TRADE, className = "org.bukkit.event.entity.VillagerReplenishTradeEvent")
	public void onVillagerReplenishTrade(Event event) {
		BukkitMCVillagerReplenishTradeEvent vrte = new BukkitMCVillagerReplenishTradeEvent(event);
		EventUtils.TriggerListener(Driver.VILLAGER_REPLENISH_TRADE, "villager_replenish_trade", vrte);
	}
}
