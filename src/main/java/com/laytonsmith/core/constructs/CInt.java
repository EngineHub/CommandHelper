

package com.laytonsmith.core.constructs;

import com.laytonsmith.annotations.typename;

/**
 *
 * @author layton
 */
@typename("int")
public class CInt extends Construct implements Cloneable{
    
    public CInt(long value, Target t){
        super(Long.toString(value), t);
    }

    public long getInt(){
        return (Long)value();
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
