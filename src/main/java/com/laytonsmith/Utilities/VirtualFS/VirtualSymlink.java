package com.laytonsmith.Utilities.VirtualFS;

import java.io.File;

/**
 * A virtual symlink is a transparent link from a VirtualFile to a real File, anywhere on the actual file system.
 * According to the virtual file system, this link will be completely transparent.
 *
 */
public class VirtualSymlink {

	private final VirtualFile virtual;
	private final File real;

	public VirtualSymlink(VirtualFile virtual, File real) {
		this.virtual = virtual;
		this.real = real;
	}

	public VirtualFile getVirtual() {
		return virtual;
	}

	public File getReal() {
		return real;
	}
}
