/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions;

import com.webobjects.directtoweb.*;
import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import com.webobjects.appserver.*;
import java.util.*;
import org.apache.log4j.Category;

/**
 * This component can be used for two things:<br/>
 * 1) Generating direct action urls for use in
 * components that are being e-mailed to people.
 * 2) Support for encoding enterprise objects in
 * the form values of generated urls.
 * At the moment this component still contains some
 * custy code that needs to be cleaned up before it
 * can really be used, like adding the .wo and .api files ;0.<br/>
 * <br/>
 * Synopsis:<br/>
 * actionClass=<i>anActionClass</i>;directActionName=<i>aDirectActionName</i>;[entityNameSeparator=<i>aSeparator</i>;]
 * [shouldEncryptObjectFormValues=<i>aBoolean</i>;][objectsForFormValues=<i>anArray</i>;]
 * [bindingDictionary=<i>aDictionary</i>;][unencryptedBindingDictionary=<i>aDictionary</i>;]
 *
 * @binding actionClass direct action class to be used
 * @binding directActionName direct action name
 * @binding entityNameSeparator string to use as the separator when encoding
 *		the enterprise objects
 * @binding shouldEncryptObjectFormValues boolean flag that tells if the primary keys
 *		of the enterprise objects should be encrypted using blowfish
 * @binding objectForFormValue an enterprise object to be encoded in the url
 * @binding objectsForFormValues array of enterprise objects to be encoded in the url
 * @binding bindingDictionary adds the key-value pairs to generated url as
 * 		form values, encrypting the values with blowfish.
 * @binding unencryptedBindingDictionary adds the key-value pairs to generated url as
 * 		form values
 */
public class ERXDirectActionHyperlink extends WOComponent {

    /** Key used to denote an adaptor prefix for a generated url string */
    // MOVEME: ERXWOUtilities
    public final static String ADAPTOR_PREFIX_MARKER="**ADAPTOR_PREFIX**";
    /** Key used to denote a suffix for a generated url string */ 
    // MOVEME: ERXWOUtilities
    public final static String SUFFIX_MARKER="**SUFFIX**";

    /** logging support */
    public static final Category cat = Category.getInstance(ERXDirectActionHyperlink.class);
    /**
     * Holds the default entity name separator
     * that is used when objects are encoded into urls.
     * Default value is: _
     */
    public static final String DefaultEntityNameSeparator = "_";

    /**
     * Public constructor
     * @param aContext a context
     */
    public ERXDirectActionHyperlink(WOContext aContext) {
        super(aContext);
    }

    /**
     * Component is stateless
     * @return true
     */
    public boolean isStateless() { return true; }

    /**
     * Cover method to return the binding: <b>entityNameSeparator</b>
     * The entity name separator is used when constructing 
     * @return returns the value for binding: <b>entityNameSeparator</b> 
     */
    public String entityNameSeparator() { return (String)valueForBinding("entityNameSeparator"); }

    /**
     * Cover method to return the boolean value
     * of the binding: <b>shouldEncryptObjectFormValues</b>
     * Defaults to <code>false</code>. 
     * @return returns if the encoded objects' primary keys
     *		should be encrypted or not.
     */
    public boolean shouldEncryptObjectFormValues() {
        return ERXUtilities.booleanValue(valueForBinding("shouldEncryptObjectFormValues"));
    }
    /**
     * Cover method to return the binding: <b>objectsForFormValues</b>
     * This is an array of objects to be encoded as form values.
     * @return returns bound array of objects to be encoded
     */
    public NSArray objectsForFormValues() {
        return (NSArray)valueForBinding("objectsForFormValues");
    }
    /**
     * Cover method to return the binding: <b>objectsForFormValue</b>
     * This is an enterprise object to be encoded as form values.
     * @return returns bound enterprise object to be encoded
     */
    public EOEnterpriseObject objectForFormValue() {
        return (EOEnterpriseObject)valueForBinding("objectForFormValue");
    }

