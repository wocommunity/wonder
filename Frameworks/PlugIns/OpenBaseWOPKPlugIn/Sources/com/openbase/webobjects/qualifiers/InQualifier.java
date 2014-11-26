package com.openbase.webobjects.qualifiers;
//
//  InQualifier.java
//
//  Much thanks to the Wonder guys and Pierre Bernard for their helpful examples
//
//  The in qualifier allows qualification on an attribute that is contained in a list of values. This qualifier
//  supports both in-memory and sql based qualification.
//  
//  The SQL generated is of the form: "VALUE IN (XX, YY, ZZ)"
//  
//  Created by Alex Cone on 12/16/05.
//  Copyright (c) 2005 __MyCompanyName__. All rights reserved.
//

import com.webobjects.foundation.*;
import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import java.util.*;
import org.apache.log4j.*;

public class InQualifier extends EOKeyValueQualifier implements EOQualifierEvaluation, Cloneable {
    protected static Logger LOGGER = Logger.getLogger(InQualifier.class.getName());
    
    private static final String	InKeyword = " IN ";
    
    private String	_key = null;
    private NSArray	_values = null;
    
    /** register SQL generation support for the qualifier */
    static {
        EOQualifierSQLGeneration.Support.setSupportForClass(new InQualifierSQLGenerationSupport(), InQualifier.class);
    }
    
    public static String allButLastPathComponent(String path) {
        int i = path.lastIndexOf(NSKeyValueCodingAdditions.KeyPathSeparator);
        return (i < 0) ? null : path.substring(0, i);
	}
    
    public static String lastPathComponent(String path) {
        int i = path.lastIndexOf(NSKeyValueCodingAdditions.KeyPathSeparator);
        return (i < 0) ? path : path.substring(i + 1);
	}
    
    public InQualifier(String aKey, NSArray values) {
        // Just to make EOKeyValueQualifier happy
        super(aKey, EOQualifier.QualifierOperatorEqual, values);
        this.setKey(aKey);
        this.setValues(values);
    }
    
    public String key() {
        return _key;
    }
    
    public void setKey(String aValue) {
        _key = aValue;
    }
    
    public NSArray values() {
        return _values;
    }
    
    public void setValues(NSArray anArray) {
        _values = anArray;
    }
    
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        
		buffer.append("(");
		buffer.append(key());
		buffer.append(InKeyword);
		buffer.append("(");
        
