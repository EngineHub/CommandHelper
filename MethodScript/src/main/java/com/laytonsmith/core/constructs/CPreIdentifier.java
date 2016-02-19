package com.laytonsmith.core.constructs;

/**
 *
 * 
 */
public class CPreIdentifier extends Construct {
    
    public CPreIdentifier(String value, Target t){
        super(value, ConstructType.IDENTIFIER, t);
    }

    @Override
    public boolean isDynamic() {
        return false;
    }
    
}
