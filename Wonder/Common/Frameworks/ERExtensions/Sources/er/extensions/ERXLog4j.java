/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions;

import com.webobjects.foundation.*;
import com.webobjects.appserver.WOApplication;
import java.util.*;
import java.io.*;
import org.apache.log4j.*;

/**
 * This class is used to configure the log4j system.  By calling
 * ERXLog4j.configureLogging() will cause the log4j configuration
 * file to be loaded.  The file path is specified in the 
 * property ERConfigurationPath or the WebObjects.properties found
 * in the current users home directory. ConfigureLogging can be called many
 * times as it will only be configured the first time.
 */
// CHECKME: Some of this (if not all) can go away now that we are in WO 5 and the property system is
//	    actually integrated into the runtime.
public class ERXLog4j {
    
    /**
     * We want to make sure that when this class is called
     * it will initialize the the class {@link ERXLogger}.
     */
    static {
        ERXLogger.class.getName();
    }

    /** logging support */
    public static Category cat;

    /**
     * holds the notification that is posted after
     * the configuration system is updated.
     */
    public static final String ConfigurationDidChangeNotification = "ConfigurationDidChangeNotification";

    /** holds a reference to the current configuration file */
    private static String _configurationFilePath;

    /** records if the logging system has been configured */
    private static boolean _isLoggingConfigured=false;

    /** records if the logging system has been initialized */
    private static boolean _isInitialized=false;

    /** records if rapid turnaround has been enabled */
    private static boolean _initializedRapidTurnAround=false;

    /**
     * Public inner class used for rapid turn around mode. This
     * observer listens for changes made to the configuration
     * file. When changes occur the configuration is reloaded.
     */
    public static class Observer {

        /**
         * Method invoked by the notification center when
         * the configuration file changes.
         * @param n current notification
         */
        public void reloadConfigurationFile(NSNotification n) {
            ERXLog4j.loadConfiguration();
        }
    }    

    /**
     * Cover method for returning the current configuration
     * file path.
     * @return current configuration file path
     */
    public static String configurationFilePath() { return _configurationFilePath; }

    /**
     * Cover method used for setting the current configuration
     * path to be used.
     * @param value current configuration file path
     */
    public static void setConfigurationFilePath(String value) {
        _configurationFilePath = value;
    }

    /**
     * Cover method that returns if the logging system is configured.
     * @return if the logging system has been configured
     */
    public static boolean isLoggingConfigured() { return _isLoggingConfigured; }

    /**
     * Sets if the logging system has been configured.
     * @param value if the logging sytem is configured
     */
    public static void setIsLoggingConfigured(boolean value) { _isLoggingConfigured = value; }

    /**
     * The entry point for setting up the logging system.
     * This method should be called as early as possible,
     * the best spot being in a static block of the
     * application class. Calling this method multiple times
     * will not have any additional effect after the first call.
     */
    public static void configureLogging() {
        if (!_isInitialized) {
            String configurationPath=System.getProperty("ERConfigurationPath");
            if (configurationPath==null) {
                File mainProperties=new File(System.getProperty("user.home"),
                                             "WebObjects.properties");
                configurationPath=mainProperties.getPath();
            }
            if (configurationPath != null) {
                setConfigurationFilePath(configurationPath);
                loadConfiguration();
                cat = Category.getInstance(ERXLog4j.class);
                cat.info("Logging system configured.");
            } else {
                // ENHANCEME: Should configure the BasicConfigurator at this point
            }
            setIsLoggingConfigured(true);
            _isInitialized = true;
        }
    }

    /**
     * This method is used to configure the log4j system
     * for rapid turnaround mode. Rapid turnaround mode
     * will only be enabled if a configuration file is set
     * and the system has WOCaching disabled.
     */
    public static void configureRapidTurnAround() {
        if (!_initializedRapidTurnAround) {
            if (!WOApplication.application().isCachingEnabled() && configurationFilePath() != null) {
                if (cat.isDebugEnabled()) cat.debug("Registering observer for file change.");
                Observer o = new Observer();
                ERXRetainer.retain(o);
                ERXFileNotificationCenter.defaultCenter().addObserver(o,
                                                                      new NSSelector("reloadConfigurationFile",
                                                                                     ERXConstant.NotificationClassArray),
                                                                      configurationFilePath());
            }
            _initializedRapidTurnAround = true;
        }
    }

    /**
     * This method is used to load the current configuration file. When this
     * method is called it will post the notification ConfigurationDidChangeNotification.
     * This method is called when rapid turnaround is enabled and the configuration
     * file changes.
     */
    public static void loadConfiguration() {
        if (configurationFilePath() != null) {
            PropertyConfigurator.configure(readPropertiesFromPath(configurationFilePath()));
            NSNotificationCenter.defaultCenter().postNotification(ConfigurationDidChangeNotification, null);
        } else {
            if (isLoggingConfigured())
                cat.error("Unable to reset logging, configFilePath is null.");
            else
                System.err.println("Unable to reset logging, configFilePath is null.");
        }
    }

    /**
     * Simple utility method used to load a properties file
     * for a given file path.
     * @param path file path to the properties file
     * @return properties file with the values from the file
     *		specified.
     */
    // MOVEME: ERXProperties or ERXPropertyUtilities
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
