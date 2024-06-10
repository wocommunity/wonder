/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions.eof.qualifiers;

import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EODatabaseContext;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOJoin;
import com.webobjects.eoaccess.EOProperty;
import com.webobjects.eoaccess.EOQualifierSQLGeneration;
import com.webobjects.eoaccess.EORelationship;
import com.webobjects.eoaccess.EOSQLExpression;
import com.webobjects.eoaccess.EOSQLExpressionFactory;
import com.webobjects.eocontrol.EOClassDescription;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.eocontrol.EOKeyValueArchiver;
import com.webobjects.eocontrol.EOKeyValueArchiving;
import com.webobjects.eocontrol.EOKeyValueCodingAdditions;
import com.webobjects.eocontrol.EOKeyValueUnarchiver;
import com.webobjects.eocontrol.EOObjectStoreCoordinator;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSCoder;
import com.webobjects.foundation.NSCoding;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSKeyValueCoding.UnknownKeyException;
import com.webobjects.foundation.NSKeyValueCodingAdditions;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableSet;

import er.extensions.eof.ERXKey;
import er.extensions.foundation.ERXArrayUtilities;
import er.extensions.foundation.ERXStringUtilities;

/**
 * A qualifier that qualifies using an EXISTS clause.
 *
 * It will produce an SQL clause like the following:
 *
 * <code>select t0.ID, t0.ATT_1, ... t0.ATT_N from FIRST_TABLE t0 where EXISTS (select t1.ID from ANOTHER_TABLE where t1.ATT_1 = ? and t1.FIRST_TABLE_ID = t0.ID)</code>
 *
 * @author Travis Cripps, Aaron Rosenzweig, Samuel Pelletier
 */
public class ERXExistsQualifier extends EOQualifier implements Cloneable, NSCoding, EOKeyValueArchiving {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	/** 
	 * <a href="http://wiki.wocommunity.org/display/documentation/Wonder+Logging">new org.slf4j.Logger</a> 
	 */
	static final Logger log = LoggerFactory.getLogger(ERXExistsQualifier.class);

	static final Pattern PATTERN = Pattern.compile("([ '\"\\(]|^)(t)([0-9])([ ,\\.'\"\\(]|$)");

	public static final String EXISTS_ALIAS = "exists";
	public static final boolean UseSQLInClause = true;
	public static final boolean UseSQLExistsClause = false;

	// an EXISTS can be rewritten as an IN and vice versa. Which is faster depends 
	// on both the database and the data itself. If one is slow for you, try the other 
	// by flipping this boolean flag.
	protected boolean usesInQualInstead = false;

    /** Register SQL generation support for the qualifier. */
    static {
        EOQualifierSQLGeneration.Support.setSupportForClass(new ExistsQualifierSQLGenerationSupport(),
                                                            ERXExistsQualifier.class);
    }

    /** Holds the key path from the base entity to the entity to which the exists clause (and qualifier) will be applied. */
    protected String baseKeyPath;

	/** Holds the subqualifier that will be applied in the exists clause. */
	protected EOQualifier subqualifier;

    /**
	 * Public single argument constructor. Use this constructor for sub-qualification on the same table.
	 * @param subqualifier sub-qualifier
	 */
	@SuppressWarnings("hiding")
	public ERXExistsQualifier(EOQualifier subqualifier) {
		this(subqualifier, null);
	}

