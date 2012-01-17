/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.laytonsmith.core.exceptions;

/**
 *
 * @author Layton
 */
public class CancelCommandException extends RuntimeException{
    String message;
    public CancelCommandException(String message){
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }

}
