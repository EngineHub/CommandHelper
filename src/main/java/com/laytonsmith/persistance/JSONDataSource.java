package com.laytonsmith.persistance;

import com.laytonsmith.annotations.datasource;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.persistance.io.ConnectionMixinFactory;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONValue;

/**
 *
 * @author lsmith
 */
@datasource("json")
public class JSONDataSource extends StringDataSource {
	
	private JSONDataSource() {
		
	}

	public JSONDataSource(URI uri, ConnectionMixinFactory.ConnectionMixinOptions options) throws DataSourceException {
		super(uri, options);
	}

	@Override
	protected void populateModel(String data) throws DataSourceException {
		try {
			Map map = (Map) JSONValue.parse(data);
			model = new DataSourceModel(map);
		} catch (ClassCastException e) {
			throw new DataSourceException("Could not cast value returned from JSON parser to a map!", e);
		}
	}

	@Override
	protected String serializeModel() {
		StringWriter writer = new StringWriter();
		try {
			JSONValue.writeJSONString(model.toMap(), writer);
		} catch (IOException ex) {
			//Won't ever happen
			Logger.getLogger(JSONDataSource.class.getName()).log(Level.SEVERE, null, ex);
		}
		return writer.toString();
	}

	public DataSourceModifier[] implicitModifiers() {
		return null;
	}

	public DataSourceModifier[] invalidModifiers() {
		return new DataSourceModifier[]{DataSourceModifier.PRETTYPRINT};
	}

	public String docs() {
		return "JSON {json:///path/to/file.json} This type stores data in JSON"
			+ " format. All the pros and cons of yml apply here, but instead"
			+ " of using the yml style to store the data, values are stored"
			+ " in a JSON medium. The JSON will be an array, where each"
			+ " namespace is its own array or value, so 'name.of.key'"
			+ " = 'value' would be stored as such:"
			+ " {\"name\":{\"of\":{\"key\":\"value\"}}}. Due to lack of"
			+ " support for pretty printing in the json library currently used,"
			+ " prettyprint is unsupported, however it is intended to be"
			+ " supported in the future.";
	}

	@Override
	protected String getBlankDataModel() {
		return "{}";
	}

	public CHVersion since() {
		return CHVersion.V3_3_1;
	}
}
