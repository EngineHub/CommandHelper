


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
@typeof("double")
public class CDouble extends CNumber implements Cloneable {

    public static final long serialVersionUID = 1L;
    final double val;

    public CDouble(String value, Target t){
        super(value, ConstructType.INT, t);
        try{
            val = Double.parseDouble(value);
        } catch(NumberFormatException e){
            throw ConfigRuntimeException.BuildException("Could not cast " + value + " to double", CREFormatException.class, t);
        }
    }

    public CDouble(double value, Target t){
        super(Double.toString(value), ConstructType.DOUBLE, t);
        val = value;
    }

    public double getDouble(){
        return val;
    }

    @Override
    public CDouble clone() throws CloneNotSupportedException{
        return this;
    }

    @Override
    public boolean isDynamic() {
        return false;
    }

	@Override
	public String docs() {
		return "A double is a floating point value, such as PI, 3.1415. Integral values can also be stored in a double, though they are"
				+ " represented differently, both internally and when displayed.";
	}

	@Override
	public Version since() {
		return CHVersion.V3_0_1;
	}
}
