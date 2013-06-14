package com.laytonsmith.core.natives;

import com.laytonsmith.core.constructs.CPrimitive;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.natives.interfaces.Mixed;

/**
 *
 */
public class MClass implements Mixed {

	public Object value() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public String val() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public boolean isNull() {
		//Since we have a concrete type, it's obviously not null.
		return false;
	}

	public String typeName() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public CPrimitive primitive(Target t) throws ConfigRuntimeException {
		throw ConfigRuntimeException.CreateUncatchableException("Cannot cast " + typeName() + " to a primitive", t);
	}

	public boolean isImmutable() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public boolean isDynamic() {
		throw new UnsupportedOperationException("TODO: Not supported yet.");
	}

	public void destructor() {
		throw new UnsupportedOperationException("TODO: Not supported yet.");
	}

	public Mixed doClone() {
		throw new UnsupportedOperationException("TODO: Not supported yet.");
	}

	public Target getTarget() {
		throw new UnsupportedOperationException("TODO: Not supported yet.");
	}
	
}
