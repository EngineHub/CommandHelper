package com.laytonsmith.core.functions;

import com.laytonsmith.annotations.api;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.exceptions.MarshalException;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author lsmith
 */
public class DataTransformations {
	public static String docs(){
		return "This class provides functions that are able to transform data from native objects to"
				+ " their serialized forms, i.e. json, ini, etc.";
	}
	
	@api
	public static class json_encode extends AbstractFunction {

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException};
		}

		public boolean isRestricted() {
			return false;
		}

		public Boolean runAsync() {
			return null;
		}

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			CArray ca = Static.getArray(args[0], t);
			try {
				return new CString(Construct.json_encode(ca, t), t);
			} catch (MarshalException ex) {
				//This shouldn't happen, because the construct will be an array.
				throw new Error(ex);
			}
		}

		public String getName() {
			return "json_encode";
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		public String docs() {
			return "string {array} Converts an array into a JSON encoded string. Both normal and associative arrays are supported.";
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
		
	}
	
	@api
	public static class json_decode extends AbstractFunction {

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.FormatException};
		}

		public boolean isRestricted() {
			return false;
		}

		public Boolean runAsync() {
			return null;
		}

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			String s = args[0].val();
			try {
				return Construct.json_decode(s, t);
			} catch (MarshalException ex) {
				throw new Exceptions.FormatException("The JSON string is improperly formatted.", t, ex);
			}
		}

		public String getName() {
			return "json_decode";
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		public String docs() {
			return "array {string} Takes a JSON encoded string, and returns an array, either normal or associative,"
					+ " depending on the contents of the JSON string. If the JSON string is improperly formatted,"
					+ " a FormatException is thrown.";
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
		
	}
}
