

package com.laytonsmith.core.events.drivers;

import com.laytonsmith.PureUtilities.Geometry.Point3D;
import com.laytonsmith.PureUtilities.StringUtils;
import com.laytonsmith.abstraction.*;
import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.abstraction.blocks.MCBlockFace;
import com.laytonsmith.abstraction.enums.MCAction;
import com.laytonsmith.abstraction.enums.MCDamageCause;
import com.laytonsmith.abstraction.enums.MCFishingState;
import com.laytonsmith.abstraction.enums.MCTeleportCause;
import com.laytonsmith.abstraction.events.*;
import com.laytonsmith.annotations.api;
import com.laytonsmith.commandhelper.CommandHelperPlugin;
import com.laytonsmith.core.*;
import com.laytonsmith.core.constructs.*;
import com.laytonsmith.core.environments.CommandHelperEnvironment;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.events.*;
import com.laytonsmith.core.events.BoundEvent.ActiveEvent;
import com.laytonsmith.core.events.Prefilters.PrefilterType;
import com.laytonsmith.core.events.drivers.EntityEvents.entity_death;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.exceptions.EventException;
import com.laytonsmith.core.exceptions.PrefilterNonMatchException;
import com.laytonsmith.core.functions.Exceptions;
import com.laytonsmith.core.functions.StringHandling;
import com.laytonsmith.core.natives.interfaces.Mixed;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.UnknownFormatConversionException;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class PlayerEvents {
    public static String docs(){
        return "Contains events related to a player";
    }

	@api
	public static class player_consume extends AbstractEvent {

		public String getName() {
			return "player_consume";
		}

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

		public boolean matches(Map<String, Mixed> prefilter, BindableEvent e, Target t) throws PrefilterNonMatchException {
			if (e instanceof MCPlayerItemConsumeEvent) {
				MCPlayerItemConsumeEvent event = (MCPlayerItemConsumeEvent) e;
				Prefilters.match(prefilter, "item", Static.ParseItemNotation(event.getItem()), PrefilterType.ITEM_MATCH);
				return true;
			}
			return false;
		}

		public BindableEvent convert(CArray manualObject) {
			MCPlayer p = Static.GetPlayer(manualObject.get("player"), Target.UNKNOWN);
			MCItemStack i = ObjectGenerator.GetGenerator().item(manualObject.get("item"), Target.UNKNOWN);
			return EventBuilder.instantiate(MCPlayerItemConsumeEvent.class, p, i);
		}

		public Map<String, Mixed> evaluate(BindableEvent event)
				throws EventException {
			if (event instanceof MCPlayerItemConsumeEvent) {
				MCPlayerItemConsumeEvent e = (MCPlayerItemConsumeEvent) event;
				Map<String, Mixed> ret = evaluate_helper(e);
				Mixed item = ObjectGenerator.GetGenerator().item(e.getItem(), Target.UNKNOWN);
				ret.put("item", item);
				return ret;
			} else {
				throw new EventException("Cannot convert to MCPlayerItemConsumeEvent");
			}
		}

		public Driver driver() {
			return Driver.PLAYER_CONSUME;
		}

		public boolean modifyEvent(String key, Mixed value, BindableEvent event, Target t) {
			if (event instanceof MCPlayerItemConsumeEvent) {
				MCPlayerItemConsumeEvent e = (MCPlayerItemConsumeEvent) event;
				if (key.equalsIgnoreCase("item")) {
					e.setItem(ObjectGenerator.GetGenerator().item(value, t));
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
    public static class player_kick extends AbstractEvent {

        public String getName() {
            return "player_kick";
        }

        public String docs() {
            return "{player: <macro> | reason: <macro>}"
                    + "Fired when a player is kicked from the game. "
                    + "{player: the kicked player | message: the message shown to all online"
                    + " players | reason: the message shown to the player getting kicked}"
                    + "{message|reason}"
                    + "{player|message|reason}";
        }

        public boolean matches(Map<String, Mixed> prefilter, BindableEvent e, Target t)
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

        public BindableEvent convert(CArray manualObject) {
            return null;
        }

        public Map<String, Mixed> evaluate(BindableEvent e)
                throws EventException {
            if (e instanceof MCPlayerKickEvent) {
                MCPlayerKickEvent event = (MCPlayerKickEvent) e;
                Map<String, Mixed> map = evaluate_helper(e);
                //Fill in the event parameters
                map.put("message", new CString(event.getMessage(), Target.UNKNOWN));
                map.put("reason", new CString(event.getReason(), Target.UNKNOWN));
                return map;
            } else {
                throw new EventException("Cannot convert e to MCPlayerKickEvent");
            }
        }

        public Driver driver() {
            return Driver.PLAYER_KICK;
        }

        public boolean modifyEvent(String key, Mixed value,
                BindableEvent event, Target t) {
            if(event instanceof MCPlayerKickEvent){
                MCPlayerKickEvent e = (MCPlayerKickEvent) event;
                if(key.equalsIgnoreCase("message")){
                    e.setMessage(value == null?null:value.val());
                    return true;
                }
                if(key.equalsIgnoreCase("reason")){
                    e.setReason(value == null?null:value.val());
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
	public static class player_teleport extends AbstractEvent {
		public String getName() {
			return "player_teleport";
		}

		public String docs() {
			return "{player: <macro> The player that teleport. Switching worlds will trigger this event, but world_changed is called "
				+ "after, only if this isn't cancelled first."
				+ "| from: <custom> This should be a location array (x, y, z, world)."
				+ " The location is matched via block matching, so if the array's x parameter were 1, if the player"
				+ "moved from 1.3, that parameter would match."
				+ "| to: <custom> The location the player is now in. This should be a location array as well. "
				+ "{player | from: The location the player is coming from | to: The location the player is now in | "
				+ "type: the type of teleport occuring, one of: " + StringUtils.Join(MCTeleportCause.values(), ", ") + "}"
				+ "{to}"
				+ "{}";
		}

		public boolean matches(Map<String, Mixed> prefilter, BindableEvent e, Target t) throws PrefilterNonMatchException {
			if(e instanceof MCPlayerTeleportEvent){
				MCPlayerTeleportEvent event = (MCPlayerTeleportEvent)e;

				if (prefilter.containsKey("player")) {
					if (!(prefilter.get("player").toString().equalsIgnoreCase(event.getPlayer().getName()))){
						return false;
					}
				}

				if (prefilter.containsKey("type")) {
					if (!(prefilter.get("type").toString().equalsIgnoreCase(event.getCause()))) {
						return false;
					}
				}

				if (prefilter.containsKey("from")){
					MCLocation pLoc = ObjectGenerator.GetGenerator().location(prefilter.get("from"), event.getPlayer().getWorld(), Target.UNKNOWN);
					MCLocation loc = event.getFrom();

					if(loc.getBlockX() != pLoc.getBlockX() || loc.getBlockY() != pLoc.getBlockY() || loc.getBlockZ() != pLoc.getBlockZ()){
						return false;
					}
				}

				if(prefilter.containsKey("to")){
					MCLocation pLoc = ObjectGenerator.GetGenerator().location(prefilter.get("to"), event.getPlayer().getWorld(), Target.UNKNOWN);
					MCLocation loc = event.getFrom();

					if(loc.getBlockX() != pLoc.getBlockX() || loc.getBlockY() != pLoc.getBlockY() || loc.getBlockZ() != pLoc.getBlockZ()){
						return false;
					}
				}

				return true;

			}

			return false ;
		}

		public BindableEvent convert(CArray manualObject) {
			MCPlayer p = Static.GetPlayer(manualObject.get("player"), Target.UNKNOWN);
			MCLocation from = ObjectGenerator.GetGenerator().location(manualObject.get("from"), p.getWorld(), manualObject.getTarget());
			MCLocation to = ObjectGenerator.GetGenerator().location(manualObject.get("to"), p.getWorld(), manualObject.getTarget());
			return EventBuilder.instantiate(MCPlayerTeleportEvent.class, p, from, to);
		}

		public Map<String, Mixed> evaluate(BindableEvent e) throws EventException {
			if (e instanceof MCPlayerTeleportEvent) {
                MCPlayerTeleportEvent event = (MCPlayerTeleportEvent) e;
                Map<String, Mixed> map = evaluate_helper(e);

                //Fill in the event parameters
				map.put("player", new CString(event.getPlayer().getName(), Target.UNKNOWN));
                map.put("from", ObjectGenerator.GetGenerator().location(event.getFrom()));
                map.put("to", ObjectGenerator.GetGenerator().location(event.getTo()));
				map.put("type", new CString(event.getCause(), Target.UNKNOWN));

                return map;
            } else {
                throw new EventException("Cannot convert e to MCPlayerMovedEvent");
            }
		}

		public Driver driver() {
			return Driver.PLAYER_TELEPORT;
		}

		public boolean modifyEvent(String key, Mixed value, BindableEvent event, Target t) {
			if (event instanceof MCPlayerTeleportEvent) {
				MCPlayerTeleportEvent e = (MCPlayerTeleportEvent)event;

				if (key.equalsIgnoreCase("to")) {
					MCLocation loc = ObjectGenerator.GetGenerator().location((Mixed)value, null, t);
					e.setTo(loc);

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
    public static class player_prelogin extends AbstractEvent {

		public String getName() {
			return "player_prelogin";
		}

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

		public boolean matches(Map<String, Mixed> prefilter, BindableEvent e, Target t)
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

		public BindableEvent convert(CArray manualObject) {
			return null;
		}

		public Map<String, Mixed> evaluate(BindableEvent e)
				throws EventException {
			if(e instanceof MCPlayerPreLoginEvent){
                MCPlayerPreLoginEvent event = (MCPlayerPreLoginEvent) e;
                Map<String, Mixed> map = evaluate_helper(e);

                map.put("player", new CString(event.getName(), Target.UNKNOWN));
                map.put("ip", new CString(event.getIP(), Target.UNKNOWN));
                map.put("result", new CString(event.getResult(), Target.UNKNOWN));
                map.put("kickmsg", new CString(event.getKickMessage(), Target.UNKNOWN));

                return map;
            } else{
                throw new EventException("Cannot convert e to PlayerPreLoginEvent");
            }
		}

		public Driver driver() {
			return Driver.PLAYER_PRELOGIN;
		}

		public boolean modifyEvent(String key, Mixed value,
				BindableEvent e, Target t) {
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

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

    }

    @api
    public static class player_login extends AbstractEvent {

		public String getName() {
			return "player_login";
		}

		public String docs() {
			return "{player: <string match>} "
					+ "This event is called when a player is about to log in. "
                    + "This event cannot be cancelled. Instead, you can deny them by setting "
					+ "'result' to KICK_BANNED, KICK_WHITELIST, KICK_OTHER, or KICK_FULL. "
                    + "The default for 'result' is ALLOWED. When setting 'result', you "
                    + "can specify the kick message by modifying 'kickmsg'. "
                    + "{player: The player's name | kickmsg: The default kick message | "
                    + "ip: the player's IP address | result: the default response to their logging in}"
                    + "{kickmsg|result}"
                    + "{player|kickmsg|ip|result}";
		}

		public boolean matches(Map<String, Mixed> prefilter, BindableEvent e, Target t)
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

		public BindableEvent convert(CArray manualObject) {
			return null;
		}

		public Map<String, Mixed> evaluate(BindableEvent e)
				throws EventException {
			if(e instanceof MCPlayerLoginEvent){
                MCPlayerLoginEvent event = (MCPlayerLoginEvent) e;
                Map<String, Mixed> map = evaluate_helper(e);

                map.put("player", new CString(event.getName(), Target.UNKNOWN));
                map.put("ip", new CString(event.getIP(), Target.UNKNOWN));
				//TODO: The event.getResult needs to be enum'd
                map.put("result", new CString(event.getResult(), Target.UNKNOWN));
                map.put("kickmsg", new CString(event.getKickMessage(), Target.UNKNOWN));

                return map;
            } else{
                throw new EventException("Cannot convert e to PlayerLoginEvent");
            }
		}

		public Driver driver() {
			return Driver.PLAYER_LOGIN;
		}

		public boolean modifyEvent(String key, Mixed value,
				BindableEvent e, Target t) {
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

        public String getName() {
            return "player_join";
        }

        public String docs() {
            return "{player: <string match> |"
                    + "join_message: <regex>} This event is called when a player logs in. "
                    + "Setting join_message to null causes it to not be displayed at all. Cancelling "
                    + "the event does not prevent them from logging in. Instead, you should just kick() them."
                    + "{player: The player's name | join_message: The default join message | first_login: True if this is the first time"
                    + " the player has logged in.}"
                    + "{join_message}"
                    + "{player|join_message}";
        }

        public CHVersion since() {
            return CHVersion.V3_3_0;
        }

        public Driver driver(){
            return Driver.PLAYER_JOIN;
        }

        public boolean matches(Map<String, Mixed> prefilter, BindableEvent e, Target t) throws PrefilterNonMatchException {
            if(e instanceof MCPlayerJoinEvent){
                MCPlayerJoinEvent ple = (MCPlayerJoinEvent) e;
                if(prefilter.containsKey("player")){
                    if(!ple.getPlayer().getName().equals(prefilter.get("player").val())){
                        return false;
                    }
                }
                Prefilters.match(prefilter, "join_message", ple.getJoinMessage(), Prefilters.PrefilterType.REGEX);
                return true;
            }
            return false;
        }

        public Map<String, Mixed> evaluate(BindableEvent e) throws EventException {
            if(e instanceof MCPlayerJoinEvent){
                MCPlayerJoinEvent ple = (MCPlayerJoinEvent) e;
                Map<String, Mixed> map = evaluate_helper(e);
                //map.put("player", new CString(ple.getPlayer().getName(), Target.UNKNOWN));
                map.put("join_message", new CString(ple.getJoinMessage(), Target.UNKNOWN));
                map.put("first_login", new CBoolean(ple.getPlayer().isNewPlayer(), Target.UNKNOWN));
                return map;
            } else{
                throw new EventException("Cannot convert e to PlayerLoginEvent");
            }
        }

        public boolean modifyEvent(String key, Mixed value, BindableEvent event, Target t) {
            if(event instanceof MCPlayerJoinEvent){
                MCPlayerJoinEvent pje = (MCPlayerJoinEvent)event;
                if(key.equals("join_message") || key.equals("message")){
                    if(value == null){
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

        public BindableEvent convert(CArray manual){
            MCPlayerJoinEvent e = EventBuilder.instantiate(MCPlayerJoinEvent.class, Static.GetPlayer(manual.get("player").val(), Target.UNKNOWN),
                    manual.get("join_message").val());
            return e;
        }

    }

    @api
    public static class player_interact extends AbstractEvent {

        public String getName() {
            return "player_interact";
        }

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

        public CHVersion since() {
            return CHVersion.V3_3_0;
        }

        public Driver driver() {
            return Driver.PLAYER_INTERACT;
        }

        public boolean matches(Map<String, Mixed> prefilter, BindableEvent e, Target t) throws PrefilterNonMatchException {
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

        public Map<String, Mixed> evaluate(BindableEvent e) throws EventException {
            if(e instanceof MCPlayerInteractEvent){
                MCPlayerInteractEvent pie = (MCPlayerInteractEvent) e;
                Map<String, Mixed> map = evaluate_helper(e);
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
                    MCBlock b = pie.getClickedBlock();
                    map.put("location", new CArray(Target.UNKNOWN, new CInt(b.getX(), Target.UNKNOWN),
                            new CInt(b.getY(), Target.UNKNOWN), new CInt(b.getZ(), Target.UNKNOWN),
                            new CString(b.getWorld().getName(), Target.UNKNOWN)));
                }
                map.put("item", new CString(Static.ParseItemNotation(pie.getItem()), Target.UNKNOWN));
                return map;
            } else {
                throw new EventException("Cannot convert e to PlayerInteractEvent");
            }
        }

        public BindableEvent convert(CArray manual){
            MCPlayer p = Static.GetPlayer(manual.get("player"), Target.UNKNOWN);
            MCAction a = MCAction.valueOf(manual.get("action").val().toUpperCase());
            MCItemStack is = Static.ParseItemNotation("player_interact event", manual.get("item").val(), 1, Target.UNKNOWN);
            MCBlock b = ObjectGenerator.GetGenerator().location(manual.get("location"), null, Target.UNKNOWN).getBlock();
            MCBlockFace bf = MCBlockFace.valueOf(manual.get("facing").val().toUpperCase());
            MCPlayerInteractEvent e = EventBuilder.instantiate(MCPlayerInteractEvent.class, p, a, is, b, bf);
            return e;
        }

        public boolean modifyEvent(String key, Mixed value, BindableEvent event, Target t) {
            if(event instanceof MCPlayerInteractEvent){
                MCPlayerInteractEvent pie = (MCPlayerInteractEvent)event;

            }
            return false;
        }

    }
	
    public abstract static class player_bed_event extends AbstractEvent {
		
		public boolean matches(Map<String, Mixed> prefilter, BindableEvent e, Target t) throws PrefilterNonMatchException {
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

		public BindableEvent convert(CArray manual) {
			MCPlayer p = Static.GetPlayer(manual.get("player"), Target.UNKNOWN);
            MCBlock b = ObjectGenerator.GetGenerator().location(manual.get("location"), null, Target.UNKNOWN).getBlock();
            
			MCPlayerBedEvent e = EventBuilder.instantiate(MCPlayerBedEvent.class, p, b);
            
			return e;
		}

		public Map<String, Mixed> evaluate(BindableEvent e) throws EventException {
			if(e instanceof MCPlayerBedEvent){
                MCPlayerBedEvent bee = (MCPlayerBedEvent) e;
                Map<String, Mixed> map = evaluate_helper(e);
				
                map.put("location", ObjectGenerator.GetGenerator().location(bee.getBed().getLocation()));
				map.put("player", new CString(bee.getPlayer().getName(), Target.UNKNOWN));
				
				return map;
			} else {
				throw new EventException("Cannot convert e to an appropriate PlayerBedEvent.");
			}
		}

		public Driver driver() {
			return Driver.PLAYER_BED_EVENT;
		}

		public boolean modifyEvent(String key, Mixed value, BindableEvent event, Target t) {
			return false;
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}
	
	@api
	public static class player_enter_bed extends player_bed_event {
		
		public String docs() {
			return "{location: The location of the bed} "
                    + "Fires when a player tries to enter a bed."
                    + "{location: The location of the bed |"
                    + " player: The player associated with this event}"
                    + "{}"
                    + "{location|player}";
		}

		public String getName() {
			return "player_enter_bed";
		}
	}
	
	@api
	public static class player_leave_bed extends player_bed_event {
		
		public String docs() {
			return "{location: The location of the bed} "
                    + "Fires when a player leaves a bed."
                    + "{location: The location of the bed |"
                    + " player: The player associated with this event}"
                    + "{}"
                    + "{location|player}";
		}

		public String getName() {
			return "player_leave_bed";
		}
	}
	
	@api
    public static class pressure_plate_activated extends AbstractEvent {

		public String getName() {
			return "pressure_plate_activated";
		}

		public String docs() {
			return "{location: The location of the pressure plate | activated: If true, only will trigger when the plate is stepped on. Currently,"
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

		public boolean matches(Map<String, Mixed> prefilter, BindableEvent e, Target t) throws PrefilterNonMatchException {
			if(e instanceof MCPlayerInteractEvent){
                MCPlayerInteractEvent pie = (MCPlayerInteractEvent)e;
                if(!((MCPlayerInteractEvent)e).getAction().equals(MCAction.PHYSICAL)){
                    return false;
                }
				if(prefilter.containsKey("location")){
					MCLocation loc = ObjectGenerator.GetGenerator().location(prefilter.get("location"), null, t);
					if(!pie.getClickedBlock().getLocation().equals(loc)){
						return false;
					}
				}
				if(prefilter.containsKey("activated")){
					//TODO: Once activation is supported, check for that here
				}
				return true;
			}
			return false;
		}

		public BindableEvent convert(CArray manual) {
			MCPlayer p = Static.GetPlayer(manual.get("player"), Target.UNKNOWN);
            MCBlock b = ObjectGenerator.GetGenerator().location(manual.get("location"), null, Target.UNKNOWN).getBlock();
            MCPlayerInteractEvent e = EventBuilder.instantiate(MCPlayerInteractEvent.class, p, MCAction.PHYSICAL, null, b, MCBlockFace.UP);
            return e;
		}

		public Map<String, Mixed> evaluate(BindableEvent e) throws EventException {
			if(e instanceof MCPlayerInteractEvent){
                MCPlayerInteractEvent pie = (MCPlayerInteractEvent) e;
                Map<String, Mixed> map = evaluate_helper(e);
                map.put("location", ObjectGenerator.GetGenerator().location(pie.getClickedBlock().getLocation()));
				//TODO: Once activation is supported, set that appropriately here.
				map.put("activated", new CBoolean(true, Target.UNKNOWN));
				return map;
			} else {
				throw new EventException("Cannot convert e to PlayerInteractEvent");
			}
		}

		public Driver driver() {
			return Driver.PLAYER_INTERACT;
		}

		public boolean modifyEvent(String key, Mixed value, BindableEvent event, Target t) {
			return false;
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
		
	}

    @api
    public static class player_spawn extends AbstractEvent {

        public String getName() {
            return "player_spawn";
        }

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

        public Driver driver() {
            return Driver.PLAYER_SPAWN;
        }

        public CHVersion since() {
            return CHVersion.V3_3_0;
        }

        public boolean matches(Map<String, Mixed> prefilter, BindableEvent e, Target t) throws PrefilterNonMatchException {
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

        public Map<String, Mixed> evaluate(BindableEvent e) throws EventException {
            if (e instanceof MCPlayerRespawnEvent) {
                MCPlayerRespawnEvent event = (MCPlayerRespawnEvent) e;
                Map<String, Mixed> map = evaluate_helper(e);
                //the helper puts the player in for us
                CArray location = ObjectGenerator.GetGenerator().location(event.getRespawnLocation());
                map.put("location", location);
				map.put("bed_spawn", new CBoolean(event.isBedSpawn(), Target.UNKNOWN));
                return map;
            } else {
                throw new EventException("Cannot convert e to PlayerRespawnEvent");
            }
        }

        public BindableEvent convert(CArray manual) {
            //For firing off the event manually, we have to convert the CArray into an
            //actual object that will trigger it
            MCPlayer p = Static.GetPlayer(manual.get("player"), Target.UNKNOWN);
            MCLocation l = ObjectGenerator.GetGenerator().location(manual.get("location"), p.getWorld(), Target.UNKNOWN);
            MCPlayerRespawnEvent e = EventBuilder.instantiate(MCPlayerRespawnEvent.class, p, l, false);
            return e;
        }

        public boolean modifyEvent(String key, Mixed value, BindableEvent event, Target t) {
            if (event instanceof MCPlayerRespawnEvent) {
                MCPlayerRespawnEvent e = (MCPlayerRespawnEvent) event;
                if (key.equals("location")) {
                    //Change this parameter in e to value
                    e.setRespawnLocation(ObjectGenerator.GetGenerator().location((Mixed)value, e.getPlayer().getWorld(), t));
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
                    + "{player: The player that died | drops: An array of the dropped items"
                    + "| xp: The xp that will be dropped | cause: The cause of death | death_message: The"
					+ " death message | keep_level | new_level: the player's level when they respawn"
					+ "| killer: The name of the killer, if a player killed them, otherwise, null}"
                    + "{xp|drops: An array of item objects, or null. The items to be dropped"
					+ " are replaced with the given items, not added to|death_message: the death message,"
					+ " or null to remove it entirely|keep_level: if true, the player will not lose"
					+ " their xp and levels|new_level}"
                    + "{player | drops | death_message}";
        }

		@Override
        public Driver driver() {
            return Driver.PLAYER_DEATH;
        }

		@Override
        public CHVersion since() {
            return CHVersion.V3_3_0;
        }

        public boolean matches(Map<String, Mixed> prefilter, BindableEvent e, Target t) throws PrefilterNonMatchException {
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
        public Map<String, Mixed> evaluate(BindableEvent e) throws EventException {
            if (e instanceof MCPlayerDeathEvent) {
                MCPlayerDeathEvent event = (MCPlayerDeathEvent) e;
				Map<String, Mixed> map = super.evaluate(e);
				map.putAll(evaluate_helper(e));
				map.put("death_message", new CString(event.getDeathMessage(), Target.UNKNOWN));
				map.put("keep_level", new CBoolean(event.getKeepLevel(), Target.UNKNOWN));
				map.put("new_level", new CInt(event.getNewLevel(), Target.UNKNOWN));
				if(event.getKiller() instanceof MCPlayer){
					map.put("killer", new CString(((MCPlayer)event.getKiller()).getName(), Target.UNKNOWN));
				} else {
					map.put("killer", null);
				}
                return map;
            } else {
                throw new EventException("Cannot convert e to EntityDeathEvent");
            }
        }

		@Override
        public BindableEvent convert(CArray manual) {
            //For firing off the event manually, we have to convert the CArray into an
            //actual object that will trigger it
            String splayer = manual.get("player").val();
            List<MCItemStack> list = new ArrayList<MCItemStack>();
            String deathMessage = manual.get("death_message").val();
            CArray clist = (CArray)manual.get("drops");
            for(String key : clist.keySet()){
                list.add(ObjectGenerator.GetGenerator().item(clist.get(key), clist.getTarget()));
            }
            MCPlayerDeathEvent e = EventBuilder.instantiate(MCPlayerDeathEvent.class, Static.GetPlayer(splayer, Target.UNKNOWN), list,
                    0, deathMessage);
            return e;
        }

        //Given the paramters, change the underlying event
		@Override
        public boolean modifyEvent(String key, Mixed value, BindableEvent event, Target t) {
			if (super.modifyEvent(key, value, event, t)) {
				return true;
			} else if (event instanceof MCPlayerDeathEvent) {
                MCPlayerDeathEvent e = (MCPlayerDeathEvent) event;
                if (key.equals("xp")) {
                    //Change this parameter in e to value
                    e.setDroppedExp(value.primitive(t).castToInt32(t));
					return true;
				}
                if(key.equals("death_message")){
                    e.setDeathMessage(value == null?null:value.val());
                    return true;
                }
                if(key.equals("drops")){
                    if(value == null){
                        value = new CArray(t);
                    }
                    if(!(value instanceof CArray)){
                        throw new ConfigRuntimeException("drops must be an array, or null", Exceptions.ExceptionType.CastException, t);
                    }
                    e.clearDrops();
                    CArray drops = (CArray) value;
                    for(String dropID : drops.keySet()){
                        e.addDrop(ObjectGenerator.GetGenerator().item(drops.get(dropID), t));
                    }
                    return true;
                }
                if(event instanceof MCPlayerDeathEvent && key.equals("death_message")){
                    ((MCPlayerDeathEvent)event).setDeathMessage(value == null?null:value.val());
                    return true;
                }
				if (key.equals("keep_level")) {
					e.setKeepLevel(value.primitive(t).castToBoolean());
					return true;
				}
				if (key.equals("new_level")) {
					e.setNewLevel(value.primitive(t).castToInt32(t));
					return true;
				}
            }
            return false;
        }
    }

    @api
    public static class player_quit extends AbstractEvent {

        public String getName() {
            return "player_quit";
        }

        public String docs() {
            return "{player: <macro>}"
                    + "Fired when any player quits."
                    + "{message: The message to be sent}"
                    + "{message}"
                    + "{player|message}";
        }

        public Driver driver() {
            return Driver.PLAYER_QUIT;
        }

        public CHVersion since() {
            return CHVersion.V3_3_1;
        }

        public boolean matches(Map<String, Mixed> prefilter, BindableEvent e, Target t) throws PrefilterNonMatchException {
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

        public BindableEvent convert(CArray manualObject) {
            //Get the parameters from the manualObject
            MCPlayer player = Static.GetPlayer(manualObject.get("player"), Target.UNKNOWN);
            String message = manualObject.get("message").val();

            BindableEvent e = EventBuilder.instantiate(MCPlayerCommandEvent.class,
                player, message);
            return e;
        }

        public Map<String, Mixed> evaluate(BindableEvent e) throws EventException {
            if (e instanceof MCPlayerQuitEvent) {
                MCPlayerQuitEvent event = (MCPlayerQuitEvent) e;
                Map<String, Mixed> map = evaluate_helper(e);
                //Fill in the event parameters
                map.put("message", new CString(event.getMessage(), Target.UNKNOWN));
                return map;
            } else {
                throw new EventException("Cannot convert e to MCPlayerQuitEvent");
            }
        }

        public boolean modifyEvent(String key, Mixed value, BindableEvent event, Target t) {
            if (event instanceof MCPlayerQuitEvent) {
                MCPlayerQuitEvent e = (MCPlayerQuitEvent)event;
                if("message".equals(key)){
                    e.setMessage(value == null?null:value.val());
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

        public String getName() {
            return "player_chat";
        }

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

        public Driver driver() {
            return Driver.PLAYER_CHAT;
        }

        public CHVersion since() {
            return CHVersion.V3_3_0;
        }

        public boolean matches(Map<String, Mixed> prefilter, BindableEvent e, Target t) throws PrefilterNonMatchException {
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

        public BindableEvent convert(CArray manualObject) {
            //Get the parameters from the manualObject
            MCPlayer player = Static.GetPlayer(manualObject.get("player"), Target.UNKNOWN);
            String message = manualObject.get("message").val();
			String format = manualObject.get("format").val();

            BindableEvent e = EventBuilder.instantiate(MCPlayerChatEvent.class,
                player, message, format);
            return e;
        }

        public Map<String, Mixed> evaluate(BindableEvent e) throws EventException {
            if (e instanceof MCPlayerChatEvent) {
                MCPlayerChatEvent event = (MCPlayerChatEvent) e;
                Map<String, Mixed> map = evaluate_helper(e);
                //Fill in the event parameters
                map.put("message", new CString(event.getMessage(), Target.UNKNOWN));
                CArray ca = new CArray(Target.UNKNOWN);
                for(MCPlayer recipient : event.getRecipients()){
                    ca.push(new CString(recipient.getName(), Target.UNKNOWN));
                }
				map.put("format", new CString(event.getFormat(), Target.UNKNOWN));
                map.put("recipients", ca);
                return map;
            } else {
                throw new EventException("Cannot convert e to MCPlayerChatEvent");
            }
        }

        public boolean modifyEvent(String key, Mixed value, BindableEvent event, Target t) {
            if (event instanceof MCPlayerChatEvent) {
                MCPlayerChatEvent e = (MCPlayerChatEvent)event;
                if("message".equals(key)){
                    e.setMessage(value == null?null:value.val());
                }
                if("recipients".equals(key)){
                    if(value instanceof CArray){
                        List<MCPlayer> list = new ArrayList<MCPlayer>();
                        for(String index : ((CArray)value).keySet()){
                            Mixed v = ((CArray)value).get(index);
                            try{
                                list.add(Static.GetPlayer(v, t));
                            } catch(ConfigRuntimeException ex){
                                //Ignored
                            }
                        }
                        e.setRecipients(list);
                    } else {
                        throw new ConfigRuntimeException("recipients must be an array", Exceptions.ExceptionType.CastException, t);
                    }
                }
				if("format".equals(key)){
					try{
						e.setFormat(value.primitive(t).castToString());
					} catch(UnknownFormatConversionException ex){
						throw new Exceptions.FormatException(ex.getMessage(), t);
					}
				}
                return true;
            }
            return false;
        }
    }

    @api
    public static class player_command extends AbstractEvent {

        public String getName() {
            return "player_command";
        }

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

        public Driver driver() {
            return Driver.PLAYER_COMMAND;
        }

        public CHVersion since() {
            return CHVersion.V3_3_1;
        }

        public boolean matches(Map<String, Mixed> prefilter, BindableEvent e, Target t) throws PrefilterNonMatchException {
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
                        if(!ca.get(0).val().equals(prefilter.get("prefix").val())){
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

        public BindableEvent convert(CArray manualObject) {
            MCPlayer player = Static.GetPlayer(manualObject.get("player"), Target.UNKNOWN);
            String command = manualObject.get("command").val();

            BindableEvent e = EventBuilder.instantiate(MCPlayerCommandEvent.class,
                player, command);
            return e;
        }

        public Map<String, Mixed> evaluate(BindableEvent e) throws EventException {
            if (e instanceof MCPlayerCommandEvent) {
                MCPlayerCommandEvent event = (MCPlayerCommandEvent) e;
                Map<String, Mixed> map = evaluate_helper(e);
                //Fill in the event parameters
                map.put("command", new CString(event.getCommand(), Target.UNKNOWN));

                StringHandling.parse_args pa = new StringHandling.parse_args();
                CArray ca = (CArray)pa.exec(Target.UNKNOWN, null, new CString(event.getCommand(), Target.UNKNOWN));
                map.put("prefix", new CString(ca.get(0).val(), Target.UNKNOWN));

                return map;
            } else {
                throw new EventException("Cannot convert e to MCPlayerCommandEvent");
            }
        }

        public boolean modifyEvent(String key, Mixed value, BindableEvent event, Target t) {
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

        public String getName() {
            return "world_changed";
        }

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

        public Driver driver() {
            return Driver.WORLD_CHANGED;
        }

        public CHVersion since() {
            return CHVersion.V3_3_1;
        }

        public boolean matches(Map<String, Mixed> prefilter, BindableEvent e, Target t) throws PrefilterNonMatchException {
            if (e instanceof MCWorldChangedEvent) {
                MCWorldChangedEvent event = (MCWorldChangedEvent) e;
                Prefilters.match(prefilter, "player", event.getPlayer().getName(), PrefilterType.MACRO);
                Prefilters.match(prefilter, "from", event.getFrom().getName(), PrefilterType.STRING_MATCH);
                Prefilters.match(prefilter, "to", event.getTo().getName(), PrefilterType.STRING_MATCH);
                return true;
            }
            return false;
        }

        public BindableEvent convert(CArray manualObject) {
            MCPlayer player = Static.GetPlayer(manualObject.get("player"), Target.UNKNOWN);
            MCWorld from = Static.getServer().getWorld(manualObject.get("from").val());

            BindableEvent e = EventBuilder.instantiate(MCPlayerCommandEvent.class,
                player, from);
            return e;
        }

        public Map<String, Mixed> evaluate(BindableEvent e) throws EventException {
            if (e instanceof MCWorldChangedEvent) {
                MCWorldChangedEvent event = (MCWorldChangedEvent) e;
                Map<String, Mixed> map = evaluate_helper(e);
                //Fill in the event parameters
                map.put("from", new CString(event.getFrom().getName(), Target.UNKNOWN));
                map.put("to", new CString(event.getTo().getName(), Target.UNKNOWN));
                return map;
            } else {
                throw new EventException("Cannot convert e to MCWorldChangedEvent");
            }
        }

        public boolean modifyEvent(String key, Mixed value, BindableEvent event, Target t) {
            if (event instanceof MCWorldChangedEvent) {
                MCWorldChangedEvent e = (MCWorldChangedEvent) event;

                return true;
            }
            return false;
        }

    }

	@api
	public static class player_move extends AbstractEvent{

		/*
		 * TODO:
		 * 1. Add player prefilter
		 * 2. See if the same event can be fired with different from fields, so that
		 * one move only causes one "chain" of handlers to be fired.
		 * 3. Add both region points and named WG region support in the prefilters.
		 * (The sk_ functiosn can probably be used directly)
		 * 4. Figure out why the cancel() isn't working.
		 * 5. Tie this in to player teleport events. Probably set a prefilter that determines
		 * whether or not a teleport should count as a movement or not.
		 */

		private boolean threadRunning = false;
		private Set<Integer> thresholdList = new HashSet<Integer>();
		private Map<Integer, Map<String, MCLocation>> thresholds = new HashMap<Integer, Map<String, MCLocation>>();

		@Override
		public void bind(Map<String, Mixed> prefilters, Target t) {
			if(prefilters.containsKey("threshold")){
				int i = prefilters.get("threshold").primitive(t).castToInt32(t);
				thresholdList.add(i);
			}
			if(!threadRunning){
				thresholdList.add(1);
				threadRunning = true;
				new Thread(new Runnable() {

					public void run() {
						outerLoop: while(true){
							MCPlayer players[] = Static.getServer().getOnlinePlayers();
							for(final MCPlayer p : players){
								//We need to loop through all the thresholds
								//and see if any of the points meet them. If so,
								//we know we need to fire the event. If none of them
								//match, carry on with the next player. As soon as
								//one matches though, we can't quit the loop, because
								//we have to set all the thresholds.
								thresholdLoop: for(Integer i : thresholdList){
									if(thresholds.containsKey(i) && thresholds.get(i).containsKey(p.getName())){
										final MCLocation last = thresholds.get(i).get(p.getName());
										final MCLocation current = p.asyncGetLocation();
										if(!p.getWorld().getName().equals(last.getWorld().getName())){
											//They moved worlds. simply put their new location in here, then
											//continue.
											thresholds.get(i).put(p.getName(), p.getLocation());
											continue thresholdLoop;
										}
										Point3D lastPoint = new Point3D(last.getX(), last.getY(), last.getZ());
										Point3D currentPoint = new Point3D(current.getX(), current.getY(), current.getZ());
										double distance = lastPoint.distance(currentPoint);
										if(distance > i){
											//We've met the threshold.
											//Well, we're still not sure. To run the prefilters on this thread,
											//we're gonna simulate a prefilter match now. We have to run this manually,
											//because each bind could have a different threshold, and it will be expecting
											//THIS from location. Other binds will be expecting other from locations.
											final MCPlayerMoveEvent fakeEvent = new MCPlayerMoveEvent() {
												boolean cancelled = false;
												public MCPlayer getPlayer() {
													return p;
												}

												public MCLocation getFrom() {
													return last;
												}

												public MCLocation getTo() {
													return current;
												}

												public Object _GetObject() {
													return null;
												}

												public void setCancelled(boolean state) {
													cancelled = state;
												}

												public boolean isCancelled() {
													return cancelled;
												}
											};
											//We need to run the prefilters on this thread, so we have
											//to do this all by hand.
											final SortedSet<BoundEvent> toRun = EventUtils.GetMatchingEvents(Driver.PLAYER_MOVE, player_move.this.getName(), fakeEvent, player_move.this);
											//Ok, now the events to be run need to actually be run on the main server thread, so let's run that now.
											try {
												StaticLayer.GetConvertor().runOnMainThreadAndWait(new Callable<Object>(){

													public Object call() throws Exception {
														EventUtils.FireListeners(toRun, player_move.this, fakeEvent);
														return null;
													}
												});
											} catch (Exception ex) {
												Logger.getLogger(PlayerEvents.class.getName()).log(Level.SEVERE, null, ex);
											}
											if(fakeEvent.isCancelled()){
												//Put them back at the from location
												p.teleport(last);
											} else {
												thresholds.get(i).put(p.getName(), current);
											}
										}
									} else {
										//If there is no location here, just put the current location in there.
										if(!thresholds.containsKey(i)){
											Map<String, MCLocation> map = new HashMap<String, MCLocation>();
											thresholds.put(i, map);
										}
										thresholds.get(i).put(p.getName(), p.asyncGetLocation());
									}
								}
								synchronized(player_move.this){
									try {
										//Throttle this thread just a little
										player_move.this.wait(10);
									} catch (InterruptedException ex) {
										//
									}
								}
							}
						}
					}
				}, "CommandHelperPlayerMoveEventRunner").start();
			}
		}

		public String getName() {
			return "player_move";
		}

		public String docs() {
			return "{player: <macro> The player that moved. Switching worlds does not trigger this event. "
					+ "| from: <custom> This should be a location array (x, y, z, world)."
					+ " The location is matched via block matching, so if the array's x parameter were 1, if the player"
					+ "moved from 1.3, that parameter would match."
                    + "| to: <custom> The location the player is now in. This should be a location array as well."
					+ "| threshold: <custom> The minimum distance the player must have travelled before the event"
					+ " will be triggered. This is based on the 3D distance, and is measured in block units.}"
                    + " This event is fired off AFTER a player has moved. This is a read only event because of this,"
					+ " however, the determination logic is run asynchronously from the main server thread, so general"
					+ " detection of a movement will not cause any lag, beyond lag caused by any other thread. Prefilters"
					+ " are extremely important to use however, because the prefilter code is also run asynchronously,"
					+ " however your code is not, and therefore, is slower. It is also advisable to use a threshold,"
					+ " so you are not firing an event every time a player moves. A threshold of 5 or 10 will likely"
					+ " be sufficient for all use cases, and should considerably reduce server thread resources. Though this"
					+ " event is read only, you can \"cancel\" the event by moving the player back to the from location,"
					+ " or otherwise \"change\" the location by using set_ploc(). Note that on a server with"
					+ " lots of players, this \"stride\" distance, that is, the distance a player will have moved"
					+ " before the event picks it up will be greater. The movement detection thread is slightly"
					+ " throttled."
                    + "{player | from: The location the player is coming from | to: The location the player is now in}"
                    + "{}"
                    + "{}";
		}

		@Override
		public void cancel(BindableEvent o, boolean state) {
			if(o instanceof MCPlayerMoveEvent){
				((MCPlayerMoveEvent)o).setCancelled(state);
			}
		}

		@Override
		public boolean isCancellable(BindableEvent o) {
			return true;
		}

		@Override
		public boolean isCancelled(BindableEvent o) {
			if(o instanceof MCPlayerMoveEvent){
				return ((MCPlayerMoveEvent)o).isCancelled();
			} else {
				return false;
			}
		}



		public boolean matches(Map<String, Mixed> prefilter, BindableEvent e, Target t) throws PrefilterNonMatchException {
			if(e instanceof MCPlayerMoveEvent){
				MCPlayerMoveEvent event = (MCPlayerMoveEvent)e;
				if(!event.getFrom().getWorld().getName().equals(event.getTo().getWorld().getName())){
					return false;
				}
				if(prefilter.containsKey("threshold")){
					Point3D from = new Point3D(event.getFrom().getX(), event.getFrom().getY(), event.getFrom().getZ());
					Point3D to = new Point3D(event.getTo().getX(), event.getTo().getY(), event.getTo().getZ());
					double distance = from.distance(to);
					double pDistance = prefilter.get("threshold").primitive(t).castToDouble(t);
					if(pDistance > distance){
						return false;
					}
				}
				if(prefilter.containsKey("from")){
					MCLocation pLoc = ObjectGenerator.GetGenerator().location(prefilter.get("from"), event.getPlayer().getWorld(), Target.UNKNOWN);
					MCLocation loc = event.getFrom();
					if(loc.getBlockX() != pLoc.getBlockX() || loc.getBlockY() != pLoc.getBlockY() || loc.getBlockZ() != pLoc.getBlockZ()){
						return false;
					}
				}
				if(prefilter.containsKey("to")){
					MCLocation pLoc = ObjectGenerator.GetGenerator().location(prefilter.get("to"), event.getPlayer().getWorld(), Target.UNKNOWN);
					MCLocation loc = event.getFrom();
					if(loc.getBlockX() != pLoc.getBlockX() || loc.getBlockY() != pLoc.getBlockY() || loc.getBlockZ() != pLoc.getBlockZ()){
						return false;
					}
				}
				Prefilters.match(prefilter, "player", event.getPlayer().getName(), PrefilterType.MACRO);
				return true;
			}
			return false ;
		}

		public BindableEvent convert(CArray manualObject) {
			MCPlayer p = Static.GetPlayer(manualObject.get("player"), Target.UNKNOWN);
			MCLocation from = ObjectGenerator.GetGenerator().location(manualObject.get("from"), p.getWorld(), manualObject.getTarget());
			MCLocation to = ObjectGenerator.GetGenerator().location(manualObject.get("to"), p.getWorld(), manualObject.getTarget());
			return EventBuilder.instantiate(MCPlayerMoveEvent.class, p, from, to);
		}


		public Map<String, Mixed> evaluate(BindableEvent e) throws EventException {
			if (e instanceof MCPlayerMoveEvent) {
                MCPlayerMoveEvent event = (MCPlayerMoveEvent) e;
                Map<String, Mixed> map = evaluate_helper(e);
                //Fill in the event parameters
				map.put("player", new CString(event.getPlayer().getName(), Target.UNKNOWN));
                map.put("from", ObjectGenerator.GetGenerator().location(event.getFrom()));
                map.put("to", ObjectGenerator.GetGenerator().location(event.getTo()));
                return map;
            } else {
                throw new EventException("Cannot convert e to MCPlayerMovedEvent");
            }
		}

		public Driver driver() {
			return Driver.PLAYER_MOVE;
		}

		public boolean modifyEvent(String key, Mixed value, BindableEvent event, Target t) {
			//Nothing can be modified, so always return false
			return false;
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

	}

	@api
	public static class player_fish extends AbstractEvent {
	
		public String getName() {
			return "player_fish";
		}
	
		public String docs() {
			return "{state: <macro> Can be one of " + StringUtils.Join(MCFishingState.values(), ", ", ", or ")
					+ " | player: <macro> The player who is fishing}"
					+ " Fires when a player casts or reels a fishing rod {player | state | chance | xp"
					+ " | hook: the fishhook entity | caught: the id of the snared entity, can be a fish item}"
					+ " {chance: the chance of catching a fish from pulling the bobber at random"
					+ " | xp: the exp the player will get from catching a fish}"
					+ " {}";
		}
	
		public boolean matches(Map<String, Mixed> prefilter, BindableEvent e, Target t)
				throws PrefilterNonMatchException {
			if (e instanceof MCPlayerFishEvent) {
				MCPlayerFishEvent event = (MCPlayerFishEvent) e;
				Prefilters.match(prefilter, "state", event.getState().name(), PrefilterType.MACRO);
				Prefilters.match(prefilter, "player", event.getPlayer().getName(), PrefilterType.MACRO);
				return true;
			}
			return false;
		}
	
		public BindableEvent convert(CArray manualObject) {
			throw ConfigRuntimeException.CreateUncatchableException("Unsupported Operation", Target.UNKNOWN);
		}
	
		public Map<String, Mixed> evaluate(BindableEvent e)
				throws EventException {
			if (e instanceof MCPlayerFishEvent) {
				MCPlayerFishEvent event = (MCPlayerFishEvent) e;
				Target t = Target.UNKNOWN;
				Map<String, Mixed> ret = evaluate_helper(event);
				ret.put("state", new CString(event.getState().name(), t));
				ret.put("hook", new CInt(event.getHook().getEntityId(), t));
				ret.put("xp", new CInt(event.getExpToDrop(), t));
				CInt caught = null;
				if (event.getCaught() instanceof MCEntity) {
					caught = new CInt(event.getCaught().getEntityId(), t);
				}
				ret.put("caught", caught);
				ret.put("chance", new CDouble(event.getHook().getBiteChance(), t));
				return ret;
			} else {
				throw new EventException("Could not convert to MCPlayerFishEvent");
			}
		}
	
		public boolean modifyEvent(String key, Mixed value, BindableEvent event, Target t) {
			if (event instanceof MCPlayerFishEvent) {
				MCPlayerFishEvent e = (MCPlayerFishEvent) event;
				if (key.equals("chance")) {
					double chance = value.primitive(t).castToDouble(t);
					if (chance > 1.0 || chance < 0.0) {
						throw new Exceptions.FormatException("Chance must be between 0.0 and 1.0", Target.UNKNOWN);
					}
					e.getHook().setBiteChance(chance);
					return true;
				}
				if (key.equals("xp")) {
					e.setExpToDrop(value.primitive(t).castToInt32(t));
					return true;
				}
			}
			return false;
		}
	
		public Driver driver() {
			return Driver.PLAYER_FISH;
		}
	
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}
}
