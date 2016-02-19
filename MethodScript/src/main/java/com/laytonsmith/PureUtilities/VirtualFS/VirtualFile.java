package com.laytonsmith.PureUtilities.VirtualFS;

/**
 * A virtual file represents a path to a file
 * in the virtual file system. Not many operations
 * can be done with this class, it is passed instead to
 * the VirtualFileSystem.
 * 
 */
public class VirtualFile {
	/**
	 * These characters are not allowed in a file name
	 */
	private static final String [] RESTRICTED_CHARS = new String[]{"?","%","*",":","|","\"","<",">"," "};
	
	private String path;
	private boolean isAbsolute;
	
	
	public VirtualFile(String path){
		String working = path;
		working = working.replace('\\', '/');
		for(String s : RESTRICTED_CHARS){
			if(working.contains(s)){
				throw new InvalidVirtualFile("VirtualFiles cannot contain the '" + s + "' character.");
			}
		}
		//Now, remove duplicate slashes
		working = working.replaceAll("[/]{2,}", "/");
		//Remove unneccessary dots
		if(working.startsWith("./")){
			working = working.substring(2);
		}
		working = working.replaceAll("/\\./", "/");
		isAbsolute = working.startsWith("/");
		this.path = working;
	}
	
	boolean isAbsolute(){
		return isAbsolute;
	}
	
	/**
	 * Returns the canonicalized path for this VirtualFile
	 * @return 
	 */
	public String getPath(){
		return path;
	}
	
	@Override
	public String toString(){
		return getPath();
	}
}
