package com.laytonsmith.core.events.drivers;

import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.MCHanging;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCLivingEntity;
import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.MCProjectile;
import com.laytonsmith.abstraction.MCWorld;
import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.abstraction.enums.MCMobs;
import com.laytonsmith.abstraction.enums.MCRemoveCause;
import com.laytonsmith.abstraction.enums.MCSpawnReason;
import com.laytonsmith.abstraction.events.MCCreatureSpawnEvent;
import com.laytonsmith.abstraction.events.MCEntityChangeBlockEvent;
import com.laytonsmith.abstraction.events.MCEntityDamageByEntityEvent;
import com.laytonsmith.abstraction.events.MCEntityDamageEvent;
import com.laytonsmith.abstraction.events.MCEntityDeathEvent;
import com.laytonsmith.abstraction.events.MCEntityEnterPortalEvent;
import com.laytonsmith.abstraction.events.MCEntityExplodeEvent;
import com.laytonsmith.abstraction.events.MCEntityTargetEvent;
import com.laytonsmith.abstraction.events.MCHangingBreakEvent;
import com.laytonsmith.abstraction.events.MCItemSpawnEvent;
import com.laytonsmith.abstraction.events.MCPlayerDropItemEvent;
import com.laytonsmith.abstraction.events.MCPlayerInteractEntityEvent;
import com.laytonsmith.abstraction.events.MCPlayerPickupItemEvent;
import com.laytonsmith.abstraction.events.MCProjectileHitEvent;
import com.laytonsmith.abstraction.events.MCProjectileLaunchEvent;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.ObjectGenerator;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.*;
import com.laytonsmith.core.events.*;
import com.laytonsmith.core.events.Prefilters.PrefilterType;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.exceptions.EventException;
import com.laytonsmith.core.exceptions.PrefilterNonMatchException;
import com.laytonsmith.core.functions.Exceptions;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 *
 * @author EntityReborn
 */
public class EntityEvents {
    public static String docs(){
        return "Contains events related to an entity";
    }
    
	@api
	public static class item_spawn extends AbstractEvent {

		public String getName() {
			return "item_spawn";
		}

		public String docs() {
			return "{item: <item match> the item id and data value to check}"
					+ " Fires when an item entity comes into existance."
					+ " {location: where the item spawns | id: the item's entityID | item}"
					+ " {item: the itemstack of the entity}"
					+ " {}";
		}

		public boolean matches(Map<String, Construct> prefilter, BindableEvent e) throws PrefilterNonMatchException {
			if (e instanceof MCItemSpawnEvent) {
				Prefilters.match(prefilter, "item", Static.ParseItemNotation(
						((MCItemSpawnEvent) e).getEntity().getItemStack()), PrefilterType.ITEM_MATCH);
				return true;
			}
			return false;
		}

		public BindableEvent convert(CArray manualObject) {
			throw ConfigRuntimeException.CreateUncatchableException("Unsupported Operation", Target.UNKNOWN);
		}

		public Map<String, Construct> evaluate(BindableEvent e) throws EventException {
			if (e instanceof MCItemSpawnEvent) {
				Target t = Target.UNKNOWN;
				MCItemSpawnEvent event = (MCItemSpawnEvent) e;
				Map<String, Construct> ret = evaluate_helper(event);
				ret.put("location", ObjectGenerator.GetGenerator().location(event.getLocation()));
				ret.put("id", new CInt(event.getEntity().getEntityId(), t));
				ret.put("item", ObjectGenerator.GetGenerator().item(event.getEntity().getItemStack(), t));
				return ret;
			} else {
				throw new EventException("Could not convert to MCItemSpawnEvent");
			}
		}

		public Driver driver() {
			return Driver.ITEM_SPAWN;
		}

		public boolean modifyEvent(String key, Construct value, BindableEvent event) {
			if (event instanceof MCItemSpawnEvent) {
				if ("item".equals(key)) {
					((MCItemSpawnEvent) event).getEntity().setItemStack(ObjectGenerator.GetGenerator().item(value, Target.UNKNOWN));
					return true;
				}
			}
			return false;
		}

		public Version since() {
			return CHVersion.V3_3_1;
		}
	}

	@api
	public static class entity_explode extends AbstractEvent {

		public String getName() {
			return "entity_explode";
		}

		public String docs() {
			return "{type: <macro> The type of entity exploding. If null is used here, it will match events that"
					+ " lack a specific entity, such as using the explosion function} Fires when an explosion occurs."
					+ " The entity itself may not exist if triggered by a plugin. Cancelling this event only protects blocks,"
					+ " entities are handled in damage events. {id: entityID, or null if no entity"
					+ " | type: entitytype, or null if no entity | location: where the explosion occurs | blocks | yield}"
					+ " {blocks: An array of blocks destroyed by the explosion. | yield: Percent of the blocks destroyed"
					+ " that should drop items. A value greater than 100 will cause more drops than the original blocks.}"
					+ " {}";
		}

		public boolean matches(Map<String, Construct> prefilter, BindableEvent event) throws PrefilterNonMatchException {
			if (event instanceof MCEntityExplodeEvent) {
				MCEntityExplodeEvent e = (MCEntityExplodeEvent) event;
				if (prefilter.containsKey("type")) {
					if (e.getEntity() == null){
						if (prefilter.get("type") instanceof CNull || prefilter.get("type").val().equals("null")) {
							return true;
						}
						return false;
					}
					Prefilters.match(prefilter, "type", e.getEntity().getType().name(), PrefilterType.MACRO);
				}
				return true;
			}
			return false;
		}

		public BindableEvent convert(CArray manualObject) {
			throw ConfigRuntimeException.CreateUncatchableException("Unsupported Operation", Target.UNKNOWN);
		}

