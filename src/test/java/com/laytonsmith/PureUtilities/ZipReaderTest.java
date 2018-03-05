package com.laytonsmith.PureUtilities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URLDecoder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 *
 */
public class ZipReaderTest {

	private static File TestZip;
	private static File TestFile;
	private static File TestNestedZip;

	public ZipReaderTest() {
	}

	@BeforeClass
	public static void setUpClass() throws Exception {
		File parent = new File(URLDecoder.decode(ZipReaderTest.class.getResource("/test.zip").getFile(), "UTF-8"));
		TestZip = new File(parent, "file.txt");

		TestFile = new File(URLDecoder.decode(ZipReaderTest.class.getResource("/test.txt").getFile(), "UTF-8"));

		File nestedParent = new File(URLDecoder.decode(ZipReaderTest.class.getResource("/testNested.zip").getFile(), "UTF-8"));
		TestNestedZip = new File(nestedParent, "innerZip.zip" + File.separator + "test.txt");
	}

	//TODO: Nested reads may be easier than I'm trying to make it, but either way, this
	//is a deeper problem, and I don't care to support this just yet.
//    @Test
//    public void testNestedRead() throws IOException{
//        String contents = new ZipReader(TestNestedZip).getFileContents();
//        assertNotNull("Could not read contents!", contents);
//        assertEquals("Hello World!", contents.trim());
//    }
	@Test
	public void testStringRead() throws FileNotFoundException, IOException {
		// TODO review the generated test code and remove the default call to fail.        
		String contents = new ZipReader(TestZip).getFileContents();
		assertNotNull("Could not read contents!", contents);
		assertEquals("This is a file", contents.trim());
	}

	@Test
	public void testTrivialRead() throws IOException {
		String contents = new ZipReader(TestFile).getFileContents();
		assertNotNull("Could not read contents!", contents);
		assertEquals("Hello World!", contents.trim());
	}

	@Test(expected = FileNotFoundException.class)
	public void testNestedFileNotFound() throws IOException {
		new ZipReader(new File(TestZip.getParent(), "notAFile.txt")).getFileContents();
	}

	@Test
	public void testNestedNotAZip() {
		try {
			new ZipReader(new File(new File(TestZip.getParent(), "file.txt"), "file.txt")).getFileContents();
			fail("Wanted IOException, but none was thrown");
		} catch(IOException e) {
			//pass
		}
	}
}
