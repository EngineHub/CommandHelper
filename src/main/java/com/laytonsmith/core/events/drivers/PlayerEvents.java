package com.laytonsmith.core.events.drivers;

import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.abstraction.MCBookMeta;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.MCWorld;
import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.abstraction.blocks.MCBlockFace;
import com.laytonsmith.abstraction.enums.MCAction;
import com.laytonsmith.abstraction.enums.MCFishingState;
import com.laytonsmith.abstraction.enums.MCGameMode;
import com.laytonsmith.abstraction.enums.MCTeleportCause;
import com.laytonsmith.abstraction.events.*;
import com.laytonsmith.annotations.api;
import com.laytonsmith.annotations.hide;
import com.laytonsmith.commandhelper.CommandHelperPlugin;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.ObjectGenerator;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CBoolean;
import com.laytonsmith.core.constructs.CDouble;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.constructs.CNull;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.CommandHelperEnvironment;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.events.AbstractEvent;
import com.laytonsmith.core.events.BindableEvent;
import com.laytonsmith.core.events.BoundEvent;
import com.laytonsmith.core.events.BoundEvent.ActiveEvent;
import com.laytonsmith.core.events.Driver;
import com.laytonsmith.core.events.EventBuilder;
import com.laytonsmith.core.events.EventUtils;
import com.laytonsmith.core.events.Prefilters;
import com.laytonsmith.core.events.Prefilters.PrefilterType;
import com.laytonsmith.core.events.drivers.EntityEvents.entity_death;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.exceptions.EventException;
import com.laytonsmith.core.exceptions.PrefilterNonMatchException;
import com.laytonsmith.core.functions.Exceptions;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;
import com.laytonsmith.core.functions.StringHandling;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IllegalFormatConversionException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UnknownFormatConversionException;

/**
 *
 *
 */
public class PlayerEvents {
    public static String docs(){
        return "Contains events related to a player";
    }
	
	@api
	public static class food_level_changed extends AbstractEvent {

		@Override
		public String getName() {
			return "food_level_changed";
		}

		@Override
		public String docs() {
			return "{player: <string match>}"
					+ " Fires as a player's food level changes."
					+ " Cancelling the event will cause the change to not be"
					+ " applied."
					+ " {player: the player | level: the new food level to be applied"
					+ " | difference: the difference between the old level and the new }"
					+ " {level: A different level to be applied }"
					+ " {}";
		}

		@Override
		public boolean matches(Map<String, Construct> prefilter, BindableEvent e)
				throws PrefilterNonMatchException {
			if (e instanceof MCFoodLevelChangeEvent) {
				
				MCFoodLevelChangeEvent event = (MCFoodLevelChangeEvent) e;
				Prefilters.match(prefilter, "player", event.getEntity().getName(), PrefilterType.STRING_MATCH);
				
				return true;
			}
			
			return false;
		}

		@Override
		public BindableEvent convert(CArray manualObject, Target t) {
			return null;
		}

		@Override
		public Map<String, Construct> evaluate(BindableEvent e)
				throws EventException {
			if (e instanceof MCFoodLevelChangeEvent) {
				Map<String, Construct> ret = evaluate_helper(e);
				MCFoodLevelChangeEvent event = (MCFoodLevelChangeEvent) e;
				
				ret.put("player", new CString(event.getEntity().getName(), Target.UNKNOWN));
				ret.put("level", new CInt(event.getFoodLevel(), Target.UNKNOWN));
				ret.put("difference", new CInt(event.getDifference(), Target.UNKNOWN));
				
				return ret;
			} else {
				throw new EventException("Cannot convert to MCFoodLevelChangeEvent");
			}
		}

		@Override
		public Driver driver() {
			return Driver.FOOD_LEVEL_CHANGED;
		}

		@Override
		public boolean modifyEvent(String key, Construct value,
				BindableEvent event) {
			if (event instanceof MCFoodLevelChangeEvent) {
				MCFoodLevelChangeEvent e = (MCFoodLevelChangeEvent) event;
				
				if (key.equalsIgnoreCase("level")) {
					e.setFoodLevel(Static.getInt32(value, Target.UNKNOWN));
					return true;
				}
			}
			
			return false;
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}

	@api
	public static class player_consume extends AbstractEvent {

		@Override
		public String getName() {
			return "player_consume";
		}

		@Override
		public String docs() {
			return "{item: <item match>}"
					+ " Fires as a player is finishing eating/drinking an item."
					+ " Cancelling the event will cause any effects to not be"
					+ " applied and the item to not be taken from the player."
					+ " {player: the player consuming | item: the item being consumed}"
					+ " {item: A different item to be consumed, changing this will"
					+ " cause the original item to remain in the inventory}"
					+ " {player|item}";
		}

		@Override
		public boolean matches(Map<String, Construct> prefilter, BindableEvent e)
				throws PrefilterNonMatchException {
			if (e instanceof MCPlayerItemConsumeEvent) {
				MCPlayerItemConsumeEvent event = (MCPlayerItemConsumeEvent) e;
				Prefilters.match(prefilter, "item", Static.ParseItemNotation(event.getItem()), PrefilterType.ITEM_MATCH);
				return true;
			}
			return false;
		}

		@Override
		public BindableEvent convert(CArray manualObject, Target t) {
			MCPlayer p = Static.GetPlayer(manualObject.get("player", Target.UNKNOWN), Target.UNKNOWN);
			MCItemStack i = ObjectGenerator.GetGenerator().item(manualObject.get("item", Target.UNKNOWN), Target.UNKNOWN);
			return EventBuilder.instantiate(MCPlayerItemConsumeEvent.class, p, i);
		}

		@Override
		public Map<String, Construct> evaluate(BindableEvent event)
				throws EventException {
			if (event instanceof MCPlayerItemConsumeEvent) {
				MCPlayerItemConsumeEvent e = (MCPlayerItemConsumeEvent) event;
				Map<String, Construct> ret = evaluate_helper(e);
				Construct item = ObjectGenerator.GetGenerator().item(e.getItem(), Target.UNKNOWN);
				ret.put("item", item);
				return ret;
			} else {
				throw new EventException("Cannot convert to MCPlayerItemConsumeEvent");
			}
		}

		@Override
		public Driver driver() {
			return Driver.PLAYER_CONSUME;
		}

		@Override
		public boolean modifyEvent(String key, Construct value,
				BindableEvent event) {
			if (event instanceof MCPlayerItemConsumeEvent) {
				MCPlayerItemConsumeEvent e = (MCPlayerItemConsumeEvent) event;
				if (key.equalsIgnoreCase("item")) {
					e.setItem(ObjectGenerator.GetGenerator().item(value, Target.UNKNOWN));
					return true;
				}
			}
			return false;
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

	}

    @api
    public static class player_kick extends AbstractEvent {

		@Override
        public String getName() {
            return "player_kick";
        }

		@Override
        public String docs() {
            return "{player: <macro> | reason: <macro>}"
                    + "Fired when a player is kicked from the game. "
                    + "{player: the kicked player | message: the message shown to all online"
                    + " players | reason: the message shown to the player getting kicked}"
                    + "{message|reason}"
                    + "{player|message|reason}";
        }

		@Override
        public boolean matches(Map<String, Construct> prefilter, BindableEvent e)
                throws PrefilterNonMatchException {
            if (e instanceof MCPlayerKickEvent) {
                //I gather we do not what to intercept anything from players in interpreter mode
                //because there would be no one to recieve the information
                if(CommandHelperPlugin.self.interpreterListener.isInInterpreterMode(((MCPlayerKickEvent)e).getPlayer().getName())){
                    throw new PrefilterNonMatchException();
                }

                Prefilters.match(prefilter, "player", ((MCPlayerKickEvent)e).getPlayer().getName(), PrefilterType.MACRO);
                Prefilters.match(prefilter, "reason", ((MCPlayerKickEvent)e).getReason(), PrefilterType.MACRO);
                return true;
            }
            return false;
        }

		@Override
        public BindableEvent convert(CArray manualObject, Target t) {
            return null;
        }

		@Override
        public Map<String, Construct> evaluate(BindableEvent e)
                throws EventException {
            if (e instanceof MCPlayerKickEvent) {
                MCPlayerKickEvent event = (MCPlayerKickEvent) e;
                Map<String, Construct> map = evaluate_helper(e);
                //Fill in the event parameters
                map.put("message", new CString(event.getMessage(), Target.UNKNOWN));
                map.put("reason", new CString(event.getReason(), Target.UNKNOWN));
                return map;
            } else {
                throw new EventException("Cannot convert e to MCPlayerKickEvent");
            }
        }

		@Override
        public Driver driver() {
            return Driver.PLAYER_KICK;
        }

		@Override
        public boolean modifyEvent(String key, Construct value,
                BindableEvent event) {
            if(event instanceof MCPlayerKickEvent){
                MCPlayerKickEvent e = (MCPlayerKickEvent) event;
                if(key.equalsIgnoreCase("message")){
                    e.setMessage(value.nval());
                    return true;
                }
                if(key.equalsIgnoreCase("reason")){
                    e.setReason(value.nval());
                    return true;
                }
            }
            return false;
        }

		@Override
        public CHVersion since() {
            return CHVersion.V3_3_1;
        }

    }

	@api
	public static class player_teleport extends AbstractEvent {
		@Override
		public String getName() {
			return "player_teleport";
		}

		@Override
		public String docs() {
			return "{player: <macro> The player that teleport. Switching worlds will trigger this event, but world_changed is called "
				+ "after, only if this isn't cancelled first."
				+ "| from: <location match> This should be a location array (x, y, z, world)."
				+ "| to: <location match> The location the player is now in. This should be a location array as well.} "
				+ "{player | from: The location the player is coming from | to: The location the player is now in | "
				+ "type: the type of teleport occuring, one of: " + StringUtils.Join(MCTeleportCause.values(), ", ") + "}"
				+ "{to}"
				+ "{}";
		}

		@Override
		public boolean matches(Map<String, Construct> prefilter, BindableEvent e) throws PrefilterNonMatchException {
			if(e instanceof MCPlayerTeleportEvent){
				MCPlayerTeleportEvent event = (MCPlayerTeleportEvent)e;

				if (prefilter.containsKey("player")) {
					if (!(prefilter.get("player").toString().equalsIgnoreCase(event.getPlayer().getName()))){
						return false;
					}
				}

				if (prefilter.containsKey("type")) {
					if (!(prefilter.get("type").toString().equalsIgnoreCase(event.getCause().toString()))) {
						return false;
					}
				}

				Prefilters.match(prefilter, "from", event.getFrom(), PrefilterType.LOCATION_MATCH);
				Prefilters.match(prefilter, "to", event.getTo(), PrefilterType.LOCATION_MATCH);

				return true;

			}

			return false ;
		}

		@Override
		public BindableEvent convert(CArray manualObject, Target t) {
			MCPlayer p = Static.GetPlayer(manualObject.get("player", Target.UNKNOWN), Target.UNKNOWN);
			MCLocation from = ObjectGenerator.GetGenerator().location(manualObject.get("from", Target.UNKNOWN), p.getWorld(), manualObject.getTarget());
			MCLocation to = ObjectGenerator.GetGenerator().location(manualObject.get("to", Target.UNKNOWN), p.getWorld(), manualObject.getTarget());
			return EventBuilder.instantiate(MCPlayerTeleportEvent.class, p, from, to);
		}