    /**
	 * Public two argument constructor. Use this constructor for for building queries based on a key path to a separate
     * entity from the current entity.
	 * @param subqualifier sub qualifier
	 * @param baseKeyPath to the entity to which the subqualifier will be applied.  Note that this should end in a
     * relationship rather than an attribute, e.g., the key path from an Employee might be <code>department.division</code>.
	 */
	@SuppressWarnings("hiding")
	public ERXExistsQualifier(EOQualifier subqualifier, String baseKeyPath) {
		super();
		/*
		 * HACK ALERT!! ERXExistsQualifier is broken when passed a keypath. It
		 * would compare the PK of the baseTable to the PK of the related table.
		 * I was not able to figure out how to modify the existing logic with
		 * any amount of confidence that it wouldn't somehow break in some other
		 * way. This recursion "fixes" the problem by creating Multiple nested
		 * "in" clauses in the SQL code, which is not the most elegant, readable
		 * or likely efficient SQL.
		 */
		if (baseKeyPath != null && baseKeyPath.contains(EOKeyValueCodingAdditions.KeyPathSeparator)) {
			String tailPath = ERXStringUtilities.keyPathWithoutFirstProperty(baseKeyPath);
			subqualifier = new ERXExistsQualifier(subqualifier, tailPath, UseSQLInClause); // must use "in" clause otherwise sub-select table aliases (exists0) collide
			this.subqualifier = subqualifier; // use the new "nested" ERXExistsQualifier
			this.baseKeyPath = ERXStringUtilities.firstPropertyKeyInKeyPath(baseKeyPath);
		}
		else {
			this.subqualifier = subqualifier;
			this.baseKeyPath = baseKeyPath;
		}
	}

    /**
	 * Public three argument constructor. Use this constructor when you want to try converting the EXISTS into an IN clause
	 * @param subqualifier sub qualifier
	 * @param baseKeyPath to the entity to which the subqualifier will be applied.  Note that this should end in a
     * relationship rather than an attribute, e.g., the key path from an Employee might be <code>department.division</code>.
     * @param usesInQualInstead when true will convert the EXISTS clause into an IN clause - to be used if it makes the query plan faster.
	 */
    @SuppressWarnings("hiding")
	public ERXExistsQualifier(EOQualifier subqualifier, String baseKeyPath, boolean usesInQualInstead) {
		this(subqualifier, baseKeyPath);
		setUsesInQualInstead(usesInQualInstead);
	}
    
    /**
     * Gets the subqualifier that will be applied in the exists clause.
     * @return the subqualifier
     */
    public EOQualifier subqualifier() {
        return subqualifier;
    }

    /**
     * Gets the key path from the base base entity to the entity to which the exists clause (and qualifier) will be applied.
     * @return the key path
     */
    public String baseKeyPath() {
        return baseKeyPath;
    }

	/**
	 * Only used with qualifier keys which are not supported in this qualifier at this time. Does nothing.
	 * @param aSet of qualifier keys
	 */
	// FIXME: Should do something here ...
	@Override
	public void addQualifierKeysToSet(NSMutableSet aSet) {
        
	}

	/**
	 * Creates another qualifier after replacing the values of the bindings.
	 * Since this qualifier does not support qualifier binding keys a clone of the qualifier is returned.
	 * @param someBindings some bindings
	 * @param requiresAll tells if the qualifier requires all bindings
	 * @return clone of the current qualifier.
	 */
	@Override
	public EOQualifier qualifierWithBindings(NSDictionary someBindings, boolean requiresAll) {
		return (EOQualifier)clone();
	}

	/**
	 * This qualifier does not perform validation. This is a no-op method.
	 * @param aClassDescription to validation the qualifier keys against.
	 */
	// FIXME: Should do something here ...
	@Override
	public void validateKeysWithRootClassDescription(EOClassDescription aClassDescription) {
        
	}

    /**
     * Implements the SQL generation for the exists qualifier.
     */
    public static class ExistsQualifierSQLGenerationSupport extends EOQualifierSQLGeneration.Support {

        /**
         * Public constructor
         */
        public ExistsQualifierSQLGenerationSupport() {
            super();
        }

