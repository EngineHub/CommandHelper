package com.laytonsmith.PureUtilities.VirtualFS;

/**
 * Thrown to indicate that the path given to the VirtualFile
 * contains restricted characters.
 */
public class InvalidVirtualFile extends RuntimeException {

	public InvalidVirtualFile(String message) {
		super(message);
	}

	public InvalidVirtualFile(Throwable cause) {
		super(cause);
	}

	public InvalidVirtualFile(String message, Throwable cause) {
		super(message, cause);
	}
	
}
