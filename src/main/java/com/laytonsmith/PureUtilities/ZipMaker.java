package com.laytonsmith.PureUtilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 *
 *
 */
public final class ZipMaker {

	private ZipMaker() {
	}

	/**
	 * Makes a zip file using all the files in the directory specified. The filename is used to place the zip file at
	 * the same level as the starting directory.
	 *
	 * @param startingDir The directory to zip up
	 * @param filename The name of the zip file to create
	 */
	public static void MakeZip(File startingDir, String filename) throws IOException {
		MakeZip(startingDir, filename, false);
	}

	/**
	 * Makes a zip file using all the files in the directory specified. The filename is used to place the zip file at
	 * the same level as the starting directory.
	 *
	 * @param startingDir The directory to zip up
	 * @param filename The name of the zip file to create
	 * @param createTopLevelFolder If true, then files in the zip will be created inside a folder named the same as the
	 * filename (minus extension)
	 */
	public static void MakeZip(File startingDir, String filename, boolean createTopLevelFolder) throws IOException {
		String topLevel = "";
		if(createTopLevelFolder) {
			if(filename.lastIndexOf(".") == -1) {
				topLevel = filename + "/";
			} else {
				topLevel = filename.substring(0, filename.lastIndexOf(".")) + "/";
			}
		}
		if(startingDir.isDirectory()) {
			Set<File> files = new LinkedHashSet<File>();
			GetFiles(files, startingDir.getCanonicalFile(), startingDir.getCanonicalFile());
			MakeZip(files, new File(startingDir.getParentFile(), filename), startingDir, topLevel);
		} else {
			MakeZip(new LinkedHashSet<File>(Arrays.asList(startingDir)), new File(startingDir.getParentFile(), filename), startingDir, topLevel);
		}
	}

	private static void MakeZip(Set<File> files, File output, File base, String topLevel) throws IOException {
		// These are the files to include in the ZIP file

		// Create a buffer for reading the files
		byte[] buf = new byte[1024];
		// Create the ZIP file
		ZipOutputStream out = new ZipOutputStream(new FileOutputStream(output));

		// Compress the files
		for(File f : files) {
			FileInputStream in = new FileInputStream(new File(base, f.getPath()));

			// Add ZIP entry to output stream.
			out.putNextEntry(new ZipEntry(topLevel + GetUnabsoluteFile(base, f).getPath()));

			// Transfer bytes from the file to the ZIP file
			int len;
			while((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}

			// Complete the entry
			out.closeEntry();
			in.close();
			//JVM Bug
			in = null;
			GCUtil.BlockUntilGC();
		}

		// Complete the ZIP file
		out.close();
	}

	private static void GetFiles(Set<File> ongoing, File directory, File base) throws IOException {
		if(directory.isDirectory()) {
			for(File f : directory.listFiles()) {
				GetFiles(ongoing, f, base);
			}
		} else {
			File file = new File(directory.getAbsolutePath().replaceFirst(Pattern.quote(base.getAbsolutePath() + "/"), ""));
			ongoing.add(file);
		}
	}

	private static File GetUnabsoluteFile(File base, File child) throws IOException {
		String path = new File(base, child.getPath()).getCanonicalPath().replaceFirst(Pattern.quote(base.getCanonicalPath() + File.separatorChar), "");
		return new File(path);
	}
}
