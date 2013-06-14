
package com.laytonsmith.abstraction;

/**
 *
 */
public interface MCPlugin extends AbstractionObject{
    public boolean isEnabled();
    public boolean isInstanceOf(Class c);
	public String getName();
}
