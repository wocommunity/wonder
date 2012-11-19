//
// ERXInQualifier.java
// Project EOInQualifier
//
// Created by max on Mon Jul 15 2002
//
package er.extensions.eof.qualifiers;

import java.util.Enumeration;

import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOJoin;
import com.webobjects.eoaccess.EOQualifierSQLGeneration;
import com.webobjects.eoaccess.EORelationship;
import com.webobjects.eoaccess.EOSQLExpression;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOAndQualifier;
import com.webobjects.eocontrol.EOClassDescription;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOKeyValueArchiver;
import com.webobjects.eocontrol.EOKeyValueQualifier;
import com.webobjects.eocontrol.EOKeyValueUnarchiver;
import com.webobjects.eocontrol.EOObjectStoreCoordinator;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.eocontrol.EOQualifierVariable;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSCoder;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSKeyValueCodingAdditions;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSSet;

import er.extensions.eof.ERXEOAccessUtilities;
import er.extensions.foundation.ERXProperties;
import er.extensions.jdbc.ERXSQLHelper;
import er.extensions.qualifiers.ERXKeyValueQualifier;

/**
 * The ERXInQualifier is useful for creating qualifiers that
 * will generate SQL using the 'IN' key word.<br>
 * <br>
 * For example constructing this qualifer:<br>
 * <code>ERXInQualifier q = new ERXInQualifier("userId", arrayOfNumbers);</code>
 * Then this qualifier would generate SQL of the form:
 * USER_ID IN (&lt;array of numbers or data&gt;)
 */
// ENHANCEME: Should support restrictive qualifiers, don't need to subclass KeyValueQualifier
public class ERXInQualifier extends ERXKeyValueQualifier implements Cloneable {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

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
     * Constructs an in qualifier for a given
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
            // as that might extend the set inadvertently.
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
    @Override
    public String toString() {
        return " <" + getClass().getName() + " key: " + key() + " > IN '" + value() + "'";
    }

    public NSArray values() {
        return (NSArray)value();
    }

    /** Tests if the given object's key is in the supplied values */ 
    // FIXME: this doesn't work right with EOs when the key() is keypath across a relationship
    @Override
    public boolean evaluateWithObject(Object object) {
        Object value = null;
        String key = key();
        if(object instanceof EOEnterpriseObject) {
            EOEnterpriseObject eo = (EOEnterpriseObject)object;
            EOEditingContext ec = eo.editingContext();
            EOClassDescription cd = eo.classDescription();
            if (cd.attributeKeys().containsObject(key)) {
                value = NSKeyValueCodingAdditions.Utility.valueForKeyPath(eo, key);
            } else if (cd.toOneRelationshipKeys().containsObject(key)) {
                value = eo.valueForKeyPath(key);
            } else if (EOUtilities.entityNamed(ec, eo.entityName()).primaryKeyAttributeNames().containsObject(key)) {
                // when object is an EO and key is a cross-relationship keypath, we drop through to this case
                // and we'll fail.
                value = EOUtilities.primaryKeyForObject(ec,eo).objectForKey(key);
            } else {
                // ok, it could be any key-path not directly listed as a to-one relationship
                value = NSKeyValueCodingAdditions.Utility.valueForKeyPath(eo, key);
                if (value instanceof NSArray) {
                    // here we will only handle to-many (NSArray).
                    // so we try to compare 'n' values with 'm' objects.
                    // we use set intersection
                    NSSet vs = new NSSet((NSArray) value);
                    NSSet vss = new NSSet(values());
                    return vs.intersectsSet(vss);
                }
            }
        } else {
            value = NSKeyValueCodingAdditions.Utility.valueForKeyPath(object, key);
        }
        return value != null && values().containsObject(value);
    }
    
    /**
     * EOF seems to be wanting to clone qualifiers when
     * they are inside an and-or qualifier without this
     * method, ERXInQualifier is cloned into
     * an EOKeyValueQualifier and the generated SQL is incorrect...
     * @return cloned primary key list qualifier.
     */
    @Override
    public Object clone() {
        return new ERXInQualifier(key(), values());
    }

    /**
     * Adds SQL generation support. Note that the database needs to support
     * the IN operator.
     */
    public static class InQualifierSQLGenerationSupport extends EOQualifierSQLGeneration._KeyValueQualifierSupport {

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
        @Override
        public String sqlStringForSQLExpression(EOQualifier eoqualifier, EOSQLExpression e) {
            ERXInQualifier inqualifier = (ERXInQualifier)eoqualifier;
            String result;
            if (inqualifier.value() instanceof NSArray) {
                String key = inqualifier.key();
                result = ERXSQLHelper.newSQLHelper(e).sqlWhereClauseStringForKey(e, key, (NSArray) inqualifier.value());
            } else {
                throw new RuntimeException("Unsupported value type: " + inqualifier.value().getClass().getName());
            }
            return result;
        }

