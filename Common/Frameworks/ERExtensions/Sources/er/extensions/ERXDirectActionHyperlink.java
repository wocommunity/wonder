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

public class ERXDirectActionHyperlink extends WOComponent {

    public ERXDirectActionHyperlink(WOContext aContext) {
        super(aContext);
    }

    public final static String ADAPTOR_PREFIX_MARKER="**ADAPTOR_PREFIX**";
    public final static String SUFFIX_MARKER="**SUFFIX**";

    /** logging support */
    public static final Category cat = Category.getInstance(ERXDirectActionHyperlink.class);

    // DELETEME: Not needed
    protected String oneTime;
    public boolean synchronizesVariablesWithBindings() { return false; }
    public boolean isStateless() { return true; }

    public String entityNameSeparator() { return (String)valueForBinding("entityNameSeparator"); }
    public boolean shouldEncryptObjectFormValues() { return ERXUtilities.booleanValue(valueForBinding("shouldEncryptObjectFormValues")); }
    public NSArray objectsForFormValues() { return (NSArray)valueForBinding("objectsForFormValues"); }
    public EOEnterpriseObject objectForFormValue() { return (EOEnterpriseObject)valueForBinding("objectForFormValue"); }

    private static String _applicationHostUrl;
    public static String applicationHostUrl() {
        if (_applicationHostUrl ==null) {
            _applicationHostUrl = System.getProperty("ERApplicationHostURL");
            if (_applicationHostUrl ==null || _applicationHostUrl.length()==0)
                throw new RuntimeException("The ERApplicationHostURL default was empty -- please set it for the machine running the target application: it should look like http://mymachine.com");
        }
        return _applicationHostUrl;
    }

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


    public static final String DefaultEntityNameSeparator = "_";
    
    public static void appendSeparatorIfLastNot(char separator, char not, StringBuffer sb) {
        if (sb.charAt(sb.length() - 1) != not) sb.append(separator);
    }

    public static NSDictionary dictionaryOfFormValuesForEnterpriseObjects(NSArray eos, String separator, boolean encrypt){
        String base = encodeEnterpriseObjectsPrimaryKeyForUrl(eos, separator, encrypt);
        NSArray elements = NSArray.componentsSeparatedByString (base, "&");
        return (NSDictionary)NSPropertyListSerialization.propertyListFromString("{"+elements.componentsJoinedByString(";")+"}");
    }
    
    // Constructs a simple key-value pair for a url like: sep=_&BuyerUser_1=56 || sep=_&BuyerUser_E1=8T67H
    public static String encodeEnterpriseObjectPrimaryKeyForUrl(EOEnterpriseObject eo, String seperator, boolean encrypt) {
        return encodeEnterpriseObjectsPrimaryKeyForUrl(new NSArray(eo), seperator, encrypt);
    }

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

    /** @deprecated -- bad spelling */
    public static NSArray deccodeEnterpriseObjectsFromFormValues(EOEditingContext ec, NSDictionary values) {
        return decodeEnterpriseObjectsFromFormValues(ec, values);
    }
    public static NSArray decodeEnterpriseObjectsFromFormValues(EOEditingContext ec, NSDictionary values) {
        if (cat.isDebugEnabled()) cat.debug("values = "+values);
        NSMutableArray encoded = new NSMutableArray();
        NSArray temp = (NSArray)values.objectForKey("sep");
        String separator = temp != null && temp.count() > 0 ? (String)temp.lastObject() : null;
        if (temp != null && temp.count() > 1)
            cat.warn("Found multiple separators in form values: " + temp);
        if (separator == null) separator = DefaultEntityNameSeparator;
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

    public String stringForBinding(String binding) {
        Object v=valueForBinding(binding);
        return v!=null ? v.toString() : null;
    }

    public String href() {
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
        // FIXME: Ditch this
        if(hasBinding("id")){
            String v= stringForBinding ("id");
            if (v!=null && v.length()>0) {
               appendSeparatorIfLastNot('&', '?', result);
                result.append("id=");
                String eId = ERXCrypto.blowfishEncode(v).toString();
                result.append(eId);
            } else
                cat.error("Invalid id specified "+v);
        }
        // FIXME: Ditch this
        if (hasBinding("id2")) { // We should have a better way of passing args than id and id2!!
            //result.append('&'); // target=_top;
            String v= stringForBinding ("id2");
            if (v!=null && v.length()>0) {
                appendSeparatorIfLastNot('&', '?', result);
                result.append("id2=");
                String eId2 = ERXCrypto.blowfishEncode(v).toString();
                result.append(eId2);
            } else cat.error("Invalid id2 "+v);
        }
        // FIXME: Ditch this
        if (hasBinding("c") && canGetValueForBinding("c")) {
            appendSeparatorIfLastNot('&', '?', result);
            result.append("c=");
            String eCode = ERXCrypto.blowfishEncode((valueForBinding("c")).toString());
            result.append(eCode);
        }
        // FIXME: Ditch this
        if (hasBinding("loginMessageName") && canGetValueForBinding("loginMessageName")){
            appendSeparatorIfLastNot('&', '?', result);
            result.append("loginMessageName=");
            result.append(valueForBinding("loginMessageName"));
        }
        // FIMXE: Bad, need to get rid of this oneTime stuff.
        //If the OneTime is turned on, then add code and dateCreated keyValue pairs in the href.
        if( hasBinding("oneTime") && ((Integer)valueForBinding("oneTime")).intValue()==1){
            EOEditingContext ec = ERXExtensions.newEditingContext();
            EOClassDescription cd=EOClassDescription.classDescriptionForEntityName("OneTimeCode");
            ERXGenericRecord code=(ERXGenericRecord)cd.createInstanceWithEditingContext(ec,null);
            ec.insertObject(code);
            String expirationDate = (String)valueForBinding("expirationDate");
            if (expirationDate!=null) {
                code.takeValueForKey(valueForBinding("expirationDate"), "expirationDate");
            }
            ec.saveChanges();
            appendSeparatorIfLastNot('&', '?', result);
            result.append("oneTimeCode=");
            result.append(((Integer)code.valueForKey("code")).intValue());
        }
        if (allObjectsForFormValues().count() > 0) {
            appendSeparatorIfLastNot('&', '?', result);
            result.append(encodeEnterpriseObjectsPrimaryKeyForUrl(allObjectsForFormValues(), entityNameSeparator(), shouldEncryptObjectFormValues()));
        }
        result.append(SUFFIX_MARKER);
        return result.toString();
    }



}
