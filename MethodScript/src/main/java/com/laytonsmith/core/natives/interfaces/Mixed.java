

package com.laytonsmith.core.natives.interfaces;

import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.SimpleDocumentation;
import com.laytonsmith.core.constructs.Target;

/**
 *
 *
 */
@typeof("mixed")
public interface Mixed extends Cloneable, SimpleDocumentation {
    public String val();

	public void setTarget(Target target);

	public Mixed clone() throws CloneNotSupportedException;
}
