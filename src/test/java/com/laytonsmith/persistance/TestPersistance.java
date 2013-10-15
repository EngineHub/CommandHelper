package com.laytonsmith.persistance;

import com.laytonsmith.PureUtilities.DaemonManager;
import com.laytonsmith.PureUtilities.Common.FileUtil;
import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.PureUtilities.Common.Misc;
import com.laytonsmith.PureUtilities.ZipReader;
import com.laytonsmith.persistance.io.ConnectionMixinFactory;
import com.laytonsmith.persistance.io.ReadWriteFileConnection;
import static com.laytonsmith.testing.StaticTest.*;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author lsmith
 */
public class TestPersistance {

	/**
	 * TODO: Need to test the following: 
	 * Ensuring correct behavior with hidden keys that conflict
	 */
	public TestPersistance() {
	}
	public Map<String[], String> testData = new HashMap<String[], String>();
	List<File> toDelete = new ArrayList<File>();
	ConnectionMixinFactory.ConnectionMixinOptions options;
	DaemonManager dm;

	@Before
	public void setUp() throws Exception {
		testData.put(new String[]{"a", "b"}, "value1");
		testData.put(new String[]{"a", "b", "c1"}, "value2");
		testData.put(new String[]{"a", "b", "c2"}, "value3");
		options = new ConnectionMixinFactory.ConnectionMixinOptions();
		options.setWorkingDirectory(new File("."));
		dm = new DaemonManager();
	}

	@After
	public void tearDown() {
		for (File f : toDelete) {
			f.delete();
		}
	}

	@Test
	public void testYML() {
		assertEquals("a:\n"
			+ "  b: {c1: value2, c2: value3, _: value1}\n", doOutput("yml://test.yml", testData));
	}

