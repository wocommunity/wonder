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
import com.webobjects.appserver.*;
import java.util.*;
import org.apache.log4j.Category;

// Optimized toMany qualifier, much, much better SQL than the Apple provided qualifier.

// FIXME: Should rename ERXToManyQualifier
public class ERXEOToManyQualifier extends EOSQLQualifier implements Cloneable {

    /** logging support */
    public static final Category cat = Category.getInstance(ERXEOToManyQualifier.class);

    /** holds the entity */
    private EOEntity _entity;
    /** holds the to many key */    
    private String _toManyKey;
    /** holds the array of elements */    
    private NSArray _elements;
    /** holds the min count to match against, defaults to 0 */
    private int _minCount = 0;
    
    public ERXEOToManyQualifier(EOEntity e,
                                String toManyKey,
                                NSArray elements) {
        super(e,null,null);
        _entity=e;
        _toManyKey=toManyKey;
        _elements=elements;
    }

    public ERXEOToManyQualifier (EOEntity e,
                              String toManyKey,
                              NSArray elements,
                              int minCount) {
        super(e,null,null);
        _entity=e;
        _toManyKey=toManyKey;
        _elements=elements;
        _minCount = minCount;
    }
    
    public static void appendColumnForAttributeToStringBuffer(EOAttribute attribute,
                                                              StringBuffer sb) {
        sb.append(attribute.entity().externalName());
        sb.append('.');
        sb.append(attribute.columnName());

    }

    /**
     * Creates an array containing all of the primary
     * keys of the given objects.
     * @param eos array of enterprise objects
     */
    // MOVEME: ERXEOFUtilities
    public static NSArray primaryKeysForObjectsFromSameEntity(NSArray eos) {
        NSMutableArray result=new NSMutableArray();
        if (eos.count()>0) {
            for (Enumeration e=eos.objectEnumerator(); e.hasMoreElements();) {
                EOEnterpriseObject target=(EOEnterpriseObject)e.nextElement();
                NSDictionary pKey=EOUtilities.primaryKeyForObject(target.editingContext(),target);
                result.addObject(pKey.allValues().objectAtIndex(0));
            }
        }
        return result;           
    }

    /**
     * 
     */
    // MOVEME: ERXEOFUtilities
    // FIXME: Misnamed
    public static NSArray primaryKeysForObjectsFromSameEntity(String relKey, NSArray eos) {
        NSMutableArray result=new NSMutableArray();
        if (eos.count()>0) {
            String entityName=((EOEnterpriseObject)eos.objectAtIndex(0)).entityName();
            EOEditingContext ec = ((EOEnterpriseObject)eos.objectAtIndex(0)).editingContext();
            // FIXME: Bad way to get the model group
            EOEntity entity=EOModelGroup.defaultGroup().entityNamed(entityName);
            EORelationship relationship = entity.relationshipNamed(relKey);
            EOAttribute attribute = (EOAttribute)relationship.sourceAttributes().objectAtIndex(0);
            EODatabaseContext context = EOUtilities.databaseContextForModelNamed(ec, entity.model().name());
            String name=attribute.name();
            for (Enumeration e=eos.objectEnumerator(); e.hasMoreElements();) {
                EOEnterpriseObject target=(EOEnterpriseObject)e.nextElement();
                Object value = (context.snapshotForGlobalID(ec.globalIDForObject(target))).valueForKey(name);
                result.addObject(value);
            }
        }
        return result;
    }