		@Override
		public Map<String, Construct> evaluate(BindableEvent e) throws EventException {
			if (e instanceof MCPlayerTeleportEvent) {
                MCPlayerTeleportEvent event = (MCPlayerTeleportEvent) e;
                Map<String, Construct> map = evaluate_helper(e);

                //Fill in the event parameters
				map.put("player", new CString(event.getPlayer().getName(), Target.UNKNOWN));
                map.put("from", ObjectGenerator.GetGenerator().location(event.getFrom()));
                map.put("to", ObjectGenerator.GetGenerator().location(event.getTo()));
				map.put("type", new CString(event.getCause().toString(), Target.UNKNOWN));

                return map;
            } else {
                throw new EventException("Cannot convert e to MCPlayerTeleportEvent");
            }
		}

		@Override
		public Driver driver() {
			return Driver.PLAYER_TELEPORT;
		}

		@Override
		public boolean modifyEvent(String key, Construct value, BindableEvent event) {
			if (event instanceof MCPlayerTeleportEvent) {
				MCPlayerTeleportEvent e = (MCPlayerTeleportEvent)event;

				if (key.equalsIgnoreCase("to")) {
					MCLocation loc = ObjectGenerator.GetGenerator().location(value, null, Target.UNKNOWN);
					e.setTo(loc);

					return true;
				}
			}

			return false;
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}

	@api
	public static class player_portal_travel extends player_teleport {

		@Override
		public String getName() {
			return "player_portal_travel";
		}

		@Override
		public String docs() {
			return "{player: <macro> | from: <location match> This should be a location array (x, y, z, world)."
					+ " | to: <location match> The location the player is coming to. This should be a location array as well."
					+ " | type: the type of portal occuring, one of: " +  StringUtils.Join(MCTeleportCause.values(), ", ") + "}"
					+ "Fired when a player collides with portal."
					+ "{player: The player that teleport | from: The location the player is coming from"
					+ " | to: The location the player is coming to. Returns null when using Nether portal and \"allow-nether\""
					+ " in server.properties is set to false or when using Ender portal and \"allow-end\" in bukkit.yml"
					+ " is set to false. | type: the type of portal occuring | creationradius: Gets the maximum radius from"
					+ " the given location to create a portal. | searchradius: Gets the search radius value for finding"
					+ " an available portal.}"
					+ "{to|creationradius|searchradius}"
					+ "{}";
		}

		@Override
		public boolean matches(Map<String, Construct> prefilter, BindableEvent e) throws PrefilterNonMatchException {

			if(e instanceof MCPlayerPortalEvent) {
				MCPlayerPortalEvent event = (MCPlayerPortalEvent)e;
				Prefilters.match(prefilter, "player", event.getPlayer().getName(), PrefilterType.MACRO);
				Prefilters.match(prefilter, "type", event.getCause().toString(), PrefilterType.MACRO);
				Prefilters.match(prefilter, "from", event.getFrom(), PrefilterType.LOCATION_MATCH);
				if (event.getTo() != null) {
					Prefilters.match(prefilter, "to", event.getTo(), PrefilterType.LOCATION_MATCH);
				} else {
					Prefilters.match(prefilter, "to", CNull.NULL, PrefilterType.MACRO);
				}
				return true;

			}

			return false;
		}

		@Override
		public BindableEvent convert(CArray manualObject, Target t) {
			MCPlayer p = Static.GetPlayer(manualObject.get("player", Target.UNKNOWN), Target.UNKNOWN);
			MCLocation from = ObjectGenerator.GetGenerator().location(manualObject.get("from", Target.UNKNOWN), p.getWorld(), manualObject.getTarget());
			MCLocation to = ObjectGenerator.GetGenerator().location(manualObject.get("to", Target.UNKNOWN), p.getWorld(), manualObject.getTarget());
			return EventBuilder.instantiate(MCPlayerPortalEvent.class, p, from, to);
		}

		@Override
		public Map<String, Construct> evaluate(BindableEvent e) throws EventException {
			if (e instanceof MCPlayerPortalEvent) {
				MCPlayerPortalEvent event = (MCPlayerPortalEvent) e;
				Map<String, Construct> map = evaluate_helper(e);

                //Fill in the event parameters
				map.put("player", new CString(event.getPlayer().getName(), Target.UNKNOWN));
				map.put("from", ObjectGenerator.GetGenerator().location(event.getFrom()));
				if (event.getTo() == null) {
					map.put("to", CNull.NULL);
				} else {
					map.put("to", ObjectGenerator.GetGenerator().location(event.getTo()));
				}
				map.put("type", new CString(event.getCause().toString(), Target.UNKNOWN));
				map.put("creationradius", new CInt(event.getPortalTravelAgent().getCreationRadius(), Target.UNKNOWN));
				map.put("searchradius", new CInt(event.getPortalTravelAgent().getSearchRadius(), Target.UNKNOWN));
				return map;
			} else {
				throw new EventException("Cannot convert e to MCPlayerPortalEvent");
			}
		}

		@Override
		public Driver driver() {
			return Driver.PLAYER_PORTAL_TRAVEL;
		}

		@Override
		public boolean modifyEvent(String key, Construct value, BindableEvent event) {
			if (event instanceof MCPlayerPortalEvent) {
				MCPlayerPortalEvent e = (MCPlayerPortalEvent)event;

				if (key.equalsIgnoreCase("to")) {
					e.useTravelAgent(true);
					MCLocation loc = ObjectGenerator.GetGenerator().location(value, null, Target.UNKNOWN);
					e.setTo(loc);
					return true;
				}

				if (key.equalsIgnoreCase("creationradius")) {
					e.useTravelAgent(true);
					e.getPortalTravelAgent().setCreationRadius(Static.getInt32(value, Target.UNKNOWN));
					return true;
				}

				if (key.equalsIgnoreCase("searchradius")) {
					e.useTravelAgent(true);
					e.getPortalTravelAgent().setSearchRadius(Static.getInt32(value, Target.UNKNOWN));
					return true;
				}
			}

			return false;
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}

    @api
    public static class player_prelogin extends AbstractEvent {

		@Override
		public String getName() {
			return "player_prelogin";
		}

		@Override
		public String docs() {
			return "{player: <string match>} "
					+ "This event is called when a player is about to be authed. "
					+ "This event only fires if your server is in online mode. "
                    + "This event cannot be cancelled. Instead, you can deny them by setting "
					+ "'result' to KICK_BANNED, KICK_WHITELIST, KICK_OTHER, or KICK_FULL. "
                    + "The default for 'result' is ALLOWED. When setting 'result', you "
                    + "can specify the kick message by modifying 'kickmsg'. "
                    + "{player: The player's name | kickmsg: The default kick message | "
                    + "ip: the player's IP address | result: the default response to their logging in}"
                    + "{kickmsg|result}"
                    + "{player|kickmsg|ip|result}";
		}

		@Override
		public boolean matches(Map<String, Construct> prefilter, BindableEvent e)
				throws PrefilterNonMatchException {
			if(e instanceof MCPlayerPreLoginEvent){
                MCPlayerPreLoginEvent event = (MCPlayerPreLoginEvent)e;
                if(prefilter.containsKey("player")){
                    if(!event.getName().equals(prefilter.get("player").val())){
                        return false;
                    }
                }
			}

			return true;
		}

		@Override
		public BindableEvent convert(CArray manualObject, Target t) {
			return null;
		}

		@Override
		public Map<String, Construct> evaluate(BindableEvent e)
				throws EventException {
			if(e instanceof MCPlayerPreLoginEvent){
                MCPlayerPreLoginEvent event = (MCPlayerPreLoginEvent) e;
                Map<String, Construct> map = evaluate_helper(e);

                map.put("player", new CString(event.getName(), Target.UNKNOWN));
                map.put("ip", new CString(event.getIP(), Target.UNKNOWN));
                map.put("result", new CString(event.getResult(), Target.UNKNOWN));
                map.put("kickmsg", new CString(event.getKickMessage(), Target.UNKNOWN));

                return map;
            } else{
                throw new EventException("Cannot convert e to PlayerPreLoginEvent");
            }
		}

		@Override
		public Driver driver() {
			return Driver.PLAYER_PRELOGIN;
		}

		@Override
		public boolean modifyEvent(String key, Construct value,
				BindableEvent e) {
			if(e instanceof MCPlayerPreLoginEvent){
                MCPlayerPreLoginEvent event = (MCPlayerPreLoginEvent)e;
                if (key.equals("result")) {
                	String[] possible = new String[] {"ALLOWED", "KICK_WHITELIST",
                			"KICK_BANNED", "KICK_FULL", "KICK_OTHER"};
                	if(Arrays.asList(possible).contains(value.val().toUpperCase())) {
                		event.setResult(value.val().toUpperCase());
                	}
                } else if (key.equals("kickmsg")) {
                	event.setKickMessage(value.val());
                }
			}
			return false;
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

    }

    @api
    public static class player_login extends AbstractEvent {

		@Override
		public String getName() {
			return "player_login";
		}

		@Override
		public String docs() {
			return "{player: <string match>} "
					+ "This event is called when a player is about to log in. "
                    + "This event cannot be cancelled. Instead, you can deny them by setting "
					+ "'result' to KICK_BANNED, KICK_WHITELIST, KICK_OTHER, or KICK_FULL. "
                    + "The default for 'result' is ALLOWED. When setting 'result', you "
                    + "can specify the kick message by modifying 'kickmsg'. "
                    + "{player: The player's name | uuid: The player's unique id | "
					+ "kickmsg: The default kick message | ip: the player's IP address | "
					+ "hostname: The hostname used to reach the server | "
					+ "result: the default response to their logging in}"
                    + "{kickmsg|result}"
                    + "{player|kickmsg|ip|result}";
		}

		@Override
		public boolean matches(Map<String, Construct> prefilter, BindableEvent e)
				throws PrefilterNonMatchException {
			if(e instanceof MCPlayerPreLoginEvent){
                MCPlayerPreLoginEvent event = (MCPlayerPreLoginEvent)e;
                if(prefilter.containsKey("player")){
                    if(!event.getName().equals(prefilter.get("player").val())){
                        return false;
                    }
                }
			}

			return true;
		}

		@Override
		public BindableEvent convert(CArray manualObject, Target t) {
			return null;
		}

		@Override
		public Map<String, Construct> evaluate(BindableEvent e)
				throws EventException {
			if(e instanceof MCPlayerLoginEvent){
                MCPlayerLoginEvent event = (MCPlayerLoginEvent) e;
                Map<String, Construct> map = evaluate_helper(e);

                map.put("player", new CString(event.getName(), Target.UNKNOWN));
				map.put("uuid", new CString(event.getUniqueId(), Target.UNKNOWN));
                map.put("ip", new CString(event.getIP(), Target.UNKNOWN));
				//TODO: The event.getResult needs to be enum'd
                map.put("result", new CString(event.getResult(), Target.UNKNOWN));
                map.put("kickmsg", new CString(event.getKickMessage(), Target.UNKNOWN));
                map.put("hostname", new CString(event.getHostname(), Target.UNKNOWN));

                return map;
            } else{
                throw new EventException("Cannot convert e to PlayerLoginEvent");
            }
		}

		@Override
		public Driver driver() {
			return Driver.PLAYER_LOGIN;
		}

