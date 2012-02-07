/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.abstraction;

/**
 *
 * @author layton
 */
public interface MCEntity {
    public int getEntityId();

    public boolean isTameable();

    public MCTameable getMCTameable();
}