    /** Holds the application host url */
    // MOVEME: This stuff might be better served if it was off of ERXApplication
    private static String _applicationHostUrl;
    /**
     * This returns the value stored in the system properties:
     * <b>ERApplicationHostURL</b> if this isn't set then a
     * runtime exception is thrown. This property should be of
     * the form: http://mymachine.com
     * @return the application host url that should be used when
     *		complete urls are generated.
     */
    // MOVEME: This stuff might be better served if it was off of ERXApplication
    public static String applicationHostUrl() {
        if (_applicationHostUrl ==null) {
            // FIXME: Should be: er.extensions.ERXApplicationHostURL
            _applicationHostUrl = System.getProperty("ERApplicationHostURL");
            if (_applicationHostUrl ==null || _applicationHostUrl.length()==0)
                throw new RuntimeException("The ERApplicationHostURL default was empty -- please set it for the machine running the target application: it should look like http://mymachine.com");
        }
        return _applicationHostUrl;
    }

    /**
     * This method is useful for completing urls that are being generated
     * in components that are going to be e-mailed to users. This method
     * has the ability to substitute different application names which
     * can be helpful if one application is generating the component, but
     * the action of the url points to a different application on the
     * same host.
     * @param s href string to be completed
     * @param c current context
     * @param applicationName to be substituted if ADAPTOR_PREFIX_MARKER
     *		is present
     * @param relative flag to indicate if the generated url should be
     *		relative or absolute in which case the applicationHostUrl
     * 		will be used
     * @param suffix string to be substitued if the SUFFIX_MARKER string
     *		is present
     * @return complete url after substitutions have been made
     */
    // MOVEME: ERXWOUtilities
    public static String completeURLFromString(String s,
                                               WOContext c,
                                               String applicationName,
                                               boolean relative,
                                               String suffix) {
        if (s!=null && s.indexOf(ADAPTOR_PREFIX_MARKER)!=-1) {
            if (applicationName==null || applicationName.length()==0)
                throw new RuntimeException("completeURLFromString: found ADAPTOR_PREFIX_MARKER and no application name to replace it - original text:"+s);
            NSArray a=NSArray.componentsSeparatedByString(s, ADAPTOR_PREFIX_MARKER);
            // BIG ASSUMPTION : the target application must have the same suffix as this application
            String postFix=c.request().adaptorPrefix()+"/"+applicationName;
            s= a.componentsJoinedByString(relative ? postFix : applicationHostUrl()+postFix);
        }
        if (s!=null && s.indexOf(SUFFIX_MARKER)!=-1) {
            NSArray a=NSArray.componentsSeparatedByString(s, SUFFIX_MARKER);
            // BIG ASSUMPTION : the target application must have the same suffix as this application
            String postFix=suffix!=null ? suffix : "";
            s= a.componentsJoinedByString(postFix);
        }
        return s;
    }

