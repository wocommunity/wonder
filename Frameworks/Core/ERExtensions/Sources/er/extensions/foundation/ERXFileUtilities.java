//
//  ERXFileUtilities.java
//  ERExtensions
//
//  Created by Max Muller on Thu Jan 09 2003.
//
package er.extensions.foundation;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.apache.commons.lang.CharEncoding;
import org.apache.log4j.Logger;

import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOResourceManager;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSBundle;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSPropertyListSerialization;

import er.extensions.ERXExtensions;
import er.extensions.foundation.ERXRuntimeUtilities.Result;
import er.extensions.foundation.ERXRuntimeUtilities.TimeoutException;

/**
 * Collection of handy {java.io.File} utilities.
 *
 * By default, will use UTF-8 for the character set, though one can set the static ivar to
 * override this choice.
 */
public class ERXFileUtilities {

    //	===========================================================================
    //	Class Constants
    //	---------------------------------------------------------------------------

    /** logging support */
    public static final Logger log = Logger.getLogger(ERXFileUtilities.class);

    private static Charset charset = null;

    static { setDefaultCharset(CharEncoding.UTF_8); }

    //  ===========================================================================
    //  Static Methods
    //  ---------------------------------------------------------------------------

    public static Charset charset() { return charset; }

    public static void setDefaultCharset(String name) {
        Charset original = charset;
        try {
            charset = Charset.forName(name);
        } catch (Exception e) {
            log.error("Unable to set default charset to \""+name+"\"");
            charset = original;
        }
    }

    //	===========================================================================
    //	Class Methods
    //	---------------------------------------------------------------------------

    /**
     * Copies the contents of the given URL to a temporary file.
     * 
     * @param url the URL to copy from
     * @param prefix the temporary file prefix
     * @param suffix the temporary file suffix (if null, the extension from the URL is used) 
     * @return the temporary file
     * @throws IOException if the copy fails
     */
    public static File writeUrlToTempFile(String url, String prefix, String suffix) throws IOException {
    	return ERXFileUtilities.writeUrlToTempFile(new URL(url), prefix, suffix);
    }
    
    /**
     * Copies the contents of the given URL to a temporary file.
     * 
     * @param url the URL to copy from
     * @param prefix the temporary file prefix
     * @param suffix the temporary file suffix (if null, the extension from the URL is used)
     * @return the temporary file
     * @throws IOException if the copy fails
     */
    public static File writeUrlToTempFile(URL url, String prefix, String suffix) throws IOException {
      String extension;
      if (suffix == null) {
	      String urlStr = url.toExternalForm();
	      int dotIndex = urlStr.lastIndexOf('.');
	      if (dotIndex >= 0) {
	        int questionMarkIndex = urlStr.indexOf('?', dotIndex);
	        if (questionMarkIndex == -1) {
	        	extension = urlStr.substring(dotIndex);
	        }
	        else {
	        	extension = urlStr.substring(dotIndex, questionMarkIndex);
	        }
	      }
	      else {
	        extension = "";
	      }
      }
      else {
    	  extension = suffix;
      }
      File tempFile = ERXFileUtilities.writeInputStreamToTempFile(url.openStream(), prefix, extension);
      return tempFile;
    }

    /**
     * Copies the contents of the given URL to a file.
     * 
     * @param url the URL to copy from
     * @param file the File to write to 
     * @throws IOException if the copy fails
     */
    public static void writeUrlToTempFile(String url, File file) throws IOException {
      ERXFileUtilities.writeUrlToTempFile(new URL(url), file);
    }

    /**
     * Copies the contents of the given URL to a file.
     * 
     * @param url the URL to copy from
     * @param file the File to write to 
     * @throws IOException if the copy fails
     */
    public static void writeUrlToTempFile(URL url, File file) throws IOException {
      ERXFileUtilities.writeInputStreamToFile(url.openStream(), file);
    }
    

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
     * @param encoding to be used, <code>null</code> will use the default
     * @return string representation of the stream
     * @throws IOException if things go wrong
     */
    public static String stringFromInputStream(InputStream in, String encoding) throws IOException {
        return new String(bytesFromInputStream(in), encoding);
    }

    /**
     * Returns a string from the input stream using the default
     * encoding.
     * @param in stream to read
     * @return string representation of the stream.
     * @throws IOException if things go wrong
     */
    public static String stringFromInputStream(InputStream in) throws IOException {
        return new String(bytesFromInputStream(in));
    }
    
