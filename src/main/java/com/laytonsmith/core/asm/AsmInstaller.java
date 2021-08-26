package com.laytonsmith.core.asm;

import com.laytonsmith.PureUtilities.CommandExecutor;
import com.laytonsmith.PureUtilities.Common.FileUtil;
import com.laytonsmith.PureUtilities.Common.OSUtils;
import com.laytonsmith.PureUtilities.Common.StreamUtils;
import com.laytonsmith.PureUtilities.SimpleVersion;
import com.laytonsmith.PureUtilities.TermColors;
import com.laytonsmith.PureUtilities.Version;
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

	public AsmInstaller() {
	}

	private static void log(String output) {
		StreamUtils.GetSystemOut().println(output + TermColors.RESET);
	}


	public void install(boolean nonInteractive) throws IOException, InterruptedException {
		if(OSUtils.GetOSBitDepth() != OSUtils.BitDepth.B64) {
			// Some of the code below looks like it supports 32 bit, but actually it doesn't. Not sure
			// if there's actually any real demand for 32 bit these days, but if there is, more work needs
			// to be done, anyways.
			throw new UnsupportedOperationException("This is only supported on 64 bit systems currently.");
		}
		if(OSUtils.GetOS().isWindows()) {
			installWindows(nonInteractive);
		} else if(OSUtils.GetOS().isLinux()) {
			installLinux(nonInteractive);
		} else if(OSUtils.GetOS().isMac()) {
			installMac(nonInteractive);
		} else {
			throw new UnsupportedOperationException("Toolchain installation not supported on this platform");
		}
	}

	private void installWindows(boolean nonInteractive) throws IOException, InterruptedException {
		// NOTE: Installation MUST be idempotent, please ensure it stays that way.

		String installerUrl = null;
		String destFilename = null;

		Map<String, String> llvmExtras = new HashMap<>();
		String extrasBase = "https://raw.githubusercontent.com/LadyCailin/MethodScriptExtra/master";
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
			log(TermColors.GREEN + "Detected 64 bit Windows");
			installerUrl
					= "https://github.com/llvm/llvm-project/releases/download/llvmorg-12.0.1/LLVM-12.0.1-win64.exe";
			destFilename = "LLVM-12.0.1-win64.exe";
		}

		String redistFileName = "winsdksetup.exe";
		String buildToolsFileName = "vs_BuildTools.exe";
		File lld = new File("C:\\Program Files\\LLVM\\bin\\lld-link.exe");
		File winSDK = new File("C:\\Program Files (x86)\\Windows Kits\\10\\Lib\\10.0.19041.0");
		log(TermColors.GREEN + "Installing LLVM...");
		if(!lld.exists() || !CommandExecutor.Execute(lld.getAbsolutePath(), "--version").contains("LLD 12.0.1")) {
			if(new File("C:\\ProgramData\\chocolatey\\bin\\choco.exe").exists()) {
				// Use chocolatey, since it's non-interactive
				CommandExecutor.ExecuteWithRedirect("choco", "install", "llvm", "-y");
			} else {
				File exe = download(installerUrl, destFilename);
				log(TermColors.YELLOW + "Please note, when installing LLVM, install in the standard location,"
						+ " \"C:\\Program Files\\LLVM\", otherwise the toolchain will not work." + TermColors.reset());
				CommandExecutor.Execute(exe.getAbsolutePath());
			}
		}

		{
			log(TermColors.GREEN + "Installing extras...");
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
			log(TermColors.GREEN + "Installing Windows 10 SDK");
			CommandExecutor.Execute(redist.getAbsolutePath(), "/quiet");
		}

		boolean buildToolsUpToDate = false;
		{
			final Version minBuildToolsVersion = new SimpleVersion(14, 29, 30037);
			Version installedVersion = getInstalledBuildToolsVersion();
			if(installedVersion != null && installedVersion.gte(minBuildToolsVersion)) {
				buildToolsUpToDate = true;
			}
		}

		if(!buildToolsUpToDate) {
			File buildToolsInstaller = download(buildToolsUrl, buildToolsFileName);
			log(TermColors.GREEN + "Installing MSVC Build Tools");
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

		log(TermColors.GREEN + "Done.");

	}

	/**
	 * Assuming that the Windows build tools are installed, returns the newest version. If no build tools are installed,
	 * (or if this isn't Windows), null is returned.
	 * @return
	 */
	public static Version getInstalledBuildToolsVersion() {
		File buildTools = new File("C:\\Program Files (x86)\\Microsoft Visual Studio\\2019\\BuildTools\\VC\\Tools\\MSVC\\");
		if(!buildTools.exists() || buildTools.listFiles().length == 0) {
			return null;
		}
		Version latestVersion = new SimpleVersion(0, 0, 0);
		for(File installedBuildToolVersion : buildTools.listFiles()) {
			Version v = new SimpleVersion(installedBuildToolVersion.getName());
			if(v.gt(latestVersion)) {
				latestVersion = v;
			}
		}
		return latestVersion;
	}

	private void installLinux(boolean nonInteractive) throws IOException, InterruptedException {
		String distro = OSUtils.GetLinuxDistro().toLowerCase();
		if(distro.contains("debian") || distro.contains("ubuntu")) {
			installDebianBased(nonInteractive);
		} else {
			throw new UnsupportedOperationException("Your linux distribution is not supported yet. It may be a simple"
					+ " matter to add support, however, please file an issue (or PR!) if you're interested in adding"
					+ " support for " + OSUtils.GetLinuxDistro());
		}
	}

	private void installDebianBased(boolean nonInteractive) throws IOException, InterruptedException {

		boolean isUbuntu = CommandExecutor.Execute("lsb_release", "-a").contains("buntu");
		if(!isUbuntu) {
			log(TermColors.RED + "Only Ubuntu flavors are officially supported. You may run into installation failures.");
		}
		if(OSUtils.GetOSBitDepth() != OSUtils.BitDepth.B64) {
			log(TermColors.RED + "Only 64 bit systems are supported.");
			return;
		}
		log(TermColors.GREEN + "Detected 64 bit Linux" + TermColors.RESET);
		log(TermColors.GREEN + "Updating apt db" + TermColors.RESET);
		new CommandExecutor("apt", "update")
				.setSystemInputsAndOutputs()
				.start()
				.waitFor();
		log(TermColors.GREEN + "Installing llvm-12" + TermColors.RESET);
		new CommandExecutor("apt", "install", "llvm-12")
				.setSystemInputsAndOutputs()
				.start()
				.waitFor();
		log(TermColors.GREEN + "Installing lld-12" + TermColors.RESET);
		new CommandExecutor("apt", "install", "lld-12")
				.setSystemInputsAndOutputs()
				.start()
				.waitFor();
	}

	private void installMac(boolean nonInteractive) throws IOException, InterruptedException {
		String brewLocation = CommandExecutor.Execute("which", "brew");
		if(!"/usr/local/bin/brew\n".equals(brewLocation)) {
			log(TermColors.RED + "Homebrew is required for installation. Please install homebrew with "
					+ TermColors.RESET
					+ "/bin/bash -c \"$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)\"");
			return;
		}
		log(TermColors.GREEN + "Updating brew formulae" + TermColors.RESET);
		new CommandExecutor("brew", "update")
				.setSystemInputsAndOutputs()
				.start()
				.waitFor();
		log(TermColors.GREEN + "Installing llvm@12" + TermColors.RESET);
		new CommandExecutor("brew", "install", "llvm@12")
				.setSystemInputsAndOutputs()
				.start()
				.waitFor();
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
	 * @return True if the toolchain appears to be installed, false otherwise.
	 */
	public static boolean validateToolchain() {
		if(OSUtils.GetOS().isWindows()) {
			return validateWindowsToolchain();
		} else if(OSUtils.GetOS().isLinux()) {
			return validateLinuxToolchain();
		} else if(OSUtils.GetOS().isMac()) {
			return validateMacToolchain();
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
			File msvc = new File("C:\\Program Files (x86)\\Microsoft Visual Studio\\2019\\BuildTools\\VC\\Tools\\MSVC");
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

	public static boolean validateLinuxToolchain() {
		String[] which = new String[]{
				"llc-12", "ld.lld-12"
		};
		try {
			for(String bin : which) {
				if(CommandExecutor.Execute("which", bin).isBlank()) {
					log("Missing one or more toolchain file, please re-install the toolchain.");
					return false;
				}
			}
			return true;
		} catch (InterruptedException | IOException e) {
			return false;
		}
	}

	public static boolean validateMacToolchain() {
		if(!new File("/usr/local/opt/llvm@12/bin/clang").exists()) {
			log("Please (re)-install the toolchain.");
			return false;
		}
		return true;
	}
}