    public String sqlStringForSQLExpression(EOSQLExpression e) {

        StringBuffer result=new StringBuffer();

        NSArray pKeys=primaryKeysForObjectsFromSameEntity(_elements);

        String tableName=_entity.externalName();
        NSArray toManyKeys=NSArray.componentsSeparatedByString(_toManyKey,".");
        EORelationship targetRelationship=null;
        EOEntity targetEntity=_entity;
        for (int i=0; i<toManyKeys.count()-1;i++) {
            targetRelationship= targetEntity.relationshipNamed((String)toManyKeys.objectAtIndex(i));
            targetEntity=targetRelationship.destinationEntity();
        }
        targetRelationship=targetEntity.relationshipNamed((String)toManyKeys.lastObject());
        targetEntity=targetRelationship.destinationEntity();

       
        if (targetRelationship.joins()==null || targetRelationship.joins().count()==0) {
            // we have a flattened many to many
            String definitionKeyPath=targetRelationship.definition();                        
            NSArray definitionKeys=NSArray.componentsSeparatedByString(definitionKeyPath,".");
            EOEntity lastStopEntity=targetRelationship.entity();
            String lastStopPrimaryKeyName=(String)lastStopEntity.primaryKeyAttributeNames().objectAtIndex(0);
            EORelationship firstHopRelationship= lastStopEntity.relationshipNamed((String)definitionKeys.objectAtIndex(0));
            EOEntity endOfFirstHopEntity= firstHopRelationship.destinationEntity();
            EOJoin join=(EOJoin) firstHopRelationship.joins().objectAtIndex(0); // assumes 1 join
            EOAttribute sourceAttribute=join.sourceAttribute();
            EOAttribute targetAttribute=join.destinationAttribute();
            EORelationship secondHopRelationship=endOfFirstHopEntity.relationshipNamed((String)definitionKeys.objectAtIndex(1));
            join=(EOJoin) secondHopRelationship.joins().objectAtIndex(0); // assumes 1 join
            EOAttribute secondHopSourceAttribute=join.sourceAttribute();

            NSMutableArray lastStopPKeyPath=new NSMutableArray(toManyKeys);
            lastStopPKeyPath.removeLastObject();
            lastStopPKeyPath.addObject(firstHopRelationship.name());
            lastStopPKeyPath.addObject(targetAttribute.name());
            String firstHopRelationshipKeyPath=lastStopPKeyPath.componentsJoinedByString(".");
            result.append(e.sqlStringForAttributeNamed(firstHopRelationshipKeyPath));
            result.append(" IN ( SELECT ");

            result.append(lastStopEntity.externalName());
            result.append('.');
            result.append(((EOAttribute)lastStopEntity.primaryKeyAttributes().objectAtIndex(0)).columnName());

            result.append(" FROM ");

            result.append(lastStopEntity.externalName());
            result.append(',');

            lastStopPKeyPath.removeLastObject();
            String tableAliasForJoinTable=(String)e.aliasesByRelationshipPath().
                objectForKey(lastStopPKeyPath.componentsJoinedByString("."));//"j"; //+random#
            result.append(endOfFirstHopEntity.externalName());
            result.append(' ');
            result.append(tableAliasForJoinTable);

            result.append(" WHERE ");

            appendColumnForAttributeToStringBuffer(sourceAttribute,result);
            result.append('=');
            result.append(e.sqlStringForAttributeNamed(firstHopRelationshipKeyPath));

            result.append(" AND ");
            
            result.append(tableAliasForJoinTable);
            result.append('.');
            result.append(secondHopSourceAttribute.columnName());
            
            result.append(" IN "); // FIXME !!
            result.append(pKeys);

            result.append(" GROUP BY ");
            appendColumnForAttributeToStringBuffer(sourceAttribute,result);

            result.append(" HAVING COUNT(*)");
            if (_minCount <= 0) {
                result.append("=" + _elements.count());
            } else {
                result.append(">=" + _minCount);                
            }
            result.append(" )");
        } else {
            throw new RuntimeException("not implemented!!");
        }
        return result.toString();
    }

    public String description() {
        return _toManyKey+" contains "+_elements;
    }
    public String toString() {
        return description();
    }

    /**
     * EOF seems to be wanting to clone qualifiers when
     * the are inside an and-or qualifier without this
     * method, ERXToManyQualifier is cloned into an
     * EOSQLQualifier and the generated SQL is incorrect..
     */
    public Object clone() {
        return new ERXEOToManyQualifier(_entity, _toManyKey, _elements, _minCount);
    }
}