/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions.eof.qualifiers;

import java.util.Enumeration;

import org.apache.log4j.Logger;

import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EODatabaseContext;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOQualifierSQLGeneration;
import com.webobjects.eoaccess.EORelationship;
import com.webobjects.eoaccess.EOSQLExpression;
import com.webobjects.eoaccess.EOSQLExpressionFactory;
import com.webobjects.eocontrol.EOClassDescription;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.eocontrol.EOObjectStoreCoordinator;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.eocontrol.EOQualifierEvaluation;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSKeyValueCodingAdditions;
import com.webobjects.foundation.NSMutableSet;

import er.extensions.eof.ERXEOAccessUtilities;

/**
 * Generates a subquery for the qualifier given in argument:
 * 
 * <pre><code>
 * EOQualifier q = EOQualifier.qualifierWithQualifierFormat(&quot;firstName = 'Max'&quot;, null);
 * ERXQualifierInSubquery qq = new ERXQualifierInSubquery(q, &quot;User&quot;, &quot;group&quot;);
 * EOFetchSpecification fs = new EOFetchSpecification(&quot;Group&quot;, qq, null);
 * </code></pre>
 * 
 * Would generate: "SELECT t0.GROUP_ID, t0.NAME FROM USER t0 WHERE t0.GROUP_ID
 * IN ( SELECT t0.GROUP_ID FROM GROUP t0 WHERE t0.NAME = ? ) "
 * 
 * This class can be used to work around the EOF bug where OR queries involving
 * many-to-manies are incorrectly generated
 * 
 * 
 * It will also generate
 * 
 * ... t0.FOREIGN_KEY_ID in (select t1.ID from X where [your qualifier here])
 * 
 * with the 3 arg constructor
 */

public class ERXQualifierInSubquery extends EOQualifier implements EOQualifierSQLGeneration, Cloneable, EOQualifierEvaluation {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	/** logging support */
	public static final Logger log = Logger.getLogger(ERXQualifierInSubquery.class);

	/** holds the subqualifier */
	protected EOQualifier qualifier;

	/** holds the entity name */
	protected String entityName;

	/** holds the relationship name */
	protected String relationshipName;

	/** holds the attribute name */
	protected String attributeName;

	/** holds the attribute name */
	protected String destinationAttName;

	/**
	 * Public single argument constructor. Use this constructor for
	 * sub-qualification on the same table.
	 * 
	 * @param qualifier
	 *            sub qualifier
	 */
	public ERXQualifierInSubquery(EOQualifier qualifier) {
		this(qualifier, null, null);
	}

	/**
	 * @param qualifier
	 *            sub qualifier
	 * @param entityName
	 *            of the sub qualification
	 * @param relationshipName
	 *            relationship name
	 */
	public ERXQualifierInSubquery(EOQualifier qualifier, String entityName, String relationshipName) {
		this.qualifier = qualifier;
		this.entityName = entityName;
		if(relationshipName != null) {
			this.relationshipName = relationshipName;
			EORelationship rel = ERXEOAccessUtilities.entityNamed(null, entityName).relationshipNamed(relationshipName);
			attributeName = rel.sourceAttributes().lastObject().name();
			destinationAttName = rel.destinationAttributes().lastObject().name();
		}
	}

	/**
	 * @param qualifier
	 *            sub qualifier
	 * @param entityName
	 *            of the sub qualification
	 * @param attributeName
	 *            foreign key attribute name
	 * @param destinationAttName
	 *            destination key name
	 */

	public ERXQualifierInSubquery(EOQualifier qualifier, String entityName, String attributeName, String destinationAttName) {
		this.qualifier = qualifier;
		this.entityName = entityName;
		this.attributeName = attributeName;
		this.destinationAttName = destinationAttName;
	}

	/**
	 * Only used with qualifier keys which are not supported in this qualifier
	 * at this time. Does nothing.
	 * 
	 * @param aSet
	 *            of qualifier keys
	 */
	// FIXME: Should do something here ...
	@Override
	public void addQualifierKeysToSet(NSMutableSet aSet) {
	}

	/**
	 * Creates another qualifier after replacing the values of the bindings.
	 * Since this qualifier does not support qualifier binding keys a clone of
	 * the qualifier is returned.
	 * 
	 * @param someBindings
	 *            some bindings
	 * @param requiresAll
	 *            tells if the qualifier requires all bindings
	 * @return clone of the current qualifier.
	 */
	@Override
	public EOQualifier qualifierWithBindings(NSDictionary someBindings, boolean requiresAll) {
		return (EOQualifier) clone();
	}

	/**
	 * This qualifier does not perform validation. This is a no-op method.
	 * 
	 * @param aClassDescription
	 *            to validation the qualifier keys against.
	 */
	// FIXME: Should do something here ...
	@Override
	public void validateKeysWithRootClassDescription(EOClassDescription aClassDescription) {
	}

