// ERXCompilerProxy
// Created by ak on Mon Mar 04 2002

package er.extensions;

import java.io.*;
import java.net.*;
import java.util.*;

import com.webobjects.appserver.*;
import com.webobjects.appserver._private.*;
import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;

/** ERXCompilerProxy is a class that creates a rapid-turnaround mode for WebObjects
5 for Java similar to how you use WebScript with WO 4.5.1. <br />
<p>Instead of recompiling
your Application or Frameworks, you simply change the source code in question
and reload your page. In some cases, you may also have to restart your Application
but you won't have to rebuild it.</p>
<p>ERXCompilerProxy is especially well suited for DirectToWeb applications. The
restrictions of Java's classloading are partially compensated by the highly
dynamic nature of D2W apps.</p>
<h2>Installation</h2>
<p>Check if you have <code>jikes</code> installed in <code>/usr/bin</code>. On
Windows, install <code>jikes.exe</code> in <code>$NEXT_ROOT/Local/Library/Executables/</code></p>
<h2>Usage</h2>
<p>To use this framework you must complete a few easy steps and you will very
seldom have to rebuild or even restart your project again:</p>
<ul>
<li>Add ERExtensions.framework and ERJars.framework to your project. You do
not need to add the ERExtensions.framework to your framework projects if you
only want to use the ERXCompilerProxy, but you need to add it to the application.
<br>
<li>In your <code>~/WebObjects.properties</code> file add a line
<p><code>er.extensions.ERXCompilerProxyEnabled=true</code></p>
<li>Then <b>either</b> create a file named <code>CPFileList.txt</code> in your
project's and your framework's root directory. This file should contain lines
with "<code>./project_relative_path_to_Someclass.java:package_of_SomeClass_or_empty&lt;return&gt;</code>"
for every file you want to get recompiled. On Windows, this is currently your
only option unless someone writes a shell script that mimics what the supplied
unix script does (find all java classes under the project root, grep for package
                  and class names and put it into a <code>CPFileList.txt</code> file)<br>
<li><b>or</b> create a new shell build phase right after the "Frameworks &amp;
Libraries":</p>
<ol>
<li> Set the current target to your app
<li> Click on "Files &amp; Build Phases" tab
<li> Choose "Project/New Build Phase/New Shell Script Build Phase" from
the Menu and drag the build phase right after the "Frameworks &amp; Libraries"
<li> Insert the code <code>
<pre>sh "${TARGET_BUILD_DIR}/ERExtensions.framework/Resources/InstallCompilerProxySupport.sh"</pre>
</code>
</ol>
Now, everytime you build this project, every source file directly under the
project directory will be put into the <code>CPFileList.txt</code> file.<br>
</ul>
That's all. Now build your app, make a change in Main.java and reload. You should
see the changed version and a message from ERXCompilerProxy in the console.
<h2>What it does</h2>
<p>The ERXCompilerProxy does several things to works it's magic. First, on startup,
it registers for the <code>ApplicationDidFinishLaunching</code> Notification.
When the App starts, it reads the <code>CPFileList.txt</code> files from your
<b>currently opened</b> projects to find out about the files to watch. So remember
to have your framework projects open in PB.</p>
<p>It also registers for the <code>ApplicationWillDispatchRequest</code> Notification,
which gets triggered whenever a page is requested. It tries to compile the files
that have changed and throws an Exception describing the errors if there where
any and then aborts further handling of the request - you will get an error
from your browser. However, if everything went well, it creates a new ClassLoader
and throws away the component definition cache and a few other things. Then,
when the next "pageWithName" is called, the updated class will be used.</p>
<p>This is slightly different from how WebScript handled things. In Java, you
simply can't change an Object's class implementation at runtime - so if you
already have a page (or any other Object) this object will not use the newer
class. Only Objects created after the compilation will have new behaviour.</p>
<p>The freshly compiled files will be put into your App's <code>Resources/Java</code>
directory into the proper package hierarchy. So, if you simply restart your
App, the newer files will be used. When you re-build your project, your normal
<code>.jar</code> will contains the correct classes anyway.</p>
<p><b>NOTE:</b> Since all new class files end up in <code>Resources/Java</code>,
when you clean-build your App, you also need to recompile your frameworks when
you have changed classes there! A normal build will not delete the files, so
recompilation of the frameworks is not needed.</p>
<h2>Knows bugs and limitations</h2>
<ul>
<li>Since Java can't redefine objects, the new code will get used only for new
objects. You may either have to reload multiple times or start a new session
to get things to work.
<li>You may see an error <code>"IllegalAccess in KeyValueCodingProtectedAccessor"</code>.
You will probably have defined a new variable in your component with protected
access. Either set the scope of the variable to public or restart your app.
The number of these messages is greatly reduced if your components and EOs
are in a package hierarchy and you have a KeyValueCodingProtectedAccessor
subclass for this package.
<li>You must have the respective projects opened in Project Builder. If you
don't, ERXCompilerProxy can't find the sources. Alternately, you can set the NSProjectSearchPath argument.
<li>You must reload after making changes and before stopping your application,
because the file modification times are read only at start. If you stop the
application before reloading, your last changes will not be recognized.
<li>If you change a class in a framework, the compiled file still ends up in your
app's Java/ directory. This is intentional, but means that you should clean-rebuild
your app if you rebuild your framework.
<li>Changes in the Application class (among others) will not be recognized before
a restart. They are only read once, due to the nature of Java's class loading
and can't be fixed. If you cache your classes, you need to register for <code>CompilerProxyDidCompileClassesNotification</code>
and reset the cache when you recieve it.
<li>If you make changes in your EO's classes, you should definitely restart
your application after pressing reload. You may seriously mess up your data
if you have a mix of previously instaniated Objects and new ones.
<li>Try not to use casts, use WOComponents and KeyValueCoding instead. Say you
have a List page and a Detail page and you change the Detail page. Further
you have code like:<br>
<pre>    public WOComponent detailsAction() {
    Details newPage = (Details)pageWithName(&quot;Details&quot;);
    newPage.setObject(currentObject);
    return newPage;
} </pre>
<br>
This doesn't work because List and the reference to Details is in the old
class loader, but pageWithName() instantiates from the new class loader. Rather use this code:
<pre>    public WOComponent detailsAction() {
    WOComponent newPage = pageWithName(&quot;Details&quot;);
    newPage.takeValueForKey(currentObject, "object");
    return newPage;
} </pre></ul>
 */

