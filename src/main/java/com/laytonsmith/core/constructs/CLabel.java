

package com.laytonsmith.core.constructs;

/**
 *
 * @author Layton
 */
public class CLabel extends Construct{
    Construct label;
    public CLabel(Construct value){
        super(value.val(), value.getTarget()); 
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

	public String typeName() {
		return "$label";
	}
		
}
