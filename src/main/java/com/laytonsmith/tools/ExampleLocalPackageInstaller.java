package com.laytonsmith.tools;

import com.laytonsmith.PureUtilities.Common.StreamUtils;
import com.laytonsmith.PureUtilities.ZipReader;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * 
 */
public class ExampleLocalPackageInstaller {
	private static File jarFolder;
	public static void run(File jarFolder, String arg) throws IOException{
		ExampleLocalPackageInstaller.jarFolder = jarFolder;
		if(arg.isEmpty()){
			listOptions();
		} else {
			install(arg);
		}
	}
	
	private static void listOptions(){
		try {
			ZipReader reader = new ZipReader(new File(ExampleLocalPackageInstaller.class.getResource("/local_packages").getFile()));
			for(ZipReader z : reader.zipListFiles()){
				StreamUtils.GetSystemOut().println(z.getName());
			}
		} catch (IOException ex) {
			Logger.getLogger(ExampleLocalPackageInstaller.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
	private static void install(String pkg) throws IOException{
		URL url = ExampleLocalPackageInstaller.class.getResource("/local_packages/" + pkg);
		if(url == null){
			StreamUtils.GetSystemOut().println("\"" + pkg + "\" is not a valid package name.");
			System.exit(1);
		}
		ZipReader reader = new ZipReader(url);
	    File localPackages = new File(jarFolder, "CommandHelper/LocalPackages/" + pkg);
		if(localPackages.exists() && localPackages.list().length != 0){
			StreamUtils.GetSystemOut().println("The LocalPackage " + pkg + " already exists on your system, and is not empty. Are you sure you wish to possibly overwrite files? (Y/N)");
			StreamUtils.GetSystemOut().print(">");
			String response = new Scanner(System.in).nextLine();
			if(!"Y".equalsIgnoreCase(response)){
				System.exit(0);
			}
		}
		reader.recursiveCopy(localPackages, true);
		StreamUtils.GetSystemOut().println("Local package installed at " + localPackages);
	}
}
