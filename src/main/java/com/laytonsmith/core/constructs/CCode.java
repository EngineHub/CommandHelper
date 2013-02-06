package com.laytonsmith.core.constructs;

import com.laytonsmith.annotations.typename;

/**
 * A CCode object is an implied closure. It isn't created directly, but is
 * simply used as a placeholder to fill in documentation.
 * @author lsmith
 */
@typename("code")
public class CCode extends Construct {

	public CCode(Target t) {
		super("<code>", t);
	}

	@Override
	public boolean isDynamic() {
		return true;
	}

	public String typeName() {
		return "code";
	}
	
}
