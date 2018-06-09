package com.laytonsmith.PureUtilities;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import sun.misc.Signal;

/**
 * Adds a generic way to handle signals, if the VM was started with the -Xrs option.
 */
public class SignalHandler {

	private static final Map<SignalType, SignalCallback> HANDLERS = new HashMap<>();

	private static final Set<SignalType> SETUP = new HashSet<>();

	/**
	 * Registers a new signal handler, and returns the last one
	 *
	 * @param type The signal type to register for
	 * @param handler The handler, which will be called when the signal occurs.
	 * @return The last handler that was registered for this signal, or null if no previous handler was registered.
	 * @throws IllegalArgumentException If the signal cannot be registered, for instance, if the signal is already
	 * registered by the VM or the OS, it won't be possible to register for this signal. Also, if the signal type itself
	 * is uncatchable, this is also thrown.
	 */
	public static SignalCallback addHandler(final SignalType type, SignalCallback handler) {
		if(!type.isCatchable()) {
			throw new IllegalArgumentException(type.getSignalName() + " cannot be caught, and therefore cannot be registered.");
		}
		SignalCallback last = null;
		if(HANDLERS.containsKey(type)) {
			last = HANDLERS.get(type);
		}
		HANDLERS.put(type, handler);
		if(!SETUP.contains(type)) {
			sun.misc.Signal.handle(new sun.misc.Signal(type.getSignalName()), new sun.misc.SignalHandler() {

				@Override
				public void handle(Signal sig) {
					boolean handled = HANDLERS.get(type).handle(type);
					if(!handled) {
						if(type.getDefaultAction() == SignalType.DefaultAction.IGNORE) {
							sun.misc.SignalHandler.SIG_IGN.handle(sig);
						} else {
							sun.misc.SignalHandler.SIG_DFL.handle(sig);
						}
					}
				}
			});
			SETUP.add(type);
		}
		return last;
	}

	/**
	 * Raises the specified signal in the process.
	 *
	 * @param type
	 */
	public static void raise(SignalType type) {
		sun.misc.Signal.raise(new sun.misc.Signal(type.getSignalName()));
	}

	public static interface SignalCallback {

		/**
		 * When the signal this was registered with occurs, this method is called.
		 *
		 * @param type The type that activated this.
		 * @return If the signal should be ignored, return true. If false is returned, the default action will occur.
		 */
		boolean handle(SignalType type);
	}

}
