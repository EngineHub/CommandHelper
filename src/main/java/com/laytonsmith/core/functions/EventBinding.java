package com.laytonsmith.core.functions;

import com.laytonsmith.annotations.api;
import com.laytonsmith.core.*;
import com.laytonsmith.core.arguments.ArgList;
import com.laytonsmith.core.arguments.Argument;
import com.laytonsmith.core.arguments.ArgumentBuilder;
import com.laytonsmith.core.arguments.Generic;
import com.laytonsmith.core.constructs.*;
import com.laytonsmith.core.environments.CommandHelperEnvironment;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.GlobalEnv;
import com.laytonsmith.core.events.BoundEvent;
import com.laytonsmith.core.events.BoundEvent.ActiveEvent;
import com.laytonsmith.core.events.BoundEvent.Priority;
import com.laytonsmith.core.events.Event;
import com.laytonsmith.core.events.EventUtils;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.exceptions.EventException;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;
import com.laytonsmith.core.natives.interfaces.Mixed;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Layton
 */
public class EventBinding {

	public static String docs() {
		return "This class of functions provide methods to hook deep into the server's event architecture";
	}

	@api(environments=CommandHelperEnvironment.class)
	public static class bind extends AbstractFunction {

		public String getName() {
			return "bind";
		}

		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		public String docs() {
			return "Binds some functionality to an event, so that"
					+ " when said event occurs, the event handler will fire. Returns the id of this event, so it can be unregistered"
					+ " later, if need be.";
		}
		
		public Argument returnType() {
			return new Argument("The identifier automatically assigned to this bind (or the id you assigned)", CString.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.MANUAL;
		}

		@Override
		public String argumentsManual() {
			return "string event_name, array options, array prefilter, ivar event_obj, [ivar... custom_params], code codeBlock";
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.BindException};
		}

		public boolean isRestricted() {
			return true;
		}

		@Override
		public boolean preResolveVariables() {
			return false;
		}

		public CHVersion since() {
			return CHVersion.V3_3_0;
		}

		public Boolean runAsync() {
			return false;
		}

		public Construct exec(Target t, Environment env, Construct... args) throws ConfigRuntimeException {
			return new CVoid(t);
		}

		@Override
		public Construct execs(Target t, Environment env, Script parent, ParseTree... nodes) {
			if (nodes.length < 5) {
				throw new ConfigRuntimeException("bind accepts 5 or more parameters", ExceptionType.InsufficientArgumentsException, t);
			}
			Construct name = parent.seval(nodes[0], env);
			Construct options = parent.seval(nodes[1], env);
			Construct prefilter = parent.seval(nodes[2], env);
			Construct event_obj = parent.eval(nodes[3], env);
			IVariableList custom_params = new IVariableList();
			for (int a = 0; a < nodes.length - 5; a++) {
				Construct var = parent.eval(nodes[4 + a], env);
				if (!(var instanceof IVariable)) {
					throw new ConfigRuntimeException("The custom parameters must be ivariables", ExceptionType.CastException, t);
				}
				IVariable cur = (IVariable) var;
				custom_params.set(cur, env.getEnv(GlobalEnv.class).GetVarList().get(cur, cur.getTarget()));
			}
			Environment newEnv = env;
			try {
				newEnv = env.clone();
			} catch (Exception e) {
			}
			newEnv.getEnv(GlobalEnv.class).SetVarList(custom_params);
			ParseTree tree = nodes[nodes.length - 1];

			//Check to see if our arguments are correct
			if (!(options.isNull() || options instanceof CArray)) {
				throw new ConfigRuntimeException("The options must be an array or null", ExceptionType.CastException, t);
			}
			if (!(prefilter.isNull() || prefilter instanceof CArray)) {
				throw new ConfigRuntimeException("The prefilters must be an array or null", ExceptionType.CastException, t);
			}
			if (!(event_obj instanceof IVariable)) {
				throw new ConfigRuntimeException("The event object must be an IVariable", ExceptionType.CastException, t);
			}
			CString id;
			if (options.isNull()) {
				options = null;
			}
			if (prefilter.isNull()) {
				prefilter = null;
			}
			try {
				BoundEvent be = new BoundEvent(name.val(), (CArray) options, (CArray) prefilter,
						((IVariable) event_obj).getName(), newEnv, tree, t);
				EventUtils.RegisterEvent(be);
				id = new CString(be.getId(), t);
			} catch (EventException ex) {
				throw new ConfigRuntimeException(ex.getMessage(), ExceptionType.BindException, t);
			}

			return id;
		}

