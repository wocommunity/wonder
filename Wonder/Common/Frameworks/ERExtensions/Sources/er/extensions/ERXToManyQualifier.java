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

/**
Optimized toMany qualifier, much, much better SQL than the Apple provided qualifier.
Really nice when you want to find all the eos that have say five of the
ten eos in their toMany relationship. This qualifier will always only
generate three joins no matter how many eos you are  trying to find. Example usage:

 NSArray employees; // given
 EOEntity department; // given

 // Find all of the departments that have all of those employees
 ERXToManyQualifier q = new ERXToManyQualifier(department,                                                    "toEmployees", employees);
 EOFetchSpecification fs = new EOFetchSpecification("Department", q,                                                     null);
 NSArray departments = ec.objectsWithFetchSpecification(fs);

 If you want to find say departments that have 5 or more of the given
 employees (imagine you have a list of 10 or so), then you could
 construct the qualifier like:

 ERXToManyQualifier q = new ERXToManyQualifier(department,                                                    "toEmployees", employees, 5);

 or to find any department that has at least one of the given employees

 ERXToManyQualifier q = new ERXToManyQualifier(department,                                                    "toEmployees", employees, 1);
 */

public class ERXToManyQualifier extends EOSQLQualifier implements Cloneable {

    /** logging support */
    public static final ERXLogger log = ERXLogger.getERXLogger(ERXToManyQualifier.class);

    /** holds the entity */
    private EOEntity _entity;
    /** holds the to many key */    
    private String _toManyKey;
    /** holds the array of elements */    
    private NSArray _elements;
    /** holds the min count to match against, defaults to 0 */
    private int _minCount = 0;

    ///// Fix for EOSQLQualifier ///
    public boolean _isEmpty(){
        return false;
    }
    
    public ERXToManyQualifier(EOEntity e,
                                String toManyKey,
                                NSArray elements) {
        super(e,null,null);
        _entity=e;
        _toManyKey=toManyKey;
        _elements=elements;
    }

    public ERXToManyQualifier (EOEntity e,
                              String toManyKey,
                              NSArray elements,
                              int minCount) {
        super(e,null,null);
        _entity=e;
        _toManyKey=toManyKey;
        _elements=elements;
        _minCount = minCount;
    }
    
    protected static void appendColumnForAttributeToStringBuffer(EOAttribute attribute,
                                                              StringBuffer sb) {
        sb.append(attribute.entity().externalName());
        sb.append('.');
        sb.append(attribute.columnName());

    }

    public String sqlStringForSQLExpression(EOSQLExpression e) {

        StringBuffer result=new StringBuffer();

        NSArray pKeys=ERXEOAccessUtilities.primaryKeysForObjects(_elements);

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

    /**
     * Description of the qualfier.
     * @return description of the key and which elements it
     *		should contain.
     */
    public String toString() {
        return _toManyKey+" contains "+_elements;
    }

    /**
     * Implementation of the Cloneable interface.
     * @return clone of the qualifier.
     */
    public Object clone() {
        return new ERXToManyQualifier(_entity, _toManyKey, _elements, _minCount);
    }
}