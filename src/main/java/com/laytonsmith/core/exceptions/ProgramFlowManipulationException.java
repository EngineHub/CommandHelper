package com.laytonsmith.core.exceptions;

/**
 * If an exception is meant to break the program flow in the script itself, it should
 * extend this, so if an exception passes all the way up to a top level handler, it
 * can address it in a standard way if it doesn't know what to do with these types
 * of exceptions.
 * @author layton
 */
public class ProgramFlowManipulationException extends RuntimeException {
    
}
