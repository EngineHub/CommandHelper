

package com.laytonsmith.core;

import com.laytonsmith.PureUtilities.Common.FileUtil;
import com.laytonsmith.PureUtilities.Common.StreamUtils;
import com.laytonsmith.core.profiler.Profiler;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class provides installation methods. They create the appropriate folders and files, if
 * they aren't already there.
 */
public final class Installer {

    private Installer(){}

    public static void Install(File root) {
		//Make the main folder
		root.mkdirs();
		//Create the includes folder
        new File(root, "includes").mkdirs();
		//Extensions folder
		new File(root, "extensions").mkdirs();
        //Check to see if the auto_include file exists. If not, include it now
        File auto_include = new File(root, "auto_include.ms");
        if(!auto_include.exists()){
            String sample = parseISToString(Installer.class.getResourceAsStream("/samp_auto_include.txt"));
            sample = sample.replaceAll("\n|\r\n", System.getProperty("line.separator"));
            try {
                FileUtil.write(sample, auto_include);
            } catch (IOException ex) {
                Logger.getLogger(Installer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
		//Create the local packages folder
        MethodScriptFileLocations.getDefault().getLocalPackagesDirectory().mkdirs();
		try {
			//Let the profiler get set up
			Profiler.Install(MethodScriptFileLocations.getDefault().getProfilerConfigFile());
		} catch (IOException ex) {
			Logger.getLogger(Installer.class.getName()).log(Level.SEVERE, null, ex);
		}
		File persistenceNetwork = MethodScriptFileLocations.getDefault().getPersistenceConfig();
		if(!persistenceNetwork.exists()){
			try {
				FileUtil.write(StreamUtils.GetString(Installer.class.getResourceAsStream("/samp_persistence_network.txt"), "UTF-8"), persistenceNetwork, true);
			} catch (IOException ex) {
				Logger.getLogger(Installer.class.getName()).log(Level.SEVERE, null, ex);
			}
		}

		if(!MethodScriptFileLocations.getDefault().getProfilesFile().exists()){
			try {
				FileUtil.write(StreamUtils.GetString(Installer.class.getResourceAsStream("/samp_profiles.xml"), "UTF-8"), MethodScriptFileLocations.getDefault().getProfilesFile(), true);
			} catch (IOException ex) {
				Logger.getLogger(Installer.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
    }

	public static void InstallCmdlineInterpreter(){
		MethodScriptFileLocations.getDefault().getCmdlineInterpreterDirectory().mkdir();
		try {
			MethodScriptFileLocations.getDefault().getCmdlineInterpreterAutoIncludeFile().createNewFile();
		} catch (IOException ex) {
			Logger.getLogger(Installer.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

    public static String parseISToString(java.io.InputStream is) {
        BufferedReader din = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        try {
            String line;
            while ((line = din.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (Exception ex) {
            ex.getMessage();
        } finally {
            try {
                is.close();
            } catch (Exception ex) {
            }
        }
        return sb.toString();
    }
}
