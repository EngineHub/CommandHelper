package com.laytonsmith.core.events.drivers;

import java.util.Map;

import com.laytonsmith.PureUtilities.StringUtils;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.abstraction.enums.MCCollisionType;
import com.laytonsmith.abstraction.events.MCVehicleBlockCollideEvent;
import com.laytonsmith.abstraction.events.MCVehicleCollideEvent;
import com.laytonsmith.abstraction.events.MCVehicleEnitityCollideEvent;
import com.laytonsmith.abstraction.events.MCVehicleEnterExitEvent;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.ObjectGenerator;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CBoolean;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.constructs.CNull;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.events.AbstractEvent;
import com.laytonsmith.core.events.BindableEvent;
import com.laytonsmith.core.events.Driver;
import com.laytonsmith.core.events.Prefilters;
import com.laytonsmith.core.events.Prefilters.PrefilterType;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.exceptions.EventException;
import com.laytonsmith.core.exceptions.PrefilterNonMatchException;

/**
 * 
 * @author jb_aero
 */
public class VehicleEvents {

	@api
	public static class vehicle_enter extends AbstractEvent {

		public String getName() {
			return "vehicle_enter";
		}

		public String docs() {
			return "{vehicletype: <macro> the entitytype of the vehicle | passengertype: <macro>"
					+ " the enitytype of the passenger} Fires when an entity enters a vehicle."
					+ " {vehicletype | passengertype | vehicle: entityID | passenger: entityID}"
					+ " {}"
					+ " {}";
		}

		public boolean matches(Map<String, Construct> prefilter, BindableEvent event) throws PrefilterNonMatchException {
			if (event instanceof MCVehicleEnterExitEvent) {
				MCVehicleEnterExitEvent e = (MCVehicleEnterExitEvent) event;
				Prefilters.match(prefilter, "vehicletype", e.getVehicle().getType().name(), PrefilterType.MACRO);
				Prefilters.match(prefilter, "passengertype", e.getEntity().getType().name(), PrefilterType.MACRO);
				return true;
			}
			return false;
		}

		public BindableEvent convert(CArray manualObject) {
			throw new ConfigRuntimeException("Unsupported Operation", Target.UNKNOWN);
		}

		public Map<String, Construct> evaluate(BindableEvent event) throws EventException {
			if (event instanceof MCVehicleEnterExitEvent) {
				MCVehicleEnterExitEvent e = (MCVehicleEnterExitEvent) event;
				Target t = Target.UNKNOWN;
				Map<String, Construct> ret = evaluate_helper(e);
				ret.put("vehicletype", new CString(e.getVehicle().getType().name(), t));
				ret.put("passengertype", new CString(e.getEntity().getType().name(), t));
				ret.put("vehicle", new CInt(e.getVehicle().getEntityId(), t));
				ret.put("passenger", new CInt(e.getEntity().getEntityId(), t));
				return ret;
			} else {
				throw new EventException("Could not convert to MCVehicleEnterExitEvent");
			}
		}

		public Driver driver() {
			return Driver.VEHICLE_ENTER;
		}

		public boolean modifyEvent(String key, Construct value, BindableEvent event) {
			return false;
		}

		public Version since() {
			return CHVersion.V3_3_1;
		}
	}
	
	//@api
	public static class vehicle_leave extends AbstractEvent {

		public String getName() {
			return "vehicle_leave";
		}

		public String docs() {
			return "{vehicletype: <macro> the entitytype of the vehicle | passengertype: <macro>"
					+ " the enitytype of the passenger} Fires when an entity leaves a vehicle."
					+ " {vehicletype | passengertype | vehicle: entityID | passenger: entityID}"
					+ " {}"
					+ " {}";
		}