		@Override
		public boolean modifyEvent(String key, Construct value,
				BindableEvent e) {
			if(e instanceof MCPlayerLoginEvent){
                MCPlayerLoginEvent event = (MCPlayerLoginEvent)e;
                if (key.equals("result")) {
                	String[] possible = new String[] {"ALLOWED", "KICK_WHITELIST",
                			"KICK_BANNED", "KICK_FULL", "KICK_OTHER"};
                	if(Arrays.asList(possible).contains(value.val().toUpperCase())) {
                		event.setResult(value.val().toUpperCase());
                	}
                } else if (key.equals("kickmsg")) {
                	event.setKickMessage(value.val());
                }
			}

			return false;
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

        @Override
        public void preExecution(Environment env, ActiveEvent activeEvent) {
            if(activeEvent.getUnderlyingEvent() instanceof MCPlayerLoginEvent){
                //Static lookups of the player don't seem to work here, but
                //the player is passed in with the event.
                MCPlayer player = ((MCPlayerLoginEvent)activeEvent.getUnderlyingEvent()).getPlayer();
                env.getEnv(CommandHelperEnvironment.class).SetPlayer(player);
                Static.InjectPlayer(player);
            }
        }

        @Override
        public void postExecution(Environment env, ActiveEvent activeEvent) {
            if(activeEvent.getUnderlyingEvent() instanceof MCPlayerLoginEvent){
                MCPlayer player = ((MCPlayerLoginEvent)activeEvent.getUnderlyingEvent()).getPlayer();
                Static.UninjectPlayer(player);
            }
        }

    }

    @api
    public static class player_join extends AbstractEvent{

		@Override
        public String getName() {
            return "player_join";
        }

		@Override
        public String docs() {
            return "{player: <string match> | world: <string match> |"
                    + "join_message: <regex>} This event is called when a player logs in. "
                    + "Setting join_message to null causes it to not be displayed at all. Cancelling "
                    + "the event does not prevent them from logging in. Instead, you should just pkick() them."
                    + "{player: The player's name | world | join_message: The default join message | first_login: True if this is the first time"
                    + " the player has logged in.}"
                    + "{join_message}"
                    + "{player|world|join_message}";
        }

		@Override
        public CHVersion since() {
            return CHVersion.V3_3_0;
        }

		@Override
        public Driver driver(){
            return Driver.PLAYER_JOIN;
        }

		@Override
        public boolean matches(Map<String, Construct> prefilter, BindableEvent e) throws PrefilterNonMatchException {
            if(e instanceof MCPlayerJoinEvent){
                MCPlayerJoinEvent ple = (MCPlayerJoinEvent) e;
                if(prefilter.containsKey("player")){
                    if(!ple.getPlayer().getName().equals(prefilter.get("player").val())){
                        return false;
                    }
                }
                Prefilters.match(prefilter, "join_message", ple.getJoinMessage(), Prefilters.PrefilterType.REGEX);
				Prefilters.match(prefilter, "world", ple.getPlayer().getWorld().getName(), PrefilterType.STRING_MATCH);
                return true;
            }
            return false;
        }

		@Override
        public Map<String, Construct> evaluate(BindableEvent e) throws EventException {
            if(e instanceof MCPlayerJoinEvent){
                MCPlayerJoinEvent ple = (MCPlayerJoinEvent) e;
                Map<String, Construct> map = evaluate_helper(e);
                //map.put("player", new CString(ple.getPlayer().getName(), Target.UNKNOWN));
				map.put("world", new CString(ple.getPlayer().getWorld().getName(), Target.UNKNOWN));
                map.put("join_message", new CString(ple.getJoinMessage(), Target.UNKNOWN));
                map.put("first_login", CBoolean.get(ple.getPlayer().isNewPlayer()));
                return map;
            } else{
                throw new EventException("Cannot convert e to PlayerLoginEvent");
            }
        }

		@Override
        public boolean modifyEvent(String key, Construct value, BindableEvent event) {
            if(event instanceof MCPlayerJoinEvent){
                MCPlayerJoinEvent pje = (MCPlayerJoinEvent)event;
                if(key.equals("join_message") || key.equals("message")){
                    if(value instanceof CNull){
                        pje.setJoinMessage(null);
                        return pje.getJoinMessage() == null;
                    } else {
                        pje.setJoinMessage(value.val());
                        return pje.getJoinMessage().equals(value.val());
                    }
                }
            }
            return false;
        }

		@Override
        public BindableEvent convert(CArray manual, Target t){
            MCPlayerJoinEvent e = EventBuilder.instantiate(MCPlayerJoinEvent.class, Static.GetPlayer(manual.get("player", Target.UNKNOWN).val(), Target.UNKNOWN),
                    manual.get("join_message", Target.UNKNOWN).val());
            return e;
        }

    }

    @api
    public static class player_interact extends AbstractEvent {

		@Override
        public String getName() {
            return "player_interact";
        }

		@Override
        public String docs() {
            return "{block: <item match> If the block the player interacts with is this"
                    + " | button: <string match> left or right. If they left or right clicked |"
                    + " item: <item match> The item they are holding when they interacted |"
                    + " player: <string match> The player that triggered the event} "
                    + "Fires when a player left or right clicks a block or the air"
                    + "{action: One of either: left_click_block, right_click_block, left_click_air, or right_click_air |"
                    + "block: The id of the block they clicked, or 0 if they clicked the air. If they clicked the air, "
                    + " neither facing or location will be present. |"
                    + "player: The player associated with this event |"
                    + "facing: The (lowercase) face of the block they clicked. (One of " + StringUtils.Join(MCBlockFace.values(), ", ", ", or ") + ") |"
                    + "location: The (x, y, z, world) location of the block they clicked}"
                    + "{}"
                    + "{player|action|item|location|facing}";
        }

		@Override
        public CHVersion since() {
            return CHVersion.V3_3_0;
        }

		@Override
        public Driver driver() {
            return Driver.PLAYER_INTERACT;
        }

		@Override
        public boolean matches(Map<String, Construct> prefilter, BindableEvent e) throws PrefilterNonMatchException {
            if(e instanceof MCPlayerInteractEvent){
                MCPlayerInteractEvent pie = (MCPlayerInteractEvent)e;
                if(((MCPlayerInteractEvent)e).getAction().equals(MCAction.PHYSICAL)){
                    return false;
                }
                if(prefilter.containsKey("button")){
                    if(pie.getAction().equals(MCAction.LEFT_CLICK_AIR) || pie.getAction().equals(MCAction.LEFT_CLICK_BLOCK)){
                        if(!prefilter.get("button").val().toLowerCase().equals("left")){
                            return false;
                        }
                    }
                    if(pie.getAction().equals(MCAction.RIGHT_CLICK_AIR) || pie.getAction().equals(MCAction.RIGHT_CLICK_BLOCK)){
                        if(!prefilter.get("button").val().toLowerCase().equals("right")){
                            return false;
                        }
                    }
                }

                Prefilters.match(prefilter, "item", Static.ParseItemNotation(pie.getItem()), PrefilterType.ITEM_MATCH);
                Prefilters.match(prefilter, "block", Static.ParseItemNotation(pie.getClickedBlock()), PrefilterType.ITEM_MATCH);
                Prefilters.match(prefilter, "player", pie.getPlayer().getName(), PrefilterType.MACRO);

                return true;
            }
            return false;
        }

		@Override
        public Map<String, Construct> evaluate(BindableEvent e) throws EventException {
            if(e instanceof MCPlayerInteractEvent){
                MCPlayerInteractEvent pie = (MCPlayerInteractEvent) e;
                Map<String, Construct> map = evaluate_helper(e);
                //map.put("player", new CString(pie.getPlayer().getName(), Target.UNKNOWN));
                MCAction a = pie.getAction();
                map.put("action", new CString(a.name().toLowerCase(), Target.UNKNOWN));
                map.put("block", new CString(Static.ParseItemNotation(pie.getClickedBlock()), Target.UNKNOWN));
                if(a == MCAction.LEFT_CLICK_AIR || a == MCAction.LEFT_CLICK_BLOCK){
                    map.put("button", new CString("left", Target.UNKNOWN));
                } else {
                    map.put("button", new CString("right", Target.UNKNOWN));
                }
                if(a == MCAction.LEFT_CLICK_BLOCK || a == MCAction.RIGHT_CLICK_BLOCK){
                    map.put("facing", new CString(pie.getBlockFace().name().toLowerCase(), Target.UNKNOWN));
                    map.put("location", ObjectGenerator.GetGenerator().location(pie.getClickedBlock().getLocation(), false));
                }
				map.put("world", new CString(pie.getPlayer().getWorld().getName(), Target.UNKNOWN));
                map.put("item", new CString(Static.ParseItemNotation(pie.getItem()), Target.UNKNOWN));
                return map;
            } else {
                throw new EventException("Cannot convert e to PlayerInteractEvent");
            }
        }

		@Override
        public BindableEvent convert(CArray manual, Target t){
            MCPlayer p = Static.GetPlayer(manual.get("player", Target.UNKNOWN), Target.UNKNOWN);
            MCAction a = MCAction.valueOf(manual.get("action", Target.UNKNOWN).val().toUpperCase());
            MCItemStack is = Static.ParseItemNotation("player_interact event", manual.get("item", Target.UNKNOWN).val(), 1, Target.UNKNOWN);
            MCBlock b = ObjectGenerator.GetGenerator().location(manual.get("location", Target.UNKNOWN), null, Target.UNKNOWN).getBlock();
            MCBlockFace bf = MCBlockFace.valueOf(manual.get("facing", Target.UNKNOWN).val().toUpperCase());
            MCPlayerInteractEvent e = EventBuilder.instantiate(MCPlayerInteractEvent.class, p, a, is, b, bf);
            return e;
        }

		@Override
        public boolean modifyEvent(String key, Construct value, BindableEvent event) {
            if(event instanceof MCPlayerInteractEvent){
                MCPlayerInteractEvent pie = (MCPlayerInteractEvent)event;

            }
            return false;
        }

    }

    public abstract static class player_bed_event extends AbstractEvent {

		@Override
		public boolean matches(Map<String, Construct> prefilter, BindableEvent e) throws PrefilterNonMatchException {
			if(e instanceof MCPlayerBedEvent){
                MCPlayerBedEvent be = (MCPlayerBedEvent)e;

				if(prefilter.containsKey("location")){
					MCLocation loc = ObjectGenerator.GetGenerator().location(prefilter.get("location"), null, Target.UNKNOWN);

					if(!be.getBed().getLocation().equals(loc)){
						return false;
					}
				}

				return true;
			}

			return false;
		}

		@Override
		public BindableEvent convert(CArray manual, Target t) {
			MCPlayer p = Static.GetPlayer(manual.get("player", Target.UNKNOWN), Target.UNKNOWN);
            MCBlock b = ObjectGenerator.GetGenerator().location(manual.get("location", Target.UNKNOWN), null, Target.UNKNOWN).getBlock();

			MCPlayerBedEvent e = EventBuilder.instantiate(MCPlayerBedEvent.class, p, b);

			return e;
		}

		@Override
		public Map<String, Construct> evaluate(BindableEvent e) throws EventException {
			if(e instanceof MCPlayerBedEvent){
                MCPlayerBedEvent bee = (MCPlayerBedEvent) e;
                Map<String, Construct> map = evaluate_helper(e);

                map.put("location", ObjectGenerator.GetGenerator().location(bee.getBed().getLocation(), false));
				map.put("player", new CString(bee.getPlayer().getName(), Target.UNKNOWN));

				return map;
			} else {
				throw new EventException("Cannot convert e to an appropriate PlayerBedEvent.");
			}
		}

