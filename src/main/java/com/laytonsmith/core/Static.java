package com.laytonsmith.core;

import com.laytonsmith.PureUtilities.Common.DateUtils;
import com.laytonsmith.PureUtilities.SimpleVersion;
import com.laytonsmith.PureUtilities.TermColors;
import com.laytonsmith.PureUtilities.XMLDocument;
import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.MCCommandSender;
import com.laytonsmith.abstraction.MCConsoleCommandSender;
import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCLivingEntity;
import com.laytonsmith.abstraction.MCMetadatable;
import com.laytonsmith.abstraction.MCOfflinePlayer;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.MCPlugin;
import com.laytonsmith.abstraction.MCServer;
import com.laytonsmith.abstraction.MCVehicle;
import com.laytonsmith.abstraction.MCWorld;
import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.commandhelper.CommandHelperPlugin;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CBoolean;
import com.laytonsmith.core.constructs.CByteArray;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.CDouble;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.constructs.CNull;
import com.laytonsmith.core.constructs.CResource;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.CVoid;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.NativeTypeList;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.constructs.Variable;
import com.laytonsmith.core.environments.CommandHelperEnvironment;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.GlobalEnv;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;
import com.laytonsmith.core.functions.Function;
import com.laytonsmith.core.profiler.Profiler;
import com.laytonsmith.core.taskmanager.TaskManager;
import com.laytonsmith.persistence.DataSourceException;
import com.laytonsmith.persistence.PersistenceNetwork;
import com.laytonsmith.persistence.io.ConnectionMixinFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Array;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class contains several static methods to get various objects that really
 * should be static in the first place, but aren't. For the most part, when any
 * code is running, these things will have been initialized, but in the event
 * they aren't, each function will throw a NotInitializedYetException, which is
 * a RuntimeException, so you don't have to check for exceptions whenever you
 * use them. The Exception is caught on a higher level though, so it shouldn't
 * bubble up too far.
 *
 */
public final class Static {

	private Static() {
	}

	private static final Logger logger = Logger.getLogger("CommandHelper");

	private static Map<String, String> hostCache = new HashMap<String, String>();

	private static final String consoleName = "~console";

	private static final String blockPrefix = "#"; // Chosen over @ because that does special things when used by the block

	/**
	 * In case the API being used doesn't support permission groups, a permission node in the format
	 * <code>String permission = groupPrefix + groupName;</code>
	 * can be assigned to players to declare their permission group.
	 *
	 * Third party APIs may provide better access.
	 */
	public static final String groupPrefix = "group.";

	/**
	 * The label representing unrestricted access.
	 */
	public static final String GLOBAL_PERMISSION = "*";

	/**
	 * Returns a CArray object from a given construct, throwing a common error
	 * message if not.
	 *
	 * @param construct
	 * @param t
	 * @return
	 */
	public static CArray getArray(Construct construct, Target t) {
		return ArgumentValidation.getArray(construct, t);
	}

	/**
	 * Works like the other get* methods, but works in a more generic way for
	 * other types of Constructs.
	 *
	 * @param <T> The type expected.
	 * @param construct The generic object
	 * @param t Code target
	 * @param expectedClassName The expected class type, for use in the error
	 * message if the construct is the wrong type.
	 * @param clazz The type expected.
	 * @return The properly cast object.
	 * @deprecated Use
	 * {@link #getObject(com.laytonsmith.core.constructs.Construct, com.laytonsmith.core.constructs.Target, java.lang.Class)}
	 * instead, as that gets the expected class name automatically.
	 */
	@Deprecated
	public static <T extends Construct> T getObject(Construct construct, Target t, String expectedClassName, Class<T> clazz) {
		return ArgumentValidation.getObject(construct, t, expectedClassName, clazz);
	}

	/**
	 * Works like the other get* methods, but works in a more generic way for
	 * other types of Constructs. It also assumes that the class specified is
	 * tagged with a typeof annotation, thereby preventing the need for the
	 * expectedClassName like the deprecated version uses.
	 *
	 * @param <T> The type expected.
	 * @param construct The generic object
	 * @param t Code target
	 * @param clazz The type expected.
	 * @return The properly cast object.
	 */
	public static <T extends Construct> T getObject(Construct construct, Target t, Class<T> clazz) {
		return ArgumentValidation.getObject(construct, t, clazz);
	}

	/**
	 * This function pulls a numerical equivalent from any given construct. It
	 * throws a ConfigRuntimeException if it cannot be converted, for instance
	 * the string "s" cannot be cast to a number. The number returned will
	 * always be a double.
	 *
	 * @param c
	 * @return
	 */
	public static double getNumber(Construct c, Target t) {
		return ArgumentValidation.getNumber(c, t);
	}

	/**
	 * Alias to getNumber
	 *
	 * @param c
	 * @return
	 */
	public static double getDouble(Construct c, Target t) {
		return ArgumentValidation.getDouble(c, t);
	}

	public static float getDouble32(Construct c, Target t) {
		return ArgumentValidation.getDouble32(c, t);
	}

	/**
	 * Returns an integer from any given construct.
	 *
	 * @param c
	 * @return
	 */
	public static long getInt(Construct c, Target t) {
		return ArgumentValidation.getInt(c, t);
	}

	/**
	 * Returns a 32 bit int from the construct. Since the backing value is
	 * actually a long, if the number contained in the construct is not the same
	 * after truncating, an exception is thrown (fail fast). When needing an int
	 * from a construct, this method is much preferred over silently truncating.
	 *
	 * @param c
	 * @param t
	 * @return
	 */
	public static int getInt32(Construct c, Target t) {
		return ArgumentValidation.getInt32(c, t);
	}

	/**
	 * Returns a 16 bit int from the construct (a short). Since the backing
	 * value is actually a long, if the number contained in the construct is not
	 * the same after truncating, an exception is thrown (fail fast). When
	 * needing an short from a construct, this method is much preferred over
	 * silently truncating.
	 *
	 * @param c
	 * @param t
	 * @return
	 */
	public static short getInt16(Construct c, Target t) {
		return ArgumentValidation.getInt16(c, t);
	}

	/**
	 * Returns an 8 bit int from the construct (a byte). Since the backing value
	 * is actually a long, if the number contained in the construct is not the
	 * same after truncating, an exception is thrown (fail fast). When needing a
	 * byte from a construct, this method is much preferred over silently
	 * truncating.
	 *
	 * @param c
	 * @param t
	 * @return
	 */
	public static byte getInt8(Construct c, Target t) {
		return ArgumentValidation.getInt8(c, t);
	}

