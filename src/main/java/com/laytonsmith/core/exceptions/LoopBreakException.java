

package com.laytonsmith.core.exceptions;

import com.laytonsmith.core.constructs.Target;

/**
 *
 * @author Layton
 */
public class LoopBreakException extends LoopManipulationException{
    public LoopBreakException(int times, Target t){
		super(times, "break", t);
    }
}
