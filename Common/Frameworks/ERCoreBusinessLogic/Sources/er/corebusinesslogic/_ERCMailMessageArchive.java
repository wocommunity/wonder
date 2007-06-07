// _ERCMailMessageArchive.java
// 
// Created by eogenerator
// DO NOT EDIT.  Make changes to ERCMailMessageArchive.java instead.
package er.corebusinesslogic;
import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import er.extensions.*;
import java.util.*;
import java.math.BigDecimal;

public abstract class _ERCMailMessageArchive extends er.corebusinesslogic.ERCMailMessage {

    public static final String ENTITY = "ERCMailMessageArchive";

    public interface Key extends er.corebusinesslogic.ERCMailMessage.Key {
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

    public static abstract class _ERCMailMessageArchiveClazz extends ERXGenericRecord.ERXGenericRecordClazz {
 

    }

}
