/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions;

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import com.webobjects.appserver.*;
import java.util.*;
import java.io.*;
import java.lang.reflect.*;
import org.apache.log4j.*;

/////////////////////////////////////////////////////////////////////////////////////////////////////
// Configuration Manager
//
//     This object handles swizzling of the EOModel connection dictionaries
//
//   used to handle user default functionality rendered obsolete by the properties system
//   as of WO 5.1 -- instead use ~/WebObjects.properties
//
//
//
// Changing the connection dictionary.
//	To do this for Oracle you can either specify on a per model basis or on a global basis.
//
//	Global:
//		dbConnectServerGLOBAL = myDatabaseServer
//		dbConnectUserGLOBAL = me
//		dbConnectPasswordGLOBAL = secret
//	Per Model for say model ER:
//		ER.DBServer = myDatabaseServer
//		ER.DBUser = me
//		ER.DBPassword = secret
//
//
//
// Openbase: same, with DBDatabase and DBHostname
//
// JDBC: same with urlGlobal, or db.url
//
/////////////////////////////////////////////////////////////////////////////////////////////////////

public class ERXConfigurationManager {

    /////////////////////////////////////  log4j category  ////////////////////////////////////
    public static final Category cat = Category.getInstance("er.extensions.ConfigurationManager");

    static ERXConfigurationManager defaultManager=null;
    
    public static void initializeDefaults() {
        try {
            defaultManager();
        } catch (Throwable e) {
            // Too early to call WOApplication.application()
            System.err.println("********* Caught exception trying to initialize Configuration Manager : "+e);
            e.printStackTrace(System.err);
            throw new RuntimeException(e.toString());
        }
    }
    
    private ERXConfigurationManager() {
        NSNotificationCenter.defaultCenter().addObserver(this,
                                                         new NSSelector("modelAddedHandler", ERXConstant.NotificationClassArray),
                                                         EOModelGroup.ModelAddedNotification,
                                                         null);
        
    }

    /*
     return the single instance of this class
     */
    public static ERXConfigurationManager defaultManager() {
        if (defaultManager==null)
            defaultManager=new ERXConfigurationManager();
        return defaultManager;
    }
    
    public String stringForKey(String key) { return System.getProperty(key); }

    public void modelAddedHandler(NSNotification n) {
        resetConnectionDictionaryInModel((EOModel)n.object());
    }
    
