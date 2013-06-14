
package com.laytonsmith.core.compiler;

import com.laytonsmith.annotations.typename;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.arguments.Argument;
import com.laytonsmith.core.constructs.CPrimitive;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.DocComment;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.Exceptions;
import java.util.List;

/**
 *
 */
@typename("procedure")
public class ProcedureDefinition extends Construct{
	String name;
	DocComment comment;
	Environment env;
	List<String> varNames;
	List<ParseTree> varDefaults;
	ParseTree code;
	Argument returnType = Argument.AUTO; //TODO
	public ProcedureDefinition(Argument returnType, String name, DocComment comment, List<String> varNames, 
			List<ParseTree> varDefaults, ParseTree code, Target target){
		super(name, target);
		this.returnType = returnType;
	}

	public boolean isDynamic() {
		boolean ret = true;
		for(ParseTree defs : varDefaults){
			if(defs.isDynamic()){
				ret = false;
				break;
			}
		}
		if(code.isDynamic()){
			ret = false;
		}
		return ret;
	}

	public String typeName() {
		return "procedure";
	}

	public CPrimitive primitive(Target t) throws ConfigRuntimeException {
		throw new Exceptions.CastException("Cannot cast procedure to a primitive", t);
	}
	
	public Argument returnType(){
		return returnType;
	}
}
