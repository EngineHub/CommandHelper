

package com.laytonsmith.core;

import com.laytonsmith.PureUtilities.FileUtility;
import com.laytonsmith.PureUtilities.StreamUtils;
import com.laytonsmith.core.profiler.Profiler;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Layton
 */
public final class Installer {
    
    private Installer(){}

    public static void Install() {
		//Make the main folder
		new File("plugins/CommandHelper").mkdirs();
		//Create the includes folder
        new File("plugins/CommandHelper/includes").mkdirs();
        //Check to see if the auto_include file exists. If not, include it now
        File auto_include = new File("plugins/CommandHelper/auto_include.ms");
        if(!auto_include.exists()){
            String sample = parseISToString(Installer.class.getResourceAsStream("/samp_auto_include.txt"));
            sample = sample.replaceAll("\n|\r\n", System.getProperty("line.separator"));
            try {
                FileUtility.write(sample, auto_include);
            } catch (IOException ex) {
                Logger.getLogger(Installer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
		//Create the local packages folder
        new File("plugins/CommandHelper/LocalPackages").mkdirs();
		try {
			//Let the profiler get set up
			Profiler.Install(new File("plugins/CommandHelper/profiler.config"));
		} catch (IOException ex) {
			Logger.getLogger(Installer.class.getName()).log(Level.SEVERE, null, ex);
		}
		File persistanceNetwork = new File("plugins/CommandHelper/persistance.config");
		if(!persistanceNetwork.exists()){
			try {
				FileUtility.write(StreamUtils.GetString(Installer.class.getResourceAsStream("/samp_persistance_network.txt"), "UTF-8"), persistanceNetwork, true);
			} catch (IOException ex) {
				Logger.getLogger(Installer.class.getName()).log(Level.SEVERE, null, ex);
			}
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
