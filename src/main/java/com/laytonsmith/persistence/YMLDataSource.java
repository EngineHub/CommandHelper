package com.laytonsmith.persistence;

import com.laytonsmith.annotations.datasource;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.persistence.io.ConnectionMixinFactory;
import java.net.URI;
import java.util.Map;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

/**
 *
 *
 */
@datasource("yml")
public class YMLDataSource extends StringSerializableDataSource {

	private YMLDataSource() {

	}

	public YMLDataSource(URI uri, ConnectionMixinFactory.ConnectionMixinOptions options) throws DataSourceException {
		super(uri, options);
	}

	@Override
	public DataSourceModifier[] implicitModifiers() {
		return null;
	}

	@Override
	public DataSourceModifier[] invalidModifiers() {
		return null;
	}

	@Override
	public String docs() {
		return "YML {yml:///path/to/yml/file.yml} This type stores data in plain text,"
				+ " in a yml file. Extremely simple to use, it is less scalable than"
				+ " database driven solutions, and even the Serialized Persistence will"
				+ " perform better. However, since it is stored in plain text, it is"
				+ " easy to edit locally, with a plain text editor, or using other tools. ";
	}

	@Override
	public MSVersion since() {
		return MSVersion.V3_3_1;
	}

	@Override
	protected void populateModel(String data) throws DataSourceException {
		Yaml yaml = new Yaml();
		try {
			model = new DataSourceModel((Map<String, Object>) yaml.load(data));
		} catch (Exception e) {
			throw new DataSourceException("Could not load data source for " + uri + ": " + e.getMessage(), e);
		}
	}

	@Override
	protected String serializeModel() {
		DumperOptions options = new DumperOptions();
		if(hasModifier(DataSourceModifier.PRETTYPRINT)) {
			options.setPrettyFlow(true);
		}
		Yaml yaml = new Yaml(options);
		return yaml.dump(model.toMap());
	}

}
