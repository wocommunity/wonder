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

/** 
 * <code>Configuration Manager</code> handles rapid turnaround for 
 * system configuration as well as swizzling of the EOModel connection 
 * dictionaries. 
 * <p>
 * <strong>Placing configuration parameters</strong>
 * <p> 
 * You can provide the system configuration by the following ways:<br/>
 * Note: This is the standard way for WebObjects 5.x applications.
 * <ul>
 *   <li><code>Properties</code> file under the Reources group of the  
 *       application and framework project. 
 *       It's a {@link java.util.Properties} file and Project Wonder's 
 *       standard project templates include it. (The templates won't 
 *       be available on some platforms at this moment.)</li>
 * 
 *   <li><code>WebObjects.properties</code> under the user home directory; 
 *       same format to Properties. <br/> 
 *       Note that the user home directory depends on the user who 
 *       launch the application. They may change between the 
 *       developent and deployment time.</li>
 * 
 *   <li>Command line arguments<br/>
 *       For example: <code>-WOCachingEnabled false -com.webobjects.pid $$</code></br>
 *       Don't forget to put a dash "-" before the key.</li> 
 * </ul>
 * <p>
 * <strong>Loading order of the configuration parameters</strong>
 * <p>
 * When the application launches, configuration parameters will 
 * be loaded by the following order. ERXConfigurationManager trys 
 * to reload them by the exactly same order when one of those 
 * configuration files changes. 
 * <p>
 * 1. Properties in frameworks that the application links to<br/>
 * 2. Properties in the application<br/>
 * 3. WebObjects.properties under the home directory<br/>
 * 4. Command line arguments<br/>
 * <p>
 * If there is a conflicting parameter between the files and 
 * arguments, the latter one overrides the earlier one. 
 * <p>
 * Note that the order between frameworks does not seems 
 * to be specified. You should not put conflicting parameters 
 * between framework Properties files. On the other hand, 
 * the application Properties should be always loaded after 
 * all framework Properties are loaded. You can safely 
 * override parameters on the frameworks from the applications
 * Properties. 
 * 
 * 
 * 
 * <p>
 * <strong>Changing the connection dictionary</strong>
 * <p>
 * To do this for Oracle you can either specify on a per model basis 
 * or on a global basis.
 * <pre>
 * <strong>Global:</strong>
 * 		dbConnectServerGLOBAL = myDatabaseServer
 * 		dbConnectUserGLOBAL = me
 * 		dbConnectPasswordGLOBAL = secret
 * <strong>Per Model for say model ER:</strong>
 * 		ER.DBServer = myDatabaseServer
 * 		ER.DBUser = me
 * 		ER.DBPassword = secret
 * 
 * <strong>Openbase:</strong> same, with DBDatabase and DBHostname
 * 
 * <strong>JDBC:</strong> same with urlGlobal, or db.url
 * 
 * </pre>
 * <p>
 * Prototypes can be swapped globally or per model either by 
 * hydrating an archived prototype entity for a file or from 
 * another entity.
 */
public class ERXConfigurationManager {

    /** logging support */
    public static final ERXLogger log = ERXLogger.getERXLogger(ERXConfigurationManager.class);

    /** 
     * Notification posted when the configuration is updated.  
     * The Java system properties is the part of the configuration.
     */ 
    public static final String ConfigurationDidChangeNotification = "ConfigurationDidChangeNotification";        

    /** Configuration manager singleton */ 
    static ERXConfigurationManager defaultManager = null;
    
    private String[] _commandLineArguments; 
    private NSMutableArray _monitoredProperties;
    private boolean _isInitialized = false;
    private boolean _isRapidTurnAroundInitialized = false;

    /** Private constructor to prevent instantiation from outside the class */
    private ERXConfigurationManager() {
        /* empty */
    }

    /**
     * Returns the single instance of this class
     * 
     * @return the configuration manager
     */
    public static ERXConfigurationManager defaultManager() {
        if (defaultManager == null)
            defaultManager = new ERXConfigurationManager();
        return defaultManager;
    }
    
    /** 
     * Returns the command line arguments. 
     * {@link ERXApplication.main} sets this value. 
     * 
     * @return the command line arguments as a String[]
     * @see #setCommandLineArguments
     */
    public String[] commandLineArguments() {
        return _commandLineArguments;
    }
    
    /** 
     * Sets the command line arguments. 
     * {@link ERXApplication.main} will call this method 
     * when the application starts up. 
     * 
     * @see #commandLineArguments
     */
    public void setCommandLineArguments(String [] newCommandLineArguments) {
        _commandLineArguments = newCommandLineArguments;
    }

    /**
     * Initializes the configuration manager. 
     * The framework principal {@link ERXExtensions} calles 
     * this method when the ERExtensions framework is loaded. 
     */
    public void initialize() {
        if (! _isInitialized) {
            _isInitialized = true;
            NSNotificationCenter.defaultCenter().addObserver(this,
                    new NSSelector("modelAddedHandler", ERXConstant.NotificationClassArray),
                    EOModelGroup.ModelAddedNotification,
                    null);
        }
    }

