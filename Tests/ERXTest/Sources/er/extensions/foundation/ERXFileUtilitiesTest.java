package er.extensions.foundation;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.Charset;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.lang.CharEncoding;
import org.junit.Assert;

import com.webobjects.foundation.NSArray;

import er.erxtest.ERXTestCase;

public class ERXFileUtilitiesTest extends ERXTestCase {

    private static final String defaultCharsetName = CharEncoding.UTF_8;

    private static final String alternateCharsetName = CharEncoding.ISO_8859_1;

    public void testSetDefaultCharset() {

        assertEquals(defaultCharsetName, ERXFileUtilities.charset().name());

        Charset csAlt = Charset.forName(alternateCharsetName);

        ERXFileUtilities.setDefaultCharset(alternateCharsetName);
        assertEquals(csAlt, ERXFileUtilities.charset());

        // Cleanup
        ERXFileUtilities.setDefaultCharset(defaultCharsetName);
    }

    public void testArrayByAddingFilesInDirectory() throws java.io.IOException {

        String name = "NotVeryLikely";

        File dir = null;

        do { name = name+"27248E02"; dir = new File(name); } while (dir.exists());
        assertFalse(dir.exists());

        NSArray<File> noneShallPass = ERXFileUtilities.arrayByAddingFilesInDirectory(dir, false);
        assertEquals(0, noneShallPass.size());

        assertTrue(dir.mkdirs());

        noneShallPass = ERXFileUtilities.arrayByAddingFilesInDirectory(dir, false);
        assertEquals(0, noneShallPass.size());

        File file = new File(dir.getPath()+File.separator+"file1.txt");
        ERXFileUtilities.stringToFile("File1IsAGoodFile", file);
        assertTrue(file.exists());

        NSArray<File> someFiles = ERXFileUtilities.arrayByAddingFilesInDirectory(dir, false);
        assertEquals(1, someFiles.size());

        someFiles = ERXFileUtilities.arrayByAddingFilesInDirectory(dir, true);
        assertEquals(1, someFiles.size());

        File subDir = new File(dir.getPath()+File.separator+"dir2");
        assertTrue(subDir.mkdirs());

        File subFile = new File(subDir.getPath()+File.separator+"file2.txt");
        ERXFileUtilities.stringToFile("File2IsGoodToo", subFile);
        assertTrue(subFile.exists());

        someFiles = ERXFileUtilities.arrayByAddingFilesInDirectory(dir, true);
        assertEquals(2, someFiles.size());

        // Cleanup
        assertTrue(subFile.delete());
        assertTrue(subDir.delete());
        assertTrue(file.delete());
        assertTrue(dir.delete());
    }

    public void testBytesFromFile() throws java.io.IOException {

        File file = File.createTempFile("ByteFulFile", ".txt");
        ERXFileUtilities.stringToFile("BytesAreYummy!", file);
        assertTrue(file.exists());

        byte[] expected = new byte[] { 0x42, 0x79, 0x74, 0x65, 0x73, 0x41, 0x72, 0x65, 0x59, 0x75, 0x6d, 0x6d, 0x79, 0x21 };
        byte[] found = ERXFileUtilities.bytesFromFile(file);

        Assert.assertArrayEquals(expected, found);

        // Cleanup
        assertTrue(file.delete());
    }

    public void testBytesFromGZippedFile() throws java.io.IOException {

        File file = File.createTempFile("GzipFulFile", ".gz");
        FileOutputStream fout = new FileOutputStream(file);
        GZIPOutputStream gzout = new GZIPOutputStream(fout);
        gzout.write("hello gzip!".getBytes());
        gzout.close();

        byte[] found = ERXFileUtilities.bytesFromGZippedFile(file);

        Assert.assertArrayEquals("hello gzip!".getBytes(), found);

        // Cleanup
        assertTrue(file.delete());
    }

    public void xxxtestBytesFromInputStream() {
        fail("Not Yet Implemented");
    }

