package com.laytonsmith.core.objects;

/**
 * This exception is thrown if the ObjectDefinition could not be found.
 */
public class ObjectDefinitionNotFoundException extends Exception {

	public ObjectDefinitionNotFoundException() {
		super();
	}

	public ObjectDefinitionNotFoundException(String message) {
		super(message);
	}

	public ObjectDefinitionNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

}
