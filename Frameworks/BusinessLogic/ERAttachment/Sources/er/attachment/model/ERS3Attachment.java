package er.attachment.model;

import java.io.File;

import org.apache.log4j.Logger;

import com.amazon.s3.AWSAuthConnection;
import com.amazon.s3.QueryStringAuthGenerator;
import com.webobjects.eocontrol.EOEditingContext;

import er.extensions.foundation.ERXProperties;

/**
 * ERS3Attachment (type = "s3") represents an attachment whose content is stored on Amazon's S3 service and will be served directly from S3. This type may eventually support proxying as well, but currently only direct links are enabled.
 * 
 * @author mschrag
 */
public class ERS3Attachment extends _ERS3Attachment {
	public static final String STORAGE_TYPE = "s3";
	private static Logger log = Logger.getLogger(ERS3Attachment.class);

	private File _pendingUploadFile;
	private boolean _pendingDelete;

	public ERS3Attachment() {
	}

	public void _setPendingUploadFile(File pendingUploadFile, boolean pendingDelete) {
		_pendingUploadFile = pendingUploadFile;
	}

	public File _pendingUploadFile() {
		return _pendingUploadFile;
	}

	public boolean _isPendingDelete() {
		return _pendingDelete;
	}

	@Override
	public void awakeFromInsertion(EOEditingContext ec) {
		super.awakeFromInsertion(ec);
		setStorageType(ERS3Attachment.STORAGE_TYPE);
	}

	/**
	 * Sets the S3 location for this attachment.
	 * 
	 * @param bucket
	 *          the S3 bucket
	 * @param key
	 *          the S3 key
	 */
	public void setS3Location(String bucket, String key) {
		setWebPath("/" + bucket + "/" + key);
	}

	/**
	 * @return the S3 bucket for this attachment.
	 */
	public String bucket() {
		String[] paths = webPath().split("/");
		String bucket = paths[1];
		return bucket;
	}

	/**
	 * @return the S3 key for this attachment.
	 */
	public String key() {
		String[] paths = webPath().split("/");
		String key = paths[2];
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
		return new QueryStringAuthGenerator(accessKeyID(), secretAccessKey(), false);
	}

	public AWSAuthConnection awsConnection() {
		AWSAuthConnection conn = new AWSAuthConnection(accessKeyID(), secretAccessKey(), true);
		return conn;
	}
}
