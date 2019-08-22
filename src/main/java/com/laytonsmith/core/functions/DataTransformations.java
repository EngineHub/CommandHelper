package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.PureUtilities.XMLDocument;
import com.laytonsmith.annotations.api;
import com.laytonsmith.annotations.core;
import com.laytonsmith.annotations.noboilerplate;
import com.laytonsmith.core.ArgumentValidation;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CNull;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CRE.CRECastException;
import com.laytonsmith.core.exceptions.CRE.CREFormatException;
import com.laytonsmith.core.exceptions.CRE.CREThrowable;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.exceptions.MarshalException;
import com.laytonsmith.core.natives.interfaces.Mixed;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import javax.xml.xpath.XPathExpressionException;
import org.xml.sax.SAXException;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.parser.ParserException;
import org.yaml.snakeyaml.scanner.ScannerException;

/**
 *
 */
@core
public class DataTransformations {

	public static String docs() {
		return "This class provides functions that are able to transform data from native objects to"
				+ " their serialized forms, i.e. json, ini, etc.";
	}

	@api
	public static class json_encode extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			CArray ca = Static.getArray(args[0], t);
			try {
				return new CString(Construct.json_encode(ca, t), t);
			} catch (MarshalException ex) {
				throw new CRECastException(ex.getMessage(), t);
			}
		}

		@Override
		public String getName() {
			return "json_encode";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "string {array} Converts an array into a JSON encoded string. Both normal and associative arrays are supported."
					+ " Within the array, only primitives and arrays can be encoded.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Simple usage", "json_encode(array(emptyObject: associative_array(), anArray: array(1, 2, 3), anObject: array(one: 1, two: 2)));")
			};
		}
	}

	@api
	public static class json_decode extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREFormatException.class};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			String s = args[0].val();
			try {
				return Construct.json_decode(s, t);
			} catch (MarshalException ex) {
				throw new CREFormatException("The input JSON string is improperly formatted. Check your formatting and try again.", t, ex);
			}
		}

		@Override
		public String getName() {
			return "json_decode";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "array {string} Takes a JSON encoded string, and returns an array, either normal or associative,"
					+ " depending on the contents of the JSON string. If the JSON string is improperly formatted,"
					+ " a FormatException is thrown.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Simple usage", "json_decode(\"{\\\"one\\\": 1, \\\"two\\\": [1, 2, 3]}\");")
			};
		}
	}

	@api
	public static class yml_encode extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			CArray ca = Static.getArray(args[0], t);
			boolean prettyPrint = false;
			if(args.length == 2) {
				prettyPrint = ArgumentValidation.getBoolean(args[1], t);
			}
			DumperOptions options = new DumperOptions();
			if(prettyPrint) {
				options.setPrettyFlow(true);
				options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
			}
			Yaml yaml = new Yaml(options);
			try {
				return new CString(yaml.dump(Construct.GetPOJO(ca)), t);
			} catch (ClassCastException ex) {
				throw new CRECastException(ex.getMessage(), t);
			}
		}

		@Override
		public String getName() {
			return "yml_encode";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		@Override
		public String docs() {
			return "string {array, [prettyPrint]} Converts an array into a YML encoded string. Only associative arrays are supported."
					+ " prettyPrint defaults to false. Within the array, only primitives and arrays can be encoded.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}
	}

	@api
	public static class yml_decode extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREFormatException.class};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			String data = args[0].val();
			Yaml yaml = new Yaml();
			Object ret = null;
			Exception cause = null;
			try {
				ret = yaml.load(data);
			} catch (ScannerException | ParserException ex) {
				cause = ex;
			}
			if(!(ret instanceof Map) && !(ret instanceof Collection)) {
				throw new CREFormatException("Improperly formatted YML", t, cause);
			}
			return Construct.GetConstruct(ret);
		}

		@Override
		public String getName() {
			return "yml_decode";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "array {string} Takes a YML encoded string, and returns an associative array,"
					+ " depending on the contents of the YML string. If the YML string is improperly formatted,"
					+ " a FormatException is thrown.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}
	}

	@api
	public static class ini_encode extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREFormatException.class, CRECastException.class};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			Properties props = new Properties();
			CArray arr = Static.getArray(args[0], t);
			String comment = null;
			if(args.length == 2) {
				comment = args[1].val();
			}
			if(!arr.inAssociativeMode()) {
				throw new CRECastException("Expecting an associative array", t);
			}
			for(String key : arr.stringKeySet()) {
				Mixed c = arr.get(key, t);
				String val;
				if(c instanceof CNull) {
					val = "";
				} else if(c.isInstanceOf(CArray.TYPE)) {
					throw new CRECastException("Arrays cannot be encoded with ini_encode.", t);
				} else {
					val = c.val();
				}
				props.setProperty(key, val);
			}
			StringWriter writer = new StringWriter();
			try {
				props.store(writer, comment);
			} catch (IOException ex) {
				// Won't happen
			}
			return new CString(writer.toString(), t);
		}

		@Override
		public String getName() {
			return "ini_encode";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		@Override
		public String docs() {
			return "string {array, [comment]} Encodes an array into an INI format output. An associative array is expected, and"
					+ " a format exception is thrown if it is a normal array. The comment is optional, but if provided will"
					+ " be added to the header of the returned string. Inner arrays cannot be stored, and will"
					+ " throw a CastException if attempted. Nulls are encoded as an empty string,"
					+ " so when reading the value back in, the difference between '' and null is lost. All values are"
					+ " stored as strings, so if 1 is stored, it will be returned as a string '1'. This is a limitation"
					+ " of the ini format, as it is expected that the code that reads the ini knows what the type of the"
					+ " data is anticipated, not the data itself.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}

	}

	@api
	public static class ini_decode extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREFormatException.class};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			Properties props = new Properties();
			Reader reader = new StringReader(args[0].val());
			try {
				props.load(reader);
			} catch (IOException ex) {
				throw new CREFormatException(ex.getMessage(), t);
			}
			CArray arr = CArray.GetAssociativeArray(t);
			for(String key : props.stringPropertyNames()) {
				arr.set(key, props.getProperty(key));
			}
			return arr;
		}

		@Override
		public String getName() {
			return "ini_decode";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "array {string} Returns an array, given an INI format input. INI files are loosely defined"
					+ " as a set of key->value pairs, which lends itself to an associative array format. Key value"
					+ " pairs are denoted usually by a <code>key=value</code> format. The specific rules for"
					+ " decoding an INI file can be found [http://docs.oracle.com/javase/6/docs/api/java/util/Properties.html#load%28java.io.Reader%29 here]."
					+ " An associative array is returned. All values are"
					+ " stored as strings, so if 1 was stored, it will be returned as a string '1'. This is a limitation"
					+ " of the ini format, as it is expected that the code that reads the ini knows what the type of the"
					+ " data is anticipated, not the data itself. You can easily cast data that is expected to be numeric"
					+ " via the {{function|integer}} and {{function|double}} functions when reading in the data if exact types"
					+ " are truly needed. INI doesn't easily support non-string values, if that is needed, consider using"
					+ " {{function|json_encode}}/{{function|json_decode}} instead.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}

	}

	@api
	@noboilerplate
	public static class xml_read extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREFormatException.class};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			XMLDocument doc;
			try {
				doc = new XMLDocument(args[0].val());
			} catch (SAXException ex) {
				throw new CREFormatException("Malformed XML.", t, ex);
			}
			try {
				return Static.resolveConstruct(doc.getNode(args[1].val()), t);
			} catch (XPathExpressionException ex) {
				throw new CREFormatException(ex.getMessage(), t, ex);
			}
		}

		@Override
		public String getName() {
			return "xml_read";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "mixed {xml, xpath} Reads a field from some xml using an XPath address. The XPath address is assumed"
					+ " to be absolute, even if it doesn't start with a '/'.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}

	}

	//@api
	public static class xml_write extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public boolean isRestricted() {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public Boolean runAsync() {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public String getName() {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public Integer[] numArgs() {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public String docs() {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public Version since() {
			throw new UnsupportedOperationException("Not supported yet.");
		}

	}
}