public class ERXCompilerProxy {

    /* logging support */
    protected static final ERXLogger log = ERXLogger.getERXLogger(ERXCompilerProxy.class.getName());
    protected static final ERXLogger classLoaderLog = ERXLogger.getERXLogger(ERXCompilerProxy.class.getName()+".loading");

    /** Notification you can register to when the Compiler Proxy reloads classes.
     * <br/>
     * The object is an array of classes that did recompiled since the last time the notification was sent.
     */

    public static final String CompilerProxyDidCompileClassesNotification = "CompilerProxyDidCompileClasses";

    /** 
     * denotes the name of  file which describes in each line a path to java class to watch for.<br />
     * The format of this file is:<br />
     * name.of.package:unix_path_to_java_file_relative_to_this_file&lt;return&gt;
     */
    protected static final String CPFileList = "CPFileList.txt";

    /** Path to the jikes binary.<br />
     */
    protected static String _jikesPath = "/usr/bin/jikes";

    /** Holds the Compilerproxy singleton
     */
    protected static ERXCompilerProxy _defaultProxy;

    /**
     * Holds the classpath of the current app.
     */
    protected static String _classPath;

    /**
     * Holds a boolean that tells wether an error should raise an Exception or only log the error.
     */
    protected static boolean _raiseOnError = false;

    protected boolean initialized = false;
    
    /**
     * Holds the files to watch.
     */
    protected NSMutableDictionary _filesToWatch;
    protected String _className;
    
    /**
     * Holds the path where the compiled <code>.class</code> files go.
     * Default is <code>Contents/Resources/Java</code>.
     */
    protected String _destinationPath;
    
    /**
     * Currently compiled classes.
     */
    protected NSMutableSet classFiles = new NSMutableSet();

    protected NSArray _projectSearchPath;
    
    /** 
     * Returns the Compiler Proxy Singleton.<br/>
     * Creates one if needed.
     * 
     * @return compiler proxy singleton
     */
    public static ERXCompilerProxy defaultProxy() {
        if(_defaultProxy == null)
            _defaultProxy = new ERXCompilerProxy();
        return _defaultProxy;
    }
    
