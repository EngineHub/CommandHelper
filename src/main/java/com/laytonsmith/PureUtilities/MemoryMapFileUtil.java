/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.PureUtilities;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Layton
 */
public class MemoryMapFileUtil {
	private static Map<String, MemoryMapFileUtil> instances = new HashMap<String, MemoryMapFileUtil>();
	
	public static MemoryMapFileUtil getInstance(File f, DataGrabber grabber) throws IOException{
		String s = f.getCanonicalPath();
		MemoryMapFileUtil mem;
		if(!instances.containsKey(s)){
			mem = new MemoryMapFileUtil(f, grabber);
			instances.put(s, mem);
		} else {
			mem = instances.get(s);
		}		
		mem.grabber = grabber;
		return mem;
	}
	
	public static interface DataGrabber{
		byte[] getData();
	}
	
	private String file;
	private DataGrabber grabber;
	private boolean dirty = false;
	private boolean running = false;
	private ExecutorService service;
	private MemoryMapFileUtil(File file, DataGrabber grabber) throws IOException{
		this.file = file.getCanonicalPath();
		this.grabber = grabber;
	}
	
	private void run(){
		try{
			synchronized(this){
				running = true;
			}
			while(true){
				
				File temp = null;
				try {
					synchronized(this){
						if(!dirty){
							return;
						}
					}
					temp = File.createTempFile("MemoryMapFile", ".tmp");
					File permanent = new File(file);
					FileUtility.write(grabber.getData(), temp, FileUtility.OVERWRITE, true);
					if(FileUtility.move(temp, permanent)){
						//If and only if the file was moved, do we want to clear the dirty flag.
						synchronized(this){
							dirty = false;
						}
					}
				} catch (IOException ex) {
					Logger.getLogger(MemoryMapFileUtil.class.getName()).log(Level.SEVERE, null, ex);
				} finally {
					if(temp != null){
						temp.delete();
						temp.deleteOnExit();
					}
				}
			}
		} finally{
			synchronized(this){
				running = false;
			}
		}
	}
	
	/**
	 * Marks the data as dirty. This also triggers the writer to start if it isn't already
	 * started. Multiple calls to mark do not necessarily cause the output to be written
	 * multiple times, it simply sets the flag
	 */
	public void mark(){
		synchronized(this){
			dirty = true;
			if(!running){
				getService().submit(new Runnable() {

					public void run() {
						MemoryMapFileUtil.this.run();
					}
				});
			}
		}
	}
	
	private ExecutorService getService(){
		if(service == null){
			service = Executors.newSingleThreadExecutor(new ThreadFactory() {

				public Thread newThread(Runnable r) {
					return new Thread(r, "MemoryMapWriter-" + file);
				}
			});
		}
		return service;
	}
}
