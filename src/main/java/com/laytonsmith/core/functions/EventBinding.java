package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.annotations.api;
import com.laytonsmith.annotations.core;
import com.laytonsmith.annotations.hide;
import com.laytonsmith.core.ArgumentValidation;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.Optimizable;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.Script;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.compiler.BranchStatement;
import com.laytonsmith.core.compiler.CompilerEnvironment;
import com.laytonsmith.core.compiler.CompilerWarning;
import com.laytonsmith.core.compiler.FileOptions;
import com.laytonsmith.core.compiler.SelfStatement;
import com.laytonsmith.core.compiler.VariableScope;
import com.laytonsmith.core.compiler.analysis.Namespace;
import com.laytonsmith.core.compiler.analysis.ReturnableDeclaration;
import com.laytonsmith.core.compiler.analysis.Scope;
import com.laytonsmith.core.compiler.analysis.StaticAnalysis;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CBoolean;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.CFunction;
import com.laytonsmith.core.constructs.CNull;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.CVoid;
import com.laytonsmith.core.constructs.IVariable;
import com.laytonsmith.core.constructs.IVariableList;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.GlobalEnv;
import com.laytonsmith.core.environments.StaticRuntimeEnv;
import com.laytonsmith.core.events.BindableEvent;
import com.laytonsmith.core.events.BoundEvent;
import com.laytonsmith.core.events.BoundEvent.ActiveEvent;
import com.laytonsmith.core.events.BoundEvent.Priority;
import com.laytonsmith.core.events.Event;
import com.laytonsmith.core.events.EventList;
import com.laytonsmith.core.events.EventUtils;
import com.laytonsmith.core.events.prefilters.Prefilter;
import com.laytonsmith.core.events.prefilters.PrefilterStatus;
import com.laytonsmith.core.exceptions.CRE.CREBindException;
import com.laytonsmith.core.exceptions.CRE.CRECastException;
import com.laytonsmith.core.exceptions.CRE.CREInsufficientArgumentsException;
import com.laytonsmith.core.exceptions.CRE.CREThrowable;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigCompileGroupException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.exceptions.EventException;
import com.laytonsmith.core.natives.interfaces.Mixed;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.eclipse.lsp4j.SymbolKind;

/**
 *
 */
@core
public class EventBinding {

	public static String docs() {
		return "This class of functions provide methods to hook deep into the server's event architecture";
	}

	private static final AtomicInteger BIND_COUNTER = new AtomicInteger(0);

	@api
	@SelfStatement
	public static class bind extends AbstractFunction implements Optimizable, BranchStatement, VariableScope, DocumentSymbolProvider {

