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
import java.util.HashMap;
import java.util.Map;

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
		if(OSUtils.GetOSBitDepth() != OSUtils.BitDepth.B64) {
			// Some of the code below looks like it supports 32 bit, but actually it doesn't. Not sure
			// if there's actually any real demand for 32 bit these days, but if there is, more work needs
			// to be done, anyways.
			throw new UnsupportedOperationException("This is only supported on 64 bit systems currently.");
		}
		if(OSUtils.GetOS().isWindows()) {
			installWindows();
		} else {
			throw new UnsupportedOperationException("Toolchain installation not supported on this platform");
		}
	}

	private void installWindows() throws IOException, InterruptedException {
		// NOTE: Installation MUST be idempotent, please ensure it stays that way.

		String installerUrl = null;
		String destFilename = null;

		Map<String, String> llvmExtras = new HashMap<>();
		String extrasBase = "https://raw.githubusercontent.com/LadyCailin/MethodScriptExtra/master";
		String msvcVersion = "14.29.30037";
		String llvmExtrasBase = extrasBase + "/installers/llvm/12.0.1/x64/";
		llvmExtras.put(llvmExtrasBase + "llc/llc.exe", "llc.exe");
		llvmExtras.put(llvmExtrasBase + "lli/lli.exe", "lli.exe");
		llvmExtras.put(llvmExtrasBase + "llvm-as/llvm-as.exe", "llvm-as.exe");
		llvmExtras.put(llvmExtrasBase + "llvm-dis/llvm-dis.exe", "llvm-dis.exe");
		llvmExtras.put(llvmExtrasBase + "opt/opt.exe", "opt.exe");
		llvmExtras.put(llvmExtrasBase + "llvm-link/llvm-link.exe", "llvm-link.exe");

		String redistUrl = extrasBase + "/installers/winsdk/10.0.19041.0/winsdksetup.exe";
		String buildToolsUrl = extrasBase + "/installers/winsdk/MSVC/vs_BuildTools.exe";
		if(OSUtils.GetOSBitDepth() == OSUtils.BitDepth.B64) {
			log("Detected 64 bit Windows");
			installerUrl
					= "https://github.com/llvm/llvm-project/releases/download/llvmorg-12.0.1/LLVM-12.0.1-win64.exe";
			destFilename = "LLVM-12.0.1-win64.exe";
		}

		String redistFileName = "winsdksetup.exe";
		String buildToolsFileName = "vs_BuildTools.exe";
		File lld = new File("C:\\Program Files\\LLVM\\bin\\lld-link.exe");
		File winSDK = new File("C:\\Program Files (x86)\\Windows Kits\\10\\Lib\\10.0.19041.0");
		File buildTools = new File("C:\\Program Files (x86)\\Microsoft Visual Studio\\2019\\BuildTools\\VC\\Tools\\MSVC\\" + msvcVersion);
		log("Installing LLVM...");
		if(!lld.exists() || !CommandExecutor.Execute(lld.getAbsolutePath(), "--version").contains("LLD 12.0.1")) {
			File exe = download(installerUrl, destFilename);
			log(TermColors.YELLOW + "Please note, when installing LLVM, install in the standard location,"
					+ " \"C:\\Program Files\\LLVM\", otherwise the toolchain will not work." + TermColors.reset());
			CommandExecutor.Execute(exe.getAbsolutePath());
		}

		{
			log("Installing extras...");
			File root = new File("C:\\Program Files\\LLVM\\bin\\");
			for(Map.Entry<String, String> entry : llvmExtras.entrySet()) {
				String url = entry.getKey();
				String filename = entry.getValue();
				File file = new File(root, filename);
				if(!file.exists()
						|| !CommandExecutor.Execute(file.getAbsolutePath(), "--version").contains("LLVM version 12.0.1")) {
					log("Downloading " + filename);

					File exe = download(url, filename);
					log("Moving " + filename + " into " + file.getAbsolutePath() + ".");
					try {
						FileUtil.move(exe, file);
						exe.deleteOnExit();
					} catch (IOException ex) {
						log(TermColors.RED + ex.getMessage() + TermColors.reset());
					}
				}
			}
		}

		if(!winSDK.exists()) {
			File redist = download(redistUrl, redistFileName);
			log("Installing Windows 10 SDK");
			CommandExecutor.Execute(redist.getAbsolutePath());
		}

		if(!buildTools.exists()) {
			File buildToolsInstaller = download(buildToolsUrl, buildToolsFileName);
			log("Installing MSVC Build Tools");
			String[] args = new String[]{
				buildToolsInstaller.getAbsolutePath(),
				// Cmdline option references can be found here
				// https://docs.microsoft.com/en-us/visualstudio/install/use-command-line-parameters-to-install-visual-studio?view=vs-2019
				"--passive", "--norestart", "--downloadThenInstall",
				// Valid workloads can be found here
				// https://docs.microsoft.com/en-us/visualstudio/install/workload-component-id-vs-build-tools?view=vs-2019&preserve-view=true
				"--add", "Microsoft.VisualStudio.Component.VC.Tools.x86.x64"
			};
			CommandExecutor.Execute(args);
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
			File msvc = new File("C:\\Program Files (x86)\\Microsoft Visual Studio\\2019\\BuildTools\\VC\\Tools\\MSVC\\14.29.30037");
			if(!lld.exists() || !CommandExecutor.Execute(lld.getAbsolutePath(), "--version").contains("LLD 12.0.1")) {
				log("Missing correct version of lld tool, please re-install the toolchain.");
				return false;
			}
			if(!llc.exists()
				|| !CommandExecutor.Execute(llc.getAbsolutePath(), "--version").contains("LLVM version 12.0.1")) {
				log("Missing correct version of llc tool, please re-install the toolchain.");
				return false;
			}
			if(!winSDK.exists()) {
				log("Missing correct version of Windows SDK, please re-install the toolchain.");
				return false;
			}
			if(!msvc.exists()) {
				log("Missing MSVC BuildTools, please re-install the toolchain.");
			}
			return true;
		} catch (InterruptedException | IOException ex) {
			return false;
		}
	}
}
