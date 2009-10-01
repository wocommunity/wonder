package er.directtoweb;

/**
 * Created by IntelliJ IDEA.
 * User: dscheck
 * Date: Jun 27, 2007
 * Time: 7:44:35 PM
 * 
 */

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WODisplayGroup;
import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOObjectNotAvailableException;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOKeyValueArchiver;
import com.webobjects.eocontrol.EOKeyValueArchiving;
import com.webobjects.eocontrol.EOKeyValueUnarchiver;
import com.webobjects.eocontrol.EOSharedEditingContext;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSSelector;
import com.webobjects.foundation.NSTimestampFormatter;
import com.webobjects.foundation._NSUtilities;
import er.extensions.ERXArrayUtilities;
import er.extensions.ERXConstant;
import er.extensions.ERXEOAccessUtilities;
import er.extensions.ERXEOControlUtilities;
import er.extensions.ERXLogger;
import er.extensions.ERXProperties;
import er.extensions.ERXSelectorUtilities;
import er.extensions.ERXSession;

import java.text.ParseException;
import java.util.Enumeration;


public abstract class ERDSavedQueriesComponent extends WOComponent {
    public static final ERXLogger log = ERXLogger.getERXLogger(ERDSavedQueriesComponent.class);

    public static EOKeyValueArchiving.Support originalEOKVArchivingTimestampSupport = new EOKeyValueArchiving._TimestampSupport();
    public static EOKeyValueArchiving.Support newEOKVArchivingTimestampSupport = new ERDSavedQueriesComponent._TimestampSupport();

    /** @deprecated  use {@link #originalEOKVArchivingTimestampSupport} */
    public static EOKeyValueArchiving.Support originalEOKVArchiningTimestampSupport = originalEOKVArchivingTimestampSupport;
    /** @deprecated  use {@link #newEOKVArchivingTimestampSupport} */
    public static EOKeyValueArchiving.Support newEOKVArchiningTimestampSupport = newEOKVArchivingTimestampSupport;

    public ERDSavedQueriesComponent(WOContext context) {
        super(context);
    }

    private NSKeyValueCoding _userPreferences=null;

    protected NSKeyValueCoding userPreferences() {
        // Dynamically invoke ERCoreUserPreferences.userPreferences() so NeutralLook doesn't have to depend on ERCoreBusinessLogic
        if (_userPreferences == null) {
            NSSelector s = new NSSelector("userPreferences", new Class[]{});
            Class prefClass = _NSUtilities.classWithName("ERCoreUserPreferences");

            if (prefClass == null)
                throw new RuntimeException(getClass().getName()+" Requires ERCoreUserPreferences, make sure this project includes the ERCoreBusinessLogic.framework");
                
            _userPreferences = (NSKeyValueCoding) ERXSelectorUtilities.invoke(s, prefClass);
        }
        return _userPreferences;
    }

    public static class SavedQuery implements NSKeyValueCoding, EOKeyValueArchiving {
        private NSMutableDictionary dict = null;
        public final String NAME_KEY = "name";
        public final String QUERY_MIN_KEY = "queryMin";
        public final String QUERY_MAX_KEY = "queryMax";
        public final String QUERY_MATCH_KEY = "queryMatch";
        public final String QUERY_OPERATOR_KEY = "queryOperator";
        public final String QUERY_BINDINGS_KEY = "queryBindings";

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
        
        protected String logDictionary(String title, NSDictionary dictionary, String indentStr) {
            StringBuffer buf=new StringBuffer();
            
            buf.append("\r\n"+indentStr+"=========="+((title!=null)?title:"")+"==================\r\n");
            buf.append(indentStr+"Dictionary dump, count="+dictionary.count()+"\r\n");
            
            for (Enumeration anEnum=dictionary.keyEnumerator(); anEnum.hasMoreElements(); ) {
                String key=(String) anEnum.nextElement();
                Object value = dictionary.objectForKey(key);
                
                buf.append(indentStr+"key="+key);
                buf.append(", valueClass="+value.getClass().getName());
                buf.append(", toString = "+value.toString()+"\r\n");
                
                if (value instanceof NSDictionary)
                    logDictionary(null, (NSDictionary)value, indentStr+"    ");
            }
            buf.append(indentStr+"============================\r\n");
            
            return buf.toString();
        }
        