		@Override
		public String getName() {
			return "bind";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		@Override
		public String docs() {
			return "string {event_name, options, prefilter, event_obj, [custom_params], &lt;code&gt;} Binds some functionality to an event, so that"
					+ " when said event occurs, the event handler will fire. Returns the id of this event, so it can be unregistered"
					+ " later, if need be. See more on the page detailing [[Events]]. The options array can contain"
					+ " \"id\" and \"priority\", where priority is one of: "
					+ StringUtils.Join(Priority.values(), ", ", ", or ") + ". The prefilters vary from event to event.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREBindException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public boolean preResolveVariables() {
			return false;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_0;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			return CVoid.VOID;
		}

		@Override
		public Mixed execs(Target t, Environment env, Script parent, ParseTree... nodes) {
			if(nodes.length < 5) {
				throw new CREInsufficientArgumentsException("bind accepts 5 or more parameters", t);
			}
			Mixed name = parent.seval(nodes[0], env);
			Mixed options = parent.seval(nodes[1], env);
			Mixed prefilter = parent.seval(nodes[2], env);
			Mixed event_obj = parent.eval(nodes[3], env);
			IVariableList custom_params = new IVariableList(env.getEnv(GlobalEnv.class).GetVarList());
			for(int a = 0; a < nodes.length - 5; a++) {
				Mixed var = parent.eval(nodes[4 + a], env);
				if(!(var instanceof IVariable)) {
					throw new CRECastException("The custom parameters must be ivariables", t);
				}
				IVariable cur = (IVariable) var;
				custom_params.set(env.getEnv(GlobalEnv.class).GetVarList().get(cur.getVariableName(),
						cur.getTarget(), env));
			}
			Environment newEnv = env;
			try {
				newEnv = env.clone();
			} catch (Exception e) {
			}
			// Set the permission to global if it's null, since that means
			// it wasn't set, and so we aren't in a secured environment anyway.
			if(newEnv.getEnv(GlobalEnv.class).GetLabel() == null) {
				newEnv.getEnv(GlobalEnv.class).SetLabel(Static.GLOBAL_PERMISSION);
			}
			newEnv.getEnv(GlobalEnv.class).SetVarList(custom_params);
			ParseTree tree = nodes[nodes.length - 1];

			//Check to see if our arguments are correct
			if(!(options instanceof CNull || options.isInstanceOf(CArray.TYPE))) {
				throw new CRECastException("The options must be an array or null", t);
			}
			if(!(prefilter instanceof CNull || prefilter.isInstanceOf(CArray.TYPE))) {
				throw new CRECastException("The prefilters must be an array or null", t);
			}
			if(!(event_obj instanceof IVariable)) {
				throw new CRECastException("The event object must be an IVariable", t);
			}
			CString id;
			if(options instanceof CNull) {
				options = null;
			}
			if(prefilter instanceof CNull) {
				prefilter = null;
			}
			Event event;
			try {
				BoundEvent be = new BoundEvent(name.val(), (CArray) options, (CArray) prefilter,
						((IVariable) event_obj).getVariableName(), newEnv, tree, t);
				EventUtils.RegisterEvent(be);
				id = new CString(be.getId(), t);
				event = be.getEventDriver();
			} catch (EventException ex) {
				throw new CREBindException(ex.getMessage(), t);
			}

			//Set up our bind counter, but only if the event is supposed to be added to the counter
			if(event.addCounter()) {
				synchronized(BIND_COUNTER) {
					if(BIND_COUNTER.get() == 0) {
						env.getEnv(StaticRuntimeEnv.class).GetDaemonManager().activateThread(null);
						StaticLayer.GetConvertor().addShutdownHook(() -> {
							synchronized(BIND_COUNTER) {
								BIND_COUNTER.set(0);
							}
						});
					}
					BIND_COUNTER.incrementAndGet();
				}
			}
			return id;
		}

		@Override
		public CClassType typecheck(StaticAnalysis analysis,
				ParseTree ast, Environment env, Set<ConfigCompileException> exceptions) {

			// Get and check the types of the function's arguments.
			List<ParseTree> children = ast.getChildren();
			List<CClassType> argTypes = new ArrayList<>(children.size());
			List<Target> argTargets = new ArrayList<>(children.size());
			String eventName = (children.isEmpty() || !children.get(0).isConst()
					? null : children.get(0).getData().val());
			for(int i = 0; i < children.size(); i++) {
				ParseTree child = children.get(i);

				// Perform prefilter validation for known events.
				if(i == 2 && eventName != null) {
					try {
						argTypes.add(this.typecheckPrefilterParseTree(
								analysis, eventName, child, env, ast.getFileOptions(), exceptions));
					} catch(ConfigCompileException ex) {
						exceptions.add(ex);
					} catch(ConfigCompileGroupException ex) {
						exceptions.addAll(ex.getList());
					}
					argTargets.add(child.getTarget());
				} else {

					// Typecheck child node.
					argTypes.add(analysis.typecheck(child, env, exceptions));
					argTargets.add(child.getTarget());
				}
			}

			// Return the return type of this function.
			return this.getReturnType(ast.getTarget(), argTypes, argTargets, env, exceptions);
		}

		private CClassType typecheckPrefilterParseTree(
				StaticAnalysis analysis, String eventName, ParseTree prefilterParseTree,
				Environment env, FileOptions fileOptions, Set<ConfigCompileException> exceptions)
				throws ConfigCompileException, ConfigCompileGroupException {

			// Return if the prefilter parse tree is not a hard-coded "array(...)" node.
			if(!(prefilterParseTree.getData() instanceof CFunction)
					|| (!prefilterParseTree.getData().val().equals(DataHandling.array.NAME)
					&& !prefilterParseTree.getData().val().equals(DataHandling.associative_array.NAME))) {
				return analysis.typecheck(prefilterParseTree, env, exceptions);
			}

			// Return if the event name is invalid.
			Event ev = EventList.getEvent(eventName);
			if(ev == null) {
				return analysis.typecheck(prefilterParseTree, env, exceptions);
			}

			// Return if there are no prefilters defined for this event.
			Map<String, Prefilter<? extends BindableEvent>> prefilters = ev.getPrefilters();
			if(prefilters == null) {
				return analysis.typecheck(prefilterParseTree, env, exceptions);
			}

			// Validate prefilters.
			Map<Prefilter<? extends BindableEvent>, ParseTree> fullPrefilters = new HashMap<>();
			for(ParseTree node : prefilterParseTree.getChildren()) {
				if(node.getData() instanceof CFunction && node.getData().val().equals(Compiler.centry.NAME)) {
					List<ParseTree> children = node.getChildren();
					String prefilterKey = children.get(0).getData().val();
					ParseTree prefilterEntryValParseTree = children.get(1);
					if(prefilters.containsKey(prefilterKey)) {
						Prefilter<? extends BindableEvent> prefilter = prefilters.get(prefilterKey);
						prefilter.getMatcher().typecheck(analysis, prefilterEntryValParseTree, env, exceptions);
						fullPrefilters.put(prefilter, prefilterEntryValParseTree);
						if(prefilter.getStatus().contains(PrefilterStatus.REMOVED)) {
							exceptions.add(new ConfigCompileException("This prefilter has been removed,"
									+ " and is no longer available.", prefilterEntryValParseTree.getTarget()));
						} else if(prefilter.getStatus().contains(PrefilterStatus.DEPRECATED)) {
							env.getEnv(CompilerEnvironment.class).addCompilerWarning(fileOptions,
							new CompilerWarning("This prefilter is deprecated, and will be removed in a future"
									+ " release. Please see the documentation"
									+ " for this event for more details on the replacement options available.",
									children.get(0).getTarget(), null));
						}
					} else {
						env.getEnv(CompilerEnvironment.class).addCompilerWarning(fileOptions,
									new CompilerWarning("Unexpected prefilter, this will be ignored."
											+ " (This warning will eventually become a compile error.)",
											children.get(0).getTarget(), null));
					}
				} else {

					// Non-centry node, type check and continue.
					analysis.typecheck(node, env, exceptions);
				}
			}

			ev.validatePrefilters(fullPrefilters, env);

			// All array entries have been typechecked, so we can just return the array type here.
			return CArray.TYPE;
		}

		@Override
		public Scope linkScope(StaticAnalysis analysis, Scope parentScope,
				ParseTree ast, Environment env, Set<ConfigCompileException> exceptions) {

			// Fully ignore the bind() if it will generate an exception later anyways.
			if(ast.numberOfChildren() < 5) {
				return parentScope;
			}

			ParseTree eventName = ast.getChildAt(0);
			ParseTree options = ast.getChildAt(1);
			ParseTree prefilter = ast.getChildAt(2);
			ParseTree eventObj = ast.getChildAt(3);
			ParseTree code = ast.getChildAt(ast.numberOfChildren() - 1);

			// Order: eventName -> options -> prefilter -> eventObj -> (params)* -> code.
			Scope eventNameScope = analysis.linkScope(parentScope, eventName, env, exceptions);
			Scope optionsScope = analysis.linkScope(eventNameScope, options, env, exceptions);
			Scope prefilterScope = analysis.linkScope(optionsScope, prefilter, env, exceptions);
			Scope paramScope = analysis.createNewScope();
			paramScope.addSpecificParent(prefilterScope, Namespace.PROCEDURE);
			Scope[] eventObjScopes = analysis.linkParamScope(paramScope, prefilterScope, eventObj, env, exceptions);
			paramScope = eventObjScopes[0];
			Scope valScope = eventObjScopes[1];
			for(int paramInd = 4; paramInd < ast.numberOfChildren() - 1; paramInd++) {
				ParseTree param = ast.getChildAt(paramInd);
				Scope[] scopes = analysis.linkParamScope(paramScope, valScope, param, env, exceptions);
				paramScope = scopes[0];
				valScope = scopes[1];
			}
			analysis.linkScope(paramScope, code, env, exceptions);

			// Create returnable declaration in the inner root scope.
			paramScope.addDeclaration(new ReturnableDeclaration(CVoid.TYPE, ast.getNodeModifiers(), ast.getTarget()));

			// Allow code after bind() to access declarations in assigned values, but not parameters themselves.
			return valScope;
		}

		@Override
		public boolean useSpecialExec() {
			return true;
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(OptimizationOption.OPTIMIZE_DYNAMIC, OptimizationOption.CUSTOM_LINK);
		}

		@Override
		public ParseTree optimizeDynamic(Target t, Environment env,
				Set<Class<? extends Environment.EnvironmentImpl>> envs,
				List<ParseTree> topChildren, FileOptions fileOptions)
				throws ConfigCompileException, ConfigRuntimeException, ConfigCompileGroupException {
			if(topChildren.size() < 5) {
				throw new CREInsufficientArgumentsException("bind accepts 5 or more parameters", t);
			}
			if(!topChildren.get(0).isConst()) {
				// Const event name allows for better compilation checks of event type, once objects are added.
				// This will throw an exception when linking, so let's give a more specific message here
				throw new ConfigCompileException("Event names must be constant in bind().", t);
			}

			Set<ConfigCompileException> exceptions = new HashSet<>();
			// Validate options
			ParseTree child = topChildren.get(1);
			if(child.getData() instanceof CFunction && (child.getData().val().equals(DataHandling.array.NAME)
					|| child.getData().val().equals(DataHandling.associative_array.NAME))) {
				for(ParseTree node : child.getChildren()) {
					if(node.getData() instanceof CFunction && node.getData().val().equals(Compiler.centry.NAME)) {
						List<ParseTree> children = node.getChildren();
						if(children.get(0).getData().val().equals("id")
								&& children.get(1).getData().isInstanceOf(CString.TYPE)) {
							if(children.get(1).getData().val().matches(".*?:\\d*?")) {
								exceptions.add(new ConfigCompileException(children.get(1).getData().val()
											+ " is not a valid event identifier."
											+ " It cannot match the regex \".*?:\\d*?\".", children.get(1).getTarget()));
							}
						} else if(children.get(0).getData().val().equals("priority")
								&& children.get(1).getData().isInstanceOf(CString.TYPE)) {
							try {
								BoundEvent.Priority.valueOf(children.get(1).getData().val().toUpperCase());
								try {
									BoundEvent.Priority.valueOf(children.get(1).getData().val());
								} catch (IllegalArgumentException ex) {
									env.getEnv(CompilerEnvironment.class).addCompilerWarning(fileOptions,
											new CompilerWarning("This field will become case sensitive in the future,"
													+ " so should be capitalised to match the actual enum values.",
													children.get(1).getTarget(), null));
								}
							} catch (IllegalArgumentException ex) {
								exceptions.add(new ConfigCompileException(children.get(1).getData().val()
											+ " is not a valid enum in ms.lang.Priority",
												children.get(1).getTarget()));
							}
						} else {
							List<String> validProps = Arrays.asList("id", "priority");
							String prop = children.get(0).getData().val();
							if(!validProps.contains(prop)) {
								env.getEnv(CompilerEnvironment.class).addCompilerWarning(fileOptions,
											new CompilerWarning("Unexpected entry, this will be ignored.",
													children.get(0).getTarget(), null));
							}
						}
					}
				}
			}
			if(!exceptions.isEmpty()) {
				throw new ConfigCompileGroupException(exceptions);
			}
			return null;
		}

		@Override
		public void link(Target t, List<ParseTree> children) throws ConfigCompileException {
			if(children.size() < 5) {
				throw new CREInsufficientArgumentsException("bind accepts 5 or more parameters", t);
			}
			String name = children.get(0).getData().val();
			try {
				EventUtils.verifyEventName(name);
			} catch (IllegalArgumentException ex) {
				throw new ConfigCompileException(ex.getMessage(), children.get(0).getTarget());
			}
		}

		@Override
		public List<Boolean> isBranch(List<ParseTree> children) {
			List<Boolean> ret = new ArrayList<>(children.size());
			for(int i = 0; i < children.size() - 1; i++) {
				ret.add(false);
			}
			ret.add(true);
			return ret;
		}

		@Override
		public List<Boolean> isScope(List<ParseTree> children) {
			return isBranch(children);
		}

		@Override
		public String symbolDisplayName(List<ParseTree> children) {
			return "bind " + children.get(0).getData().val() + ":" + children.get(0).getTarget().line();
		}

		@Override
		public SymbolKind getSymbolKind() {
			return SymbolKind.Event;
		}

	}

