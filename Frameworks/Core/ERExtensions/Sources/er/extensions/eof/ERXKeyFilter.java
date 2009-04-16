package er.extensions.eof;

import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSMutableSet;
import com.webobjects.foundation.NSSet;

/**
 * <p>
 * ERXKeyFilter provides a way to specify hierarchical rules for 
 * including and exluding ERXKeys. This is useful if you need
 * to perform operations on a set of EO's and optional relationships
 * and attributes within those EO's. As an example, ERXRest uses
 * ERXKeyFilter to programmatically specify which attribtues and
 * relationships will be rendered for a particular root EO.
 * </p>
 * 
 * <p>
 * ERXKeyFilter is a hierarchical mapping between ERXKeys (single key, 
 * not keypaths), whether an include or exclude
 * rule should be applied for that key, and if it's an include, the 
 * next set of filter rules to apply to the destination object.
 * </p>
 * 
 * <pre>
 * ERXKeyFilter companyFilter = new ERXKeyFilter(ERXKeyFilter.Base.Attributes);
 * ERXKeyFilter remindersFilter = companyFilter.include(Company.REMINDERS);
 * remindersFilter.include(Reminder.SUMMARY);
 * ERXKeyFilter reminderAuthorFilter = remindersFilter.include(Reminder.AUTHOR);
 * reminderAuthorFilter.includeAll();
 * reminderAuthorFilter.exclude(Author.HUGE_RELATIONSHIP);
 * </pre>
 * 
 * more method comments to come ...
 * 
 * @author mschrag
 */
public class ERXKeyFilter {
	/**
	 * ERXKeyFilter.Base defines the base rule that is applied
	 * to a filter.
	 * 
	 * @author mschrag
	 */
	public static enum Base {
		None, Attributes, AttributesAndToOneRelationships, All;
	}

	private ERXKeyFilter.Base _base;
	private NSMutableDictionary<ERXKey, ERXKeyFilter> _includes;
	private NSMutableSet<ERXKey> _excludes;

	/**
	 * Creates a new ERXKeyFilter.
	 * 
	 * @param base the base rule to apply
	 */
	public ERXKeyFilter(ERXKeyFilter.Base base) {
		_base = base;
		_includes = new NSMutableDictionary<ERXKey, ERXKeyFilter>();
		_excludes = new NSMutableSet<ERXKey>();
	}
	
	/**
	 * Shortcut to return a new ERXKeyFilter(None)
	 * @return a new ERXKeyFilter(None)
	 */
	public static ERXKeyFilter none() {
		return new ERXKeyFilter(ERXKeyFilter.Base.None);
	}
	
	/**
	 * Shortcut to return a new ERXKeyFilter(Attributes)
	 * @return a new ERXKeyFilter(Attributes)
	 */
	public static ERXKeyFilter attributes() {
		return new ERXKeyFilter(ERXKeyFilter.Base.Attributes);
	}
	
	/**
	 * Shortcut to return a new ERXKeyFilter(AttributesAndToOneRelationships)
	 * @return a new ERXKeyFilter(AttributesAndToOneRelationships)
	 */
	public static ERXKeyFilter attributesAndToOneRelationships() {
		return new ERXKeyFilter(ERXKeyFilter.Base.AttributesAndToOneRelationships);
	}
	
	/**
	 * Shortcut to return a new ERXKeyFilter(All)
	 * @return a new ERXKeyFilter(All)
	 */
	public static ERXKeyFilter all() {
		return new ERXKeyFilter(ERXKeyFilter.Base.All);
	}

	/**
	 * Returns the base rule for this filter.
	 * 
	 * @return the base rule for this filter
	 */
	public ERXKeyFilter.Base base() {
		return _base;
	}

	/**
	 * Sets the base rule to All.
	 */
	public void includeAll() {
		setBase(ERXKeyFilter.Base.All);
	}

	/**
	 * Sets the base rule to Attributes.
	 */
	public void includeAttributes() {
		setBase(ERXKeyFilter.Base.Attributes);
	}

	/**
	 * Sets the base rule to AttribtuesAndToOneRelationships.
	 */
	public void includeAttributesAndToOneRelationships() {
		setBase(ERXKeyFilter.Base.AttributesAndToOneRelationships);
	}

	/**
	 * Sets the base rule to None.
	 */
	public void includeNone() {
		setBase(ERXKeyFilter.Base.None);
	}

	/**
	 * Sets the base rule.
	 * 
	 * @param base the base rule
	 */
	public void setBase(ERXKeyFilter.Base base) {
		_base = base;
	}

	public ERXKeyFilter _filterForKey(ERXKey key) {
		ERXKeyFilter filter = _includes.objectForKey(key);
		if (filter == null) {
			filter = new ERXKeyFilter(_base);
		}
		return filter;
	}

	public NSDictionary<ERXKey, ERXKeyFilter> includes() {
		return _includes;
	}

	public NSSet<ERXKey> excludes() {
		return _excludes;
	}

	public void include(ERXKey... keys) {
		for (ERXKey key : keys) {
			include(key);
		}
	}

	public boolean includes(ERXKey key) {
		return _includes.containsKey(key);
	}

	public ERXKeyFilter include(ERXKey key) {
		ERXKeyFilter filter;
		String keyPath = key.key();
		int dotIndex = keyPath.indexOf('.');
		if (dotIndex == -1) {
			filter = _includes.objectForKey(key);
			if (filter == null) {
				filter = new ERXKeyFilter(_base);
				_includes.setObjectForKey(filter, key);
				_excludes.removeObject(key);
			}
		}
		else {
			ERXKeyFilter subFilter = include(new ERXKey(keyPath.substring(0, dotIndex)));
			filter = subFilter.include(new ERXKey(keyPath.substring(dotIndex + 1)));
		}
		return filter;
	}

	public boolean excludes(ERXKey key) {
		return _excludes.contains(key);
	}

	public void exclude(ERXKey... keys) {
		for (ERXKey key : keys) {
			String keyPath = key.key();
			int dotIndex = keyPath.indexOf('.');
			if (dotIndex == -1) {
				_excludes.addObject(key);
				_includes.removeObjectForKey(key);
			}
			else {
				ERXKeyFilter subFilter = include(new ERXKey(keyPath.substring(0, dotIndex)));
				subFilter.exclude(new ERXKey(keyPath.substring(dotIndex + 1)));
			}
		}
	}

	public void only(ERXKey... keys) {
		for (ERXKey key : keys) {
			only(key);
		}
	}

	public ERXKeyFilter only(ERXKey key) {
		_base = ERXKeyFilter.Base.None;
		return include(key);
	}

	public boolean matches(ERXKey key, ERXKey.Type type) {
		boolean matches = false;
		if (_base == ERXKeyFilter.Base.None) {
			matches = includes(key);
		}
		else if (_base == ERXKeyFilter.Base.Attributes) {
			if (type == ERXKey.Type.Attribute) {
				matches = includes(key) || !excludes(key);
			}
		}
		else if (_base == ERXKeyFilter.Base.AttributesAndToOneRelationships) {
			if (type == ERXKey.Type.Attribute || type == ERXKey.Type.ToOneRelationship) {
				matches = includes(key) || !excludes(key);
			}
		}
		else if (_base == ERXKeyFilter.Base.All) {
			matches = !excludes(key);
		}
		else {
			throw new IllegalArgumentException("Unknown base '" + _base + "'.");
		}
		return matches;
	}
}
