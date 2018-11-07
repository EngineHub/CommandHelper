package com.laytonsmith.PureUtilities.VirtualFS;

import com.laytonsmith.PureUtilities.Common.FileUtil;
import com.sun.istack.internal.logging.Logger;
import java.io.File;
import java.io.IOException;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 *
 */
public class VirtualFSTest {

	public VirtualFSTest() {
	}

	static final File ROOT = new File("./VirtualFS");
	static final File EXTERNAL = new File("./OutsideVFS");
	static final File SETTINGS_FILE = new File(ROOT, ".vfsmeta/settings.yml");

	@BeforeClass
	public static void setUpClass() {
		ROOT.mkdirs();
		EXTERNAL.mkdirs();
	}

	@AfterClass
	public static void tearDownClass() {
		FileUtil.recursiveDelete(ROOT);
		FileUtil.recursiveDelete(EXTERNAL);
		assertFalse(ROOT + " was not deleted!", ROOT.exists());
		assertFalse(EXTERNAL + " was not deleted!", EXTERNAL.exists());
	}

	@Before
	public void setUp() {
	}

	@After
	public void tearDown() {
	}

	/**
	 * Convenience method to write the given settings to file.
	 *
	 * @param settings
	 */
	private void writeSettings(String settings) throws IOException {
		FileUtil.write(settings, SETTINGS_FILE);
	}

	private VirtualFileSystem setupVFS(String settings) throws IOException {
		writeSettings(settings);
		return new VirtualFileSystem(ROOT, new VirtualFileSystemSettings(SETTINGS_FILE));
	}

	/**
	 * This test sees if the VFS starts up correctly, that is, the file system is created with all the appropriate meta
	 * files.
	 *
	 * @throws Exception
	 */
	@Test
	@SuppressWarnings("ResultOfObjectAllocationIgnored")
	public void virtualFSSetup() throws Exception {
		String settingsString = "'**': {\n"
				+ "  hidden: true,\n"
				+ "  readonly: true\n"
				+ "}\n";
		writeSettings(settingsString);
		new VirtualFileSystem(ROOT, new VirtualFileSystemSettings(SETTINGS_FILE));
		assertTrue(FileUtil.read(SETTINGS_FILE).contains(VirtualFileSystemSettings.getDefaultSettingsString()));
		assertTrue(FileUtil.read(SETTINGS_FILE).contains(settingsString));
	}

	/**
	 * This simple test sees if a file writes to the appropriate location, and can be re-read with a read call.
	 *
	 * @throws Exception
	 */
	@Test
	@Ignore("TODO")
	public void testWriteReadWithNewFile() throws Exception {
		String fileText = "This is the text in the file";
		VirtualFileSystem vfs = new VirtualFileSystem(ROOT, null);
		String fname = "testWriteReadWithNewFile.txt";
		VirtualFile vf = new VirtualFile("/" + fname);
		File realFile = new File(ROOT, fname);
		vfs.writeUTFString(vf, fileText);
		assertEquals(fileText, FileUtil.read(realFile));
		assertEquals(fileText, vfs.readUTFString(vf));
	}

	private void testGlob(VirtualGlob glob, boolean expectMatch, VirtualFile... files) {
		for(VirtualFile f : files) {
			assertEquals(expectMatch, glob.matches(f));
		}
	}

	@Test
	@Ignore
	public void testGlobbingWorks() throws Exception {
		VirtualFile v1 = new VirtualFile("/top.txt");
		VirtualFile v1_2 = new VirtualFile("/top.txtt");
		VirtualFile v1_3 = new VirtualFile("/top.tx");
		VirtualFile v2 = new VirtualFile("/top.ms");
		VirtualFile v3 = new VirtualFile("/dir/middle.txt");
		VirtualFile v4 = new VirtualFile("/dir/middle.ms");
		VirtualFile v4_2 = new VirtualFile("/dir2/test.txt");
		VirtualFile v4_3 = new VirtualFile("/dir3/test.txt");
		VirtualFile v5 = new VirtualFile("/dir/dir/bottom.txt");
		VirtualFile v5_2 = new VirtualFile("/dir/dir/test.txt");
		VirtualFile v6 = new VirtualFile("/dir/dir/bottom.ms");

		VirtualGlob glob1 = new VirtualGlob("**");
		VirtualGlob glob2 = new VirtualGlob("**.ms");
		VirtualGlob glob3 = new VirtualGlob("/top.txtt?");
		VirtualGlob glob4 = new VirtualGlob("/*/test.txt");
		VirtualGlob glob5 = new VirtualGlob("/**/test.txt");

		testGlob(glob1, true, v1, v2, v3, v4, v5, v6);

		testGlob(glob2, true, v2, v4, v6);
		testGlob(glob2, false, v1, v3, v5);

		testGlob(glob3, true, v1);
		testGlob(glob3, false, v2);

		testGlob(glob3, true, v1, v1_2);
		testGlob(glob3, false, v1_3);

		testGlob(glob4, true, v4_2, v4_3);
		testGlob(glob4, false, v5_2);

		testGlob(glob5, true, v4_2, v4_3, v5_2);
		testGlob(glob5, false, v1, v2, v3, v4, v5, v6);
	}

	@Test
	@Ignore("TODO")
	public void testCordonedOffIsGlobal() throws Exception {
		VirtualFileSystemSettings s = new VirtualFileSystemSettings("'**': {\n  cordoned-off: true\n}\n");
		assertTrue(s.isCordonedOff());
		VirtualFileSystemSettings s2 = new VirtualFileSystemSettings("");
		assertFalse(s2.isCordonedOff());

		try {
			VirtualFileSystemSettings s3 = new VirtualFileSystemSettings("'directory': {\n  cordoned-off: true\n}\n");
			fail();
		} catch (IllegalArgumentException ex) {
			// pass
		}
	}