	@api
	public static class dump_events extends AbstractFunction {

		@Override
		public String getName() {
			return "dump_events";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0};
		}

		@Override
		public String docs() {
			return "array {} Returns an array of all the events currently registered on the server. Mostly meant for debugging,"
					+ " however it would be possible to parse this response to cherry pick events to unregister.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_0;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			return EventUtils.DumpEvents();
		}
	}

	@api
	public static class unbind extends AbstractFunction {

		@Override
		public String getName() {
			return "unbind";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		@Override
		public String docs() {
			return "void {[eventID]} Unbinds an event, which causes it to not run anymore. If called from within an event handler, eventID is"
					+ " optional, and defaults to the current event id.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREBindException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_0;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			String id;
			BoundEvent be;
			if(args.length == 1) {
				//We are cancelling an arbitrary event
				id = args[0].val();
				be = EventUtils.GetEventById(id);
			} else {
				//We are cancelling this event. If we are not in an event, throw an exception
				if(environment.getEnv(GlobalEnv.class).GetEvent() == null) {
					throw new CREBindException("No event ID specified, and not running inside an event", t);
				}
				be = environment.getEnv(GlobalEnv.class).GetEvent().getBoundEvent();
				id = be.getId();
			}
			Event event = null;
			if(be != null) {
				event = be.getEventDriver();
			}
			EventUtils.UnregisterEvent(id);
			//Only remove the counter if it had been added in the first place.
			if(event != null && event.addCounter()) {
				synchronized(BIND_COUNTER) {
					BIND_COUNTER.decrementAndGet();
					if(BIND_COUNTER.get() == 0) {
						environment.getEnv(StaticRuntimeEnv.class).GetDaemonManager().deactivateThread(null);
					}
				}
			}
			return CVoid.VOID;
		}
	}

