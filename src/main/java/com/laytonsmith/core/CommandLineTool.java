package com.laytonsmith.core;

import com.laytonsmith.PureUtilities.ArgumentParser;

/**
 * Implementations of this that are tagged with {@link tool} will be entered into the cmdline tool system.
 * Internally, all tools are implemented like this, but extensions are also welcome to add new tools as well.
 *
 * Generally, this should not be directly used, use {@link AbstractCommandLineTool} instead.
 */
public interface CommandLineTool {

	/**
	 * Return a new instance of an ArgumentParser for this tool.
	 * @return
	 */
	ArgumentParser getArgumentParser();

	/**
	 * If the tool is executed, this will be called, with the parsedArgs passed in. Within this method, it is
	 * appropriate and expected that {@link System#exit} is called at the end. If the method returns, the framework
	 * will {@code System.exit(0);}
	 * @param parsedArgs The arguments entered by the user.
	 * @throws java.lang.Exception Any exception may be thrown, and it will be printed to {@link System#err}.
	 */
	void execute(ArgumentParser.ArgumentParserResults parsedArgs) throws Exception;

	/**
	 * Whether or not to call System.exit(0) when the program completes. If this returns true, then the tool is
	 * expected to clean up, System.exit will not be called. False (which is the default for
	 * {@link AbstractCommandLineTool} causes it to exit on completion.
	 * @return
	 */
	boolean noExitOnReturn();

}
