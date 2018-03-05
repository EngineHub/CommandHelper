package com.laytonsmith.core.compiler;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.DocComment;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.natives.interfaces.Mixed;
import com.laytonsmith.core.natives.interfaces.ObjectType;
import java.util.List;

/**
 *
 *
 */
public class ProcedureDefinition extends Construct {

	String name;
	DocComment comment;
	Environment env;
	List<String> varNames;
	List<ParseTree> varDefaults;
	ParseTree code;

	public ProcedureDefinition(String name, DocComment comment, List<String> varNames, List<ParseTree> varDefaults, ParseTree code, Target target) {
		super(name, Construct.ConstructType.FUNCTION, target);
	}

	@Override
	public boolean isDynamic() {
		boolean ret = true;
		for(ParseTree defs : varDefaults) {
			if(defs.isDynamic()) {
				ret = false;
				break;
			}
		}
		if(code.isDynamic()) {
			ret = false;
		}
		return ret;
	}

	@Override
	public Version since() {
		return super.since();
	}

	@Override
	public String docs() {
		return super.docs();
	}

	@Override
	public CClassType[] getSuperclasses() {
		return new CClassType[]{Mixed.TYPE};
	}

	@Override
	public CClassType[] getInterfaces() {
		return new CClassType[]{};
	}

	@Override
	public ObjectType getObjectType() {
		return super.getObjectType();
	}

}
