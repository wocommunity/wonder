package er.extensions.eof;

import java.util.Iterator;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.eocontrol.EOKeyComparisonQualifier;
import com.webobjects.eocontrol.EOKeyValueQualifier;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.eocontrol.EOQualifierEvaluation;
import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSPropertyListSerialization;
import com.webobjects.foundation.NSRange;

import er.extensions.crypting.ERXCrypto;
import er.extensions.qualifiers.ERXQualifierTraversal;

/**
 * Extended fetch specification.
 * <ul>
 * <li>has an identifier for caching</li>
 * <li>type-safe, can fetch objects of a certain type</li>
 * <li>has a user info</li>
 * </ul>
 * @author ak
 *
 * @param <T> the type of objects this fetch spec will return
 */
public class ERXFetchSpecification<T extends EOEnterpriseObject> extends EOFetchSpecification {

	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	private NSMutableDictionary _userInfo;
	private boolean _includeEditingContextChanges;
	private NSRange _fetchRange;

	public static EOFetchSpecification fetchSpec(String entityName, EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings, boolean usesDistinct, boolean isDeep, NSDictionary hints) {
		return new ERXFetchSpecification(entityName, qualifier, sortOrderings, usesDistinct, isDeep, hints);
	}

	public ERXFetchSpecification(String entityName, EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings, boolean usesDistinct, boolean isDeep, NSDictionary hints) {
		super(entityName, qualifier, sortOrderings, usesDistinct, isDeep, hints);
	}

	public ERXFetchSpecification(String entityName, EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings) {
		super(entityName, qualifier, sortOrderings);
	}

	public ERXFetchSpecification(EOFetchSpecification spec) {
		super(spec.entityName(), spec.qualifier(), spec.sortOrderings(), spec.usesDistinct(), spec.isDeep(), spec.hints());
		setFetchesRawRows(spec.fetchesRawRows());
		setFetchLimit(spec.fetchLimit());
		setLocksObjects(spec.locksObjects());
		setRawRowKeyPaths(spec.rawRowKeyPaths());
		setPromptsAfterFetchLimit(spec.promptsAfterFetchLimit());
		setRefreshesRefetchedObjects(spec.refreshesRefetchedObjects());
		setPrefetchingRelationshipKeyPaths(spec.prefetchingRelationshipKeyPaths());
	}

	public ERXFetchSpecification(ERXFetchSpecification<T> spec) {
		this((EOFetchSpecification)spec);
		_userInfo = spec.userInfo().count() > 0 ? null : spec.userInfo().mutableClone();
		_fetchRange = spec.fetchRange();
	}

	/**
	 * Constructs a new fetch specification for the given entity with isDeep = true.
	 *
	 * @param entityName the name of the entity
	 */
	public ERXFetchSpecification(String entityName) {
		super(entityName, null, null, false, true, null);
	}

	/**
	 * When true, objectsWithFetchSpecification will include newly inserted objects, newly removed objects, and newly updated
	 * objects in your fetch results (@see ERXEOControlUtilities.objectsWithQualifier).
	 *
	 * @param includeEditingContextChanges whether or not to include editing context changes
	 */
	public void setIncludeEditingContextChanges(boolean includeEditingContextChanges) {
		_includeEditingContextChanges = includeEditingContextChanges;
	}
	
	/**
	 * Returns whether or not to include editing context changes.
	 *
	 * @return whether or not to include editing context changes
	 */
	public boolean includeEditingContextChanges() {
		return _includeEditingContextChanges;
	}

	/**
	 * Sets a arbitrary value.
	 *
	 * @param value
	 * @param key
	 */
	public void setObjectForKey(Object value, String key) {
		_userInfo = _userInfo == null ? new NSMutableDictionary() : _userInfo;
		_userInfo.takeValueForKey(value, key);
	}

	/**
	 * Gets an arbitrary value.
	 *
	 * @param key
	 * @return object for given key
	 */
	public Object objectForKey(String key) {
		return _userInfo!= null ? _userInfo.valueForKey(key) : null;
	}
	
