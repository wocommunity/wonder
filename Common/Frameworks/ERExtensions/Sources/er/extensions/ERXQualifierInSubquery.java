/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions;

import java.util.Enumeration;

import org.apache.log4j.Logger;

import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EODatabaseContext;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOQualifierSQLGeneration;
import com.webobjects.eoaccess.EOSQLExpression;
import com.webobjects.eoaccess.EOSQLExpressionFactory;
import com.webobjects.eocontrol.EOClassDescription;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.eocontrol.EOObjectStoreCoordinator;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableSet;

/**
 * Generates a subquery for the qualifier given in argument
 *
 *   ...  t0.ID IN (SELECT t0.ID FROM X WHERE <your qualifier here> ) ..
 *
 *
 * this class can be used to work around the EOF bug where OR
 * queries involving many-to-manies are incorrectly generated
 *
 *
 * It will also generate
 *
 *  ... t0.FOREIGN_KEY_ID in (select t1.ID from X where <your qualifier here>)
 *
 * with the 3 arg constructor
 */
 
//FIXME: Dues to the way the SQL is generated the three arguement constructor has conflicts in the
//       table names used for instance, this bit of code:
//       EOQualifier q = EOQualifier.qualifierWithQualifierFormat("firstName = 'Max'", null);
//       ERXQualifierInSubquery qq = new ERXQualifierInSubquery(q, "User", "groupId");
//       EOFetchSpecification fs = new EOFetchSpecification("Group", qq, null);
//
// Would generate: "SELECT t0.GROUP_ID, t0.NAME FROM GROUP t0 WHERE t0.GROUP_ID IN 
//                    ( SELECT t0.GROUP_ID FROM GROUP t0 WHERE t0.NAME = ? ) "
public class ERXQualifierInSubquery extends EOQualifier implements EOQualifierSQLGeneration, Cloneable {

    /** logging support */
    public static final Logger log = Logger.getLogger(ERXQualifierInSubquery.class);
    
    /** holds the subqualifier */
    protected EOQualifier qualifier;

    /** holds the entity name */
    protected String entityName;

    /** holds the attribute name */
    protected String attributeName;

    protected String destinationAttName;
    
    /**
     * Public single argument constructor. Use
     * this constructor for sub-qualification
     * on the same table.
     * @param q sub-qualifier
     */
    public ERXQualifierInSubquery(EOQualifier q) {        
        this(q, null, null, null);
    }

    /**
     * Public three argument constructor. Use
     * this constructor for for building queries
     * on foreign key attributes of the current
     * entity.
     * @param q sub qualifier
     * @param entityName of the sub qualification
     * @param attributeName foriegn key attribute name
     */
    // ENHANCEME: Should be able to just use a relationship key instead of both.
    public ERXQualifierInSubquery(EOQualifier q, String entityName, String attributeName, String destinationAttName) {
        super();
        qualifier = q;
        this.entityName = entityName;
        this.attributeName = attributeName;
        this.destinationAttName = destinationAttName;
    }

    //	===========================================================================
    //	EOQualifier method(s)
    //	---------------------------------------------------------------------------
    
    /**
     * Only used with qualifier keys which are not supported in
     * this qualifier at this time. Does nothing.
     * @param aSet of qualifier keys
     */
    // FIXME: Should do something here ...
    public void addQualifierKeysToSet(NSMutableSet aSet) {
    }

    /**
     * Creates another qualifier after replacing the values of the bindings.
     * Since this qualifier does not support qualifier binding keys a clone
     * of the qualifier is returned.
     * @param someBindings some bindings
     * @param requiresAll tells if the qualifier requires all bindings
     * @return clone of the current qualifier.
     */    
    public EOQualifier qualifierWithBindings(NSDictionary someBindings, boolean requiresAll) {
        return (EOQualifier)clone();
    }

    /**
     * This qualifier does not perform validation. This
     * is a no-op method.
     * @param aClassDescription to validation the qualifier keys
     *		against.
     */
    // FIXME: Should do something here ...
    public void validateKeysWithRootClassDescription(EOClassDescription aClassDescription)
    {
    }    
    