	@Test
	public void testYMLPretty() {
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
	public void testJSON() {
		assertEquals("{\"a\":{\"b\":{\"c1\":\"value2\",\"c2\":\"value3\",\"_\":\"value1\"}}}", doOutput("json://test.json", testData));
	}

	@Test
	@SuppressWarnings("ResultOfObjectAllocationIgnored")
	public void testFilterExceptions() throws URISyntaxException {
		try {
			new DataSourceFilter("$1alias=yml://blah.yml\na.*=$1alias\n", new URI(""));
			fail("Expected an exception when defining numeric alias");
		} catch (DataSourceException e) {
			//Pass
		}
		try {
			new DataSourceFilter("$alias!=yml://blah.yml\na.*.(**)=$alias\n", new URI(""));
			fail("Expected an exception when putting bad characters in a filter");
		} catch (DataSourceException e) {
			//Pass
		}
		try {
			new DataSourceFilter("$alias=yml://blah.yml\na.*.**=$aliasnope\n", new URI(""));
			fail("Expected an exception when using undefined alias");
		} catch (DataSourceException e) {
			//Pass
		}
		try {
			new DataSourceFilter("$alias=!@#$%^&*()blah$1.yml\na.*.**=$alias\n", new URI(""));
			fail("Expected an exception when having an invalid uri");
		} catch (DataSourceException e) {
			//Pass
		}
	}

	@Test
	public void testMatch1() throws Exception {
		assertEquals("yml://test.yml", getConnection("a.b.c", "a.b.c=yml://test.yml", "a.b.c.d=yml://no.yml"));
	}

	@Test
	public void testMatch2() throws Exception {
		assertEquals("yml://yes.yml", getConnection("a.b.c", "a.b.*=yml://yes.yml", "a.b.c.d=yml://no.yml"));
	}

	@Test
	public void testMatch3() throws Exception {
		assertEquals("yml://yes.yml", getConnection("a.b.c.d", "a.b.**=yml://no.yml", "a.b.c.*=yml://yes.yml"));
		assertEquals("yml://yes.yml", getConnection("a.b.c.d", "a.b.c.*=yml://yes.yml", "a.b.**=yml://no.yml"));
	}

	@Test
	public void testMultimatch1() throws Exception {
		assertEquals(getSet("default", "yml://yes.yml"), getConnections("a.b.c", "a.**=yml://yes.yml"));
	}

	@Test
	public void testMultimatch2() throws Exception {
		assertEquals(getSet("default", "yml://yes1.yml", "yml://yes2.yml"), getConnections("a.b.c", "a.**=yml://yes1.yml", "a.b.**=yml://yes2.yml", "b.**=yml://no.yml"));
	}

	@Test
	public void testHasValue() throws Exception {
		PersistanceNetwork network = new PersistanceNetwork("**=json://folder/default.json", new URI("default"), options);
		network.set(dm, new String[]{"key"}, "value");
		dm.waitForThreads();
		assertTrue(network.hasKey(new String[]{"key"}));
		deleteFiles("folder/");
	}

	@Test
	public void testClearValue1() throws Exception {
		PersistanceNetwork network = new PersistanceNetwork("**=json://folder/default.json", new URI("default"), options);
		network.set(dm, new String[]{"key"}, "value");
		network.set(dm, new String[]{"key2"}, "value");
		assertTrue(network.get(new String[]{"key"}).equals("value"));
		network.clearKey(dm, new String[]{"key"});
		dm.waitForThreads();
		assertFalse(network.hasKey(new String[]{"key"}));
		assertEquals("{\"key2\":\"value\"}", FileUtil.read(new File("folder/default.json")));
		deleteFiles("folder/");
	}
	
	@Test
	public void testNotTransient() throws Exception{
		PersistanceNetwork network = new PersistanceNetwork("**=json://folder/default.json", new URI("default"), options);
		network.set(dm, new String[]{"key"}, "value");
		dm.waitForThreads();
		assertEquals("value", network.get(new String[]{"key"}));
		FileUtil.write("{\"key\":\"nope\"}", new File("folder/default.json"));
		//This should be cached in memory
		assertEquals("value", network.get(new String[]{"key"}));
		deleteFiles("folder/");
	}
	
	@Test
	public void testTransient() throws Exception{
		PersistanceNetwork network = new PersistanceNetwork("**=transient:json://folder/default.json", new URI("default"), options);
		network.set(dm, new String[]{"key"}, "value1");
		dm.waitForThreads();
		assertEquals("value1", network.get(new String[]{"key"}));
		FileUtil.write("{\"key\":\"value2\"}", new File("folder/default.json"));
		//This should not be cached in memory
		assertEquals("value2", network.get(new String[]{"key"}));
		deleteFiles("folder/");
	}
	
	@Test
	public void testSer() throws Exception{
		//This is hard to test, since it's binary data. Instead, we just check for the file's existance, and to see if 
		//contains the key and value somewhere in the data
		PersistanceNetwork network = new PersistanceNetwork("**=ser://folder/default.ser", new URI("default"), options);
		network.set(dm, new String[]{"key"}, "value");
		dm.waitForThreads();
		String contents = FileUtil.read(new File("folder/default.ser"));
		assertTrue(contents.contains("value") && contents.contains("key") && contents.contains("java.util.HashMap"));
		deleteFiles("folder/");
	}
	
	@Test
	public void testConflictingKeys() throws Exception{
		//If two data sources have the same key, only one should be currently operated on.
		PersistanceNetwork network = new PersistanceNetwork("**=transient:json://folder/default.json\nkey.*=transient:json://folder/other.json\n", new URI("default"), options);
		FileUtil.write("{\"key\":{\"key\":\"value1\"}}", new File("folder/other.json"), true);
		FileUtil.write("{\"key\":{\"key\":\"nope\"}}", new File("folder/default.json"), true);
		assertEquals("value1", network.get(new String[]{"key", "key"}));
		deleteFiles("folder/");
	}
	
	@Test
	public void testSQLiteBasic() throws Exception{
		PersistanceNetwork network = new PersistanceNetwork("**=sqlite://folder/sqlite.db", new URI("default"), options);
		network.set(dm, new String[]{"key", "key"}, "value");
		dm.waitForThreads();
		assertEquals("value", network.get(new String[]{"key", "key"}));
		deleteFiles("folder/");
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testNamespaceWithUnderscore() throws Exception {
		PersistanceNetwork network = new PersistanceNetwork("**=sqlite://folder/sqlite.db", new URI("default"), options);
		try{
			network.set(dm, new String[]{"Bad", "_", "Key"}, "value");
			dm.waitForThreads();
		} finally {
			deleteFiles("folder/");
		}
	}
	
	@Test
	public void testMemoryDataSource() throws Exception{
		PersistanceNetwork network = new PersistanceNetwork("**=mem:default", new URI("default"), options);
		String[] key = new String[]{"a", "b"};
		network.set(dm, key, "value");
		dm.waitForThreads();
		assertEquals("value", network.get(key));
		assertFalse(network.hasKey(new String[]{"a"}));
		assertTrue(network.hasKey(key));
		network.clearKey(dm, key);
		dm.waitForThreads();
		assertFalse(network.hasKey(key));
		
		network.set(dm, key, "value");
		dm.waitForThreads();
		assertEquals("value", network.get(key));
		MemoryDataSource.ClearDatabases();
		assertFalse(network.hasKey(key));
	}
	
	@Test
	public void testGetValues() throws Exception {
		PersistanceNetwork network = new PersistanceNetwork("**=json://folder/persistance.json", new URI("default"), options);
		try{
			network.set(dm, new String[]{"t", "test1"}, "test");
			network.set(dm, new String[]{"t", "test2"}, "test");
			network.set(dm, new String[]{"t", "test3", "third"}, "test");
			dm.waitForThreads();
			Map<String[], String> list = network.getNamespace(new String[]{"t"});
			List<String> output = new ArrayList<String>();
			for(String[] key : list.keySet()){
				output.add(StringUtils.Join(key, ".") + ": " + list.get(key));
			}
			Collections.sort(output);
			String out = StringUtils.Join(output, ", ");
			assertEquals("t.test1: test, t.test2: test, t.test3.third: test", out);
		} finally {
			deleteFiles("folder/");
		}
	}

	public String doOutput(String uri, Map<String[], String> data) {
		try {
			DataSource ds = DataSourceFactory.GetDataSource(uri, options);
			if (ds instanceof StringSerializableDataSource) {
				StringSerializableDataSource sdc = (StringSerializableDataSource) ds;
				if (sdc.getConnectionMixin() instanceof ReadWriteFileConnection) {

					//It is a file based URI, so we can test this.
					for (String[] key : data.keySet()) {
						ds.set(dm, key, data.get(key));
					}
					dm.waitForThreads();
					File output = GetPrivate(sdc.getConnectionMixin(), "file", File.class);
					String out = FileUtil.read(output);
					output.delete();
					return out;
				} else {
					fail("Cannot test non-file based URIs with this method!");
					return null;
				}
			} else {
				fail("Cannot test non string based data sources with this method!");
				return null;
			}
		} catch (Exception ex) {
			fail(Misc.GetStacktrace(ex));
			return null;
		}
	}

	File getFileFromDataSource(DataSource ds) {
		if (ds instanceof StringSerializableDataSource) {
			Object output = GetPrivate(ds, "output", Object.class);
			if (output instanceof ZipReader) {
				//It is a file based URI, so we can test this.
				File outFile = ((ZipReader) output).getFile();
				return outFile;
			}
		}
		return null;
	}

	public static void deleteFiles(String... files) {
		for (String f : files) {
			FileUtil.recursiveDelete(new File(f));
		}
	}

	public String getConnection(String key, String... mapping) throws Exception {
		return getConnection(key, StringUtils.Join(mapping, "\n"));
	}

	public String getConnection(String key, String mapping) throws Exception {
		DataSourceFilter dsf = new DataSourceFilter(mapping, new URI(""));
		URI conn = dsf.getConnection(key);
		return conn == null ? null : conn.toString();
	}

	public SortedSet<String> getConnections(String key, String... mapping) throws Exception {
		DataSourceFilter dsf = new DataSourceFilter(StringUtils.Join(mapping, "\n"), new URI("default"));
		List<URI> uris = dsf.getAllConnections(key);
		SortedSet<String> set = new TreeSet<String>();
		for (URI uri : uris) {
			set.add(uri.toString());
		}
		return set;
	}

	public SortedSet<String> getSet(String... strings) {
		SortedSet<String> set = new TreeSet<String>();
		set.addAll(Arrays.asList(strings));
		return set;
	}

	public String stringifyMap(Map<String[], String> map) {
		SortedSet<String> append = new TreeSet<String>();
		for (String[] key : map.keySet()) {
			append.add(Arrays.toString(key) + "=" + map.get(key));
		}
		return "[" + StringUtils.Join(append, ", ") + "]";
	}
}
