/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions.eof.qualifiers;

import org.apache.log4j.Logger;

import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOJoin;
import com.webobjects.eoaccess.EOQualifierSQLGeneration;
import com.webobjects.eoaccess.EORelationship;
import com.webobjects.eoaccess.EOSQLExpression;
import com.webobjects.eocontrol.EOClassDescription;
import com.webobjects.eocontrol.EOKeyValueQualifier;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSKeyValueCodingAdditions;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableSet;

import er.extensions.eof.ERXEOAccessUtilities;
import er.extensions.foundation.ERXArrayUtilities;
import er.extensions.qualifiers.ERXKeyValueQualifier;

/**
 * Optimized toMany qualifier, much, much better SQL than the Apple provided qualifier.
 * Really nice when you want to find all the eos that have say five of the
 * ten eos in their toMany relationship. This qualifier will always only
 * generate three joins no matter how many eos you are  trying to find. Example usage:
 * <pre><code>
 * NSArray employees; // given, can be null
 * // Find all of the departments that have all of those employees
 * ERXToManyQualifier q = new ERXToManyQualifier("toEmployees", employees);
 * EOFetchSpecification fs = new EOFetchSpecification("Department", q, null);
 * NSArray departments = ec.objectsWithFetchSpecification(fs);
 * </code></pre>
 * If you want to find say departments that have 5 or more of the given
 * employees (imagine you have a list of 10 or so), then you could
 * construct the qualifier like: <br>
 * <code> ERXToManyQualifier q = new ERXToManyQualifier("toEmployees", employees, 5);</code><br>
 * or to find any department that has at least one of the given employees<br>
 * <code> ERXToManyQualifier q = new ERXToManyQualifier("toEmployees", employees, 1);</code>
 */

public class ERXToManyQualifier extends ERXKeyValueQualifier implements Cloneable {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    /** register SQL generation support for the qualifier */
    static {
        EOQualifierSQLGeneration.Support.setSupportForClass(new ToManyQualifierSQLGenerationSupport(), ERXToManyQualifier.class);
    }
    
    public static final String MatchesAllInArraySelectorName = "matchesAllInArray";

    /** logging support */
    public static final Logger log = Logger.getLogger(ERXToManyQualifier.class);

    /** holds the to many key */    
    private String _toManyKey;
    /** holds the array of elements */    
    private NSArray _elements;
    /** holds the min count to match against, defaults to 0 */
    private int _minCount = 0;

    public ERXToManyQualifier(String toManyKey, NSArray elements) {
        this(toManyKey,elements, 0);
    }

    public ERXToManyQualifier(String toManyKey, NSArray elements, int minCount) {
    	super(toManyKey, EOQualifier.QualifierOperatorEqual, elements);
        _toManyKey=toManyKey;
        _elements=elements;
        _minCount = minCount;
    }
    
    public NSArray elements() {
        return _elements;
    }
    
    @Override
	public String key() {
        return _toManyKey;
    }
    
    public int minCount() {
        return _minCount;
    }

    /**
     * Description of the qualifier.
     * @return description of the key and which elements it
     *		should contain.
     */
    @Override
	public String toString() {
        return "<" +_toManyKey + " contains " + (_minCount > 0 ? " " + _minCount + " "  : " all ") + " of " + _elements + ">";
    }

    /**
     * Implementation of the Cloneable interface.
     * @return clone of the qualifier.
     */
    @Override
	public Object clone() {
        return new ERXToManyQualifier(_toManyKey, _elements, _minCount);
    }

    /**
     * Adds SQL generation support. Note that the database needs to support
     * the IN operator.
     */
    public static class ToManyQualifierSQLGenerationSupport extends EOQualifierSQLGeneration.Support {

        /**
         * Public constructor
         */
        public ToManyQualifierSQLGenerationSupport() {
            super();
        }

        protected static void appendColumnForAttributeToStringBuilder(EOAttribute attribute, StringBuilder sb) {
            sb.append(attribute.entity().externalName());
            sb.append('.');
            sb.append(attribute.columnName());
        }

