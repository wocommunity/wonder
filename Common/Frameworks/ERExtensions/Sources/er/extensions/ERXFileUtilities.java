//
//  ERXFileUtilities.java
//  ERExtensions
//
//  Created by Max Muller on Thu Jan 09 2003.
//
package er.extensions;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import java.io.*;

/**
 * Collection of handy {java.io.File} utilities.
 */
public class ERXFileUtilities {

    /** logging support */
    public static final ERXLogger log = ERXLogger.getERXLogger(ERXFileUtilities.class);
    
    /**
    * Returns the byte array for a given file.
     * @param f file to get the bytes from
     * @throws IOException if things go wrong
     * @return byte array of the file.
     */
    public static byte[] bytesFromFile(File f) throws IOException {
        if (f == null)
            throw new IOException("null file");
        int size = (int) f.length();
        FileInputStream fis = new FileInputStream(f);
        byte[] data = new byte[size];
        int bytesRead = 0;
        while (bytesRead < size)
            bytesRead += fis.read(data, bytesRead, size - bytesRead);
        fis.close();
        return data;
    }

    /**
    * Returns a string from the file using the default
     * encoding.
     * @param f file to read
     * @return string representation of that file.
     */
    public static String stringFromFile(File f) throws IOException {
        return new String(bytesFromFile(f));
    }
    /**
    * Returns a string from the file using the specified
     * encoding.
     * @param f file to read
     * @param encoding to be used, null will use the default
     * @return string representation of the file.
     */
    public static String stringFromFile(File f, String encoding) throws IOException {
        return new String(bytesFromFile(f), encoding);
    }

    /**
    * Determines the last modification date for a given file
     * in a framework. Note that this method will only test for
     * the global resource not the localized resources.
     * @param fileName name of the file
     * @param frameworkName name of the framework, null or "app"
     *		for the application bundle
     * @return the <code>lastModified</code> method off of the
     *		file object
     */
    // ENHANCEME: Should be able to specify the language to check
    public static long lastModifiedDateForFileInFramework(String fileName, String frameworkName) {
        long lastModified = 0;
        String filePath = WOApplication.application().resourceManager().pathForResourceNamed(fileName,
                                                                                             frameworkName,
                                                                                             null);
        if (filePath!=null) {
            lastModified = new File(filePath).lastModified();
        }
        return lastModified;
    }

    /**
    * Reads a file in from the file system and then parses
     * it as if it were a property list.
     * @param fileName name of the file
     * @param aFrameWorkName name of the framework, null or
     *		'app' for the application bundle.
     * @return de-serialized object from the plist formatted file
     *		specified.
     */
    public static Object readPropertyListFromFileInFramework(String fileName, String aFrameWorkName) {
        return readPropertyListFromFileInFramework(fileName, aFrameWorkName, null);
    }

    /**
    * Reads a file in from the file system for the given set
     * of languages and then parses the file as if it were a
     * property list.
     * @param fileName name of the file
     * @param aFrameWorkName name of the framework, null or
     *		'app' for the application bundle.
     * @param languageList language list search order
     * @return de-serialized object from the plist formatted file
     *		specified.
     */
    // FIXME: Not the best way of handling encoding
    // ENHANCEME: Should be using an InputStream instead of a File
    public static Object readPropertyListFromFileInFramework(String fileName,
                                                             String aFrameWorkName,
                                                             NSArray languageList) {
        String filePath = WOApplication.application().resourceManager().pathForResourceNamed(fileName,
                                                                                             aFrameWorkName,
                                                                                             languageList);
        Object result=null;
        if (filePath!=null) {
            File file = new File(filePath);
            try {
                try {
                    result=NSPropertyListSerialization.propertyListFromString(stringFromFile(file));
                } catch (IllegalArgumentException iae) {
                    result=NSPropertyListSerialization.propertyListFromString(stringFromFile(file, "UTF-16"));
                }
            } catch (IOException ioe) {
                log.error("ConfigurationManager: Error reading "+filePath);
            }
        }
        return result;
    }

