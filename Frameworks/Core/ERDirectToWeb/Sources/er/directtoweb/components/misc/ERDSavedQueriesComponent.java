package er.directtoweb.components.misc;

import java.text.ParseException;
import java.util.Enumeration;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WODisplayGroup;
import com.webobjects.directtoweb.D2WContext;
import com.webobjects.directtoweb.D2WModel;
import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOObjectNotAvailableException;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOKeyValueArchiver;
import com.webobjects.eocontrol.EOKeyValueArchiving;
import com.webobjects.eocontrol.EOKeyValueUnarchiver;
import com.webobjects.eocontrol.EOSharedEditingContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSSelector;
import com.webobjects.foundation.NSTimestamp;
import com.webobjects.foundation.NSTimestampFormatter;

import er.extensions.eof.ERXConstant;
import er.extensions.eof.ERXEOAccessUtilities;
import er.extensions.eof.ERXEOControlUtilities;
import er.extensions.foundation.ERXArrayUtilities;
import er.extensions.foundation.ERXPatcher;
import er.extensions.foundation.ERXProperties;
import er.extensions.foundation.ERXSelectorUtilities;
import er.extensions.foundation.ERXValueUtilities;
import er.extensions.logging.ERXLogger;

/**
 * This Component will store the forms values in the displayGroup of a Query Page into user preferences
 * under a saved name.  The list of saved queries will be available in the popup for future use.
 *
 * Preference key = "SavedQueryFor"+pageConfiguration
 *
 * The ERNEUQueryPage has been modified to switch in the value of the rule keyPath = "savedQueryComponentName"
 * So, if you want this component to appear on your query page, create a rule defining savedQueryComponentName="ERNEUSavedQueriesComponent"
 *
 * @author dscheck
 */
public abstract class ERDSavedQueriesComponent extends WOComponent {
	public static final ERXLogger log = ERXLogger.getERXLogger(ERDSavedQueriesComponent.class);

	public static EOKeyValueArchiving.Support originalEOKVArchiningTimestampSupport = new EOKeyValueArchiving._TimestampSupport();
	public static EOKeyValueArchiving.Support newEOKVArchiningTimestampSupport = new ERDSavedQueriesComponent._TimestampSupport();

	public ERDSavedQueriesComponent(WOContext context) {
		super(context);
	}

	private NSKeyValueCoding _userPreferences = null;

	protected NSKeyValueCoding userPreferences() {
		if (_userPreferences == null) {
			Class prefClass = ERXPatcher.classForName("ERCoreUserPreferences");

			if (prefClass == null) {
				_userPreferences = (NSKeyValueCoding) session().objectForKey("ERCoreUserPreferences");
				if (_userPreferences == null) {
					_userPreferences = new NSMutableDictionary();
					session().setObjectForKey(_userPreferences, "ERCoreUserPreferences");
				}
			} else {
				NSSelector s = new NSSelector("userPreferences", new Class[] {});
				_userPreferences = (NSKeyValueCoding) ERXSelectorUtilities.invoke(s, prefClass);
			}
		}
		return _userPreferences;
	}

	public static class SavedQuery extends Object implements NSKeyValueCoding, EOKeyValueArchiving {
		private NSMutableDictionary dict = null;
		public final String NAME_KEY = "name";
		public final String QUERY_MIN_KEY = "queryMin";
		public final String QUERY_MAX_KEY = "queryMax";
		public final String QUERY_MATCH_KEY = "queryMatch";
		public final String QUERY_OPERATOR_KEY = "queryOperator";
		public final String QUERY_BINDINGS_KEY = "queryBindings";

		public SavedQuery() {
			super();

			dict = new NSMutableDictionary();
		}

		// Use this when creating from the current values in the displayGroup
		public SavedQuery(String name, WODisplayGroup displayGroup) {
			this();

			setName(name);

			getValuesFromDisplayGroup(displayGroup);
		}

		protected String logDictionary(String title, NSDictionary dictionary, String indentStr) {
			StringBuffer buf = new StringBuffer();

			buf.append("\r\n" + indentStr + "==========" + ((title != null) ? title : "") + "==================\r\n");
			buf.append(indentStr + "Dictionary dump, count=" + dictionary.count() + "\r\n");

			for (Enumeration anEnum = dictionary.keyEnumerator(); anEnum.hasMoreElements();) {
				String key = (String) anEnum.nextElement();
				Object value = dictionary.objectForKey(key);

				buf.append(indentStr + "key=" + key);
				buf.append(", valueClass=" + value.getClass().getName());
				buf.append(", toString = " + value.toString() + "\r\n");

				if (value instanceof NSDictionary)
					logDictionary(null, (NSDictionary) value, indentStr + "    ");
			}
			buf.append(indentStr + "============================\r\n");

			return buf.toString();
		}

