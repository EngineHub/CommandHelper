
package com.laytonsmith.abstraction.enums;

import com.laytonsmith.annotations.typename;
import com.laytonsmith.core.constructs.CPrimitive;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.natives.MEnum;

/**
 *
 * @author layton
 */
@typename("Effect")
public enum MCEffect implements MEnum {
    BOW_FIRE(1002),
    CLICK1(1001),
    CLICK2(1000),
    DOOR_TOGGLE(1003),
    EXTINGUISH(1004),
    RECORD_PLAY(1005),
    GHAST_SHRIEK(1007),
    GHAST_SHOOT(1008),
    BLAZE_SHOOT(1009),
    SMOKE(2000),
    STEP_SOUND(2001),
    POTION_BREAK(2002),
    ENDER_SIGNAL(2003),
	/**
     * Sound of zombies chewing on wooden doors.
     */
    ZOMBIE_CHEW_WOODEN_DOOR(1010),
    /**
     * Sound of zombies chewing on iron doors.
     */
    ZOMBIE_CHEW_IRON_DOOR(1011),
    /**
     * Sound of zombies destroying a door.
     */
    ZOMBIE_DESTROY_DOOR(1012),
    MOBSPAWNER_FLAMES(2004);

    private final int id;

    MCEffect(int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }

	public Object value() {
		return this;
	}

	public String val() {
		return name();
	}

	public boolean isNull() {
		return false;
	}

	public String typeName() {
		return this.getClass().getAnnotation(typename.class).value();
	}

	public CPrimitive primitive(Target t) throws ConfigRuntimeException {
		throw new Error();
	}

	public boolean isImmutable() {
		return true;
	}
}
