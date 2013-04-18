/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.core.events.drivers;

import com.laytonsmith.abstraction.events.MCServerPingEvent;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.CArray;
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
import java.util.Map;

/**
 *
 * @author Layton
 */
public class ServerEvents {

	@api
	public static class server_ping extends AbstractEvent {
	
		public String getName() {
			return "server_ping";
		}
	
		public String docs() {
			return "{players: <math match> | maxplayers: <math match>}"
					+ " Fired when a user who has saved this server looks at their serverlist."
					+ " {ip: The address the ping is coming from | players: the number of players online"
					+ " | maxplayers: the number of slots on the server | motd}"
					+ " {motd: The message a player is shown on the serverlist | maxplayers}"
					+ " {}";
		}
	
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
	
		public BindableEvent convert(CArray manualObject) {
			throw new ConfigRuntimeException("Unsupported Operation", Target.UNKNOWN);
		}
	
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
				return ret;
			} else {
				throw new EventException("Could not convert to MCPingEvent");
			}
		}
	
		public boolean modifyEvent(String key, Construct value,
				BindableEvent event) {
			if (event instanceof MCServerPingEvent) {
				MCServerPingEvent e = (MCServerPingEvent) event;
				if (key.equals("motd")) {
					e.setMOTD(value.val());
					return true;
				}
				if (key.equals("maxplayers")) {
					e.setMaxPlayers(Static.getInt32(value, Target.UNKNOWN));
					return true;
				}
			}
			return false;
		}
	
		public Driver driver() {
			return Driver.SERVER_PING;
		}
	
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}	
}
