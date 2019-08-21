package com.laytonsmith.core.events.drivers;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.abstraction.MCCommandSender;
import com.laytonsmith.abstraction.MCBlockCommandSender;
import com.laytonsmith.abstraction.MCConsoleCommandSender;
import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.entities.MCCommandMinecart;
import com.laytonsmith.abstraction.events.MCBroadcastMessageEvent;
import com.laytonsmith.abstraction.events.MCCommandTabCompleteEvent;
import com.laytonsmith.abstraction.events.MCServerCommandEvent;
import com.laytonsmith.abstraction.events.MCRedstoneChangedEvent;
import com.laytonsmith.abstraction.events.MCServerPingEvent;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.ArgumentValidation;
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
import com.laytonsmith.core.environments.CommandHelperEnvironment;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.events.AbstractEvent;
import com.laytonsmith.core.events.BindableEvent;
import com.laytonsmith.core.events.BoundEvent.ActiveEvent;
import com.laytonsmith.core.events.Driver;
import com.laytonsmith.core.events.EventBuilder;
import com.laytonsmith.core.events.Prefilters;
import com.laytonsmith.core.events.Prefilters.PrefilterType;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.exceptions.EventException;
import com.laytonsmith.core.exceptions.PrefilterNonMatchException;
import com.laytonsmith.core.exceptions.CRE.CRECastException;
import com.laytonsmith.core.natives.interfaces.Mixed;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ServerEvents {

	public static String docs() {
		return "Contains non-specific server-wide events.";
	}

	@api
	public static class server_command extends AbstractEvent {

		@Override
		public String getName() {
			return "server_command";
		}

		@Override
		public String docs() {
			return "{prefix: <string match> The first part of the command, i.e. 'cmd' in '/cmd blah blah'"
					+ " | type: <string match> The command sender type}"
					+ "This event is fired off when any command is run from the console or commandblock. This fires"
					+ " before CommandHelper aliases, allowing you to insert control beforehand. Be careful with this"
					+ " event, because it can override ALL server commands, potentially creating all sorts of havoc."
					+ "{command: The entire command | prefix: The prefix of the command"
					+ " | sendertype: The command sender type. This is one of console, command_block,"
					+ " command_minecart or null if the sender is unknown to CommandHelper.}"
					+ "{command}"
					+ "{}";
		}

		@Override
		public Driver driver() {
			return Driver.SERVER_COMMAND;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_2;
		}

		@Override
		public boolean matches(Map<String, Mixed> prefilter, BindableEvent e) throws PrefilterNonMatchException {
			if(!(e instanceof MCServerCommandEvent)) {
				return false;
			}
			MCServerCommandEvent event = (MCServerCommandEvent) e;
			String prefix = event.getCommand().split(" ", 2)[0];
			Prefilters.match(prefilter, "prefix", prefix, PrefilterType.STRING_MATCH);
			Prefilters.match(prefilter, "sendertype",
					getCommandsenderString(event.getCommandSender()), PrefilterType.STRING_MATCH);
			return true;
		}

		@Override
		public BindableEvent convert(CArray manualObject, Target t) {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public Map<String, Mixed> evaluate(BindableEvent e) throws EventException {
			if(!(e instanceof MCServerCommandEvent)) {
				throw new EventException("Cannot convert e to MCServerCommandEvent");
			}
			MCServerCommandEvent event = (MCServerCommandEvent) e;
			Map<String, Mixed> map = new HashMap<>();
			map.put("command", new CString(event.getCommand(), Target.UNKNOWN));
			String prefix = event.getCommand().split(" ", 2)[0];
			map.put("prefix", new CString(prefix, Target.UNKNOWN));

			// Set the command sender type.
			String type = getCommandsenderString(event.getCommandSender());
			map.put("sendertype", (type == null ? CNull.NULL : new CString(type, Target.UNKNOWN)));

			return map;
		}

		private static String getCommandsenderString(MCCommandSender sender) {
			if(sender instanceof MCConsoleCommandSender) {
				return "console";
			} else if(sender instanceof MCBlockCommandSender) {
				return "command_block";
			} else if(sender instanceof MCCommandMinecart) {
				return "command_minecart";
			} else {
				return null; // Unknown sender implementation.
			}
		}

		@Override
		public boolean modifyEvent(String key, Mixed value, BindableEvent event) {
			if(event instanceof MCServerCommandEvent) {
				MCServerCommandEvent e = (MCServerCommandEvent) event;
				if(key.equals("command")) {
					e.setCommand(value.val());
					return true;
				}
			}
			return false;
		}

		@Override
		public void preExecution(Environment env, ActiveEvent activeEvent) {
			if(activeEvent.getUnderlyingEvent() instanceof MCServerCommandEvent) {
				MCServerCommandEvent event = (MCServerCommandEvent) activeEvent.getUnderlyingEvent();
				env.getEnv(CommandHelperEnvironment.class).SetCommandSender(event.getCommandSender());
			}
		}
	}

	@api
	public static class server_ping extends AbstractEvent {

		@Override
		public String getName() {
			return "server_ping";
		}

		@Override
		public String docs() {
			return "{players: <math match> | maxplayers: <math match>}"
					+ " Fired when a user who has saved this server looks at their serverlist."
					+ " {ip: The address the ping is coming from | players: The number of players online"
					+ " | maxplayers: The number of slots on the server | motd: The message a player is shown on the serverlist"
					+ " | list: The list of connected players}"
					+ " {motd | maxplayers | list: It is only possible to remove players, the added players"
					+ " will be ignored. This will also change the player count.}"
					+ " {}";
		}

		@Override
		public boolean matches(Map<String, Mixed> prefilter, BindableEvent e) throws PrefilterNonMatchException {
			if(e instanceof MCServerPingEvent) {
				MCServerPingEvent event = (MCServerPingEvent) e;
				Prefilters.match(prefilter, "players", event.getNumPlayers(), PrefilterType.MATH_MATCH);
				Prefilters.match(prefilter, "maxplayers", event.getMaxPlayers(), PrefilterType.MATH_MATCH);
				return true;
			}
			return false;
		}

		@Override
		public BindableEvent convert(CArray manualObject, Target t) {
			throw ConfigRuntimeException.CreateUncatchableException("Unsupported Operation", Target.UNKNOWN);
		}

		@Override
		public Map<String, Mixed> evaluate(BindableEvent e) throws EventException {
			if(e instanceof MCServerPingEvent) {
				MCServerPingEvent event = (MCServerPingEvent) e;
				Target t = Target.UNKNOWN;
				Map<String, Mixed> ret = evaluate_helper(event);
				String ip;
				try {
					ip = event.getAddress().getHostAddress();
				} catch (NullPointerException npe) {
					ip = "";
				}
				ret.put("ip", new CString(ip, t));
				ret.put("motd", new CString(event.getMOTD(), t));
				ret.put("players", new CInt(event.getNumPlayers(), t));
				ret.put("maxplayers", new CInt(event.getMaxPlayers(), t));
				CArray players = new CArray(t);
				for(MCPlayer player : event.getPlayers()) {
					players.push(new CString(player.getName(), t), t);
				}
				ret.put("list", players);
				return ret;
			} else {
				throw new EventException("Could not convert to MCPingEvent");
			}
		}

		@Override
		public boolean modifyEvent(String key, Mixed value, BindableEvent event) {
			if(event instanceof MCServerPingEvent) {
				MCServerPingEvent e = (MCServerPingEvent) event;
				switch(key.toLowerCase()) {
					case "motd":
						e.setMOTD(value.val());
						return true;
					case "maxplayers":
						e.setMaxPlayers(Static.getInt32(value, value.getTarget()));
						return true;
					case "list":
						// Modifies the player list. The new list will be the intersection of the original
						// and the given list. Names and UUID's outside this intersection will simply be ignored.
						Set<MCPlayer> modifiedPlayers = new HashSet<>();
						List<Mixed> passedList = ArgumentValidation.getArray(value, value.getTarget()).asList();
						for(MCPlayer player : e.getPlayers()) {
							for(Mixed construct : passedList) {
								String playerStr = construct.val();
								if(playerStr.length() > 0 && playerStr.length() <= 16) { // "player" is a name.
									if(playerStr.equalsIgnoreCase(player.getName())) {
										modifiedPlayers.add(player);
										break;
									}
								} else { // "player" is the UUID of the player.
									if(playerStr.equalsIgnoreCase(player.getUniqueID().toString())) {
										modifiedPlayers.add(player);
										break;
									}
								}
							}
						}
						e.setPlayers(modifiedPlayers);
						return true;
				}
			}
			return false;
		}

		@Override
		public Driver driver() {
			return Driver.SERVER_PING;
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}
	}

	@api
	public static class tab_complete_command extends AbstractEvent {

		@Override
		public String getName() {
			return "tab_complete_command";
		}

		@Override
		public String docs() {
			return "{}"
					+ " This will fire if a tab completer has not been set for a command registered with"
					+ " register_command(), or if the set tab completer doesn't return an array. If completions are "
					+ " not modified, registered commands will tab complete online player names."
					+ " {command: The command name that was registered. | alias: The alias the player entered to run"
					+ " the command. | args: The given arguments after the alias. | completions: The available"
					+ " completions for the last argument. | sender: The player that ran the command. }"
					+ " {completions}"
					+ " {}";
		}

		@Override
		public boolean matches(Map<String, Mixed> prefilter, BindableEvent event) throws PrefilterNonMatchException {
			return event instanceof MCCommandTabCompleteEvent;
		}

		@Override
		public BindableEvent convert(CArray manualObject, Target t) {
			throw ConfigRuntimeException.CreateUncatchableException("Unsupported Operation", Target.UNKNOWN);
		}

		@Override
		public Map<String, Mixed> evaluate(BindableEvent event) throws EventException {
			if(event instanceof MCCommandTabCompleteEvent) {
				MCCommandTabCompleteEvent e = (MCCommandTabCompleteEvent) event;
				Target t = Target.UNKNOWN;
				Map<String, Mixed> ret = evaluate_helper(event);
				ret.put("sender", new CString(e.getCommandSender().getName(), t));
				CArray comp = new CArray(t);
				if(e.getCompletions() != null) {
					for(String c : e.getCompletions()) {
						comp.push(new CString(c, t), t);
					}
				}
				ret.put("completions", comp);
				ret.put("command", new CString(e.getCommand().getName(), t));
				CArray args = new CArray(t);
				for(String a : e.getArguments()) {
					args.push(new CString(a, t), t);
				}
				ret.put("args", args);
				ret.put("alias", new CString(e.getAlias(), t));
				return ret;
			} else {
				throw new EventException("Could not convert to MCCommandTabCompleteEvent");
			}
		}

		@Override
		public Driver driver() {
			return Driver.TAB_COMPLETE;
		}

		@Override
		public boolean modifyEvent(String key, Mixed value, BindableEvent event) {
			if(event instanceof MCCommandTabCompleteEvent) {
				MCCommandTabCompleteEvent e = (MCCommandTabCompleteEvent) event;
				if("completions".equals(key)) {
					if(value.isInstanceOf(CArray.TYPE)) {
						List<String> comp = new ArrayList<>();
						if(((CArray) value).inAssociativeMode()) {
							for(Mixed k : ((CArray) value).keySet()) {
								comp.add(((CArray) value).get(k, value.getTarget()).val());
							}
						} else {
							for(Mixed v : ((CArray) value).asList()) {
								comp.add(v.val());
							}
						}
						e.setCompletions(comp);
						return true;
					}
				}
			}
			return false;
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}
	}

	private static final Map<MCLocation, Boolean> REDSTONE_MONITORS =
			Collections.synchronizedMap(new HashMap<MCLocation, Boolean>());

	/**
	 * Returns a synchronized set of redstone monitors. When iterating on the list, be sure to synchronize manually.
	 *
	 * @return
	 */
	public static Map<MCLocation, Boolean> getRedstoneMonitors() {
		return REDSTONE_MONITORS;
	}

	@api
	public static class redstone_changed extends AbstractEvent {

		@Override
		public void hook() {
			REDSTONE_MONITORS.clear();
		}

		@Override
		public String getName() {
			return "redstone_changed";
		}

		@Override
		public String docs() {
			return "{location: <location match>}"
					+ " Fired when a redstone activatable block is toggled, either on or off, AND the block has been set to be monitored"
					+ " with the monitor_redstone function."
					+ " {location: The location of the block | active: Whether or not the block is now active, or disabled.}"
					+ " {}"
					+ " {}";
		}

		@Override
		public boolean matches(Map<String, com.laytonsmith.core.natives.interfaces.Mixed> prefilter, BindableEvent e) throws PrefilterNonMatchException {
			if(e instanceof MCRedstoneChangedEvent) {
				MCRedstoneChangedEvent event = (MCRedstoneChangedEvent) e;
				Prefilters.match(prefilter, "location", event.getLocation(), PrefilterType.LOCATION_MATCH);
				return true;
			}
			return false;
		}

		@Override
		public BindableEvent convert(CArray manualObject, Target t) {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public Map<String, Mixed> evaluate(BindableEvent e) throws EventException {
			MCRedstoneChangedEvent event = (MCRedstoneChangedEvent) e;
			Map<String, Mixed> map = evaluate_helper(e);
			map.put("location", ObjectGenerator.GetGenerator().location(event.getLocation()));
			map.put("active", CBoolean.get(event.isActive()));
			return map;
		}

		@Override
		public Driver driver() {
			return Driver.REDSTONE_CHANGED;
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
	public static class broadcast_message extends AbstractEvent {

		@Override
		public String getName() {
			return "broadcast_message";
		}

		@Override
		public String docs() {
			return "{message: <string match>}"
					+ " Fired when a message is broadcasted on the server."
					+ " {message: The message that will be broadcasted"
					+ " | player_recipients: An array of players who will receive the message.}"
					+ " {message}"
					+ " {}";
		}

		@Override
		public boolean matches(Map<String, Mixed> prefilter, BindableEvent e) throws PrefilterNonMatchException {
			if(e instanceof MCBroadcastMessageEvent) {
				MCBroadcastMessageEvent event = (MCBroadcastMessageEvent) e;
				Prefilters.match(prefilter, "message", event.getMessage(), PrefilterType.STRING_MATCH);
				return true;
			}
			return false;
		}

		@Override
		public BindableEvent convert(CArray manualObject, Target t) {

			// Get the player recipients.
			Mixed cRecipients = manualObject.get("player_recipients", t);
			if(!(cRecipients instanceof CArray) && !(cRecipients instanceof CNull)) {
				throw new CRECastException("Expected player_recepients to be an array, but received: "
						+ cRecipients.typeof().toString(), t);
			}
			Set<MCCommandSender> recipients = new HashSet<>();
			CArray recipientsArray = (CArray) cRecipients;
			for(int i = 0; i < recipientsArray.size(); i++) {
				MCPlayer player = Static.GetPlayer(recipientsArray.get(i, t), t);
				recipients.add(player);
			}

			// Get the message.
			Mixed cMessage = manualObject.get("message", t);
			if(!(cMessage instanceof CString)) {
				throw new CRECastException("Expected message to be a string, but received: "
						+ cMessage.typeof().toString(), t);
			}

			// Instantiate and return the event.
			return EventBuilder.instantiate(MCBroadcastMessageEvent.class,
					Construct.nval((CString) cMessage), recipients);
		}

		@Override
		public Map<String, Mixed> evaluate(BindableEvent e) throws EventException {
			MCBroadcastMessageEvent event = (MCBroadcastMessageEvent) e;
			Map<String, Mixed> map = evaluate_helper(e);
			map.put("message", new CString(event.getMessage(), Target.UNKNOWN));
			CArray cRecipients = new CArray(Target.UNKNOWN);
			for(MCPlayer player : event.getPlayerRecipients()) {
				cRecipients.push(new CString(player.getName(), Target.UNKNOWN), Target.UNKNOWN);
			}
			map.put("player_recipients", cRecipients);
			return map;
		}

		@Override
		public Driver driver() {
			return Driver.BROADCAST_MESSAGE;
		}

		@Override
		public boolean modifyEvent(String key, Mixed value, BindableEvent e) {
			if(key.equals("message")) {
				MCBroadcastMessageEvent event = (MCBroadcastMessageEvent) e;
				event.setMessage(Construct.nval(value));
				return true;
			}
			return false;
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_2;
		}
	}
}
