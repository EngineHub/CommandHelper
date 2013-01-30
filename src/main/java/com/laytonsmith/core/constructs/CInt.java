

package com.laytonsmith.core.constructs;

import com.laytonsmith.annotations.typename;

/**
 *
 * @author layton
 */
@typename("int")
public class CInt extends CNumber implements Cloneable {
    
    public CInt(long value, Target t){
        super(value, t);
    }
    
    @Override
    public CInt clone() throws CloneNotSupportedException{
        return this;
    }

    @Override
    public boolean isDynamic() {
        return false;
    }

	public String typeName() {
		return "int";
	}

}
