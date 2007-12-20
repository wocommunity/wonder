// $LastChangedRevision: 4733 $ DO NOT EDIT.  Make changes to ERCMailMessage.java instead.
package er.corebusinesslogic;

import er.extensions.ERXGenericRecord;
import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;
import java.math.*;
import java.util.*;


@SuppressWarnings("all")
public abstract class _ERCMailMessage extends ERXGenericRecord {

	public static final String ENTITY_NAME = "ERCMailMessage";

    public interface Key {
	// Attributes
	   public static final String BCC_ADDRESSES = "bccAddresses";
	   public static final String CC_ADDRESSES = "ccAddresses";
	   public static final String CONTENT_GZIPPED = "contentGzipped";
	   public static final String CREATED = "created";
	   public static final String DATE_SENT = "dateSent";
	   public static final String EXCEPTION_REASON = "exceptionReason";
	   public static final String FROM_ADDRESS = "fromAddress";
	   public static final String IS_READ = "isRead";
	   public static final String LAST_MODIFIED = "lastModified";
	   public static final String PLAIN_TEXT = "plainText";
	   public static final String PLAIN_TEXT_COMPRESSED = "plainTextCompressed";
	   public static final String REPLY_TO_ADDRESS = "replyToAddress";
	   public static final String SHOULD_ARCHIVE_SENT_MAIL = "shouldArchiveSentMail";
	   public static final String STATE = "state";
	   public static final String TEXT = "text";
	   public static final String TEXT_COMPRESSED = "textCompressed";
	   public static final String TITLE = "title";
	   public static final String TO_ADDRESSES = "toAddresses";
	   public static final String X_MAILER = "xMailer";

	// Relationships
	   public static final String ATTACHMENTS = "attachments";
    }

    public static class _ERCMailMessageClazz extends ERXGenericRecord.ERXGenericRecordClazz<ERCMailMessage> {
        /* more clazz methods here */
    }

  public String bccAddresses() {
    return (String) storedValueForKey(Key.BCC_ADDRESSES);
  }
  public void setBccAddresses(String value) {
    takeStoredValueForKey(value, Key.BCC_ADDRESSES);
  }

  public String ccAddresses() {
    return (String) storedValueForKey(Key.CC_ADDRESSES);
  }
  public void setCcAddresses(String value) {
    takeStoredValueForKey(value, Key.CC_ADDRESSES);
  }

  public Boolean contentGzipped() {
    return (Boolean) storedValueForKey(Key.CONTENT_GZIPPED);
  }
  public void setContentGzipped(Boolean value) {
    takeStoredValueForKey(value, Key.CONTENT_GZIPPED);
  }

  public NSTimestamp created() {
    return (NSTimestamp) storedValueForKey(Key.CREATED);
  }
  public void setCreated(NSTimestamp value) {
    takeStoredValueForKey(value, Key.CREATED);
  }

  public NSTimestamp dateSent() {
    return (NSTimestamp) storedValueForKey(Key.DATE_SENT);
  }
  public void setDateSent(NSTimestamp value) {
    takeStoredValueForKey(value, Key.DATE_SENT);
  }

  public String exceptionReason() {
    return (String) storedValueForKey(Key.EXCEPTION_REASON);
  }
  public void setExceptionReason(String value) {
    takeStoredValueForKey(value, Key.EXCEPTION_REASON);
  }

  public String fromAddress() {
    return (String) storedValueForKey(Key.FROM_ADDRESS);
  }
  public void setFromAddress(String value) {
    takeStoredValueForKey(value, Key.FROM_ADDRESS);
  }

  public Boolean isRead() {
    return (Boolean) storedValueForKey(Key.IS_READ);
  }
  public void setIsRead(Boolean value) {
    takeStoredValueForKey(value, Key.IS_READ);
  }

  public NSTimestamp lastModified() {
    return (NSTimestamp) storedValueForKey(Key.LAST_MODIFIED);
  }
  public void setLastModified(NSTimestamp value) {
    takeStoredValueForKey(value, Key.LAST_MODIFIED);
  }

  public String plainText() {
    return (String) storedValueForKey(Key.PLAIN_TEXT);
  }
  public void setPlainText(String value) {
    takeStoredValueForKey(value, Key.PLAIN_TEXT);
  }

  public NSData plainTextCompressed() {
    return (NSData) storedValueForKey(Key.PLAIN_TEXT_COMPRESSED);
  }
  public void setPlainTextCompressed(NSData value) {
    takeStoredValueForKey(value, Key.PLAIN_TEXT_COMPRESSED);
  }

  public String replyToAddress() {
    return (String) storedValueForKey(Key.REPLY_TO_ADDRESS);
  }
  public void setReplyToAddress(String value) {
    takeStoredValueForKey(value, Key.REPLY_TO_ADDRESS);
  }

  public Boolean shouldArchiveSentMail() {
    return (Boolean) storedValueForKey(Key.SHOULD_ARCHIVE_SENT_MAIL);
  }
  public void setShouldArchiveSentMail(Boolean value) {
    takeStoredValueForKey(value, Key.SHOULD_ARCHIVE_SENT_MAIL);
  }

  public er.corebusinesslogic.ERCMailState state() {
    return (er.corebusinesslogic.ERCMailState) storedValueForKey(Key.STATE);
  }
  public void setState(er.corebusinesslogic.ERCMailState value) {
    takeStoredValueForKey(value, Key.STATE);
  }

  public String text() {
    return (String) storedValueForKey(Key.TEXT);
  }
  public void setText(String value) {
    takeStoredValueForKey(value, Key.TEXT);
  }

  public NSData textCompressed() {
    return (NSData) storedValueForKey(Key.TEXT_COMPRESSED);
  }
  public void setTextCompressed(NSData value) {
    takeStoredValueForKey(value, Key.TEXT_COMPRESSED);
  }

  public String title() {
    return (String) storedValueForKey(Key.TITLE);
  }
  public void setTitle(String value) {
    takeStoredValueForKey(value, Key.TITLE);
  }

  public String toAddresses() {
    return (String) storedValueForKey(Key.TO_ADDRESSES);
  }
  public void setToAddresses(String value) {
    takeStoredValueForKey(value, Key.TO_ADDRESSES);
  }

  public String xMailer() {
    return (String) storedValueForKey(Key.X_MAILER);
  }
  public void setXMailer(String value) {
    takeStoredValueForKey(value, Key.X_MAILER);
  }

  public NSArray<er.corebusinesslogic.ERCMessageAttachment> attachments() {
    return (NSArray<er.corebusinesslogic.ERCMessageAttachment>)storedValueForKey(Key.ATTACHMENTS);
  }
  public void addToAttachments(er.corebusinesslogic.ERCMessageAttachment object) {
      includeObjectIntoPropertyWithKey(object, Key.ATTACHMENTS);
  }
  public void removeFromAttachments(er.corebusinesslogic.ERCMessageAttachment object) {
      excludeObjectFromPropertyWithKey(object, Key.ATTACHMENTS);
  }

}
