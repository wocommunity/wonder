/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOApplication;
import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOModel;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSBundle;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSNotification;
import com.webobjects.foundation.NSNotificationCenter;
import com.webobjects.foundation.NSPathUtilities;
import com.webobjects.foundation.NSPropertyListSerialization;
import com.webobjects.foundation.NSSelector;

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
 *   <li><code>Properties</code> file under the Resources group of the  
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
 *      dbConnectServerGLOBAL = myDatabaseServer
 *      dbConnectUserGLOBAL = me
 *      dbConnectPasswordGLOBAL = secret
 *      dbConnectPluginGLOBAL = Oracle
 * <strong>Per Model for say model ER:</strong>
 *      ER.DBServer = myDatabaseServer
 *      ER.DBUser = me
 *      ER.DBPassword = secret
 *      ER.DBPlugin = Oracle
 * 
 * <strong>Openbase:</strong> same, with DBDatabase and DBHostname
 * 
 * <strong>JDBC:</strong> same with dbConnectURLGLOBAL, or ER.DBURL
 * 
 * </pre>
 * <p>
 * Prototypes can be swapped globally or per model either by 
 * hydrating an archived prototype entity for a file or from 
 * another entity.
 */
public class ERXConfigurationManager {

    /** logging support */
    public static final Logger log = Logger.getLogger(ERXConfigurationManager.class);

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
     * If set, touching this path will be used to signal a change to properties files.
     */
    private static String propertiesTouchFile() {
        return ERXProperties.stringForKey("er.extensions.ERXConfigurationManager.PropertiesTouchFile");
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
     * {@link ERXApplication#main} sets this value. 
     * 
     * @return the command line arguments as a String[]
     * @see #setCommandLineArguments
     */
    public String[] commandLineArguments() {
        return _commandLineArguments;
    }
    
    /** 
     * Sets the command line arguments. 
     * {@link ERXApplication#main} will call this method 
     * when the application starts up. 
     * 
     * @see #commandLineArguments
     */
    public void setCommandLineArguments(String [] newCommandLineArguments) {
        _commandLineArguments = newCommandLineArguments;
    }

    /**
     * Initializes the configuration manager. 
     * The framework principal {@link ERXExtensions} calls 
     * this method when the ERExtensions framework is loaded. 
     */
    public void initialize() {
        if (! _isInitialized) {
            _isInitialized = true;
             loadOptionalConfigurationFiles();
             if(!ERXModelGroup.patchModelsOnLoad) {
                 NSNotificationCenter.defaultCenter().addObserver(this,
                         new NSSelector("modelAddedHandler", ERXConstant.NotificationClassArray),
                         EOModelGroup.ModelAddedNotification, null);
             }
       }
    }

    /**
     * Sets up the system for rapid turnaround mode. It will watch the changes
     * on Properties files in application and framework bundles and
     * WebObjects.properties under the home directory. Rapid turnaround mode
     * will only be enabled if there are such files available and system has
     * WOCaching disabled.
     */
    public void configureRapidTurnAround() {
        if (_isRapidTurnAroundInitialized)      return;

        _isRapidTurnAroundInitialized = true;
        
        boolean rapidTurnaround = true;
        
        if (WOApplication.application()!=null && WOApplication.application().isCachingEnabled()) {
            log.info("WOCachingEnabled is true. Disabling the rapid turnaround for Properties files");
            rapidTurnaround = false;
        }

        NSArray propertyPaths = ERXProperties.pathsForUserAndBundleProperties(/* logging */ true);
        _monitoredProperties = new NSMutableArray();

        for (Enumeration e = propertyPaths.objectEnumerator(); e.hasMoreElements();) {
            String path = (String) e.nextElement();

            _monitoredProperties.addObject(path);
            
            if (rapidTurnaround) {
                registerForFileNotification(path, "updateSystemProperties");
            }
        }
        
        if (!rapidTurnaround) {
            registerPropertiesTouchFiles();
        }
    }

    private void registerPropertiesTouchFiles() {
        String propertiesTouchFile = propertiesTouchFile();
        
        if (propertiesTouchFile != null) {
            String appNamePlaceHolder = "/{AppName}/";
            int appNamePlaceHolderIndex = propertiesTouchFile.lastIndexOf(appNamePlaceHolder);
            if (appNamePlaceHolderIndex == -1) {
                registerForFileNotification(propertiesTouchFile, "updateAllSystemProperties");
            }
            else {
                if (WOApplication.application() != null) {
                    StringBuffer appSpecificTouchFile = new StringBuffer();
                    
                    appSpecificTouchFile.append(propertiesTouchFile.substring(0, appNamePlaceHolderIndex + 1));
                    appSpecificTouchFile.append(WOApplication.application().name());
                    appSpecificTouchFile.append(propertiesTouchFile.substring(appNamePlaceHolderIndex + appNamePlaceHolder.length() - 1));
                    
                    registerForFileNotification(appSpecificTouchFile.toString(), "updateAllSystemProperties");
                }
                
                StringBuffer globalTouchFile = new StringBuffer();
                
                globalTouchFile.append(propertiesTouchFile.substring(0, appNamePlaceHolderIndex + 1));
                globalTouchFile.append(propertiesTouchFile.substring(appNamePlaceHolderIndex + appNamePlaceHolder.length()));
                
                registerForFileNotification(globalTouchFile.toString(), "updateAllSystemProperties");
            }            
        }
    }
    
    private void registerForFileNotification(String path, String callbackMethod) {
        try {
            ERXFileNotificationCenter.defaultCenter().addObserver(this,
                                                                  new NSSelector(callbackMethod, ERXConstant.NotificationClassArray),
                                                                  path);
            log.debug("Registered: " + path);
        } catch (Exception ex) {
            log.error("An exception occured while registering the observer for the "
                      + "logging configuration file: " 
                      + ex.getClass().getName() + " " + ex.getMessage());
        }            
    }

    /**
     * This will overlay the current system config files. It will then
     * re-load the command line args.
     */
    public void loadOptionalConfigurationFiles() {
        if (ERXProperties.optionalConfigurationFiles() != null
            && ERXProperties.optionalConfigurationFiles().count() > 0) {
            Properties systemProperties = System.getProperties();
            for (Enumeration configEnumerator = ERXProperties.optionalConfigurationFiles().objectEnumerator();
                 configEnumerator.hasMoreElements();) {
                String configFile = (String)configEnumerator.nextElement();
                File file = new File(configFile);
                if (file.exists()  &&  file.isFile()  &&  file.canRead()) {
                    try {
                        Properties props = ERXProperties.propertiesFromFile(file);
                        ERXProperties.transferPropertiesFromSourceToDest(props, systemProperties);
                        ERXSystem.updateProperties();
                    } catch (java.io.IOException ex) {
                        log.error("Unable to load optional configuration file: " + configFile, ex);
                    }
                }
            }
            if (_commandLineArguments != null  &&  _commandLineArguments.length > 0)
                _reinsertCommandLineArgumentsToSystemProperties(_commandLineArguments);
        }
    }
    
    /** 
     * Updates the configuration from the current configuration and 
     * posts {@link #ConfigurationDidChangeNotification}. It also  
     * calls {@link Logger#configureLogging} to reconfigure 
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
     * @param  n NSNotification object for the event (null means load all files)
     */
    public synchronized void updateSystemProperties(NSNotification n) {
        _updateSystemPropertiesFromMonitoredProperties(n != null ? (File)n.object() : null, _monitoredProperties);
        if (_commandLineArguments != null  &&  _commandLineArguments.length > 0) 
            _reinsertCommandLineArgumentsToSystemProperties(_commandLineArguments);
        
        ERXLogger.configureLoggingWithSystemProperties();
        
        NSNotificationCenter.defaultCenter().postNotification(ConfigurationDidChangeNotification, null);
    }
    
    public synchronized void updateAllSystemProperties(NSNotification notification) {
        updateSystemProperties(null);
    }
    
    /**
     * If updatedFile is null, all files are reread.
     */
    private void _updateSystemPropertiesFromMonitoredProperties(File updatedFile, NSArray monitoredProperties) {
        if (monitoredProperties == null  ||  monitoredProperties.count() == 0)  return;
        
        int firstDirtyFile = 0;
        
        if (updatedFile != null) {
            try {
                // Find the position of the updatedFile in the monitoredProperties list, 
                // so that we can reload it and everything after it on the list. 
                firstDirtyFile = monitoredProperties.indexOfObject(updatedFile.getCanonicalPath());
                if (firstDirtyFile < 0) {
                    return;
                }
            } catch (IOException ex) {
                log.error(ex.toString());
                return; 
            }
        }
        

        Properties systemProperties = System.getProperties();
        for (int i = firstDirtyFile; i < _monitoredProperties.count(); i++) {
            String monitoredPropertiesPath = (String) _monitoredProperties.objectAtIndex(i);
            Properties loadedProperty = ERXProperties.propertiesFromPath(monitoredPropertiesPath);
            ERXProperties.transferPropertiesFromSourceToDest(loadedProperty, systemProperties);
            ERXSystem.updateProperties();
        }
    }

    private void _reinsertCommandLineArgumentsToSystemProperties(String[] commandLineArguments) {
        Properties commandLineProperties = ERXProperties.propertiesFromArgv(commandLineArguments);
        Properties systemProperties = System.getProperties(); 
        ERXProperties.transferPropertiesFromSourceToDest(commandLineProperties, systemProperties);
        ERXSystem.updateProperties();
        log.debug("Reinserted the command line arguments to the system properties.");
    }

    /**
     * Called when a model is loaded. This will reset the connection
     * dictionary and insert the correct EOPrototypes if those are used
     * @param n notification posted when a model is loaded. The object is
     * 		the model.
     */
    public void modelAddedHandler(NSNotification n) {
        EOModel model = (EOModel)n.object();
        for (Enumeration enumerator = model.entities().objectEnumerator(); enumerator.hasMoreElements();) {
            EOEntity entity = (EOEntity) enumerator.nextElement();
            adjustLocalizedAttributes(entity);
        }
        resetConnectionDictionaryInModel(model);
    }
    

    protected void adjustLocalizedAttributes(EOEntity entity) {
        NSArray attributes = entity.attributes().immutableClone();
        NSArray classProperties = entity.classProperties().immutableClone();
        NSArray attributesUsedForLocking = entity.attributesUsedForLocking().immutableClone();
        if(attributes == null) attributes = NSArray.EmptyArray;
        if(classProperties == null) classProperties = NSArray.EmptyArray;
        if(attributesUsedForLocking == null) attributesUsedForLocking = NSArray.EmptyArray;
        NSMutableArray mutableAttributes = new NSMutableArray();
        NSMutableArray mutableClassProperties = new NSMutableArray();
        NSMutableArray mutableAttributesUsedForLocking = new NSMutableArray();
        if(attributes != null) {
            for(Enumeration e = attributes.objectEnumerator(); e.hasMoreElements(); ) {
                EOAttribute attribute = (EOAttribute)e.nextElement();
                boolean isClassProperty = classProperties.containsObject(attribute);
                boolean isUsedForLocking = attributesUsedForLocking.containsObject(attribute);
                NSArray languages = (NSArray)(attribute.userInfo() != null ? attribute.userInfo().objectForKey("ERXLanguages") : null);
                if(languages != null && languages.count() > 0) {
                    String name = attribute.name();
                    String columnName = attribute.columnName();
                    for (int i = 0; i < languages.count(); i++) {
                        EOAttribute copy = new EOAttribute();
                        String language = (String) languages.objectAtIndex(i);
                        String newName = name + "_" +language;
                        String newColumnName = columnName + "_" +language;
                        //columnName = columnName.replaceAll("_(\\w)$", "_" + language);
                        // NOTE: order is important here. To add the prototype,
                        // we need it in the entity and we need a name to add it there
                        copy.setName(newName);
                        entity.addAttribute(copy);
                        copy.setPrototype(attribute.prototype());
                        
                        copy.setColumnName(newColumnName);
                        copy.setExternalType(attribute.externalType());
                        copy.setValueType(attribute.valueType());
                        copy.setPrecision(attribute.precision());
                        copy.setAllowsNull(attribute.allowsNull());
                        copy.setClassName(attribute.className());
                        copy.setWidth(attribute.width());
                        copy.setScale(attribute.scale());
                        copy.setExternalType(attribute.externalType());
                        if(isClassProperty) {
                            mutableClassProperties.addObject(copy);
                        }
                        if(isUsedForLocking) {
                            mutableAttributesUsedForLocking.addObject(copy);
                        }
                    }
                    entity.removeAttribute(attribute);
                } else {
                    if(isClassProperty) {
                        mutableClassProperties.addObject(attribute);
                    }
                    if(isUsedForLocking) {
                        mutableAttributesUsedForLocking.addObject(attribute);
                    }
                }
            }
            entity.setAttributesUsedForLocking(mutableAttributesUsedForLocking);
            entity.setClassProperties(mutableClassProperties);
        }
    }

    
    private String getProperty(String key, String alternateKey, String defaultValue) {
        String value = ERXSystem.getProperty(key);
        if(value == null) {
            value = ERXSystem.getProperty(alternateKey); 
        }
        if(value == null) {
            value = defaultValue; 
        }
        return value;
    }
    
    private String getProperty(String key, String alternateKey) {
        return getProperty(key, alternateKey, null);
    }

    protected void fixOracleDictionary(EOModel model) {
        String modelName = model.name();
        String serverName = getProperty(modelName + ".DBServer", "dbConnectServerGLOBAL");
        String userName = getProperty(modelName + ".DBUser", "dbConnectUserGLOBAL");
        String passwd = getProperty(modelName + ".DBPassword", "dbConnectPasswordGLOBAL");

        NSMutableDictionary newConnectionDictionary = new NSMutableDictionary(model.connectionDictionary());
        if (serverName != null)
            newConnectionDictionary.setObjectForKey(serverName, "serverId");
        if (userName != null)
            newConnectionDictionary.setObjectForKey(userName, "userName");
        if (passwd != null)
            newConnectionDictionary.setObjectForKey(passwd, "password");
        model.setConnectionDictionary(newConnectionDictionary);
    }
    
    protected void fixFlatDictionary(EOModel model) {
        String aModelName = model.name();
        String path = getProperty(aModelName + ".DBPath", "dbConnectPathGLOBAL");
        if (path != null) {
            if (path.indexOf(" ") != -1) {
                NSArray a = NSArray.componentsSeparatedByString(path, " ");
                if (a.count() == 2) {
                    path = ERXFileUtilities.pathForResourceNamed((String) a.objectAtIndex(0), (String) a.objectAtIndex(1), null);
                }
            }
        } else {
            // by default we take <modelName>.db in the directory we
            // found the model
            path = model.pathURL().getFile();
            path = NSPathUtilities.stringByDeletingLastPathComponent(path);
            path = NSPathUtilities.stringByAppendingPathComponent(path, model.name() + ".db");
        }
        NSMutableDictionary newConnectionDictionary = new NSMutableDictionary(model.connectionDictionary());
        if (path != null)
            newConnectionDictionary.setObjectForKey(path, "path");
        if (operatingSystem() == WindowsOperatingSystem)
            newConnectionDictionary.setObjectForKey("\r\n", "rowSeparator");
        model.setConnectionDictionary(newConnectionDictionary);
    }
    
    protected void fixOpenBaseDictionary(EOModel model) {
        String aModelName = model.name();
        String db = getProperty(aModelName + ".DBDatabase", "dbConnectDatabaseGLOBAL");
        String h = getProperty(aModelName + ".DBHostName", "dbConnectHostNameGLOBAL");
        NSMutableDictionary newConnectionDictionary = new NSMutableDictionary(model.connectionDictionary());
        if (db != null)
            newConnectionDictionary.setObjectForKey(db, "databaseName");
        if (h != null)
            newConnectionDictionary.setObjectForKey(h, "hostName");
        model.setConnectionDictionary(newConnectionDictionary);
    }
    
    protected void fixJDBCDictionary(EOModel model) {
        String aModelName = model.name();
        
        boolean poolConnections = ERXJDBCAdaptor.useConnectionBroker();

        String url = getProperty(aModelName + ".URL", "dbConnectURLGLOBAL");
        String userName = getProperty(aModelName + ".DBUser", "dbConnectUserGLOBAL");
        String passwd = getProperty(aModelName + ".DBPassword", "dbConnectPasswordGLOBAL");
        String driver = getProperty(aModelName + ".DBDriver", "dbConnectDriverGLOBAL");
        String serverName = getProperty(aModelName + ".DBServer", "dbConnectServerGLOBAL");
        String h = getProperty(aModelName + ".DBHostName", "dbConnectHostNameGLOBAL");
        String jdbcInfo = getProperty(aModelName + ".DBJDBCInfo", "dbConnectJDBCInfoGLOBAL");

        // additional information used for ERXJDBCConnectionBroker
        NSMutableDictionary poolingDictionary = new NSMutableDictionary();
        if (poolConnections) {
            poolingDictionary.setObjectForKey(getProperty(aModelName + ".DBMinConnections", "dbMinConnectionsGLOBAL",
                    "1"), "minConnections");
            poolingDictionary.setObjectForKey(getProperty(aModelName + ".DBMaxConnections", "dbMaxConnectionsGLOBAL",
                    "20"), "maxConnections");
            poolingDictionary.setObjectForKey(getProperty(aModelName + ".DBLogPath", "dbLogPathGLOBAL",
                    "/tmp/ERXJDBCConnectionBroker_@@name@@_@@WOPort@@.log"), "logPath");
            poolingDictionary.setObjectForKey(getProperty(aModelName + ".DBConnectionRecycle", "dbConnectionRecycleGLOBAL", 
                    "1.0"), "connectionRecycle");
            poolingDictionary.setObjectForKey(getProperty(aModelName + ".DBMaxCheckout", "dbMaxCheckoutGLOBAL", 
                    "86400"), "maxCheckout");
            poolingDictionary.setObjectForKey(getProperty(aModelName + ".DBDebugLevel", "dbDebugLevelGLOBAL", 
                    "1"), "debugLevel");
        }

        NSDictionary jdbcInfoDictionary = null;
        if (jdbcInfo != null && jdbcInfo.length() > 0 && jdbcInfo.charAt(0) == '^') {
            String modelName = jdbcInfo.substring(1, jdbcInfo.length());
            EOModel modelForCopy = model.modelGroup().modelNamed(modelName);
            if (modelForCopy != null && modelForCopy != model) {
                jdbcInfoDictionary = (NSDictionary) modelForCopy.connectionDictionary().objectForKey("jdbc2Info");
            } else {
                log.warn("Unable to find model named \"" + modelName + "\"");
                jdbcInfo = null;
            }
        }

        String plugin = getProperty(aModelName + ".DBPlugin", "dbConnectPluginGLOBAL");

        // build the URL if we have a Postgresql plugin
        if ("Postgresql".equals(plugin) && ERXStringUtilities.stringIsNullOrEmpty(url)
                && !ERXStringUtilities.stringIsNullOrEmpty(serverName) && !ERXStringUtilities.stringIsNullOrEmpty(h)) {
            url = "jdbc:postgresql://" + h + "/" + serverName;
        }

        NSMutableDictionary newConnectionDictionary = new NSMutableDictionary(model.connectionDictionary());
        if (url != null)
            newConnectionDictionary.setObjectForKey(url, "URL");
        if (userName != null)
            newConnectionDictionary.setObjectForKey(userName, "username");
        if (passwd != null)
            newConnectionDictionary.setObjectForKey(passwd, "password");
        if (driver != null)
            newConnectionDictionary.setObjectForKey(driver, "driver");
        if (jdbcInfoDictionary != null) {
            newConnectionDictionary.setObjectForKey(jdbcInfoDictionary, "jdbc2Info");
        } else if (jdbcInfo != null) {
            NSDictionary d = (NSDictionary) NSPropertyListSerialization.propertyListFromString(jdbcInfo);
            if (d != null)
                newConnectionDictionary.setObjectForKey(d, "jdbc2Info");
            else
                newConnectionDictionary.removeObjectForKey("jdbc2Info");
        }
        if (plugin != null)
            newConnectionDictionary.setObjectForKey(plugin, "plugin");

        // set the information for ERXJDBCConnectionBroker
        newConnectionDictionary.addEntriesFromDictionary(poolingDictionary);
        
        String removeJdbc2Info = getProperty(aModelName + ".removeJdbc2Info", "dbRemoveJdbc2InfoGLOBAL", "false");
        if (ERXValueUtilities.booleanValue(removeJdbc2Info)) {
            newConnectionDictionary.removeObjectForKey("jdbc2Info");
        }

        model.setConnectionDictionary(newConnectionDictionary);
    }
    
    /**
     * Resets the connection dictionary to the specified values that are in the
     * defaults. This method will look for defaults in the form:
     * 
     * <pre><code>
     *  		&lt;MODELNAME&gt;.DBServer
     *  		&lt;MODELNAME&gt;.DBUser
     *  		&lt;MODELNAME&gt;.DBPassword
     *  		&lt;MODELNAME&gt;.URL (for JDBC)        
     * </code></pre>
     * 
     * if the serverName and username both exists, we overwrite the connection
     * dict (password is optional). Otherwise we fall back to what's in the
     * model.
     * 
     * Likewise default values can be specified of the form:
     * 
     * <pre><code>
     *  dbConnectUserGLOBAL
     *  dbConnectPasswordGLOBAL
     *  dbConnectURLGLOBAL
     * </code></pre>
     * 
     * @param model
     *            to be reset
     */
    public void resetConnectionDictionaryInModel(EOModel model)  {
        if(model == null) {
            throw new IllegalArgumentException("Model can't be null"); 
        }
        String modelName=model.name();
        log.debug("Adjusting "+modelName);
        NSDictionary old = model.connectionDictionary();

        if(model.adaptorName() == null) {
            log.info("Skipping model '" + modelName + "', it has no adaptor name set");
            return;
        }

        // Support for EODatabaseConfig from EntityModeler.  The value of YourEOModelName.DBConfigName is
        // used to lookup the corresponding EODatabaseConfig name from user info.  The connection dictionary
        // defined in the databaseConfig section completely replaces the connection dictionary in the 
        // EOModel. After the initial replacement, all the additional PW model configurations are then 
        // applied to the new dictionary.
        String databaseConfigName = getProperty(modelName + ".DBConfigName", "dbConfigNameGLOBAL");
        NSDictionary databaseConfig = null;
        if (databaseConfigName != null) {
            NSDictionary userInfo = model.userInfo();
            if (userInfo != null) {
                NSDictionary entityModelerDictionary = (NSDictionary) userInfo.objectForKey("_EntityModeler");
                if (entityModelerDictionary != null) {
                    NSDictionary databaseConfigsDictionary = (NSDictionary) entityModelerDictionary.objectForKey("databaseConfigs");
                    if (databaseConfigsDictionary != null) {
                        databaseConfig = (NSDictionary) databaseConfigsDictionary.objectForKey(databaseConfigName);
                        if (databaseConfig != null) {
                            NSDictionary connectionDictionary = (NSDictionary) databaseConfig.objectForKey("connectionDictionary");
                            model.setConnectionDictionary(connectionDictionary);
                        }
                    }
                }
            }
        }

        if (model.adaptorName().indexOf("Oracle") != -1) {
            fixOracleDictionary(model);
        } else if (model.adaptorName().indexOf("Flat") != -1) {
            fixFlatDictionary(model);
        } else if (model.adaptorName().indexOf("OpenBase")!=-1) {
            fixOpenBaseDictionary(model);
        } else if (model.adaptorName().indexOf("JDBC")!=-1) {
            fixJDBCDictionary(model);
        }

        if (log.isDebugEnabled() && !old.equals(model.connectionDictionary())) {
            NSMutableDictionary dict = model.connectionDictionary().mutableClone();
            if (dict.objectForKey("password") != null) {
                dict.setObjectForKey("<deleted for log>", "password");
                log.debug("New Connection Dictionary for " + modelName + ": " + dict);
            }
        }

        fixPrototypes(model, databaseConfig);

    }

    private void fixPrototypes(EOModel model, NSDictionary databaseConfig) {
        String modelName = model.name();
        // based on an idea from Stefan Apelt <stefan@tetlabors.de>
        String f = getProperty(modelName + ".EOPrototypesFile", "EOPrototypesFileGLOBAL");
        if (f != null) {
            NSDictionary dict = (NSDictionary) NSPropertyListSerialization
            .propertyListFromString(ERXStringUtilities.stringFromResource(f, "", null));
            if (dict != null) {
                if (log.isDebugEnabled())
                    log.debug("Adjusting prototypes from " + f);
                EOEntity proto = model.entityNamed("EOPrototypes");
                if (proto == null) {
                    log.warn("No prototypes found in model named \"" + modelName
                            + "\", although the EOPrototypesFile default was set!");
                } else {
                    model.removeEntity(proto);
                    proto = new EOEntity(dict, model);
                    proto.awakeWithPropertyList(dict);
                    model.addEntity(proto);
                }
            }
        }

        String prototypeEntityName = ERXSystem.getProperty(modelName + ".EOPrototypesEntity");
        if (prototypeEntityName == null && databaseConfig != null) {
            prototypeEntityName = (String) databaseConfig.objectForKey("prototypeEntityName");
        }

        if(prototypeEntityName == null && !(ERXModelGroup.patchModelsOnLoad())) {
            String pluginName = ERXEOAccessUtilities.guessPluginName(model);
            if (pluginName != null) {
                String pluginPrototypeEntityName = "EOJDBC" + pluginName + "Prototypes";
                // This check isn't technically necessary since
                // it doesn't down below, but since
                // we are guessing here, I don't want themt o
                // get a warning about the prototype not
                // being found if they aren't even using Wonder
                // prototypes.
                if (model.modelGroup().entityNamed(pluginPrototypeEntityName) != null) {
                    prototypeEntityName = pluginPrototypeEntityName;
                }
            }
        }

        // global prototype setting not supported yet
        // e = e ==null ? ERXSystem.getProperty("EOPrototypesEntityGLOBAL")
        // : e;
        if (prototypeEntityName != null) {
            // we look for the entity globally so we can have one prototype
            // entity
            EOEntity newPrototypeEntity = model.modelGroup().entityNamed(prototypeEntityName);
            if (newPrototypeEntity == null) {
                log.warn("Prototype Entity named " + prototypeEntityName + " not found in model " + model.name());
            } else {
                if (log.isDebugEnabled())
                    log.debug("Adjusting prototypes to those from entity " + prototypeEntityName);
                
                String finalPrototypeEntityName = "EO" + model.adaptorName() + "Prototypes";

                EOEntity finalPrototypeEntity = model.entityNamed(finalPrototypeEntityName);
                if(false) {
                    // additive prototype handling
                    if (finalPrototypeEntity == null) {
                        finalPrototypeEntity = new EOEntity();
                        finalPrototypeEntity.setName(finalPrototypeEntityName);
                        model.addEntity(finalPrototypeEntity);
                    }
                    for (Enumeration enumerator = newPrototypeEntity.attributes().objectEnumerator(); enumerator.hasMoreElements();) {
                    	EOAttribute attribute = (EOAttribute) enumerator.nextElement();
                    	if(attribute != null) {
                    		EOAttribute existing = finalPrototypeEntity.anyAttributeNamed(attribute.name());
                    		if(existing != null) {
                    			if(finalPrototypeEntity.anyAttributeNamed(existing.name()) != null) {
                    				finalPrototypeEntity.removeAttribute(existing);
                    			}
                    		}
                    		newPrototypeEntity.removeAttribute(attribute);
                    		finalPrototypeEntity.addAttribute(attribute);
                    	}
                    }
                } else {
                    // replacing prototype handling
                    if (finalPrototypeEntity != null) {
                        finalPrototypeEntity.model().removeEntity(finalPrototypeEntity);
                    }
                    newPrototypeEntity.setName(finalPrototypeEntityName);
                    model.removeEntity(newPrototypeEntity);
                    model.addEntity(newPrototypeEntity);
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
            String osName=ERXSystem.getProperty("os.name").toLowerCase();
            if (osName.indexOf("windows")!=-1) _operatingSystem=WindowsOperatingSystem;
            else if (osName.indexOf("solaris")!=-1) _operatingSystem=SolarisOperatingSystem;
            else if (osName.indexOf("macos")!=-1 || osName.indexOf("mac os")!=-1) _operatingSystem=MacOSXOperatingSystem;
            else _operatingSystem=UnknownOperatingSystem;
        }
        return _operatingSystem;
    }

    /**
     * Path to the web server's document root.
     * This implementation tries first to resolve the
     * <code>application.name()+ "DocumentRoot"</code> property value,
     * then the <code>ERXDocumentRoot</> property before
     * getting the <code>DocumentRoot</code> key in your WebServerConfig.plist in the
     * JavaWebObjects bundle.
     * @return to the web server's document root.
     */
    protected String documentRoot; 
    public String documentRoot() {
        if (documentRoot == null) {
            // for WebObjects.properties
            documentRoot = ERXProperties.stringForKey(WOApplication.application().name() + "DocumentRoot");
            if(documentRoot == null) {
                // for command line and Properties
                documentRoot = ERXProperties.stringForKey("ERXDocumentRoot");
                if(documentRoot == null) {
                    // default value
                    NSDictionary dict = ERXDictionaryUtilities.dictionaryFromPropertyList("WebServerConfig", NSBundle.bundleForName("JavaWebObjects"));
                    if(dict != null)
                        documentRoot = (String)dict.objectForKey("DocumentRoot");
                }
            }
        }
        return documentRoot;
    }

    /** holds the host name */
    protected String _hostName;

    /**
     * Gets the default host name for the current local host.
     * @return host name or UnknownHost if the host is unknown.
     */
    public String hostName() {
        if (_hostName == null) {
            try {
                _hostName = java.net.InetAddress.getLocalHost().getHostName();
            } catch (java.net.UnknownHostException ehe) {
                log.warn("Caught unknown host exception: " + ehe.getMessage());
                _hostName = "UnknownHost";
            }
        }
        return _hostName;
    }    
    
    /**
     * Checks if the application is deployed as a servlet.
     * <p>
     * The current implementation only checks if the application  
     * is linked against <code>JavaWOJSPServlet.framework</code>. 
     * 
     * @return true if the application is deployed as a servlet
     */
    public boolean isDeployedAsServlet() {
        NSArray frameworkNames = (NSArray)NSBundle.frameworkBundles().valueForKey("name");
        return frameworkNames.containsObject("JavaWOJSPServlet");
    }
        
}