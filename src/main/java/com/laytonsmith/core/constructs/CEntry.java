

package com.laytonsmith.core.constructs;

/**
 *
 * @author Layton
 */
public class CEntry extends Construct{
    Construct ckey;
    Construct construct;

    public CEntry(Construct key, Construct value, Target t){
        super(key.val() + ":(CEntry)", t);
        this.ckey = key;
        this.construct = value;
    }
    
    @Override
    public String toString(){
        return construct.val();
    }
    
    public Construct construct(){
        return this.construct;
    }

    @Override
    public boolean isDynamic() {
        return false;
    }

	public String typeName() {
		return "$entry";
	}
    
}
