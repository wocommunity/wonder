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
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSPropertyListSerialization;

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
 * @param &lt;T&gt;
 */
public class ERXFetchSpecification<T extends EOEnterpriseObject> extends EOFetchSpecification {
	private NSMutableDictionary _userInfo;
	
	public ERXFetchSpecification(String entityName, EOQualifier qualifier, NSArray sortOrderings, boolean usesDistinct, boolean isDeep, NSDictionary hints) {
		super(entityName, qualifier, sortOrderings, usesDistinct, isDeep, hints);
	}

	public ERXFetchSpecification(String entityName, EOQualifier qualifier, NSArray sortOrderings) {
		super(entityName, qualifier, sortOrderings);
	}

	public ERXFetchSpecification(EOFetchSpecification spec) {
		super(spec.entityName(), spec.qualifier(), spec.sortOrderings(), spec.usesDistinct(), spec.isDeep(), spec.hints());
		setFetchesRawRows(spec.fetchesRawRows());
		setFetchLimit(spec.fetchLimit());
		setLocksObjects(spec.locksObjects());
		setRawRowKeyPaths(spec.rawRowKeyPaths());
		setPromptsAfterFetchLimit(spec.promptsAfterFetchLimit());
	}

	public ERXFetchSpecification(ERXFetchSpecification<T> spec) {
		this((EOFetchSpecification)spec);
		_userInfo = spec.userInfo().count() > 0 ? null : spec.userInfo().mutableClone();
	}

	/**
	 * Sets a arbitrary value.
	 * @return
	 */
	public void setObjectForKey(Object value, String key) {
		_userInfo = _userInfo == null ? new NSMutableDictionary() : _userInfo;
		_userInfo.takeValueForKey(value, key);
	}

	/**
	 * Gets an arbitrary value.
	 * @param key
	 */
	public Object objectForKey(String key) {
		return _userInfo!= null ? _userInfo.valueForKey(key) : null;
	}
	
	/**
	 * Gets the user info.
	 * @return
	 */
	public NSDictionary userInfo() {
		return _userInfo == null ? NSDictionary.EmptyDictionary : _userInfo.immutableClone(); 
	}
	
	/**
	 * Type-safe method to fetch objects for this fetch spec.
	 * @param ec
	 * @return
	 */
	public NSArray<T> fetchObjects(EOEditingContext ec) {
		return ec.objectsWithFetchSpecification(this);
	}
	
	/**
	 * Type-safe method to fetch raw rows.
	 * @param ec
	 * @return
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
	 * Collects all relevant attributes and the bindings and returns a key suitable for caching.
	 * @return
	 */
	public String identifier() {
		return identifierForFetchSpec(this);
	}
	
	protected String additionalIdentifierInfo() {
		return "";
	}
	
	@Override
	public Object clone() {
		return super.clone();
	}
	
	/**
	 * Converts a normal fetch spec to an ERX one.
	 * @param <T>
	 * @param fs
	 * @param clazz
	 * @return
	 */
	public static <T extends EOEnterpriseObject> ERXFetchSpecification<T> fetchSpec(EOFetchSpecification fs, Class<T> clazz) {
		if (fs instanceof ERXFetchSpecification) {
			return (ERXFetchSpecification) fs;
		}
		return new ERXFetchSpecification<T>(fs);
	}
	
	/**
	 * Converts a normal fetch spec to an ERX one.
	 * @param <T>
	 * @param fs
	 * @param clazz
	 * @return
	 */
	public static <T extends EOEnterpriseObject> ERXFetchSpecification<T> fetchSpec(EOFetchSpecification fs) {
		if (fs instanceof ERXFetchSpecification) {
			return (ERXFetchSpecification) fs;
		}
		return new ERXFetchSpecification<T>(fs);
	}
	
	/**
	 * Helper to create a string from a qualifier.
	 * @param q
	 * @return
	 */
	protected static String identifierForQualifier(EOQualifier q) {
		final StringBuilder sb = new StringBuilder();
		if(q != null) {
			ERXQualifierTraversal traversal = new ERXQualifierTraversal() {

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
			traversal.traverse(q);
		}
		return sb.toString();
	}
	
	/**
	 * Builds an identifier for the given fetch spec which is suitable for caching.
	 * @param fs
	 * @return
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
