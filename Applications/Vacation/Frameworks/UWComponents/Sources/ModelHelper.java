package com.uw.shared;

import java.util.*; 
import com.webobjects.foundation.*; 
import com.webobjects.eoaccess.*; 

public class ModelHelper extends Object {

    static private ModelHelper sharedInstance = null;


    // add this line to your application constructor
    // ModelHelper.sharedInstance().installEOModelAddedNotificationHandler();
    
    static public ModelHelper sharedInstance() { 
        if (sharedInstance == null) 
          { 
            sharedInstance = new ModelHelper(); 
          } 

        return sharedInstance; 
    } 


    public void installEOModelAddedNotificationHandler() { 

        Class arrClass [] = {NSNotification.class}; 
        NSNotificationCenter.defaultCenter().addObserver(this, new NSSelector("eoModelAddedNotificationHandler", arrClass), EOModelGroup.ModelAddedNotification, null);

    } 


    public void eoModelAddedNotificationHandler(NSNotification notification) 
        throws Exception 
    { 
        EOModel theModel = (EOModel)notification.object(); 
        setConnectionDictionaryForEOModel(theModel); 
    } 


    public void setConnectionDictionaryForAllEOModels() 
        throws Exception 
    { 
        Enumeration modelsEnum = EOModelGroup.defaultGroup().models().objectEnumerator(); 
        while (modelsEnum.hasMoreElements()) { 
            EOModel theModel = (EOModel)modelsEnum.nextElement(); 
            setConnectionDictionaryForEOModel(theModel); 
        } 
    } 


    // Sets the ConnectionDictionary from NSProperties 
    private void setConnectionDictionaryForEOModel(EOModel theModel) 
        throws Exception 
    {

        // get the app ConnectionDictionaryName
        String connectionDictionaryName = System.getProperty("ConnectionDictionaryName"); 
        if (connectionDictionaryName == null) { 
            throw new Exception("Could not find connection ConnectionDictionaryName in System Properties " + connectionDictionaryName);
        }

        // get the list of available dictionaries
        NSDictionary connectionDictionaries = NSPropertyListSerialization.dictionaryForString(System.getProperty("ConnectionDictionaries"));
        if (connectionDictionaries == null) { 
            throw new Exception("Could not find ConnectionDictionaries in System Properties " + connectionDictionaryName); 
        } 

        NSDictionary connectionDictionary = (NSDictionary)connectionDictionaries.objectForKey(connectionDictionaryName);

        if (connectionDictionary == null) { 
            throw new Exception("Could not find connection dictionary named " + connectionDictionaryName + " in the ConnectionDictionaries " + connectionDictionaries.allKeys() + " in user defaults");

        } 

        theModel.setConnectionDictionary(connectionDictionary); 

        System.out.println(theModel.name() + " using connection dictionary named " + connectionDictionaryName); 
    } 

} 
