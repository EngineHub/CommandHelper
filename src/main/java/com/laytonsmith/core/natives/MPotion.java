package com.laytonsmith.core.natives;

import com.laytonsmith.annotations.documentation;
import com.laytonsmith.annotations.typename;
import com.laytonsmith.core.natives.interfaces.MObject;

/**
 *
 */
@typename("Potion")
public class MPotion extends MObject {
	@documentation(docs="The potion ID. See http://www.minecraftwiki.net/wiki/Potion_effects for a complete listing of valid potion effects.")
	public int id;
	@documentation(docs="This is the number of levels to add to the base power (effect level 1). If this or seconds is set to 0, the effect will be"
			+ " removed.")
	public int strength;
	@documentation(docs="The duration of the effect. If this or strength is set to 0, the effect will be removed.")
	public int seconds;
	@documentation(docs="If true, particles will be less noticable.")
	public boolean ambient;
}