        /**
         * Generates the EXISTS SQL string for the given SQL expression.
         * The bulk of the logic for generating the sub-query is in this method.
         * @param qualifier for which to generate the SQL
         * @param expression to use during SQL generation
         * @return SQL string for the current sub-query
         */
        @Override
        public String sqlStringForSQLExpression(EOQualifier qualifier, EOSQLExpression expression) {
            if (null == qualifier || null == expression) {
                return null;
            }

            ERXExistsQualifier existsQualifier = (ERXExistsQualifier)qualifier;
            EOQualifier subqualifier = existsQualifier.subqualifier();
            String baseKeyPath = existsQualifier.baseKeyPath();

            EOEntity baseEntity = expression.entity();
            EORelationship relationship = null;

            // Walk the key path to the last entity.
            if (baseKeyPath != null) {
                for (String path : NSArray.componentsSeparatedByString(baseKeyPath, ".")) {
                    if (null == relationship) {
                        relationship = baseEntity.anyRelationshipNamed(path);
                    } else {
                        relationship = relationship.destinationEntity().anyRelationshipNamed(path);
                    }
                }
            }
            
            // If this is a flattened relationship, we need to insert the middle hidden entity and adjust key paths
            if (relationship != null && relationship.definition() != null) {
            	if (relationship.componentRelationships().count() != 2) {
            		throw new IllegalArgumentException("ERXExistsQualifier only support many to many flattened relationship.");
            	}
            	EORelationship innerToManyRelationship = (EORelationship) relationship.componentRelationships().objectAtIndex(0);
            	EORelationship innerToOneRelationship = (EORelationship) relationship.componentRelationships().objectAtIndex(1);
            	
            	if (baseKeyPath != null) {
            		baseKeyPath = baseKeyPath.replaceAll(relationship.name()+"$", innerToManyRelationship.relationshipPath());
            	}
            	relationship = innerToManyRelationship;
            	
            	ERXKey<Object> prefixKey = new ERXKey<>(innerToOneRelationship.relationshipPath());
            	subqualifier = prefixKey.prefix(subqualifier);
            }

            EOEntity srcEntity = relationship != null ? relationship.entity() : baseEntity;
            EOEntity destEntity = relationship != null ? relationship.destinationEntity() : baseEntity;

            // We need to do a bunch of hand-waiving to get the right table aliases for the table used in the exists
            // subquery and for the join clause back to the source table.
            String sourceTableAlias = "t0"; // The alias for the the source table of the baseKeyPath from the main query.
            String destTableAlias; // The alias for the table used in the subquery.
            if (!srcEntity.equals(baseEntity)) { // The exists clause is applied to the different table.
                sqlStringForAttributeNamedInExpression(baseKeyPath, expression);
                destTableAlias = (String)expression.aliasesByRelationshipPath().valueForKey(baseKeyPath);
                if (null == destTableAlias) {
                    destTableAlias = EXISTS_ALIAS + (expression.aliasesByRelationshipPath().count()); // The first entry = "t0".
                    expression.aliasesByRelationshipPath().takeValueForKey(destTableAlias, baseKeyPath);
                }
            } else { // The exists clause is applied to the base table.
                destTableAlias = EXISTS_ALIAS + expression.aliasesByRelationshipPath().count(); // Probably "t1"
            }

            String srcEntityForeignKey = null;
            NSArray<EOAttribute> sourceAttributes = relationship != null ? relationship.sourceAttributes() : null;
            if (sourceAttributes != null && sourceAttributes.count() > 0) {
                EOAttribute fk = sourceAttributes.lastObject();
                srcEntityForeignKey = expression.sqlStringForAttribute(fk);
            } else {
            	// (AR) could not find relationship from source object into "exists" clause, use primary key then instead
                EOAttribute pk = srcEntity.primaryKeyAttributes().lastObject();
                srcEntityForeignKey = expression.sqlStringForAttribute(pk);
            }
            
            String destEntityForeignKey;
            NSArray<EOAttribute> destinationAttributes;
            if (relationship != null) {
                EOJoin parentChildJoin = ERXArrayUtilities.firstObject(relationship.joins());
                destEntityForeignKey = "." + expression.sqlStringForSchemaObjectName(parentChildJoin.destinationAttribute().columnName());
                destinationAttributes = relationship.destinationAttributes();
            } else {
                EOAttribute pk = srcEntity.primaryKeyAttributes().lastObject();
                destEntityForeignKey = "." + expression.sqlStringForSchemaObjectName(pk.columnName());
                destinationAttributes = destEntity.primaryKeyAttributes();
            }
            
            EOQualifier qual = EOQualifierSQLGeneration.Support._schemaBasedQualifierWithRootEntity(subqualifier, destEntity);
            EOFetchSpecification fetchSpecification = new EOFetchSpecification(destEntity.name(), qual, null, false, true, null);

            EODatabaseContext context = EODatabaseContext.registeredDatabaseContextForModel(destEntity.model(), EOObjectStoreCoordinator.defaultCoordinator());
            EOSQLExpressionFactory factory = context.database().adaptor().expressionFactory();

            EOSQLExpression subExpression = factory.expressionForEntity(destEntity);
            subExpression.setUseAliases(true);
            subExpression.prepareSelectExpressionWithAttributes(destinationAttributes, false, fetchSpecification);

            for (Enumeration bindEnumeration = subExpression.bindVariableDictionaries().objectEnumerator(); bindEnumeration.hasMoreElements();) {
                expression.addBindVariableDictionary((NSDictionary)bindEnumeration.nextElement());
            }

            String subExprStr = subExpression.statement();

    		Matcher matcher = PATTERN.matcher(subExprStr);
    		if (matcher.find()) {
    			subExprStr = matcher.replaceAll("$1" + EXISTS_ALIAS + "$3$4");
    		}
    		
            StringBuffer sb = new StringBuffer();
            if (existsQualifier.usesInQualInstead()) {
            	// (AR) Write the IN clause
                sb.append(srcEntityForeignKey);
                sb.append(" IN ( ");
            } else {
                sb.append(" EXISTS ( ");
            }
            sb.append(subExprStr);
            if ( ! existsQualifier.usesInQualInstead()) {
            	String examineBuffer = sb.toString();
            	examineBuffer = examineBuffer.substring(0, examineBuffer.length() - 1);
            	if (examineBuffer.endsWith(EXISTS_ALIAS)) {
            		// (AR) If we end with a table alias we must add a "where" clause
                    sb.append(" WHERE ");
            	} else {
            		// (AR) there was already a where clause so we must add a "and"
                    sb.append(" AND ");
            	}
            	
                sb.append(EXISTS_ALIAS + "0" + destEntityForeignKey);
                sb.append(" = ");
                sb.append(StringUtils.replaceOnce(srcEntityForeignKey, "t0.", sourceTableAlias + "."));
            }
            sb.append(" ) ");
            return sb.toString();
        }

