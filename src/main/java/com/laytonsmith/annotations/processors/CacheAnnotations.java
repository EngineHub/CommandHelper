
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
	
	public static void main(String[] args) throws Exception {
		File outputDir = new File(args[0]);
		File scanDir = new File(args[1]);
		System.out.println("-- Caching annotations --");
		System.out.println("Scanning for classes in " + scanDir.getAbsolutePath());
		System.out.println("Outputting file to directory " + outputDir.getAbsolutePath());
		long start = System.currentTimeMillis();
		new ClassDiscoveryCache(new URL("file:" + scanDir.getCanonicalPath()))
				.writeDescriptor(new FileOutputStream(new File(outputDir, ClassDiscovery.OUTPUT_FILENAME)));
		System.out.println("Done writing " + ClassDiscovery.OUTPUT_FILENAME + ", which took " + (System.currentTimeMillis() - start) + " ms.");
	}
}
