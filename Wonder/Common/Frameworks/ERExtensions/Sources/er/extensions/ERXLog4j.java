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
import org.apache.log4j.*;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// This class is used to configure the log4j system.  By calling ERXLog4j.configureLogging() will cause the log4j
// configuration file to be loaded.  The file path is specified in a defaults write of ERConfigurationPath.
// ConfigureLogging can be called many times as it will only be configured the first time.
////////////////////////////////////////////////////////////////////////////////////////////////////////////////
public class ERXLog4j {

    ///////////////////////////////////////////////  log4j category  ////////////////////////////////////////////
    public static final Category cat = Category.getInstance("er.utilities.log4j.ERXLog4j");

    /////////////////////////////////////////////// Notification Titles /////////////////////////////////////////
    public static final String ConfigurationDidChangeNotification = "ConfigurationDidChangeNotification";

    private static String _configurationFilePath;
    public static String configurationFilePath() { return _configurationFilePath; }
    public static void setConfigurationFilePath(String value) {
        _configurationFilePath = value;
    }

    private static boolean _isLoggingConfigured=false;
    public static boolean isLoggingConfigured() { return _isLoggingConfigured; }
    public static void setIsLoggingConfigured(boolean value) { _isLoggingConfigured = value; }

    private static boolean _isInitialized=false;
    public static void configureLogging() {
        if (!_isInitialized) {
            // This must be passed in via the command line.  This will be useful for variable substitution in a
            // log4j config file
            if (System.getProperty("ERLogApplicationName") != null)
                ERXProperties.setStringForKey(System.getProperty("ERLogApplicationName"), "er.applicationName");
            String configurationPath=ERXProperties.stringForKey("ERConfigurationPath");
            if (configurationPath==null) {
                File mainProperties=new File(System.getProperty("user.home"),
                                             "WebObjects.properties");
                configurationPath=mainProperties.getPath();
            }
            setConfigurationFilePath(configurationPath);
            loadConfiguration();
            cat.info("Log4j configured.");
            setIsLoggingConfigured(true);
            Category.defaultHierarchy.setCategoryFactory(new ERXFactory());
            _isInitialized = true;
        }
    }

    static class ERXFactory implements org.apache.log4j.spi.CategoryFactory {
        public Category makeNewCategoryInstance(String name) {
            return new ERXLog(name);
        }
    }

    public static class Observer {
        public void reloadConfigurationFile(NSNotification n) {
            ERXLog4j.loadConfiguration();
        }        
    }
    // This is used in developement.  If WOCaching is not enabled then an observer will be created and register for the
    // notification: WORequestHandlerDidHandleRequestNotification
    // Note that this needs to be called at a latter point after usually called by the notification WOApplicationDidFinishLaunchingNotification
    private static boolean _initializedRapidTurnAround=false;
    public static void configureRapidTurnAround() {
        if (!_initializedRapidTurnAround) {
            if (!WOApplication.application().isCachingEnabled()) {
                if (cat.isDebugEnabled()) cat.debug("Registering observer for file change.");
                ERXFileNotificationCenter.defaultCenter().addObserver(new Observer(),
                                                                new NSSelector("reloadConfigurationFile",
                                                                               ERXConstant.NotificationClassArray),
                                                                configurationFilePath());
            }
            _initializedRapidTurnAround = true;
        }
    }

    // This loads and configures the Log4j system and posts the notification: Log4jConfigurationDidChangeNotification
    //private static long _lastModifiedDate = 0;
    // Now this is only called when the config file has changed.
    public static void loadConfiguration() {
        if (configurationFilePath() != null) {
            //File configurationFile = new File(configurationFilePath());
            //if (configurationFile != null && configurationFile.lastModified() > _lastModifiedDate) {
                //_lastModifiedDate = configurationFile.lastModified();
                PropertyConfigurator.configure(readPropertiesFromPath(configurationFilePath()));
                NSNotificationCenter.defaultCenter().postNotification(ConfigurationDidChangeNotification, null);
            //}
        } else {
            if (isLoggingConfigured())
                cat.error("Unable to reset logging, configFilePath is null.");
            else
                System.err.println("Unable to reset logging, configFilePath is null.");
        }
    }

    public static Properties readPropertiesFromPath(String path) {
        Properties result = new Properties();
        if (path!=null) {
            try {
                FileInputStream in = new FileInputStream(path);
                result.load(in);
                in.close();
                if (isLoggingConfigured())
                    cat.info("Loaded configuration file at path: "+path);
            } catch (IOException e) {
                if (isLoggingConfigured())
                    cat.error("Unable to initialize properties from file "+path+"\nError :"+e);
                else
                    System.err.println("Unable to initialize properties from file "+path+"\nError :"+e);
            }
        } else {
            cat.warn("Attempting to read property file for null file path");
        }
        return result;
    }
}