	/**
	 * Gets the user info.
	 *
	 * @return user info dictionary
	 */
	public NSDictionary userInfo() {
		return _userInfo == null ? NSDictionary.EmptyDictionary : _userInfo.immutableClone();
	}
	
	public NSRange fetchRange() {
		return _fetchRange;
	}
	
	/**
	 * Defines a batch range that should be applied to the SQL statement. Only useful if the database plugin supports it and as an alternative to fetchLimit.
	 * The SQL generation behavior when both a fetchLimit and a fetchRange are specified is undefined and dependent on the individual database plugin.
	 *
	 * @param range
	 */
	public void setFetchRange(NSRange range) {
		_fetchRange = range;
	}
	
	/**
	 * Type-safe method to fetch objects for this fetch spec.
	 *
	 * @param ec
	 * @return object array
	 */
	public NSArray<T> fetchObjects(EOEditingContext ec) {
		return ec.objectsWithFetchSpecification(this);
	}
	
	/**
	 * Type-safe method to fetch raw rows.
	 *
	 * @param ec
	 * @return array of raw row dictionaries
	 */
	public NSArray<NSDictionary<String, Object>> fetchRawRows(EOEditingContext ec) {
		boolean old = fetchesRawRows();
		if(!old) {
			setFetchesRawRows(true);
		}
		try {
			return ec.objectsWithFetchSpecification(this);
		} finally {
			if(!old) {
				setFetchesRawRows(old);
			}
		}
	}
	
	/**
	 * Sets a list of attribute keys to be fetched as raw data. Uses two params for backwards
	 * compatibility as a <code>setRawRowKeyPaths(null)</code> would be ambiguous otherwise.
	 *
	 * @see #setRawRowKeyPaths(NSArray)
	 * @param keyPath
	 * @param keyPaths list of attribute keys
	 */
	public void setRawRowKeyPaths(String keyPath, String... keyPaths) {
		super.setRawRowKeyPaths(new NSArray<String>(keyPath, keyPaths));
	}

	/**
	 * Sets the relationships to prefetch along with the main fetch.
	 * 
	 * @see #setPrefetchingRelationshipKeyPaths(NSArray)
	 * @param prefetchingRelationshipKeyPaths list of keys to prefetch
	 */
	public void setPrefetchingRelationshipKeyPaths(ERXKey<?>... prefetchingRelationshipKeyPaths) {
		NSMutableArray<String> keypaths = new NSMutableArray<String>();
		for (ERXKey<?> key : prefetchingRelationshipKeyPaths) {
			keypaths.addObject(key.key());
		}
		setPrefetchingRelationshipKeyPaths(keypaths);
	}

	/**
	 * Collects all relevant attributes and the bindings and returns a key suitable for caching.
	 *
	 * @return identifier string
	 */
	public String identifier() {
		return identifierForFetchSpec(this);
	}
	
	protected String additionalIdentifierInfo() {
		return "";
	}
	
	@Override
	public Object clone() {
		ERXFetchSpecification<T> fs = fetchSpec((EOFetchSpecification) super.clone());
		fs._fetchRange = _fetchRange;
		fs._userInfo = _userInfo == null ? null : _userInfo.mutableClone();
		fs._includeEditingContextChanges = _includeEditingContextChanges;
		return fs;
	}
	
	/**
	 * Sets the qualifier on this fetch specification and returns "this" for chaining.
	 *
	 * @param qualifier the qualifier to set
	 * @return this
	 */
	public ERXFetchSpecification<T> qualify(EOQualifier qualifier) {
		setQualifier(qualifier);
		return this;
	}
	
	/**
	 * Sets the sort orderings on this fetch specification and returns "this" for chaining.
	 *
	 * @param sortOrderings the sort orderings to set
	 * @return this
	 */
	public ERXFetchSpecification<T> sort(NSArray<EOSortOrdering> sortOrderings) {
		setSortOrderings(sortOrderings);
		return this;
	}
	
