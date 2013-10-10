package er.attachment.processors;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.amazon.s3.AWSAuthConnection;
import com.amazon.s3.Response;
import com.silvasoftinc.s3.S3StreamObject;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSTimestamp;

import er.attachment.ERAttachmentRequestHandler;
import er.attachment.model.ERS3Attachment;
import er.extensions.concurrency.ERXAsyncQueue;
import er.extensions.eof.ERXEC;
import er.extensions.foundation.ERXExceptionUtilities;
import er.extensions.foundation.ERXProperties;

/**
 * ERS3AttachmentProcessor implements storing attachments in Amazon's S3
 * service. For more information about configuring an ERS3AttachmentProcessor,
 * see the top level documentation.
 * 
 * @property er.attachment.[configurationName].s3.bucket
 * @property er.attachment.s3.bucket
 * @property er.attachment.[configurationName].s3.key
 * @property er.attachment.s3.key
 * @property er.attachment.[configurationName].s3.accessKeyID
 * @property er.attachment.s3.accessKeyID
 * @property er.attachment.[configurationName].s3.secretAccessKey
 * @property er.attachment.s3.secretAccessKey
 * @author mschrag
 */
public class ERS3AttachmentProcessor extends
		ERAttachmentProcessor<ERS3Attachment> {
	public static final String S3_URL = "http://s3.amazonaws.com";

	private ERS3UploadQueue _queue;

	public ERS3AttachmentProcessor() {
		_queue = new ERS3UploadQueue();
		_queue.start();
	}

	@Override
	public ERS3Attachment _process(EOEditingContext editingContext,
			File uploadedFile, String recommendedFileName, String mimeType,
			String configurationName, String ownerID, boolean pendingDelete) {
		boolean proxy = true;
		String proxyStr = ERXProperties.stringForKey("er.attachment."
				+ configurationName + ".s3.proxy");
		if (proxyStr == null) {
			proxyStr = ERXProperties.stringForKey("er.attachment.s3.proxy");
		}
		if (proxyStr != null) {
			proxy = Boolean.parseBoolean(proxyStr);
		}

		String bucket = ERXProperties.decryptedStringForKey("er.attachment."
				+ configurationName + ".s3.bucket");
		if (bucket == null) {
			bucket = ERXProperties
					.decryptedStringForKey("er.attachment.s3.bucket");
		}
		if (bucket == null) {
			throw new IllegalArgumentException("There is no 'er.attachment."
					+ configurationName
					+ ".s3.bucket' or 'er.attachment.s3.bucket' property set.");
		}

		String keyTemplate = ERXProperties.stringForKey("er.attachment."
				+ configurationName + ".s3.key");
		if (keyTemplate == null) {
			keyTemplate = ERXProperties.stringForKey("er.attachment.s3.key");
		}
		if (keyTemplate == null) {
			keyTemplate = "${pk}${ext}";
		}

		ERS3Attachment attachment = ERS3Attachment.createERS3Attachment(
				editingContext, Boolean.FALSE, new NSTimestamp(), mimeType,
				recommendedFileName, proxy,
				Integer.valueOf((int) uploadedFile.length()), null);
		if (delegate() != null) {
			delegate().attachmentCreated(this, attachment);
		}
		try {
			String key = ERAttachmentProcessor._parsePathTemplate(attachment,
					keyTemplate, recommendedFileName);

			attachment.setS3Location(bucket, key);
			attachment.setConfigurationName(configurationName);
			String s3Path = attachment.queryStringAuthGenerator().makeBareURL(
					bucket, key);
			attachment.setS3Path(s3Path);

			attachment._setPendingUploadFile(uploadedFile, pendingDelete);

		} catch (RuntimeException e) {
			attachment.delete();
			if (pendingDelete) {
				uploadedFile.delete();
			}
			throw e;
		}

		return attachment;
	}

	@Override
	public InputStream attachmentInputStream(ERS3Attachment attachment)
			throws IOException {
		return new URL(attachment.s3Path()).openStream();
	}

	@Override
	public String attachmentUrl(ERS3Attachment attachment, WORequest request,
			WOContext context) {
		String attachmentUrl = attachment.s3Path();

		if (attachment.proxied()) {

			if (!attachment.acl().equals("private")) {
				log.warn("You are proxying an s3 attachment but do not have the attachment configured for private acl. This likely means the s3 attachment is publically readable via s3, and therefore I'm wondering why you are proxying it through your app. You should either change the acl configuraiton for this attachment to 'private', or why not just serve the attachment up directly from s3 ?");
			}

			// drop the AWS domain
			try {
				attachmentUrl = (new URL(attachmentUrl)).getPath();
				attachmentUrl = "id/" + attachment.primaryKey() + attachmentUrl;
				attachmentUrl = context.urlWithRequestHandlerKey(
						ERAttachmentRequestHandler.REQUEST_HANDLER_KEY,
						attachmentUrl, null);
			} catch (MalformedURLException e) {
				log.fatal(
						"attachment.s3Path() is returning something that isn't a valid URl. This is a bt strange. I'm going to reutrn it in it's raw format which will result in either a 'url cannot be found' error or may result in a 403 from s3.",
						e);
			}

		}

		return attachmentUrl;

	}

	@Override
	public void deleteAttachment(ERS3Attachment attachment)
			throws MalformedURLException, IOException {
		AWSAuthConnection conn = attachment.awsConnection();
		String bucket = attachment.bucket();
		String key = attachment.key();
		Response response = conn.delete(bucket, key, null);
		if (failed(response)) {
			throw new IOException("Failed to delete '" + bucket + "/" + key
					+ "' to S3: Error " + response.connection.getResponseCode()
					+ ": " + response.connection.getResponseMessage());
		}
	}

	@Override
	public void attachmentInserted(ERS3Attachment attachment) {
		super.attachmentInserted(attachment);
		_queue.enqueue(attachment);
	}

	public void performUpload(File uploadedFile, String originalFileName,
			String bucket, String key, String mimeType,
			ERS3Attachment attachment) throws MalformedURLException,
			IOException {
		try {
			AWSAuthConnection conn = attachment.awsConnection();
			FileInputStream attachmentFileInputStream = new FileInputStream(
					uploadedFile);
			BufferedInputStream attachmentInputStream = new BufferedInputStream(
					attachmentFileInputStream);
			try {
				S3StreamObject attachmentStreamObject = new S3StreamObject(
						attachmentInputStream, null);

				Map<String, List<String>> headers = new TreeMap<String, List<String>>();
				headers.put("Content-Type",
						Arrays.asList(new String[] { mimeType }));
				headers.put("Content-Length", Arrays
						.asList(new String[] { String.valueOf(uploadedFile
								.length()) }));
				headers.put("x-amz-acl",
						Arrays.asList(new String[] { attachment.acl() }));

				if (originalFileName != null) {
					headers.put("Content-Disposition", Arrays
							.asList(new String[] { "attachment; filename="
									+ originalFileName }));
				}

				Response response = conn.putStream(bucket, key,
						attachmentStreamObject, headers);
				if (failed(response)) {
					throw new IOException("Failed to write '" + bucket + "/"
							+ key + "' to S3: Error "
							+ response.connection.getResponseCode() + ": "
							+ response.connection.getResponseMessage());
				}
			} finally {
				attachmentInputStream.close();
			}
		} finally {
			if (attachment._isPendingDelete()) {
				uploadedFile.delete();
			}
		}

	}

	protected boolean failed(Response response) throws IOException {
		int responseCode = response.connection.getResponseCode();
		return (responseCode < 200 || responseCode >= 300);
	}

	public class ERS3QueueEntry {
		private File _uploadedFile;
		private ERS3Attachment _attachment;

		public ERS3QueueEntry(File uploadedFile, ERS3Attachment attachment) {
			_uploadedFile = uploadedFile;
			_attachment = attachment;
		}

		public File uploadedFile() {
			return _uploadedFile;
		}

		public ERS3Attachment attachment() {
			return _attachment;
		}
	}

	public class ERS3UploadQueue extends ERXAsyncQueue<ERS3QueueEntry> {
		private EOEditingContext _editingContext;

		public ERS3UploadQueue() {
			super("ERS3AsyncQueue");
			_editingContext = ERXEC.newEditingContext();
		}

		public void enqueue(ERS3Attachment attachment) {
			_editingContext.lock();
			try {
				ERS3Attachment localAttachment = attachment
						.localInstanceIn(_editingContext);
				ERS3QueueEntry entry = new ERS3QueueEntry(
						attachment._pendingUploadFile(), localAttachment);
				enqueue(entry);
			} finally {
				_editingContext.unlock();
			}
		}

		@Override
		public void process(ERS3QueueEntry object) {
			ERS3Attachment attachment = object.attachment();

			File uploadedFile = object.uploadedFile();
			if (uploadedFile != null && uploadedFile.exists()) {
				String bucket;
				String key;
				String mimeType;
				String configurationName;
				String originalFileName = null;

				_editingContext.lock();
				try {
					bucket = attachment.bucket();
					key = attachment.key();
					mimeType = attachment.mimeType();
					configurationName = attachment.configurationName();
					if (proxyAsAttachment(attachment)) {
						originalFileName = attachment.originalFileName();
					}
				} finally {
					_editingContext.unlock();
				}

				try {
					performUpload(uploadedFile, originalFileName,
									bucket, key, mimeType, attachment);

					_editingContext.lock();
					try {
						attachment.setAvailable(Boolean.TRUE);
						_editingContext.saveChanges();
					} finally {
						_editingContext.unlock();
					}
					if (delegate() != null) {
						delegate()
								.attachmentAvailable(
										ERS3AttachmentProcessor.this,
										attachment);
					}
				} catch (Throwable t) {
					if (delegate() != null) {
						delegate()
								.attachmentNotAvailable(
										ERS3AttachmentProcessor.this,
										attachment,
										ERXExceptionUtilities.toParagraph(t));
					}
					ERAttachmentProcessor.log.error("Failed to upload '"
							+ uploadedFile + "' to S3.", t);
				} finally {
					if (attachment._isPendingDelete()) {
						uploadedFile.delete();
					}
				}
			} else {
				if (delegate() != null) {
					delegate()
							.attachmentNotAvailable(
									ERS3AttachmentProcessor.this,
									attachment,
									"Missing attachment file '" + uploadedFile
											+ "'.");
				}
				ERAttachmentProcessor.log.error("Missing attachment file '"
						+ uploadedFile + "'.");
			}
		}
	}

}