    public void testChmod() throws java.io.IOException {

        String r = null;

        // For non-Unix systems, I am not sure what to use here, so if /bin/ls is going to fail, we will not run the rest of the test. -rrk
        //
        try {
            r = ERXFileUtilities.stringFromInputStream(Runtime.getRuntime().exec("/bin/ls").getInputStream());
        } catch (java.io.IOException ioe) {
            return;
        }

        File file = File.createTempFile("TestFile", ".tmp");
        ERXFileUtilities.stringToFile("ThisIsATest", file);

        ERXFileUtilities.chmod(file, "666");
        r = ERXFileUtilities.stringFromInputStream(Runtime.getRuntime().exec("/bin/ls -l "+file.getPath()).getInputStream());
        assertEquals("-rw-rw-rw-", r.substring(0, 10));

        ERXFileUtilities.chmod(file, "444");
        r = ERXFileUtilities.stringFromInputStream(Runtime.getRuntime().exec("/bin/ls -l "+file.getPath()).getInputStream());
        assertEquals("-r--r--r--", r.substring(0, 10));

        ERXFileUtilities.chmod(file, "777");
        r = ERXFileUtilities.stringFromInputStream(Runtime.getRuntime().exec("/bin/ls -l "+file.getPath()).getInputStream());
        assertEquals("-rwxrwxrwx", r.substring(0, 10));

        // Cleanup
        assertTrue(file.delete());
    }

    public void testChmodRecursively() throws java.io.IOException {

        String r = null;

        // For non-Unix systems, I am not sure what to use here, so if /bin/ls is going to fail, we will not run the rest of the test. -rrk
        //
        try {
            r = ERXFileUtilities.stringFromInputStream(Runtime.getRuntime().exec("/bin/ls").getInputStream());
        } catch (java.io.IOException ioe) {
            return;
        }

        File dir = ERXFileUtilities.createTempDir();
        File file = new File(dir.getPath()+File.separator+"TempFile.tmp");
        ERXFileUtilities.stringToFile("TempNess", file);

        ERXFileUtilities.chmod(file, "777");
        r = ERXFileUtilities.stringFromInputStream(Runtime.getRuntime().exec("/bin/ls -l "+file.getPath()).getInputStream());
        assertEquals("-rwxrwxrwx", r.substring(0, 10));

        ERXFileUtilities.chmod(dir, "777");
        r = ERXFileUtilities.stringFromInputStream(Runtime.getRuntime().exec("/bin/ls -l "+file.getPath()).getInputStream());
        assertEquals("-rwxrwxrwx", r.substring(0, 10));

        ERXFileUtilities.chmodRecursively(dir, "555");

        r = ERXFileUtilities.stringFromInputStream(Runtime.getRuntime().exec("/bin/ls -l "+file.getPath()).getInputStream());
        assertEquals("-r-xr-xr-x", r.substring(0, 10));
        r = ERXFileUtilities.stringFromInputStream(Runtime.getRuntime().exec("/bin/ls -ld "+dir.getPath()).getInputStream());
        assertEquals("dr-xr-xr-x", r.substring(0, 10));

        // Cleanup - for some reason, these cause an exception. Why? -rrk
        // assertTrue(file.delete());
        // assertTrue(dir.delete());
    }

    public void xxxtestCopyFilesFromDirectory() {
        fail("Not Yet Implemented");
    }

