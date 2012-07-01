package com.laytonsmith.core.constructs;

import com.laytonsmith.core.GenericTreeNode;

/**
 *
 * @author layton
 */
public class CIdentifier extends Construct{

    private final GenericTreeNode<Construct> contained;
    public CIdentifier(String type, GenericTreeNode<Construct> c, Target t){
        super(type, ConstructType.IDENTIFIER, t);
        contained = c;
    }
    
    public GenericTreeNode<Construct> contained(){
        return contained;
    }
    
    @Override
    public boolean isDynamic() {
        return contained.data.isDynamic();
    }
    
}
