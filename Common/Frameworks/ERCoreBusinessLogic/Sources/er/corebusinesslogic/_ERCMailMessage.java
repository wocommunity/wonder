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

    public _ERCMailMessage() {
        super();
    }

    public static abstract class _ERCMailMessageClazz extends er.extensions.ERXGenericRecord.ERXGenericRecordClazz {

        public NSArray messagesToBeSent(EOEditingContext ec) {
            return EOUtilities.objectsWithFetchSpecificationAndBindings(ec, "ERCMailMessage", "messagesToBeSent", null);
        }

        public NSArray ripeMessagesWithDate(EOEditingContext ec, Object date) {
            NSMutableDictionary _dict = new NSMutableDictionary(2);
            
            if(date != null) _dict.setObjectForKey( date, "date");
            return EOUtilities.objectsWithFetchSpecificationAndBindings(ec, "ERCMailMessage", "ripeMessages", _dict);
        }

    }


    public NSTimestamp created() {
        return (NSTimestamp)storedValueForKey("created");
    }
    public void setCreated(NSTimestamp aValue) {
        takeStoredValueForKey(aValue, "created");
    }

    public NSTimestamp lastModified() {
        return (NSTimestamp)storedValueForKey("lastModified");
    }
    public void setLastModified(NSTimestamp aValue) {
        takeStoredValueForKey(aValue, "lastModified");
    }

    public String fromAddress() {
        return (String)storedValueForKey("fromAddress");
    }
    public void setFromAddress(String aValue) {
        takeStoredValueForKey(aValue, "fromAddress");
    }

    public String toAddresses() {
        return (String)storedValueForKey("toAddresses");
    }
    public void setToAddresses(String aValue) {
        takeStoredValueForKey(aValue, "toAddresses");
    }

    public String ccAddresses() {
        return (String)storedValueForKey("ccAddresses");
    }
    public void setCcAddresses(String aValue) {
        takeStoredValueForKey(aValue, "ccAddresses");
    }

    public String bccAddresses() {
        return (String)storedValueForKey("bccAddresses");
    }
    public void setBccAddresses(String aValue) {
        takeStoredValueForKey(aValue, "bccAddresses");
    }

    public String title() {
        return (String)storedValueForKey("title");
    }
    public void setTitle(String aValue) {
        takeStoredValueForKey(aValue, "title");
    }

    public String text() {
        return (String)storedValueForKey("text");
    }
    public void setText(String aValue) {
        takeStoredValueForKey(aValue, "text");
    }

    public NSTimestamp dateSent() {
        return (NSTimestamp)storedValueForKey("dateSent");
    }
    public void setDateSent(NSTimestamp aValue) {
        takeStoredValueForKey(aValue, "dateSent");
    }

    public String replyToAddresses() {
        return (String)storedValueForKey("replyToAddresses");
    }
    public void setReplyToAddresses(String aValue) {
        takeStoredValueForKey(aValue, "replyToAddresses");
    }

    public String xMailer() {
        return (String)storedValueForKey("xMailer");
    }
    public void setXMailer(String aValue) {
        takeStoredValueForKey(aValue, "xMailer");
    }

    public String contentType() {
        return (String)storedValueForKey("contentType");
    }
    public void setContentType(String aValue) {
        takeStoredValueForKey(aValue, "contentType");
    }

    public String isRead() {
        return (String)storedValueForKey("isRead");
    }
    public void setIsRead(String aValue) {
        takeStoredValueForKey(aValue, "isRead");
    }

    public ERCMailState state() {
        return (ERCMailState)storedValueForKey("state");
    }

    public void setState(ERCMailState aValue) {
        takeStoredValueForKey(aValue, "state");
    }
    public void addToBothSidesOfState(ERCMailState object) {
        addObjectToBothSidesOfRelationshipWithKey(object, "state");
    }
    public void removeFromBothSidesOfState(ERCMailState object) {
        removeObjectFromBothSidesOfRelationshipWithKey(object, "state");
    }


    public NSArray attachments() {
        return (NSArray)storedValueForKey("attachments");
    }
    public void setAttachments(NSMutableArray aValue) {
        takeStoredValueForKey(aValue, "attachments");
    }
    public void addToAttachments(ERCMessageAttachment object) {
        NSMutableArray array = (NSMutableArray)attachments();

        willChange();
        array.addObject(object);
    }
    public void removeFromAttachments(ERCMessageAttachment object) {
        NSMutableArray array = (NSMutableArray)attachments();

        willChange();
        array.removeObject(object);
    }
    public void addToBothSidesOfAttachments(ERCMessageAttachment object) {
        addObjectToBothSidesOfRelationshipWithKey(object, "attachments");
    }
    public void removeFromBothSidesOfAttachments(ERCMessageAttachment object) {
        removeObjectFromBothSidesOfRelationshipWithKey(object, "attachments");
    }

}
