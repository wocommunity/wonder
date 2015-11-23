package er.extensions.eof;

import java.util.Enumeration;

import org.apache.log4j.Logger;

import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOModel;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSComparator;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSPropertyListSerialization;
import com.webobjects.foundation.NSTimestampFormatter;

import er.extensions.crypting.ERXCrypto;
import er.extensions.foundation.ERXArrayUtilities;
import er.extensions.foundation.ERXProperties;

/**
 * 
 * @property er.extensions.ERXEOEncodingUtilities.EntityNameSeparator
 * @property er.extensions.ERXEOEncodingUtilities.SpecifySeparatorInURL
 */
public class ERXEOEncodingUtilities {

    /** logging support */
    public static final Logger log = Logger.getLogger(ERXEOEncodingUtilities.class);

    /**
     * Holds the default entity name separator
     * that is used when objects are encoded into urls.
     * Default value is: _ and it must not equal 
     * <code>AttributeValueSeparator</code>
     */
    private static String EntityNameSeparator = "_";
    
    /**
     * Holds the attribute value separator used
     * when objects with compound keys are encoded into urls.
     * Its value is: .
     */
    //immutable to avoid changing encoding/decoding methods... 
    private static String AttributeValueSeparator = ".";

    /** Key used in EOModeler to specify the encoded (or abbreviated) entity 
     * named used when encoding an enterprise-object is an url. 
     */
    public final static String EncodedEntityNameKey = "EncodedEntityName";

    /** This dictionary contains the encoded entity names used in the defaultGroup */ 
    protected static NSMutableDictionary _encodedEntityNames = null;

    private static boolean SpecifySeparatorInURL = true;
    
    private static boolean initialized;
    
    
    /** Class initialization */
    public synchronized static void init() {
        // Find out if the user has set properties different than the defaults
        // EntityNameSeparator
        String entityNameSep = System.getProperty("er.extensions.ERXEOEncodingUtilities.EntityNameSeparator");
        if ((entityNameSep != null) && (entityNameSep.length() > 0) && !entityNameSep.equals(AttributeValueSeparator))
            setEntityNameSeparator(entityNameSep);
        
        // Specify separator in link ?
        setSpecifySeparatorInURL(ERXProperties.booleanForKeyWithDefault("er.extensions.ERXEOEncodingUtilities.SpecifySeparatorInURL", true));
        initialized = true;
    }
    
    public static void setSpecifySeparatorInURL(boolean specifySeparatorInURL) {
        SpecifySeparatorInURL = specifySeparatorInURL;
    }

    public static boolean isSpecifySeparatorInURL() {
        return SpecifySeparatorInURL;
    }

    public static void setEntityNameSeparator(String entityNameSeparator) {
        EntityNameSeparator = entityNameSeparator;
    }

    public static String entityNameSeparator() {
        if (!initialized) {
            init();
        }
        return EntityNameSeparator;
    }
    
    /**
     * Returns enterprise objects grouped by entity name. 
     * The specific encoding is specified in the method: <code>encodeEnterpriseObjectsPrimaryKeyForUrl</code>.
     * 
     * @param ec    the editing context to fetch the objects from
     * @param formValues    dictionary where the values are an
     *		encoded representation of the primary key values in either
     *		cleartext or encrypted format.
     * @return enterprise objects grouped by entity name
     */
    //DELETEME?: grouping objects is not this class' responsibility...
    public static NSDictionary groupedEnterpriseObjectsFromFormValues(EOEditingContext ec, NSDictionary formValues) {
        NSArray formValueObjects = decodeEnterpriseObjectsFromFormValues(ec, formValues);
        return ERXArrayUtilities.arrayGroupedByKeyPath(formValueObjects, "entityName");
    }
    
    /**
     * Returns the enterprise object fetched with decoded <code>formValues</code> from
     * <code>entityName</code>.
     * @param ec    the editing context to fetch the object from
     * @param entityName    the entity to fetch the object from
     * @param formValues    dictionary where the values are an
     *		encoded representation of the primary key values in either
     *		cleartext or encrypted format.
     * @return the enterprise object
     */
    public static EOEnterpriseObject enterpriseObjectForEntityNamedFromFormValues(EOEditingContext ec, String entityName, NSDictionary formValues) {
        NSArray entityGroup = enterpriseObjectsForEntityNamedFromFormValues(ec, entityName, formValues);
        if (entityGroup.count() > 1)
            log.warn("Multiple objects for entity name: " + entityName + " expecting one. objects: " + entityGroup);
        return entityGroup.count() > 0 ? (EOEnterpriseObject)entityGroup.lastObject() : null;
    }
    
