/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.laytonsmith.aliasengine;

/**
 *
 * @author Layton
 */
public class CancelCommandException extends Exception{
    String message;
    public CancelCommandException(String message){
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }

}
