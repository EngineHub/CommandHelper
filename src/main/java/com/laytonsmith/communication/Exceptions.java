package com.laytonsmith.communication;

/**
 *
 * @author import
 */
public class Exceptions {
    public static class InvalidNameException extends Exception {
        public InvalidNameException(String message) {
            super(message);
        }
    }
    
    public static class InvalidChannelException extends Exception {
        public InvalidChannelException(String message) {
            super(message);
        }
    }
}
