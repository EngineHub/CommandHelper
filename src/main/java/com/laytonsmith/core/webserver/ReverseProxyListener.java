package com.laytonsmith.core.webserver;

import com.laytonsmith.PureUtilities.ArgumentParser;
import com.laytonsmith.PureUtilities.ArgumentParser.ArgumentBuilder;
import com.laytonsmith.PureUtilities.CommandExecutor;
import com.laytonsmith.PureUtilities.Common.ArrayUtils;
import com.laytonsmith.core.AbstractCommandLineTool;
import com.laytonsmith.core.Main;
import com.laytonsmith.core.tool;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class ReverseProxyListener {

	public static void main(String[] args) throws Exception {
		// For easy testing
		Main.main(new String[]{"webserver", "--start", "--foreground"});
	}

	@tool("webserver")
	public static class WebServerCtrl extends AbstractCommandLineTool {

		@Override
		public ArgumentParser getArgumentParser() {
			return ArgumentParser.GetParser()
					.addDescription("Provides a controller interface for a MethodScript web server. Note that this is"
							+ " not meant to be used as a front facing web server, and is instead meant to be used"
							+ " as a backend server behind an Nginx, Apache, or other frontend web server.")
					.addExtendedDescription("In addition to providing the installer, this provides an"
							+ " easy way to control the webserver from the command line. This server cannot"
							+ " be used as a server frontend, it will only work with MethodScript files, and"
							+ " cannot serve static content, such as css and javascript files. It provides"
							+ " a special runtime environment for efficiently running behind another front facing"
							+ " web server, such as Nginx, Apache, or any other server that supports \"reverse"
							+ " proxy\" techniques.")
					.setErrorOnUnknownArgs(true)
					.addArgument(new ArgumentBuilder()
						.setDescription("Installs the webserver. This creates the config file templates, and"
								+ " creates other folders as needed.")
						.asFlag()
						.setName("install"))
					.addArgument(new ArgumentBuilder()
						.setDescription("Starts the webserver.")
						.asFlag()
						.setName("start"))
					.addArgument(new ArgumentBuilder()
						.setDescription("(Used with the --start flag) Does not create a daemon process,"
								+ " and runs directly"
								+ " in the current shell. Note that Ctrl-C is supported in this mode, and does a"
								+ " non-graceful shutdown. Ctrl-D will perform a graceful shutdown.")
						.asFlag()
						.setName("foreground"))
					.addArgument(new ArgumentBuilder()
						.setDescription("Stops the webserver.")
						.asFlag()
						.setName("stop"))
					.addArgument(new ArgumentBuilder()
						.setDescription("(Used with the --stop flag) By default, the server stops gracefully,"
								+ " allowing running scripts to finish,"
								+ " but stopping accepting new connections. If you wish to force an immmediate shutdown"
								+ " however, you can use this flag.")
						.asFlag()
						.setName("force"))
					.addArgument(new ArgumentBuilder()
						.setDescription("Causes the webserver to discard the compiled scripts, and recompile"
								+ " everything. This has the same effect as restarting the process, but is quicker.")
						.asFlag()
						.setName("recompile"));
		}

		@Override
		public void execute(ArgumentParser.ArgumentParserResults parsedArgs) throws Exception {
			if(parsedArgs.isFlagSet("install")) {
				install();
			} else if(parsedArgs.isFlagSet("start")) {
				start(parsedArgs.isFlagSet("foreground"));
			} else if(parsedArgs.isFlagSet("stop")) {
				stop(!parsedArgs.isFlagSet("force"));
			} else if(parsedArgs.isFlagSet("recompile")) {
				recompile();
			} else {
				throw new Exception("Unsupported command");
			}
		}

		@Override
		public boolean noExitOnReturn() {
			return true;
		}

		private void install() throws IOException {
			File prefs = ReverseProxySettings.getPrefsFile();
			ReverseProxySettings.init(prefs);
			ReverseProxySettings.getCtrlFolder().mkdirs();
			WebServerController.GetWebServer().install();
		}

		private void start(boolean foreground) throws Exception {
			if(WebServerController.GetWebServer().isServerUp()) {
				System.err.println("Server already running. Stop the server first with --stop.");
				System.exit(1);
			}
			if(!foreground) {
				restartInForeground();
				return;
			}

			System.out.println("Starting server");

			WebServerController.GetWebServer().start();
		}

		@SuppressWarnings("SleepWhileInLoop")
		private void restartInForeground() throws IOException {
			List<String> largs = new ArrayList<>();
			largs.add("mscript");
			largs.add("--");
			largs.add("webserver");
			largs.add("--start");
			largs.add("--foreground");
			CommandExecutor ce = new CommandExecutor(largs.toArray(new String[largs.size()]));
			ce.start();
			while(!WebServerController.GetWebServer().isServerUp()) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException ex) {
					//
				}
			}
			System.out.println("Started in background. pid: " + WebServerController.GetWebServer().getPid());
			System.exit(0);
		}

		private void stop(boolean graceful) {
			try {
				WebServerController.GetWebServer().writeCmd(WebServerController.Verb.STOP, new byte[]{graceful ? (byte) 1 : (byte) 0});
			} catch (ServerNotRunningException ex) {
				System.err.println("Server not running");
				System.exit(1);
			} catch (IOException ex) {
				System.err.println("Could not communicate with server:");
				ex.printStackTrace(System.err);
				System.exit(1);
			}
			System.exit(0);
		}

		private void recompile() {
			try {
				WebServerController.GetWebServer().writeCmd(WebServerController.Verb.RECOMPILE, ArrayUtils.EMPTY_BYTE_ARRAY);
				// TODO: Wait for and read any error messages printed out
			} catch (ServerNotRunningException ex) {
				System.err.println("Server not running");
				System.exit(1);
			} catch (IOException ex) {
				System.err.println("Could not communicate with server:");
				ex.printStackTrace(System.err);
				System.exit(1);
			}
		}

	}

}