		@Override
		public boolean useSpecialExec() {
			return true;
		}
	}

	@api
	public static class dump_events extends AbstractFunction {

		public String getName() {
			return "dump_events";
		}

		public Integer[] numArgs() {
			return new Integer[]{0};
		}

		public String docs() {
			return "array {} Returns an array of all the events currently registered on the server. Mostly meant for debugging,"
					+ " however it would be possible to parse this response to cherry pick events to unregister.";
		}
		
		public Argument returnType() {
			return new Argument("An array of the events currently bound", CArray.class).setGenerics(new Generic(CString.class));
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.NONE;
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{};
		}

		public boolean isRestricted() {
			return true;
		}

		public CHVersion since() {
			return CHVersion.V3_3_0;
		}

		public Boolean runAsync() {
			return null;
		}

		public Construct exec(Target t, Environment env, Construct... args) throws ConfigRuntimeException {
			return EventUtils.DumpEvents();
		}
	}

	@api(environments=CommandHelperEnvironment.class)
	public static class unbind extends AbstractFunction {

		public String getName() {
			return "unbind";
		}

		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		public String docs() {
			return "void {[eventID]} Unbinds an event, which causes it to not run anymore. If called from within an event handler, eventID is"
					+ " optional, and defaults to the current event id.";
		}
		
		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("The event ID to unbind, or the currently running event if none specified", CString.class, "eventID").setOptionalDefaultNull()
					);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.BindException};
		}

		public boolean isRestricted() {
			return true;
		}

		public CHVersion since() {
			return CHVersion.V3_3_0;
		}

		public Boolean runAsync() {
			return null;
		}

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			String id = null;
			if (args.length == 1) {
				//We are cancelling an arbitrary event
				id = args[0].val();
			} else {
				//We are cancelling this event. If we are not in an event, throw an exception
				if (environment.getEnv(CommandHelperEnvironment.class).GetEvent() == null) {
					throw new ConfigRuntimeException("No event ID specified, and not running inside an event", ExceptionType.BindException, t);
				}
				id = environment.getEnv(CommandHelperEnvironment.class).GetEvent().getBoundEvent().getId();
			}
			EventUtils.UnregisterEvent(id);
			return new CVoid(t);
		}
	}

	@api(environments=CommandHelperEnvironment.class)
	public static class cancel extends AbstractFunction {

		public String getName() {
			return "cancel";
		}

		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		public String docs() {
			return "Cancels the event (if applicable). If the event is not cancellable, or is already cancelled, nothing happens."
					+ " If called from outside an event handler, a BindException is thrown. By default, state is true, but you can"
					+ " uncancel an event (if possible) by calling cancel(false).";
		}
		
		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("If true, cancels the event, if false, uncancels it, if possible.", CBoolean.class, "state").setOptionalDefault(true)
					);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.BindException};
		}

		public boolean isRestricted() {
			return true;
		}

		public CHVersion since() {
			return CHVersion.V3_3_0;
		}

		public Boolean runAsync() {
			return null;
		}

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			ArgList list = getBuilder().parse(args, this, t);
			boolean state = list.getBoolean("state", t);

			BoundEvent.ActiveEvent original = environment.getEnv(CommandHelperEnvironment.class).GetEvent();
			if (original == null) {
				throw new ConfigRuntimeException("cancel cannot be called outside an event handler", ExceptionType.BindException, t);
			}
			if (original.getUnderlyingEvent() != null && original.isCancellable()) {
				original.setCancelled(state);
			}
			return new CVoid(t);
		}
	}

	@api(environments=CommandHelperEnvironment.class)
	public static class is_cancelled extends AbstractFunction {

		public String getName() {
			return "is_cancelled";
		}

		public Integer[] numArgs() {
			return new Integer[]{0};
		}

		public String docs() {
			return "Returns whether or not the underlying event is cancelled or not. If the event is not cancellable in the first place,"
					+ " false is returned. If called from outside an event, a BindException is thrown";
		}
		
		public Argument returnType() {
			return new Argument("True, if the the event is cancelled", CBoolean.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.NONE;
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.BindException};
		}

		public boolean isRestricted() {
			return true;
		}

		public CHVersion since() {
			return CHVersion.V3_3_0;
		}

		public Boolean runAsync() {
			return false;
		}

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			BoundEvent.ActiveEvent original = environment.getEnv(CommandHelperEnvironment.class).GetEvent();
			if (original == null) {
				throw new ConfigRuntimeException("is_cancelled cannot be called outside an event handler", ExceptionType.BindException, t);
			}
			boolean result = false;
			if (original.getUnderlyingEvent() != null && original.isCancellable()) {
				result = original.isCancelled();
			}
			return new CBoolean(result, t);
		}
	}

	@api
	public static class trigger extends AbstractFunction {

		public String getName() {
			return "trigger";
		}

		public Integer[] numArgs() {
			return new Integer[]{2, 3};
		}

		public String docs() {
			return "Manually triggers bound events. The event object passed to this function is "
					+ " sent directly as-is to the bound events. Check the documentation for each event to see what is required."
					+ " No checks will be done on the data here, but it is not recommended to fail to send all parameters required."
					+ " If serverWide is true, the event is triggered directly in the server, unless it is a CommandHelper specific"
					+ " event, in which case, serverWide is irrelevant. Defaults to false, which means that only CommandHelper code"
					+ " will receive the event.";
		}
		
		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("The event name to trigger", CString.class, "eventName"),
						new Argument("The event object", CArray.class, "eventObject"),
						new Argument("If true, it will be triggered serverwide", CBoolean.class, "serverWide").setOptionalDefault(false)
					);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException};
		}

		public boolean isRestricted() {
			return true;
		}

		public CHVersion since() {
			return CHVersion.V3_3_0;
		}

		public Boolean runAsync() {
			return false;
		}

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			ArgList list = getBuilder().parse(args, this, t);
			String eventName = list.getString("eventName", t);
			CArray eventObject = list.getEmptyArrayIfNull("eventObject");
			boolean serverWide = list.getBoolean("serverWide", t);
			EventUtils.ManualTrigger(eventName, eventObject, serverWide, t);
			return new CVoid(t);
		}
	}

	@api(environments=CommandHelperEnvironment.class)
	public static class modify_event extends AbstractFunction {

		public String getName() {
			return "modify_event";
		}

		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		public String docs() {
			return "Modifies the underlying event object, if applicable."
					+ " The documentation for each event will explain what parameters can be modified,"
					+ " and what their expected values are. ---- If an invalid parameter name is passed in,"
					+ " nothing will happen. If this function is called from outside an event"
					+ " handler, a BindException is thrown. Note that modifying the underlying event"
					+ " will NOT update the event object passed in to the event handler. The function returns"
					+ " whether or not the parameter was updated successfully. It could fail to modify the"
					+ " event if a higher priority handler has locked this parameter, or if updating the underlying"
					+ " event failed. If throwOnFailure is true, instead of returning false, it will throw"
					+ " a BindException. The default for throwOnFailure is false. If a monitor level handler"
					+ " even attempts to modify an event, an exception will be thrown.";
		}
		
		public Argument returnType() {
			return new Argument("True, if the value was modified as intended. If throwOnFailure is true, this will never return false", CBoolean.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("The name of the parameter to modify", CString.class, "parameter"),
						new Argument("The value to set it to", Mixed.class, "value"),
						new Argument("If true, an exception will be thrown, instead of returning false, if the parameter is not able to be set", CBoolean.class, "throwOnFailure").setOptionalDefault(false)
					);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException, ExceptionType.BindException};
		}

		public boolean isRestricted() {
			return true;
		}

		public CHVersion since() {
			return CHVersion.V3_3_0;
		}

		public Boolean runAsync() {
			return false;
		}

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			ArgList list = getBuilder().parse(args, this, t);
			String parameter = list.getString("parameter", t);
			Mixed value = list.get("value");
			boolean throwOnFailure = list.getBoolean("throwOnFailure", t);
			if (environment.getEnv(CommandHelperEnvironment.class).GetEvent() == null) {
				throw new ConfigRuntimeException(this.getName() + " must be called from within an event handler", ExceptionType.BindException, t);
			}
			Event e = environment.getEnv(CommandHelperEnvironment.class).GetEvent().getEventDriver();
			if (environment.getEnv(CommandHelperEnvironment.class).GetEvent().getBoundEvent().getPriority().equals(Priority.MONITOR)) {
				throw new ConfigRuntimeException("Monitor level handlers may not modify an event!", ExceptionType.BindException, t);
			}
			ActiveEvent active = environment.getEnv(CommandHelperEnvironment.class).GetEvent();
			boolean success = false;
			if (!active.isLocked(parameter)) {
				try {
					success = e.modifyEvent(parameter, value, environment.getEnv(CommandHelperEnvironment.class).GetEvent().getUnderlyingEvent(), t);
				} catch (ConfigRuntimeException ex) {
					ex.setFile(t.file());
					ex.setLineNum(t.line());
					ex.setColumn(t.col());
					throw ex;
				}
			} else {
				success = false;
			}
			if (throwOnFailure && !success) {
				throw new ConfigRuntimeException("Event parameter is already locked!", ExceptionType.BindException, t);
			}
			return new CBoolean(success, t);
		}
	}

	@api(environments=CommandHelperEnvironment.class)
	public static class lock extends AbstractFunction {

		public String getName() {
			return "lock";
		}

		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		public String docs() {
			return "Locks the specified event parameter(s), or all of them,"
					+ " if specified with no arguments. Locked parameters become read only for lower priority event handlers.";
		}
		
		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("The params to lock", CArray.class, "params").setVarargs().setGenerics(new Generic(CString.class))
					);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.BindException};
		}

		public boolean isRestricted() {
			return true;
		}

		public Boolean runAsync() {
			return false;
		}

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			if (environment.getEnv(CommandHelperEnvironment.class).GetEvent() == null) {
				throw new ConfigRuntimeException("lock must be called from within an event handler", ExceptionType.BindException, t);
			}

			BoundEvent.ActiveEvent e = environment.getEnv(CommandHelperEnvironment.class).GetEvent();
			Priority p = e.getBoundEvent().getPriority();
			List<String> params = new ArrayList<String>();
			if (args.length == 0) {
				e.lock(null);
			} else {
				if (args[0] instanceof CArray) {
					CArray ca = (CArray) args[1];
					for (int i = 0; i < ca.size(); i++) {
						params.add(ca.get(i, t).val());
					}
				} else {
					for (int i = 0; i < args.length; i++) {
						params.add(args[i].val());
					}
				}
			}
			for (String param : params) {
				e.lock(param);
			}
			return new CVoid(t);
		}

		public CHVersion since() {
			return CHVersion.V3_3_0;
		}
	}

	@api(environments=CommandHelperEnvironment.class)
	public static class is_locked extends AbstractFunction {

		public String getName() {
			return "is_locked";
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		public String docs() {
			return "Returns whether or not a call to modify_event() would fail, based on"
					+ " the parameter being locked by a higher priority handler. If this returns false, it"
					+ " is still not a guarantee that the event would be successfully modified, just that"
					+ " it isn't locked.";
		}
		
		public Argument returnType() {
			return new Argument("True, if the parameter is locked", CBoolean.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("The parameter to check", CString.class, "parameter")
					);
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.BindException};
		}

		public boolean isRestricted() {
			return true;
		}

		public Boolean runAsync() {
			return false;
		}

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			if (environment.getEnv(CommandHelperEnvironment.class).GetEvent() == null) {
				throw new ConfigRuntimeException("is_locked may only be called from inside an event handler", ExceptionType.BindException, t);
			}
			boolean locked = environment.getEnv(CommandHelperEnvironment.class).GetEvent().isLocked(args[0].val());
			return new CBoolean(locked, t);
		}

		public CHVersion since() {
			return CHVersion.V3_3_0;
		}
	}

	@api(environments=CommandHelperEnvironment.class)
	public static class consume extends AbstractFunction {

		public String getName() {
			return "consume";
		}

		public Integer[] numArgs() {
			return new Integer[]{0};
		}

		public String docs() {
			return "Consumes an event, so that lower priority handlers don't even"
					+ " recieve the event. Monitor level handlers will still recieve it, however,"
					+ " and they can check to see if the event was consumed.";
		}
		
		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.NONE;
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.BindException};
		}

		public boolean isRestricted() {
			return true;
		}

		public Boolean runAsync() {
			return false;
		}

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			if (environment.getEnv(CommandHelperEnvironment.class).GetEvent() == null) {
				throw new ConfigRuntimeException("consume may only be called from an event handler!", ExceptionType.BindException, t);
			}
			environment.getEnv(CommandHelperEnvironment.class).GetEvent().consume();
			return new CVoid(t);
		}

		public CHVersion since() {
			return CHVersion.V3_3_0;
		}
	}

	@api(environments=CommandHelperEnvironment.class)
	public static class is_consumed extends AbstractFunction {

		public String getName() {
			return "is_consumed";
		}

		public Integer[] numArgs() {
			return new Integer[]{0};
		}

		public String docs() {
			return "Returns whether or not this event has been consumed. Usually only useful"
					+ " for Monitor level handlers, it could also be used for highly robust code,"
					+ " as an equal priority handler could have consumed the event, but this handler"
					+ " would still recieve it.";
		}
		
		public Argument returnType() {
			return new Argument("True, if this event is consumed", CBoolean.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.NONE;
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.BindException};
		}

		public boolean isRestricted() {
			return true;
		}

		public Boolean runAsync() {
			return false;
		}

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			if (environment.getEnv(CommandHelperEnvironment.class).GetEvent() == null) {
				throw new ConfigRuntimeException("is_consumed must be called from within an event handler", ExceptionType.BindException, t);
			}
			return new CBoolean(environment.getEnv(CommandHelperEnvironment.class).GetEvent().isConsumed(), t);
		}

		public CHVersion since() {
			return CHVersion.V3_3_0;
		}
	}

