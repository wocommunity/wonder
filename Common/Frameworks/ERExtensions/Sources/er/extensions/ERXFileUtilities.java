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
        int size = (int)f.length();
        FileInputStream fis = new FileInputStream(f);
        byte[] data = new byte[size];
        int bytesRead = 0;
        while (bytesRead < size)
            bytesRead += fis.read(data, bytesRead, size - bytesRead);
        fis.close();
        return data;
    }

    /**
     * Writes the contents of an InputStream to a specified file.
     * @param file to write to
     * @param stream to pull data from
     */
    public static void writeInputStreamToFile(File file, InputStream stream) throws IOException {
        if (file == null)
            throw new IOException("Attempting to write to a null file!");
        FileOutputStream out = new FileOutputStream(file);
        
        for (int b = stream.read(); b != -1; b = stream.read()) {
            out.write(b);
        }
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
     * Deletes a given directory in a recursive fashion.
     * @param directory to be deleted
     * @return if the directory deleted successfully
     */
    public static boolean deleteDirectory(File directory) {
        if (! directory.isDirectory()) return directory.delete();

        String[] fileNames = directory.list();
        for (int i = 0; i < fileNames.length; i++) {
            File file = new File(directory, fileNames[i]);

            if (file.isDirectory()) {
                if (!deleteDirectory(file)) return false;
            } else {
                if (!file.delete()) return false;
            }
        }
        return directory.delete();
    }    
    
    /**
     * Creates a symlink for a given file. Note this only works on
     * civilized OSs which support symbolic linking.
     * @param source to create the link to
     * @param destination file to create the link to
     * @param symbolic determines if a symlink should be created
     * @param allowUnlink determines if the symlink is a hardlink which allows unlinking
     */
    public static void linkFiles(File source, File destination, boolean symbolic, boolean allowUnlink) throws IOException {
        if (destination == null || source == null)
            throw new IllegalArgumentException("null source or destination not allowed");

        String cmd = "ln "                       +
            (allowUnlink ? "-f " : "")  +
            (symbolic    ? "-s " : "")  +
            source.getPath() + " "      +
            destination.getPath();

        Process task = null;
        try {
            task = Runtime.getRuntime().exec(cmd);
            while (true) {
                try { task.waitFor(); break; }
                catch (InterruptedException e) {}
            }
            if (task.exitValue() != 0) {
                BufferedReader err = new BufferedReader(new InputStreamReader(task.getErrorStream()));
                throw new IOException("Unable to create link: " + err.readLine());
            }            
        } finally {
            ERXExtensions.freeProcessResources(task);
        }
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
