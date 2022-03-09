package com.laytonsmith.core.events.prefilters;

import com.laytonsmith.PureUtilities.Common.Annotations.ForceImplementation;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.SimpleDocumentation;
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
public interface PrefilterMatcher<T extends BindableEvent> extends SimpleDocumentation {

	public static interface PrefilterDocs extends SimpleDocumentation {
		String getNameWiki();
	}

	/**
	 * Given the value and event, should return false if this doesn't match.If true is returned, this indicates that the
	 * event matches, and assuming all other prefilters match, means the event will run.
	 *
	 * @param key The name of this prefilter. This is a bit of a leaky abstraction, but can be useful for macro based
	 * prefilters.
	 * @param value The value of the prefilter
	 * @param event The event
	 * @param t The code target.
	 * @return False if the event doesn't match, true otherwise.
	 */
	boolean matches(String key, Mixed value, T event, Target t);

	/**
	 * Returns the filter type, which is used in text based documentation.
	 *
	 * @return
	 */
	@Override
	String getName();

	/**
	 * Returns the filter type, which is used in Wiki based documentation.
	 * @return
	 */
	String getNameWiki();

	/**
	 * Returns documentation for this filter type, which is used in the events page for matchers tagged with @api.
	 * Optional if this class isn't tagged with @api.
	 * @return
	 */
	@Override
	String docs();

	/**
	 * Returns the documentation object, if it exists.
	 * @return
	 */
	@ForceImplementation
	PrefilterDocs getDocsObject();

	/**
	 * If additional prefilter validation can be performed at compile time, this should be done here.Note that this
	 * method is run during compilation, and so the value may be of any type (CFunction, etc).The type will always be
	 * declared however (though may be AUTO) and can be typechecked as necessary.
	 *
	 * @param node The parse tree for the prefilter node.
	 * @param env
	 * @throws com.laytonsmith.core.exceptions.ConfigCompileException
	 * @throws com.laytonsmith.core.exceptions.ConfigCompileGroupException
	 */
	void validate(ParseTree node, Environment env) throws ConfigCompileException, ConfigCompileGroupException, ConfigRuntimeException;

}
