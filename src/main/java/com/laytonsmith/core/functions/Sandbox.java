package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.PureUtilities.TermColors;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.PureUtilities.ZipReader;
import com.laytonsmith.abstraction.MCEnchantment;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.MCPlayerInventory;
import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.annotations.api;
import com.laytonsmith.annotations.hide;
import com.laytonsmith.annotations.noboilerplate;
import com.laytonsmith.commandhelper.BukkitDirtyRegisteredListener;
import com.laytonsmith.core.ArgumentValidation;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.MethodScriptCompiler;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.Security;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CBoolean;
import com.laytonsmith.core.constructs.CDouble;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.constructs.CNull;
import com.laytonsmith.core.constructs.CResource;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.CVoid;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.CommandHelperEnvironment;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.GlobalEnv;
import com.laytonsmith.core.events.BoundEvent;
import com.laytonsmith.core.exceptions.CRE.CREBindException;
import com.laytonsmith.core.exceptions.CRE.CRECastException;
import com.laytonsmith.core.exceptions.CRE.CREEnchantmentException;
import com.laytonsmith.core.exceptions.CRE.CREIOException;
import com.laytonsmith.core.exceptions.CRE.CREIncludeException;
import com.laytonsmith.core.exceptions.CRE.CRENotFoundException;
import com.laytonsmith.core.exceptions.CRE.CREPlayerOfflineException;
import com.laytonsmith.core.exceptions.CRE.CRESecurityException;
import com.laytonsmith.core.exceptions.CRE.CREThrowable;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigCompileGroupException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import org.bukkit.event.Cancellable;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Random;

/**
 *
 */
public class Sandbox {