	/**
	 * Returns a boolean from any given construct. Depending on the type of the
	 * construct being converted, it follows the following rules: If it is an
	 * integer or a double, it is false if 0, true otherwise. If it is a string,
	 * if it is empty, it is false, otherwise it is true.
	 *
	 * @param c
	 * @return
	 */
	public static boolean getBoolean(Construct c) {
		return ArgumentValidation.getBoolean(c, Target.UNKNOWN);
	}

	public static CByteArray getByteArray(Construct c, Target t) {
		return ArgumentValidation.getByteArray(c, t);
	}

	/**
	 * Returns true if any of the constructs are a CDouble, false otherwise.
	 *
	 * @param c
	 * @return
	 */
	public static boolean anyDoubles(Construct... c) {
		return ArgumentValidation.anyDoubles(c);
	}

	/**
	 * Return true if any of the constructs are CStrings, false otherwise.
	 *
	 * @param c
	 * @return
	 */
	public static boolean anyStrings(Construct... c) {
		return ArgumentValidation.anyStrings(c);
	}

	/**
	 * Returns true if any of the constructs are CBooleans, false otherwise.
	 *
	 * @param c
	 * @return
	 */
	public static boolean anyBooleans(Construct... c) {
		return ArgumentValidation.anyBooleans(c);
	}

	/**
	 * Returns true if any of the constructs are null.
	 * @param c
	 * @return
	 */
	public static boolean anyNulls(Construct... c){
		return ArgumentValidation.anyNulls(c);
	}

	/**
	 * Returns the logger for the plugin
	 *
	 * @return
	 */
	public static Logger getLogger() {
		return logger;
	}

	/**
	 * Returns the server for this plugin
	 *
	 * @return
	 * @throws NotInitializedYetException
	 */
	public static MCServer getServer() throws NotInitializedYetException {
		MCServer s = com.laytonsmith.commandhelper.CommandHelperPlugin.myServer;
		if (s == null) {
			throw new NotInitializedYetException("The server has not been initialized yet");
		}
		return s;
	}

	/**
	 * Gets the reference to the AliasCore for this plugin
	 *
	 * @return
	 * @throws NotInitializedYetException
	 */
	public static AliasCore getAliasCore() throws NotInitializedYetException {
		AliasCore ac = com.laytonsmith.commandhelper.CommandHelperPlugin.getCore();
		if (ac == null) {
			throw new NotInitializedYetException("The core has not been initialized yet");
		}
		return ac;
	}

	/**
	 * Gets the current version of the plugin
	 *
	 * @return
	 * @throws NotInitializedYetException
	 */
	public static SimpleVersion getVersion() throws NotInitializedYetException {
		SimpleVersion v = com.laytonsmith.commandhelper.CommandHelperPlugin.version;
		if (v == null) {
			throw new NotInitializedYetException("The plugin has not been initialized yet");
		}
		return v;
	}

	private static String debugLogFileCurrent = null;
	private static FileWriter debugLogFileHandle = null;

	/**
	 * Returns a file that is most likely ready to write to. The timestamp
	 * variables have already been replaced, and parent directories are all
	 * created.
	 *
	 * @return
	 */
	public static FileWriter debugLogFile(File root) throws IOException {
		String currentFileName = root.getPath() + "/" + DateUtils.ParseCalendarNotation(Prefs.DebugLogFile());
		if (!currentFileName.equals(debugLogFileCurrent)) {
			if (debugLogFileHandle != null) {
				//We're done with the old one, close it.
				debugLogFileHandle.close();
			}
			debugLogFileCurrent = currentFileName;
			new File(debugLogFileCurrent).getParentFile().mkdirs();
			if (!new File(debugLogFileCurrent).exists()) {
				new File(debugLogFileCurrent).createNewFile();
			}
			debugLogFileHandle = new FileWriter(currentFileName);
		}
		return debugLogFileHandle;
	}
	private static String standardLogFileCurrent = null;
	private static FileWriter standardLogFileHandle = null;

	public static FileWriter standardLogFile(File root) throws IOException {
		String currentFileName = root.getPath() + DateUtils.ParseCalendarNotation(Prefs.StandardLogFile());
		if (!currentFileName.equals(standardLogFileCurrent)) {
			if (standardLogFileHandle != null) {
				//We're done with the old one, close it.
				standardLogFileHandle.close();
			}
			standardLogFileCurrent = currentFileName;
			new File(standardLogFileCurrent).getParentFile().mkdirs();
			standardLogFileHandle = new FileWriter(currentFileName);
		}
		return standardLogFileHandle;
	}
	private static String profilingLogFileCurrent = null;
	private static FileWriter profilingLogFileHandle = null;

	public static FileWriter profilingLogFile(File root) throws IOException {
		String currentFileName = root.getPath() + DateUtils.ParseCalendarNotation(Prefs.ProfilingFile());
		if (!currentFileName.equals(profilingLogFileCurrent)) {
			if (profilingLogFileHandle != null) {
				//We're done with the old one, close it.
				profilingLogFileHandle.close();
			}
			profilingLogFileCurrent = currentFileName;
			new File(profilingLogFileCurrent).getParentFile().mkdirs();
			profilingLogFileHandle = new FileWriter(currentFileName);
		}
		return profilingLogFileHandle;
	}

	public static void checkPlugin(String name, Target t) throws ConfigRuntimeException {
		if (Static.getServer().getPluginManager().getPlugin(name) == null) {
			throw new ConfigRuntimeException("Needed plugin " + name + " not found!",
					ExceptionType.InvalidPluginException, t);
		}
	}

