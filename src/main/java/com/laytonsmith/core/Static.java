package com.laytonsmith.core;

import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscovery;
import com.laytonsmith.PureUtilities.Common.DateUtils;
import com.laytonsmith.PureUtilities.Common.StackTraceUtils;
import com.laytonsmith.PureUtilities.Common.StreamUtils;
import com.laytonsmith.PureUtilities.SimpleVersion;
import com.laytonsmith.PureUtilities.TermColors;
import com.laytonsmith.PureUtilities.XMLDocument;
import com.laytonsmith.PureUtilities.ZipReader;
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
import com.laytonsmith.abstraction.MCWorld;
import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.abstraction.blocks.MCMaterial;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.commandhelper.CommandHelperPlugin;
import com.laytonsmith.core.compiler.CompilerEnvironment;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CBareString;
import com.laytonsmith.core.constructs.CBoolean;
import com.laytonsmith.core.constructs.CByteArray;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.CDecimal;
import com.laytonsmith.core.constructs.CDouble;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.constructs.CNull;
import com.laytonsmith.core.constructs.CNumber;
import com.laytonsmith.core.constructs.CPrimitive;
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
import com.laytonsmith.core.exceptions.CRE.CREBadEntityException;
import com.laytonsmith.core.exceptions.CRE.CRECastException;
import com.laytonsmith.core.exceptions.CRE.CREFormatException;
import com.laytonsmith.core.exceptions.CRE.CREIOException;
import com.laytonsmith.core.exceptions.CRE.CREInvalidPluginException;
import com.laytonsmith.core.exceptions.CRE.CREInvalidWorldException;
import com.laytonsmith.core.exceptions.CRE.CRELengthException;
import com.laytonsmith.core.exceptions.CRE.CRENullPointerException;
import com.laytonsmith.core.exceptions.CRE.CREPlayerOfflineException;
import com.laytonsmith.core.exceptions.CRE.CRERangeException;
import com.laytonsmith.core.exceptions.CRE.CREThrowable;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.Function;
import com.laytonsmith.core.natives.interfaces.Mixed;
import com.laytonsmith.core.profiler.Profiler;
import com.laytonsmith.core.taskmanager.TaskManagerImpl;
import com.laytonsmith.persistence.DataSourceException;
import com.laytonsmith.persistence.PersistenceNetwork;
import com.laytonsmith.persistence.PersistenceNetworkImpl;
import com.laytonsmith.persistence.io.ConnectionMixinFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Array;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.yaml.snakeyaml.Yaml;

/**
 * This class contains several static methods to get various objects that really should be static in the first place,
 * but aren't. For the most part, when any code is running, these things will have been initialized, but in the event
 * they aren't, each function will throw a NotInitializedYetException, which is a RuntimeException, so you don't have to
 * check for exceptions whenever you use them. The Exception is caught on a higher level though, so it shouldn't bubble
 * up too far.
 *
 */
public final class Static {

	private Static() {
	}

	private static Logger logger;

	private static final Map<String, String> HOST_CACHE = new HashMap<String, String>();

	private static final String CONSOLE_NAME = "~console";

	// Chosen over @ because that does special things when used by the block.
	private static final String BLOCK_PREFIX = "#";

	/**
	 * In case the API being used doesn't support permission groups, a permission node in the format
	 * <code>String permission = groupPrefix + groupName;</code> can be assigned to players to declare their permission
	 * group.
	 *
	 * Third party APIs may provide better access.
	 */
	public static final String GROUP_PREFIX = "group.";

	/**
	 * The label representing unrestricted access.
	 */
	public static final String GLOBAL_PERMISSION = "*";

	/**
	 * Returns a CArray object from a given construct, throwing a common error message if not.
	 *
	 * @param construct
	 * @param t
	 * @return
	 */
	public static CArray getArray(Mixed construct, Target t) {
		return ArgumentValidation.getArray(construct, t);
	}

	/**
	 * Works like the other get* methods, but works in a more generic way for other types of Constructs. It also assumes
	 * that the class specified is tagged with a typeof annotation, thereby preventing the need for the
	 * expectedClassName like the deprecated version uses.
	 *
	 * @param <T> The type expected.
	 * @param construct The generic object
	 * @param t Code target
	 * @param clazz The type expected.
	 * @return The properly cast object.
	 */
	public static <T extends Mixed> T getObject(Mixed construct, Target t, Class<T> clazz) {
		return ArgumentValidation.getObject(construct, t, clazz);
	}

	/**
	 * Returns a CNumber construct (CInt or CDouble) from any java number.
	 *
	 * @param number The java number to convert.
	 * @param t The code target.
	 * @return A construct equivalent to the given java number, whose the type is the better to represent it.
	 */
	public static CNumber getNumber(Number number, Target t) {
		long longValue = number.longValue();
		double doubleValue = number.doubleValue();
		return longValue == doubleValue ? new CInt(longValue, t) : new CDouble(doubleValue, t);
	}

	/**
	 * This function pulls a numerical equivalent from any given construct. It throws a ConfigRuntimeException if it
	 * cannot be converted, for instance the string "s" cannot be cast to a number. The number returned will always be a
	 * double.
	 *
	 * @param c
	 * @return
	 */
	public static double getNumber(Mixed c, Target t) {
		return ArgumentValidation.getNumber(c, t);
	}

	/**
	 * Alias to getNumber
	 *
	 * @param c
	 * @return
	 */
	public static double getDouble(Mixed c, Target t) {
		return ArgumentValidation.getDouble(c, t);
	}

	public static float getDouble32(Mixed c, Target t) {
		return ArgumentValidation.getDouble32(c, t);
	}

	/**
	 * Returns an integer from any given construct.
	 *
	 * @param c
	 * @throws CRERangeException If the value would be truncated
	 * @throws CRECastException If the value cannot be cast to an int
	 * @return
	 */
	public static long getInt(Mixed c, Target t) {
		return ArgumentValidation.getInt(c, t);
	}

	/**
	 * Returns a 32 bit int from the construct. Since the backing value is actually a long, if the number contained in
	 * the construct is not the same after truncating, an exception is thrown (fail fast). When needing an int from a
	 * construct, this method is much preferred over silently truncating.
	 *
	 * @param c
	 * @param t
	 * @throws CRERangeException If the value would be truncated
	 * @throws CRECastException If the value cannot be cast to an int
	 * @return
	 */
	public static int getInt32(Mixed c, Target t) {
		return ArgumentValidation.getInt32(c, t);
	}

