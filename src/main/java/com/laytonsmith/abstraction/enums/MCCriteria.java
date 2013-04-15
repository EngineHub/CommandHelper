package com.laytonsmith.abstraction.enums;

import com.laytonsmith.annotations.typename;
import com.laytonsmith.core.constructs.CPrimitive;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.natives.MEnum;

/**
 * Criteria names which trigger an objective to be modified by actions in-game
 * 
 * @author jb_aero
 */
@typename("Criteria")
public enum MCCriteria implements MEnum {
	DEATHCOUNT("deathCount"),
	HEALTH("health"),
	PLAYERKILLCOUNT("playerKillCount"),
	TOTALKILLCOUNT("totalKillCount"),
	DUMMY("dummy");
	
	private String criteria;
	
	private MCCriteria(String crit) {
		criteria = crit;
	}
	
	public String getCriteria() {
		return criteria;
	}

	public Object value() {
		return this;
	}

	public String val() {
		return name();
	}

	public boolean isNull() {
		return false;
	}

	public String typeName() {
		return this.getClass().getAnnotation(typename.class).value();
	}

	public CPrimitive primitive(Target t) throws ConfigRuntimeException {
		throw new Error();
	}

	public boolean isImmutable() {
		return true;
	}
}
