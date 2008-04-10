package er.extensions;

import java.util.Iterator;

import com.webobjects.eoaccess.EOAttribute;
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

import er.extensions.qualifiers.ERXQualifierTraversal;

/**
 * Extended fetch specification. 
 * <ul>
 * <li>has an identifier for caching</li>
 * <li>type-safe, can fetch objects of a certain type</li>
 * <li>has a user info</li>
 * <li>has grouping support (not done yet)</li>
 * </ul>
 * @author ak
 *
 * @param &lt;T&gt;
 */
public class ERXFetchSpecification<T extends EOEnterpriseObject> extends EOFetchSpecification {

	/**
	 * List of supported aggregate operators.
	 */
	public static interface Operators {
		public String SUM = "sum";
		public String AVG = "avg";
		public String MIN = "min";
		public String MAX = "max";
	}
	
	private NSArray<String> _groupingKeyPaths = NSArray.EmptyArray;
	private EOQualifier _havingQualifier;
	private NSDictionary _userInfo = NSDictionary.EmptyDictionary;
	
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
		this(spec.entityName(), spec.qualifier(), spec.sortOrderings(), spec.usesDistinct(), spec.isDeep(), spec.hints());
		setFetchesRawRows(spec.fetchesRawRows());
		setFetchLimit(spec.fetchLimit());
		setLocksObjects(spec.locksObjects());
		setRawRowKeyPaths(spec.rawRowKeyPaths());
		setPromptsAfterFetchLimit(spec.promptsAfterFetchLimit());
		setGroupingKeyPaths(spec.groupingKeyPaths());
		setUserInfo(spec.userInfo());
	}

	public NSArray<String> groupingKeyPaths() {
		return _groupingKeyPaths;
	}

	public void setGroupingKeyPaths(NSArray<String> attributes) {
		_groupingKeyPaths = attributes;
	}

	public EOQualifier havingQualifier() {
		return _havingQualifier;
	}

	public void havingQualifier(EOQualifier qualifier) {
		_havingQualifier = qualifier;
	}

	/**
	 * User info to stuff arbitary stuff into.
	 * @return
	 */
	public NSDictionary userInfo() {
		return _userInfo == null ? NSDictionary.EmptyDictionary : _userInfo;
	}

	/**
	 * Set the user info dictionary.
	 * @param info
	 */
	public void setUserInfo(NSDictionary info) {
		_userInfo = info != null ? info.immutableClone() : null;
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
		setFetchesRawRows(true);
		try {
			return ec.objectsWithFetchSpecification(this);
		} finally {
			setFetchesRawRows(old);
		}
	}

	/**
	 * Collects all relevant attributes and the bindings and returns a key suitable for caching.
	 * @return
	 */
	public String identifier() {
		final StringBuilder sb = new StringBuilder();
		
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
							s +=object;
						}
					}
					value = s;
				}
				sb.append(q.key()).append(q.selector().name()).append(value);
				return super.traverseKeyValueQualifier(q);
			}
		};
		for (Iterator iterator = sortOrderings().iterator(); iterator.hasNext();) {
			EOSortOrdering so = (EOSortOrdering) iterator.next();
			sb.append(so.key()).append(so.selector().name());
		}
		traversal.traverse(qualifier());
		traversal.traverse(havingQualifier());
		sb.append(fetchesRawRows()).append(fetchLimit()).append(locksObjects()).append(isDeep());
		sb.append(entityName());
		sb.append(hints());
		return sb.toString();
	}
	
	public static <T extends EOEnterpriseObject> ERXFetchSpecification<T> fetchSpec(EOFetchSpecification fs, Class<T> clazz) {
		return new ERXFetchSpecification<T>(fs);
	}
	
	public static <T extends EOEnterpriseObject> ERXFetchSpecification<T> fetchSpec(EOFetchSpecification fs) {
		return new ERXFetchSpecification<T>(fs);
	}
}