		@Override
		public Driver driver() {
			return Driver.PLAYER_BED_EVENT;
		}

		@Override
		public boolean modifyEvent(String key, Construct value, BindableEvent event) {
			return false;
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}

	@api
	public static class player_enter_bed extends player_bed_event {

		@Override
		public String docs() {
			return "{location: The location of the bed} "
                    + "Fires when a player tries to enter a bed."
                    + "{location: The location of the bed |"
                    + " player: The player associated with this event}"
                    + "{}"
                    + "{location|player}";
		}

		@Override
		public String getName() {
			return "player_enter_bed";
		}
	}

	@api
	public static class player_leave_bed extends player_bed_event {

		@Override
		public String docs() {
			return "{location: The location of the bed} "
                    + "Fires when a player leaves a bed."
                    + "{location: The location of the bed |"
                    + " player: The player associated with this event}"
                    + "{}"
                    + "{location|player}";
		}

		@Override
		public String getName() {
			return "player_leave_bed";
		}
	}

	@api
    public static class pressure_plate_activated extends AbstractEvent {

		@Override
		public String getName() {
			return "pressure_plate_activated";
		}

		@Override
		public String docs() {
			return "{location: <location match> The location of the pressure plate | activated: <boolean match> If true, only will trigger when the plate is stepped on. Currently,"
					+ " this will only be true, since the event is only triggered on activations, not deactivations, but is reserved for future use.} "
                    + "Fires when a player steps on a pressure plate"
                    + "{location: The location of the pressure plate |"
					+ " activated: If true, then the player has stepped on the plate, if false, they have gotten off of it. Currently,"
					+ " this will always be true, because the event is only triggered for activations, not deactivations, but is reserved"
					+ " for future use. |"
                    + " player: The player associated with this event}"
                    + "{}"
                    + "{}";
		}

		@Override
		public boolean matches(Map<String, Construct> prefilter, BindableEvent e) throws PrefilterNonMatchException {
			if(e instanceof MCPlayerInteractEvent){
                MCPlayerInteractEvent pie = (MCPlayerInteractEvent)e;
                if(!((MCPlayerInteractEvent)e).getAction().equals(MCAction.PHYSICAL)){
                    return false;
                }
				Prefilters.match(prefilter, "location", pie.getClickedBlock().getLocation(), PrefilterType.LOCATION_MATCH);
				if(prefilter.containsKey("activated")){
					//TODO: Once activation is supported, check for that here
				}
				return true;
			}
			return false;
		}

		@Override
		public BindableEvent convert(CArray manual, Target t) {
			MCPlayer p = Static.GetPlayer(manual.get("player", Target.UNKNOWN), Target.UNKNOWN);
            MCBlock b = ObjectGenerator.GetGenerator().location(manual.get("location", Target.UNKNOWN), null, Target.UNKNOWN).getBlock();
            MCPlayerInteractEvent e = EventBuilder.instantiate(MCPlayerInteractEvent.class, p, MCAction.PHYSICAL, null, b, MCBlockFace.UP);
            return e;
		}

		@Override
		public Map<String, Construct> evaluate(BindableEvent e) throws EventException {
			if(e instanceof MCPlayerInteractEvent){
                MCPlayerInteractEvent pie = (MCPlayerInteractEvent) e;
                Map<String, Construct> map = evaluate_helper(e);
                map.put("location", ObjectGenerator.GetGenerator().location(pie.getClickedBlock().getLocation(), false));
				//TODO: Once activation is supported, set that appropriately here.
				map.put("activated", CBoolean.TRUE);
				return map;
			} else {
				throw new EventException("Cannot convert e to PlayerInteractEvent");
			}
		}

		@Override
		public Driver driver() {
			return Driver.PLAYER_INTERACT;
		}

		@Override
		public boolean modifyEvent(String key, Construct value, BindableEvent event) {
			return false;
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

	}

    @api
    public static class player_spawn extends AbstractEvent {

		@Override
        public String getName() {
            return "player_spawn";
        }

		@Override
        public String docs() {
            return "{x: <expression>| y: <expression>| z: <expression>| world: <string match>| player: <macro>}"
                    + "Fires when a player respawns. Technically during this time, the player is not considered to be"
                    + " 'online'. This can cause problems if you try to run an external command with run() or something."
                    + " CommandHelper takes into account the fact that the player is offline, and works around this, so"
                    + " all CH functions should respond correctly, as if the player was online, however other plugins or"
                    + " plain text commands that are run may not."
                    + "{player: The player that is respawning | "
                    + "location: The location they are going to respawn at | "
					+ "bed_spawn: True if the respawn location is the player's bed}"
                    + "{location}"
                    + "{player|location|bed_spawn}";
        }

		@Override
        public Driver driver() {
            return Driver.PLAYER_SPAWN;
        }

		@Override
        public CHVersion since() {
            return CHVersion.V3_3_0;
        }

		@Override
        public boolean matches(Map<String, Construct> prefilter, BindableEvent e) throws PrefilterNonMatchException {
            if (e instanceof MCPlayerRespawnEvent) {
                MCPlayerRespawnEvent event = (MCPlayerRespawnEvent) e;
                Prefilters.match(prefilter, "player", event.getPlayer().getName(), PrefilterType.MACRO);
                Prefilters.match(prefilter, "x", event.getRespawnLocation().getBlockX(), PrefilterType.EXPRESSION);
                Prefilters.match(prefilter, "y", event.getRespawnLocation().getBlockY(), PrefilterType.EXPRESSION);
                Prefilters.match(prefilter, "z", event.getRespawnLocation().getBlockZ(), PrefilterType.EXPRESSION);
                Prefilters.match(prefilter, "world", event.getRespawnLocation().getWorld().getName(), PrefilterType.STRING_MATCH);
                return true;
            }
            return false;
        }

		@Override
        public Map<String, Construct> evaluate(BindableEvent e) throws EventException {
            if (e instanceof MCPlayerRespawnEvent) {
                MCPlayerRespawnEvent event = (MCPlayerRespawnEvent) e;
                Map<String, Construct> map = evaluate_helper(e);
                //the helper puts the player in for us
                CArray location = ObjectGenerator.GetGenerator().location(event.getRespawnLocation());
                map.put("location", location);
				map.put("bed_spawn", CBoolean.get(event.isBedSpawn()));
                return map;
            } else {
                throw new EventException("Cannot convert e to PlayerRespawnEvent");
            }
        }

		@Override
        public BindableEvent convert(CArray manual, Target t) {
            //For firing off the event manually, we have to convert the CArray into an
            //actual object that will trigger it
            MCPlayer p = Static.GetPlayer(manual.get("player", Target.UNKNOWN), Target.UNKNOWN);
            MCLocation l = ObjectGenerator.GetGenerator().location(manual.get("location", Target.UNKNOWN), p.getWorld(), Target.UNKNOWN);
            MCPlayerRespawnEvent e = EventBuilder.instantiate(MCPlayerRespawnEvent.class, p, l, false);
            return e;
        }

		@Override
        public boolean modifyEvent(String key, Construct value, BindableEvent event) {
            if (event instanceof MCPlayerRespawnEvent) {
                MCPlayerRespawnEvent e = (MCPlayerRespawnEvent) event;
                if (key.equals("location")) {
                    //Change this parameter in e to value
                    e.setRespawnLocation(ObjectGenerator.GetGenerator().location(value, e.getPlayer().getWorld(), Target.UNKNOWN));
                    return true;
                }
            }
            return false;
        }

        @Override
        public void preExecution(Environment env, ActiveEvent activeEvent) {
            if(activeEvent.getUnderlyingEvent() instanceof MCPlayerRespawnEvent){
                //Static lookups of the player don't seem to work here, but
                //the player is passed in with the event.
                MCPlayer player = ((MCPlayerRespawnEvent)activeEvent.getUnderlyingEvent()).getPlayer();
                env.getEnv(CommandHelperEnvironment.class).SetPlayer(player);
                Static.InjectPlayer(player);
            }
        }

        @Override
        public void postExecution(Environment env, ActiveEvent activeEvent) {
            if(activeEvent.getUnderlyingEvent() instanceof MCPlayerRespawnEvent){
                MCPlayer player = ((MCPlayerRespawnEvent)activeEvent.getUnderlyingEvent()).getPlayer();
                Static.UninjectPlayer(player);
            }
        }
    }


    @api
	public static class player_death extends entity_death {

		@Override
        public String getName() {
            return "player_death";
        }

		@Override
        public String docs() {
            return "{player: <macro>}"
                    + "Fired when a player dies."
                    + "{player: The player that died |"
					+ " drops: An array of the items that will be dropped, or null |"
                    + " xp: The amount of experience that will be dropped |"
					+ " cause: The cause of death |"
					+ " death_message: The death message, or null if absent |"
					+ " keep_inventory: If the player will keep their inventory |"
					+ " keep_level: If the player will keep their experience and their level |"
					+ " new_exp: The player's experience when they will respawn |"
					+ " new_level: The player's level when they will respawn |"
					+ " new_total_exp: The player's total experience when they will respawn |"
					+ " killer: The name of the killer if a player killed them, otherwise null}"
                    + "{xp | drops: The items will be replaced by the given items | death_message | keep_inventory | keep_level | new_exp | new_level | new_total_exp}"
                    + "{}";
        }

		@Override
        public Driver driver() {
            return Driver.PLAYER_DEATH;
        }

		@Override
        public CHVersion since() {
            return CHVersion.V3_3_0;
        }

		@Override
        public boolean matches(Map<String, Construct> prefilter, BindableEvent e) throws PrefilterNonMatchException {
            if (e instanceof MCPlayerDeathEvent) {
                MCPlayerDeathEvent event = (MCPlayerDeathEvent) e;
                Prefilters.match(prefilter, "player", ((MCPlayer)event.getEntity()).getName(), PrefilterType.MACRO);
                return true;
            }
            return false;
        }

        //We have an actual event now, change it into a Map
        //that will end up being the @event object
		@Override
        public Map<String, Construct> evaluate(BindableEvent e) throws EventException {
            if (e instanceof MCPlayerDeathEvent) {
                MCPlayerDeathEvent event = (MCPlayerDeathEvent) e;
				Map<String, Construct> map = super.evaluate(e);
				map.putAll(evaluate_helper(e));
				map.put("death_message", new CString(event.getDeathMessage(), Target.UNKNOWN));
				map.put("keep_inventory", CBoolean.get(event.getKeepInventory()));
				map.put("keep_level", CBoolean.get(event.getKeepLevel()));
				map.put("new_exp", new CInt(event.getNewExp(), Target.UNKNOWN));
				map.put("new_level", new CInt(event.getNewLevel(), Target.UNKNOWN));
				map.put("new_total_exp", new CInt(event.getNewTotalExp(), Target.UNKNOWN));
				if(event.getKiller() instanceof MCPlayer){
					map.put("killer", new CString(((MCPlayer)event.getKiller()).getName(), Target.UNKNOWN));
				} else {
					map.put("killer", CNull.NULL);
				}
                return map;
            } else {
                throw new EventException("Cannot convert e to EntityDeathEvent");
            }
        }

