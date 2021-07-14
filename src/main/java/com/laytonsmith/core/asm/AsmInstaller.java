package com.laytonsmith.core.asm;

import com.laytonsmith.PureUtilities.CommandExecutor;
import com.laytonsmith.PureUtilities.Common.FileUtil;
import com.laytonsmith.PureUtilities.Common.OSUtils;
import com.laytonsmith.PureUtilities.Common.StreamUtils;
import com.laytonsmith.PureUtilities.ProgressIterator;
import com.laytonsmith.PureUtilities.TermColors;
import com.laytonsmith.PureUtilities.Web.RequestSettings;
import com.laytonsmith.PureUtilities.Web.WebUtility;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 *
 */
public class AsmInstaller {
	private static final ProgressIterator NOOP = (current, total) -> {};
	private final ProgressIterator progress;
	public static final String LLVM_VERSION = "10.0.0";

	public AsmInstaller() {
		this.progress = NOOP;
	}

	public AsmInstaller(ProgressIterator progress) {
		this.progress = progress;
	}

	private static void log(String output) {
		StreamUtils.GetSystemOut().println(output);
	}


	public void install() throws IOException, InterruptedException {
		if(OSUtils.GetOS().isWindows()) {
			installWindows();
		} else {
			throw new UnsupportedOperationException("Toolchain installation not supported on this platform");
		}
	}

	private void installWindows() throws IOException, InterruptedException {
		// NOTE: Installation MUST be idempotent, please ensure it stays that way.
		String installerUrl;
		String destFilename;
		String redistUrl = "https://raw.githubusercontent.com"
				+ "/LadyCailin/MethodScriptExtra/master/installers/winsdk/10.0.19041.0/winsdksetup.exe";
		String redistFileName = "winsdksetup.exe";
		if(OSUtils.GetOSBitDepth() == OSUtils.BitDepth.B64) {
			log("Detected 64 bit Windows");
			installerUrl
					= "https://github.com/llvm/llvm-project/releases/download/llvmorg-10.0.0/LLVM-10.0.0-win64.exe";
			destFilename = "LLVM-10.0.0-win64.exe";
		} else {
			log("Detected 32 bit Windows");
			installerUrl
					= "https://github.com/llvm/llvm-project/releases/download/llvmorg-10.0.0/LLVM-10.0.0-win32.exe";
			destFilename = "LLVM-10.0.0-win32.exe";
		}

		File lld = new File("C:\\Program Files\\LLVM\\bin\\lld-link.exe");
		File llc = new File("C:\\Program Files\\LLVM\\bin\\llc.exe");
		File winSDK = new File("C:\\Program Files (x86)\\Windows Kits\\10\\Lib\\10.0.19041.0");
		log("Installing...");
		if(!lld.exists() || !CommandExecutor.Execute(lld.getAbsolutePath(), "--version").contains("LLD 10.0.0")) {
			File exe = download(installerUrl, destFilename);
			log(TermColors.YELLOW + "Please note, when installing LLVM, install in the standard location,"
					+ " \"C:\\Program Files\\LLVM\", otherwise the toolchain will not work." + TermColors.reset());
			CommandExecutor.Execute(exe.getAbsolutePath());
		}
		if(!llc.exists()
				|| !CommandExecutor.Execute(llc.getAbsolutePath(), "--version").contains("LLVM version 10.0.0")) {
			log("Downloading llc.exe");
			String llcUrl
					= "https://raw.githubusercontent.com"
					+ "/LadyCailin/MethodScriptExtra/master/installers/llvm/10.0.0/llc/llc.exe";
			File llcExe = download(llcUrl, "llc.exe");
			File llcDest = new File("C:\\Program Files\\LLVM\\bin\\llc.exe");
			log("Moving llc.exe into " + llcDest + ". If this is incorrect, please manually move it to the"
					+ " correct location.");
			try {
				llcExe.delete();
				FileUtil.move(llcExe, llcDest);
			} catch (IOException ex) {
				log(TermColors.RED + ex.getMessage() + TermColors.reset());
			}
		}
		if(!winSDK.exists()) {
			File redist = download(redistUrl, redistFileName);
			log("Installing Windows 10 SDK");
			CommandExecutor.Execute(redist.getAbsolutePath());
		}
		log("Done.");

	}

	private File download(String installerUrl, String destFilename) throws IOException, InterruptedException {
		log("Downloading installer from " + installerUrl);
		Path tmp = Files.createTempDirectory("llvm-installer");
		tmp.toFile().deleteOnExit();
		File dest = new File(tmp.toFile(), destFilename);
		RequestSettings rs = new RequestSettings();
		rs.setBlocking(true);
		rs.setDownloadTo(dest);
		WebUtility.GetPage(new URL(installerUrl), rs);
		return dest;
	}

	/**
	 * Validates the currently installed toolchain, to see if there is an update needed. If so, the user is prompted
	 * to run the --install-toolchain command before trying again.
	 * @return
	 */
	public static boolean validateToolchain() {
		if(OSUtils.GetOS().isWindows()) {
			return validateWindowsToolchain();
		} else {
			log("Toolchain installation not supported on this platform");
			return false;
		}
	}

	public static boolean validateWindowsToolchain() {
		try {
			File lld = new File("C:\\Program Files\\LLVM\\bin\\lld-link.exe");
			File llc = new File("C:\\Program Files\\LLVM\\bin\\llc.exe");
			File winSDK = new File("C:\\Program Files (x86)\\Windows Kits\\10\\Lib\\10.0.19041.0");
			if(!lld.exists() || !CommandExecutor.Execute(lld.getAbsolutePath(), "--version").contains("LLD 10.0.0")) {
				log("Missing correct version of lld tool, please re-install the toolchain.");
				return false;
			}
			if(!llc.exists()
				|| !CommandExecutor.Execute(llc.getAbsolutePath(), "--version").contains("LLVM version 10.0.0")) {
				log("Missing correct version of llc tool, please re-install the toolchain.");
				return false;
			}
			if(!winSDK.exists()) {
				log("Missing correct version of Windows SDK, please re-install the toolchain.");
				return false;
			}
			return true;
		} catch (InterruptedException | IOException ex) {
			return false;
		}
	}
}
