
package com.laytonsmith.abstraction;

import java.util.List;

/**
 *
 * @author layton
 */
public interface MCPluginManager extends AbstractionObject{
    public MCPlugin getPlugin(String name);
	public List<MCPlugin> getPlugins();
}
