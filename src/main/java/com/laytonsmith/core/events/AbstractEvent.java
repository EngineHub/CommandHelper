package com.laytonsmith.core.events;

import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscovery;
import com.laytonsmith.PureUtilities.Common.StreamUtils;
import com.laytonsmith.abstraction.MCCommandSender;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.annotations.core;
import com.laytonsmith.annotations.hide;
import com.laytonsmith.core.Documentation;
import com.laytonsmith.core.LogLevel;
import com.laytonsmith.core.MethodScriptCompiler;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CNull;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.CommandHelperEnvironment;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.GlobalEnv;
import com.laytonsmith.core.exceptions.CRE.CREFormatException;
import com.laytonsmith.core.exceptions.CancelCommandException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.exceptions.EventException;
import com.laytonsmith.core.exceptions.FunctionReturnException;
import com.laytonsmith.core.exceptions.ProgramFlowManipulationException;
import com.laytonsmith.core.natives.interfaces.Mixed;
import com.laytonsmith.core.profiler.ProfilePoint;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * This helper class implements a few of the common functions in event, and most (all?) Events should extend this class.
 *
 */
public abstract class AbstractEvent implements Event, Comparable<Event> {

	private EventMixinInterface mixin;
//	protected EventHandlerInterface handler;
//
//	protected AbstractEvent(EventHandlerInterface handler){
//		this.handler = handler;
//	}
//

	public final void setAbstractEventMixin(EventMixinInterface mixin) {
		this.mixin = mixin;
	}

	/**
	 * If the event needs to run special code when a player binds the event, it can be done here. By default, an
	 * UnsupportedOperationException is thrown, but is caught and ignored.
	 */
	@Override
	public void bind(BoundEvent event) {

	}

	/**
	 * If the event needs to run special code when a player unbinds the event, it can be done here. By default, an
	 * UnsupportedOperationException is thrown, but is caught and ignored.
	 */
	@Override
	public void unbind(BoundEvent event) {

	}

	/**
	 * If the event needs to run special code at server startup, it can be done here. By default, nothing happens.
	 */
	@Override
	public void hook() {

	}

	/**
	 * This function is run when the actual event occurs.
	 *
	 * @param tree The compiled parse tree
	 * @param b The bound event
	 * @param env The operating environment
	 * @param activeEvent The active event being executed
	 */
	@Override
	public final void execute(ParseTree tree, BoundEvent b, Environment env, BoundEvent.ActiveEvent activeEvent) throws ConfigRuntimeException {
		preExecution(env, activeEvent);
		// Various events have a player to put into the env.
		// Do this after preExcecution() in case the particular event needs to inject the player first.
		Mixed c = activeEvent.getParsedEvent().get("player");
		if(c != null) {
			if(c instanceof CNull) {
				// This is a CNull "player", likely from an entity event, so we need to ensure player() does
				// not return a player inherited from the bind's parent environment.
				if(env.getEnv(CommandHelperEnvironment.class).GetPlayer() != null) {
					env.getEnv(CommandHelperEnvironment.class).SetCommandSender(Static.getServer().getConsole());
				}
			} else {
				MCCommandSender p = Static.getServer().getPlayer(c.val());
				if(p == null) {
					p = Static.GetInjectedPlayer(c.val());
				}
				if(p != null) {
					env.getEnv(CommandHelperEnvironment.class).SetPlayer((MCPlayer) p);
				} else {
					Static.getLogger().log(Level.WARNING, "Player missing in player event (NPC?): " + b.getEventName());
					// Set env CommandSender to prevent incorrect inherited player from being used in a player event.
					if(env.getEnv(CommandHelperEnvironment.class).GetPlayer() != null) {
						env.getEnv(CommandHelperEnvironment.class).SetCommandSender(Static.getServer().getConsole());
					}
				}
			}
		}
		ProfilePoint event = null;
		if(env.getEnv(GlobalEnv.class).GetProfiler() != null) {
			event = env.getEnv(GlobalEnv.class).GetProfiler().start("Event " + b.getEventName() + " (defined at " + b.getTarget().toString() + ")", LogLevel.ERROR);
		}
		try {
			try {
				//Get the label from the bind time environment, and put it in the current environment.
				String label = b.getEnvironment().getEnv(GlobalEnv.class).GetLabel();
				if(label == null) {
					//Set the permission to global if it's null, since that means
					//it wasn't set, and so we aren't in a secured environment anyways.
					label = Static.GLOBAL_PERMISSION;
				}
				env.getEnv(GlobalEnv.class).SetLabel(label);
				MethodScriptCompiler.execute(tree, env, null, null);
			} catch (CancelCommandException ex) {
				if(ex.getMessage() != null && !ex.getMessage().isEmpty()) {
					StreamUtils.GetSystemOut().println(ex.getMessage());
				}
			} catch (FunctionReturnException ex) {
				//We simply allow this to end the event execution
			} catch (ProgramFlowManipulationException ex) {
				ConfigRuntimeException.HandleUncaughtException(new CREFormatException("Unexpected control flow operation used.", ex.getTarget()), env);
			}
		} finally {
			if(event != null) {
				event.stop();
			}
			// Finally, among other things, we need to clean-up injected players and entities
			postExecution(env, activeEvent);
		}
	}

