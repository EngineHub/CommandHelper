package com.laytonsmith.core.webserver;

import com.laytonsmith.PureUtilities.Common.FileUtil;
import com.laytonsmith.PureUtilities.Common.OSUtils;
import com.laytonsmith.PureUtilities.Common.StackTraceUtils;
import com.laytonsmith.PureUtilities.Common.StreamUtils;
import com.laytonsmith.PureUtilities.SignalHandler;
import com.laytonsmith.PureUtilities.SignalType;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.StandardOpenOption;
import java.util.Timer;
import java.util.TimerTask;

/**
 *
 */
@SuppressWarnings("PointlessBitwiseExpression")
public final class WebServerController {

	private static final int NORMAL_SHUTDOWN = 0;
	private static final int HARD_SHUTDOWN = 1 << 0;


	private final File ctrlDir = ReverseProxySettings.getCtrlFolder();
	private final File pidFile = new File(ctrlDir, "pid");
	private final File cmdFile = new File(ctrlDir, "cmd");

	private final WebServer server;

	public static void main(String[] args) throws Exception {
		ReverseProxyListener.main(args);
	}

	/**
	 * Verbs are the supported out-of-band commands that can be sent to the web server by an external process.
	 */
	public static enum Verb {
		/**
		 * Stops the server.
		 *
		 * Parameters:
		 * 0th: byte - 0 - Forces a shutdown, stopping scripts as needed. 1 - Gives scripts an unlimited amount of time
		 *		to shut down, but does not accept any new connections.
		 */
		STOP((byte) 0, (server, cmd) -> {
			byte graceful = cmd[1];
			server.shutdown(graceful != (byte) 0);
		}),
		/**
		 * Causes scripts to recompile. Currently processing requests will continue with the old script,
		 * but new requests will be served with the new scripts.
		 */
		RECOMPILE((byte) 1, (server, cmd) -> {
			server.recompile();
		});

		private final byte id;
		private final VerbAction action;

		private Verb(byte id, VerbAction action) {
			this.id = id;
			this.action = action;
		}

		/**
		 * Returns the ordinal of this Verb
		 * @return
		 */
		public byte getByte() {
			return id;
		}

		/**
		 * Returns the Verb, given a particular ordinal.
		 * @param b The ordinal
		 * @return The given Verb
		 * @throws InvalidVerbException If the ordinal is out of range. Internally, this should always be consistent
		 * (or it's a programmer error) but it could be that the commandor process is running a different version
		 * than the commandee.
		 */
		public static Verb fromByte(byte b) throws InvalidVerbException {
			for(Verb v : values()) {
				if(v.id == b) {
					return v;
				}
			}
			throw new InvalidVerbException("Unrecognized verb: " + b);
		}

		/**
		 * Executes the given verb against the server.
		 * @param server
		 * @param cmd The full command, as some Verbs accept argument, which are individually parsed.
		 */
		public void execute(WebServerController server, byte[] cmd) {
			this.action.activate(server, cmd);
		}

		private static interface VerbAction {
			void activate(WebServerController server, byte[] cmd);
		}

	}

