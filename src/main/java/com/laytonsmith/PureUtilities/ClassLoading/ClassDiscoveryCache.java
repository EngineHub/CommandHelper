
package com.laytonsmith.PureUtilities.ClassLoading;

import com.laytonsmith.PureUtilities.ProgressIterator;
import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.PureUtilities.ZipReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * This file represents a location on disk that can be used by the ClassDiscovery
 * class to facilitate caching. Files will be automatically managed by this class,
 * and it provides high level functions for getting a cache, regardless of whether
 * or not it actually exists yet.
 */
public class ClassDiscoveryCache {
	
	/**
	 * This is the name of the jar annotation file. getResource(ClassDiscovery.OUTPUT_FILENAME) should
	 * return the file that was output during the build, for this jar for sure. Third party libs
	 * may not be using the same convention though, so this will fail. Regardless, the system should
	 * still function, though it will have to do one time cache setup first.
	 */
	public static final String OUTPUT_FILENAME = "jarInfo.ser";
	
	/**
	 * How much of the file we read in to hash to check for collisions.
	 */
	private static final int READ_SIZE = 2048;

	private File cacheDir;
	private ProgressIterator progress;
	private Logger logger;
	
	/**
	 * Creates a new ClassDiscoveryCache. The File is the location on
	 * disk which is used to write the cache files to.
	 * @param cacheDir 
	 */
	public ClassDiscoveryCache(File cacheDir){
		this.cacheDir = cacheDir;
	}
	
	/**
	 * If not null, informational output is logged to this logger.
	 * @param logger 
	 */
	public void setLogger(Logger logger){
		this.logger = logger;
	}
	
	/**
	 * Given a file location, retrieves the ClassDiscoveryURLCache from it.
	 * If it is a jar, and the jarInfo.ser file exists, it is returned. If not,
	 * the file is hashed, and checked for a local cache copy, and if so, that is
	 * returned. If not, one is created, saved to disk, then returned.
	 * 
	 * No exceptions will be thrown from this class, if something fails, it will fall back
	 * to ultimately just regenerating the cache from source.
	 * @param fromClassLocation The jar to be cached. Note that if this doesn't
	 * denote a jar, the cache will not be written to disk, however a URLCache will
	 * be returned none-the-less.
	 * @return 
	 */
	public ClassDiscoveryURLCache getURLCache(URL fromClassLocation){
		if(fromClassLocation.toString().endsWith(".jar")){
			File location = new File(fromClassLocation.getFile(), ClassDiscoveryCache.OUTPUT_FILENAME);
			ZipReader reader = new ZipReader(location);
			if(reader.exists()){
				try {
					return new ClassDiscoveryURLCache(fromClassLocation, reader.getInputStream());
				} catch (Exception ex) {
					//Do nothing, we'll just re-load from disk.
				}
			}
			File cacheOutputName = null;
			try {
				File jarFile = new File(fromClassLocation.getFile());
				FileInputStream fis = new FileInputStream(jarFile);
				byte[] data = new byte[READ_SIZE];
				fis.read(data);
				fis.close();
				MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
				digest.update(data);
				String fileName = StringUtils.toHex(digest.digest());
				cacheOutputName = new File(cacheDir, fileName);
				if(cacheOutputName.exists()){
					//Cool, already exists, so we'll just return this.
					//Note that we write the data out as a zip, since it is
					//huge otherwise, and compresses quite well, so we have 
					//to read it in as a zip now.
					ZipReader cacheReader = new ZipReader(new File(cacheOutputName, "data"));
					return new ClassDiscoveryURLCache(fromClassLocation, cacheReader.getInputStream());
				}
				//Doesn't exist, but we set cacheOutputName, so it will save it there
				//after it scans.
			} catch (Exception ex) {
				//Hmm. Ok, well, we'll just regenerate.
			}
			if(logger != null){
				logger.log(Level.INFO, "Performing one time scan of {0}, this may take a few moments.", fromClassLocation);
			}
			ClassDiscoveryURLCache cache = new ClassDiscoveryURLCache(fromClassLocation, progress);
			if(cacheOutputName != null){
				try {
					ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(cacheOutputName, false));
					zos.putNextEntry(new ZipEntry("data"));
					cache.writeDescriptor(zos);
					zos.close();
				} catch (IOException ex) {
					//Well, we couldn't write it out, so report the error, but continue anyways.
					if(logger != null){
						logger.log(Level.SEVERE, null, ex);
					} else {
						//Report errors even if the logger passed in is null.
						Logger.getLogger(ClassDiscoveryCache.class.getName()).log(Level.SEVERE, null, ex);
					}
				}
			}
			return cache;
		} else {
			return new ClassDiscoveryURLCache(fromClassLocation, progress);
		}
	}
	
	public void setProgressIterator(ProgressIterator progress){
		this.progress = progress;
	}
}
