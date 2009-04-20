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
	private NSMutableDictionary<ERXKey, ERXKey> _map;
	private ERXKeyFilter.Base _nextBase;

	/**
	 * Creates a new ERXKeyFilter.
	 * 
	 * @param base the base rule to apply
	 */
	public ERXKeyFilter(ERXKeyFilter.Base base) {
		_base = base;
		_nextBase = ERXKeyFilter.Base.None;
		_includes = new NSMutableDictionary<ERXKey, ERXKeyFilter>();
		_excludes = new NSMutableSet<ERXKey>();
		_map = new NSMutableDictionary<ERXKey, ERXKey>();
	}
	
	/**
	 * Adds a key mapping to this filter.
	 * 
	 * @param fromKey the key to map from
	 * @param toKey the key to map to
	 */
	public void addMap(ERXKey fromKey, ERXKey toKey) {
		_map.setObjectForKey(toKey, fromKey);
	}
	
	/**
	 * Returns the key that is mapped to from the given input key.
	 * 
	 * @param <T> the type of the key (which doesn't change)
	 * @param fromKey the key to map from
	 * @return the key that maps to the given key
	 */
	public <T> ERXKey<T> keyMap(ERXKey<T> fromKey) {
		@SuppressWarnings("cast") ERXKey<T> toKey = (ERXKey<T>) _map.objectForKey(fromKey);
		if (toKey == null) {
			toKey = fromKey;
		}
		return toKey;
	}
	
	/**
	 * Shortcut to return a new ERXKeyFilter(None)
	 * @return a new ERXKeyFilter(None)
	 */
	public static ERXKeyFilter filterWithNone() {
		return new ERXKeyFilter(ERXKeyFilter.Base.None);
	}
	
	/**
	 * Shortcut to return a new ERXKeyFilter(Attributes)
	 * @return a new ERXKeyFilter(Attributes)
	 */
	public static ERXKeyFilter filterWithAttributes() {
		return new ERXKeyFilter(ERXKeyFilter.Base.Attributes);
	}
	
	/**
	 * Shortcut to return a new ERXKeyFilter(AttributesAndToOneRelationships)
	 * @return a new ERXKeyFilter(AttributesAndToOneRelationships)
	 */
	public static ERXKeyFilter filterWithAttributesAndToOneRelationships() {
		return new ERXKeyFilter(ERXKeyFilter.Base.AttributesAndToOneRelationships);
	}
	
	/**
	 * Shortcut to return a new ERXKeyFilter(All)
	 * @return a new ERXKeyFilter(All)
	 */
	public static ERXKeyFilter filterWithAll() {
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
	
	/**
	 * Returns the base that is used for subkeys of this key by default.
	 * 
	 * @return the base that is used for subkeys of this key by default
	 */
	public ERXKeyFilter.Base nextBase() {
		return _nextBase;
	}
	
	/**
	 * Sets the base that is used for subkeys of this key by default.
	 * @param nextBase the base that is used for subkeys of this key by default
	 * @return this (for chaining) 
	 */
	public ERXKeyFilter setNextBase(ERXKeyFilter.Base nextBase) {
		_nextBase = nextBase;
		return this;
	}

	/**
	 * Returns the filter for the given key, or creates a "nextBase" filter
	 * if there isn't one.  This should usually only be called when you 
	 * know exactly what you're doing, as this doesn't fully interpret 
	 * include/exclude rules.
	 * 
	 * @param key the key to lookup
	 * @return the key filter
	 */
	public ERXKeyFilter _filterForKey(ERXKey key) {
		ERXKeyFilter filter = _includes.objectForKey(key);
		if (filter == null) {
			filter = new ERXKeyFilter(_nextBase);
			filter.setNextBase(_nextBase);
		}
		return filter;
	}

	/**
	 * Returns the included keys and the next filters they map to.
	 * 
	 * @return the included keys and the next filters they map to
	 */
	public NSDictionary<ERXKey, ERXKeyFilter> includes() {
		return _includes;
	}

	/**
	 * Returns the set of keys that are explicitly excluded.
	 * 
	 * @return the set of keys that are explicitly excluded
	 */
	public NSSet<ERXKey> excludes() {
		return _excludes;
	}

	/**
	 * Includes the given set of keys in this filter.
	 * 
	 * @param keys the keys to include
	 */
	public void include(ERXKey... keys) {
		for (ERXKey key : keys) {
			include(key);
		}
	}

	/**
	 * Returns whether or not the given key is included in this filter.
	 * 
	 * @param key the key to lookup
	 * @return whether or not the given key is included in this filter
	 */
	public boolean includes(ERXKey key) {
		return _includes.containsKey(key);
	}

	/**
	 * Includes the given key in this filter.
	 * 
	 * @param key the key to include
	 * @return the next filter
	 */
	public ERXKeyFilter include(ERXKey key) {
		ERXKeyFilter filter;
		String keyPath = key.key();
		int dotIndex = keyPath.indexOf('.');
		if (dotIndex == -1) {
			filter = _includes.objectForKey(key);
			if (filter == null) {
				filter = new ERXKeyFilter(_nextBase);
				filter.setNextBase(_nextBase);
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

	/**
	 * Returns whether or not the given key is excluded.
	 * 
	 * @param key the key to lookup
	 * @return whether or not the given key is excluded
	 */
	public boolean excludes(ERXKey key) {
		return _excludes.contains(key);
	}

	/**
	 * Excludes the given keys from this filter.
	 * 
	 * @param keys the keys to exclude
	 */
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

	/**
	 * Restricts this filter to only allow the given keys.
	 * 
	 * @param keys the keys to restrict to
	 */
	public void only(ERXKey... keys) {
		_base = ERXKeyFilter.Base.None;
		_includes.clear();
		_excludes.clear();
		for (ERXKey key : keys) {
			include(key);
		}
	}

	/**
	 * Restricts this filter to only allow the given key.
	 *   
	 * @param key the only key to allow
	 * @return the next filter
	 */
	public ERXKeyFilter only(ERXKey key) {
		_base = ERXKeyFilter.Base.None;
		_includes.clear();
		_excludes.clear();
		return include(key);
	}

	/**
	 * Returns whether or not the given key (of the given type, if known) is included in this filter.
	 * 
	 * @param key the key to lookup 
	 * @param type the type of the key (if known)
	 * @return whether or not this filter matches the key
	 */
	public boolean matches(ERXKey key, ERXKey.Type type) {
		boolean matches = false;
		if (includes(key) && !excludes(key)) {
			matches = true;
		}
		else if (_base == ERXKeyFilter.Base.None) {
			matches = includes(key) && !excludes(key);
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
