package com.laytonsmith.core.events.drivers;

import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.PureUtilities.Geometry.Point3D;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.MCVehicle;
import com.laytonsmith.abstraction.StaticLayer;
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
import com.laytonsmith.core.constructs.CInt;
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
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.exceptions.EventException;
import com.laytonsmith.core.exceptions.PrefilterNonMatchException;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

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
				ret.put("vehicle", new CInt(e.getVehicle().getEntityId(), t));
				ret.put("passenger", new CInt(e.getEntity().getEntityId(), t));
				if (e.getEntity().getType() == MCEntityType.PLAYER) {
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
				ret.put("vehicle", new CInt(e.getVehicle().getEntityId(), t));
				ret.put("passenger", new CInt(e.getEntity().getEntityId(), t));
				if (e.getEntity().getType() == MCEntityType.PLAYER) {
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
				ret.put("id", new CInt(e.getVehicle().getEntityId(), t));
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
						entity = new CInt(vec.getEntity().getEntityId(), t);
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

	@api
	public static class vehicle_move extends AbstractEvent {

		private static Thread thread = null;
		private Set<Integer> thresholdList = new HashSet<Integer>();
		private Map<Integer, Map<Integer, MCLocation>> thresholds = new HashMap<Integer, Map<Integer, MCLocation>>();

		@Override
		public void bind(BoundEvent event) {
			Map<String, Construct> prefilters = event.getPrefilter();
			if (prefilters.containsKey("threshold")) {
				int i = Static.getInt32(prefilters.get("threshold"), Target.UNKNOWN);
				thresholdList.add(i);
			}
			if (thread == null) {
				thresholdList.add(1);
				thread = new Thread(new Runnable() {
					@Override
					public void run() {
						outerLoop:
						while (true) {
							if (thread != Thread.currentThread()) {
								//If it's a different thread, kill it.
								return;
							}

							List<MCVehicle> vehicles = null;
							try {
								vehicles = Static.getVehicles();
							} catch (ConcurrentModificationException ex) {
								continue outerLoop;
							}

							for (final MCVehicle v : vehicles) {
								final MCLocation current = ((MCEntity) v).asyncGetLocation();
								Point3D currentPoint = new Point3D(current.getX(), current.getY(), current.getZ());
								//We need to loop through all the thresholds
								//and see if any of the points meet them. If so,
								//we know we need to fire the event. If none of them
								//match, carry on with the next vehicle. As soon as
								//one matches though, we can't quit the loop, because
								//we have to set all the thresholds.
								thresholdLoop:
								for (final Integer i : thresholdList) {
									if (thresholds.containsKey(i) && thresholds.get(i).containsKey(v.getEntityId())) {
										final MCLocation last = thresholds.get(i).get(v.getEntityId());
										if (!v.getWorld().getName().equals(last.getWorld().getName())) {
											//They moved worlds. simply put their new location in here, then
											//continue.
											thresholds.get(i).put(v.getEntityId(), v.getLocation());
											continue thresholdLoop;
										}
										Point3D lastPoint = new Point3D(last.getX(), last.getY(), last.getZ());
										double distance = lastPoint.distance(currentPoint);
										if (distance > i) {
											//We've met the threshold.
											//Well, we're still not sure. To run the prefilters on this thread,
											//we're gonna simulate a prefilter match now. We have to run this manually,
											//because each bind could have a different threshold, and it will be expecting
											//THIS from location. Other binds will be expecting other from locations.
											final MCVehicleMoveEvent fakeEvent = new MCVehicleMoveEvent() {
												boolean cancelled = false;

												@Override
												public int getThreshold() {
													return i;
												}

												@Override
												public MCLocation getFrom() {
													return last;
												}

												@Override
												public MCLocation getTo() {
													return current;
												}

												@Override
												public Object _GetObject() {
													return null;
												}

												@Override
												public void setCancelled(boolean state) {
													cancelled = state;
												}

												@Override
												public boolean isCancelled() {
													return cancelled;
												}

												@Override
												public MCVehicle getVehicle() {
													return v;
												}
											};
											//We need to run the prefilters on this thread, so we have
											//to do this all by hand.
											final SortedSet<BoundEvent> toRun = EventUtils.GetMatchingEvents(Driver.VEHICLE_MOVE, vehicle_move.this.getName(), fakeEvent, vehicle_move.this);
											//Ok, now the events to be run need to actually be run on the main server thread, so let's run that now.
											try {
												StaticLayer.GetConvertor().runOnMainThreadAndWait(new Callable<Object>() {
													@Override
													public Object call() throws Exception {
														EventUtils.FireListeners(toRun, vehicle_move.this, fakeEvent);
														return null;
													}
												});
											} catch (Exception ex) {
												Logger.getLogger(VehicleEvents.class.getName()).log(Level.SEVERE, null, ex);
											}
											if (fakeEvent.isCancelled()) {
												//Put them back at the from location
												v.teleport(last);
											} else {
												thresholds.get(i).put(v.getEntityId(), current);
											}
										}
									} else {
										//If there is no location here, just put the current location in there.
										if (!thresholds.containsKey(i)) {
											thresholds.put(i, new HashMap<Integer, MCLocation>());
										}
										thresholds.get(i).put(v.getEntityId(), v.asyncGetLocation());
									}
								}
							}
							synchronized (vehicle_move.this) {
								try {
									//Throttle this thread just a little
									vehicle_move.this.wait(10);
								} catch (InterruptedException ex) {
									//
								}
							}
						}
					}
				}, Implementation.GetServerType().getBranding() + "VehicleMoveEventRunner");
				thread.start();
				StaticLayer.GetConvertor().addShutdownHook(new Runnable() {
					@Override
					public void run() {
						thresholdList.clear();
						thread = null;
					}
				});
			}
		}

		@Override
		public String getName() {
			return "vehicle_move";
		}

		@Override
		public String docs() {
			return "{vehicletype: <macro> the entitytype of the vehicle | passengertype: <macro>"
					+ " the enitytype of the passenger} Fires when an vehicle is moving."
					+ " {from: Get the previous position | to: Get the next position"
					+ " | vehicletype | passengertype | id: entityID | passenger: entityID"
					+ " | player: player name if passenger is a player, null otherwise}"
					+ " {}"
					+ " {}";
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
			if (o instanceof MCVehicleMoveEvent) {
				return ((MCVehicleMoveEvent) o).isCancelled();
			} else {
				return false;
			}
		}

		@Override
		public boolean matches(Map<String, Construct> prefilter, BindableEvent e) throws PrefilterNonMatchException {
			if (e instanceof MCVehicleMoveEvent) {
				MCVehicleMoveEvent event = (MCVehicleMoveEvent) e;

				if (!event.getFrom().getWorld().getName().equals(event.getTo().getWorld().getName())) {
					return false;
				}

				if(prefilter.containsKey("threshold")) {
					Prefilters.match(prefilter, "threshold", event.getThreshold(), PrefilterType.MATH_MATCH);
				} else if(event.getThreshold() != 1) {
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

			int id = Static.getInt32(manualObject.get("id", Target.UNKNOWN), Target.UNKNOWN);
			MCEntity e = Static.getEntity(id, Target.UNKNOWN);
			if (!(e instanceof MCVehicle)) {
				throw new ConfigRuntimeException("The id was not a vehicle",
						ExceptionType.BadEntityException, Target.UNKNOWN);
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
				Map<String, Construct> ret = evaluate_helper(e);
				ret.put("from", ObjectGenerator.GetGenerator().location(((MCVehicleMoveEvent) e).getFrom()));
				ret.put("to", ObjectGenerator.GetGenerator().location(((MCVehicleMoveEvent) e).getTo()));
				ret.put("vehicletype", new CString(e.getVehicle().getType().name(), t));
				ret.put("id", new CInt(e.getVehicle().getEntityId(), t));

				MCEntity passenger = e.getVehicle().getPassenger();

				if (passenger == null) {
					ret.put("passenger", CNull.NULL);
					ret.put("passengertype", CNull.NULL);
					ret.put("player", CNull.NULL);
				} else {

					MCEntityType passengertype = e.getVehicle().getPassenger().getType();

					ret.put("passengertype", new CString(passengertype.name(), t));
					ret.put("passenger", new CInt(passenger.getEntityId(), t));

					if (passengertype == MCEntityType.PLAYER) {
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
