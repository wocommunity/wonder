//
//  ERXFileUtilities.java
//  ERExtensions
//
//  Created by Max Muller on Thu Jan 09 2003.
//
package er.extensions;

import java.io.*;
// Java 1.4 only
import java.nio.channels.*;
import java.net.*;
import java.util.*;
import java.util.zip.*;

import com.webobjects.appserver.*;
import com.webobjects.foundation.*;

/**
* Collection of handy {java.io.File} utilities.
 */
public class ERXFileUtilities {

    //	===========================================================================
    //	Class Constants
    //	---------------------------------------------------------------------------

    /** logging support */
    public static final ERXLogger log = ERXLogger.getERXLogger(ERXFileUtilities.class);

    //	===========================================================================
    //	Class Methods
    //	---------------------------------------------------------------------------

    /**
    * Returns the byte array for a given stream.
     * @param in stream to get the bytes from
     * @throws IOException if things go wrong
     * @return byte array of the stream.
     */
    public static byte[] bytesFromInputStream(InputStream in) throws IOException {
        if (in == null) throw new IllegalArgumentException("null input stream");

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        int read = -1;
        byte[] buf = new byte[1024 * 50];
        while ((read = in.read(buf)) != -1) {
            bout.write(buf, 0, read);
        }

        return bout.toByteArray();
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
        if (f == null) throw new IllegalArgumentException("null file");
        return bytesFromFile(f, (int)f.length());
    }

    /**
     * Returns the byte array for a given gzipped file.
     * @param f file to get the bytes from
     * @throws IOException if things go wrong
     * @return byte array of the file.
     */
    public static byte[] bytesFromGZippedFile(File f) throws IOException {
        if (f == null) throw new IllegalArgumentException("null file");
        FileInputStream fis = new FileInputStream(f);
        GZIPInputStream gis = new GZIPInputStream(fis);
        byte[] result = bytesFromInputStream(gis);
        fis.close();
        gis.close();
        return result;
    }

    /**
     * Returns an array of the first n bytes for a given file.
     * @param f file to get the bytes from
     * @param n number of bytes to read from input file
     * @throws IOException if things go wrong
     * @return byte array of the first n bytes from the file.
     */
    public static byte[] bytesFromFile(File f, int n) throws IOException {
        if (f == null) throw new IllegalArgumentException("null file");
        FileInputStream fis = new FileInputStream(f);
        byte[] result = bytesFromInputStream(fis,n);
        fis.close();
        return result;
    }

    /**
     * Returns an array of the first n bytes for a given input stream
     * @param fis inputstream to get the bytes from
     * @param n number of bytes to read from input stream
     * @throws IOException if things go wrong
     * @return byte array of the first n bytes from the file.
     */
    public static byte[] bytesFromInputStream(InputStream fis, int n) throws IOException {
        byte[] data = new byte[n];
        int bytesRead = 0;
        while (bytesRead < n)
            bytesRead += fis.read(data, bytesRead, n - bytesRead);
        return data;
    }
    
    
    /**
        * @deprecated use writeInputStreamToFile(InputStream is, File f) instead
     */
    public static void writeInputStreamToFile(File f, InputStream is) throws IOException {
        writeInputStreamToFile(is, f);
    }

    /**
        * Writes the contents of an InputStream to a specified file.
     * @param file to write to
     * @param stream to pull data from
     */
    public static void writeInputStreamToFile(InputStream stream, File file) throws IOException {
        if (file == null) throw new IllegalArgumentException("Attempting to write to a null file!");
        FileOutputStream out = new FileOutputStream(file);
        writeInputStreamToOutputStream(stream, out);
    }

    public static void writeInputStreamToGZippedFile(InputStream stream, File file) throws IOException {
        if (file == null) throw new IllegalArgumentException("Attempting to write to a null file!");
        FileOutputStream out = new FileOutputStream(file);
        writeInputStreamToOutputStream(stream, new GZIPOutputStream(out));
        out.close();
    }
    
