package com.laytonsmith.core;

/**
 * A Finalizable object is one that has some cleanup actions associated with it, beyond just the memory constraints.
 * When the object is freed (either through explicit calls to free() by user code, or reload), this method will be
 * called.
 */
public interface Finalizable {
	/**
	 * Called when the object should clean up external resources. It is guaranteed that when this is called, user code
	 * will no longer have reference to this object.
	 * @throws Exception
	 */
	void msFinalize();
}
