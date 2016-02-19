package com.laytonsmith.PureUtilities.VirtualFS;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;

/**
 *
 * 
 */
@FileSystemLayer.fslayer("file")
public class RealFileSystemLayer extends FileSystemLayer {

	protected final File real;

	public RealFileSystemLayer(VirtualFile path, VirtualFileSystem fileSystem, String symlink) throws IOException {
		super(path, fileSystem);
		if (symlink == null) {
			real = new File(fileSystem.root, path.getPath());
			if (!real.getCanonicalPath().startsWith(fileSystem.root.getCanonicalPath())) {
				throw new PermissionException(path.getPath() + " extends above the root directory of this file system, and does not point to a valid file.");
			}
		} else {
			File symlinkRoot = new File(fileSystem.symlinkFile, symlink);
			real = new File(symlinkRoot, path.getPath());
			//If the path extends above the symlink, disallow it
			if (!real.getCanonicalPath().startsWith(symlinkRoot.getCanonicalPath())) {
				//Unless of course, the path is still within the full real path, then
				//eh, we'll allow it.
				if (!real.getCanonicalPath().startsWith(fileSystem.root.getCanonicalPath())) {
					throw new PermissionException(path.getPath() + " extends above the root directory of this file system, and does not point to a valid file.");
				}
			}
		}
	}

	@Override
	public InputStream getInputStream() throws FileNotFoundException {
		return new FileInputStream(real);
	}

	@Override
	public void writeByteArray(byte[] bytes) throws IOException {
		FileUtils.writeByteArrayToFile(real, bytes);
	}

	@Override
	public VirtualFile[] listFiles() throws IOException {
		List<VirtualFile> virtuals = new ArrayList<VirtualFile>();
		for (File sub : real.listFiles()) {
			virtuals.add(normalize(sub));
		}
		return virtuals.toArray(new VirtualFile[virtuals.size()]);
	}

	private VirtualFile normalize(File real) throws IOException {
		String path = real.getCanonicalPath().replaceFirst(Pattern.quote(fileSystem.root.getCanonicalPath()), "");
		path = path.replace('\\', '/');
		if (!path.startsWith("/")) {
			path = "/" + path;
		}
		return new VirtualFile(path);
	}

	@Override
	public void delete() throws IOException {
		if (!real.delete()) {
			throw new IOException("Could not delete the file");
		}
	}

	@Override
	public void deleteOnExit() {
		real.deleteOnExit();
	}

	@Override
	public boolean exists() {
		return real.exists();
	}

	@Override
	public boolean canRead() {
		return real.canRead();
	}

	@Override
	public boolean canWrite() {
		return real.canWrite();
	}

	@Override
	public boolean isDirectory() {
		return real.isDirectory();
	}

	@Override
	public boolean isFile() {
		return real.isFile();
	}

	@Override
	public void mkdirs() throws IOException {
		if (!real.mkdirs()) {
			throw new IOException("Directory structure could not be created");
		}
	}

	@Override
	public void createNewFile() throws IOException {
		if (!real.createNewFile()) {
			throw new IOException("File already exists!");
		}
	}
}
