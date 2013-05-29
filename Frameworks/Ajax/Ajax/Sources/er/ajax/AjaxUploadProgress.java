package er.ajax;

import java.io.File;

/**
 * AjaxUploadProgress is an AjaxProgress extended for tracking an Ajax File Upload.
 * 
 * @author mschrag
 */
public class AjaxUploadProgress extends AjaxProgress {
	public static interface Delegate {
		public void uploadFinished(AjaxUploadProgress progress);
	}
	
	private File _tempFile;
	private String _fileName;
	private String _contentType;

	/**
	 * Construct an AjaxUploadProgress.
	 * 
	 * @param id the id of this upload
	 * @param tempFile the File that is being written to
	 * @param fileName the name of the file uploaded from the client
	 * @param streamLength the total length of the stream
	 */
	public AjaxUploadProgress(String id, File tempFile, String fileName, long streamLength) {
		super(id, streamLength);
		_tempFile = tempFile;
		_fileName = fileName;
	}

	@Deprecated
	public AjaxUploadProgress(String id, File tempFile, String fileName, int streamLength) {
		this(id, tempFile, fileName, (long) streamLength);
	}

	/**
	 * Returns the name of the file the client uploaded.
	 * 
	 * @return the name of the file the client uploaded
	 */
	public String fileName() {
		return _fileName;
	}

	/**
	 * Returns the File object that was written to during the upload.
	 * 
	 * @return the File object that was written to during the upload
	 */
	public File tempFile() {
		return _tempFile;
	}
	
	public String contentType() {
		return _contentType;
	}
	
	public void setContentType(String type) {
		_contentType = type;
	}

	/**
	 * Deletes the temporary file.
	 */
	@Override
	public void dispose() {
		_tempFile.delete();
		super.dispose();
	}
}