    /**
     * Returns the String from the contents of the given URL.
     * 
     * @param url the URL to read from
     * @return the String contents of the URL
     * @throws IOException if an error occurs
     */
    public static String stringFromURL(URL url) throws IOException {
    	InputStream is = url.openStream();
    	try {
    		return ERXFileUtilities.stringFromInputStream(is);
    	}
    	finally {
    		is.close();
    	}
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
        byte[] result = null;
        try {
            result = bytesFromInputStream(gis);
        } finally {
            gis.close();
        }
        return result;
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
     * Returns an array of the first n bytes for a given file.
     * @param f file to get the bytes from
     * @param n number of bytes to read from input file
     * @throws IOException if things go wrong
     * @return byte array of the first n bytes from the file.
     */
    public static byte[] bytesFromFile(File f, int n) throws IOException {
        if (f == null) throw new IllegalArgumentException("null file");
        FileInputStream fis = new FileInputStream(f);
        try {
        	byte[] result = bytesFromInputStream(fis,n);
            return result;
        }
        finally {
        	fis.close();
        }
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
     * @deprecated use {@link #writeInputStreamToFile(InputStream, File)}
     */
    @Deprecated
	public static void writeInputStreamToFile(File f, InputStream is) throws IOException {
        writeInputStreamToFile(is, f);
    }

	/**
	 * Writes the contents of an InputStream to a temporary file.
	 * 
	 * @param stream
	 *            to pull data from
	 * @return the temp file that was created
	 * @throws IOException if things go wrong
	 */
	public static File writeInputStreamToTempFile(InputStream stream) throws IOException {
		return ERXFileUtilities.writeInputStreamToTempFile(stream, "_Wonder", ".tmp");
	}
	
	/**
	 * Writes the contents of an InputStream to a temporary file.
	 * 
	 * @param stream
	 *            to pull data from
	 * @param prefix the filename prefix of the temp file
	 * @param suffix the filename suffix of the temp file
	 * @return the temp file that was created 
	 * @throws IOException if things go wrong
	 */
	public static File writeInputStreamToTempFile(InputStream stream, String prefix, String suffix) throws IOException {
	    File tempFile;
	    try {
	        tempFile = File.createTempFile(prefix, suffix);
	        try {
	            ERXFileUtilities.writeInputStreamToFile(stream, tempFile);
	        }
	        catch (RuntimeException e) {
	            if (! tempFile.delete())
	                log.error("RuntimeException occured, but cannot delete tempFile \""+tempFile.getPath()+"\"");
	            throw e;
	        }
	        catch (IOException e) {
	            if (! tempFile.delete())
	                log.error("IOException occured, but cannot delete tempFile \""+tempFile.getPath()+"\"");
	            throw e;
	        }
	    }
	    finally {
	        stream.close();
	    }
	    return tempFile;
    }

    /**
     * Writes the contents of an InputStream to a specified file.
     * @param file to write to
     * @param stream to pull data from
     * @throws IOException if things go wrong
     */
    public static void writeInputStreamToFile(InputStream stream, File file) throws IOException {
    	FileOutputStream out = null;
    	try {
	        if (file == null) throw new IllegalArgumentException("Attempting to write to a null file!");
	        File parent = file.getParentFile();
	        if(parent != null && !parent.exists()) {
	            if (! parent.mkdirs())
                        throw new RuntimeException("Cannot create parent directory for file");
	        }
	        out = new FileOutputStream(file);
	        ERXFileUtilities.writeInputStreamToOutputStream(stream, true, out, true);
    	} finally {
    		stream.close();
    		if (out != null) {
    			out.close();
    		}
    	}
    }
    
    public static void writeInputStreamToGZippedFile(InputStream stream, File file) throws IOException {
    	if (file == null) throw new IllegalArgumentException("Attempting to write to a null file!");
    	GZIPOutputStream out = null;
     	try {
     		out = new GZIPOutputStream(new FileOutputStream(file));
     		ERXFileUtilities.writeInputStreamToOutputStream(stream, false, out, true);
     	} finally {
     		if (out != null) {
     			out.close();
     		}
     	}
    }
 	
    /**
     * Copies the contents of the input stream to the given output stream.  Both streams are
     * guaranteed to be closed by the end of this method.
     * 
     * @param in the input stream to copy from
     * @param out the output stream to copy to
     * @throws IOException if there is any failure
     */
    public static void writeInputStreamToOutputStream(InputStream in, OutputStream out) throws IOException {
    	ERXFileUtilities.writeInputStreamToOutputStream(in, true, out, true);
    }
     	
    /**
     * Copies the contents of the input stream to the given output stream.
     * 
     * @param in the input stream to copy from
     * @param closeInputStream if true, the input stream will be closed
     * @param out the output stream to copy to
     * @param closeOutputStream if true, the output stream will be closed
     * @throws IOException if there is any failure
     */
    public static void writeInputStreamToOutputStream(InputStream in, boolean closeInputStream, OutputStream out, boolean closeOutputStream) throws IOException {
    	try {
	        BufferedInputStream bis = new BufferedInputStream(in);
	        try {
		        byte buf[] = new byte[1024 * 50]; //64 KBytes buffer
		        int read = -1;
		        while ((read = bis.read(buf)) != -1) {
		            out.write(buf, 0, read);
		        }
	        }
	        finally {
	        	if (closeInputStream) {
	        		bis.close();
	        	}
	        }
			out.flush();
    	}
    	finally {
    		if (closeOutputStream) {
    			out.close();
    		}
    	}
    }
    
    public static void stringToGZippedFile(String s, File f) throws IOException {
	    if (s == null) throw new NullPointerException("string argument cannot be null");
	    if (f == null) throw new NullPointerException("file argument cannot be null");
	    
	    byte[] bytes = s.getBytes(charset().name());
	    ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
	    writeInputStreamToGZippedFile(bais, f);
    }
    
    /**
     * Writes the contents of <code>s</code> to <code>f</code>
     * using the platform's default encoding.
     * 
     * @param s the string to be written to file
     * @param f the destination file
     * @throws IOException if things go wrong
     */
    public static void stringToFile(String s, File f) throws IOException {
        stringToFile( s, f, System.getProperty("file.encoding") );
    }
    
    /**
     * Writes the contents of <code>s</code> to <code>f</code>
     * using specified encoding.
     * 
     * @param s the string to be written to file
     * @param f the destination file
     * @param encoding  the desired encoding
     * @throws IOException if things go wrong
     */
    public static void stringToFile(String s, File f, String encoding) throws IOException {
        if (s == null) throw new IllegalArgumentException("string argument cannot be null");
        if (f == null) throw new IllegalArgumentException("file argument cannot be null");
        if (encoding == null) throw new IllegalArgumentException("encoding argument cannot be null");
        Reader reader = new BufferedReader(new StringReader(s));
        FileOutputStream fos = new FileOutputStream(f);
        Writer out = new BufferedWriter( new OutputStreamWriter(fos, encoding) );
        char buf[] = new char[1024 * 50];
        int read = -1;
        try {
            while ((read = reader.read(buf)) != -1) {
                out.write(buf, 0, read);
            }
        } finally {
            reader.close();
            out.flush();
            out.close();
        }
    }

    /**
     * Copy a file across hosts using scp.
     * @param srcHost host to send from (<code>null</code> if file is local)
     * @param srcPath path on srcHost to read from
     * @param dstHost host to send to (<code>null</code> if file is local)
     * @param dstPath path on srcHost to write to
     * @throws IOException if things go wrong
     */
    public static void remoteCopyFile(String srcHost, String srcPath, String dstHost, String dstPath) throws IOException {
        if (srcPath == null) throw new IllegalArgumentException("null source path not allowed");
        if (dstPath == null) throw new IllegalArgumentException("null source path not allowed");

        NSMutableArray<String> args = new NSMutableArray<String>(7);
        args.addObject("/usr/bin/scp");
        args.addObject("-B");
        args.addObject("-q");
        args.addObject("-o"); 
        args.addObject("StrictHostKeyChecking=no");
        args.addObject(((srcHost != null) ? (srcHost + ":") : "") + srcPath);
        args.addObject(((dstHost != null) ? (dstHost + ":") : "") + dstPath);

        String[] cmd = ERXArrayUtilities.toStringArray(args);
        try {
            Result result = ERXRuntimeUtilities.execute(cmd, null, null, 0L);
            if(result.getExitValue() != 0) {
                throw new IOException("Unable to remote copy file: (exit status = " + result.getExitValue() + ") " + result.getErrorAsString() + "\n");
            }
        } catch (TimeoutException e) {
            throw new IOException("Command timed out");
        }
   }

    /**
     * Copy a file across hosts using scp.
     * @param srcFile local file to send
     * @param dstHost host to send to (<code>null</code> if file is local)
     * @param dstPath path on srcHost to write to
     * @throws IOException if things go wrong
     */
    public static void remoteCopyFile(File srcFile, String dstHost, String dstPath) throws IOException {
        remoteCopyFile(null, srcFile.getPath(), dstHost, dstPath);
    }

    /**
     * Copy a file across hosts using scp.
     * @param srcHost host to send from (<code>null</code> if file is local)
     * @param srcPath path on srcHost to read from
     * @param dstFile local file to write to
     * @throws IOException if things go wrong
     */
    public static void remoteCopyFile(String srcHost, String srcPath, File dstFile) throws IOException {
        remoteCopyFile(srcHost, srcPath, null, dstFile.getPath());
    }
    
    /**
     * Returns a string from the gzipped file using the default
     * encoding.
     * @param f file to read
     * @return string representation of that file.
     * @throws IOException if things go wrong
     */
    public static String stringFromGZippedFile(File f) throws IOException {
        return new String(bytesFromGZippedFile(f), charset().name());
    }
 	
    /**
     * Returns a string from the file using the default
     * encoding.
     * @param f file to read
     * @return string representation of that file.
     * @throws IOException if things go wrong
     */
    public static String stringFromFile(File f) throws IOException {
        return new String(bytesFromFile(f), charset().name());
    }

    /**
     * Returns a string from the file using the specified
     * encoding.
     * @param f file to read
     * @param encoding to be used, <code>null</code> will use the default
     * @return string representation of the file.
     * @throws IOException if things go wrong
     */
    public static String stringFromFile(File f, String encoding) throws IOException {
        if (encoding == null) {
            return new String(bytesFromFile(f), charset().name());
        }
        return new String(bytesFromFile(f), encoding);
    }

    /**
     * Determines the path of the specified Resource. This is done
     * to get a single entry point due to the deprecation of pathForResourceNamed
     * @param fileName name of the file
     * @param frameworkName name of the framework, <code>null</code> or "app"
     *		for the application bundle
     * @param languages array of languages to get localized resource or <code>null</code>
     * @return the absolutePath method off of the
     *		file object
     */
    public static String pathForResourceNamed(String fileName, String frameworkName, NSArray<String> languages) {
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
     * Determines if a given resource exists. This is done
     * to get a single entry point due to the deprecation of pathForResourceNamed
     * @param fileName name of the file
     * @param frameworkName name of the framework, <code>null</code> or "app"
     *      for the application bundle
     * @param languages array of languages to get localized resource or <code>null</code>
     * @return the absolutePath method off of the
     *      file object
     */
    public static boolean resourceExists(String fileName, String frameworkName, NSArray<String> languages) {
        URL url = WOApplication.application().resourceManager().pathURLForResourceNamed(fileName, frameworkName, languages);
        return url != null;
    }
    
    
   
    
    /**
     * Get the input stream from the specified Resource. 
     * @param fileName name of the file
     * @param frameworkName name of the framework, <code>null</code> or "app"
     *		for the application bundle
     * @param languages array of languages to get localized resource or <code>null</code>
     * @return the absolutePath method off of the file object
     */
    public static InputStream inputStreamForResourceNamed(String fileName, String frameworkName, NSArray<String> languages) {
        return WOApplication.application().resourceManager().inputStreamForResourceNamed(fileName, frameworkName, languages);
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
        StringBuilder datePath = new StringBuilder();
        datePath.append(rootPath);
        datePath.append("/y");
        datePath.append(year);
        datePath.append((month > 9) ? "/m" : "/m0");
        datePath.append(month);
        datePath.append((day > 9) ? "/d" : "/d0");
        datePath.append(day);
        datePath.append((hour > 9) ? "/h" : "/h0");
        datePath.append(hour);
        return datePath.toString();
    }

    /**
     * Determines the path URL of the specified Resource. This is done
     * to get a single entry point due to the deprecation of pathForResourceNamed.
     * In a later version this will call out to the resource managers new methods directly.
     * @param fileName name of the file
     * @param frameworkName name of the framework, <code>null</code> or "app"
     *		for the application bundle
     * @param languages array of languages to get localized resource or <code>null</code>
     * @return the absolutePath method off of the file object
     */
    public static URL pathURLForResourceNamed(String fileName, String frameworkName, NSArray<String> languages) {
    	URL url = null;
    	WOApplication application = WOApplication.application();
    	if (application != null) {
	    	WOResourceManager resourceManager = application.resourceManager();
	    	if (resourceManager != null) {
	    		url = resourceManager.pathURLForResourceNamed(fileName, frameworkName, languages);
	    	}
    	}
        return url;
    }

    /**
     * Create an URL for a given file.
     * @param file name of the file
     * @return file:// URL for the given path
     */
    public static URL URLFromFile(File file) {
        URL url = null;
        if(file != null) {
            try {
                url = URLFromPath(file.getCanonicalPath());
            } catch (IOException ex) {
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
     * @param frameworkName name of the framework, <code>null</code> or "app"
     *		for the application bundle
     * @return the <code>lastModified</code> method off of the
     *		file object
     */
    public static long lastModifiedDateForFileInFramework(String fileName, String frameworkName) {
        return lastModifiedDateForFileInFramework(fileName, frameworkName, null);
    }

    /**
     * Determines the last modification date for a given file
     * in a framework. Note that this method will only test for
     * the global resource not the localized resources.
     * @param fileName name of the file
     * @param frameworkName name of the framework, <code>null</code> or "app"
     *		for the application bundle
     * @param languages array of languages to get localized resource or <code>null</code>
     * @return the <code>lastModified</code> method off of the file object
     */
    public static long lastModifiedDateForFileInFramework(String fileName, String frameworkName, NSArray<String> languages) {
        long lastModified = 0;
        String filePath = pathForResourceNamed(fileName, frameworkName, languages);
        if (filePath != null) {
            lastModified = new File(filePath).lastModified();
        }
        return lastModified;
    }

    /**
     * Reads a file in from the file system and then parses
     * it as if it were a property list, using the platform's default encoding.
     * @param fileName name of the file
     * @param aFrameWorkName name of the framework, <code>null</code> or
     *		'app' for the application bundle.
     * @return de-serialized object from the plist formatted file
     *		specified.
     */
    public static Object readPropertyListFromFileInFramework(String fileName, String aFrameWorkName) {
        return readPropertyListFromFileInFramework(fileName, aFrameWorkName, null, System.getProperty("file.encoding"));
    }

    /**
     * Reads a file in from the file system and then parses
     * it as if it were a property list, using the specified encoding.
     *
     * @param fileName name of the file
     * @param aFrameWorkName name of the framework, <code>null</code> or
     *		'app' for the application bundle.
     * @param encoding  the encoding used with <code>fileName</code>
     * @return de-serialized object from the plist formatted file
     *		specified.
     */
    public static Object readPropertyListFromFileInFramework(String fileName, String aFrameWorkName, String encoding) {
        return readPropertyListFromFileInFramework(fileName, aFrameWorkName, null, encoding);
    }
    
    /**
     * Reads a file in from the file system for the given set
     * of languages and then parses the file as if it were a
     * property list, using the platform's default encoding.
     * @param fileName name of the file
     * @param aFrameWorkName name of the framework, <code>null</code> or
     *		'app' for the application bundle.
     * @param languageList language list search order
     * @return de-serialized object from the plist formatted file
     *		specified.
     */
    public static Object readPropertyListFromFileInFramework(String fileName,
                                                             String aFrameWorkName,
                                                             NSArray<String> languageList) {
        Object plist = null;
        try {
            plist = readPropertyListFromFileInFramework( fileName, aFrameWorkName, languageList, System.getProperty("file.encoding")); 
        } catch (IllegalArgumentException e) {
            try {
                // BUGFIX: we didnt use an encoding before, so java tried to guess the encoding. Now some Localizable.strings plists
                // are encoded in MacRoman whereas others are UTF-16.
                plist = readPropertyListFromFileInFramework(fileName, aFrameWorkName, languageList, CharEncoding.UTF_16);
            } catch (IllegalArgumentException e1) {
                // OK, whatever it is, try to parse it!
                plist = readPropertyListFromFileInFramework(fileName, aFrameWorkName, languageList, CharEncoding.UTF_8);
            }
        }
        return plist;
    }

    /**
     * Reads a file in from the file system for the given set
     * of languages and then parses the file as if it were a
     * property list, using the specified encoding.
     *
     * @param fileName name of the file
     * @param aFrameWorkName name of the framework, <code>null</code> or
     *		'app' for the application bundle.
     * @param languageList language list search order
     * @param encoding  the encoding used with <code>fileName</code>
     * @return de-serialized object from the plist formatted file
     *		specified.
     */    
    public static Object readPropertyListFromFileInFramework(String fileName,
            String aFrameWorkName,
            NSArray<String> languageList,
            String encoding) {
        Object result = null;
        InputStream stream = inputStreamForResourceNamed(fileName, aFrameWorkName, languageList);
        try {
        	if(stream != null) {
        		String stringFromFile = stringFromInputStream(stream, encoding);
        		result = NSPropertyListSerialization.propertyListFromString(stringFromFile);
            }
        } catch (IOException ioe) {
            log.error("ConfigurationManager: Error reading file <"+fileName+"> from framework " + aFrameWorkName);
        } finally {
        	try {if(stream != null) {stream.close();}} catch(IOException e) { log.error("Failed attempt to close stream.");}
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
    	deleteFilesInDirectory(directory, null, recurseIntoDirectories, true);
    }
    
    /**
     * Deletes all of the files in a given directory with the option to
     * recursively delete all of the files in the given directory.
     * @param directory to delete all of the files from
     * @param filter optional FileFilter to restrict what gets deleted, <code>null</code> to delete everything
     * @param recurseIntoDirectories determines if the delete is recursive
     * @param removeDirectories <code>true</code> if directories should be removed as well as files, <code>false</code> to only remove files
     */
    public static void deleteFilesInDirectory(File directory, FileFilter filter, boolean recurseIntoDirectories, boolean removeDirectories) {
        if (!directory.exists())
            throw new RuntimeException("Attempting to delete files from a non-existent directory: " + directory);
        if (!directory.isDirectory())
            throw new RuntimeException("Attmepting to delete files from a file that is not a directory: " + directory);
        File files[] = filter != null ? directory.listFiles(filter) : directory.listFiles() ;
        if (files != null && files.length > 0) {
            for (int i = 0; i < files.length; i++) {
                File aFile = files[i];
                if (aFile.isDirectory() && recurseIntoDirectories) {
                    deleteFilesInDirectory(aFile, filter, recurseIntoDirectories, removeDirectories);
                }
                if (aFile.isFile() || (aFile.isDirectory() && removeDirectories
                                       && (aFile.listFiles() == null || aFile.listFiles().length == 0))) {
                    if (! aFile.delete())
                        throw new RuntimeException("Directory \""+directory+"\" not successfully deleted.");
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

        boolean deletedAllFiles = true;
        String[] fileNames = directory.list();
        for (int i = 0; i < fileNames.length; i++) {
            File file = new File(directory, fileNames[i]);

            if (file.isDirectory()) {
                if (!deleteDirectory(file) && deletedAllFiles) deletedAllFiles = false;
            } else {
                if (!file.delete() && deletedAllFiles) deletedAllFiles = false;
            }
        }
        if (!directory.delete() && deletedAllFiles) deletedAllFiles = false;
        return deletedAllFiles;
    }

    /**
     * Java wrapper for call out to chmod.  Only works if your OS supports the chmod command.
     *
     * @param file the File to run chmod on
     * @param mode see the chmod man page
     * @throws IOException if things go wrong
     */
    public static void chmod(File file, String mode) throws IOException {
    	Runtime.getRuntime().exec(new String[] {"chmod", mode, file.getAbsolutePath()});
    }

    /**
     * Java wrapper for call out to chmod with -R parameter for recursive processing.  Only works if your OS supports the chmod command.
     *
     * @param dir the File to run chmod on
     * @param mode see the chmod man page
     * @throws IOException if things go wrong
     */
    public static void chmodRecursively(File dir, String mode) throws IOException {
    	Runtime.getRuntime().exec(new String[] {"chmod", "-R", mode, dir.getAbsolutePath()});
    }
    
    /**
     * Creates a symlink for a given file. Note this only works on
     * civilized OSs which support symbolic linking.
     * @param source to create the link to
     * @param destination file to create the link to
     * @param symbolic determines if a symlink should be created
     * @param allowUnlink determines if the symlink is a hardlink which allows unlinking
     * @param followSymbolicLinks If the destination is a symbolic link, follow it
     * @throws IOException if the link could not be created
     */
    public static void linkFiles(File source, File destination,
                                 boolean symbolic,
                                 boolean allowUnlink,
                                 boolean followSymbolicLinks) throws IOException {
                                     if (destination == null || source == null)
                                         throw new IllegalArgumentException("null source or destination not allowed");

                                 	ArrayList<String> array = new ArrayList<String>();
                                	array.add("ln");

                                	if (allowUnlink)
                                		array.add("-f");

                                	if (symbolic)
                                		array.add("-s");

                                	if (!followSymbolicLinks)
                                		array.add("-n");

                                	array.add(source.getPath());
                                	array.add(destination.getPath());

                                	String[] cmd = new String[array.size()];
                                	for (int i=0; i<array.size(); i++)
                                		cmd[i] = array.get(i);

                                     Process task = null;
                                     try {
                                         task = Runtime.getRuntime().exec(cmd);
                                         while (true) {
                                             try { task.waitFor(); break; }
                                             catch (InterruptedException e) {}
                                         }
                                         if (task.exitValue() != 0) {
                                             BufferedReader err = new BufferedReader(new InputStreamReader(task.getErrorStream(), charset()));
                                             throw new IOException("Unable to create link: " + err.readLine());
                                         }
                                     } finally {
                                         ERXExtensions.freeProcessResources(task);
                                     }
                                 }
    /**
     * Copies all of the files in a given directory to another directory.  Existing files are replaced.
     * @param srcDirectory source directory
     * @param dstDirectory destination directory
     * @param deleteOriginals tells if the original files, the file is deleted even if appuser has no write
     * rights. This is compareable to a <code>rm -f filename</code> instead of <code>rm filename</code>
     * @param recursiveCopy specifies if directories should be recursively copied
     * @param filter which restricts the files to be copied
     * @throws IOException if things go wrong
     */
	 public static void copyFilesFromDirectory(File srcDirectory,
	                                           File dstDirectory,
	                                           boolean deleteOriginals,
	                                           boolean recursiveCopy,
	                                           FileFilter filter)
	     throws IOException {
		 copyFilesFromDirectory(srcDirectory, dstDirectory, deleteOriginals, true, recursiveCopy, filter);
 	}

    /**
     * Copies all of the files in a given directory to another directory.
     * @param srcDirectory source directory
     * @param dstDirectory destination directory
     * @param deleteOriginals tells if the original files, the file is deleted even if appuser has no write
     * rights. This is comparable to a <code>rm -f filename</code> instead of <code>rm filename</code>
     * @param replaceExistingFiles <code>true</code> if the destination should be overwritten if it already exists
     * @param recursiveCopy specifies if directories should be recursively copied
     * @param filter which restricts the files to be copied
     * @throws IOException if things go wrong
     */
    public static void copyFilesFromDirectory(File srcDirectory,
                                              File dstDirectory,
                                              boolean deleteOriginals,
                                              boolean replaceExistingFiles,
                                              boolean recursiveCopy,
                                              FileFilter filter) throws IOException {

            if (!srcDirectory.exists() || !dstDirectory.exists())
                throw new RuntimeException("Both the src and dst directories must exist! Src: " + srcDirectory
                                           + " Dst: " + dstDirectory);

            File srcFiles[] = filter!=null ?
                srcDirectory.listFiles(filter) :
                srcDirectory.listFiles();

            if (srcFiles != null && srcFiles.length > 0) {

                for (int i = 0; i < srcFiles.length; i++) {
                    File srcFile = srcFiles[i];
                    File dstFile = new File(dstDirectory, srcFile.getName());
                    if (srcFile.isDirectory() && recursiveCopy) {
                        // Create the destination directory
                        if (deleteOriginals) {
                            renameTo(srcFile, dstFile);
                        } else {
                            if (dstFile.exists() || dstFile.mkdirs()) {
                                copyFilesFromDirectory(srcFile, dstFile, deleteOriginals, replaceExistingFiles, recursiveCopy, filter);
                            } else {
                                log.error("Error creating directories for destination \""+dstDirectory.getPath()+"\"");
                            }
                        }
                    } else if (!srcFile.isDirectory()) {
                    	if (replaceExistingFiles || ! dstFile.exists()) {
                            copyFileToFile(srcFile, dstFile, deleteOriginals, true);
                    	} else if (log.isDebugEnabled()) {
                            log.debug("Destination file: " + dstFile + " skipped as it exists and replaceExistingFiles is set to false.");
                      }
                    } else if (log.isDebugEnabled()) {
                        log.debug("Source file: " + srcFile + " is a directory inside: "
                                  + dstDirectory + " and recursive copy is set to false.");
                    }
                }
            }
        }

    /**
     * Copies the source file to the destination.
     * Automatically creates parent directory or directories of {@code dstFile} if they are missing.
     *
     * @param srcFile source file
     * @param dstFile destination file which may or may not exist already. If it exists, its contents will be overwritten.
     * @param deleteOriginals if {@code true} then {@code srcFile} will be deleted. Note that if the appuser has no write rights
     * on {@code srcFile} it is NOT deleted unless {@code forceDelete} is true
     * @param forceDelete if {@code true} then missing write rights are ignored and the file is deleted.
     * @throws IOException if things go wrong
     */
    public static void copyFileToFile(File srcFile, File dstFile, boolean deleteOriginals, boolean forceDelete) throws IOException {
        if (srcFile.exists() && srcFile.isFile()) {
        	boolean copied = false;
            if (deleteOriginals && (!forceDelete || srcFile.canWrite())) {
                copied = srcFile.renameTo(dstFile);
            } 
            if (!copied) {
                Throwable thrownException = null;
                File parent = dstFile.getParentFile();
                if (! parent.exists() && ! parent.mkdirs()) {
                	throw new IOException("Failed to create the directory " + parent + ".");
                }
                
                FileInputStream in = new FileInputStream(srcFile);
                try {
                	// Create channel on the source
                	FileChannel srcChannel = in.getChannel();
                	try {
                        FileOutputStream out = new FileOutputStream(dstFile);
                        try {
	                    	// Create channel on the destination
	                    	FileChannel dstChannel = out.getChannel();
	                    	try {
		                    	// Copy file contents from source to destination
		                    	dstChannel.transferFrom(srcChannel, 0, srcChannel.size());
	                    	}
	                    	catch (Throwable t) {
	                    		thrownException = t;
	                    	}
	                    	finally {
	                        	dstChannel.close();
	                    	}
                        }
                        catch (Throwable t) {
                    		if (thrownException == null) {
                    			thrownException = t;
                    		}
                        }
                        finally {
                        	out.close();
                        }
                    } catch (Throwable t) {
                		if (thrownException == null) {
                			thrownException = t;
                		}
                    } finally {
                    	srcChannel.close();
                	}
                } catch (Throwable t) {
                	if (thrownException == null) {
                		thrownException = t;
                	}
                } finally {
                	try {
                		in.close();
                	}
                	catch (IOException e) {
                		if (thrownException == null) {
                			thrownException = e;
                		}
                	}
                }

                if (deleteOriginals && (srcFile.canWrite() || forceDelete)) {
                    if (!srcFile.delete()) {
                    	throw new IOException("Failed to delete " + srcFile + ".");
                    }
                }

                if (thrownException != null) {
                    if (thrownException instanceof IOException) {
                    	throw (IOException)thrownException;
                    }
                    else if (thrownException instanceof Error) {
                    	throw (Error)thrownException;
                    }
                    else {
                    	throw (RuntimeException)thrownException;
                    }
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
        File f = createTempDir("WonderTempDir", "");

        if (f.delete() || f.delete())
            log.debug("Could not delete temporary directory: \""+f.getPath()+"\"");

        if (! f.mkdirs())
            log.error("Could not create temporary directory: \""+f.getPath()+"\"");

        return f;
    }

    /**
     * Creates a temporary directory.
     *
     * @param prefix prefix to use for the filename
     * @param suffix suffix to use for the filename
     *
     * @return a temporary directory
     *
     * @exception IOException if something goes wrong
     */
    public static final File createTempDir(String prefix, String suffix) throws IOException {
        File f = File.createTempFile(prefix, suffix);

        if (f.delete() || f.delete()) 
            log.debug("Could not delete temporary directory: \""+f.getPath()+"\"");

        if (! f.mkdirs())
            log.error("Could not create temporary directory: \""+f.getPath()+"\"");

        return f;
    }

    /**
     * Creates a new NSArray which contains all files in the specified directory.
     *
     * @param directory the directory from which to add the files
     * @param recursive if <code>true</code> then files are added recursively meaning subdirectories are scanned, too.
     *
     * @return a NSArray containing the files in the directory. If the specified directory does not
     * exist then the array is empty.
     */
    public static NSArray<File> arrayByAddingFilesInDirectory(File directory, boolean recursive) {
        ERXFile erxDirectory = new ERXFile(directory.getAbsolutePath());
        NSMutableArray<File> files = new NSMutableArray<File>();
        if (!erxDirectory.exists()) {
            return files;
        }

        File[] fileList = erxDirectory.listFiles();
        if (fileList == null) {
            return files;
        }

        for (int i = 0; i < fileList.length; i++) {
            File f = fileList[i];
            if (f.isDirectory() && recursive) {
                files.addObjectsFromArray(ERXFileUtilities.arrayByAddingFilesInDirectory(f, true));
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

        if (path.endsWith(tmp)) {
            return path;
        }
        int index = path.lastIndexOf(".");
        if (index > 0) {
            String p = path.substring(0, index);
            return p + tmp;
        }
        return path + tmp;
    }

    /**
     * Decompresses the specified zipfile. If the file is a compressed directory, the whole subdirectory
     * structure is created as a subdirectory with the name if the zip file minus the .zip extension
     * from destination. All intermittent directories are also created. If destination is <code>null</code>
     * then the <code>System Property</code> "java.io.tmpdir" is used as destination for the
     * uncompressed file(s).
     *
     *
     * @param f The file to unzip
     * @param destination the destination directory. If directory is <code>null</code> then the file will be unzipped in
     * java.io.tmpdir, if it does not exist, then a directory is created and if it exists but is a file
     * then the destination is set to the directory in which the file is located.
     *
     *
     * @return the file or directory in which the zipfile was unzipped
     *
     * @exception IOException if something goes wrong
     */
    public static File unzipFile(File f, File destination) throws IOException {

        if (!f.exists()) {
            throw new FileNotFoundException("file "+f+" does not exist");
        }

        String absolutePath;
        if (destination != null) {
            absolutePath = destination.getAbsolutePath();
            if (!destination.exists()) {
                if (! destination.mkdirs())
                    throw new RuntimeException("Cannot create destination directory: \""+destination.getPath()+"\"");
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
                if (new File(dir).mkdirs())
                    absolutePath = dir + File.separator;
                else
                    throw new IOException("Cannot create directory: \""+dir+"\"");
            }
        } else {
            return null;
        }

        for (Enumeration e = zipFile.entries(); e.hasMoreElements(); ) {
            ZipEntry ze = (ZipEntry)e.nextElement();
            String name = ze.getName();
            if (ze.isDirectory()) {
                File d = new File(absolutePath + name);
                if (! d.mkdirs())
                    throw new IOException("Cannot create directory: \""+d.getPath()+"\"");
                if (log.isDebugEnabled()) {
                    log.debug("created directory "+d.getAbsolutePath());
                }
            } else {
                InputStream is = null;
                try {
                	is = zipFile.getInputStream(ze);
	                writeInputStreamToFile(is, new File(absolutePath, name));
	                if (log.isDebugEnabled()) {
	                    log.debug("unzipped file "+ze.getName()+" into "+(absolutePath + name));
	                }
                } finally {
                	if (is != null) {
                		is.close();
                	}
                }
            }
        }

        return new File(absolutePath);
    }

    /**
     * Compresses a given File with zip.
     * @param f the file to zip, either a file or a directory
     * @param absolutePaths if <code>true</code> then the files are added with absolute paths
     * @param deleteOriginal if <code>true</code> then the original file is deleted
     * @param forceDelete if <code>true</code> then the original is deleted even if the file is read only
     * @return file pointer to the zip archive
     * @throws IOException if something goes wrong
     */
    public static File zipFile(File f, boolean absolutePaths, boolean deleteOriginal, boolean forceDelete) throws IOException {
        return zipFile(f, absolutePaths, deleteOriginal, forceDelete, 9);
    }

    /**
     * Compresses a given File with zip.
     * @param f the file to zip, either a file or a directory
     * @param absolutePaths if <code>true</code> then the files are added with absolute paths
     * @param deleteOriginal if <code>true</code> then the original file is deleted
     * @param forceDelete if <code>true</code> then the original is deleted even if the file is read only
     * @param level the compression level (0-9)
     * @return file pointer to the zip archive
     * @throws IOException if something goes wrong
     */
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

        NSArray<File> files = f.isDirectory() ? arrayByAddingFilesInDirectory(f, true) : new NSArray<File>(f);

        try {
            BufferedInputStream origin = null;

            byte data[] = new byte[2048];
            // get a list of files from current directory

            for (int i = 0; i < files.count(); i++) {
                File currentFile = files.objectAtIndex(i);
                FileInputStream fi = new FileInputStream(currentFile);
                origin = new BufferedInputStream(fi, 2048);
                String entryName = currentFile.getAbsolutePath();
                if (!absolutePaths) {
                		if (f.isDirectory()) {
                			entryName = entryName.substring(f.getAbsolutePath().length() + 1, entryName.length());
                		} else {
                			entryName = entryName.substring(f.getParentFile().getAbsolutePath().length() + 1, entryName.length());
                		}
                    
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
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
        	zout.close();
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
     * @exception IOException if file could not be read
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
     * @exception IOException if the input stream could not be read
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
     * @exception IOException if the file could not be read
     */
    public static String md5Hex(File file) throws IOException {
        return ERXStringUtilities.byteArrayToHexString(md5(file));
    }

    /**
     * Generate an MD5 hash from an input stream.
     *
     * @param in the input stream to sum
     * @return the hex encoded MD5 sum of the bytes in file
     * @exception IOException if the input stream could not be read
     */
    public static String md5Hex(InputStream in) throws IOException {
        return ERXStringUtilities.byteArrayToHexString(md5(in));
    }    
    
    /**
     * Returns the size of the given file. If <code>f</code> points
     * to a directory the size of all its children will be computed.
     * @param f file to get the size of
     * @return the file size
     */
    public static long length(File f) {
        if (!f.isDirectory()) {
            return f.length();
        }
        long length = 0;
        File[] files = f.listFiles();
        for (int i = files.length; i-- > 0;) {
            length += length(files[i]);
        }
        return length;
    }
    
    /** shortens a filename, for example aVeryLongFileName.java -> aVer...Name.java
     * @param name the name to modify
     * @param maxLength the maximum length of the name.
     * <code>maxLength</code> values under 4 have no effect, the returned string is
     * always a....java
     * @return the shortened filename
     */
    public static String shortenFilename(String name, int maxLength) {
        String ext = fileExtension( name );
        String s = removeFileExtension( name );
        // not sure but we could use \u2026, instead...
        String elips = "...";
        int elipsLength = elips.length();
        int stringLength = s.length();
        if( stringLength == maxLength )
            return name;
        if( maxLength  <= elipsLength )
            maxLength = elipsLength + 1;
        int noOfChars = maxLength - elipsLength;
        int mod = noOfChars%2;
        int firstHalf = noOfChars/2 + mod;
        int secondHalf = firstHalf - mod;        
        StringBuilder sb = new StringBuilder();
        sb.append( s.substring( 0, firstHalf ) );
        sb.append( elips );
        sb.append( s.substring( stringLength-secondHalf, stringLength ) );
        sb.append('.');
        sb.append( ext );
        return sb.toString();
    }

    /** returns the filename without its fileExtension
     * @param name the name of the file
     * @return the name of the file without the fileExtension
     */
    public static String removeFileExtension(String name) {
        int index = name.lastIndexOf(".");
        if (index == -1) {
            return name;
        }
        return name.substring(0, index);
    }

    /** returns the fileExtension from the specified filename
     * @param name the name of the file
     * @return the fileExtension
     */
    public static String fileExtension(String name) {
        int index = name.lastIndexOf(".");
        if (index == -1) {
            return "";
        }
        return name.substring(index + 1);
    }

    /** 
     * Deletes all files in array <code>filesToDelete</code> by
     * using the method deleteDirectory.
     * 
     * @param filesToDelete array of files to delete
     * @return <code>true</code> if all file have been deleted,
     * <code>false</code> otherwise
     */
    public static boolean deleteFiles(NSArray<File> filesToDelete) {
        boolean deletedAllFiles = true;
        for (int i = filesToDelete.count(); i-- > 0;) {
            File currentFile = filesToDelete.objectAtIndex(i);
            if (!deleteFile(currentFile) && deletedAllFiles) deletedAllFiles = false;
        }
        return deletedAllFiles;
    }

    /**
     * Deletes the given file by using the method deleteDirectory.
     * @param fileToDelete file to delete
     * @return <code>true</code> if file has been deleted,
     * <code>false</code> otherwise
     */
    public static boolean deleteFile(File fileToDelete) {
        return deleteDirectory(fileToDelete);
    }
    
    /** Lists all directories in the specified directory, is desired recursive.
     *  
     * @param baseDir the dir from which to list the child directories
     * @param recursive if <code>true</code> this methods works recursively
     * @return an array of files which are directories
     */
    public static File[] listDirectories(File baseDir, boolean recursive) {
        File[] files = baseDir.listFiles(new FileFilter() {

            public boolean accept(File f) {
                return f.isDirectory();
            }
        });
        if (recursive) {
        	NSMutableArray<File> a = new NSMutableArray<File>(files);
            for (int i = files.length; i-- > 0;) {
                File currentDir = files [i];
                File[] currentDirs = listDirectories(currentDir, true);
                a.addObjects(currentDirs);
            }
            Object[] objects = a.objects();
            files = new File[objects.length];
            System.arraycopy(objects, 0, files, 0, objects.length);
        }
        return files;
    }

    
    /**
     * Lists all files in the specified directory, if desired recursively.
     *  
     * @param baseDir the dir from which to list the child files
     * @param recursive if <code>true</code> this method works recursively
     * @param filter filter to match the files against. If <code>null</code>, all files will be included. 
     * @return an array of files
     */
    public static File[] listFiles(File baseDir, boolean recursive, FileFilter filter) {
        File[] files = baseDir.listFiles(filter);
        if (files != null && recursive) {
        	NSMutableArray<File> a = new NSMutableArray<File>();
            for (int i = files.length; i-- > 0;) {
                File currentFile = files[i];
            	a.addObject(currentFile);
                if(currentFile.isDirectory()) {
                	File[] currentFiles = listFiles(currentFile, true, filter);
                	a.addObjects(currentFiles);
                }
            }
            Object[] objects = a.objects();
            files = new File[objects.length];
            System.arraycopy(objects, 0, files, 0, objects.length);
        }
        return files;
    }

    /**
     * Moves a file from one location to another one. This works different
     * than java.io.File.renameTo as renameTo does not work across partitions
     * 
     * @param source the file to move
     * @param destination the destination to move the source to
     * @throws IOException if things go wrong
     */
    public static void renameTo(File source, File destination) throws IOException {
        if (!source.renameTo(destination)) {
            ERXFileUtilities.copyFileToFile(source, destination, true, true);
        }
    }
    
	/**
	 * Returns the file name portion of a browser submitted path.
	 * 
	 * @param path the full path from the browser
	 * @return the file name portion
	 */
    public static String fileNameFromBrowserSubmittedPath(String path) {
        String fileName = path;
    	if (path != null) {
	    	// Windows
	    	int separatorIndex = path.lastIndexOf("\\");
	        // Unix
	    	if (separatorIndex == -1) {
	            separatorIndex = path.lastIndexOf("/");
	        }
	    	// MacOS 9
	        if (separatorIndex == -1) {
	        	separatorIndex = path.lastIndexOf(":");
	        }
	        if (separatorIndex != -1) {
	        	fileName = path.substring(separatorIndex + 1);
	        }
	        // ... A tiny security check here ... Just in case.
	        fileName = fileName.replaceAll("\\.\\.", "_");
    	}
        return fileName;
    }
    
    /**
     * Reserves a unique file on the filesystem based on the given file name.  If the given
     * file cannot be reserved, then "-1", "-2", etc will be appended to the filename in front
     * of the extension until a unique file name is found.  This will also ensure that the
     * parent folder is created. 
     * 
     * @param desiredFile the desired destination file to write
     * @param overwrite if true, this will immediately return desiredFile
     * @return a unique, reserved, filename
     * @throws IOException if the file cannot be created
     */
    public static File reserveUniqueFile(File desiredFile, boolean overwrite) throws IOException {
		File destinationFile = desiredFile;
		
		// ... make sure the destination folder exists.  This code runs twice here
		// in case there was a race condition.
	    File destinationFolder = destinationFile.getParentFile();
	    if (!destinationFolder.exists()) {
	      if (!destinationFolder.mkdirs()) {
	        if (!destinationFolder.exists()) {
	          throw new IOException("Unable to create the destination folder '" + destinationFolder + "'.");
	        }
	      }
	    }

		if (!overwrite) {
			// try to reserve file name
			if (!desiredFile.createNewFile()) {
				File parentFolder = desiredFile.getParentFile();
				String fileName = desiredFile.getName();
				// didn't work, so try new name consisting of
				// prefix + number + suffix
				int dotIndex = fileName.lastIndexOf('.');
				String prefix, suffix;

				if (dotIndex < 0) {
					prefix = fileName;
					suffix = "";
				}
				else {
					prefix = fileName.substring(0, dotIndex);
					suffix = fileName.substring(dotIndex);
				}

				int counter = 1;
				// try until we can reserve a file
				do {
					destinationFile = new File(parentFolder, prefix + "-" + counter + suffix);
					counter ++;
				}
				while (!destinationFile.createNewFile());
			}
		}
		
		return destinationFile;
    }
}
