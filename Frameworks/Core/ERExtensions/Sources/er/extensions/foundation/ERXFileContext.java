package er.extensions.foundation;

import java.io.File;

/**
 * A class to encapsulate a file reference and related information in the
 * context of web application usage.
 * 
 * Very commonly, you want a user friendly name for a download file or you want
 * to grab the filename of a file that the client uploaded, yet you might
 * have the file data streamed to or from a temporary file with a long meaningless unique name in
 * temp directory. This class just makes it convenient to have a real file, a client upload or download filename
 * and a mimetype bundled together in one class.
 * 
 * Useful for custom components that wrap FileUpload allowing you to bind a single ERXFileContext instead
 * of 3 bindings. You can bind the attributes of this class to any of the standard file upload WOComponent bindings
 * that match the 3 attributes encapsulated by this class.
 * 
 * Useful for passing around file download information as a single object rather than fiddling with 3 pieces of information.
 * 
 * @author kieran
 * 
 */
public class ERXFileContext {

	public ERXFileContext() {
	}

	public ERXFileContext(File file) {
		_file = file;
	}
	
	public ERXFileContext(File file, String clientFileName, String mimeType) {
		_file = file;
		_clientFileName = clientFileName;
		_mimeType = mimeType;
	}

	public ERXFileContext(String path) {
		_file = new File(path);
	}

	public void reset() {
		_file = null;
		_clientFileName = null;
		_mimeType = null;
	}

	// Note we maintain this and set it if it is set by other means such as path
	// name
	private File _file;

	/** @return the {@link File} */
	public File file() {
		return _file;
	}

	/**
	 * @param file
	 */
	public void setFile(File file) {
		_file = file;
	}

	private String _path;

	/** @return the absolute pathname of the file */
	public String path() {
		if (_file == null) {
			return null;
		}
		return _file.getAbsolutePath();
	}

	/**
	 * @param path
	 *            the absolute pathname of the file
	 */
	public void setPath(String path) {
		if (path == null) {
			throw new IllegalArgumentException("cannot set a 'null' path");
		}
		_file = new File(path);
	}

	private String _clientFileName;

	/**
	 * @return the file name from the perspective of the client or user, for
	 *         example the original name of a file that was uploaded, or the
	 *         name we are assigning to a file to be downloaded which may be
	 *         different to the temporary and/or unique name that we assign in
	 *         the file system.
	 */
	public String clientFileName() {
		if (_clientFileName == null) {
			if (_file == null) {
				return null;
			}
			return _file.getName();
		} // ~ if (_clientFileName == null)
		return _clientFileName;
	}

	/**
	 * @param clientFileName
	 */
	public void setClientFileName(String clientFileName) {
		_clientFileName = clientFileName;
	}

	private String _mimeType;

	/** @return the File mime-type */
	public String mimeType() {
		return _mimeType;
	}

	/**
	 * @param mimeType
	 *            the File mime-type
	 */
	public void setMimeType(String mimeType) {
		_mimeType = mimeType;
	}

	/**
	 * Returns the extension for the file represented by the receiver.
	 * 
	 * @return the extension of the filename (or null)
	 */
	public String extension() {
		String extension = null;
		if (_clientFileName != null) {
			extension = extensionForFileName(_clientFileName);
		}

		if (extension == null && _file != null) {
			extension = extensionForFileName(path());
		}

		return extension;
	}

	/**
	 * Returns the extension for the given filename.
	 * 
	 * @param fileName
	 *            the filename
	 * @return the extension of the filename (or null)
	 */
	private String extensionForFileName(String fileName) {
		String extension = null;
		if (fileName != null) {
			int dotIndex = fileName.lastIndexOf('.');
			if (dotIndex != -1) {
				extension = fileName.substring(dotIndex + 1);
			}
		}
		return extension;
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder(super.toString());
		b.append(";file = " + _file);
		b.append(";clientFileName = " + _clientFileName);
		b.append(";mimeType =" + _mimeType);
		return b.toString();
	}
}