    /**
     * Deletes all of the files in a given directory with the option to
     * recursively delete all of the directories in the given directory.
     * @param directory to delete all of the files from
     * @param recurseIntoDirectories determines if the delete is recursive
     */
    public static void deleteFilesInDirectory(File directory, boolean recurseIntoDirectories) {
        if (!directory.exists())
            throw new RuntimeException("Attempting to delete files from a non-existant directory: " + directory);
        if (!directory.isDirectory())
            throw new RuntimeException("Attmepting to delete files from a file that is not a directory: " + directory);
        File files[] = directory.listFiles();
        if (files != null && files.length > 0) {
            for (int i = 0; i < files.length; i++) {
                File aFile = files[i];
                if (aFile.isDirectory() && recurseIntoDirectories) {
                    deleteFilesInDirectory(aFile, recurseIntoDirectories);
                }
                if (aFile.isFile() || (aFile.isDirectory()
                                       && (aFile.listFiles() == null || aFile.listFiles().length == 0))) {
                    aFile.delete();
                }
            }
        }
    }

    /**
     * Creates a symlink for a given file. Note this only works on
     * civilized OSs which support symbolic linking.
     * @param file to create the link to
     * @param symlinkPath file to create the link to
     * @param deleteSymlinkIfExists controls if the original symlink should be
     *		deleted if it already exists
     * @return if the link was successfully created
     */
    public static boolean createSymbolicLink(File file, File symlink, boolean deleteSymlinkIfExists)
        throws IOException {
            boolean linkSuccessfullyCreated = false;
        if (file == null || symlink == null)
            throw new RuntimeException("Both file and symlink path must be non-null. File: "
                                       + file + " symlink path: " + symlink);
        if (!file.exists())
            throw new RuntimeException("Attempting to link to a file that does not exist: " + file);
        String shellCommand = "ln -s " + file.getAbsolutePath() + " " + symlink.getAbsolutePath();
        Process linkProcess = null;
        try {
            linkProcess = Runtime.getRuntime().exec(shellCommand);
            linkProcess.waitFor();
            if (linkProcess.exitValue() != 0) {
                BufferedReader buffer = new BufferedReader(new InputStreamReader(linkProcess.getErrorStream()));
                log.error("Link process error message: " + buffer.readLine());
            } else {
                linkSuccessfullyCreated = true;
            }
        } catch (InterruptedException e) {
            log.error("Caught InterruptedException when linking: " + file + " to " + symlink, e);
        } finally {
            ERXExtensions.freeProcessResources(linkProcess);
        }
        return linkSuccessfullyCreated;
    }

    /**
     * Copys all of the files in a given directory to another directory.
     * @param srcDirectory source directory
     * @param dstDirectory destination directory
     * @param deleteOriginals tells if the original files
     */
    // ENHANCEME: Should support recursive directory copying.
    public static void copyFilesFromDirectory(File srcDirectory, File dstDirectory, boolean deleteOriginals)
        throws FileNotFoundException, IOException {
        if (!srcDirectory.exists() || !dstDirectory.exists())
            throw new RuntimeException("Both the src and dst directories must exist! Src: " + srcDirectory
                                       + " Dst: " + dstDirectory);
        File srcFiles[] = srcDirectory.listFiles();
        if (srcFiles != null && srcFiles.length > 0) {
            FileInputStream in;
            FileOutputStream out;

                for (int i = 0; i < srcFiles.length; i++) {
                    File srcFile = srcFiles[i];
                    if (srcFile.exists() && srcFile.isFile()) {
                        in = new FileInputStream(srcFile);
                        File dstFile = new File(dstDirectory.getAbsolutePath() + File.pathSeparator + srcFile.getName());
                        out = new FileOutputStream(dstFile);
                        int c;
                        while ((c = in.read()) != -1) {
                            out.write(c);
                        }
                        out.close();
                        in.close();
                        if (deleteOriginals)
                            srcFile.delete();
                    }                    
                }
        }
    }
}