		@Override
        public BindableEvent convert(CArray manual, Target t) {
            //For firing off the event manually, we have to convert the CArray into an
            //actual object that will trigger it
            String splayer = manual.get("player", Target.UNKNOWN).val();
            List<MCItemStack> list = new ArrayList<>();
            String deathMessage = manual.get("death_message", Target.UNKNOWN).val();
            CArray clist = (CArray)manual.get("drops", Target.UNKNOWN);
            for(String key : clist.stringKeySet()){
                list.add(ObjectGenerator.GetGenerator().item(clist.get(key, Target.UNKNOWN), clist.getTarget()));
            }
            MCPlayerDeathEvent e = EventBuilder.instantiate(MCPlayerDeathEvent.class, Static.GetPlayer(splayer, Target.UNKNOWN), list,
                    0, deathMessage);
            return e;
        }

        //Given the paramters, change the underlying event
		@Override
		public boolean modifyEvent(String key, Construct value, BindableEvent event) {
			if (event instanceof MCPlayerDeathEvent) {
				MCPlayerDeathEvent e = (MCPlayerDeathEvent) event;
				switch (key) {
					case "death_message":
						e.setDeathMessage(value.nval());
						return true;
					case "keep_inventory":
						e.setKeepInventory(Static.getBoolean(value));
						return true;
					case "keep_level":
						e.setKeepLevel(Static.getBoolean(value));
						return true;
					case "new_exp":
						e.setNewExp(Static.getInt32(value, Target.UNKNOWN));
						return true;
					case "new_level":
						e.setNewLevel(Static.getInt32(value, Target.UNKNOWN));
						return true;
					case "new_total_exp":
						e.setNewTotalExp(Static.getInt32(value, Target.UNKNOWN));
						return true;
					default:
						return super.modifyEvent(key, value, event);
				}
			} else {
				return false;
			}
		}
	}

    @api
    public static class player_quit extends AbstractEvent {

		@Override
        public String getName() {
            return "player_quit";
        }

		@Override
        public String docs() {
            return "{player: <macro>}"
                    + "Fired when any player quits."
                    + "{message: The message to be sent}"
                    + "{message}"
                    + "{player|message}";
        }

		@Override
        public Driver driver() {
            return Driver.PLAYER_QUIT;
        }

		@Override
        public CHVersion since() {
            return CHVersion.V3_3_1;
        }

		@Override
        public boolean matches(Map<String, Construct> prefilter, BindableEvent e) throws PrefilterNonMatchException {
            if (e instanceof MCPlayerQuitEvent) {
                //As a very special case, if this player is currently in interpreter mode, we do not want to
                //intercept their chat event
                if(CommandHelperPlugin.self.interpreterListener.isInInterpreterMode(((MCPlayerQuitEvent)e).getPlayer().getName())){
                    throw new PrefilterNonMatchException();
                }

                Prefilters.match(prefilter, "player", ((MCPlayerQuitEvent)e).getPlayer().getName(), PrefilterType.MACRO);
                return true;
            }
            return false;
        }

		@Override
        public BindableEvent convert(CArray manualObject, Target t) {
            //Get the parameters from the manualObject
            MCPlayer player = Static.GetPlayer(manualObject.get("player", Target.UNKNOWN), Target.UNKNOWN);
            String message = manualObject.get("message", Target.UNKNOWN).nval();

            BindableEvent e = EventBuilder.instantiate(MCPlayerCommandEvent.class,
                player, message);
            return e;
        }

		@Override
        public Map<String, Construct> evaluate(BindableEvent e) throws EventException {
            if (e instanceof MCPlayerQuitEvent) {
                MCPlayerQuitEvent event = (MCPlayerQuitEvent) e;
                Map<String, Construct> map = evaluate_helper(e);
                //Fill in the event parameters
                map.put("message", new CString(event.getMessage(), Target.UNKNOWN));
                return map;
            } else {
                throw new EventException("Cannot convert e to MCPlayerQuitEvent");
            }
        }

		@Override
        public boolean modifyEvent(String key, Construct value, BindableEvent event) {
            if (event instanceof MCPlayerQuitEvent) {
                MCPlayerQuitEvent e = (MCPlayerQuitEvent)event;
                if("message".equals(key)){
                    e.setMessage(value.nval());
                }
                return true;
            }
            return false;
        }

	@Override
        public void preExecution(Environment env, ActiveEvent activeEvent) {
           if(activeEvent.getUnderlyingEvent() instanceof MCPlayerQuitEvent){
                //Static lookups of the player don't seem to work here, but
                //the player is passed in with the event.
                MCPlayer player = ((MCPlayerQuitEvent)activeEvent.getUnderlyingEvent()).getPlayer();
                env.getEnv(CommandHelperEnvironment.class).SetPlayer(player);
                Static.InjectPlayer(player);
            }
        }

        @Override
        public void postExecution(Environment env, ActiveEvent activeEvent) {
            if(activeEvent.getUnderlyingEvent() instanceof MCPlayerQuitEvent){
                MCPlayer player = ((MCPlayerQuitEvent)activeEvent.getUnderlyingEvent()).getPlayer();
                Static.UninjectPlayer(player);
            }
        }
    }

    @api
    public static class player_chat extends AbstractEvent {

		@Override
        public String getName() {
            return "player_chat";
        }

		@Override
        public String docs() {
            return "{player: <macro>}"
                    + "Fired when any player attempts to send a chat message."
                    + "{message: The message to be sent | recipients | format}"
                    + "{message|recipients: An array of"
                    + " players that will recieve the chat message. If a player doesn't exist"
                    + " or is offline, and is in the array, it is simply ignored, no"
                    + " exceptions will be thrown.|format: The \"printf\" format string, by "
					+ " default: \"<%1$s> %2$s\". The first parameter is the player's display"
					+ " name, and the second one is the message.}"
                    + "{player|message|format}";
        }

		@Override
        public Driver driver() {
            return Driver.PLAYER_CHAT;
        }

		@Override
        public CHVersion since() {
            return CHVersion.V3_3_0;
        }

		@Override
        public boolean matches(Map<String, Construct> prefilter, BindableEvent e) throws PrefilterNonMatchException {
            if (e instanceof MCPlayerChatEvent) {
                //As a very special case, if this player is currently in interpreter mode, we do not want to
                //intercept their chat event
                if(CommandHelperPlugin.self.interpreterListener.isInInterpreterMode(((MCPlayerChatEvent)e).getPlayer().getName())){
                    throw new PrefilterNonMatchException();
                }
                Prefilters.match(prefilter, "player", ((MCPlayerChatEvent)e).getPlayer().getName(), PrefilterType.MACRO);
                return true;
            }
            return false;
        }

		@Override
        public BindableEvent convert(CArray manualObject, Target t) {
            //Get the parameters from the manualObject
            MCPlayer player = Static.GetPlayer(manualObject.get("player", Target.UNKNOWN), Target.UNKNOWN);
            String message = manualObject.get("message", Target.UNKNOWN).nval();
			String format = manualObject.get("format", Target.UNKNOWN).nval();

            BindableEvent e = EventBuilder.instantiate(MCPlayerChatEvent.class,
                player, message, format);
            return e;
        }

		@Override
        public Map<String, Construct> evaluate(BindableEvent e) throws EventException {
            if (e instanceof MCPlayerChatEvent) {
                MCPlayerChatEvent event = (MCPlayerChatEvent) e;
                Map<String, Construct> map = evaluate_helper(e);
                //Fill in the event parameters
                map.put("message", new CString(event.getMessage(), Target.UNKNOWN));
                CArray ca = new CArray(Target.UNKNOWN);
                for(MCPlayer recipient : event.getRecipients()){
                    ca.push(new CString(recipient.getName(), Target.UNKNOWN), Target.UNKNOWN);
                }
				map.put("format", new CString(event.getFormat(), Target.UNKNOWN));
                map.put("recipients", ca);
                return map;
            } else {
                throw new EventException("Cannot convert e to MCPlayerChatEvent");
            }
        }

		@Override
        public boolean modifyEvent(String key, Construct value, BindableEvent event) {
            if (event instanceof MCPlayerChatEvent) {
                MCPlayerChatEvent e = (MCPlayerChatEvent)event;
                if("message".equals(key)){
                    e.setMessage(value.nval());
                }
                if("recipients".equals(key)){
                    if(value instanceof CArray){
                        List<MCPlayer> list = new ArrayList<MCPlayer>();
                        for(String index : ((CArray)value).stringKeySet()){
                            Construct v = ((CArray)value).get(index, Target.UNKNOWN);
                            try{
                                list.add(Static.GetPlayer(v, Target.UNKNOWN));
                            } catch(ConfigRuntimeException ex){
                                //Ignored
                            }
                        }
                        e.setRecipients(list);
                    } else {
                        throw ConfigRuntimeException.BuildException("recipients must be an array", Exceptions.ExceptionType.CastException, value.getTarget());
                    }
                }
				if("format".equals(key)){
					try{
						e.setFormat(value.nval());
					} catch(UnknownFormatConversionException|IllegalFormatConversionException ex){
						throw new Exceptions.FormatException(ex.getMessage(), Target.UNKNOWN);
					}
				}
                return true;
            }
            return false;
        }
    }

    @api
	@hide("Experimental until further notice")
    public static class async_player_chat extends AbstractEvent {

		@Override
        public String getName() {
            return "async_player_chat";
        }

		@Override
        public String docs() {
            return "{player: <macro>}"
                    + "Fired when any player attempts to send a chat message. The event handler is run on the async thread, and not"
					+ " the main server thread, which can lead to undefined results if your code accesses non-threadsafe methods, hence"
					+ " why this feature is undocumented. If this event is cancelled, player_chat binds will not fire."
                    + "{message: The message to be sent | recipients | format}"
                    + "{message|recipients: An array of"
                    + " players that will recieve the chat message. If a player doesn't exist"
                    + " or is offline, and is in the array, it is simply ignored, no"
                    + " exceptions will be thrown.|format: The \"printf\" format string, by "
					+ " default: \"<%1$s> %2$s\". The first parameter is the player's display"
					+ " name, and the second one is the message.}"
                    + "{}";
        }

		@Override
        public Driver driver() {
            return Driver.PLAYER_CHAT;
        }

		@Override
        public CHVersion since() {
            return CHVersion.V3_3_1;
        }

		@Override
        public boolean matches(Map<String, Construct> prefilter, BindableEvent e) throws PrefilterNonMatchException {
            if (e instanceof MCPlayerChatEvent) {
                //As a very special case, if this player is currently in interpreter mode, we do not want to
                //intercept their chat event
                if(CommandHelperPlugin.self.interpreterListener.isInInterpreterMode(((MCPlayerChatEvent)e).getPlayer().getName())){
                    throw new PrefilterNonMatchException();
                }
                Prefilters.match(prefilter, "player", ((MCPlayerChatEvent)e).getPlayer().getName(), PrefilterType.MACRO);
                return true;
            }
            return false;
        }

		@Override
        public BindableEvent convert(CArray manualObject, Target t) {
            //Get the parameters from the manualObject
            MCPlayer player = Static.GetPlayer(manualObject.get("player", Target.UNKNOWN), Target.UNKNOWN);
            String message = manualObject.get("message", Target.UNKNOWN).nval();

            BindableEvent e = EventBuilder.instantiate(MCPlayerChatEvent.class,
                player, message);
            return e;
        }

