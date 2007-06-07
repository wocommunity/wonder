// _ERCMailMessage.java
// 
// Created by eogenerator
// DO NOT EDIT.  Make changes to ERCMailMessage.java instead.
package er.corebusinesslogic;
import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import er.extensions.*;
import java.util.*;
import java.math.BigDecimal;

public abstract class _ERCMailMessage extends ERCStampedEnterpriseObject {

    public static final String ENTITY = "ERCMailMessage";

    public interface Key extends ERCStampedEnterpriseObject.Key {
        public static final String X_MAILER = "xMailer";
        public static final String TO_ADDRESSES = "toAddresses";
        public static final String TITLE = "title";
        public static final String TEXT_COMPRESSED = "textCompressed";
        public static final String TEXT = "text";
        public static final String STATE = "state";
        public static final String SHOULD_ARCHIVE_SENT_MAIL = "shouldArchiveSentMail";
        public static final String REPLY_TO_ADDRESS = "replyToAddress";
        public static final String PLAIN_TEXT_COMPRESSED = "plainTextCompressed";
        public static final String PLAIN_TEXT = "plainText";
        public static final String LAST_MODIFIED = "lastModified";
        public static final String IS_READ = "isRead";
        public static final String FROM_ADDRESS = "fromAddress";
        public static final String EXCEPTION_REASON = "exceptionReason";
        public static final String DATE_SENT = "dateSent";
        public static final String CREATED = "created";
        public static final String CONTENT_GZIPPED = "contentGzipped";
        public static final String CC_ADDRESSES = "ccAddresses";
        public static final String BCC_ADDRESSES = "bccAddresses";
        public static final String ATTACHMENTS = "attachments";  
    }

    public static abstract class _ERCMailMessageClazz extends ERXGenericRecord.ERXGenericRecordClazz {
 

        public NSArray objectsForMessagesToBeSent(EOEditingContext context) {
            EOFetchSpecification spec = EOFetchSpecification.fetchSpecificationNamed("messagesToBeSent", "ERCMailMessage");

            return context.objectsWithFetchSpecification(spec);
        }

        public NSArray objectsForRipeMessages(EOEditingContext context, NSTimestamp dateBinding) {
            EOFetchSpecification spec = EOFetchSpecification.fetchSpecificationNamed("ripeMessages", "ERCMailMessage");

            NSMutableDictionary bindings = new NSMutableDictionary();

            if (dateBinding != null)
                bindings.setObjectForKey(dateBinding, "date");
            spec = spec.fetchSpecificationWithQualifierBindings(bindings);

            return context.objectsWithFetchSpecification(spec);
        }

    }


    public String bccAddresses() {
        return (String)storedValueForKey(Key.BCC_ADDRESSES);
    }
    public void setBccAddresses(String aValue) {
        takeStoredValueForKey(aValue, Key.BCC_ADDRESSES);
    }

    public String ccAddresses() {
        return (String)storedValueForKey(Key.CC_ADDRESSES);
    }
    public void setCcAddresses(String aValue) {
        takeStoredValueForKey(aValue, Key.CC_ADDRESSES);
    }

    public boolean contentGzipped() {
        return ((Boolean)storedValueForKey(Key.CONTENT_GZIPPED)).booleanValue();
    }
    public void setContentGzipped(boolean aValue) {
        takeStoredValueForKey((aValue ? Boolean.TRUE : Boolean.FALSE), Key.CONTENT_GZIPPED);
    }

    public NSTimestamp created() {
        return (NSTimestamp)storedValueForKey(Key.CREATED);
    }
    public void setCreated(NSTimestamp aValue) {
        takeStoredValueForKey(aValue, Key.CREATED);
    }

    public NSTimestamp dateSent() {
        return (NSTimestamp)storedValueForKey(Key.DATE_SENT);
    }
    public void setDateSent(NSTimestamp aValue) {
        takeStoredValueForKey(aValue, Key.DATE_SENT);
    }