	/**
	 * This test sees if a file that is created from an outside process cannot be read, since it is not in the manifest
	 *
	 * @throws Exception
	 */
	@Test
	@Ignore("TODO")
	public void testCordonedFileNotFound() throws Exception {
		String settingsString = "'**': {\n"
				+ "  cordoned-off: true\n"
				+ "}\n";
		VirtualFileSystem s = setupVFS(settingsString);
		String fn = "testCordonedFileNotFound";
		File real = new File(ROOT, fn);
		real.createNewFile();
		assertTrue(real.exists());
		VirtualFile virtual = new VirtualFile("/" + fn);
		assertFalse(s.exists(virtual));
	}

	/**
	 * This test sees if writing to a file that was created from an outside process fails, since it is not in the
	 * manifest.
	 *
	 * @throws Exception
	 */
	@Test
	@Ignore
	public void testCordonedOffTryToWriteOverExternalFile() throws Exception {
		// Required for minimum product
	}

	/**
	 * This tests the trivial case where a new file is successfully created in an empty spot.
	 *
	 * @throws Exception
	 */
	@Test
	@Ignore
	public void testCordonedOffNewFileCreation() throws Exception {
		// Required for minimum product
	}

	/**
	 * This ensures that reads and writes to the meta directory always fail.
	 *
	 * @throws Exception
	 */
	@Test
	@Ignore
	public void testMetaFileReadWriteFails() throws Exception {
		// Required for minimum product
	}

	/**
	 * This test verifies that reading and writing above the file system will not work.
	 *
	 * @throws Exception
	 */
	@Test
	@Ignore
	public void testReadWriteAboveFSFails() throws Exception {
		// Required for minimum product
	}

	/**
	 * This ensures that symlinks work with plain local files.
	 *
	 * @throws Exception
	 */
	@Test
	@Ignore
	public void testSymlink() throws Exception {

	}

	/**
	 * This ensures that symlinks to a remote file system work.
	 *
	 * @throws Exception
	 */
	@Test
	@Ignore
	public void testSSHSymlink() throws Exception {

	}

	/**
	 * This ensures that listing files works properly
	 *
	 * @throws Exception
	 */
	@Test
	@Ignore
	public void testList() throws Exception {
		// Required for minimum product
	}

	/**
	 * This ensures file deletion works.
	 *
	 * @throws Exception
	 */
	@Test
	@Ignore
	public void testDelete() throws Exception {
		// Required for minimum product
	}

	/**
	 * Ensures file existance checking works.
	 *
	 * @throws Exception
	 */
	@Test
	@Ignore
	public void testExists() throws Exception {
		// Required for minimum product
	}

	/**
	 * Tests isAbsolute
	 *
	 * @throws Exception
	 */
	@Test
	@Ignore
	public void testIsAbsolute() throws Exception {

	}

	/**
	 * Tests isDirectory
	 *
	 * @throws Exception
	 */
	@Test
	@Ignore
	public void testIsDirectory() throws Exception {

	}

	/**
	 * Tests isFile
	 *
	 * @throws Exception
	 */
	@Test
	@Ignore
	public void testIsFile() throws Exception {

	}

	/**
	 * Tests mkdirs
	 *
	 * @throws Exception
	 */
	@Test
	@Ignore
	public void testMkDirs() throws Exception {

	}

	/**
	 * Tests mkdir
	 *
	 * @throws Exception
	 */
	@Test
	@Ignore
	public void testMkDir() throws Exception {
		// Required for minimum product
	}

	/**
	 * Tests creating an empty file
	 *
	 * @throws Exception
	 */
	@Test
	@Ignore
	public void testCreateEmptyFile() throws Exception {

	}

	/**
	 * Tests creating a temp file.
	 *
	 * @throws Exception
	 */
	@Test
	@Ignore
	public void testCreateTmpFile() throws Exception {

	}

	/**
	 * Ensures that a folder that is hidden will not show up
	 *
	 * @throws Exception
	 */
	@Test
	@Ignore
	public void testHiddenFileNotShowing() throws Exception {

	}

	/**
	 * Ensures that quotas cannot be exceeded
	 *
	 * @throws Exception
	 */
	@Test
	@Ignore
	public void testQuota() throws Exception {

	}

	/**
	 * Ensures that a read only file cannot be written to
	 *
	 * @throws Exception
	 */
	@Test
	@Ignore
	public void testReadOnly() throws Exception {
		// Required for minimum product
	}

	/**
	 * Ensures that folder depth cannot be exceeded if a restriction is in place.
	 *
	 * @throws Exception
	 */
	@Test
	@Ignore
	public void testFolderDepth() throws Exception {

	}

	/**
	 * Ensures globs match correctly
	 *
	 * @throws Exception
	 */
	@Test
	@Ignore
	public void testGlobMatching() throws Exception {

	}

	/**
	 * Tests basic symlinking, map one file to another.
	 *
	 * @throws Exception
	 */
	@Test
	@Ignore
	public void testBasicSymlink() throws Exception {
		// Required for minimum product
	}

	/**
	 * This test ensures that a symlink to deeper in the file system works, and it also tests to make sure that it can
	 * in fact go up past the "root" of the symlink, as long as ultimately it stays inside the file system.
	 *
	 * @throws Exception
	 */
	@Test
	@Ignore
	public void testSymlinkDown() throws Exception {
		// Required for minimum product
	}

	/**
	 * This test ensures that a symlink to a folder completely out of the file system works, and also ensures that it
	 * can't go up past the "root" of the symlink at all.
	 *
	 * @throws Exception
	 */
	@Test
	@Ignore
	public void testSymlinkOther() throws Exception {
		// Required for minimum product
	}

}