//    @api public static class when_triggered extends AbstractFunction{
//        
//    }
//    @api public static class when_cancelled extends AbstractFunction{
//        
//    }
	@api(environments=CommandHelperEnvironment.class)
	public static class event_meta extends AbstractFunction {

		public String getName() {
			return "event_meta";
		}

		public Integer[] numArgs() {
			return new Integer[]{0};
		}

		public String docs() {
			return "Returns meta information about the activity in regards to this event. This"
					+ " is meant as a debug tool.";
		}
		
		public Argument returnType() {
			return new Argument("Meta information about the event, in no particular format", CArray.class).setGenerics(new Generic(CString.class));
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.NONE;
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.BindException};
		}

		public boolean isRestricted() {
			return true;
		}

		public Boolean runAsync() {
			return false;
		}

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			if (environment.getEnv(CommandHelperEnvironment.class).GetEvent() == null) {
				throw new ConfigRuntimeException("event_meta must be called from within an event handler!", ExceptionType.BindException, t);
			}
			CArray history = new CArray(t);
			for (String entry : environment.getEnv(CommandHelperEnvironment.class).GetEvent().getHistory()) {
				history.push(new CString(entry, t));
			}
			return history;
		}

		public CHVersion since() {
			return CHVersion.V3_3_0;
		}
	}
}
