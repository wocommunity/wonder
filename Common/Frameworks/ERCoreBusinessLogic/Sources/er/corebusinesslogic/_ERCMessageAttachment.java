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

    public static final String ENTITY = "ERCMessageAttachment";

    public interface Key  {
        public static final String MIME_TYPE = "mimeType";
        public static final String MAIL_MESSAGE = "mailMessage";
        public static final String FILE_PATH = "filePath";  
    }

    public static abstract class _ERCMessageAttachmentClazz extends ERXGenericRecord.ERXGenericRecordClazz {
 

    }


    public String filePath() {
        return (String)storedValueForKey(Key.FILE_PATH);
    }
    public void setFilePath(String aValue) {
        takeStoredValueForKey(aValue, Key.FILE_PATH);
    }

    public String mimeType() {
        return (String)storedValueForKey(Key.MIME_TYPE);
    }
    public void setMimeType(String aValue) {
        takeStoredValueForKey(aValue, Key.MIME_TYPE);
    }

    public er.corebusinesslogic.ERCMailMessage mailMessage() {
        return (er.corebusinesslogic.ERCMailMessage)storedValueForKey(Key.MAIL_MESSAGE);
    }
    public void setMailMessage(er.corebusinesslogic.ERCMailMessage object) {
        takeStoredValueForKey(object, Key.MAIL_MESSAGE);
    }

}
