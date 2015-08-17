package com.laytonsmith.core.constructs;

/**
 *
 *
 */
public class CDotOperator extends Construct{

    public CDotOperator(String value, Target t){
        super(value, Construct.ConstructType.IDENTIFIER, t);
    }

    @Override
    public boolean isDynamic() {
        return false;
    }

}
