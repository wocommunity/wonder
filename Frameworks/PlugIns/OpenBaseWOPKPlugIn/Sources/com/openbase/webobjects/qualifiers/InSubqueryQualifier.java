package com.openbase.webobjects.qualifiers;
//
//  InSubqueryQualifier.java
//
//  Much thanks to the Wonder guys and Pierre Bernard for their helpful examples
//
//  Created by Alex Cone on 12/18/05.
//  Copyright (c) 2005 __MyCompanyName__. All rights reserved.
//

import com.webobjects.foundation.*;
import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;

import java.util.*;
import java.lang.reflect.*;

import org.apache.log4j.*;

public class InSubqueryQualifier extends EOQualifier implements EOQualifierEvaluation, Cloneable {
    protected static Logger LOGGER = Logger.getLogger(InQualifier.class.getName());

    private static final String	InKeyword = " IN ";

    /** Path to an attribute or relationship of the qualified entity
        */
	protected String _key;
    
	/** Name of the entity from which to get values to match against
        */
	protected String _entityName;
    
	/** Name of the attribute in the destination entity to match against
        */
	protected String _attributePath;
    
	/** Qualifier to limit the list of acceptable values
        */
	protected EOQualifier _subQualifier;
    
    /** register SQL generation support for the qualifier */
    static {
        EOQualifierSQLGeneration.Support.setSupportForClass(new InSubqueryQualifierSQLGenerationSupport(), InSubqueryQualifier.class);
    }
    
    /** Constructor for queries off an attribute.
        * 
        * @param key key path to an attribute of the qualified entity
        * @param entityName name of the entity from which to get values to match against
        * @param attributePath name of the attribute in the destination entity to match against
        * @param subQualifier qualifier to limit the list of acceptable values
        */
	public InSubqueryQualifier(String key, String entityName, String attributePath, EOQualifier subQualifier) {
		if ((key == null) || (entityName == null) || (attributePath == null)) {
			throw new IllegalArgumentException("Arguments key, entityName and attributePath may not be null");
		}
        this.setKey(key);
        this.setEntityName(entityName);
        this.setAttributePath(attributePath);
        this.setSubQualifier(subQualifier);
	}
    
	/** Constructor for queries off a relationship
        * 
        * @param key key path to a relationship of the qualified entity
        * @param subQualifier qualifier to limit the list of acceptable values
        */
	public InSubqueryQualifier(String key, EOQualifier subQualifier) {
		if (key == null) {
			throw new IllegalArgumentException("Argument key may not be null");
		}
        this.setKey(key);
        this.setSubQualifier(subQualifier);
	}
    
    public String key() {
        return _key;
    }
    
    public void setKey(String aValue) {
        _key = aValue;
    }
    
	public String entityName() {
		return _entityName;
	}
    
    public void setEntityName(String aValue) {
        _entityName = aValue;
    }
    
	public String attributePath() {
		return _attributePath;
	}
    
    public void setAttributePath(String aValue) {
        _attributePath = aValue;
    }
    
	public EOQualifier subQualifier() {
		return _subQualifier;
	}
    
    public void setSubQualifier(EOQualifier aValue) {
        _subQualifier = aValue;
    }
    
    public String toString() {
		StringBuffer buffer = new StringBuffer();
 		buffer.append("(");
		buffer.append(key());
		buffer.append(InKeyword);
		buffer.append("(");
		buffer.append(" SELECT ");
		buffer.append((attributePath() != null) ? attributePath() : "*");
        
		if (entityName() != null) {
			buffer.append(" FROM ");
			buffer.append(entityName());
		}
		if (subQualifier() != null) {
			buffer.append(" WHERE ");
			buffer.append(subQualifier().toString());
		}
		buffer.append(")");
		return buffer.toString();
	}
    
    public void addQualifierKeysToSet(NSMutableSet keySet) 	{
		keySet.addObject(key());
		if (subQualifier() != null) {
			NSMutableSet subKeySet = new NSMutableSet();
			subQualifier().addQualifierKeysToSet(subKeySet);
			if (entityName() == null) {
				Enumeration subKeys = subKeySet.objectEnumerator();
				String prefix = key() + NSKeyValueCodingAdditions.KeyPathSeparator;
 				while (subKeys.hasMoreElements()) {
					keySet.addObject(prefix + subKeys.nextElement());
				}
			} else {
				keySet.addObjectsFromArray(subKeySet.allObjects());
			}
		}
	}
    
    // we don't do bindings yet
    public EOQualifier qualifierWithBindings(NSDictionary someBindings, boolean requiresAll) {
        return (EOQualifier) this.clone();
    }
    // we don't do validation
    public void validateKeysWithRootClassDescription(EOClassDescription aClassDescription) {
    }
    
    public Object clone() {
		return new InSubqueryQualifier(key(), entityName(), attributePath(), subQualifier());
	}
    
    public static class InSubqueryQualifierSQLGenerationSupport extends EOQualifierSQLGeneration.Support {
        
        public InSubqueryQualifierSQLGenerationSupport() {
            super();
        }
        
