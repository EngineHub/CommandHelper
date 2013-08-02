
package com.laytonsmith.annotations.processors;

import com.laytonsmith.PureUtilities.ClassDiscovery;
import com.laytonsmith.PureUtilities.ClassDiscoveryCache;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;

/**
 *
 */
public class CacheAnnotations {
	
	/**
	 * This is the name of the jar annotation file. getResource(CacheAnotations.OUTPUT_FILENAME) should
	 * return the file that was output during the build, for this jar for sure. Third party libs
	 * may not be using the same convention though, so this will fail. Regardless, the system should
	 * still function, though it will have to do one time cache setup first.
	 */
	public static final String OUTPUT_FILENAME = "jarInfo.ser";
	
	public static void main(String[] args) throws Exception {
		File outputDir = new File(args[0]);
		File scanDir = new File(args[1]);
		System.out.println("-- Caching annotations --");
		System.out.println("Scanning for classes in " + scanDir.getAbsolutePath());
		System.out.println("Outputting file to directory " + outputDir.getAbsolutePath());
		long start = System.currentTimeMillis();
		new ClassDiscoveryCache(new URL("file:" + scanDir.getCanonicalPath()))
				.writeDescriptor(new FileOutputStream(new File(outputDir, OUTPUT_FILENAME)));
		System.out.println("Done writing " + OUTPUT_FILENAME + ", which took " + (System.currentTimeMillis() - start) + " ms.");
	}
}
