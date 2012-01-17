/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
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
