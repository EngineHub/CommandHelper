package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.Common.StreamUtils;
import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.PureUtilities.TermColors;
import com.laytonsmith.abstraction.MCCommandSender;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.MCServer;
import com.laytonsmith.abstraction.enums.MCChatColor;
import com.laytonsmith.annotations.api;
import com.laytonsmith.annotations.noboilerplate;
import com.laytonsmith.annotations.seealso;
import com.laytonsmith.core.ArgumentValidation;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.Optimizable;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CBoolean;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.CVoid;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.CommandHelperEnvironment;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.GlobalEnv;
import com.laytonsmith.core.exceptions.CRE.CRECastException;
import com.laytonsmith.core.exceptions.CRE.CREFormatException;
import com.laytonsmith.core.exceptions.CRE.CREInsufficientArgumentsException;
import com.laytonsmith.core.exceptions.CRE.CRELengthException;
import com.laytonsmith.core.exceptions.CRE.CREPlayerOfflineException;
import com.laytonsmith.core.exceptions.CRE.CRERangeException;
import com.laytonsmith.core.exceptions.CRE.CREThrowable;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.natives.interfaces.Mixed;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Echoes {

	public static String docs() {
		return "These functions allow you to echo information to the screen";
	}

	//Technically it needs CommandHelperEnvironment, but we have special exception handling in case we're running
	//in cmdline mode.
	@api
	@noboilerplate
	public static class msg extends AbstractFunction {

		@Override
		public String getName() {
			return "msg";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		@Override
		public Mixed exec(final Target t, Environment env, final Mixed... args) throws ConfigRuntimeException {
			StringBuilder b = new StringBuilder();
			for(Mixed arg : args) {
				b.append(arg.val());
			}
			if(env.hasEnv(CommandHelperEnvironment.class)) {
				final MCCommandSender p = env.getEnv(CommandHelperEnvironment.class).GetCommandSender();
				Static.SendMessage(p, b.toString(), t);
			} else {
				String mes = Static.MCToANSIColors(b.toString());
				if(mes.contains("\033")) {
					//We have terminal colors, we need to reset them at the end
					mes += TermColors.reset();
				}
				StreamUtils.GetSystemOut().println(mes);
				StreamUtils.GetSystemOut().flush();
			}
			return CVoid.VOID;
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREPlayerOfflineException.class};
		}

		@Override
		public String docs() {
			return "void {var1, [var2...]} Echoes a message to the player running the command";
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_0_1;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public CClassType getReturnType(Target t, List<CClassType> argTypes, List<Target> argTargets, Environment env, Set<ConfigCompileException> exceptions) {
			return CVoid.TYPE;
		}

	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class tmsg extends AbstractFunction {

		@Override
		public String getName() {
			return "tmsg";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			if(args.length < 2) {
				throw new CREInsufficientArgumentsException("You must send at least 2 arguments to tmsg", t);
			}
			MCCommandSender p;
			if(Static.getConsoleName().equals(args[0].val())) {
				p = Static.getServer().getConsole();
			} else {
				p = Static.GetPlayer(args[0], t);
			}
			StringBuilder b = new StringBuilder();
			for(int i = 1; i < args.length; i++) {
				b.append(args[i].val());
			}
			Static.SendMessage(p, b.toString(), t);
			return CVoid.VOID;
		}

		@Override
		public String docs() {
			return "void {player, msg, [...]} Displays a message on the specified players screen, similar to msg, but"
					+ " targets a specific user.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREPlayerOfflineException.class, CREInsufficientArgumentsException.class,
				CRELengthException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_0_1;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class tellraw extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class};
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
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			String selector = "@a";
			String json;
			if(args.length == 1) {
				json = new DataTransformations.json_encode().exec(t, environment, args[0]).val();
			} else {
				selector = ArgumentValidation.getString(args[0], t);
				json = new DataTransformations.json_encode().exec(t, environment, args[1]).val();
			}
			Static.getServer().runasConsole("minecraft:tellraw " + selector + " " + json);
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "tellraw";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		@Override
		public String docs() {
			return "void {[string selector], array raw} A thin wrapper around the tellraw command from console context,"
					+ " this simply passes the input to the command. The raw is passed in as a normal"
					+ " (possibly associative) array, and json encoded. No validation is done on the input, so the"
					+ " command may fail. If not provided, the selector defaults to @a. Do not use double quotes"
					+ " (smart string) when providing the selector. See {{function|ptellraw}} if you need player"
					+ " context. ---- The specification of the array may change from version to version of Minecraft,"
					+ " but is documented here https://minecraft.gamepedia.com/Commands#Raw_JSON_text."
					+ " This function is simply written in terms of json_encode and runas, and is otherwise equivalent"
					+ " to runas('~console', '/minecraft:tellraw ' . @selector . ' ' . json_encode(@raw))";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_4;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[] {
				new ExampleScript("Simple usage with a plain message",
						"tellraw(array('text': 'Hello World!'));",
						"<<Would output the plain message to all players.>>")
			};
		}

	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class title extends AbstractFunction {

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_2;
		}

		@Override
		public String getName() {
			return "title";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2, 3, 5, 6};
		}

		@Override
		public String docs() {
			return "void {[player], title, subtitle, [fadein, stay, fadeout]} Shows a title and/or subtitle to a player."
					+ " The title and subtitle parameters can be null. The integers fadein, stay, and fadeout define the"
					+ " time in ticks that the title will be displayed. The defaults are 10, 70, and 20 respectively.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREPlayerOfflineException.class, CRECastException.class, CRERangeException.class,
				CRELengthException.class};
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
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCPlayer player;
			int fadein = 10;
			int stay = 70;
			int fadeout = 20;
			int offset = 0;

			if(args.length == 3 || args.length == 6) {
				player = Static.GetPlayer(args[0].val(), t);
				offset = 1;
			} else {
				player = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
				Static.AssertPlayerNonNull(player, t);
			}

			if(args.length > 3) {
				fadein = ArgumentValidation.getInt32(args[2 + offset], t);
				stay = ArgumentValidation.getInt32(args[3 + offset], t);
				fadeout = ArgumentValidation.getInt32(args[4 + offset], t);
			}

			player.sendTitle(Construct.nval(args[offset]), Construct.nval(args[1 + offset]), fadein, stay, fadeout);
			return CVoid.VOID;
		}
	}

	@api
	@seealso({colorize.class})
	public static class color extends AbstractFunction implements Optimizable {

		private static final Map<String, CString> CACHED_COLORS = new HashMap<>();

		@Override
		public String getName() {
			return "color";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			String val = Construct.nval(args[0]);
			if(val == null) {
				return new CString(MCChatColor.WHITE.toString(), t);
			}
			CString cachedColor = CACHED_COLORS.get(val);
			if(cachedColor != null) {
				return cachedColor;
			}
			String color = null;
			try {
				color = MCChatColor.valueOf(val.toUpperCase()).toString();
			} catch (IllegalArgumentException e) {
				if("".equals(val)) {
					color = MCChatColor.WHITE.toString();

				} else if(val.charAt(0) == '#') {
					// Hex color codes
					color = MCChatColor.fromRGBValue(val);

				} else {
					// Simple color codes
					String c = val.toLowerCase();
					// Translate common alternatives to conventional color codes here
					switch(c) {
						case "10":
							c = "a";
							break;
						case "11":
							c = "b";
							break;
						case "12":
							c = "c";
							break;
						case "13":
							c = "d";
							break;
						case "14":
							c = "e";
							break;
						case "strike":
							c = "m";
							break;
						case "underlined":
							c = "n";
							break;
						case "italics":
							c = "o";
							break;
					}
					MCChatColor cc = MCChatColor.getByChar(c.charAt(0));
					if(cc != null) {
						color = cc.toString();
					}
				}
				if(color == null) {
					color = MCChatColor.WHITE.toString();
				}
			}
			CString ret = new CString(color, t);
			CACHED_COLORS.put(val, ret);
			return ret;
		}

		@Override
		public String docs() {
			MCChatColor[] values = MCChatColor.values();
			String[] colors = new String[values.length];
			for(int i = 0; i < colors.length; i++) {
				colors[i] = values[i].name();
			}
			return "string {name} Returns the color (or style) code modifier for a given value."
					+ " If the value isn't valid, white is used instead."
					+ " The list of valid color names is: " + StringUtils.Join(colors, ", ", ", or ") + "."
					+ " Other supported values include the color integers 0-15, the color hex numbers 0-F,"
					+ " and the style values k, l, m, n, o, and r."
					+ " Additionally, any RGB color can be used in the hex format '#rrggbb' (except in tellraw)."
					+ " ---- Since the vanilla Minecraft client does not support RGB color codes, these are translated"
					+ " to json text components by the server when passed to messaging functions. As such, these are"
					+ " not directly supported in tellraw functions or commands. Instead, the color must be manually"
					+ " specified under the 'color' key of the json text component.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_0_1;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
					OptimizationOption.CONSTANT_OFFLINE,
					OptimizationOption.CACHE_RETURN
			);
		}

	}

	@api
	public static class strip_colors extends AbstractFunction {

		@Override
		public String getName() {
			return "strip_colors";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "string {toStrip} Strips all the color codes from a given string";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_0;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			return new CString(MCChatColor.stripColor(args[0].val()), t);
		}

	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class chat extends AbstractFunction {

		@Override
		public String getName() {
			return "chat";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public Mixed exec(final Target t, final Environment env, Mixed... args) throws ConfigRuntimeException {
			MCPlayer p = env.getEnv(CommandHelperEnvironment.class).GetPlayer();
			if(p != null) {
				p.chat(args[0].val());
			} else {
				throw new CREPlayerOfflineException("Console cannot chat. Use something like broadcast() instead.", t);
			}
			return CVoid.VOID;
		}

		@Override
		public String docs() {
			return "void {string} Echoes string to the chat, as if the user simply typed something into the chat bar."
					+ " This function cannot be run from console, a PlayerOfflineException is thrown if attempted."
					+ " Use broadcast() instead.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREPlayerOfflineException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_0_1;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class chatas extends AbstractFunction {

		@Override
		public String getName() {
			return "chatas";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "void {player, msg} Sends a chat message to the server, as the given player. Otherwise the same as"
					+ " the chat() function";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREPlayerOfflineException.class, CRELengthException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_0_2;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			final MCPlayer player = Static.GetPlayer(args[0], t);
			player.chat(args[1].val());
			return CVoid.VOID;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class broadcast extends AbstractFunction {

		@Override
		public String getName() {
			return "broadcast";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		@Override
		public String docs() {
			return "void {message, [permission] | message, [recipients]} Broadcasts a message to all or some players"
					+ " and/or console."
					+ " If permission is given, only players with that permission and console will see the broadcast."
					+ " If an array of recipients is given, only online players in the list will see the broadcast."
					+ " Console will receive the broadcast only when the array contains case-insensitive '~console'."
					+ " Offline players and duplicate recipients in the list will be ignored."
					+ " If permission/recipients is null, all players and console will see the broadcast."
					+ " Throws FormatException when the given recipients array is associative.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREFormatException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_0_1;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			final MCServer server = Static.getServer();

			// Handle "broadcast(message, [null])".
			if(args.length == 1 || Construct.nval(args[1]) == null) { // args.length can only be 1 or 2 due to the numArgs().
				server.broadcastMessage(args[0].val());
				return CVoid.VOID;
			}

			// Handle "broadcast(message, recipientsArray)".
			if(args[1].isInstanceOf(CArray.TYPE)) {

				// Get the CArray and validate that it is non-associative.
				CArray array = (CArray) args[1];
				if(array.isAssociative()) {
					throw new CREFormatException(
							"Expected a non-associative array or permission as the second parameter.", t);
				}

				// Get the recipients from the array.
				Set<MCCommandSender> recipients = new HashSet<>();
				for(Mixed p : array.asList()) {
					if(p.val().equalsIgnoreCase("~console")) {
						recipients.add(server.getConsole());
					} else {
						try {
							recipients.add(Static.GetPlayer(p, t));
						} catch (CREPlayerOfflineException cre) {
							// Ignore offline players.
						}
					}
				}

				// Perform the broadcast and return cvoid.
				server.broadcastMessage(args[0].val(), recipients);
				return CVoid.VOID;
			}

			// Handle "broadcast(message, permission)".
			String permission = Construct.nval(args[1]);
			server.broadcastMessage(args[0].val(), permission);
			return CVoid.VOID;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

	}

	@api
	@noboilerplate
	public static class console extends AbstractFunction {

		@Override
		public String getName() {
			return "console";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		@Override
		public String docs() {
			return "void {message, [prefix]} Logs a message to the console. If prefix is true, prepends "
					+ "\"CommandHelper:\""
					+ " to the message. Default is true. If you wish to set the default value of prefix to false,"
					+ " use set_runtime_setting('function.console.prefix_default', false).";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_0_2;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			String mes = Static.MCToANSIColors(args[0].val());
			boolean prefix = ArgumentValidation.getBooleanish(env.getEnv(GlobalEnv.class)
					.GetRuntimeSetting("function.console.prefix_default", CBoolean.TRUE), t);
			if(args.length > 1) {
				prefix = ArgumentValidation.getBoolean(args[1], t);
			}

			if(prefix) {
				mes = "CommandHelper: " + mes;
			}
			if(mes.contains("\033")) {
				//We have terminal colors, we need to reset them at the end
				mes += TermColors.reset();
			}
			StreamUtils.GetSystemOut().println(mes);
			return CVoid.VOID;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

	}

	@api
	@seealso({color.class})
	public static class colorize extends AbstractFunction implements Optimizable {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return null;
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			Mixed text = args[0];
			String symbol = "&";
			if(args.length == 2) {
				symbol = args[1].val();
			}
			if(!(text.isInstanceOf(CString.TYPE))) {
				return text;
			}
			String stext = text.val();
			StringBuilder b = new StringBuilder();
			int sl = symbol.length();
			for(int i = 0; i < stext.length(); i++) {
				if(i + sl >= stext.length()) {
					b.append(stext.substring(i));
					break;
				}
				String subsequence1 = stext.substring(i, i + sl);
				if(!symbol.equals(subsequence1)) {
					b.append(stext.charAt(i));
					continue;
				}
				try {
					String subsequence2 = stext.substring(i + sl, i + (sl * 2));
					if(subsequence2.equals(subsequence1)) {
						b.append(subsequence1);
						i += (sl * 2) - 1;
						continue;
					}
				} catch (IndexOutOfBoundsException e) {
					//Ignored, it just means there aren't enough characters to do a second subsequence
				}
				Character c;
				try {
					c = stext.charAt(i + sl);
				} catch (IndexOutOfBoundsException e) {
					b.append(stext.charAt(i + sl - 1));
					break;
				}
				MCChatColor color = MCChatColor.getByChar(Character.toLowerCase(c));
				if(color != null) {
					b.append(color);
					i += sl;
					continue;
				}
				if(c.equals('#')) {
					try {
						String subsequence2 = stext.substring(i + sl, i + sl + 7);
						String rgbColor = MCChatColor.fromRGBValue(subsequence2);
						if(rgbColor != null) {
							b.append(rgbColor);
							i += sl + 6;
							continue;
						}
					} catch (IndexOutOfBoundsException e) {
						// Not enough characters left for a full hex code
					}
				}
				b.append(subsequence1);
				i += sl - 1;
			}
			return new CString(b.toString(), t);
		}

		@Override
		public String getName() {
			return "colorize";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		@Override
		public String docs() {
			return "mixed {text, [symbol]} Replaces all the colorizable character codes in the string."
					+ " For instance, colorize('&aText') would be equivalent to (color('a').'Text')."
					+ " By default, the symbol is '&', but that can be any arbitrary string that you specify."
					+ " If text is not a string, that value is simply returned. If you need to \"escape\" a symbol,"
					+ " (that is have a literal symbol followed by a letter that is a valid color) just repeat the"
					+ " symbol twice, for instance '&&c' would return a literal '&c' instead of a red modifier."
					+ " Additionally, any RGB color can be used in the hex format '&#rrggbb' (except in tellraw)."
					+ " ---- Since the vanilla Minecraft client does not support RGB color codes, these are translated"
					+ " to json text components by the server when passed to messaging functions. As such, these are"
					+ " not directly supported in tellraw functions or commands. Instead, the color must be manually"
					+ " specified under the 'color' key of the json text component.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(OptimizationOption.CONSTANT_OFFLINE);
		}

	}
}
