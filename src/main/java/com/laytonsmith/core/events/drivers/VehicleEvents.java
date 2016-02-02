package com.laytonsmith.core.events.drivers;

import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.MCVehicle;
import com.laytonsmith.abstraction.enums.MCCollisionType;
import com.laytonsmith.abstraction.enums.MCEntityType;
import com.laytonsmith.abstraction.events.MCVehicleBlockCollideEvent;
import com.laytonsmith.abstraction.events.MCVehicleCollideEvent;
import com.laytonsmith.abstraction.events.MCVehicleEnitityCollideEvent;
import com.laytonsmith.abstraction.events.MCVehicleEnterExitEvent;
import com.laytonsmith.abstraction.events.MCVehicleMoveEvent;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.ObjectGenerator;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CBoolean;
import com.laytonsmith.core.constructs.CNull;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.Construct;
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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 *
 * @author jb_aero
 */
public class VehicleEvents {

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
		public boolean matches(Map<String, Construct> prefilter, BindableEvent event) throws PrefilterNonMatchException {
			if (event instanceof MCVehicleEnterExitEvent) {
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
		public Map<String, Construct> evaluate(BindableEvent event) throws EventException {
			if (event instanceof MCVehicleEnterExitEvent) {
				MCVehicleEnterExitEvent e = (MCVehicleEnterExitEvent) event;
				Target t = Target.UNKNOWN;
				Map<String, Construct> ret = evaluate_helper(e);
				ret.put("vehicletype", new CString(e.getVehicle().getType().name(), t));
				ret.put("passengertype", new CString(e.getEntity().getType().name(), t));
				ret.put("vehicle", new CString(e.getVehicle().getUniqueId().toString(), t));
				ret.put("passenger", new CString(e.getEntity().getUniqueId().toString(), t));
				if (e.getEntity().getType().getAbstracted() == MCEntityType.MCVanillaEntityType.PLAYER) {
					ret.put("player", new CString(((MCPlayer)e.getEntity()).getName(), t));
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
		public boolean modifyEvent(String key, Construct value, BindableEvent event) {
			return false;
		}

		@Override
		public Version since() {
			return CHVersion.V3_3_1;
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
		public boolean matches(Map<String, Construct> prefilter, BindableEvent event) throws PrefilterNonMatchException {
			if (event instanceof MCVehicleEnterExitEvent) {
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
		public Map<String, Construct> evaluate(BindableEvent event) throws EventException {
			if (event instanceof MCVehicleEnterExitEvent) {
				MCVehicleEnterExitEvent e = (MCVehicleEnterExitEvent) event;
				Target t = Target.UNKNOWN;
				Map<String, Construct> ret = evaluate_helper(e);
				ret.put("vehicletype", new CString(e.getVehicle().getType().name(), t));
				ret.put("passengertype", new CString(e.getEntity().getType().name(), t));
				ret.put("vehicle", new CString(e.getVehicle().getUniqueId().toString(), t));
				ret.put("passenger", new CString(e.getEntity().getUniqueId().toString(), t));
				if (e.getEntity().getType().getAbstracted() == MCEntityType.MCVanillaEntityType.PLAYER) {
					ret.put("player", new CString(((MCPlayer)e.getEntity()).getName(), t));
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
		public boolean modifyEvent(String key, Construct value, BindableEvent event) {
			return false;
		}

		@Override
		public Version since() {
			return CHVersion.V3_3_1;
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
			return "{type: <macro> The entitytype of the vehicle | collisiontype: <macro> One of "
					+ StringUtils.Join(MCCollisionType.values(), ", ", ", or ", " or ")
					+ " | hittype: <macro> Matches an entitytype in an enitity collision"
					+ " | hittype: <item match> Matches a block in a block collision}"
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
		public boolean matches(Map<String, Construct> prefilter, BindableEvent e) throws PrefilterNonMatchException {
			if (e instanceof MCVehicleCollideEvent) {
				MCVehicleCollideEvent event = (MCVehicleCollideEvent) e;
				Prefilters.match(prefilter, "type", event.getVehicle().getType().name(), PrefilterType.MACRO);
				Prefilters.match(prefilter, "collisiontype", event.getCollisionType().name(), PrefilterType.MACRO);
				switch (event.getCollisionType()) {
					case BLOCK:
						Prefilters.match(prefilter, "hittype", Static.ParseItemNotation(((MCVehicleBlockCollideEvent) event)
								.getBlock()), PrefilterType.ITEM_MATCH);
						break;
					case ENTITY:
						Prefilters.match(prefilter, "hittype", ((MCVehicleEnitityCollideEvent) event)
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
		public Map<String, Construct> evaluate(BindableEvent event) throws EventException {
			if (event instanceof MCVehicleCollideEvent) {
				MCVehicleCollideEvent e = (MCVehicleCollideEvent) event;
				Target t = Target.UNKNOWN;
				Map<String, Construct> ret = evaluate_helper(e);
				ret.put("type", new CString(e.getVehicle().getType().name(), t));
				ret.put("id", new CString(e.getVehicle().getUniqueId().toString(), t));
				ret.put("collisiontype", new CString(e.getCollisionType().name(), t));
				Construct block = CNull.NULL;
				Construct entity = CNull.NULL;
				boolean collide = true;
				boolean pickup = false;
				switch (e.getCollisionType()) {
					case BLOCK:
						block = ObjectGenerator.GetGenerator().location(
								((MCVehicleBlockCollideEvent) e).getBlock().getLocation());
						break;
					case ENTITY:
						MCVehicleEnitityCollideEvent vec = (MCVehicleEnitityCollideEvent) e;
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
		public boolean modifyEvent(String key, Construct value, BindableEvent event) {
			if (event instanceof MCVehicleEnitityCollideEvent) {
				MCVehicleEnitityCollideEvent e = (MCVehicleEnitityCollideEvent) event;
				if (key.equals("collide")) {
					e.setCollisionCancelled(!Static.getBoolean(value));
					return true;
				}
				if (key.equals("pickup")) {
					e.setPickupCancelled(!Static.getBoolean(value));
					return true;
				}
			}
			return false;
		}

		@Override
		public Version since() {
			return CHVersion.V3_3_1;
		}
	}

	private static final Set<Integer> thresholdList = new HashSet<>();

	public static Set<Integer> GetThresholdList(){
		return thresholdList;
	}

	private static final Map<Integer, Map<UUID, MCLocation>> lastVehicleLocations = new HashMap<>();

	public static Map<UUID, MCLocation> GetLastLocations(Integer i){
		if (!lastVehicleLocations.containsKey(i)) {
			HashMap<UUID, MCLocation> newLocation = new HashMap<>();
			lastVehicleLocations.put(i, newLocation);
			return newLocation;
		}
		return(lastVehicleLocations.get(i));
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
					+ " the enitytype of the passenger | world: <string> the world the vehicle is in}"
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
			thresholdList.clear();
			lastVehicleLocations.clear();
		}

		@Override
		public void bind(BoundEvent event) {
			int threshold = 1;
			Map<String, Construct> prefilters = event.getPrefilter();
			if(prefilters.containsKey("threshold")) {
				threshold = Static.getInt32(prefilters.get("threshold"), Target.UNKNOWN);
			}
			thresholdList.add(threshold);
		}

		@Override
		public void unbind(BoundEvent event) {
			int threshold = 1;
			Map<String, Construct> prefilters = event.getPrefilter();
			if(prefilters.containsKey("threshold")) {
				threshold = Static.getInt32(prefilters.get("threshold"), Target.UNKNOWN);
			}
			for (BoundEvent b : EventUtils.GetEvents(event.getDriver())) {
				if (b.getId().equals(event.getId())) {
					continue;
				}
				if (b.getPrefilter().containsKey("threshold")) {
					if(threshold == Static.getInt(b.getPrefilter().get("threshold"), Target.UNKNOWN)) {
						return;
					}
				}
			}
			thresholdList.remove(threshold);
			lastVehicleLocations.remove(threshold);
		}

		@Override
		public void cancel(BindableEvent o, boolean state) {
			if (o instanceof MCVehicleMoveEvent) {
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
		public boolean matches(Map<String, Construct> prefilter, BindableEvent e) throws PrefilterNonMatchException {
			if (e instanceof MCVehicleMoveEvent) {
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
				if (prefilter.containsKey("from")) {
					MCLocation pLoc = ObjectGenerator.GetGenerator().location(prefilter.get("from"), event.getVehicle().getVehicle().getWorld(), Target.UNKNOWN);
					MCLocation loc = event.getFrom();
					if (loc.getBlockX() != pLoc.getBlockX() || loc.getBlockY() != pLoc.getBlockY() || loc.getBlockZ() != pLoc.getBlockZ()) {
						return false;
					}
				}
				if (prefilter.containsKey("to")) {
					MCLocation pLoc = ObjectGenerator.GetGenerator().location(prefilter.get("to"), event.getVehicle().getVehicle().getWorld(), Target.UNKNOWN);
					MCLocation loc = event.getFrom();
					if (loc.getBlockX() != pLoc.getBlockX() || loc.getBlockY() != pLoc.getBlockY() || loc.getBlockZ() != pLoc.getBlockZ()) {
						return false;
					}
				}

				Prefilters.match(prefilter, "vehicletype", event.getVehicle().getType().name(), PrefilterType.MACRO);
				MCEntity passenger = event.getVehicle().getPassenger();
				if (passenger != null) {
					Prefilters.match(prefilter, "passengertype", passenger.getType().name(), PrefilterType.MACRO);
				}

				return true;
			}
			return false;
		}

		@Override
		public BindableEvent convert(CArray manualObject, Target t) {

			MCEntity e = Static.getEntity(manualObject.get("id", Target.UNKNOWN), Target.UNKNOWN);
			if (!(e instanceof MCVehicle)) {
				throw ConfigRuntimeException.BuildException("The id was not a vehicle",
						CREBadEntityException.class, Target.UNKNOWN);
			}

			MCLocation from = ObjectGenerator.GetGenerator().location(manualObject.get("from", Target.UNKNOWN), e.getWorld(), manualObject.getTarget());
			MCLocation to = ObjectGenerator.GetGenerator().location(manualObject.get("to", Target.UNKNOWN), e.getWorld(), manualObject.getTarget());
			return EventBuilder.instantiate(MCVehicleMoveEvent.class, e, from, to);
		}

		@Override
		public Map<String, Construct> evaluate(BindableEvent event) throws EventException {
			if (event instanceof MCVehicleMoveEvent) {
				MCVehicleMoveEvent e = (MCVehicleMoveEvent) event;
				Target t = Target.UNKNOWN;
				Map<String, Construct> ret = new HashMap<>();
				ret.put("world", new CString(e.getFrom().getWorld().getName(), t));
				ret.put("from", ObjectGenerator.GetGenerator().location(e.getFrom()));
				ret.put("to", ObjectGenerator.GetGenerator().location(e.getTo()));
				ret.put("vehicletype", new CString(e.getVehicle().getType().name(), t));
				ret.put("id", new CString(e.getVehicle().getUniqueId().toString(), t));

				MCEntity passenger = e.getVehicle().getPassenger();

				if (passenger == null) {
					ret.put("passenger", CNull.NULL);
					ret.put("passengertype", CNull.NULL);
					ret.put("player", CNull.NULL);
				} else {

					MCEntityType passengertype = e.getVehicle().getPassenger().getType();

					ret.put("passengertype", new CString(passengertype.name(), t));
					ret.put("passenger", new CString(passenger.getUniqueId().toString(), t));

					if (passengertype.getAbstracted() == MCEntityType.MCVanillaEntityType.PLAYER) {
						ret.put("player", new CString(((MCPlayer) e.getVehicle().getPassenger()).getName(), t));
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
		public boolean modifyEvent(String key, Construct value, BindableEvent event) {
			//Nothing can be modified, so always return false
			return false;
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}
}