	/**
	 * Returns a 16 bit int from the construct (a short). Since the backing value is actually a long, if the number
	 * contained in the construct is not the same after truncating, an exception is thrown (fail fast). When needing an
	 * short from a construct, this method is much preferred over silently truncating.
	 *
	 * @param c
	 * @param t
	 * @throws CRERangeException If the value would be truncated
	 * @throws CRECastException If the value cannot be cast to an int
	 * @return
	 */
	public static short getInt16(Mixed c, Target t) {
		return ArgumentValidation.getInt16(c, t);
	}

	/**
	 * Returns an 8 bit int from the construct (a byte). Since the backing value is actually a long, if the number
	 * contained in the construct is not the same after truncating, an exception is thrown (fail fast). When needing a
	 * byte from a construct, this method is much preferred over silently truncating.
	 *
	 * @param c
	 * @param t
	 * @throws CRERangeException If the value would be truncated
	 * @throws CRECastException If the value cannot be cast to an int
	 * @return
	 */
	public static byte getInt8(Mixed c, Target t) {
		return ArgumentValidation.getInt8(c, t);
	}

	/**
	 * Currently forwards the call to
	 * {@link ArgumentValidation#getBooleanish},
	 * to keep backwards compatible behavior, but will be removed in a future release. Explicitely use either
	 * {@link ArgumentValidation#getBooleanish} or {@link ArgumentValidation#getBooleanObject}.
	 * @param c
	 * @param t
	 * @return
	 * @deprecated Use {@link ArgumentValidation#getBooleanish} for current behavior, or
	 * {@link ArgumentValidation#getBooleanObject} for strict behavior. Note: While this is deprecated, and will be
	 * removed from Static, it will not be removed until all the other methods that are duplicated here and in
	 * {@link ArgumentValidation} are officially deprecated and removed, so there is no immediate need to backport older
	 * code, as it will probably be easier to do it all at once later. However, new code should no longer use this
	 * method. (Or any of the other methods that are duplicated.)
	 */
	@Deprecated
	public static boolean getBoolean(Mixed c, Target t) {
		return ArgumentValidation.getBooleanish(c, t);
	}

	/**
	 * Returns a primitive from any given construct.
	 *
	 * @param c
	 * @param t
	 * @return
	 */
	public static CPrimitive getPrimitive(Mixed c, Target t) {
		return ArgumentValidation.getObject(c, t, CPrimitive.class);
	}

	/**
	 * Returns a CByteArray from any given construct.
	 *
	 * @param c
	 * @param t
	 * @return
	 */
	public static CByteArray getByteArray(Mixed c, Target t) {
		return ArgumentValidation.getByteArray(c, t);
	}

	/**
	 * Returns true if any of the constructs are a CDouble, false otherwise.
	 *
	 * @param c
	 * @return
	 */
	public static boolean anyDoubles(Mixed... c) {
		return ArgumentValidation.anyDoubles(c);
	}

	/**
	 * Return true if any of the constructs are CStrings, false otherwise.
	 *
	 * @param c
	 * @return
	 */
	public static boolean anyStrings(Mixed... c) {
		return ArgumentValidation.anyStrings(c);
	}

	/**
	 * Returns true if any of the constructs are CBooleans, false otherwise.
	 *
	 * @param c
	 * @return
	 */
	public static boolean anyBooleans(Mixed... c) {
		return ArgumentValidation.anyBooleans(c);
	}

	/**
	 * Returns true if any of the constructs are null.
	 *
	 * @param c
	 * @return
	 */
	public static boolean anyNulls(Mixed... c) {
		return ArgumentValidation.anyNulls(c);
	}

