


package com.laytonsmith.core.constructs;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.exceptions.CRE.CREFormatException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;

/**
 *
 *
 */
@typeof("int")
public class CInt extends CNumber implements Cloneable {

    public static final long serialVersionUID = 1L;
    final long val;
    public CInt(String value, Target t){
        super(value, ConstructType.INT, t);
        try{
            val = Long.parseLong(value);
        } catch(NumberFormatException e){
            throw ConfigRuntimeException.BuildException("Could not parse " + value + " as an integer", CREFormatException.class, t);
        }
    }

    public CInt(long value, Target t){
        super(Long.toString(value), ConstructType.INT, t);
        val = value;
    }

    public long getInt(){
        return val;
    }

    @Override
    public CInt clone() throws CloneNotSupportedException{
        return this;
    }

    @Override
    public boolean isDynamic() {
        return false;
    }

	@Override
	public String docs() {
		return "An integer is a discreet numerical value. All positive and negative counting numbers, as well as 0.";
	}

	@Override
	public Version since() {
		return CHVersion.V3_0_1;
	}

}