		@Override
        public Map<String, Construct> evaluate(BindableEvent e) throws EventException {
            if (e instanceof MCPlayerChatEvent) {
                MCPlayerChatEvent event = (MCPlayerChatEvent) e;
                Map<String, Construct> map = evaluate_helper(e);
                //Fill in the event parameters
                map.put("message", new CString(event.getMessage(), Target.UNKNOWN));
                CArray ca = new CArray(Target.UNKNOWN);
                for(MCPlayer recipient : event.getRecipients()){
                    ca.push(new CString(recipient.getName(), Target.UNKNOWN), Target.UNKNOWN);
                }
				map.put("format", new CString(event.getFormat(), Target.UNKNOWN));
                map.put("recipients", ca);
                return map;
            } else {
                throw new EventException("Cannot convert e to MCPlayerChatEvent");
            }
        }

		@Override
        public boolean modifyEvent(String key, Construct value, BindableEvent event) {
			if (event instanceof MCPlayerChatEvent) {
                MCPlayerChatEvent e = (MCPlayerChatEvent)event;
                if("message".equals(key)){
                    e.setMessage(value.nval());
                }
                if("recipients".equals(key)){
                    if(value instanceof CArray){
                        List<MCPlayer> list = new ArrayList<MCPlayer>();
                        for(String index : ((CArray)value).stringKeySet()){
                            Construct v = ((CArray)value).get(index, Target.UNKNOWN);
                            try{
                                list.add(Static.GetPlayer(v, Target.UNKNOWN));
                            } catch(ConfigRuntimeException ex){
                                //Ignored
                            }
                        }
                        e.setRecipients(list);
                    } else {
                        throw ConfigRuntimeException.BuildException("recipients must be an array", Exceptions.ExceptionType.CastException, value.getTarget());
                    }
                }
				if("format".equals(key)){
					try{
						e.setFormat(value.nval());
					} catch(UnknownFormatConversionException|IllegalFormatConversionException ex){
						throw new Exceptions.FormatException(ex.getMessage(), Target.UNKNOWN);
					}
				}
                return true;
            }
            return false;
        }
    }

    @api
    public static class player_command extends AbstractEvent {

		@Override
        public String getName() {
            return "player_command";
        }

		@Override
        public String docs() {
            return "{command: <string match> The entire command the player ran "
                    + "| prefix: <string match> Just the first part of the command, i.e. '/cmd' in '/cmd blah blah'"
                    + "| player: <macro> The player using the command}"
                    + "This event is fired off when a player runs any command at all. This actually fires before normal "
                    + " CommandHelper aliases, allowing you to insert control before defined aliases, even."
                    + "{command: The entire command | prefix: Just the prefix of the command}"
                    + "{command}"
                    + "{command}";
        }

		@Override
        public Driver driver() {
            return Driver.PLAYER_COMMAND;
        }

		@Override
        public CHVersion since() {
            return CHVersion.V3_3_1;
        }

		@Override
        public boolean matches(Map<String, Construct> prefilter, BindableEvent e) throws PrefilterNonMatchException {
            if (e instanceof MCPlayerCommandEvent) {
                MCPlayerCommandEvent event = (MCPlayerCommandEvent) e;
                String command = event.getCommand();
                Prefilters.match(prefilter, "player", event.getPlayer().getName(), PrefilterType.MACRO);
                if(prefilter.containsKey("command") && !command.equals(prefilter.get("command").val())){
                    return false;
                }
                if(prefilter.containsKey("prefix")){
                    StringHandling.parse_args pa = new StringHandling.parse_args();
                    CArray ca = (CArray)pa.exec(Target.UNKNOWN, null, new CString(command, Target.UNKNOWN));
                    if(ca.size() > 0){
                        if(!ca.get(0, Target.UNKNOWN).val().equals(prefilter.get("prefix").val())){
                            return false;
                        }
                    } else {
                        return false;
                    }
                }
                return true;
            }
            return false;
        }

		@Override
        public BindableEvent convert(CArray manualObject, Target t) {
            MCPlayer player = Static.GetPlayer(manualObject.get("player", Target.UNKNOWN), Target.UNKNOWN);
            String command = manualObject.get("command", Target.UNKNOWN).nval();

            BindableEvent e = EventBuilder.instantiate(MCPlayerCommandEvent.class,
                player, command);
            return e;
        }

		@Override
        public Map<String, Construct> evaluate(BindableEvent e) throws EventException {
            if (e instanceof MCPlayerCommandEvent) {
                MCPlayerCommandEvent event = (MCPlayerCommandEvent) e;
                Map<String, Construct> map = evaluate_helper(e);
                //Fill in the event parameters
                map.put("command", new CString(event.getCommand(), Target.UNKNOWN));

                StringHandling.parse_args pa = new StringHandling.parse_args();
                CArray ca = (CArray)pa.exec(Target.UNKNOWN, null, new CString(event.getCommand(), Target.UNKNOWN));
                map.put("prefix", new CString(ca.get(0, Target.UNKNOWN).val(), Target.UNKNOWN));

                return map;
            } else {
                throw new EventException("Cannot convert e to MCPlayerCommandEvent");
            }
        }

		@Override
        public boolean modifyEvent(String key, Construct value, BindableEvent event) {
            if (event instanceof MCPlayerCommandEvent) {
                MCPlayerCommandEvent e = (MCPlayerCommandEvent) event;

                if("command".equals(key)){
                    e.setCommand(value.val());
                }

                return true;
            }
            return false;
        }

        @Override
        public void cancel(BindableEvent o, boolean state) {
            ((MCPlayerCommandEvent)o).cancel();
        }
    }

    @api
    public static class world_changed extends AbstractEvent {

		@Override
        public String getName() {
            return "world_changed";
        }

		@Override
        public String docs() {
            return "{player: <macro> The player that switched worlds "
                    + "| from: <string match> The world the player is coming from "
                    + "| to: <string match> The world the player is now in}"
					+ " This event is fired off when a player changes worlds. This event is not cancellable,"
					+ " so to prevent it, the player_teleport event must be checked."
                    + "{player | from: The world the player is coming from | to: The world the player is now in}"
                    + "{}"
                    + "{player, from}";
        }

		@Override
        public Driver driver() {
            return Driver.WORLD_CHANGED;
        }

		@Override
        public CHVersion since() {
            return CHVersion.V3_3_1;
        }

		@Override
        public boolean matches(Map<String, Construct> prefilter, BindableEvent e) throws PrefilterNonMatchException {
            if (e instanceof MCWorldChangedEvent) {
                MCWorldChangedEvent event = (MCWorldChangedEvent) e;
                Prefilters.match(prefilter, "player", event.getPlayer().getName(), PrefilterType.MACRO);
                Prefilters.match(prefilter, "from", event.getFrom().getName(), PrefilterType.STRING_MATCH);
                Prefilters.match(prefilter, "to", event.getTo().getName(), PrefilterType.STRING_MATCH);
                return true;
            }
            return false;
        }

		@Override
        public BindableEvent convert(CArray manualObject, Target t) {
            MCPlayer player = Static.GetPlayer(manualObject.get("player", Target.UNKNOWN), Target.UNKNOWN);
            MCWorld from = Static.getServer().getWorld(manualObject.get("from", Target.UNKNOWN).val());

            BindableEvent e = EventBuilder.instantiate(MCPlayerCommandEvent.class,
                player, from);
            return e;
        }

		@Override
        public Map<String, Construct> evaluate(BindableEvent e) throws EventException {
            if (e instanceof MCWorldChangedEvent) {
                MCWorldChangedEvent event = (MCWorldChangedEvent) e;
                Map<String, Construct> map = evaluate_helper(e);
                //Fill in the event parameters
                map.put("from", new CString(event.getFrom().getName(), Target.UNKNOWN));
                map.put("to", new CString(event.getTo().getName(), Target.UNKNOWN));
                return map;
            } else {
                throw new EventException("Cannot convert e to MCWorldChangedEvent");
            }
        }

		@Override
        public boolean modifyEvent(String key, Construct value, BindableEvent event) {
            if (event instanceof MCWorldChangedEvent) {
                MCWorldChangedEvent e = (MCWorldChangedEvent) event;

                return true;
            }
            return false;
        }

    }

	private static final Set<Integer> thresholdList = new HashSet<>();

	public static Set<Integer> GetThresholdList(){
		return thresholdList;
												}

	private static final Map<Integer, Map<String, MCLocation>> lastPlayerLocations = new HashMap<>();

	public static Map<String, MCLocation> GetLastLocations(Integer i){
		if (!lastPlayerLocations.containsKey(i)) {
			HashMap<String, MCLocation> newLocation = new HashMap<>();
			lastPlayerLocations.put(i, newLocation);
			return newLocation;
												}
		return(lastPlayerLocations.get(i));
												}

	@api
	public static class player_move extends AbstractEvent{

		@Override
		public String getName() {
			return "player_move";
		}

		@Override
		public String docs() {
			return "{player: <macro> The player that moved. Switching worlds does not trigger this event. "
					+ "| world: <string match> The world the player moved in."
					+ "| from: <location match> This should be a location array (x, y, z, world)."
					+ "| to: <location match> The location the player is now in. This should be a location array as well."
					+ "| threshold: <custom> The minimum distance the player must have travelled before the event"
					+ " will be triggered. This is based on the 3D distance, and is measured in block units.}"
                    + " This event is fired off after a player has moved a certain distance. Due to the high frequency"
					+ " of this event, prefilters are extremely important to use -- especially a threshold -- so that"
					+ " the script doesn't run every time."
                    + "{player | world | from: The location the player is coming from | to: The location the player is now in}"
                    + "{}"
                    + "{}";
		}

		@Override
		public void hook() {
			thresholdList.clear();
			lastPlayerLocations.clear();
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
			lastPlayerLocations.remove(threshold);
		}

		@Override
		public boolean matches(Map<String, Construct> prefilter, BindableEvent e) throws PrefilterNonMatchException {
			if(e instanceof MCPlayerMoveEvent){
				MCPlayerMoveEvent event = (MCPlayerMoveEvent)e;
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
				Prefilters.match(prefilter, "from", event.getFrom(), PrefilterType.LOCATION_MATCH);
				Prefilters.match(prefilter, "to", event.getTo(), PrefilterType.LOCATION_MATCH);
				Prefilters.match(prefilter, "player", event.getPlayer().getName(), PrefilterType.MACRO);
				return true;
			}
			return false ;
		}

		@Override
		public BindableEvent convert(CArray manualObject, Target t) {
			MCPlayer p = Static.GetPlayer(manualObject.get("player", Target.UNKNOWN), Target.UNKNOWN);
			MCLocation from = ObjectGenerator.GetGenerator().location(manualObject.get("from", Target.UNKNOWN), p.getWorld(), manualObject.getTarget());
			MCLocation to = ObjectGenerator.GetGenerator().location(manualObject.get("to", Target.UNKNOWN), p.getWorld(), manualObject.getTarget());
			return EventBuilder.instantiate(MCPlayerMoveEvent.class, p, from, to);
		}

		@Override
		public Map<String, Construct> evaluate(BindableEvent e) throws EventException {
			if (e instanceof MCPlayerMoveEvent) {
                MCPlayerMoveEvent event = (MCPlayerMoveEvent) e;
				Map<String, Construct> map = new HashMap<>();
				map.put("player", new CString(event.getPlayer().getName(), Target.UNKNOWN));
				map.put("world", new CString(event.getFrom().getWorld().getName(), Target.UNKNOWN));
                map.put("from", ObjectGenerator.GetGenerator().location(event.getFrom()));
                map.put("to", ObjectGenerator.GetGenerator().location(event.getTo()));
                return map;
            } else {
                throw new EventException("Cannot convert e to MCPlayerMovedEvent");
            }
		}

