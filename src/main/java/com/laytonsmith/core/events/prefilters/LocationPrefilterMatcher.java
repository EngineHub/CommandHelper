package com.laytonsmith.core.events.prefilters;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.ArgumentValidation;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.compiler.CompilerEnvironment;
import com.laytonsmith.core.compiler.CompilerWarning;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CBoolean;
import com.laytonsmith.core.constructs.CNull;
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
public abstract class LocationPrefilterMatcher<T extends BindableEvent> extends AbstractPrefilterMatcher<T> {

	@api
	public static class LocationPrefilterDocs implements PrefilterDocs {

		@Override
		public String getName() {
			return "location match";
		}

		@Override
		public String getNameWiki() {
			return "[[Prefilters#location match|Location Match]]";
		}

		@Override
		public String docs() {
			return "A location match accepts an array that looks similar to a location array, though is slightly modified."
					+ " In general, the array can contain x, y, z, and world keys (or indexes 0, 1, 2, and 3, though the"
					+ " named parameters take precedence). However, it can also include another parameter, called "
					+ " \"tolerance\", which indicates how fuzzy of a match the x, y, and z values can be."
					+ " For instance, if the event"
					+ " location occurs at x = 64.5837, and the prefilter indicates array(x: 65, tolerance: 1), then"
					+ " this would match. Missing keys in the prefilter are ignored and so will match any value in the"
					+ " event. If tolerance is not explicitely provided in the prefilter, the default tolerance is used,"
					+ " which itself defaults to 1.0, unless otherwise documented in the specific prefilter.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_5;
		}
	}

	@Override
	public PrefilterDocs getDocsObject() {
		return new LocationPrefilterDocs();
	}

	@Override
	public void validate(ParseTree node, Environment env) throws ConfigCompileException, ConfigCompileGroupException, ConfigRuntimeException {
		if(!node.getType(env).doesExtend(CBoolean.TYPE)) {
			env.getEnv(CompilerEnvironment.class).addCompilerWarning(node.getFileOptions(),
					new CompilerWarning("Expected a location array here, this may not perform as expected.",
							node.getTarget(), null));
		}
	}

	@Override
	public boolean matches(String key, Mixed value, T event, Target t) {
		MCLocation eventLocation = getLocation(event);
		if(eventLocation == null) {
			return CNull.NULL.equals(value);
		}
		if(CNull.NULL.equals(value)) {
			// At this point, the event wasn't null, so this is a non-match.
			return false;
		}

		CArray location = ArgumentValidation.getArray(value, t);
		Double x = null;
		Double y = null;
		Double z = null;
		String world = null;

		if(location.containsKey("x")) {
			x = ArgumentValidation.getDouble(location.get("x", t), t);
		} else if(location.containsKey(0)) {
			x = ArgumentValidation.getDouble(location.get(0, t), t);
		}

		if(location.containsKey("y")) {
			y = ArgumentValidation.getDouble(location.get("y", t), t);
		} else if(location.containsKey(1)) {
			y = ArgumentValidation.getDouble(location.get(1, t), t);
		}

		if(location.containsKey("z")) {
			z = ArgumentValidation.getDouble(location.get("z", t), t);
		} else if(location.containsKey(2)) {
			z = ArgumentValidation.getDouble(location.get(2, t), t);
		}

		if(location.containsKey("world")) {
			world = ArgumentValidation.getString(location.get("world", t), t);
		} else if(location.containsKey(3)) {
			world = ArgumentValidation.getString(location.get(3, t), t);
		}

		double tolerance = getDefaultTolerance();
		if(location.containsKey("tolerance")) {
			tolerance = ArgumentValidation.getDouble(location.get("tolerance", t), t);
		}

		if(world != null && !eventLocation.getWorld().getName().equals(world)) {
			return false;
		}

		if(x != null && Math.abs(x - eventLocation.getX()) > tolerance) {
			return false;
		}

		if(y != null && Math.abs(y - eventLocation.getY()) > tolerance) {
			return false;
		}

		if(z != null && Math.abs(z - eventLocation.getZ()) > tolerance) {
			return false;
		}

		return true;
	}

	protected abstract MCLocation getLocation(T event);

	protected double getDefaultTolerance() {
		return 1.0;
	}

}