	@api
	public static class cancel extends AbstractFunction {

		@Override
		public String getName() {
			return "cancel";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		@Override
		public String docs() {
			return "void {[state]} Cancels the event (if applicable). If the event is not cancellable, or is already set to the specified"
					+ " cancelled state, nothing happens."
					+ " If called from outside an event handler, a BindException is thrown. By default, state is true, but you can"
					+ " uncancel an event (if possible) by calling cancel(false).";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREBindException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_0;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			boolean cancelled = true;
			if(args.length == 1) {
				cancelled = ArgumentValidation.getBoolean(args[0], t);
			}

			BoundEvent.ActiveEvent original = environment.getEnv(GlobalEnv.class).GetEvent();
			if(original == null) {
				throw new CREBindException("cancel cannot be called outside an event handler", t);
			}
			if(original.getUnderlyingEvent() != null && original.isCancellable()) {
				original.setCancelled(cancelled);
			}
			return CVoid.VOID;
		}
	}

	@api
	public static class is_cancelled extends AbstractFunction {

		@Override
		public String getName() {
			return "is_cancelled";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0};
		}

		@Override
		public String docs() {
			return "boolean {} Returns whether or not the underlying event is cancelled or not. If the event is not cancellable in the first place,"
					+ " false is returned. If called from outside an event, a BindException is thrown";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREBindException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_0;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			BoundEvent.ActiveEvent original = environment.getEnv(GlobalEnv.class).GetEvent();
			if(original == null) {
				throw new CREBindException("is_cancelled cannot be called outside an event handler", t);
			}
			boolean result = false;
			if(original.getUnderlyingEvent() != null && original.isCancellable()) {
				result = original.isCancelled();
			}
			return CBoolean.get(result);
		}
	}

