// ERCMailMessage.java
// (c) by Anjo Krank (ak@kcmedia.ag)
package er.corebusinesslogic;

import org.apache.log4j.Logger;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSTimestamp;
import com.webobjects.foundation.NSValidation;

import er.extensions.eof.EOEnterpriseObjectClazz;
import er.extensions.eof.ERXEOControlUtilities;
import er.extensions.eof.ERXFetchSpecificationBatchIterator;
import er.extensions.foundation.ERXCompressionUtilities;
import er.extensions.foundation.ERXProperties;
import er.extensions.foundation.ERXValueUtilities;
import er.extensions.validation.ERXValidationFactory;

/**
 *
 * @property er.corebusinesslogic.ERCMailMessage.ShouldArchive
 * @property er.corebusinesslogic.ERCMailMessage.ShouldGzipContent
 */
public class ERCMailMessage extends _ERCMailMessage {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;


    //	===========================================================================
    //	Class Constant(s)
    //	---------------------------------------------------------------------------
        
    /** logging support */
    public static final Logger log = Logger.getLogger(ERCMailMessage.class);

    /** holds the address separator */
    public static final String AddressSeparator = ",";

    //	===========================================================================
    //	Clazz Object(s)
    //	---------------------------------------------------------------------------    
    
    /**
     * Clazz object used to hold all clazz related methods.
     */
    public static class ERCMailMessageClazz extends _ERCMailMessageClazz {

        /**
         * Gets an iterator for batching through un sent messages.
         * @return batch iterator for messages to be sent
         */
        public ERXFetchSpecificationBatchIterator batchIteratorForUnsentMessages() {
            EOFetchSpecification fetchSpec = EOFetchSpecification.fetchSpecificationNamed("messagesToBeSent",
                                                                                          "ERCMailMessage");
            return new ERXFetchSpecificationBatchIterator(fetchSpec);
        }
    }

    //	===========================================================================
    //	Class Method(s)
    //	---------------------------------------------------------------------------

    /**
     * Gets the singleton clazz object for this Class.
     * @return sigleton clazz object
     */
    public static ERCMailMessageClazz mailMessageClazz() {
        return (ERCMailMessageClazz)EOEnterpriseObjectClazz.clazzForEntityNamed("ERCMailMessage");
    }

    //	===========================================================================
    //	Instance Constructor(s)
    //	---------------------------------------------------------------------------
    
    /**
     * Public constructor.
     */
    public ERCMailMessage() {
        super();
    }

    //	===========================================================================
    //	Instance Method(s)
    //	---------------------------------------------------------------------------    
    
    /**
     * Default state of the mail message is
     * 'Ready To Be Sent'.
     * @param anEditingContext inserted into
     */
    @Override
    public void init(EOEditingContext anEditingContext) {
        super.init(anEditingContext);
        setState(ERCMailState.READY_TO_BE_SENT_STATE);
        
        boolean shouldArchive = ERXProperties.booleanForKeyWithDefault("er.corebusinesslogic.ERCMailMessage.ShouldArchive", false);
        setShouldArchiveSentMail(shouldArchive);
        
        boolean shouldZip = ERXProperties.booleanForKeyWithDefault("er.corebusinesslogic.ERCMailMessage.ShouldGzipContent", true);
        setContentGzipped(shouldZip);       
        
        setIsRead(false);
    }
        
    // State Methods
    public boolean isReadyToSendState() {
		return state() == ERCMailState.READY_TO_BE_SENT_STATE;
	}

	public boolean isSentState() {
		return state() == ERCMailState.SENT_STATE;
	}

	public boolean isExceptionState() {
		return state() == ERCMailState.EXCEPTION_STATE;
	}

	public boolean isReceivedState() {
		return state() == ERCMailState.RECEIVED_STATE;
	}

    // IMPLEMENTME: MarkReadInterface
    public void markReadBy(EOEnterpriseObject by) {
    	setIsRead(true);
    }

    /**
     * Use setIsRead(boolean)
     * @deprecated use {@link #setIsRead(Boolean)}
     */
    @Deprecated
    public void setReadAsBoolean(boolean read) {
        setIsRead(read);
    }
    public boolean isReadAsBoolean() {
        return ERXValueUtilities.booleanValue(isRead());
    }

    public NSArray toAddressesAsArray() {
        return toAddresses() != null ? NSArray.componentsSeparatedByString(toAddresses(), ",") : NSArray.EmptyArray;
    }

    public void setToAddressesAsArray(NSArray toAddresses) {
        if (toAddresses != null && toAddresses.count() > 0) {
            setToAddresses(toAddresses.componentsJoinedByString(AddressSeparator));
        }
    }

    public NSArray ccAddressesAsArray() {
        return ccAddresses() != null ? NSArray.componentsSeparatedByString(ccAddresses(), ",") : NSArray.EmptyArray;
    }

    public void setCcAddressesAsArray(NSArray ccAddresses) {
        if (ccAddresses != null && ccAddresses.count() > 0) {
            setCcAddresses(ccAddresses.componentsJoinedByString(AddressSeparator));
        }
    }