	/**
	 * Generates the sql string for the given sql expression. Bulk of the logic
	 * for generating the sub-query is in this method.
	 * 
	 * @param e
	 *            a given sql expression
	 * @return sql string for the current sub-query.
	 */
	public String sqlStringForSQLExpression(EOSQLExpression e) {
		StringBuilder sb = new StringBuilder();
		if (attributeName != null)
			sb.append(e.sqlStringForAttributeNamed(attributeName));
		else {
			EOAttribute pk = e.entity().primaryKeyAttributes().lastObject();
			sb.append(e.sqlStringForAttribute(pk));
		}
		sb.append(" IN ( ");
		EOEntity entity = entityName == null ? e.entity() : e.entity().model().modelGroup().entityNamed(entityName);

		EOFetchSpecification fs = new EOFetchSpecification(entity.name(), qualifier, null, false, true, null);

		if (qualifier != null) {
			qualifier = EOQualifierSQLGeneration.Support._schemaBasedQualifierWithRootEntity(qualifier, entity);
		}
		if (qualifier != fs.qualifier()) {
			fs.setQualifier(qualifier);
		}

		// ASSUME: This makes a few assumptions, if anyone can figure out a fool
		// proof way that would be nice to get the model
		// Note you can't use:
		// EOAdaptor.adaptorWithModel(e.entity().model()).expressionFactory();
		// as this creates a
		//
		EODatabaseContext context = EODatabaseContext.registeredDatabaseContextForModel(entity.model(), EOObjectStoreCoordinator.defaultCoordinator());
		EOSQLExpressionFactory factory = context.database().adaptor().expressionFactory();

		NSArray subAttributes = destinationAttName != null ? new NSArray(entity.attributeNamed(destinationAttName)) : entity.primaryKeyAttributes();

		EOSQLExpression subExpression = factory.expressionForEntity(entity);

		// Arroz: Having this table identifier replacement causes serious
		// problems if you have more than a table being processed in the subquery. Disabling
		// it will apparently not cause problems, because t0 inside the subquery is not
		// the same t0 outside it.
		// subExpression.aliasesByRelationshipPath().setObjectForKey("t1", "");

		subExpression.setUseAliases(true);
		subExpression.prepareSelectExpressionWithAttributes(subAttributes, false, fs);
		// EOSQLExpression
		// expression=factory.selectStatementForAttributes(entity.primaryKeyAttributes(),
		// false, fs,  entity);

		for (Enumeration bindEnumeration = subExpression.bindVariableDictionaries().objectEnumerator(); bindEnumeration.hasMoreElements();) {
			e.addBindVariableDictionary((NSDictionary) bindEnumeration.nextElement());
		}

		// sb.append(ERXStringUtilities.replaceStringByStringInString("t0.",
		// "t1.", subExpression.statement()));
		sb.append(subExpression.statement());
		sb.append(" ) ");
		return sb.toString();
	}

	/**
	 * Implementation of the EOQualifierSQLGeneration interface. Just clones the
	 * qualifier.
	 * 
	 * @param anEntity
	 *            an entity.
	 * @return clone of the current qualifier.
	 */
	public EOQualifier schemaBasedQualifierWithRootEntity(EOEntity anEntity) {
		return (EOQualifier) clone();
	}

	/**
	 * Implementation of the EOQualifierSQLGeneration interface. Just clones the
	 * qualifier.
	 * 
	 * @param anEntity
	 *            an entity
	 * @param aPath
	 *            relationship path
	 * @return clone of the current qualifier.
	 */
	public EOQualifier qualifierMigratedFromEntityRelationshipPath(EOEntity anEntity, String aPath) {
		return (EOQualifier) clone();
	}

	/**
	 * Description of the qualifier
	 * 
	 * @return human readable description of the qualifier.
	 */
	@Override
	public String toString() {
		return " <" + getClass().getName() + "> '" + qualifier.toString() + "'";
	}

	/**
	 * Implementation of the Clonable interface. Clones the current qualifier.
	 * 
	 * @return cloned qualifier.
	 */
	@Override
	public Object clone() {
		if(relationshipName != null) {
			return new ERXQualifierInSubquery(qualifier, entityName, relationshipName);
		}
		return new ERXQualifierInSubquery(qualifier, entityName, attributeName, destinationAttName);
	}
	
	@Override
	public boolean evaluateWithObject(Object object) {
		Object destinationValue = NSKeyValueCodingAdditions.Utility.valueForKeyPath(object, relationshipName);
		if(destinationValue != null) {
			if (destinationValue instanceof NSArray) {
				NSArray arr = (NSArray) destinationValue;
				return EOQualifier.filteredArrayWithQualifier(arr, qualifier).count() > 0;
			}
			return qualifier.evaluateWithObject(destinationValue);
		}
		return false;
	}
	
}