    public String exceptionReason() {
        return (String)storedValueForKey(Key.EXCEPTION_REASON);
    }
    public void setExceptionReason(String aValue) {
        takeStoredValueForKey(aValue, Key.EXCEPTION_REASON);
    }

    public String fromAddress() {
        return (String)storedValueForKey(Key.FROM_ADDRESS);
    }
    public void setFromAddress(String aValue) {
        takeStoredValueForKey(aValue, Key.FROM_ADDRESS);
    }

    public boolean isRead() {
        return ((Boolean)storedValueForKey(Key.IS_READ)).booleanValue();
    }
    public void setIsRead(boolean aValue) {
        takeStoredValueForKey((aValue ? Boolean.TRUE : Boolean.FALSE), Key.IS_READ);
    }

    public NSTimestamp lastModified() {
        return (NSTimestamp)storedValueForKey(Key.LAST_MODIFIED);
    }
    public void setLastModified(NSTimestamp aValue) {
        takeStoredValueForKey(aValue, Key.LAST_MODIFIED);
    }

    public String plainText() {
        return (String)storedValueForKey(Key.PLAIN_TEXT);
    }
    public void setPlainText(String aValue) {
        takeStoredValueForKey(aValue, Key.PLAIN_TEXT);
    }

    public NSData plainTextCompressed() {
        return (NSData)storedValueForKey(Key.PLAIN_TEXT_COMPRESSED);
    }
    public void setPlainTextCompressed(NSData aValue) {
        takeStoredValueForKey(aValue, Key.PLAIN_TEXT_COMPRESSED);
    }

    public String replyToAddress() {
        return (String)storedValueForKey(Key.REPLY_TO_ADDRESS);
    }
    public void setReplyToAddress(String aValue) {
        takeStoredValueForKey(aValue, Key.REPLY_TO_ADDRESS);
    }

    public boolean shouldArchiveSentMail() {
        return ((Boolean)storedValueForKey(Key.SHOULD_ARCHIVE_SENT_MAIL)).booleanValue();
    }
    public void setShouldArchiveSentMail(boolean aValue) {
        takeStoredValueForKey((aValue ? Boolean.TRUE : Boolean.FALSE), Key.SHOULD_ARCHIVE_SENT_MAIL);
    }

    public er.corebusinesslogic.ERCMailState state() {
        return (er.corebusinesslogic.ERCMailState)storedValueForKey(Key.STATE);
    }
    public void setState(er.corebusinesslogic.ERCMailState aValue) {
        takeStoredValueForKey(aValue, Key.STATE);
    }

    public String text() {
        return (String)storedValueForKey(Key.TEXT);
    }
    public void setText(String aValue) {
        takeStoredValueForKey(aValue, Key.TEXT);
    }

    public NSData textCompressed() {
        return (NSData)storedValueForKey(Key.TEXT_COMPRESSED);
    }
    public void setTextCompressed(NSData aValue) {
        takeStoredValueForKey(aValue, Key.TEXT_COMPRESSED);
    }

    public String title() {
        return (String)storedValueForKey(Key.TITLE);
    }
    public void setTitle(String aValue) {
        takeStoredValueForKey(aValue, Key.TITLE);
    }

    public String toAddresses() {
        return (String)storedValueForKey(Key.TO_ADDRESSES);
    }
    public void setToAddresses(String aValue) {
        takeStoredValueForKey(aValue, Key.TO_ADDRESSES);
    }

    public String xMailer() {
        return (String)storedValueForKey(Key.X_MAILER);
    }
    public void setXMailer(String aValue) {
        takeStoredValueForKey(aValue, Key.X_MAILER);
    }

    public NSArray attachments() {
        return (NSArray)storedValueForKey(Key.ATTACHMENTS);
    }
    public void addToAttachments(er.corebusinesslogic.ERCMessageAttachment object) {
        includeObjectIntoPropertyWithKey(object, Key.ATTACHMENTS);
    }
    public void removeFromAttachments(er.corebusinesslogic.ERCMessageAttachment object) {
        excludeObjectFromPropertyWithKey(object, Key.ATTACHMENTS);
    }

}