        /**
         * Gets the sql string for the named attribute using the provided expression.  The difference between this and the
         * standard {@link EOSQLExpression#sqlStringForAttributeNamed} is this one can handle an "attribute" name that ends
         * in a EORelationship rather than an actual EOAttribute.  This is necessary to support the 
         * {@link ERXExistsQualifier#baseKeyPath} syntax (being the relationship path to the entity to which the
         * subqualifier will be applied) chosen for this qualifier.
         * @param name of the attribute to get, e.g., department.division
         * @param expression to use when generating the SQL
         * @return the SQL string for the attribute
         */
        private String sqlStringForAttributeNamedInExpression(String name, EOSQLExpression expression) {
            NSArray<String> pieces = NSArray.componentsSeparatedByString(name, ".");
            EOEntity entity = expression.entity();
            EORelationship rel;
            EOAttribute att;
            NSMutableArray<EOProperty> path = new NSMutableArray<>();
            int numPieces = pieces.count();

            if (numPieces == 1 && null == entity.anyRelationshipNamed(name)) {
                att = entity.anyAttributeNamed(name);
                if (null == att) { return null; }
                return expression.sqlStringForAttribute(att);
            }
            
            for (int i = 0; i < numPieces - 1; i++) {
                rel = entity.anyRelationshipNamed(pieces.objectAtIndex(i));
                if (null == rel) {
                    return null;
                }
                path.addObject(rel);
                entity = rel.destinationEntity();
            }

            String key = pieces.lastObject();
            if (entity.anyRelationshipNamed(key) != null) { // Test first for a relationship.
                rel = entity.anyRelationshipNamed(key);
                if (rel.isFlattened()) {
                    String relPath = rel.relationshipPath();
                    for (String relPart : NSArray.componentsSeparatedByString(relPath, ".")) {
                        rel = entity.anyRelationshipNamed(relPart);
                        path.addObject(rel);
                        entity = rel.destinationEntity();
                    }
                } else {
                    path.addObject(rel);
                }
                att = rel.destinationAttributes().lastObject();
            } else { // The test for an attribute.
                att = entity.anyAttributeNamed(key);
            }

            if (null == att) {
                return null;
            }
            path.addObject(att);
            
            return expression.sqlStringForAttributePath(path);
        }

