//
// ERXEOEncodingUtilities.java
// Project ERExtensions
//
// Created by max on Sun Sep 29 2002
//
package er.extensions;

import java.util.*;

import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;

public class ERXEOEncodingUtilities {

    /** logging support */
    public static final ERXLogger log = ERXLogger.getERXLogger(ERXEOEncodingUtilities.class);

    /**
     * Holds the default entity name separator
     * that is used when objects are encoded into urls.
     * Default value is: _
     */
    private static String EntityNameSeparator = "_";

    /** Key used in EOModeler to specify the encoded (or abbreviated) entity 
     * named used when encoding an enterprise-object is an url. 
     */
    public final static String EncodedEntityNameKey = "EncodedEntityName";

    private static boolean SpecifySeparatorInURL = true;
    
    private static boolean initialized;
    
    /** Class initialization */
    public synchronized static void init() {
        // Find out if the user has set properties different than the defaults
        // EntityNameSeparator
        String entityNameSep = System.getProperty("er.extensions.ERXEOEncodingUtilities.EntityNameSeparator");
        if ((entityNameSep != null) && (entityNameSep.length() > 0))
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

    public static NSArray enterpriseObjectsFromFormValues(EOEditingContext ec, NSDictionary formValues) {
        if (ec == null)
            throw new RuntimeException("Attempting to decode enterprise objects with null editing context.");
        return decodeEnterpriseObjectsFromFormValues(ec, formValues);
    }

    public static NSDictionary groupedEnterpriseObjectsFromFormValues(EOEditingContext ec, NSDictionary formValues) {
        NSArray formValueObjects = enterpriseObjectsFromFormValues(ec, formValues);
        return ERXArrayUtilities.arrayGroupedByKeyPath(formValueObjects, "entityName");
    }

    public static EOEnterpriseObject enterpriseObjectForEntityNamedFromFormValues(EOEditingContext ec, String entityName, NSDictionary formValues) {
        NSArray entityGroup = enterpriseObjectsForEntityNamedFromFormValues(ec, entityName, formValues);
        if (entityGroup.count() > 1)
            log.warn("Multiple objects for entity name: " + entityName + " expecting one. objects: " + entityGroup);
        return entityGroup.count() > 0 ? (EOEnterpriseObject)entityGroup.lastObject() : null;
    }

    public static NSArray enterpriseObjectsForEntityNamedFromFormValues(EOEditingContext ec, String entityName, NSDictionary formValues) {
        NSDictionary groups = groupedEnterpriseObjectsFromFormValues(ec, formValues);
	EOEntity entity = ERXEOAccessUtilities.entityNamed(ec, entityName);        
	NSMutableArray entityGroup = new NSMutableArray();
        if (entity != null && entity.isAbstractEntity()) {
            for (Enumeration e = ERXUtilities.allSubEntitiesForEntity(entity, false).objectEnumerator(); e.hasMoreElements();) {
                EOEntity subEntity = (EOEntity)e.nextElement();
                NSArray aGroup = (NSArray)groups.objectForKey(subEntity.name());
                if (aGroup != null)
                    entityGroup.addObjectsFromArray(aGroup);        
            }
        } else {
            entityGroup.addObjectsFromArray((NSArray)groups.objectForKey(entityName));            
        }
        return entityGroup != null ? entityGroup : NSArray.EmptyArray;
    }
    
    /** This dictionary contains the encoded entity names used in the defaultGroup */ 
    protected static NSMutableDictionary _encodedEntityNames = null;

    /**
     * This method encodes the entity name of the enterprise object
     * by searching in the default model group wether it can find
     * the key EncodedEntityNameKey in the user info dictionary.
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
    // FIXME: This code is not thread safe
    protected static final NSDictionary encodedEntityNames () {
        if (_encodedEntityNames == null) {
            _encodedEntityNames = new NSMutableDictionary ();
            NSArray models = (NSArray)EOModelGroup.defaultGroup ().models ();
            for (Enumeration en = models.objectEnumerator ();
                en.hasMoreElements ();) {
                NSArray entities = ((EOModel)en.nextElement ()).entities ();
                for (Enumeration entEn = entities.objectEnumerator ();
                    entEn.hasMoreElements ();) {
                    EOEntity entity = (EOEntity)entEn.nextElement ();
                    NSDictionary userInfo = entity.userInfo ();
                    String encodedEntityName = (String)entity.userInfo ().objectForKey (EncodedEntityNameKey);
                    if (encodedEntityName != null)
                        _encodedEntityNames.setObjectForKey (entity.name (), encodedEntityName);
                }
            }
        }

        return _encodedEntityNames;
    }

    /**
     * Decodes the encoded entity name.
     * @return the decoded entity name.
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
        return(NSDictionary)NSPropertyListSerialization.propertyListFromString("{"+elements.componentsJoinedByString(";")+"}");
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
     * name and the primary key.<br/>
     * <br/>
     * For example imagine that an array containing two User objects(pk 13 and 24)
     * and one Company object(pk 56) are passed to this method, null is passed in
     * for the separator which means the default seperator will be used which is
     * '_' and false is passed for encryption. Then the url that would be generated
     * would be: sep=_&User_1=13&User_2=24&Company_3=56<br/>
     * <br/>
     * If on the other hand let's say you use the _ character in entity names and you
     * want the primary keys encrypted then passing in the same array up above but with
     * "##" specified as the separator and true for the encrypt boolean would yield:
     * sep=##&User##E1=SOMEGARBAGE8723&User##E2=SOMEGARBAGE23W&Company##E3=SOMEGARBAGE8723
     * <br/>
     * Note that in the above encoding the seperator is always passed and the upper case
     * E specifies if the corresponding value should be decrypted.
     * Note: At the moment this method does not handle compound primary keys
     * <br/>
     * <b>EncodedEntityName</b><br/>
     * You can specify an abbreviation for the encoded entityName.
     * This is very useful when you don't want to disclose the internals of your application
     * or simply because the entity name is rather long.<br/>
     * To do this:
     * <ul>
     * <li>open EOModeler,</li>
     * <li>click on the entity you want to edit,</li>
     * <li>get the "Info Panel"</li>
     * <li>go to the "User Info" tab (the last tab represented by a book)</li>
     * <li>add a key named <b>EncodedEntityName</b> with the value you want</li>
     * </ul>
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
            String pk = ERXEOControlUtilities.primaryKeyStringForObject(eo);
            if(pk == null && eo instanceof ERXGeneratesPrimaryKeyInterface) {
                NSDictionary pkDict =((ERXGeneratesPrimaryKeyInterface)eo).primaryKeyDictionary(false);
                if(pkDict != null && pkDict.allValues().count() == 1)
                    pk = pkDict.allValues().lastObject().toString();
                else
                    log.warn("Attempting to use an eo: " + eo + " that implements GeneratesPrimaryKeyInterface that gave back: " + pkDict);
            }

            if(pk == null)
                throw new RuntimeException("Primary key is null for object: " + eo);

            // Get the EncodedEntityName of the object
            String encodedEntityName = entityNameEncode (eo);

            // Add the result to the list of encoded objects
            encoded.addObject(encodedEntityName + separator + (encrypt ? "E" : "") + c++ + "=" +(encrypt ? ERXCrypto.blowfishEncode(pk) : pk));
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
     *		NSArray containing the primary key of the object in either
     *		cleartext or encrypted format.
     * @return array of enterprise objects corresponding to the passed
     *		in form value array.
     */
    public static NSArray decodeEnterpriseObjectsFromFormValues(EOEditingContext ec, NSDictionary values) {
        if(log.isDebugEnabled()) log.debug("values = "+values);
        NSMutableArray encoded = new NSMutableArray();

        NSArray temp = (NSArray)values.objectForKey("sep");
        String separator = temp != null && temp.count() > 0 ?(String)temp.lastObject() : null;
        if(temp != null && temp.count() > 1)
            log.warn("Found multiple separators in form values: " + temp);
        if(separator == null)
            separator = entityNameSeparator();

            for(Enumeration e = values.keyEnumerator(); e.hasMoreElements();) {
                String key =(String)e.nextElement();
                if(key.indexOf(separator) != -1) {
                    boolean isEncrypted = key.indexOf(separator + "E") != -1;
                    String encodedEntityName = key.substring(0, key.indexOf(separator));
                    String entityName = entityNameDecode (encodedEntityName);

		    // FIXME: This needs to be made case insensitive
                    EOEntity entity = ERXEOAccessUtilities.entityNamed(ec, entityName);
                    if(entity != null) {
                        if(entity.primaryKeyAttributes().count() == 1) {
                            for(Enumeration ee =((NSArray)values.objectForKey(key)).objectEnumerator(); ee.hasMoreElements();) {
                                String value =(String)ee.nextElement();
                                if(isEncrypted) {
                                    value = ERXCrypto.blowfishDecode(value);
                                    if(value != null)
                                        value = value.trim();
                                }
                                if(value != null) {
                                    // ENHANCEME: Could just form a fault here seeing that we have the pk.
                                    EOEnterpriseObject eo =(EOEnterpriseObject)EOUtilities
                                        .objectMatchingKeyAndValue(ec, entity.name(),
                                        ((EOAttribute)entity.primaryKeyAttributes().lastObject()).name(), value);
                                    if(eo != null)
                                        encoded.addObject(eo);
                                } else {
                                    log.warn("Value null after blowfish decode: " 
                                                            + values.objectForKey(key));
                                }
                            }
                        } else {
                            log.warn("Entity: " + entity.name() + " has compound pk. Attributes: " 
                                                            + entity.primaryKeyAttributes());
                        }
                } else {                    
                    log.warn("Unable to find entity for name: " + entityName);
                }
            }
        }
        return encoded;
    }    
}