		private NSMutableDictionary _updateDisplayGroupForKey(WODisplayGroup displayGroup, String key, boolean clearFormFirst) {
			NSDictionary source = (NSDictionary) valueForKey(key);

			NSMutableDictionary destination = (NSMutableDictionary) displayGroup.valueForKey(key);

			if (log.isDebugEnabled())
				log.debug("\r\nBEGIN key=" + key + ", destination.class=" + destination.getClass().getName() + ", destination.hashCode=" + destination.hashCode() + ", destination=" + destination);

			if (clearFormFirst && ERXProperties.booleanForKeyWithDefault("er.neutral.ERDSavedQueriesComponent.removeAllObjectBeforeUpdating", true))
				destination.removeAllObjects();

			if (source != null) {
				if (log.isDebugEnabled())
					log.debug("source != null, adding to destination.  source=" + source);
				destination.addEntriesFromDictionary(source);
			} else {
				if (log.isDebugEnabled())
					log.debug("source == null, NOT adding to destination.");
			}

			if (log.isDebugEnabled()) {
				log.debug("END key=" + key + ", destination.class=" + destination.getClass().getName() + ", destination=" + destination + "\r\n");

				log.debug(logDictionary(key, destination, "**"));
			}

			return destination;
		}

		public void sendValuesToDisplayGroup(WODisplayGroup displayGroup, boolean clearFormFirst) {
			_updateDisplayGroupForKey(displayGroup, QUERY_MIN_KEY, clearFormFirst);
			_updateDisplayGroupForKey(displayGroup, QUERY_MAX_KEY, clearFormFirst);
			_updateDisplayGroupForKey(displayGroup, QUERY_MATCH_KEY, clearFormFirst);
			_updateDisplayGroupForKey(displayGroup, QUERY_OPERATOR_KEY, clearFormFirst);
			_updateDisplayGroupForKey(displayGroup, QUERY_BINDINGS_KEY, clearFormFirst);
		}

		public void getValuesFromDisplayGroup(WODisplayGroup displayGroup) {
			if (displayGroup != null) {
				setQueryMin(displayGroup.queryMin().mutableClone());
				setQueryMax(displayGroup.queryMax().mutableClone());
				setQueryMatch(displayGroup.queryMatch().mutableClone());
				setQueryOperator(displayGroup.queryOperator().mutableClone());
				setQueryBindings(displayGroup.queryBindings().mutableClone());
			}
		}

		public NSDictionary values() {
			return dict;
		}

		public String name() {
			return (String) dict.objectForKey(NAME_KEY);
		}

		public void setName(String aName) {
			if (aName == null)
				aName = "Untitled Saved Query";

			dict.setObjectForKey(aName, NAME_KEY);
		}

		public NSDictionary queryMin() {
			return (NSDictionary) dict.objectForKey(QUERY_MIN_KEY);
		}

		public void setQueryMin(NSDictionary queryMin) {
			if (queryMin == null)
				queryMin = NSDictionary.EmptyDictionary;

			dict.setObjectForKey(queryMin, QUERY_MIN_KEY);
		}

		public NSDictionary queryMax() {
			return (NSDictionary) dict.objectForKey(QUERY_MAX_KEY);
		}

		public void setQueryMax(NSDictionary queryMax) {
			if (queryMax == null)
				queryMax = NSDictionary.EmptyDictionary;

			dict.setObjectForKey(queryMax, QUERY_MAX_KEY);
		}

		public NSDictionary queryMatch() {
			return (NSDictionary) dict.objectForKey(QUERY_MATCH_KEY);
		}

		public void setQueryMatch(NSDictionary queryMatch) {
			if (queryMatch == null)
				queryMatch = NSDictionary.EmptyDictionary;

			log.debug(logDictionary("setQueryMatch", queryMatch, "==="));

			dict.setObjectForKey(queryMatch, QUERY_MATCH_KEY);
		}

