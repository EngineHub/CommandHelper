

package com.laytonsmith.core.events.drivers;

import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.MCProjectile;
import com.laytonsmith.abstraction.events.MCEntityDamageByEntityEvent;
import com.laytonsmith.abstraction.events.MCEntityDamageEvent;
import com.laytonsmith.abstraction.events.MCEntityTargetEvent;
import com.laytonsmith.abstraction.events.MCPlayerDropItemEvent;
import com.laytonsmith.abstraction.events.MCPlayerInteractEntityEvent;
import com.laytonsmith.abstraction.events.MCPlayerPickupItemEvent;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.ObjectGenerator;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.*;
import com.laytonsmith.core.events.*;
import com.laytonsmith.core.exceptions.EventException;
import com.laytonsmith.core.exceptions.PrefilterNonMatchException;
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
	public static class entity_damage extends AbstractEvent {

		public String getName() {
			return "entity_damage";
		}

		public String docs() {
			return "{type: <macro> The type of entity being damaged | cause: <macro>} "
				+ "Fires when any loaded entity takes damage. Using prefilters is advised when possible, as "
				+ "the event fires in rapid succession on occasion, such as when sunrise kills the undead. "
				+ "{type: The type of entity the got damaged | id: The entityID of the victim | " 
				+ "cause: The cause of damage | amount}" // | sourcetype: type of entity causing damage | "
				//+ "sourceid: the ID of the source entity} "
				+ "{amount: the amount of damage recieved (in half hearts)} "
				+ "{}";
		}

		public boolean matches(Map<String, Construct> prefilter, BindableEvent event)
				throws PrefilterNonMatchException {
			if(event instanceof MCEntityDamageEvent){
				MCEntityDamageEvent e = (MCEntityDamageEvent) event;
				Prefilters.match(prefilter, "type", e.getEntity().getType().name(), Prefilters.PrefilterType.MACRO);
				Prefilters.match(prefilter, "cause", e.getCause().name(), Prefilters.PrefilterType.MACRO);
				
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
				
				MCEntity victim = event.getEntity();
				map.put("type", new CString(victim.getType().name(), Target.UNKNOWN));
				map.put("id", new CInt(victim.getEntityId(), Target.UNKNOWN));
				map.put("cause", new CString(event.getCause().name(), Target.UNKNOWN));
				map.put("amount", new CInt(event.getDamage(), Target.UNKNOWN));
				
				/*Construct sourcetype;
				Construct sourceid;
				if (event instanceof MCEntityDamageByEntityEvent) {
					sourcetype = new CString(((MCEntityDamageByEntityEvent) event).getDamager().getType().name(), Target.UNKNOWN);
					sourceid = new CInt(((MCEntityDamageByEntityEvent) event).getDamager().getEntityId(), Target.UNKNOWN);
				} else {
					sourcetype = new CNull(Target.UNKNOWN);
					sourceid = new CNull(Target.UNKNOWN);
				}
				
				map.put("sourcetype", sourcetype);
				map.put("sourceid", sourceid);*/
				
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
			return "{} "
				+ "Fires when a player right clicks an entity. Note, not all entities are clickable"
				+ "{player: the player clicking | clicked: the entity type clicked | "
				+ "id: the id of the entity | data: if a player is clicked, this will contain their name} "
				+ "{} "
				+ "{player|clicked|id|data}";
		}

		public boolean matches(Map<String, Construct> prefilter, BindableEvent e)
				throws PrefilterNonMatchException {
			if(e instanceof MCPlayerInteractEntityEvent){
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
                
                Prefilters.match(prefilter, "item", Static.ParseItemNotation(event.getItemDrop()), Prefilters.PrefilterType.ITEM_MATCH);
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
                map.put("item", ObjectGenerator.GetGenerator().item(event.getItemDrop(), Target.UNKNOWN));
                
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
                    
                    e.setItem(stack);
                    
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
				+ "{item} "
				+ "{player|item|remaining}";
		}

		public boolean matches(Map<String, Construct> prefilter, BindableEvent e) throws PrefilterNonMatchException {
			if (e instanceof MCPlayerPickupItemEvent) {
				MCPlayerPickupItemEvent event = (MCPlayerPickupItemEvent)e;
				
				Prefilters.match(prefilter, "item", Static.ParseItemNotation(event.getItem()), Prefilters.PrefilterType.ITEM_MATCH);
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
                map.put("item", ObjectGenerator.GetGenerator().item(event.getItem(), Target.UNKNOWN));
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
					
					e.setItem(stack);
					
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
			return "{} "
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
			if(e instanceof MCEntityDamageByEntityEvent){
				MCEntityDamageByEntityEvent event = (MCEntityDamageByEntityEvent) e;
				return event.getEntity() instanceof MCPlayer;
			}
			return false;
		}

		public BindableEvent convert(CArray manualObject) {
			return null;
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
                map.put("amount",  new CInt(event.getDamage(), Target.UNKNOWN));
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
}
