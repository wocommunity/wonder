package er.attachment.model;

import java.io.File;

import org.apache.log4j.Logger;

import com.amazon.s3.AWSAuthConnection;
import com.amazon.s3.QueryStringAuthGenerator;
import com.webobjects.eocontrol.EOEditingContext;

import er.attachment.upload.ERRemoteAttachment;
import er.extensions.eof.ERXGenericRecord;
import er.extensions.foundation.ERXProperties;

/**
 * <span class="en">
 * ERS3Attachment (type = "s3") represents an attachment whose content is stored on Amazon's S3 service and will be served directly from S3. 
 * This type may eventually support proxying as well, but currently only direct links are enabled.
 * </span>
 * 
 * <span class="ja">
 * ERS3Attachment (type "s3") はアタッチメントが Amazon's S3 サービスに保存されます。
 * S3 より直接共有されます。現在ではダイレクト・リンクのみがサポートされます。
 * </span>
 * 
 * @author mschrag
 */
public class ERS3Attachment extends _ERS3Attachment implements ERRemoteAttachment {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	public static final String STORAGE_TYPE = "s3";
	@SuppressWarnings("unused")
	private static Logger log = Logger.getLogger(ERS3Attachment.class);

	private File _pendingUploadFile;
	private boolean _pendingDelete;

	public void _setPendingUploadFile(File pendingUploadFile, boolean pendingDelete) {
		_pendingUploadFile = pendingUploadFile;
		_pendingDelete = pendingDelete;
	}

	public File _pendingUploadFile() {
		return _pendingUploadFile;
	}

	public boolean _isPendingDelete() {
		return _pendingDelete;
	}
	
	@Override
	public void didCopyFromChildInEditingContext(ERXGenericRecord originalEO, EOEditingContext childEditingContext) {
		super.didCopyFromChildInEditingContext(originalEO, childEditingContext);
		_setPendingUploadFile(((ERS3Attachment) originalEO)._pendingUploadFile(), false);
	}
	
	@Override
	public void awakeFromInsertion(EOEditingContext ec) {
		super.awakeFromInsertion(ec);
		setStorageType(ERS3Attachment.STORAGE_TYPE);
	}

	/**
	 * <span class="en">
	 * Sets the S3 location for this attachment.
	 * 
	 * @param bucket
	 *          the S3 bucket
	 * @param key
	 *          the S3 key
	 * </span>
	 * 
	 * <span class="ja">
   * このアタッチメントの S3 ロケーションをセットします。
   * 
   * @param bucket - S3 のパケット
   * @param key - S3 のキー
   * </span>
	 */
	public void setS3Location(String bucket, String key) {
		setWebPath("/" + bucket + "/" + key);
	}

	/**
	 * <span class="en">
	 * @return the S3 bucket for this attachment.
	 * </span>
	 * 
	 * <span class="ja">
   * このアタッチメントの S3 パケットを戻します。
   * 
   * @return S3 のパケット
   * </span>
	 */
	public String bucket() {
		String[] paths = webPath().split("/");
		String bucket = paths[1];
		return bucket;
	}

	/**
	 * <span class="en">
	 * @return the S3 key for this attachment.
	 * </span>
	 * 
	 * <span class="ja">
   * このアタッチメントの S3 キーを戻します。
   * 
   * @return S3 のキー
   * </span>
	 */
	public String key() {
		// Retrieve the index of the second slash, considering the first char is always a slash
		int indexOfKeySeparator = webPath().indexOf("/", 1);
		String key = webPath().substring(indexOfKeySeparator + 1);
		return key;
	}

	/**
	 * @return the s3 path for this attachment. If the acl for this configuration is 'private' then it serves up a signed url
	 */
	// TODO We should really store the acl value when the attachment is uploaded. If the user subsequently changes this value s3 knows nothing about it and the urls generated may not then be appropriate (ie. a non-signed url may be served up for an
	// attachment that is stored on s3 as 'private').
	@Override
	public String s3Path() {
		String s3Path = super.s3Path();
		if ("private".equals(acl())) {
			// Private, so we need a signed url.
			// Should we really store the acl posture on the DB in case it's subsequently changed ?
			QueryStringAuthGenerator q = queryStringAuthGenerator();
			q.setExpiresIn(linkLife());
			s3Path = q.get(bucket(), key(), null);
		}
		return s3Path;
	}

	public Integer linkLife() {
		String linkLife = ERXProperties.decryptedStringForKey("er.attachment." + configurationName() + ".s3.linkLife");
		if (linkLife == null) {
			linkLife = ERXProperties.decryptedStringForKey("er.attachment.s3.linkLife");
		}
		if (linkLife == null) {
			linkLife = "60000";
		}
		return Integer.valueOf(linkLife);
	}

	public String accessKeyID() {
		String accessKeyID = ERXProperties.decryptedStringForKey("er.attachment." + configurationName() + ".s3.accessKeyID");
		if (accessKeyID == null) {
			accessKeyID = ERXProperties.decryptedStringForKey("er.attachment.s3.accessKeyID");
		}
		if (accessKeyID == null) {
			throw new IllegalArgumentException("There is no 'er.attachment." + configurationName() + ".s3.accessKeyID' or 'er.attachment.s3.accessKeyID' property set.");
		}
		return accessKeyID;
	}

	public String secretAccessKey() {
		String secretAccessKey = ERXProperties.decryptedStringForKey("er.attachment." + configurationName() + ".s3.secretAccessKey");
		if (secretAccessKey == null) {
			secretAccessKey = ERXProperties.decryptedStringForKey("er.attachment.s3.secretAccessKey");
		}
		if (secretAccessKey == null) {
			throw new IllegalArgumentException("There is no 'er.attachment." + configurationName() + ".s3.secretAccessKey' or 'er.attachment.s3.secretAccessKey' property set.");
		}
		return secretAccessKey;
	}

	public String acl() {
		String acl = ERXProperties.decryptedStringForKey("er.attachment." + configurationName() + ".s3.acl");
		if (acl == null) {
			acl = ERXProperties.decryptedStringForKey("er.attachment.s3.acl");
		}
		if (acl == null) {
			acl = "public-read";
		}
		return acl;
	}

	public QueryStringAuthGenerator queryStringAuthGenerator() {
		String host = ERXProperties.stringForKey("er.attachment." + configurationName() + ".s3.host");
		if (host == null) {
			host = ERXProperties.stringForKey("er.attachment.s3.host");
		}
		if (host == null)
			return new QueryStringAuthGenerator(accessKeyID(), secretAccessKey(), false);
		else
			return new QueryStringAuthGenerator(accessKeyID(), secretAccessKey(), false, host);
	}

	public AWSAuthConnection awsConnection() {
		String host = ERXProperties.stringForKey("er.attachment." + configurationName() + ".s3.host");
		if (host == null) {
			host = ERXProperties.stringForKey("er.attachment.s3.host");
		}
		if (host == null)
			return new AWSAuthConnection(accessKeyID(), secretAccessKey(), true);
		else
			return new AWSAuthConnection(accessKeyID(), secretAccessKey(), true, host);
	}
}
