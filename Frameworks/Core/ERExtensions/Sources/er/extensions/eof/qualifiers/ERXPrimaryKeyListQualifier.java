/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions.eof.qualifiers;

import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOEntityClassDescription;
import com.webobjects.eoaccess.EOQualifierSQLGeneration;
import com.webobjects.eoaccess.EOSQLExpression;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOKeyValueQualifier;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSArray;

import er.extensions.eof.ERXEOAccessUtilities;

/**
 * The primary key list qualifier is used to generate
 * a qualifier that can be used to filter a result set
 * for a given set of primary keys. Note that this uses
 * the IN expression and as such may not work with some
 * databases.
 * <p>
 * Given a list of EOs, this generates a query looking like
 *
 *   ...  t0.ID in (&lt; the list of primary Keys for EOs in the list &gt;) ..
 *
 * this is useful for pre-fetching type uses.
 */
public class ERXPrimaryKeyListQualifier extends ERXInQualifier {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public static final String IsContainedInArraySelectorName = "isContainedInArray";
    
    static {
        EOQualifierSQLGeneration.Support.setSupportForClass(new ERXPrimaryKeyListQualifier.Support(), ERXPrimaryKeyListQualifier.class);
    }
    
    /**
     * Support class that listens for EOKeyValueQualifiers that have an <code>isContainedInArray</code>-selector and replaces these
     * with the ERXInQualifier. This means that when you set <code>isContainedInArray</code> as a display group
     * queryOperator and an NSArray of EOs as the value, then this qualifier is magically replaced by
     * one that selects objects with an IN qualifier.
     * @author ak
     */
    public static class Support extends EOQualifierSQLGeneration._KeyValueQualifierSupport {

        @Override
        public String sqlStringForSQLExpression(EOQualifier eoqualifier, EOSQLExpression e) {
            return super.sqlStringForSQLExpression(eoqualifier, e);
        }

        @Override
        public EOQualifier schemaBasedQualifierWithRootEntity(EOQualifier eoqualifier, EOEntity eoentity) {
            EOQualifier result = null;
            EOKeyValueQualifier qualifier = (EOKeyValueQualifier)eoqualifier;
            String key = qualifier.key();
            if(key.indexOf('.') < 0) {
                // ak: this code is only for binding values in display groups and
                // to support the twolevelrelationship and the ERD2WQuery*Relationship, it probably should go away...
                Object value = qualifier.value();
                if(!(value instanceof NSArray)) {
                    value = new NSArray(value);
                }
                NSArray objects = ((NSArray)value);
                if(objects.lastObject() instanceof EOEnterpriseObject) {
                	objects = ERXEOAccessUtilities.primaryKeysForObjects(objects);
                    value = objects;
                }
            }
            EOQualifierSQLGeneration.Support support = EOQualifierSQLGeneration.Support.supportForClass(ERXInQualifier.class);
            result = support.schemaBasedQualifierWithRootEntity(qualifier, eoentity);
            return result;
        }

        @Override
        public EOQualifier qualifierMigratedFromEntityRelationshipPath(EOQualifier eoqualifier, EOEntity eoentity, String s) {
            return super.qualifierMigratedFromEntityRelationshipPath(eoqualifier, eoentity, s);
        }
    }
    

    /**
     * Constructs a primary key list qualifer for a given
     * set of enterprise objects. For now only use this
     * qualifier if the primary key attribute of your
     * enterprise object is named 'id'.
     * @param eos array of enterprise objects
     */
    public ERXPrimaryKeyListQualifier(NSArray eos) {
        this(primaryKeyNameForObjects(eos), eos);
    }

    // Only used during cloning
    private ERXPrimaryKeyListQualifier(String key, NSArray eos, boolean ignoreMe) {
        super(key,eos);
    }
    
    /**
     * Constructs a primary key list qualifer for a given
     * set of enterprise objects and the primary key
     * attribute name.
     * @param key primary key attribute name
     * @param eos array of enterprise objects
     */
    public ERXPrimaryKeyListQualifier(String key, NSArray eos) {
        super(key, ERXEOAccessUtilities.primaryKeysForObjects(eos));
    }

    /**
     * Constructs a primary key list qualifer for a given
     * set of enterprise objects, the primary key
     * attribute name and a foreign key. This type of
     * qualifier can be useful for prefetching a to-one
     * relationship off of many enterprise objects.
     * @param key primary key attribute name
     * @param foreignKey attribute name.
     * @param eos array of enterprise objects
     */    
    public ERXPrimaryKeyListQualifier(String key, String foreignKey, NSArray eos) {
        this(key, ERXEOAccessUtilities.snapshotsForObjectsFromRelationshipNamed(eos, foreignKey));
    }
        
    /*
     * Implementation of the Cloneable interface.
     * @return cloned primary key list qualifier.
     */
    @Override
    public Object clone() {
        return new ERXPrimaryKeyListQualifier(key(), (NSArray)value(), true);
    }

    /**
     * Calculates the primary key attribute name for an
     * array of enterprise objects. This method assumes
     * that all the entities of the objects have the same
     * primary key attribute name.
     * @param eos array of enterprise objects
     * @return primary key name for the enterprise objects
     *		in the array.
     */
    protected static String primaryKeyNameForObjects(NSArray eos) {
        validateObjects(eos);
        EOEnterpriseObject eo = (EOEnterpriseObject)eos.lastObject();
        EOEntity entity = ((EOEntityClassDescription)eo.classDescription()).entity();
        if (entity.primaryKeyAttributeNames().count() != 1)
            throw new IllegalStateException("Attempting to construct a qualifier for an entity with a compound primary key: " + entity);
        return entity.primaryKeyAttributeNames().lastObject();
    }

    /**
     * Simple validation routine used to ensure that
     * the objects being passed in are enterprise
     * objects and have more than one in the array.
     * @param eos array of objects to check
     * @return the array of objects if they pass the
     *		check.
     */
    protected static NSArray validateObjects(NSArray eos) {
        if (eos == null || eos.count() == 0 || !(eos.lastObject() instanceof EOEnterpriseObject))
            throw new IllegalStateException("Attempting to construct a qualifier for a bad array: " + eos);
        return eos;
    }    
}
