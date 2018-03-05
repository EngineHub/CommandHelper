package com.laytonsmith.abstraction;

import java.util.List;

public interface MCPluginManager extends AbstractionObject {

	MCPlugin getPlugin(String name);

	List<MCPlugin> getPlugins();
}