    public static void setDefaultProxy(ERXCompilerProxy p) {
        if(_defaultProxy != null) {
            NSNotificationCenter.defaultCenter().removeObserver(_defaultProxy);
        }
        _defaultProxy = p;
        _defaultProxy.initialize();
    }

    protected String pathForCPFileList(String directory) {
        return directory + File.separator + CPFileList;
    }
    
    /** 
     * Returns an array of paths to the opened projects that have a <code>CPFileList.txt</code>.<br/>
     * This code is pretty fragile and subject to changes between versions of the dev-tools.
     * You can get around it by setting <code>NSProjectSearchPath</code> to the paths to your projects:
     * <code>(/Users/ak/Wonder/Common/Frameworks,/Users/Work)</code>
     * will look for directories with a CPFileList.txt in all loaded bundles.
     * So when you link to <code>ERExtensions.framework</code>,
     * <code>/Users/ak/Wonder/Common/Frameworks/ERExtensions</code> will get found.
     * 
     * @return paths to opened projects
     */

    public String projectInSearchPath(String bundleName) {
        for(Enumeration e = _projectSearchPath.objectEnumerator(); e.hasMoreElements();) {
            String path = e.nextElement() + File.separator + bundleName;
            if(new File(pathForCPFileList(path)).exists()) {
                return path;
            }
        }
        return null;
    }

    protected NSArray projectPaths() {
	NSArray frameworkNames = (NSArray)NSBundle.frameworkBundles().valueForKey("name");
	NSMutableArray projectPaths = new NSMutableArray();
	String mainProject = null;
	String mainBundleName = NSBundle.mainBundle().name();

        WOProjectBundle mainBundle = WOProjectBundle.projectBundleForProject(mainBundleName,false);
        if(mainBundle == null) {
            mainProject = projectInSearchPath(mainBundleName);
            if(mainProject == null)
                mainProject = "../..";
        } else {
            mainProject = mainBundle.projectPath();
        }
        if(mainProject == null || mainProject.equals("")){
            // Assuming that bundle path is $PROJECT_DIR/build/foo.woa
            mainProject = new File(mainBundle.bundlePath()).getParentFile().getParentFile().getAbsolutePath();
        }
        if((new File(pathForCPFileList(mainProject))).exists()) {
	    log.debug("Found open project for app at path " + mainProject);
	    projectPaths.addObject(mainProject);
	}
	for(Enumeration e = frameworkNames.objectEnumerator(); e.hasMoreElements();) {
	    String name = (String)e.nextElement();
            WOProjectBundle bundle = WOProjectBundle.projectBundleForProject(name, true);
            String path;

            if(bundle != null) {
                path = bundle.projectPath();
                if(path == null || path.equals("")){
                    // Assuming that bundle path is $PROJECT_DIR/build/foo.framework
                    path = new File(bundle.bundlePath()).getParentFile().getParentFile().getAbsolutePath();
                }
            } else {
                path = projectInSearchPath(name);
            }
            if(path != null) {
                File f = new File(pathForCPFileList(path));
                if(f.exists()) {
                    log.debug("Found open project for framework '" +name+ "' at path " + path);
                    projectPaths.addObject(path);
                }
            }
        }
	return projectPaths;
    }

    /** 
     * Returns the class registered for the name <code>className</code>.<br/>
     * Uses the private WebObjects class cache.
     * 
     * @param className class name
     * @return class for the registered name or null
     */
    public Class classForName(String className) {
        return ERXPatcher.classForName(className);
    }

    /** 
     * Sets the class registered for the name <code>className</code> to the given class.<br/>
     * Changes the private WebObjects class cache.
     * 
     * @param clazz class object
     * @param className name for the class - normally clazz.getName()
     */
    public void setClassForName(Class clazz, String className) {
        ERXPatcher.setClassForName(clazz, className);
    }

