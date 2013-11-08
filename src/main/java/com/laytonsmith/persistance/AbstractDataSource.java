package com.laytonsmith.persistance;

import com.laytonsmith.PureUtilities.DaemonManager;
import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.annotations.datasource;
import com.laytonsmith.persistance.io.ConnectionMixin;
import com.laytonsmith.persistance.io.ConnectionMixinFactory;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

/**
 *
 * @author lsmith
 */
public abstract class AbstractDataSource implements DataSource {

	protected final URI uri;
	protected final Set<DataSourceModifier> modifiers = EnumSet.noneOf(DataSourceModifier.class);
	private Set<DataSourceModifier> invalidModifiers;
	private ConnectionMixin connectionMixin;
	private ConnectionMixinFactory.ConnectionMixinOptions mixinOptions;
	private boolean inTransaction = false;
			
	
	protected AbstractDataSource() {
		try {
			uri = new URI("");
		} catch (URISyntaxException ex) {
			throw new RuntimeException(ex);
		}
	}

	protected AbstractDataSource(URI uri, ConnectionMixinFactory.ConnectionMixinOptions mixinOptions) throws DataSourceException {
		this.uri = uri;
		this.mixinOptions = mixinOptions;
		setInvalidModifiers();
		DataSourceModifier[] implicit = this.implicitModifiers();
		if (implicit != null) {
			for (DataSourceModifier dsm : this.implicitModifiers()) {
				addModifier(dsm);
			}
		}
	}
	
