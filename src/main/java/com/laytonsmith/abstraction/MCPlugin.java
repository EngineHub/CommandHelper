
package com.laytonsmith.abstraction;

/**
 *
 * @author layton
 */
public interface MCPlugin extends AbstractionObject{
    public boolean isEnabled();
    public boolean isInstanceOf(Class c);
	public String getName();
}
