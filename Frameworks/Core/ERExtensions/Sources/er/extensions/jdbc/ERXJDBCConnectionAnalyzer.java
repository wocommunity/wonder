package er.extensions.jdbc;

import java.net.URL;
import java.net.URLClassLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.eoaccess.EOAdaptor;
import com.webobjects.eoaccess.EOModel;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.eocontrol.EOObjectStoreCoordinator;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.jdbcadaptor.JDBCAdaptor;
import com.webobjects.jdbcadaptor.JDBCPlugIn;

import er.extensions.foundation.ERXExceptionUtilities;

/**
 * Attempts to verify that a JDBC connection can be made and prints out diagnostic suggestions and information if it
 * cannot.
 * 
 * @author Charles Hill and Sacha Mallais
 * {@literal @}madeWonderfulBy mschrag
 */
public class ERXJDBCConnectionAnalyzer {
    private static final Logger log = LoggerFactory.getLogger("er.transaction.adaptor.ConnectionAnalyzer");

    private NSDictionary _connectionDictionary;
	private JDBCAdaptor _targetAdaptor;
	private JDBCPlugIn _targetPlugIn;

	/**
	 * Designated constructor. Uses the information in <code>aConnectionDictionary</code> to attempt to make a JDBC
	 * connection.
	 * 
	 * @param aConnectionDictionary
	 *            the connection information for the JDBC connection
	 */
	public ERXJDBCConnectionAnalyzer(NSDictionary aConnectionDictionary) {
		super();

		/** require [valid_connection_dictionary] aConnectionDictionary != null; * */

		_connectionDictionary = aConnectionDictionary;
		analyzeConnection();
	}

	/**
	 * Uses the connection dictionary information in <code>aModel</code> to attempt to make a JDBC connection.
	 * 
	 * @param aModel
	 *            the <code>EOModel</code> from which to take the connection information for the JDBC connection
	 */
	public ERXJDBCConnectionAnalyzer(EOModel aModel) {
		this(aModel.connectionDictionary());

		/** require [valid_model] aModel != null; * */
	}

	/**
	 * Uses the connection dictionary information in the EOModel named <code>aModelName</code>aModel to attempt to
	 * make a JDBC connection.
	 * 
	 * @param aModelName
	 *            the name of the <code>EOModel</code> from which to take the connection information for the JDBC
	 *            connection
	 */
	public ERXJDBCConnectionAnalyzer(String aModelName) {
		this(EOModelGroup.defaultGroup().modelNamed(aModelName));

		/***************************************************************************************************************
		 * require [valid_model_name] aModelName != null; [model_exists]
		 * EOModelGroup.defaultGroup().modelNamed(aModelName) != null;
		 **************************************************************************************************************/
	}

	/**
	 * Controls the order of analysis.
	 */
	public void analyzeConnection() {
		NSMutableDictionary mutableConnectionDictionary = _connectionDictionary.mutableClone();
		mutableConnectionDictionary.setObjectForKey("<password deleted for log>", "password");
		log.info("Checking JDBC connection with information {}", mutableConnectionDictionary);

		EOObjectStoreCoordinator.defaultCoordinator().lock();
		try {
			findAdaptor();
			findPlugin();
			findJDBCDriver();
			testConnection();
		}
		catch (RuntimeException t) {
			log.error(ERXExceptionUtilities.toParagraph(t));
		}
		finally {
			EOObjectStoreCoordinator.defaultCoordinator().unlock();
		}
	}

	/**
	 * Attempts to load the JDBCAdaptor.
	 */
	public void findAdaptor() {
		log.info("Trying to create JDBCAdaptor...");
		try {
			_targetAdaptor = (JDBCAdaptor) EOAdaptor.adaptorWithName("JDBC");
		}
		catch (java.lang.IllegalStateException e) {
			log.info("Error: Failed to load JavaJDBCAdaptor.framework");
			log.info("This framework needs to be included in your application to make JDBC connections.");
			dumpClasspath();
			throw new RuntimeException("JDBC Connection Analysis: JavaJDBCAdaptor.framework not on classpath");
		}
		log.info("Successfully created adaptor {}", targetAdaptor().getClass());

		/** ensure [targetAdaptor_created] targetAdaptor() != null; * */
	}

