

package com.laytonsmith.core.constructs;

import com.laytonsmith.annotations.typename;

/**
 *
 */
@typename("void")
public class CVoid extends Construct implements Cloneable{
    
    public CVoid(Target t){
        super("", t);
    }
    
    @Override
    public CVoid clone() throws CloneNotSupportedException{
        return this;
    }

    @Override
    public boolean isDynamic() {
        return false;
    }

	public String typeName() {
		return "void";
	}
}
