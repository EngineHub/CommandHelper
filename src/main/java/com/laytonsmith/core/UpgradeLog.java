
package com.laytonsmith.core;

import com.laytonsmith.PureUtilities.Common.FileUtil;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import org.json.simple.JSONValue;

/**
 * This class performs a check to see if this is an upgrade, and if so,
 * can be used to perform various upgrades. The upgrade log is updated with
 * information about upgrade history, so upgrade actions are not performed
 * unnecessarily. 
 */
public class UpgradeLog {

	private final File logFile;
	private final List<UpgradeTask> tasks;
	private final List<Upgrade> upgrades = new ArrayList<Upgrade>();
	
	/**
	 * Creates a new UpgradeLog object.
	 * @param logFile The location of the upgrade log. This doesn't need to yet
	 * exist, but it does need to be createable as a file.
	 */
	public UpgradeLog(File logFile) {
		this.logFile = logFile;
		this.tasks = new ArrayList<UpgradeTask>();
	}
	
	/**
	 * Adds an upgrade task. This task will be run only if the task's
	 * doRun() method returns true.
	 * @param task
	 */
	public void addUpgradeTask(UpgradeTask task) {
		tasks.add(task);
		task.that = this;
	}
	
	/**
	 * Runs the required upgrade tasks. Tasks are run sequentially, from oldest to newest
	 * version tasks, starting with the last version that was run.
	 * @throws java.io.IOException If the output log can't be written to.
	 */
	public void runTasks() throws IOException{
		if(logFile.exists()){
			List<Map<String, String>> jsonUpgrades = (List<Map<String, String>>)JSONValue.parse(FileUtil.read(logFile));
			for(Map<String, String> m : jsonUpgrades){
				upgrades.add(Upgrade.fromMap(m));
			}
		}
		
		for(UpgradeTask task : tasks){
			if(task.doRun()){
				task.run();
			}
		}
		
		List<Map<String, String>> jsonUpgrades = new ArrayList<Map<String, String>>();
		for(Upgrade u : upgrades){
			jsonUpgrades.add(u.toMap());
		}
		String newJSON = JSONValue.toJSONString(jsonUpgrades);
		FileUtil.write(newJSON, logFile);
	}
	
	public static abstract class UpgradeTask implements Runnable {
		UpgradeLog that = null;
		
		/**
		 * Checks to see if a previous task has left a breadcrumb.
		 * See {@link UpgradeTask#leaveBreadcrumb(java.lang.String)} for
		 * more information about breadcrumbs.
		 * @param breadcrumb
		 * @return 
		 */
		protected boolean hasBreadcrumb(String breadcrumb){
			for(Upgrade u : that.upgrades){
				if(u.breadcrumb.equals(breadcrumb)){
					return true;
				}
			}
			return false;
		}
		
		/**
		 * This method should return true if the associated upgrade task
		 * should be run, or false if not. If true is returned, run() will
		 * be called. If false, run() will not be called.
		 * @return 
		 */
		public abstract boolean doRun();
		
		/**
		 * Leaves a breadcrumb. A breadcrumb should be a unique
		 * name, which can be used by future upgrade detection
		 * algorithms to determine if this task has run or not.
		 * @param breadcrumb 
		 */
		protected void leaveBreadcrumb(String breadcrumb){
			Upgrade u = new Upgrade();
			u.breadcrumb = breadcrumb;
			u.upgradeTime = System.currentTimeMillis();
			that.upgrades.add(u);
		}
	}
	
	private static class Upgrade {
		
		String breadcrumb;
		long upgradeTime;
		
		public static Upgrade fromMap(Map<String, String> map){
			Upgrade u = new Upgrade();
			u.breadcrumb = map.get("breadcrumb");
			u.upgradeTime = Long.parseLong(map.get("upgradeTime"));
			return u;
		}
		
		public Map<String, String> toMap(){
			Map<String, String> map = new HashMap<String, String>();
			map.put("breadcrumb", breadcrumb);
			map.put("upgradeTime", Long.toString(upgradeTime));
			return map;
		}
	}
}
