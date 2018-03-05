package com.laytonsmith.abstraction;

public interface MCPlugin extends AbstractionObject {

	boolean isEnabled();

	boolean isInstanceOf(Class c);

	String getName();
}
