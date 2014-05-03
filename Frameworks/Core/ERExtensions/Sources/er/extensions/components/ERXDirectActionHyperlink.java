/*
 * Copyright(C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions.components;

import java.util.Enumeration;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;

import er.extensions.crypting.ERXCrypto;
import er.extensions.eof.ERXEOEncodingUtilities;
import er.extensions.foundation.ERXStringUtilities;

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
 * [actionClass=<i>anActionClass</i>];directActionName=<i>aDirectActionName</i>;[entityNameSeparator=<i>aSeparator</i>;]
 * [relative=<i>aBoolean</i>;][shouldEncryptObjectFormValues=<i>aBoolean</i>;][objectsForFormValues=<i>anArray</i>;]
 * [bindingDictionary=<i>aDictionary</i>;][unencryptedBindingDictionary=<i>aDictionary</i>;]
 *
 * @binding actionClass direct action class to be used
 * @binding directActionName direct action name
 * @binding entityNameSeparator separator used when constructiong urls with encoded enterprise objects
 * @binding relative generates relative or absolute url
 * @binding shouldEncryptObjectFormValues boolean flag that tells if the primary keys
 *		of the enterprise objects should be encrypted using blowfish
 * @binding objectForFormValue an enterprise object to be encoded in the url
 * @binding objectsForFormValues array of enterprise objects to be encoded in the url
 * @binding bindingDictionary adds the key-value pairs to generated url as
 * 		form values, encrypting the values with blowfish.
 * @binding unencryptedBindingDictionary adds the key-value pairs to generated url as
 * 		form values
 */
