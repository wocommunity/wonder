/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions.eof.qualifiers;

import java.util.Enumeration;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EODatabaseContext;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOProperty;
import com.webobjects.eoaccess.EOQualifierSQLGeneration;
import com.webobjects.eoaccess.EORelationship;
import com.webobjects.eoaccess.EOSQLExpression;
import com.webobjects.eoaccess.EOSQLExpressionFactory;
import com.webobjects.eocontrol.EOClassDescription;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.eocontrol.EOKeyValueArchiver;
import com.webobjects.eocontrol.EOKeyValueArchiving;
import com.webobjects.eocontrol.EOKeyValueUnarchiver;
import com.webobjects.eocontrol.EOObjectStoreCoordinator;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSCoder;
import com.webobjects.foundation.NSCoding;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableSet;

import er.extensions.foundation.ERXStringUtilities;

/**
 * A qualifier that qualifies using an EXISTS clause.
 *
 * It will produce an SQL clause like the following:
 *
 * <code>select t0.ID, t0.ATT_1, ... t0.ATT_N from FIRST_TABLE t0 where EXISTS (select t1.ID from ANOTHER_TABLE where t1.ATT_1 = ? and t1.FIRST_TABLE_ID = t0.ID)</code>
 *
 * @author Travis Cripps
 */
public class ERXExistsQualifier extends EOQualifier implements Cloneable, NSCoding, EOKeyValueArchiving {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	public static final Logger log = Logger.getLogger(ERXExistsQualifier.class);

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
    public ERXExistsQualifier(EOQualifier subqualifier, String baseKeyPath) {
		super();
		this.subqualifier = subqualifier;
		this.baseKeyPath = baseKeyPath;
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

            EOEntity srcEntity = relationship != null ? relationship.entity() : baseEntity;
            EOEntity destEntity = relationship != null ? relationship.destinationEntity() : baseEntity;

            // We need to do a bunch of hand-waiving to get the right table aliases for the table used in the exists
            // subquery and for the join clause back to the source table.
            String sourceTableAlias = "t0"; // The alias for the the source table of the baseKeyPath from the main query.
            String destTableAlias; // The alias for the table used in the subquery.
            if (!srcEntity.equals(baseEntity)) { // The exists clause is applied to the different table.
                String sourceKeyPath = ERXStringUtilities.keyPathWithoutLastProperty(baseKeyPath);
                sqlStringForAttributeNamedInExpression(sourceKeyPath, expression);
                sqlStringForAttributeNamedInExpression(baseKeyPath, expression);
                sourceTableAlias = (String)expression.aliasesByRelationshipPath().valueForKey(sourceKeyPath);
                destTableAlias = (String)expression.aliasesByRelationshipPath().valueForKey(baseKeyPath);
                if (null == destTableAlias) {
                    destTableAlias = "t" + (expression.aliasesByRelationshipPath().count()); // The first entry = "t0".
                    expression.aliasesByRelationshipPath().takeValueForKey(destTableAlias, baseKeyPath);
                }
            } else { // The exists clause is applied to the base table.
                destTableAlias = "t" + expression.aliasesByRelationshipPath().count(); // Probably "t1"
            }

            EOAttribute sourceKeyAttribute = srcEntity.primaryKeyAttributes().lastObject();
            String sourceKey = expression.sqlStringForAttribute(sourceKeyAttribute);

            EOAttribute destKeyAttribute = relationship.destinationAttributes().lastObject();
            String destKey = expression.sqlStringForAttribute(destKeyAttribute);
            
            EOQualifier qual = EOQualifierSQLGeneration.Support._schemaBasedQualifierWithRootEntity(subqualifier, destEntity);
            EOFetchSpecification fetchSpecification = new EOFetchSpecification(destEntity.name(), qual, null, false, true, null);

            EODatabaseContext context = EODatabaseContext.registeredDatabaseContextForModel(destEntity.model(), EOObjectStoreCoordinator.defaultCoordinator());
            EOSQLExpressionFactory factory = context.database().adaptor().expressionFactory();

            EOSQLExpression subExpression = factory.expressionForEntity(destEntity);
            subExpression.aliasesByRelationshipPath().setObjectForKey(destTableAlias, "");
            subExpression.setUseAliases(true);
            subExpression.prepareSelectExpressionWithAttributes(destEntity.primaryKeyAttributes(), false, fetchSpecification);

            for (Enumeration bindEnumeration = subExpression.bindVariableDictionaries().objectEnumerator(); bindEnumeration.hasMoreElements();) {
                expression.addBindVariableDictionary((NSDictionary)bindEnumeration.nextElement());
            }

            StringBuilder sb = new StringBuilder();
            sb.append(" EXISTS ( ");
            sb.append(StringUtils.replace(subExpression.statement(), "t0.", destTableAlias + "."));
            sb.append(" AND ");
            sb.append(StringUtils.replace(destKey, "t0.", destTableAlias + "."));
            sb.append(" = ");
            sb.append(StringUtils.replace(sourceKey, "t0.", sourceTableAlias + "."));
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
            NSMutableArray<EOProperty> path = new NSMutableArray<EOProperty>();
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
		return new ERXExistsQualifier(subqualifier, baseKeyPath);
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
	
}