		public NSDictionary queryOperator() {
			return (NSDictionary) dict.objectForKey(QUERY_OPERATOR_KEY);
		}

		public void setQueryOperator(NSDictionary queryOperator) {
			if (queryOperator == null)
				queryOperator = NSDictionary.EmptyDictionary;

			dict.setObjectForKey(queryOperator, QUERY_OPERATOR_KEY);
		}

		public NSDictionary queryBindings() {
			return (NSDictionary) dict.objectForKey(QUERY_BINDINGS_KEY);
		}

		public void setQueryBindings(NSDictionary queryBindings) {
			if (queryBindings == null)
				queryBindings = NSDictionary.EmptyDictionary;

			dict.setObjectForKey(queryBindings, QUERY_BINDINGS_KEY);
		}

		// NSKeyValueCoding methods

		public Object valueForKey(String key) {
			return NSKeyValueCoding.DefaultImplementation.valueForKey(this, key);
		}

		public void takeValueForKey(Object value, String key) {
			NSKeyValueCoding.DefaultImplementation.takeValueForKey(this, value, key);
		}

		// NSKeyValueCoding.ErrorHandling methods

		public Object handleQueryWithUnboundKey(String key) {
			return NSKeyValueCoding.DefaultImplementation.handleQueryWithUnboundKey(this, key);
		}

		public void handleTakeValueForUnboundKey(Object value, String key) {
			NSKeyValueCoding.DefaultImplementation.handleTakeValueForUnboundKey(this, value, key);
		}

		public void unableToSetNullForKey(String key) {
			NSKeyValueCoding.DefaultImplementation.unableToSetNullForKey(this, key);
		}

		public void encodeWithKeyValueArchiver(EOKeyValueArchiver archiver) {
			archiver.encodeObject(name(), "name");
			archiver.encodeObject(encodeDictionaryWithSharedEOs(queryMin().mutableClone()), "queryMin");
			archiver.encodeObject(encodeDictionaryWithSharedEOs(queryMax().mutableClone()), "queryMax");
			archiver.encodeObject(encodeDictionaryWithSharedEOs(queryMatch().mutableClone()), "queryMatch");
			archiver.encodeObject(encodeDictionaryWithSharedEOs(queryOperator().mutableClone()), "queryOperator");
			archiver.encodeObject(encodeDictionaryWithSharedEOs(queryBindings().mutableClone()), "queryBindings");
		}

		public static Object decodeWithKeyValueUnarchiver(EOKeyValueUnarchiver unarchiver) {
			return new SavedQuery(unarchiver);
		}

		// Use this when resetting from a stored preference
		private SavedQuery(EOKeyValueUnarchiver unarchiver) {
			this();
			setName((String) unarchiver.decodeObjectForKey("name"));
			setQueryMin((NSDictionary) decodeDictionaryWithSharedEOs((NSDictionary) unarchiver.decodeObjectForKey("queryMin")));
			setQueryMax((NSDictionary) decodeDictionaryWithSharedEOs((NSDictionary) unarchiver.decodeObjectForKey("queryMax")));
			setQueryMatch((NSDictionary) decodeDictionaryWithSharedEOs((NSDictionary) unarchiver.decodeObjectForKey("queryMatch")));
			setQueryOperator((NSDictionary) decodeDictionaryWithSharedEOs((NSDictionary) unarchiver.decodeObjectForKey("queryOperator")));
			setQueryBindings((NSDictionary) decodeDictionaryWithSharedEOs((NSDictionary) unarchiver.decodeObjectForKey("queryBindings")));
		}

		public String toString() {
			return "SavedQuery: " + hashCode() + " dict=" + dict;
		}

		private boolean isDictionaryAnEncodedSharedEO(NSDictionary dictionary) {
			return dictionary != null && dictionary.objectForKey("encodedSharedEO") != null;
		}

		private NSMutableDictionary encodeSharedEO(EOEnterpriseObject eo) {
			NSMutableDictionary encodedDict = new NSMutableDictionary(3);

			encodedDict.setObjectForKey(Boolean.TRUE, "encodedSharedEO");
			encodedDict.setObjectForKey(eo.entityName(), "entityName");
			encodedDict.setObjectForKey(ERXEOControlUtilities.primaryKeyStringForObject(eo), "pk");

			if (log.isDebugEnabled())
				log.debug("encodeSharedEO, eo=" + eo + ",  encodedDict=" + encodedDict);

			return encodedDict;
		}

