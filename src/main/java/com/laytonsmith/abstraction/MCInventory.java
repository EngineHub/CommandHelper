/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.abstraction;

/**
 *
 * @author Layton
 */
public interface MCInventory extends AbstractionObject {
	public MCInventoryType getType();
	public int getSize();
	public MCItemStack getItem(int index);
	public void setItem(int index, MCItemStack stack);
}
