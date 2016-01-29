

package com.laytonsmith.core.constructs;

import com.laytonsmith.core.constructs.Construct.ConstructType;

/**
 *
 * 
 */
public class CLabel extends Construct {
    Construct label;
    public CLabel(Construct value){
        super(value.val(), ConstructType.LABEL, value.getTarget()); 
        label = value;
    }
    
    public Construct cVal(){
        return label;
    }

    @Override
    public boolean isDynamic() {
        return false;
    }

	@Override
	public String toString() {
		return label.toString() + ":";
	}
		
}
