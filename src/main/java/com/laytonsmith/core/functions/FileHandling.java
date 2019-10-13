package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.Common.FileUtil;
import com.laytonsmith.PureUtilities.Common.StreamUtils;
import com.laytonsmith.PureUtilities.RunnableQueue;
import com.laytonsmith.PureUtilities.SSHWrapper;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.PureUtilities.ZipReader;
import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.annotations.DocumentLink;
import com.laytonsmith.annotations.api;
import com.laytonsmith.annotations.core;
import com.laytonsmith.annotations.noboilerplate;
import com.laytonsmith.core.MSLog;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.LogLevel;
import com.laytonsmith.core.ObjectGenerator;
import com.laytonsmith.core.Optimizable;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.Security;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.compiler.FileOptions;
import com.laytonsmith.core.constructs.CByteArray;
import com.laytonsmith.core.constructs.CClosure;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.constructs.CNull;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.CVoid;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.GlobalEnv;
import com.laytonsmith.core.exceptions.CRE.CRECastException;
import com.laytonsmith.core.exceptions.CRE.CREIOException;
import com.laytonsmith.core.exceptions.CRE.CRESecurityException;
import com.laytonsmith.core.exceptions.CRE.CREThrowable;
import com.laytonsmith.core.exceptions.CancelCommandException;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.natives.interfaces.Mixed;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.zip.GZIPInputStream;

/**
 *
 */
@core
public class FileHandling {

	public static String docs() {
		return "This class contains methods that help manage files on the file system. Most are restricted with the base-dir setting"
				+ " in your preferences.";
	}

	@api
	@noboilerplate
	@DocumentLink(0)
	public static class read extends AbstractFunction implements DocumentLinkProvider {

		public static String file_get_contents(String fileLocation) throws Exception {
			return new ZipReader(new File(fileLocation)).getFileContents();
		}

		@Override
		public String getName() {
			return "read";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
			File location = Static.GetFileFromArgument(args[0].val(), env, t, null);
			try {
				if(!Static.InCmdLine(env, true)) {
					//Verify this file is not above the craftbukkit directory (or whatever directory the user specified
					//Cmdline mode doesn't currently have this restriction.
					if(!Security.CheckSecurity(location)) {
						throw new CRESecurityException("You do not have permission to access the file '" + location + "'", t);
					}
				}
				String s = file_get_contents(location.getAbsolutePath());
				s = s.replaceAll("\n|\r\n", "\n");
				return new CString(s, t);
			} catch (Exception ex) {
				MSLog.GetLogger().Log(MSLog.Tags.GENERAL, LogLevel.INFO, "Could not read in file while attempting to find "
						+ location.getAbsolutePath()
						+ "\nFile " + (location.exists() ? "exists" : "does not exist"), t);
				throw new CREIOException("File \"" + location + "\" could not be read in.", t);
			}
		}

		@Override
		public String docs() {
			return "string {file} Reads in a file from the file system at location var1 and returns it as a string. The path is relative to"
					+ " the file that is being run, not CommandHelper. If the file is not found, or otherwise can't be read in, an IOException is thrown."
					+ " If the file specified is not within base-dir (as specified in the preferences file), a SecurityException is thrown."
					+ " The line endings for the string returned will always be \\n, even if they originally were \\r\\n.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREIOException.class, CRESecurityException.class};
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
			//Because we do disk IO
			return true;
		}

		@Override
		public LogLevel profileAt() {
			return LogLevel.DEBUG;
		}
	}

	@api
	@noboilerplate
	@DocumentLink(0)
	public static class comp_read extends AbstractFunction implements Optimizable, DocumentLinkProvider {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{};
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
			return CNull.NULL;
		}

		@Override
		public String getName() {
			return "comp_read";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "string {path} Returns the value of a file at compile time only. Unlike read, this runs and is fully resolved"
					+ " at compile time. This is useful for optimization reasons, if you have a file that is unchanging, this can be"
					+ " used instead of read(), to prevent a runtime hit each time the code is executed. Otherwise, this method is"
					+ " equivalent to read(). The path must be fully resolved at compile time.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(OptimizationOption.OPTIMIZE_DYNAMIC);
		}