	/**
	 * Sends the given command to the webserver.
	 * @param cmd The command to run
	 * @param params If the verb accepts arguments, these are provided here. The verbs are individually responsible
	 * for deserializing the parameters.
	 * @throws ServerNotRunningException If the server does not appear to be running.
	 * @throws java.io.IOException If an IOException occured, for instance if the ctrl files can't be read/written to.
	 */
	public synchronized void writeCmd(Verb cmd, byte[] params) throws ServerNotRunningException, IOException {
		if(!isServerUp()) {
			throw new ServerNotRunningException();
		}
		FileChannel ch = FileChannel.open(cmdFile.toPath(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
		FileLock lock = null;
		try {
			lock = ch.lock();
			ch.truncate(0);
			byte[] data = new byte[params.length + 1];
			data[0] = cmd.getByte();
			System.arraycopy(params, 0, data, 1, params.length);
			ByteBuffer contents = ByteBuffer.wrap(data);
			ch.write(contents);
		} finally {
			if(lock != null) {
				lock.release();
			}
			ch.close();
		}
	}

	/**
	 * Checks if the server is up and running.
	 * @return
	 * @throws java.io.IOException
	 */
	public boolean isServerUp() throws IOException {
		if(!ctrlDir.exists()) {
			return false;
		}
		if(!pidFile.exists()) {
			return false;
		}

		long pid = getPid();
		return OSUtils.GetRunningProcesses().stream().anyMatch(p -> p.getPid() == pid);
	}

	public long getPid() throws IOException {
		FileChannel ch = FileChannel.open(pidFile.toPath(), StandardOpenOption.READ, StandardOpenOption.WRITE);
		FileLock lock = null;
		try {
			lock = ch.lock();
			long pid = Long.parseLong(FileUtil.read(ch));
			return pid;
		} finally {
			if(lock != null) {
				lock.release();
			}
			ch.close();
		}
	}

	/**
	 * Given the presumed already initialized prefs file, installs to systemd if on a supported unix system.
	 * @throws java.io.IOException
	 */
	public void install() throws IOException {
		if(OSUtils.GetOS().isUnixLike()) {
			installUnix();
		} else if(OSUtils.GetOS().isWindows()) {
			// No op for now
		}
	}

	/**
	 * On Windows, this resolves to a bogus file, but doesn't matter, it isn't used.
	 */
	private static final File SYSTEMD = new File("/lib/systemd/system");

	private void installUnix() throws IOException {
		if(!SYSTEMD.exists()) {
			return;
		}
		FileUtil.write(StreamUtils.GetResource("systemd.service").replace("%%PIDFILE%%", pidFile.getAbsolutePath()),
				new File(SYSTEMD, "msws.service"), true);
	}


	public void start() throws ServerAlreadyUpException, IOException {
		if(isServerUp()) {
			throw new ServerAlreadyUpException();
		}
		log("Starting up...");
		File prefs = ReverseProxySettings.getPrefsFile();
		ReverseProxySettings.init(prefs);
		ctrlDir.mkdirs();
		registerPid();
		startWatchDog();
		registerShutdownListeners();
		server.start(ReverseProxySettings.getPort(),
				ReverseProxySettings.getRoot(),
				ReverseProxySettings.getThreads());
		log("Started");
	}

	private void registerShutdownListeners() {
		SignalHandler.addStopHandlers((SignalType type) -> {
			shutdown(false);
			return true;
		});
	}

	private void registerPid() throws IOException {
		FileChannel ch = FileChannel.open(pidFile.toPath(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
		FileLock lock = null;
		try {
			lock = ch.lock();
			FileUtil.write(ch, Long.toString(OSUtils.GetMyPid()));
		} finally {
			pidFile.deleteOnExit();
			cmdFile.deleteOnExit();
			if(lock != null) {
				lock.release();
			}
			ch.close();
		}
	}

	private boolean stopWatchdog = false;


	private void startWatchDog() {
		// Need to run as not a daemon, since we do I/O.
		final Timer t = new Timer("MSWS-Watchdog", false);
		t.schedule(new TimerTask() {
			@Override
			public void run() {
				if(stopWatchdog) {
					t.cancel();
					stopWatchdog = false;
					return;
				}
				try {
					readCmd();
				} catch (IOException ex) {
					log(ex);
				} catch (InvalidVerbException ex) {
					log("Invalid verb specified. Are you running the same version?");
				}
			}

		}, 0, 1000);
		log("Watchdog thread started");
	}

	private void stopWatchdog() throws InterruptedException {
		this.stopWatchdog = true;
		Thread.sleep(2000);
	}

	/**
	 * The watchdog timer reads commands at a steady rate of 1 per second or so. The command is read, cleared from
	 * the file, a new thread kicked off to actually handle the command, and then the command file unlocked.
	 * It is super important that this thread continues to run until explicitely shut down, which is the absolute
	 * last operation, so that if an operator wishes to try a graceful shutdown at first, but can't, they can still
	 * issue commands to the server (for instance a non-graceful shutdown). Therefore, we can't make any assumptions
	 * here about what a given command does.
	 * @throws IOException
	 * @throws InvalidVerbException
	 */
	private void readCmd() throws IOException, InvalidVerbException {
		if(!cmdFile.exists()) {
			return;
		}
		FileChannel ch = FileChannel.open(cmdFile.toPath(), StandardOpenOption.READ, StandardOpenOption.WRITE);
		FileLock lock = null;
		try {
			lock = ch.lock();
			byte[] cmd = FileUtil.readData(ch);
			if(cmd.length < 1) {
				// No command.
				return;
			}
			Verb verb = Verb.fromByte(cmd[0]);
			FileUtil.write(ch, "");
			new Thread(() -> {
				verb.execute(WebServerController.this, cmd);
			}, "VerbExecute-" + verb.name()).start();
		} finally {
			if(lock != null) {
				lock.release();
			}
			ch.close();
		}
	}

	public void shutdown(boolean graceful) {
		log((graceful ? "Graceful" : "Hard") + " shutdown detected");
		if(!graceful) {
			System.exit(HARD_SHUTDOWN);
		}
		server.stop();
		try {
			stopWatchdog();
		} catch (InterruptedException ex) {
			//
		}
		System.exit(NORMAL_SHUTDOWN);
	}

	public void recompile() {
		server.recompile();
	}

	private void log(Throwable t) {
		log(StackTraceUtils.GetStacktrace(t));
	}

	private void log(String message) {
		StreamUtils.GetSystemOut().println(message);
	}

	private WebServerController() {
		server = new WebServer();
	}

	private static volatile WebServerController webServerController = null;
	private static final Object WEB_SERVER_CONTROLLER_LOCK = new Object();

	public static WebServerController GetWebServer() {
		@SuppressWarnings("LocalVariableHidesMemberVariable")
		WebServerController webServer = WebServerController.webServerController;
		if(webServer == null) {
			synchronized(WEB_SERVER_CONTROLLER_LOCK) {
				webServer = WebServerController.webServerController;
				if(webServer == null) {
					WebServerController.webServerController = webServer = new WebServerController();
				}
			}
		}
		return webServer;
	}


}
