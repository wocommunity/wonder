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

    public static final String ENTITY = "Difficulty";

    public interface Key  {
        public static final String DIFFICULTY_DESCRIPTION = "difficultyDescription";  
    }

    public static abstract class _DifficultyClazz extends ERXGenericRecord.ERXGenericRecordClazz {
 

        public NSArray objectsForFetchAll(EOEditingContext context) {
            EOFetchSpecification spec = EOFetchSpecification.fetchSpecificationNamed("FetchAll", "Difficulty");

            return context.objectsWithFetchSpecification(spec);
        }

    }


    public String difficultyDescription() {
        return (String)storedValueForKey(Key.DIFFICULTY_DESCRIPTION);
    }
    public void setDifficultyDescription(String aValue) {
        takeStoredValueForKey(aValue, Key.DIFFICULTY_DESCRIPTION);
    }
}
