//
//  ERXEOAccessUtilities.java
//  ERExtensions
//
//  Created by Max Muller on Sat Feb 22 2003.
//
package er.extensions;

import com.webobjects.foundation.*;
import com.webobjects.eoaccess.*;

/**
 * Collection of EOAccess related utilities.
 */
public class ERXEOAccessUtilities {

    /**
    * Convenience method to get the next unique ID from a sequence.
     * @param ec editing context
     * @param modelNamed name of the model which connects to the database
     *			that has the sequence in it
     * @param sequenceName name of the sequence
     * @return next value in the sequence
     */
    // ENHANCEME: Need a non-oracle specific way of doing this. Should poke around at
    //		the adaptor level and see if we can't find something better.
    public static Number getNextValFromSequenceNamed(EOEditingContext ec,
                                                     String modelNamed,
                                                     String sequenceName) {
        String sqlString = "select "+sequenceName+".nextVal from dual";
        NSArray array = EOUtilities.rawRowsForSQL(ec, modelNamed, sqlString);
        if (array.count() == 0)
            throw new RuntimeException("Unable to generate value from sequence named: " + sequenceName
                                       + " in model: " + modelNamed);
        NSDictionary dictionary = (NSDictionary)array.objectAtIndex(0);
        NSArray valuesArray = dictionary.allValues();
        return (Number)valuesArray.objectAtIndex(0);
    }
        
}
