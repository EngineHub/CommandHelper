

package com.laytonsmith.core.exceptions;

import com.laytonsmith.core.constructs.Target;

/**
 *
 */
public class LoopContinueException extends LoopManipulationException{
    public LoopContinueException(int times, Target t){
		super(times, "continue", t);
    }
}
