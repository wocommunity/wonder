/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions;

import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;

/**
 * The primary key list qualifier is used to generate
 * a qualifier that can be used to filter a result set
 * for a given set of primary keys. Note that this uses
 * the IN expression and as such may not work with some
 * databases. <br/>
 * <br/>
 * Given a list of EOs, this generates a query looking like
 *
 *   ...  t0.ID in (< the list of primary Keys for EOs in the list>) ..
 *
 * this is useful for pre-fetching type uses.
 */
public class ERXPrimaryKeyListQualifier extends ERXInQualifier {
    
    /** logging support */
    protected static final ERXLogger log = ERXLogger.getERXLogger(ERXPrimaryKeyListQualifier.class);

    public static String IsContainedInArraySelectorName = "isContainedInArray";
    
    /**
     * Support class that listens for EOKeyValueQualifiers that have an <code>isContainedInArray</code>-selector and replaces these
     * with the ERXPrimaryKeyListQualifier. This means that when you set <code>isContainedInArray</code> as a display group
     * queryOperator and an NSArray of EOs as the value, then this qualifier is magically replaced by
     * one that selects objects with an IN qualifier.
     * @author ak
     */
    public static class KeyValueQualifierSQLGenerationSupport extends EOQualifierSQLGeneration.Support {
        
        private EOQualifierSQLGeneration.Support _old;
        
        public KeyValueQualifierSQLGenerationSupport(EOQualifierSQLGeneration.Support old) {
            _old = old;
        }
        
        public String sqlStringForSQLExpression(EOQualifier eoqualifier, EOSQLExpression e) {
            return _old.sqlStringForSQLExpression(eoqualifier, e);
        }
        
        public EOQualifier schemaBasedQualifierWithRootEntity(EOQualifier eoqualifier, EOEntity eoentity) {
            EOKeyValueQualifier qualifier = (EOKeyValueQualifier)eoqualifier;
            if(IsContainedInArraySelectorName.equals(qualifier.selector().name())) {
                return new ERXPrimaryKeyListQualifier(qualifier.key(), (NSArray)qualifier.value());
            }
            EOQualifier result = _old.schemaBasedQualifierWithRootEntity(eoqualifier, eoentity);
            return result;
        }
        
        public EOQualifier qualifierMigratedFromEntityRelationshipPath(EOQualifier eoqualifier, EOEntity eoentity, String s) {
            return _old.qualifierMigratedFromEntityRelationshipPath(eoqualifier, eoentity, s);
        }
    }
    
    private static boolean _installedSupport = false;
    
    public static void installSupport() {
        if(!_installedSupport) {
            EOQualifierSQLGeneration.Support old = EOQualifierSQLGeneration.Support.supportForClass(EOKeyValueQualifier.class);
            EOQualifierSQLGeneration.Support.setSupportForClass(new KeyValueQualifierSQLGenerationSupport(old), EOKeyValueQualifier.class);
            _installedSupport = true;
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
        return (String)entity.primaryKeyAttributeNames().lastObject();
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
