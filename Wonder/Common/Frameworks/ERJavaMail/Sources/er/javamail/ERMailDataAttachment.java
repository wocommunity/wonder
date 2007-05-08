package er.javamail;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileTypeMap;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.util.ByteArrayDataSource;

import com.webobjects.foundation.NSData;

/**
 * Attaches a byte array or NSData to a mail. The mime type is pulled form the file name, so be sure to provide a
 * reasonable one.
 * 
 * 
 * @author ak
 */
public class ERMailDataAttachment extends ERMailAttachment {

	protected String _fileName;
	protected String _contentID;
	protected String _mimeType;
	protected DataHandler _dataHandler;

	protected ERMailDataAttachment(Object content) {
		super(content);
	}

	public ERMailDataAttachment(String fileName, String id, NSData content) {
		this(fileName, id, content._bytesNoCopy());
	}

	public ERMailDataAttachment(String fileName, String id, byte content[]) {
		super(content);
		_mimeType = FileTypeMap.getDefaultFileTypeMap().getContentType(fileName);
		this.setFileName(fileName);
		this.setContentID(id);
	}
	
	public ERMailDataAttachment(String aFilename, String anId, DataHandler aHandler)  {
		super(null);
		this.setFileName(aFilename);
		this.setContentID(anId);
		_dataHandler = aHandler;
		_mimeType = FileTypeMap.getDefaultFileTypeMap().getContentType(aFilename);
	}

	public String fileName() {
		if (_fileName == null)
			_fileName = "attachement.txt";
		return _fileName;
	}

	public DataHandler getDataHandler() {
		return _dataHandler;
	}

	public void setFileName(String name) {
		_fileName = name;
	}

	public String contentID() {
		return _contentID;
	}

	public void setContentID(String id) {
		_contentID = id;
	}

	protected BodyPart getBodyPart() throws MessagingException {
		MimeBodyPart bp = new MimeBodyPart();
		if (getDataHandler() == null) {
			DataSource ds = new ByteArrayDataSource((byte[]) content(), _mimeType);
			bp.setDataHandler(new DataHandler(ds));
		} else {
			bp.setDataHandler(getDataHandler());
			if (_mimeType != null) {
				bp.setHeader("Content-type", _mimeType);
			}
		}

		if (this.contentID() != null) {
			bp.setHeader("Content-ID", this.contentID());
		}
		bp.setFileName(this.fileName());
		return bp;
	}
}