	/**
	 * Attempts to load JDBCPlugIn or sub-class and verify related configuration.
	 */
	public void findPlugin() {
		/** require [targetAdaptor_created] targetAdaptor() != null; * */

		log.info("Trying to create plugin...");

		try {
			_targetAdaptor.setConnectionDictionary(connectionDictionary());
			_targetPlugIn = targetAdaptor().plugIn();
			log.info("Created plugin {}", targetPlugIn().getClass());
		}
		catch (java.lang.NoClassDefFoundError e) {
			log.info("Error: Failed to load class {} when creating JDBC plugin.", e.getMessage());
			log.info("This is probably a class which is required by the plugin class and can also indicate that the JDBC driver was not found.");
			log.info("Either (a) your classpath is wrong or (b) something is missing from the JRE extensions directory/ies.");
			dumpClasspath();
			dumpExtensionDirectories();
			throw new RuntimeException("JDBC Connection Analysis: Missing class needed by plugin");
		}
		catch (Exception e) {
			// Unwrap the exception to get at the real problem
			Throwable t = ERXExceptionUtilities.getMeaningfulThrowable(e);
			log.info("Error: Plugin creationg failed.", t);
			throw new RuntimeException("JDBC Connection Analysis: unexpected failure creating plugin");
		}

		if (targetPlugIn().getClass().equals(com.webobjects.jdbcadaptor.JDBCPlugIn.class)) {
			String driverClassName = (String) connectionDictionary().objectForKey(JDBCAdaptor.DriverKey);
			if ((driverClassName == null) || (driverClassName.length() == 0)) {
				log.info("Error: Failed to load custom JDBC plugin and connection dictionary does not include the driver class name under the key {}", JDBCAdaptor.DriverKey);
				log.info("Either \n(a) the plugin is missing from your classpath or \n(b) the connection dictionary has a misspelled '{}' key or \n(c) the plug-in name specified under the '{}' key is incorrect or \n(d) the class name for the JDBC driver under the key '{}' is missing from the connection dictionary or\n(e)the connection dictionary has a misspelled '{}' key", JDBCAdaptor.PlugInKey, JDBCAdaptor.PlugInKey, JDBCAdaptor.DriverKey, JDBCAdaptor.DriverKey);
				dumpClasspath();
				throw new RuntimeException("JDBC Connection Analysis: Missing plugin or driver");
			}
			log.info("WARNING: using generic JDBCPlugIn.");
		}

		/** ensure [targetPlugIn_created] targetPlugIn() != null; * */
	}

	/**
	 * Attempts to load JDBC driver class.
	 */
	public void findJDBCDriver() {
		/** require [targetPlugIn_created] targetPlugIn() != null; * */

		log.info("Trying to load JDBC driver {}...", targetAdaptor().driverName());
		Class targetDriver;

		try {
			targetDriver = Class.forName(targetAdaptor().driverName());
		}
		catch (ClassNotFoundException e) {
			log.info("Error: Failed to load JDBC driver class {}", e.getMessage());
			log.info("The JDBC driver jar is either missing from  (a) " + "your classpath or (b) the JRE extensions directory/ies.");
			dumpClasspath();
			dumpExtensionDirectories();
			throw new RuntimeException("JDBC Connection Analysis: Cannot load JDBC driver. " + e.getMessage());
		}
		log.info("Successfully loaded JDBC driver {}", targetDriver.getName());
	}

	/**
	 * Attempts to make connection to databas via JDBC.
	 */
	public void testConnection() {
		/** require [targetPlugIn_created] targetPlugIn() != null; * */

		log.info("JDBC driver and plugin are loaded, trying to connect...");
		try {
			targetAdaptor().assertConnectionDictionaryIsValid();
		}
		catch (RuntimeException t) {
			log.info("Error: Exception thrown while connecting.\nCheck exception message carefully.");
			throw t;
		}
		catch (Error e) {
			log.info("Error: Exception thrown while connecting.\nCheck exception message carefully.");
			throw e;
		}
		log.info("JDBC connection successful!");
	}

	/*
	 * Prints out the classpath being used by this JVM.
	 */
	public void dumpClasspath() {
		log.info("The classpath being used is: ");

		URLClassLoader classLoader = (URLClassLoader) getClass().getClassLoader();
		URL[] sourceURLs = classLoader.getURLs();
		for (int i = 0; i < sourceURLs.length; i++) {
			log.info("{}", sourceURLs[i]);
		}
	}

	/*
	 * Prints out the Java extension directories being used by this JVM.
	 */
	public void dumpExtensionDirectories() {
		log.info("The JRE extension directories being used are: ");
		log.info(System.getProperties().getProperty("java.ext.dirs"));
	}

	/**
	 * Returns the connection dictionary being analyzed.
	 * 
	 * @return the connection dictionary being analyzed.
	 */
	public NSDictionary connectionDictionary() {
		return _connectionDictionary;
	}

	/**
	 * Returns an instance of JDBCAdaptor.
	 * 
	 * @return an instance of JDBCAdaptor
	 */
	public JDBCAdaptor targetAdaptor() {
		return _targetAdaptor;
	}

	/**
	 * Returns an instance of JDBCPlugIn or sub-class created from the connection dictionary information.
	 * 
	 * @return an instance of JDBCPlugIn or sub-class
	 */
	public JDBCPlugIn targetPlugIn() {
		return _targetPlugIn;
	}

	/** invariant [valid_connectionDictionary] connectionDictionary != null; * */
}
