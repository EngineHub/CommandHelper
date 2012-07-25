package com.laytonsmith.persistance;

import com.laytonsmith.core.CHVersion;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.yaml.snakeyaml.Yaml;

/**
 *
 * @author lsmith
 */
@datasource("yml")
public class YMLDataSource extends StringDataSource{
    
    public YMLDataSource(URI uri) throws DataSourceException{
        super(uri);
    }

    private Object model;
    
    public String protocol() {
        return "yml";
    }

    public List<String> keySet() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String get(String [] key) {
        return getValue(new ArrayList<String>(Arrays.asList(key)), model);
    }
    
    private String getValue(List<String> keys, Object object){
        if(object == null){
            return null;
        }
        if(object instanceof Map){
            if(keys.isEmpty()){
                return null;
            }
            String key = keys.get(0);
            keys.remove(0);
            Map map = (Map)object;
            if(map.containsKey(key)){
                return getValue(keys, map.get(key));
            } else {
                return null;
            }
        }
        return object.toString();
    }

    public boolean set(String [] key, String value) throws ReadOnlyException {
        checkSet();
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public DataSourceModifier[] implicitModifiers() {
        return null;
    }

    public DataSourceModifier[] invalidModifiers() {
        return null;
    }

    public String docs() {
        return "YML {yml:///path/to/yml/file.yml} This type stores data in plain text,"
                + " in a yml file. Extremely simple to use, it is less scalable than"
                + " database driven solutions, and even the Serialized Persistance will"
                + " perform better. However, since it is stored in plain text, it is"
                + " easy to edit locally, with a plain text editor, or using other tools. ";
    }

    public CHVersion since() {
        return CHVersion.V3_3_1;
    }

    @Override
    protected void populateModel(String data) throws DataSourceException {
        Yaml yaml = new Yaml();
        model = yaml.load(data);
    }
    
}
