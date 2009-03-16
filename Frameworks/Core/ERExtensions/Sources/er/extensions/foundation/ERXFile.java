package er.extensions.foundation;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URI;

import com.ibm.icu.text.Normalizer;

/**
 * @author david teran
 * 
 * This class fixes some inconsistencies with java.io.File on MacOS X HFS+ filesystem
 * The following code does not work as expected:
 * <code>
 * 	File f = new File("/tmp/FilenameWithSpecialCharacterÄÖÜ");
 * 	f.createNewFile();
 * 	File tmp = new File("/tmp");
 * 	File[] contents = tmp.listFiles();
 * 	for (int i = 0; i < contents.length; i++) {
 * 		if (contents[i].getName().equals("FilenameWithSpecialCharacterÄÖÜ")) {
 * 			System.out.println("found it!");
 * 		}
 * 	}
 * </code>
 * One would expect that the comparision 
 * <code>contents[i].getName().equals("FilenameWithSpecialCharacterÄÖÜ")</code>
 * would result to <code>true</code>. This is not the case, at least not on HFS+
 * This subclass fixes this and should be used instead of java.io.File.
 * 
 */
public class ERXFile extends File {

    
    /**
     * @param arg0
     * @param arg1
     */
    public ERXFile(File arg0, String arg1) {
        super(arg0, arg1);
    }
    /**
     * @param arg0
     */
    public ERXFile(String arg0) {
        super(arg0);
    }
    /**
     * @param arg0
     * @param arg1
     */
    public ERXFile(String arg0, String arg1) {
        super(arg0, arg1);
    }
    /**
     * @param arg0
     */
    public ERXFile(URI arg0) {
        super(arg0);
    }

    /* (non-Javadoc)
     * @see java.io.File#getAbsolutePath()
     */
    public String getAbsolutePath() {
        return normalizedPath(super.getAbsolutePath());
    }
    /* (non-Javadoc)
     * @see java.io.File#getName()
     */
    public String getName() {
        return normalizedPath(super.getName());
    }
    
    
    /* (non-Javadoc)
     * @see java.io.File#list()
     */
    public String[] list() {
    	String[] names = super.list();
    	if (names == null) return null;
    	for (int i = 0; i < names.length; i++) {
    		names[i] = normalizedPath(names[i]);
    	}
    	return names;
    }
    /* (non-Javadoc)
     * @see java.io.File#list(java.io.FilenameFilter)
     */
    public String[] list(FilenameFilter arg0) {
        String[] names = super.list(arg0);
        if (names == null) return null;
        for (int i = 0; i < names.length; i++) {
        	names[i] = normalizedPath(names[i]);
        }
        return names;
    }
    /* (non-Javadoc)
     * @see java.io.File#listFiles()
     */
    public File[] listFiles() {
        File[] files = super.listFiles();
        if (files == null) return null;
        for (int i = 0; i < files.length; i++) {
            files[i] = new ERXFile(files[i].getAbsolutePath());
        }
        return files;
    }
    /* (non-Javadoc)
     * @see java.io.File#listFiles(java.io.FileFilter)
     */
    public File[] listFiles(FileFilter arg0) {
        File[] files = super.listFiles(arg0);
        if (files == null) return null;
        for (int i = 0; i < files.length; i++) {
            files[i] = new ERXFile(files[i].getAbsolutePath());
        }
        return files;
    }
    /* (non-Javadoc)
     * @see java.io.File#listFiles(java.io.FilenameFilter)
     */
    public File[] listFiles(FilenameFilter arg0) {
        File[] files = super.listFiles(arg0);
        if (files == null) return null;
        for (int i = 0; i < files.length; i++) {
            files[i] = new ERXFile(files[i].getAbsolutePath());
        }
        return files;
    }

    /* (non-Javadoc)
     * @see java.io.File#getAbsoluteFile()
     */
    public File getAbsoluteFile() {
        File f = super.getAbsoluteFile();
        ERXFile f1 = new ERXFile(f.getAbsolutePath());
        return f1;
    }
    /* (non-Javadoc)
     * @see java.io.File#getCanonicalFile()
     */
    public File getCanonicalFile() throws IOException {
        // TODO Auto-generated method stub
        return super.getCanonicalFile();
    }
    /* (non-Javadoc)
     * @see java.io.File#getParentFile()
     */
    public File getParentFile() {
        File f = super.getParentFile();
        ERXFile f1 = new ERXFile(f.getAbsolutePath());
        return f1;
    }
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return super.toString();
    }
    
    public static String normalizedPath(String name) {
    	 if (Normalizer.quickCheck(name, Normalizer.NFD) == Normalizer.YES) {
             return Normalizer.normalize(name, Normalizer.NFC);
         }
    	 return name;
    }
}
