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
    public static Category log;

    //CHECKME: (tatsuya) This will be moved to ERXProperties, ERXPropertyUtilities or ERXConfigurationManager
    protected static String ERConfigurationPathPropertyName = "ERConfigurationPath";

    /**
     * holds the notification that is posted after
     * the configuration system is updated.
     */
    public static final String ConfigurationDidChangeNotification = "ConfigurationDidChangeNotification";

    /** holds a reference to the current configuration file */
    private static String _configurationFilePath;

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
     * <p>The entry point for setting up the logging system.
     * This method should be called as early as possible. </p>
     * <p>ERXExtensions class will call this method twice 
     * during the initialization process: <br> 
     * - At the very beginning of loading ERExtensions framework <br>
     * - When the application class posts finishedLaunchingApp
     * notification </p>
     * <p>The second one is necessary to reconfigure the logging 
     * system when ERConfigurationPath is specified as a launch 
     * argument. </p>
     * <p>Calling this method multiple times will have 
     * no effect unless the configuration file path (not the file 
     * contents) has changed from the last call. (This will be 
     * the case when ERConfigurationPath is specified.)</p>
     * <p>ERXLogger will also call this method when it's initialized, 
     * for just in case the logging system has never been 
     * configured. </p>
     */
    public static void configureLogging() {
        String originalConfigFilePath = configurationFilePath(); // Note: This can be null.
        if (!_isInitialized) {
            BasicConfigurator.configure();
            log = Category.getInstance(ERXLog4j.class.getName());
            _isInitialized = true;
        }
        log.debug("Configure logging requested.");
        
        File configurationFile = null;
        String erConfigurationPath = System.getProperty(ERConfigurationPathPropertyName);
        
        if (erConfigurationPath != null) {
            configurationFile = new File(erConfigurationPath);
            if (! configurationFile.exists()  &&  ! erConfigurationPath.equals(originalConfigFilePath)) {
                log.warn("The configuration file \"" + erConfigurationPath + "\"" 
                            + " spacified by " + ERConfigurationPathPropertyName 
                            + " does not exist. The default configuration file will be used.");
                configurationFile = null;
            }
        }
            
        if (configurationFile == null) 
            configurationFile = new File(System.getProperty("user.home"), "WebObjects.properties");

        if (configurationFile.exists()) {
            setConfigurationFilePath(configurationFile.getPath());
            if (! configurationFilePath().equals(originalConfigFilePath)) {
                loadConfiguration();
                log.info("Logging system configured from the configuration file \""
                            + configurationFilePath() + "\".");
            } else {
                log.debug("Skipped to load the configuration since the configuration file \""
                            + configurationFilePath() + "\" remains same as the last one.");
            }
        } else {
            setConfigurationFilePath(originalConfigFilePath);
            log.warn("The configuration file \"" + configurationFile.getPath() + "\" does not exist. " 
                        + "Logging system will keep the last configuration with file \""
                        + originalConfigFilePath + "\".");
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
                log.debug("Registering observer for file change.");
                Observer o = new Observer();
                ERXRetainer.retain(o);
                try {
                    ERXFileNotificationCenter.defaultCenter().addObserver(o,
                                                                      new NSSelector("reloadConfigurationFile",
                                                                                     ERXConstant.NotificationClassArray),
                                                                      configurationFilePath());
                } catch (Exception ex) {
                    log.error("An exception occured while registering an observer for the "
                            + "logging configuration file: " + ex.getMessage());
                    ERXRetainer.release(o);
                }
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
            log.error("Unable to reset logging, configFilePath is null.");
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
        if (path != null) {
            try {
                FileInputStream in = new FileInputStream(path);
                result.load(in);
                in.close();
                log.info("Loaded configuration file at path: "+path);
            } catch (IOException e) {
                log.error("Unable to initialize properties from file "+path+"\nError :"+e);
            }
        } else {
            log.warn("Attempting to read property file for null file path");
        }
        return result;
    }
}
