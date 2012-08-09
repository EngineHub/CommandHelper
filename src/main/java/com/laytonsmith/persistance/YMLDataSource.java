package com.laytonsmith.persistance;

import com.laytonsmith.annotations.datasource;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.persistance.io.ConnectionMixinFactory;
import java.net.URI;
import java.util.Map;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

/**
 *
 * @author lsmith
 */
@datasource("yml")
public class YMLDataSource extends StringDataSource{
    
    public YMLDataSource(URI uri, ConnectionMixinFactory.ConnectionMixinOptions options) throws DataSourceException{
        super(uri, options);
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
        model = new DataSourceModel((Map<String, Object>)yaml.load(data));
    }

    @Override
    protected String serializeModel() {
        DumperOptions options = new DumperOptions();
        if(hasModifier(DataSourceModifier.PRETTYPRINT)){
            options.setPrettyFlow(true);
        }
        Yaml yaml = new Yaml(options);
        return yaml.dump(model.toMap());
    }
    
}
