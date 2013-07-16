package com.laytonsmith.persistance;

import com.laytonsmith.core.CHVersion;
import com.laytonsmith.persistance.io.ConnectionMixinFactory;
import java.net.URI;

/**
 *
 * @author lsmith
 */
//@datasource("csv")
public class CSVDataSource extends StringSerializableDataSource {
	
	private CSVDataSource() {
		
	}
	
	public CSVDataSource(URI uri, ConnectionMixinFactory.ConnectionMixinOptions options) throws DataSourceException{
		super(uri, options);
	}

	@Override
	protected void populateModel(String data) throws DataSourceException {
		
	}

	@Override
	protected String serializeModel() {
		return "";
	}

	@Override
	public DataSourceModifier[] implicitModifiers() {
		return null;
	}

	@Override
	public DataSourceModifier[] invalidModifiers() {
		return null;
	}

	public String docs() {
		return "CSV {csv:///path/to/csv/file.csv} This type stores data"
			+ " in a CSV format. All the pros and cons of yml apply"
			+ " here, but instead of using the yml style to store the"
			+ " data, values are stored as a CSV file. The CSV file"
			+ " must have exactly two entries per line, the key, then"
			+ " the value, then a newline.";
	}

	public CHVersion since() {
		return CHVersion.V0_0_0;
	}
	
}