	/**
	 * Given a string input, creates and returns a Construct of the appropriate
	 * type. This takes into account that null, true, and false are keywords.
	 *
	 * @param val
	 * @param t
	 * @return
	 * @throws ConfigRuntimeException If the value is a hex or binary value, but
	 * has invalid characters in it.
	 */
	public static Construct resolveConstruct(String val, Target t) throws ConfigRuntimeException {
		if (val == null) {
			return new CString("", t);
		}
		if(val.equals("true")){
			return CBoolean.TRUE;
		}
		if(val.equals("false")){
			return CBoolean.FALSE;
		}
		if(val.equals("null")){
			return CNull.NULL;
		}
		if(val.equals("void")){
			return CClassType.VOID;
		}
		if (val.matches("0x[a-fA-F0-9]*[^a-fA-F0-9]+[a-fA-F0-9]*")) {
			throw new ConfigRuntimeException("Hex numbers must only contain digits 0-9, and the letters A-F, but \"" + val + "\" was found.",
					ExceptionType.FormatException, t);
		}
		if (val.matches("0x[a-fA-F0-9]+")) {
			//Hex number
			return new CInt(Long.parseLong(val.substring(2), 16), t);
		}
		if (val.matches("0b[0-1]*[^0-1]+[0-1]*")) {
			throw new ConfigRuntimeException("Binary numbers must only contain digits 0 and 1, but \"" + val + "\" was found.",
					ExceptionType.FormatException, t);
		}
		if (val.matches("0b[0-1]+")) {
			//Binary number
			return new CInt(Long.parseLong(val.substring(2), 2), t);
		}
		if(val.matches("0o[0-7]*[^0-7]+[0-7]*")){
			throw new ConfigRuntimeException("Octal numbers must only contain digits 0-7, but \"" + val + "\" was found.",
					ExceptionType.FormatException, t);
		}
		if(val.matches("0o[0-7]+")){
			return new CInt(Long.parseLong(val.substring(2), 8), t);
		}
		try {
			return new CInt(Long.parseLong(val), t);
		} catch (NumberFormatException e) {
			try {
				if (!(val.contains(" ") || val.contains("\t"))) {
                        //Interesting behavior in Double.parseDouble causes it to "trim" strings first, then
					//try to parse them, which is not desireable in our case. So, if the value contains
					//any characters other than [\-0-9\.], we want to make it a string instead
					return new CDouble(Double.parseDouble(val), t);
				}
			} catch (NumberFormatException g) {
                    // Not a double either
			}
		}
		// TODO: Once compiler environments are added, we would need to check to see if the value here is a custom
		// type. However, as it stands, since we only support the native types, we will just hardcode the check here.
		if(NativeTypeList.getNativeTypeList().contains(val)){
			return new CClassType(val, t);
		} else {
			return new CString(val, t);
		}
	}

	public static Construct resolveDollarVar(Construct variable, List<Variable> vars) {
		if (variable == null) {
			return CNull.NULL;
		}
		if (variable.getCType() == Construct.ConstructType.VARIABLE) {
			for (Variable var : vars) {
				if (var.getName().equals(((Variable) variable).getName())) {
					return new CString(var.val(), var.getTarget());
				}
			}
			return new CString(((Variable) variable).getDefault(), variable.getTarget());
		} else {
			return variable;
		}
	}

	/**
	 * This function sends a message to the player. If the player is not online,
	 * a CRE is thrown.
	 *
	 * @param m
	 * @param msg
	 */
	public static void SendMessage(final MCCommandSender m, String msg, final Target t) {
		if (m != null) {
			if (m instanceof MCPlayer) {
				MCPlayer p = (MCPlayer) m;
				if (!p.isOnline()) {
					throw new ConfigRuntimeException("The player " + p.getName() + " is not online",
							ExceptionType.PlayerOfflineException, t);
				}
			}
			m.sendMessage(msg);
		} else {
			msg = Static.MCToANSIColors(msg);
			if (msg.matches("(?sm).*\033.*")) {
				//We have terminal colors, we need to reset them at the end
				msg += TermColors.reset();
			}
			System.out.println(msg);
		}
	}

	/**
	 * Works like
	 * {@link #SendMessage(com.laytonsmith.abstraction.MCCommandSender, java.lang.String, com.laytonsmith.core.constructs.Target)}
	 * except it doesn't require a target, and ignores the message if the
	 * command sender is offline.
	 *
	 * @param m
	 * @param msg
	 */
	public static void SendMessage(final MCCommandSender m, String msg) {
		try {
			SendMessage(m, msg, Target.UNKNOWN);
		} catch (ConfigRuntimeException e) {
			//Ignored
		}
	}

	/**
	 * Returns the name set aside to identify console via string<br>
	 * This is done here so that if it ever changes, it will update in all
	 * functions/docs
	 *
	 * @return
	 */
	public static String getConsoleName() {
		return consoleName;
	}

	/**
	 * Returns the string set aside to prefix block names to distinguish them
	 * from players
	 *
	 * @return
	 */
	public static String getBlockPrefix() {
		return blockPrefix;
	}

	/**
	 * Returns an item stack from the given item notation. Defaulting to the
	 * specified qty, this will throw an exception if the notation is invalid.
	 *
	 * @param functionName
	 * @param notation
	 * @param qty
	 * @throws ConfigRuntimeException FormatException if the notation is
	 * invalid.
	 * @return
	 */
	public static MCItemStack ParseItemNotation(String functionName, String notation, int qty, Target t) {
		int type = 0;
		short data = 0;
		MCItemStack is = null;
		if (notation.matches("\\d*:\\d*")) {
			String[] sData = notation.split(":");
			try {
				type = (int) Integer.parseInt(sData[0]);
				if (sData.length > 1) {
					data = (short) Integer.parseInt(sData[1]);
				}
			} catch (NumberFormatException e) {
				throw new ConfigRuntimeException("Item value passed to " + functionName + " is invalid: " + notation, ExceptionType.FormatException, t);
			}
		} else {
			type = Static.getInt32(Static.resolveConstruct(notation, t), t);
		}

		is = StaticLayer.GetItemStack(type, qty);
		is.setDurability(data);
		//is.setData(new MaterialData(type, data));
		return is;
	}

	/**
	 * Works in reverse from the other ParseItemNotation
	 *
	 * @param is
	 * @return
	 */
	public static String ParseItemNotation(MCItemStack is) {
		if (is == null) {
			return "0";
		}
		String append = null;
		if (is.getDurability() != 0) {
			append = Short.toString(is.getDurability());
		} else if (is.getData() != null) {
			append = Integer.toString(is.getData().getData());
		}
		return is.getTypeId() + (append == null ? "" : ":" + append);
	}

	public static String ParseItemNotation(MCBlock b) {
		if (b == null || b.isNull()) {
			return "0";
		}
		return b.getTypeId() + (b.getData() == 0 ? "" : ":" + Byte.toString(b.getData()));
	}

	private static Map<String, MCCommandSender> injectedPlayers = new HashMap<String, MCCommandSender>();
	private static final Pattern DASHLESS_PATTERN = Pattern.compile("^([A-Fa-f0-9]{8})([A-Fa-f0-9]{4})([A-Fa-f0-9]{4})([A-Fa-f0-9]{4})([A-Fa-f0-9]{12})$");

