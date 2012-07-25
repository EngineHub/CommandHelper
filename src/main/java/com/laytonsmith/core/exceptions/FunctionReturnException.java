

package com.laytonsmith.core.exceptions;

import com.laytonsmith.core.constructs.Construct;

/**
 *
 * @author Layton
 */
public class FunctionReturnException extends ProgramFlowManipulationException{
    Construct ret;
    public FunctionReturnException(Construct ret){
        this.ret = ret;
    }
    public Construct getReturn(){
        return ret;
    }
}