    /* reset the connection dictionary to the specified values that are in the defaults.
	This method will look for defaults in the form 
		<MODELNAME>.DBServer
		<MODELNAME>.DBUser
		<MODELNAME>.DBPassword
		<MODELNAME>.URL (for JDBC)        
        if the serverName and username both exists, we overwrite the connection dict
           (password is optional). Otherwise we fall back to what's in the model.
    */
    public void resetConnectionDictionaryInModel(EOModel aModel)  {
        if(aModel!=null) {
            String aModelName=aModel.name();
            cat.debug("Adjusting "+aModelName);
            if (aModel.adaptorName().indexOf("Oracle")!=-1) {
                String serverName= stringForKey(aModelName + ".DBServer");
                serverName=serverName==null ? stringForKey("dbConnectServerGLOBAL") : serverName;
                String userName= stringForKey(aModelName + ".DBUser");
                userName= userName ==null ? stringForKey("dbConnectUserGLOBAL") : userName;
                String passwd= stringForKey(aModelName + ".DBPassword");
                passwd= passwd ==null ? stringForKey("dbConnectPasswordGLOBAL") : passwd;

                if((serverName!=null) || (userName!=null) || (passwd!=null)) {
                    NSMutableDictionary dict=new NSMutableDictionary(aModel.connectionDictionary());
                    if (serverName!=null) dict.setObjectForKey(serverName,"serverId");
                    if (userName!=null) dict.setObjectForKey(userName,"userName");
                    if (passwd!=null) dict.setObjectForKey(passwd,"password");
                    aModel.setConnectionDictionary(dict);
                    if (cat.isDebugEnabled()) cat.debug("New Connection Dictionary "+dict);
                }
                
            } else if (aModel.adaptorName().indexOf("Flat")!=-1) {
                String path= stringForKey(aModelName + ".DBPath");
                path = path ==null ? stringForKey("dbConnectPathGLOBAL") : path;
                if (path!=null) {                    
                    if (path.indexOf(" ")!=-1) {
                        NSArray a=NSArray.componentsSeparatedByString(path," ");
                        //System.out.println("found "+a);
                        if (a.count()==2) {
                            path =WOApplication.application().resourceManager().pathForResourceNamed((String)a.objectAtIndex(0),
                                                                                                    (String)a.objectAtIndex(1),
                                                                                                     null);
                            //System.out.println("path= "+path);
                        }
                    }
                } else {
                    // by default we take <modelName>.db in the directory we found the model
                    path=aModel.path();
                    path=NSPathUtilities.stringByDeletingLastPathComponent(path);
                    path=NSPathUtilities.stringByAppendingPathComponent(path,aModel.name()+".db");                    
                }
                NSMutableDictionary dict=new NSMutableDictionary(aModel.connectionDictionary());
                if (path!=null) dict.setObjectForKey(path,"path");
                if (operatingSystem()==WindowsOperatingSystem) dict.setObjectForKey("\r\n","rowSeparator");
                aModel.setConnectionDictionary(dict);
                if (cat.isDebugEnabled()) cat.debug("New Connection Dictionary "+dict);
            } else if (aModel.adaptorName().indexOf("OpenBase")!=-1) {
                String db= stringForKey(aModelName + ".DBDatabase");
                db = db ==null ? stringForKey("dbConnectDatabaseGLOBAL") : db;
                if (db!=null) {
                    NSMutableDictionary newCD=new NSMutableDictionary(aModel.connectionDictionary());
                    newCD.setObjectForKey(db, "databaseName");
                    aModel.setConnectionDictionary(newCD);
                }
                String h= stringForKey(aModelName + ".DBHostName");
                h = h ==null ? stringForKey("dbConnectHostNameGLOBAL") : h;
                if (h!=null) {
                    NSMutableDictionary newCD=new NSMutableDictionary(aModel.connectionDictionary());
                    newCD.setObjectForKey(h, "hostName");
                    aModel.setConnectionDictionary(newCD);
                    if (cat.isDebugEnabled()) cat.debug("New Connection Dictionary "+newCD);
                }
            } else if (aModel.adaptorName().indexOf("JDBC")!=-1) {
                String url= stringForKey(aModelName + ".URL");
                url = url ==null ? stringForKey("dbConnectURLGLOBAL") : url;
                String userName= stringForKey(aModelName + ".DBUser");
                userName= userName ==null ? stringForKey("dbConnectUserGLOBAL") : userName;
                String passwd= stringForKey(aModelName + ".DBPassword");
                passwd= passwd ==null ? stringForKey("dbConnectPasswordGLOBAL") : passwd;
                String driver= stringForKey(aModelName + ".DBDriver");
                driver= driver ==null ? stringForKey("dbConnectDriverGLOBAL") : driver;
                String jdbcInfo= stringForKey(aModelName + ".DBJDBCInfo");
                jdbcInfo= jdbcInfo ==null ? stringForKey("dbConnectJDBCInfoGLOBAL") : jdbcInfo;
                String plugin= stringForKey(aModelName + ".DBPlugin");
                plugin= plugin ==null ? stringForKey("dbConnectPluginGLOBAL") : plugin;
                if (url!=null || userName!=null || passwd!=null || driver!=null || jdbcInfo!=null || plugin!=null) {
                    NSMutableDictionary newCD=new NSMutableDictionary(aModel.connectionDictionary());
                    if (url!=null) newCD.setObjectForKey(url, "URL");
                    if (userName!=null) newCD.setObjectForKey(userName,"username");
                    if (passwd!=null) newCD.setObjectForKey(passwd,"password");
                    if (driver!=null) newCD.setObjectForKey(driver,"driver");
                    if (jdbcInfo!=null) {
                        NSDictionary d=(NSDictionary)NSPropertyListSerialization.propertyListFromString(jdbcInfo);
                        if (d!=null)
                            newCD.setObjectForKey(d,"jdbc2Info");
                        else
                            newCD.removeObjectForKey("jdbc2Info");
                    }
                    if (plugin!=null) newCD.setObjectForKey(plugin,"plugin");                    
                    aModel.setConnectionDictionary(newCD);
                    if (cat.isDebugEnabled()) cat.debug("New Connection Dictionary for "+aModel.name()+": "+newCD);
                }
            }
            // based on an idea from Stefan Apelt <stefan@tetlabors.de>
            String f = stringForKey(aModelName + ".EOPrototypesFile");
            f = f ==null ? stringForKey("EOPrototypesFileGLOBAL") : f;
            if(f != null) {
                NSDictionary dict = (NSDictionary)NSPropertyListSerialization.propertyListFromString(ERXStringUtilities.stringFromResource(f, "", null));
                if(dict != null) {
                    if (cat.isDebugEnabled()) cat.debug("Adjusting prototypes from " + f);
                    EOEntity proto = aModel.entityNamed("EOPrototypes");
                    if (proto == null) {
                        cat.warn("No prototypes found in model named \"" + aModelName + "\", although the EOPrototypesFile default was set!");
                    } else {
                        aModel.removeEntity(proto);
                        proto = new EOEntity(dict, aModel);
                        proto.awakeWithPropertyList(dict);
                        aModel.addEntity(proto);
                    }
                }
            }
            String e = stringForKey(aModelName + ".EOPrototypesEntity");
            e = e ==null ? stringForKey("EOPrototypesEntityGLOBAL") : e;
            if(e != null) {
                EOEntity newproto = aModel.entityNamed(e);
                if (newproto == null) {
                    cat.warn("No prototypes found in model named \"" + aModelName + "\", although the EOPrototypesEntity default was set!");
                } else {
                    if (cat.isDebugEnabled()) cat.debug("Adjusting prototypes from " + e);
                    NSMutableDictionary dict = new NSMutableDictionary();
                    newproto.encodeIntoPropertyList(dict);
                    EOEntity proto = aModel.entityNamed("EOPrototypes");
                    if(proto != null)
                        aModel.removeEntity(proto);
                    aModel.removeEntity(newproto);
                    proto = new EOEntity(dict, aModel);
                    proto.awakeWithPropertyList(dict);
                    aModel.addEntity(proto);
                }
            }
        }
        
    }


    public final static int WindowsOperatingSystem=1;
    public final static int MacOSXOperatingSystem=2;
    public final static int SolarisOperatingSystem=3;
    public final static int UnknownOperatingSystem=3;

    private int _operatingSystem=0;
    public int operatingSystem() {
        if (_operatingSystem==0) {
            String osName=System.getProperty("os.name").toLowerCase();
            if (osName.indexOf("windows")!=-1) _operatingSystem=WindowsOperatingSystem;
            else if (osName.indexOf("solaris")!=-1) _operatingSystem=SolarisOperatingSystem;
            else if (osName.indexOf("macos")!=-1) _operatingSystem=MacOSXOperatingSystem;
            else _operatingSystem=UnknownOperatingSystem;
        }
        return _operatingSystem;
    }
    
}