	/**
	 * Based on https://github.com/sk89q/SquirrelID
	 *
	 * @param subject
	 * @param t
	 * @return
	 */
	public static UUID GetUUID(String subject, Target t) {
		try {
			if (subject.length() == 36) {
				return UUID.fromString(subject);
			}
			if (subject.length() == 32) {
				Matcher matcher = DASHLESS_PATTERN.matcher(subject);
				if (!matcher.matches()) {
					throw new IllegalArgumentException("Invalid UUID format.");
				}
				return UUID.fromString(matcher.replaceAll("$1-$2-$3-$4-$5"));
			} else {
				throw new ConfigRuntimeException("A UUID is expected to be 32 or 36 characters,"
						+ " but the given string was " + subject.length() + " characters.",
						ExceptionType.LengthException, t);
			}
		} catch (IllegalArgumentException iae) {
			throw new ConfigRuntimeException("A UUID length string was given, but was not a valid UUID.",
					ExceptionType.IllegalArgumentException, t);
		}
	}

	public static MCOfflinePlayer GetUser(Construct search, Target t) {
		return GetUser(search.val(), t);
	}

	/**
	 * Provides a user object containing info that doesn't require an online player.
	 * If provided a string between 1 and 16 characters, the lookup will be name-based.
	 * If provided a string that is 32 or 36 characters, the lookup will be uuid-based.
	 *
	 * @param search The text to be searched, can be between 1 and 16 characters, or 32 or 36 characters
	 * @param t
	 * @return
	 */
	public static MCOfflinePlayer GetUser(String search, Target t) {
		MCOfflinePlayer ofp;
		if (search.length() > 0 && search.length() <= 16) {
			ofp = getServer().getOfflinePlayer(search);
		} else {
			try {
				ofp = getServer().getOfflinePlayer(GetUUID(search, t));
			} catch (ConfigRuntimeException cre) {
				if (cre.getExceptionType().equals(ExceptionType.LengthException)) {
					throw new ConfigRuntimeException("The given string was the wrong size to identify a player."
							+ " A player name is expected to be between 1 and 16 characters. " + cre.getMessage(),
							ExceptionType.LengthException, t);
				} else {
					throw cre;
				}
			}
		}
		return ofp;
	}

	/**
	 * Returns the player specified by name. Injected players also are returned in this list.
	 * If provided a string between 1 and 16 characters, the lookup will be name-based.
	 * If provided a string that is 32 or 36 characters, the lookup will be uuid-based.
	 *
	 * @param player
	 * @param t
	 * @return
	 * @throws ConfigRuntimeException
	 */
	public static MCPlayer GetPlayer(String player, Target t) throws ConfigRuntimeException {
		MCCommandSender m;
		if (player.length() > 0 && player.length() <= 16) {
			m = GetCommandSender(player, t);
		} else {
			try {
				m = getServer().getPlayer(GetUUID(player, t));
			} catch (ConfigRuntimeException cre) {
				if (cre.getExceptionType().equals(ExceptionType.LengthException)) {
					throw new ConfigRuntimeException("The given string was the wrong size to identify a player."
							+ " A player name is expected to be between 1 and 16 characters. " + cre.getMessage(),
							ExceptionType.LengthException, t);
				} else {
					throw cre;
				}
			}
		}
		if (m == null) {
			throw new ConfigRuntimeException("The specified player (" + player + ") is not online",
					ExceptionType.PlayerOfflineException, t);
		}
		if (!(m instanceof MCPlayer)) {
			throw new ConfigRuntimeException("Expecting a player name, but \"" + player + "\" was found.",
					ExceptionType.PlayerOfflineException, t);
		}
		MCPlayer p = (MCPlayer) m;
		if (!p.isOnline()) {
			throw new ConfigRuntimeException("The specified player (" + player + ") is not online",
					ExceptionType.PlayerOfflineException, t);
		}
		return p;
	}

	/**
	 * Returns the specified command sender. Players are supported, as is the
	 * special ~console user. The special ~console user will always return a
	 * user.
	 *
	 * @param player
	 * @param t
	 * @return
	 * @throws ConfigRuntimeException
	 */
	public static MCCommandSender GetCommandSender(String player, Target t) throws ConfigRuntimeException {
		MCCommandSender m = null;
		if (injectedPlayers.containsKey(player)) {
			m = injectedPlayers.get(player);
		} else {
			if (consoleName.equals(player)) {
				m = Static.getServer().getConsole();
			} else {
				try {
					m = Static.getServer().getPlayer(player);
				} catch (Exception e) {
					//Apparently the server can occasionally throw exceptions here, so instead of rethrowing
					//a NPE or whatever, we'll assume that the player just isn't online, and
					//throw a CRE instead.
				}
			}
		}
		if (m == null || (m instanceof MCPlayer && (!((MCPlayer) m).isOnline() && !injectedPlayers.containsKey(player)))) {
			throw new ConfigRuntimeException("The specified player (" + player + ") is not online", ExceptionType.PlayerOfflineException, t);
		}
		return m;
	}

	public static MCPlayer GetPlayer(Construct player, Target t) throws ConfigRuntimeException {
		return GetPlayer(player.val(), t);
	}

	/**
	 * If the sender is a player, it is returned, otherwise a
	 * ConfigRuntimeException is thrown.
	 *
	 * @param environment
	 * @param t
	 * @return
	 */
	public static MCPlayer getPlayer(Environment environment, Target t) {
		MCPlayer player = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
		if (player != null) {
			return player;
		} else {
			throw new ConfigRuntimeException("The passed arguments induce that the function must be run by a player.", ExceptionType.PlayerOfflineException, t);
		}
	}

	public static boolean isNull(Construct construct) {
		return construct instanceof CNull;
	}

	public static int Normalize(int i, int min, int max) {
		return java.lang.Math.min(max, java.lang.Math.max(min, i));
	}

	/**
	 * Returns the entity with the specified id. If it doesn't exist, a
	 * ConfigRuntimeException is thrown.
	 *
	 * @param id
	 * @return
	 */
	public static MCEntity getEntity(int id, Target t) {
		for (MCWorld w : Static.getServer().getWorlds()) {
			for (MCEntity e : w.getEntities()) {
				if (e.getEntityId() == id) {
					return StaticLayer.GetCorrectEntity(e);
				}
			}
		}
		throw new ConfigRuntimeException("That entity (ID " + id + ") does not exist.", ExceptionType.BadEntityException, t);
	}

