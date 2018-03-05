package com.laytonsmith.core.constructs;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.core.natives.interfaces.Mixed;

/**
 * Provides a native MethodScript lock object.
 */
public final class CLock extends Construct {

	private final Object lock = new Object();
	/**
	 * This isn't an AtomicInteger, because we use the lock object to synchronize on, and all operations on count are
	 * handled within those blocks. It is volatile because multiple threads will possibly see it.
	 */
	private volatile int count = 0;

	public CLock(Target t) {
		super("", ConstructType.LOCK, t);
	}

	@Override
	public boolean isDynamic() {
		return false;
	}

	@Override
	public String val() {
		return "lock:" + lock.toString();
	}

	@Override
	public String toString() {
		return val();
	}

	/**
	 * Obtains a lock on this object, waiting until the lock is available. If no one has obtained the lock, it returns
	 * immediately.
	 */
	public void obtainLock() throws InterruptedException {
		synchronized (lock) {
			count++;
			while (count > 0) {
				lock.wait();
			}
		}
	}

	/**
	 * Releases the lock on this object, and unblocking any threads that have obtainedLock previously, if any. If
	 * nothing has called obtainLock before calling releaseLock, nothing happens, it returns immediately.
	 *
	 * @param signal
	 */
	public void releaseLock(Construct signal) {
		synchronized (lock) {
			if (count > 0) {
				count--;
			}
			lock.notifyAll();
		}
	}

//	public Construct getSignal(){
//
//	}
	@Override
	public Version since() {
		return super.since();
	}

	@Override
	public String docs() {
		return super.docs();
	}

	@Override
	public CClassType[] getSuperclasses() {
		return new CClassType[]{Mixed.TYPE};
	}

	@Override
	public CClassType[] getInterfaces() {
		return new CClassType[]{};
	}

}