		@Override
		public ParseTree optimizeDynamic(Target t, Environment env,
				Set<Class<? extends Environment.EnvironmentImpl>> envs,
				List<ParseTree> children, FileOptions fileOptions)
				throws ConfigCompileException, ConfigRuntimeException {
			if(children.get(0).isDynamic()) {
				throw new ConfigCompileException(getName() + " can only accept hardcoded paths.", t);
			}

			String ret = new read().exec(t, env, children.get(0).getData()).val();
			ParseTree tree = new ParseTree(new CString(ret, t), fileOptions);
			return tree;
		}

	}

	@api
	@noboilerplate
	@DocumentLink(0)
	public static class async_read extends AbstractFunction implements DocumentLinkProvider {

		private static RunnableQueue queue;
		private static volatile boolean started = false;
		private static final Object LOCK = new Object();

		// It's not really nested, it's within the callback, but the IDE doesn't understand that.
		@SuppressWarnings("NestedSynchronizedStatement")
		private void startup() {
			if(!started) {
				synchronized(LOCK) {
					if(!started) {
						queue = new RunnableQueue("MethodScript-asyncRead");
						queue.invokeLater(null, () -> {
							//This warms up the queue.
						});
						StaticLayer.GetConvertor().addShutdownHook(() -> {
							synchronized(LOCK) {
								queue.shutdown();
								started = false;
							}
						});
						started = true;
					}
				}
			}
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRESecurityException.class};
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
		public Mixed exec(final Target t, final Environment environment, Mixed... args) throws ConfigRuntimeException {
			startup();
			final String file = args[0].val();
			final CClosure callback;
			if(!(args[1].isInstanceOf(CClosure.TYPE))) {
				throw new CRECastException("Expected paramter 2 of " + getName() + " to be a closure!", t);
			} else {
				callback = ((CClosure) args[1]);
			}
			if(!Static.InCmdLine(environment, true)) {
				try {
					if(!Security.CheckSecurity(file)) {
						throw new CRESecurityException("You do not have permission to access the file '" + file + "'", t);
					}
				} catch (IOException ex) {
					throw new CREIOException(ex.getMessage(), t, ex);
				}
			}
			queue.invokeLater(environment.getEnv(GlobalEnv.class).GetDaemonManager(), new Runnable() {

				@Override
				public void run() {
					String returnString = null;
					ConfigRuntimeException exception = null;
					if(file.contains("@")) {
						try {
							//It's an SCP transfer
							returnString = SSHWrapper.SCPReadString(file);
							SSHWrapper.closeSessions();
						} catch (IOException ex) {
							exception = new CREIOException(ex.getMessage(), t, ex);
						}
					} else {
						try {
							//It's a local file read
							File _file = Static.GetFileFromArgument(file, environment, t, null);
							returnString = FileUtil.read(_file);
						} catch (IOException ex) {
							exception = new CREIOException(ex.getMessage(), t, ex);
						}
					}
					final Mixed cret;
					if(returnString == null) {
						cret = CNull.NULL;
					} else {
						cret = new CString(returnString, t);
					}
					final Mixed cex;
					if(exception == null) {
						cex = CNull.NULL;
					} else {
						cex = ObjectGenerator.GetGenerator().exception(exception, environment, t);
					}
					StaticLayer.GetConvertor().runOnMainThreadLater(environment.getEnv(GlobalEnv.class).GetDaemonManager(), new Runnable() {

						@Override
						public void run() {
							callback.executeCallable(new Mixed[]{cret, cex});
						}
					});
				}
			});
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "async_read";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "void {file, callback} Asyncronously reads in a file. ---- "
					+ " This may be a remote file accessed with an SCP style path. (See the [[SCP|wiki article]]"
					+ " about SCP credentials for more information.) If the file is not found, or otherwise can't be read in, an IOException is thrown."
					+ " If the file specified is not within base-dir (as specified in the preferences file), a SecurityException is thrown."
					+ " (This is not applicable for remote files)"
					+ " The line endings for the string returned will always be \\n, even if they originally were \\r\\n."
					+ " This method will immediately return, and asynchronously read in the file, and finally send the contents"
					+ " to the callback once the task completes. The callback should have the following signature: closure(@contents, @exception){ &lt;code&gt; }."
					+ " If @contents is null, that indicates that an exception occured, and @exception will not be null, but instead have an"
					+ " exeption array. Otherwise, @contents will contain the file's contents, and @exception will be null. This method is useful"
					+ " to use in two cases, either you need a remote file via SCP, or a local file is big enough that you notice a delay when"
					+ " simply using the read() function. async_read is threadsafe.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

	}

	@api
	@DocumentLink(0)
	public static class file_size extends AbstractFunction implements DocumentLinkProvider {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREIOException.class, CRESecurityException.class};
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
			File location = Static.GetFileFromArgument(args[0].val(), environment, t, null);
			try {
				if(!Security.CheckSecurity(location) && !Static.InCmdLine(environment, true)) {
					throw new CRESecurityException("You do not have permission to access the file '" + location + "'", t);
				}
			} catch (IOException ex) {
				throw new CREIOException(ex.getMessage(), t, ex);
			}
			return new CInt(location.length(), t);
		}

		@Override
		public String getName() {
			return "file_size";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "int {path} Returns the size of a file on the file system.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

	}

	@api
	public static class read_gzip_binary extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREIOException.class};
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
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			File location = Static.GetFileFromArgument(args[0].val(), env, t, null);
			try {
				if(!Static.InCmdLine(env, true)) {
					//Verify this file is not above the craftbukkit directory (or whatever directory the user specified
					//Cmdline mode doesn't currently have this restriction.
					if(!Security.CheckSecurity(location)) {
						throw new CRESecurityException("You do not have permission to access the file '" + location + "'", t);
					}
				}
				InputStream stream = new GZIPInputStream(new FileInputStream(location));
				return CByteArray.wrap(StreamUtils.GetBytes(stream), t);
			} catch (IOException ex) {
				Static.getLogger().log(Level.SEVERE, "Could not read in file while attempting to find " + location.getAbsolutePath()
						+ "\nFile " + (location.exists() ? "exists" : "does not exist"));
				throw new CREIOException("File could not be read in.", t);
			}
		}

		@Override
		public String getName() {
			return "read_gzip_binary";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "byte_array {file} Reads in a gzipped file, and returns a byte_array for it. The file is returned"
					+ " exactly as is on disk, no conversions are done other than unzipping it."
					+ " base-dir restrictions are enforced for the"
					+ " path, the same as read(). If file is relative, it is assumed to be relative to this file.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}

	}

	@api
	public static class read_binary extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREIOException.class};
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
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			File location = Static.GetFileFromArgument(args[0].val(), env, t, null);
			try {
				if(!Static.InCmdLine(env, true)) {
					//Verify this file is not above the craftbukkit directory (or whatever directory the user specified
					//Cmdline mode doesn't currently have this restriction.
					if(!Security.CheckSecurity(location)) {
						throw new CRESecurityException("You do not have permission to access the file '" + location + "'", t);
					}
				}
				InputStream stream = new BufferedInputStream(new FileInputStream(location));
				return CByteArray.wrap(StreamUtils.GetBytes(stream), t);
			} catch (IOException ex) {
				Static.getLogger().log(Level.SEVERE, "Could not read in file while attempting to find " + location.getAbsolutePath()
						+ "\nFile " + (location.exists() ? "exists" : "does not exist"));
				throw new CREIOException("File could not be read in.", t);
			}
		}

		@Override
		public String getName() {
			return "read_binary";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "byte_array {file} Reads in a file, and returns a byte_array for it. The file is returned"
					+ " exactly as is on disk, no conversions are done. base-dir restrictions are enforced for the"
					+ " path, the same as read(). If file is relative, it is assumed to be relative to this file."
					+ " This is useful for managing binary files.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}

	}

	//@api
	public static class file_parent extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{};
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
			//TODO: Doesn't work yet.
			//TODO: Be sure to change over to Static.GetFileFromArgument
			String path = args[0].val().trim().replace('\\', '/');
			//Remove duplicate /
			path = path.replaceAll("(/)(?=.*?/)", path);
			if("/".equals(path) || path.matches("[a-zA-Z]:/")) {
				//This is the root path, return null.
				return CNull.NULL;
			}
			//If the path ends with /, take it off
			while(path.endsWith("/")) {
				path = path.substring(0, path.length() - 2);
			}
			return new CString(path.substring(0, path.length() - path.lastIndexOf("/")), t);
		}

		@Override
		public String getName() {
			return "file_parent";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "string {string} Returns the parent directory for the specified file/directory path. For instance,"
					+ " given the string '/path/to/file', then '/path/to/' would be returned. Regardless of whether"
					+ " or not the system uses forwards or backwards slashes, the file path returned will use forward"
					+ " slashes. The path doesn't need to actually exist for this function to work, and the path returned"
					+ " is assumed to be a directory, so will always end with '/'. If the path represents the root path,"
					+ " for instance, 'C:/' or '/', null is returned.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}

	}

	@api
	@DocumentLink(0)
	public static class file_resolve extends AbstractFunction implements DocumentLinkProvider {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREIOException.class};
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
			File f = Static.GetFileFromArgument(args[0].val(), environment, t, null);
			try {
				return new CString(f.getCanonicalPath(), t);
			} catch (IOException ex) {
				throw new CREIOException(ex.getMessage(), t, ex);
			}
		}

		@Override
		public String getName() {
			return "file_resolve";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "string {file} Returns the canonical, absolute path of the given path. This provides a context independent"
					+ " and unique path which always points to the specified path, and removes any duplicate . or .. parts.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}

	}
}
