package com.laytonsmith.PureUtilities.VirtualFS;

/**
 * A VirtualGlob is a simple wrapper around a file matching glob. The following
 * rules are applied: All characters are taken literally, except for the asterisk,
 * double asterisk, or question mark, which mean the following:
 * <ul>
 * <li>* - Match any number of characters, except directory separators (forward slash)</li>
 * <li>** - Match any number of characters, including directory separators (forward slash)</li>
 * <li>? - Match exactly one character, except directory separators (forward slash)</li>
 * </ul>
 * 
 * The essential operations of a glob simply ask if a particular VirtualFile actually match
 * this glob or not.
 * 
 * @author lsmith
 */
public class VirtualGlob implements Comparable<VirtualGlob> {
	
	private String glob;
	
	public VirtualGlob(String glob){
		throw new UnsupportedOperationException("Not implemented yet.");
	}
	
	/**
	 * Creates a glob that will match this file.
	 * @param file 
	 */
	public VirtualGlob(VirtualFile file){
		glob = file.getPath();
	}
	
	public boolean matches(VirtualFile file){
		throw new UnsupportedOperationException("Not implemented yet.");
	}

	public int compareTo(VirtualGlob o) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public String toString() {
		return glob;
	}
	
	
}
