//
// ERXInQualifier.java
// Project EOInQualifier
//
// Created by max on Mon Jul 15 2002
//
package er.extensions;

import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;

/**
 * The ERXInQualifier is useful for creating qualifiers that
 * will generate SQL using the 'IN' key word.<br>
 * <br>
 * For example constructing this qualifer:<br>
 * <code>ERXInQualifier q = new ERXInQualifier("userId", arrayOfNumbers);</code>
 * Then this qualifier would generate SQL of the form:
 * USER_ID IN (<array of numbers or data>)
 */
// ENHANCEME: Should support restrictive qualifiers, don't need to subclass KeyValueQualifier
public class ERXInQualifier extends EOKeyValueQualifier implements Cloneable {

    /** register SQL generation support for the qualifier */
    static {
        EOQualifierSQLGeneration.Support.setSupportForClass(new InQualifierSQLGenerationSupport(), ERXInQualifier.class);
    }
    
    /**
    * Constructs an in qualifer for a given
     * attribute name and an array of values.
     * @param key attribute name
     * @param eos array of values
     */
    public ERXInQualifier(String key, NSArray values) {
        super(key, EOQualifier.QualifierOperatorEqual, values);
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
    public boolean evaluateWithObject(Object object) {
        boolean result = false;
        Object value = null;
        if(object instanceof EOEnterpriseObject) {
            EOEnterpriseObject eo = (EOEnterpriseObject)object;
            EOEditingContext ec = eo.editingContext();
            if(eo.classDescription().attributeKeys().containsObject(key())) {
                value = NSKeyValueCodingAdditions.Utility.valueForKeyPath(eo, key());
            } else if(EOUtilities.entityNamed(ec, eo.entityName()).primaryKeyAttributeNames().containsObject(key())) {
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
            String result = null;
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
            String newPath = EOQualifierSQLGeneration.Support._translateKeyAcrossRelationshipPath(inQualifier.key(), s, eoentity);
            return new ERXInQualifier(newPath, inQualifier.values());
        }
    }
}