    public void testCopyFileToFileWhenDestFileDoesNotExist() throws java.io.IOException {

        String contents = "TestString";
        File srcFile = File.createTempFile("SrcTestFile", ".tmp");
        ERXFileUtilities.stringToFile(contents, srcFile);

        // Make a temporary directory using the same name as srcFile with a dir suffix
        File destDir = new File(srcFile.getAbsolutePath() + ".destdir");
        File destFile = new File(destDir, "DestTestFile.tmp");

        // Confirm destDir does not yet exist
        assertFalse(destDir.exists());

        // Since destDir does not yet exist, we do not have to delete the destination.
        ERXFileUtilities.copyFileToFile(srcFile, destFile, false, false);
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
    public void testCopyFileToFileWhenDestFileDoesExist() throws java.io.IOException {

        File srcFile = File.createTempFile("SrcTestFile", ".tmp");
        ERXFileUtilities.stringToFile("TestString", srcFile);

        File destFile = File.createTempFile("DestTestFile", ".tmp");
        ERXFileUtilities.stringToFile("OverwrittenString", destFile);

        // Confirm destFile does exist
        assertTrue(destFile.exists());

        ERXFileUtilities.copyFileToFile(srcFile, destFile, false, false);
        assertTrue(destFile.exists());

        String destContents = ERXFileUtilities.stringFromFile(destFile);
        assertEquals("TestString", destContents);

        // Cleanup
        assertTrue(srcFile.delete());
        assertTrue(destFile.delete());
    }

    public void testCopyFileToFileWhenDestDirDoesNotExist() throws java.io.IOException {

        String contents = "TestString";
        File srcFile = File.createTempFile("SrcTestFile", ".tmp");
        ERXFileUtilities.stringToFile(contents, srcFile);

        // Make a temporary directory using the same name as srcFile with a dir suffix
        File destDir = new File(srcFile.getAbsolutePath() + ".destdir");
        File destFile = new File(destDir, "DestTestFile.tmp");

        // Note destDir does not yet exist
        assertFalse(destDir.exists());

        ERXFileUtilities.copyFileToFile(srcFile, destFile, false, false);
        assertTrue(destFile.exists());

        String destContents = ERXFileUtilities.stringFromFile(destFile);
        assertEquals("TestString", destContents);

        // Cleanup
        assertTrue(srcFile.delete());
        assertTrue(destFile.delete());
        assertTrue(destDir.delete());
    }

    public void testCreateTempDir() throws java.io.IOException {
        File dir1 = ERXFileUtilities.createTempDir();
        File dir2 = ERXFileUtilities.createTempDir();

        assertTrue(dir1.exists());
        assertTrue(dir2.exists());
        assertFalse(dir1.getPath().equals(dir2.getPath()));

        // Cleanup
        assertTrue(dir1.delete());
        assertTrue(dir2.delete());
    }

    public void xxxtestDatePathWithRoot() {
        fail("Not Yet Implemented");
    }

    public void testDeleteDirectory() throws java.io.IOException {
        File dir = ERXFileUtilities.createTempDir();

        assertTrue(dir.exists());
        ERXFileUtilities.deleteDirectory(dir);
        assertFalse(dir.exists());

        // Cleanup - none needed
    }

    public void xxxtestDeleteFile() {
        fail("Not Yet Implemented");
    }

    public void xxxtestDeleteFilesInDirectory() {
        fail("Not Yet Implemented");
    }

    public void testFileExtension() {
        assertEquals("tmp", ERXFileUtilities.fileExtension("TestFile.tmp"));
        assertEquals("tmplonger", ERXFileUtilities.fileExtension("TestFile.tmplonger"));
        assertEquals("tmp", ERXFileUtilities.fileExtension("TestFile..tmp"));
        assertEquals("システム", ERXFileUtilities.fileExtension("TestFile.システム"));
        assertEquals("システム", ERXFileUtilities.fileExtension("システム.システム"));
        assertEquals("", ERXFileUtilities.fileExtension("TestFile"));
        assertEquals("", ERXFileUtilities.fileExtension(""));
    }

    public void xxxtestFileNameFromBrowserSubmittedPath() {
        fail("Not Yet Implemented");
    }

    public void xxxtestInputStreamForResourceNamed() {
        fail("Not Yet Implemented");
    }

    public void xxxtestLastModifiedDateForFileInFramework() {
        fail("Not Yet Implemented");
    }

    public void xxxtestLength() {
        fail("Not Yet Implemented");
    }

    public void xxxtestLinkFiles() {
        fail("Not Yet Implemented");
    }

    public void xxxtestListDirectories() {
        fail("Not Yet Implemented");
    }

    public void xxxtestListFiles() {
        fail("Not Yet Implemented");
    }

    public void xxxtestMd5() {
        fail("Not Yet Implemented");
    }

    public void xxxtestMd5Hex() {
        fail("Not Yet Implemented");
    }

    public void xxxtestPathForResourceNamed() {
        fail("Not Yet Implemented");
    }

    public void xxxtestPathURLForResourceNamed() {
        fail("Not Yet Implemented");
    }

    public void xxxtestReadPropertyListFromFileInFramework() {
        fail("Not Yet Implemented");
    }

    public void xxxtestRemoteCopyFile() {
        fail("Not Yet Implemented");
    }

    public void testRemoveFileExtension() {
        assertEquals("TestFile", ERXFileUtilities.removeFileExtension("TestFile.tmp"));
        assertEquals("TestFile", ERXFileUtilities.removeFileExtension("TestFile.tmplonger"));
        assertEquals("TestFile.tmp", ERXFileUtilities.removeFileExtension("TestFile.tmp.tmp"));
        assertEquals("TestFile", ERXFileUtilities.removeFileExtension("TestFile.システム"));
        assertEquals("TestFileシステム", ERXFileUtilities.removeFileExtension("TestFileシステム.tmp"));
        assertEquals("システム", ERXFileUtilities.removeFileExtension("システム.システム"));
    }

    public void xxxtestRenameTo() {
        fail("Not Yet Implemented");
    }

    public void testReplaceFileExtension() {
        assertEquals("TestFile.tmp", ERXFileUtilities.replaceFileExtension("TestFile.abc", "tmp"));
        assertEquals("TestFile.", ERXFileUtilities.replaceFileExtension("TestFile.abc", ""));
        assertEquals("TestFile.システム", ERXFileUtilities.replaceFileExtension("TestFile.abc", "システム"));
        assertEquals("TestFile.xxx", ERXFileUtilities.replaceFileExtension("TestFile", "xxx"));
        assertEquals("TestFile.xxx", ERXFileUtilities.replaceFileExtension("TestFile.システム", "xxx"));
    }

    public void xxxtestReserveUniqueFile() {
        fail("Not Yet Implemented");
    }

    public void xxxtestResourceExists() {
        fail("Not Yet Implemented");
    }

    public void xxxtestShortenFilename() {
        fail("Not Yet Implemented");
    }

    public void xxxtestStringFromFile() {
        fail("Not Yet Implemented");
    }

    public void xxxtestStringFromGZippedFile() {
        fail("Not Yet Implemented");
    }

    public void xxxtestStringFromInputStream() {
        fail("Not Yet Implemented");
    }

    public void xxxtestStringFromURL() {
        fail("Not Yet Implemented");
    }

    public void xxxtestStringToFile() {
        fail("Not Yet Implemented");
    }

    public void xxxtestStringToGZippedFile() {
    }

    public void xxxtestUnzipFile() {
        fail("Not Yet Implemented");
    }

    public void xxxtestURLFromFile() {
        fail("Not Yet Implemented");
    }

    public void xxxtestURLFromPath() {
        fail("Not Yet Implemented");
    }

    public void xxxtestWriteInputStreamToFile() {
        fail("Not Yet Implemented");
    }

    public void xxxtestWriteInputStreamToGZippedFile() {
        fail("Not Yet Implemented");
    }

    public void xxxtestWriteInputStreamToOutputStream() {
        fail("Not Yet Implemented");
    }

    public void xxxtestWriteInputStreamToTempFile() {
        fail("Not Yet Implemented");
    }

    public void xxxtestWriteUrlToTempFile() {
        fail("Not Yet Implemented");
    }

    public void xxxtestZipFile() {
        fail("Not Yet Implemented");
    }
}