    /** 
     * Initializes the CopilerProxy singleton.<br/>
     * Registers for ApplicationWillDispatchRequest notification.
     */
    public void initialize() {
	if (initialized) {
	    return;
	}
        log.debug("initialize start");

        initialized = true;
        
        if(WOApplication.application()!=null && WOApplication.application().isCachingEnabled()) {
            log.debug("I assume this is deployment mode, rapid-turnaround mode is disabled");
            _filesToWatch = new NSMutableDictionary();
            return;
        }

        if(!ERXProperties.booleanForKeyWithDefault("er.extensions.ERXCompilerProxyEnabled", false)) {
            log.debug("Rapid-turnaround mode is disabled, set 'er.extensions.ERXCompilerProxyEnabled=true' in your WebObjects.properties to enable it.");
            _filesToWatch = new NSMutableDictionary();
            return;
	}

        boolean isWindows = File.pathSeparatorChar == ';';
        // Oh, yuck! Please comebody come up with a better way...
        if(isWindows) {
            _jikesPath = System.getProperty("NEXT_ROOT") + "/Local/Library/Executables/jikes.exe";
        }

        if(!(new File(_jikesPath)).exists()) {
            log.error("Can't use compiler proxy, no jikes found at path :<" + _jikesPath + ">");
            return;
        }
        
        String classPathSeparator = ":";
        if(isWindows)
            classPathSeparator = ";";
	_classPath = System.getProperty("com.webobjects.classpath");
	if ( _classPath != null && _classPath.length() > 0) {
	    if (System.getProperty("java.class.path") != null && System.getProperty("java.class.path").length() > 0) {
		System.setProperty("java.class.path", _classPath + classPathSeparator + System.getProperty("java.class.path"));
	    } else {
		System.setProperty("java.class.path", _classPath);
	    }
	    _classPath = System.getProperty("java.class.path");
	} else {
	    _classPath = System.getProperty("java.class.path");
	}
	//end of fix
	
        if(_classPath.indexOf("Classes/classes.jar") < 0 && !isWindows) {
            // (ak) We need this when we do an Ant build, until WOProject is fixed to include classes.jar
            // This wouldn't work on windows of course, but then again, the rest of this class doesn't, too.
            String systemRoot = "/System/Library/Frameworks/JavaVM.framework/Classes/";
            _classPath += classPathSeparator + systemRoot + "classes.jar";
            _classPath += classPathSeparator + systemRoot + "ui.jar";
        }

        _raiseOnError = ERXProperties.booleanForKey("CPRaiseOnError");

	NSArray projectPaths = projectPaths();
        if(projectPaths.count() == 0) {
            log.info("No open projects found with a CPFileList.txt");
            _filesToWatch = new NSMutableDictionary();
	    return;
        }
        if(_classPath.indexOf(".woa") == -1) {
            log.info("Sorry, can't find the .woa wrapper of this application. There is no support for the CompilerProxy in servlet deployment, will try to get it via NSBundle.");
	    log.info("java.class.path="+_classPath);
        }

	NSBundle b = NSBundle.mainBundle();
	String path = b.resourcePath();
	if (path.indexOf(".woa") == -1) {
            log.info("Sorry, can't find the .woa wrapper of this application. There is no support for the CompilerProxy in servlet deployment.");
	    log.info("mainBundle.resourcePath="+path);
	}
	//_classPath = path;
	//_destinationPath = _classPath.substring(0,_classPath.indexOf(".woa")) + ".woa/Contents/Resources/Java/";
	_destinationPath = path.substring(0, path.indexOf(".woa")) + ".woa/Contents/Resources/Java/";
	_filesToWatch = new NSMutableDictionary();
	for(Enumeration e = projectPaths.objectEnumerator(); e.hasMoreElements();) {
	    String sourcePath = (String)e.nextElement();
	    String fileListPath = pathForCPFileList(sourcePath);
	    String fileList = _NSStringUtilities.stringFromFile(fileListPath, _NSStringUtilities.ASCII_ENCODING);

	    NSArray allFiles = NSArray.componentsSeparatedByString(fileList, "\n");
	    for(Enumeration sourceFiles = allFiles.objectEnumerator(); sourceFiles.hasMoreElements();) {
		String line = (String)sourceFiles.nextElement();
                line = line.trim();
		String packageName = "";
		String sourceName = "";
                try {
                    NSArray items = NSArray.componentsSeparatedByString(line, ":");
                    if(items.count() > 1) {
                        sourceName = (String)items.objectAtIndex(0);
                        packageName = (String)items.objectAtIndex(1);
                        CacheEntry entry = new CacheEntry(sourcePath, sourceName, packageName);
                        _filesToWatch.setObjectForKey((Object)entry,entry.classNameWithPackage());
                        if(entry.classFile() != null)
                            classFiles.addObject(entry);
                        if(WOApplication.application().isDebuggingEnabled()) {
                            log.debug("fileToWatch:" + entry.classNameWithPackage());
                        }
                    }
                } catch (Exception ex) {
                    log.debug("initializeOnNotification: error parsing " +fileListPath+ " line '" + line +"':"+ ex);
		}
	    }
	}
	NSNotificationCenter.defaultCenter().addObserver(this, new NSSelector("checkAndCompileOnNotification", new Class[] { NSNotification.class } ), WOApplication.ApplicationWillDispatchRequestNotification, null);
        log.info("initialize end");
    }

