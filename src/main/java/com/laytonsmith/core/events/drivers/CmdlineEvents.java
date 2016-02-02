
package com.laytonsmith.core.events.drivers;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.annotations.api;
import com.laytonsmith.annotations.core;
import com.laytonsmith.annotations.hide;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.events.AbstractEvent;
import com.laytonsmith.core.events.BindableEvent;
import com.laytonsmith.core.events.BoundEvent;
import com.laytonsmith.core.events.CancellableEvent;
import com.laytonsmith.core.events.Driver;
import com.laytonsmith.core.events.EventUtils;
import com.laytonsmith.core.exceptions.EventException;
import com.laytonsmith.core.exceptions.PrefilterNonMatchException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
@core
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
		public BindableEvent convert(CArray manualObject, Target t) {
			return new BindableEvent() {

				@Override
				public Object _GetObject() {
					return new Object();
				}
			};
		}

		@Override
		public Map<String, Construct> evaluate(BindableEvent e) throws EventException {
			Map<String, Construct> map = new HashMap<>();
			map.put("time", new CInt(System.currentTimeMillis(), Target.UNKNOWN));
			return map;
		}

		@Override
		public Driver driver() {
			return Driver.EXTENSION;
		}

		@Override
		public boolean modifyEvent(String key, Construct value, BindableEvent event) {
			return false;
		}

		@Override
		public Version since() {
			return CHVersion.V0_0_0;
		}

	}

	@api
	public static class cmdline_prompt_input extends AbstractEvent {

		@Override
		public String getName() {
			return "cmdline_prompt_input";
		}

		@Override
		public String docs() {
			return "{}"
					+ " Fired when a command is issued from the interactive prompt. If the event is not"
					+ " cancelled, the interpreter will handle it as normal. Otherwise, the event can"
					+ " be cancelled, and custom handling can be triggered."
					+ " {command: The command that was triggered}"
					+ " {}"
					+ " {}";
		}

		@Override
		public boolean matches(Map<String, Construct> prefilter, BindableEvent e) throws PrefilterNonMatchException {
			return true;
		}

		@Override
		public BindableEvent convert(CArray manualObject, Target t) {
			CmdlinePromptInput cpi = new CmdlinePromptInput(manualObject.get("command", t).val());
			return cpi;
		}

		@Override
		public Map<String, Construct> evaluate(BindableEvent e) throws EventException {
			CmdlinePromptInput cpi = (CmdlinePromptInput)e;
			Map<String, Construct> map = new HashMap<>();
			map.put("command", new CString(cpi.getCommand(), Target.UNKNOWN));
			return map;
		}

		@Override
		public Driver driver() {
			return Driver.CMDLINE_PROMPT_INPUT;
		}

		@Override
		public boolean modifyEvent(String key, Construct value, BindableEvent event) {
			return false;
		}

		@Override
		public Version since() {
			return CHVersion.V3_3_1;
		}

		@Override
		public boolean addCounter() {
			return false;
		}

		public static class CmdlinePromptInput implements BindableEvent, CancellableEvent {

			private boolean isCancelled = false;
			private final String command;
			public CmdlinePromptInput(String command){
				this.command = command;
			}

			@Override
			public Object _GetObject() {
				throw new UnsupportedOperationException("TODO: Not supported yet.");
			}

			public String getCommand(){
				return command;
			}

			@Override
			public void cancel(boolean state) {
				isCancelled = state;
			}

			public boolean isCancelled(){
				return isCancelled;
			}



		}

	}

	@api
	public static class shutdown extends AbstractEvent {

		@Override
		public String getName() {
			return "shutdown";
		}

		@Override
		public String docs() {
			return "{}"
					+ " Fired the process is being shut down. This is not guaranteed to run, because some"
					+ " cases may cause the process to die unexpectedly. Code within the event handler should"
					+ " take as little time as possible, as the process may force an exit if the handler"
					+ " takes too long."
					+ " {}"
					+ " {}"
					+ " {}";
		}

		@Override
		public boolean matches(Map<String, Construct> prefilter, BindableEvent e) throws PrefilterNonMatchException {
			return true;
		}

		@Override
		public BindableEvent convert(CArray manualObject, Target t) {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public Map<String, Construct> evaluate(BindableEvent e) throws EventException {
			return Collections.EMPTY_MAP;
		}

		@Override
		public Driver driver() {
			return Driver.SHUTDOWN;
		}

		@Override
		public boolean modifyEvent(String key, Construct value, BindableEvent event) {
			return false;
		}

		@Override
		public Version since() {
			return CHVersion.V3_3_1;
		}

		@Override
		public boolean addCounter() {
			return false;
		}

	}
}
