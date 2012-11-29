package com.laytonsmith.persistance;

import com.laytonsmith.persistance.io.ConnectionMixinFactory;
import java.io.IOException;
import java.net.URI;
import java.util.Set;

/**
 * For data sources that can input and output strings as the complete
 * data model, this class should be extended. The data source may not
 * be written to file, but it is for sure going to be stored (or at least retrievable)
 * from a UTF-8 encoded string.
 *
 * @author lsmith
 */
public abstract class StringSerializableDataSource extends AbstractDataSource {
	/**
	 * A reference to the DataSourceModel used by the set and get methods.
	 */
	protected DataSourceModel model;
	
	protected StringSerializableDataSource(){
		
	}

	protected StringSerializableDataSource(URI uri, ConnectionMixinFactory.ConnectionMixinOptions options) throws DataSourceException {
		super(uri, options);
	}

	/**
	 * Writes the stringified data to whatever output is associated with
	 * this data source.
	 *
	 * @throws IOException
	 */
	protected void writeData(String data) throws IOException, ReadOnlyException, DataSourceException {
		if (modifiers.contains(DataSourceModifier.READONLY)) {
			throw new ReadOnlyException();
		}
		getConnectionMixin().writeData(data);
	}

	@Override
	protected void clearKey0(String[] key) throws DataSourceException, ReadOnlyException, IOException {
		model.clearKey(key);
		writeData(serializeModel());
	}

	public void populate() throws DataSourceException {
		String data;
		try {
			data = getConnectionMixin().getData();
		} catch (Exception e) {
			throw new DataSourceException("Could not populate the data source with data", e);
		}
		populateModel(data);
	}

	public Set<String[]> keySet() {
		return model.keySet();
	}

	protected final String get0(String[] key, boolean bypassTransient) throws DataSourceException {
		return model.get(key);
	}

	protected final boolean set0(String[] key, String value) throws ReadOnlyException, IOException, DataSourceException {
		String old = get(key, false);
		if ((old == null && value == null) || (old != null && old.equals(value))) {
			return false;
		}
		model.set(key, value);
		//We need to output the model now
		writeData(serializeModel());
		return true;
	}

	/**
	 * Given some data retrieved from who knows where, populate the model.
	 *
	 * @param data
	 * @throws Exception
	 */
	protected abstract void populateModel(String data) throws DataSourceException;

	/**
	 * Serializes the underlying model to a string, which can be written out
	 * to disk/network
	 *
	 * @return
	 */
	protected abstract String serializeModel();

	/**
	 * Subclasses that need a certain type of file to be the "blank" version
	 * of a data model can override this. By default, an empty string is
	 * returned.
	 *
	 * @return
	 */
	@Override
	protected String getBlankDataModel() {
		return "";
	}
}
