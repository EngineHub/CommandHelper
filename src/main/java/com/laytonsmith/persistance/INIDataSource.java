package com.laytonsmith.persistance;

import com.laytonsmith.PureUtilities.Pair;
import com.laytonsmith.PureUtilities.StringUtils;
import com.laytonsmith.annotations.datasource;
import com.laytonsmith.core.CHVersion;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * 
 * @author lsmith
 */
@datasource("ini")
public class INIDataSource extends StringDataSource {

    public INIDataSource(URI uri) throws DataSourceException{
        super(uri);
    }
    
    @Override
    protected void populateModel(String data) throws DataSourceException {
        Properties props = new Properties();
        try {
            props.load(new StringReader(data));
        } catch (IOException ex) {
            //Won't ever happen, but sure.
            throw new DataSourceException(null, ex);
        }
        List<Pair<String, String>> list = new ArrayList<Pair<String, String>>();
        for(String key : props.stringPropertyNames()){
            list.add(new Pair<String, String>(key, props.getProperty(key)));
        }
        model = new DataSourceModel(list);
    }

    @Override
    protected String serializeModel() {
        StringBuilder b = new StringBuilder();        
        for(String [] key : model.keySet()){
            b.append(StringUtils.Join(key, ".")).append("=").append(model.get(key)).append("\n");
        }
        return b.toString();
    }

    public DataSourceModifier[] implicitModifiers() {
        return null;
    }

    public DataSourceModifier[] invalidModifiers() {
        return new DataSourceModifier[]{DataSourceModifier.PRETTYPRINT};
    }

    public String docs() {
        return "INI {ini:///path/to/ini/file.ini} This type stores data in plain"
                + " text, in a ini style. All the pros and cons of yml apply here,"
                + " but instead of using the yml style to store the data, values"
                + " are stored with key=value\\n signatures. Pretty print is not supported,"
                + " since whitespace is relevant to the meta information.";
    }

    public CHVersion since() {
        return CHVersion.V3_3_1;
    }
    
}