    /** 
     * Contructor - does nothing special.
     */
    public ERXCompilerProxy() {
        _projectSearchPath = ERXProperties.arrayForKeyWithDefault("NSProjectSearchPath", NSArray.EmptyArray);
    }

    /** 
     * Method that will be called upon <code>ApplicationWillDispatchRequest</code>.<br/>
        * Checks if the request is not a resource request and then calls {@link #checkAndCompileOnNotification()}
     * 
     * @param theNotification notification sent upon 
     *     ApplicationWillDispatchRequest
     */
    public void checkAndCompileOnNotification(NSNotification theNotification) {
        //log.debug("Received ApplicationWillDispatchRequestNotification");
        WORequest r = (WORequest)theNotification.object();
        String key = "/" + WOApplication.application().resourceRequestHandlerKey();
        // log.info(r.uri() + " - " + key);
        if(!(r.uri().indexOf(key) > 0))
            checkAndCompileAllClasses();
    }

    /** 
     * Main magic bullet routine.<br/>
     * 
     * You don't need to understand what it does, in fact you don't even want to...it will be certainly different tomorrow.
     */
    void checkAndCompileAllClasses() {
        CacheEntry cacheEntry;
        Enumeration e = _filesToWatch.objectEnumerator();
        NSMutableArray filesToCompile  = new NSMutableArray();
        while(e.hasMoreElements()) {
            cacheEntry = (CacheEntry)e.nextElement();
            if(cacheEntry.needsRefresh()) {
                filesToCompile.addObject(cacheEntry);
            }
        }
        if(filesToCompile.count() > 0) {
            Compiler compiler = new Compiler(filesToCompile.objects(), _destinationPath);
            if(compiler.compile()) {
                e = filesToCompile.objectEnumerator();
		log.debug("after compile: classFiles="+classFiles);
                while(e.hasMoreElements()) {
                    cacheEntry = (CacheEntry)e.nextElement();
                    classFiles.addObject(cacheEntry);
                }
                boolean didReset = false;
                boolean didResetModelGroup = false;
                CompilerClassLoader cl = null;
		log.debug("classFiles="+classFiles);
                e = classFiles.objectEnumerator();
                NSMutableDictionary kvcAccessors = new NSMutableDictionary();
                while(e.hasMoreElements()) {
                    cacheEntry = (CacheEntry)e.nextElement();
                    String className = cacheEntry.classNameWithPackage();
                    try {
                        if(cl == null)
			    cl = new CompilerClassLoader(_destinationPath, activeLoader);
                        //   Object o = Class.forName(className).newInstance();
                        Class class_ = cl.loadClass(className, true);

                        // the whole magic is in these lines
                        Class oldClass_ = ERXPatcher.classForName(cacheEntry.className());
                        ERXPatcher.setClassForName(class_, className);
                        if(oldClass_ != null && !cacheEntry.className().equals(className)) {
                            ERXPatcher.setClassForName(class_, cacheEntry.className());
                        }
                        if(!didReset) {
                            com.webobjects.appserver.WOApplication.application()._removeComponentDefinitionCacheContents();
                            NSKeyValueCoding.DefaultImplementation._flushCaches();
                            NSKeyValueCoding._ReflectionKeyBindingCreation._flushCaches();
                            NSKeyValueCoding.ValueAccessor._flushCaches();
                            didReset = true;
                        }

                        if(cacheEntry.packageName().length() > 0 && false) {
                            Object kvc = kvcAccessors.objectForKey(cacheEntry.packageName());
                            if(kvc == null) {
                                try {
                                    Class kvcAccessor = cl.reloadClass(cacheEntry.packageName() + ".KeyValueCodingProtectedAccessor");
                                    log.info("KVC for " + cacheEntry.packageName() + ": " + kvcAccessor);
                                    if(kvcAccessor != null) {
                                        log.info("Classloaders :" + kvcAccessor.getClassLoader() + " vs " + class_.getClassLoader());
                                        kvcAccessors.setObjectForKey(kvcAccessor, cacheEntry.packageName());
                                        NSKeyValueCoding.ValueAccessor.setProtectedAccessorForPackageNamed((NSKeyValueCoding.ValueAccessor)kvcAccessor.newInstance(), cacheEntry.packageName());
                                    } else {
                                        kvcAccessors.setObjectForKey("<no entry>", cacheEntry.packageName());
                                    }
                                } catch(Exception kvcException) {
                                    log.info("Error setting KVC accessor:" + kvcException);
                                }
                            }
                        }
                        
                        if(WODirectAction.class.isAssignableFrom(class_)) {
                            WOApplication app = WOApplication.application();
                            WORequestHandler currentDAHandler = app.requestHandlerForKey(app.directActionRequestHandlerKey());
                            WODirectActionRequestHandler handler = null;
                            try {
                                Class[] paramArray = new Class[] { String.class, String.class, Boolean.TYPE };
                                java.lang.reflect.Constructor constructor = currentDAHandler.getClass().getConstructor(paramArray);
                                handler = (WODirectActionRequestHandler)constructor.newInstance(new Object[] { cacheEntry.className(), "default", Boolean.FALSE } );
                            } catch(Exception ex) {
                                log.error("Failed to instantiate DA Handler: " + ex);
                            }
                            boolean directActionIsDefault = currentDAHandler == app.defaultRequestHandler();
                            app.registerRequestHandler(handler, app.directActionRequestHandlerKey());
                            if(directActionIsDefault)
                                app.setDefaultRequestHandler(handler);
                            log.info("WODirectAction loaded: "+ cacheEntry.className());
                        }
                        if(WOComponent.class.isAssignableFrom(class_)) {
                            WOContext context = ERXWOContext.newContext();
                            WOApplication.application().pageWithName(cacheEntry.className(), context)._componentDefinition().componentInstanceInContext(context); // mark an instance as locked
                        }
                        if(EOEnterpriseObject.class.isAssignableFrom(class_) && !didResetModelGroup) {
                            EOModelGroup.setDefaultGroup(ERXModelGroup.modelGroupForLoadedBundles());
                            didResetModelGroup = true;
                        }
                        if(ERXCompilerProxy.class.getName().equals(className)) {
                            try {
                                ERXCompilerProxy.setDefaultProxy((ERXCompilerProxy)class_.newInstance());
                            } catch(Exception ex) {
                                log.error("Can't reload compiler proxy", ex);
                            }
                        }
                        cacheEntry.update();
                        // sparkle dust ends here
                        // keep compiler happy
                        if(false) throw new InstantiationException();
                    } catch(InstantiationException ex) {
                        log.debug("Can't load " + className + ", it's probably abstract.");
                    } catch(ClassNotFoundException ex) {
                        throw new RuntimeException("Could not load the class "+ className + " with exception:" + ex.toString());
                    }
                }
                NSNotificationCenter.defaultCenter().postNotification(CompilerProxyDidCompileClassesNotification, classFiles);
            }
        }
    }