		private EOEnterpriseObject decodeSharedEO(NSDictionary dictionary) {
			String entityName = (String) dictionary.objectForKey("entityName");
			String pk = (String) dictionary.objectForKey("pk");
			EOEntity entity = ERXEOAccessUtilities.entityNamed(null, entityName);
			EOAttribute primaryKeyAttribute = (EOAttribute) ERXArrayUtilities.firstObject(entity.primaryKeyAttributes());
			Object primaryKeyObject = pk;
			EOEnterpriseObject eo = null;

			if (log.isDebugEnabled())
				log.debug("decodeSharedEO, dict=" + dictionary);

			if (primaryKeyAttribute != null && !String.class.getName().equals(primaryKeyAttribute.className()))
				primaryKeyObject = ERXConstant.integerForString(pk);

			try {
				eo = ERXEOControlUtilities.sharedObjectWithPrimaryKey(entityName, primaryKeyObject);
			} catch (EOObjectNotAvailableException e) {

			}
			return eo;
		}

		// Recursively walk down the given dictionary and convert any values
		// that are SharedEntity EOs to a dictionary with keys (encodedSharedEO,
		// entityName, pk)
		protected NSMutableDictionary encodeDictionaryWithSharedEOs(NSDictionary dictionary) {
			NSMutableDictionary theMutableDictionary = (NSMutableDictionary) ((dictionary instanceof NSMutableDictionary) ? dictionary : dictionary.mutableClone());

			for (Enumeration anEnum = theMutableDictionary.keyEnumerator(); anEnum.hasMoreElements();) {
				String key = (String) anEnum.nextElement();
				Object value = theMutableDictionary.objectForKey(key);

				if (value instanceof EOEnterpriseObject && ((EOEnterpriseObject) value).editingContext().equals(EOSharedEditingContext.defaultSharedEditingContext())) {
					EOEnterpriseObject eo = (EOEnterpriseObject) value;

					NSMutableDictionary encodedDict = encodeSharedEO(eo);

					theMutableDictionary.setObjectForKey(encodedDict, key);
				} else if (value instanceof NSDictionary) {
					theMutableDictionary.setObjectForKey(encodeDictionaryWithSharedEOs((NSDictionary) value), key);
				}
			}
			return theMutableDictionary;
		}

		// Recursively walk down the given dictionary and convert any
		// dictionaries with keys (encodedSharedEO, entityName, pk) back to the
		// Shared EO
		protected Object decodeDictionaryWithSharedEOs(NSDictionary dictionary) {
			NSMutableDictionary theMutableDictionary = (NSMutableDictionary) ((dictionary instanceof NSMutableDictionary) ? dictionary : dictionary.mutableClone());

			if (isDictionaryAnEncodedSharedEO(dictionary)) {
				Object eo = decodeSharedEO(dictionary);

				// if we cant decode the sharedEO, then just return the
				// dictionary
				return (eo != null) ? eo : dictionary;
			}

			for (Enumeration anEnum = theMutableDictionary.keyEnumerator(); anEnum.hasMoreElements();) {
				String key = (String) anEnum.nextElement();
				Object value = theMutableDictionary.objectForKey(key);

				if (value instanceof NSDictionary) {
					theMutableDictionary.setObjectForKey(decodeDictionaryWithSharedEOs((NSDictionary) value), key);
				}
			}
			return theMutableDictionary;
		}
	}

	// Replacement helper class to archive the NSTimestamp. We need to remove
	// the %z from the format string because
	// NSTimestampFormatter cant deal with the %z
	public static class _TimestampSupport extends EOKeyValueArchiving.Support {
		private static NSTimestampFormatter _formatter = new NSTimestampFormatter("%Y-%m-%d %H:%M:%S");

		public void encodeWithKeyValueArchiver(Object receiver, EOKeyValueArchiver archiver) {
			archiver.encodeObject(_formatter.format(receiver), "value");
		}

		public Object decodeObjectWithKeyValueUnarchiver(EOKeyValueUnarchiver unarchiver) {
			try {
				return _formatter.parseObject((String) unarchiver.decodeObjectForKey("value"));
			} catch (ParseException exception) {
				throw NSForwardException._runtimeExceptionForThrowable(exception);
			}
		}
	}

