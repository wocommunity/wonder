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

	/**
	 * Construct an AjaxUploadProgress.
	 * 
	 * @param id the id of this upload
	 * @param tempFile the File that is being written to
	 * @param fileName the name of the file uploaded from the client
	 * @param streamLength the total length of the stream
	 */
	public AjaxUploadProgress(String id, File tempFile, String fileName, int streamLength) {
		super(id, streamLength);
		_tempFile = tempFile;
		_fileName = fileName;
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

	/**
	 * Deletes the temporary file.
	 */
	public void dispose() {
		_tempFile.delete();
		super.dispose();
	}
}