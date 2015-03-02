package com.laytonsmith.core.events.drivers;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.events.MCCommandTabCompleteEvent;
import com.laytonsmith.abstraction.events.MCRedstoneChangedEvent;
import com.laytonsmith.abstraction.events.MCServerPingEvent;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.ArgumentValidation;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.ObjectGenerator;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CBoolean;
import com.laytonsmith.core.constructs.CInt;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 *
 */
public class ServerEvents {

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
					+ " will be ignored, moreover, add offline players will throw a PlayerOfflineException each time the event is triggered."
					+ " This will also change the player count.}"
					+ " {}";
		}

		@Override
		public boolean matches(Map<String, Construct> prefilter, BindableEvent e)
				throws PrefilterNonMatchException {
			if (e instanceof MCServerPingEvent) {
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
		public Map<String, Construct> evaluate(BindableEvent e)
				throws EventException {
			if (e instanceof MCServerPingEvent) {
				MCServerPingEvent event = (MCServerPingEvent) e;
				Target t = Target.UNKNOWN;
				Map<String, Construct> ret = evaluate_helper(event);
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
				for (MCPlayer player : event.getPlayers()) {
					players.push(new CString(player.getName(), t));
				}
				ret.put("list", players);
				return ret;
			} else {
				throw new EventException("Could not convert to MCPingEvent");
			}
		}

		@Override
		public boolean modifyEvent(String key, Construct value, BindableEvent event) {
			if (event instanceof MCServerPingEvent) {
				MCServerPingEvent e = (MCServerPingEvent) event;
				switch (key.toLowerCase()) {
					case "motd":
						e.setMOTD(value.val());
						return true;
					case "maxplayers":
						e.setMaxPlayers(Static.getInt32(value, Target.UNKNOWN));
						return true;
					case "list":
						Set<MCPlayer> players = new HashSet<>();
						for (Construct construct : ArgumentValidation.getArray(value, Target.UNKNOWN).asList()) {
							players.add(Static.GetPlayer(construct, Target.UNKNOWN));
						}
						e.setPlayers(players);
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
			return CHVersion.V3_3_1;
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
					+ " register_command(), or if the set tab completer doesn't return an array."
					+ " {command: The command name that was registered. | alias: The alias the player entered to run"
					+ " the command. | args: The given arguments after the alias. | completions: The available"
					+ " completions for the last argument. | sender: The player that ran the command. }"
					+ " {completions}"
					+ " {}";
		}

		@Override
		public boolean matches(Map<String, Construct> prefilter, BindableEvent event) throws PrefilterNonMatchException {
			if (event instanceof MCCommandTabCompleteEvent) {
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
			if (event instanceof MCCommandTabCompleteEvent) {
				MCCommandTabCompleteEvent e = (MCCommandTabCompleteEvent) event;
				Target t = Target.UNKNOWN;
				Map<String, Construct> ret = evaluate_helper(event);
				ret.put("sender", new CString(e.getCommandSender().getName(), t));
				CArray comp = new CArray(t);
				for (String c : e.getCompletions()) {
					comp.push(new CString(c, t));
				}
				ret.put("completions", comp);
				ret.put("command", new CString(e.getCommand().getName(), t));
				CArray args = new CArray(t);
				for (String a : e.getArguments()) {
						args.push(new CString(a, t));
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
		public boolean modifyEvent(String key, Construct value, BindableEvent event) {
			if (event instanceof MCCommandTabCompleteEvent) {
				MCCommandTabCompleteEvent e = (MCCommandTabCompleteEvent) event;
				if ("completions".equals(key)) {
					if (value instanceof CArray) {
						e.getCompletions().clear();
						if (((CArray) value).inAssociativeMode()) {
							for (Construct k : ((CArray) value).keySet()) {
								e.getCompletions().add(((CArray) value).get(k, Target.UNKNOWN).val());
							}
						} else {
							for (Construct v : ((CArray) value).asList()) {
								e.getCompletions().add(v.val());
							}
						}
						return true;
					}
				}
			}
			return false;
		}

		@Override
		public Version since() {
			return CHVersion.V3_3_1;
		}
	}

	private final static Map<MCLocation, Boolean> redstoneMonitors = Collections.synchronizedMap(new HashMap<MCLocation, Boolean>());

	/**
	 * Returns a synchronized set of redstone monitors. When iterating on the
	 * list, be sure to synchronize manually.
	 * @return
	 */
	public static Map<MCLocation, Boolean> getRedstoneMonitors(){
		return redstoneMonitors;
	}

	@api
	public static class redstone_changed extends AbstractEvent {

		@Override
		public void hook() {
			redstoneMonitors.clear();
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
		public boolean matches(Map<String, Construct> prefilter, BindableEvent e) throws PrefilterNonMatchException {
			if(e instanceof MCRedstoneChangedEvent){
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
		public Map<String, Construct> evaluate(BindableEvent e) throws EventException {
			MCRedstoneChangedEvent event = (MCRedstoneChangedEvent) e;
			Map<String, Construct> map = evaluate_helper(e);
			map.put("location", ObjectGenerator.GetGenerator().location(event.getLocation()));
			map.put("active", CBoolean.get(event.isActive()));
			return map;
		}

		@Override
		public Driver driver() {
			return Driver.REDSTONE_CHANGED;
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

}
