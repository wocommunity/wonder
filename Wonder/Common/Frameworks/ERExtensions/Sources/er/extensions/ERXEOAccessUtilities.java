//
//  ERXEOAccessUtilities.java
//  ERExtensions
//
//  Created by Max Muller on Sat Feb 22 2003.
//
package er.extensions;

import com.webobjects.foundation.*;
import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;

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
        if (array.count() == 0) {
            throw new RuntimeException("Unable to generate value from sequence named: " + sequenceName
                                       + " in model: " + modelNamed);            
        }
        NSDictionary dictionary = (NSDictionary)array.objectAtIndex(0);
        NSArray valuesArray = dictionary.allValues();
        return (Number)valuesArray.objectAtIndex(0);
    }

    /**
     * Utility method used to execute arbitrary SQL. This
     * has the advantage over the
     * {@link com.webobjects.eoaccess.EOUtilities EOUtilities}
     * <code>rawRowsForSQL</code> in that it can be used with
     * other statements besides just SELECT without throwing
     * exceptions.
     * @param ec editing context that determines which model group
     *		and database context to use.
     * @param entityName name of an entity in the model connected
     *		to the database you wish to execute SQL against
     * @param exp SQL expression
     */
    // ENHANCEME: Should support the use of bindings
    // ENHANCEME: Could also support the option of using a seperate EOF stack so as to execute
    //		sql in a non-blocking fashion.
    public static void evaluateSQLWithEntityNamed(EOEditingContext ec, String entityName, String exp) {
        EOEntity entity = EOUtilities.entityNamed(ec, entityName);
        EODatabaseContext dbContext = EODatabaseContext.registeredDatabaseContextForModel(entity.model(), ec);
        EOAdaptorChannel adaptorChannel = dbContext.availableChannel().adaptorChannel();
        if (!adaptorChannel.isOpen())
            adaptorChannel.openChannel();
        EOSQLExpressionFactory factory=adaptorChannel.adaptorContext().adaptor().expressionFactory();
        adaptorChannel.evaluateExpression(factory.expressionForString(exp));
    }
}
