

package com.laytonsmith.core.constructs;

/**
 *
 * @author Layton
 */
public class CVoid extends Construct implements Cloneable{
    
    public CVoid(Target t){
        super("", ConstructType.VOID, t);
    }
    
    @Override
    public CVoid clone() throws CloneNotSupportedException{
        return this;
    }

    @Override
    public boolean isDynamic() {
        return false;
    }
}
