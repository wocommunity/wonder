// _Difficulty.java
// 
// Created by eogenerator
// DO NOT EDIT.  Make changes to Difficulty.java instead.
package er.bugtracker;
import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import er.extensions.*;
import java.util.*;
import java.math.BigDecimal;

public abstract class _Difficulty extends ERXGenericRecord {

    public _Difficulty() {
        super();
    }

    public static abstract class _DifficultyClazz extends er.extensions.ERXGenericRecord.ERXGenericRecordClazz {

        public NSArray fetchAll(EOEditingContext ec) {
            return EOUtilities.objectsWithFetchSpecificationAndBindings(ec, "Difficulty", "FetchAll", null);
        }

    }


    public String difficultyDescription() {
        return (String)storedValueForKey("difficultyDescription");
    }
    public void setDifficultyDescription(String aValue) {
        takeStoredValueForKey(aValue, "difficultyDescription");
    }
}
