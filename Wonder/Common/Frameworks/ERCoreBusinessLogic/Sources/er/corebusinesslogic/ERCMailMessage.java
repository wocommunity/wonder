// ERCMailMessage.java
// (c) by Anjo Krank (ak@kcmedia.ag)
package er.corebusinesslogic;

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import java.util.*;
import java.math.BigDecimal;
import er.extensions.*;

public class ERCMailMessage extends _ERCMailMessage implements /*ERXReadStateTrackedInterface, */ ERXGeneratesPrimaryKeyInterface {
    static final ERXLogger log = ERXLogger.getLogger(ERCMailMessage.class);

    public ERCMailMessage() {
        super();
    }

    public void awakeFromInsertion(EOEditingContext anEditingContext) {
        super.awakeFromInsertion(anEditingContext);
        setState(ERCMailState.READY_TO_BE_SENT_STATE);
        setLastModified(new NSTimestamp());
        setCreated(new NSTimestamp());
    }
    
    
    // Class methods go here
    
    public static class ERCMailMessageClazz extends _ERCMailMessageClazz {
        
    }

    public static ERCMailMessageClazz mailMessageClazz() { return (ERCMailMessageClazz)EOGenericRecordClazz.clazzForEntityNamed("ERCMailMessage"); }



    
    /////////////////////////////////////////////// Instance Methods //////////////////////////////////////////////////////////
    // State Methods
    public String relationshipNameForLogEntry() {  return null; }
    public EOEnterpriseObject logEntryType() 	{  return null; }

    public boolean isReadyToSendState() 	{ return state() == ERCMailState.READY_TO_BE_SENT_STATE; }
    public boolean isSentState() 		{ return state() == ERCMailState.SENT_STATE; }
    public boolean isExceptionState() 		{ return state() == ERCMailState.EXCEPTION_STATE; }
    public boolean isReceivedState() 		{ return state() == ERCMailState.RECEIVED_STATE; }

    public void markReadBy(EOEnterpriseObject by) {
        // this will be useful for marketing to track who opens the emails
       setReadAsBoolean(true);
    }

    public void setReadAsBoolean(boolean read) {
        setIsRead(read ? "Y":"N");
    }
    public boolean isReadAsBoolean() {
        return "Y".equals(isRead());
    }
    
    // Init Method

    public String longDescription() {
        StringBuffer sb=new StringBuffer();
        sb.append("To: ");
        sb.append(toAddresses());
        sb.append("\n");
        sb.append("cc: ");
        sb.append(ccAddresses());
        sb.append("\n");
        sb.append("Created: ");
        sb.append(created());
        sb.append("\n");
        sb.append("Title: ");
        sb.append(title());
        sb.append("\n");
        sb.append("Text: ");
        sb.append(text());
        sb.append("\n");
        return sb.toString();
    }

    // Useful for nested mime messages or multi-part messages
    public void appendText(String text) {
        String storedText = text();
        setText((storedText == null ? "" : storedText) + " " + text);
    }

    public Object validateEmptyStringForKey(Object value, String field) {
        if(value == null || "".equals(value) || ((String)value).length() == 0) {
            throw new ERXValidationException(this, field, value, "null");
        }
        return value;
    }
    
    // Validation Methods
    public Object validateFromAddress(String newValue) {
        return validateEmptyStringForKey(newValue, "fromAddress");
    }

    public Object validateTitle(String newValue) {
        return validateEmptyStringForKey(newValue, "title");
    }
    
    public Object validateToAddresses(String newValue) {
        return validateEmptyStringForKey(newValue, "toAddresses");
    }

    public Object validateText(String newValue) {
        return validateEmptyStringForKey(newValue, "text");
    }

    public void attachFileWithMimeType(String filePath, String mimeType) {
        ERCMessageAttachment attachment = (ERCMessageAttachment)ERXUtilities.createEO("ERCMessageAttachment", editingContext());
        attachment.setFilePath(filePath);
        if(mimeType != null)
            attachment.setMimeType(mimeType);
        addToBothSidesOfAttachments(attachment);
    }
    
}
