

package com.laytonsmith.core.exceptions;

import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;

/**
 *
 * @author Layton
 */
public class FunctionReturnException extends ProgramFlowManipulationException{
    Construct ret;
    public FunctionReturnException(Construct ret, Target t){
		super(t);
        this.ret = ret;
    }
    public Construct getReturn(){
        return ret;
    }
}
