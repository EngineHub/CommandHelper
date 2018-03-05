package com.laytonsmith.PureUtilities;

/**
 * This is a collection of all the POSIX standard signals.
 */
public enum Signals implements SignalType {
	/**
	 * The SIGABRT signal is sent to a process to tell it to abort, i.e. to terminate. The signal is usually initiated
	 * by the process itself when it calls abort function of the C Standard Library, but it can be sent to the process
	 * from outside as well as any other signal.
	 */
	SIGABRT("ABRT", DefaultAction.ACTION_TERMINATE),
	/**
	 * The SIGALRM, SIGVTALRM and SIGPROF signal is sent to a process when the time limit specified in a call to a
	 * preceding alarm setting function (such as setitimer) elapses. SIGALRM is sent when real or clock time elapses.
	 * SIGVTALRM is sent when CPU time used by the process elapses. SIGPROF is sent when CPU time used by the process
	 * and by the system on behalf of the process elapses.
	 */
	SIGALRM("ALRM", DefaultAction.TERMINATE),
	/**
	 * The SIGBUS signal is sent to a process when it causes a bus error. The conditions that lead to the signal being
	 * raised are, for example, incorrect memory access alignment or non-existent physical address.
	 */
	SIGBUS("BUS", DefaultAction.ACTION_TERMINATE),
	/**
	 * The SIGCHLD signal is sent to a process when a child process terminates, is interrupted, or resumes after being
	 * interrupted. One common usage of the signal is to instruct the operating system to clean up the resources used by
	 * a child process after its termination without an explicit call to the wait system call.
	 */
	SIGCHLD("CHLD", DefaultAction.IGNORE),
	/**
	 * The SIGCONT signal instructs the operating system to continue (restart) a process previously paused by the
	 * SIGSTOP or SIGTSTP signal. One important use of this signal is in job control in the Unix shell.
	 */
	SIGCONT("CONT", DefaultAction.CONTINUE),
	/**
	 * The SIGFPE signal is sent to a process when it executes an erroneous arithmetic operation, such as division by
	 * zero (the FPE stands for floating point exception).
	 */
	SIGFPE("FPE", DefaultAction.ACTION_TERMINATE),
	/**
	 * The SIGHUP signal is sent to a process when its controlling terminal is closed. It was originally designed to
	 * notify the process of a serial line drop (a hangup). In modern systems, this signal usually means that the
	 * controlling pseudo or virtual terminal has been closed. Many daemons will reload their configuration files and
	 * reopen their logfiles instead of exiting when receiving this signal.
	 */
	SIGHUP("HUP", DefaultAction.TERMINATE),
	/**
	 * The SIGILL signal is sent to a process when it attempts to execute an illegal, malformed, unknown, or privileged
	 * instruction.
	 */
	SIGILL("ILL", DefaultAction.ACTION_TERMINATE),
	/**
	 * The SIGINT signal is sent to a process by its controlling terminal when a user wishes to interrupt the process.
	 * This is typically initiated by pressing Control-C, but on some systems, the "delete" character or "break" key can
	 * be used.
	 */
	SIGINT("INT", DefaultAction.TERMINATE),
	/**
	 * The SIGKILL signal is sent to a process to cause it to terminate immediately (kill). In contrast to SIGTERM and
	 * SIGINT, this signal cannot be caught or ignored, and the receiving process cannot perform any clean-up upon
	 * receiving this signal.
	 */
	SIGKILL("KILL", DefaultAction.TERMINATE, false),
	/**
	 * The SIGPIPE signal is sent to a process when it attempts to write to a pipe without a process connected to the
	 * other end.
	 */
	SIGPIPE("PIPE", DefaultAction.TERMINATE),
	/**
	 * The SIGQUIT signal is sent to a process by its controlling terminal when the user requests that the process quit
	 * and perform a core dump.
	 */
	SIGQUIT("QUIT", DefaultAction.ACTION_TERMINATE),
	/**
	 * The SIGSEGV signal is sent to a process when it makes an invalid virtual memory reference, or segmentation fault,
	 * i.e. when it performs a segmentation violation.
	 */
	SIGSEGV("SEGV", DefaultAction.ACTION_TERMINATE),
	/**
	 * The SIGSTOP signal instructs the operating system to stop a process for later resumption. Cannot be caught or
	 * ignored.
	 */
	SIGSTOP("STOP", DefaultAction.STOP, false),
	/**
	 * The SIGTERM signal is sent to a process to request its termination. Unlike the SIGKILL signal, it can be caught
	 * and interpreted or ignored by the process. This allows the process to perform nice termination releasing
	 * resources and saving state if appropriate. It should be noted that SIGINT is nearly identical to SIGTERM.
	 */
	SIGTERM("TERM", DefaultAction.TERMINATE),
	/**
	 * The SIGTSTP signal is sent to a process by its controlling terminal to request it to stop temporarily. It is
	 * commonly initiated by the user pressing Control-Z. Unlike SIGSTOP, the process can register a signal handler for
	 * or ignore the signal.
	 */
	SIGTSTP("STP", DefaultAction.STOP),
	/**
	 * The SIGTTIN and SIGTTOU signals are sent to a process when it attempts to read in or write out respectively from
	 * the tty while in the background. Typically, this signal can be received only by processes under job control;
	 * daemons do not have controlling terminals and should never receive this signal.
	 */
	SIGTTIN("TTIN", DefaultAction.STOP),
	/**
	 * The SIGTTIN and SIGTTOU signals are sent to a process when it attempts to read in or write out respectively from
	 * the tty while in the background. Typically, this signal can be received only by processes under job control;
	 * daemons do not have controlling terminals and should never receive this signal.
	 */
	SIGTTOU("TTOU", DefaultAction.STOP),
	/**
	 * The SIGUSR1 and SIGUSR2 signals are sent to a process to indicate user-defined conditions.
	 */
	SIGUSR1("USR1", DefaultAction.TERMINATE),
	/**
	 * The SIGUSR1 and SIGUSR2 signals are sent to a process to indicate user-defined conditions.
	 */
	SIGUSR2("USR2", DefaultAction.TERMINATE),
	/**
	 * The SIGPOLL signal is sent to a process when an asynchronous I/O event occurs (meaning it has been polled).
	 */
	SIGPOLL("POLL", DefaultAction.TERMINATE),
	/**
	 * The SIGALRM, SIGVTALRM and SIGPROF signal is sent to a process when the time limit specified in a call to a
	 * preceding alarm setting function (such as setitimer) elapses. SIGALRM is sent when real or clock time elapses.
	 * SIGVTALRM is sent when CPU time used by the process elapses. SIGPROF is sent when CPU time used by the process
	 * and by the system on behalf of the process elapses.
	 */
	SIGPROF("PROF", DefaultAction.TERMINATE),
	/**
	 * The SIGSYS signal is sent to a process when it passes a bad argument to a system call.
	 */
	SIGSYS("SYS", DefaultAction.ACTION_TERMINATE),
	/**
	 * The SIGTRAP signal is sent to a process when an exception (or trap) occurs: a condition that a debugger has
	 * requested to be informed of — for example, when a particular function is executed, or when a particular variable
	 * changes value.
	 */
	SIGTRAP("TRAP", DefaultAction.ACTION_TERMINATE),
	/**
	 * The SIGURG signal is sent to a process when a socket has urgent or out-of-band data available to read.
	 */
	SIGURG("URG", DefaultAction.IGNORE),
	/**
	 * The SIGALRM, SIGVTALRM and SIGPROF signal is sent to a process when the time limit specified in a call to a
	 * preceding alarm setting function (such as setitimer) elapses. SIGALRM is sent when real or clock time elapses.
	 * SIGVTALRM is sent when CPU time used by the process elapses. SIGPROF is sent when CPU time used by the process
	 * and by the system on behalf of the process elapses.
	 */
	SIGVTALRM("VTALRM", DefaultAction.TERMINATE),
	/**
	 * The SIGXCPU signal is sent to a process when it has used up the CPU for a duration that exceeds a certain
	 * predetermined user-settable value. The arrival of a SIGXCPU signal provides the receiving process a chance to
	 * quickly save any intermediate results and to exit gracefully, before it is terminated by the operating system
	 * using the SIGKILL signal.
	 */
	SIGXCPU("XCPU", DefaultAction.ACTION_TERMINATE),
	/**
	 * The SIGXFSZ signal is sent to a process when it grows a file larger than the maximum allowed size.
	 */
	SIGXFSZ("XFSZ", DefaultAction.ACTION_TERMINATE),;

	private final String signalName;
	/**
	 * The default action of a signal
	 */
	private final DefaultAction defaultAction;
	/**
	 * Whether or not a signal is catchable
	 */
	private final boolean catchable;

	private Signals(String signalName, DefaultAction defaultAction) {
		this(signalName, defaultAction, true);
	}

	private Signals(String signalName, DefaultAction defaultAction, boolean catchable) {
		this.signalName = signalName;
		this.defaultAction = defaultAction;
		this.catchable = catchable;
	}

	/**
	 * Returns the default action of the signal.
	 *
	 * @return
	 */
	@Override
	public DefaultAction getDefaultAction() {
		return this.defaultAction;
	}

	/**
	 * Some signals are not catchable. If this returns true, this is one of those signals.
	 *
	 * @return
	 */
	@Override
	public boolean isCatchable() {
		return this.catchable;
	}

	/**
	 * Returns the signal name, as required by the JVM.
	 *
	 * @return
	 */
	@Override
	public String getSignalName() {
		return this.signalName;
	}

}
