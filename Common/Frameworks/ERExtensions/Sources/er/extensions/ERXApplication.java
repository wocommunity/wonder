/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.log4j.Appender;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;

import com.webobjects.appserver.WOAction;
import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOMessage;
import com.webobjects.appserver.WORedirect;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WORequestHandler;
import com.webobjects.appserver.WOResourceManager;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver.WOSession;
import com.webobjects.appserver.WOTimer;
import com.webobjects.eoaccess.EOAdaptorChannel;
import com.webobjects.eoaccess.EODatabaseContext;
import com.webobjects.eoaccess.EOGeneralAdaptorException;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOTemporaryGlobalID;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSBundle;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSLog;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSNotification;
import com.webobjects.foundation.NSNotificationCenter;
import com.webobjects.foundation.NSProperties;
import com.webobjects.foundation.NSPropertyListSerialization;
import com.webobjects.foundation.NSRange;
import com.webobjects.foundation.NSSelector;
import com.webobjects.foundation.NSTimestamp;
import com.webobjects.jdbcadaptor.JDBCAdaptorException;

import er.extensions.migration.ERXMigrator;

/**
 * ERXApplication is the abstract superclass of WebObjects applications built with the ER frameworks.<br/> <br/> Useful
 * enhancements include the ability to change the deployed name of the application, support for automatic application
 * restarting at given intervals and more context information when handling exceptions.
 */

public abstract class ERXApplication extends ERXAjaxApplication implements ERXGracefulShutdown.GracefulApplication {

	/** logging support */
	public static final Logger log = Logger.getLogger(ERXApplication.class);

	/** request logging support */
	public static final Logger requestHandlingLog = Logger.getLogger("er.extensions.ERXApplication.RequestHandling");

	private static boolean _wasERXApplicationMainInvoked = false;

	/**
	 * Notification to get posted when we can an OutOfMemoryError. You should register your caching classes for this
	 * notification so you can release memory. Registration should happen at launch time.
	 */
	public static String LowMemoryNotification = "LowMemoryNotification";

	/**
	 * Buffer we reserve lowMemBufSize KB to release when we get an OutOfMemoryError, so we can post our notification
	 * and do other stuff
	 */
	private static byte lowMemBuffer[];

	/**
	 * Size of the memory in KB to reserve for low-mem situations, pulled form the system property
	 * <code>er.extensions.ERXApplication.lowMemBufferSize</code>. Default is 0, indicating no reserve.
	 */
	private static int lowMemBufferSize = 0;

	/** Holds the framework names during startup */
	private static Set allFrameworks;

	/** Notifcation to post when all bundles were loaded but before their principal was called */
	public static final String AllBundlesLoadedNotification = "NSBundleAllBundlesLoaded";

	/** Notifcation to post when all bundles were loaded but before their principal was called */
	public static final String ApplicationDidCreateNotification = "NSApplicationDidCreateNotification";

	private static Properties allBundleProps;
	
    private static NSDictionary propertiesFromArgv; 