		public boolean matches(Map<String, Construct> prefilter, BindableEvent event) throws PrefilterNonMatchException {
			if (event instanceof MCVehicleEnterExitEvent) {
				MCVehicleEnterExitEvent e = (MCVehicleEnterExitEvent) event;
				Prefilters.match(prefilter, "vehicletype", e.getVehicle().getType().name(), PrefilterType.MACRO);
				Prefilters.match(prefilter, "passengertype", e.getEntity().getType().name(), PrefilterType.MACRO);
				return true;
			}
			return false;
		}

		public BindableEvent convert(CArray manualObject) {
			throw new ConfigRuntimeException("Unsupported Operation", Target.UNKNOWN);
		}

		public Map<String, Construct> evaluate(BindableEvent event) throws EventException {
			if (event instanceof MCVehicleEnterExitEvent) {
				MCVehicleEnterExitEvent e = (MCVehicleEnterExitEvent) event;
				Target t = Target.UNKNOWN;
				Map<String, Construct> ret = evaluate_helper(e);
				ret.put("vehicletype", new CString(e.getVehicle().getType().name(), t));
				ret.put("passengertype", new CString(e.getEntity().getType().name(), t));
				ret.put("vehicle", new CInt(e.getVehicle().getEntityId(), t));
				ret.put("passenger", new CInt(e.getEntity().getEntityId(), t));
				return ret;
			} else {
				throw new EventException("Could not convert to MCVehicleEnterExitEvent");
			}
		}

		public Driver driver() {
			return Driver.VEHICLE_LEAVE;
		}

		public boolean modifyEvent(String key, Construct value, BindableEvent event) {
			return false;
		}

		public Version since() {
			return CHVersion.V3_3_1;
		}
	}
	
	@api
	public static class vehicle_collide extends AbstractEvent {

		public String getName() {
			return "vehicle_collide";
		}

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
							throw new ConfigRuntimeException("Greetings from the future! If you are seeing this message,"
									+ " Minecraft has reached the point where vehicles can hit things that are neither"
									+ " a block nor an entity.", Target.UNKNOWN);
				}
				return true;
			}
			return false;
		}

		public BindableEvent convert(CArray manualObject) {
			throw new ConfigRuntimeException("Unsupported Operation", Target.UNKNOWN);
		}

		public Map<String, Construct> evaluate(BindableEvent event) throws EventException {
			if (event instanceof MCVehicleCollideEvent) {
				MCVehicleCollideEvent e = (MCVehicleCollideEvent) event;
				Target t = Target.UNKNOWN;
				Map<String, Construct> ret = evaluate_helper(e);
				ret.put("type", new CString(e.getVehicle().getType().name(), t));
				ret.put("id", new CInt(e.getVehicle().getEntityId(), t));
				ret.put("collisiontype", new CString(e.getCollisionType().name(), t));
				Construct block = new CNull(t);
				Construct entity = new CNull(t);
				boolean collide = true;
				boolean pickup = false;
				switch (e.getCollisionType()) {
					case BLOCK:
						block = ObjectGenerator.GetGenerator().location(
								((MCVehicleBlockCollideEvent) e).getBlock().getLocation());
						break;
					case ENTITY:
						MCVehicleEnitityCollideEvent vec = (MCVehicleEnitityCollideEvent) e;
						entity = new CInt(vec.getEntity().getEntityId(), t);
						collide = !vec.isCollisionCancelled();
						pickup = !vec.isPickupCancelled();
						break;
						default:
							throw new ConfigRuntimeException("Greetings from the future! If you are seeing this message,"
									+ " Minecraft has reached the point where vehicles can hit things that are neither"
									+ " a block nor an entity.", t);
				}
				ret.put("block", block);
				ret.put("entity", entity);
				ret.put("pickup", new CBoolean(pickup, t));
				ret.put("collide", new CBoolean(collide, t));
				return ret;
			} else {
				throw new EventException("The event could not be converted to MCVehicleCollideEvent.");
			}
		}

		public Driver driver() {
			return Driver.VEHICLE_COLLIDE;
		}

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

		public Version since() {
			return CHVersion.V3_3_1;
		}
	}
}