		@Override
        @SuppressWarnings("unchecked")
		public String sqlStringForSQLExpression(EOQualifier eoqualifier, EOSQLExpression e) {
            ERXToManyQualifier qualifier = (ERXToManyQualifier)eoqualifier;
            StringBuilder result = new StringBuilder();
            EOEntity targetEntity=e.entity();

            NSArray<String> toManyKeys=NSArray.componentsSeparatedByString(qualifier.key(),".");
            EORelationship targetRelationship=null;
            for (int i=0; i<toManyKeys.count()-1;i++) {
                targetRelationship= targetEntity.anyRelationshipNamed(toManyKeys.objectAtIndex(i));
                targetEntity=targetRelationship.destinationEntity();
            }
            targetRelationship=targetEntity.relationshipNamed(toManyKeys.lastObject());
            targetEntity=targetRelationship.destinationEntity();

            if (targetRelationship.joins()==null || targetRelationship.joins().isEmpty()) {
                // we have a flattened many to many
                String definitionKeyPath=targetRelationship.definition();                        
                NSArray<String> definitionKeys=NSArray.componentsSeparatedByString(definitionKeyPath,".");
                EOEntity lastStopEntity=targetRelationship.entity();
                EORelationship firstHopRelationship= lastStopEntity.relationshipNamed(definitionKeys.objectAtIndex(0));
                EOEntity endOfFirstHopEntity= firstHopRelationship.destinationEntity();
                EOJoin join= firstHopRelationship.joins().objectAtIndex(0); // assumes 1 join
                EOAttribute sourceAttribute=join.sourceAttribute();
                EOAttribute targetAttribute=join.destinationAttribute();
                EORelationship secondHopRelationship=endOfFirstHopEntity.relationshipNamed(definitionKeys.objectAtIndex(1));
                join= secondHopRelationship.joins().objectAtIndex(0); // assumes 1 join
                EOAttribute secondHopSourceAttribute=join.sourceAttribute();

                NSMutableArray<String> lastStopPKeyPath = toManyKeys.mutableClone();
                lastStopPKeyPath.removeLastObject();
                lastStopPKeyPath.addObject(firstHopRelationship.name());
                lastStopPKeyPath.addObject(targetAttribute.name());
                String firstHopRelationshipKeyPath=lastStopPKeyPath.componentsJoinedByString(".");
                result.append(e.sqlStringForAttributeNamed(firstHopRelationshipKeyPath));
                result.append(" IN ( SELECT ");

                result.append(lastStopEntity.externalName());
                result.append('.');
                result.append(lastStopEntity.primaryKeyAttributes().objectAtIndex(0).columnName());

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

                appendColumnForAttributeToStringBuilder(sourceAttribute, result);
                result.append('=');
                result.append(e.sqlStringForAttributeNamed(firstHopRelationshipKeyPath));
                
                if(qualifier.elements() != null) {
                    NSArray pKeys=ERXEOAccessUtilities.primaryKeysForObjects(qualifier.elements());
                    result.append(" AND ");
                    
                    result.append(tableAliasForJoinTable);
                    result.append('.');
                    result.append(secondHopSourceAttribute.columnName());
                    
                    result.append(" IN ("); 
                    EOAttribute pk = targetEntity.primaryKeyAttributes().lastObject();
                    for(int i = 0; i < pKeys.count(); i++) {
                        
                        Object key = pKeys.objectAtIndex(i);
                        String keyString = e.formatValueForAttribute(key, pk);
                        //AK: default is is broken
                        if("NULL".equals(keyString)) {
                        	keyString = "" + key;
                        }
                        result.append(keyString);
                        if(i < pKeys.count()-1) {
                            result.append(',');
                        }
                    }
                    result.append(") ");
                }
                result.append(" GROUP BY ");
                appendColumnForAttributeToStringBuilder(sourceAttribute, result);

                result.append(" HAVING COUNT(*)");
                if (qualifier.minCount() <= 0) {
                    result.append("=" + qualifier.elements().count());
                } else {
                    result.append(">=" + qualifier.minCount());                
                }
                result.append(" )");
            } else {
                throw new RuntimeException("not implemented!!");
            }
            return result.toString();
        }
        
        // ENHANCEME: This should support restrictive qualifiers on the root entity
        @Override
		public EOQualifier schemaBasedQualifierWithRootEntity(EOQualifier eoqualifier, EOEntity eoentity) {
            EOQualifier result = null;
            EOKeyValueQualifier qualifier = (EOKeyValueQualifier)eoqualifier;
            String key = qualifier.key();
             if(qualifier.selector().name().equals(MatchesAllInArraySelectorName)) {
            	EOQualifierSQLGeneration.Support support = EOQualifierSQLGeneration.Support.supportForClass(ERXToManyQualifier.class);
            	NSArray array = (NSArray) qualifier.value();
            	ERXToManyQualifier q = new ERXToManyQualifier(key, array, array.count() );
            	result = support.schemaBasedQualifierWithRootEntity(q, eoentity);
                return result;
            }
            return (EOQualifier)eoqualifier.clone();
        }

        @Override
		public EOQualifier qualifierMigratedFromEntityRelationshipPath(EOQualifier eoqualifier, EOEntity eoentity, String relationshipPath) {
            ERXToManyQualifier qualifier=(ERXToManyQualifier)eoqualifier;
            String newPath =  EOQualifierSQLGeneration.Support._translateKeyAcrossRelationshipPath(qualifier.key(), relationshipPath, eoentity);
            return new ERXToManyQualifier(newPath, qualifier.elements(), qualifier.minCount());
        }
    }

    @Override
	public EOQualifier qualifierWithBindings(NSDictionary arg0, boolean arg1) {
       	if (arg0 != null && arg0.count() > 0) {
    		throw new IllegalStateException(getClass().getName() + " doesn't support bindings");
    	}
    	return this;
     }

    /* (non-Javadoc)
     * @see com.webobjects.eocontrol.EOQualifier#validateKeysWithRootClassDescription(com.webobjects.eocontrol.EOClassDescription)
     */
    @Override
	public void validateKeysWithRootClassDescription(EOClassDescription arg0) {
        // TODO Auto-generated method stub
    }

    @Override
	public void addQualifierKeysToSet(NSMutableSet arg0) {
        throw new IllegalStateException(getClass().getName() + " doesn't support adding keys");
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean evaluateWithObject(Object object) {
    	boolean result = false;
    	if (object != null && object instanceof NSKeyValueCoding) {
            Object obj = ((NSKeyValueCoding)object).valueForKey(key());
            if (obj == null && object instanceof NSKeyValueCodingAdditions) {
            	obj = ((NSKeyValueCodingAdditions)object).valueForKeyPath(key());
            }
            if (obj instanceof NSArray) {
            	NSArray objArray = (NSArray)obj;
            	if (!objArray.isEmpty()) {
            		if(_minCount == 0) {
            			result = ERXArrayUtilities.arrayContainsArray(objArray, elements());
            		} else {
            			result = ERXArrayUtilities.intersectingElements(objArray, elements()).count() >= _minCount;
            		}
            	}
            }
    	}
    	return result;
    }
}