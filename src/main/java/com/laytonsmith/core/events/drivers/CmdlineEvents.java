
package com.laytonsmith.core.events.drivers;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.annotations.api;
import com.laytonsmith.annotations.hide;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.events.AbstractEvent;
import com.laytonsmith.core.events.BindableEvent;
import com.laytonsmith.core.events.BoundEvent;
import com.laytonsmith.core.events.Driver;
import com.laytonsmith.core.events.EventUtils;
import com.laytonsmith.core.exceptions.EventException;
import com.laytonsmith.core.exceptions.PrefilterNonMatchException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class CmdlineEvents {
	public static String docs(){
		return "Contains events related to cmdline events.";
	}
	
	
	@api
	@hide("Test event, not meant for normal use")
	public static class cmdline_test_event extends AbstractEvent {
		
		private static Thread testThread = null;

		@Override
		public void bind(BoundEvent event) {
			if(testThread == null){
				testThread = new Thread(new Runnable() {

					@Override
					public void run() {
						while(true){
							if(Thread.currentThread().isInterrupted()){
								break;
							}
							EventUtils.TriggerListener(Driver.EXTENSION, "cmdline_test_event", new BindableEvent() {

								@Override
								public Object _GetObject() {
									return new Object();
								}
							});
							try {
								Thread.sleep(5000);
							} catch (InterruptedException ex) {
								//
							}
						}
					}
				}, "cmdline-test-event-thread");
				testThread.start();
				StaticLayer.GetConvertor().addShutdownHook(new Runnable() {

					@Override
					public void run() {
						testThread.interrupt();
					}
				});
			}
		}

		

		@Override
		public String getName() {
			return "cmdline_test_event";
		}

		@Override
		public String docs() {
			return "Fires off every 5 seconds, with no other side effects.";
		}

		@Override
		public boolean matches(Map<String, Construct> prefilter, BindableEvent e) throws PrefilterNonMatchException {
			return true;
		}

		@Override
		public BindableEvent convert(CArray manualObject) {
			throw new UnsupportedOperationException("TODO: Not supported yet.");
		}

		@Override
		public Map<String, Construct> evaluate(BindableEvent e) throws EventException {
			Map<String, Construct> map = new HashMap<String, Construct>();
			map.put("time", new CInt(System.currentTimeMillis(), Target.UNKNOWN));
			return map;
		}

		@Override
		public Driver driver() {
			return Driver.EXTENSION;
		}

		@Override
		public boolean modifyEvent(String key, Construct value, BindableEvent event) {
			throw new UnsupportedOperationException("TODO: Not supported yet.");
		}

		@Override
		public Version since() {
			return CHVersion.V0_0_0;
		}
		
	}
}
