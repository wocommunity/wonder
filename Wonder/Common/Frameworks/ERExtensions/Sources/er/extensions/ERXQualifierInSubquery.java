/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions;

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;

// generates a subquery for the qualifier given in argument
//
//   ...  t0.ID IN (SELECT t0.ID FROM X WHERE <your qualifier here> ) ..
//
//
// this class can be used to work around the EOF bug where OR
// queries involving many-to-manies are incorrectly generated
//
//
// It will also generate
//
//  ... t0.FOREIGN_KEY_ID in (select t1.ID from X where <your qualifier here>)
//
// with the 3 arg constructor

// FIXME: Dues to the way the SQL is generated the three arguement constructor has conflicts in the
//	  table names used for instance, this bit of code:
//        EOQualifier q = EOQualifier.qualifierWithQualifierFormat("firstName = 'Max'", null);
//	  ERXQualifierInSubquery qq = new ERXQualifierInSubquery(q, "User", "groupId");
//	  EOFetchSpecification fs = new EOFetchSpecification("Group", qq, null);
//
// Would generate: "SELECT t0.GROUP_ID, t0.NAME FROM GROUP t0 WHERE t0.GROUP_ID IN ( SELECT t0.GROUP_ID FROM GROUP t0 WHERE t0.NAME = ? ) "
public class ERXQualifierInSubquery extends EOQualifier implements EOQualifierSQLGeneration, Cloneable {

    /** holds the subqualifier */
    protected EOQualifier qualifier;

    /** holds the entity name */
    protected String entityName;

    /** holds the attribute name */
    protected String attributeName;

    /**
     * Public single argument constructor. Use
     * this constructor for sub-qualification
     * on the same table.
     * @param q sub-qualifier
     */
    public ERXQualifierInSubquery(EOQualifier q) {        
        this(q, null, null);
    }

    /**
     * Public three qrgument constructor. Use
     * this constructor for for building queries
     * on foriegn key attributes of the current
     * entity.
     * @param q sub qualifier
     * @param entityName of the sub qualification
     * @param attributeName foriegn key attribute name
     */
    // ENHANCEME: Should be able to just use a relationship key instead of both.
    public ERXQualifierInSubquery(EOQualifier q, String entityName, String attributeName) {
        super();
        this.qualifier = q;
        this.entityName = entityName;
        this.attributeName = attributeName;
    }

    //	===========================================================================
    //	EOQualifier method(s)
    //	---------------------------------------------------------------------------
    // FIXME: Shoudl do something here ...
    public void addQualifierKeysToSet(NSMutableSet aSet) {
    }

    public EOQualifier qualifierWithBindings(NSDictionary someBindings, boolean requiresAll) {
        return (EOQualifier)this.clone();
    }

    // FIXME: Should do something here ...
    public void validateKeysWithRootClassDescription(EOClassDescription aClassDescription)
    {
    }    

    
    public String sqlStringForSQLExpression(EOSQLExpression e) {
        StringBuffer sb = new StringBuffer();
        if (attributeName != null)
            sb.append(e.sqlStringForAttributeNamed(attributeName));
        else {
            EOAttribute pk = (EOAttribute)e.entity().primaryKeyAttributes().lastObject();
            sb.append(e.sqlStringForAttribute(pk));            
        }
        sb.append(" IN ( ");
        EOEntity entity;
        if (entityName == null) {
            entity = e.entity();
        } else {
            entity = e.entity().model().entityNamed(entityName);
        }
        EOFetchSpecification fs=new EOFetchSpecification(entity.name(),
                                                         qualifier,
                                                         null,
                                                         false,
                                                         true,
                                                         null);
        // ASSUME: This makes a few assumptions, if anyone can figure out a full proof way that would be nice to get the model
        //	   Note you can't use: EOAdaptor.adaptorWithModel(e.entity().model()).expressionFactory(); as this creates a
        //
        EODatabaseContext context = EODatabaseContext.registeredDatabaseContextForModel(entity.model(), EOObjectStoreCoordinator.defaultCoordinator());
        EOSQLExpressionFactory factory = context.database().adaptor().expressionFactory();
        EOSQLExpression expression=factory.selectStatementForAttributes(entity.primaryKeyAttributes(),
                                           false,
                                           fs,
                                           entity);

        sb.append(expression.statement());        
        sb.append(" ) ");
        return sb.toString();
    }

    public EOQualifier schemaBasedQualifierWithRootEntity(EOEntity anEntity) {
        return (EOQualifier)this.clone();
    }

    public EOQualifier qualifierMigratedFromEntityRelationshipPath(EOEntity anEntity, String aPath) {
        return (EOQualifier)this.clone();
    }    
    
    public String toString() { return " <" + getClass().getName() +"> '" + qualifier.toString() + "'"; }

    public Object clone() {
        return new ERXQualifierInSubquery(qualifier, entityName, attributeName);
    }    
}