        // ENHANCEME: This should support restrictive qualifiers on the root entity
        @Override
        public EOQualifier schemaBasedQualifierWithRootEntity(EOQualifier eoqualifier, EOEntity eoentity) {
            EOKeyValueQualifier eokeyvaluequalifier = (EOKeyValueQualifier)eoqualifier;
            String key = eokeyvaluequalifier.key();
            EORelationship eorelationship = eoentity._relationshipForPath(key);
            if(eorelationship == null) {
            	if(!(eokeyvaluequalifier instanceof ERXInQualifier)) {
            		eokeyvaluequalifier = new ERXInQualifier(key, (NSArray) eokeyvaluequalifier.value());
            	}
                return eokeyvaluequalifier;
            }
            if(eorelationship.isFlattened()) {
                eorelationship = ERXEOAccessUtilities.lastRelationship(eorelationship);
            }
            NSArray joins = eorelationship.joins();
            int l = joins.count();
            NSMutableArray destinationAttibuteNames = new NSMutableArray(l);
            for(int i = l - 1; i >= 0; i--) {
                destinationAttibuteNames.addObject(((EOJoin)joins.objectAtIndex(i)).destinationAttribute().name());
            }
            
            Object value = eokeyvaluequalifier.value();
            Object obj;
            if(value == NSKeyValueCoding.NullValue || (value instanceof EOQualifierVariable)) {
                NSMutableDictionary mapping = new NSMutableDictionary(l);
                 for(int j = 0; j < l; j++) {
                    mapping.setObjectForKey(value, destinationAttibuteNames.objectAtIndex(j));
                }
                obj = mapping;
            } else {
                NSMutableDictionary mapping = new NSMutableDictionary(l);
                for(int j = 0; j < l; j++) {
                    NSMutableArray realValues = new NSMutableArray();
                    for(Enumeration e = ((NSArray)value).objectEnumerator(); e.hasMoreElements();) {
                        Object o = e.nextElement();
                        NSDictionary dict =  null;
                        String currentKey = (String) destinationAttibuteNames.objectAtIndex(j);
                        Object v;
                        if (o instanceof EOEnterpriseObject) {
                            EOEnterpriseObject eoenterpriseobject = (EOEnterpriseObject)o;
                            EOObjectStoreCoordinator osc = ((EOObjectStoreCoordinator)eoenterpriseobject.editingContext().rootObjectStore());
                            dict = osc.valuesForKeys(new NSArray(currentKey), eoenterpriseobject);
                            v = dict.objectForKey(currentKey);
                         } else {
                            v = o; 
                         }
                        realValues.addObject(v);
                    }
                    mapping.setObjectForKey(realValues, destinationAttibuteNames.objectAtIndex(j));
                }
                
                obj = mapping;
            }
            
            NSMutableArray qualifiers = null;
            ERXInQualifier result = null;
            l = destinationAttibuteNames.count();
            for(int k = 0; k < l; k++) {
                String s1 = (String)destinationAttibuteNames.objectAtIndex(k);
                String s2 = _optimizeQualifierKeyPath(eoentity, key, s1);
                Object o = ((NSDictionary) (obj)).objectForKey(s1);
                result = new ERXInQualifier(s2, (NSArray) o);
                if(l <= 1)
                    continue;
                if(qualifiers == null) {
                    qualifiers = new NSMutableArray();
                    qualifiers.addObject(result);
                }
            }
            if(qualifiers == null) {
                return result;
            }
            return new EOAndQualifier(qualifiers);
        }

        @Override
        public EOQualifier qualifierMigratedFromEntityRelationshipPath(EOQualifier eoqualifier, EOEntity eoentity, String s) {
            // the key migration is the same as for EOKeyValueQualifier
            ERXInQualifier inQualifier=(ERXInQualifier)eoqualifier;
            String newPath = EOQualifierSQLGeneration.Support._translateKeyAcrossRelationshipPath(inQualifier.key(), s, eoentity);
            return new ERXInQualifier(newPath, inQualifier.values());
        }
    }
    
    @Override
    public Class classForCoder() {
    	return getClass();
    }
    
	public static Object decodeObject(NSCoder coder) {
		String key = (String) coder.decodeObject();
		NSArray values = (NSArray)coder.decodeObject();
		return new ERXInQualifier(key, values);
	}

	@Override
	public void encodeWithCoder(NSCoder coder) {
		coder.encodeObject(key());
		coder.encodeObject(values());
	}

	@Override
	public void encodeWithKeyValueArchiver(EOKeyValueArchiver archiver) {
		archiver.encodeObject(key(), "key");
		archiver.encodeObject(values(), "values");
	}

	public static Object decodeWithKeyValueUnarchiver(EOKeyValueUnarchiver unarchiver) {
		return new ERXInQualifier(
				(String)unarchiver.decodeObjectForKey("key"),
				(NSArray)unarchiver.decodeObjectForKey("values"));
	}
}