        private NSMutableDictionary _updateDisplayGroupForKey(WODisplayGroup displayGroup, String key, boolean clearFormFirst) {
            NSDictionary source=(NSDictionary) valueForKey(key);
            
            NSMutableDictionary destination = (NSMutableDictionary) displayGroup.valueForKey(key);
            
            if (log.isDebugEnabled()) log.debug("\r\nBEGIN key="+key+", destination.class="+destination.getClass().getName()+", destination.hashCode="+destination.hashCode()+", destination="+destination);
            
            if (clearFormFirst && ERXProperties.booleanForKeyWithDefault("er.neutral.ERDSavedQueriesComponent.removeAllObjectBeforeUpdating", true))
                destination.removeAllObjects();

            if (source != null) {
                if (log.isDebugEnabled()) log.debug("source != null, adding to destination.  source="+source);
                destination.addEntriesFromDictionary(source);
            }
            else {
                if (log.isDebugEnabled()) log.debug("source == null, NOT adding to destination.");
            }
            
            if (log.isDebugEnabled()) {
                log.debug("END key="+key+", destination.class="+destination.getClass().getName()+", destination="+destination+"\r\n");
                
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
            if (aName == null) aName="Untitled Saved Query";

            dict.setObjectForKey(aName, NAME_KEY);
        }
        
        public NSDictionary queryMin() {
            return (NSDictionary) dict.objectForKey(QUERY_MIN_KEY);
        }
        
        public void setQueryMin(NSDictionary queryMin) {
            if (queryMin == null) queryMin=NSDictionary.EmptyDictionary;
            
            dict.setObjectForKey(queryMin, QUERY_MIN_KEY);
        }

        public NSDictionary queryMax() {
            return (NSDictionary) dict.objectForKey(QUERY_MAX_KEY);
        }
        
        public void setQueryMax(NSDictionary queryMax) {
            if (queryMax == null) queryMax=NSDictionary.EmptyDictionary;

            dict.setObjectForKey(queryMax, QUERY_MAX_KEY);
        }

        public NSDictionary queryMatch() {
            return (NSDictionary) dict.objectForKey(QUERY_MATCH_KEY);
        }

        public void setQueryMatch(NSDictionary queryMatch) {
            if (queryMatch == null) queryMatch=NSDictionary.EmptyDictionary;

            log.debug(logDictionary("setQueryMatch", queryMatch, "==="));
            
            dict.setObjectForKey(queryMatch, QUERY_MATCH_KEY);
        }
        
        public NSDictionary queryOperator() {
            return (NSDictionary) dict.objectForKey(QUERY_OPERATOR_KEY);
        }

        public void setQueryOperator(NSDictionary queryOperator) {
            if (queryOperator == null) queryOperator=NSDictionary.EmptyDictionary;

            dict.setObjectForKey(queryOperator, QUERY_OPERATOR_KEY);
        }        

       public NSDictionary queryBindings() {
            return (NSDictionary) dict.objectForKey(QUERY_BINDINGS_KEY);
        }

        public void setQueryBindings(NSDictionary queryBindings) {
            if (queryBindings == null) queryBindings=NSDictionary.EmptyDictionary;

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
        
        public String toString() {
            return "SavedQuery: "+hashCode()+" dict="+dict;
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
            
            if (log.isDebugEnabled()) log.debug("encodeSharedEO, eo=" + eo + ",  encodedDict=" + encodedDict);
            
            return encodedDict;
        }

        private EOEnterpriseObject decodeEO(NSDictionary dictionary) {
            String entityName = (String)dictionary.objectForKey(SerializationKeys.EntityName);
            String pk = (String)dictionary.objectForKey(SerializationKeys.PrimaryKey);
            EOEntity entity = ERXEOAccessUtilities.entityNamed(null, entityName);
            EOAttribute primaryKeyAttribute;
            EOEnterpriseObject eo = null;
            if (entity != null) {
                primaryKeyAttribute = (EOAttribute)ERXArrayUtilities.firstObject(entity.primaryKeyAttributes());
                Object primaryKeyObject = pk;
                if (log.isDebugEnabled()) log.debug("decodeSerializedEOWithDictionary with dict: " + dictionary);

                if (primaryKeyAttribute != null && !String.class.getName().equals(primaryKeyAttribute.className())) {
                    primaryKeyObject = ERXConstant.integerForString(pk);
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
        
        // Recursively walk down the given dictionary and convert any values that are EOs to a dictionary with keys ('encodedSharedEO' or 'encodedEO', 'entityName', 'pk').
        protected NSMutableDictionary encodeDictionaryWithEOs(NSDictionary dictionary) {
            NSMutableDictionary theMutableDictionary = (NSMutableDictionary) ((dictionary instanceof NSMutableDictionary) ? dictionary : dictionary.mutableClone());
            
            for (Enumeration anEnum=theMutableDictionary.keyEnumerator(); anEnum.hasMoreElements(); ) {
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

        // Recursively walk down the given dictionary and convert any dictionaries with keys ('encodedSharedEO' or 'encodedEO', 'entityName', 'pk') back to the EO.
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
    
    // Replacement helper class to archive the NSTimestamp.  We need to remove the %z from the format string because
    // NSTimestampFormatter cant deal with the %z
    public static class _TimestampSupport extends EOKeyValueArchiving.Support {
         private static NSTimestampFormatter _formatter = new NSTimestampFormatter("%Y-%m-%d %H:%M:%S");
        
         public void encodeWithKeyValueArchiver(Object receiver, EOKeyValueArchiver archiver) {
             archiver.encodeObject(_formatter.format(receiver), "value");
         }

         public Object decodeObjectWithKeyValueUnarchiver(EOKeyValueUnarchiver unarchiver) {
             try {
                 return _formatter.parseObject((String)unarchiver.decodeObjectForKey("value"));
             } catch (ParseException exception) {
                 throw NSForwardException._runtimeExceptionForThrowable(exception);
             }
         }
     }
    
}
