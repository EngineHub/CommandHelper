package com.laytonsmith.core.natives.interfaces;

/**
 *
 * @author cailin
 */
public enum ObjectModifier {
    /**
     * A final class is one that cannot be extended by subclasses. Only a non-abstract class
     * can use this keyword, as interfaces and abstract classes must be extended/implemented
     * for its existence to make sense.
     */
    FINAL,
    /**
     * A public class is one that can be accessed from any other class.
     */
    PUBLIC,
    /**
     * A package level class is one that can be accessed from other classes that are within the same package
     */
    PACKAGE,
    /**
     * A private class is one that can only be accessed from other classes within the same containing class.
     * This is not useable in top level classes, as it wouldn't make sense otherwise.
     */
    PRIVATE,
    /**
     * A static class is one that is not tied to the containing class's instance scope, but is tied to the
     * static scope. This in not useable in top level classes, as it wouldn't make sense otherwise.
     */
    STATIC,
}
