package er.extensions.foundation;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URI;

import com.ibm.icu.text.Normalizer;

/**
 * Fixes some inconsistencies with java.io.File on MacOS X HFS+ filesystem and
 * should be used as a replacement.
 *
 * For example, the following code will not work as expected:
 * <pre><code>
 * 	File f = new File("/tmp/FilenameWithSpecialCharacterÄÖÜ");
 * 	f.createNewFile();
 * 	File tmp = new File("/tmp");
 * 	File[] contents = tmp.listFiles();
 * 	for (int i = 0; i &lt; contents.length; i++) {
 * 		if (contents[i].getName().equals("FilenameWithSpecialCharacterÄÖÜ")) {
 * 			System.out.println("found it!");
 * 		}
 * 	}
 * </code></pre>
 *
 * One would expect that the comparison
 * <code>contents[i].getName().equals("FilenameWithSpecialCharacterÄÖÜ")</code>
 * would result in <code>true</code>. This is not the case, at least not on HFS+
 * This subclass fixes this and should be used instead of java.io.File.
 *
 * Actually, the code above _does_ work (WO 5.4.3, Mac OS X 10.5.8, java 1.5.0_22),
 * but what else does this do? -rrk
 * 
 * @deprecated use plain java.io.File instead
 *
 * @author David Teran
 */
@Deprecated
public class ERXFile extends File {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new File instance from a parent abstract pathname and a child pathname string.
	 * 
	 * If parent is null then the new File instance is created as if by invoking the single-argument
	 * File constructor on the given child pathname string.
	 * 
	 * Otherwise the parent abstract pathname is taken to denote a directory, and the child pathname
	 * string is taken to denote either a directory or a file. If the child pathname string is absolute
	 * then it is converted into a relative pathname in a system-dependent way. If parent is the empty
	 * abstract pathname then the new File instance is created by converting child into an abstract
	 * pathname and resolving the result against a system-dependent default directory. Otherwise each
	 * pathname string is converted into an abstract pathname and the child abstract pathname is
	 * resolved against the parent.
	 * 
	 * @param parent - The parent abstract pathname
	 * @param child - The child pathname string
	 * 
	 * @throws NullPointerException - If child is null
	 */
    public ERXFile(File parent, String child) {
        super(parent, child);
    }

    /**
     * Creates a new File instance by converting the given pathname string into an abstract pathname.
     * If the given string is the empty string, then the result is the empty abstract pathname.
     * 
     * @param pathname - A pathname string
     * 
     * @throws NullPointerException - If the pathname argument is null
     */
    public ERXFile(String pathname) {
        super(pathname);
    }

    /**
     * Creates a new File instance from a parent pathname string and a child pathname string.
     * 
     * If parent is null then the new File instance is created as if by invoking the single-argument File
     * constructor on the given child pathname string.
     * 
     * Otherwise the parent pathname string is taken to denote a directory, and the child pathname string is
     * taken to denote either a directory or a file. If the child pathname string is absolute then it is
     * converted into a relative pathname in a system-dependent way. If parent is the empty string then the
     * new File instance is created by converting child into an abstract pathname and resolving the result
     * against a system-dependent default directory. Otherwise each pathname string is converted into an
     * abstract pathname and the child abstract pathname is resolved against the parent.
     * 
     * @param parent - The parent pathname string
     * @param child - The child pathname string
     * 
     * @throws NullPointerException - If child is null
     */
    public ERXFile(String parent, String child) {
        super(parent, child);
    }

    /**
     * Creates a new File instance by converting the given file: URI into an abstract pathname.
     * 
     * The exact form of a file: URI is system-dependent, hence the transformation performed by this
     * constructor is also system-dependent.
     * 
     * For a given abstract pathname <code>f</code> it is guaranteed that
     * 
     * <pre><code>new File(f.toURI()).equals(f.getAbsoluteFile())</code></pre>
     * 
     * so long as the original abstract pathname, the URI, and the new abstract pathname are all
     * created in (possibly different invocations of) the same Java virtual machine. This relationship
     * typically does not hold, however, when a file: URI that is created in a virtual machine on one
     * operating system is converted into an abstract pathname in a virtual machine on a different
     * operating system.
     * 
     * @param uri - An absolute, hierarchical URI with a scheme equal to "file", a non-empty path component,
     * and undefined authority, query, and fragment components
     * 
     * @throws NullPointerException - If uri is null
     * @throws IllegalArgumentException - If the preconditions on the parameter do not hold
     * 
     * @since 1.4
     */
    public ERXFile(URI uri) {
        super(uri);
    }

    @Override
    public String getAbsolutePath() {
        return normalizedPath(super.getAbsolutePath());
    }

    @Override
    public String getName() {
        return normalizedPath(super.getName());
    }

    @Override
    public String[] list() {
    	String[] names = super.list();
    	if (names == null) return null;
    	for (int i = 0; i < names.length; i++) {
    		names[i] = normalizedPath(names[i]);
    	}
    	return names;
    }

    @Override
    public String[] list(FilenameFilter arg0) {
        String[] names = super.list(arg0);
        if (names == null) return null;
        for (int i = 0; i < names.length; i++) {
        	names[i] = normalizedPath(names[i]);
        }
        return names;
    }

    @Override
    public File[] listFiles() {
        File[] files = super.listFiles();
        if (files == null) return null;
        for (int i = 0; i < files.length; i++) {
            files[i] = new ERXFile(files[i].getAbsolutePath());
        }
        return files;
    }

    @Override
    public File[] listFiles(FileFilter arg0) {
        File[] files = super.listFiles(arg0);
        if (files == null) return null;
        for (int i = 0; i < files.length; i++) {
            files[i] = new ERXFile(files[i].getAbsolutePath());
        }
        return files;
    }

    @Override
    public File[] listFiles(FilenameFilter arg0) {
        File[] files = super.listFiles(arg0);
        if (files == null) return null;
        for (int i = 0; i < files.length; i++) {
            files[i] = new ERXFile(files[i].getAbsolutePath());
        }
        return files;
    }

    @Override
    public File getAbsoluteFile() {
        File f = super.getAbsoluteFile();
        ERXFile f1 = new ERXFile(f.getAbsolutePath());
        return f1;
    }

    @Override
    public File getCanonicalFile() throws IOException {
        return super.getCanonicalFile();
    }

    @Override
    public File getParentFile() {
        File f = super.getParentFile();
        if (f == null) {
        	return null;
        }
        ERXFile f1 = new ERXFile(f.getAbsolutePath());
        return f1;
    }

    @Override
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
