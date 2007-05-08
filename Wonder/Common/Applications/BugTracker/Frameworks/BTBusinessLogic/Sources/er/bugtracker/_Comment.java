// _Comment.java
// 
// Created by eogenerator
// DO NOT EDIT.  Make changes to Comment.java instead.
package er.bugtracker;
import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import er.extensions.*;
import java.util.*;
import java.math.BigDecimal;

public abstract class _Comment extends ERXGenericRecord {

    public interface Key  {
        public static final String TEXT_DESCRIPTION = "textDescription";
        public static final String ORIGINATOR = "originator";
        public static final String DATE_SUBMITTED = "dateSubmitted";
        public static final String BUG = "bug";  
    }

    public static abstract class _CommentClazz extends ERXGenericRecord.ERXGenericRecordClazz {

    }


    public NSTimestamp dateSubmitted() {
        return (NSTimestamp)storedValueForKey(Key.DATE_SUBMITTED);
    }
    public void setDateSubmitted(NSTimestamp aValue) {
        takeStoredValueForKey(aValue, Key.DATE_SUBMITTED);
    }

    public String textDescription() {
        return (String)storedValueForKey(Key.TEXT_DESCRIPTION);
    }
    public void setTextDescription(String aValue) {
        takeStoredValueForKey(aValue, Key.TEXT_DESCRIPTION);
    }

    public er.bugtracker.Bug bug() {
        return (er.bugtracker.Bug)storedValueForKey(Key.BUG);
    }
    public void setBug(er.bugtracker.Bug object) {
        takeStoredValueForKey(object, Key.BUG);
    }


    public er.bugtracker.People originator() {
        return (er.bugtracker.People)storedValueForKey(Key.ORIGINATOR);
    }
    public void setOriginator(er.bugtracker.People object) {
        takeStoredValueForKey(object, Key.ORIGINATOR);
    }

}
