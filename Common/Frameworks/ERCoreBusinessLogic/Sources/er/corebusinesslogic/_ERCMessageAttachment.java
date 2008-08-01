// _ERCMessageAttachment.java
// 
// Created by eogenerator
// DO NOT EDIT.  Make changes to ERCMessageAttachment.java instead.
package er.corebusinesslogic;
import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import er.extensions.*;
import java.util.*;
import java.math.BigDecimal;

public abstract class _ERCMessageAttachment extends ERXGenericRecord {

    public _ERCMessageAttachment() {
        super();
    }

    public static abstract class _ERCMessageAttachmentClazz extends er.extensions.ERXGenericRecord.ERXGenericRecordClazz {

    }


    public Boolean deleteOnSent() {
        return (Boolean)storedValueForKey("deleteOnSent");
    }
    public void setDeleteOnSent(Boolean aValue) {
        takeStoredValueForKey(aValue, "deleteOnSent");
    }

    public String filePath() {
        return (String)storedValueForKey("filePath");
    }
    public void setFilePath(String aValue) {
        takeStoredValueForKey(aValue, "filePath");
    }

    public String mimeType() {
        return (String)storedValueForKey("mimeType");
    }
    public void setMimeType(String aValue) {
        takeStoredValueForKey(aValue, "mimeType");
    }

    public ERCMailMessage mailMessage() {
        return (ERCMailMessage)storedValueForKey("mailMessage");
    }

    public void setMailMessage(ERCMailMessage aValue) {
        takeStoredValueForKey(aValue, "mailMessage");
    }
    public void addToBothSidesOfMailMessage(ERCMailMessage object) {
        addObjectToBothSidesOfRelationshipWithKey(object, "mailMessage");
    }
    public void removeFromBothSidesOfMailMessage(ERCMailMessage object) {
        removeObjectFromBothSidesOfRelationshipWithKey(object, "mailMessage");
    }

}
