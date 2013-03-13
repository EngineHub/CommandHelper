package com.laytonsmith.core.natives;

import com.laytonsmith.abstraction.enums.MCSound;
import com.laytonsmith.annotations.documentation;
import com.laytonsmith.annotations.typename;
import com.laytonsmith.core.natives.interfaces.MObject;

/**
 *
 */
@typename("Sound")
public class MSound extends MObject {
	
	@documentation(docs="The sound type to play")
	public MCSound sound;
	@documentation(docs="The volume of the sound")
	public int volume = 1;
	@documentation(docs="The pitch of the sound")
	public int pitch = 1;
	
}