    /**
     * Sets up the system for rapid turnaround mode. It will watch the 
     * changes on Properties files in application and framework bundles 
     * and WebObjects.properties under the home directory. 
     * Rapid turnaround mode will only be enabled if there are such 
     * files available and system has WOCaching disabled.
     */
    public void configureRapidTurnAround() {
        if (_isRapidTurnAroundInitialized)      return;

        _isRapidTurnAroundInitialized = true;
        
        if (WOApplication.application().isCachingEnabled()) {
            log.info("WOCachingEnabled is true. Disabling the raphidTurnAround for Properties files");
            return;
        }
        
        NSArray propertyPaths = ERXProperties.pathsForUserAndBundleProperties(/* logging */ true);
        _monitoredProperties = new NSMutableArray();
                 
        Enumeration e = propertyPaths.objectEnumerator();
        while (e.hasMoreElements()) {
            String path = (String) e.nextElement();
            try {
                ERXFileNotificationCenter.defaultCenter().addObserver(this,
                        new NSSelector("updateSystemProperties", ERXConstant.NotificationClassArray),
                        path);
                _monitoredProperties.addObject(path);
                log.debug("Registered: " + path);
            } catch (Exception ex) {
                log.error("An exception occured while registering the observer for the "
                            + "logging configuration file: " 
                            + ex.getClass().getName() + " " + ex.getMessage());
            }
        }
    }

    /** 
     * Updates the configuration from the current configuration and 
     * posts {@link #ConfigurationDidChangeNotification}. It also  
     * calls {@link ERXLogger.configureLogging} to reconfigure 
     * the logging system. 
     * <p>
     * The configuration files: Properties and WebObjects.properties 
     * files are reloaded to the Java system properties by the same 
     * order to the when the system starts up. Then the command line 
     * arguments will be applied to the properties again so that 
     * the configuration will be consistent during the application 
     * lifespan. 
     * <p>
     * This method is called when rapid turnaround is enabled and one 
     * of the configuration files changes.
     * 
     * @param  n NSNotification object for the event 
     */
    public synchronized void updateSystemProperties(NSNotification n) {
        _updateSystemPropertiesFromMonitoredProperties((File)n.object(), _monitoredProperties);
        _reinsertCommandLineArgumentsToSystemProperties(_commandLineArguments);
        ERXLogger.configureLogging(System.getProperties());
        
        NSNotificationCenter.defaultCenter().postNotification(ConfigurationDidChangeNotification, null);
    }

    private void _updateSystemPropertiesFromMonitoredProperties(File updatedFile, NSArray monitoredProperties) {
        if (monitoredProperties == null  ||  monitoredProperties.count() == 0)  return;
        
        String updatedFilePath = null;
        try {
            updatedFilePath = updatedFile.getCanonicalPath();
        } catch (IOException ex) {
            log.error(ex.toString());
            return; 
        }

        Properties systemProperties = System.getProperties();
        // Find the position of the updatedFile in the monitoredProperties list, 
        // then reload it and everything after it on the list. 
        for (int i = monitoredProperties.indexOfObject(updatedFilePath); 
                  0 <= i  &&  i < _monitoredProperties.count(); i++) {
            String monitoredPropertiesPath = (String) _monitoredProperties.objectAtIndex(i);
            Properties loadedProperty = ERXProperties.propertiesFromPath(monitoredPropertiesPath);
            ERXProperties.transferPropertiesFromSourceToDest(loadedProperty, systemProperties);
        }
    }

    private void _reinsertCommandLineArgumentsToSystemProperties(String[] commandLineArguments) {
        Properties commandLineProperties = ERXProperties.propertiesFromArgv(commandLineArguments);
        Properties systemProperties = System.getProperties(); 
        ERXProperties.transferPropertiesFromSourceToDest(commandLineProperties, systemProperties);
        log.info("Reinserted the command line arguments to the system properties.");
    }


    private String stringForKey(String key) { return System.getProperty(key); }

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
            log.debug("Adjusting "+aModelName);
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
                    if (log.isDebugEnabled()) log.debug("New Connection Dictionary "+dict);
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
                if (log.isDebugEnabled()) log.debug("New Connection Dictionary "+dict);
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
                    if (log.isDebugEnabled()) log.debug("New Connection Dictionary "+newCD);
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
                    if (log.isDebugEnabled()) log.debug("New Connection Dictionary for "+aModel.name()+": "+newCD);
                }
            }
            // based on an idea from Stefan Apelt <stefan@tetlabors.de>
            String f = stringForKey(aModelName + ".EOPrototypesFile");
            f = f ==null ? stringForKey("EOPrototypesFileGLOBAL") : f;
            if(f != null) {
                NSDictionary dict = (NSDictionary)NSPropertyListSerialization.propertyListFromString(ERXStringUtilities.stringFromResource(f, "", null));
                if(dict != null) {
                    if (log.isDebugEnabled()) log.debug("Adjusting prototypes from " + f);
                    EOEntity proto = aModel.entityNamed("EOPrototypes");
                    if (proto == null) {
                        log.warn("No prototypes found in model named \"" + aModelName + "\", although the EOPrototypesFile default was set!");
                    } else {
                        aModel.removeEntity(proto);
                        proto = new EOEntity(dict, aModel);
                        proto.awakeWithPropertyList(dict);
                        aModel.addEntity(proto);
                    }
                }
            }
            String e = stringForKey(aModelName + ".EOPrototypesEntity");
            // global prototype setting not supported yet
            //e = e ==null ? stringForKey("EOPrototypesEntityGLOBAL") : e;
            if(e != null) {
                // we look for the entity globally so we can have one prototype entity
                EOEntity newPrototypeEntity = aModel.modelGroup().entityNamed(e);
                if (newPrototypeEntity == null) {
                    log.warn("Prototype Entity named "+e+" not found in model "+aModel.name());
                } else {
                    if (log.isDebugEnabled()) log.debug("Adjusting prototypes to those from entity " + e);

                    EOEntity proto = aModel.entityNamed("EOPrototypes");
                    if(proto != null) aModel.removeEntity(proto);
                    
                    aModel.removeEntity(newPrototypeEntity);
                    newPrototypeEntity.setName("EOPrototypes");
                    aModel.addEntity(newPrototypeEntity);
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
