package com.laytonsmith.core;

import com.laytonsmith.PureUtilities.SSHWrapper;

/**
 * This class is for testing concepts
 *
 * @author Layton
 */
public class MainSandbox {

	public static void main(String[] argv) throws Exception {
		//SSHWrapper.SCP("/home/lsmith/test.txt", "lsmith@localhost:/home/lsmith/test2.txt");
		SSHWrapper.SCP("lsmith@localhost:/home/lsmith/test.txt", "/home/lsmith/test2.txt");
	}
}
