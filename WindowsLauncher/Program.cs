using Microsoft.Win32;
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;
using System.IO.Compression;
using System.Linq;

namespace mscript {
	class Program {
		static void Main(string[] args) {
			//try {
			//	if (System.Deployment.Application.ApplicationDeployment.CurrentDeployment.IsFirstRun) {
			//		Console.WriteLine("Finishing installation...");
			//		SetAddRemoveProgramsIcon();
			//		Console.WriteLine("Installation complete. Press enter to exit.");
			//		Console.ReadLine();
			//		return;
			//	}
			//} catch(System.Deployment.Application.InvalidDeploymentException e) {
			//	// Being run directly from the executable, so this is not the first installed run anyways.
			//}

			string[] activationData = null;
			if (AppDomain.CurrentDomain.SetupInformation.ActivationArguments != null) {
				activationData = AppDomain.CurrentDomain.SetupInformation.ActivationArguments.ActivationData;
			}
			string startFile = null;
			if (activationData != null) {
				// We were launched directly if this is null, otherwise
				// activationData[0] has the name of the file we were launched from
				startFile = activationData[0];
			}

			string jarLocation = Registry.CurrentUser.OpenSubKey("Software\\MethodScript").GetValue("JarLocation").ToString();
			List<string> modulesArgs = new List<string>();
			// Pull out the modules and add them here if java > 8
			{
				ProcessStartInfo start = new ProcessStartInfo();
				start.FileName = "java.exe";
				start.RedirectStandardOutput = true;
				start.UseShellExecute = false;
				List<string> a = new List<string>();
				a.Add("-jar");
				a.Add(jarLocation);
				a.Add("java-version");
				start.Arguments = JoinAndEscape(a);
				string strOutput;
				using(Process p = Process.Start(start)) {
					strOutput = p.StandardOutput.ReadToEnd();
					p.WaitForExit();
				}
				int javaVersion = int.Parse(strOutput);
				if(javaVersion > 8) {
					ZipArchive zipFile = ZipFile.OpenRead(jarLocation);
					ZipArchiveEntry file = zipFile.Entries.Where((ZipArchiveEntry e) => {
						return e.FullName.Equals("interpreter-helpers/modules");
					}).Single();
					Stream stream = file.Open();
					StreamReader reader = new StreamReader(stream);
					string modules = reader.ReadToEnd();
					modules = modules.Replace("\r", "");
					foreach(string module in modules.Split('\n')) {
						if(module.Equals(string.Empty)) {
							continue;
						}
						modulesArgs.Add("--add-opens");
						modulesArgs.Add(module + "=ALL-UNNAMED");
					}
				}
			}

			List<string> command = new List<string>();
			if(System.Environment.GetEnvironmentVariable("DEBUG_MSCRIPT") != null && System.Environment.GetEnvironmentVariable("DEBUG_MSCRIPT") == "1") {
				// java debug mode
				command.Add("-Xrs");
				command.Add("-Xdebug");
				command.Add("-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=9001");
			}
			command.AddRange(modulesArgs);
			if(args.Length == 0) {
				// start interpreter
				command.Add("-jar");
				command.Add(jarLocation);
				command.Add("interpreter");
				// Don't think these are needed here
				//command.Add("--location-----");
				//command.Add(AppDomain.CurrentDomain.BaseDirectory);
			} else {
				// launched from a *.ms file
				if(args.Length > 0 && args[0] == "--") {
					// Script passthrough to java -jar CH.jar <arguments>
					command.Add("-jar");
					command.Add(jarLocation);
					// Remove the -- from the args
					for(int i = 1; i < args.Length; i++) {
						command.Add(args[i]);
					}
				} else { 
					// Normal launch with script
					command.Add("-jar");
					command.Add(jarLocation);
					command.Add("cmdline");
					command.Add(startFile);
					command.AddRange(args);
				}
			}

			{
				ProcessStartInfo start = new ProcessStartInfo();
				start.UseShellExecute = false;
				start.Arguments = JoinAndEscape(command);
#if DEBUG
				Console.WriteLine("Command is: " + start.Arguments);
#endif
				start.FileName = "java.exe";
				start.UseShellExecute = false;

				//Console.WriteLine(start.Arguments);

				int exitCode;

				using(Process proc = Process.Start(start)) {
					proc.WaitForExit();
					exitCode = proc.ExitCode;
				}
			}

			//Console.WriteLine("command: " + string.Join(" ", command));
			//Console.ReadLine();
			return;
		}

		private static string JoinAndEscape(List<string> args) {
			string s = "";
			foreach(string arg in args) {
				if(arg == null) {
					continue;
				}
				string arg2 = arg.Replace("\\", "\\\\");
				arg2 = arg2.Replace("\"", "\\\"");
				arg2 = "\"" + arg2 + "\"";
				s += arg2;			
				s += " ";
			}
			return s;
		}

		//private static void SetAddRemoveProgramsIcon() {
		//	try {
		//		string assemblyDescription = "MethodScript";
				
		//		//the icon is included in this program
		//		string iconSourcePath = Path.Combine(System.Windows.Forms.Application.StartupPath, "commandhelper_icon.ico");
		//		//Console.WriteLine("iconSourcePath: " + iconSourcePath);
		//		//Console.WriteLine("assemblyDescription: " + assemblyDescription);

		//		if (!File.Exists(iconSourcePath))
		//			return;

		//		RegistryKey myUninstallKey = Registry.CurrentUser.OpenSubKey(@"Software\Microsoft\Windows\CurrentVersion\Uninstall");
		//		string[] mySubKeyNames = myUninstallKey.GetSubKeyNames();
		//		for (int i = 0; i < mySubKeyNames.Length; i++) {
		//			RegistryKey myKey = myUninstallKey.OpenSubKey(mySubKeyNames[i], true);
		//			object myValue = myKey.GetValue("DisplayName");
		//			if (myValue != null && myValue.ToString() == assemblyDescription) {
		//				//Console.WriteLine("Setting iconSourcePath in " + myKey.Name);
		//				myKey.SetValue("DisplayIcon", iconSourcePath);
		//				break;
		//			}
		//		}
		//	} catch(Exception) {
		//		//log an error
		//	}
		//}
	}
}