	public static String docs() {
		return "This class is for functions that are experimental. They don't actually get added"
				+ " to the documentation, and are subject to removal at any point in time, nor are they"
				+ " likely to have good documentation.";
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class super_cancel extends AbstractFunction {

		@Override
		public String getName() {
			return "super_cancel";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0};
		}

		@Override
		public String docs() {
			return "void {} \"Super Cancels\" an event. This only will work if play-dirty is set to true. If an event is"
					+ " super cancelled, not only is the cancelled flag set to true, the event stops propagating down, so"
					+ " no other plugins (as in other server plugins, not just CH scripts) will receive the event at all "
					+ " (other than monitor level plugins). This is useful for overridding"
					+ " event handlers for plugins that don't respect the cancelled flag. This function hooks into the play-dirty"
					+ " framework that injects custom event handlers into bukkit.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREBindException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_3_0;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			BoundEvent.ActiveEvent original = environment.getEnv(GlobalEnv.class).GetEvent();
			if(original == null) {
				throw new CREBindException("is_cancelled cannot be called outside an event handler", t);
			}
			if(original.getUnderlyingEvent() != null && original.getUnderlyingEvent() instanceof Cancellable
					&& original.getUnderlyingEvent() instanceof org.bukkit.event.Event) {
				((Cancellable) original.getUnderlyingEvent()).setCancelled(true);
				BukkitDirtyRegisteredListener.setCancelled((org.bukkit.event.Event) original.getUnderlyingEvent());
			}
			environment.getEnv(GlobalEnv.class).GetEvent().setCancelled(true);
			return CVoid.VOID;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class enchant_inv_unsafe extends AbstractFunction {

		@Override
		public String getName() {
			return "enchant_inv_unsafe";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{3, 4};
		}

		@Override
		public String docs() {
			return "void {[player], slot, type, level} Works the same as enchant_inv, except anything goes. "
					+ " You can enchant a fish with a level 5000 enchantment if you wish. Side effects"
					+ " may include nausia, dry mouth, insomnia, or server crashes. (Seriously, this might"
					+ " crash your server, be careful with it.)";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CREEnchantmentException.class,
				CREPlayerOfflineException.class, CRENotFoundException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public CHVersion since() {
			return CHVersion.V0_0_0;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			MCPlayer m = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
			int offset = 1;
			if(args.length == 4) {
				m = Static.GetPlayer(args[0].val(), t);
				offset = 0;
			}
			Static.AssertPlayerNonNull(m, t);
			MCItemStack is;
			if(args[1 - offset] instanceof CNull) {
				is = m.getItemInHand();
			} else {
				int slot = Static.getInt32(args[1 - offset], t);
				MCPlayerInventory pinv = m.getInventory();
				if(pinv == null) {
					throw new CRENotFoundException(
							"Could not find the inventory of the given player (are you running in cmdline mode?)", t);
				}
				is = pinv.getItem(slot);
			}
			CArray enchantArray = new CArray(t);
			if(!(args[2 - offset] instanceof CArray)) {
				enchantArray.push(args[2 - offset], t);
			} else {
				enchantArray = (CArray) args[2 - offset];
			}

			CArray levelArray = new CArray(t);
			if(!(args[3 - offset] instanceof CArray)) {
				levelArray.push(args[3 - offset], t);
			} else {
				levelArray = (CArray) args[3 - offset];
			}
			for(String key : enchantArray.stringKeySet()) {
				MCEnchantment e = StaticLayer.GetEnchantmentByName(Enchantments.ConvertName(enchantArray.get(key, t).val()));
				if(e == null) {
					throw new CREEnchantmentException(enchantArray.get(key, t).val().toUpperCase() + " is not a valid enchantment type", t);
				}
				int level = Static.getInt32(new CString(Enchantments.ConvertLevel(levelArray.get(key, t).val()), t), t);

				is.addUnsafeEnchantment(e, level);
			}
			return CVoid.VOID;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class raw_set_pvanish extends AbstractFunction {

		@Override
		public String getName() {
			return "raw_set_pvanish";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2, 3};
		}

		@Override
		public String docs() {
			return "void {[player], isVanished, otherPlayer} Sets the visibility"
					+ " of the current player (or the one specified) to visible or invisible"
					+ " (based on the value of isVanished) from the view of the otherPlayer."
					+ " This is the raw access function, you probably shouldn't use this, as"
					+ " the CommandHelper vanish api functions will probably be easier to use.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREPlayerOfflineException.class};
		}

		@Override
		public boolean isRestricted() {
			return true; //lol, very
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			MCPlayer me;
			boolean isVanished;
			MCPlayer other;
			if(args.length == 2) {
				me = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
				isVanished = Static.getBoolean(args[0], t);
				other = Static.GetPlayer(args[1], t);
			} else {
				me = Static.GetPlayer(args[0], t);
				isVanished = Static.getBoolean(args[1], t);
				other = Static.GetPlayer(args[2], t);
			}

			other.setVanished(isVanished, me);

			return CVoid.VOID;
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_3_0;
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class raw_pcan_see extends AbstractFunction {

		@Override
		public String getName() {
			return "raw_pcan_see";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		@Override
		public String docs() {
			return "boolean {[player], other} Returns a boolean stating if the other player can"
					+ " see this player or not. This is the raw access function, you probably shouldn't use this, as"
					+ " the CommandHelper vanish api functions will probably be easier to use.";
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
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			MCPlayer me;
			MCPlayer other;
			if(args.length == 1) {
				me = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
				other = Static.GetPlayer(args[0], t);
			} else {
				me = Static.GetPlayer(args[0], t);
				other = Static.GetPlayer(args[1], t);
			}
			return CBoolean.get(me.canSee(other));
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_3_0;
		}
	}

	private static String GenerateMooSaying(String text) {
		String[] saying = text.split("\r\n|\n|\n\r");
		int longest = 0;
		for(String s : saying) {
			longest = java.lang.Math.max(longest, s.length());
		}
		String divider = "";
		for(int i = 0; i < longest + 4; i++) {
			divider += "-";
		}
		String[] lines = new String[saying.length];
		for(int i = 0; i < saying.length; i++) {
			int spaces = longest - saying[i].length();
			String sSpaces = "";
			for(int j = 0; j < spaces; j++) {
				sSpaces += " ";
			}
			lines[i] = "| " + saying[i] + sSpaces + " |";
		}
		return divider + "\n"
				+ StringUtils.Join(lines, "\n") + "\n"
				+ divider + "\n";
	}

	@api
	@hide("This is an easter egg.")
	public static class moo extends DummyFunction {

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			return new CString(GenerateMooSaying(args[0].val())
					+ " \\   ^__^\n"
					+ "  \\  (oo)\\_______\n"
					+ "     (__)\\       )\\/\\\n"
					+ "         ||----w |\n"
					+ "         ||     ||\n", t);
		}

	}

	@api
	@hide("This is an easter egg.")
	public static class moo2 extends DummyFunction {

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			return new CString(
					GenerateMooSaying(args[0].val())
					+ "              ^__^   /\n"
					+ "      _______/(oo)  /\n"
					+ " /\\/(        /(__)\n"
					+ "      | w----||\n"
					+ "      ||     ||\n", t);
		}

	}

	@api
	@hide("This is an easter egg.")
	public static class upupdowndownleftrightleftrightbastart extends DummyFunction {

		@Override
		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			return new CString("  .-*)) `*-.\n"
					+ " /*  ((*   *'.\n"
					+ "|   *))  *   *\\\n"
					+ "| *  ((*   *  /\n"
					+ " \\  *))  *  .'\n"
					+ "  '-.((*_.-'", t);
		}

	}

	@api
	@hide("This is an easter egg")
	@noboilerplate
	public static class norway extends DummyFunction {

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			Function color = new Echoes.color();
			String red = color.exec(t, environment, args.length == 3 ? args[0] : new CString("RED", t)).val();
			String white = color.exec(t, environment, args.length == 3 ? args[1] : new CString("WHITE", t)).val();
			String blue = color.exec(t, environment, args.length == 3 ? args[2] : new CString("BLUE", t)).val();
			int multiplier = 2;
			char c = '=';
			String one = multiply(c, 1 * multiplier);
			String two = multiply(c, 2 * multiplier);
			String six = multiply(c, 6 * multiplier);
			String seven = multiply(c, 7 * multiplier);
			String twelve = multiply(c, 12 * multiplier);
			String thirteen = multiply(c, 13 * multiplier);
			String twentytwo = multiply(c, 22 * multiplier);
			for(int i = 0; i < 6; ++i) {
				System.out.println(Static.MCToANSIColors(red + six + white + one + blue + two + white + one + red + twelve) + TermColors.RESET);
			}
			System.out.println(Static.MCToANSIColors(white + seven + blue + two + white + thirteen) + TermColors.RESET);
			for(int i = 0; i < 2; ++i) {
				System.out.println(Static.MCToANSIColors(blue + twentytwo) + TermColors.RESET);
			}
			System.out.println(Static.MCToANSIColors(white + seven + blue + two + white + thirteen) + TermColors.RESET);
			for(int i = 0; i < 6; ++i) {
				System.out.println(Static.MCToANSIColors(red + six + white + one + blue + two + white + one + red + twelve) + TermColors.RESET);
			}

			return CVoid.VOID;
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0, 3};
		}

		public static String multiply(char c, int times) {
			StringBuilder b = new StringBuilder();
			for(int i = 0; i < times; ++i) {
				b.append(c);
			}
			return b.toString();
		}

	}

	@api
	public static class srand extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class};
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
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			Random r;
			try {
				r = (Random) ArgumentValidation.getObject(args[0], t, CResource.class).getResource();
			} catch(ClassCastException ex) {
				throw new CRECastException("Expected a resource of type " + ResourceManager.ResourceTypes.RANDOM, t, ex);
			}
			double d = r.nextDouble();
			return new CDouble(d, t);
		}

