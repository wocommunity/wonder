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
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSBundle;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSNotification;
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
    private NSArray _monitoredProperties;
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
    	}
    }

    private NSArray monitoredProperties() {
        if( _monitoredProperties == null ) {
            _monitoredProperties = ERXProperties.pathsForUserAndBundleProperties(/* logging */ true);
        }
        return _monitoredProperties;
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

        if (WOApplication.application()!=null && WOApplication.application().isCachingEnabled()) {
            log.info("WOCachingEnabled is true. Disabling the rapid turnaround for Properties files");
            registerPropertiesTouchFiles();
            return;
        }

        for (Enumeration e = monitoredProperties().objectEnumerator(); e.hasMoreElements();) {
            String path = (String) e.nextElement();
                registerForFileNotification(path, "updateSystemProperties");
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
    	NSMutableArray additionalConfigurationFiles = new NSMutableArray();
    	NSArray optionalConfigurationFiles = ERXProperties.optionalConfigurationFiles();
    	if (optionalConfigurationFiles != null) {
    		additionalConfigurationFiles.addObjectsFromArray(optionalConfigurationFiles);
    	}

    	String applicationUserPropertiesPath = ERXProperties.applicationUserProperties();
    	if (applicationUserPropertiesPath != null) {
    		additionalConfigurationFiles.addObject(applicationUserPropertiesPath);
    	}

        if (additionalConfigurationFiles.count() > 0) {
            Properties systemProperties = System.getProperties();
            for (Enumeration configEnumerator = additionalConfigurationFiles.objectEnumerator(); configEnumerator.hasMoreElements();) {
                String configFile = (String)configEnumerator.nextElement();
                File file = new File(configFile);
                if (file.exists() && file.isFile() && file.canRead()) {
                    try {
                        Properties props = ERXProperties.propertiesFromFile(file);
                        ERXProperties.transferPropertiesFromSourceToDest(props, systemProperties);
                        ERXSystem.updateProperties();
                    } catch (java.io.IOException ex) {
                        log.error("Unable to load optional configuration file: " + configFile, ex);
                    }
                }
                else {
                	ERXConfigurationManager.log.error("The optional configuration file '" + file.getAbsolutePath() + "' either does not exist or cannot be read.");
                }
            }
            if (_commandLineArguments != null  &&  _commandLineArguments.length > 0) {
                _reinsertCommandLineArgumentsToSystemProperties(_commandLineArguments);
            }
        }
    }
    
    /** 
     * Updates the configuration from the current configuration and 
     * posts {@link #ConfigurationDidChangeNotification}. It also  
     * calls {@link ERXLogger#configureLogging} to reconfigure 
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
        _updateSystemPropertiesFromMonitoredProperties(n != null ? (File)n.object() : null);
        if (_commandLineArguments != null  &&  _commandLineArguments.length > 0) 
            _reinsertCommandLineArgumentsToSystemProperties(_commandLineArguments);
        
        ERXLogger.configureLoggingWithSystemProperties();
    }
    
    public synchronized void updateAllSystemProperties(NSNotification notification) {
        updateSystemProperties(null);
    }
    
    /**
     * If updatedFile is null, all files are reread.
     */
    private void _updateSystemPropertiesFromMonitoredProperties(File updatedFile) {
        NSArray monitoredProperties = monitoredProperties();

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
        for (int i = firstDirtyFile; i < monitoredProperties.count(); i++) {
            String monitoredPropertiesPath = (String) monitoredProperties.objectAtIndex(i);
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