	public static MCEntity getEntity(Construct id, Target t) {
		return getEntity(Static.getInt32(id, t), t);
	}

	/**
	 * Returns the entity with the specified unique id. If it doesn't exist, a
	 * ConfigRuntimeException is thrown.
	 *
	 * @param id
	 * @return
	 */
	public static MCEntity getEntityByUuid(UUID id, Target t) {
		for (MCWorld w : Static.getServer().getWorlds()) {
			for (MCEntity e : w.getEntities()) {
				if (e.getUniqueId().compareTo(id) == 0) {
					return StaticLayer.GetCorrectEntity(e);
				}
			}
		}
		throw new ConfigRuntimeException("That entity (UUID " + id + ") does not exist.", ExceptionType.BadEntityException, t);
	}

	/**
	 * Returns the living entity with the specified id. If it doesn't exist or
	 * isn't living, a ConfigRuntimeException is thrown.
	 *
	 * @param id
	 * @return
	 */
	public static MCLivingEntity getLivingEntity(int id, Target t) {
		for (MCWorld w : Static.getServer().getWorlds()) {
			for (MCLivingEntity e : w.getLivingEntities()) {
				if (e.getEntityId() == id) {
					try {
						return (MCLivingEntity) StaticLayer.GetCorrectEntity(e);
					} catch (ClassCastException cce) {
						throw new ConfigRuntimeException("The entity found was misinterpreted by the converter, this is"
								+ " a developer mistake, please file a ticket.", ExceptionType.BadEntityException, t);
					}
				}
			}
		}
		throw new ConfigRuntimeException("That entity (" + id + ") does not exist or is not alive.", ExceptionType.BadEntityException, t);
	}

	/**
	 * Returns all vehicles from all maps.
	 *
	 * @return
	 */
	public static List<MCVehicle> getVehicles() {

		List<MCVehicle> vehicles = new ArrayList<MCVehicle>();

		for (MCWorld w : Static.getServer().getWorlds()) {
			for (MCEntity e : w.getEntities()) {
				MCEntity entity = StaticLayer.GetCorrectEntity(e);
				if (entity instanceof MCVehicle) {
					vehicles.add((MCVehicle) entity);
				}
			}
		}
		return vehicles;
	}

	/**
	 * Returns the world with the specified name. If it does not exist, a
	 * ConfigRuntimeException is thrown.
	 *
	 * @param name
	 * @param t
	 * @return
	 */
	public static MCWorld getWorld(String name, Target t) {
		MCWorld world = getServer().getWorld(name);
		if (world != null) {
			return world;
		} else {
			throw new ConfigRuntimeException("Unknown world:" + name + ".", ExceptionType.InvalidWorldException, t);
		}
	}

	/**
	 * Returns the world with the specified name. If it does not exist, a
	 * ConfigRuntimeException is thrown.
	 *
	 * @param name
	 * @param t
	 * @return
	 */
	public static MCWorld getWorld(Construct name, Target t) {
		return getWorld(name.val(), t);
	}

	/**
	 * Returns the plugin with the specified name. If it does not exist, a
	 * ConfigRuntimeException is thrown.
	 *
	 * @param name
	 * @param t
	 * @return
	 */
	public static MCPlugin getPlugin(String name, Target t) {
		MCPlugin plugin = getServer().getPluginManager().getPlugin(name);
		if (plugin != null) {
			return plugin;
		} else {
			throw new ConfigRuntimeException("Unknown plugin:" + name + ".", ExceptionType.InvalidPluginException, t);
		}
	}

	public static MCPlugin getPlugin(Construct name, Target t) {
		return getPlugin(name.val(), t);
	}

	/**
	 * Returns the metadatable object designated by the given construct. If the
	 * construct is invalid or if the object does not exist, a
	 * ConfigRuntimeException is thrown.
	 *
	 * @param construct
	 * @param t
	 * @return
	 */
	public static MCMetadatable getMetadatable(Construct construct, Target t) {
		if (construct instanceof CInt) {
			return Static.getEntity(construct, t);
		} else if (construct instanceof CArray) {
			return ObjectGenerator.GetGenerator().location(construct, null, t).getBlock();
		} else if (construct instanceof CString) {
			return Static.getWorld(construct, t);
		} else {
			throw new ConfigRuntimeException("An array, an int or a string was expected, but " + construct.val() + " was found.", ExceptionType.CastException, t);
		}
	}

	public static String strJoin(Collection c, String inner) {
		StringBuilder b = new StringBuilder();
		Object[] o = c.toArray();
		for (int i = 0; i < o.length; i++) {
			if (i != 0) {
				b.append(inner);
			}
			b.append(o[i]);
		}
		return b.toString();
	}

	public static String strJoin(Object[] o, String inner) {
		StringBuilder b = new StringBuilder();
		for (int i = 0; i < o.length; i++) {
			if (i != 0) {
				b.append(inner);
			}
			b.append(o[i]);
		}
		return b.toString();
	}

	/**
	 * Returns the system based line seperator character
	 *
	 * @return
	 */
	public static String LF() {
		return System.getProperty("line.separator");
	}

	public static void LogDebug(File root, String message) throws IOException {
		LogDebug(root, message, LogLevel.OFF);
	}

	/**
	 * Equivalent to LogDebug(root, message, level, true);
	 */
	public static synchronized void LogDebug(File root, String message, LogLevel level) throws IOException {
		LogDebug(root, message, level, true);
	}

	/**
	 * Logs an error message, depending on the log level of the message and the
	 * user's preferences.
	 *
	 * @param root
	 * @param message
	 * @param level
	 * @param printScreen If true, the message (if otherwise shown) will be
	 * printed to the screen. If false, it never will be, though it will still
	 * be logged to the log file.
	 * @throws IOException
	 */
	public static synchronized void LogDebug(File root, String message, LogLevel level, boolean printScreen) throws IOException {
		//If debug mode is on in the prefs, we want to log this to the screen too
		if (Prefs.DebugMode() || Prefs.ShowWarnings() || level == LogLevel.ERROR) {
			String color = "";
			Level lev = Level.INFO;
			boolean show = false;
			switch (level) {
				case ERROR:
					color = TermColors.RED;
					lev = Level.SEVERE;
					show = true;
					break;
				case WARNING:
					color = TermColors.YELLOW;
					lev = Level.WARNING;
					if (Prefs.DebugMode() || Prefs.ShowWarnings()) {
						show = true;
					}
					break;
				case INFO:
					color = TermColors.GREEN;
					lev = Level.INFO;
					if (Prefs.DebugMode()) {
						show = true;
					}
					break;
				case DEBUG:
					color = TermColors.BRIGHT_BLUE;
					lev = Level.INFO;
					if (Prefs.DebugMode()) {
						show = true;
					}
					break;
				case VERBOSE:
					color = TermColors.WHITE;
					lev = Level.INFO;
					if (Prefs.DebugMode()) {
						show = true;
					}
					break;
			}
			if (show && printScreen) {
				Static.getLogger().log(lev, "{0}{1}{2}", new Object[]{color, message, TermColors.reset()});
			}
		}
		String timestamp = DateUtils.ParseCalendarNotation("%Y-%M-%D %h:%m.%s - ");
		QuickAppend(Static.debugLogFile(root), timestamp + message + Static.LF());
	}

