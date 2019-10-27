package com.laytonsmith.PureUtilities;

import com.laytonsmith.PureUtilities.Common.StringUtils;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Provides utilities for dealing with the java garbage collector.
 */
public final class GCUtil {

	private GCUtil() {}

	public static void main(String[] args) {
		debug = true;
		BlockUntilGC();
	}

	private static boolean debug = false;

	/**
	 * This method calls System.gc, but it blocks until it detects that a garbage collection has run. This
	 * should only be used when absolutely necessary, for instance, with file based operations.
	 * There is one caveat, the Epsilon GC does
	 * nothing, ever. So if we run this method with that GC, we get stuck in an infinite loop until we die due
	 * to out of memory error. So if that garbage collector is the only one, then we just throw without blocking.
	 * In such a situation, since no garbage collection will ever occur anyways, whatever was trying to be
	 * accomplished by calling this method will never happen anyways.
	 */
	public static void BlockUntilGC() {
		debug(() -> "Starting");
		/*
		There may be multiple garbage collectors on a system, for instance, old generations and new generations.
		We want to ensure that at least one has run before moving on. There is one caveat, the Epsilon GC does
		nothing, ever. So if we run this method with that GC, we get stuck in an infinite loop until we die due
		to out of memory error. So if that garbage collector is the only one, then we just return.
		*/
		List<GarbageCollectorMXBean> gcs = ManagementFactory.getGarbageCollectorMXBeans();
		if(gcs.size() == 1) {
			debug(() -> gcs.get(0).getObjectName().getCanonicalName());
			if(gcs.get(0).getObjectName().getCanonicalName()
					.equals("java.lang:name=Epsilon Heap,type=GarbageCollector")) {
				throw new UnsupportedOperationException("Cannot continue, Epsilon GC is the only garbage collector.");
			}
		}
		Map<GarbageCollectorMXBean, Long> startCounts = new HashMap<>();
		for(GarbageCollectorMXBean gc : gcs) {
			debug(() -> "Found GC " + gc.getObjectName() + " with run count " + gc.getCollectionCount());
			startCounts.put(gc, gc.getCollectionCount());
		}
		debug(() -> "Starting free memory: " + h(Runtime.getRuntime().freeMemory()));
		outer: while(true) {
			System.gc();
			Iterator<GarbageCollectorMXBean> it = gcs.iterator();
			while(it.hasNext()) {
				GarbageCollectorMXBean g = it.next();
				debug(() -> "Checking " + g.getObjectName() + ". Run count: " + g.getCollectionCount()
						+ ". Current free memory: " + h(Runtime.getRuntime().freeMemory()) + ". Total GC Count "
						+ ManagementFactory.getGarbageCollectorMXBeans().size()
						+ ". Heap: " + h(Runtime.getRuntime().totalMemory()) + "/"
						+ h(Runtime.getRuntime().maxMemory()));
				if(g.getCollectionCount() > startCounts.get(g)) {
					debug(() -> "Found that " + g.getObjectName() + " has run, returning");
					break outer;
				}
			}
		}
	}

	private static interface StringProvider {
		String provide();
	}

	private static String h(long n) {
		return StringUtils.HumanReadableByteCount(n);
	}

	private static void debug(StringProvider msg) {
		if(debug) {
			System.out.println(msg.provide());
		}
	}

}
