package com.laytonsmith.persistance;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author lsmith
 */
public abstract class AbstractDataSource implements DataSource{

    protected final URI uri;
    protected final List<DataSourceModifier> modifiers = new ArrayList<DataSourceModifier>();
    
    protected AbstractDataSource(URI uri) throws DataSourceException{
        this.uri = uri;
        populate();
    }    
    
    public String getName() {
        return this.getClass().getAnnotation(datasource.class).value();
    }

    public void addModifier(DataSourceModifier modifier) {
        if(modifier == DataSourceModifier.HTTP || modifier == DataSourceModifier.HTTPS){
            modifiers.add(DataSourceModifier.READONLY);
        }
        modifiers.add(modifier);
    }

    /**
     * This method checks to see if a set operation should simply throw
     * a ReadOnlyException based on the modifiers.
     */
    protected void checkSet() throws ReadOnlyException {
        if(modifiers.contains(DataSourceModifier.READONLY)){
            throw new ReadOnlyException();
        }
    }

    public List<DataSourceModifier> getModifiers() {
        return new ArrayList<DataSourceModifier>(modifiers);
    }       
    
    
}
