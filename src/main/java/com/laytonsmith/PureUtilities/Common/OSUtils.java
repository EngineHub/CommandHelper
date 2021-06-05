package com.laytonsmith.PureUtilities.Common;

import com.laytonsmith.PureUtilities.CommandExecutor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class contains utilities that help with OS specific tasks.
 *
 */
public class OSUtils {

	/**
	 * The bit depth of the OS.
	 */
	public static enum BitDepth {
		/**
		 * The OS is a 32 bit architecture.
		 */
		B32,
		/**
		 * The OS is a 64 bit architecture.
		 */
		B64
	}

	public static enum OS {
		WINDOWS,
		MAC,
		LINUX,
		SOLARIS,
		UNKNOWN;

		/**
		 * Returns true if this is {@link #WINDOWS}
		 * @return
		 */
		public boolean isWindows() {
			return this == WINDOWS;
		}

		/**
		 * Returns true if this is {@link #MAC}
		 * @return
		 */
		public boolean isMac() {
			return this == MAC;
		}

		/**
		 * Returns true if this is {@link #LINUX}
		 * @return
		 */
		public boolean isLinux() {
			return this == LINUX;
		}

		/**
		 * Returns true if this is {@link #SOLARIS}
		 * @return
		 */
		public boolean isSolaris() {
			return this == SOLARIS;
		}

		/**
		 * Returns true if this is {@link #UNKNOWN}
		 * @return
		 */
		public boolean isUnknown() {
			return this == UNKNOWN;
		}

		/**
		 * Returns true if this is a strict UNIX implementation, that is, {@link #MAC} or {@link #SOLARIS}
		 *
		 * @return
		 */
		public boolean isUnix() {
			return this == MAC || this == SOLARIS;
		}

		/**
		 * Returns true if this {@link #isUnix()} returns true, or if this is {@link #LINUX}. This is a less strict
		 * category than {@link #isUnix()}, because for most purposes, Linux is UNIX compatible, but this is not
		 * strictly the case. Depending on what you're doing, you may need to differentiate between strict UNIX OSes
		 * or not, so if this matters, you'll need to do a more granular check.
		 * @return
		 */
		public boolean isUnixLike() {
			return isUnix() || this == LINUX;
		}
	}

	/**
	 * Returns the OS that is currently running
	 *
	 * @return
	 */
	public static OS GetOS() {
		String os = System.getProperty("os.name").toLowerCase();
		if(os.contains("win")) {
			return OS.WINDOWS;
		} else if(os.contains("mac")) {
			return OS.MAC;
		} else if(os.contains("nix") || os.contains("nux")) {
			return OS.LINUX;
		} else if(os.contains("sunos")) {
			return OS.SOLARIS;
		} else {
			return OS.UNKNOWN;
		}
	}

	public static String GetLineEnding() {
		return System.getProperty("line.separator");
	}

	/**
	 * Returns the process id of the currently running Java process. In Java versions &lt; 9, this actually just a best
	 * effort, and it is possible in some implementations of Java, this will cause an Exception. However, if Java 9 or
	 * greater is running, this will be an authoritative response.
	 * @return
	 * @throws UnsupportedOperationException If the java implementation does not have correct support for this
	 * operation.
	 */
	@SuppressWarnings("checkstyle:localvariablename")
	public static long GetMyPid() throws UnsupportedOperationException {
		return ProcessHandle.current().pid();
	}

	public static class ProcessInfo {
		private final long pid;
		private final String command;

		ProcessInfo(long pid, String command) {
			this.pid = pid;
			this.command = command;
		}

		/**
		 * Returns the process id of the given process.
		 * @return
		 */
		public long getPid() {
			return pid;
		}

		/**
		 * Returns the process name, i.e. "nginx" on linux or "cmd.exe" on windows.
		 * @return
		 */
		public String getCommand() {
			return command;
		}

		@Override
		public String toString() {
			return pid + ": " + command;
		}

	}

	/**
	 * Returns a list of processes running on this system.
	 * @return
	 */
	public static List<ProcessInfo> GetRunningProcesses() {
		try {
			if(GetOS().isWindows()) {
					return GetRunningProcessesWindows();
			} else if(GetOS().isUnixLike()) {
				return GetRunningProcessesUnix();
			} else {
				throw new UnsupportedOperationException("Unsupported OS");
			}
		} catch (IOException | InterruptedException ex) {
			throw new RuntimeException(ex);
		}
	}

	public static BitDepth GetOSBitDepth() {
		if(GetOS().isWindows()) {
			// Windows lies if we're running 32 bit java on a 64 bit architecture, but this really isn't
			// what we need to know, so "thanks".
			String arch = System.getenv("PROCESSOR_ARCHITECTURE");
			String wow64Arch = System.getenv("PROCESSOR_ARCHITEW6432");

			return arch != null && arch.endsWith("64")
				|| wow64Arch != null && wow64Arch.endsWith("64")
					? BitDepth.B64 : BitDepth.B64;
		} else {
			return System.getProperty("os.arch").endsWith("64") ? BitDepth.B64 : BitDepth.B32;
		}
	}

	private static List<ProcessInfo> GetRunningProcessesWindows() throws InterruptedException, IOException {
		List<ProcessInfo> list = new ArrayList<>();
		String cmd = CommandExecutor.Execute("tasklist.exe /fo list");
		String imageName = null;
		String pid = null;
		for(String line : cmd.split("\n|\r\n|\n\r")) {
			line = line.trim();
			if(line.isEmpty()) {
				if(imageName != null) {
					list.add(new ProcessInfo(Long.parseLong(pid), imageName));
				}
				continue;
			}
			if(line.startsWith("Image Name")) {
				imageName = line.replaceAll("Image Name:\\s*(.*)", "$1");
				continue;
			}
			if(line.startsWith("PID")) {
				pid = line.replaceAll("PID:\\s*(.*)", "$1");
			}
		}
		return list;
	}

	private static List<ProcessInfo> GetRunningProcessesUnix() throws InterruptedException, IOException {
		List<ProcessInfo> list = new ArrayList<>();
		String cmd = CommandExecutor.Execute("ps -ea");
		boolean first = true;
		for(String line : cmd.split("\n|\r\n|\n\r")) {
			if(first) {
				// First line is header
				first = false;
				continue;
			}
			line = line.trim();
			String[] params = line.split("\\s+", 4);
			list.add(new ProcessInfo(Long.parseLong(params[0]), params[3]));
		}
		return list;
	}



}