	@api
	@hide("At the time this function was hidden, it was completely broken. Before unhiding this function, implement a"
			+ " working version, reviewing at least the following points: Should all events implement support for this?"
			+ " Should usage of this function change to support getting event results (cancelled, modified)?")
	public static class trigger extends AbstractFunction {

		@Override
		public String getName() {
			return "trigger";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2, 3};
		}

		@Override
		public String docs() {
			return "void {eventName, eventObject, [serverWide]} Manually triggers bound events. The event object passed"
					+ " to this function is sent directly as-is to the bound events. Check the documentation for each"
					+ " event to see what is required. No checks will be done on the data here, but it is not"
					+ " recommended to fail to send all parameters required."
					+ " If serverWide is true, the event is triggered directly in the server, unless it is a"
					+ " CommandHelper specific event, in which case, serverWide is irrelevant."
					+ " Defaults to false, which means that only CommandHelper code will receive the event."
					+ " Throws a CastException when eventObject is not an array and not null."
					+ " Throws a BindException when " + getName() + "() is not yet supported by the given event.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CREBindException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_0;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			CArray obj = null;
			if(args[1] instanceof CNull) {
				obj = new CArray(t);
			} else if(args[1].isInstanceOf(CArray.TYPE)) {
				obj = (CArray) args[1];
			} else {
				throw new CRECastException("The eventObject must be null, or an array", t);
			}
			boolean serverWide = false;
			if(args.length == 3) {
				serverWide = ArgumentValidation.getBoolean(args[2], t);
			}
			EventUtils.ManualTrigger(args[0].val(), obj, t, serverWide);
			return CVoid.VOID;
		}
	}

