//
// ERXInQualifier.java
// Project EOInQualifier
//
// Created by max on Mon Jul 15 2002
//
package er.extensions;

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;

/**
 * The ERXInQualifier is useful for creating qualifiers that
 * will generate SQL using the 'IN' key word.<br>
 * <br>
 * For example constructing this qualifer:<br>
 * <code>ERXInQualifier q = new ERXInQualifier("userId", arrayOfNumbers);</code>
 * Then this qualifier would generate SQL of the form:
 * USER_ID IN (&lt;array of numbers or data>)
 */
// ENHANCEME: Should support restrictive qualifiers, don't need to subclass KeyValueQualifier
public class ERXInQualifier extends EOKeyValueQualifier implements Cloneable {
    private static final int DefaultPadToSize =
            ERXProperties.intForKeyWithDefault("er.extensions.ERXInQualifier.DefaultPadToSize", 8);

    /** register SQL generation support for the qualifier */
    static {
        EOQualifierSQLGeneration.Support.setSupportForClass(new InQualifierSQLGenerationSupport(), ERXInQualifier.class);
    }
    
    /**
    * Constructs an in qualifer for a given
     * attribute name and an array of values.
     * @param key attribute name
     * @param values array of values
     */
    public ERXInQualifier(String key, NSArray values) {
        this(key, values, DefaultPadToSize);
    }

    /**
    * Constructs an in qualifer for a given
     * attribute name and an array of values.
     * @param key attribute name
     * @param values array of values
     * @param padToSize the size which is expected to be reasonable for this qualifier.  If the NSArray values
     * has more than one element, the padToSize is used to round up the number of elements and pad with nulls.
     * Doing this reduces the number of unique queries which result from having an arbitrary number of values.
     * For example, if the padToSize is 8, then we'll either have 1, or 8, or 16, or... bind variables as
     * compared to 1..., 2..., 3..., 4..., or ....16
     */
    public ERXInQualifier(String key, NSArray values, final int padToSize) {
        super(key, EOQualifier.QualifierOperatorEqual, paddedValues(values, padToSize));
    }

    /**
     * @param values  see ERXInQualifier
     * @param padToSize see ERXInQualifier
     * @return an NSArray with a count that is an even multiple of padToSize and padded with the last element of the
     * values array.
     */
    private static NSArray paddedValues(NSArray values, final int padToSize) {
        final int count = values.count();
        if (count > 1) {
            final int paddedSize = (((count - 1) / padToSize) + 1) * padToSize;
            final NSMutableArray paddedValues = new NSMutableArray(values);
            int padCount = paddedSize - count;
            // We pad with the last element repeated padCount times.  Do not pad with null
            // as that might extend the set inadvertantly.
            Object padElement = values.lastObject();
            while (padCount > 0) {
                paddedValues.addObject(padElement);
                padCount--;
            }
            values = paddedValues;
        }
        return values;
    }


    /**
    * String representation of the in
     * qualifier.
     * @return string description of the qualifier
     */
    public String toString() {
        return " <" + getClass().getName() + " key: " + key() + " > IN '" + value() + "'";
    }

    public NSArray values() {
        return (NSArray)value();
    }

    /** Tests if the given object's key is in the supplied values */ 
    // FIXME: this doesn't work right with EOs when the key() is keypath across a relationship
    public boolean evaluateWithObject(Object object) {
        Object value = null;
        if(object instanceof EOEnterpriseObject) {
            EOEnterpriseObject eo = (EOEnterpriseObject)object;
            EOEditingContext ec = eo.editingContext();
            if(eo.classDescription().attributeKeys().containsObject(key())) {
                value = NSKeyValueCodingAdditions.Utility.valueForKeyPath(eo, key());
            } else if(EOUtilities.entityNamed(ec, eo.entityName()).primaryKeyAttributeNames().containsObject(key())) {
                // when object is an EO and key() is a cross-relationship keypath, we drop through to this case
                // and we'll fail.
                value = EOUtilities.primaryKeyForObject(ec,eo).objectForKey(key());
            }
        } else {
            value = NSKeyValueCodingAdditions.Utility.valueForKeyPath(object, key());
        }
        return value != null && values().containsObject(value);
    }
    
    /*
     * EOF seems to be wanting to clone qualifiers when
     * they are inside an and-or qualifier without this
     * method, ERXInQualifier is cloned into
     * an EOKeyValueQualifier and the generated SQL is incorrect..
     * @return cloned primary key list qualifier.
     */
    public Object clone() {
        return new ERXInQualifier(key(), values());
    }

    /**
     * Adds SQL generation support. Note that the database needs to support
     * the IN operator.
     */
    public static class InQualifierSQLGenerationSupport extends EOQualifierSQLGeneration.Support {

        /**
         * Public constructor
         */
        public InQualifierSQLGenerationSupport() {
            super();
        }

        /**
         * Generates the SQL string for an ERXInQualifier.
         * @param eoqualifier an in qualifier
         * @param e current eo sql expression
         * @return SQL for the current qualifier.
         */
        public String sqlStringForSQLExpression(EOQualifier eoqualifier, EOSQLExpression e) {
            ERXInQualifier inqualifier = (ERXInQualifier)eoqualifier;
            String result;
            if (inqualifier.value() instanceof NSArray) {
                result = ERXEOAccessUtilities.sqlWhereClauseStringForKey(e, inqualifier.key(),  (NSArray)inqualifier.value());
            } else {
                throw new RuntimeException("Unsupported value type: " + inqualifier.value().getClass().getName());
            }
            return result;
        }

        // ENHANCEME: This should support restrictive qualifiers on the root entity
        public EOQualifier schemaBasedQualifierWithRootEntity(EOQualifier eoqualifier, EOEntity eoentity) {
            return (EOQualifier)eoqualifier.clone();
        }

        public EOQualifier qualifierMigratedFromEntityRelationshipPath(EOQualifier eoqualifier, EOEntity eoentity, String s) {
            // the key migration is the same as for EOKeyValueQualifier
            ERXInQualifier inQualifier=(ERXInQualifier)eoqualifier;
            return new ERXInQualifier(_translateKeyAcrossRelationshipPath(inQualifier.key(), s, eoentity),
                                      inQualifier.values());
        }
    }
}
