package com.laytonsmith.persistence.io;

import com.laytonsmith.persistence.DataSource;
import com.laytonsmith.persistence.DataSourceException;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Objects;
import java.util.Set;

/**
 * A ConnectionMixin class dictates how a data source connects to its data.
 * This can vary depending on the URI, and so this class grabs the appropriate
 * mixin based on the original URI.
 * 
 */
public class ConnectionMixinFactory {
	private ConnectionMixinFactory(){}
	
	public static class ConnectionMixinOptions{
		File workingDirectory = null;
		/**
		 * In the case of file based connections, this is the working
		 * directory, that is, the "." directory used to resolve
		 * relative paths.
		 * @param workingDirectory 
		 */
		public void setWorkingDirectory(File workingDirectory){
			this.workingDirectory = workingDirectory;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			final ConnectionMixinOptions other = (ConnectionMixinOptions) obj;
			if (!Objects.equals(this.workingDirectory, other.workingDirectory)) {
				return false;
			}
			return true;
		}

		@Override
		public int hashCode() {
			int hash = 7;
			hash = 41 * hash + Objects.hashCode(this.workingDirectory);
			return hash;
		}
		
		
		
	}
	
	/**
	 * A ConnectionMixin class dictates how a data source connects to its data.
	 * This can vary depending on the URI, and so this class grabs the appropriate
	 * mixin based on the original URI. It isn't always the case that a connection mixin
	 * will exist for this URI, if the data source implicitely provides it's own
	 * connection, it won't need this. This is generally the case for non-string
	 * based connections. If the data source does provide it's own connection information,
	 * this should be ignored, because it will probably not return the correct type
	 * anyways.
	 * @param uri
	 * @param modifiers
	 * @return 
	 */
	public static ConnectionMixin GetConnectionMixin(URI uri, Set<DataSource.DataSourceModifier> modifiers, ConnectionMixinOptions options, String blankDataModel) throws DataSourceException{
		if(modifiers.contains(DataSource.DataSourceModifier.HTTP) || modifiers.contains(DataSource.DataSourceModifier.HTTPS)){
			try {
				//This is a WebConnection
				return new WebConnection(uri, modifiers.contains(DataSource.DataSourceModifier.HTTP)?false:true);
			} catch (MalformedURLException ex) {
				throw new DataSourceException("Malformed URL.", ex);
			}
		} else if(modifiers.contains(DataSource.DataSourceModifier.SSH)){
			//This is an SSHConnection
			return new SSHConnection(uri);
		} else {
			//Else it's a file connection, or null, but we will go ahead
			//and assume it's file.
			try {
				if(modifiers.contains(DataSource.DataSourceModifier.READONLY)){
					return new ReadOnlyFileConnection(uri, options.workingDirectory, blankDataModel);
				} else {
					return new ReadWriteFileConnection(uri, options.workingDirectory, blankDataModel);
				}
			} catch (IOException ex) {
				throw new DataSourceException("IOException: " + ex.getMessage(), ex);
			}
		}
	}
}
