


package com.laytonsmith.core.exceptions;

/**
 *
 * @author Layton
 */
public class CancelCommandException extends ProgramFlowManipulationException{
    String message;
    public CancelCommandException(String message){
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }

}
