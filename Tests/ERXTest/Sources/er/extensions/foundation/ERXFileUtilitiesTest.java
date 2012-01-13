package er.extensions.foundation;

import java.io.File;
import java.io.IOException;

import er.erxtest.ERXTestCase;

public class ERXFileUtilitiesTest extends ERXTestCase {

	/**
	 * Tests when dest parent dirs do not exist.
	 * 
	 * @throws IOException
	 */
	public void testCopyFileToFile() throws IOException {
		String contents = "TestString";
		File srcFile = File.createTempFile("SrcTestFile", ".tmp");
		ERXFileUtilities.stringToFile(contents, srcFile);

		// Make a temporary directory using the same name as srcFile with a dir
		// suffix
		File destDir = new File(srcFile.getAbsolutePath() + ".destdir");
		File destFile = new File(destDir, "DestTestFile.tmp");

		// Note destDir does not yet exist
		assertFalse(destDir.exists());

		ERXFileUtilities.copyFileToFile(srcFile, destFile, true, false);
		assertFalse(srcFile.exists());
		assertTrue(destFile.exists());

		String destContents = ERXFileUtilities.stringFromFile(destFile);
		assertEquals("TestString", destContents);

		// Cleanup
		assertTrue(destFile.delete());
		assertTrue(destDir.delete());
	}

	/**
	 * Tests the file channel copy logic block.
	 * 
	 * @throws IOException
	 */
	public void testCopyFileToFile2() throws IOException {
		String contents = "TestString";
		File srcFile = File.createTempFile("SrcTestFile", ".tmp");
		ERXFileUtilities.stringToFile(contents, srcFile);

		// Make a temporary directory using the same name as srcFile with a dir
		// suffix
		File destDir = new File(srcFile.getAbsolutePath() + ".destdir");
		File destFile = new File(destDir, "DestTestFile.tmp");

		// Note destDir does not yet exist
		assertFalse(destDir.exists());

		ERXFileUtilities.copyFileToFile(srcFile, destFile, false, false);
		assertTrue(srcFile.exists());
		assertTrue(destFile.exists());

		String destContents = ERXFileUtilities.stringFromFile(destFile);
		assertEquals("TestString", destContents);

		// Cleanup
		assertTrue(srcFile.delete());
		assertTrue(destFile.delete());
		assertTrue(destDir.delete());
	}

	/**
	 * Tests the file channel copy logic block, copying and replace existing
	 * destination.
	 * 
	 * @throws IOException
	 */
	public void testCopyFileToFile3() throws IOException {

		File srcFile = File.createTempFile("SrcTestFile", ".tmp");
		ERXFileUtilities.stringToFile("TestString", srcFile);

		File destFile = File.createTempFile("DestTestFile", ".tmp");
		ERXFileUtilities.stringToFile("OverwrittenString", destFile);

		ERXFileUtilities.copyFileToFile(srcFile, destFile, false, false);
		assertTrue(srcFile.exists());
		assertTrue(destFile.exists());

		String destContents = ERXFileUtilities.stringFromFile(destFile);
		assertEquals("TestString", destContents);

		// Cleanup
		assertTrue(srcFile.delete());
		assertTrue(destFile.delete());
	}

	/**
	 * Tests when dest parent dirs do exist.
	 * 
	 * @throws IOException
	 */
	public void testCopyFileToFile4() throws IOException {
		String contents = "TestString";
		File srcFile = File.createTempFile("SrcTestFile", ".tmp");
		ERXFileUtilities.stringToFile(contents, srcFile);

		// Make a temporary directory using the same name as srcFile with a dir
		// suffix
		File destDir = new File(srcFile.getAbsolutePath() + ".destdir");
		assertTrue(destDir.mkdirs());

		File destFile = new File(destDir, "DestTestFile.tmp");

		// Note destDir does not yet exist
		assertTrue(destDir.exists());

		ERXFileUtilities.copyFileToFile(srcFile, destFile, true, false);
		assertFalse(srcFile.exists());
		assertTrue(destFile.exists());

		String destContents = ERXFileUtilities.stringFromFile(destFile);
		assertEquals("TestString", destContents);

		// Cleanup
		assertTrue(destFile.delete());
		assertTrue(destDir.delete());
	}
}
