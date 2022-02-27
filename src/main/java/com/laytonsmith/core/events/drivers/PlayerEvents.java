package com.laytonsmith.core.events.drivers;

import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.abstraction.MCBookMeta;
import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.MCWorld;
import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.abstraction.blocks.MCBlockFace;
import com.laytonsmith.abstraction.blocks.MCMaterial;
import com.laytonsmith.abstraction.entities.MCFishHook;
import com.laytonsmith.abstraction.enums.MCAction;
import com.laytonsmith.abstraction.enums.MCEnterBedResult;
import com.laytonsmith.abstraction.enums.MCEquipmentSlot;
import com.laytonsmith.abstraction.enums.MCFishingState;
import com.laytonsmith.abstraction.enums.MCGameMode;
import com.laytonsmith.abstraction.enums.MCResourcePackStatus;
import com.laytonsmith.abstraction.enums.MCTeleportCause;
import com.laytonsmith.abstraction.events.MCExpChangeEvent;
import com.laytonsmith.abstraction.events.MCFoodLevelChangeEvent;
import com.laytonsmith.abstraction.events.MCGamemodeChangeEvent;
import com.laytonsmith.abstraction.events.MCPlayerChatEvent;
import com.laytonsmith.abstraction.events.MCPlayerCommandEvent;
import com.laytonsmith.abstraction.events.MCPlayerDeathEvent;
import com.laytonsmith.abstraction.events.MCPlayerEditBookEvent;
import com.laytonsmith.abstraction.events.MCPlayerEnterBedEvent;
import com.laytonsmith.abstraction.events.MCPlayerFishEvent;
import com.laytonsmith.abstraction.events.MCPlayerInteractEvent;
import com.laytonsmith.abstraction.events.MCPlayerItemConsumeEvent;
import com.laytonsmith.abstraction.events.MCPlayerJoinEvent;
import com.laytonsmith.abstraction.events.MCPlayerKickEvent;
import com.laytonsmith.abstraction.events.MCPlayerLeaveBedEvent;
import com.laytonsmith.abstraction.events.MCPlayerLoginEvent;
import com.laytonsmith.abstraction.events.MCPlayerMoveEvent;
import com.laytonsmith.abstraction.events.MCPlayerPortalEvent;
import com.laytonsmith.abstraction.events.MCPlayerQuitEvent;
import com.laytonsmith.abstraction.events.MCPlayerResourcePackEvent;
import com.laytonsmith.abstraction.events.MCPlayerRespawnEvent;
import com.laytonsmith.abstraction.events.MCPlayerTeleportEvent;
import com.laytonsmith.abstraction.events.MCPlayerToggleFlightEvent;
import com.laytonsmith.abstraction.events.MCPlayerToggleSneakEvent;
import com.laytonsmith.abstraction.events.MCPlayerToggleSprintEvent;
import com.laytonsmith.abstraction.events.MCWorldChangedEvent;
import com.laytonsmith.annotations.api;
import com.laytonsmith.annotations.hide;
import com.laytonsmith.commandhelper.CommandHelperPlugin;
import com.laytonsmith.core.ArgumentValidation;
import com.laytonsmith.core.MSLog;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.ObjectGenerator;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CBoolean;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.constructs.CNull;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.constructs.generics.GenericParameters;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.events.AbstractEvent;
import com.laytonsmith.core.events.BindableEvent;
import com.laytonsmith.core.events.BoundEvent;
import com.laytonsmith.core.events.BoundEvent.ActiveEvent;
import com.laytonsmith.core.events.Driver;
import com.laytonsmith.core.events.EventBuilder;
import com.laytonsmith.core.events.Prefilters;
import com.laytonsmith.core.events.Prefilters.PrefilterType;
import com.laytonsmith.core.events.drivers.EntityEvents.entity_death;
import com.laytonsmith.core.exceptions.CRE.CREBindException;
import com.laytonsmith.core.exceptions.CRE.CRECastException;
import com.laytonsmith.core.exceptions.CRE.CREFormatException;
import com.laytonsmith.core.exceptions.CRE.CRENullPointerException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.exceptions.EventException;
import com.laytonsmith.core.exceptions.PrefilterNonMatchException;
import com.laytonsmith.core.functions.EventBinding.modify_event;
import com.laytonsmith.core.functions.StringHandling;
import com.laytonsmith.core.natives.interfaces.Mixed;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.IllegalFormatConversionException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UnknownFormatConversionException;

public class PlayerEvents {

