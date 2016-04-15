/*
 * See COPYING for license information.
 */

package com.rackspacecloud.client.cloudfiles;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Cloud Files utilities
 */
public class FilesUtil
{
    private static final Logger log = LoggerFactory.getLogger(FilesUtil.class);

    /**
     * The name of the properties file we're looking for
     */
    private final static String file = "cloudfiles.properties";

    /**
     * A cache of the properties
     */
    private static Properties props = null;


    /**
     * Find the properties file in the class path and load it.
     *
     * @throws IOException
     */
    private static synchronized void loadPropertiesFromClasspath() throws IOException
    {
        props = new Properties();
        InputStream io = FilesUtil.class.getClassLoader().getResourceAsStream(file);
        if (io == null)
        {
            throw new FileNotFoundException("Property file '" + file + "' not found in the classpath.");
        }
        try {
        	loadProperties(io);
        } finally {
        	io.close();
        }
    }


    /**
     * Loads properties from input stream.
     *
     * @param io
     * @throws IOException
     */
    public static void loadProperties(final InputStream io) throws IOException
    {
        if (null == io)
        {
            //throw new IllegalArgumentException("Input stream cannot be null.");
        }
        props = new Properties();
        props.load(io);
    }


    /**
     * Look up a property from the properties file.
     *
     * @param key The name of the property to be found
     * @return The value of the property
     */
    public static String getProperty(final String key)
    {
        if (props == null)
        {
            try
            {
                loadPropertiesFromClasspath();
            }
            catch (Exception e)
            {
                log.warn("Unable to load properties file.", e);
            }
        }
        return props != null ? props.getProperty(key) : null;
    }


    /**
     * Look up a property from the properties file.
     *
     * @param key The name of the property to be found
     * @param defaultValue The default value if property is null
     * @return The value of the property
     */
    public static String getProperty(final String key, final String defaultValue)
    {
        if (props == null)
        {
            try
            {
                loadPropertiesFromClasspath();
            }
            catch (Exception e)
            {
                log.warn("Unable to load properties file.", e);
            }
        }
        return props != null ? props.getProperty(key, defaultValue) : defaultValue;
    }


    /**
     * Looks up the value of a key from the properties file and converts it to an integer.
     *
     * @param key The name of the property to be found
     * @return The value of that key
     */
    public static int getIntProperty(final String key)
    {
        final String property = getProperty(key);
        if (property == null)
        {
            log.warn("Could not load integer property {}.", key);
            return -1;
        }
        try
        {
            return Integer.parseInt(property);
        }
        catch (NumberFormatException e)
        {
            log.warn("Invalid format for a number in properties file: {}.", property, e);
            return -1;
        }
    }
}
