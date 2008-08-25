// ERCMailMessage.java
// (c) by Anjo Krank (ak@kcmedia.ag)
package er.corebusinesslogic;

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import java.io.File;
import java.util.*;
import java.math.BigDecimal;
import er.extensions.*;

public class ERCMailMessage extends _ERCMailMessage {

    //	===========================================================================
    //	Class Constant(s)
    //	---------------------------------------------------------------------------
        
    /** logging support */
    public static final ERXLogger log = ERXLogger.getERXLogger(ERCMailMessage.class);

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
        
        /**
         * @param jobSet
         * @return batch iterator for messages to be sent for the primary keys matching the jobSet
         */
        public ERXFetchSpecificationBatchIterator batchIteratorForUnsentMessages(ERXJobLoadBalancer.JobSet jobSet) {
            EOFetchSpecification fetchSpec = EOFetchSpecification.fetchSpecificationNamed("messagesToBeSent",
                    "ERCMailMessage");  
            EOQualifier q=fetchSpec.qualifier();
            ERXModuloQualifier q2=new ERXModuloQualifier("id", jobSet.modulo(), jobSet.index());
            NSArray quals=new NSArray(new Object[] {q, q2 });
            EOAndQualifier and=new  EOAndQualifier(quals);
            EOFetchSpecification fs=new EOFetchSpecification(fetchSpec.entityName(), 
                    and, fetchSpec.sortOrderings()); 
            return new ERXFetchSpecificationBatchIterator(fs);
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
    public void awakeFromInsertion(EOEditingContext anEditingContext) {
        super.awakeFromInsertion(anEditingContext);
        setState(ERCMailState.READY_TO_BE_SENT_STATE);
        if (ERXProperties.booleanForKeyWithDefault("er.corebusinesslogic.ERCMailMessage.ShouldArchive",
                                                   false)) {
            setShouldArchiveSentMail(Boolean.TRUE);
        } else {
            setShouldArchiveSentMail(Boolean.FALSE);            
        }
        if (ERXProperties.booleanForKeyWithDefault("er.corebusinesslogic.ERCMailMessage.ShouldGzipContent",
                                                   true)) {
            setContentGzipped(Boolean.TRUE);
        } else {
            setShouldArchiveSentMail(Boolean.FALSE);            
        }
    }
        
    // State Methods
    public boolean isReadyToSendState() 	{ return state() == ERCMailState.READY_TO_BE_SENT_STATE; }
    public boolean isSentState() 		{ return state() == ERCMailState.SENT_STATE; }
    public boolean isExceptionState() 		{ return state() == ERCMailState.EXCEPTION_STATE; }
    public boolean isReceivedState() 		{ return state() == ERCMailState.RECEIVED_STATE; }

    // IMPLEMENTME: MarkReadInterface
    public void markReadBy(EOEnterpriseObject by) {
       setReadAsBoolean(true);
    }

    public void setReadAsBoolean(boolean read) {
        setIsRead(read ? ERXConstant.OneInteger : ERXConstant.ZeroInteger);
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
        return shouldArchiveSentMail() != null && shouldArchiveSentMail().booleanValue();
    }
    
    /**
     * Long description of the mail message.
     * @return very verbose description of the mail message.
     */
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

    public String toString() {
        StringBuffer sb = new StringBuffer();
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
        return sb.toString();
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

    public Object validateText(String newValue) {
        return validateEmptyStringForKey(newValue, "text");
    }

    public void attachFileWithMimeType(String filePath, String mimeType) {
        attachFileWithMimeType(filePath, mimeType, false);
    }

    public void attachFileWithMimeType(String filePath, String mimeType, boolean deleteOnSent) {
        ERCMessageAttachment attachment = (ERCMessageAttachment)ERXEOControlUtilities.createAndInsertObject(editingContext(),
                                                                                                            "ERCMessageAttachment");
        attachment.setFilePath(filePath);
        if(mimeType != null)
            attachment.setMimeType(mimeType);
        attachment.setDeleteOnSent(Boolean.valueOf(deleteOnSent));
        addToBothSidesOfAttachments(attachment);
    }

    public void willDelete() {
        super.willDelete();
        captureFilesToDelete();
    }

    public void didDelete(EOEditingContext ec) {
        super.didDelete(ec);
        deleteCapturedFiles();
    }

    public void didUpdate() {
        super.didUpdate();
        
        captureFilesToDelete();
        deleteCapturedFiles();
    }

    private NSArray _filesToDelete;
    private void captureFilesToDelete() {
        if(!isSentState()) {
            _filesToDelete = NSArray.EmptyArray;
            return;
        }
        
        NSArray attachments = attachments();
        if(attachments.count() == 0) {
            _filesToDelete = NSArray.EmptyArray;
            return;
        }
        
        NSMutableArray filesToDelete = new NSMutableArray();
        for(int i=0; i<attachments.count(); i++) {
            ERCMessageAttachment attachment = (ERCMessageAttachment) attachments.objectAtIndex(i);
            if(attachment.deleteOnSent()) {
                filesToDelete.addObject(attachment.file());
            }
        }
        
        _filesToDelete = filesToDelete.immutableClone();
        log.debug("found "+ _filesToDelete.count() +" files to delete");
    }

    private void deleteCapturedFiles() {
        for (int i = 0; i < _filesToDelete.count(); i++) {
            File file = (File) _filesToDelete.objectAtIndex(i);
            if(!file.delete())
                log.debug("failed to delete "+ file);
        }
    }
}