	private NSMutableArray _savedQueries = null;
	public ERDSavedQueriesComponent.SavedQuery aSavedQuery = null;
	public ERDSavedQueriesComponent.SavedQuery selectedSavedQuery = null;
	public String newQueryName = null;
	public final String DEFAULT_QUERY_NONE = "";
	public boolean needsAutoSubmit = false;

	public String userPreferenceNameForPageConfiguration(String pageConfiguration) {
		return "SavedQueryFor:" + pageConfiguration;
	}

	public String userPreferenceNameForDefaultQueryWithPageConfiguration(String pageConfiguration) {
		return "DefaultQueryNameFor:" + pageConfiguration;
	}

	public String userPreferenceNameForAutoSubmitWithPageConfiguration(String pageConfiguration) {
		return "AutoSubmitQueryFor:" + pageConfiguration;
	}

	public NSMutableArray loadSavedQueriesForPageConfigurationNamed(String pageConfigurationName) {
		NSArray savedQueries = null;

		EOKeyValueArchiving.Support.setSupportForClass(newEOKVArchiningTimestampSupport, NSTimestamp._CLASS);

		try {
			savedQueries = (NSArray) userPreferences().valueForKey(userPreferenceNameForPageConfiguration(pageConfigurationName));
		} finally {
			EOKeyValueArchiving.Support.setSupportForClass(originalEOKVArchiningTimestampSupport, NSTimestamp._CLASS);
		}

		if (log.isDebugEnabled())
			log.debug("loadSavedQueriesForPageConfigurationNamed(" + pageConfigurationName + "): queries = " + savedQueries);

		return savedQueries == null ? new NSMutableArray() : savedQueries.mutableClone();
	}

	public void saveQueriesForPageConfigurationNamed(NSArray queries, String pageConfigurationName) {
		if (log.isDebugEnabled())
			log.debug("saveQueriesForPageConfigurationNamed(" + pageConfigurationName + "): queries = " + queries);

		EOKeyValueArchiving.Support.setSupportForClass(newEOKVArchiningTimestampSupport, NSTimestamp._CLASS);

		try {
			userPreferences().takeValueForKey(queries, userPreferenceNameForPageConfiguration(pageConfigurationName));
		} finally {
			EOKeyValueArchiving.Support.setSupportForClass(originalEOKVArchiningTimestampSupport, NSTimestamp._CLASS);
		}
	}

	/** component does not synchronize variables */
	public boolean synchronizesVariablesWithBindings() {
		return false;
	}

	/** component is not stateless */
	public boolean isStateless() {
		return false;
	}

	public void sleep() {
		needsAutoSubmit = false;
		super.sleep();
	}

	public D2WContext d2wContext() {
		return (D2WContext) valueForBinding("localContext");
	}

	public WODisplayGroup displayGroup() {
		return (WODisplayGroup) valueForBinding("displayGroup");
	}

	public String pageConfiguration() {
		return (String) d2wContext().valueForKey(D2WModel.DynamicPageKey);
	}

	public NSMutableArray savedQueries() {
		if (_savedQueries == null) {
			String pageConfiguration = pageConfiguration();

			_savedQueries = loadSavedQueriesForPageConfigurationNamed(pageConfiguration);

			String defaultName = defaultQueryNameForPageConfiguration(pageConfiguration);

			selectedSavedQuery = null;

			// Try to setup the default query if it is set.
			if (_savedQueries.count() > 0 && !DEFAULT_QUERY_NONE.equals(defaultName)) {
				for (Enumeration queryEnum = _savedQueries.objectEnumerator(); queryEnum.hasMoreElements();) {
					ERDSavedQueriesComponent.SavedQuery aQuery = (ERDSavedQueriesComponent.SavedQuery) queryEnum.nextElement();

					if (defaultName.equals(aQuery.name())) {
						selectedSavedQuery = aQuery;

						// since this is the inital population dont loose
						// anything that is already set there from the query
						// setup code
						selectedSavedQuery.sendValuesToDisplayGroup(displayGroup(), false);

						newQueryName = selectedSavedQuery.name();

						break;
					}

				}
			}
		}

		return _savedQueries;
	}

	private static SavedQuery emptySavedQueryForDeletes = new SavedQuery();

	public WOComponent popupChangedSelection() {
		if (selectedSavedQuery != null) {
			selectedSavedQuery.sendValuesToDisplayGroup(displayGroup(), true);
			newQueryName = selectedSavedQuery.name();

			needsAutoSubmit = autoSubmitEnabled();
		} else {
			clearForm();
		}

		return null;
	}

