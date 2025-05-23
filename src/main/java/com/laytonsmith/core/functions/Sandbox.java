package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.Common.StreamUtils;
import com.laytonsmith.abstraction.enums.MCChatColor;
import com.laytonsmith.core.FileWriteMode;
import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.PureUtilities.TermColors;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.PureUtilities.ZipReader;
import com.laytonsmith.annotations.api;
import com.laytonsmith.annotations.hide;
import com.laytonsmith.annotations.noboilerplate;
import com.laytonsmith.commandhelper.BukkitDirtyRegisteredListener;
import com.laytonsmith.core.ArgumentValidation;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.MethodScriptCompiler;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.Security;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.compiler.analysis.StaticAnalysis;
import com.laytonsmith.core.constructs.CByteArray;
import com.laytonsmith.core.constructs.CDouble;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.constructs.CResource;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.CVoid;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.CommandHelperEnvironment;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.GlobalEnv;
import com.laytonsmith.core.environments.StaticRuntimeEnv;
import com.laytonsmith.core.events.BoundEvent;
import com.laytonsmith.core.exceptions.CRE.CREBindException;
import com.laytonsmith.core.exceptions.CRE.CRECastException;
import com.laytonsmith.core.exceptions.CRE.CREIOException;
import com.laytonsmith.core.exceptions.CRE.CREIncludeException;
import com.laytonsmith.core.exceptions.CRE.CRESecurityException;
import com.laytonsmith.core.exceptions.CRE.CREThrowable;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigCompileGroupException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.natives.interfaces.Mixed;
import org.bukkit.event.Cancellable;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import org.apache.commons.io.FileUtils;

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
					+ " (other than monitor level plugins). This is useful for overriding"
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
		public MSVersion since() {
			return MSVersion.V3_3_0;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
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
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
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
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
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
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
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
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCChatColor red = MCChatColor.RED;
			MCChatColor white = MCChatColor.WHITE;
			MCChatColor blue = MCChatColor.BLUE;
			int multiplier = 2;
			char c = '=';
			String one = multiply(c, 1 * multiplier);
			String two = multiply(c, 2 * multiplier);
			String six = multiply(c, 6 * multiplier);
			String seven = multiply(c, 7 * multiplier);
			String twelve = multiply(c, 12 * multiplier);
			String thirteen = multiply(c, 13 * multiplier);
			String twentytwo = multiply(c, 22 * multiplier);
			PrintStream out = StreamUtils.GetSystemOut();
			String vertical = Static.MCToANSIColors(red + six + white + one + blue + two + white + one + red + twelve);
			for(int i = 0; i < 6; ++i) {
				out.println(vertical + TermColors.RESET);
			}
			out.println(Static.MCToANSIColors(white + seven + blue + two + white + thirteen) + TermColors.RESET);
			for(int i = 0; i < 2; ++i) {
				out.println(Static.MCToANSIColors(blue + twentytwo) + TermColors.RESET);
			}
			out.println(Static.MCToANSIColors(white + seven + blue + two + white + thirteen) + TermColors.RESET);
			for(int i = 0; i < 6; ++i) {
				out.println(vertical + TermColors.RESET);
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
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			Random r;
			try {
				r = (Random) ArgumentValidation.getObject(args[0], t, CResource.class).getResource();
			} catch (ClassCastException ex) {
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
			return "double {Resource} Returns a new rand value using the provided a RANDOM Resource."
					+ " If the seed used to create the resource is the same, each resulting"
					+ " series of numbers will be the same.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_2;
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
			return MSVersion.V3_3_2;
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
			return "int {path} Recompiles specified files already compiled with include()."
					+ " If there's no compile errors, scripts that then include() these files will use the updated code."
					+ " Note that this bypasses Static Analysis, even if you have it enabled."
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
		public MSVersion since() {
			return MSVersion.V3_3_2;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			try {
				File file = Static.GetFileFromArgument(args[0].val(), env, t, null).getCanonicalFile();
				if(!Static.InCmdLine(env, true) && !Security.CheckSecurity(file)) {
					throw new CRESecurityException("The script cannot access " + file
							+ " due to restrictions imposed by the base-dir setting.", t);
				}
				IncludeCache cache = env.getEnv(StaticRuntimeEnv.class).getIncludeCache();
				if(file.isDirectory()) {
					Map<File, ParseTree> includes = new HashMap<>();
					compileDirectory(includes, file, env, cache, t);
					cache.addAll(includes);
					return new CInt(includes.size(), t);
				} else if(cache.has(file)) {
					cache.add(file, compileFile(file, env, t));
					return new CInt(1, t);
				}
				return new CInt(0, t);
			} catch (IOException ex) {
				throw new CREIOException(ex.getMessage(), t, ex);
			}
		}

		private void compileDirectory(Map<File, ParseTree> includes, File dir, Environment env, IncludeCache cache, Target t) {
			File[] files = dir.listFiles();
			if(files != null) {
				for(File f : files) {
					if(f.isDirectory()) {
						compileDirectory(includes, f, env, cache, t);
					} else if(cache.has(f)) {
						includes.put(f, compileFile(f, env, t));
					}
				}
			}
		}

		private ParseTree compileFile(File file, Environment env, Target t) {
			try {
				String s = new ZipReader(file).getFileContents();
				StaticAnalysis staticAnalysis = new StaticAnalysis(true);
				staticAnalysis.setLocalDisabled(true);
				return MethodScriptCompiler.compile(MethodScriptCompiler.lex(s, env, file, true), env, env.getEnvClasses(),
						staticAnalysis);
			} catch (ConfigCompileException ex) {
				throw new CREIncludeException("There was a compile error when trying to recompile the script at "
						+ file + "\n" + ex.getMessage() + " :: " + file.getName() + ":" + ex.getLineNum(), t);
			} catch (ConfigCompileGroupException ex) {
				StringBuilder b = new StringBuilder();
				b.append("There were compile errors when trying to recompile the script at ").append(file).append("\n");
				for(ConfigCompileException e : ex.getList()) {
					b.append(e.getMessage()).append(" :: ").append(e.getFile().getName()).append(":")
							.append(e.getLineNum()).append("\n");
				}
				throw new CREIncludeException(b.toString(), t);
			} catch (IOException ex) {
				throw new CREIOException("The script at " + file + " could not be found or read in.", t);
			}
		}
	}

	@api
	@noboilerplate
	public static class x_write extends AbstractFunction {
		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRESecurityException.class, CREIOException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			if(!Static.InCmdLine(environment, true)) {
				throw new CRESecurityException(getName() + " is only available in cmdline mode.", t);
			}
			File location = Static.GetFileFromArgument(args[0].val(), environment, t, null);
			if(location.isDirectory()) {
				throw new CREIOException("Path already exists, and is a directory", t);
			}

			byte[] content;
			if(!(args[1].isInstanceOf(CByteArray.TYPE))) {
				content = args[1].val().getBytes(Charset.forName("UTF-8"));
			} else {
				content = ArgumentValidation.getByteArray(args[1], t).asByteArrayCopy();
			}
			FileWriteMode mode = FileWriteMode.SAFE_WRITE;
			if(args.length > 2) {
				mode = ArgumentValidation.getEnum(args[2], FileWriteMode.class, t);
			}
			if(mode == FileWriteMode.SAFE_WRITE && location.exists()) {
				throw new CREIOException("File already exists, refusing to overwrite.", t);
			}

			try {
				FileUtils.writeByteArrayToFile(location, content, mode == FileWriteMode.APPEND);
			} catch (IOException e) {
				throw new CREIOException(e.getMessage(), t, e);
			}
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "x_write";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2, 3};
		}

		@Override
		public String docs() {
			return "void {path, content, [mode]} Writes a file to the file system. This method only works from the"
					+ " cmdline,"
					+ " if not in cmdline, a SecurityException is thrown. Because of this, there is no check against"
					+ " the base-dir path. ---- The path, if relative, is relative to this script"
					+ " file. If the path already exists, and is a directory, an IOException is thrown."
					+ " The content may be a string, in which case it is written out as UTF-8 text. It could also"
					+ " be a byte_array, in which cases it is written as is. Mode can be one of the following, but"
					+ " defaults to SAFE_WRITE:\n"
					+ createEnumTable(FileWriteMode.class);
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_4;
		}

	}

}