	private static Properties readProperties(File file) {
		Properties result = null;
		if (file.exists()) {
			result = new Properties();
			try {
				result.load(file.toURL().openStream());
			}
			catch (MalformedURLException e) {
				e.printStackTrace();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}
    
	/**
	 * Copies the props from the command line to the static dict propertiesFromArgv.
	 *
	 */
    private static void insertCommandLineArguments() {
		NSArray keys = propertiesFromArgv.allKeys();
		int count = keys.count();
		for (int i = 0; i < count; i++) {
			Object key = keys.objectAtIndex(i);
			Object value = propertiesFromArgv.objectForKey(key);
			NSProperties._setProperty((String) key, (String) value);
		}
	}

	/**
	 * Will be called after each bundle load. We use it to know when the last
	 * bundle loaded so we can post a notification for it. Note that the bundles
	 * will get loaded in the order of the classpath but the main bundle will
	 * get loaded last. So in order to set the properties correctly, we first
	 * add all the props that are not already set, then we add the main bundle
	 * and the WebObjects.properties and finally the command line props.
	 * 
	 * @param n
	 */
	public static void bundleDidLoad(NSNotification n) {
		NSBundle bundle = (NSBundle) n.object();
		// System.out.println(bundle.name() + ": " + allFrameworks);
		allFrameworks.remove(bundle.name());
		if (allBundleProps == null) {
			allBundleProps = new Properties();
		}
		Properties bundleProps = bundle.properties();
		if (bundleProps != null) {
			for (Iterator iter = bundleProps.entrySet().iterator(); iter.hasNext();) {
				Map.Entry entry = (Map.Entry) iter.next();
				if (!allBundleProps.containsKey(entry.getKey())) {
					allBundleProps.setProperty((String) entry.getKey(), (String) entry.getValue());
				}
			}
		}

		if (allFrameworks.size() == 0) {
			Properties mainProps = null;
			NSBundle mainBundle = null;
			String mainBundleName = NSProperties._mainBundleName();
			if (mainBundleName != null) {
				mainBundle = NSBundle.bundleForName(mainBundleName);
			}
			if (mainBundle == null) {
				mainBundle = NSBundle.mainBundle();
			}
			if (mainBundle == null) {
				// AK: when we get here, the main bundle wasn't inited yet
				// so we do it ourself...
				try {
					Field ClassPath = NSBundle.class.getDeclaredField("ClassPath");
					ClassPath.setAccessible(true);
					if(ClassPath.get(NSBundle.class) != null) {
						Method init = NSBundle.class.getDeclaredMethod("InitMainBundle", null);
						init.setAccessible(true);
						init.invoke(NSBundle.class, null);
					}
				}
				catch (Exception e) {
					System.err.println(e);
					e.printStackTrace();
					System.exit(1);
				}
				mainBundle = NSBundle.mainBundle();
			}
			if (mainBundle != null) {
				mainProps = NSBundle.mainBundle().properties();
			}
			if (mainProps == null) {
				String woUserDir = NSProperties.getProperty("webobjects.user.dir");
				if (woUserDir == null) {
					woUserDir = System.getProperty("user.dir");
				}
				mainProps = readProperties(new File(woUserDir, "Contents" + File.separator + "Resources" + File.separator + "Properties"));
			}
			
			if(mainProps == null) {
				throw new IllegalStateException("Main bundle 'Properties' file can't be read.\nPlease post your deployment configuration in the Wonder mailing list.");
			}
			allBundleProps.putAll(mainProps);

			String userhome = System.getProperty("user.home");
			if (userhome != null && userhome.length() > 0) {
				Properties userProps = readProperties(new File(userhome, "WebObjects.properties"));
				if(userProps != null) {
					allBundleProps.putAll(userProps);
				}
			}
			
			Properties props = NSProperties._getProperties();
			props.putAll(allBundleProps);
			NSProperties._setProperties(props);
			insertCommandLineArguments();
			NSNotificationCenter.defaultCenter().postNotification(new NSNotification(AllBundlesLoadedNotification, NSKeyValueCoding.NullValue));
		}
	}

	static class AppClassLoader extends URLClassLoader {

		public static ClassLoader getAppClassLoader() {
			String classPath = System.getProperty("java.class.path");
			if(System.getProperty("com.webobjects.classpath") != null) {
				classPath += File.pathSeparator + System.getProperty("com.webobjects.classpath");
			}
			String files[] = classPath.split(File.pathSeparator);
			URL urls[] = new URL[files.length];
			for (int i = 0; i < files.length; i++) {
				String string = files[i];
				try {
					urls[i] = new File(string).toURL();
				}
				catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return new AppClassLoader(urls, Thread.currentThread().getContextClassLoader());
		}

		public synchronized Class loadClass(String s, boolean flag) throws ClassNotFoundException {
			SecurityManager securitymanager = System.getSecurityManager();
			if (securitymanager != null) {
				String s1 = s.replace('/', '.');
				if (s1.startsWith("[")) {
					int i = s1.lastIndexOf('[') + 2;
					if (i > 1 && i < s1.length()) {
						s1 = s1.substring(i);
					}
				}
				int j = s1.lastIndexOf('.');
				if (j != -1) {
					securitymanager.checkPackageAccess(s1.substring(0, j));
				}
			}
			return super.loadClass(s, flag);
		}

		AppClassLoader(URL aurl[], ClassLoader classloader) {
			super(aurl, classloader);
		}
	}

	private static boolean isSystemJar(String jar) {
		// check maven path
		if (jar.indexOf("webobjects" + File.separator + "apple") > 0) {
			return true;
		}
		// check mac path
		if (jar.indexOf("System" + File.separator + "Library") > 0) {
			return true;
		}
		// check win path
		if (jar.indexOf("Apple" + File.separator + "Library") > 0) {
			return true;
		}
		// if embedded, check explicit names
		if (jar.matches("Frameworks[/\\\\]Java(Foundation|EOControl|EOAccess|WebObjects).*")) {
			return true;
		}
		return false;
	}
	
	private static String stringFromJar(String jar, String path) {
		JarFile f;
		try {
			f = new JarFile(jar);
			JarEntry e = (JarEntry) f.getEntry(path);
			if(e != null) {
				InputStream is =  f.getInputStream(e);
				ByteArrayOutputStream bout = new ByteArrayOutputStream();
				int read = -1;
				byte[] buf = new byte[1024 * 50];
				while ((read = is.read(buf)) != -1) {
					bout.write(buf, 0, read);
				}

				String content = new String(bout.toByteArray(), "UTF-8");
				return content;
			}
			return null;
		}
		catch (IOException e1) {
			throw NSForwardException._runtimeExceptionForThrowable(e1);
		}
	}

	/**
	 * Called when the application starts up and saves the command line arguments for {@link ERXConfigurationManager}.
	 * 
	 * @see WOApplication#main(String[], Class)
	 */
	public static void main(String argv[], Class applicationClass) {
		setup(argv);
		WOApplication.main(argv, applicationClass);
	}

	/**
	 * Called prior to actually initing the app. Defines framework load order, 
	 * class path order, checks patches etc.
	 */
	public static void setup(String[] argv) {
		_wasERXApplicationMainInvoked = true;
		String cps[] = new String[] {"java.class.path", "com.webobjects.classpath"};
        propertiesFromArgv = NSProperties.valuesFromArgv(argv);
        allFrameworks = new HashSet();
		for (int var = 0; var < cps.length; var++) {
			String cpName = cps[var];
			String cp = System.getProperty(cpName);
			if(cp != null) {
				String parts[] = cp.split(File.pathSeparator);
				String normalLibs = "";
				String systemLibs = "";
				for (int i = 0; i < parts.length; i++) {
					String jar = parts[i];
//					System.out.println("Checking: " + jar);
					// all patched frameworks here
					if(isSystemJar(jar)) {
						systemLibs += jar + File.pathSeparator;
					}
					else {
						normalLibs += jar + File.pathSeparator;
					}
					String bundle = jar.replaceAll(".*?[/\\\\](\\w+)\\.framework.*", "$1");
					if (bundle.matches("^\\w+$") && !"JavaVM".equals(bundle)) {
						allFrameworks.add(bundle);
					} else if(jar.endsWith(".jar")) {
						String info = stringFromJar(jar, "Resources/Info.plist");
						if(info != null) {
							NSDictionary dict = (NSDictionary) NSPropertyListSerialization.propertyListFromString(info);
							bundle = (String) dict.objectForKey("CFBundleExecutable");
							allFrameworks.add(bundle);
							// System.out.println("Jar bundle: " + bundle);
						}
					}
				}

				if (systemLibs.length() > 1) {
					systemLibs = systemLibs.substring(0, systemLibs.length() - 1);
				}
				if (normalLibs.length() > 1) {
					normalLibs = normalLibs.substring(0, normalLibs.length() - 1);
				}
				cp = normalLibs + File.pathSeparator + systemLibs;
				// AK: this is pretty experimental for now. The classpath reordering
				// should actually be done in a WOLips bootstrap because as this time all
				// the static inits of WO app have already happened (which include NSMutableArray and _NSThreadSaveSet)

				if (System.getProperty("_DisableClasspathReorder") == null) {
					System.setProperty(cpName, cp);
				}
			}
		}
		if (System.getProperty("_DisableClasspathReorder") == null) {
			ClassLoader loader = AppClassLoader.getAppClassLoader();
			Thread.currentThread().setContextClassLoader(loader);
		}
		Class arrayClass = NSMutableArray.class;
		try {
			Field f = arrayClass.getField("ERX_MARKER");
		}
		catch (NoSuchFieldException e) {
			System.err.println("No ERX_MARKER field in NSMutableArray found. \nThis means your class path is incorrect. Adjust it so that ERExtensions come before JavaFoundation.");
			System.exit(1);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		NSNotificationCenter.defaultCenter().addObserver(ERXApplication.class, new NSSelector("bundleDidLoad", new Class[] { NSNotification.class }), "NSBundleDidLoadNotification", null);
		ERXConfigurationManager.defaultManager().setCommandLineArguments(argv);
		ERXFrameworkPrincipal.setUpFrameworkPrincipalClass(ERXExtensions.class);
	}

	public void installDefaultEncoding(String encoding) {
		WOMessage.setDefaultEncoding(encoding);
		WOMessage.setDefaultURLEncoding(encoding);
		ERXMessageEncoding.setDefaultEncoding(encoding);
		ERXMessageEncoding.setDefaultEncodingForAllLanguages(encoding);
	}

	/**
	 * Installs several bufixes and enhancements to WODynamicElements. Sets the Context class name to
	 * "er.extensions.ERXWOContext" if it is "WOContext". Patches ERXWOForm, ERXWOFileUpload, ERXWOText to be used
	 * instead of WOForm, WOFileUpload, WOText.
	 */
	public void installPatches() {
		ERXPatcher.installPatches();
		if (contextClassName().equals("WOContext"))
			setContextClassName("er.extensions.ERXWOContext");
		if (contextClassName().equals("WOServletContext") || contextClassName().equals("com.webobjects.appserver.jspservlet.WOServletContext"))
			setContextClassName("er.extensions.ERXWOServletContext");

		ERXPatcher.setClassForName(ERXWOForm.class, "WOForm");
		ERXPatcher.setClassForName(ERXAnyField.class, "WOAnyField");
		ERXPatcher.setClassForName(ERXWORepetition.class, "WORepetition");
		ERXPatcher.setClassForName(ERXActiveImage.class, "WOActiveImage");

		// use our localizing string class
		// works around #3574558
		if (ERXLocalizer.isLocalizationEnabled()) {
			ERXPatcher.setClassForName(ERXWOString.class, "WOString");
			ERXPatcher.setClassForName(ERXWOTextField.class, "WOTextField");
		}

		// ERXPatcher.setClassForName(ERXSubmitButton.class, "WOSubmitButton");

		// Fix for 3190479 URI encoding should always be UTF8
		// See http://www.w3.org/International/O-URL-code.html
		// For WO 5.1.x users, please comment this statement to compile.
		com.webobjects.appserver._private.WOURLEncoder.WO_URL_ENCODING = "UTF8";

		// WO 5.1 specific patches
		if (ERXProperties.webObjectsVersionAsDouble() < 5.2d) {
			// ERXWOText contains a patch for WOText to not include the value
			// attribute (#2948062). Fixed in WO 5.2
			ERXPatcher.setClassForName(ERXWOText.class, "WOText");

		}
		// ERXWOFileUpload returns a better warning than throwing a ClassCastException.
		// Fixed in WO 5.2
		// ERXPatcher.setClassForName(ERXWOFileUpload.class, "WOFileUpload");
	}

    public WOResourceManager createResourceManager() {
        return new ERXResourceManager();
    }
    
	/**
	 * The ERXApplication contructor.
	 */
	public ERXApplication() {
		super();
		if (allFrameworks.size() > 0) {
			throw new RuntimeException("ERXExtensions have not been initialized. Please report the classpath and the rest of the bundles to the Wonder mailing list: " + "\nRemaining" + allFrameworks + "\n" + System.getProperty("java.class.path"));
		}
		if ("JavaFoundation".equals(NSBundle.mainBundle().name())) {
			throw new RuntimeException("Your main bundle is \"JavaFoundation\".  You are not launching this WO application properly.  If you are using Eclipse, most likely you launched your WOA as a \"Java Application\" instead of a \"WO Application\".");
		}
		// ak: telling Log4J to re-init the Console appenders so we get logging into WOOutputPath again
		for(Enumeration e = Logger.getRootLogger().getAllAppenders(); e.hasMoreElements(); ) {
			Appender appender = (Appender) e.nextElement();
			if (appender instanceof ConsoleAppender) {
				ConsoleAppender app = (ConsoleAppender) appender;
				app.activateOptions();
			}
		}
		if (!ERXConfigurationManager.defaultManager().isDeployedAsServlet() && !_wasERXApplicationMainInvoked) {
			_displayMainMethodWarning();
		}
		NSNotificationCenter.defaultCenter().postNotification(new NSNotification(ApplicationDidCreateNotification, this));
		installPatches();
		lowMemBufferSize = ERXProperties.intForKeyWithDefault("er.extensions.ERXApplication.lowMemBufferSize", 0);
		if (lowMemBufferSize > 0) {
			lowMemBuffer = new byte[lowMemBufferSize];
		}
		registerRequestHandler(new ERXDirectActionRequestHandler(), directActionRequestHandlerKey());
		if (isDirectConnectEnabled()) {
			registerRequestHandler(new ERXStaticResourceRequestHandler(), "_wr_");
		}

		Long timestampLag = Long.getLong("EOEditingContextDefaultFetchTimestampLag");
		if (timestampLag != null)
			EOEditingContext.setDefaultFetchTimestampLag(timestampLag.longValue());

		String defaultMessageEncoding = System.getProperty("er.extensions.ERXApplication.DefaultMessageEncoding");
		if (defaultMessageEncoding != null) {
			log.debug("Setting WOMessage default encoding to \"" + defaultMessageEncoding + "\"");
			WOMessage.setDefaultEncoding(defaultMessageEncoding);
		}

		// Configure the WOStatistics CLFF logging since it can't be controled by a property, grrr.
		configureStatisticsLogging();

		NSNotificationCenter.defaultCenter().addObserver(this, new NSSelector("finishInitialization", ERXConstant.NotificationClassArray), WOApplication.ApplicationWillFinishLaunchingNotification, null);

		NSNotificationCenter.defaultCenter().addObserver(this, new NSSelector("didFinishLaunching", ERXConstant.NotificationClassArray), WOApplication.ApplicationDidFinishLaunchingNotification, null);
		ERXEC.setUseUnlocker(useEditingContextUnlocker());
		ERXEC.setTraceOpenEditingContextLocks(traceOpenEditingContextLocks());

		// Signal handling support
		if (ERXGracefulShutdown.isEnabled()) {
			ERXGracefulShutdown.installHandler();
		}
		// AK: this makes it possible to retrieve the creating instance from an NSData PK.
		// it should still be unique, as one host can only have one running instance to a port
		EOTemporaryGlobalID._setProcessIdentificationBytesFromInt(port().intValue());
	}

	/**
	 * Decides whether to use editing context unlocking.
	 * 
	 * @return true if ECs should be unlocked after each RR-loop
	 */
	public boolean useEditingContextUnlocker() {
		return ERXProperties.booleanForKeyWithDefault("er.extensions.ERXApplication.useEditingContextUnlocker", false);
	}

	/**
	 * Decides whether or not to keep track of open editing context locks.
	 * 
	 * @return true if editing context locks should be tracked
	 */
	public boolean traceOpenEditingContextLocks() {
		return ERXProperties.booleanForKeyWithDefault("er.extensions.ERXApplication.traceOpenEditingContextLocks", false);
	}

	/**
	 * Configures the statistics logging for a given application. By default will log to a file <base log directory>/<WOApp
	 * Name>-<host>-<port>.log if the base log path is defined. The base log path is defined by the property
	 * <code>er.extensions.ERXApplication.StatisticsBaseLogPath</code> The default log rotation frequency is 24 hours,
	 * but can be changed by setting in milliseconds the property
	 * <code>er.extensions.ERXApplication.StatisticsLogRotationFrequency</code>
	 */
	public void configureStatisticsLogging() {
		String statisticsBasePath = System.getProperty("er.extensions.ERXApplication.StatisticsBaseLogPath");
		if (statisticsBasePath != null) {
			// Defaults to a single day
			int rotationFrequency = ERXProperties.intForKeyWithDefault("er.extensions.ERXApplication.StatisticsLogRotationFrequency", 24 * 60 * 60 * 1000);
			String logPath = statisticsBasePath + File.separator + name() + "-" + ERXConfigurationManager.defaultManager().hostName() + "-" + port() + ".log";
			if (log.isDebugEnabled()) {
				log.debug("Configured statistics logging to file path \"" + logPath + "\" with rotation frequency: " + rotationFrequency);
			}
			statisticsStore().setLogFile(logPath, (long) rotationFrequency);
		}
	}

	/**
	 * Notification method called when the application posts the notification
	 * {@link WOApplication#ApplicationWillFinishLaunchingNotification}. This method calls subclasse's
	 * {@link #finishInitialization} method.
	 * 
	 * @param n
	 *            notification that is posted after the WOApplication has been constructed, but before the application
	 *            is ready for accepting requests.
	 */
	public final void finishInitialization(NSNotification n) {
		finishInitialization();
		if (ERXMigrator.shouldMigrateAtStartup()) {
			migrator().migrateToLatest();
		}
	}

	/**
	 * Notification method called when the application posts the notification
	 * {@link WOApplication#ApplicationDidFinishLaunchingNotification}. This method calls subclasse's
	 * {@link #didFinishLaunching} method.
	 * 
	 * @param n
	 *            notification that is posted after the WOApplication has finished launching and is ready for accepting
	 *            requests.
	 */
	public final void didFinishLaunching(NSNotification n) {
		didFinishLaunching();
		if (isDevelopmentMode() && !autoOpenInBrowser()) {
			log.warn("You are running in development mode with WOAutoOpenInBrowser = false.  No browser will open and it will look like the application is hung, but it's not.  There's just not a browser opening automatically.");
		}
	}

	/**
	 * Called when the application posts {@link WOApplication#ApplicationWillFinishLaunchingNotification}. Override
	 * this to perform application initialization. (optional)
	 */
	public void finishInitialization() {
		// empty
	}

	/**
	 * Called when the application posts {@link WOApplication#ApplicationDidFinishLaunchingNotification}. Override this
	 * to perform application specific tasks after the application has been initialized. THis is a good spot to perform
	 * batch application tasks.
	 */
	public void didFinishLaunching() {
		// empty
	}

	/**
	 * The ERXApplication singleton.
	 * 
	 * @return returns the <code>WOApplication.application()</code> cast as an ERXApplication
	 */
	public static ERXApplication erxApplication() {
		return (ERXApplication) WOApplication.application();
	}

	/**
	 * Adds support for automatic application cycling. Applications can be configured to cycle in two ways:<br/> <br/>
	 * The first way is by setting the System property <b>ERTimeToLive</b> to the number of seconds (+ a random
	 * interval of 10 minutes) that the application should be up before terminating. Note that when the application's
	 * time to live is up it will quit calling the method <code>killInstance</code>.<br/> <br/> The second way is by
	 * setting the System property <b>ERTimeToDie</b> to the time in seconds after midnight when the app should be
	 * starting to refuse new sessions. In this case when the application starts to refuse new sessions it will also
	 * register a kill timer that will terminate the application between 0 minutes and 1:00 minutes.<br/>
	 */
	public void run() {
		int timeToLive = ERXProperties.intForKey("ERTimeToLive");
		if (timeToLive > 0) {
			log.info("Instance will live " + timeToLive + " seconds.");
			NSLog.out.appendln("Instance will live " + timeToLive + " seconds.");
			// add a fudge factor of around 10 minutes
			timeToLive += (new Random()).nextFloat() * 600;
			NSTimestamp exitDate = (new NSTimestamp()).timestampByAddingGregorianUnits(0, 0, 0, 0, 0, timeToLive);
			WOTimer t = new WOTimer(exitDate, 0, this, "killInstance", null, null, false);
			t.schedule();
		}
		int timeToDie = ERXProperties.intForKey("ERTimeToDie");
		if (timeToDie > 0) {
			log.info("Instance will not live past " + timeToDie + ":00.");
			NSLog.out.appendln("Instance will not live past " + timeToDie + ":00.");
			NSTimestamp now = new NSTimestamp();
			int s = (timeToDie - ERXTimestampUtility.hourOfDay(now)) * 3600 - ERXTimestampUtility.minuteOfHour(now) * 60;
			if (s < 0)
				s += 24 * 3600; // how many seconds to the deadline

			// deliberately randomize this so that not all instances restart at the same time
			// adding up to 1 hour
			s += (new Random()).nextFloat() * 3600;

			NSTimestamp stopDate = now.timestampByAddingGregorianUnits(0, 0, 0, 0, 0, s);
			WOTimer t = new WOTimer(stopDate, 0, this, "startRefusingSessions", null, null, false);
			t.schedule();
		}
		super.run();
	}

	/**
	 * Creates the request object for this loop. Overridden to use an {@link ERXRequest} object that fixes a bug with
	 * localization.
	 * 
	 * @param aMethod
	 *            the HTTP method object used to send the request, must be one of "GET", "POST" or "HEAD"
	 * @param aURL -
	 *            must be non-null
	 * @param anHTTPVersion -
	 *            the version of HTTP used
	 * @param someHeaders -
	 *            dictionary whose String keys correspond to header names
	 * @param aContent -
	 *            the HTML content of the receiver
	 * @param someInfo -
	 *            an NSDictionary that can contain any kind of information related to the current response.
	 * @return a new WORequest object
	 */
	public WORequest createRequest(String aMethod, String aURL, String anHTTPVersion, NSDictionary someHeaders, NSData aContent, NSDictionary someInfo) {

		// Workaround for #3428067 (Apache Server Side Include module will feed
		// "INCLUDED" as the HTTP version, which causes a request object not to be
		// created by an excepion.
		if (anHTTPVersion.startsWith("INCLUDED"))
			anHTTPVersion = "HTTP/1.0";

		WORequest worequest = new ERXRequest(aMethod, aURL, anHTTPVersion, someHeaders, aContent, someInfo);
		return worequest;
	}

	/**
	 * Used to instanciate a WOComponent when no context is available, typically ouside of a session
	 * 
	 * @param pageName -
	 *            The name of the WOComponent that must be instanciated.
	 * @return created WOComponent with the given name
	 */
	public static WOComponent instantiatePage(String pageName) {
		// Create a context from a fake request
		WORequest fakeRequest = new ERXRequest("GET", "", "HTTP/1.1", null, null, null);
		WOContext context = application().createContextForRequest(fakeRequest);
		return application().pageWithName(pageName, context);
	}

	/**
	 * Stops the application from handling any new requests. Will still handle requests from existing sessions.
	 */
	public void startRefusingSessions() {
		log.info("Refusing new sessions");
		NSLog.out.appendln("Refusing new sessions");
		refuseNewSessions(true);
	}

	protected WOTimer _killTimer;

	/**
	 * Overridden to install/uninstall a timer that will terminate the application in <code>ERTimeToKill</code>
	 * seconds from the time this method is called. The timer will get uninstalled if you allow new sessions again
	 * during that time span.
	 */

	public void refuseNewSessions(boolean value) {
		super.refuseNewSessions(value);
		// we assume that we changed our mind about killing the instance.
		if (_killTimer != null) {
			_killTimer.invalidate();
			_killTimer = null;
		}
		if (isRefusingNewSessions()) {
			int timeToKill = ERXProperties.intForKey("ERTimeToKill");
			if (timeToKill > 0) {
				log.info("Registering kill timer");
				NSTimestamp exitDate = (new NSTimestamp()).timestampByAddingGregorianUnits(0, 0, 0, 0, 0, timeToKill);
				_killTimer = new WOTimer(exitDate, 0, this, "killInstance", null, null, false);
				_killTimer.schedule();
			}
		}
	}

	/**
	 * Killing the instance will log a 'Forcing exit' message and then call <code>System.exit(1)</code>
	 */
	public void killInstance() {
		log.info("Forcing exit");
		NSLog.out.appendln("Forcing exit");
		System.exit(1);
	}

	/** cached name suffix */
	private String _nameSuffix;
	/** has the name suffix been cached? */
	private boolean _nameSuffixLookedUp = false;

	/**
	 * The name suffix is appended to the current name of the application. This adds the ability to add a useful suffix
	 * to differentuate between different sets of applications on the same machine.<br/> <br/> The name suffix is set
	 * via the System property <b>ERApplicationNameSuffix</b>.<br/> <br/> For example if the name of an application is
	 * Buyer and you want to have a training instance appear with the name BuyerTraining then you would set the
	 * ERApplicationNameSuffix to Training.<br/> <br/>
	 * 
	 * @return the System property <b>ERApplicationNameSuffix</b> or <code>null</code>
	 */
	public String nameSuffix() {
		if (!_nameSuffixLookedUp) {
			_nameSuffix = System.getProperty("ERApplicationNameSuffix");
			_nameSuffix = _nameSuffix == null ? "" : _nameSuffix;
			_nameSuffixLookedUp = true;
		}
		return _nameSuffix;
	}

	/** cached computed name */
	private String _userDefaultName;

	/**
	 * Adds the ability to completely change the applications name by setting the System property <b>ERApplicationName</b>.
	 * Will also append the <code>nameSuffix</code> if one is set.<br/> <br/>
	 * 
	 * @return the computed name of the application.
	 */
	public String name() {
		if (_userDefaultName == null) {
			_userDefaultName = System.getProperty("ERApplicationName");
			if (_userDefaultName == null)
				_userDefaultName = super.name();
			if (_userDefaultName != null) {
				String suffix = nameSuffix();
				if (suffix != null && suffix.length() > 0)
					_userDefaultName += suffix;
			}
		}
		return _userDefaultName;
	}

	/**
	 * This method returns {@link WOApplication}'s <code>name</code> method.<br/>
	 * 
	 * @return the name of the application executable.
	 */
	public String rawName() {
		return super.name();
	}

	/**
	 * Puts together a dictionary with a bunch of useful information relative to the current state when the exception
	 * occurred. Potentially added information:<br/>
	 * <ol>
	 * <li>the current page name</li>
	 * <li>the current component</li>
	 * <li>the complete hierarchy of nested components</li>
	 * <li>the requested uri</li>
	 * <li>the D2W page configuration</li>
	 * <li>the previous page list (from the WOStatisticsStore)</li>
	 * </ol>
	 * Also, in case the top-level exception was a EOGeneralAdaptorException, then you also get the failed ops and the
	 * sql exception. <br/>
	 * 
	 * @return dictionary containing extra information for the current context.
	 */
	public NSMutableDictionary extraInformationForExceptionInContext(Exception e, WOContext context) {
		NSMutableDictionary extraInfo = new NSMutableDictionary();

		if (e instanceof EOGeneralAdaptorException) {
			// AK NOTE: you might have sensitive info in your failed ops...
			NSDictionary dict = ((EOGeneralAdaptorException) e).userInfo();
			if (dict != null) {
				Object value;
				// this one is a little bit heavyweight...
				// value = NSPropertyListSerialization.stringFromPropertyList(dict);
				value = dict.objectForKey(EODatabaseContext.FailedDatabaseOperationKey);
				if (value != null) {
					extraInfo.setObjectForKey(value.toString(), EODatabaseContext.FailedDatabaseOperationKey);
				}
				value = dict.objectForKey(EOAdaptorChannel.AdaptorFailureKey);
				if (value != null) {
					extraInfo.setObjectForKey(value.toString(), EOAdaptorChannel.AdaptorFailureKey);
				}
				value = dict.objectForKey(EOAdaptorChannel.FailedAdaptorOperationKey);
				if (value != null) {
					extraInfo.setObjectForKey(value.toString(), EOAdaptorChannel.FailedAdaptorOperationKey);
				}
				if (e instanceof JDBCAdaptorException) {
					value = ((JDBCAdaptorException) e).sqlException();
					if (value != null) {
						extraInfo.setObjectForKey(value.toString(), "SQLException");
					}
				}
			}
		}
		if (context != null && context.page() != null) {
			extraInfo.setObjectForKey(context.page().name(), "CurrentPage");
			if (context.component() != null) {
				extraInfo.setObjectForKey(context.component().name(), "CurrentComponent");
				if (context.component().parent() != null) {
					WOComponent component = context.component();
					NSMutableArray hierarchy = new NSMutableArray(component.name());
					while (component.parent() != null) {
						component = component.parent();
						hierarchy.addObject(component.name());
					}
					extraInfo.setObjectForKey(hierarchy, "CurrentComponentHierarchy");
				}
			}
			extraInfo.setObjectForKey(context.request().uri(), "uri");
			NSSelector d2wSelector = new NSSelector("d2wContext");
			if (d2wSelector.implementedByObject(context.page())) {
				try {
					NSKeyValueCoding c = (NSKeyValueCoding) d2wSelector.invoke(context.page());
					if (c != null) {
						String pageConfiguration = (String) c.valueForKey("pageConfiguration");
						if (pageConfiguration != null) {
							extraInfo.setObjectForKey(pageConfiguration, "D2W-PageConfiguration");
						}
						String propertyKey = (String) c.valueForKey("propertyKey");
						if (propertyKey != null) {
							extraInfo.setObjectForKey(propertyKey, "D2W-PropertyKey");
						}
						NSArray displayPropertyKeys = (NSArray) c.valueForKey("displayPropertyKeys");
						if (displayPropertyKeys != null) {
							extraInfo.setObjectForKey(displayPropertyKeys, "D2W-DisplayPropertyKeys");
						}
					}
				}
				catch (Exception ex) {
				}
			}
			if (context.hasSession() && context.session().statistics() != null) {
				extraInfo.setObjectForKey(context.session().statistics(), "PreviousPageList");
			}
			NSMutableDictionary bundleVersions = new NSMutableDictionary();
			for (Enumeration bundles = NSBundle._allBundlesReally().objectEnumerator(); bundles.hasMoreElements();) {
				NSBundle bundle = (NSBundle) bundles.nextElement();
				String version = ERXProperties.versionStringForFrameworkNamed(bundle.name());
				if(version == null) {
					version = "No version provided";
				}
				bundleVersions.setObjectForKey(version, bundle.name());
			}
			extraInfo.setObjectForKey(bundleVersions, "Bundles");
		}
		return extraInfo;
	}

	/**
	 * Reports an exception. This method only logs the error and could be overriden to return a valid error page.
	 * 
	 * @param exception
	 *            to be reported
	 * @param extraInfo
	 *            dictionary of extra information about what was happening when the exception was thrown.
	 * @return a valid response to display or null. In that case the superclasses
	 *         {@link #handleException(Exception, WOContext)} is called
	 */
	public WOResponse reportException(Throwable exception, NSDictionary extraInfo) {
		Throwable t = exception instanceof NSForwardException ? ((NSForwardException) exception).originalException() : exception;

		log.error("Exception caught: " + exception.getMessage() + "\nExtra info: " + NSPropertyListSerialization.stringFromPropertyList(extraInfo) + "\n", t);
		return null;
	}

	/**
	 * Workaround for WO 5.2 DirectAction lock-ups. As the super-implementation is empty, it is fairly safe to override
	 * here to call the normal exception handling earlier than usual.
	 * 
	 * @see WOApplication#handleActionRequestError(WORequest, Exception, String, WORequestHandler, String, String,
	 *      Class, WOAction)
	 */
	// NOTE: if you use WO 5.1, comment out this method, otherwise it won't compile.
	public WOResponse handleActionRequestError(WORequest aRequest, Exception exception, String reason, WORequestHandler aHandler, String actionClassName, String actionName, Class actionClass, WOAction actionInstance) {
		WOContext context = actionInstance != null ? actionInstance.context() : null;
		WOResponse response = handleException(exception, context);
		// AK: bugfix for #4186886 (Session store deadlock with DAs). The bug occurs in 5.2.3, I'm not sure about other
		// versions.
		// It may create other problems, but this one is very severe to begin with
		// The crux of the matter is that for certain exceptions, the DA request handler does not check sessions back in
		// which leads to a deadlock in the session store when the session is accessed again.
		if (context != null && context.hasSession() && ("InstantiationError".equals(reason) || "InvocationError".equals(reason))) {
			context._putAwakeComponentsToSleep();
			saveSessionForContext(context);
		}
		return response;
	}

	/**
	 * Logs extra information about the current state.
	 * 
	 * @param exception
	 *            to be handled
	 * @param context
	 *            current context
	 * @return the WOResponse of the generated exception page.
	 */
	public WOResponse handleException(Exception exception, WOContext context) {
		if (ERXProperties.booleanForKey("er.extensions.ERXApplication.redirectOnMissingObjects")) {
			// AK: the idea here is that you might have a stale object that was deleted from the DB
			// while you weren't looking so the next time around your page might get a chance earlier to
			// realize it isn't there anymore. Unfortunalty, this doesn�t work in all scenarios.
			if (exception instanceof ERXDatabaseContextDelegate.ObjectNotAvailableException && context != null) {
				String retryKey = context.request().stringFormValueForKey("ERXRetry");
				if (retryKey == null) {
					WORedirect page = new WORedirect(context);
					page.setUrl(context.request().uri() + "?ERXRetry=1");
					return page.generateResponse();
				}
			}
		}
		// We first want to test if we ran out of memory. If so we need to quit ASAP.
		handlePotentiallyFatalException(exception);

		// Not a fatal exception, business as usual.
		NSDictionary extraInfo = extraInformationForExceptionInContext(exception, context);
		WOResponse response = reportException(exception, extraInfo);
		if (response == null)
			response = super.handleException(exception, context);
		return response;
	}

	/**
	 * Standard exception page. Also logs error to standard out.
	 * 
	 * @param exception
	 *            to be handled
	 * @param context
	 *            current context
	 * @return the WOResponse of the generic exception page.
	 */
	public WOResponse genericHandleException(Exception exception, WOContext context) {
		return super.handleException(exception, context);
	}

	/**
	 * Handles the potentially fatal OutOfMemoryError by quiting the application ASAP. Broken out into a separate method
	 * to make custom error handling easier, ie generating your own error pages in production, etc.
	 * 
	 * @param exception
	 *            to check if it is a fatal exception.
	 */
	public void handlePotentiallyFatalException(Exception exception) {
		Throwable throwable = ERXRuntimeUtilities.originalThrowable(exception);
		if (throwable instanceof Error) {
			boolean shouldQuit = false;
			if (throwable instanceof OutOfMemoryError) {
				shouldQuit = true;
				// AK: I'm not sure this actually works, in particular when the
				// buffer is in the long-running generational mem, but it's worth a try.
				// what we do is set up a last-resort buffer during startup
				if (lowMemBuffer != null) {
					Runtime.getRuntime().freeMemory();
					try {
						lowMemBuffer = null;
						System.gc();
						log.error("Ran out of memory, sending notification to clear caches");
						NSNotificationCenter.defaultCenter().postNotification(new NSNotification(LowMemoryNotification, this));
						shouldQuit = false;
						// try to reclaim our twice of our buffer
						// if this worked maybe we can continue running
						lowMemBuffer = new byte[lowMemBufferSize * 2];
						// shrink buffer to normal size
						lowMemBuffer = new byte[lowMemBufferSize];
					}
					catch (Throwable ex) {
						shouldQuit = true;
					}
				}
				// We first log just in case the log4j call puts us in a bad state.
				if (shouldQuit) {
					NSLog.err.appendln("Ran out of memory, killing this instance");
					log.error("Ran out of memory, killing this instance");
				}
			}
			else {
				// We first log just in case the log4j call puts us in a bad state.
				NSLog.err.appendln("java.lang.Error \"" + throwable.getClass().getName() + "\" occured. Can't recover, I'm killing this instance.");
				log.error("java.lang.Error \"" + throwable.getClass().getName() + "\" occured. Can't recover, I'm killing this instance.", throwable);
			}
			if (shouldQuit)
				Runtime.getRuntime().exit(1);
		}
	}

	/** use the redirect feature */
	protected Boolean useComponentActionRedirection;

	/**
	 * Set the <code>er.extensions.ERXComponentActionRedirector.enabled=true</code> property to actually the redirect
	 * feature.
	 * 
	 * @return flag if to use the redirect feature
	 */
	public boolean useComponentActionRedirection() {
		if (useComponentActionRedirection == null) {
			useComponentActionRedirection = ERXProperties.booleanForKey("er.extensions.ERXComponentActionRedirector.enabled") ? Boolean.TRUE : Boolean.FALSE;
		}
		return useComponentActionRedirection.booleanValue();
	}

	/**
	 * Overridden to allow for redirected responses.
	 * 
	 * @param request
	 *            object
	 * @param context
	 *            object
	 */
	public WOActionResults invokeAction(WORequest request, WOContext context) {
		WOActionResults results = super.invokeAction(request, context);
		if (useComponentActionRedirection()) {
			ERXComponentActionRedirector.createRedirector(results);
		}
		return results;
	}

	/**
	 * Overridden to allow for redirected responses.
	 * 
	 * @param response
	 *            object
	 * @param context
	 *            object
	 */
	public void appendToResponse(WOResponse response, WOContext context) {
		super.appendToResponse(response, context);
		if (useComponentActionRedirection()) {
			ERXComponentActionRedirector redirector = ERXComponentActionRedirector.currentRedirector();
			if (redirector != null) {
				redirector.setOriginalResponse(response);
			}
		}
	}

	/**
	 * Overridden to allow for redirected responses and null the thread local storage.
	 * 
	 * @param request
	 *            object
	 * @return response
	 */
	public WOResponse dispatchRequest(WORequest request) {
		WOResponse response = null;
		try {
			if (useComponentActionRedirection()) {
				ERXComponentActionRedirector redirector = ERXComponentActionRedirector.redirectorForRequest(request);
				if (redirector == null) {
					response = super.dispatchRequest(request);
					redirector = ERXComponentActionRedirector.currentRedirector();
					if (redirector != null) {
						response = redirector.redirectionResponse();
					}
				}
				else {
					response = redirector.originalResponse();
				}
			}
			else {
				response = super.dispatchRequest(request);
			}
			WOContext context = (WOContext) ERXThreadStorage.valueForKey("wocontext");
			if (context != null && context.request() != null) {
				if (ERXApplication.requestHandlingLog.isDebugEnabled()) {
					ERXApplication.requestHandlingLog.debug(context.request());
				}
			}
		}
		finally {
			// We always want to clean up the thread storage variables, so they don't end up on
			// someone else's thread by accident
			ERXThreadStorage.reset();
			// We *always* want to unlock left over ECs.
			ERXEC.unlockAllContextsForCurrentThread();
		}
		if (requestHandlingLog.isDebugEnabled()) {
			requestHandlingLog.debug("Returning, encoding: " + response.contentEncoding() + " response: " + response);
		}
		
		if (responseCompressionEnabled()) {
			String contentType = response.headerForKey("content-type");
			if ((contentType != null) && (contentType.startsWith("text/") || contentType.equals("application/x-javascript"))) {
				String acceptEncoding = request.headerForKey("accept-encoding");
				if ((acceptEncoding != null) && (acceptEncoding.toLowerCase().indexOf("gzip") != -1)) {
					NSData input = response.content();
					byte[] inputBytes = input._bytesNoCopy();
					long start = System.currentTimeMillis();
					byte[] compressedData = ERXCompressionUtilities.gzipByteArray(inputBytes);
					if (compressedData == null) {
						// something went wrong
					}
					else {
						response.setContent(new NSData(compressedData, new NSRange(0, compressedData.length), true));
						response.setHeader(String.valueOf(compressedData.length), "content-length");
						response.setHeader("gzip", "content-encoding");
						if (log.isDebugEnabled()) {
							log.debug("before: " + inputBytes.length + ", after " + compressedData.length + ", time: " + (System.currentTimeMillis() - start));
						}
					}
				}
			}
		}
		return response;
	}

	/**
	 * When a context is created we push it into thread local storage. This handles the case for direct actions.
	 * 
	 * @param request
	 *            the request
	 * @return the newly created context
	 */
	public WOContext createContextForRequest(WORequest request) {
		WOContext context = super.createContextForRequest(request);
		// We only want to push in the context the first time it is
		// created, ie we don't want to lose the current context
		// when we create a context for an error page.
		if (ERXThreadStorage.valueForKey("wocontext") == null) {
			ERXThreadStorage.takeValueForKey(context, "wocontext");
		}
		return context;
	}

	public WOResponse createResponseInContext(WOContext context) {
		WOResponse response = new ERXResponse();
		return response;
	}

	/**
	 * Override to perform any last minute cleanup before the application terminates. See
	 * {@class er.extensions.ERXGracefulShutdown ERXGracefulShutdown} for where this is called if signal handling is
	 * enabled. Default implementation calls terminate.
	 */
	public void gracefulTerminate() {
		terminate();
	}

	/**
	 * Logs the warning message if the main method was not called during the startup.
	 */
	private void _displayMainMethodWarning() {
		log.warn("\n\nIt seems that your application class " + application().getClass().getName() + " did not call " + ERXApplication.class.getName() + ".main(argv[], applicationClass) method. " + "Please modify your Application.java as the followings so that " + ERXConfigurationManager.class.getName() + " can provide its " + "rapid turnaround feature completely. \n\n" + "Please change Application.java like this: \n" + "public static void main(String argv[]) { \n" + "    ERXApplication.main(argv, Application.class); \n" + "}\n\n");
	}

	/** improved streaming support */
	protected NSMutableArray _streamingRequestHandlerKeys = new NSMutableArray(streamActionRequestHandlerKey());

	public void registerStreamingRequestHandlerKey(String s) {
		if (!_streamingRequestHandlerKeys.containsObject(s))
			_streamingRequestHandlerKeys.addObject(s);
	}

	public boolean isStreamingRequestHandlerKey(String s) {
		return _streamingRequestHandlerKeys.containsObject(s);
	}

	/** use the redirect feature */
	protected Boolean _useSessionStoreDeadlockDetection;

	/**
	 * Deadlock in session-store detection. Note that the detection only work in singlethreaded mode, and is mostly
	 * useful to find cases when a session is checked out twice in a single RR-loop, which will lead to a session store
	 * lockup. Set the <code>er.extensions.ERXApplication.useSessionStoreDeadlockDetection=true</code> property to
	 * actually the this feature.
	 * 
	 * @return flag if to use the this feature
	 */
	public boolean useSessionStoreDeadlockDetection() {
		if (_useSessionStoreDeadlockDetection == null) {
			_useSessionStoreDeadlockDetection = ERXProperties.booleanForKey("er.extensions.ERXApplication.useSessionStoreDeadlockDetection") ? Boolean.TRUE : Boolean.FALSE;
			if (isConcurrentRequestHandlingEnabled() && _useSessionStoreDeadlockDetection.booleanValue()) {
				log.error("Sorry, useSessionStoreDeadlockDetection does not work with concurrent request handling enabled.");
				_useSessionStoreDeadlockDetection = Boolean.FALSE;
			}
		}
		return _useSessionStoreDeadlockDetection.booleanValue();
	}

	/**
	 * Returns whether or not this application is in development mode.  This one is
	 * named "Safe" because it does not require you to be running an ERXApplication (and
	 * because you can't have a static and not-static method of the same name. bah).  If
	 * you are using ERXApplication, this will call isDevelopmentMode on your application.
	 * If not, it will call ERXApplication_defaultIsDevelopmentMode() which checks
	 * for the system properties "er.extensions.ERXApplication.developmentMode"
	 * and/or "WOIDE".
	 *  
	 * @return whether or not the current application is in development mode
	 */
	public static boolean isDevelopmentModeSafe() {
		boolean developmentMode;
		WOApplication application = WOApplication.application();
		if (application instanceof ERXApplication) {
			ERXApplication erxApplication = (ERXApplication)application;
			developmentMode = erxApplication.isDevelopmentMode();
		}
		else {
			developmentMode = ERXApplication._defaultIsDevelopmentMode();
		}
		return developmentMode;
	}

	/**
	 * Returns whether or not this application is running in development-mode. If you are using Xcode, you should add a
	 * WOIDE=Xcode setting to your launch parameters.
	 */
	protected static boolean _defaultIsDevelopmentMode() {
		boolean developmentMode = false;
		if (ERXProperties.stringForKey("er.extensions.ERXApplication.developmentMode") != null) {
			developmentMode = ERXProperties.booleanForKey("er.extensions.ERXApplication.developmentMode");
		}
		else {
			String ide = ERXProperties.stringForKey("WOIDE");
			if ("WOLips".equals(ide) || "Xcode".equals(ide)) {
				developmentMode = true;
			}
		}
		return developmentMode;
	}

	/**
	 * Returns whether or not this application is running in development-mode. If you are using Xcode, you should add a
	 * WOIDE=Xcode setting to your launch parameters.
	 */
	public boolean isDevelopmentMode() {
		return ERXApplication._defaultIsDevelopmentMode();
	}

	/** holds the info on checked-out sessions */
	private Hashtable _sessions = new Hashtable();

	/** Holds info about where and who checked out */
	private class SessionInfo {
		Exception _trace = new Exception();
		WOContext _context;

		public SessionInfo(WOContext wocontext) {
			_context = wocontext;
		}

		public Exception trace() {
			return _trace;
		}

		public WOContext context() {
			return _context;
		}

		public String exceptionMessageForCheckout(WOContext wocontext) {
			String contextDescription = null;
			if (_context != null) {
				contextDescription = "contextId: " + _context.contextID() + " request: " + _context.request();
			}
			else {
				contextDescription = "<NULL>";
			}

			log.error("There is an error in the session check-out: old context: " + contextDescription, trace());
			if (_context == null) {
				return "Original context was null";
			}
			else if (_context.equals(wocontext)) {
				return "Same context did check out twice";
			}
			else {
				return "Context with id '" + wocontext.contextID() + "' did check out again";
			}
		}
	}

	/** Overridden to check the sessions */
	public WOSession createSessionForRequest(WORequest worequest) {
		WOSession wosession = super.createSessionForRequest(worequest);
		if (useSessionStoreDeadlockDetection()) {
			_sessions.put(wosession.sessionID(), new SessionInfo(null));
		}
		return wosession;
	}

	/** Overridden to check the sessions */
	public void saveSessionForContext(WOContext wocontext) {
		if (useSessionStoreDeadlockDetection()) {
			WOSession wosession = wocontext._session();
			if (wosession != null) {
				String sessionID = wosession.sessionID();
				SessionInfo sessionInfo = (SessionInfo) _sessions.get(sessionID);
				if (sessionInfo == null) {
					throw new IllegalStateException("Check-In of session that was not checked out");
				}
				_sessions.remove(sessionID);
			}
		}
		super.saveSessionForContext(wocontext);
	}

	/** Overridden to check the sessions */
	public WOSession restoreSessionWithID(String sessionID, WOContext wocontext) {
		WOSession session;
		if (useSessionStoreDeadlockDetection()) {
			SessionInfo sessionInfo = (SessionInfo) _sessions.get(sessionID);
			if (sessionInfo != null) {
				throw new IllegalStateException(sessionInfo.exceptionMessageForCheckout(wocontext));
			}
			session = super.restoreSessionWithID(sessionID, wocontext);
			if (session != null) {
				_sessions.put(session.sessionID(), new SessionInfo(wocontext));
			}
		}
		else {
			session = super.restoreSessionWithID(sessionID, wocontext);
		}
		return session;
	}

	public Number sessionTimeOutInMinutes() {
		return new Integer(sessionTimeOut().intValue() / 60);
	}

	protected static final ERXFormatterFactory _formatterFactory = new ERXFormatterFactory();

	/** Getting formatters into KVC: bind to <code>application.formatterFactory.(60/#,##0.00)</code> */
	public ERXFormatterFactory formatterFactory() {
		return _formatterFactory;
	}

	protected Boolean _responseCompressionEnabled;

	/**
	 * checks the value of <code>er.extensions.ERXApplication.responseCompressionEnabled</code> and if true turns on
	 * response compression by gzip
	 */
	public boolean responseCompressionEnabled() {
		if (_responseCompressionEnabled == null) {
			_responseCompressionEnabled = ERXProperties.booleanForKeyWithDefault("er.extensions.ERXApplication.responseCompressionEnabled", false) ? Boolean.TRUE : Boolean.FALSE;
		}
		return _responseCompressionEnabled.booleanValue();
	}

	/**
	 * Returns an ERXMigrator with the lock owner name "appname-instancenumber".
	 */
	public ERXMigrator migrator() {
		return new ERXMigrator(name() + "-" + number());
	}
	
	/**
	 * This method is called by ERXWOContext and provides the application a hook to rewrite generated URLs.
	 * @param url the URL to rewrite
	 * @return the rewritten URL
	 */
	public String _rewriteURL(String url) {
		return url;
	}

	/**
	 * Set the default endocing of the app (message encodings)
	 * @param encoding
	 */
	public void setDefaultEncoding(String encoding) {
        WOMessage.setDefaultEncoding(encoding);
        WOMessage.setDefaultURLEncoding(encoding);
        ERXMessageEncoding.setDefaultEncoding(encoding);
        ERXMessageEncoding.setDefaultEncodingForAllLanguages(encoding);
	}
}