	protected ConnectionMixin getConnectionMixin() throws DataSourceException{
		if(connectionMixin == null){
			connectionMixin = ConnectionMixinFactory.GetConnectionMixin(uri, modifiers, mixinOptions, getBlankDataModel());
		}
		return connectionMixin;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final String get(String[] key) throws DataSourceException {
		checkGet(key);
		return get0(key);
	}

	@Override
	public final void startTransaction(DaemonManager dm) {
		inTransaction = true;
		startTransaction0(dm);
	}

	@Override
	public final void stopTransaction(DaemonManager dm, boolean rollback) throws DataSourceException, IOException {
		inTransaction = false;
		stopTransaction0(dm, rollback);
	}
	
	/**
	 * Returns true if we are currently in a transaction. Inside of the call to
	 * stopTransaction, this will be false.
	 * @return 
	 */
	public boolean inTransaction(){
		return inTransaction;
	}
	
	protected abstract void startTransaction0(DaemonManager dm);
	
	protected abstract void stopTransaction0(DaemonManager dm, boolean rollback) throws DataSourceException, IOException;

	@Override
	public final boolean set(DaemonManager dm, String[] key, String value) throws ReadOnlyException, DataSourceException, IOException {
		checkSet(key);
		return set0(dm, key, value);
	}
	
	/**
	 * Subclasses should implement this, instead of set(), as our version of set() does some standard validation
	 * on the input.
	 * @param key
	 * @param value
	 * @return
	 * @throws ReadOnlyException
	 * @throws DataSourceException
	 * @throws IOException 
	 */
	protected abstract boolean set0(DaemonManager dm, String[] key, String value) throws ReadOnlyException, DataSourceException, IOException;
	
	/**
	 * Subclasses should implement this, instead of get(), as our version of get() does
	 * some standard validation on the input.
	 * @param key
	 * @return 
	 */
	protected abstract String get0(String[] key) throws DataSourceException;

	/**
	 * The default implementation of string simply walks through keySet, and
	 * manually joins the keys together. If an implementation can provide a
	 * more efficient method, this should be overridden.
	 *
	 * @return
	 */
	@Override
	public Set<String> stringKeySet() throws DataSourceException {
		Set<String> keys = new TreeSet<String>();
		for (String[] key : keySet()) {
			keys.add(StringUtils.Join(key, "."));
		}
		return keys;
	}

	@Override
	public Set<String[]> getNamespace(String[] namespace) throws DataSourceException {
		Set<String[]> list = new HashSet<String[]>();
		String ns = StringUtils.Join(namespace, ".");
		for (String key : stringKeySet()) {
			if ("".equals(ns) //Blank string; this means they want it to always match.
					|| key.matches(Pattern.quote(ns) + "(?:$|\\..*)")) {
				String[] split = key.split("\\.");
				list.add(split);
			}
		}
		return list;
	}

	private void setInvalidModifiers() {
		DataSourceModifier[] invalid = this.invalidModifiers();
		if (invalid == null) {
			return;
		}
		this.invalidModifiers = EnumSet.copyOf(Arrays.asList(invalid));
	}

	public final String getName() {
		return this.getClass().getAnnotation(datasource.class).value();
	}

	@Override
	public final void addModifier(DataSourceModifier modifier) {
		if (invalidModifiers != null && invalidModifiers.contains(modifier)) {
			return;
		}
		if (modifier == DataSourceModifier.HTTP || modifier == DataSourceModifier.HTTPS) {
			modifiers.add(DataSourceModifier.READONLY);
			modifiers.add(DataSourceModifier.ASYNC);
		}
		if (modifier == DataSourceModifier.SSH){
			modifiers.add(DataSourceModifier.ASYNC);
		}
		modifiers.add(modifier);
	}
		

	@Override
	public final boolean hasKey(String[] key) throws DataSourceException {
		checkGet(key);
		return hasKey0(key);
	}
	
	/**
	 * By default, returns true if the value stored is non-null. In general,
	 * if clearKey0 is overridden, this should be as well.
	 * @param key
	 * @return
	 * @throws DataSourceException 
	 */
	protected boolean hasKey0(String[] key) throws DataSourceException{
		return get(key) != null;
	}
	
	@Override
	public final void clearKey(DaemonManager dm, String [] key) throws ReadOnlyException, DataSourceException, IOException{
		checkSet(key);
		clearKey0(dm, key);
	}
	
	/**
	 * By default, setting the value to null should clear the value,
	 * but that can be overridden if a data source has a better method.
	 * @param key
	 * @throws ReadOnlyException
	 * @throws DataSourceException
	 * @throws IOException 
	 */
	protected void clearKey0(DaemonManager dm, String [] key) throws ReadOnlyException, DataSourceException, IOException{
		set(dm, key, null);		
	}

	/**
	 * This method checks for invalid or non-sensical combinations of
	 * modifiers, and throws an exception if any combinations exist that are
	 * strange.
	 *
	 * @throws DataSourceException
	 */
	public final void checkModifiers() throws DataSourceException {
		List<String> errors = new ArrayList();
		if (invalidModifiers != null) {
			for (DataSourceModifier dsm : invalidModifiers) {
				if (modifiers.contains(dsm)) {
					errors.add(uri.toString() + " contains the modifier " + dsm.getName() + ", which is not applicable. This will be ignored.");
				}
			}
		}
		if (modifiers.contains(DataSourceModifier.PRETTYPRINT) && modifiers.contains(DataSourceModifier.READONLY)) {
			errors.add(uri.toString() + " contains both prettyprint and readonly modifiers, which doesn't make sense, because we cannot write out the file; prettyprint will be ignored.");
			modifiers.remove(DataSourceModifier.PRETTYPRINT);
		}
		if ((modifiers.contains(DataSourceModifier.HTTP) || modifiers.contains(DataSourceModifier.HTTPS)) && modifiers.contains(DataSourceModifier.SSH)) {
			errors.add(uri.toString() + " contains both http(s) and ssh modifiers.");
		}
		if (modifiers.contains(DataSourceModifier.HTTP) && modifiers.contains(DataSourceModifier.HTTPS)) {
			errors.add(uri.toString() + " contains both http and https modifiers. Because these are mutually exclusive, this doesn't make sense, and https will be assumed.");
			modifiers.remove(DataSourceModifier.HTTP);
		}
		if (!errors.isEmpty()) {
			throw new DataSourceException(StringUtils.Join(errors, "\n"));
		}
	}

	@Override
	public final boolean hasModifier(DataSourceModifier modifier) {
		return modifiers.contains(modifier);
	}

	/**
	 * This method checks to see if a set operation should simply throw a
	 * ReadOnlyException based on the modifiers.
	 */
	private void checkSet(String [] key) throws ReadOnlyException {
		for(String namespace : key){
			if("_".equals(namespace)){
				throw new IllegalArgumentException("In the key \"" + StringUtils.Join(key, ".") + ", the namespace \"_\" is not allowed."
						+ " (Namespaces may contain an underscore, but may not be just an underscore.)");
			}
		}
		if (modifiers.contains(DataSourceModifier.READONLY)) {
			throw new ReadOnlyException();
		}
	}

	/**
	 * This method checks to see if get operations should re-populate at
	 * this time. If the data set is transient, it will do so.
	 */
	private void checkGet(String[] key) throws DataSourceException {
		for(String namespace : key){
			if("_".equals(namespace)){
				throw new IllegalArgumentException("In the key \"" + StringUtils.Join(key, ".") + ", the namespace \"_\" is not allowed."
						+ " (Namespaces may contain an underscore, but may not be just an underscore.)");
			}
		}
		if(this.getModifiers().contains(DataSource.DataSourceModifier.TRANSIENT)){
            this.populate();
        }
		if (hasModifier(DataSourceModifier.TRANSIENT)) {
			populate();
		}
	}

	@Override
	public final Set<DataSourceModifier> getModifiers() {
		return EnumSet.copyOf(modifiers);
	}
	
	/**
	 * Subclasses that need a certain type of file to be the "blank" version
	 * of a data model can override this. By default, null is
	 * returned.
	 *
	 * @return
	 */
	protected String getBlankDataModel() {
		return "";
	}
	
	@Override
	public String toString(){
		StringBuilder b = new StringBuilder();
		for(DataSourceModifier m : modifiers){
			b.append(m.getName().toLowerCase()).append(":");
		}
		b.append(uri.toString());
		return b.toString();
	}

	@Override
	public URL getSourceJar() {
		throw new UnsupportedOperationException("TODO: Not supported yet.");
	}
	
}
