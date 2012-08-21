/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.core.profiler;

/**
 *
 * @author Layton
 */
final class GarbageCollectionDetector {

	private Profiler profiler;
	public GarbageCollectionDetector(Profiler profiler) {
		this.profiler = profiler;
	}
	
	@Override
	protected void finalize() throws Throwable {		
		if (profiler.queuedProfilePoints > 0) {
			for (ProfilePoint p : profiler.operations.keySet()) {
				p.garbageCollectorRun();
			}
		}
		new GarbageCollectionDetector(profiler);
	}
    
}