		Enumeration e = values().objectEnumerator();
		while (e.hasMoreElements()) {
			Object object = e.nextElement();
            
			if (object == NSKeyValueCoding.NullValue) {
				buffer.append("null");
			} else if (object instanceof Number) {
				buffer.append(object);
			} else if (object instanceof EOQualifierVariable) {
				buffer.append("$");
				buffer.append(((EOQualifierVariable) object).key());
			} else {
				buffer.append("'");
				buffer.append(object);
				buffer.append("'");
			}
			if (e.hasMoreElements()) {
				buffer.append(", ");
			}
		}
		buffer.append("))");
		return buffer.toString();
    }
    
    public void addQualifierKeysToSet(NSMutableSet aSet) {
        if (aSet != null) {
            String	aKey = this.key();
            if (aKey != null) {
                aSet.addObject(aKey);
            }
        }
    }
    
    public boolean evaluateWithObject(Object object) {
		Object value = NSKeyValueCodingAdditions.Utility.valueForKeyPath(object, key());
		if (value == null) {
			value = NSKeyValueCoding.NullValue;
		}
		return values().containsObject(value);
	}
    
    // we don't do bindings
    public EOQualifier qualifierWithBindings(NSDictionary someBindings, boolean requiresAll) {
        return (EOQualifier) this.clone();
    }
    // we don't do validation
    public void validateKeysWithRootClassDescription(EOClassDescription aClassDescription) {
        super.validateKeysWithRootClassDescription(aClassDescription);
    }
    
    
    public static class InQualifierSQLGenerationSupport extends EOQualifierSQLGeneration.Support {
        
        public InQualifierSQLGenerationSupport() {
            super();
        }
        
        public String sqlStringForSQLExpression(EOQualifier eoqualifier, EOSQLExpression aSQLExpression) {
            String sqlString = null;
            if ((aSQLExpression != null) && (aSQLExpression.entity() != null)) {
                InQualifier inQualifier = (InQualifier)eoqualifier;
                EOEntity anEntity = aSQLExpression.entity();
                String aKey = inQualifier.key();
                NSArray aValuesArray = inQualifier.values();
                
                if ((aKey != null) && (aValuesArray != null) && (aValuesArray.count() > 0)) {
                    StringBuffer sb = new StringBuffer();
                    EOAttribute keyAttr = attributeForPath(anEntity, aKey);
                    String attributeString = aSQLExpression.sqlStringForAttribute(keyAttr);
                    sb.append(aSQLExpression.formatSQLString(attributeString, keyAttr.readFormat()));
                    sb.append(InQualifier.InKeyword);
                    sb.append("(");
                    
                    for (int i = 0; i < aValuesArray.count(); i++ ) {
                        EOKeyValueQualifier containsQualifier = new EOKeyValueQualifier(aKey, EOQualifier.QualifierOperatorContains, 
                                                                                        aValuesArray.objectAtIndex(i));
                        containsQualifier = (EOKeyValueQualifier) anEntity.schemaBasedQualifier(containsQualifier);
                        if ( i > 0 ) {
                            sb.append(", ");
                        }
                        sb.append(aSQLExpression.sqlStringForValue(containsQualifier.value(), containsQualifier.key()));
                    }
                    sb.append(")");
                    sqlString = sb.toString();
                }
            }
            return sqlString;
        }
        
        public EOQualifier schemaBasedQualifierWithRootEntity(EOQualifier qualifier, EOEntity entity) {
            InQualifier inQualifier = (InQualifier) qualifier;
            String keyPath = inQualifier.key();
            EORelationship relationship = relationshipForPath(entity, keyPath);
            
            if (relationship != null) {
                if (relationship.isFlattened()) {
                    relationship = (EORelationship) relationship.componentRelationships().lastObject();
                }
                // just handle single key joins for now
                EOJoin join = (EOJoin)relationship.joins().objectAtIndex(0);
                String destAttributeName = join.destinationAttribute().name();
                String optimizedPath = optimizeQualifierKeyPath(entity, keyPath, destAttributeName);
                                    
                NSMutableSet newValues = new NSMutableSet(inQualifier.values().count());
                Enumeration values = inQualifier.values().objectEnumerator();
                
                while (values.hasMoreElements()) {
                    Object value = values.nextElement();
                    
                    if (value == NSKeyValueCoding.NullValue || (value instanceof EOQualifierVariable)) {
                        newValues.addObject(value);
                    } else {
                        EOEnterpriseObject enterpriseObject = (EOEnterpriseObject) value;
                        EOObjectStoreCoordinator objectStoreCoordinator =
                            (EOObjectStoreCoordinator) enterpriseObject.editingContext().rootObjectStore();
                        NSDictionary destValues =
                            objectStoreCoordinator.valuesForKeys(new NSArray(destAttributeName), enterpriseObject);
                        Object destVal = destValues.objectForKey(destAttributeName);
                        newValues.addObject((destVal != null ? destVal: NSKeyValueCoding.NullValue));
                    }
                }
                return nullValueAwareQualifier(new InQualifier(optimizedPath, newValues.allObjects()));
            } else {
                return nullValueAwareQualifier(inQualifier);
            }
        }
        
        public EOQualifier qualifierMigratedFromEntityRelationshipPath(EOQualifier eoqualifier, EOEntity eoentity, String s) {
            // the key migration is the same as for EOKeyValueQualifier
            InQualifier inQualifier=(InQualifier)eoqualifier;
            return new InQualifier(_translateKeyAcrossRelationshipPath(inQualifier.key(), s, eoentity), inQualifier.values());
        }
        
        // Protected instance methods
        protected EOQualifier nullValueAwareQualifier(InQualifier inQualifier) {
            if (inQualifier.values().containsObject(NSKeyValueCoding.NullValue)) {
                EOQualifier nullQual = new EOKeyValueQualifier(inQualifier.key(), EOQualifier.QualifierOperatorEqual, NSKeyValueCoding.NullValue);
                NSSet aSet = new NSSet(inQualifier.values());
                NSSet noNullSet = aSet.setBySubtractingSet(new NSSet(NSKeyValueCoding.NullValue));
                EOQualifier noNullQual = new InQualifier(inQualifier.key(), noNullSet.allObjects());
                return new EOOrQualifier(new NSArray(new Object[] { nullQual, noNullQual }));
            } else {
                return inQualifier;
            }
        }
        
        protected EOAttribute attributeForPath(EOEntity entity, String keyPath) {
            if (keyPath != null) {
                StringTokenizer tokenizer = new StringTokenizer(keyPath, NSKeyValueCodingAdditions.KeyPathSeparator);
                EORelationship relationship = null;
                while (tokenizer.hasMoreElements()) {
                    String key = tokenizer.nextToken();
                    if (tokenizer.hasMoreElements()) {
                        relationship = entity.anyRelationshipNamed(key);
                    } else {
                        EOAttribute attribute = entity.anyAttributeNamed(key);
                        if (attribute == null) {
                            relationship = entity.anyRelationshipNamed(key);
                            if (relationship != null) {
                                if (relationship.isFlattened()) {
                                    relationship = (EORelationship) relationship.componentRelationships().lastObject();
                                }
                                // just handle single key joins for now
                                EOJoin join = (EOJoin)relationship.joins().objectAtIndex(0);
                                return join.sourceAttribute();
                            }
                        }
                    }
                    if (relationship != null) {
                        entity = relationship.destinationEntity();
                    } else {
                        return null;
                    }
                }
                return null;
            }
            return null;
        }

        protected EORelationship relationshipForPath(EOEntity entity, String keyPath) {
            if (keyPath != null) {
                StringTokenizer tokenizer = new StringTokenizer(keyPath, NSKeyValueCodingAdditions.KeyPathSeparator);
                EORelationship relationship = null;
                while (tokenizer.hasMoreElements()) {
                    String key = tokenizer.nextToken();
                    relationship = entity.anyRelationshipNamed(key);
                    if (relationship != null) {
                        entity = relationship.destinationEntity();
                    } else {
                        return null;
                    }
                }
                return relationship;
            }
            return null;
        }
        
        protected String optimizeQualifierKeyPath(EOEntity entity, String keyPath, String attributeName) {
            if ((keyPath == null) || (keyPath.length() == 0)) {
                return attributeName;
            } else {
                EORelationship relationship = (entity == null) ? null : relationshipForPath(entity, keyPath);
                if (relationship != null) {
                    NSArray joins = relationship.joins();
                    int joinCount = (joins == null) ? 0 : joins.count();
                    for (int i = joinCount - 1; i >= 0; i--) {
                        EOJoin join = (EOJoin) joins.objectAtIndex(i);
                        if (join.destinationAttribute().name().equals(attributeName)) {
                            String newPath = allButLastPathComponent(keyPath);
                            String newAttributeName = join.sourceAttribute().name();
                            return optimizeQualifierKeyPath(entity, newPath, newAttributeName);
                        }
                    }
                }
                return (keyPath + NSKeyValueCodingAdditions.KeyPathSeparator + attributeName);
            }
        }
        
    }
        
    // cloning support
    public Object clone() {
        return new InQualifier(this.key(), this.values());
    }
    
}