    /**
     * Returns the enterprise objects fetched with decoded <code>formValues</code> from
     * <code>entityName</code>.
     * @param ec    the editing context to fetch the objects from
     * @param entityName    the entity to fetch the objects from
     * @param formValues    dictionary where the values are an
     *		encoded representation of the primary key values in either
     *		cleartext or encrypted format.
     * @return the enterprise objects
     */
    public static NSArray enterpriseObjectsForEntityNamedFromFormValues(EOEditingContext ec, String entityName, NSDictionary formValues) {
        NSArray formValueObjects = decodeEnterpriseObjectsFromFormValues(ec, formValues);
        NSDictionary groups = ERXArrayUtilities.arrayGroupedByKeyPath(formValueObjects, "entityName");
        EOEntity entity = ERXEOAccessUtilities.entityNamed(ec, entityName);
        NSMutableArray entityGroup = new NSMutableArray();
        if (entity != null && entity.isAbstractEntity()) {
            for (Enumeration e = ERXEOAccessUtilities.allSubEntitiesForEntity(entity, false).objectEnumerator(); e.hasMoreElements();) {
                EOEntity subEntity = (EOEntity)e.nextElement();
                NSArray aGroup = (NSArray)groups.objectForKey(subEntity.name());
                if (aGroup != null)
                    entityGroup.addObjectsFromArray(aGroup);
            }
        } else {
            entityGroup.addObjectsFromArray((NSArray)groups.objectForKey(entityName));
        }
        return entityGroup;
    }
    
    /**
     * This method encodes the entity name of the enterprise object
     * by searching in the default model group whether it can find
     * the key EncodedEntityNameKey in the user info dictionary.
     * @param eo    the enterprise object
     * @return the encoded entity name defaulting to the given eo's entityName
     */
    public static String entityNameEncode (EOEnterpriseObject eo) {
        // Get the EncodedEntityName of the object
        // Default to eo's entityName
        String encodedEntityName = eo.entityName();
        EOEntity entity = EOModelGroup.defaultGroup ().entityNamed (eo.entityName ());
        NSDictionary userInfo = entity.userInfo ();
        if (userInfo != null && userInfo.objectForKey (EncodedEntityNameKey) != null)
            encodedEntityName = (String)userInfo.objectForKey (EncodedEntityNameKey);
        return encodedEntityName;
    }

    /**
     * This method  constructs a dictionary with encoded
     * entity names as keys and entity names as values.
     * @return the shared dictionary containing encoded entity names.
     */
    // FIXME: (tuscland) Should we listen to model group notifications ?
    // If this method is called too early, we might not have all the entities in the model group,
    // but this case is rare.
    protected static final NSDictionary encodedEntityNames () {
    	if (_encodedEntityNames == null) {
    		synchronized(ERXEOEncodingUtilities.class) {
    			if(_encodedEntityNames == null) {
    				_encodedEntityNames = new NSMutableDictionary ();
    				NSArray models = EOModelGroup.defaultGroup().models();
    				for (Enumeration en = models.objectEnumerator ();
    				en.hasMoreElements ();) {
    					NSArray entities = ((EOModel)en.nextElement ()).entities ();
    					for (Enumeration entEn = entities.objectEnumerator ();
    					entEn.hasMoreElements ();) {
    						EOEntity entity = (EOEntity)entEn.nextElement ();
    						NSDictionary userInfo = entity.userInfo ();
    						if(userInfo != null) {
    							String encodedEntityName = (String)userInfo.objectForKey (EncodedEntityNameKey);
    							if (encodedEntityName != null)
    								_encodedEntityNames.setObjectForKey (entity.name (), encodedEntityName);
    						}
    					}
    				}
    			}
    		}
    	}

    	return _encodedEntityNames;
    }

    /**
     * Decodes the encoded entity name.
     * @param encodedName the encode name.
     * @return decoded entity name.
     */
    public static String entityNameDecode (String encodedName) {
        String entityName = encodedName;
        NSDictionary entityNames = encodedEntityNames ();
        synchronized (entityNames) {
            entityName = (String)entityNames.objectForKey (encodedName);
        }
        return entityName;
    }

    /**
     * Constructs the form values dictionary by first calling
     * the method <code>encodeEnterpriseObjectsPrimaryKeyForUrl</code>
     * and then using the results of that to construct the dictionary.
     * @param eos array of enterprise objects to be encoded in the url
     * @param separator to be used to separate entity names
     * @param encrypt flag to determine if the primary key
     *		of the objects should be encrypted.
     * @return dictionary containing all of the key value pairs where
     * 		the keys denote the entity names and the values denote
     *		the possibly encrypted primary keys.
     */
    public static NSDictionary dictionaryOfFormValuesForEnterpriseObjects(NSArray eos, String separator, boolean encrypt){
        String base = encodeEnterpriseObjectsPrimaryKeyForUrl(eos, separator, encrypt);
        NSArray elements = NSArray.componentsSeparatedByString(base, "&");
        return(NSDictionary)NSPropertyListSerialization.propertyListFromString("{"+elements.componentsJoinedByString(";")+";}");
    }

