package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.PropertiesManager;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.PureUtilities.XMLDocument;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.arguments.ArgList;
import com.laytonsmith.core.arguments.Argument;
import com.laytonsmith.core.arguments.ArgumentBuilder;
import com.laytonsmith.core.arguments.Generic;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CBoolean;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.exceptions.MarshalException;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;
import com.laytonsmith.core.natives.annotations.NonNull;
import com.laytonsmith.core.natives.interfaces.Mixed;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.xpath.XPathExpressionException;
import org.xml.sax.SAXException;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

/**
 *
 * @author lsmith
 */
public class DataTransformations {

	public static String docs() {
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

		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
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
			return "Converts an array into a JSON encoded string. Both normal and associative arrays are supported.";
		}
		
		public Argument returnType() {
			return new Argument("The JSON encoded string", CString.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("The array to encode", CArray.class, "array")
					);
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

		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			String s = args[0].val();
			try {
				return Construct.json_decode(s, t);
			} catch (MarshalException ex) {
				throw new Exceptions.FormatException("The input JSON string is improperly formatted. Check your formatting and try again.", t, ex);
			}
		}

		public String getName() {
			return "json_decode";
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		public String docs() {
			return "Takes a JSON encoded string, and returns an array, either normal or associative,"
					+ " depending on the contents of the JSON string. If the JSON string is improperly formatted,"
					+ " a FormatException is thrown.";
		}
		
		public Argument returnType() {
			return new Argument("The array from the decoded JSON string", CArray.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("The JSON encoded string", CString.class, "string")
					);
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}

	@api
	public static class yml_encode extends AbstractFunction {

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException};
		}

		public boolean isRestricted() {
			return false;
		}