		public Map<String, Construct> evaluate(BindableEvent event) throws EventException {
			if (event instanceof MCEntityExplodeEvent) {
				Target t = Target.UNKNOWN;
				MCEntityExplodeEvent e = (MCEntityExplodeEvent) event;
				Map<String, Construct> ret = evaluate_helper(e);
				CArray blocks = new CArray(t);
				for (MCBlock b : e.getBlocks()) {
					blocks.push(ObjectGenerator.GetGenerator().location(b.getLocation()));
				}
				ret.put("blocks", blocks);
				Construct entity = new CNull(t);
				Construct entitytype = new CNull(t);
				if (e.getEntity() != null) {
					entity = new CInt(e.getEntity().getEntityId(), t);
					entitytype = new CString(e.getEntity().getType().name(), t);
				}
				ret.put("id", entity);
				ret.put("type", entitytype);
				ret.put("location", ObjectGenerator.GetGenerator().location(e.getLocation()));
				ret.put("yield", new CDouble(e.getYield(), t));
				return ret;
			} else {
				throw new EventException("Could not convert to MCEntityExplodeEvent");
			}
		}

		public Driver driver() {
			return Driver.ENTITY_EXPLODE;
		}

		public boolean modifyEvent(String key, Construct value, BindableEvent event) {
			if (event instanceof MCEntityExplodeEvent) {
				MCEntityExplodeEvent e = (MCEntityExplodeEvent) event;
				if (key.equals("yield")) {
					e.setYield(Static.getDouble32(value, Target.UNKNOWN));
					return true;
				}
				if (key.equals("blocks")) {
					if (value instanceof CArray) {
						CArray ba = (CArray) value;
						List<MCBlock> blocks = new ArrayList<MCBlock>();
						for (String b : ba.keySet()) {
							MCWorld w = e.getLocation().getWorld();
							MCLocation loc = ObjectGenerator.GetGenerator().location(ba.get(b), w, Target.UNKNOWN);
							blocks.add(loc.getWorld().getBlockAt(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
						}
						e.setBlocks(blocks);
						return true;
					}
				}
			}
			return false;
		}

		public Version since() {
			return CHVersion.V3_3_1;
		}
	}

	@api
	public static class projectile_hit extends AbstractEvent {

		public String getName() {
			return "projectile_hit";
		}

		public String docs() {
			return "{type: <macro> the entity type of the projectile}"
					+ " Fires when a projectile collides with something."
					+ " {type | id: the entityID of the projectile |" 
					+ " location: where it makes contact | shooter}"
					+ " {shooter: the entityID of the mob/player that fired"
					+ " the projectile, or null if it is from a dispenser}"
					+ " {id}";
		}

		public boolean matches(Map<String, Construct> prefilter, 
				BindableEvent event) throws PrefilterNonMatchException {
			if (event instanceof MCProjectileHitEvent) {
				MCProjectileHitEvent e = (MCProjectileHitEvent) event;
				Prefilters.match(prefilter, "type", e.getEntityType().name(), PrefilterType.MACRO);
				return true;
			}
			return false;
		}

		public BindableEvent convert(CArray manualObject) {
			int id = Static.getInt32(manualObject.get("id"), Target.UNKNOWN);
			MCEntity p = Static.getEntity(id, Target.UNKNOWN);
			if (!(p instanceof MCProjectile)) {
				throw new ConfigRuntimeException("The id was not a projectile", 
						ExceptionType.BadEntityException, Target.UNKNOWN);
			}
			return EventBuilder.instantiate(MCProjectileHitEvent.class, p);
		}

		public Map<String, Construct> evaluate(BindableEvent event)
				throws EventException {
			if (event instanceof MCProjectileHitEvent) {
				Target t = Target.UNKNOWN;
				MCProjectileHitEvent e = (MCProjectileHitEvent) event;
				Map<String, Construct> ret = evaluate_helper(e);
				MCProjectile pro = e.getEntity();
				ret.put("id", new CInt(pro.getEntityId(), t));
				ret.put("type", new CString(pro.getType().name(), t));
				CArray loc = ObjectGenerator.GetGenerator().location(pro.getLocation());
				ret.put("location", loc);
				MCLivingEntity shooter = pro.getShooter();
				if (shooter == null) {
					ret.put("shooter", new CNull(t));
				} else {
					ret.put("shooter", new CInt(shooter.getEntityId(), t));
				}
				return ret;
			} else {
				throw new EventException("Could not convert to ProjectileHit");
			}
		}

		public Driver driver() {
			return Driver.PROJECTILE_HIT;
		}

		public boolean modifyEvent(String key, Construct value,
				BindableEvent event) {
			if (event instanceof MCProjectileHitEvent) {
				MCProjectileHitEvent e = (MCProjectileHitEvent) event;
				if (key.equalsIgnoreCase("shooter")) {
					MCLivingEntity le;
					if (value instanceof CNull) {
						le = null;
					} else {
						int id = Static.getInt32(value, Target.UNKNOWN);
						le = Static.getLivingEntity(id, Target.UNKNOWN);
					}
					e.getEntity().setShooter(le);
				}
			}
			return false;
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
    	
    }

	@api
	public static class projectile_launch extends AbstractEvent {

		public String getName() {
			return "projectile_launch";
		}

		public Driver driver() {
			return Driver.PROJECTILE_LAUNCH;
		}

		public String docs() {
			return "{type: <macro> The entity type of the projectile | shootertype: <macro> The entity type of the shooter | world: <macro>}"
					+ " This event is called when a projectile is launched."
					+ " Cancelling the event will only cancel the launching of the projectile."
					+ " For instance when a player shoots an arrow with a bow, if the event is cancelled the bow will still take damage from use."
					+ " {id: The entityID of the projectile | type: The entity type of the projectile |"
					+ " shooter: The entityID of the shooter (null if the projectile is launched by a dispenser) |"
					+ " shootertype: The entity type of the shooter (null if the projectile is launched by a dispenser) |"
					+ " player: the player which has launched the projectile (null if the shooter is not a player) |"
					+ " location: from where the projectile is launched | velocity: the velocity of the projectile}"
					+ " {velocity}"
					+ " {}";
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

		public boolean matches(Map<String, Construct> prefilter, BindableEvent event) throws PrefilterNonMatchException {
			if (event instanceof MCProjectileLaunchEvent) {
				MCProjectileLaunchEvent projectileLaunchEvent = (MCProjectileLaunchEvent) event;
				Prefilters.match(prefilter, "type", projectileLaunchEvent.getEntityType().name(), PrefilterType.MACRO);
				MCLivingEntity shooter = projectileLaunchEvent.getEntity().getShooter();
				if (shooter != null) {
					Prefilters.match(prefilter, "shootertype", shooter.getType().name(), PrefilterType.MACRO);
				} else {
					Prefilters.match(prefilter, "shootertype", "null", PrefilterType.MACRO);
				}
				Prefilters.match(prefilter, "world", projectileLaunchEvent.getEntity().getWorld().getName(), PrefilterType.MACRO);
				return true;
			} else {
				return false;
			}
		}

		public BindableEvent convert(CArray manualObject) {
			return null;
		}

		public Map<String, Construct> evaluate(BindableEvent event) throws EventException {
			if (event instanceof MCProjectileLaunchEvent) {
				MCProjectileLaunchEvent projectileLaunchEvent = (MCProjectileLaunchEvent) event;
				Map<String, Construct> mapEvent = evaluate_helper(event);
				MCProjectile projectile = projectileLaunchEvent.getEntity();
				mapEvent.put("id", new CInt(projectile.getEntityId(), Target.UNKNOWN));
				mapEvent.put("type", new CString(projectileLaunchEvent.getEntityType().name(), Target.UNKNOWN));
				MCLivingEntity shooter = projectile.getShooter();
				if (shooter != null) {
					mapEvent.put("shooter", new CInt(shooter.getEntityId(), Target.UNKNOWN));
					mapEvent.put("shootertype", new CString(shooter.getType().name(), Target.UNKNOWN));
					if (shooter instanceof MCPlayer) {
						mapEvent.put("player", new CString(((MCPlayer) shooter).getName(), Target.UNKNOWN));
					} else {
						mapEvent.put("player", new CNull(Target.UNKNOWN));
					}
				} else {
					mapEvent.put("shooter", new CNull(Target.UNKNOWN));
					mapEvent.put("shootertype", new CNull(Target.UNKNOWN));
					mapEvent.put("player", new CNull(Target.UNKNOWN));
				}
				mapEvent.put("location", ObjectGenerator.GetGenerator().location(projectile.getLocation()));
				mapEvent.put("velocity", ObjectGenerator.GetGenerator().velocity(projectile.getVelocity(), Target.UNKNOWN));
				return mapEvent;
			} else {
				throw new EventException("Cannot convert event to ProjectileLaunchEvent");
			}
		}

		public boolean modifyEvent(String key, Construct value, BindableEvent event) {
			if (event instanceof MCProjectileLaunchEvent) {
				MCProjectileLaunchEvent projectileLaunchEvent = (MCProjectileLaunchEvent) event;
				if (key.equals("velocity")) {
					projectileLaunchEvent.getEntity().setVelocity(ObjectGenerator.GetGenerator().velocity(value, Target.UNKNOWN));
					return true;
				}
			}
			return false;
		}
	}

	@api
	public static class entity_death extends AbstractEvent {

		public String getName() {
			return "entity_death";
		}

		public String docs() {
			return "{type: <macro> The type of entity dying.}"
					+ " Fires when any living entity dies."
					+ " {type | id: The entityID | drops: an array of item arrays of each stack"
					+ " | xp | cause: the last entity_damage object for this entity"
					+ " | location: where the entity was when it died}" 
					+ " {drops: an array of item arrays of what will be dropped,"
					+ " replaces the normal drops, can be null | xp: the amount of xp to drop}" 
					+ " {id|drops|xp}";
		}

		public boolean matches(Map<String, Construct> prefilter, BindableEvent e)
				throws PrefilterNonMatchException {
			if (e instanceof MCEntityDeathEvent) {
				MCEntityDeathEvent event = (MCEntityDeathEvent) e;
				Prefilters.match(prefilter, "type", event.getEntity().getType().name(), PrefilterType.MACRO);
				return true;
			}
			return false;
		}

		public BindableEvent convert(CArray manualObject) {
			return null;
		}

		public Map<String, Construct> evaluate(BindableEvent event)
				throws EventException {
			if (event instanceof MCEntityDeathEvent) {
				MCEntityDeathEvent e = (MCEntityDeathEvent) event;
				Target t = Target.UNKNOWN;
				MCLivingEntity dead = e.getEntity();
				Map<String, Construct> map = evaluate_helper(event);
				CArray drops = new CArray(t);
				for(MCItemStack is : e.getDrops()){
					drops.push(ObjectGenerator.GetGenerator().item(is, t));
				}
				map.put("type", new CString(dead.getType().name(), t));
				map.put("id", new CInt(dead.getEntityId(), t));
				map.put("drops", drops);
				map.put("xp", new CInt(e.getDroppedExp(), t));
				CArray cod = CArray.GetAssociativeArray(t);
				Map<String, Construct> ldc = 
						parseEntityDamageEvent(dead.getLastDamageCause(), 
								new HashMap<String, Construct>());
				for (String key : ldc.keySet()) {
					cod.set(key, ldc.get(key), t);
				}
				map.put("cause", cod);
				map.put("location", ObjectGenerator.GetGenerator().location(dead.getLocation()));
				return map;
			} else {
				throw new EventException("Cannot convert e to EntityDeathEvent");
			}
		}

		public Driver driver() {
			return Driver.ENTITY_DEATH;
		}

		public boolean modifyEvent(String key, Construct value, BindableEvent event) {
			if (event instanceof MCEntityDeathEvent) {
				MCEntityDeathEvent e = (MCEntityDeathEvent) event;
				if (key.equals("xp")) {
					e.setDroppedExp(Static.getInt32(value, Target.UNKNOWN));
					return true;
				}
				if(key.equals("drops")){
					if(value instanceof CNull){
						value = new CArray(Target.UNKNOWN);
					}
					if(!(value instanceof CArray)){
						throw new ConfigRuntimeException("drops must be an array, or null", Exceptions.ExceptionType.CastException, value.getTarget());
					}
					e.clearDrops();
					CArray drops = (CArray) value;
					for(String dropID : drops.keySet()){
						e.addDrop(ObjectGenerator.GetGenerator().item(drops.get(dropID), Target.UNKNOWN));
					}
					return true;
				}
			}
			return false;
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
		
	}
	
	@api
	public static class creature_spawn extends AbstractEvent {

		public String getName() {
			return "creature_spawn";
		}

		public String docs() {
			return "{type: <macro> | reason: <macro> One of " + StringUtils.Join(MCSpawnReason.values(), ", ", ", or ", " or ") + "}"
				+ " Fired when a living entity spawns on the server."
				+ " {type: the type of creature spawning | id: the entityID of the creature"
				+ " | reason: the reason this creature is spawning | location: locationArray of the event}"
				+ " {type: Spawn a different entity instead. This will fire a new event with a reason of 'CUSTOM'.}"
				+ " {}";
		}

		public boolean matches(Map<String, Construct> prefilter, BindableEvent event)
				throws PrefilterNonMatchException {
			if (event instanceof MCCreatureSpawnEvent) {
				MCCreatureSpawnEvent e = (MCCreatureSpawnEvent) event;
				Prefilters.match(prefilter, "type", e.getEntity().getType().name(), PrefilterType.MACRO);
				Prefilters.match(prefilter, "reason", e.getSpawnReason().name(), PrefilterType.MACRO);
				return true;
			}
			return false;
		}

		public BindableEvent convert(CArray manualObject) {
			return null;
		}

		public Map<String, Construct> evaluate(BindableEvent event)
				throws EventException {
			if (event instanceof MCCreatureSpawnEvent) {
				MCCreatureSpawnEvent e = (MCCreatureSpawnEvent) event;
				Map<String, Construct> map = evaluate_helper(e);
				
				map.put("type", new CString(e.getEntity().getType().name(), Target.UNKNOWN));
				map.put("id", new CInt(e.getEntity().getEntityId(), Target.UNKNOWN));
				map.put("reason", new CString(e.getSpawnReason().name(), Target.UNKNOWN));
				map.put("location", ObjectGenerator.GetGenerator().location(e.getLocation()));
				
				return map;
			} else {
				throw new EventException("Could not convert to MCCreatureSpawnEvent");
			}
		}

		public Driver driver() {
			return Driver.CREATURE_SPAWN;
		}

		public boolean modifyEvent(String key, Construct value,
				BindableEvent event) {
			MCCreatureSpawnEvent e = (MCCreatureSpawnEvent) event;
			if (key.equals("type")) {
				MCMobs type;
				try {
					type = MCMobs.valueOf(value.val());
				} catch (IllegalArgumentException iae) {
					throw new Exceptions.FormatException(value.val() + " is not a valid mob type.", Target.UNKNOWN);
				}
				e.setType(type);
			}
			return false;
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
		
	}
	
	@api
	public static class entity_damage extends AbstractEvent {

		public String getName() {
			return "entity_damage";
		}

		public String docs() {
			return "{type: <macro> The type of entity being damaged | cause: <macro> | world: <string match>}"
				+ " Fires when any loaded entity takes damage."
				+ " {type: The type of entity the got damaged | id: The entityID of the victim"
				+ " | player: the player who got damaged (only present if type is PLAYER) | world"
				+ " | cause: The type of damage | amount | damager: If the source of damage is a player this will"
				+ " contain their name, otherwise it will be the entityID of the damager (only available when"
				+ " an entity causes damage) | shooter: The name of the player who shot, otherwise the entityID"
				+ " (only available when damager is a projectile)}"
				+ " {amount: the amount of damage recieved (in half hearts)}"
				+ " {}";
		}

		public boolean matches(Map<String, Construct> prefilter, BindableEvent event)
				throws PrefilterNonMatchException {
			if(event instanceof MCEntityDamageEvent){
				MCEntityDamageEvent e = (MCEntityDamageEvent) event;
				Prefilters.match(prefilter, "type", e.getEntity().getType().name(), Prefilters.PrefilterType.MACRO);
				Prefilters.match(prefilter, "cause", e.getCause().name(), Prefilters.PrefilterType.MACRO);
				Prefilters.match(prefilter, "world", e.getEntity().getWorld().getName(), Prefilters.PrefilterType.STRING_MATCH);
				
				return true;
			}
			return false;
		}

		public BindableEvent convert(CArray manualObject) {
			return null;
		}

		public Map<String, Construct> evaluate(BindableEvent e)
				throws EventException {
			if (e instanceof MCEntityDamageEvent) {
				MCEntityDamageEvent event = (MCEntityDamageEvent) e;
				Map<String, Construct> map = evaluate_helper(e);
				
				map = parseEntityDamageEvent(event, map);
				
				return map;
			} else {
				throw new EventException("Cannot convert e to MCEntityDamageEvent");
			}
		}

		public Driver driver() {
			return Driver.ENTITY_DAMAGE;
		}

		public boolean modifyEvent(String key, Construct value,
				BindableEvent event) {
			MCEntityDamageEvent e = (MCEntityDamageEvent) event;
			if (key.equals("amount")) {
				if (value instanceof CInt) {
					e.setDamage((int) Static.getInt(value, Target.UNKNOWN));
					return true;
				}
			}
			return false;
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
		
	}
	
	@api
	public static class player_interact_entity extends AbstractEvent {

		public String getName() {
			return "player_interact_entity";
		}

		public String docs() {
			return "{clicked: the type of entity being clicked}"
				+ " Fires when a player right clicks an entity. Note, not all entities are clickable."
				+ " {player: the player clicking | clicked | id: the id of the entity"
				+ " | data: if a player is clicked, this will contain their name}"
				+ " {}"
				+ " {player|clicked|id|data}";
		}

		public boolean matches(Map<String, Construct> prefilter, BindableEvent event)
				throws PrefilterNonMatchException {
			if(event instanceof MCPlayerInteractEntityEvent){
				MCPlayerInteractEntityEvent e = (MCPlayerInteractEntityEvent) event;
				Prefilters.match(prefilter, "clicked", e.getEntity().getType().name(), Prefilters.PrefilterType.MACRO);
				return true;
			}
			return false;
		}

		public BindableEvent convert(CArray manualObject) {
			return null;
		}

		public Map<String, Construct> evaluate(BindableEvent e)
				throws EventException {
			if (e instanceof MCPlayerInteractEntityEvent) {
				MCPlayerInteractEntityEvent event = (MCPlayerInteractEntityEvent) e;
				Map<String, Construct> map = evaluate_helper(e);
				
				map.put("player", new CString(event.getPlayer().getName(), Target.UNKNOWN));
				map.put("clicked", new CString(event.getEntity().getType().name(), Target.UNKNOWN));
				map.put("id", new CInt(event.getEntity().getEntityId(),Target.UNKNOWN));
				
				String data = "";
				if(event.getEntity() instanceof MCPlayer) {
					data = ((MCPlayer)event.getEntity()).getName();
				}
				map.put("data",  new CString(data, Target.UNKNOWN));
				
				return map;
			} else {
				throw new EventException("Cannot convert e to MCPlayerDropItemEvent");
			}
		}

		public Driver driver() {
			return Driver.PLAYER_INTERACT_ENTITY;
		}

		public boolean modifyEvent(String key, Construct value,
				BindableEvent event) {
			return false;
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
		
	}
    
    @api
    public static class item_drop extends AbstractEvent {
        
        public String getName() {
            return "item_drop";
        }
        
        public String docs() {
            return "{player: <string match> | item: <item match>} "
                    + "This event is called when a player drops an item. "
                    + "{player: The player | item: An item array representing " 
                    + "the item being dropped. } "
                    + "{item: setting this to null removes the dropped item} "
                    + "{player|item}";
        }
        
        public BindableEvent convert(CArray manualObject) {
            return null;
        }

        public CHVersion since() {
            return CHVersion.V3_3_1;
        }

        public Driver driver() {
            return Driver.ITEM_DROP;
        }
        
        public boolean matches(Map<String, Construct> prefilter, BindableEvent e) throws PrefilterNonMatchException {
            if (e instanceof MCPlayerDropItemEvent) {
                MCPlayerDropItemEvent event = (MCPlayerDropItemEvent)e;
                
                Prefilters.match(prefilter, "item", Static.ParseItemNotation(event.getItemDrop().getItemStack()), Prefilters.PrefilterType.ITEM_MATCH);
                Prefilters.match(prefilter, "player", event.getPlayer().getName(), Prefilters.PrefilterType.MACRO);
                
                return true;
            }
            return false;
        }

        public Map<String, Construct> evaluate(BindableEvent e) throws EventException {
            if (e instanceof MCPlayerDropItemEvent) {
                MCPlayerDropItemEvent event = (MCPlayerDropItemEvent) e;
                Map<String, Construct> map = evaluate_helper(e);
                
                map.put("player", new CString(event.getPlayer().getName(), Target.UNKNOWN));
                map.put("item", ObjectGenerator.GetGenerator().item(event.getItemDrop().getItemStack(), Target.UNKNOWN));
                map.put("id", new CInt(event.getItemDrop().getEntityId(), Target.UNKNOWN));
                
                return map;
            } else {
                throw new EventException("Cannot convert e to MCPlayerDropItemEvent");
            }
        }

        public boolean modifyEvent(String key, Construct value, BindableEvent event) {
            if (event instanceof MCPlayerDropItemEvent) {
                MCPlayerDropItemEvent e = (MCPlayerDropItemEvent)event;
                
                if (key.equalsIgnoreCase("item")) {
                    MCItemStack stack = ObjectGenerator.GetGenerator().item(value, Target.UNKNOWN);
                    
                    e.setItemStack(stack);
                    
                    return true;
                }
            }
            return false;
        }
    }
    
	@api
	public static class item_pickup extends AbstractEvent {

		public String getName() {
			return "item_pickup";
		}

		public String docs() {
			return "{player: <string match> | item: <item match>} "
				+ "This event is called when a player picks up an item."
				+ "{player: The player | item: An item array representing " 
				+ "the item being picked up | "
				+ "remaining: Other items left on the ground. } "
				+ "{item: setting this to null will remove the item from the world} "
				+ "{player|item|remaining}";
		}

		public boolean matches(Map<String, Construct> prefilter, BindableEvent e) throws PrefilterNonMatchException {
			if (e instanceof MCPlayerPickupItemEvent) {
				MCPlayerPickupItemEvent event = (MCPlayerPickupItemEvent)e;
				
				Prefilters.match(prefilter, "item", Static.ParseItemNotation(event.getItem().getItemStack()), Prefilters.PrefilterType.ITEM_MATCH);
				Prefilters.match(prefilter, "player", event.getPlayer().getName(), Prefilters.PrefilterType.MACRO);
				
				return true;
			}
			
			return false;
		}
		
		public BindableEvent convert(CArray manualObject) {
			return null;
		}

		public Map<String, Construct> evaluate(BindableEvent e) throws EventException {
			if (e instanceof MCPlayerPickupItemEvent) {
                MCPlayerPickupItemEvent event = (MCPlayerPickupItemEvent) e;
                Map<String, Construct> map = evaluate_helper(e);
				
                //Fill in the event parameters
				map.put("player", new CString(event.getPlayer().getName(), Target.UNKNOWN));
				map.put("id", new CInt(event.getItem().getEntityId(), Target.UNKNOWN));
				map.put("item", ObjectGenerator.GetGenerator().item(event.getItem().getItemStack(), Target.UNKNOWN));
				map.put("remaining", new CInt(event.getRemaining(), Target.UNKNOWN));
				
                return map;
            } else {
                throw new EventException("Cannot convert e to MCPlayerPickupItemEvent");
            }
		}

		public Driver driver() {
			return Driver.ITEM_PICKUP;
		}

		public boolean modifyEvent(String key, Construct value, BindableEvent event) {
			if (event instanceof MCPlayerPickupItemEvent) {
				MCPlayerPickupItemEvent e = (MCPlayerPickupItemEvent)event;
				
				if (key.equalsIgnoreCase("item")) {
					MCItemStack stack = ObjectGenerator.GetGenerator().item(value, Target.UNKNOWN);
					
					e.setItemStack(stack);
					
					return true;
				}
			}
			
			return false;
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
		
	}
	
    
    @api
    public static class entity_damage_player extends AbstractEvent {

		public String getName() {
			return "entity_damage_player";
		}

		public String docs() {
			return "{damager: <string match>} "
            		+ "This event is called when a player is damaged by another entity."
                    + "{player: The player being damaged | damager: The type of entity causing damage | "
            		+ "amount: amount of damage caused | cause: the cause of damage | "
                    + "data: the attacking player's name or the shooter if damager is a projectile | "
            		+ "id: EntityID of the damager} "
                    + "{amount} "
                    + "{player|amount|damager|cause|data|id}";
		}

		public boolean matches(Map<String, Construct> prefilter, BindableEvent e)
				throws PrefilterNonMatchException {
			if (e instanceof MCEntityDamageByEntityEvent) {
				MCEntityDamageByEntityEvent event = (MCEntityDamageByEntityEvent) e;
				Prefilters.match(prefilter, "damager", event.getDamager().getType().name(), PrefilterType.MACRO);
				return event.getEntity() instanceof MCPlayer;
			}
			return false;
		}

		public BindableEvent convert(CArray manualObject) {
			throw ConfigRuntimeException.CreateUncatchableException("Unsupported Operation", Target.UNKNOWN);
		}

		public Map<String, Construct> evaluate(BindableEvent e)
				throws EventException {
			if(e instanceof MCEntityDamageByEntityEvent){
                MCEntityDamageByEntityEvent event = (MCEntityDamageByEntityEvent) e;
                Map<String, Construct> map = evaluate_helper(e);
                
                // Guaranteed to be a player via matches
                String name = ((MCPlayer)event.getEntity()).getName();
                map.put("player", new CString(name, Target.UNKNOWN));
                String dtype = event.getDamager().getType().name();
                map.put("damager",  new CString(dtype, Target.UNKNOWN));
                map.put("cause",  new CString(event.getCause().name(), Target.UNKNOWN));
                map.put("amount",  new CDouble(event.getDamage(), Target.UNKNOWN));
                map.put("id", new CInt(event.getDamager().getEntityId(), Target.UNKNOWN));
                
                String data = "";
                if(event.getDamager() instanceof MCPlayer) {
                	data = ((MCPlayer)event.getDamager()).getName();
                } else if (event.getDamager() instanceof MCProjectile) {
                	MCEntity shooter = ((MCProjectile)event.getDamager()).getShooter();
                	
                	if(shooter instanceof MCPlayer) {
                		data = ((MCPlayer)shooter).getName();
                	} else if(shooter instanceof MCEntity) {
                		data = ((MCEntity)shooter).getType().name().toUpperCase();
                	}
                }
                map.put("data",  new CString(data, Target.UNKNOWN));
                
                return map;
            } else {
                throw new EventException("Cannot convert e to EntityDamageByEntityEvent");
            }
		}

		public Driver driver() {
			return Driver.ENTITY_DAMAGE_PLAYER;
		}

		public boolean modifyEvent(String key, Construct value,
				BindableEvent e) {
			MCEntityDamageByEntityEvent event = (MCEntityDamageByEntityEvent)e;
            
    		if (key.equals("amount")) {
    			if (value instanceof CInt) {
    				event.setDamage(Integer.parseInt(value.val()));
    				
    				return true;
    			}
    		}
    		return false;
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
    	
    }
    
    @api
    public static class target_player extends AbstractEvent{

        public String getName() {
            return "target_player";
        }

        public String docs() {
            return "{player: <string match> | mobtype: <macro>} "
            		+ "This event is called when a player is targeted by another entity."
                    + "{player: The player's name | mobtype: The type of mob targeting "
                    + "the player (this will be all capitals!) | id: The EntityID of the mob}"
                    + "{player: target a different player, or null to make the mob re-look for targets}"
                    + "{player|mobtype}";
        }

        public CHVersion since() {
            return CHVersion.V3_3_1;
        }
        
        public Driver driver(){
            return Driver.TARGET_ENTITY;
        }

        public boolean matches(Map<String, Construct> prefilter, BindableEvent e) throws PrefilterNonMatchException {
        	if(e instanceof MCEntityTargetEvent){
        		MCEntityTargetEvent ete = (MCEntityTargetEvent) e;
        		
        		Prefilters.match(prefilter, "mobtype", ete.getEntityType().name(), Prefilters.PrefilterType.MACRO);
        		
        		MCEntity target = ete.getTarget();
        		if (target == null) {
        			return false;
        		}
        		
        		if (target instanceof MCPlayer) {
	        		Prefilters.match(prefilter, "player", ((MCPlayer)target).getName(), Prefilters.PrefilterType.MACRO);
	        		
	        		return true;
	        	}
        	}
        	
        	return false;
        }
        
        public Map<String, Construct> evaluate(BindableEvent e) throws EventException {
            if(e instanceof MCEntityTargetEvent){
                MCEntityTargetEvent ete = (MCEntityTargetEvent) e;
                Map<String, Construct> map = evaluate_helper(e);
                
                String name = "";
                MCEntity target = ete.getTarget();
                if (target instanceof MCPlayer) {
                	name = ((MCPlayer)ete.getTarget()).getName();
                } 
                
                map.put("player", new CString(name, Target.UNKNOWN));
                
                String type = ete.getEntityType().name();
                map.put("mobtype", new CString(type, Target.UNKNOWN));
                map.put("id", new CInt(ete.getEntity().getEntityId(), Target.UNKNOWN));
                
                return map;
            } else {
                throw new EventException("Cannot convert e to EntityTargetLivingEntityEvent");
            }
        }
        
        public boolean modifyEvent(String key, Construct value, BindableEvent event) {
        	if(event instanceof MCEntityTargetEvent){
        		MCEntityTargetEvent ete = (MCEntityTargetEvent)event;
            
        		if (key.equals("player")) {
        			if (value instanceof CNull) {
        				ete.setTarget(null);
        				return true;
        			} else if (value instanceof CString) {
        				MCPlayer p = Static.GetPlayer(value.val(), Target.UNKNOWN);
        				
        				if (p.isOnline()) {
        					ete.setTarget((MCEntity)p);
        					return true;
        				}
        			}
        		}
        	}
        	
        	return false;
        }
        
        public BindableEvent convert(CArray manual){
            MCEntityTargetEvent e = EventBuilder.instantiate(MCEntityTargetEvent.class, Static.GetPlayer(manual.get("player").val(), Target.UNKNOWN));
            return e;
        }
        
    }
    
	@api
	public static class entity_enter_portal extends AbstractEvent {
	
		public String getName() {
			return "entity_enter_portal";
		}
	
		public String docs() {
			return "{type: <macro> the type of entity | block: <math match> the blockID of the portal}"
					+ " Fires when an entity touches a portal block."
					+ " {id: the entityID of the entity | location: the location of the block touched | type | block}"
					+ " {}"
					+ " {}";
		}
	
		public boolean matches(Map<String, Construct> prefilter, BindableEvent e)
				throws PrefilterNonMatchException {
			if (e instanceof MCEntityEnterPortalEvent) {
				MCEntityEnterPortalEvent event = (MCEntityEnterPortalEvent) e;
				Prefilters.match(prefilter, "type", event.getEntity().getType().name(), PrefilterType.MACRO);
				Prefilters.match(prefilter, "block", event.getLocation().getBlock().getTypeId(), PrefilterType.MATH_MATCH);
				return true;
			}
			return false;
		}
	
		public BindableEvent convert(CArray manualObject) {
			throw ConfigRuntimeException.CreateUncatchableException("Unsupported Operation", Target.UNKNOWN);
		}
	
		public Map<String, Construct> evaluate(BindableEvent e)
				throws EventException {
			if (e instanceof MCEntityEnterPortalEvent) {
				MCEntityEnterPortalEvent event = (MCEntityEnterPortalEvent) e;
				Target t = Target.UNKNOWN;
				Map<String, Construct> ret = evaluate_helper(event);
				ret.put("id", new CInt(event.getEntity().getEntityId(), t));
				ret.put("type", new CString(event.getEntity().getType().name(), t));
				ret.put("location", ObjectGenerator.GetGenerator().location(event.getLocation(), false));
				ret.put("block", new CInt(event.getLocation().getBlock().getTypeId(), t));
				return ret;
			} else {
				throw new EventException("Could not convert to MCPortalEnterEvent");
			}
		}
	
		public boolean modifyEvent(String key, Construct value,
				BindableEvent event) {
			return false;
		}
	
		public Driver driver() {
			return Driver.ENTITY_ENTER_PORTAL;
		}
	
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}

	@api
	public static class entity_change_block extends AbstractEvent {

		public String getName() {
			return "entity_change_block";
		}

		public String docs() {
			return "{from: <math match> the block ID before the change | to: <math match> the block ID after change"
					+ " | location: <location match> the location of the block changed}"
					+ " Fires when an entity change block in some way."
					+ " {entity: the entity ID of the entity which changed block | from: the block ID before the change"
					+ " | data: the data value for the block being changed | to: the block ID after change"
					+ " | location: the location of the block changed}"
					+ " {}"
					+ " {from|to|location}";
		}

		public boolean matches(Map<String, Construct> prefilter, BindableEvent e)
				throws PrefilterNonMatchException {
			if (e instanceof MCEntityChangeBlockEvent) {
				MCEntityChangeBlockEvent event = (MCEntityChangeBlockEvent) e;
				Prefilters.match(prefilter, "from", event.getBlock().getTypeId(), PrefilterType.MATH_MATCH);
				Prefilters.match(prefilter, "to", event.getTo().getType(), PrefilterType.MATH_MATCH);
				Prefilters.match(prefilter, "location", event.getBlock().getLocation(), PrefilterType.LOCATION_MATCH);
				return true;
			}
			return false;
		}

		public BindableEvent convert(CArray manualObject) {
			return null;
		}

		public Map<String, Construct> evaluate(BindableEvent e)
				throws EventException {
			if (e instanceof MCEntityChangeBlockEvent) {
				MCEntityChangeBlockEvent event = (MCEntityChangeBlockEvent) e;
				Target t = Target.UNKNOWN;
				Map<String, Construct> ret = evaluate_helper(event);
				ret.put("entity", new CInt(event.getEntity().getEntityId(), t));
				ret.put("from", new CInt(event.getBlock().getTypeId(), t));
				ret.put("data", new CInt(event.getData(), t));
				ret.put("to", new CInt(event.getTo().getType(), t));
				ret.put("location", ObjectGenerator.GetGenerator().location(event.getBlock().getLocation(), false));
				return ret;
			} else {
				throw new EventException("Could not convert to MCEntityChangeBlockEvent");
			}
		}

		public boolean modifyEvent(String key, Construct value,
				BindableEvent event) {
			return false;
		}

		public Driver driver() {
			return Driver.ENTITY_CHANGE_BLOCK;
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}

	public static Map<String, Construct> parseEntityDamageEvent(MCEntityDamageEvent event,
			Map<String, Construct> map) {
		if (event != null) {
			MCEntity victim = event.getEntity();
			map.put("type", new CString(victim.getType().name(), Target.UNKNOWN));
			map.put("id", new CInt(victim.getEntityId(), Target.UNKNOWN));
			map.put("cause", new CString(event.getCause().name(), Target.UNKNOWN));
			map.put("amount", new CDouble(event.getDamage(), Target.UNKNOWN));
			map.put("world", new CString(event.getEntity().getWorld().getName(), Target.UNKNOWN));

			if (event instanceof MCEntityDamageByEntityEvent) {
				MCEntity damager = ((MCEntityDamageByEntityEvent) event).getDamager();
				if (damager instanceof MCPlayer) {
					map.put("damager", new CString(((MCPlayer) damager).getName(), Target.UNKNOWN));
				} else {
					map.put("damager", new CInt(damager.getEntityId(), Target.UNKNOWN));
				}
				if (damager instanceof MCProjectile) {
					MCEntity shooter = ((MCProjectile) damager).getShooter();
					if (shooter instanceof MCPlayer) {
						map.put("shooter", new CString(((MCPlayer) shooter).getName(), Target.UNKNOWN));
					} else if (shooter instanceof MCEntity) {
						map.put("shooter", new CInt(shooter.getEntityId(), Target.UNKNOWN));
					}
				}
			}
		}
		return map;
	}

	@api
	public static class hanging_break extends AbstractEvent {

		public String getName() {
			return "hanging_break";
		}

		public Driver driver() {
			return Driver.HANGING_BREAK;
		}

		public String docs() {
			return "{type: <macro> The entity type of the hanging entity | cause: <macro> The cause of the removing | world: <macro>}"
					+ " This event is called when a hanged entity is broken."
					+ " {id: The entityID of the hanging entity | type: The entity type of the hanging entity, can be ITEM_FRAME, PAINTING or LEASH_HITCH |"
					+ " cause: The cause of the removing, can be " + StringUtils.Join(MCRemoveCause.values(), ", ", ", or ", " or ")
					+ " | location: Where was the hanging entity before the removing |"
					+ " remover: If the hanging entity has been removed by an other entity, this will contain its entityID, otherwise null |"
					+ " player: If the hanging entity has been removed by a player, this will contain their name, otherwise null}"
					+ " {}"
					+ " {}";
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

		public boolean matches(Map<String, Construct> prefilter, BindableEvent event) throws PrefilterNonMatchException {
			if (event instanceof MCHangingBreakEvent) {
				MCHangingBreakEvent hangingBreakEvent = (MCHangingBreakEvent) event;
				MCHanging hanging = hangingBreakEvent.getEntity();
				Prefilters.match(prefilter, "type", hanging.getType().name(), PrefilterType.MACRO);
				Prefilters.match(prefilter, "cause", hangingBreakEvent.getCause().name(), PrefilterType.MACRO);
				Prefilters.match(prefilter, "world", hanging.getWorld().getName(), PrefilterType.MACRO);
				return true;
			} else {
				return false;
			}
		}

		public BindableEvent convert(CArray manualObject) {
			return null;
		}

		public Map<String, Construct> evaluate(BindableEvent event) throws EventException {
			if (event instanceof MCHangingBreakEvent) {
				MCHangingBreakEvent hangingBreakEvent = (MCHangingBreakEvent) event;
				Map<String, Construct> mapEvent = evaluate_helper(event);
				MCHanging hanging = hangingBreakEvent.getEntity();
				mapEvent.put("id", new CInt(hanging.getEntityId(), Target.UNKNOWN));
				mapEvent.put("type", new CString(hanging.getType().name(), Target.UNKNOWN));
				mapEvent.put("location", ObjectGenerator.GetGenerator().location(hanging.getLocation()));
				mapEvent.put("cause", new CString(hangingBreakEvent.getCause().name(), Target.UNKNOWN));
				MCEntity remover = hangingBreakEvent.getRemover();
				if (remover != null) {
					mapEvent.put("remover", new CInt(remover.getEntityId(), Target.UNKNOWN));
					if (remover instanceof MCPlayer) {
						mapEvent.put("player", new CString(((MCPlayer) remover).getName(), Target.UNKNOWN));
					} else {
						mapEvent.put("player", new CNull(Target.UNKNOWN));
					}
				} else {
					mapEvent.put("remover", new CNull(Target.UNKNOWN));
					mapEvent.put("player", new CNull(Target.UNKNOWN));
				}
				return mapEvent;
			} else {
				throw new EventException("Cannot convert event to HangingBreakEvent");
			}
		}

		public boolean modifyEvent(String key, Construct value, BindableEvent event) {
			return false;
		}
	}
}
