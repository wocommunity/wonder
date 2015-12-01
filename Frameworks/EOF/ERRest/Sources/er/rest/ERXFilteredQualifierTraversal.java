package er.rest;

import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EORelationship;
import com.webobjects.eocontrol.EOKeyComparisonQualifier;
import com.webobjects.eocontrol.EOKeyValueQualifier;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.eocontrol.EOQualifierEvaluation;

import er.extensions.eof.ERXKey;
import er.extensions.eof.ERXKeyFilter;
import er.extensions.qualifiers.ERXQualifierTraversal;

/**
 * ERXFilteredQualifierTraversal performs a security check on a qualifier, throwing a SecurityException if a qualifier
 * is found that attempts to qualify a key that isn't permitted by a given ERXKeyFilter. This prevents people from doing
 * things like "employee.salary &gt; 100000" when employee.salary is an excluded key in your filter.
 * 
 * @author mschrag
 */
public class ERXFilteredQualifierTraversal extends ERXQualifierTraversal {
	private EOEntity _entity;
	private ERXKeyFilter _filter;

	/**
	 * Constructs a new ERXFilteredQualifierTraversal.
	 * 
	 * @param entity
	 *            the entity to resolve keypaths on
	 * @param filter
	 *            the filter to check against
	 */
	protected ERXFilteredQualifierTraversal(EOEntity entity, ERXKeyFilter filter) {
		_entity = entity;
		_filter = filter;
	}

	/**
	 * Checks the given key and throws an exception if the filter does not match it.
	 * 
	 * @param key
	 *            the key to check
	 * @throws SecurityException
	 *             if the key does not match
	 */
	protected void checkKey(String key) throws SecurityException {
		if (!_filter.matches(new ERXKey<Object>(key), ERXFilteredQualifierTraversal.typeForKeyInEntity(key, _entity))) {
			throw new SecurityException("You do not have access to the key path '" + key + "'.");
		}
	}

	@Override
	protected boolean traverseKeyComparisonQualifier(EOKeyComparisonQualifier q) {
		checkKey(q.leftKey());
		checkKey(q.rightKey());
		return super.traverseKeyComparisonQualifier(q);
	}

	@Override
	protected boolean traverseKeyValueQualifier(EOKeyValueQualifier q) {
		checkKey(q.key());
		return super.traverseKeyValueQualifier(q);
	}

	@Override
	protected boolean traverseUnknownQualifier(EOQualifierEvaluation q) {
		throw new SecurityException("Unknown qualifier: " + q);
	}

	/**
	 * Returns the ERXKey.Type for a keypath on a particular entity. This should probably be in a more generic utility
	 * class. I can't put it on ERXKey because it would break JavaClient people when they use eogenerated classes with
	 * ERXKey properties.
	 * 
	 * @param key
	 *            the key to lookup
	 * @param entity
	 *            the entity to resolve the keypath on
	 * @return the ERXKey.Type for the given keypath, or null if there is no matching key
	 */
	public static ERXKey.Type typeForKeyInEntity(String key, EOEntity entity) {
		ERXKey.Type type;
		EOAttribute attribute = entity._attributeForPath(key);
		if (attribute != null) {
			type = ERXKey.Type.Attribute;
		}
		else {
			EORelationship relationship = entity._relationshipForPath(key);
			if (relationship != null) {
				if (relationship.isToMany()) {
					type = ERXKey.Type.ToManyRelationship;
				}
				else {
					type = ERXKey.Type.ToOneRelationship;
				}
			}
			else {
				type = null;
			}
		}
		return type;
	}

	/**
	 * Traverses the given qualifier, checking each keypath against the given filter, evaluated against the given
	 * entity.
	 * 
	 * @param qualifier
	 *            the qualifier to check
	 * @param entity
	 *            the entity to resolve keypaths against
	 * @param filter
	 *            the filter to evaluate with
	 * @throws SecurityException
	 *             if a keypath is not matched by the filter
	 */
	public static void checkQualifierForEntityWithFilter(EOQualifier qualifier, EOEntity entity, ERXKeyFilter filter) throws SecurityException {
		new ERXFilteredQualifierTraversal(entity, filter).traverse(qualifier);
	}
}
