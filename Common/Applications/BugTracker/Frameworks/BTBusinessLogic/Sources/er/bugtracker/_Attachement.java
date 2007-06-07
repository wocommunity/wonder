// _Attachement.java
// 
// Created by eogenerator
// DO NOT EDIT.  Make changes to Attachement.java instead.
package er.bugtracker;
import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import er.extensions.*;
import java.util.*;
import java.math.BigDecimal;

public abstract class _Attachement extends ERXGenericRecord {

    public static final String ENTITY = "Attachement";

    public interface Key  {
        public static final String MIME_TYPE = "mimeType";
        public static final String FILE_NAME = "fileName";
        public static final String COMMENT = "comment";  
    }

    public static abstract class _AttachementClazz extends ERXGenericRecord.ERXGenericRecordClazz {
    
    	public Attachement createAttachement(EOEditingContext editingContext, String fileName, String mimeType, er.bugtracker.Comment comment) {
	   		Attachement eo = (Attachement)EOUtilities.createAndInsertInstance(editingContext, Attachement.ENTITY);
	    	eo.setFileName(fileName);
	    	eo.setMimeType(mimeType);
	    	eo.setComment(comment);
	    	return eo;
 		}


    }


    public String fileName() {
        return (String)storedValueForKey(Key.FILE_NAME);
    }
    public void setFileName(String aValue) {
        takeStoredValueForKey(aValue, Key.FILE_NAME);
    }

    public String mimeType() {
        return (String)storedValueForKey(Key.MIME_TYPE);
    }
    public void setMimeType(String aValue) {
        takeStoredValueForKey(aValue, Key.MIME_TYPE);
    }

    public er.bugtracker.Comment comment() {
        return (er.bugtracker.Comment)storedValueForKey(Key.COMMENT);
    }
    public void setComment(er.bugtracker.Comment object) {
        takeStoredValueForKey(object, Key.COMMENT);
    }

}
