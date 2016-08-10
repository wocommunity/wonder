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
 * <span class="en">
 * Attaches a byte array or NSData to a mail. The mime type is pulled from the file name, so be sure to provide a
 * reasonable one.
 * </span>
 * 
 * <span class="ja">
 * byte 配列、又は NSData をメールに添付します。
 * mime タイプはファイル名より取得され、正しいファイル名を渡す必要がある。
 * </span>
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
		_fileName = fileName;
		_contentID = id;
	}
	
	public ERMailDataAttachment(String fileName, String id, DataHandler aHandler)  {
		super(null);
		_fileName = fileName;
		_contentID = id;
		_dataHandler = aHandler;
		_mimeType = FileTypeMap.getDefaultFileTypeMap().getContentType(fileName);
	}

	/**
	 * <span class="ja">
	 * ファイル名を取得します。
	 * 設定されていなければ、"attachement.txt" が戻ります。
	 * 
	 * @return ファイル名
	 * </span>
	 */
	public String fileName() {
		if (_fileName == null)
			_fileName = "attachement.txt";
		return _fileName;
	}

	/**
	 * <span class="ja">
	 * DataHandler を取得します。
	 * 
	 * @return DataHandler
	 * </span>
	 */
	public DataHandler getDataHandler() {
		return _dataHandler;
	}

	/**
	 * <span class="ja">
	 * ファイル名をセットします。
	 * 
	 * @param name - ファイル名
	 * </span>
	 */
	public void setFileName(String name) {
		_fileName = name;
	}

	/**
	 * <span class="ja">
	 * コンテント ID を取得します。
	 * 
	 * @return コンテント ID
	 * </span>
	 */
	public String contentID() {
		return _contentID;
	}

	/**
	 * <span class="ja">
	 * コンテント ID をセットします。
	 * 
	 * @param id - コンテント ID
	 * </span>
	 */
	public void setContentID(String id) {
		_contentID = id;
	}

	@Override
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

		if (contentID() != null) {
			bp.setHeader("Content-ID", contentID());
		}
		bp.setFileName(fileName());
		return bp;
	}
}
