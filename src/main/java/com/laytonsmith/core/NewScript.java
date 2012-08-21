package com.laytonsmith.core;

import com.laytonsmith.PureUtilities.StringUtils;
import com.laytonsmith.core.constructs.Construct;
import java.util.List;

/**
 *
 * @author lsmith
 */
public class NewScript {
	ParseTree executable;
	List<Construct> signature;
	String label;
	
	public NewScript(List<Construct> signature, ParseTree executable, String label){
		this.signature = signature;
		this.executable = executable;
		this.label = label;
		if("".equals(label)){
			this.label = null;
		}
	}

	@Override
	public String toString() {
		return (label != null?label + ":":"") + StringUtils.Join(signature, " ");
	}		
	
}