    /** 
     * Tests whether or not the class name with package is contained in the set of 
     * CacheEntry objects. Convenient to check if a specific class was recompiled 
     * in a CompilerProxyDidCompileClassesNotification. 
     *
     * @param classNameWithPackage   string of the class name
     * @param cacheEntries   NSSet contains CacheEntry objects; 
     *                       typically obtained by a notification's object() method 
     * @return if the classNameWithPackage is contained by cacheEntries
     */ 
    public static boolean isClassContainedBySet(String classNameWithPackage, NSSet cacheEntries) {
        boolean isContained = false;
        Enumeration e = cacheEntries.objectEnumerator();
        while (e.hasMoreElements()) {
            CacheEntry cacheEntry = (CacheEntry)e.nextElement();
            if (cacheEntry.classNameWithPackage().equals(classNameWithPackage)) {
                isContained = true;
                break;
            }
        }
        return isContained;
    }

    class CacheEntry {
        String _path;
        String _className;
        String _packageName;
        long _lastModified;
        File _sourceFile;
        File _classFile;

	/** @param basePath 
	 * @param path 
	 * @param packageName 
	 */
        public CacheEntry(String basePath, String path, String packageName) {
            _className = NSPathUtilities.lastPathComponent(path);
            _className = _className.substring(0,_className.indexOf("."));
            if(path.startsWith("/"))
                _path = path;
            else
                _path = basePath +"/"+ path;
            _sourceFile = new File(_path);
            _packageName = packageName;
            _lastModified = _sourceFile.lastModified();
        }

