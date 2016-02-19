
package com.laytonsmith.abstraction;

import java.util.List;

/**
 *
 * 
 */
public interface MCPluginManager extends AbstractionObject{
    public MCPlugin getPlugin(String name);
	public List<MCPlugin> getPlugins();
}
