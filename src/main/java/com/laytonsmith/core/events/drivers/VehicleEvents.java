package com.laytonsmith.core.events.drivers;

import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.entities.MCVehicle;
import com.laytonsmith.abstraction.entities.MCProjectile;
import com.laytonsmith.abstraction.MCProjectileSource;
import com.laytonsmith.abstraction.blocks.MCBlockProjectileSource;
import com.laytonsmith.abstraction.enums.MCCollisionType;
import com.laytonsmith.abstraction.enums.MCEntityType;
import com.laytonsmith.abstraction.events.MCVehicleBlockCollideEvent;
import com.laytonsmith.abstraction.events.MCVehicleCollideEvent;
import com.laytonsmith.abstraction.events.MCVehicleEntityCollideEvent;
import com.laytonsmith.abstraction.events.MCVehicleEnterExitEvent;
import com.laytonsmith.abstraction.events.MCVehicleMoveEvent;
import com.laytonsmith.abstraction.events.MCVehicleDestroyEvent;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.ArgumentValidation;
import com.laytonsmith.core.MSLog;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.ObjectGenerator;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CBoolean;
import com.laytonsmith.core.constructs.CNull;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.events.AbstractEvent;
import com.laytonsmith.core.events.BindableEvent;
import com.laytonsmith.core.events.BoundEvent;
import com.laytonsmith.core.events.Driver;
import com.laytonsmith.core.events.EventBuilder;
import com.laytonsmith.core.events.EventUtils;
import com.laytonsmith.core.events.Prefilters;
import com.laytonsmith.core.events.Prefilters.PrefilterType;
import com.laytonsmith.core.exceptions.CRE.CREBadEntityException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.exceptions.EventException;
import com.laytonsmith.core.exceptions.PrefilterNonMatchException;
import com.laytonsmith.core.natives.interfaces.Mixed;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class VehicleEvents {

	public static String docs() {
		return "Contains events related to vehicle entity types.";
	}

	@api
	public static class vehicle_enter extends AbstractEvent {

		@Override
		public String getName() {
			return "vehicle_enter";
		}

		@Override
		public String docs() {
			return "{vehicletype: <macro> the entitytype of the vehicle | passengertype: <macro>"
					+ " the enitytype of the passenger} Fires when an entity enters a vehicle."
					+ " {vehicletype | passengertype | vehicle: entityID | passenger: entityID"
					+ " | player: player name if passenger is a player, null otherwise}"
					+ " {}"
					+ " {}";
		}

		@Override
		public boolean matches(Map<String, Mixed> prefilter, BindableEvent event) throws PrefilterNonMatchException {
			if(event instanceof MCVehicleEnterExitEvent) {
				MCVehicleEnterExitEvent e = (MCVehicleEnterExitEvent) event;
				Prefilters.match(prefilter, "vehicletype", e.getVehicle().getType().name(), PrefilterType.MACRO);
				Prefilters.match(prefilter, "passengertype", e.getEntity().getType().name(), PrefilterType.MACRO);
				return true;
			}
			return false;
		}

		@Override
		public BindableEvent convert(CArray manualObject, Target t) {
			throw ConfigRuntimeException.CreateUncatchableException("Unsupported Operation", Target.UNKNOWN);
		}

		@Override
		public Map<String, Mixed> evaluate(BindableEvent event) throws EventException {
			if(event instanceof MCVehicleEnterExitEvent) {
				MCVehicleEnterExitEvent e = (MCVehicleEnterExitEvent) event;
				Target t = Target.UNKNOWN;
				Map<String, Mixed> ret = evaluate_helper(e);
				ret.put("vehicletype", new CString(e.getVehicle().getType().name(), t));
				ret.put("passengertype", new CString(e.getEntity().getType().name(), t));
				ret.put("vehicle", new CString(e.getVehicle().getUniqueId().toString(), t));
				ret.put("passenger", new CString(e.getEntity().getUniqueId().toString(), t));
				if(e.getEntity().getType().getAbstracted() == MCEntityType.MCVanillaEntityType.PLAYER) {
					ret.put("player", new CString(((MCPlayer) e.getEntity()).getName(), t));
				} else {
					ret.put("player", CNull.NULL);
				}
				return ret;
			} else {
				throw new EventException("Could not convert to MCVehicleEnterExitEvent");
			}
		}

		@Override
		public Driver driver() {
			return Driver.VEHICLE_ENTER;
		}

		@Override
		public boolean modifyEvent(String key, Mixed value, BindableEvent event) {
			return false;
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}
	}

	@api
	public static class vehicle_leave extends AbstractEvent {

		@Override
		public String getName() {
			return "vehicle_leave";
		}

		@Override
		public String docs() {
			return "{vehicletype: <macro> the entitytype of the vehicle | passengertype: <macro>"
					+ " the enitytype of the passenger} Fires when an entity leaves a vehicle."
					+ " {vehicletype | passengertype | vehicle: entityID | passenger: entityID"
					+ " | player: player name if passenger is a player, null otherwise}"
					+ " {}"
					+ " {}";
		}

		@Override
		public boolean matches(Map<String, Mixed> prefilter, BindableEvent event) throws PrefilterNonMatchException {
			if(event instanceof MCVehicleEnterExitEvent) {
				MCVehicleEnterExitEvent e = (MCVehicleEnterExitEvent) event;
				Prefilters.match(prefilter, "vehicletype", e.getVehicle().getType().name(), PrefilterType.MACRO);
				Prefilters.match(prefilter, "passengertype", e.getEntity().getType().name(), PrefilterType.MACRO);
				return true;
			}
			return false;
		}

		@Override
		public BindableEvent convert(CArray manualObject, Target t) {
			throw ConfigRuntimeException.CreateUncatchableException("Unsupported Operation", Target.UNKNOWN);
		}

		@Override
		public Map<String, Mixed> evaluate(BindableEvent event) throws EventException {
			if(event instanceof MCVehicleEnterExitEvent) {
				MCVehicleEnterExitEvent e = (MCVehicleEnterExitEvent) event;
				Target t = Target.UNKNOWN;
				Map<String, Mixed> ret = evaluate_helper(e);
				ret.put("vehicletype", new CString(e.getVehicle().getType().name(), t));
				ret.put("passengertype", new CString(e.getEntity().getType().name(), t));
				ret.put("vehicle", new CString(e.getVehicle().getUniqueId().toString(), t));
				ret.put("passenger", new CString(e.getEntity().getUniqueId().toString(), t));
				if(e.getEntity().getType().getAbstracted() == MCEntityType.MCVanillaEntityType.PLAYER) {
					ret.put("player", new CString(((MCPlayer) e.getEntity()).getName(), t));
				} else {
					ret.put("player", CNull.NULL);
				}
				return ret;
			} else {
				throw new EventException("Could not convert to MCVehicleEnterExitEvent");
			}
		}

		@Override
		public Driver driver() {
			return Driver.VEHICLE_LEAVE;
		}

		@Override
		public boolean modifyEvent(String key, Mixed value, BindableEvent event) {
			return false;
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}
	}

	@api
	public static class vehicle_collide extends AbstractEvent {

		@Override
		public String getName() {
			return "vehicle_collide";
		}

		@Override
		public String docs() {
			return "{type: <macro> The entitytype of the vehicle | collisiontype: <string match> One of "
					+ StringUtils.Join(MCCollisionType.values(), ", ", ", or ", " or ")
					+ " | hittype: <macro> Matches an entitytype in an enitity collision"
					+ " | hittype: <string match> Matches a block in a block collision}"
					+ " Fires when a vehicle runs into something. If it ran into a block,"
					+ " event data will contain block info. If it ran into an entity,"
					+ " event data will contain info and options relevant to hitting an entity."
					+ " {type | id: The entityID of the vehicle | entity: the entityID of the entity that was hit"
					+ " | block: the location of the block that was hit | collisiontype | collide | pickup}"
					+ " {collide: whether the vehicle hits the entity or passes through it | pickup: whether or not the"
					+ " vehicle pick up the entity | both fields can only be modified for entity collisions}"
					+ " {}";
		}

		@Override
		@SuppressWarnings("deprecation")
		public void bind(BoundEvent event) {
			// handle deprecated prefilter
			Map<String, Mixed> prefilter = event.getPrefilter();
			if(prefilter.containsKey("hittype")) {
				Mixed type = prefilter.get("hittype");
				if(type.isInstanceOf(CString.TYPE) && type.val().contains(":") || ArgumentValidation.isNumber(type)) {
					MSLog.GetLogger().w(MSLog.Tags.DEPRECATION, "The 0:0 block format in " + getName()
							+ " is deprecated in \"hittype\".", event.getTarget());
					MCItemStack is = Static.ParseItemNotation(null, type.val(), 1, event.getTarget());
					prefilter.put("hittype", new CString(is.getType().getName(), event.getTarget()));
				}
			}
		}

		@Override
		public boolean matches(Map<String, Mixed> prefilter, BindableEvent e) throws PrefilterNonMatchException {
			if(e instanceof MCVehicleCollideEvent) {
				MCVehicleCollideEvent event = (MCVehicleCollideEvent) e;
				Prefilters.match(prefilter, "type", event.getVehicle().getType().name(), PrefilterType.MACRO);
				if(prefilter.containsKey("collisiontype")) {
					if(!event.getCollisionType().name().equals(prefilter.get("collisiontype").val())) {
						return false;
					}
				}
				switch(event.getCollisionType()) {
					case BLOCK:
						Prefilters.match(prefilter, "hittype",
								((MCVehicleBlockCollideEvent) event).getBlock().getType().getName(),
								PrefilterType.STRING_MATCH);
						break;
					case ENTITY:
						Prefilters.match(prefilter, "hittype", ((MCVehicleEntityCollideEvent) event)
								.getEntity().getType().name(), PrefilterType.MACRO);
						break;
					default:
						throw ConfigRuntimeException.CreateUncatchableException("Greetings from the future! If you are seeing this message,"
								+ " Minecraft has reached the point where vehicles can hit things that are neither"
								+ " a block nor an entity. Please report this error to developers.", Target.UNKNOWN);
				}
				return true;
			}
			return false;
		}

		@Override
		public BindableEvent convert(CArray manualObject, Target t) {
			throw ConfigRuntimeException.CreateUncatchableException("Unsupported Operation", Target.UNKNOWN);
		}

		@Override
		public Map<String, Mixed> evaluate(BindableEvent event) throws EventException {
			if(event instanceof MCVehicleCollideEvent) {
				MCVehicleCollideEvent e = (MCVehicleCollideEvent) event;
				Target t = Target.UNKNOWN;
				Map<String, Mixed> ret = evaluate_helper(e);
				ret.put("type", new CString(e.getVehicle().getType().name(), t));
				ret.put("id", new CString(e.getVehicle().getUniqueId().toString(), t));
				ret.put("collisiontype", new CString(e.getCollisionType().name(), t));
				Mixed block = CNull.NULL;
				Mixed entity = CNull.NULL;
				boolean collide = true;
				boolean pickup = false;
				switch(e.getCollisionType()) {
					case BLOCK:
						block = ObjectGenerator.GetGenerator().location(
								((MCVehicleBlockCollideEvent) e).getBlock().getLocation());
						break;
					case ENTITY:
						MCVehicleEntityCollideEvent vec = (MCVehicleEntityCollideEvent) e;
						entity = new CString(vec.getEntity().getUniqueId().toString(), t);
						collide = !vec.isCollisionCancelled();
						pickup = !vec.isPickupCancelled();
						break;
					default:
						throw ConfigRuntimeException.CreateUncatchableException("Greetings from the future! If you are seeing this message,"
								+ " Minecraft has reached the point where vehicles can hit things that are neither"
								+ " a block nor an entity. Please report this error to developers.", t);
				}
				ret.put("block", block);
				ret.put("entity", entity);
				ret.put("pickup", CBoolean.get(pickup));
				ret.put("collide", CBoolean.get(collide));
				return ret;
			} else {
				throw new EventException("The event could not be converted to MCVehicleCollideEvent.");
			}
		}

		@Override
		public Driver driver() {
			return Driver.VEHICLE_COLLIDE;
		}

		@Override
		public boolean modifyEvent(String key, Mixed value, BindableEvent event) {
			if(event instanceof MCVehicleEntityCollideEvent) {
				MCVehicleEntityCollideEvent e = (MCVehicleEntityCollideEvent) event;
				if(key.equals("collide")) {
					e.setCollisionCancelled(!ArgumentValidation.getBoolean(value, Target.UNKNOWN));
					return true;
				}
				if(key.equals("pickup")) {
					e.setPickupCancelled(!ArgumentValidation.getBoolean(value, Target.UNKNOWN));
					return true;
				}
			}
			return false;
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}
	}

	private static final Set<Integer> THRESHOLD_LIST = new HashSet<>();

	public static Set<Integer> GetThresholdList() {
		return THRESHOLD_LIST;
	}

	private static final Map<Integer, Map<UUID, MCLocation>> LAST_VEHICLE_LOCATIONS = new HashMap<>();

	public static Map<UUID, MCLocation> GetLastLocations(Integer i) {
		if(!LAST_VEHICLE_LOCATIONS.containsKey(i)) {
			HashMap<UUID, MCLocation> newLocation = new HashMap<>();
			LAST_VEHICLE_LOCATIONS.put(i, newLocation);
			return newLocation;
		}
		return (LAST_VEHICLE_LOCATIONS.get(i));
	}

	@api
	public static class vehicle_move extends AbstractEvent {

		@Override
		public String getName() {
			return "vehicle_move";
		}

		@Override
		public String docs() {
			return "{vehicletype: <macro> the entitytype of the vehicle | passengertype: <macro>"
					+ " the enitytype of the passenger | world: <string match> the world the vehicle is in"
					+ "| from: <location match> This should be a location array (x, y, z, world)."
					+ "| to: <location match> The location the vehicle is now in."
					+ "| threshold: <custom> The minimum distance the vehicle must have travelled before the event"
					+ " will be triggered. This is based on the 3D distance, and is measured in block units.}"
					+ " Fires when a vehicle is moving. Due to the high frequency of this event, prefilters are"
					+ " extremely important to use -- especially threshold."
					+ "{world | from: Get the previous position | to: Get the next position"
					+ " | vehicletype | passengertype | id: entityID | passenger: entityID"
					+ " | player: player name if passenger is a player, null otherwise}"
					+ " {}"
					+ " {}";
		}

		@Override
		public void hook() {
			THRESHOLD_LIST.clear();
			LAST_VEHICLE_LOCATIONS.clear();
		}

		@Override
		public void bind(BoundEvent event) {
			int threshold = 1;
			Map<String, Mixed> prefilters = event.getPrefilter();
			if(prefilters.containsKey("threshold")) {
				threshold = Static.getInt32(prefilters.get("threshold"), Target.UNKNOWN);
			}
			THRESHOLD_LIST.add(threshold);
		}

		@Override
		public void unbind(BoundEvent event) {
			int threshold = 1;
			Map<String, Mixed> prefilters = event.getPrefilter();
			if(prefilters.containsKey("threshold")) {
				threshold = Static.getInt32(prefilters.get("threshold"), Target.UNKNOWN);
			}
			for(BoundEvent b : EventUtils.GetEvents(event.getDriver())) {
				if(b.getId().equals(event.getId())) {
					continue;
				}
				if(b.getPrefilter().containsKey("threshold")) {
					if(threshold == Static.getInt(b.getPrefilter().get("threshold"), Target.UNKNOWN)) {
						return;
					}
				}
			}
			THRESHOLD_LIST.remove(threshold);
			LAST_VEHICLE_LOCATIONS.remove(threshold);
		}

		@Override
		public void cancel(BindableEvent o, boolean state) {
			if(o instanceof MCVehicleMoveEvent) {
				((MCVehicleMoveEvent) o).setCancelled(state);
			}
		}

		@Override
		public boolean isCancellable(BindableEvent o) {
			return true;
		}

		@Override
		public boolean isCancelled(BindableEvent o) {
			return o instanceof MCVehicleMoveEvent && ((MCVehicleMoveEvent) o).isCancelled();
		}

		@Override
		public boolean matches(Map<String, Mixed> prefilter, BindableEvent e) throws PrefilterNonMatchException {
			if(e instanceof MCVehicleMoveEvent) {
				MCVehicleMoveEvent event = (MCVehicleMoveEvent) e;
				if(prefilter.containsKey("threshold")) {
					if(Static.getInt(prefilter.get("threshold"), Target.UNKNOWN) != event.getThreshold()) {
						return false;
					}
				} else if(event.getThreshold() != 1) {
					return false;
				}
				if(prefilter.containsKey("world")
						&& !prefilter.get("world").val().equals(event.getFrom().getWorld().getName())) {
					return false;
				}
				if(prefilter.containsKey("from")) {
					MCLocation pLoc = ObjectGenerator.GetGenerator().location(prefilter.get("from"), event.getVehicle().getVehicle().getWorld(), Target.UNKNOWN);
					MCLocation loc = event.getFrom();
					if(loc.getBlockX() != pLoc.getBlockX() || loc.getBlockY() != pLoc.getBlockY() || loc.getBlockZ() != pLoc.getBlockZ()) {
						return false;
					}
				}
				if(prefilter.containsKey("to")) {
					MCLocation pLoc = ObjectGenerator.GetGenerator().location(prefilter.get("to"), event.getVehicle().getVehicle().getWorld(), Target.UNKNOWN);
					MCLocation loc = event.getFrom();
					if(loc.getBlockX() != pLoc.getBlockX() || loc.getBlockY() != pLoc.getBlockY() || loc.getBlockZ() != pLoc.getBlockZ()) {
						return false;
					}
				}

				Prefilters.match(prefilter, "vehicletype", event.getVehicle().getType().name(), PrefilterType.MACRO);
				List<MCEntity> passengers = event.getVehicle().getPassengers();
				if(!passengers.isEmpty()) {
					Prefilters.match(prefilter, "passengertype", passengers.get(0).getType().name(), PrefilterType.MACRO);
				}

				return true;
			}
			return false;
		}

		@Override
		public BindableEvent convert(CArray manualObject, Target t) {

			MCEntity e = Static.getEntity(manualObject.get("id", Target.UNKNOWN), Target.UNKNOWN);
			if(!(e instanceof MCVehicle)) {
				throw new CREBadEntityException("The id was not a vehicle", Target.UNKNOWN);
			}

			MCLocation from = ObjectGenerator.GetGenerator().location(manualObject.get("from", Target.UNKNOWN), e.getWorld(), manualObject.getTarget());
			MCLocation to = ObjectGenerator.GetGenerator().location(manualObject.get("to", Target.UNKNOWN), e.getWorld(), manualObject.getTarget());
			return EventBuilder.instantiate(MCVehicleMoveEvent.class, e, from, to);
		}

		@Override
		public Map<String, Mixed> evaluate(BindableEvent event) throws EventException {
			if(event instanceof MCVehicleMoveEvent) {
				MCVehicleMoveEvent e = (MCVehicleMoveEvent) event;
				Target t = Target.UNKNOWN;
				Map<String, Mixed> ret = new HashMap<>();
				ret.put("world", new CString(e.getFrom().getWorld().getName(), t));
				ret.put("from", ObjectGenerator.GetGenerator().location(e.getFrom()));
				ret.put("to", ObjectGenerator.GetGenerator().location(e.getTo()));
				ret.put("vehicletype", new CString(e.getVehicle().getType().name(), t));
				ret.put("id", new CString(e.getVehicle().getUniqueId().toString(), t));

				List<MCEntity> passengers = e.getVehicle().getPassengers();

				if(passengers.isEmpty()) {
					ret.put("passenger", CNull.NULL);
					ret.put("passengertype", CNull.NULL);
					ret.put("player", CNull.NULL);
				} else {
					MCEntity passenger = passengers.get(0);
					MCEntityType<?> passengertype = passenger.getType();

					ret.put("passengertype", new CString(passengertype.name(), t));
					ret.put("passenger", new CString(passenger.getUniqueId().toString(), t));

					if(passengertype.getAbstracted() == MCEntityType.MCVanillaEntityType.PLAYER) {
						ret.put("player", new CString(((MCPlayer) passenger).getName(), t));
					} else {
						ret.put("player", CNull.NULL);
					}
				}

				return ret;
			} else {
				throw new EventException("Could not convert to MCVehicleMoveEvent");
			}
		}

		@Override
		public Driver driver() {
			return Driver.VEHICLE_MOVE;
		}

		@Override
		public boolean modifyEvent(String key, Mixed value, BindableEvent event) {
			//Nothing can be modified, so always return false
			return false;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}
	}

	@api
	public static class vehicle_destroy extends AbstractEvent {

		@Override
		public String getName() {
			return "vehicle_destroy";
		}

		@Override
		public String docs() {
			return "{vehicletype: <macro> the entitytype of the vehicle} "
					+ "Fires when a vehicle is destroyed."
					+ " {vehicletype | vehicle: entityID"
					+ " | damager: If the source of damage is a player this will contain their name, otherwise it will"
					+ " be the entityID of the damager (only available when an entity causes damage)"
					+ " | shooter: The name of the player who shot, otherwise the entityID"
					+ " (only available when damager is a projectile)}"
					+ " {}"
					+ " {}";
		}

		@Override
		public boolean matches(Map<String, Mixed> prefilter, BindableEvent event) throws PrefilterNonMatchException {
			if(event instanceof MCVehicleDestroyEvent) {
				MCVehicleDestroyEvent e = (MCVehicleDestroyEvent) event;
				Prefilters.match(prefilter, "vehicletype", e.getVehicle().getType().name(), PrefilterType.MACRO);
				return true;
			}
			return false;
		}

		@Override
		public BindableEvent convert(CArray manualObject, Target t) {
			throw ConfigRuntimeException.CreateUncatchableException("Unsupported Operation", Target.UNKNOWN);
		}

		@Override
		public Map<String, Mixed> evaluate(BindableEvent event) throws EventException {
			if(event instanceof MCVehicleDestroyEvent) {
				MCVehicleDestroyEvent e = (MCVehicleDestroyEvent) event;
				Target t = Target.UNKNOWN;
				Map<String, Mixed> ret = evaluate_helper(e);
				ret.put("vehicletype", new CString(e.getVehicle().getType().name(), t));
				ret.put("vehicle", new CString(e.getVehicle().getUniqueId().toString(), t));
				MCEntity damager = ((MCVehicleDestroyEvent) event).getAttacker();
				if(damager instanceof MCPlayer) {
					ret.put("damager", new CString(((MCPlayer) damager).getName(), Target.UNKNOWN));
				} else if(damager != null) {
					ret.put("damager", new CString(damager.getUniqueId().toString(), Target.UNKNOWN));
				}
				if(damager instanceof MCProjectile) {
					MCProjectileSource shooter = ((MCProjectile) damager).getShooter();
					if(shooter instanceof MCPlayer) {
						ret.put("shooter", new CString(((MCPlayer) shooter).getName(), Target.UNKNOWN));
					} else if(shooter instanceof MCEntity) {
						ret.put("shooter", new CString(((MCEntity) shooter).getUniqueId().toString(), Target.UNKNOWN));
					} else if(shooter instanceof MCBlockProjectileSource) {
						ret.put("shooter", ObjectGenerator.GetGenerator().location(((MCBlockProjectileSource) shooter).getBlock().getLocation()));
					}
				}
				ret.put("location", ObjectGenerator.GetGenerator().location(e.getVehicle().getLocation()));
				return ret;
			} else {
				throw new EventException("Could not convert to MCVehicleDestroyEvent");
			}
		}

		@Override
		public Driver driver() {
			return Driver.VEHICLE_DESTROY;
		}

		@Override
		public boolean modifyEvent(String key, Mixed value, BindableEvent event) {
			return false;
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_2;
		}
	}
}