    public static void writeInputStreamToOutputStream(InputStream in, OutputStream out) throws IOException {
        BufferedInputStream bis = new BufferedInputStream(in);
        byte buf[] = new byte[1024 * 50]; //64 KBytes buffer
        int read = -1;
        while ((read = bis.read(buf)) != -1) {
            out.write(buf, 0, read);
        }
        bis.close();
        out.flush();
        out.close();
        
    }

    public static void stringToFile(String s, File f) throws IOException {
        if (s == null) throw new NullPointerException("string argument cannot be null");
        if (f == null) throw new NullPointerException("file argument cannot be null");

        byte[] bytes = s.getBytes();
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        writeInputStreamToFile(bais, f);
    }


    public static void stringToGZippedFile(String s, File f) throws IOException {
        if (s == null) throw new NullPointerException("string argument cannot be null");
        if (f == null) throw new NullPointerException("file argument cannot be null");

        byte[] bytes = s.getBytes();
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        writeInputStreamToGZippedFile(bais, f);
    }

    /**
        * Copy a file across hosts using scp.
     * @param srcHost host to send from (null if file is local)
     * @param srcPath path on srcHost to read from
     * @param dstHost host to send to (null if file is local)
     * @param dstPath path on srcHost to write to
     */
    public static void remoteCopyFile(String srcHost, String srcPath, String dstHost, String dstPath) throws IOException {
        if (srcPath == null) throw new IllegalArgumentException("null source path not allowed");
        if (dstPath == null) throw new IllegalArgumentException("null source path not allowed");

        ArrayList args = new ArrayList(7);

        args.add("/usr/bin/scp");
        args.add("-B");
        args.add("-q");
        args.add("-o"); args.add("StrictHostKeyChecking=no");
        args.add(((srcHost != null) ? (srcHost + ":") : "") + srcPath);
        args.add(((dstHost != null) ? (dstHost + ":") : "") + dstPath);

        String[] cmd = (String[])args.toArray(new String[]{});

        Process task = null;
        try {
            task = Runtime.getRuntime().exec(cmd);
            while (true) {
                try { task.waitFor(); break; }
                catch (InterruptedException e) {}
            }
            if (task.exitValue() != 0) {
                BufferedReader err = new BufferedReader(new InputStreamReader(task.getErrorStream()));
                String message = err.readLine();
                throw new IOException("Unable to remote copy file: (exit status = " + task.exitValue() + ") " + message + "\n");
            }
        } finally {
            ERXExtensions.freeProcessResources(task);
        }
    }

    /**
        * Copy a file across hosts using scp.
     * @param srcFile local file to send
     * @param dstHost host to send to (null if file is local)
     * @param dstPath path on srcHost to write to
     */
    public static void remoteCopyFile(File srcFile, String dstHost, String dstPath) throws IOException {
        remoteCopyFile(null, srcFile.getPath(), dstHost, dstPath);
    }

