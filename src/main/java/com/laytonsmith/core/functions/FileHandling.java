package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.FileUtility;
import com.laytonsmith.PureUtilities.SSHWrapper;
import com.laytonsmith.PureUtilities.ZipReader;
import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.Env;
import com.laytonsmith.core.ObjectGenerator;
import com.laytonsmith.core.Security;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.Threader;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CClosure;
import com.laytonsmith.core.constructs.CNull;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.CVoid;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.exceptions.CancelCommandException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author lsmith
 */
public class FileHandling {

	public static String docs(){
		return "This class contains methods that help manage files on the file system. Most are restricted with the base-dir setting"
			+ " in your preferences.";
	}
	
	@api
	public static class read extends AbstractFunction {

		public static String file_get_contents(String file_location) throws Exception {
			return new ZipReader(new File(file_location)).getFileContents();
		}

		public String getName() {
			return "read";
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		public Construct exec(Target t, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
			String location = args[0].val();
			location = new File(t.file().getParentFile(), location).getAbsolutePath();
			//Verify this file is not above the craftbukkit directory (or whatever directory the user specified
			if (!Security.CheckSecurity(location)) {
				throw new ConfigRuntimeException("You do not have permission to access the file '" + location + "'",
					Exceptions.ExceptionType.SecurityException, t);
			}
			try {
				String s = file_get_contents(location);
				s = s.replaceAll("\n|\r\n", "\n");
				return new CString(s, t);
			} catch (Exception ex) {
				Static.getLogger().log(Level.SEVERE, "Could not read in file while attempting to find " + new File(location).getAbsolutePath()
					+ "\nFile " + (new File(location).exists() ? "exists" : "does not exist"));
				throw new ConfigRuntimeException("File could not be read in.",
					Exceptions.ExceptionType.IOException, t);
			}
		}

		public String docs() {
			return "string {file} Reads in a file from the file system at location var1 and returns it as a string. The path is relative to"
				+ " the file that is being run, not CommandHelper. If the file is not found, or otherwise can't be read in, an IOException is thrown."
				+ " If the file specified is not within base-dir (as specified in the preferences file), a SecurityException is thrown."
				+ " The line endings for the string returned will always be \\n, even if they originally were \\r\\n.";
		}

		public Exceptions.ExceptionType[] thrown() {
			return new Exceptions.ExceptionType[]{Exceptions.ExceptionType.IOException, Exceptions.ExceptionType.SecurityException};
		}

		public boolean isRestricted() {
			return true;
		}

		public CHVersion since() {
			return CHVersion.V3_0_1;
		}

		public Boolean runAsync() {
			//Because we do disk IO
			return true;
		}
	}
	
	@api
	public static class async_read extends AbstractFunction{

		public ExceptionType[] thrown() {
			return new ExceptionType[]{};
		}

		public boolean isRestricted() {
			return true;
		}

		public Boolean runAsync() {
			return null;
		}

		public Construct exec(final Target t, Env environment, Construct... args) throws ConfigRuntimeException {
			final String file = args[0].val();
			final CClosure callback;
			if(!(args[1] instanceof CClosure)){
				throw new ConfigRuntimeException("Expected paramter 2 of " + getName() + " to be a closure!", t);
			} else {
				callback = ((CClosure)args[1]);
			}
			Threader.GetThreader().submit(new Runnable() {

				public void run() {
					String returnString = null;					
					ConfigRuntimeException exception = null;
					if(file.contains("@")){
						try {
							//It's an SCP transfer
							returnString = SSHWrapper.SCPReadString(file);
						} catch (IOException ex) {
							exception = new ConfigRuntimeException(ex.getMessage(), ExceptionType.IOException, t, ex);
						}
					} else {
						try {
							//It's a local file read
							returnString = FileUtility.read(new File(file));
						} catch (FileNotFoundException ex) {
							exception = new ConfigRuntimeException(ex.getMessage(), ExceptionType.IOException, t, ex);
						}
					}
					final Construct cret;
					if(returnString == null){
						cret = new CNull(t);
					} else {
						cret = new CString(returnString, t);
					}
					final Construct cex;
					if(exception == null){
						cex = new CNull(t);
					} else {
						cex = ObjectGenerator.GetGenerator().exception(exception, t);
					}
					StaticLayer.SetFutureRunnable(0, new Runnable() {

						public void run() {
							callback.execute(new Construct[]{cret, cex});
						}
					});
				}
			});
			return new CVoid(t);
		}

		public String getName() {
			return "async_read";
		}

		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		public String docs() {
			return "void {file, callback} Asyncronously reads in a file."
				+ " This may be a remote file accessed with an SCP style path. (See the [[CommandHelper/SCP|wiki article]]"
				+ " about SCP credentials for more information.) If the file is not found, or otherwise can't be read in, an IOException is thrown."
				+ " If the file specified is not within base-dir (as specified in the preferences file), a SecurityException is thrown."
				+ " (This is not applicable for remote files)"
				+ " The line endings for the string returned will always be \\n, even if they originally were \\r\\n."
				+ " This method will immediately return, and asynchronously read in the file, and finally send the contents"
				+ " to the callback once the task completes. The callback should have the following signature: closure(@contents, @exception, <code>)."
				+ " If @contents is null, that indicates that an exception occured, and @exception will not be null, but instead have an"
				+ " exeption array. Otherwise, @contents will contain the file's contents, and @exception will be null. This method is useful"
				+ " to use in two cases, either you need a remote file via SCP, or a local file is big enough that you notice a delay when"
				+ " simply using the read() function.";
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
		
	}
}
