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
    * Method used to determine if a given entity is a shared entity.
     * @param ec editing context
     * @param entityName name of the entity
     * @return if the entity is a shared entity
     */
    public static boolean entityWithNamedIsShared(EOEditingContext ec, String entityName) {
        if (ec == null)
            throw new IllegalStateException("Editing context argument is null for method: entityWithNamedIsShared");
        if (entityName == null)
            throw new IllegalStateException("Entity name argument is null for method: entityWithNamedIsShared");
        EOEntity entity = EOUtilities.entityNamed(ec, entityName);
        return entity.sharedObjectFetchSpecificationNames().count() > 0;
    }
    
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

    /**
     * Creates an aggregate attribute for a given function name. These can then be
     * used to query on when using raw rows.
     * @param ec editing context used to locate the model group
     * @param function name of the function MAX, MIN, etc
     * @param attributeName name of the attribute
     * @param entityName name of the entity
     * @return aggregate function attribute
     */
    public static EOAttribute createAggregateAttribute(EOEditingContext ec,
                                                       String function,
                                                       String attributeName,
                                                       String entityName) {
        if (ec == null)
            throw new IllegalStateException("EditingContext is null. Required to know which model group to use.");
        if (function == null)
            throw new IllegalStateException("Function is null.");
        if (attributeName == null)
            throw new IllegalStateException("Attribute name is null.");
        if (entityName == null)
            throw new IllegalStateException("Entity name is null.");
        
        EOEntity entity = EOUtilities.entityNamed(ec, entityName);

        if (entity == null)
            throw new IllegalStateException("Unable find entity named: " + entityName);
        
        EOAttribute attribute = entity.attributeNamed(attributeName);

        if (attribute == null)
            throw new IllegalStateException("Unable find attribute named: " + attributeName
                                            + " for entity: " + entityName);
        
        EOAttribute aggregate = new EOAttribute();
        aggregate.setName("p_object" + function + "Attribute");
        aggregate.setColumnName("p_object" + function + "Attribute");
        aggregate.setClassName("java.lang.Number");
        aggregate.setValueType("i");
        aggregate.setReadFormat(function + "(t0." + attribute.columnName() + ")");
        return aggregate;
    }
}