	/**
	 * Returns the logger for the plugin
	 *
	 * @return
	 */
	public static Logger getLogger() {
		if(logger == null) {
			if(Implementation.GetServerType() == Implementation.Type.BUKKIT) {
				logger = CommandHelperPlugin.self.getLogger();
			} else {
				logger = Logger.getLogger("MethodScript");
			}
		}
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
		if(s == null) {
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
		if(ac == null) {
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
		SimpleVersion v = null;
		if(Implementation.GetServerType() == Implementation.Type.BUKKIT) {
			v = com.laytonsmith.commandhelper.CommandHelperPlugin.version;
		} else {
			try {
				v = loadSelfVersion();
			} catch (Exception ex) {
				//Ignored
			}
		}
		if(v == null) {
			throw new NotInitializedYetException("The plugin has not been initialized yet");
		}
		return v;
	}

	@SuppressWarnings({"ThrowableInstanceNotThrown", "ThrowableInstanceNeverThrown"})
	public static SimpleVersion loadSelfVersion() throws Exception {
		File file = new File(new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI()), "plugin.yml");
		ZipReader reader = new ZipReader(file);
		if(!reader.exists()) {
			throw new FileNotFoundException(String.format("%s does not exist", file.getPath()));
		}
		try {
			String contents = reader.getFileContents();
			Yaml yaml = new Yaml();
			Map<String, Object> map = (Map<String, Object>) yaml.load(contents);
			return new SimpleVersion((String) map.get("version"));
		} catch (RuntimeException | IOException ex) {
			throw new Exception(ex);
		}
	}

	public static String getNoClassDefFoundErrorMessage(NoClassDefFoundError error) {
		String ret = "The main class requires craftbukkit or bukkit to be included in order to run. If you are seeing"
				+ " this message, you have two options. First, it seems you have renamed your craftbukkit jar, or"
				+ " you are altogether not using craftbukkit. If this is the case, you can download craftbukkit and place"
				+ " it in the correct directory (one above this one) or you can download bukkit, rename it to bukkit.jar,"
				+ " and put it in the CommandHelper directory.";
		//if(Prefs.DebugMode()) {
		ret += " If you're dying for more details, here:\n";
		ret += StackTraceUtils.GetStacktrace(error);
		//}
		return ret;
	}

	private static String debugLogFileCurrent = null;
	private static FileWriter debugLogFileHandle = null;

	/**
	 * Returns a file that is most likely ready to write to. The timestamp variables have already been replaced, and
	 * parent directories are all created.
	 *
	 * @return
	 */
	public static FileWriter debugLogFile(File root) throws IOException {
		String currentFileName = root.getPath() + "/" + DateUtils.ParseCalendarNotation(Prefs.DebugLogFile());
		if(!currentFileName.equals(debugLogFileCurrent)) {
			if(debugLogFileHandle != null) {
				//We're done with the old one, close it.
				debugLogFileHandle.close();
			}
			debugLogFileCurrent = currentFileName;
			new File(debugLogFileCurrent).getParentFile().mkdirs();
			if(!new File(debugLogFileCurrent).exists()) {
				new File(debugLogFileCurrent).createNewFile();
			}
			debugLogFileHandle = new FileWriter(currentFileName, true);
		}
		return debugLogFileHandle;
	}
	private static String standardLogFileCurrent = null;
	private static FileWriter standardLogFileHandle = null;

	public static FileWriter standardLogFile(File root) throws IOException {
		String currentFileName = root.getPath() + DateUtils.ParseCalendarNotation(Prefs.StandardLogFile());
		if(!currentFileName.equals(standardLogFileCurrent)) {
			if(standardLogFileHandle != null) {
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
		if(!currentFileName.equals(profilingLogFileCurrent)) {
			if(profilingLogFileHandle != null) {
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
		if(Static.getServer().getPluginManager().getPlugin(name) == null) {
			throw new CREInvalidPluginException("Needed plugin " + name + " not found!", t);
		}
	}

	/**
	 * Regex patterns
	 */
	private static final Pattern INVALID_HEX = Pattern.compile("-?0x[a-fA-F0-9]*[^a-fA-F0-9]+[a-fA-F0-9]*");
	private static final Pattern VALID_HEX = Pattern.compile("-?0x[a-fA-F0-9]+");
	private static final Pattern INVALID_BINARY = Pattern.compile("-?0b[01]*[^01]+[01]*");
	private static final Pattern VALID_BINARY = Pattern.compile("-?0b[01]+");
	private static final Pattern INVALID_OCTAL = Pattern.compile("-?0o[0-7]*[^0-7]+[0-7]*");
	private static final Pattern VALID_OCTAL = Pattern.compile("-?0o[0-7]+");
	private static final Pattern VALID_DECIMAL = Pattern.compile("-?0m[0-9]+");
	private static final Pattern INVALID_DECIMAL = Pattern.compile("-?0m[0-9]*[^0-9]+[0-9]*");


	/**
	 * Given a string input, creates and returns a Construct of the appropriate type. This takes into account that null,
	 * true, and false are keywords.
	 *
	 * @param val
	 * @param t
	 * @return
	 * @throws ConfigRuntimeException If the value is a hex or binary value, but has invalid characters in it.
	 */
	public static Construct resolveConstruct(String val, Target t) throws ConfigRuntimeException {
		return resolveConstruct(val, t, false);
	}

	/**
	 * Given a string input, creates and returns a Construct of the appropriate type. This takes into account that null,
	 * true, and false are keywords.
	 *
	 * If returnBareStrings is true, then we don't return CString, we return CBareString.
	 * @param val
	 * @param t
	 * @param returnBareStrings
	 * @return
	 * @throws ConfigRuntimeException
	 */
	public static Construct resolveConstruct(String val, Target t, boolean returnBareStrings)
			throws ConfigRuntimeException {
		if(val == null) {
			return new CString("", t);
		}
		if(val.equals("true")) {
			return CBoolean.TRUE;
		}
		if(val.equals("false")) {
			return CBoolean.FALSE;
		}
		if(val.equals("null")) {
			return CNull.NULL;
		}
		if(val.equals("void")) {
			return CVoid.VOID;
		}
		if(INVALID_HEX.matcher(val).matches()) {
			throw new CREFormatException("Hex numbers must only contain digits 0-9, and the letters A-F, but \"" + val + "\" was found.", t);
		}
		if(VALID_HEX.matcher(val).matches()) {
			//Hex number
			return new CInt(Long.parseLong(val.substring(2), 16), t);
		}
		if(INVALID_BINARY.matcher(val).matches()) {
			throw new CREFormatException("Binary numbers must only contain digits 0 and 1, but \"" + val + "\" was found.", t);
		}
		if(VALID_BINARY.matcher(val).matches()) {
			//Binary number
			return new CInt(Long.parseLong(val.substring(2), 2), t);
		}
		if(INVALID_OCTAL.matcher(val).matches()) {
			throw new CREFormatException("Octal numbers must only contain digits 0-7, but \"" + val + "\" was found.", t);
		}
		if(VALID_OCTAL.matcher(val).matches()) {
			return new CInt(Long.parseLong(val.substring(2), 8), t);
		}
		if(INVALID_DECIMAL.matcher(val).matches()) {
			throw new CREFormatException("Decimal numbers must only contain digits, but \"" + val + "\" was found.", t);
		}
		if(VALID_DECIMAL.matcher(val).matches()) {
			return new CDecimal(val.substring(2), t);
		}
		try {
			return new CInt(Long.parseLong(val), t);
		} catch (NumberFormatException e) {
			try {
				if(!(val.contains(" ") || val.contains("\t"))) {
					//Interesting behavior in Double.parseDouble causes it to "trim" strings first, then
					//try to parse them, which is not desireable in our case. So, if the value contains
					//any characters other than [\-0-9\.], we want to make it a string instead
					return new CDouble(Double.parseDouble(val), t);
				}
			} catch (NumberFormatException g) {
				// Not a double either
			}
		}
		String fqType = NativeTypeList.resolveNativeType(val);
		if(fqType != null) {
			try {
				return CClassType.get(FullyQualifiedClassName.forFullyQualifiedClass(fqType));
			} catch (ClassNotFoundException ex) {
				// Can't happen, because we just resolved the type, and it wasn't null.
				throw new Error(ex);
			}
		}
		if(returnBareStrings) {
			return new CBareString(val, t);
		} else {
			return new CString(val, t);
		}
	}

	public static Construct resolveDollarVar(Construct variable, List<Variable> vars) {
		if(variable == null) {
			return CNull.NULL;
		}
		if(variable.getCType() == Construct.ConstructType.VARIABLE) {
			for(Variable var : vars) {
				if(var.getVariableName().equals(((Variable) variable).getVariableName())) {
					return new CString(var.val(), var.getTarget());
				}
			}
			return new CString(((Variable) variable).getDefault(), variable.getTarget());
		} else {
			return variable;
		}
	}

	/**
	 * This function sends a message to the player. If the player is not online, a CRE is thrown.
	 *
	 * @param m
	 * @param msg
	 */
	public static void SendMessage(final MCCommandSender m, String msg, final Target t) {
		if(m != null && !(m instanceof MCConsoleCommandSender)) {
			if(m instanceof MCPlayer) {
				MCPlayer p = (MCPlayer) m;
				if(!p.isOnline()) {
					throw new CREPlayerOfflineException("The player " + p.getName() + " is not online", t);
				}
			}
			m.sendMessage(msg);
		} else {
			msg = Static.MCToANSIColors(msg);
			if(msg.contains("\033")) {
				//We have terminal colors, we need to reset them at the end
				msg += TermColors.reset();
			}
			StreamUtils.GetSystemOut().println(msg);
		}
	}

	/**
	 * Works like
	 * {@link #SendMessage(com.laytonsmith.abstraction.MCCommandSender, java.lang.String, com.laytonsmith.core.constructs.Target)}
	 * except it doesn't require a target, and ignores the message if the command sender is offline.
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
	 * This is done here so that if it ever changes, it will update in all functions/docs
	 *
	 * @return
	 */
	public static String getConsoleName() {
		return CONSOLE_NAME;
	}

	/**
	 * Returns the string set aside to prefix block names to distinguish them from players
	 *
	 * @return
	 */
	public static String getBlockPrefix() {
		return BLOCK_PREFIX;
	}

	/**
	 * Returns an item stack from the given item notation. Defaulting to the specified qty, this will throw an exception
	 * if the notation is invalid.
	 *
	 * @param functionName
	 * @param notation
	 * @param qty
	 * @throws CREFormatException If the notation is invalid.
	 * @return
	 * @deprecated Use MCMaterial instead
	 */
	@Deprecated
	public static MCItemStack ParseItemNotation(String functionName, String notation, int qty, Target t) {
		int type;
		short data = 0;
		try {
			int separatorIndex = notation.indexOf(':');
			if(separatorIndex != -1) {
				type = Integer.parseInt(notation.substring(0, separatorIndex));
				data = (short) Integer.parseInt(notation.substring(separatorIndex + 1));
			} else {
				type = Integer.parseInt(notation);
			}
		} catch (NumberFormatException e) {
			throw new CREFormatException("Invalid item format: " + notation, t);
		}
		MCMaterial mat = StaticLayer.GetMaterialFromLegacy(type, data);
		if(mat == null) {
			throw new CREFormatException("Invalid item format: " + notation, t);
		}
		MCItemStack is = StaticLayer.GetItemStack(mat, qty);
		MSLog.GetLogger().w(MSLog.Tags.DEPRECATION, "Item notation is deprecated."
				+ " Converting '" + notation + "' to '" + is.getType().getName() + "'.", t);
		return is;
	}

	private static final Map<String, MCCommandSender> INJECTED_PLAYERS = new HashMap<>();
	private static MCEntity injectedEntity;
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
			if(subject.length() == 36) {
				return UUID.fromString(subject);
			}
			if(subject.length() == 32) {
				Matcher matcher = DASHLESS_PATTERN.matcher(subject);
				if(!matcher.matches()) {
					throw new IllegalArgumentException("Invalid UUID format.");
				}
				return UUID.fromString(matcher.replaceAll("$1-$2-$3-$4-$5"));
			} else {
				throw new CRELengthException("A UUID is expected to be 32 or 36 characters,"
						+ " but the given string was " + subject.length() + " characters.", t);
			}
		} catch (IllegalArgumentException iae) {
			throw new CREFormatException("A UUID length string was given, but was not a valid UUID.", t);
		}
	}

	public static UUID GetUUID(Mixed subject, Target t) {
		return GetUUID(subject.val(), t);
	}

	public static MCOfflinePlayer GetUser(Mixed search, Target t) {
		return GetUser(search.val(), t);
	}

	/**
	 * Provides a user object containing info that doesn't require an online player. If provided a string between 1 and
	 * 16 characters, the lookup will be name-based. If provided a string that is 32 or 36 characters, the lookup will
	 * be uuid-based.
	 *
	 * @param search The text to be searched, can be between 1 and 16 characters, or 32 or 36 characters
	 * @param t
	 * @return
	 */
	public static MCOfflinePlayer GetUser(String search, Target t) {
		MCOfflinePlayer ofp;
		if(search.length() > 0 && search.length() <= 16) {
			ofp = getServer().getOfflinePlayer(search);
		} else {
			try {
				ofp = getServer().getOfflinePlayer(GetUUID(search, t));
			} catch (ConfigRuntimeException cre) {
				if(cre instanceof CREThrowable && ((CREThrowable) cre).isInstanceOf(CRELengthException.TYPE)) {
					throw new CRELengthException("The given string was the wrong size to identify a player."
							+ " A player name is expected to be between 1 and 16 characters. " + cre.getMessage(), t);
				} else {
					throw cre;
				}
			}
		}
		return ofp;
	}

	/**
	 * Returns the player specified by name. Injected players also are returned in this list. If provided a string
	 * between 1 and 16 characters, the lookup will be name-based. If provided a string that is 32 or 36 characters, the
	 * lookup will be uuid-based.
	 *
	 * @param player
	 * @param t
	 * @return
	 * @throws ConfigRuntimeException
	 */
	public static MCPlayer GetPlayer(String player, Target t) throws ConfigRuntimeException {
		MCCommandSender m;

		if(player == null) {
			throw new CREPlayerOfflineException("No player was specified!", t);
		}

		if(player.length() > 0 && player.length() <= 16) {
			m = GetCommandSender(player, t);
		} else {
			try {
				m = getServer().getPlayer(GetUUID(player, t));
			} catch (ConfigRuntimeException cre) {
				if(cre instanceof CREThrowable && ((CREThrowable) cre).isInstanceOf(CRELengthException.TYPE)) {
					throw new CRELengthException("The given string was the wrong size to identify a player."
							+ " A player name is expected to be between 1 and 16 characters. " + cre.getMessage(), t);
				} else {
					throw cre;
				}
			}
		}
		if(m == null) {
			throw new CREPlayerOfflineException("The specified player (" + player + ") is not online", t);
		}
		if(!(m instanceof MCPlayer)) {
			throw new CREPlayerOfflineException("Expecting a player name, but \"" + player + "\" was found.", t);
		}
		MCPlayer p = (MCPlayer) m;
		if(!p.isOnline()) {
			throw new CREPlayerOfflineException("The specified player (" + player + ") is not online", t);
		}
		return p;
	}

	public static MCPlayer GetPlayer(Mixed player, Target t) throws ConfigRuntimeException {
		return GetPlayer(player.val(), t);
	}

	/**
	 * Returns the specified command sender. Players are supported, as is the special ~console user. The special
	 * ~console user will always return a user.
	 *
	 * @param player
	 * @param t
	 * @return
	 * @throws ConfigRuntimeException
	 */
	public static MCCommandSender GetCommandSender(String player, Target t) throws ConfigRuntimeException {
		MCCommandSender m = null;
		if(INJECTED_PLAYERS.containsKey(player)) {
			m = INJECTED_PLAYERS.get(player);
		} else if(CONSOLE_NAME.equals(player)) {
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
		if(m == null || (m instanceof MCPlayer && (!((MCPlayer) m).isOnline() && !INJECTED_PLAYERS.containsKey(player)))) {
			throw new CREPlayerOfflineException("The specified player (" + player + ") is not online", t);
		}
		return m;
	}

	/**
	 * If the sender is a player, it is returned, otherwise a ConfigRuntimeException is thrown.
	 *
	 * @param environment
	 * @param t
	 * @return
	 */
	public static MCPlayer getPlayer(Environment environment, Target t) {
		MCPlayer player = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
		if(player != null) {
			return player;
		} else {
			throw new CREPlayerOfflineException("The passed arguments induce that the function must be run by a player.", t);
		}
	}

	public static boolean isNull(Mixed construct) {
		return construct instanceof CNull;
	}

	public static int Normalize(int i, int min, int max) {
		return java.lang.Math.min(max, java.lang.Math.max(min, i));
	}

	public static MCEntity getEntity(Mixed id, Target t) {
		return getEntityByUuid(GetUUID(id.val(), t), t);
	}

	public static MCLivingEntity getLivingEntity(Mixed id, Target t) {
		return getLivingByUUID(GetUUID(id.val(), t), t);
	}

	/**
	 * Returns the entity with the specified unique id. If it doesn't exist, a ConfigRuntimeException is thrown.
	 *
	 * @param id
	 * @return
	 */
	public static MCEntity getEntityByUuid(UUID id, Target t) {
		if(injectedEntity != null && injectedEntity.getUniqueId().compareTo(id) == 0) {
			// This entity is not in the world yet, but it was injected by the event
			return injectedEntity;
		}
		MCEntity ent = getServer().getEntity(id);
		if(ent == null) {
			// Sometimes a bug may cause a player entity to be missing from entity lists in the server,
			// so we'll double check the player list.
			ent = getServer().getPlayer(id);
			if(ent == null) {
				throw new CREBadEntityException("That entity (UUID: " + id + ") does not exist.", t);
			}
		}
		return ent;
	}

	/**
	 * Returns the living entity with the specified unique id. If it doesn't exist or isn't living, a
	 * ConfigRuntimeException is thrown.
	 *
	 * @param id
	 * @return
	 */
	public static MCLivingEntity getLivingByUUID(UUID id, Target t) {
		if(injectedEntity != null && injectedEntity.getUniqueId().compareTo(id) == 0) {
			// This entity is not in the world yet, but it was injected by the event
			if(injectedEntity instanceof MCLivingEntity) {
				return (MCLivingEntity) injectedEntity;
			}
			throw new CREBadEntityException("That entity (UUID: " + id + ") is not alive.", t);
		}
		MCEntity ent = getServer().getEntity(id);
		if(ent == null) {
			throw new CREBadEntityException("That entity (UUID: " + id + ") does not exist.", t);
		}
		if(!(ent instanceof MCLivingEntity)) {
			throw new CREBadEntityException("That entity (UUID: " + id + ") is not alive.", t);
		}
		return (MCLivingEntity) ent;
	}

	/**
	 * Returns the world with the specified name. If it does not exist, a ConfigRuntimeException is thrown.
	 *
	 * @param name
	 * @param t
	 * @return
	 */
	public static MCWorld getWorld(String name, Target t) {
		MCWorld world = getServer().getWorld(name);
		if(world != null) {
			return world;
		} else {
			throw new CREInvalidWorldException("Unknown world:" + name + ".", t);
		}
	}

	/**
	 * Returns the world with the specified name. If it does not exist, a ConfigRuntimeException is thrown.
	 *
	 * @param name
	 * @param t
	 * @return
	 */
	public static MCWorld getWorld(Mixed name, Target t) {
		return getWorld(name.val(), t);
	}

	/**
	 * Returns the plugin with the specified name. If it does not exist, a ConfigRuntimeException is thrown.
	 *
	 * @param name
	 * @param t
	 * @return
	 */
	public static MCPlugin getPlugin(String name, Target t) {
		MCPlugin plugin = getServer().getPluginManager().getPlugin(name);
		if(plugin != null) {
			return plugin;
		} else {
			throw new CREInvalidPluginException("Unknown plugin:" + name + ".", t);
		}
	}

	public static MCPlugin getPlugin(Mixed name, Target t) {
		return getPlugin(name.val(), t);
	}

	/**
	 * Returns the metadatable object designated by the given construct. If the construct is invalid or if the object
	 * does not exist, a ConfigRuntimeException is thrown.
	 *
	 * @param construct
	 * @param t
	 * @return
	 */
	public static MCMetadatable getMetadatable(Mixed construct, Target t) {
		if(construct.isInstanceOf(CArray.TYPE)) {
			return ObjectGenerator.GetGenerator().location(construct, null, t).getBlock();
		} else if(construct instanceof CString) {
			switch(construct.val().length()) {
				case 32:
				case 36:
					return Static.getEntity(construct, t);
				default:
					return Static.getWorld(construct, t);
			}
		} else {
			throw new CRECastException("An array or a string was expected, but " + construct.val()
					+ " was found.", t);
		}
	}

	public static String strJoin(Collection c, String inner) {
		StringBuilder b = new StringBuilder();
		Object[] o = c.toArray();
		for(int i = 0; i < o.length; i++) {
			if(i != 0) {
				b.append(inner);
			}
			b.append(o[i]);
		}
		return b.toString();
	}

	public static String strJoin(Object[] o, String inner) {
		StringBuilder b = new StringBuilder();
		for(int i = 0; i < o.length; i++) {
			if(i != 0) {
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
	 * Logs an error message, depending on the log level of the message and the user's preferences.
	 *
	 * @param root
	 * @param message
	 * @param level
	 * @param printScreen If true, the message (if otherwise shown) will be printed to the screen. If false, it never
	 * will be, though it will still be logged to the log file.
	 * @throws IOException
	 */
	public static synchronized void LogDebug(File root, String message, LogLevel level, boolean printScreen) throws IOException {
		//If debug mode is on in the prefs, we want to log this to the screen too
		if(Prefs.DebugMode() || Prefs.ShowWarnings() || level == LogLevel.ERROR) {
			String color = "";
			Level lev = Level.INFO;
			boolean show = false;
			switch(level) {
				case ERROR:
					color = TermColors.RED;
					lev = Level.SEVERE;
					show = true;
					break;
				case WARNING:
					color = TermColors.YELLOW;
					lev = Level.WARNING;
					if(Prefs.DebugMode() || Prefs.ShowWarnings()) {
						show = true;
					}
					break;
				case INFO:
					color = TermColors.GREEN;
					lev = Level.INFO;
					if(Prefs.DebugMode()) {
						show = true;
					}
					break;
				case DEBUG:
					color = TermColors.BRIGHT_BLUE;
					lev = Level.INFO;
					if(Prefs.DebugMode()) {
						show = true;
					}
					break;
				case VERBOSE:
					color = TermColors.WHITE;
					lev = Level.INFO;
					if(Prefs.DebugMode()) {
						show = true;
					}
					break;
			}
			if(show && printScreen) {
				Static.getLogger().log(lev, "{0}{1}{2}", new Object[]{color, message, TermColors.reset()});
			}
		}
		String timestamp = DateUtils.ParseCalendarNotation("%Y-%M-%D %h:%m.%s - ");
		QuickAppend(Static.debugLogFile(root), timestamp + "[" + Implementation.GetServerType().getBranding() + "]"
				+ message + Static.LF());
	}

	public static void QuickAppend(FileWriter f, String message) throws IOException {
		f.append(message);
		f.flush();
	}

	public static boolean hasCHPermission(String functionName, Environment env) {
		//The * label completely overrides everything
		if(GLOBAL_PERMISSION.equals(env.getEnv(GlobalEnv.class).GetLabel())) {
			return true;
		}
		MCPlayer player = env.getEnv(CommandHelperEnvironment.class).GetPlayer();
		return player == null || player.isOp()
				|| player.hasPermission("ch.func.use." + functionName)
				|| player.hasPermission("commandhelper.func.use." + functionName);
	}

	public static String Logo() {
		String logo = Installer.parseISToString(Static.class.getResourceAsStream("/mainlogo"));
		logo = logo.replaceAll("( +)", TermColors.BG_BLACK + "$1");
		logo = logo.replaceAll("_", TermColors.BG_RED + TermColors.RED + "_");
		logo = logo.replaceAll("/", TermColors.BG_BRIGHT_WHITE + TermColors.WHITE + "/");
		logo = logo.replace("\n", TermColors.reset() + "\n");
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
	 * Pulls out the MCChatColors from the string, and replaces them with the nearest match ANSI terminal color.
	 *
	 * @param mes If null, simply returns null
	 * @return
	 */
	public static String MCToANSIColors(String mes) {
		//Pull out the MC colors
		if(mes == null) {
			return null;
		}
		return mes
				.replaceAll("§0", TermColors.BLACK)
				.replaceAll("§1", TermColors.BLUE)
				.replaceAll("§2", TermColors.GREEN)
				.replaceAll("§3", TermColors.CYAN)
				.replaceAll("§4", TermColors.RED)
				.replaceAll("§5", TermColors.MAGENTA)
				.replaceAll("§6", TermColors.YELLOW)
				.replaceAll("§7", TermColors.WHITE)
				.replaceAll("§8", TermColors.BRIGHT_BLACK)
				.replaceAll("§9", TermColors.BRIGHT_BLUE)
				.replaceAll("§a", TermColors.BRIGHT_GREEN)
				.replaceAll("§b", TermColors.BRIGHT_CYAN)
				.replaceAll("§c", TermColors.BRIGHT_RED)
				.replaceAll("§d", TermColors.BRIGHT_MAGENTA)
				.replaceAll("§e", TermColors.BRIGHT_YELLOW)
				.replaceAll("§f", TermColors.BRIGHT_WHITE)
				.replaceAll("§k", "") //Uh, no equivalent for "random"
				.replaceAll("§l", TermColors.BOLD)
				.replaceAll("§m", TermColors.STRIKE)
				.replaceAll("§n", TermColors.UNDERLINE)
				.replaceAll("§o", TermColors.ITALIC)
				.replaceAll("§r", TermColors.RESET);

	}

	public static MCCommandSender GetInjectedPlayer(String name) {
		return INJECTED_PLAYERS.get(name);
	}

	public static void InjectPlayer(MCCommandSender player) {
		String name = player.getName();
		if("CONSOLE".equals(name)) {
			name = "~console";
		}
		INJECTED_PLAYERS.put(name, player);
	}

	/**
	 * Removes a player into the global player proxy system. Returns the player removed (or null if none were injected).
	 *
	 * @param player
	 * @return
	 */
	public static MCCommandSender UninjectPlayer(MCCommandSender player) {
		String name = player.getName();
		if("CONSOLE".equals(name)) {
			name = "~console";
		}
		return INJECTED_PLAYERS.remove(name);
	}

	public static void InjectEntity(MCEntity entity) {
		injectedEntity = entity;
	}

	public static void UninjectEntity(MCEntity entity) {
		injectedEntity = null;
	}

	public static void HostnameCache(final String name, final InetSocketAddress address) {
		CommandHelperPlugin.hostnameLookupThreadPool.submit(new Runnable() {
			@Override
			public void run() {
				CommandHelperPlugin.hostnameLookupCache.put(name, address.getHostName());
			}
		});
	}

	public static void SetPlayerHost(String playerName, String host) {
		HOST_CACHE.put(playerName, host);
	}

	public static String GetHost(MCPlayer p) {
		return HOST_CACHE.get(p.getName());
	}

	public static void AssertPlayerNonNull(MCPlayer p, Target t) throws ConfigRuntimeException {
		if(p == null) {
			throw new CREPlayerOfflineException("No player was specified!", t);
		}
	}

	public static long msToTicks(long ms) {
		return ms / 50;
	}

	public static long ticksToMs(long ticks) {
		return ticks * 50;
	}

	public static void AssertNonNull(Object var, String message) throws NullPointerException {
		if(var == null) {
			throw new NullPointerException(message);
		}
	}

	/**
	 * Generates a new environment, assuming that the jar has a folder next to it named CommandHelper, and that folder
	 * is the root.
	 *
	 * @return
	 * @throws IOException
	 * @throws DataSourceException
	 * @throws URISyntaxException
	 */
	public static Environment GenerateStandaloneEnvironment(boolean install) throws IOException, DataSourceException, URISyntaxException, Profiles.InvalidProfileException {
		File platformFolder = MethodScriptFileLocations.getDefault().getConfigDirectory();
		if(install) {
			Installer.Install(platformFolder);
		}
		ConnectionMixinFactory.ConnectionMixinOptions options = new ConnectionMixinFactory.ConnectionMixinOptions();
		options.setWorkingDirectory(platformFolder);
		Profiles profiles = null;
		if(MethodScriptFileLocations.getDefault().getProfilesFile().exists()) {
			profiles = new ProfilesImpl(MethodScriptFileLocations.getDefault().getProfilesFile());
		}
		PersistenceNetwork persistenceNetwork = new PersistenceNetworkImpl(MethodScriptFileLocations.getDefault().getPersistenceConfig(),
				new URI(URLEncoder.encode("sqlite://" + new File(platformFolder, "persistence.db").getCanonicalPath().replace('\\', '/'), "UTF-8")), options);
		GlobalEnv gEnv = new GlobalEnv(new MethodScriptExecutionQueue("MethodScriptExecutionQueue", "default"),
				new Profiler(MethodScriptFileLocations.getDefault().getProfilerConfigFile()), persistenceNetwork, platformFolder,
				profiles, new TaskManagerImpl());
		gEnv.SetLabel(GLOBAL_PERMISSION);
		return Environment.createEnvironment(gEnv, new CompilerEnvironment());
	}

	public static Environment GenerateStandaloneEnvironment() throws IOException, DataSourceException, URISyntaxException, Profiles.InvalidProfileException {
		return GenerateStandaloneEnvironment(true);
	}

	/**
	 * Asserts that all the args are not CNulls. If so, throws a ConfigRuntimeNullPointerException
	 *
	 * @param t
	 * @param args
	 * @throws ConfigRuntimeException
	 */
	public static void AssertNonCNull(Target t, Mixed... args) throws ConfigRuntimeException {
		for(Mixed arg : args) {
			if(arg instanceof CNull) {
				throw new CRENullPointerException("Argument was null, and nulls are not allowed.", t);
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
	 * Returns the actual file location, given the script's partial (or absolute) file path, and depending on the
	 * context, the correct File object. Security checking is not done at this stage, this merely transforms the path
	 * into the correct File object. Additionally, if arg is null, then the default is returned. If it is known that the
	 * arg won't ever be null, null may be set as the default. Except in cases where both arg and def are null, this
	 * function will never return null. If the arg starts with ~, it is replaced with the user's home directory, as
	 * defined by the system property user.home.
	 *
	 * This generally condenses a 5 or 6 line operation into 1 line.
	 *
	 * @param arg The path to parse. May be null.
	 * @param env The environment, required to properly resolve relative paths.
	 * @param t Code target, for errors.
	 * @param def The default file, which is returned if {@code arg} is null. (Maybe also be null).
	 * @return
	 */
	public static File GetFileFromArgument(String arg, Environment env, Target t, File def)
			throws ConfigRuntimeException {
		if(arg == null) {
			return def;
		}
		if(arg.startsWith("~")) {
			arg = System.getProperty("user.home") + arg.substring(1);
		}
		File f = new File(arg);
		if(f.isAbsolute()) {
			return f;
		}
		//Ok, it's not absolute, so we need to see if we're in cmdline mode or not.
		//If so, we use the root directory, not the target.
		if(env != null && InCmdLine(env, true)) {
			return new File(env.getEnv(GlobalEnv.class).GetRootFolder(), arg);
		} else if(t.file() == null) {
			throw new CREIOException("Unable to receive a non-absolute file with an unknown target", t);
		} else {
			return new File(t.file().getParent(), arg);
		}
	}

	/**
	 * Returns true if currently running in cmdline mode. If the environment is null, or the GlobalEnv is
	 * not available, then defaultValue is returned.
	 *
	 * @param environment
	 * @param defaultValue What should be returned if the environment is null or GlobalEnv is not present. (Happens
	 * during compile time.)
	 * @return
	 */
	public static boolean InCmdLine(Environment environment, boolean defaultValue) {
		if(environment == null || !environment.hasEnv(GlobalEnv.class)) {
			return defaultValue;
		}
		return environment.getEnv(GlobalEnv.class).GetCustom("cmdline") instanceof Boolean
				&& (Boolean) environment.getEnv(GlobalEnv.class).GetCustom("cmdline");
	}

	/**
	 * This verifies that the type required is actually present, and returns the value, cast to the appropriate type,
	 * or, if not the correct type, a CRE.
	 * <p>
	 * Note that this does not do type coersion, and therefore does not work on primitives, and is only meant for
	 * arrays, closures, and other complex types.
	 *
	 * @param <T> The type desired to be cast to
	 * @param type The type desired to be cast to
	 * @param args The array of arguments.
	 * @param argNumber The argument number, used both for grabbing the correct argument from args, and building the
	 * error message if the cast cannot occur.
	 * @param func The function, in case this errors out, to build the error message.
	 * @param t The code target
	 * @return The value, cast to the desired type.
	 */
	public static <T extends Mixed> T AssertType(Class<T> type, Mixed[] args, int argNumber, Function func, Target t) {
		Mixed value = args[argNumber];
		if(!type.isAssignableFrom(value.getClass())) {
			typeof todesired = ClassDiscovery.GetClassAnnotation(type, typeof.class);
			CClassType toactual = value.typeof();
			if(todesired != null) {
				throw new CRECastException("Argument " + (argNumber + 1) + " of " + func.getName() + " was expected to be a "
						+ todesired.value() + ", but " + toactual + " \"" + value.val() + "\" was found.", t);
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
		if(object == null) {
			return CNull.NULL;
		} else if(object instanceof Boolean) {
			return CBoolean.get((boolean) object);
		} else if((object instanceof Byte) || (object instanceof Short) || (object instanceof Integer) || (object instanceof Long)) {
			return new CInt((long) object, t);
		} else if((object instanceof Float) || (object instanceof Double)) {
			return new CDouble((double) object, t);
		} else if(object instanceof Character) {
			return new CString((char) object, t);
		} else if(object instanceof String) {
			return new CString((String) object, t);
		} else if(object instanceof StringBuffer) {
			return new CResource<>((StringBuffer) object, new CResource.ResourceToString() {
				@Override
				public String getString(CResource res) {
					return res.getResource().toString();
				}
			}, t);
		} else if(object instanceof XMLDocument) {
			return new CResource<>((XMLDocument) object, t);
		} else if(object instanceof Construct) {
			return (Construct) object;
		} else if(object instanceof boolean[]) {
			boolean[] array = (boolean[]) object;
			CArray r = new CArray(t);
			for(boolean b : array) {
				r.push(CBoolean.get(b), t);
			}
			return r;
		} else if(object instanceof byte[]) {
			return CByteArray.wrap((byte[]) object, t);
		} else if(object instanceof char[]) {
			char[] array = (char[]) object;
			CArray r = new CArray(t);
			for(char c : array) {
				r.push(new CString(c, t), t);
			}
			return r;
		} else if(object instanceof short[]) {
			short[] array = (short[]) object;
			CArray r = new CArray(t);
			for(short s : array) {
				r.push(new CInt(s, t), t);
			}
			return r;
		} else if(object instanceof int[]) {
			int[] array = (int[]) object;
			CArray r = new CArray(t);
			for(int i : array) {
				r.push(new CInt(i, t), t);
			}
			return r;
		} else if(object instanceof long[]) {
			long[] array = (long[]) object;
			CArray r = new CArray(t);
			for(long l : array) {
				r.push(new CInt(l, t), t);
			}
			return r;
		} else if(object instanceof float[]) {
			float[] array = (float[]) object;
			CArray r = new CArray(t);
			for(float f : array) {
				r.push(new CDouble(f, t), t);
			}
			return r;
		} else if(object instanceof double[]) {
			double[] array = (double[]) object;
			CArray r = new CArray(t);
			for(double d : array) {
				r.push(new CDouble(d, t), t);
			}
			return r;
		} else if(object instanceof Object[]) {
			CArray r = new CArray(t);
			for(Object o : (Object[]) object) {
				r.push((o == object) ? r : getMSObject(o, t), t);
			}
			return r;
		} else if(object instanceof Collection) {
			return getMSObject(((Collection) object).toArray(), t);
		} else if(object instanceof Map) {
			Map map = ((Map) object);
			CArray r = new CArray(t);
			for(Object key : map.keySet()) {
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
	public static Object getJavaObject(Mixed construct) {
		if((construct == null) || (construct instanceof CNull)) {
			return null;
		} else if(construct instanceof CVoid) {
			return "";
		} else if(construct instanceof CBoolean) {
			return ((CBoolean) construct).getBoolean();
		} else if(construct instanceof CInt) {
			return ((CInt) construct).getInt();
		} else if(construct instanceof CDouble) {
			return ((CDouble) construct).getDouble();
		} else if(construct instanceof CString) {
			return construct.val();
		} else if(construct instanceof CByteArray) {
			return ((CByteArray) construct).asByteArrayCopy();
		} else if(construct instanceof CResource) {
			return ((CResource) construct).getResource();
		} else if(construct.isInstanceOf(CArray.TYPE)) {
			CArray array = (CArray) construct;
			if(array.isAssociative()) {
				HashMap<String, Object> map = new HashMap<>();
				for(Mixed key : array.keySet()) {
					Mixed c = array.get(key.val(), Target.UNKNOWN);
					map.put(key.val(), (c == array) ? map : getJavaObject(c));
				}
				return map;
			} else {
				Object[] a = new Object[(int) array.size()];
				boolean nullable = false;
				Class<?> clazz = null;
				for(int i = 0; i < array.size(); i++) {
					Mixed c = array.get(i, Target.UNKNOWN);
					if(c == array) {
						a[i] = a;
					} else {
						a[i] = getJavaObject(array.get(i, Target.UNKNOWN));
					}
					if(a[i] != null) {
						if(clazz == null) {
							clazz = a[i].getClass();
						} else if(!clazz.equals(Object.class)) {
							//to test if it is possible to return something more specific than Object[]
							Class<?> cl = a[i].getClass();
							while(!clazz.isAssignableFrom(cl)) {
								clazz = clazz.getSuperclass();
							}
						}
					} else {
						nullable = true;
					}
				}
				if((clazz != null) && (!clazz.equals(Object.class))) {
					if(clazz.equals(Boolean.class) && !nullable) {
						boolean[] r = new boolean[a.length];
						for(int i = 0; i < a.length; i++) {
							r[i] = (boolean) a[i];
						}
						return r;
					}
					if(clazz.equals(Long.class) && !nullable) {
						long[] r = new long[a.length];
						for(int i = 0; i < a.length; i++) {
							r[i] = (long) a[i];
						}
						return r;
					} else if(clazz.equals(Double.class) && !nullable) {
						double[] r = new double[a.length];
						for(int i = 0; i < a.length; i++) {
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

	/**
	 * Given a locale string, returns the java locale, or null if it can't be found.
	 *
	 * @param fromLocaleString
	 * @return
	 */
	public static Locale GetLocale(String fromLocaleString) {
		for(Locale loc : Locale.getAvailableLocales()) {
			if(loc.toString().toLowerCase().equals(fromLocaleString.toLowerCase())) {
				return loc;
			}
		}
		return null;
	}
}