public class ERXDirectActionHyperlink extends ERXStatelessComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    // Class instances -------------------------------------------------

    /** Key used to denote an adaptor prefix for a generated url string */
    // MOVEME: ERXWOUtilities
    public final static String ADAPTOR_PREFIX_MARKER="**ADAPTOR_PREFIX**";
    /** Key used to denote a suffix for a generated url string */ 
    // MOVEME: ERXWOUtilities
    public final static String SUFFIX_MARKER="**SUFFIX**";

    /** logging support */
    public static final Logger log = Logger.getLogger(ERXDirectActionHyperlink.class);

	
    // Constructor -------------------------------------------------
    /**
     * Public constructor
     * @param aContext a context
     */
    public ERXDirectActionHyperlink(WOContext aContext) {
        super(aContext);
    }

	
    // Component methods -------------------------------------------------

    /**
     * Cover method to return the binding: <b>entityNameSeparator</b>
     * The entity name separator is used when constructing URLs with enterprise objects encoded in the url.
     * This value default to the value defined in the system property <i>er.extensions.ERXDirectActionHyperlink.EntityNameSeparator</i> which defaults as well to the character '<pre>_</pre>'.
     * @return returns the value for binding: <b>entityNameSeparator</b>
     */
    public String entityNameSeparator() {
        String separator = (String)valueForBinding("entityNameSeparator");
        if (separator == null)
            separator = ERXEOEncodingUtilities.entityNameSeparator();
        return separator;
    }

    /**
     * Cover method to return the boolean value
     * of the binding: <b>relative</b>
     * Defaults to <code>true</code>.
     * @return returns if the generated url should be relative or not(absolute).
     */
    public boolean relative() {
        return booleanValueForBinding("relative", true);
    }

    /**
     * Cover method to return the boolean value
     * of the binding: <b>shouldEncryptObjectFormValues</b>
     * Defaults to <code>false</code>. 
     * @return returns if the encoded objects' primary keys
     *		should be encrypted or not.
     */
    public boolean shouldEncryptObjectFormValues() {
        return booleanValueForBinding("shouldEncryptObjectFormValues");
    }
    /**
     * Cover method to return the binding: <b>objectsForFormValues</b>
     * This is an array of objects to be encoded as form values.
     * @return returns bound array of objects to be encoded
     */
    public NSArray objectsForFormValues() {
        return(NSArray)valueForBinding("objectsForFormValues");
    }
    /**
     * Cover method to return the binding: <b>objectsForFormValue</b>
     * This is an enterprise object to be encoded as form values.
     * @return returns bound enterprise object to be encoded
     */
    public EOEnterpriseObject objectForFormValue() {
        return(EOEnterpriseObject)valueForBinding("objectForFormValue");
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
        if(hasBinding("objectsForFormValues") || hasBinding("objectForFormValue")) {
            objects = new NSMutableArray();
            if(objectsForFormValues() != null)
                objects.addObjectsFromArray(objectsForFormValues());
            if(objectForFormValue() != null)
                objects.addObject(objectForFormValue());
        }
        return objects != null ? objects : NSArray.EmptyArray;
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
        String directActionName = null;
        NSDictionary encryptedBindingDict   = null;
        NSDictionary unencryptedBindingDict = null;
        NSArray formValuesObjects = null;

        // Compose the direct action name from the bindings
        // Typically, something like "DirectActionClass/actionMethod".
        // Keep consistency with directActionName semantics as it is defined in directActionHref static method
        if(hasBinding("actionClass")) {
            StringBuilder daBuffer = new StringBuilder();
            daBuffer.append(valueForBinding("actionClass"));
            daBuffer.append('/');
            daBuffer.append(valueForBinding("directActionName"));
            directActionName = daBuffer.toString();
        } else {
            directActionName = (String)valueForBinding("directActionName");
        }

        if((directActionName == null) || (directActionName.length() == 0))
            throw new IllegalArgumentException("ERXDirectActionHyperlink: directActionName must be specified.");

        // Get the binding dictionaries
        // FIXME: Rename binding to encryptedBindingDictionary
        if(hasBinding("bindingDictionary"))
            encryptedBindingDict = (NSDictionary)valueForBinding("bindingDictionary");

        if(hasBinding("unencryptedBindingDictionary"))
            unencryptedBindingDict = (NSDictionary)valueForBinding("unencryptedBindingDictionary");

        // Get the objects to encode
        if(allObjectsForFormValues().count() > 0)
            formValuesObjects = allObjectsForFormValues();

        // Compose and return the final url
        return directActionHyperlink(context(),
                shouldEncryptObjectFormValues(), formValuesObjects, entityNameSeparator(),
		encryptedBindingDict, unencryptedBindingDict,
                application().name(), directActionName,
                relative(), null);
    }
	
    // Class methods -------------------------------------------------

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
        if(_applicationHostUrl ==null) {
            // FIXME: Should be: er.extensions.ERXApplicationHostURL
            _applicationHostUrl = System.getProperty("ERApplicationHostURL");
            if(_applicationHostUrl==null || _applicationHostUrl.length()==0)
                throw new RuntimeException("The ERApplicationHostURL default was empty -- please set it for the machine running the target application: it should look like http://mymachine.com");
        }
        return _applicationHostUrl;
    }

    public static String directActionHyperlink(WOContext context,
                boolean encryptEos, NSArray eos, String entityNameSeparator,
                NSDictionary encryptedDict, NSDictionary unencryptedDict,
                String appName, String daName,
                boolean relative, String suffix) {
        StringBuffer result = new StringBuffer(ADAPTOR_PREFIX_MARKER);
        result.append(WOApplication.application().applicationExtension());
        result.append('/');
        result.append(WOApplication.application().directActionRequestHandlerKey());
        result.append('/');
        result.append(daName);
        result.append('?');

        if(encryptedDict != null) {
            NSArray allKeys = encryptedDict.allKeys();
            for(Enumeration e = allKeys.objectEnumerator(); e.hasMoreElements();) {
                String key =(String)e.nextElement();
                String value = encryptedDict.objectForKey(key).toString();
                ERXStringUtilities.appendSeparatorIfLastNot('&', '?', result);
                result.append(key);
                result.append('=');
                result.append(ERXCrypto.crypterForAlgorithm(ERXCrypto.BLOWFISH).encrypt(value));
            }
        }

        if(unencryptedDict != null) {
            NSArray allKeys = unencryptedDict.allKeys();
            for(Enumeration e = allKeys.objectEnumerator(); e.hasMoreElements();) {
                String key =(String)e.nextElement();
                String value = unencryptedDict.objectForKey(key).toString();
                ERXStringUtilities.appendSeparatorIfLastNot('&', '?', result);
                result.append(key);
                result.append('=');
                result.append(value);
            }
        }

        if((eos != null) &&(eos.count() > 0)) {
            ERXStringUtilities.appendSeparatorIfLastNot('&', '?', result);
            result.append(ERXEOEncodingUtilities.encodeEnterpriseObjectsPrimaryKeyForUrl(eos, entityNameSeparator, encryptEos));
        }

        if(suffix != null)
            result.append(SUFFIX_MARKER);

        return completeURLFromString(result.toString(), context, appName, relative, suffix);
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
        if(s!=null && s.indexOf(ADAPTOR_PREFIX_MARKER)!=-1) {
            if(applicationName==null || applicationName.length()==0)
                throw new RuntimeException("completeURLFromString: found ADAPTOR_PREFIX_MARKER and no application name to replace it - original text:"+s);
            NSArray a=NSArray.componentsSeparatedByString(s, ADAPTOR_PREFIX_MARKER);
            // BIG ASSUMPTION : the target application must have the same suffix as this application
            String postFix=c.request().adaptorPrefix()+"/"+applicationName;
            s= a.componentsJoinedByString(relative ? postFix : applicationHostUrl()+postFix);
        }
        if(s!=null && s.indexOf(SUFFIX_MARKER)!=-1) {
            NSArray a=NSArray.componentsSeparatedByString(s, SUFFIX_MARKER);
            // BIG ASSUMPTION : the target application must have the same suffix as this application
            String postFix=suffix!=null ? suffix : "";
            s= a.componentsJoinedByString(postFix);
        }
        return s;
    }
}
