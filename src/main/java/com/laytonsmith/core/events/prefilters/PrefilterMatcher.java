package com.laytonsmith.core.events.prefilters;

import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.events.BindableEvent;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigCompileGroupException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.natives.interfaces.Mixed;

/**
 *
 * @param <T>
 */
public interface PrefilterMatcher<T extends BindableEvent> {

	/**
	 * Given the value and event, should return false if this doesn't match.If true is returned, this indicates that the
	 * event matches, and assuming all other prefilters match, means the event will run.
	 *
	 * @param value The value of the prefilter
	 * @param event The event
	 * @param t The code target.
	 * @return False if the event doesn't match, true otherwise.
	 */
	boolean matches(Mixed value, T event, Target t);

	/**
	 * Returns the filter type, which is used in documentation.
	 *
	 * @return
	 */
	String filterType();

	/**
	 * If additional prefilter validation can be performed at compile time, this should be done here.Note that this
	 * method is run during compiation, and so the value may be of any type (CFunction, etc).The type will always be
	 * declared however (though may be AUTO) and can be typechecked as necessary.
	 *
	 * @param node The parse tree for the prefilter node.
	 * @param env
	 * @throws com.laytonsmith.core.exceptions.ConfigCompileException
	 * @throws com.laytonsmith.core.exceptions.ConfigCompileGroupException
	 */
	void validate(ParseTree node, Environment env) throws ConfigCompileException, ConfigCompileGroupException, ConfigRuntimeException;

}
