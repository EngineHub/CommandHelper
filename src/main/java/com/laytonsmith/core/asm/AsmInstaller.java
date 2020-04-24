package com.laytonsmith.core.asm;

import com.laytonsmith.PureUtilities.CommandExecutor;
import com.laytonsmith.PureUtilities.Common.FileUtil;
import com.laytonsmith.PureUtilities.Common.OSUtils;
import com.laytonsmith.PureUtilities.Common.StreamUtils;
import com.laytonsmith.PureUtilities.ProgressIterator;
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

	private void log(String output) {
		StreamUtils.GetSystemOut().println(output);
	}


	public void install() throws IOException, InterruptedException {
		if(OSUtils.GetOS().isWindows()) {
			installWindows();
		} else {
			throw new UnsupportedOperationException("Toolchain installation not supported on this platform,"
					+ " though manual installation may still be possible. Note that the currently supported"
					+ " toolchain is version " + LLVM_VERSION);
		}
	}

	private void installWindows() throws IOException, InterruptedException {
		String installerUrl;
		String destFilename;
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

		File exe = download(installerUrl, destFilename);
		log("Installing...");
		log("Please note, when installing, either add the toolchain to PATH, or install in the standard location,"
				+ " \"C:\\Program Files\\LLVM\" to avoid having to specify the toolchain location on every invocation."
				+ " In general, you really should just leave that as default, unless you're absolutely positive you"
				+ " know what you're doing.");
		CommandExecutor.Execute(exe.getAbsolutePath());
		log("Downloading llc.exe");
		String llcUrl
				= "https://raw.githubusercontent.com"
				+ "/LadyCailin/MethodScriptExtra/master/installers/llvm/10.0.0/llc/llc.exe";
		File llcExe = download(llcUrl, "llc.exe");
		File llcDest = new File("C:\\Program Files\\LLVM\\bin\\llc.exe");
		log("Moving llc.exe into " + llcDest + ". If this is incorrect, please manually move it to the"
				+ " correct location.");
		FileUtil.move(llcExe, llcDest);
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
}