	/**
	 * This method is called before the event handling code is run, and provides a place for the event code itself to
	 * modify the environment or active event data.
	 *
	 * @param env The environment, at the time just before the event handler is called.
	 * @param activeEvent The event handler code.
	 */
	public void preExecution(Environment env, BoundEvent.ActiveEvent activeEvent) {

	}

	/**
	 * This method is called after the event handling code is run, and provides a place for the event code itself to
	 * modify or cleanup the environment or active event data.
	 *
	 * @param env The environment, at the time just before the event handler is called.
	 * @param activeEvent The event handler code.
	 * @throws UnsupportedOperationException If the preExecution isn't supported, this may be thrown, and it will be
	 * ignored.
	 */
	public void postExecution(Environment env, BoundEvent.ActiveEvent activeEvent) {

	}

	/**
	 * For sorting and optimizing events, we need a comparison operation. By default it is compared by looking at the
	 * event name.
	 *
	 * @param o
	 * @return
	 */
	@Override
	public int compareTo(Event o) {
		return this.getName().compareTo(o.getName());
	}

	/**
	 * Since most events are minecraft events, we return true by default.
	 *
	 * @return
	 */
	@Override
	public boolean supportsExternal() {
		return true;
	}

	/**
	 * If it is ok to by default do a simple conversion from a CArray to a Map, this method can do it for you. Likely
	 * this is not acceptable, so hard-coding the conversion will be necessary.
	 *
	 * @param manualObject
	 * @return
	 */
	public static Object DoConvert(CArray manualObject) {
		Map<String, Mixed> map = new HashMap<>();
		for(String key : manualObject.stringKeySet()) {
			map.put(key, manualObject.get(key, Target.UNKNOWN));
		}
		return map;
	}

	public Map<String, Mixed> evaluate_helper(BindableEvent e) throws EventException {
		return mixin.evaluate_helper(e);
	}

	/**
	 * By default, this function triggers the event by calling the mixin handler. If this is not the desired behavior,
	 * this method can be overridden in the actual event (if it's an external event, for instance)
	 *
	 * @param o
	 */
	@Override
	public void manualTrigger(BindableEvent o) {
		mixin.manualTrigger(o);
	}

	@Override
	public void cancel(BindableEvent o, boolean state) {
		mixin.cancel(o, state);
	}

	@Override
	public boolean isCancellable(BindableEvent o) {
		return mixin.isCancellable(o);
	}

	@Override
	public boolean isCancelled(BindableEvent o) {
		return mixin.isCancelled(o);
	}

	@Override
	public URL getSourceJar() {
		return ClassDiscovery.GetClassContainer(this.getClass());
	}

	/**
	 * Returns true if the event is annotated with @hide
	 *
	 * @return
	 */
	@Override
	public final boolean appearInDocumentation() {
		return this.getClass().getAnnotation(hide.class) != null;
	}

	private static final Class[] EMPTY_CLASS = new Class[0];

	@Override
	public Class<? extends Documentation>[] seeAlso() {
		return EMPTY_CLASS;
	}

	/**
	 * Most events should return true for this, but passive events may override this to return null.
	 *
	 * @return
	 */
	@Override
	public boolean addCounter() {
		return true;
	}

	@Override
	public final boolean isCore() {
		Class c = this.getClass();
		do {
			if(c.getAnnotation(core.class) != null) {
				return true;
			}
			c = c.getDeclaringClass();
		} while(c != null);
		return false;
	}

}
