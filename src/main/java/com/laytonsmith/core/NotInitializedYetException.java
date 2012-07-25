

package com.laytonsmith.core;

/**
 *
 * @author Layton
 */
public class NotInitializedYetException extends RuntimeException{
    public NotInitializedYetException(String msg){
        super(msg);
    }
}
