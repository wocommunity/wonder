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
import java.util.*;
import java.util.zip.*;

/**
* Collection of handy {java.io.File} utilities.
 */
public class ERXFileUtilities {

    /** logging support */
    public static final ERXLogger log = ERXLogger.getERXLogger(ERXFileUtilities.class);

    /**
     * Returns the byte array for a given stream.
     * @param in stream to get the bytes from
     * @throws IOException if things go wrong
     * @return byte array of the stream.
     */
    public static byte[] bytesFromInputStream(InputStream in) throws IOException {
        if (in == null) throw new IllegalArgumentException("null input stream");
        final int BUFSIZ = 1024;
        byte[] data = new byte[BUFSIZ];
        int total = 0, c = 0, x = 0;
        while ((c = in.read(data, total, x)) != -1) {
            total += c;
            x -= c;
            if (x == 0) { // Need more buffer
                byte[] tmp = new byte[total + BUFSIZ];
                System.arraycopy(data, 0, tmp, 0, total);
                data = tmp; x = BUFSIZ;
            }
        }
        return data;
    }

    /**
     * Returns a string from the input stream using the specified
     * encoding.
     * @param in stream to read
     * @param encoding to be used, null will use the default
     * @return string representation of the stream.
     */
    public static String stringFromInputStream(InputStream in, String encoding) throws IOException {
        return new String(bytesFromInputStream(in), encoding);
    }

    /**
     * Returns a string from the input stream using the default
     * encoding.
     * @param in stream to read
     * @param encoding to be used, null will use the default
     * @return string representation of the stream.
     */
    public static String stringFromInputStream(InputStream in) throws IOException {
        return new String(bytesFromInputStream(in));
    }
    
    /**
    * Returns the byte array for a given file.
     * @param f file to get the bytes from
     * @throws IOException if things go wrong
     * @return byte array of the file.
     */
    public static byte[] bytesFromFile(File f) throws IOException {
        if (f == null)
            throw new IllegalStateException("null file");
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
            throw new IllegalStateException("Attempting to write to a null file!");
        FileOutputStream out = new FileOutputStream(file);
        //50 KBytes buffer
        byte buf[] = new byte[1024 * 50];
        int read = -1;
        while ((read = stream.read(buf)) != -1) {
            out.write(buf, 0, read);
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
     * Determines the path of the specified Resource. This is done
     * to get a single entry point due to the deprecation of pathForResourceNamed
     * @param fileName name of the file
     * @param frameworkName name of the framework, null or "app"
     *		for the application bundle
     * @return the absolutePath method off of the
     *		file object
     */
    public static String pathForResourceNamed(String fileName, String frameworkName, NSArray languages) {
       return WOApplication.application().resourceManager().pathForResourceNamed(fileName, frameworkName, languages);
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
        String filePath = pathForResourceNamed(fileName, frameworkName, null);
        if (filePath != null) {
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
        String filePath = pathForResourceNamed(fileName, aFrameWorkName, languageList);
        Object result=null;
        if (filePath!=null) {
            File file = new File(filePath);
            try {
                try {
                    result = NSPropertyListSerialization.propertyListFromString(stringFromFile(file));
                } catch (IllegalArgumentException iae) {
                    result = NSPropertyListSerialization.propertyListFromString(stringFromFile(file, "UTF-16"));
                }
            } catch (IOException ioe) {
                log.error("ConfigurationManager: Error reading file <"+filePath+">");
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
    public static void linkFiles(File source, File destination,
                                 boolean symbolic,
                                 boolean allowUnlink,
                                 boolean followSymbolicLinks) throws IOException {
        if (destination == null || source == null)
            throw new IllegalArgumentException("null source or destination not allowed");

        String[] cmd = new String[6];

        int i = 0;
        cmd[i++] = "ln";
        if (allowUnlink         ) cmd[i++] = "-f";
        if (symbolic            ) cmd[i++] = "-s";
        if (!followSymbolicLinks) cmd[i++] = "-h";
        cmd[i++] = source.getPath();
        cmd[i++] = destination.getPath();

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
                        File dstFile = new File(dstDirectory.getAbsolutePath() + File.separator + srcFile.getName());
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


    /** Creates a new NSArray which contains all files in the specified directory.
        *
        * @param directory the directory from which to add the files
        * @param recursive if true then files are added recursively meaning subdirectories are scanned, too.
        *
        * @return a NSArray containing the files in the directory. If the specified directory does not
        * exist then the array is empty.
        */
    public static NSArray arrayByAddingFilesInDirectory(File directory, boolean recursive) {
        NSMutableArray files = new NSMutableArray();
        if (!directory.exists()) {
            return files;
        }

        File[] fileList = directory.listFiles();
        if (fileList == null) {
            return files;
        }

        for (int i = 0; i < fileList.length; i++) {
            File f = fileList[i];
            if (f.isDirectory() && recursive) {
                files.addObjectsFromArray(arrayByAddingFilesInDirectory(f, true));
            } else {
                files.addObject(f);
            }
        }
        return files;
    }


/** Decompresses the specified zipfile. If the file is a compressed directory, the whole subdirectory
        * structure is created as a subdirectory from destination. If destination is <code>null</code>
        * then the <code>System Property</code> "java.io.tmpdir" is used as destination for the
        * uncompressed file(s).
        *
        *
        * @param f The file to unzip
        * @param destination the destination directory. If directory is null then the file will be unzipped in
        * java.io.tmpdir, if it does not exist, then a directory is created and if it exists but is a file
        * then the destination is set to the directory in which the file is located.
        *
        *
        * @return the file or directory in which the zipfile was unzipped
        *
        * @exception IOException
        */
    public static File unzipFile(File f, File destination) throws IOException {
        if (!f.exists()) {
            throw new FileNotFoundException("file "+f+" does not exist");
        }

        String absolutePath;
        if (destination != null) {
            absolutePath = destination.getAbsolutePath();
            if (!destination.exists()) {
                destination.mkdir();
            } else if (!destination.isDirectory()) {
                absolutePath = absolutePath.substring(0, absolutePath.lastIndexOf(File.separator));
            }
        } else {
            absolutePath = System.getProperty("java.io.tmpdir");
        }
        if (!absolutePath.endsWith(File.separator)) {
            absolutePath += File.separator;
        }

        ZipFile zipFile = new ZipFile(f);

        Enumeration en = zipFile.entries();
        if (en.hasMoreElements()) {
            ZipEntry firstEntry = (ZipEntry)en.nextElement();
            if (firstEntry.isDirectory() || en.hasMoreElements()) {
                String dir = absolutePath + f.getName();
                if (dir.endsWith(".zip")) {
                    dir = dir.substring(0, dir.length() - 4);
                }
                new File(dir).mkdir();
                absolutePath = dir + File.separator;
            } 
        } else {
            return null;
        }
        
        for (Enumeration e = zipFile.entries(); e.hasMoreElements(); ) {
            ZipEntry ze = (ZipEntry)e.nextElement();
            String name = ze.getName();
            if (ze.isDirectory()) {
                File d = new File(absolutePath + name);
                d.mkdir();
                if (log.isDebugEnabled()) {
                    log.debug("created directory "+d.getAbsolutePath());
                }
            } else {
                InputStream is = zipFile.getInputStream(ze);
                writeInputStreamToFile(new File(absolutePath + name), is);
                if (log.isDebugEnabled()) {
                    log.debug("unzipped file "+ze.getName()+" into "+(absolutePath + name));
                }
            }
        }

        return new File(absolutePath);
    }
}
