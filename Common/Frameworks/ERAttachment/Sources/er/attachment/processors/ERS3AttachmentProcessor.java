package er.attachment.processors;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.amazon.s3.AWSAuthConnection;
import com.amazon.s3.QueryStringAuthGenerator;
import com.amazon.s3.Response;
import com.silvasoftinc.s3.S3StreamObject;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.eocontrol.EOEditingContext;

import er.attachment.model.ERS3Attachment;
import er.extensions.ERXProperties;

/**
 * ERS3AttachmentProcessor implements storing attachments in Amazon's S3 service.  For more 
 * information about configuring an ERS3AttachmentProcessor, see the top level documentation.
 * 
 * @author mschrag
 */
public class ERS3AttachmentProcessor extends ERAttachmentProcessor<ERS3Attachment> {
  public static final String S3_URL = "http://s3.amazonaws.com";

  @Override
  public ERS3Attachment _process(EOEditingContext editingContext, File uploadedFile, String recommendedFileName, String mimeType, String configurationName) throws IOException {
    String accessKeyID = ERXProperties.decryptedStringForKey("er.attachment.s3." + configurationName + ".accessKeyID");
    if (accessKeyID == null) {
      accessKeyID = ERXProperties.decryptedStringForKey("er.attachment.s3.accessKeyID");
    }
    if (accessKeyID == null) {
      throw new IllegalArgumentException("There is no 'er.attachment.s3." + configurationName + ".accessKeyID' or 'er.attachment.s3.accessKeyID' property set.");
    }

    String secretAccessKey = ERXProperties.decryptedStringForKey("er.attachment.s3." + configurationName + ".secretAccessKey");
    if (secretAccessKey == null) {
      secretAccessKey = ERXProperties.decryptedStringForKey("er.attachment.s3.secretAccessKey");
    }
    if (secretAccessKey == null) {
      throw new IllegalArgumentException("There is no 'er.attachment.s3." + configurationName + ".secretAccessKey' or 'er.attachment.s3.secretAccessKey' property set.");
    }

    String bucket = ERXProperties.decryptedStringForKey("er.attachment.s3." + configurationName + ".bucket");
    if (bucket == null) {
      bucket = ERXProperties.decryptedStringForKey("er.attachment.s3.bucket");
    }
    if (bucket == null) {
      throw new IllegalArgumentException("There is no 'er.attachment.s3." + configurationName + ".bucket' or 'er.attachment.s3.bucket' property set.");
    }

    String keyTemplate = ERXProperties.stringForKey("er.attachment.s3." + configurationName + ".key");
    if (keyTemplate == null) {
      keyTemplate = ERXProperties.stringForKey("er.attachment.s3.key");
    }
    if (keyTemplate == null) {
      keyTemplate = "${pk}${ext}";
    }

    ERS3Attachment attachment = ERS3Attachment.createERS3Attachment(editingContext, mimeType, recommendedFileName, Boolean.FALSE, Integer.valueOf((int) uploadedFile.length()), null);
    try {
      String key = ERAttachmentProcessor._parsePathTemplate(attachment, keyTemplate, recommendedFileName);
      attachment.setWebPath("/" + bucket + "/" + key);

      AWSAuthConnection conn = new AWSAuthConnection(accessKeyID, secretAccessKey, true);
      FileInputStream attachmentFileInputStream = new FileInputStream(uploadedFile);
      BufferedInputStream attachmentInputStream = new BufferedInputStream(attachmentFileInputStream);
      try {
        S3StreamObject attachmentStreamObject = new S3StreamObject(attachmentInputStream, null);

        Map<String, List<String>> headers = new TreeMap<String, List<String>>();
        headers.put("Content-Type", Arrays.asList(new String[] { attachment.mimeType() }));
        headers.put("Content-Length", Arrays.asList(new String[] { String.valueOf(attachment.size()) }));
        headers.put("x-amz-acl", Arrays.asList(new String[] { "public-read" }));
        Response response = conn.putStream(bucket, key, attachmentStreamObject, headers);
        int responseCode = response.connection.getResponseCode();
        if (responseCode < 200 || responseCode >= 300) {
          String responseMessage = response.connection.getResponseMessage();
          throw new IOException("Failed to write '" + bucket + "/" + key + "' to S3: Error " + responseCode + ": " + responseMessage);
        }
      }
      finally {
        attachmentInputStream.close();
      }

      String s3Path = new QueryStringAuthGenerator(accessKeyID, secretAccessKey, false).makeBareURL(bucket, key);
      attachment.setS3Path(s3Path);
    }
    catch (IOException e) {
      attachment.delete();
      throw e;
    }
    catch (RuntimeException e) {
      attachment.delete();
      throw e;
    }
    finally {
      uploadedFile.delete();
    }
    
    return attachment;
  }

  @Override
  public InputStream attachmentInputStream(ERS3Attachment attachment) throws IOException {
    return new URL(attachment.s3Path()).openStream();
  }

  @Override
  public String attachmentUrl(ERS3Attachment attachment, WORequest request, WOContext context, String configurationName) {
    String attachmentUrl = attachment.s3Path();
    return attachmentUrl;
  }
}