	public static void QuickAppend(FileWriter f, String message) throws IOException {
		f.append(message);
		f.flush();
	}

	

	public static boolean hasCHPermission(String functionName, Environment env) {
		//The * label completely overrides everything
		if (GLOBAL_PERMISSION.equals(env.getEnv(GlobalEnv.class).GetLabel())) {
			return true;
		}
		MCPlayer player = env.getEnv(CommandHelperEnvironment.class).GetPlayer();
		MCCommandSender commandSender = env.getEnv(CommandHelperEnvironment.class).GetCommandSender();
		String label = env.getEnv(GlobalEnv.class).GetLabel();
		boolean perm = false;
		if (label != null && GLOBAL_PERMISSION.equals(label)) {
			perm = true;
		}
		if (commandSender != null) {
			if (commandSender.isOp()) {
				perm = true;
			} else if (commandSender instanceof MCPlayer) {
				perm = player.hasPermission("ch.func.use." + functionName)
						|| player.hasPermission("commandhelper.func.use." + functionName);
				if (label != null && label.startsWith("~")) {
					String[] groups = label.substring(1).split("/");
					for (String group : groups) {
						if (player.inGroup(group)) {
							perm = true;
							break;
						}
					}
				} else {
					if (label != null) {
						if (label.contains(".")) {
                            //We are using a non-standard permission. Don't automatically
							//add CH's prefix
							if (player.hasPermission(label)) {
								perm = true;
							}
						} else if ((player.hasPermission("ch.alias." + label))
								|| player.hasPermission("commandhelper.alias." + label)) {
							perm = true;
						}
					}
				}
			} else if (commandSender instanceof MCConsoleCommandSender) {
				perm = true;
			}
		} else {
			perm = true;
		}
		return perm;
	}

	public static String Logo() {
		String logo = Installer.parseISToString(Static.class.getResourceAsStream("/mainlogo"));
		logo = logo.replaceAll("( +)", TermColors.BG_BLACK + "$1");
		logo = logo.replaceAll("_", TermColors.BG_RED + TermColors.RED + "_");
		logo = logo.replaceAll("/", TermColors.BG_BRIGHT_WHITE + TermColors.WHITE + "/");
		String s = logo + TermColors.reset();
		return s;
	}

	public static String DataManagerLogo() {
		String logo = Installer.parseISToString(Static.class.getResourceAsStream("/datamanagerlogo"));
		logo = logo.replaceAll("( +)", TermColors.BG_BLACK + "$1");
		logo = logo.replaceAll("_", TermColors.CYAN + TermColors.BG_CYAN + "_");
		logo = logo.replaceAll("/", TermColors.BG_WHITE + TermColors.WHITE + "/");
		String s = logo + TermColors.reset();
		return s;
	}

	public static String GetStringResource(String name) {
		return GetStringResource(Static.class, name);
	}

	public static String GetStringResource(Class path, String name) {
		return Installer.parseISToString(path.getResourceAsStream(name));
	}

	/**
	 * Pulls out the MCChatColors from the string, and replaces them with the
	 * nearest match ANSI terminal color.
	 *
	 * @param mes If null, simply returns null
	 * @return
	 */
	public static String MCToANSIColors(String mes) {
		//Pull out the MC colors
		if (mes == null) {
			return null;
		}
		//We have to reset the color, then set it to the appropriate color, so that
		//background and non-foreground color characters are also reset at that point.
		//Hence the RESETs everywhere.
		return mes
				.replaceAll("§0", TermColors.RESET + TermColors.BLACK)
				.replaceAll("§1", TermColors.RESET + TermColors.BLUE)
				.replaceAll("§2", TermColors.RESET + TermColors.GREEN)
				.replaceAll("§3", TermColors.RESET + TermColors.CYAN)
				.replaceAll("§4", TermColors.RESET + TermColors.RED)
				.replaceAll("§5", TermColors.RESET + TermColors.MAGENTA)
				.replaceAll("§6", TermColors.RESET + TermColors.YELLOW)
				.replaceAll("§7", TermColors.RESET + TermColors.WHITE)
				.replaceAll("§8", TermColors.RESET + TermColors.BRIGHT_BLACK)
				.replaceAll("§9", TermColors.RESET + TermColors.BRIGHT_BLUE)
				.replaceAll("§a", TermColors.RESET + TermColors.BRIGHT_GREEN)
				.replaceAll("§b", TermColors.RESET + TermColors.BRIGHT_CYAN)
				.replaceAll("§c", TermColors.RESET + TermColors.BRIGHT_RED)
				.replaceAll("§d", TermColors.RESET + TermColors.BRIGHT_MAGENTA)
				.replaceAll("§e", TermColors.RESET + TermColors.BRIGHT_YELLOW)
				.replaceAll("§f", TermColors.RESET + TermColors.BRIGHT_WHITE)
				.replaceAll("§k", TermColors.RESET + "") //Uh, no equivalent for "random"
				.replaceAll("§l", TermColors.RESET + TermColors.BOLD)
				.replaceAll("§m", TermColors.RESET + TermColors.STRIKE)
				.replaceAll("§n", TermColors.RESET + TermColors.UNDERLINE)
				.replaceAll("§o", TermColors.RESET + TermColors.ITALIC)
				.replaceAll("§r", TermColors.reset());

	}

	public static void InjectPlayer(MCCommandSender player) {
		String name = player.getName();
		if ("CONSOLE".equals(name)) {
			name = "~console";
		}
		injectedPlayers.put(name, player);
	}