	public static String docs() {
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
		public boolean matches(Map<String, Mixed> prefilter, BindableEvent e, Environment env) throws PrefilterNonMatchException {
			if(e instanceof MCFoodLevelChangeEvent event) {

				Prefilters.match(prefilter, "player", event.getEntity().getName(), PrefilterType.STRING_MATCH, env);

				return true;
			}

			return false;
		}

		@Override
		public BindableEvent convert(CArray manualObject, Target t, Environment env) {
			return null;
		}

		@Override
		public Map<String, Mixed> evaluate(BindableEvent e, Environment env) throws EventException {
			if(e instanceof MCFoodLevelChangeEvent event) {
				Map<String, Mixed> ret = evaluate_helper(e);

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
		public boolean modifyEvent(String key, Mixed value, BindableEvent event, Environment env) {
			if(event instanceof MCFoodLevelChangeEvent e) {

				if(key.equalsIgnoreCase("level")) {
					e.setFoodLevel(ArgumentValidation.getInt32(value, Target.UNKNOWN, env));
					return true;
				}
			}

			return false;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
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
			return "{itemname: <string match>}"
					+ " Fires as a player is finishing eating/drinking an item."
					+ " Cancelling the event will cause any effects to not be"
					+ " applied and the item to not be taken from the player."
					+ " {player: the player consuming | item: the item being consumed}"
					+ " {item: A different item to be consumed, changing this will"
					+ " cause the original item to remain in the inventory}"
					+ " {player|item}";
		}

		@Override
		@SuppressWarnings("deprecation")
		public void bind(BoundEvent event) {
			// handle deprecated prefilter
			Map<String, Mixed> prefilter = event.getPrefilter();
			if(prefilter.containsKey("item")) {
				MSLog.GetLogger().w(MSLog.Tags.DEPRECATION, "The \"item\" prefilter in " + getName()
						+ " is deprecated for \"itemname\".", event.getTarget());
				MCItemStack is = Static.ParseItemNotation(null, prefilter.get("item").val(), 1, event.getTarget());
				prefilter.put("itemname", new CString(is.getType().getName(), event.getTarget()));
			}
		}

		@Override
		public boolean matches(Map<String, Mixed> prefilter, BindableEvent e, Environment env) throws PrefilterNonMatchException {
			if(e instanceof MCPlayerItemConsumeEvent event) {
				Prefilters.match(prefilter, "itemname", event.getItem().getType().getName(),
						PrefilterType.STRING_MATCH, env);
				return true;
			}
			return false;
		}

		@Override
		public BindableEvent convert(CArray manualObject, Target t, Environment env) {
			MCPlayer p = Static.GetPlayer(manualObject.get("player", Target.UNKNOWN, env), Target.UNKNOWN, env);
			MCItemStack i = ObjectGenerator.GetGenerator().item(manualObject.get("item", Target.UNKNOWN, env),
					Target.UNKNOWN, env);
			return EventBuilder.instantiate(MCPlayerItemConsumeEvent.class, p, i);
		}

		@Override
		public Map<String, Mixed> evaluate(BindableEvent event, Environment env) throws EventException {
			if(event instanceof MCPlayerItemConsumeEvent e) {
				Map<String, Mixed> ret = evaluate_helper(e);
				Mixed item = ObjectGenerator.GetGenerator().item(e.getItem(), Target.UNKNOWN, env);
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
		public boolean modifyEvent(String key, Mixed value, BindableEvent event, Environment env) {
			if(event instanceof MCPlayerItemConsumeEvent e) {
				if(key.equalsIgnoreCase("item")) {
					e.setItem(ObjectGenerator.GetGenerator().item(value, Target.UNKNOWN, env));
					return true;
				}
			}
			return false;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
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
		public boolean matches(Map<String, Mixed> prefilter, BindableEvent e, Environment env) throws PrefilterNonMatchException {
			if(e instanceof MCPlayerKickEvent) {
				Prefilters.match(prefilter, "player", ((MCPlayerKickEvent) e).getPlayer().getName(),
						PrefilterType.MACRO, env);
				Prefilters.match(prefilter, "reason", ((MCPlayerKickEvent) e).getReason(), PrefilterType.MACRO, env);
				return true;
			}
			return false;
		}

		@Override
		public BindableEvent convert(CArray manualObject, Target t, Environment env) {
			return null;
		}

		@Override
		public Map<String, Mixed> evaluate(BindableEvent e, Environment env) throws EventException {
			if(e instanceof MCPlayerKickEvent event) {
				Map<String, Mixed> map = evaluate_helper(e);
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
		public boolean modifyEvent(String key, Mixed value,
				BindableEvent event, Environment env) {
			if(event instanceof MCPlayerKickEvent e) {
				if(key.equalsIgnoreCase("message")) {
					e.setMessage(Construct.nval(value));
					return true;
				}
				if(key.equalsIgnoreCase("reason")) {
					e.setReason(Construct.nval(value));
					return true;
				}
			}
			return false;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
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
			return "{player: <string match> The player that teleport. Switching worlds will trigger this event, but"
					+ " world_changed is called after, only if this isn't cancelled first. | type: <string match>"
					+ "| from: <location match> This should be a location array (x, y, z, world)."
					+ "| to: <location match> The location the player is now in. This should be a location array as"
					+ " well.} "
					+ "{player | from: The location the player is coming from | to: The location the player is now in |"
					+ " type: the type of teleport occuring, one of "
					+ StringUtils.Join(MCTeleportCause.values(), ", ") + "}"
					+ "{to}"
					+ "{}";
		}

		@Override
		public boolean matches(Map<String, Mixed> prefilter, BindableEvent e, Environment env) throws PrefilterNonMatchException {
			if(e instanceof MCPlayerTeleportEvent event) {

				if(prefilter.containsKey("player")) {
					if(!(prefilter.get("player").toString().equalsIgnoreCase(event.getPlayer().getName()))) {
						return false;
					}
				}

				if(prefilter.containsKey("type")) {
					if(!(prefilter.get("type").toString().equalsIgnoreCase(event.getCause().toString()))) {
						return false;
					}
				}

				Prefilters.match(prefilter, "from", event.getFrom(), PrefilterType.LOCATION_MATCH, env);
				Prefilters.match(prefilter, "to", event.getTo(), PrefilterType.LOCATION_MATCH, env);

				return true;

			}

			return false;
		}

		@Override
		public BindableEvent convert(CArray manualObject, Target t, Environment env) {
			MCPlayer p = Static.GetPlayer(manualObject.get("player", Target.UNKNOWN, env), Target.UNKNOWN, env);
			MCLocation from = ObjectGenerator.GetGenerator().location(manualObject.get("from", Target.UNKNOWN, env),
					p.getWorld(), manualObject.getTarget(), env);
			MCLocation to = ObjectGenerator.GetGenerator().location(manualObject.get("to", Target.UNKNOWN, env),
					p.getWorld(), manualObject.getTarget(), env);
			return EventBuilder.instantiate(MCPlayerTeleportEvent.class, p, from, to);
		}

		@Override
		public Map<String, Mixed> evaluate(BindableEvent e, Environment env) throws EventException {
			if(e instanceof MCPlayerTeleportEvent event) {
				Map<String, Mixed> map = evaluate_helper(e);

				//Fill in the event parameters
				map.put("player", new CString(event.getPlayer().getName(), Target.UNKNOWN));
				map.put("from", ObjectGenerator.GetGenerator().location(event.getFrom(), env));
				map.put("to", ObjectGenerator.GetGenerator().location(event.getTo(), env));
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
		public boolean modifyEvent(String key, Mixed value, BindableEvent event, Environment env) {
			if(event instanceof MCPlayerTeleportEvent e) {

				if(key.equalsIgnoreCase("to")) {
					MCLocation loc = ObjectGenerator.GetGenerator().location(value, null, Target.UNKNOWN, env);
					e.setTo(loc);

					return true;
				}
			}

			return false;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
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
			return "{player: <macro> | from: <location match> An exact location array where the player is coming from."
					+ " | to: <location match> An exact location array where the player is going to."
					+ " | type: <macro> The type of portal occurring, either NETHER_PORTAL or END_PORTAL}"
					+ "Fired when a player collides with portal."
					+ "{player: The player that teleport | from: The location the player is coming from"
					+ " | to: The location the player is coming to. Returns null when using nether portal and"
					+ " \"allow-nether\" in server.properties is set to false or when using end portal and"
					+ " \"allow-end\" in bukkit.yml is set to false."
					+ " | type: the type of portal occurring | creationallowed: If a new portal can be created."
					+ " | creationradius: Gets the maximum radius from the given location to create a portal."
					+ " | searchradius: Gets the search radius for finding an available portal.}"
					+ "{to | creationradius | searchradius | creationallowed}"
					+ "{}";
		}

		@Override
		public boolean matches(Map<String, Mixed> prefilter, BindableEvent e, Environment env) throws PrefilterNonMatchException {
			if(e instanceof MCPlayerPortalEvent event) {
				Prefilters.match(prefilter, "player", event.getPlayer().getName(), PrefilterType.MACRO, env);
				Prefilters.match(prefilter, "type", event.getCause().toString(), PrefilterType.MACRO, env);
				Prefilters.match(prefilter, "from", event.getFrom(), PrefilterType.LOCATION_MATCH, env);
				if(event.getTo() != null) {
					Prefilters.match(prefilter, "to", event.getTo(), PrefilterType.LOCATION_MATCH, env);
				} else {
					Prefilters.match(prefilter, "to", CNull.NULL, PrefilterType.MACRO, env);
				}
				return true;

			}

			return false;
		}

		@Override
		public BindableEvent convert(CArray manualObject, Target t, Environment env) {
			MCPlayer p = Static.GetPlayer(manualObject.get("player", Target.UNKNOWN, env), Target.UNKNOWN, env);
			MCLocation from = ObjectGenerator.GetGenerator().location(manualObject.get("from", Target.UNKNOWN, env),
					p.getWorld(), manualObject.getTarget(), env);
			MCLocation to = ObjectGenerator.GetGenerator().location(manualObject.get("to", Target.UNKNOWN, env),
					p.getWorld(), manualObject.getTarget(), env);
			return EventBuilder.instantiate(MCPlayerPortalEvent.class, p, from, to);
		}

		@Override
		public Map<String, Mixed> evaluate(BindableEvent e, Environment env) throws EventException {
			if(e instanceof MCPlayerPortalEvent event) {
				Map<String, Mixed> map = evaluate_helper(e);
				map.put("player", new CString(event.getPlayer().getName(), Target.UNKNOWN));
				map.put("from", ObjectGenerator.GetGenerator().location(event.getFrom(), env));
				if(event.getTo() == null) {
					map.put("to", CNull.NULL);
				} else {
					map.put("to", ObjectGenerator.GetGenerator().location(event.getTo(), env));
				}
				map.put("type", new CString(event.getCause().toString(), Target.UNKNOWN));
				map.put("creationallowed", CBoolean.get(event.canCreatePortal()));
				map.put("creationradius", new CInt(event.getCreationRadius(), Target.UNKNOWN));
				map.put("searchradius", new CInt(event.getSearchRadius(), Target.UNKNOWN));
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
		public boolean modifyEvent(String key, Mixed value, BindableEvent event, Environment env) {
			if(event instanceof MCPlayerPortalEvent e) {

				if(key.equalsIgnoreCase("to")) {
					MCLocation loc = ObjectGenerator.GetGenerator().location(value, null, value.getTarget(), env);
					e.setTo(loc);
					return true;
				}

				if(key.equalsIgnoreCase("creationallowed")) {
					e.setCanCreatePortal(ArgumentValidation.getBooleanObject(value, value.getTarget(), env));
					return true;
				}

				if(key.equalsIgnoreCase("creationradius")) {
					e.setCreationRadius(ArgumentValidation.getInt32(value, value.getTarget(), env));
					return true;
				}

				if(key.equalsIgnoreCase("searchradius")) {
					e.setSearchRadius(ArgumentValidation.getInt32(value, value.getTarget(), env));
					return true;
				}
			}

			return false;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
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
		public boolean matches(Map<String, Mixed> prefilter, BindableEvent e, Environment env) throws PrefilterNonMatchException {
			if(e instanceof MCPlayerLoginEvent event) {
				if(prefilter.containsKey("player")) {
					return event.getName().equals(prefilter.get("player").val());
				}
				return true;
			}
			return false;
		}

		@Override
		public BindableEvent convert(CArray manualObject, Target t, Environment env) {
			return null;
		}

		@Override
		public Map<String, Mixed> evaluate(BindableEvent e, Environment env) throws EventException {
			if(e instanceof MCPlayerLoginEvent event) {
				Map<String, Mixed> map = evaluate_helper(e);

				map.put("player", new CString(event.getName(), Target.UNKNOWN));
				map.put("uuid", new CString(event.getUniqueId(), Target.UNKNOWN));
				map.put("ip", new CString(event.getIP(), Target.UNKNOWN));
				//TODO: The event.getResult needs to be enum'd
				map.put("result", new CString(event.getResult(), Target.UNKNOWN));
				map.put("kickmsg", new CString(event.getKickMessage(), Target.UNKNOWN));
				map.put("hostname", new CString(event.getHostname(), Target.UNKNOWN));

				return map;
			} else {
				throw new EventException("Cannot convert e to PlayerLoginEvent");
			}
		}

		@Override
		public Driver driver() {
			return Driver.PLAYER_LOGIN;
		}

		@Override
		public boolean modifyEvent(String key, Mixed value, BindableEvent e, Environment env) {
			if(e instanceof MCPlayerLoginEvent event) {
				if(key.equals("result")) {
					String[] possible = new String[]{"ALLOWED", "KICK_WHITELIST",
						"KICK_BANNED", "KICK_FULL", "KICK_OTHER"};
					if(Arrays.asList(possible).contains(value.val().toUpperCase())) {
						event.setResult(value.val().toUpperCase());
					}
				} else if(key.equals("kickmsg")) {
					event.setKickMessage(value.val());
				}
			}

			return false;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public void preExecution(Environment env, ActiveEvent activeEvent) {
			if(activeEvent.getUnderlyingEvent() instanceof MCPlayerLoginEvent) {
				//Static lookups of the player don't seem to work here, but
				//the player is passed in with the event.
				MCPlayer player = ((MCPlayerLoginEvent) activeEvent.getUnderlyingEvent()).getPlayer();
				Static.InjectPlayer(player);
			}
		}

		@Override
		public void postExecution(Environment env, ActiveEvent activeEvent) {
			if(activeEvent.getUnderlyingEvent() instanceof MCPlayerLoginEvent) {
				MCPlayer player = ((MCPlayerLoginEvent) activeEvent.getUnderlyingEvent()).getPlayer();
				Static.UninjectPlayer(player);
			}
		}

	}

	@api
	public static class player_join extends AbstractEvent {

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
					+ "{player: The player's name | world | join_message: The default join message |"
					+ " first_login: True if this is the first time"
					+ " the player has logged in.}"
					+ "{join_message}"
					+ "{player|world|join_message}";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_0;
		}

		@Override
		public Driver driver() {
			return Driver.PLAYER_JOIN;
		}

		@Override
		public boolean matches(Map<String, Mixed> prefilter, BindableEvent e, Environment env) throws PrefilterNonMatchException {
			if(e instanceof MCPlayerJoinEvent ple) {
				if(prefilter.containsKey("player")) {
					if(!ple.getPlayer().getName().equals(prefilter.get("player").val())) {
						return false;
					}
				}
				Prefilters.match(prefilter, "join_message", ple.getJoinMessage(), Prefilters.PrefilterType.REGEX, env);
				Prefilters.match(prefilter, "world", ple.getPlayer().getWorld().getName(), PrefilterType.STRING_MATCH, env);
				return true;
			}
			return false;
		}

		@Override
		public Map<String, Mixed> evaluate(BindableEvent e, Environment env) throws EventException {
			if(e instanceof MCPlayerJoinEvent ple) {
				Map<String, Mixed> map = evaluate_helper(e);
				map.put("world", new CString(ple.getPlayer().getWorld().getName(), Target.UNKNOWN));
				map.put("join_message", new CString(ple.getJoinMessage(), Target.UNKNOWN));
				map.put("first_login", CBoolean.get(ple.getPlayer().isNewPlayer()));
				return map;
			} else {
				throw new EventException("Cannot convert e to PlayerLoginEvent");
			}
		}

		@Override
		public boolean modifyEvent(String key, Mixed value, BindableEvent event, Environment env) {
			if(event instanceof MCPlayerJoinEvent pje) {
				if(key.equals("join_message") || key.equals("message")) {
					if(value instanceof CNull) {
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
		public BindableEvent convert(CArray manual, Target t, Environment env) {
			return EventBuilder.<MCPlayerJoinEvent>instantiate(MCPlayerJoinEvent.class,
					Static.GetPlayer(manual.get("player", Target.UNKNOWN, env).val(), Target.UNKNOWN, env),
					manual.get("join_message", Target.UNKNOWN, env).val());
		}

		@Override
		public void preExecution(Environment env, ActiveEvent activeEvent) {
			if(activeEvent.getUnderlyingEvent() instanceof MCPlayerJoinEvent) {
				//Static lookups of the player as entity don't seem to work here, but
				//the player is passed in with the event.
				MCPlayer player = ((MCPlayerJoinEvent) activeEvent.getUnderlyingEvent()).getPlayer();
				Static.InjectEntity(player);
			}
		}

		@Override
		public void postExecution(Environment env, ActiveEvent activeEvent) {
			if(activeEvent.getUnderlyingEvent() instanceof MCPlayerJoinEvent) {
				MCPlayer player = ((MCPlayerJoinEvent) activeEvent.getUnderlyingEvent()).getPlayer();
				Static.UninjectEntity(player);
			}
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
			return "{block: <string match> The block type the player interacts with, or null if nothing"
					+ " | button: <string match> left or right. If they left or right clicked |"
					+ " itemname: <string match> The item type they are holding when they interacted, or null |"
					+ " hand: <string match> The hand the player clicked with |"
					+ " player: <macro> The player that triggered the event} "
					+ "Fires when a player left or right clicks a block or the air. Note that this event may fire for"
					+ " the main hand, off hand, or twice, one for each hand, depending on the item priority and what"
					+ " is clicked. If you don't want multiple events, you can prefilter on hand. If you want to remove"
					+ " the item that is being used, you must also cancel the event."
					+ "{action: One of either left_click_block, right_click_block, left_click_air, or right_click_air."
					+ " If left or right_click_air, neither facing nor location will be present."
					+ " | block: The type of block they clicked, or null if clicked air or if the block is now empty."
					+ " | item: The item array the player used to click, or null if not holding anything in that hand"
					+ " | player: The player associated with this event"
					+ " | facing: The (lowercase) face of the block they clicked. (One of "
					+ StringUtils.Join(MCBlockFace.values(), ", ", ", or ") + ") |"
					+ "location: The (x, y, z, world) location of the block they clicked |"
					+ "hand: The hand used to click with, can be either main_hand or off_hand}"
					+ "{}"
					+ "{player|action|item|location|facing}";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_0;
		}

		@Override
		public Driver driver() {
			return Driver.PLAYER_INTERACT;
		}

		@Override
		@SuppressWarnings("deprecation")
		public void bind(BoundEvent event) {
			// handle deprecated prefilters
			Map<String, Mixed> prefilter = event.getPrefilter();
			if(prefilter.containsKey("item")) {
				MSLog.GetLogger().w(MSLog.Tags.DEPRECATION, "The \"item\" prefilter in " + getName()
						+ " is deprecated for \"itemname\".", event.getTarget());
				MCItemStack is = Static.ParseItemNotation(null, prefilter.get("item").val(), 1, event.getTarget());
				prefilter.put("itemname", new CString(is.getType().getName(), event.getTarget()));
			}
			if(prefilter.containsKey("block")) {
				Mixed ctype = prefilter.get("block");
				if(ctype.isInstanceOf(CString.TYPE, null, event.getEnvironment())
						&& ctype.val().contains(":")
						|| ArgumentValidation.isNumber(ctype, event.getEnvironment())) {
					int type;
					String notation = ctype.val();
					int separatorIndex = notation.indexOf(':');
					if(separatorIndex != -1) {
						type = Integer.parseInt(notation.substring(0, separatorIndex));
					} else {
						type = Integer.parseInt(notation);
					}
					MCMaterial mat = StaticLayer.GetMaterialFromLegacy(type, 0);
					if(mat == null) {
						throw new CREBindException("Invalid material '" + notation + "'", event.getTarget());
					}
					prefilter.put("block", new CString(mat.getName(), event.getTarget()));
					MSLog.GetLogger().w(MSLog.Tags.DEPRECATION, "The notation format in the \"block\" prefilter in "
							+ getName() + " is deprecated. Converted to " + mat.getName(), event.getTarget());
				}
			}
		}

		@Override
		public boolean matches(Map<String, Mixed> prefilter, BindableEvent e, Environment env) throws PrefilterNonMatchException {
			if(e instanceof MCPlayerInteractEvent pie) {

				if(prefilter.containsKey("button")) {
					if(pie.getAction().equals(MCAction.LEFT_CLICK_AIR)
							|| pie.getAction().equals(MCAction.LEFT_CLICK_BLOCK)) {
						if(!prefilter.get("button").val().equalsIgnoreCase("left")) {
							return false;
						}
					}
					if(pie.getAction().equals(MCAction.RIGHT_CLICK_AIR)
							|| pie.getAction().equals(MCAction.RIGHT_CLICK_BLOCK)) {
						if(!prefilter.get("button").val().equalsIgnoreCase("right")) {
							return false;
						}
					}
				}

				if(prefilter.containsKey("itemname")) {
					Mixed item = prefilter.get("itemname");
					MCMaterial mat = pie.getItem().getType();
					if(mat == null) {
						if(!(item instanceof CNull)) {
							return false;
						}
					} else if(!mat.getName().equals(item.val())) {
						return false;
					}
				}
				if(prefilter.containsKey("block")) {
					Mixed block = prefilter.get("block");
					MCBlock b = pie.getClickedBlock();
					if(b.isEmpty()) {
						if(!(block instanceof CNull)) {
							return false;
						}
					} else if(!b.getType().getName().equals(block.val())) {
						return false;
					}
				}
				Prefilters.match(prefilter, "player", pie.getPlayer().getName(), PrefilterType.MACRO, env);

				if(pie.getHand() == MCEquipmentSlot.WEAPON) {
					Prefilters.match(prefilter, "hand", "main_hand", PrefilterType.STRING_MATCH, env);
				} else {
					Prefilters.match(prefilter, "hand", "off_hand", PrefilterType.STRING_MATCH, env);
				}

				return true;
			}
			return false;
		}

		@Override
		public Map<String, Mixed> evaluate(BindableEvent e, Environment env) throws EventException {
			if(e instanceof MCPlayerInteractEvent pie) {
				Map<String, Mixed> map = evaluate_helper(e);
				MCAction a = pie.getAction();
				map.put("action", new CString(a.name().toLowerCase(), Target.UNKNOWN));
				MCBlock block = pie.getClickedBlock();
				map.put("block", block.isEmpty() ? CNull.NULL : new CString(block.getType().getName(), Target.UNKNOWN));
				if(a == MCAction.LEFT_CLICK_AIR || a == MCAction.LEFT_CLICK_BLOCK) {
					map.put("button", new CString("left", Target.UNKNOWN));
				} else {
					map.put("button", new CString("right", Target.UNKNOWN));
				}
				if(a == MCAction.LEFT_CLICK_BLOCK || a == MCAction.RIGHT_CLICK_BLOCK) {
					map.put("facing", new CString(pie.getBlockFace().name().toLowerCase(), Target.UNKNOWN));
					map.put("location", ObjectGenerator.GetGenerator().location(pie.getClickedBlock().getLocation(),
							false, env));
				}
				map.put("world", new CString(pie.getPlayer().getWorld().getName(), Target.UNKNOWN));
				map.put("item", ObjectGenerator.GetGenerator().item(pie.getItem(), Target.UNKNOWN, env));
				if(pie.getHand() == MCEquipmentSlot.WEAPON) {
					map.put("hand", new CString("main_hand", Target.UNKNOWN));
				} else {
					map.put("hand", new CString("off_hand", Target.UNKNOWN));
				}

				return map;
			} else {
				throw new EventException("Cannot convert e to PlayerInteractEvent");
			}
		}

		@Override
		public BindableEvent convert(CArray manual, Target t, Environment env) {
			MCPlayer p = Static.GetPlayer(manual.get("player", Target.UNKNOWN, env), Target.UNKNOWN, env);
			MCAction a = MCAction.valueOf(manual.get("action", Target.UNKNOWN, env).val().toUpperCase());
			MCItemStack is = Static.ParseItemNotation("player_interact event", manual.get("item", Target.UNKNOWN, env).val(),
					1, Target.UNKNOWN);
			MCBlock b = ObjectGenerator.GetGenerator().location(manual.get("location", Target.UNKNOWN, env), null,
					Target.UNKNOWN, env).getBlock();
			MCBlockFace bf = MCBlockFace.valueOf(manual.get("facing", Target.UNKNOWN, env).val().toUpperCase());
			return EventBuilder.<MCPlayerInteractEvent>instantiate(MCPlayerInteractEvent.class, p, a, is, b, bf);
		}

		@Override
		public boolean modifyEvent(String key, Mixed value, BindableEvent event, Environment env) {
			return false;
		}

	}

	@api
	public static class player_enter_bed extends AbstractEvent {

		@Override
		public String docs() {
			return "{location: <location match> The location of the bed | result: <string match>} "
					+ "Fires when a player tries to enter a bed."
					+ "{location: The location of the bed |"
					+ " player: The player associated with this event |"
					+ " result: The outcome of this attempt to enter bed. Can be one of "
					+ StringUtils.Join(MCEnterBedResult.values(), ", ", ", or ") + "}"
					+ "{}"
					+ "{location|player}";
		}

		@Override
		public String getName() {
			return "player_enter_bed";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public boolean matches(Map<String, Mixed> prefilter, BindableEvent e, Environment env) throws PrefilterNonMatchException {
			if(!(e instanceof MCPlayerEnterBedEvent be)) {
				return false;
			}

			if(prefilter.containsKey("location")) {
				MCLocation loc = ObjectGenerator.GetGenerator().location(prefilter.get("location"), null,
						Target.UNKNOWN, env);

				if(!be.getBed().getLocation().equals(loc)) {
					return false;
				}
			}
			if(prefilter.containsKey("result")) {
				return prefilter.get("result").val().equals(be.getResult().name());
			}

			return true;
		}

		@Override
		public Map<String, Mixed> evaluate(BindableEvent e, Environment env) throws EventException {
			if(e instanceof MCPlayerEnterBedEvent bee) {
				Map<String, Mixed> map = evaluate_helper(e);
				map.put("location", ObjectGenerator.GetGenerator().location(bee.getBed().getLocation(), false, env));
				map.put("result", new CString(bee.getResult().name(), Target.UNKNOWN));
				return map;
			} else {
				throw new EventException("Cannot convert e to an appropriate PlayerEnterBedEvent.");
			}
		}

		@Override
		public Driver driver() {
			return Driver.PLAYER_ENTER_BED;
		}

		@Override
		public boolean modifyEvent(String key, Mixed value, BindableEvent event, Environment env) {
			return false;
		}

		@Override
		public BindableEvent convert(CArray manual, Target t, Environment env) {
			MCPlayer p = Static.GetPlayer(manual.get("player", Target.UNKNOWN, env), Target.UNKNOWN, env);
			MCBlock b = ObjectGenerator.GetGenerator().location(manual.get("location", Target.UNKNOWN, env),
					null, Target.UNKNOWN, env).getBlock();
			MCEnterBedResult r = MCEnterBedResult.valueOf(manual.get("result", Target.UNKNOWN, env).val());
			return EventBuilder.<MCPlayerEnterBedEvent>instantiate(MCPlayerEnterBedEvent.class, p, b, r);
		}
	}

	@api
	public static class player_leave_bed extends AbstractEvent {

		@Override
		public String docs() {
			return "{location: <location match> The location of the bed} "
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

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public boolean matches(Map<String, Mixed> prefilter, BindableEvent e, Environment env) throws PrefilterNonMatchException {
			if(!(e instanceof MCPlayerLeaveBedEvent be)) {
				return false;
			}

			if(prefilter.containsKey("location")) {
				MCLocation loc = ObjectGenerator.GetGenerator().location(prefilter.get("location"), null,
						Target.UNKNOWN, env);

				return be.getBed().getLocation().equals(loc);
			}

			return true;
		}

		@Override
		public Map<String, Mixed> evaluate(BindableEvent e, Environment env) throws EventException {
			if(e instanceof MCPlayerLeaveBedEvent bee) {
				Map<String, Mixed> map = evaluate_helper(e);
				map.put("location", ObjectGenerator.GetGenerator().location(bee.getBed().getLocation(), false, env));
				return map;
			} else {
				throw new EventException("Cannot convert e to an appropriate PlayerBedEvent.");
			}
		}

		@Override
		public Driver driver() {
			return Driver.PLAYER_LEAVE_BED;
		}

		@Override
		public boolean modifyEvent(String key, Mixed value, BindableEvent event, Environment env) {
			return false;
		}

		@Override
		public BindableEvent convert(CArray manual, Target t, Environment env) {
			MCPlayer p = Static.GetPlayer(manual.get("player", Target.UNKNOWN, env), Target.UNKNOWN, env);
			MCBlock b = ObjectGenerator.GetGenerator().location(manual.get("location", Target.UNKNOWN, env),
					null, Target.UNKNOWN, env).getBlock();
			return EventBuilder.<MCPlayerEnterBedEvent>instantiate(MCPlayerEnterBedEvent.class, p, b);
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
			return "{location: <location match> | player: <macro>} "
					+ "Fires when a player steps on a pressure plate or other interactable block."
					+ "{location: The location of the block | activated: (deprecated)"
					+ " | player: The player associated with this event}"
					+ "{}"
					+ "{}";
		}

		@Override
		public boolean matches(Map<String, Mixed> prefilter, BindableEvent e, Environment env) throws PrefilterNonMatchException {
			if(e instanceof MCPlayerInteractEvent pie) {
				Prefilters.match(prefilter, "player", pie.getPlayer().getName(), PrefilterType.MACRO, env);
				Prefilters.match(prefilter, "location", pie.getClickedBlock().getLocation(),
						PrefilterType.LOCATION_MATCH, env);
				return true;
			}
			return false;
		}

		@Override
		public BindableEvent convert(CArray manual, Target t, Environment env) {
			MCPlayer p = Static.GetPlayer(manual.get("player", Target.UNKNOWN, env), Target.UNKNOWN, env);
			MCBlock b = ObjectGenerator.GetGenerator().location(manual.get("location", Target.UNKNOWN, env), null,
					Target.UNKNOWN, env).getBlock();
			return EventBuilder.<MCPlayerInteractEvent>instantiate(MCPlayerInteractEvent.class, p, MCAction.PHYSICAL, null,
					b, MCBlockFace.UP);
		}

		@Override
		public Map<String, Mixed> evaluate(BindableEvent e, Environment env) throws EventException {
			if(e instanceof MCPlayerInteractEvent pie) {
				Map<String, Mixed> map = evaluate_helper(e);
				map.put("location", ObjectGenerator.GetGenerator().location(pie.getClickedBlock().getLocation(),
						false, env));
				map.put("activated", CBoolean.TRUE); // was never used, but was documented; remove in 3.3.5
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
		public boolean modifyEvent(String key, Mixed value, BindableEvent event, Environment env) {
			return false;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
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
					+ "Fires when a player respawns. Technically during this time, the player may not be considered"
					+ " 'online'. This can cause problems if you try to run an external command with run() or"
					+ " something. CommandHelper takes into account the fact that the player is offline, and works"
					+ " around this, so all functions should respond correctly as if the player was online,"
					+ " however other plugins or plain text commands that are run may not."
					+ "{player: The player that is respawning | "
					+ "location: The location they are going to respawn at | "
					+ "bed_spawn: True if the respawn location is the player's bed"
					+ "anchor_spawn: True if the respawn location is the player's respawn anchor}"
					+ "{location}"
					+ "{}";
		}

		@Override
		public Driver driver() {
			return Driver.PLAYER_SPAWN;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_0;
		}

		@Override
		public boolean matches(Map<String, Mixed> prefilter, BindableEvent e, Environment env) throws PrefilterNonMatchException {
			if(e instanceof MCPlayerRespawnEvent event) {
				Prefilters.match(prefilter, "player", event.getPlayer().getName(), PrefilterType.MACRO, env);
				Prefilters.match(prefilter, "x", event.getRespawnLocation().getBlockX(), PrefilterType.EXPRESSION, env);
				Prefilters.match(prefilter, "y", event.getRespawnLocation().getBlockY(), PrefilterType.EXPRESSION, env);
				Prefilters.match(prefilter, "z", event.getRespawnLocation().getBlockZ(), PrefilterType.EXPRESSION, env);
				Prefilters.match(prefilter, "world", event.getRespawnLocation().getWorld().getName(),
						PrefilterType.STRING_MATCH, env);
				return true;
			}
			return false;
		}

		@Override
		public Map<String, Mixed> evaluate(BindableEvent e, Environment env) throws EventException {
			if(e instanceof MCPlayerRespawnEvent event) {
				Map<String, Mixed> map = evaluate_helper(e);
				//the helper puts the player in for us
				CArray location = ObjectGenerator.GetGenerator().location(event.getRespawnLocation(), env);
				map.put("location", location);
				map.put("bed_spawn", CBoolean.get(event.isBedSpawn()));
				map.put("anchor_spawn", CBoolean.get(event.isAnchorSpawn()));
				return map;
			} else {
				throw new EventException("Cannot convert e to PlayerRespawnEvent");
			}
		}

		@Override
		public BindableEvent convert(CArray manual, Target t, Environment env) {
			//For firing off the event manually, we have to convert the CArray into an
			//actual object that will trigger it
			MCPlayer p = Static.GetPlayer(manual.get("player", Target.UNKNOWN, env), Target.UNKNOWN, env);
			MCLocation l = ObjectGenerator.GetGenerator().location(manual.get("location", Target.UNKNOWN, env), p.getWorld(),
					Target.UNKNOWN, env);
			return EventBuilder.<MCPlayerRespawnEvent>instantiate(MCPlayerRespawnEvent.class, p, l, false);
		}

		@Override
		public boolean modifyEvent(String key, Mixed value, BindableEvent event, Environment env) {
			if(event instanceof MCPlayerRespawnEvent e) {
				if(key.equals("location")) {
					//Change this parameter in e to value
					e.setRespawnLocation(ObjectGenerator.GetGenerator().location(value, e.getPlayer().getWorld(),
							Target.UNKNOWN, env));
					return true;
				}
			}
			return false;
		}

		@Override
		public void preExecution(Environment env, ActiveEvent activeEvent) {
			if(activeEvent.getUnderlyingEvent() instanceof MCPlayerRespawnEvent) {
				//Static lookups of the player don't seem to work here, but
				//the player is passed in with the event.
				MCPlayer player = ((MCPlayerRespawnEvent) activeEvent.getUnderlyingEvent()).getPlayer();
				Static.InjectPlayer(player);
			}
		}

		@Override
		public void postExecution(Environment env, ActiveEvent activeEvent) {
			if(activeEvent.getUnderlyingEvent() instanceof MCPlayerRespawnEvent) {
				MCPlayer player = ((MCPlayerRespawnEvent) activeEvent.getUnderlyingEvent()).getPlayer();
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
					+ "{xp | drops: The items will be replaced by the given items | death_message | keep_inventory |"
					+ " keep_level | new_exp | new_level | new_total_exp}"
					+ "{}";
		}

		@Override
		public Driver driver() {
			return Driver.PLAYER_DEATH;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_0;
		}

		@Override
		public boolean matches(Map<String, Mixed> prefilter, BindableEvent e, Environment env) throws PrefilterNonMatchException {
			if(e instanceof MCPlayerDeathEvent) {
				MCPlayerDeathEvent event = (MCPlayerDeathEvent) e;
				Prefilters.match(prefilter, "player", ((MCPlayer) event.getEntity()).getName(), PrefilterType.MACRO, env);
				return true;
			}
			return false;
		}

		//We have an actual event now, change it into a Map
		//that will end up being the @event object
		@Override
		public Map<String, Mixed> evaluate(BindableEvent e, Environment env) throws EventException {
			if(e instanceof MCPlayerDeathEvent event) {
				Map<String, Mixed> map = super.evaluate(e, env);
				map.putAll(evaluate_helper(e));
				map.put("death_message", new CString(event.getDeathMessage(), Target.UNKNOWN));
				map.put("keep_inventory", CBoolean.get(event.getKeepInventory()));
				map.put("keep_level", CBoolean.get(event.getKeepLevel()));
				map.put("new_exp", new CInt(event.getNewExp(), Target.UNKNOWN));
				map.put("new_level", new CInt(event.getNewLevel(), Target.UNKNOWN));
				map.put("new_total_exp", new CInt(event.getNewTotalExp(), Target.UNKNOWN));
				if(event.getKiller() instanceof MCPlayer) {
					map.put("killer", new CString(((MCPlayer) event.getKiller()).getName(), Target.UNKNOWN));
				} else {
					map.put("killer", CNull.NULL);
				}
				return map;
			} else {
				throw new EventException("Cannot convert e to EntityDeathEvent");
			}
		}

		@Override
		public BindableEvent convert(CArray manual, Target t, Environment env) {
			//For firing off the event manually, we have to convert the CArray into an
			//actual object that will trigger it
			String splayer = manual.get("player", Target.UNKNOWN, env).val();
			List<MCItemStack> list = new ArrayList<>();
			String deathMessage = manual.get("death_message", Target.UNKNOWN, env).val();
			CArray clist = (CArray) manual.get("drops", Target.UNKNOWN, env);
			for(String key : clist.stringKeySet()) {
				list.add(ObjectGenerator.GetGenerator().item(clist.get(key, Target.UNKNOWN, env), clist.getTarget(), env));
			}
			return EventBuilder.<MCPlayerDeathEvent>instantiate(MCPlayerDeathEvent.class, Static.GetPlayer(splayer,
					Target.UNKNOWN, env), list,
					0, deathMessage);
		}

		//Given the paramters, change the underlying event
		@Override
		public boolean modifyEvent(String key, Mixed value, BindableEvent event, Environment env) {
			if(event instanceof MCPlayerDeathEvent e) {
				switch(key) {
					case "death_message":
						e.setDeathMessage(Construct.nval(value));
						return true;
					case "keep_inventory":
						e.setKeepInventory(ArgumentValidation.getBoolean(value, Target.UNKNOWN, env));
						return true;
					case "keep_level":
						e.setKeepLevel(ArgumentValidation.getBoolean(value, Target.UNKNOWN, env));
						return true;
					case "new_exp":
						e.setNewExp(ArgumentValidation.getInt32(value, Target.UNKNOWN, env));
						return true;
					case "new_level":
						e.setNewLevel(ArgumentValidation.getInt32(value, Target.UNKNOWN, env));
						return true;
					case "new_total_exp":
						e.setNewTotalExp(ArgumentValidation.getInt32(value, Target.UNKNOWN, env));
						return true;
					default:
						return super.modifyEvent(key, value, event, env);
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
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public boolean matches(Map<String, Mixed> prefilter, BindableEvent e, Environment env) throws PrefilterNonMatchException {
			if(e instanceof MCPlayerQuitEvent) {
				Prefilters.match(prefilter, "player", ((MCPlayerQuitEvent) e).getPlayer().getName(),
						PrefilterType.MACRO, env);
				return true;
			}
			return false;
		}

		@Override
		public BindableEvent convert(CArray manualObject, Target t, Environment env) {
			//Get the parameters from the manualObject
			MCPlayer player = Static.GetPlayer(manualObject.get("player", Target.UNKNOWN, env), Target.UNKNOWN, env);
			String message = Construct.nval(manualObject.get("message", Target.UNKNOWN, env));

			return EventBuilder.instantiate(MCPlayerCommandEvent.class,
					player, message);
		}

		@Override
		public Map<String, Mixed> evaluate(BindableEvent e, Environment env) throws EventException {
			if(e instanceof MCPlayerQuitEvent event) {
				Map<String, Mixed> map = evaluate_helper(e);
				//Fill in the event parameters
				map.put("message", new CString(event.getMessage(), Target.UNKNOWN));
				return map;
			} else {
				throw new EventException("Cannot convert e to MCPlayerQuitEvent");
			}
		}

		@Override
		public boolean modifyEvent(String key, Mixed value, BindableEvent event, Environment env) {
			if(event instanceof MCPlayerQuitEvent e) {
				if("message".equals(key)) {
					e.setMessage(Construct.nval(value));
				}
				return true;
			}
			return false;
		}

		@Override
		public void preExecution(Environment env, ActiveEvent activeEvent) {
			if(activeEvent.getUnderlyingEvent() instanceof MCPlayerQuitEvent) {
				//Static lookups of the player don't seem to work here, but
				//the player is passed in with the event.
				MCPlayer player = ((MCPlayerQuitEvent) activeEvent.getUnderlyingEvent()).getPlayer();
				Static.InjectPlayer(player);
			}
		}

		@Override
		public void postExecution(Environment env, ActiveEvent activeEvent) {
			if(activeEvent.getUnderlyingEvent() instanceof MCPlayerQuitEvent) {
				MCPlayer player = ((MCPlayerQuitEvent) activeEvent.getUnderlyingEvent()).getPlayer();
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
					+ " players that will receive the chat message. If a player doesn't exist"
					+ " or is offline, and is in the array, it is simply ignored, no"
					+ " exceptions will be thrown. | format: The \"printf\" format string, by "
					+ " default \"&lt;%1$s> %2$s\". The first parameter is the player's display"
					+ " name, and the second one is the message.}"
					+ "{player|message|format}";
		}

		@Override
		public Driver driver() {
			return Driver.PLAYER_CHAT;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_0;
		}

		@Override
		public boolean matches(Map<String, Mixed> prefilter, BindableEvent e, Environment env) throws PrefilterNonMatchException {
			if(e instanceof MCPlayerChatEvent) {
				//As a very special case, if this player is currently in interpreter mode, we do not want to
				//intercept their chat event
				if(CommandHelperPlugin.self.interpreterListener
						.isInInterpreterMode(((MCPlayerChatEvent) e).getPlayer().getName())) {
					throw new PrefilterNonMatchException();
				}
				Prefilters.match(prefilter, "player", ((MCPlayerChatEvent) e).getPlayer().getName(),
						PrefilterType.MACRO, env);
				return true;
			}
			return false;
		}

		@Override
		public BindableEvent convert(CArray manualObject, Target t, Environment env) {
			//Get the parameters from the manualObject
			MCPlayer player = Static.GetPlayer(manualObject.get("player", Target.UNKNOWN, env), Target.UNKNOWN, env);
			String message = Construct.nval(manualObject.get("message", Target.UNKNOWN, env));
			String format = Construct.nval(manualObject.get("format", Target.UNKNOWN, env));

			return EventBuilder.instantiate(MCPlayerChatEvent.class,
					player, message, format);
		}

		@Override
		public Map<String, Mixed> evaluate(BindableEvent e, Environment env) throws EventException {
			if(e instanceof MCPlayerChatEvent event) {
				Map<String, Mixed> map = evaluate_helper(e);
				//Fill in the event parameters
				map.put("message", new CString(event.getMessage(), Target.UNKNOWN));
				CArray ca = new CArray(Target.UNKNOWN, GenericParameters
						.addParameter(CString.TYPE, null).build(), env);
				for(MCPlayer recipient : event.getRecipients()) {
					ca.push(new CString(recipient.getName(), Target.UNKNOWN), Target.UNKNOWN, env);
				}
				map.put("format", new CString(event.getFormat(), Target.UNKNOWN));
				map.put("recipients", ca);
				return map;
			} else {
				throw new EventException("Cannot convert e to MCPlayerChatEvent");
			}
		}

		@Override
		public boolean modifyEvent(String key, Mixed value, BindableEvent event, Environment env) {
			if(event instanceof MCPlayerChatEvent e) {
				if("message".equals(key)) {
					e.setMessage(Construct.nval(value));
				}
				if("recipients".equals(key)) {
					if(value.isInstanceOf(CArray.TYPE, null, env)) {
						List<MCPlayer> list = new ArrayList<>();
						for(String index : ((CArray) value).stringKeySet()) {
							Mixed v = ((CArray) value).get(index, value.getTarget(), env);
							try {
								list.add(Static.GetPlayer(v, value.getTarget(), env));
							} catch (ConfigRuntimeException ex) {
								//Ignored
							}
						}
						e.setRecipients(list);
					} else {
						throw new CRECastException("recipients must be an array", value.getTarget());
					}
				}
				if("format".equals(key)) {
					String format = Construct.nval(value);
					if(format == null) {
						throw new CRENullPointerException("The \"format\" key in " + modify_event.NAME
								+ " for the " + this.getName()
								+ " event may not be null.", value.getTarget());
					}
					try {
						// Throws UnknownFormatConversionException, MissingFormatException,
						// IllegalFormatConversionException, FormatFlagsConversionMismatchException,
						// NullPointerException and possibly more.
						e.setFormat(format);
					} catch (Exception ex) {
						// Check the format to give a better exception message.
						if(format.replaceAll("%%", "").replaceAll("\\%\\%|\\%[12]\\$s", "").contains("%")) {
							throw new CREFormatException("The \"format\" key in " + modify_event.class.getSimpleName()
									+ " for the " + this.getName()
									+ " event only accepts %1$s and %2$s as format specifiers. Use a \"%%\" to display"
											+ " a single \"%\".", value.getTarget());
						} else {
							throw new CREFormatException("The \"format\" key in " + modify_event.class.getSimpleName()
									+ " for the " + this.getName()
									+ " event was set to an invalid value: " + format + ". The original exception"
											+ " message is: " + ex.getMessage(), value.getTarget());
						}
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
					+ "Fired when any player attempts to send a chat message. The event handler is run on the async"
					+ " thread, and not"
					+ " the main server thread, which can lead to undefined results if your code accesses"
					+ " non-threadsafe methods, hence"
					+ " why this feature is undocumented. If this event is cancelled, player_chat binds will not fire."
					+ "{message: The message to be sent | recipients | format}"
					+ "{message|recipients: An array of"
					+ " players that will receive the chat message. If a player doesn't exist"
					+ " or is offline, and is in the array, it is simply ignored, no"
					+ " exceptions will be thrown.|format: The \"printf\" format string, by "
					+ " default \"&lt;%1$s> %2$s\". The first parameter is the player's display"
					+ " name, and the second one is the message.}"
					+ "{}";
		}

		@Override
		public Driver driver() {
			return Driver.PLAYER_CHAT;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public boolean matches(Map<String, Mixed> prefilter, BindableEvent e, Environment env) throws PrefilterNonMatchException {
			if(e instanceof MCPlayerChatEvent) {
				//As a very special case, if this player is currently in interpreter mode, we do not want to
				//intercept their chat event
				if(CommandHelperPlugin.self.interpreterListener
						.isInInterpreterMode(((MCPlayerChatEvent) e).getPlayer().getName())) {
					throw new PrefilterNonMatchException();
				}
				Prefilters.match(prefilter, "player", ((MCPlayerChatEvent) e).getPlayer().getName(),
						PrefilterType.MACRO, env);
				return true;
			}
			return false;
		}

		@Override
		public BindableEvent convert(CArray manualObject, Target t, Environment env) {
			//Get the parameters from the manualObject
			MCPlayer player = Static.GetPlayer(manualObject.get("player", Target.UNKNOWN, env), Target.UNKNOWN, env);
			String message = Construct.nval(manualObject.get("message", Target.UNKNOWN, env));

			return EventBuilder.instantiate(MCPlayerChatEvent.class,
					player, message);
		}

		@Override
		public Map<String, Mixed> evaluate(BindableEvent e, Environment env) throws EventException {
			if(e instanceof MCPlayerChatEvent event) {
				Map<String, Mixed> map = evaluate_helper(e);
				//Fill in the event parameters
				map.put("message", new CString(event.getMessage(), Target.UNKNOWN));
				CArray ca = new CArray(Target.UNKNOWN, GenericParameters
					.addParameter(CString.TYPE, null).build(), env);
				for(MCPlayer recipient : event.getRecipients()) {
					ca.push(new CString(recipient.getName(), Target.UNKNOWN), Target.UNKNOWN, env);
				}
				map.put("format", new CString(event.getFormat(), Target.UNKNOWN));
				map.put("recipients", ca);
				return map;
			} else {
				throw new EventException("Cannot convert e to MCPlayerChatEvent");
			}
		}

		@Override
		public boolean modifyEvent(String key, Mixed value, BindableEvent event, Environment env) {
			if(event instanceof MCPlayerChatEvent e) {
				if("message".equals(key)) {
					e.setMessage(Construct.nval(value));
				}
				if("recipients".equals(key)) {
					if(value.isInstanceOf(CArray.TYPE, null, env)) {
						List<MCPlayer> list = new ArrayList<>();
						for(String index : ((CArray) value).stringKeySet()) {
							Mixed v = ((CArray) value).get(index, value.getTarget(), env);
							try {
								list.add(Static.GetPlayer(v, value.getTarget(), env));
							} catch (ConfigRuntimeException ex) {
								//Ignored
							}
						}
						e.setRecipients(list);
					} else {
						throw new CRECastException("recipients must be an array", value.getTarget());
					}
				}
				if("format".equals(key)) {
					try {
						e.setFormat(Construct.nval(value));
					} catch (UnknownFormatConversionException | IllegalFormatConversionException ex) {
						throw new CREFormatException(ex.getMessage(), value.getTarget());
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
					+ "This event is fired off when a player runs any command at all. This actually fires before normal"
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
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public boolean matches(Map<String, Mixed> prefilter, BindableEvent e, Environment env) throws PrefilterNonMatchException {
			if(e instanceof MCPlayerCommandEvent event) {
				String command = event.getCommand();
				Prefilters.match(prefilter, "player", event.getPlayer().getName(), PrefilterType.MACRO, env);
				if(prefilter.containsKey("command") && !command.equals(prefilter.get("command").val())) {
					return false;
				}
				if(prefilter.containsKey("prefix")) {
					StringHandling.parse_args pa = new StringHandling.parse_args();
					CArray ca = (CArray) pa.exec(Target.UNKNOWN, null, new CString(command, Target.UNKNOWN));
					if(ca.size() > 0) {
						return ca.get(0, Target.UNKNOWN, env).val().equals(prefilter.get("prefix").val());
					} else {
						return false;
					}
				}
				return true;
			}
			return false;
		}

		@Override
		public BindableEvent convert(CArray manualObject, Target t, Environment env) {
			MCPlayer player = Static.GetPlayer(manualObject.get("player", Target.UNKNOWN, env), Target.UNKNOWN, env);
			String command = Construct.nval(manualObject.get("command", Target.UNKNOWN, env));

			BindableEvent e = EventBuilder.instantiate(MCPlayerCommandEvent.class, player, command);
			return e;
		}

		@Override
		public Map<String, Mixed> evaluate(BindableEvent e, Environment env) throws EventException {
			if(e instanceof MCPlayerCommandEvent event) {
				Map<String, Mixed> map = evaluate_helper(e);
				//Fill in the event parameters
				map.put("command", new CString(event.getCommand(), Target.UNKNOWN));

				StringHandling.parse_args pa = new StringHandling.parse_args();
				CArray ca = (CArray) pa.exec(Target.UNKNOWN, null, new CString(event.getCommand(), Target.UNKNOWN));
				map.put("prefix", new CString(ca.get(0, Target.UNKNOWN, env).val(), Target.UNKNOWN));

				return map;
			} else {
				throw new EventException("Cannot convert e to MCPlayerCommandEvent");
			}
		}

		@Override
		public boolean modifyEvent(String key, Mixed value, BindableEvent event, Environment env) {
			if(event instanceof MCPlayerCommandEvent e) {

				if("command".equals(key)) {
					e.setCommand(value.val());
				}

				return true;
			}
			return false;
		}

		@Override
		public void cancel(BindableEvent o, boolean state) {
			((MCPlayerCommandEvent) o).cancel();
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
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public boolean matches(Map<String, Mixed> prefilter, BindableEvent e, Environment env) throws PrefilterNonMatchException {
			if(e instanceof MCWorldChangedEvent event) {
				Prefilters.match(prefilter, "player", event.getPlayer().getName(), PrefilterType.MACRO, env);
				Prefilters.match(prefilter, "from", event.getFrom().getName(), PrefilterType.STRING_MATCH, env);
				Prefilters.match(prefilter, "to", event.getTo().getName(), PrefilterType.STRING_MATCH, env);
				return true;
			}
			return false;
		}

		@Override
		public BindableEvent convert(CArray manualObject, Target t, Environment env) {
			MCPlayer player = Static.GetPlayer(manualObject.get("player", Target.UNKNOWN, env), Target.UNKNOWN, env);
			MCWorld from = Static.getServer().getWorld(manualObject.get("from", Target.UNKNOWN, env).val());

			return EventBuilder.instantiate(MCPlayerCommandEvent.class,
					player, from);
		}

		@Override
		public Map<String, Mixed> evaluate(BindableEvent e, Environment env) throws EventException {
			if(e instanceof MCWorldChangedEvent event) {
				Map<String, Mixed> map = evaluate_helper(e);
				//Fill in the event parameters
				map.put("from", new CString(event.getFrom().getName(), Target.UNKNOWN));
				map.put("to", new CString(event.getTo().getName(), Target.UNKNOWN));
				return map;
			} else {
				throw new EventException("Cannot convert e to MCWorldChangedEvent");
			}
		}

		@Override
		public boolean modifyEvent(String key, Mixed value, BindableEvent event, Environment env) {
			return false;
		}

	}

	private static final Map<Integer, Integer> THRESHOLD_LIST = new HashMap<>();

	public static Set<Integer> GetThresholdList() {
		return THRESHOLD_LIST.keySet();
	}

	private static final Map<Integer, Map<String, MCLocation>> LAST_PLAYER_LOCATIONS = new HashMap<>();

	public static Map<String, MCLocation> GetLastLocations(Integer i) {
		if(!LAST_PLAYER_LOCATIONS.containsKey(i)) {
			HashMap<String, MCLocation> newLocation = new HashMap<>();
			LAST_PLAYER_LOCATIONS.put(i, newLocation);
			return newLocation;
		}
		return (LAST_PLAYER_LOCATIONS.get(i));
	}

	@api
	public static class player_move extends AbstractEvent {

		@Override
		public String getName() {
			return "player_move";
		}

		@Override
		public String docs() {
			return "{player: <macro> The player that moved. Switching worlds does not trigger this event. "
					+ "| world: <string match> The world the player moved in."
					+ "| from: <location match> This should be a location array (x, y, z, world)."
					+ "| to: <location match> The location the player is now in. This should be a location array as"
					+ " well."
					+ "| threshold: <custom> The minimum distance the player must have travelled before the event"
					+ " will be triggered. This is based on the 3D distance, and is measured in block units.}"
					+ " This event is fired off after a player has moved a certain distance. Due to the high frequency"
					+ " of this event, prefilters are extremely important to use -- especially a threshold -- so that"
					+ " the script doesn't run every time."
					+ "{player | world | from: The location the player is coming from | to: The location the player is"
					+ " now in}"
					+ "{}"
					+ "{}";
		}

		@Override
		public void hook() {
			THRESHOLD_LIST.clear();
			LAST_PLAYER_LOCATIONS.clear();
		}

		@Override
		public void bind(BoundEvent event) {
			Map<String, Mixed> prefilters = event.getPrefilter();
			int threshold = (prefilters.containsKey("threshold")
					? ArgumentValidation.getInt32(prefilters.get("threshold"), Target.UNKNOWN, event.getEnvironment()) : 1);
			Integer count = THRESHOLD_LIST.get(threshold);
			THRESHOLD_LIST.put(threshold, (count != null ? count + 1 : 1));
		}

		@Override
		public void unbind(BoundEvent event) {
			Map<String, Mixed> prefilters = event.getPrefilter();
			int threshold = (prefilters.containsKey("threshold")
					? ArgumentValidation.getInt32(prefilters.get("threshold"), Target.UNKNOWN, event.getEnvironment()) : 1);
			Integer count = THRESHOLD_LIST.get(threshold);
			if(count != null) {
				if(count <= 1) {
					THRESHOLD_LIST.remove(threshold);
					LAST_PLAYER_LOCATIONS.remove(threshold);
				} else {
					THRESHOLD_LIST.put(threshold, count - 1);
				}
			}
		}

		@Override
		public boolean matches(Map<String, Mixed> prefilter, BindableEvent e, Environment env) throws PrefilterNonMatchException {
			if(e instanceof MCPlayerMoveEvent event) {
				if(prefilter.containsKey("threshold")) {
					if(ArgumentValidation.getInt(prefilter.get("threshold"), Target.UNKNOWN, env) != event.getThreshold()) {
						return false;
					}
				} else if(event.getThreshold() != 1) {
					return false;
				}
				if(prefilter.containsKey("world")
						&& !prefilter.get("world").val().equals(event.getFrom().getWorld().getName())) {
					return false;
				}
				Prefilters.match(prefilter, "from", event.getFrom(), PrefilterType.LOCATION_MATCH, env);
				Prefilters.match(prefilter, "to", event.getTo(), PrefilterType.LOCATION_MATCH, env);
				Prefilters.match(prefilter, "player", event.getPlayer().getName(), PrefilterType.MACRO, env);
				return true;
			}
			return false;
		}

		@Override
		public BindableEvent convert(CArray manualObject, Target t, Environment env) {
			MCPlayer p = Static.GetPlayer(manualObject.get("player", Target.UNKNOWN, env), Target.UNKNOWN, env);
			MCLocation from = ObjectGenerator.GetGenerator().location(manualObject.get("from", Target.UNKNOWN, env),
					p.getWorld(), manualObject.getTarget(), env);
			MCLocation to = ObjectGenerator.GetGenerator().location(manualObject.get("to", Target.UNKNOWN, env),
					p.getWorld(), manualObject.getTarget(), env);
			return EventBuilder.instantiate(MCPlayerMoveEvent.class, p, from, to);
		}

		@Override
		public Map<String, Mixed> evaluate(BindableEvent e, Environment env) throws EventException {
			if(e instanceof MCPlayerMoveEvent event) {
				Map<String, Mixed> map = new HashMap<>();
				map.put("player", new CString(event.getPlayer().getName(), Target.UNKNOWN));
				map.put("world", new CString(event.getFrom().getWorld().getName(), Target.UNKNOWN));
				map.put("from", ObjectGenerator.GetGenerator().location(event.getFrom(), env));
				map.put("to", ObjectGenerator.GetGenerator().location(event.getTo(), env));
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
		public boolean modifyEvent(String key, Mixed value, BindableEvent event, Environment env) {
			//Nothing can be modified, so always return false
			return false;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
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
					+ " {player | world | state | xp | hook: the fishhook entity id"
					+ " | caught: the id of the snared entity, can be a fish item}"
					+ " {xp: the exp the player will get from catching a fish}"
					+ " {}";
		}

		@Override
		public boolean matches(Map<String, Mixed> prefilter, BindableEvent e, Environment env) throws PrefilterNonMatchException {
			if(e instanceof MCPlayerFishEvent) {
				MCPlayerFishEvent event = (MCPlayerFishEvent) e;
				Prefilters.match(prefilter, "state", event.getState().name(), PrefilterType.MACRO, env);
				Prefilters.match(prefilter, "player", event.getPlayer().getName(), PrefilterType.MACRO, env);
				Prefilters.match(prefilter, "world", event.getPlayer().getWorld().getName(),
						PrefilterType.STRING_MATCH, env);
				return true;
			}
			return false;
		}

		@Override
		public BindableEvent convert(CArray manualObject, Target t, Environment env) {
			throw ConfigRuntimeException.CreateUncatchableException("Unsupported Operation", Target.UNKNOWN);
		}

		@Override
		public Map<String, Mixed> evaluate(BindableEvent e, Environment env) throws EventException {
			if(e instanceof MCPlayerFishEvent) {
				MCPlayerFishEvent event = (MCPlayerFishEvent) e;
				Target t = Target.UNKNOWN;
				Map<String, Mixed> ret = evaluate_helper(event);
				ret.put("world", new CString(event.getPlayer().getWorld().getName(), t));
				ret.put("state", new CString(event.getState().name(), t));
				ret.put("hook", new CString(event.getHook().getUniqueId().toString(), t));
				ret.put("xp", new CInt(event.getExpToDrop(), t));
				Mixed caught = CNull.NULL;
				if(event.getCaught() != null) {
					caught = new CString(event.getCaught().getUniqueId().toString(), t);
				}
				ret.put("caught", caught);
				return ret;
			} else {
				throw new EventException("Could not convert to MCPlayerFishEvent");
			}
		}

		@Override
		public boolean modifyEvent(String key, Mixed value, BindableEvent event, Environment env) {
			if(event instanceof MCPlayerFishEvent) {
				MCPlayerFishEvent e = (MCPlayerFishEvent) event;
				if(key.equals("xp")) {
					e.setExpToDrop(ArgumentValidation.getInt32(value, value.getTarget(), env));
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
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public void preExecution(Environment env, ActiveEvent activeEvent) {
			if(activeEvent.getUnderlyingEvent() instanceof MCPlayerFishEvent event) {
				// Static lookups of just spawned entities in certain fishing states don't work here, so inject them
				switch(event.getState()) {
					case FISHING:
						MCFishHook hook = event.getHook();
						Static.InjectEntity(hook);
						break;
					case CAUGHT_ENTITY:
					case CAUGHT_FISH:
						MCEntity entity = event.getCaught();
						Static.InjectEntity(entity);
						break;
				}
			}
		}

		@Override
		public void postExecution(Environment env, ActiveEvent activeEvent) {
			if(activeEvent.getUnderlyingEvent() instanceof MCPlayerFishEvent event) {
				switch(event.getState()) {
					case FISHING:
						MCFishHook hook = event.getHook();
						Static.UninjectEntity(hook);
						break;
					case CAUGHT_ENTITY:
					case CAUGHT_FISH:
						MCEntity entity = event.getCaught();
						Static.UninjectEntity(entity);
						break;
				}
			}
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
		public boolean matches(Map<String, Mixed> prefilter, BindableEvent event, Environment env) throws PrefilterNonMatchException {
			if(event instanceof MCGamemodeChangeEvent) {
				MCGamemodeChangeEvent e = (MCGamemodeChangeEvent) event;
				Prefilters.match(prefilter, "player", e.getPlayer().getName(), PrefilterType.MACRO, env);
				Prefilters.match(prefilter, "newmode", e.getNewGameMode().name(), PrefilterType.MACRO, env);
				return true;
			}
			return false;
		}

		@Override
		public BindableEvent convert(CArray manualObject, Target t, Environment env) {
			throw new CREBindException("Unsupported Operation", Target.UNKNOWN);
		}

		@Override
		public Map<String, Mixed> evaluate(BindableEvent event, Environment env) throws EventException {
			if(event instanceof MCGamemodeChangeEvent) {
				MCGamemodeChangeEvent e = (MCGamemodeChangeEvent) event;
				Map<String, Mixed> ret = evaluate_helper(e);
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
		public boolean modifyEvent(String key, Mixed value, BindableEvent event, Environment env) {
			return false;
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
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
		public boolean matches(Map<String, Mixed> prefilter, BindableEvent event, Environment env) throws PrefilterNonMatchException {
			if(event instanceof MCExpChangeEvent e) {
				Prefilters.match(prefilter, "player", e.getPlayer().getName(), PrefilterType.MACRO, env);
				return true;
			}
			return false;
		}

		@Override
		public BindableEvent convert(CArray manualObject, Target t, Environment env) {
			throw ConfigRuntimeException.CreateUncatchableException("Unsupported Operation", Target.UNKNOWN);
		}

		@Override
		public Map<String, Mixed> evaluate(BindableEvent event, Environment env) throws EventException {
			if(event instanceof MCExpChangeEvent) {
				MCExpChangeEvent e = (MCExpChangeEvent) event;
				Map<String, Mixed> ret = evaluate_helper(e);
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
		public boolean modifyEvent(String key, Mixed value, BindableEvent event, Environment env) {
			if(event instanceof MCExpChangeEvent) {
				MCExpChangeEvent e = (MCExpChangeEvent) event;
				if("amount".equals(key)) {
					e.setAmount(ArgumentValidation.getInt32(value, value.getTarget(), env));
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
			return "{player: <macro> The player which edited the book | signing: <boolean match> Whether or not the"
					+ " book is being signed}"
					+ " This event is called when a player edit a book."
					+ " {player: The player which edited the book | slot: The inventory slot number where the book is |"
					+ " oldbook: The book before the editing (an array with keys title, author and pages) |"
					+ " newbook: The book after the editing (an array with keys title, author and pages) |"
					+ " signing: Whether or not the book is being signed}"
					+ " {title | author | pages | signing}"
					+ " {}";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public boolean matches(Map<String, Mixed> prefilter, BindableEvent event, Environment env) throws PrefilterNonMatchException {
			if(event instanceof MCPlayerEditBookEvent playerEditBookEvent) {
				Prefilters.match(prefilter, "player", playerEditBookEvent.getPlayer().getName(), PrefilterType.MACRO, env);
				Prefilters.match(prefilter, "signing", playerEditBookEvent.isSigning(), PrefilterType.BOOLEAN_MATCH, env);
				return true;
			} else {
				return false;
			}
		}

		@Override
		public BindableEvent convert(CArray manualObject, Target t, Environment env) {
			return null;
		}

		@Override
		public Map<String, Mixed> evaluate(BindableEvent event, Environment env) throws EventException {
			if(event instanceof MCPlayerEditBookEvent playerEditBookEvent) {
				Map<String, Mixed> mapEvent = evaluate_helper(event);
				MCBookMeta oldBookMeta = playerEditBookEvent.getPreviousBookMeta();
				CArray oldBookArray = CArray.GetAssociativeArray(Target.UNKNOWN, null, env);
				if(oldBookMeta.hasTitle()) {
					oldBookArray.set("title", new CString(oldBookMeta.getTitle(), Target.UNKNOWN), Target.UNKNOWN, env);
				} else {
					oldBookArray.set("title", CNull.NULL, Target.UNKNOWN, env);
				}
				if(oldBookMeta.hasAuthor()) {
					oldBookArray.set("author", new CString(oldBookMeta.getAuthor(), Target.UNKNOWN), Target.UNKNOWN, env);
				} else {
					oldBookArray.set("author", CNull.NULL, Target.UNKNOWN, env);
				}
				if(oldBookMeta.hasPages()) {
					CArray pages = new CArray(Target.UNKNOWN, GenericParameters
							.addParameter(CString.TYPE, null).build(), env);
					for(String page : oldBookMeta.getPages()) {
						pages.push(new CString(page, Target.UNKNOWN), Target.UNKNOWN, env);
					}
					oldBookArray.set("author", pages, Target.UNKNOWN, env);
				} else {
					oldBookArray.set("pages", new CArray(Target.UNKNOWN, null, env), Target.UNKNOWN, env);
				}
				mapEvent.put("oldbook", oldBookArray);
				MCBookMeta newBookMeta = playerEditBookEvent.getNewBookMeta();
				CArray newBookArray = CArray.GetAssociativeArray(Target.UNKNOWN, null, env);
				if(newBookMeta.hasTitle()) {
					newBookArray.set("title", new CString(newBookMeta.getTitle(), Target.UNKNOWN), Target.UNKNOWN, env);
				} else {
					newBookArray.set("title", CNull.NULL, Target.UNKNOWN, env);
				}
				if(newBookMeta.hasAuthor()) {
					newBookArray.set("author", new CString(newBookMeta.getAuthor(), Target.UNKNOWN), Target.UNKNOWN, env);
				} else {
					newBookArray.set("author", CNull.NULL, Target.UNKNOWN, env);
				}
				if(newBookMeta.hasPages()) {
					CArray pages = new CArray(Target.UNKNOWN, GenericParameters
						.addParameter(CString.TYPE, null).build(), env);
					for(String page : newBookMeta.getPages()) {
						pages.push(new CString(page, Target.UNKNOWN), Target.UNKNOWN, env);
					}
					newBookArray.set("pages", pages, Target.UNKNOWN, env);
				} else {
					newBookArray.set("pages", new CArray(Target.UNKNOWN, null, env), Target.UNKNOWN, env);
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
		public boolean modifyEvent(String key, Mixed value, BindableEvent event, Environment env) {
			if(event instanceof MCPlayerEditBookEvent) {
				if(key.equalsIgnoreCase("title")) {
					MCPlayerEditBookEvent e = ((MCPlayerEditBookEvent) event);
					MCBookMeta bookMeta = e.getNewBookMeta();
					bookMeta.setTitle(value.val());
					e.setNewBookMeta(bookMeta);
					return true;
				} else if(key.equalsIgnoreCase("author")) {
					MCPlayerEditBookEvent e = ((MCPlayerEditBookEvent) event);
					MCBookMeta bookMeta = e.getNewBookMeta();
					bookMeta.setAuthor(value.val());
					e.setNewBookMeta(bookMeta);
					return true;
				} else if(key.equalsIgnoreCase("pages")) {
					CArray pageArray = ArgumentValidation.getArray(value, value.getTarget(), env);
					if(pageArray.inAssociativeMode()) {
						throw new CRECastException("The page array must not be associative.", pageArray.getTarget());
					} else {
						List<String> pages = new ArrayList<>();
						for(Mixed page : pageArray.asList()) {
							pages.add(page.val());
						}
						MCPlayerEditBookEvent e = ((MCPlayerEditBookEvent) event);
						MCBookMeta bookMeta = e.getNewBookMeta();
						bookMeta.setPages(pages);
						e.setNewBookMeta(bookMeta);
						return true;
					}
				} else if(key.equalsIgnoreCase("signing")) {
					((MCPlayerEditBookEvent) event).setSigning(ArgumentValidation.getBoolean(value, Target.UNKNOWN, env));
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
			return "{player: <macro> The player who toggled their flying state | flying: <boolean match> Whether or not"
					+ " the player is trying to start or stop flying | world: <macro>}"
					+ " Called when a player toggles their flying state."
					+ " {player: The player who toggled their flying state | flying: Whether or not the player is"
					+ " trying to start or stop flying |"
					+ " location: Where the player is}"
					+ " {}"
					+ " {}";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public boolean matches(Map<String, Mixed> prefilter, BindableEvent event, Environment env) throws PrefilterNonMatchException {
			if(event instanceof MCPlayerToggleFlightEvent ptfe) {
				MCPlayer player = ptfe.getPlayer();
				Prefilters.match(prefilter, "player", player.getName(), PrefilterType.MACRO, env);
				Prefilters.match(prefilter, "flying", ptfe.isFlying(), PrefilterType.BOOLEAN_MATCH, env);
				Prefilters.match(prefilter, "world", player.getWorld().getName(), PrefilterType.MACRO, env);
				return true;
			} else {
				return false;
			}
		}

		@Override
		public BindableEvent convert(CArray manualObject, Target t, Environment env) {
			return null;
		}

		@Override
		public Map<String, Mixed> evaluate(BindableEvent event, Environment env) throws EventException {
			if(event instanceof MCPlayerToggleFlightEvent ptfe) {
				Map<String, Mixed> mapEvent = evaluate_helper(event);
				mapEvent.put("location", ObjectGenerator.GetGenerator().location(ptfe.getPlayer().getLocation(), env));
				mapEvent.put("flying", CBoolean.get(ptfe.isFlying()));
				return mapEvent;
			} else {
				throw new EventException("Cannot convert event to PlayerToggleFlightEvent");
			}
		}

		@Override
		public boolean modifyEvent(String key, Mixed value, BindableEvent event, Environment env) {
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
			return "{player: <macro> The player who toggled their sneaking state | sneaking: <boolean match> Whether"
					+ " or not the player is now sneaking | world: <macro>}"
					+ " Called when a player toggles their sneaking state."
					+ " {player: The player who toggled their sneaking state | sneaking: Whether or not the player is"
					+ " now sneaking |"
					+ " location: Where the player is}"
					+ " {}"
					+ " {}";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public boolean matches(Map<String, Mixed> prefilter, BindableEvent event, Environment env) throws PrefilterNonMatchException {
			if(event instanceof MCPlayerToggleSneakEvent ptse) {
				MCPlayer player = ptse.getPlayer();
				Prefilters.match(prefilter, "player", player.getName(), PrefilterType.MACRO, env);
				Prefilters.match(prefilter, "sneaking", ptse.isSneaking(), PrefilterType.BOOLEAN_MATCH, env);
				Prefilters.match(prefilter, "world", player.getWorld().getName(), PrefilterType.MACRO, env);
				return true;
			} else {
				return false;
			}
		}

		@Override
		public BindableEvent convert(CArray manualObject, Target t, Environment env) {
			return null;
		}

		@Override
		public Map<String, Mixed> evaluate(BindableEvent event, Environment env) throws EventException {
			if(event instanceof MCPlayerToggleSneakEvent ptse) {
				Map<String, Mixed> mapEvent = evaluate_helper(event);
				mapEvent.put("location", ObjectGenerator.GetGenerator().location(ptse.getPlayer().getLocation(), env));
				mapEvent.put("sneaking", CBoolean.get(ptse.isSneaking()));
				return mapEvent;
			} else {
				throw new EventException("Cannot convert event to PlayerToggleSneakEvent");
			}
		}

		@Override
		public boolean modifyEvent(String key, Mixed value, BindableEvent event, Environment env) {
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
			return "{player: <macro> The player who toggled their sprinting state | sprinting: <boolean match> Whether"
					+ " or not the player is now sprinting | world: <macro>}"
					+ " Called when a player toggles their sprinting state."
					+ " {player: The player who toggled their sprinting state | sprinting: Whether or not the player"
					+ " is now sprinting |"
					+ " location: Where the player is}"
					+ " {}"
					+ " {}";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public boolean matches(Map<String, Mixed> prefilter, BindableEvent event, Environment env) throws PrefilterNonMatchException {
			if(event instanceof MCPlayerToggleSprintEvent) {
				MCPlayerToggleSprintEvent ptse = (MCPlayerToggleSprintEvent) event;
				MCPlayer player = ptse.getPlayer();
				Prefilters.match(prefilter, "player", player.getName(), PrefilterType.MACRO, env);
				Prefilters.match(prefilter, "sprinting", ptse.isSprinting(), PrefilterType.BOOLEAN_MATCH, env);
				Prefilters.match(prefilter, "world", player.getWorld().getName(), PrefilterType.MACRO, env);
				return true;
			} else {
				return false;
			}
		}

		@Override
		public BindableEvent convert(CArray manualObject, Target t, Environment env) {
			return null;
		}

		@Override
		public Map<String, Mixed> evaluate(BindableEvent event, Environment env) throws EventException {
			if(event instanceof MCPlayerToggleSprintEvent) {
				MCPlayerToggleSprintEvent ptse = (MCPlayerToggleSprintEvent) event;
				Map<String, Mixed> mapEvent = evaluate_helper(event);
				mapEvent.put("location", ObjectGenerator.GetGenerator().location(ptse.getPlayer().getLocation(), env));
				mapEvent.put("sprinting", CBoolean.get(ptse.isSprinting()));
				return mapEvent;
			} else {
				throw new EventException("Cannot convert event to PlayerToggleSprintEvent");
			}
		}

		@Override
		public boolean modifyEvent(String key, Mixed value, BindableEvent event, Environment env) {
			return false;
		}
	}

	@api
	public static class resource_pack_status extends AbstractEvent {

		@Override
		public String getName() {
			return "resource_pack_status";
		}

		@Override
		public Driver driver() {
			return Driver.RESOURCE_PACK_STATUS;
		}

		@Override
		public String docs() {
			return "{player: <string match> | status: <string match> }"
					+ " Called when a player's client responds to a request to download and load a resource pack."
					+ " Two of these events may be fired for each request: first when the client accepts the pack,"
					+ " and later when the client successfully loads (or fails to download) the pack."
					+ " {player | status: The resource pack status received from the client, one of: "
					+ StringUtils.Join(MCResourcePackStatus.values(), ", ", ", or ") + "}"
					+ " {}"
					+ " {}";
		}

		@Override
		public boolean matches(Map<String, Mixed> prefilter, BindableEvent event, Environment env) throws PrefilterNonMatchException {
			if(event instanceof MCPlayerResourcePackEvent) {
				MCPlayerResourcePackEvent prpe = (MCPlayerResourcePackEvent) event;
				Prefilters.match(prefilter, "player", prpe.getPlayer().getName(), PrefilterType.STRING_MATCH, env);
				Prefilters.match(prefilter, "status", prpe.getStatus().name(), PrefilterType.STRING_MATCH, env);
				return true;
			} else {
				return false;
			}
		}

		@Override
		public Map<String, Mixed> evaluate(BindableEvent event, Environment env) throws EventException {
			Map<String, Mixed> map = evaluate_helper(event);
			map.put("status", new CString(((MCPlayerResourcePackEvent) event).getStatus().name(), Target.UNKNOWN));
			return map;
		}

		@Override
		public boolean modifyEvent(String key, Mixed value, BindableEvent event, Environment env) {
			return false;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_4;
		}

		@Override
		public BindableEvent convert(CArray manualObject, Target t, Environment env) {
			return null;
		}
	}
}
