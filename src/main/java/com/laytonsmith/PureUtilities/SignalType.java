package com.laytonsmith.PureUtilities;

/**
 *
 */
public interface SignalType {

	/**
	 * Returns the default action of the signal.
	 *
	 * @return
	 */
	DefaultAction getDefaultAction();

	/**
	 * Some signals are not catchable. If this returns true, this is one of those signals.
	 *
	 * @return
	 */
	boolean isCatchable();

	/**
	 * Returns the signal name, as required by the JVM.
	 *
	 * @return
	 */
	String getSignalName();

	/**
	 * A default action is the action that a signal will have if it is ignored.
	 */
	public static enum DefaultAction {
		/**
		 * Abnormal termination of the process. The process is terminated with all the consequences of _exit() except
		 * that the status made available to wait() and waitpid() indicates abnormal termination by the specified
		 * signal.
		 */
		TERMINATE,
		/**
		 * Abnormal termination of the process. Additionally, implementation-defined abnormal termination actions, such
		 * as creation of a core file, may occur.
		 */
		ACTION_TERMINATE,
		/**
		 * Ignore the signal.
		 */
		IGNORE,
		/**
		 * Stop the process.
		 */
		STOP,
		/**
		 * Continue the process, if it is stopped; otherwise, ignore the signal.
		 */
		CONTINUE;
	}

}