    /**
     * Generates the sql string for the given sql expression.
     * Bulk of the logic for generating the sub-query is in 
     * this method.
     * @param e a given sql expression
     * @return sql string for the current sub-query.
     */
    public String sqlStringForSQLExpression(EOSQLExpression e) {
        StringBuffer sb = new StringBuffer();
        if (attributeName != null)
            sb.append(e.sqlStringForAttributeNamed(attributeName));
        else {
            EOAttribute pk = (EOAttribute)e.entity().primaryKeyAttributes().lastObject();
            sb.append(e.sqlStringForAttribute(pk));            
        }
        sb.append(" IN ( ");
        EOEntity entity=entityName == null ? e.entity() : e.entity().model().modelGroup().entityNamed(entityName);

        EOFetchSpecification fs=new EOFetchSpecification(entity.name(),
                                                         qualifier,
                                                         null,
                                                         false,
                                                         true,
                                                         null);
        
        if (qualifier != null) {
        	qualifier = EOQualifierSQLGeneration.Support._schemaBasedQualifierWithRootEntity(qualifier, entity);
        }
        if (qualifier != fs.qualifier()) {
            fs.setQualifier(qualifier);
        }
        
        // ASSUME: This makes a few assumptions, if anyone can figure out a full proof way that would be nice to get the model
        //	   Note you can't use: EOAdaptor.adaptorWithModel(e.entity().model()).expressionFactory(); as this creates a
        //
        EODatabaseContext context = EODatabaseContext.registeredDatabaseContextForModel(entity.model(),
                                                                                        EOObjectStoreCoordinator.defaultCoordinator());
        EOSQLExpressionFactory factory = context.database().adaptor().expressionFactory();

        NSArray subAttributes = destinationAttName != null ? new NSArray(entity.attributeNamed(destinationAttName)) : entity.primaryKeyAttributes();
        
        EOSQLExpression subExpression = factory.expressionForEntity(entity);
        
        // Arroz: Having this table identifier replacement causes serious problems if
        // you have more than a table being processed in the subquery. Disabling it will
        // aparently not cause problems, because t0 inside the subquery is not the same
        // t0 outside it.
        
        //subExpression.aliasesByRelationshipPath().setObjectForKey("t1", "");
        subExpression.setUseAliases(true);
        subExpression.prepareSelectExpressionWithAttributes(subAttributes,
                                                            false,
                                                            fs);
        //EOSQLExpression expression=factory.selectStatementForAttributes(entity.primaryKeyAttributes(),
        //                                   false,
        //                                   fs,
        //                                   entity);

        for (Enumeration bindEnumeration = subExpression.bindVariableDictionaries().objectEnumerator();
             bindEnumeration.hasMoreElements();) {
            e.addBindVariableDictionary((NSDictionary)bindEnumeration.nextElement());
        }
        
        //sb.append(ERXStringUtilities.replaceStringByStringInString("t0.", "t1.", subExpression.statement()));        
        sb.append(subExpression.statement());    
        sb.append(" ) ");
        return sb.toString();
    }

    /**
     * Implementation of the EOQualifierSQLGeneration interface. Just
     * clones the qualifier.
     * @param anEntity an entity.
     * @return clone of the current qualifier.
     */
    public EOQualifier schemaBasedQualifierWithRootEntity(EOEntity anEntity) {
        return (EOQualifier)clone();
    }

    /**
     * Implementation of the EOQualifierSQLGeneration interface. Just
     * clones the qualifier.
     * @param anEntity an entity
     * @param aPath relationship path
     * @return clone of the current qualifier.
     */
    public EOQualifier qualifierMigratedFromEntityRelationshipPath(EOEntity anEntity, String aPath) {
        return (EOQualifier)clone();
    }    
    
    /**
     * Description of the qualifier
     * @return human readable description of the qualifier.
     */
    public String toString() { return " <" + getClass().getName() +"> '" + qualifier.toString() + "'"; }

    /**
     * Implementation of the Clonable interface. Clones the current qualifier.
     * @return cloned qualifier.
     */
    public Object clone() {
        return new ERXQualifierInSubquery(qualifier, entityName, attributeName, destinationAttName);
    }    
}