        public String sqlStringForSQLExpression(EOQualifier eoqualifier, EOSQLExpression aSQLExpression) {
            String sqlString = null;
            if ((aSQLExpression != null) && (aSQLExpression.entity() != null)) {
                InSubqueryQualifier isQualifier = (InSubqueryQualifier)eoqualifier;
                EOEntity anEntity = aSQLExpression.entity();
                String aKey = isQualifier.key();
                
                StringBuffer sb = new StringBuffer();
                String attributeString = aSQLExpression.sqlStringForAttributeNamed(aKey);
                sb.append(aSQLExpression.formatSQLString(attributeString, attributeForPath(anEntity, aKey).readFormat()));
                sb.append(InSubqueryQualifier.InKeyword);
                sb.append("(");
                
                EOEntity subEntity = anEntity.model().modelGroup().entityNamed(isQualifier.entityName());
                
                EODatabaseContext context = EODatabaseContext.registeredDatabaseContextForModel(subEntity.model(),
                                                                                                EOObjectStoreCoordinator.defaultCoordinator());
                EOSQLExpressionFactory factory = context.database().adaptor().expressionFactory();
                EOSQLExpression subExpression = factory.expressionForEntity(subEntity);
                
                // EOSQLExpression subExpression = expressionForEntity(subEntity);
                EOFetchSpecification subFetch = new EOFetchSpecification(subEntity.name(), isQualifier.subQualifier(), null);
                String attributePath = isQualifier.attributePath();
                NSArray subAttributes;
                if (attributePath != null) {
                    subAttributes = new NSArray(attributeForPath(subEntity, attributePath));
                } else {
                    subAttributes = subEntity.primaryKeyAttributes();
                }
                
                StringBuffer subBuffer = new StringBuffer();
                
                subExpression.aliasesByRelationshipPath().setObjectForKey("t1", "");
                subExpression.setUseAliases(true);
                subExpression.prepareSelectExpressionWithAttributes(subAttributes, false, subFetch);
                
                subBuffer.append("SELECT ");
                subBuffer.append(subExpression.listString());
                subBuffer.append(" FROM ");
                subBuffer.append(subExpression.tableListWithRootEntity(subEntity));
                
                boolean hasWhereClause = ((subExpression.whereClauseString() != null) && 
                                          (subExpression.whereClauseString().length() > 0));
                if (hasWhereClause) {
                    subBuffer.append(" WHERE ");
                    subBuffer.append(subExpression.whereClauseString());
                }
                if ((subExpression.joinClauseString() != null)
                    && (subExpression.joinClauseString().length() > 0)) {
                    if (hasWhereClause) {
                        subBuffer.append(" AND ");
                    }
                    subBuffer.append(subExpression.joinClauseString());
                }
                String subquerySqlString = subBuffer.toString();
                sb.append(subquerySqlString.replaceAll("t0.", "t1."));
                sb.append(")");
                sqlString = sb.toString();
                
                Enumeration bindVariables = subExpression.bindVariableDictionaries().objectEnumerator();
                while (bindVariables.hasMoreElements()) {
                    aSQLExpression.addBindVariableDictionary((NSDictionary) bindVariables.nextElement());
                }
                
            }
            return sqlString;
        }
        
		public EOQualifier schemaBasedQualifierWithRootEntity(EOQualifier qualifier, EOEntity entity) {
			return qualifier;
        }

        public EOQualifier qualifierMigratedFromEntityRelationshipPath(EOQualifier eoqualifier, EOEntity eoentity, String s) {
            // the key migration is the same as for EOKeyValueQualifier
            InSubqueryQualifier isQualifier=(InSubqueryQualifier)eoqualifier;
            return new InSubqueryQualifier(_translateKeyAcrossRelationshipPath(isQualifier.key(), s, eoentity), 
                                           isQualifier.entityName(), isQualifier.attributePath(), isQualifier.subQualifier() );
        }
        
        
        // Protected instance methods
        protected EOAttribute attributeForPath(EOEntity entity, String keyPath) {
            if (keyPath != null) {
                StringTokenizer tokenizer = new StringTokenizer(keyPath, NSKeyValueCodingAdditions.KeyPathSeparator);
                EORelationship relationship = null;
                while (tokenizer.hasMoreElements()) {
                    String key = tokenizer.nextToken();
                    if (tokenizer.hasMoreElements()) {
                        relationship = entity.anyRelationshipNamed(key);
                    } else {
                        return entity.anyAttributeNamed(key);
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
        
        protected EOSQLExpression expressionForEntity(EOEntity entity) {
            try {
                Class expressionClass = ((EOAdaptor)EOAdaptor.adaptorWithModel(entity.model())).expressionClass();
                Constructor constructor = expressionClass.getConstructor(new Class[] { EOEntity.class });
                EOSQLExpression expression = (EOSQLExpression)constructor.newInstance(new Object[] { entity });
                return expression;
            } catch (Exception exception) {
                throw new NSForwardException(exception);
            }
        }
        
    }        
}
