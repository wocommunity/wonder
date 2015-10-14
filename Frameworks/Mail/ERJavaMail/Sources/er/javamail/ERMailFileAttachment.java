/*
 ERMailFileAttachment.java - Camille Troillard - tuscland@mac.com
 */

package er.javamail;

import java.io.File;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;

public class ERMailFileAttachment extends ERMailAttachment {

	protected String _fileName;
	protected String _contentID;

	protected ERMailFileAttachment(Object content) {
		super(content);
	}

	public ERMailFileAttachment(String fileName, String id, File content) {
		super(content);
		_fileName = fileName;
		_contentID = id;
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
		DataSource ds = new FileDataSource((File) content());
		bp.setDataHandler(new DataHandler(ds));

		if (contentID() != null)
			bp.setHeader("Content-ID", contentID());
		bp.setFileName(fileName());

		return bp;
	}
}
