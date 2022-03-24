package com.laytonsmith.core.events.prefilters;

import java.util.Set;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.compiler.analysis.StaticAnalysis;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.LeftHandSideType;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.events.BindableEvent;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigCompileGroupException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;

/**
 *
 * @param <T>
 */
public abstract class AbstractPrefilterMatcher<T extends BindableEvent> implements PrefilterMatcher<T> {

	@Override
	public String getName() {
		return getDocsObject().getName();
	}

	@Override
	public String docs() {
		return getDocsObject().docs();
	}

	@Override
	public Version since() {
		return getDocsObject().since();
	}

	@Override
	public String getNameWiki() {
		return getDocsObject().getNameWiki();
	}

	/**
	 * {@inheritDoc} By default, this calls {@link StaticAnalysis#typecheck(ParseTree, Set)} on the prefilter value
	 * and passes it to {@link #validate(ParseTree, CClassType, Environment)} for validation.
	 * @return The prefilter value type.
	 */
	@Override
	public LeftHandSideType typecheck(StaticAnalysis analysis,
			ParseTree prefilterValueParseTree, Environment env, Set<ConfigCompileException> exceptions) {
		LeftHandSideType prefilterValueType = analysis.typecheck(prefilterValueParseTree, env, exceptions);
		try {
			this.validate(prefilterValueParseTree, prefilterValueType, env);
		} catch (ConfigCompileException e) {
			exceptions.add(e);
		} catch (ConfigCompileGroupException e) {
			exceptions.addAll(e.getList());
		} catch (ConfigRuntimeException e) {
			exceptions.add(new ConfigCompileException(e));
		}
		return prefilterValueType;
	}
}
