package com.laytonsmith.PureUtilities.VirtualFS;

import com.laytonsmith.PureUtilities.Common.FileUtil;
import java.io.File;
import java.io.IOException;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author lsmith
 */
public class VirtualFSTest {
	public VirtualFSTest() {
	}
	
	static final File root = new File("./VirtualFS");
	static final File settingsFile = new File(root, ".vfsmeta/settings.yml");
	@BeforeClass
	public static void setUpClass() {
		root.mkdirs();
	}
	
	@AfterClass
	public static void tearDownClass() {
		FileUtil.recursiveDelete(root);
		assertFalse(root.exists());
	}
	
	@Before
	public void setUp() {
	}
	
	@After
	public void tearDown() {
	}
	
	/**
	 * Convenience method to write the given settings to file.
	 * @param settings 
	 */
	private void writeSettings(String settings) throws IOException{
		FileUtil.write(settings, settingsFile);
	}
	
	/**
	 * This test sees if the VFS starts up correctly, that is,
	 * the file system is created with all the appropriate meta
	 * files.
	 * @throws Exception 
	 */
	@Test
	@SuppressWarnings("ResultOfObjectAllocationIgnored")
	public void virtualFSSetup() throws Exception{
		String settingsString = "'**': {\n"
				+ "  hidden: true,\n"
				+ "  readonly: true\n"
				+ "}\n";
		writeSettings(settingsString);
		new VirtualFileSystem(root, new VirtualFileSystemSettings(settingsFile));
		assertTrue(FileUtil.read(settingsFile).contains(VirtualFileSystemSettings.getDefaultSettingsString()));
		assertTrue(FileUtil.read(settingsFile).contains(settingsString));
	}
	
	/**
	 * This simple test sees if a file writes to the appropriate
	 * location, and can be re-read with a read call.
	 * @throws Exception 
	 */
	@Test
	public void testWriteReadWithNewFile() throws Exception{
		String fileText = "This is the text in the file";
		VirtualFileSystem vfs = new VirtualFileSystem(root, null);
		String fname = "testWriteReadWithNewFile.txt";
		VirtualFile vf = new VirtualFile("/" + fname);
		File realFile = new File(root, fname);
		vfs.writeUTFString(vf, fileText);
		assertEquals(fileText, FileUtil.read(realFile));
		assertEquals(fileText, vfs.readUTFString(vf));
	}
	
	/**
	 * This test sees if a file that is created from an outside process
	 * cannot be read, since it is not in the manifest
	 * @throws Exception 
	 */
	@Test
	public void testCordonedFileNotFound() throws Exception{
		
	}
	
	/**
	 * This test sees if writing to a file that was created from an outside
	 * process fails, since it is not in the manifest.
	 * @throws Exception 
	 */
	@Test
	public void testCordonedOffTryToWriteOverExternalFile() throws Exception{
		
	}
	
	/**
	 * This tests the trivial case where a new file is successfully
	 * created in an empty spot.
	 * @throws Exception 
	 */
	@Test
	public void testCordonedOffNewFileCreation() throws Exception{
		
	}
	
	/**
	 * This ensures that reads and writes to the meta directory always fail.
	 * @throws Exception 
	 */
	@Test
	public void testMetaFileReadWriteFails() throws Exception{
		
	}
	
	/**
	 * This test verifies that reading and writing above the file
	 * system will not work.
	 * @throws Exception 
	 */
	@Test
	public void testReadWriteAboveFSFails() throws Exception{
		
	}
	
	/**
	 * This ensures that symlinks work with plain local files.
	 * @throws Exception 
	 */
	@Test
	public void testSymlink() throws Exception{
		
	}
	
	/**
	 * This ensures that symlinks to a remote file system work.
	 * @throws Exception 
	 */
	@Test
	public void testSSHSymlink() throws Exception{
		
	}
	
	/**
	 * This ensures that listing files works properly
	 * @throws Exception 
	 */
	@Test
	public void testList() throws Exception{
		
	}
	
	/**
	 * This ensures file deletion works.
	 * @throws Exception 
	 */
	@Test
	public void testDelete() throws Exception{
		
	}
	
	/**
	 * Ensures file existance checking works.
	 * @throws Exception 
	 */
	@Test
	public void testExists() throws Exception{
		
	}
	
	/**
	 * Tests isAbsolute
	 * @throws Exception 
	 */
	@Test
	public void testIsAbsolute() throws Exception{
		
	}
	
	/**
	 * Tests isDirectory
	 * @throws Exception 
	 */
	@Test
	public void testIsDirectory() throws Exception{
		
	}
	
	/**
	 * Tests isFile
	 * @throws Exception 
	 */
	@Test
	public void testIsFile() throws Exception{
		
	}
	
	/**
	 * Tests mkdirs
	 * @throws Exception 
	 */
	@Test
	public void testMkDirs() throws Exception{
		
	}
	
	/**
	 * Tests mkdir
	 * @throws Exception 
	 */
	@Test
	public void testMkDir() throws Exception{
		
	}
	
	/**
	 * Tests creating an empty file
	 * @throws Exception 
	 */
	@Test
	public void testCreateEmptyFile() throws Exception{
		
	}
	
	/**
	 * Tests creating a temp file.
	 * @throws Exception 
	 */
	@Test
	public void testCreateTmpFile() throws Exception{
		
	}
	
	/**
	 * Ensures that a folder that is hidden will not show up
	 * @throws Exception 
	 */
	@Test
	public void testHiddenFileNotShowing() throws Exception{
		
	}
	
	/**
	 * Ensures that quotas cannot be exceeded
	 * @throws Exception 
	 */
	@Test
	public void testQuota() throws Exception{
		
	}
	
	/**
	 * Ensures that a read only file cannot be written to
	 * @throws Exception 
	 */
	@Test
	public void testReadOnly() throws Exception{
		
	}
	
	/**
	 * Ensures that folder depth cannot be exceeded if a
	 * restriction is in place.
	 * @throws Exception 
	 */
	@Test
	public void testFolderDepth() throws Exception{
		
	}
	
	/**
	 * Ensures globs match correctly
	 * @throws Exception 
	 */
	@Test
	public void testGlobMatching() throws Exception{
		
	}
	
	/**
	 * Tests basic symlinking, map one file to another.
	 * @throws Exception 
	 */
	@Test
	public void testBasicSymlink() throws Exception{
		
	}
	
	/**
	 * This test ensures that a symlink to deeper in the file system
	 * works, and it also tests to make sure that it can in fact
	 * go up past the "root" of the symlink, as long as ultimately
	 * it stays inside the file system.
	 * @throws Exception 
	 */
	@Test
	public void testSymlinkDown() throws Exception{
		
	}
	
	/**
	 * This test ensures that a symlink to a folder completely out
	 * of the file system works, and also ensures that it can't
	 * go up past the "root" of the symlink at all.
	 * @throws Exception 
	 */
	@Test
	public void testSymlinkOther() throws Exception{
		
	}

}
