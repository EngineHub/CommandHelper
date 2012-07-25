

package com.laytonsmith.core.constructs;

/**
 *
 * @author layton
 */
public class CFunction extends Construct {
    
    public static final long serialVersionUID = 1L;    

    public CFunction(String name, Target t) {
        super(name, ConstructType.FUNCTION, t);
    }

    @Override
    public String toString() {
        return getValue();
    }
    
    @Override
    public CFunction clone() throws CloneNotSupportedException{
        return (CFunction) super.clone();
    }

    @Override
    public boolean isDynamic() {
        return true;
    }
    
}