	/**
	 * Removes a player into the global player proxy system. Returns the player
	 * removed (or null if none were injected).
	 *
	 * @param player
	 * @return
	 */
	public static MCCommandSender UninjectPlayer(MCCommandSender player) {
		String name = player.getName();
		if ("CONSOLE".equals(name)) {
			name = "~console";
		}
		return injectedPlayers.remove(name);
	}

	public static void HostnameCache(final MCPlayer p) {
		CommandHelperPlugin.hostnameLookupThreadPool.submit(new Runnable() {
			@Override
			public void run() {
				CommandHelperPlugin.hostnameLookupCache.put(p.getName(),
						p.getAddress().getHostName());
			}
		});
	}

	public static void SetPlayerHost(MCPlayer p, String host) {
		hostCache.put(p.getName(), host);
	}

	public static String GetHost(MCPlayer p) {
		return hostCache.get(p.getName());
	}

	public static void AssertPlayerNonNull(MCPlayer p, Target t) {
		if (p == null) {
			throw new ConfigRuntimeException("No player was specified!", ExceptionType.PlayerOfflineException, t);
		}
	}

	public static long msToTicks(long ms) {
		return ms / 50;
	}

	public static long ticksToMs(long ticks) {
		return ticks * 50;
	}

	public static void AssertNonNull(Object var, String message) {
		if (var == null) {
			throw new NullPointerException(message);
		}
	}

	/**
	 * Generates a new environment, assuming that the jar has a folder next to
	 * it named CommandHelper, and that folder is the root.
	 *
	 * @return
	 * @throws IOException
	 * @throws DataSourceException
	 * @throws URISyntaxException
	 */
	public static Environment GenerateStandaloneEnvironment() throws IOException, DataSourceException, URISyntaxException, Profiles.InvalidProfileException {
		File jarLocation;
		if (Static.class.getProtectionDomain().getCodeSource().getLocation() != null) {
			jarLocation = new File(Static.class.getProtectionDomain().getCodeSource().getLocation().getFile()).getParentFile();
		} else {
			jarLocation = new File(".");
		}
		File platformFolder = new File(jarLocation, Implementation.GetServerType().getBranding() + "/");
		Installer.Install(platformFolder);
		ConnectionMixinFactory.ConnectionMixinOptions options = new ConnectionMixinFactory.ConnectionMixinOptions();
		options.setWorkingDirectory(platformFolder);
		PersistenceNetwork persistenceNetwork = new PersistenceNetwork(MethodScriptFileLocations.getDefault().getPersistenceConfig(),
				new URI("sqlite://" + new File(platformFolder, "persistence.db").getCanonicalPath().replace("\\", "/")), options);
		GlobalEnv gEnv = new GlobalEnv(new MethodScriptExecutionQueue("MethodScriptExecutionQueue", "default"),
				new Profiler(MethodScriptFileLocations.getDefault().getProfilerConfigFile()), persistenceNetwork, platformFolder,
				new Profiles(MethodScriptFileLocations.getDefault().getSQLProfilesFile()), new TaskManager());
		gEnv.SetLabel(GLOBAL_PERMISSION);
		return Environment.createEnvironment(gEnv, new CommandHelperEnvironment());
	}

	/**
	 * Asserts that all the args are not CNulls. If so, throws a
	 * ConfigRuntimeNullPointerException
	 *
	 * @param t
	 * @param args
	 * @throws ConfigRuntimeException
	 */
	public static void AssertNonCNull(Target t, Construct... args) throws ConfigRuntimeException {
		for (Construct arg : args) {
			if (arg instanceof CNull) {
				throw new ConfigRuntimeException("Argument was null, and nulls are not allowed.", ExceptionType.NullPointerException, t);
			}
		}
	}

