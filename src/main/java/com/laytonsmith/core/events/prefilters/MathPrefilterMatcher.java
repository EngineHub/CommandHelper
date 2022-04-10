package com.laytonsmith.core.events.prefilters;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.ArgumentValidation;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.compiler.CompilerEnvironment;
import com.laytonsmith.core.compiler.CompilerWarning;
import com.laytonsmith.core.compiler.analysis.StaticAnalysis;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CEntry;
import com.laytonsmith.core.constructs.CFunction;
import com.laytonsmith.core.constructs.CNumber;
import com.laytonsmith.core.constructs.LeftHandSideType;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.events.BindableEvent;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigCompileGroupException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.DataHandling;
import com.laytonsmith.core.natives.interfaces.Mixed;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 *
 * @param <T>
 */
public abstract class MathPrefilterMatcher<T extends BindableEvent> extends AbstractPrefilterMatcher<T> {

	@api
	public static class MathPrefilterDocs implements PrefilterDocs {

		@Override
		public String getName() {
			return "math match";
		}

		@Override
		public String getNameWiki() {
			return "[[Prefilters#math match|Math Match]]";
		}

		@Override
		public String docs() {
			return """
				A math match is a simple match against a double value. By default, the prefilter will provide a
				tolerance, but this can be overridden by providing an array with the keys "value" and
				"tolerance", and then the tolerance can be set as necessary.

				<%CODE|
					bind('event', null, array(prefilter: 5), @event) {
						// Will fire if prefilter is 5.0
					}

					bind('event', null, array(prefilter: array(value: 5, tolerance: 2)), @event) {
						// Will fire if prefilter is 5.0, 6.5, or 4.3 for instance
					}
				%>
			""";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_5;
		}
	}

	@Override
	public PrefilterDocs getDocsObject() {
		return new MathPrefilterDocs();
	}

	private static final List<String> VALID_KEYS = Arrays.asList("value", "tolerance");

	@Override
	public LeftHandSideType typecheck(StaticAnalysis analysis,
			ParseTree prefilterValueParseTree, Environment env, Set<ConfigCompileException> exceptions) {

		// Add deep validation for hard-coded "array(...)" arguments.
		if(prefilterValueParseTree.getData() instanceof CFunction
				&& prefilterValueParseTree.getData().val().equals(DataHandling.array.NAME)) {
			for(ParseTree entry : prefilterValueParseTree.getChildren()) {
				if(entry.getData() instanceof CEntry centry) {
					String key = entry.getChildAt(0).getData().val();
					ParseTree valParseTree = entry.getChildAt(1);
					LeftHandSideType valType = analysis.typecheck(valParseTree, null, env, exceptions);
					if(!VALID_KEYS.contains(key)) {
						env.getEnv(CompilerEnvironment.class).addCompilerWarning(
								prefilterValueParseTree.getFileOptions(),
								new CompilerWarning("Unexpected key, this will be ignored.",
										centry.ckey().getTarget(), null));
						continue;
					}
					if(key.equals("value") && !valType.doesExtend(CNumber.TYPE, env)) {
						env.getEnv(CompilerEnvironment.class).addCompilerWarning(
								prefilterValueParseTree.getFileOptions(),
								new CompilerWarning("Value should be a double", centry.ckey().getTarget(), null));
					}
					if(key.equals("tolerance") && !valType.doesExtend(CNumber.TYPE, env)) {
						env.getEnv(CompilerEnvironment.class).addCompilerWarning(
								prefilterValueParseTree.getFileOptions(),
								new CompilerWarning("Tolerance should be a number", centry.ckey().getTarget(), null));
					}
				} else {
					analysis.typecheck(entry, null, env, exceptions);
				}
			}
			return CArray.TYPE.asLeftHandSideType();
		}

		// Handle other prefilter values as usual.
		return super.typecheck(analysis, prefilterValueParseTree, env, exceptions);
	}

	@Override
	public void validate(ParseTree node, LeftHandSideType nodeType, Environment env)
			throws ConfigCompileException, ConfigCompileGroupException, ConfigRuntimeException {
		if(!nodeType.doesExtend(CNumber.TYPE, env) && !nodeType.doesExtend(CArray.TYPE, env)) {
			env.getEnv(CompilerEnvironment.class).addCompilerWarning(node.getFileOptions(),
					new CompilerWarning("Expecting a number or array here, this may not perform as expected.",
							node.getTarget(), null));
		}
	}

	@Override
	public boolean matches(String key, Mixed value, T event, Target t, Environment env) {
		double val;
		double tolerance = getTolerance();
		if(value instanceof CArray array) {
			val = ArgumentValidation.getDouble(array.get("value", t), t, env);
			tolerance = ArgumentValidation.getDouble(array.get("tolerance", t), t, env);
		} else {
			val = ArgumentValidation.getDouble(value, t, env);
		}
		return Math.abs(val - getProperty(event)) <= tolerance;
	}

	protected abstract double getProperty(T event);

	protected abstract double getTolerance();

}
