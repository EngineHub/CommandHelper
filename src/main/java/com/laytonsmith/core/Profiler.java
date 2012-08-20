package com.laytonsmith.core;

import com.laytonsmith.PureUtilities.Preferences;
import com.laytonsmith.PureUtilities.Preferences.Preference;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 *
 * @author lsmith
 */
public class Profiler {
	public static void Install(File initFile) throws IOException{
		//We just want to create the config file initially
		GetPrefs(initFile);
	}
	
	public static Preferences GetPrefs(File initFile) throws IOException{
		List<Preference> defaults = new ArrayList<Preference>(Arrays.asList(new Preference[]{
				new Preference("profiler-on", "false", Preferences.Type.BOOLEAN, "Turns the profiler on or off. The profiler can cause a slight amount of lag, so generally speaking"
					+ " you don't leave it on during normal operation."),
				new Preference("profiler-granularity", "1", Preferences.Type.INT, "Sets the granularity of the profiler. 1 logs some things, while 5 logs everything possible."),
				new Preference("profiler-log", "", Preferences.Type.STRING, ""),
				new Preference("write-to-file", "true", Preferences.Type.BOOLEAN, "If true, will write results out to file."),
				new Preference("write-to-screen", "false", Preferences.Type.BOOLEAN, "If true, will write results out to screen."),
		}));
		Preferences prefs = new Preferences("CommandHelper", Static.getLogger(), defaults, "These settings control the integrated profiler");
		prefs.init(initFile);
		return prefs;
	}
	
	private Map<String, Stack<Long>> operations;
	private Map<String, Integer> logGranularities;
	private int configGranularity;
	private boolean profilerOn;
	private String logFile;
	private boolean writeToFile;
	private boolean writeToScreen;
	private Preferences prefs;
	
	public Profiler(File initFile) throws IOException{
		prefs = GetPrefs(initFile);
		operations = new HashMap<String, Stack<Long>>();
		logGranularities = new HashMap<String, Integer>();
		
		configGranularity = (Integer)prefs.getPreference("profiler-granularity");
		profilerOn = (Boolean)prefs.getPreference("profiler-on");
		logFile = (String)prefs.getPreference("profiler-log");
		writeToFile = (Boolean)prefs.getPreference("write-to-file");
		writeToScreen = (Boolean)prefs.getPreference("write-to-screen");
	}
	
	
	public void DoStart(String operationName, int granularity){
		if(!isLoggable(granularity)){
			return;
		}
		logGranularities.put(operationName, granularity);
		if(!operations.containsKey(operationName)){
			operations.put(operationName, new Stack<Long>());
		}
		operations.get(operationName).push(System.currentTimeMillis());
	}
	
	public void DoStop(String operationName){
		long stop = System.currentTimeMillis();
		if(!isLoggable(logGranularities.get(operationName))){
			return;
		}
		long total = stop - operations.get(operationName).pop();
		doLog(operationName + " took a total of " + (total) + "ms");
	}
	
	private boolean isLoggable(Integer granularity){
		if(!profilerOn || granularity == null){
			return false;
		}		
		return granularity >= configGranularity;
	}
	
	private void doLog(String message){
		if(writeToScreen){
			System.out.println(message);
		}
		if(writeToFile){
			//TODO:
		}
	}
}