    /**
     * Simple cover method that calls the method: <code>
     * encodeEnterpriseObjectsPrimaryKeyForUrl</code> with
     * an array containing the single object passed in.
     * @param eo enterprise object to encode in a url.
     * @param seperator to be used for the entity name.
     * @param encrypt flag to determine if the primary key
     *		of the object should be encrypted.
     * @return url string containing the encoded enterprise
     * 		object plus the entity name seperator used.
     */
    public static String encodeEnterpriseObjectPrimaryKeyForUrl(EOEnterpriseObject eo, String seperator, boolean encrypt) {
        return encodeEnterpriseObjectsPrimaryKeyForUrl(new NSArray(eo), seperator, encrypt);
    }

    /**
     * Encodes an array of enterprise objects for use in a url. The
     * basic idea is is to have an entity name to primary key map that
     * makes it easy to retrieve at a later date. In addition the entity
     * name key will be able to tell if the value is an encrypted key or
     * not. In this way given a key value pair the object can be fetched
     * from an editing context given that at point you will know the entity
     * name and the primary key.
     * <p>
     * For example imagine that an array containing two User objects(pk 13 and 24)
     * and one Company object(pk 56) are passed to this method, null is passed in
     * for the separator which means the default seperator will be used which is
     * '_' and false is passed for encryption. Then the url that would be generated
     * would be: sep=_&amp;User_1=13&amp;User_2=24&amp;Company_3=56
     * <p>
     * If on the other hand let's say you use the _ character in entity names and you
     * want the primary keys encrypted then passing in the same array up above but with
     * "##" specified as the separator and true for the encrypt boolean would yield:
     * sep=##&amp;User##E1=SOMEGARBAGE8723&amp;User##E2=SOMEGARBAGE23W&amp;Company##E3=SOMEGARBAGE8723
     * <p>
     * Note that in the above encoding the seperator is always passed and the upper case
     * E specifies if the corresponding value should be decrypted.
     * Compound primary keys are supported, giving the following url:
     * sep=_&amp;EntityName_1=1.1&amp;EntityName_2=1.2
     * where <code>1.1</code> and <code>1.2</code> are the primary key values. Key values 
     * follow alphabetical  order for their attribute names, just like 
     * <code>ERXEOControlUtilities.primaryKeyArrayForObject</code>.
     * Note: At the moment the attribute value separator cannot be changed.
     * <h3>EncodedEntityName</h3>
     * You can specify an abbreviation for the encoded entityName.
     * This is very useful when you don't want to disclose the internals of your application
     * or simply because the entity name is rather long.
     * To do this:
     * <ul>
     * <li>open EOModeler,</li>
     * <li>click on the entity you want to edit,</li>
     * <li>get the "Info Panel"</li>
     * <li>go to the "User Info" tab (the last tab represented by a book)</li>
     * <li>add a key named <b>EncodedEntityName</b> with the value you want</li>
     * </ul>
     * 
     * @param eos array of enterprise objects to be encoded in the url
     * @param separator to be used between the entity name and a sequence number
     * @param encrypt indicates if the primary keys of the objects should be encrypted
     * @return encoding of the objects passed that can be used as parameters in a url.
     */
    // ENHANCEME: Could also place a sha hash of the blowfish key in the form values so we can know if we are using
    //		  the correct key for decryption.
    public static String encodeEnterpriseObjectsPrimaryKeyForUrl(NSArray eos, String separator, boolean encrypt) {
        // Array of objects to be encoded
        NSMutableArray encoded = new NSMutableArray();

        // If the separator is not specified, default to the one given at class init
        if (separator == null)
            separator = entityNameSeparator();

        // Add the separator if needed
        if (isSpecifySeparatorInURL())
            encoded.addObject ("sep=" + separator);

        int c = 1;
        // Iterate through the objects
        for(Enumeration e = eos.objectEnumerator(); e.hasMoreElements();) {
            EOEnterpriseObject eo =(EOEnterpriseObject)e.nextElement();
            
            // Get the primary key of the object
            NSArray pkValues = ERXEOControlUtilities.primaryKeyArrayForObject(eo);
            if( pkValues == null && eo instanceof ERXGeneratesPrimaryKeyInterface) {
                NSDictionary pkDict = ((ERXGeneratesPrimaryKeyInterface)eo).primaryKeyDictionary(false);
                if( pkDict != null )
                    pkValues = pkDict.allValues();
            }
            if(pkValues == null)
                throw new RuntimeException("Primary key is null for object: " + eo);
            String pk = pkValues.componentsJoinedByString( AttributeValueSeparator );
            
            // Get the EncodedEntityName of the object
            String encodedEntityName = entityNameEncode (eo);

            // Add the result to the list of encoded objects
            encoded.addObject(encodedEntityName + separator + (encrypt ? "E" : "") + c++ + "=" +
                              (encrypt ? ERXCrypto.crypterForAlgorithm(ERXCrypto.BLOWFISH).encrypt(pk) : pk));
        }

        // Return the result as an url-encoded string
        return encoded.componentsJoinedByString("&");
    }