    public NSArray bccAddressesAsArray() {
        return bccAddresses() != null ? NSArray.componentsSeparatedByString(bccAddresses(), ",") : NSArray.EmptyArray;
    }

    public void setBccAddressesAsArray(NSArray bccAddresses) {
        if (bccAddresses != null && bccAddresses.count() > 0) {
            setBccAddresses(bccAddresses.componentsJoinedByString(AddressSeparator));
        }
    }

    public boolean shouldArchiveSentMailAsBoolean() {
        return ERXValueUtilities.booleanValue(shouldArchiveSentMail());
    }
    
    /**
     * Long description of the mail message.
     * @return very verbose description of the mail message.
     */
    public String longDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append("To: ");
        sb.append(toAddresses());
        sb.append('\n');
        sb.append("cc: ");
        sb.append(ccAddresses());
        sb.append('\n');
        sb.append("Created: ");
        sb.append(created());
        sb.append('\n');
        sb.append("Title: ");
        sb.append(title());
        sb.append('\n');
        sb.append("Text: ");
        sb.append(text());
        sb.append('\n');
        return sb.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("To: ");
        sb.append(toAddresses());
        sb.append('\n');
        sb.append("cc: ");
        sb.append(ccAddresses());
        sb.append('\n');
        sb.append("Created: ");
        sb.append(created());
        sb.append('\n');
        sb.append("Title: ");
        sb.append(title());
        return sb.toString();
    }

    @Override
    public String toLongString() {
        return toString();
    }
    
    public ERCMailMessage archive() {
        return (ERCMailMessage)ERXEOControlUtilities.createAndInsertObject(editingContext(),
                                                                           "ERCMailMessageArchive",
                                                                           snapshot());
    }
    
    /**
     * Appends test to the currently stored text.
     * Useful for nested mime messages or multi-part messages.
     * @param text to be appended
     */
    public void appendText(String text) {
        String storedText = text();
        setText((storedText == null ? "" : storedText) + " " + text);
    }

    public Object validateEmptyStringForKey(Object value, String field) {
        if(value == null || "".equals(value) || ((String)value).length() == 0) {
            NSValidation.ValidationException e = ERXValidationFactory.defaultFactory().createCustomException(this, field, value, "empty" + field);
            throw e;
        }
        return value;
    }
    
    /**
     * Simple test if an attachment has any attachments.
     * @return if the the message has any attachments
     */
    public boolean hasAttachments() { return attachments().count() > 0; }

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

    @Override
    public void validateForSave() throws NSValidation.ValidationException {
        final String text = text();
        final String plainText = plainText();
        
        super.validateForSave();
        
        if ( (text == null || text.length() == 0) && (plainText == null || plainText.length() == 0) )
            throw ERXValidationFactory.defaultFactory().createException(this, "plainText,text", text, "eitherPlainTextOrText");
    }

    public void attachFileWithMimeType(String filePath, String mimeType) {
        ERCMessageAttachment attachment = (ERCMessageAttachment)ERXEOControlUtilities.createAndInsertObject(editingContext(),
                                                                                                            "ERCMessageAttachment");
        attachment.setFilePath(filePath);
        if(mimeType != null)
            attachment.setMimeType(mimeType);
        addToBothSidesOfAttachments(attachment);
    }    
    
    public void addToBothSidesOfAttachments(ERCMessageAttachment attachement) {
    	addObjectToBothSidesOfRelationshipWithKey(attachement, Key.ATTACHMENTS);
    }
    
    public String storedGzippedValueForKey(String key) {
        NSData data = (NSData)storedValueForKey(key);
        String value = null;
        if (data != null && data.bytes().length > 0) {
            value = ERXCompressionUtilities.gunzipByteArrayAsString(data.bytes());
        }
        return value;
    }

    public void takeStoredGzippedValueForKey(String aValue, String key) {
        NSData valueToSet = null;
        
        if ( aValue != null ) {
            byte bytes[] = ERXCompressionUtilities.gzipStringAsByteArray(aValue);
            
            if (bytes.length > 0) {
                valueToSet = new NSData(bytes);
            }
        }
        
        takeStoredValueForKey(valueToSet, key);
    }

    @Override
    public String text() {
    	String value = null;
    	if (contentGzipped()) {
    		value = storedGzippedValueForKey("textCompressed");
    	} else {
    		value = (String)storedValueForKey(Key.TEXT);
    	}
    	return value;
    }
    
    @Override
    public void setText(String aValue) {
    	if (contentGzipped()) {
    		takeStoredGzippedValueForKey(aValue, "textCompressed");
    	} else {
    		takeStoredValueForKey(aValue, Key.TEXT);            
    	}
    }
    
    @Override
    public void willInsert() {
    	super.willInsert();
    	NSTimestamp now = new NSTimestamp();
    	setCreated(now);
    	setLastModified(now);
    }
    
    @Override
    public void willUpdate() {
    	super.willUpdate();
    	setLastModified(new NSTimestamp());
    }
}
