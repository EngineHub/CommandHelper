package com.laytonsmith.core.functions;

import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.MCPluginMeta;
import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.abstraction.pluginmessages.MCMessenger;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.arguments.Argument;
import com.laytonsmith.core.arguments.ArgumentBuilder;
import com.laytonsmith.core.arguments.Generic;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CBoolean;
import com.laytonsmith.core.constructs.CByteArray;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.CVoid;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.CommandHelperEnvironment;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;
import java.util.Set;

/**
 *
 * @author lsmith
 */
public class PluginMeta {
	public static String docs(){
		return "This class contains the functions use to communicate with other plugins and the server in general.";
	}
	
	@api
	public static class fake_incoming_plugin_message extends AbstractFunction {

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException, ExceptionType.PlayerOfflineException};
		}

		public boolean isRestricted() {
			return true;
		}

		public Boolean runAsync() {
			return false;
		}

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			MCPluginMeta meta = StaticLayer.GetConvertor().GetPluginMeta();
			MCPlayer p = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
			int offset = 0;
			if(args.length == 3){
				offset = 1;
				p = Static.GetPlayer(args[0], t);
			}
			String channel = args[0 + offset].val();
			CByteArray ba = (CByteArray)args[1 + offset];
			Static.AssertPlayerNonNull(p, t);
			meta.fakeIncomingMessage(p, channel, ba.asByteArrayCopy());
			return new CVoid(t);
		}

		public String getName() {
			return "fake_incoming_plugin_message";
		}

		public Integer[] numArgs() {
			return new Integer[]{1, 2, 3};
		}

		public String docs() {
			return "void {[player,] channel, message} Fakes an incoming plugin message from the player. Channel should be a string (the"
					+ " channel name) and message should be a byte_array primitive. Depending on the plugin, these parameters"
					+ " will vary. If message is null an empty byte_array is sent.";
		}
		
		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("", CString.class, "player").setOptionalDefaultNull(),
						new Argument("", CString.class, "channel"),
						new Argument("", CByteArray.class, "message")
					);
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
		
	}
	
	@api
	public static class send_plugin_message extends AbstractFunction {

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException, ExceptionType.PlayerOfflineException};
		}

		public boolean isRestricted() {
			return true;
		}

		public Boolean runAsync() {
			return false;
		}

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {

			MCPlayer p = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
			int offset = 0;
			if(args.length == 3){
				offset = 1;
				p = Static.GetPlayer(args[0], t);
			}
			String channel = args[0 + offset].val();
			CByteArray ba = (CByteArray)args[1 + offset];
			Static.AssertPlayerNonNull(p, t);
			p.sendPluginMessage(channel, ba.asByteArrayCopy());
			return new CVoid(t);
		}

		public String getName() {
			return "send_plugin_message";
		}

		public Integer[] numArgs() {
			return new Integer[]{2, 3};
		}

		public String docs() {
			return "Sends a plugin message to the player. Channel should be a string (the"
					+ " channel name) and message should be a byte_array primitive. Depending on the plugin, these parameters"
					+ " will vary. If message is null an empty byte_array is sent.";
		}
		
		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("", CString.class, "player").setOptionalDefaultNull(),
						new Argument("", CString.class, "channel"),
						new Argument("", CByteArray.class, "message")
					);
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
		
	}
	
	@api
	public static class register_channel extends AbstractFunction {

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.PluginChannelException};
		}

		public boolean isRestricted() {
			return true;
		}

		public Boolean runAsync() {
			return false;
		}

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			MCMessenger msgr = Static.getServer().getMessenger();
			String channel = args[0].toString();
			
			if (!msgr.isIncomingChannelRegistered(channel)) {
				msgr.registerIncomingPluginChannel(channel);
			} else {
				throw new ConfigRuntimeException("The channel '" + channel + "' is already registered.", ExceptionType.PluginChannelException, t);
			}
			
			return new CVoid(t);
		}

		public String getName() {
			return "register_channel";
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		public String docs() {
			return "Registers a plugin channel for CommandHelper to listen on."
					+ " Incoming messages can be inspected by binding to 'plugin_message_received'.";
		}
		
		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("", CString.class, "channel")
					);
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}
	
	@api
	public static class unregister_channel extends AbstractFunction {

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.PluginChannelException};
		}

		public boolean isRestricted() {
			return true;
		}

		public Boolean runAsync() {
			return false;
		}

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			MCMessenger msgr = Static.getServer().getMessenger();
			String channel = args[0].toString();
			
			if (msgr.isIncomingChannelRegistered(channel)) {
				msgr.unregisterIncomingPluginChannel(channel);
			} else {
				throw new ConfigRuntimeException("The channel '" + channel + "' is not registered.", ExceptionType.PluginChannelException, t);
			}
			
			return new CVoid(t);
		}

		public String getName() {
			return "unregister_channel";
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		public String docs() {
			return "Unregisters a plugin channel CommandHelper is listening on, if any.";
		}
		
		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("", CString.class, "channel")
					);
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}
	
	@api
	public static class is_channel_registered extends AbstractFunction {

		public ExceptionType[] thrown() {
			return new ExceptionType[]{};
		}

		public boolean isRestricted() {
			return true;
		}

		public Boolean runAsync() {
			return false;
		}

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			boolean bool = Static.getServer().getMessenger().isIncomingChannelRegistered(args[0].toString());
			
			return new CBoolean(bool, t);
		}

		public String getName() {
			return "is_channel_registered";
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		public String docs() {
			return "Returns true if commandhelper is listening to"
					+ " the given plugin channel.";
		}
		
		public Argument returnType() {
			return new Argument("", CBoolean.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("", CString.class, "channel")
					);
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}
	
	@api
	public static class get_registered_channels extends AbstractFunction {

		public ExceptionType[] thrown() {
			return new ExceptionType[]{};
		}

		public boolean isRestricted() {
			return true;
		}

		public Boolean runAsync() {
			return false;
		}

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			Set<String> chans = Static.getServer().getMessenger().getIncomingChannels();
			CArray arr = new CArray(t);
			
			for (String chan : chans) {
				arr.push(new CString(chan, t));
			}
			
			return arr;
		}

		public String getName() {
			return "get_registered_channels";
		}

		public Integer[] numArgs() {
			return new Integer[]{0};
		}

		public String docs() {
			return "Returns an array of strings containing the channels"
					+ " CommandHelper is listening on.";
		}
		
		public Argument returnType() {
			return new Argument("", CArray.class).setGenerics(new Generic(CString.class));
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.NONE;
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}
}