		@Override
		public String getName() {
			return "srand";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "double {randomResource} Returns a new rand value. If the seed used to create the resource is the same, each resulting"
					+ " series of numbers will be the same.";
		}

		@Override
		public Version since() {
			return CHVersion.V3_3_2;
		}

	}

	@api
	public static class test_composite_function extends CompositeFunction {

		@Override
		protected String script() {
			// Note that @a is not going to be in scope for the user's scripts.
			return "@a = ((@arguments[0] + @arguments[1]) > 0);"
					+ "return(@a);";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class};
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
		public String getName() {
			return "test_composite_function";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "boolean {a, b} This is a test function, which demonstrates to extension authors how to make a composite function."
					+ " It returns true if a and b added together are greater than 0, false otherwise.";
		}

		@Override
		public Version since() {
			return CHVersion.V3_3_2;
		}

	}

	@api
	public static class x_recompile_includes extends AbstractFunction {

		@Override
		public String getName() {
			return "x_recompile_includes";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "int {path} Reads and compiles specified *.ms files. This can be used for files already compiled"
					+ " with include(). Scripts that then include() these files will use the updated code."
					+ " The path can be a directory or file. It is executed recursively through all subdirectories."
					+ " If there's a compile error in any of the files, the function will throw an exception and other"
					+ " scripts will continue to use the previous version of the code when included. Returns number"
					+ " of files recompiled.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREIOException.class, CREIncludeException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_3_2;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Construct exec(Target t, Environment env, Construct... args) throws ConfigRuntimeException {
			File file = Static.GetFileFromArgument(args[0].val(), env, t, null);
			int num = 0;
			if(Security.CheckSecurity(file)) {
				if(file.isDirectory()) {
					HashMap<File, ParseTree> files = compileDirectory(file, t);
					IncludeCache.addAll(files);
					num = files.size();
				} else if(IncludeCache.has(file)) {
					IncludeCache.add(file, compileFile(file, t));
					num = 1;
				}
			} else {
				throw new CRESecurityException("The script cannot access " + file
						+ " due to restrictions imposed by the base-dir setting.", t);
			}
			return new CInt(num, t);
		}

		private HashMap<File, ParseTree> compileDirectory(File file, Target t) {
			HashMap<File, ParseTree> newFiles = new HashMap<>();
			File[] files = file.listFiles();
			if(files != null) {
				for(File f : files) {
					if(f.isDirectory()) {
						newFiles.putAll(compileDirectory(f, t));
					} else if(IncludeCache.has(f)) {
						newFiles.put(f, compileFile(f, t));
					}
				}
			}
			return newFiles;
		}

		private ParseTree compileFile(File file, Target t) {
			try {
				String s = new ZipReader(file).getFileContents();
				return MethodScriptCompiler.compile(MethodScriptCompiler.lex(s, file, true));
			} catch(ConfigCompileException ex) {
				throw new CREIncludeException("There was a compile error when trying to recompile the script at "
						+ file + "\n" + ex.getMessage() + " :: " + file.getName() + ":" + ex.getLineNum(), t);
			} catch(ConfigCompileGroupException ex) {
				StringBuilder b = new StringBuilder();
				b.append("There were compile errors when trying to recompile the script at ").append(file).append("\n");
				for(ConfigCompileException e : ex.getList()) {
					b.append(e.getMessage()).append(" :: ").append(e.getFile().getName()).append(":")
							.append(e.getLineNum()).append("\n");
				}
				throw new CREIncludeException(b.toString(), t);
			} catch(IOException ex) {
				throw new CREIOException("The script at " + file + " could not be found or read in.", t);
			}
		}
	}
}
