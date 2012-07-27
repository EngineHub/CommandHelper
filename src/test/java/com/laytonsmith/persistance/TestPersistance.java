package com.laytonsmith.persistance;

import com.laytonsmith.PureUtilities.FileUtility;
import com.laytonsmith.PureUtilities.StringUtils;
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
import java.net.URI;
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
    
    //Dumb properties get loaded in different orders, which doesn't matter, but breaks the
    //string detection here.
//    @Test
//    public void testINI(){
//        assertEquals("a.b=value1\na.b.c2=value3\na.b.c1=value2\n", doOutput("ini://test.ini", testData));
//    }
    
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
    
    @Test
    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    public void testFilterExceptions(){
        try{
            new DataSourceFilter("$1alias=yml://blah$1.yml\na.*.(**)=$1alias\n");
            fail("Expected an exception when defining numeric alias");
        } catch(DataSourceException e){
            //Pass
        }
        try{
            new DataSourceFilter("$alias!=yml://blah$1.yml\na.*.(**)=$alias\n");
            fail("Expected an exception when putting bad characters in a filter");
        } catch(DataSourceException e){
            //Pass
        }
        try{
            new DataSourceFilter("$alias=yml://blah$1.yml\na.*.(**(=$alias\n");
            fail("Expected an exception when having two left parenthesis");
        } catch(DataSourceException e){
            //Pass
        }
        try{
            new DataSourceFilter("$alias=yml://blah$1.yml\na.*.(**))=$alias\n");
            fail("Expected an exception when having two right parenthesis");
        } catch(DataSourceException e){
            //Pass
        }
        try{
            new DataSourceFilter("$alias=yml://blah$1.yml\na.*.(**=$alias\n");
            fail("Expected an exception when having no end parenthesis");
        } catch(DataSourceException e){
            //Pass
        }
        try{
            new DataSourceFilter("$alias=yml://blah$1.yml\na.*.(**)=$aliasnope\n");
            fail("Expected an exception when using undefined alias");
        } catch(DataSourceException e){
            //Pass
        }
        try{
            new DataSourceFilter("$alias=yml://blah$2.yml\na.*.(**)=$alias\n");
            fail("Expected an exception when using too high a capture group");
        } catch(DataSourceException e){
            //Pass
        }
        try{
            new DataSourceFilter("$alias=yml://blah$1.yml\na.*=$alias\na.*=$alias\n");
            fail("Expected an exception when defining the same key twice");
        } catch(DataSourceException e){
            //Pass
        }
        try{
            new DataSourceFilter("$alias=!@#$%^&*()blah$1.yml\na.*.(**)=$alias\n");
            fail("Expected an exception when having an invalid uri");
        } catch(DataSourceException e){
            //Pass
        }
    }
    
    @Test
    public void testMatch1() throws DataSourceException{
        assertEquals("yml://test.yml", getConnection("a.b.c", "a.b.c=yml://test.yml", "a.b.c.d=yml://no.yml"));
    }
    
    @Test
    public void testMatch2() throws DataSourceException{
        assertEquals("yml://yes.yml", getConnection("a.b.c", "a.b.*=yml://yes.yml", "a.b.c.d=yml://no.yml"));
    }
    
    @Test
    public void testMatch3() throws DataSourceException{
        assertEquals("yml://yes.yml", getConnection("a.b.c.d", "a.b.**=yml://no.yml", "a.b.c.*=yml://yes.yml"));
        assertEquals("yml://yes.yml", getConnection("a.b.c.d", "a.b.c.*=yml://yes.yml", "a.b.**=yml://no.yml"));        
        assertEquals("yml://yes.yml", getConnection("a.b.c.d", "a.b.(c).(*)=yml://yes.yml", "a.b.**=yml://no.yml"));
    }
    
    @Test
    public void testMatchCapture1() throws DataSourceException{
        assertEquals("yml://yes.yml", getConnection("a.b.yes", "a.b.(*)=yml://$1.yml"));
    }
    
    public String getConnection(String key, String ... mapping) throws DataSourceException{
        return getConnection(key, StringUtils.Join(mapping, "\n"));
    }
    public String getConnection(String key, String mapping) throws DataSourceException{
        DataSourceFilter dsf = new DataSourceFilter(mapping);
        URI conn = dsf.getConnection(key);
        return conn==null?null:conn.toString();
    }
    
    
}
