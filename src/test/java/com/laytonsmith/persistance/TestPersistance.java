package com.laytonsmith.persistance;

import com.laytonsmith.PureUtilities.FileUtility;
import com.laytonsmith.PureUtilities.Util;
import com.laytonsmith.PureUtilities.ZipReader;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Before;
import org.junit.Test;

import static com.laytonsmith.testing.StaticTest.*;
import java.io.File;
import static org.junit.Assert.*;

/**
 *
 * @author lsmith
 */
public class TestPersistance {
    
    public TestPersistance(){}
    
    public Map<String[], String> testData = new HashMap<String[], String>();
    @Before
    public void setUp(){
        testData.put(new String[]{"a", "b"}, "value1");
        testData.put(new String[]{"a", "b", "c1"}, "value2");
        testData.put(new String[]{"a", "b", "c2"}, "value3");
    }
    
    @Test
    public void testYML(){
        assertEquals("a:\n" +
                     "  b: {c1: value2, c2: value3, _: value1}\n", doOutput("yml://test.yml", testData));
    }
    
    @Test
    public void testYMLPretty(){
        assertEquals("a:\n"
                + "  b: {\n"
                + "    c1: value2,\n"
                + "    c2: value3,\n"
                + "    _: value1\n"
                + "  }\n", doOutput("prettyprint:yml://testpretty.yml", testData));
    }
    
    @Test
    public void testINI(){
        assertEquals("a.b=value1\na.b.c2=value3\na.b.c1=value2\n", doOutput("ini://test.ini", testData));
    }
    
    @Test
    public void testJSON(){
        assertEquals("{\"a\":{\"b\":{\"c1\":\"value2\",\"c2\":\"value3\",\"_\":\"value1\"}}}\n", doOutput("json://test.json", testData));
    }
    
    public String doOutput(String uri, Map<String[], String> data){
        try {
            DataSource ds = DataSourceFactory.GetDataSource(uri);
            if(ds instanceof StringDataSource){
                Object output = GetPrivate(ds, "output", Object.class);
                if(output instanceof ZipReader){
                    //It is a file based URI, so we can test this.
                    File outFile = ((ZipReader)output).getFile();
                    for(String[] key : data.keySet()){
                        ds.set(key, data.get(key));
                    }
                    String out = FileUtility.read(outFile);
                    outFile.delete();
                    return out;
                } else {
                    fail("Cannot test non-file based URIs with this method!");
                    return null;
                }
            } else {
                fail("Cannot test non string based data sources with this method!");
                return null;
            }
        }
        catch (Exception ex) {
            fail(Util.GetStacktrace(ex));
            return null;
        }
    }
    
    
}
