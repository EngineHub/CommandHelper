package com.laytonsmith.core.natives;

import com.laytonsmith.core.constructs.CPrimitive;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.natives.interfaces.Mixed;

/**
 *
 * @author lsmith
 */
public class MClass implements Mixed {

	public Object value() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public String val() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public boolean isNull() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public String typeName() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public CPrimitive primitive(Target t) throws ConfigRuntimeException {
		throw new UnsupportedOperationException("Not supported yet.");
	}
	
}