	@api
	public static class modify_event extends AbstractFunction {

		public static final String NAME = "modify_event";

		@Override
		public String getName() {
			return NAME;
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "boolean {parameter, value, [throwOnFailure]} Modifies the underlying event object, if applicable."
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

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CREBindException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_0;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			String parameter = args[0].val();
			Mixed value = args[1];
			boolean throwOnFailure = false;
			if(args.length == 3) {
				throwOnFailure = ArgumentValidation.getBoolean(args[3], t);
			}
			if(environment.getEnv(GlobalEnv.class).GetEvent() == null) {
				throw new CREBindException(this.getName() + " must be called from within an event handler", t);
			}
			Event e = environment.getEnv(GlobalEnv.class).GetEvent().getEventDriver();
			if(environment.getEnv(GlobalEnv.class).GetEvent().getBoundEvent().getPriority().equals(Priority.MONITOR)) {
				throw new CREBindException("Monitor level handlers may not modify an event!", t);
			}
			ActiveEvent active = environment.getEnv(GlobalEnv.class).GetEvent();
			boolean success = false;
			if(!active.isLocked(parameter)) {
				try {
					success = e.modifyEvent(parameter, value, environment.getEnv(GlobalEnv.class).GetEvent().getUnderlyingEvent());
				} catch (ConfigRuntimeException ex) {
					ex.setTarget(t);
					throw ex;
				}
			} else {
				success = false;
			}
			if(throwOnFailure && !success) {
				throw new CREBindException("Event parameter is already locked!", t);
			}
			return CBoolean.get(success);
		}
	}

	@api
	public static class lock extends AbstractFunction {

