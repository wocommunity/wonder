package er.extensions.virus;

import java.io.File;
import java.io.InputStream;

/**
 * Abstract virus scanner to check files and streams.
 * 
 * @author darkv
 */
public abstract class ERXVirusScanner {
	/**
	 * Virus scanner should scan the given file. Check {@link #isOk()} if file
	 * passed the check.
	 * 
	 * @param file
	 *            the file to scan
	 */
	public abstract void scan(File file);
	
	/**
	 * Virus scanner should scan the given input stream. Check {@link #isOk()}
	 * if file passed the check.
	 * 
	 * @param inputStream
	 *            the input stream to scan
	 */
	public abstract void scan(InputStream inputStream);
	
	/**
	 * Access the result of the previous scan. If no scan has been made yet a
	 * runtime exception will be thrown.
	 * 
	 * @return <code>true</code> if file/stream is virus free
	 */
	public abstract boolean isOk();
}
