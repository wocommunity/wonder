package er.directtoweb.components.misc;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.Enumeration;

import org.apache.commons.lang.CharEncoding;
import org.apache.log4j.Logger;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WODisplayGroup;
import com.webobjects.appserver.WOSession;
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

import er.extensions.appserver.ERXSession;
import er.extensions.eof.ERXEOAccessUtilities;
import er.extensions.eof.ERXEOControlUtilities;
import er.extensions.foundation.ERXArrayUtilities;
import er.extensions.foundation.ERXPatcher;
import er.extensions.foundation.ERXProperties;
import er.extensions.foundation.ERXSelectorUtilities;
import er.extensions.foundation.ERXStringUtilities;
import er.extensions.foundation.ERXValueUtilities;

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
public class ERDSavedQueriesComponent extends WOComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	public static final Logger log = Logger.getLogger(ERDSavedQueriesComponent.class);

    public static final EOKeyValueArchiving.Support originalEOKVArchivingTimestampSupport = new EOKeyValueArchiving._TimestampSupport();
    public static final EOKeyValueArchiving.Support newEOKVArchivingTimestampSupport = new ERDSavedQueriesComponent._TimestampSupport();

    /** @deprecated  use {@link #originalEOKVArchivingTimestampSupport} */
    @Deprecated
    public static final EOKeyValueArchiving.Support originalEOKVArchiningTimestampSupport = originalEOKVArchivingTimestampSupport;
    /** @deprecated  use {@link #newEOKVArchivingTimestampSupport} */
    @Deprecated
    public static final EOKeyValueArchiving.Support newEOKVArchiningTimestampSupport = newEOKVArchivingTimestampSupport;

	public ERDSavedQueriesComponent(WOContext context) {
		super(context);
	}

	private NSKeyValueCoding _userPreferences = null;

	protected NSKeyValueCoding userPreferences() {
		if (_userPreferences == null) {
			_userPreferences = userPreferences(session());
		}
		return _userPreferences;
	}
	
	static NSKeyValueCoding userPreferences(WOSession session) {
		NSKeyValueCoding result = null;
		Class prefClass = ERXPatcher.classForName("ERCoreUserPreferences");
		if (prefClass == null) {
			result = (NSKeyValueCoding) session.objectForKey("ERCoreUserPreferences");
			if (result == null) {
				result = new NSMutableDictionary();
				session.setObjectForKey(result, "ERCoreUserPreferences");
			}
		} else {
			NSSelector s = new NSSelector("userPreferences", new Class[] {});
			result = (NSKeyValueCoding) ERXSelectorUtilities.invoke(s, prefClass);
		}
		return result;
	}

    public static class SavedQuery implements NSKeyValueCoding, EOKeyValueArchiving {
		private NSMutableDictionary dict = null;
		public static final String NAME_KEY = "name";
		public static final String QUERY_MIN_KEY = "queryMin";
		public static final String QUERY_MAX_KEY = "queryMax";
		public static final String QUERY_MATCH_KEY = "queryMatch";
		public static final String QUERY_OPERATOR_KEY = "queryOperator";
		public static final String QUERY_BINDINGS_KEY = "queryBindings";

        public static interface SerializationKeys {
            public static final String EncodedEO = "encodedEO";
            public static final String EncodedSharedEO = "encodedSharedEO";

            public static final String EntityName = "entityName";
            public static final String PrimaryKey = "pk";
        }

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

		protected static String logDictionary(String title, NSDictionary dictionary, String indentStr) {
			StringBuilder buf = new StringBuilder();

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
		
		public void sendValuesToDisplayGroup(WODisplayGroup displayGroup, boolean clearFormFirst) {
			sendValuesToDisplayGroup(this, displayGroup, clearFormFirst);
		}
		
		public static void sendValuesToDisplayGroup(SavedQuery savedQuery, WODisplayGroup displayGroup, boolean clearFormFirst) {
			updateDisplayGroupForKey(savedQuery, displayGroup, QUERY_MIN_KEY, clearFormFirst);
			updateDisplayGroupForKey(savedQuery, displayGroup, QUERY_MAX_KEY, clearFormFirst);
			updateDisplayGroupForKey(savedQuery, displayGroup, QUERY_MATCH_KEY, clearFormFirst);
			updateDisplayGroupForKey(savedQuery, displayGroup, QUERY_OPERATOR_KEY, clearFormFirst);
			updateDisplayGroupForKey(savedQuery, displayGroup, QUERY_BINDINGS_KEY, clearFormFirst);
		}

		static NSMutableDictionary updateDisplayGroupForKey(SavedQuery savedQuery, WODisplayGroup displayGroup, String key, boolean clearFormFirst) {
			NSDictionary source = (NSDictionary) savedQuery.valueForKey(key);

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
            archiver.encodeObject(encodeDictionaryWithEOs(queryMin().mutableClone()), "queryMin");
            archiver.encodeObject(encodeDictionaryWithEOs(queryMax().mutableClone()), "queryMax");
            archiver.encodeObject(encodeDictionaryWithEOs(queryMatch().mutableClone()), "queryMatch");
            archiver.encodeObject(encodeDictionaryWithEOs(queryOperator().mutableClone()), "queryOperator");
            archiver.encodeObject(encodeDictionaryWithEOs(queryBindings().mutableClone()), "queryBindings");
		}

		public static Object decodeWithKeyValueUnarchiver(EOKeyValueUnarchiver unarchiver) {
			return new SavedQuery(unarchiver);
		}

		// Use this when resetting from a stored preference
		private SavedQuery(EOKeyValueUnarchiver unarchiver) {
			this();
            setName((String)unarchiver.decodeObjectForKey("name"));
            setQueryMin((NSDictionary) decodeDictionaryWithEOs((NSDictionary)unarchiver.decodeObjectForKey("queryMin")));
            setQueryMax((NSDictionary) decodeDictionaryWithEOs((NSDictionary)unarchiver.decodeObjectForKey("queryMax")));
            setQueryMatch((NSDictionary) decodeDictionaryWithEOs((NSDictionary)unarchiver.decodeObjectForKey("queryMatch")));
            setQueryOperator((NSDictionary) decodeDictionaryWithEOs((NSDictionary)unarchiver.decodeObjectForKey("queryOperator")));
            setQueryBindings((NSDictionary) decodeDictionaryWithEOs((NSDictionary)unarchiver.decodeObjectForKey("queryBindings")));
		}

		@Override
		public String toString() {
			return "SavedQuery: " + hashCode() + " dict=" + dict;
		}
        
        private boolean isDictionaryAnEncodedEO(NSDictionary dictionary) {
            return dictionary != null && dictionary.objectForKey(SerializationKeys.EncodedEO) != null;
        }

		private boolean isDictionaryAnEncodedSharedEO(NSDictionary dictionary) {
			return dictionary != null && dictionary.objectForKey(SerializationKeys.EncodedSharedEO) != null;
		}

		private NSMutableDictionary encodeEO(EOEnterpriseObject eo) {
			NSMutableDictionary encodedDict = new NSMutableDictionary(3);

            if (eo.editingContext() instanceof EOSharedEditingContext) {
                encodedDict.setObjectForKey(Boolean.TRUE, SerializationKeys.EncodedSharedEO);
            } else {
                encodedDict.setObjectForKey(Boolean.TRUE, SerializationKeys.EncodedEO);
            }
            encodedDict.setObjectForKey(eo.entityName(), SerializationKeys.EntityName);
            encodedDict.setObjectForKey(ERXEOControlUtilities.primaryKeyStringForObject(eo), SerializationKeys.PrimaryKey);

			if (log.isDebugEnabled())
				log.debug("encodeSharedEO, eo=" + eo + ",  encodedDict=" + encodedDict);

			return encodedDict;
		}

        private EOEnterpriseObject decodeEO(NSDictionary dictionary) {
            String entityName = (String)dictionary.objectForKey(SerializationKeys.EntityName);
            String pk = (String)dictionary.objectForKey(SerializationKeys.PrimaryKey);
            EOEntity entity = ERXEOAccessUtilities.entityNamed(null, entityName);
            EOAttribute primaryKeyAttribute;
            EOEnterpriseObject eo = null;
            if (entity != null) {
                primaryKeyAttribute = ERXArrayUtilities.firstObject(entity.primaryKeyAttributes());
                Object primaryKeyObject = pk;
                if (log.isDebugEnabled()) log.debug("decodeEO with dict: " + dictionary);

                if (primaryKeyAttribute != null && !String.class.getName().equals(primaryKeyAttribute.className())) {
                    primaryKeyObject = ERXStringUtilities.integerWithString(pk);
                }

                try {
                    if (isDictionaryAnEncodedSharedEO(dictionary)) {
                        eo = ERXEOControlUtilities.sharedObjectWithPrimaryKey(entityName, primaryKeyObject);
                    } else {
                        eo = ERXEOControlUtilities.objectWithPrimaryKeyValue(ERXSession.session().defaultEditingContext(), entityName, primaryKeyObject, null);
                    }
                } catch (EOObjectNotAvailableException eoonae) {
                    if (log.isDebugEnabled()) {
                        log.debug("Unable to restore serialized EO from saved query.  The EO for entity '" + entityName + "' with pk '" + pk + "' was not found.");
                    }
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Unable to restore serialized EO from saved query.  Could not find entity '" + entityName + ".");
                }
            }
            return eo;
        }

		// Recursively walk down the given dictionary and convert any values
		// that are SharedEntity EOs to a dictionary with keys (encodedSharedEO or encodedEO,
		// entityName, pk)
		protected NSMutableDictionary encodeDictionaryWithEOs(NSDictionary dictionary) {
			NSMutableDictionary theMutableDictionary = (NSMutableDictionary) ((dictionary instanceof NSMutableDictionary) ? dictionary : dictionary.mutableClone());

			for (Enumeration anEnum = theMutableDictionary.keyEnumerator(); anEnum.hasMoreElements();) {
				String key = (String) anEnum.nextElement();
				Object value = theMutableDictionary.objectForKey(key);

				if (value instanceof EOEnterpriseObject && !ERXEOControlUtilities.isNewObject((EOEnterpriseObject)value)) {
                    EOEnterpriseObject eo = (EOEnterpriseObject)value;
                    NSMutableDictionary encodedDict = encodeEO(eo);
					theMutableDictionary.setObjectForKey(encodedDict, key);
				} else if (value instanceof NSDictionary) {
					theMutableDictionary.setObjectForKey(encodeDictionaryWithEOs((NSDictionary) value), key);
				}
			}
			return theMutableDictionary;
		}

		// Recursively walk down the given dictionary and convert any
		// dictionaries with keys (encodedSharedEO or encodedEO, entityName, pk) back to the
		// Shared EO
		protected Object decodeDictionaryWithEOs(NSDictionary dictionary) {
			NSMutableDictionary theMutableDictionary = (NSMutableDictionary) ((dictionary instanceof NSMutableDictionary) ? dictionary : dictionary.mutableClone());

			if (isDictionaryAnEncodedSharedEO(dictionary) || isDictionaryAnEncodedEO(dictionary)) {
				Object eo = decodeEO(dictionary);

                // If we can't decode the EO, then just return the dictionary.
                return  (eo != null) ? eo : dictionary;
			}

			for (Enumeration anEnum = theMutableDictionary.keyEnumerator(); anEnum.hasMoreElements();) {
				String key = (String) anEnum.nextElement();
				Object value = theMutableDictionary.objectForKey(key);

				if (value instanceof NSDictionary) {
					theMutableDictionary.setObjectForKey(decodeDictionaryWithEOs((NSDictionary) value), key);
				}
			}
			return theMutableDictionary;
		}
	}

	// Replacement helper class to archive the NSTimestamp. We need to remove
	// the %z from the format string because
	// NSTimestampFormatter can't deal with the %z
	public static class _TimestampSupport extends EOKeyValueArchiving.Support {
		private static NSTimestampFormatter _formatter = new NSTimestampFormatter("%Y-%m-%d %H:%M:%S");

		@Override
		public void encodeWithKeyValueArchiver(Object receiver, EOKeyValueArchiver archiver) {
			archiver.encodeObject(_formatter.format(receiver), "value");
		}

		@Override
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

	public static String userPreferenceNameForPageConfiguration(String pageConfiguration) {
		return "SavedQueryFor:" + pageConfiguration;
	}

	public static String userPreferenceNameForDefaultQueryWithPageConfiguration(String pageConfiguration) {
		return "DefaultQueryNameFor:" + pageConfiguration;
	}

	public static String userPreferenceNameForAutoSubmitWithPageConfiguration(String pageConfiguration) {
		return "AutoSubmitQueryFor:" + pageConfiguration;
	}

	public NSMutableArray loadSavedQueriesForPageConfigurationNamed(String pageConfigurationName) {
		NSArray savedQueries = null;

		EOKeyValueArchiving.Support.setSupportForClass(newEOKVArchivingTimestampSupport, NSTimestamp._CLASS);

		try {
			savedQueries = (NSArray) userPreferences().valueForKey(userPreferenceNameForPageConfiguration(pageConfigurationName));
		} finally {
			EOKeyValueArchiving.Support.setSupportForClass(originalEOKVArchivingTimestampSupport, NSTimestamp._CLASS);
		}

		if (log.isDebugEnabled())
			log.debug("loadSavedQueriesForPageConfigurationNamed(" + pageConfigurationName + "): queries = " + savedQueries);

		return savedQueries == null ? new NSMutableArray() : savedQueries.mutableClone();
	}
	
	/**
	 * retrieves the saved queries for the given pageConfiguration and returns a
	 * dictionary where the key is the name of the savedQuery and value is the
	 * savedQuery itself.
	 * 
	 * @param session
	 *            {@link WOSession} - to check if there is a session level
	 *            ERCoreUserPreferences set
	 * @param pageConfigurationName
	 *            {@link String}
	 * @return {@link NSDictionary} <br/>
	 *         key - {@link String} name of savedQuery <br/>
	 *         value - {@link SavedQuery}
	 */
	public static NSDictionary savedQueriesForPageConfigurationNamed(WOSession session, String pageConfigurationName) {
	    NSArray savedQueries = null;
	    
	    NSKeyValueCoding userPreferences = userPreferences(session);
	    
	    EOKeyValueArchiving.Support.setSupportForClass(newEOKVArchivingTimestampSupport, NSTimestamp._CLASS);
	 
	    try {
	        savedQueries = (NSArray) userPreferences.valueForKey(userPreferenceNameForPageConfiguration(pageConfigurationName));
	    } finally {
	        EOKeyValueArchiving.Support.setSupportForClass(originalEOKVArchivingTimestampSupport, NSTimestamp._CLASS);
	    }

	    if (log.isDebugEnabled())
	        log.debug("loadSavedQueriesForPageConfigurationNamed(" + pageConfigurationName + "): queries = " + savedQueries);
	    
	    if(savedQueries != null) {
	        return ERXArrayUtilities.arrayGroupedByKeyPath(savedQueries, "name", false, null);
	    }
	    else {
	        return NSDictionary.emptyDictionary();
	    }
	}
	

	public void saveQueriesForPageConfigurationNamed(NSArray queries, String pageConfigurationName) {
		if (log.isDebugEnabled())
			log.debug("saveQueriesForPageConfigurationNamed(" + pageConfigurationName + "): queries = " + queries);

		EOKeyValueArchiving.Support.setSupportForClass(newEOKVArchivingTimestampSupport, NSTimestamp._CLASS);

		try {
			userPreferences().takeValueForKey(queries, userPreferenceNameForPageConfiguration(pageConfigurationName));
		} finally {
			EOKeyValueArchiving.Support.setSupportForClass(originalEOKVArchivingTimestampSupport, NSTimestamp._CLASS);
		}
	}

	/** component does not synchronize variables */
	@Override
	public boolean synchronizesVariablesWithBindings() {
		return false;
	}

	@Override
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
	
	public NSDictionary queryParamsToFetchSavedQueryResults() {
	    NSMutableDictionary dict = new NSMutableDictionary();
	    dict.setObjectForKey(pageConfiguration(), RequestParams.PageConfiguration);
	    dict.setObjectForKey(selectedSavedQuery.name(), RequestParams.SavedQueryName);
	    dict.setObjectForKey(d2wContext().entity().name(), RequestParams.EntityName);
	    
	    return dict.immutableClone();
	}
	
	/**
	 * For this method to work properly, a valid action should be passed in as a binding for key: directActionNameToFetchSavedQueryResults
	 * @return {@link String} - link to the DA which knows how to fetch the query results given the saved query name
	 */
	public String fetchSavedQueryResultsLink() {
	    StringBuffer url = new StringBuffer();
	    // make a dummy context and nullify the appNumber.
	    // there is no other easy way to generate a url with no appNumber and sessionID
	    WOContext newContext = new WOContext(context().request());
	    newContext._url().setApplicationNumber("-1");
	    
	    // 1) get protocol + servername + port 
	    newContext.request()._completeURLPrefix(url, false, 0);
	    
	    // 2) prepare request params
	    NSMutableDictionary requestParams = new NSMutableDictionary();
	    requestParams.setObjectForKey(pageConfiguration(), RequestParams.PageConfiguration);
	    requestParams.setObjectForKey(d2wContext().entity().name(), RequestParams.EntityName);
	    try {
	        requestParams.setObjectForKey(URLEncoder.encode(selectedSavedQuery.name(), CharEncoding.UTF_8), RequestParams.SavedQueryName);
	    } catch(UnsupportedEncodingException e) {
	        log.warn("error generating bookmarkable url", e);
	    }
	    
	    // 3) generate the rest of the url directActionName + request params 
	    String directActionName = (String) d2wContext().valueForKey("directActionNameToFetchSavedQueryResults");
	    url.append(newContext.directActionURLForActionNamed(directActionName, requestParams));
	    
	    return url.toString();
	}
	
	/**
	 * @return {@link Boolean}
	 * <br>true, only if valid named query has been selected and the binding to the key: 'directActionNameToFetchSavedQueryResults' points to a valid
	 * directAction  <br>
	 * false, otherwise
	 */
	public boolean showBookmarkableQueryResultsLink() {
	    return selectedSavedQuery != null && d2wContext().valueForKey("directActionNameToFetchSavedQueryResults") != null;
	}
	 
	/**
	 * interface to organize the request params used in this class
	 * 
	 * @author rajaram
	 */
	public interface RequestParams {
	    public static final String PageConfiguration = "pconfig";
	    public static final String EntityName = "ename";
	    public static final String SavedQueryName = "qname";
	}
}
