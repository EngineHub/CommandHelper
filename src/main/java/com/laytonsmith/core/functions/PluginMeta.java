package com.laytonsmith.core.functions;

import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.MCPluginMeta;
import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.abstraction.pluginmessages.MCMessenger;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CBoolean;
import com.laytonsmith.core.constructs.CByteArray;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.CVoid;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.CommandHelperEnvironment;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CRE.CRECastException;
import com.laytonsmith.core.exceptions.CRE.CRENotFoundException;
import com.laytonsmith.core.exceptions.CRE.CREPlayerOfflineException;
import com.laytonsmith.core.exceptions.CRE.CREPluginChannelException;
import com.laytonsmith.core.exceptions.CRE.CREThrowable;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import java.util.Set;

/**
 * 
 */
public class PluginMeta {
	public static String docs(){
		return "This class contains the functions use to communicate with other plugins and the server in general.";
	}
	
	@api
	public static class fake_incoming_plugin_message extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CREPlayerOfflineException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			MCPluginMeta meta = StaticLayer.GetConvertor().GetPluginMeta();
			MCPlayer p = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
			int offset = 0;
			if(args.length == 3){
				offset = 1;
				p = Static.GetPlayer(args[0], t);
			}
			String channel = args[0 + offset].val();
			CByteArray ba = Static.getByteArray(args[1 + offset], t);
			Static.AssertPlayerNonNull(p, t);
			meta.fakeIncomingMessage(p, channel, ba.asByteArrayCopy());
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "fake_incoming_plugin_message";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2, 3};
		}

		@Override
		public String docs() {
			return "void {[player,] channel, message} Fakes an incoming plugin message from the player. Channel should be a string (the"
					+ " channel name) and message should be a byte_array primitive. Depending on the plugin, these parameters"
					+ " will vary. If message is null an empty byte_array is sent.";
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
		
	}
	
	@api
	public static class send_plugin_message extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CREPlayerOfflineException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {

			MCPlayer p = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
			int offset = 0;
			if(args.length == 3){
				offset = 1;
				p = Static.GetPlayer(args[0], t);
			}
			String channel = args[0 + offset].val();
			CByteArray ba = Static.getByteArray(args[1 + offset], t);
			Static.AssertPlayerNonNull(p, t);
			p.sendPluginMessage(channel, ba.asByteArrayCopy());
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "send_plugin_message";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2, 3};
		}

		@Override
		public String docs() {
			return "void {[player,] channel, message} Sends a plugin message to the player. Channel should be a string (the"
					+ " channel name) and message should be a byte_array primitive. Depending on the plugin, these parameters"
					+ " will vary. If message is null an empty byte_array is sent.";
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
		
	}
	
	@api
	public static class register_channel extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREPluginChannelException.class, CRENotFoundException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			MCMessenger messenger = Static.getServer().getMessenger();
			if (messenger == null) {
				throw ConfigRuntimeException.BuildException(
						"Could not find the internal Messenger object (are you running in cmdline mode?)",
						CRENotFoundException.class, t);
			}
			String channel = args[0].toString();
			
			if (!messenger.isIncomingChannelRegistered(channel)) {
				messenger.registerIncomingPluginChannel(channel);
			} else {
				throw ConfigRuntimeException.BuildException("The channel '" + channel + "' is already registered.", CREPluginChannelException.class, t);
			}
			
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "register_channel";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "void {channel} Registers a plugin channel for CommandHelper to listen on."
					+ " Incoming messages can be inspected by binding to 'plugin_message_received'.";
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}
	
	@api
	public static class unregister_channel extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREPluginChannelException.class, CRENotFoundException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			MCMessenger messenger = Static.getServer().getMessenger();
			if (messenger == null) {
				throw ConfigRuntimeException.BuildException(
						"Could not find the internal Messenger object (are you running in cmdline mode?)",
						CRENotFoundException.class, t);
			}
			String channel = args[0].toString();
			
			if (messenger.isIncomingChannelRegistered(channel)) {
				messenger.unregisterIncomingPluginChannel(channel);
			} else {
				throw ConfigRuntimeException.BuildException("The channel '" + channel + "' is not registered.", CREPluginChannelException.class, t);
			}
			
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "unregister_channel";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "void {channel} Unregisters a plugin channel CommandHelper is listening on, if any.";
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}
	
	@api
	public static class is_channel_registered extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRENotFoundException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			MCMessenger messenger = Static.getServer().getMessenger();
			if (messenger == null) {
				throw ConfigRuntimeException.BuildException(
						"Could not find the internal Messenger object (are you running in cmdline mode?)",
						CRENotFoundException.class, t);
			}
			return CBoolean.get(messenger.isIncomingChannelRegistered(args[0].toString()));
		}

		@Override
		public String getName() {
			return "is_channel_registered";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "boolean {channel} Returns true if commandhelper is listening to"
					+ " the given plugin channel.";
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}
	
	@api
	public static class get_registered_channels extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRENotFoundException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			MCMessenger messenger = Static.getServer().getMessenger();
			if (messenger == null) {
				throw ConfigRuntimeException.BuildException(
						"Could not find the internal Messenger object (are you running in cmdline mode?)",
						CRENotFoundException.class, t);
			}
			Set<String> chans = messenger.getIncomingChannels();
			CArray arr = new CArray(t);
			
			for (String chan : chans) {
				arr.push(new CString(chan, t), t);
			}
			
			return arr;
		}

		@Override
		public String getName() {
			return "get_registered_channels";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0};
		}

		@Override
		public String docs() {
			return "array {} Returns an array of strings containing the channels"
					+ " CommandHelper is listening on.";
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}
}