    /**
     * Decodes all of the objects for a given set of form values in
     * the given editing context. The object encoding is very simple,
     * just a generic entity name primary key pair where the key is
     * potentially encrypted using blowfish. The specific encoding is
     * specified in the method: <code>encodeEnterpriseObjectsPrimaryKeyForUrl
     * </code>.
     * @param ec editingcontext to fetch the objects from
     * @param values form value dictionary where the values are an
     *		encoded representation of the primary key values in either
     *		cleartext or encrypted format.
     * @return array of enterprise objects corresponding to the passed
     *		in form values.
     */
    public static NSArray decodeEnterpriseObjectsFromFormValues(EOEditingContext ec, NSDictionary values) {
        if (ec == null)
            throw new IllegalArgumentException("Attempting to decode enterprise objects with null editing context.");
        if (values == null )
            throw new IllegalArgumentException("Attempting to decode enterprise objects with null form values.");
        if(log.isDebugEnabled()) log.debug("values = "+values);
        NSMutableArray result = new NSMutableArray();

        String separator = values.objectForKey( "sep" ) != null ? (String) values.objectForKey( "sep" ) : entityNameSeparator();

            for(Enumeration e = values.keyEnumerator(); e.hasMoreElements();) {
                Object o = e.nextElement();
                String key =(String)o;
                if(key.indexOf(separator) != -1) {
                    boolean isEncrypted = key.indexOf(separator + "E") != -1;
                    String encodedEntityName = key.substring(0, key.indexOf(separator));
                    String entityName = entityNameDecode (encodedEntityName);
                    entityName = entityName == null ? encodedEntityName : entityName;
                    EOEntity entity = ERXEOAccessUtilities.entityNamed(ec, entityName);
                    if(entity != null) {
                        NSDictionary pk = processPrimaryKeyValue( (String) values.objectForKey(key), entity, isEncrypted );
                        result.addObject
                            ( EOUtilities.objectWithPrimaryKey(ec, entity.name(), pk) );
                    } else {                    
                        log.warn("Unable to find entity for name: " + entityName);
                }
            }
        }
        return result;
    }    
    
    /**
     * Generates an NSDictionary representing primary key values, both simple and compound. If values are encrypted we try to 
     * create the correct attribute value type. Supported types are: strings, numbers, timestamps and custom 
     * attributes with a factory method using a string argument.
     * 
     * @param value the primary key value, either a single value or a collection
     * @param entity    the entity used to gather primary key information
     * @param isEncrypted yes/no
     * @return a dictionary with primary key values
     */
    private static NSDictionary processPrimaryKeyValue( String value, EOEntity entity, boolean isEncrypted ) {
        NSArray pkAttributeNames = entity.primaryKeyAttributeNames();
        try {
            pkAttributeNames = pkAttributeNames.sortedArrayUsingComparator
            ( NSComparator.AscendingStringComparator );
        } catch( NSComparator.ComparisonException ex ) {
            log.error( "Unable to sort attribute names: "+ ex );
            throw new NSForwardException(ex);
        }
        NSArray values = isEncrypted
            ? NSArray.componentsSeparatedByString( ERXCrypto.crypterForAlgorithm(ERXCrypto.BLOWFISH).decrypt(value).trim(), AttributeValueSeparator )
            : NSArray.componentsSeparatedByString( value, AttributeValueSeparator );  
        int attrCount = pkAttributeNames.count();
        NSMutableDictionary result = new NSMutableDictionary( attrCount );
        for( int i = 0; i < attrCount; i++ ) {
            String currentAttributeName = (String)pkAttributeNames.objectAtIndex( i );
            EOAttribute currentAttribute = entity.attributeNamed( currentAttributeName );
            Object currentValue = values.objectAtIndex( i );
            switch ( currentAttribute.adaptorValueType() ) {
                case 3:
                    NSTimestampFormatter tsf = new NSTimestampFormatter();
                    try {
                        currentValue = tsf.parseObject( (String) currentValue );    
                    } catch( java.text.ParseException ex ) {
                        log.error( "Error while trying to parse: "+currentValue );
                        throw new NSForwardException( ex );
                    }
                    case 1:
                        if( currentAttribute.valueFactoryMethodName() != null ) {
                            currentValue = currentAttribute.newValueForString( (String) currentValue );
                        }
                    case 0:
                        currentValue = new java.math.BigDecimal( (String) currentValue );
            }
            result.setObjectForKey( currentValue, currentAttributeName );
        }
        return result;
    }
    
}