		public Boolean runAsync() {
			return null;
		}

		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			ArgList list = getBuilder().parse(args, this, t);
			CArray ca = list.get("array");
			boolean prettyPrint = list.getBoolean("prettyPrint", t);
			DumperOptions options = new DumperOptions();
			if (prettyPrint) {
				options.setPrettyFlow(true);
			}
			Yaml yaml = new Yaml(options);
			return new CString(yaml.dump(Construct.GetPOJO(ca)), t);
		}

		public String getName() {
			return "yml_encode";
		}

		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		public String docs() {
			return "Converts an array into a YML encoded string. Only associative arrays are supported.";
		}
		
		public Argument returnType() {
			return new Argument("The YML encoded string", CString.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("The array to encode", CString.class, "array"),
						new Argument("If true, the returned string will be \"pretty printed\".", CBoolean.class, "prettyPrint").setOptionalDefault(false).addAnnotation(new NonNull())
					);
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}

	@api
	public static class yml_decode extends AbstractFunction {

		public ExceptionType[] thrown() {
			return new ExceptionType[]{};
		}

		public boolean isRestricted() {
			return false;
		}

		public Boolean runAsync() {
			return null;
		}

		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			String data = args[0].val();
			Yaml yaml = new Yaml();
			Map<String, Object> map = (Map<String, Object>) yaml.load(data);
			return Construct.GetConstruct(map);
		}

		public String getName() {
			return "yml_decode";
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		public String docs() {
			return "Takes a YML encoded string, and returns an associative array,"
					+ " depending on the contents of the YML string. If the YML string is improperly formatted,"
					+ " a FormatException is thrown.";
		}
		
		public Argument returnType() {
			return new Argument("The array from the decoded YML string", CArray.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("The YML encoded string", CString.class, "string")
					);
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
	}
	
	@api
	public static class ini_encode extends AbstractFunction {

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.FormatException, ExceptionType.CastException};
		}

		public boolean isRestricted() {
			return false;
		}

		public Boolean runAsync() {
			return null;
		}

		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			Properties props = new Properties();
			CArray arr = Static.getArray(args[0], t);
			String comment = null;
			if(args.length == 2){
				comment = args[1].val();
			}
			if(!arr.inAssociativeMode()){
				throw new Exceptions.CastException("Expecting an associative array", t);
			}
			for(String key : arr.keySet()){
				props.setProperty(key, arr.get(key).val());
			}
			StringWriter writer = new StringWriter();
			try {
				props.store(writer, comment);
			} catch (IOException ex) {
				// Won't happen
			}
			return new CString(writer.toString(), t);
		}

		public String getName() {
			return "ini_encode";
		}

		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		public String docs() {
			return "Encodes an array into an INI format output. An associative array is expected, and"
					+ " a format exception is thrown if it is a normal array. The comment is optional, but if provided will"
					+ " be added to the header of the returned string. All values are toString'd before output, so things like"
					+ " arrays, if stored as a value, will not be returned as arrays.";
		}
		
		public Argument returnType() {
			return new Argument("", CString.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("", CArray.class, "array"),
						new Argument("", CString.class, "comment").setOptionalDefaultNull()
					);
		}

		public Version since() {
			return CHVersion.V3_3_1;
		}
		
	}
	
	@api
	public static class ini_decode extends AbstractFunction {

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.FormatException};
		}

		public boolean isRestricted() {
			return false;
		}

		public Boolean runAsync() {
			return null;
		}

		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			Properties props = new Properties();
			Reader reader = new StringReader(args[0].val());
			try {
				props.load(reader);
			} catch (IOException ex) {
				throw new Exceptions.FormatException(ex.getMessage(), t);
			}
			CArray arr = CArray.GetAssociativeArray(t);
			for(String key : props.stringPropertyNames()){
				arr.set(key, props.getProperty(key));
			}
			return arr;
		}

		public String getName() {
			return "ini_decode";
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		public String docs() {
			return "Returns an array, given an INI format input. INI files are loosely defined"
					+ " as a set of key->value pairs, which lends itself to an associative array format. Key value"
					+ " pairs are denoted usually by a <code>key=value</code> format. The specific rules for"
					+ " decoding an INI file can be found [http://docs.oracle.com/javase/6/docs/api/java/util/Properties.html#load%28java.io.Reader%29 here]."
					+ " An associative array is returned.";
		}
		
		public Argument returnType() {
			return new Argument("", CArray.class).setGenerics(new Generic(CString.class));
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("", CString.class, "string")
					);
		}

		public Version since() {
			return CHVersion.V3_3_1;
		}
	}
	
	@api
	public static class xml_read extends AbstractFunction {

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.FormatException};
		}

		public boolean isRestricted() {
			return false;
		}

		public Boolean runAsync() {
			return null;
		}

		public Construct exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			XMLDocument doc;
			try {
				doc = new XMLDocument(args[0].val());
			} catch (SAXException ex) {
				throw new Exceptions.FormatException("Malformed XML.", t, ex);
			}
			try {
				return Static.resolveConstruct(doc.getNode(args[1].val()), t);
			} catch (XPathExpressionException ex) {
				throw new Exceptions.FormatException(ex.getMessage(), t, ex);
			}
		}

		public String getName() {
			return "xml_read";
		}

		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		public String docs() {
			return "mixed {xml, xpath} Reads a field from some xml using an XPath address. The XPath address is assumed"
					+ " to be absolute, even if it doesn't start with a '/'.";
		}

		public Version since() {
			return CHVersion.V3_3_1;
		}

		public Argument returnType() {
			return Argument.AUTO;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
					new Argument("", CString.class, "xml"),
					new Argument("", CString.class, "xpath")
				);
		}
		
	}
	
	//@api
	public static class xml_write extends AbstractFunction {

		public ExceptionType[] thrown() {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		public boolean isRestricted() {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		public Boolean runAsync() {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		public Construct exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		public String getName() {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		public Integer[] numArgs() {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		public String docs() {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		public Version since() {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		public Argument returnType() {
			throw new UnsupportedOperationException("TODO: Not supported yet.");
		}

		public ArgumentBuilder arguments() {
			throw new UnsupportedOperationException("TODO: Not supported yet.");
		}
		
	}
}