	public static String GetStacktraceString(Throwable t) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		t.printStackTrace(pw);
		return sw.toString();
	}

	/**
	 * Returns the actual file location, given the script's partial (or
	 * absolute) file path, and depending on the context, the correct File
	 * object. Security checking is not done at this stage, this merely
	 * transforms the path into the correct File object. Additionally, if arg is
	 * null, then the default is returned. If it is known that the arg won't
	 * ever be null, null may be set as the default. Except in cases where both
	 * arg and def are null, this function will never return null. If the arg
	 * starts with ~, it is replaced with the user's home directory, as defined
	 * by the system property user.home.
	 *
	 * This generally condenses a 5 or 6 line operation into 1 line.
	 *
	 * @param arg
	 * @return
	 */
	public static File GetFileFromArgument(String arg, Environment env, Target t, File def) {
		if (arg == null) {
			return def;
		}
		if (arg.startsWith("~")) {
			arg = System.getProperty("user.home") + arg.substring(1);
		}
		File f = new File(arg);
		if (f.isAbsolute()) {
			return f;
		}
		//Ok, it's not absolute, so we need to see if we're in cmdline mode or not.
		//If so, we use the root directory, not the target.
		if (env != null && InCmdLine(env)) {
			return new File(env.getEnv(GlobalEnv.class).GetRootFolder(), arg);
		} else {
			return new File(t.file().getParent(), arg);
		}
	}

	/**
	 * Returns true if currently running in cmdline mode.
	 *
	 * @param environment
	 * @return
	 */
	public static boolean InCmdLine(Environment environment) {
		return environment.getEnv(GlobalEnv.class).GetCustom("cmdline") instanceof Boolean
				&& (Boolean) environment.getEnv(GlobalEnv.class).GetCustom("cmdline");
	}

	/**
	 * This verifies that the type required is actually present, and returns the
	 * value, cast to the appropriate type, or, if not the correct type, a CRE.
	 * <p>
	 * Note that this does not do type coersion, and therefore does not work on
	 * primitives, and is only meant for arrays, closures, and other complex
	 * types.
	 *
	 * @param <T> The type desired to be cast to
	 * @param type The type desired to be cast to
	 * @param args The array of arguments.
	 * @param argNumber The argument number, used both for grabbing the correct
	 * argument from args, and building the error message if the cast cannot
	 * occur.
	 * @param func The function, in case this errors out, to build the error
	 * message.
	 * @param t The code target
	 * @return The value, cast to the desired type.
	 */
	public static <T extends Construct> T AssertType(Class<T> type, Construct[] args, int argNumber, Function func, Target t) {
		Construct value = args[argNumber];
		if (!type.isAssignableFrom(value.getClass())) {
			typeof todesired = type.getAnnotation(typeof.class);
			String toactual = value.typeof();
			if (todesired != null) {
				throw new ConfigRuntimeException("Argument " + (argNumber + 1) + " of " + func.getName() + " was expected to be a "
						+ todesired.value() + ", but " + toactual + " \"" + value.val() + "\" was found.", ExceptionType.CastException, t);
			} else {
				//If the typeof annotation isn't present, this is a programming error.
				throw new IllegalArgumentException("");
			}
		} else {
			return (T) value;
		}
	}

	/**
	 * Given a java object, returns a MethodScript object.
	 *
	 * @param object
	 * @param t
	 * @return
	 */
	public static Construct getMSObject(Object object, Target t) {
		if (object == null) {
			return CNull.NULL;
		} else if (object instanceof Boolean) {
			return CBoolean.get((boolean) object);
		} else if ((object instanceof Byte) || (object instanceof Short) || (object instanceof Integer) || (object instanceof Long)) {
			return new CInt((long) object, t);
		} else if ((object instanceof Float) || (object instanceof Double)) {
			return new CDouble((double) object, t);
		} else if (object instanceof Character) {
			return new CString((char) object, t);
		} else if (object instanceof String) {
			return new CString((String) object, t);
		} else if (object instanceof StringBuffer) {
			return new CResource<>((StringBuffer) object, new CResource.ResourceToString() {
				@Override
				public String getString(CResource res) {
					return res.getResource().toString();
				}
			}, t);
		} else if (object instanceof XMLDocument) {
			return new CResource<>((XMLDocument) object, t);
		} else if (object instanceof Construct) {
			return (Construct) object;
		} else if (object instanceof boolean[]) {
			boolean[] array = (boolean[]) object;
			CArray r = new CArray(t);
			for (boolean b : array) {
				r.push(CBoolean.get(b));
			}
			return r;
		} else if (object instanceof byte[]) {
			return CByteArray.wrap((byte[]) object, t);
		} else if (object instanceof char[]) {
			char[] array = (char[]) object;
			CArray r = new CArray(t);
			for (char c : array) {
				r.push(new CString(c, t));
			}
			return r;
		} else if (object instanceof short[]) {
			short[] array = (short[]) object;
			CArray r = new CArray(t);
			for (short s : array) {
				r.push(new CInt(s, t));
			}
			return r;
		} else if (object instanceof int[]) {
			int[] array = (int[]) object;
			CArray r = new CArray(t);
			for (int i : array) {
				r.push(new CInt(i, t));
			}
			return r;
		} else if (object instanceof long[]) {
			long[] array = (long[]) object;
			CArray r = new CArray(t);
			for (long l : array) {
				r.push(new CInt(l, t));
			}
			return r;
		} else if (object instanceof float[]) {
			float[] array = (float[]) object;
			CArray r = new CArray(t);
			for (float f : array) {
				r.push(new CDouble(f, t));
			}
			return r;
		} else if (object instanceof double[]) {
			double[] array = (double[]) object;
			CArray r = new CArray(t);
			for (double d : array) {
				r.push(new CDouble(d, t));
			}
			return r;
		} else if (object instanceof Object[]) {
			CArray r = new CArray(t);
			for (Object o : (Object[]) object) {
				r.push((o == object) ? r : getMSObject(o, t));
			}
			return r;
		} else if (object instanceof Collection) {
			return getMSObject(((Collection) object).toArray(), t);
		} else if (object instanceof Map) {
			Map map = ((Map) object);
			CArray r = new CArray(t);
			for (Object key : map.keySet()) {
				Object o = map.get(key);
				r.set(key.toString(), (o == object) ? r : getMSObject(o, t), t);
			}
			return r;
		} else {
			return new CString(object.toString(), t);
		}
	}

	/**
	 * Given a MethodScript object, returns a java object.
	 *
	 * @param construct
	 * @return
	 */
	public static Object getJavaObject(Construct construct) {
		if ((construct == null) || (construct instanceof CNull)) {
			return null;
		} else if (construct instanceof CVoid) {
			return "";
		} else if (construct instanceof CBoolean) {
			return ((CBoolean) construct).getBoolean();
		} else if (construct instanceof CInt) {
			return ((CInt) construct).getInt();
		} else if (construct instanceof CDouble) {
			return ((CDouble) construct).getDouble();
		} else if (construct instanceof CString) {
			return construct.val();
		} else if (construct instanceof CByteArray) {
			return ((CByteArray) construct).asByteArrayCopy();
		} else if (construct instanceof CResource) {
			return ((CResource) construct).getResource();
		} else if (construct instanceof CArray) {
			CArray array = (CArray) construct;
			if (array.isAssociative()) {
				HashMap<String, Object> map = new HashMap<>();
				for (Construct key : array.keySet()) {
					Construct c = array.get(key.val(), Target.UNKNOWN);
					map.put(key.val(), (c == array) ? map : getJavaObject(c));
				}
				return map;
			} else {
				Object[] a = new Object[(int) array.size()];
				boolean nullable = false;
				Class<?> clazz = null;
				for (int i = 0; i < array.size(); i++) {
					Construct c = array.get(i, Target.UNKNOWN);
					if (c == array) {
						a[i] = a;
					} else {
						a[i] = getJavaObject(array.get(i, Target.UNKNOWN));
					}
					if (a[i] != null) {
						if (clazz == null) {
							clazz = a[i].getClass();
						} else if (!clazz.equals(Object.class)) {
							//to test if it is possible to return something more specific than Object[]
							Class<?> cl = a[i].getClass();
							while (!clazz.isAssignableFrom(cl)) {
								clazz = clazz.getSuperclass();
							}
						}
					} else {
						nullable = true;
					}
				}
				if ((clazz != null) && (!clazz.equals(Object.class))) {
					if (clazz.equals(Boolean.class) && !nullable) {
						boolean[] r = new boolean[a.length];
						for (int i = 0; i < a.length; i++) {
							r[i] = (boolean) a[i];
						}
						return r;
					}
					if (clazz.equals(Long.class) && !nullable) {
						long[] r = new long[a.length];
						for (int i = 0; i < a.length; i++) {
							r[i] = (long) a[i];
						}
						return r;
					} else if (clazz.equals(Double.class) && !nullable) {
						double[] r = new double[a.length];
						for (int i = 0; i < a.length; i++) {
							r[i] = (double) a[i];
						}
						return r;
					} else {
						Object[] r = (Object[]) Array.newInstance(clazz, a.length);
						System.arraycopy(a, 0, r, 0, a.length);
						return r;
					}
				} else {
					return a;
				}
			}
		} else {
			return construct;
		}
	}
}