        public void update() {
            _lastModified = _sourceFile.lastModified();
        }

        public File classFile() {
            String fileName = _className.replace('.', File.separatorChar) + ".class";
            File f = new File( _destinationPath + File.separatorChar + fileName);
            if (f.exists() && f.isFile() && f.canRead()) {
                return f;
            }
            return null;
        }

        public String className() {
            return _className;
        }

        public String classNameWithPackage() {
            if(_packageName.length() == 0)
                return _className;
            return _packageName + "." + _className;
        }

        public String path() {
            return _path;
        }

        public String packageName() {
            return _packageName;
        }
        
        public boolean needsRefresh() {
            if(_sourceFile.exists()) {
                if(classFile() != null && classFile().lastModified() < _sourceFile.lastModified()) {
                    return true;
                }
                if(_lastModified < _sourceFile.lastModified()) {
                    return true;
                }
            }
            return false;
        }
        
        public void didRefresh() {
            _lastModified = _sourceFile.lastModified();
            log.info("Did refresh " + _path);
        }
        
        public String toString() {
            return "( className = '" + _className + "'; path = '" + _path + "';)\n";
        }
    }

    class Compiler {
        Object _files[];
        String _destinationPath;

        public Compiler(Object files[], String destinationPath) {
            _destinationPath = destinationPath;
            _files = files;
        }

        String[] commandArray() {
            String base[]  = new String[] { _jikesPath, "+E", "-g", "-d", _destinationPath, "-classpath", _destinationPath + ":" + _classPath};
            String commandLine [] =  new String [base.length+_files.length];
            for(int i = 0; i < base.length; i++ ) {
                commandLine[i] = base[i];
            }
            for(int i = base.length; i < base.length + _files.length; i++ ) {
                commandLine[i] = ((CacheEntry)_files[i-base.length]).path();
            }
            return commandLine;
        }

	String commandLine() {
            String base[]  = new String[] { _jikesPath, "+E", "-g", "-d", _destinationPath, "-classpath", _destinationPath + ":" + _classPath};
            String commandLine = "";

            for(int i = 0; i < base.length; i++ ) {
                commandLine = commandLine + " " + base[i];
            }
            for(int i = base.length; i < base.length + _files.length; i++ ) {
                commandLine = commandLine + " " + ((CacheEntry)_files[i-base.length]).path();
            }
            return commandLine;
        }

