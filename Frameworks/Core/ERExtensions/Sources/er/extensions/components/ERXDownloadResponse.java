package er.extensions.components;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOMessage;
import com.webobjects.appserver.WOResponse;

import er.extensions.foundation.ERXFileContext;

/**
 * A component that is used to generate a download response. Simply create the
 * component, use appropriate setters for the type of content to be downloaded
 * and return the component from a component action
 * 
 * @binding contentDisposition A string to set the content-disposition header. Defaults to 
 * <code>"attachment;filename=\"" + downloadFilename() + "\""</code>
 * @binding contentType A string to represents the MIME type (text/plain, application/pdf, 
 * etc.) of the file 
 * @binding downloadFilename A string that represents the name of the file. Defaults to 
 * "downloadedfile"
 * @binding fileContext An ERXFileContext object that contains a reference to the file, 
 * the file name and the MIME type. If you set this binding, you don't need to set the 
 * contentType, fileToDownload and downloadFilename bindings.
 * @binding fileToDownload A java.io.File object that will be returned in the response
 *
 * @author kieran 1/27/2006
 * 
 * 
 */
public class ERXDownloadResponse extends WOComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	private static final Logger log = LoggerFactory.getLogger(ERXDownloadResponse.class);

	private String _downloadFilename;
	private long _streamingContentSize = 0L;

	public ERXDownloadResponse(WOContext context) {
		super(context);
	}

	/**
	 * Convenience method to set file, download filename and content type.
	 * 
	 * @param fileContext
	 */
	public void setFileContext(ERXFileContext fileContext) {
		_fileToDownload = fileContext.file();
		_downloadFilename = fileContext.clientFileName();
		_contentType = fileContext.mimeType();
	}

	/**
	 * @return the filename for the file after it is downloaded. Defaults to an
	 *         arbitrary name.
	 */
	private String downloadFilename() {
		if (_downloadFilename == null) {
			if (fileToDownload() != null) {
				_downloadFilename = fileToDownload().getName();
			} else {
				_downloadFilename = "downloadedfile";
			}
		}
		return _downloadFilename;
	}

	public void setDownloadFilename(String newDownloadFilename) {
		_downloadFilename = newDownloadFilename;
	}

	private File _fileToDownload;

	/** @return the file to be downloaded (streaming) */
	private File fileToDownload() {
		return _fileToDownload;
	}

	/**
	 * @param aFile the file to be downloaded.
	 */
	public void setFileToDownload(File aFile) {
		_fileToDownload = aFile;
	}

	private InputStream _inputStreamToDownload;

	/**
	 * @return an InputStream pointing to the data to download.
	 */
	private InputStream inputStreamToDownload() {
		return _inputStreamToDownload;
	}

	/**
	 * WO 5.3.3 setter where contentSize is int
	 * 
	 * @param inStream
	 * @param contentSize
	 */
	public void setInputStreamToDownload(InputStream inStream, long contentSize) {
		_inputStreamToDownload = inStream;
		_streamingContentSize = contentSize;
	}

	@Override
	public void appendToResponse(WOResponse aResponse, WOContext aContext) {
		super.appendToResponse(aResponse, aContext);

		// Set default encoding
		// CHECKME: Is this line needed? - probably not.
		aResponse.setContentEncoding(WOMessage.defaultEncoding());

		// We want to return an InputStream always
		InputStream is = null;

		if (fileToDownload() != null) {
			try {
				is = new FileInputStream(fileToDownload());
			} catch (FileNotFoundException e) {
				throw new RuntimeException(e);
			}
			_streamingContentSize = fileToDownload().length();
		}

		if (is == null && inputStreamToDownload() != null) {
			is = inputStreamToDownload();
		}

		if (is == null) {
			throw new IllegalStateException(
					"At least one of 'fileContext', 'fileToDownload' or 'inputStreamToDownload' must be set!");
		}

		// Note, when set to zero, the buffer is assigned a default value,
		// currently 4096.
		aResponse.setContentStream(is, 0, _streamingContentSize);

		// Set content headers
		aResponse.setHeader(contentType(), "content-type");
		aResponse.setHeader(contentDisposition(), "content-disposition");

		log.debug("DownloadResponse = {}", this);
	}

	private String _contentDisposition;

	/**
	 * @return content-disposition header. Defaults to
	 *         <code>"attachment;filename=\"" + downloadFilename() + "\""</code>
	 */
	private String contentDisposition() {
		if (_contentDisposition == null) {
			_contentDisposition = "attachment;filename=\"" + downloadFilename() + "\"";
		}
		return _contentDisposition;
	}

	public void setContentDisposition(String contentDisposition) {
		_contentDisposition = contentDisposition;
	}

	private String _contentType;

	/**
	 * @return content-type header. Defaults to
	 *         <code>"application/octet-stream"</code>
	 */
	private String contentType() {
		if (_contentType == null) {
			_contentType = "application/octet-stream";
		}
		return _contentType;
	}

	public void setContentType(String contentType) {
		_contentType = contentType;
	}

	@Override
	public String toString() {
		ToStringBuilder b = new ToStringBuilder(this);
		b.append("File To Download", _fileToDownload);
		b.append("Stream to Download", _inputStreamToDownload);
		b.append("Download Filename", _downloadFilename);
		b.append("Content Type", _contentType);
		b.append("Content Disposition", _contentDisposition);
		b.append("Content Size", _streamingContentSize);
		return b.toString();
	}

}
