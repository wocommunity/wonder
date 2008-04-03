package er.extensions;

import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;

/**
 * Extended fetch specification. This is just an idea so far...
 * <ul>
 * <li>has an identifier for caching</li>
 * <li>can fetch objects of a certain type</li>
 * <li>has grouping support</li>
 * <li>has a user info</li>
 * </ul>
 * @author ak
 *
 * @param <T>
 */
public class ERXFetchSpecification<T extends EOEnterpriseObject> extends EOFetchSpecification {

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

	public NSDictionary userInfo() {
		return _userInfo == null ? NSDictionary.EmptyDictionary : _userInfo;
	}

	public void setUserInfo(NSDictionary info) {
		_userInfo = info != null ? info.immutableClone() : null;
	}
	
	public NSArray<T> fetchObjects(EOEditingContext ec) {
		return ec.objectsWithFetchSpecification(this);
	}

	public String identifier() {
		// AK: this is just an idea so far. we need to get the qualifier string and loop through the parameters. 
		return "" + hashCode();
	}
}