        /**
         * Implementation of the EOQualifierSQLGeneration interface. Just clones the qualifier.
         * @param entity an entity
         * @return clone of the current qualifier
         */
        // ENHANCEME: This should support restrictive qualifiers on the root entity
        @Override
        public EOQualifier schemaBasedQualifierWithRootEntity(EOQualifier qualifier, EOEntity entity) {
            return (EOQualifier)qualifier.clone();
        }

        /**
         * Implementation of the EOQualifierSQLGeneration interface. Just clones the qualifier.
         * @param qualifier to migrate
         * @param entity to which the qualifier should be migrated
         * @param relationshipPath upon which to base the migration
         * @return clone of the current qualifier
         */
        @Override
        public EOQualifier qualifierMigratedFromEntityRelationshipPath(EOQualifier qualifier,
                                                                       EOEntity entity,
                                                                       String relationshipPath) {
            return (EOQualifier)qualifier.clone();
        }

    }

	/**
	 * Description of the qualifier.
	 * @return human readable description of the qualifier
	 */
	@Override
	public String toString() { return " <" + getClass().getName() +"> '" + subqualifier.toString() + "' : '" + baseKeyPath + "'"; }

	/**
	 * Implementation of the Clonable interface. Clones the current qualifier.
	 * @return cloned qualifier
	 */
	@Override
	public Object clone() {
		return new ERXExistsQualifier(subqualifier, baseKeyPath, usesInQualInstead());
	}

    public Class classForCoder() {
    	return getClass();
    }
    
	public static Object decodeObject(NSCoder coder) {
		EOQualifier subqualifier = (EOQualifier) coder.decodeObject();
		String baseKeyPath = (String)coder.decodeObject();
		return new ERXExistsQualifier(subqualifier, baseKeyPath);
	}

	public void encodeWithCoder(NSCoder coder) {
		coder.encodeObject(subqualifier());
		coder.encodeObject(baseKeyPath());
	}

	public void encodeWithKeyValueArchiver(EOKeyValueArchiver archiver) {
		archiver.encodeObject(subqualifier(), "subqualifier");
		archiver.encodeObject(baseKeyPath(), "baseKeyPath");
	}

	public static Object decodeWithKeyValueUnarchiver(EOKeyValueUnarchiver unarchiver) {
		return new ERXExistsQualifier(
				(EOQualifier)unarchiver.decodeObjectForKey("subqualifier"),
				(String)unarchiver.decodeObjectForKey("baseKeyPath"));
	}
	
	@Override
	public boolean evaluateWithObject(Object object) {
		boolean match = false;
		if (object != null && subqualifier != null) {
			if (baseKeyPath != null) {
				object = NSKeyValueCodingAdditions.Utility.valueForKeyPath(object, baseKeyPath);
			}
			if (object != null) {
				if (object instanceof NSArray) {
					NSArray<NSKeyValueCoding> objArray = (NSArray<NSKeyValueCoding>) object;
					objArray = ERXArrayUtilities.removeNullValues(objArray);
					if (objArray != null && objArray.count() > 0) {
						for (NSKeyValueCoding objInArray : objArray) {
							try {
								if (subqualifier.evaluateWithObject(objInArray)) {
									match = true;
									break;
								}
							} catch (UnknownKeyException unknownE) {
								// ignore unknown keys because those objects wouldn't
								// lead to a usable result.
							}
						}
					}
				} else {
					match = subqualifier.evaluateWithObject(object);
				}
			}
		}
		return match;
	}

	public boolean usesInQualInstead() {
		return usesInQualInstead;
	}

	@SuppressWarnings("hiding")
	public void setUsesInQualInstead(boolean usesInQualInstead) {
		this.usesInQualInstead = usesInQualInstead;
	}
}
