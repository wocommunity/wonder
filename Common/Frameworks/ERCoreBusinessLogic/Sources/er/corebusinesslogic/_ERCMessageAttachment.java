// _ERCMessageAttachment.java
// 
// Created by eogenerator
// DO NOT EDIT.  Make changes to ERCMessageAttachment.java instead.
package er.corebusinesslogic;
import er.extensions.eof.ERXGenericRecord;

public abstract class _ERCMessageAttachment extends ERXGenericRecord {

    public _ERCMessageAttachment() {
        super();
    }

    public static abstract class _ERCMessageAttachmentClazz extends ERXGenericRecord.ERXGenericRecordClazz {

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