		@Override
		public Driver driver() {
			return Driver.PLAYER_MOVE;
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

	@api
	public static class player_fish extends AbstractEvent {

		@Override
		public String getName() {
			return "player_fish";
		}

		@Override
		public String docs() {
			return "{state: <macro> Can be one of " + StringUtils.Join(MCFishingState.values(), ", ", ", or ")
					+ " | player: <macro> The player who is fishing | world: <string match>}"
					+ " Fires when a player casts or reels a fishing rod."
					+ " {player | world | state | chance | xp | hook: the fishhook entity id"
					+ " | caught: the id of the snared entity, can be a fish item}"
					+ " {chance: the chance of catching a fish from pulling the bobber at random"
					+ " | xp: the exp the player will get from catching a fish}"
					+ " {}";
		}

		@Override
		public boolean matches(Map<String, Construct> prefilter, BindableEvent e)
				throws PrefilterNonMatchException {
			if (e instanceof MCPlayerFishEvent) {
				MCPlayerFishEvent event = (MCPlayerFishEvent) e;
				Prefilters.match(prefilter, "state", event.getState().name(), PrefilterType.MACRO);
				Prefilters.match(prefilter, "player", event.getPlayer().getName(), PrefilterType.MACRO);
				Prefilters.match(prefilter, "world", event.getPlayer().getWorld().getName(), PrefilterType.STRING_MATCH);
				return true;
			}
			return false;
		}

		@Override
		public BindableEvent convert(CArray manualObject, Target t) {
			throw ConfigRuntimeException.CreateUncatchableException("Unsupported Operation", Target.UNKNOWN);
		}

		@Override
		public Map<String, Construct> evaluate(BindableEvent e)
				throws EventException {
			if (e instanceof MCPlayerFishEvent) {
				MCPlayerFishEvent event = (MCPlayerFishEvent) e;
				Target t = Target.UNKNOWN;
				Map<String, Construct> ret = evaluate_helper(event);
				ret.put("world", new CString(event.getPlayer().getWorld().getName(), t));
				ret.put("state", new CString(event.getState().name(), t));
				ret.put("hook", new CString(event.getHook().getUniqueId().toString(), t));
				ret.put("xp", new CInt(event.getExpToDrop(), t));
				Construct caught = CNull.NULL;
				if (event.getCaught() != null) {
					caught = new CString(event.getCaught().getUniqueId().toString(), t);
				}
				ret.put("caught", caught);
				ret.put("chance", new CDouble(event.getHook().getBiteChance(), t));
				return ret;
			} else {
				throw new EventException("Could not convert to MCPlayerFishEvent");
			}
		}

		@Override
		public boolean modifyEvent(String key, Construct value,
				BindableEvent event) {
			if (event instanceof MCPlayerFishEvent) {
				MCPlayerFishEvent e = (MCPlayerFishEvent) event;
				if (key.equals("chance")) {
					double chance = Static.getDouble(value, Target.UNKNOWN);
					if (chance > 1.0 || chance < 0.0) {
						throw new Exceptions.FormatException("Chance must be between 0.0 and 1.0", Target.UNKNOWN);
					}
					e.getHook().setBiteChance(chance);
					return true;
				}
				if (key.equals("xp")) {
					e.setExpToDrop(Static.getInt32(value, Target.UNKNOWN));
					return true;
				}
			}
			return false;
		}

		@Override
		public Driver driver() {
			return Driver.PLAYER_FISH;
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}

	@api
	public static class gamemode_change extends AbstractEvent {

		@Override
		public String getName() {
			return "gamemode_change";
		}

		@Override
		public String docs() {
			return "{newmode: <macro> gamemode being changed to, one of "
					+ StringUtils.Join(MCGameMode.values(), ", ", ", or ", " or ")
					+ " | player: <macro>}"
					+ " Fires when something causes a player's gamemode to change. Cancelling the event will"
					+ " cancel the change. The mode itself cannot be modified."
					+ " {player: player whose mode is changing | newmode}"
					+ " {}"
					+ " {}";
		}

		@Override
		public boolean matches(Map<String, Construct> prefilter, BindableEvent event) throws PrefilterNonMatchException {
			if (event instanceof MCGamemodeChangeEvent) {
				MCGamemodeChangeEvent e = (MCGamemodeChangeEvent) event;
				Prefilters.match(prefilter, "player", e.getPlayer().getName(), PrefilterType.MACRO);
				Prefilters.match(prefilter, "newmode", e.getNewGameMode().name(), PrefilterType.MACRO);
				return true;
			}
			return false;
		}

		@Override
		public BindableEvent convert(CArray manualObject, Target t) {
			throw ConfigRuntimeException.BuildException("Unsupported Operation", ExceptionType.BindException, Target.UNKNOWN);
		}

		@Override
		public Map<String, Construct> evaluate(BindableEvent event) throws EventException {
			if (event instanceof MCGamemodeChangeEvent) {
				MCGamemodeChangeEvent e = (MCGamemodeChangeEvent) event;
				Map<String, Construct> ret = evaluate_helper(e);
				ret.put("newmode", new CString(e.getNewGameMode().name(), Target.UNKNOWN));
				return ret;
			} else {
				throw new EventException("Could not convert to MCGamemodeChangeEvent.");
			}
		}

		@Override
		public Driver driver() {
			return Driver.GAMEMODE_CHANGE;
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
	public static class tab_complete_chat extends AbstractEvent {

		@Override
		public String getName() {
			return "tab_complete_chat";
		}

		@Override
		public String docs() {
			return "{player: <macro>}"
					+ " Fires when a player asks for a list of completions to the current word in their chat message."
					+ " Setting the completions to an empty array is this event's equivalent of cancel()."
					+ " {player: the player asking for completion | message: the full message they have typed"
					+ " | last: the partial word completion is asked for | completions}"
					+ " {completions: the list of completions to send, default is player names containing the last text}"
					+ " {}";
		}

		@Override
		public boolean matches(Map<String, Construct> prefilter, BindableEvent e) throws PrefilterNonMatchException {
			if (e instanceof MCChatTabCompleteEvent) {
				Prefilters.match(prefilter, "player", ((MCChatTabCompleteEvent) e).getPlayer().getName(), PrefilterType.MACRO);
				return true;
			}
			return false;
		}

		@Override
		public BindableEvent convert(CArray manualObject, Target t) {
			throw ConfigRuntimeException.BuildException("Unsupported Operation", ExceptionType.BindException, Target.UNKNOWN);
		}

		@Override
		public Map<String, Construct> evaluate(BindableEvent e) throws EventException {
			if (e instanceof MCChatTabCompleteEvent) {
				MCChatTabCompleteEvent event = (MCChatTabCompleteEvent) e;
				Target t = Target.UNKNOWN;
				Map<String, Construct> ret = evaluate_helper(event);
				ret.put("message", new CString(event.getChatMessage(), t));
				ret.put("last", new CString(event.getLastToken(), t));
				CArray completions = new CArray(t);
				for (String c : event.getTabCompletions()) {
					completions.push(new CString(c, t), t);
				}
				ret.put("completions", completions);
				return ret;
			} else {
				throw new EventException("Could not convert to MCChatTabCompleteEvent.");
			}
		}

		@Override
		public boolean modifyEvent(String key, Construct value, BindableEvent event) {
			if (event instanceof MCChatTabCompleteEvent) {
				MCChatTabCompleteEvent e = (MCChatTabCompleteEvent) event;
				if ("completions".equals(key)) {
					if (value instanceof CArray) {
						e.getTabCompletions().clear();
						if (((CArray) value).inAssociativeMode()) {
							for (Construct k : ((CArray) value).keySet()) {
								e.getTabCompletions().add(((CArray) value).get(k, Target.UNKNOWN).val());
							}
						} else {
							for (Construct v : ((CArray) value).asList()) {
								e.getTabCompletions().add(v.val());
							}
						}
						return true;
					}
				}
			}
			return false;
		}

		@Override
		public Driver driver() {
			return Driver.TAB_COMPLETE;
		}

		@Override
		public Version since() {
			return CHVersion.V3_3_1;
		}
	}

	@api
	public static class exp_change extends AbstractEvent {

		@Override
		public String getName() {
			return "exp_change";
		}

		@Override
		public String docs() {
			return "{player: <macro>}"
					+ " Fired when a player's experience changes naturally."
					+ " {player | amount}"
					+ " {amount: an integer of the amount of exp that will be added to the player's total exp}"
					+ " {}";
		}

		@Override
		public boolean matches(Map<String, Construct> prefilter, BindableEvent event) throws PrefilterNonMatchException {
			if (event instanceof MCExpChangeEvent) {
				MCExpChangeEvent e = (MCExpChangeEvent) event;
				Prefilters.match(prefilter, "player", e.getPlayer().getName(), PrefilterType.MACRO);
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
			if (event instanceof MCExpChangeEvent) {
				MCExpChangeEvent e = (MCExpChangeEvent) event;
				Map<String, Construct> ret = evaluate_helper(e);
				ret.put("amount", new CInt(e.getAmount(), Target.UNKNOWN));
				return ret;
			} else {
				throw new EventException("Could not convert to MCExpChangeEvent.");
			}
		}

		@Override
		public Driver driver() {
			return Driver.EXP_CHANGE;
		}

		@Override
		public boolean modifyEvent(String key, Construct value, BindableEvent event) {
			if (event instanceof MCExpChangeEvent) {
				MCExpChangeEvent e = (MCExpChangeEvent) event;
				if ("amount".equals(key)) {
					e.setAmount(Static.getInt32(value, Target.UNKNOWN));
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
	public static class book_edited extends AbstractEvent {

		@Override
		public String getName() {
			return "book_edited";
		}

		@Override
		public Driver driver() {
			return Driver.BOOK_EDITED;
		}

		@Override
		public String docs() {
			return "{player: <macro> The player which edited the book | signing: <boolean match> Whether or not the book is being signed}"
					+ " This event is called when a player edit a book."
					+ " {player: The player which edited the book | slot: The inventory slot number where the book is |"
					+ " oldbook: The book before the editing (an array with keys title, author and pages) |"
					+ " newbook: The book after the editing (an array with keys title, author and pages) |"
					+ " signing: Whether or not the book is being signed}"
					+ " {title | author | pages | signing}"
					+ " {}";
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

		@Override
		public boolean matches(Map<String, Construct> prefilter, BindableEvent event) throws PrefilterNonMatchException {
			if (event instanceof MCPlayerEditBookEvent) {
				MCPlayerEditBookEvent playerEditBookEvent = (MCPlayerEditBookEvent) event;
				Prefilters.match(prefilter, "player", playerEditBookEvent.getPlayer().getName(), PrefilterType.MACRO);
				Prefilters.match(prefilter, "signing", playerEditBookEvent.isSigning(), PrefilterType.BOOLEAN_MATCH);
				return true;
			} else {
				return false;
			}
		}

		@Override
		public BindableEvent convert(CArray manualObject, Target t) {
			return null;
		}

		@Override
		public Map<String, Construct> evaluate(BindableEvent event) throws EventException {
			if (event instanceof MCPlayerEditBookEvent) {
				MCPlayerEditBookEvent playerEditBookEvent = (MCPlayerEditBookEvent) event;
				Map<String, Construct> mapEvent = evaluate_helper(event);
				MCBookMeta oldBookMeta = playerEditBookEvent.getPreviousBookMeta();
				CArray oldBookArray = CArray.GetAssociativeArray(Target.UNKNOWN);
				if (oldBookMeta.hasTitle()) {
					oldBookArray.set("title", new CString(oldBookMeta.getTitle(), Target.UNKNOWN), Target.UNKNOWN);
				} else {
					oldBookArray.set("title", CNull.NULL, Target.UNKNOWN);
				}
				if (oldBookMeta.hasAuthor()) {
					oldBookArray.set("author", new CString(oldBookMeta.getAuthor(), Target.UNKNOWN), Target.UNKNOWN);
				} else {
					oldBookArray.set("author", CNull.NULL, Target.UNKNOWN);
				}
				if (oldBookMeta.hasPages()) {
					CArray pages = new CArray(Target.UNKNOWN);
					for (String page : oldBookMeta.getPages()) {
						pages.push(new CString(page, Target.UNKNOWN), Target.UNKNOWN);
					}
					oldBookArray.set("author", pages, Target.UNKNOWN);
				} else {
					oldBookArray.set("pages", new CArray(Target.UNKNOWN), Target.UNKNOWN);
				}
				mapEvent.put("oldbook", oldBookArray);
				MCBookMeta newBookMeta = playerEditBookEvent.getNewBookMeta();
				CArray newBookArray = CArray.GetAssociativeArray(Target.UNKNOWN);
				if (newBookMeta.hasTitle()) {
					newBookArray.set("title", new CString(newBookMeta.getTitle(), Target.UNKNOWN), Target.UNKNOWN);
				} else {
					newBookArray.set("title", CNull.NULL, Target.UNKNOWN);
				}
				if (newBookMeta.hasAuthor()) {
					newBookArray.set("author", new CString(newBookMeta.getAuthor(), Target.UNKNOWN), Target.UNKNOWN);
				} else {
					newBookArray.set("author", CNull.NULL, Target.UNKNOWN);
				}
				if (newBookMeta.hasPages()) {
					CArray pages = new CArray(Target.UNKNOWN);
					for (String page : newBookMeta.getPages()) {
						pages.push(new CString(page, Target.UNKNOWN), Target.UNKNOWN);
					}
					newBookArray.set("pages", pages, Target.UNKNOWN);
				} else {
					newBookArray.set("pages", new CArray(Target.UNKNOWN), Target.UNKNOWN);
				}
				mapEvent.put("newbook", newBookArray);
				mapEvent.put("slot", new CInt(playerEditBookEvent.getSlot(), Target.UNKNOWN));
				mapEvent.put("signing", CBoolean.get(playerEditBookEvent.isSigning()));
				return mapEvent;
			} else {
				throw new EventException("Cannot convert event to PlayerEditBookEvent");
			}
		}

		@Override
		public boolean modifyEvent(String key, Construct value, BindableEvent event) {
			if (event instanceof MCPlayerEditBookEvent) {
				if (key.equalsIgnoreCase("title")) {
					MCPlayerEditBookEvent e = ((MCPlayerEditBookEvent) event);
					MCBookMeta bookMeta = e.getNewBookMeta();
					bookMeta.setTitle(value.val());
					e.setNewBookMeta(bookMeta);
					return true;
				} else if (key.equalsIgnoreCase("author")) {
					MCPlayerEditBookEvent e = ((MCPlayerEditBookEvent) event);
					MCBookMeta bookMeta = e.getNewBookMeta();
					bookMeta.setAuthor(value.val());
					e.setNewBookMeta(bookMeta);
					return true;
				} else if (key.equalsIgnoreCase("pages")) {
					CArray pageArray = Static.getArray(value, value.getTarget());
					if (pageArray.inAssociativeMode()) {
						throw ConfigRuntimeException.BuildException("The page array must not be associative.", ExceptionType.CastException, pageArray.getTarget());
					} else {
						List<String> pages = new ArrayList<String>();
						for (Construct page : pageArray.asList()) {
							pages.add(page.val());
						}
						MCPlayerEditBookEvent e = ((MCPlayerEditBookEvent) event);
						MCBookMeta bookMeta = e.getNewBookMeta();
						bookMeta.setPages(pages);
						e.setNewBookMeta(bookMeta);
						return true;
					}
				} else if (key.equalsIgnoreCase("signing")) {
					((MCPlayerEditBookEvent) event).setSigning(Static.getBoolean(value));
					return true;
				} else {
					return false;
				}
			} else {
				return false;
			}
		}
	}

	@api
	public static class player_toggle_flight extends AbstractEvent {
		@Override
		public String getName() {
			return "player_toggle_flight";
		}

		@Override
		public Driver driver() {
			return Driver.PLAYER_TOGGLE_FLIGHT;
		}

		@Override
		public String docs() {
			return "{player: <macro> The player who toggled their flying state | flying: <boolean match> Whether or not the player is trying to start or stop flying | world: <macro>}"
					+ " Called when a player toggles their flying state."
					+ " {player: The player who toggled their flying state | flying: Whether or not the player is trying to start or stop flying |"
					+ " location: Where the player is}"
					+ " {}"
					+ " {}";
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

		@Override
		public boolean matches(Map<String, Construct> prefilter, BindableEvent event) throws PrefilterNonMatchException {
			if (event instanceof MCPlayerToggleFlightEvent) {
				MCPlayerToggleFlightEvent ptfe = (MCPlayerToggleFlightEvent) event;
				MCPlayer player = ptfe.getPlayer();
				Prefilters.match(prefilter, "player", player.getName(), PrefilterType.MACRO);
				Prefilters.match(prefilter, "flying", ptfe.isFlying(), PrefilterType.BOOLEAN_MATCH);
				Prefilters.match(prefilter, "world", player.getWorld().getName(), PrefilterType.MACRO);
				return true;
			} else {
				return false;
			}
		}

		@Override
		public BindableEvent convert(CArray manualObject, Target t) {
			return null;
		}

		@Override
		public Map<String, Construct> evaluate(BindableEvent event) throws EventException {
			if (event instanceof MCPlayerToggleFlightEvent) {
				MCPlayerToggleFlightEvent ptfe = (MCPlayerToggleFlightEvent) event;
				Map<String, Construct> mapEvent = evaluate_helper(event);
				MCPlayer player = ptfe.getPlayer();
				mapEvent.put("player", new CString(player.getName(), Target.UNKNOWN));
				mapEvent.put("location", ObjectGenerator.GetGenerator().location(player.getLocation()));
				mapEvent.put("flying", CBoolean.get(ptfe.isFlying()));
				return mapEvent;
			} else {
				throw new EventException("Cannot convert event to PlayerToggleFlightEvent");
			}
		}

		@Override
		public boolean modifyEvent(String key, Construct value, BindableEvent event) {
			return false;
		}
	}

	@api
	public static class player_toggle_sneak extends AbstractEvent {
		@Override
		public String getName() {
			return "player_toggle_sneak";
		}

		@Override
		public Driver driver() {
			return Driver.PLAYER_TOGGLE_SNEAK;
		}

		@Override
		public String docs() {
			return "{player: <macro> The player who toggled their sneaking state | sneaking: <boolean match> Whether or not the player is now sneaking | world: <macro>}"
					+ " Called when a player toggles their sneaking state."
					+ " {player: The player who toggled their sneaking state | sneaking: Whether or not the player is now sneaking |"
					+ " location: Where the player is}"
					+ " {}"
					+ " {}";
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

		@Override
		public boolean matches(Map<String, Construct> prefilter, BindableEvent event) throws PrefilterNonMatchException {
			if (event instanceof MCPlayerToggleSneakEvent) {
				MCPlayerToggleSneakEvent ptse = (MCPlayerToggleSneakEvent) event;
				MCPlayer player = ptse.getPlayer();
				Prefilters.match(prefilter, "player", player.getName(), PrefilterType.MACRO);
				Prefilters.match(prefilter, "sneaking", ptse.isSneaking(), PrefilterType.BOOLEAN_MATCH);
				Prefilters.match(prefilter, "world", player.getWorld().getName(), PrefilterType.MACRO);
				return true;
			} else {
				return false;
			}
		}

		@Override
		public BindableEvent convert(CArray manualObject, Target t) {
			return null;
		}

		@Override
		public Map<String, Construct> evaluate(BindableEvent event) throws EventException {
			if (event instanceof MCPlayerToggleSneakEvent) {
				MCPlayerToggleSneakEvent ptse = (MCPlayerToggleSneakEvent) event;
				Map<String, Construct> mapEvent = evaluate_helper(event);
				MCPlayer player = ptse.getPlayer();
				mapEvent.put("player", new CString(player.getName(), Target.UNKNOWN));
				mapEvent.put("location", ObjectGenerator.GetGenerator().location(player.getLocation()));
				mapEvent.put("sneaking", CBoolean.get(ptse.isSneaking()));
				return mapEvent;
			} else {
				throw new EventException("Cannot convert event to PlayerToggleSneakEvent");
			}
		}

		@Override
		public boolean modifyEvent(String key, Construct value, BindableEvent event) {
			return false;
		}
	}

	@api
	public static class player_toggle_sprint extends AbstractEvent {
		@Override
		public String getName() {
			return "player_toggle_sprint";
		}

		@Override
		public Driver driver() {
			return Driver.PLAYER_TOGGLE_SPRINT;
		}

		@Override
		public String docs() {
			return "{player: <macro> The player who toggled their sprinting state | sprinting: <boolean match> Whether or not the player is now sprinting | world: <macro>}"
					+ " Called when a player toggles their sprinting state."
					+ " {player: The player who toggled their sprinting state | sprinting: Whether or not the player is now sprinting |"
					+ " location: Where the player is}"
					+ " {}"
					+ " {}";
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

		@Override
		public boolean matches(Map<String, Construct> prefilter, BindableEvent event) throws PrefilterNonMatchException {
			if (event instanceof MCPlayerToggleSprintEvent) {
				MCPlayerToggleSprintEvent ptse = (MCPlayerToggleSprintEvent) event;
				MCPlayer player = ptse.getPlayer();
				Prefilters.match(prefilter, "player", player.getName(), PrefilterType.MACRO);
				Prefilters.match(prefilter, "sprinting", ptse.isSprinting(), PrefilterType.BOOLEAN_MATCH);
				Prefilters.match(prefilter, "world", player.getWorld().getName(), PrefilterType.MACRO);
				return true;
			} else {
				return false;
			}
		}

		@Override
		public BindableEvent convert(CArray manualObject, Target t) {
			return null;
		}

		@Override
		public Map<String, Construct> evaluate(BindableEvent event) throws EventException {
			if (event instanceof MCPlayerToggleSprintEvent) {
				MCPlayerToggleSprintEvent ptse = (MCPlayerToggleSprintEvent) event;
				Map<String, Construct> mapEvent = evaluate_helper(event);
				MCPlayer player = ptse.getPlayer();
				mapEvent.put("player", new CString(player.getName(), Target.UNKNOWN));
				mapEvent.put("location", ObjectGenerator.GetGenerator().location(player.getLocation()));
				mapEvent.put("sprinting", CBoolean.get(ptse.isSprinting()));
				return mapEvent;
			} else {
				throw new EventException("Cannot convert event to PlayerToggleSprintEvent");
			}
		}

		@Override
		public boolean modifyEvent(String key, Construct value, BindableEvent event) {
			return false;
		}
	}
}
