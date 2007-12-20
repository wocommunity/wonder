// $LastChangedRevision: 4733 $ DO NOT EDIT.  Make changes to ERCMailMessageArchive.java instead.
package er.corebusinesslogic;

import er.extensions.ERXGenericRecord;
import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;
import java.math.*;
import java.util.*;


@SuppressWarnings("all")
public abstract class _ERCMailMessageArchive extends er.corebusinesslogic.ERCMailMessage {

	public static final String ENTITY_NAME = "ERCMailMessageArchive";

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

    public static class _ERCMailMessageArchiveClazz extends ERCMailMessage.ERCMailMessageClazz {
        /* more clazz methods here */
    }


}