		@Override
		public String getName() {
			return "lock";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		@Override
		public String docs() {
			return "void {&lt;none&gt; | parameterArray | parameter, [parameter...]} Locks the specified event parameter(s), or all of them,"
					+ " if specified with no arguments. Locked parameters become read only for lower priority event handlers.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREBindException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			if(environment.getEnv(GlobalEnv.class).GetEvent() == null) {
				throw new CREBindException("lock must be called from within an event handler", t);
			}

			BoundEvent.ActiveEvent e = environment.getEnv(GlobalEnv.class).GetEvent();
			Priority p = e.getBoundEvent().getPriority();
			List<String> params = new ArrayList<String>();
			if(args.length == 0) {
				e.lock(null);
			} else {
				if(args[0].isInstanceOf(CArray.TYPE)) {
					CArray ca = (CArray) args[1];
					for(int i = 0; i < ca.size(); i++) {
						params.add(ca.get(i, t).val());
					}
				} else {
					for(int i = 0; i < args.length; i++) {
						params.add(args[i].val());
					}
				}
			}
			for(String param : params) {
				e.lock(param);
			}
			return CVoid.VOID;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_0;
		}
	}

	@api
	public static class is_locked extends AbstractFunction {

		@Override
		public String getName() {
			return "is_locked";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "boolean {parameter} Returns whether or not a call to modify_event() would fail, based on"
					+ " the parameter being locked by a higher priority handler. If this returns false, it"
					+ " is still not a guarantee that the event would be successfully modified, just that"
					+ " it isn't locked.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREBindException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			if(environment.getEnv(GlobalEnv.class).GetEvent() == null) {
				throw new CREBindException("is_locked may only be called from inside an event handler", t);
			}
			return CBoolean.get(environment.getEnv(GlobalEnv.class).GetEvent().isLocked(args[0].val()));
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_0;
		}
	}

	@api
	public static class consume extends AbstractFunction {

		@Override
		public String getName() {
			return "consume";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0};
		}

		@Override
		public String docs() {
			return "void {} Consumes an event, so that lower priority handlers don't even"
					+ " receive the event. Monitor level handlers will still receive it, however,"
					+ " and they can check to see if the event was consumed.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREBindException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			if(environment.getEnv(GlobalEnv.class).GetEvent() == null) {
				throw new CREBindException("consume may only be called from an event handler!", t);
			}
			environment.getEnv(GlobalEnv.class).GetEvent().consume();
			return CVoid.VOID;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_0;
		}
	}

	@api
	public static class is_consumed extends AbstractFunction {

		@Override
		public String getName() {
			return "is_consumed";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0};
		}

		@Override
		public String docs() {
			return "boolean {} Returns whether or not this event has been consumed. Usually only useful"
					+ " for Monitor level handlers, it could also be used for highly robust code,"
					+ " as an equal priority handler could have consumed the event, but this handler"
					+ " would still receive it.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREBindException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			if(environment.getEnv(GlobalEnv.class).GetEvent() == null) {
				throw new CREBindException("is_consumed must be called from within an event handler", t);
			}
			return CBoolean.get(environment.getEnv(GlobalEnv.class).GetEvent().isConsumed());
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_0;
		}
	}

//	@api public static class when_triggered extends AbstractFunction{
//
//	}
//	@api public static class when_cancelled extends AbstractFunction{
//
//	}
	@api
	public static class event_meta extends AbstractFunction {

		@Override
		public String getName() {
			return "event_meta";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0};
		}

		@Override
		public String docs() {
			return "array {} Returns meta information about the activity in regards to this event. This"
					+ " is meant as a debug tool.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREBindException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			if(environment.getEnv(GlobalEnv.class).GetEvent() == null) {
				throw new CREBindException("event_meta must be called from within an event handler!", t);
			}
			CArray history = new CArray(t);
			for(String entry : environment.getEnv(GlobalEnv.class).GetEvent().getHistory()) {
				history.push(new CString(entry, t), t);
			}
			return history;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_0;
		}
	}

	@api
	public static class has_bind extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			return CBoolean.get(EventUtils.GetEventById(args[0].val()) != null);
		}

		@Override
		public String getName() {
			return "has_bind";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "boolean {id} Returns true if a bind with the specified id exists and is"
					+ " currently bound. False is returned otherwise. This can be used to"
					+ " pre-emptively avoid a BindException if duplicate ids are used.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}

	}

}