    /**
     * Utility method to append a character to a
     * StringBuffer is the last character is not
     * a certain character. Useful for determining
     * if you need to add an '&' to the end of a
     * form value string.
     * @param separator character to add to potentially
     *		add to the StringBuffer.
     * @param not character to test if the given
     *		StringBuffer ends in it.
     * @param sb StringBuffer to test and potentially
     *		append to.
     */
    // MOVEME: ERXStringUtilities
    public static void appendSeparatorIfLastNot(char separator, char not, StringBuffer sb) {
        if (sb.length() > 0 && sb.charAt(sb.length() - 1) != not)
            sb.append(separator);
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
    // MOVEME: All of this encrypting and decrypting should move to either ERXEOFUtilities or EOGenericRecordClazz
    public static NSDictionary dictionaryOfFormValuesForEnterpriseObjects(NSArray eos, String separator, boolean encrypt){
        String base = encodeEnterpriseObjectsPrimaryKeyForUrl(eos, separator, encrypt);
        NSArray elements = NSArray.componentsSeparatedByString (base, "&");
        return (NSDictionary)NSPropertyListSerialization.propertyListFromString("{"+elements.componentsJoinedByString(";")+"}");
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
    // MOVEME: EOGenericRecordClazz
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
     * For example imagine that an array containing two User objects (pk 13 and 24)
     * and one Company object (pk 56) are passed to this method, null is passed in
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
     * @param eos array of enterprise objects to be encoded in the url
     * @param separator to be used between the entity name and a sequence number
     * @param encrypt indicates if the primary keys of the objects should be encrypted
     * @return encoding of the objects passed that can be used as parameters in a url.
     */
    // ENHANCEME: Could also place a sha hash of the blowfish key in the form values so we can know if we are using
    //		  the correct key for decryption.
    // MOVEME: EOGenericRecordClazz
    public static String encodeEnterpriseObjectsPrimaryKeyForUrl(NSArray eos, String separator, boolean encrypt) {
        if (separator == null) separator = DefaultEntityNameSeparator;
        NSMutableArray encoded = new NSMutableArray("sep=" + separator);
        int c = 1;
        for (Enumeration e = eos.objectEnumerator(); e.hasMoreElements();) {
            EOEnterpriseObject eo = (EOEnterpriseObject)e.nextElement();
            String pk = ERXExtensions.primaryKeyForObject(eo);
            if (pk == null && eo instanceof ERXGeneratesPrimaryKeyInterface) {
                NSDictionary pkDict = ((ERXGeneratesPrimaryKeyInterface)eo).primaryKeyDictionary(false);
                if (pkDict != null && pkDict.allValues().count() == 1)
                    pk = pkDict.allValues().lastObject().toString();
                else
                    cat.warn("Attempting to use an eo: " + eo + " that implements GeneratesPrimaryKeyInterface that gave back: " + pkDict);
            }
            if (pk == null)
                throw new RuntimeException("Primary key is null for object: " + eo);
            encoded.addObject(eo.entityName() + separator + (encrypt ? "E" : "") + c++ + "=" + (encrypt ? ERXCrypto.blowfishEncode(pk) : pk));
        }
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
    // MOVEME: EOGenericRecordClazz
    public static NSArray decodeEnterpriseObjectsFromFormValues(EOEditingContext ec, NSDictionary values) {
        if (cat.isDebugEnabled()) cat.debug("values = "+values);
        NSMutableArray encoded = new NSMutableArray();
        NSArray temp = (NSArray)values.objectForKey("sep");
        String separator = temp != null && temp.count() > 0 ? (String)temp.lastObject() : null;
        if (temp != null && temp.count() > 1)
            cat.warn("Found multiple separators in form values: " + temp);
        if (separator == null) {
            cat.warn("Form value: sep not found, using default entity name separator: " + DefaultEntityNameSeparator);
            separator = DefaultEntityNameSeparator;  
        } 
        for (Enumeration e = values.keyEnumerator(); e.hasMoreElements();) {
            String key = (String)e.nextElement();
            if (key.indexOf(separator) != -1) {
                boolean isEncrypted = key.indexOf(separator + "E") != -1;
                String entityName = key.substring(0, key.indexOf(separator));
                cat.debug("Decoding entity named: " + entityName);
                // FIXME: This needs to be made case insensitive and should be getting the
                //        model group from the ec
                EOEntity entity = EOModelGroup.defaultGroup().entityNamed(entityName);
                if (entity != null) {
                    if (entity.primaryKeyAttributes().count() == 1) {
                        for (Enumeration ee = ((NSArray)values.objectForKey(key)).objectEnumerator(); ee.hasMoreElements();) {
                            String value = (String)ee.nextElement();
                            if (isEncrypted) {
                                value = ERXCrypto.blowfishDecode(value);
                                if (value != null)
                                    value = value.trim();
                            }
                            if (value != null) {
                                EOEnterpriseObject eo = (EOEnterpriseObject)EOUtilities.objectMatchingKeyAndValue(ec,
                                                                                               entity.name(),
    ((EOAttribute)entity.primaryKeyAttributes().lastObject()).name(),
                                                                                               value);
                                if (eo != null)
                                    encoded.addObject(eo);
                            } else {
                                cat.warn("Value null after blowfish decode: " + values.objectForKey(key));
                            }
                        }
                    } else {
                        cat.warn("Entity: " + entity.name() + " has compound pk. Attributes: " + entity.primaryKeyAttributes());
                    }
                } else {                    
                    cat.warn("Unable to find entity for name: " + entityName);
                }
            }
        }
        return encoded;
    }

    /**
     * Returns all of the objects to be encoded
     * in the form values. Collects those bound
     * to both 'objectsForFormValues' and
     * 'objectForFormValue' into a single array.
     * @return complete collection of objects to
     * 		be encoded in form values.
     */
    public NSArray allObjectsForFormValues() {
        NSMutableArray objects = null;
        if (hasBinding("objectsForFormValues") || hasBinding("objectForFormValue")) {
            objects = new NSMutableArray();
            if (objectsForFormValues() != null)
                objects.addObjectsFromArray(objectsForFormValues());
            if (objectForFormValue() != null)
                objects.addObject(objectForFormValue());
        }
        return objects != null ? objects : ERXConstant.EmptyArray;
    }

    /**
     * Retrives a given binding and if it is not null
     * then returns <code>toString</code> called on the
     * bound object.
     * @param binding to be resolved
     * @return resolved binding in string format
     */
    // MOVEME: Should move to ERXStatelessComponent and have this component subclass that
    // FIXME: Should be renamed stringValueForBinding
    public String stringForBinding(String binding) {
        Object v=valueForBinding(binding);
        return v!=null ? v.toString() : null;
    }

    /**
     * Generates an href for the given direct action based
     * on all of the bindings. Currently it generates an
     * absolute url starting with the key: ADAPTOR_PREFIX_MARKER.
     * Before this href can be really useful it needs to
     * be cleaned up.
     * @return href containing all of the specification from
     *		the bindings.
     */
    // FIXME: Lots of stuff to be fixed here.
    public String href() {
        //FIXME: Need to make this optional
        StringBuffer result=new StringBuffer(ADAPTOR_PREFIX_MARKER);
        result.append(".woa/wa/");
        // FIXME: Should make actionClass optional
        result.append(valueForBinding("actionClass"));
        result.append('/');
        result.append(valueForBinding("directActionName"));
        result.append('?'); 
        // FIXME: Rename binding to encryptedBindingDictionary
        if (hasBinding("bindingDictionary")) {
            NSDictionary bdgs = (NSDictionary)valueForBinding("bindingDictionary");
            if (bdgs != null) {
                NSArray allKeys = bdgs.allKeys();
                for (Enumeration e = allKeys.objectEnumerator();
                     e.hasMoreElements();) {
                    String key = (String)e.nextElement();
                    String value = bdgs.objectForKey(key).toString();
                    appendSeparatorIfLastNot('&', '?', result);
                    result.append(key);
                    result.append("=");
                    result.append(ERXCrypto.blowfishEncode(value));
                }
            }
        }
        if(hasBinding("unencryptedBindingDictionary")) {
            NSDictionary bdgs = (NSDictionary)valueForBinding("unencryptedBindingDictionary");
            if (bdgs != null) {
                NSArray allKeys = bdgs.allKeys();
                for (Enumeration e = allKeys.objectEnumerator();
                     e.hasMoreElements();) {
                    String key = (String)e.nextElement();
                    String value = bdgs.objectForKey(key).toString();
                    appendSeparatorIfLastNot('&', '?', result);
                    result.append(key);
                    result.append("=");
                    result.append(value);
                }
            }
        }
        if (allObjectsForFormValues().count() > 0) {
            appendSeparatorIfLastNot('&', '?', result);
            result.append(encodeEnterpriseObjectsPrimaryKeyForUrl(allObjectsForFormValues(), entityNameSeparator(), shouldEncryptObjectFormValues()));
        }
        // FIXME: Need to make this optional as well
        result.append(SUFFIX_MARKER);
        return result.toString();
    }
}