	/**
	 * Sets the sort orderings on this fetch specification and returns "this" for chaining.
	 *
	 * @param sortOrdering the sort ordering to set
	 * @return this
	 */
	public ERXFetchSpecification<T> sort(EOSortOrdering sortOrdering) {
		setSortOrderings(new NSArray<EOSortOrdering>(sortOrdering));
		return this;
	}
	
	/**
	 * Converts a normal fetch spec to an ERX one that returns instances of T.
	 *
	 * @param <T>
	 * @param fs
	 * @param clazz
	 * @return converted fetch spec
	 */
	public static <T extends EOEnterpriseObject> ERXFetchSpecification<T> fetchSpec(EOFetchSpecification fs, Class<T> clazz) {
		if (fs instanceof ERXFetchSpecification) {
			return (ERXFetchSpecification) fs;
		}
		return new ERXFetchSpecification<T>(fs);
	}
	
	/**
	 * Converts a normal fetch spec to an ERX one.
	 *
	 * @param <T>
	 * @param fs
	 * @return converted fetch spec
	 */
	public static <T extends EOEnterpriseObject> ERXFetchSpecification<T> fetchSpec(EOFetchSpecification fs) {
		if (fs instanceof ERXFetchSpecification) {
			return (ERXFetchSpecification) fs;
		}
		return new ERXFetchSpecification<T>(fs);
	}
	
	/**
	 * Helper to create a string from a qualifier.
	 *
	 * @param qualifier
	 * @return qualifier string
	 */
	protected static String identifierForQualifier(EOQualifier qualifier) {
		final StringBuilder sb = new StringBuilder();
		if(qualifier != null) {
			ERXQualifierTraversal traversal = new ERXQualifierTraversal() {

				@Override
				protected void visit(EOQualifierEvaluation q) {
					sb.append(q.getClass().getName());
				}

				@Override
				protected boolean traverseKeyComparisonQualifier(EOKeyComparisonQualifier q) {
					sb.append(q.leftKey()).append(q.selector().name()).append(q.rightKey());
					return super.traverseKeyComparisonQualifier(q);
				}

				@Override
				protected boolean traverseKeyValueQualifier(EOKeyValueQualifier q) {
					Object value = q.value();
					if (value instanceof EOEnterpriseObject) {
						EOEnterpriseObject eo = (EOEnterpriseObject) value;
						value = ERXEOControlUtilities.primaryKeyStringForObject(eo);
					} else if (value instanceof NSArray) {
						NSArray arr = (NSArray) value;
						String s = "";
						for (Object object : arr) {
							if (object instanceof EOEnterpriseObject) {
								EOEnterpriseObject eo = (EOEnterpriseObject) object;
								s += ERXEOControlUtilities.primaryKeyStringForObject(eo);
							} else {
								s += NSPropertyListSerialization.stringFromPropertyList(object);
							}
						}
						value = s;
					}
					sb.append(q.key()).append(q.selector().name()).append(value);
					return super.traverseKeyValueQualifier(q);
				}
			};
			traversal.traverse(qualifier);
		}
		return sb.toString();
	}
	
	/**
	 * Builds an identifier for the given fetch spec which is suitable for caching.
	 *
	 * @param fs
	 * @return fetch spec string
	 */
	public static String identifierForFetchSpec(EOFetchSpecification fs) {
		StringBuilder sb = new StringBuilder( identifierForQualifier(fs.qualifier()));
		for (Iterator iterator = fs.sortOrderings().iterator(); iterator.hasNext();) {
			EOSortOrdering so = (EOSortOrdering) iterator.next();
			sb.append(so.key()).append(so.selector().name());
		}
		sb.append(fs.fetchesRawRows()).append(fs.fetchLimit()).append(fs.locksObjects()).append(fs.isDeep());
		sb.append(fs.entityName());
		sb.append(fs.hints());
		if (fs instanceof ERXFetchSpecification) {
			sb.append(((ERXFetchSpecification) fs).additionalIdentifierInfo());
		}
		String result = sb.toString();
		result = ERXCrypto.base64HashedString(result);
		return result;
	}
}
