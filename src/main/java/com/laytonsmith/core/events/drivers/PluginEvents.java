package com.laytonsmith.core.events.drivers;

import com.laytonsmith.abstraction.events.MCPluginIncomingMessageEvent;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CByteArray;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.events.AbstractEvent;
import com.laytonsmith.core.events.BindableEvent;
import com.laytonsmith.core.events.Driver;
import com.laytonsmith.core.events.Prefilters;
import com.laytonsmith.core.exceptions.EventException;
import com.laytonsmith.core.exceptions.PrefilterNonMatchException;
import com.laytonsmith.core.natives.interfaces.Mixed;

import java.util.Map;

/**
 *
 * @author Jason Unger (entityreborn@gmail.com)
 */
public class PluginEvents {

	public static String docs() {
		return "Contains events related to generic plugin messaging.";
	}

	@api
	public static class plugin_message_received extends AbstractEvent {

		@Override
		public boolean isCancellable(BindableEvent o) {
			return false;
		}

		@Override
		public String getName() {
			return "plugin_message_received";
		}

		@Override
		public String docs() {
			return "{channel: <string match>}"
					+ " Fires when a player's client sends a plugin message."
					+ " {player: the player | channel: the channel used |"
					+ " bytes: byte array of the data sent}"
					+ " {}"
					+ " {player|channel|bytes}";
		}

		@Override
		public boolean matches(Map<String, Mixed> prefilter, BindableEvent e, Environment env) throws PrefilterNonMatchException {
			if(e instanceof MCPluginIncomingMessageEvent) {
				MCPluginIncomingMessageEvent event = (MCPluginIncomingMessageEvent) e;

				Prefilters.match(prefilter, "channel", event.getChannel(), Prefilters.PrefilterType.STRING_MATCH, env);

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
			if(e instanceof MCPluginIncomingMessageEvent) {
				MCPluginIncomingMessageEvent event = (MCPluginIncomingMessageEvent) e;
				Map<String, Mixed> ret = evaluate_helper(e);

				ret.put("channel", new CString(event.getChannel(), Target.UNKNOWN));
				ret.put("player", new CString(event.getPlayer().getName(), Target.UNKNOWN));

				// Insert bytes into a CByteArray
				CByteArray a = CByteArray.wrap(event.getBytes().clone(), Target.UNKNOWN, env);

				a.rewind();

				ret.put("bytes", a);

				return ret;
			} else {
				throw new EventException("Cannot convert to MCPluginIncomingMessageEvent");
			}
		}

		@Override
		public Driver driver() {
			return Driver.PLUGIN_MESSAGE_RECEIVED;
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
}
