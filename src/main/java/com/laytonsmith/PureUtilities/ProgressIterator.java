package com.laytonsmith.PureUtilities;

/**
 * A progress iterator is an interface that can be passed in to any long running internal process that may want to
 * provide progress updates to the controlling code. There is a method for updating the controller when a significant
 * progress event happens.
 */
public interface ProgressIterator {

	/**
	 * Called once a progress change is detected. This is the "current" value of the progress, which in combination with
	 * the total progress can be used to determine the progress percentage (by finding current/total).
	 *
	 * @param current The current progress, always less than or equal to total, which represents the current progress of
	 * the task.
	 * @param total The total progress, which once reaches this value is "100% done"
	 */
	void progressChanged(double current, double total);
}
