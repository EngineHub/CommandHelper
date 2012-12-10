package com.laytonsmith.core.constructs;

import com.laytonsmith.core.ParseTree;

/**
 *
 * @author layton
 */
public class CIdentifier extends CFunction {

    private final ParseTree contained;
    public CIdentifier(String type, ParseTree c, Target t){
        super(type, t);
        contained = c;
    }
    
    @Override
    public boolean isDynamic() {
        return contained.getData().isDynamic();
    }
    
    public ParseTree contained(){
        return contained;
    }
    
}