	public boolean compile() throws IllegalArgumentException {
            Process jikesProcess=null;
	    for (int i = 0; i < commandArray().length; i++) {
		log.debug("*** compiling:" + commandArray()[i]);
	    }
	    try {
                jikesProcess = Runtime.getRuntime().exec(commandArray());
                jikesProcess.waitFor();
                if (jikesProcess.exitValue() != 0) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(jikesProcess.getErrorStream()));
                    StringBuffer output = new StringBuffer();
                    String outputLine;
                    do {
                        outputLine = br.readLine();
                        if (outputLine != null && outputLine.length() > 0 )// && outputLine.startsWith("*** Caution"))
                            output.append(outputLine + "\n");
                    } while (outputLine != null);
                    if(_raiseOnError) {
                        throw new RuntimeException("Jikes returned an  error: " + output.toString());
                    } else {
                        log.error(output.toString());
                        jikesProcess.destroy();
                        return false;
                    }
                } else {
                    // We do nothing here because we except no output from jikes
                }
            } catch (IOException e) {
                log.error("Compiler: IOException: " + e.toString());
            } catch (InterruptedException e) {
                log.error("Compiler: Interrupted process: " + e.toString());
            } finally {
                ERXExtensions.freeProcessResources(jikesProcess);
            }
            return true;
        }
    }

    CompilerClassLoader activeLoader = null;
    
    class CompilerClassLoader extends ClassLoader {
        protected String _classpath;
        protected String _destinationPath;
        protected CompilerClassLoader _parent;
        
        public CompilerClassLoader(String destinationPath, CompilerClassLoader parent) {
            super();
            _destinationPath = destinationPath;
            _parent = parent;
            activeLoader = this;
	}

        public synchronized Class loadClass(String name, boolean resolveIt) throws ClassNotFoundException {
            try {
                return findClass(name);
            } catch(ClassNotFoundException ex) {
                if(_parent == null)
                    return super.loadClass(name, resolveIt);
                return _parent.loadClass(name, resolveIt);
            }
        }

        public File findClassFile(String name) {
            String fileName = name.replace('.', File.separatorChar) + ".class";
            File f = new File( _destinationPath + File.separatorChar + fileName);
            if (f.exists() && f.isFile() && f.canRead()) {
                classLoaderLog.debug("CompilerClassLoader.findClassFile:" + name + " found");
                return f;
            }
	    classLoaderLog.debug("CompilerClassLoader.findClassFile:" + _destinationPath + File.separatorChar + fileName + " NOT found");
            return null;
        }


	/** @param name 
	 * @exception ClassNotFoundException 
	 */
        protected Class findClass(String name) throws ClassNotFoundException {
	    //fix by david teran, cluster9
	    //this checks if the name from the class is one of the files that were
	    //compiled. if not, then this findClass method will fail, we must use the
	    //normal class loader to find the class.
            boolean contains = false;
            Enumeration e = classFiles.objectEnumerator();
            while (e.hasMoreElements()) {
                CacheEntry ce = (CacheEntry)e.nextElement();
                if (name.startsWith(ce.classNameWithPackage())) {
                    classLoaderLog.debug("CompilerClassLoader.findClass:" + name + " is in array");
                    contains = true;
                    break;
                }
            }
            
	    if (contains) {
		byte buffer[] = null;
		Class newClass;
		File classFile = findClassFile(name);
		if (classFile == null) {
		    throw new ClassNotFoundException(name);
		}
		try {
		    FileInputStream in = new FileInputStream(classFile);
		    int length = in.available();
		    if (length == 0) {
			throw new ClassNotFoundException(name);
		    }
		    buffer = new byte[length];
		    in.read(buffer);
		    in.close();
		} catch (IOException iox) {
		    classLoaderLog.debug(iox);
		    throw new ClassNotFoundException(iox.getMessage());
		}
		try {
		    newClass = defineClass(name, buffer, 0, buffer.length);
		    classLoaderLog.info("Did load class: " + name);
		} catch (Throwable t) {
		    classLoaderLog.debug(t);
		    throw new RuntimeException(t.getMessage());
		}
		return newClass;
            } else {
                //this class is a class that must be accessable with the normal classloader
                //it cannot be found in the filesystem because it was not compiled by CP
                Class clazz = null;
                clazz = Class.forName(name);

                if (clazz != null) {
		    return clazz;
		} else {
		    throw new IllegalStateException("could not get class "+name+" with classForName method!");
		}
	    }
	}
	/** @param name 
	 */
        public URL getResource(String name) {
            return ClassLoader.getSystemResource(name);
        }
        /** Tries to re-load the given class into the current class loader. It is used to push the KeyValueCodingProtectedAccessor class back into the system.*/
        public Class reloadClass(String className) {
            Class c = null;
            try {
                byte buffer[] = null;
                String fileName = className.replace('.', '/') + ".class";
                InputStream in = null;
                File classFile = findClassFile(className);
                if(classFile != null) {
                    in = new FileInputStream(classFile);
                } else {
                    in = getResource(fileName).openStream();
                }
                int length = in.available();
                if (length != 0) {
                    buffer = new byte[length];
                    in.read(buffer);
                    in.close();
                }
                if(buffer != null) {
                    log.info(className + ":" + buffer.length + "," + buffer);
                    c = defineClass(className, buffer, 0, buffer.length);
                }
            } catch (Throwable t) {
                log.warn("Error reloading class "+className+": " + t);
            }
            return c;
        }

    }


    private static URL[] mangleClasspathForClassLoader(String s) {
	String s1 = File.pathSeparatorChar != ';' ? "file://" : "file:///";
	NSArray paths = NSArray.componentsSeparatedByString(_classPath, ":");
	URL aurl[] = new URL[paths.count()];
	for(int i = 0; i < paths.count(); i++) {
	    try {
		aurl[i] = new URL(s1 + paths.objectAtIndex(i).toString());
	    }
	    catch(Throwable throwable) {
		throw new RuntimeException("Error creating URL " + throwable);
	    }
	}

	return aurl;
    }
}