    /**
        * Copy a file across hosts using scp.
     * @param srcHost host to send from (null if file is local)
     * @param srcPath path on srcHost to read from
     * @param dstFile local file to write to
     */
    public static void remoteCopyFile(String srcHost, String srcPath, File dstFile) throws IOException {
        remoteCopyFile(srcHost, srcPath, null, dstFile.getPath());
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
     * Returns a string from the gzipped file using the default
     * encoding.
     * @param f file to read
     * @return string representation of that file.
     */
    public static String stringFromGZippedFile(File f) throws IOException {
        return new String(bytesFromGZippedFile(f));
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
        String path = null;
        NSBundle bundle = "app".equals(frameworkName) ? NSBundle.mainBundle() : NSBundle.bundleForName(frameworkName);
        if(bundle != null && bundle.isJar()) {
            log.warn("Can't get path when run as jar: " + frameworkName + " - " + fileName);
        } else {
        	WOApplication application = WOApplication.application();
        	if (application != null) {
	            URL url = application.resourceManager().pathURLForResourceNamed(fileName, frameworkName, languages);
	            if(url != null) {
	                path = url.getFile();
	            }
        	} else if( bundle != null ) {
        		URL url = bundle.pathURLForResourcePath(fileName);
	            if(url != null) {
	                path = url.getFile();
	            }
        	}
        }
        return path;
    }

    /**
        * Returns a path containing an optional root with a directory hierarchy based on the current time
     * @param rootPath Root of the path before the above the date directories
     * @return the path based on time.
     */
    public static String datePathWithRoot(String rootPath){
        Calendar defaultCalendar = Calendar.getInstance();
        defaultCalendar.setTime(new Date());
        int year = defaultCalendar.get(Calendar.YEAR);
        int month = defaultCalendar.get(Calendar.MONTH) + 1;
        int day = defaultCalendar.get(Calendar.DAY_OF_MONTH);
        int hour = defaultCalendar.get(Calendar.HOUR_OF_DAY);
        String datePath = rootPath+"/y" + year
            + ((month > 9) ? "/m" : "/m0") + month
            + ((day > 9) ? "/d" : "/d0") + day
            + ((hour > 9) ? "/h" : "/h0") + hour;
        return datePath;
    }

    /**
     * Determines the path URL of the specified Resource. This is done
     * to get a single entry point due to the deprecation of pathForResourceNamed.
     * In a later version this will call out to the resource managers new methods directly.
     * @param fileName name of the file
     * @param frameworkName name of the framework, null or "app"
     *		for the application bundle
     * @return the absolutePath method off of the
     *		file object
     */
    public static URL pathURLForResourceNamed(String fileName, String frameworkName, NSArray languages) {
        URL url = null;
        try {
            url = new URL("file://" + pathForResourceNamed(fileName, frameworkName, languages));
        } catch(MalformedURLException ex) {
            throw new NSForwardException(ex);
        }
        return url;
    }

    /**
     * Create an URL for a given file.
     * @param fileName name of the file
     * @return file:// URL for the given path
     */
    public static URL URLFromFile(File file) {
        URL url = null;
        if(file != null) {
            try {
                url = URLFromPath(file.getCanonicalPath());
            } catch(IOException ex) {
                throw new NSForwardException(ex);
            }
        }
        return url;
    }

    /**
     * Create an URL for a given path.
     * @param fileName path of the file
     * @return file:// URL for the given path
     */
    public static URL URLFromPath(String fileName) {
        URL url = null;
        if(fileName != null) {
            try {
                url = new URL("file://" + fileName);
            } catch(MalformedURLException ex) {
                throw new NSForwardException(ex);
            }
        }
        return url;
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
        return readPropertyListFromFileInFramework(fileName,aFrameWorkName,languageList,null);
    }

    public static Object readPropertyListFromFileInFramework(String fileName,
                                                             String aFrameWorkName,
                                                             NSArray languageList,
                                                             String encoding
                                                             ) {
        String filePath = pathForResourceNamed(fileName, aFrameWorkName, languageList);
        Object result=null;
        if (filePath!=null) {
            File file = new File(filePath);
            try {
                try {
                    if (encoding == null) {
                        result = NSPropertyListSerialization.propertyListFromString(stringFromFile(file));                        
                    } else {
                        result = NSPropertyListSerialization.propertyListFromString(stringFromFile(file, encoding));
                    }
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
     * @param deleteOriginals tells if the original files, the file is deleted even if appuser has no write
     * rights. This is compareable to a <code>rm -f filename</code> instead of <code>rm filename</code>
     * @param recursiveCopy specifies if directories should be recursively copied
     * @param filter, which restricts the files to be copied
     */
    public static void copyFilesFromDirectory(File srcDirectory,
                                              File dstDirectory,
                                              boolean deleteOriginals,
                                              boolean recursiveCopy,
                                              FileFilter filter)
        throws FileNotFoundException, IOException {
            if (!srcDirectory.exists() || !dstDirectory.exists())
                throw new RuntimeException("Both the src and dst directories must exist! Src: " + srcDirectory
                                           + " Dst: " + dstDirectory);
            File srcFiles[] = filter!=null ?
                srcDirectory.listFiles(filter) :
                srcDirectory.listFiles();
            if (srcFiles != null && srcFiles.length > 0) {
                FileInputStream in = null;
                FileOutputStream out = null;

                for (int i = 0; i < srcFiles.length; i++) {
                    File srcFile = srcFiles[i];
                    File dstFile = new File(dstDirectory, srcFile.getName());
                    if (srcFile.isDirectory() && recursiveCopy) {
                        // Create the destination directory
                        if (deleteOriginals) {
                            srcFile.renameTo(dstFile);
                        } else {
                            dstFile.mkdir();
                            copyFilesFromDirectory(srcFile, dstFile, deleteOriginals, recursiveCopy, filter);
                        }
                    } else if (!srcFile.isDirectory()) {
                        copyFileToFile(srcFile, dstFile, deleteOriginals, true);
                    } else if (log.isDebugEnabled()) {
                        log.debug("Source file: " + srcFile + " is a directory inside: "
                                  + dstDirectory + " and recursive copy is set to false.");
                    }
                }
            }
        }

    /**
        * Copys the source file to the destination
     *
     * @param srcFile source file
     * @param dstFile destination file
     * @param deleteOriginals tells if original file will be deleted. Note that if the appuser has no write rights
     * on the file it is NOT deleted unless force delete is true
     * @param forceDelete if true then missing write rights are ignored and the file is deleted.
     */
    public static void copyFileToFile(File srcFile, File dstFile, boolean deleteOriginals, boolean forceDelete)
        throws FileNotFoundException, IOException {
            if (srcFile.exists() && srcFile.isFile()) {
                if (deleteOriginals && (!forceDelete || srcFile.canWrite())) {
                    srcFile.renameTo(dstFile);
                } else {
                    Throwable thrownException=null;
                    File  parent = dstFile.getParentFile();
                    parent.mkdirs();
                    FileInputStream in = new FileInputStream(srcFile);
                    FileOutputStream out = new FileOutputStream(dstFile);
                    try {
                        //////////////////////
                        // Java 1.4 Only
                        //////////////////////

                        // Create channel on the source
                        FileChannel srcChannel = in.getChannel();

                        // Create channel on the destination
                        FileChannel dstChannel = out.getChannel();

                        // Copy file contents from source to destination
                        dstChannel.transferFrom(srcChannel, 0, srcChannel.size());

                        // Close the channels
                        srcChannel.close();
                        dstChannel.close();

                        //////////////////////
                        // Java 1.3
                        //////////////////////
                        /*
                        //50 KBytes buffer
                        byte buf[] = new byte[1024 * 50];
                        int read = -1;
                        while ((read = in.read(buf)) != -1) {
                            out.write(buf, 0, read);
                        }
                         */
                        if (deleteOriginals && (srcFile.canWrite() || forceDelete))
                            srcFile.delete();
                    } catch (Throwable t) {
                        thrownException=t;
                    } finally {
                        if (out != null)
                            try {
                                out.close();
                            } catch (IOException io) {
                                if (thrownException==null) thrownException=io;
                            }
                        if (in != null) {
                            try {
                                in.close();
                            } catch (IOException io) {
                                if (thrownException==null) thrownException=io;
                            }
                        }
                    }
                    if (thrownException!=null) {
                        if (thrownException instanceof IOException) throw (IOException)thrownException;
                        else if (thrownException instanceof Error) throw (Error)thrownException;
                        else throw (RuntimeException)thrownException;
                    }
                }
            }
        }

    /**
        * Creates a temporary directory.
     *
     * @return a temporary directory
     *
     * @exception IOException if something goes wrong
     */
    public static final File createTempDir() throws IOException {
        File f = File.createTempFile("WonderTempDir", "");

        f.delete();
        f.delete();
        f.mkdirs();

        return f;
    }

    /**
        * Creates a temporary directory.
     *
     * @return a temporary directory
     *
     * @exception IOException if something goes wrong
     */
    public static final File createTempDir(String prefix, String suffix) throws IOException {
        File f = File.createTempFile(prefix, suffix);

        f.delete();
        f.delete();
        f.mkdirs();

        return f;
    }

    /**
        * Creates a new NSArray which contains all files in the specified directory.
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

    /**
        * Replaces the extension of the given file with the new extension.
     *
     * @param path the path of the file.
     * @param newExtension the new extension.
     *
     * @return the new path.
     */
    public static String replaceFileExtension(String path, String newExtension) {
        String tmp = "." + newExtension;

        if(path.endsWith(tmp)) {
            return path;

        } else {
            int index = path.lastIndexOf(".");

            if(index > 0) {
                String p = path.substring(0, index);
                return p + tmp;

            } else {
                return path + tmp;
            }
        }
    }

    /**
        * Decompresses the specified zipfile. If the file is a compressed directory, the whole subdirectory
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

    /**
        * zips a given File.
     * @param f the file to zip, either a file or a directory
     * @param absolutePaths if <code>true</code> then the files are added with absolute paths
     * @param deleteOriginal if <code>true</code> then the original file is deleted
     * @param forceDelete if <code>true</code> then the original is deleted even if the file is read only
     */
    public static File zipFile(File f, boolean absolutePaths, boolean deleteOriginal, boolean forceDelete) throws IOException {
        return zipFile(f, absolutePaths, deleteOriginal, forceDelete, 9);
    }

    public static File zipFile(File f, boolean absolutePaths, boolean deleteOriginal, boolean forceDelete, int level) throws IOException {
        

        if (!f.exists()) {
            throw new FileNotFoundException("file "+f+" does not exist");
        }

        File destination = new File(f.getAbsolutePath() + ".zip");
        if (destination.exists()) {
            throw new IOException("zipped file "+destination+" exists");
        }

        ZipOutputStream zout = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(destination)));
        zout.setLevel(level);

        NSArray files = f.isDirectory() ? arrayByAddingFilesInDirectory(f, true) : new NSArray(f);

        try {
            BufferedInputStream origin = null;

            byte data[] = new byte[2048];
            // get a list of files from current directory

            for (int i = 0; i < files.count(); i++) {
                File currentFile = (File)files.objectAtIndex(i);
                FileInputStream fi = new FileInputStream(currentFile);
                origin = new BufferedInputStream(fi, 2048);
                String entryName = currentFile.getAbsolutePath();
                if (!absolutePaths) {
                    entryName = entryName.substring(f.getAbsolutePath().length() + 1, entryName.length());
                }

                ZipEntry entry = new ZipEntry(entryName);
                zout.putNextEntry(entry);
                int count;
                while((count = origin.read(data, 0,
                                           2048)) != -1) {
                    zout.write(data, 0, count);
                }
                origin.close();
            }
            zout.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
        
        if (deleteOriginal) {
            if (f.canWrite() || forceDelete) {
                if (!deleteDirectory(f)) {
                    deleteDirectory(f);
                }
            }
        }
        return destination;
    }

    /**
     * Generate an MD5 hash from a file.
     *
     * @param file the file to sum
     * @return the MD5 sum of the bytes in file
     * @exception IOException
     */
    public static byte[] md5(File file) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        try {
            return md5(fis);            
        }
        finally {
            fis.close();
        }
    }

    /**
     * Generate an MD5 hash from an input stream.
     *
     * @param in the input stream to sum
     * @return the MD5 sum of the bytes in file
     * @exception IOException
     */
    public static byte[] md5(InputStream in) throws IOException {
        try {
            java.security.MessageDigest md5 = java.security.MessageDigest.getInstance("MD5");            
            byte[] buf = new byte[50 * 1024];
            int numRead;
            
            while ((numRead = in.read(buf)) != -1) {
                md5.update(buf, 0, numRead);
            }
            return md5.digest();
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new NSForwardException(e);
        }
    }
    
    /**
     * Generate an MD5 hash from a file.
     *
     * @param file the file to sum
     * @return the hex encoded MD5 sum of the bytes in file
     * @exception IOException
     */
    public static String md5Hex(File file) throws IOException {
        return ERXStringUtilities.byteArrayToHexString(md5(file));
    }

    /**
     * Generate an MD5 hash from an input stream.
     *
     * @param in the input stream to sum
     * @return the hex encoded MD5 sum of the bytes in file
     * @exception IOException
     */
    public static String md5Hex(InputStream in) throws IOException {
        return ERXStringUtilities.byteArrayToHexString(md5(in));
    }    
    
    public static long length(File f) {
        if (!f.isDirectory()) {
            return f.length();
        } else {
            long length = 0;
            File[] files = f.listFiles();
            for (int i = files.length; i-- > 0;) {
                length += length(files[i]);
            }
            return length;
        }
    }
    
}