	public WOComponent refresh() {
		return context().page();
	}

	public boolean autoSubmitEnabled() {
		return ERXValueUtilities.booleanValue(userPreferences().valueForKey(userPreferenceNameForAutoSubmitWithPageConfiguration(pageConfiguration())));
	}

	public void setAutoSubmitEnabled(boolean b) {
		userPreferences().takeValueForKey((b ? "1" : "0"), userPreferenceNameForAutoSubmitWithPageConfiguration(pageConfiguration()));
	}

	public WOComponent addNewQuery() {
		ERDSavedQueriesComponent.SavedQuery newQuery = new SavedQuery(newQueryName, displayGroup());

		_savedQueries.addObject(newQuery);

		selectedSavedQuery = newQuery;

		saveQueriesForPageConfigurationNamed(_savedQueries, pageConfiguration());

		return null;
	}

	public WOComponent updateCurrentQuery() {
		if (newQueryName != null && newQueryName.trim().length() > 0) {
			if (isDefaultQuery(selectedSavedQuery))
				setDefaultQueryNameForPageConfiguration(newQueryName, pageConfiguration());

			selectedSavedQuery.setName(newQueryName);
		}

		selectedSavedQuery.getValuesFromDisplayGroup(displayGroup());

		if (ERXProperties.booleanForKeyWithDefault("er.neutral.ERDSavedQueriesComponent.saveAfterUpdating", true))
			saveQueriesForPageConfigurationNamed(_savedQueries, pageConfiguration());

		return null;
	}

	public WOComponent deleteCurrentQuery() {
		if (selectedSavedQuery != null) {
			_savedQueries.removeObject(selectedSavedQuery);

			saveQueriesForPageConfigurationNamed(_savedQueries, pageConfiguration());

			if (isDefaultQuery(selectedSavedQuery))
				setDefaultQueryNameForPageConfiguration("", pageConfiguration());

			selectedSavedQuery = null;
		}

		return clearForm();
	}

	public WOComponent deleteAllSavedQueries() {
		_savedQueries.removeAllObjects();

		saveQueriesForPageConfigurationNamed(_savedQueries, pageConfiguration());

		selectedSavedQuery = null;

		return clearForm();
	}

	public WOComponent clearForm() {
		selectedSavedQuery = null;
		newQueryName = null;

		emptySavedQueryForDeletes.sendValuesToDisplayGroup(displayGroup(), true);

		return null;
	}

	public WOComponent makeDefaultSavedQuery() {
		String aQueryName = (selectedSavedQuery != null) ? selectedSavedQuery.name() : DEFAULT_QUERY_NONE;

		setDefaultQueryNameForPageConfiguration(aQueryName, pageConfiguration());

		return null;
	}

	public String defaultQueryNameForPageConfiguration(String pageConfigurationName) {
		String defaultQueryName = (String) userPreferences().valueForKey(userPreferenceNameForDefaultQueryWithPageConfiguration(pageConfigurationName));

		if (log.isDebugEnabled())
			log.debug("defaultQueryNameForPageConfiguration(" + pageConfigurationName + "): defaultQueryName = " + defaultQueryName);

		return (defaultQueryName != null) ? defaultQueryName : DEFAULT_QUERY_NONE;
	}

	public void setDefaultQueryNameForPageConfiguration(String aName, String pageConfigurationName) {
		userPreferences().takeValueForKey(aName, userPreferenceNameForDefaultQueryWithPageConfiguration(pageConfigurationName));

		if (log.isDebugEnabled())
			log.debug("setDefaultQueryNameForPageConfiguration(" + pageConfigurationName + "): defaultQueryName = " + aName);
	}

	public boolean isSelectedQueryTheDefault() {
		return isDefaultQuery(selectedSavedQuery);
	}

	public boolean isNoSelectedQuery() {
		return selectedSavedQuery == null;
	}

	public boolean hasNoSavedQueries() {
		return _savedQueries.count() == 0;
	}

	public boolean isDefaultQuery(ERDSavedQueriesComponent.SavedQuery aQuery) {
		String queryName = (aQuery != null) ? aQuery.name() : DEFAULT_QUERY_NONE;
		return queryName.equals(defaultQueryNameForPageConfiguration(pageConfiguration()));
	}

}
