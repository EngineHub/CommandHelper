
package com.laytonsmith.PureUtilities.ClassLoading.Annotations;

import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscoveryCache;
import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscoveryURLCache;
import com.laytonsmith.PureUtilities.Common.StreamUtils;
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
		if(outputDir.toString().startsWith("-classpath") || outputDir.toString().startsWith("-Xdebug")){
			//This happens when running locally. I dunno what that is, but we
			//can skip this step.
			StreamUtils.GetSystemOut().println("Skipping annotation caching, running locally.");
			return;
		}
		StreamUtils.GetSystemOut().println("-- Caching annotations --");
		StreamUtils.GetSystemOut().println("Scanning for classes in " + scanDir.getAbsolutePath());
		StreamUtils.GetSystemOut().println("Outputting file to directory " + outputDir.getAbsolutePath());
		long start = System.currentTimeMillis();
		new ClassDiscoveryURLCache(new URL("file:" + scanDir.getCanonicalPath()))
				.writeDescriptor(new FileOutputStream(new File(outputDir, ClassDiscoveryCache.OUTPUT_FILENAME)));
		StreamUtils.GetSystemOut().println("Done writing " + ClassDiscoveryCache.OUTPUT_FILENAME + ", which took " + (System.currentTimeMillis() - start) + " ms.");
	}
}
