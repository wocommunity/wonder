// ERXCompilerProxy
// Created by ak on Mon Mar 04 2002

package er.extensions;

import java.io.*;
import java.net.*;
import java.lang.*;
import java.util.*;
import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import org.apache.log4j.*;

/* To find out about how to use this package, read the README.html in the resources folder*/
/* WARNING: this is experimental and the results might be very confusing if you don«t understand what this class tries to do! */

public class ERXCompilerProxy {
    static final Category cat = Category.getInstance(ERXCompilerProxy.class.getName());
    public static final String CompilerProxyDidCompileClassesNotification = "CompilerProxyDidCompileClasses";

    /** CPFileList is the file which describes in each line a path to java class to watch for*/
    static final String CPFileList = "CPFileList.txt";

    static final String _jikesPath = "/usr/bin/jikes";

    static ERXCompilerProxy _defaultProxy;

    static String _classPath;
    static boolean _raiseOnError = false;

    NSMutableDictionary _filesToWatch;
    String _className;
    String _destinationPath;

    static ERXCompilerProxy defaultProxy() {
        if(_defaultProxy == null)
            _defaultProxy = new ERXCompilerProxy();
        return _defaultProxy;
    }

    NSArray projectPaths() {
	NSArray frameworkNames = (NSArray)NSBundle.frameworkBundles().valueForKey("name");
	NSMutableArray projectPaths = new NSMutableArray();
	String mainProject = null;
	String mainBundleName = NSBundle.mainBundle().name();
	try {
	    // horrendous hack to avoid having to set the NSProjectPath manually.
	    mainProject = com.webobjects.appserver._private.WOProjectBundle.projectBundleForProject(mainBundleName,false).projectPath();
	} catch(Exception ex) {
	    cat.warn("projectPaths exception: " + ex + " in "+ mainBundleName);
	}
	if(mainProject == null) {
	    mainProject = "../..";
	}
	if((new File(mainProject + "/CPFileList.txt")).exists()) {
	    cat.info("Found open project for app at path " + mainProject);
	    projectPaths.addObject(mainProject);
	}
	for(Enumeration e = frameworkNames.objectEnumerator(); e.hasMoreElements();) {
	    String path = (String)e.nextElement();
	    cat.debug("Checking if framework '" +path+ "' has open project...");
	    com.webobjects.appserver._private.WOProjectBundle bundle = com.webobjects.appserver._private.WOProjectBundle.projectBundleForProject(path, true);
	    if(bundle != null) {
		File f = new File(bundle.projectPath() + "/CPFileList.txt");
		if(f.exists()) {
		    cat.info("Found open project for framework '" +path+ "' at path " + bundle.projectPath());
		    projectPaths.addObject(bundle.projectPath());
		}
	    }
	}
	return projectPaths;
    }

    public void initialize() {
        if(WOApplication.application().isCachingEnabled()) {
            cat.info("I assume this is deployment mode, rapid-turnaround mode is disabled");
            _filesToWatch = new NSMutableDictionary();
            return;
        }

	if(!ERXUtilities.booleanValueWithDefault(System.getProperty("er.extensions.ERXCompilerProxyEnabled"), false)) {
            cat.info("Rapid-turnaround mode is disabled, set 'er.extensions.ERXCompilerProxyEnabled=true' in your WebObjects.properties to enable it.");
            _filesToWatch = new NSMutableDictionary();
            return;
	}
	
        cat.debug("initialize");

        _classPath = System.getProperty("java.class.path");

        _raiseOnError = System.getProperty("CPRaiseOnError") != null;

	NSArray projectPaths = projectPaths();
        if(projectPaths.count() == 0) {
            cat.info("No open projects found with a CPFileList.txt");
            _filesToWatch = new NSMutableDictionary();
	    return;
        }
	_destinationPath = _classPath.substring(0,_classPath.indexOf(".woa")) + ".woa/Contents/Resources/Java/";
	_filesToWatch = new NSMutableDictionary();
	for(Enumeration e = projectPaths.objectEnumerator(); e.hasMoreElements();) {
	    String sourcePath = (String)e.nextElement();
	    String fileListPath = sourcePath + "/CPFileList.txt";
	    String fileList = _NSStringUtilities.stringFromFile(fileListPath, _NSStringUtilities.ASCII_ENCODING);

	    NSArray allFiles = NSArray.componentsSeparatedByString(fileList, "\n");
	    for(Enumeration sourceFiles = allFiles.objectEnumerator(); sourceFiles.hasMoreElements();) {
		String line = (String)sourceFiles.nextElement();
		String packageName = "";
		String sourceName = "";
		try {
		    NSArray items = NSArray.componentsSeparatedByString(line, ":");
		    sourceName = (String)items.objectAtIndex(0);
		    if(items.count() > 1)
			packageName = (String)items.objectAtIndex(1);
		    CacheEntry entry = new CacheEntry(sourcePath, sourceName, packageName);
		    _filesToWatch.setObjectForKey((Object)entry,entry.classNameWithPackage());
		    if(WOApplication.application().isDebuggingEnabled()) {
			cat.debug("fileToWatch:" + entry.classNameWithPackage());
		    }
		} catch (Exception ex) {
		    cat.debug("initializeOnNotification:" + ex);
		}
	    }
	}
	NSNotificationCenter.defaultCenter().addObserver(this, new NSSelector("checkAndCompileOnNotification", new Class[] { NSNotification.class } ), WOApplication.ApplicationWillDispatchRequestNotification, null);
    }

    public ERXCompilerProxy() {
    }

    public void checkAndCompileOnNotification(NSNotification theNotification) {
        //cat.debug("Received ApplicationWillDispatchRequestNotification");
        checkAndCompileAllClasses();
    }

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
                CompilerClassLoader cl = null;
                e = filesToCompile.objectEnumerator();
                while(e.hasMoreElements()) {
                    cacheEntry = (CacheEntry)e.nextElement();
                    String className = cacheEntry.classNameWithPackage();
                    try {
                        if(cl == null)
			    cl = new CompilerClassLoader(_destinationPath);
                        //   Object o = Class.forName(className).newInstance();
                        Class class_ = cl.loadClass(className, true);
                        // the whole magic is in these two lines
                        com.webobjects.foundation._NSUtilities.setClassForName(class_, className);
                        com.webobjects.appserver.WOApplication.application()._removeComponentDefinitionCacheContents();
                        cacheEntry.update();
                        // sparkle dust ends here

			NSNotificationCenter.defaultCenter().postNotification(CompilerProxyDidCompileClassesNotification, className);

                    } catch(ClassNotFoundException ex) {
                        throw new RuntimeException("Could not load the class "+ className + " with exception:" + ex.toString());
                    }
                }
            }
        }
    }

    class CacheEntry {
        String _path;
        String _className;
        String _packageName;
        long _lastModified;
        File _sourceFile;
        File _classFile;

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

        public boolean needsRefresh() {
            if(_sourceFile.exists() && _lastModified < _sourceFile.lastModified()) {
                return true;
            }
            return false;
        }

        public void didRefresh() {
            _lastModified = _sourceFile.lastModified();
            cat.info("Did refresh " + _path);
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
            String base[]  = new String[] { _jikesPath, "-g", "-d", _destinationPath, "-classpath", _destinationPath + ":" + _classPath};
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
            String base[]  = new String[] { _jikesPath, "-g", "-d", _destinationPath, "-classpath", _destinationPath + ":" + _classPath};
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
            cat.debug("*** compiling:" + commandLine());
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
                        cat.error(output.toString());
                        jikesProcess.destroy();
                        return false;
                    }
                } else {
                    // We do nothing here because we except no output from jikes
                }
            } catch (IOException e) {
                cat.error("Compiler: IOException: " + e.toString());
            } catch (InterruptedException e) {
                cat.error("Compiler: Interrupted process: " + e.toString());
            } finally {
                if (jikesProcess != null)
                    jikesProcess.destroy();
            }
            return true;
        }
    }

    class CompilerClassLoader extends ClassLoader {
        protected String _classpath;
        protected String _destinationPath;

        public CompilerClassLoader(String destinationPath) {
            super();
            _destinationPath = destinationPath;
	}

        public synchronized Class loadClass(String name, boolean resolveIt) throws ClassNotFoundException {
            try {
                return findClass(name);
            } catch(ClassNotFoundException ex) {
                return super.loadClass(name, resolveIt);
            }
        }

        public File findClassFile(String name) {
            String fileName = name.replace('.', File.separatorChar) + ".class";
            File f = new File( _destinationPath + File.separatorChar + fileName);
            if (f.exists() && f.isFile() && f.canRead()) {
                cat.debug("CompilerClassLoader.findClassFile:" + name);
                return f;
            }
            return null;
        }


        protected Class findClass(String name) throws ClassNotFoundException {
            File classFile = findClassFile(name);
            if (classFile == null) {
                throw new ClassNotFoundException(name);
            }
            Class newClass;
            try {
                FileInputStream in = new FileInputStream(classFile);
                int length = in.available();
                if (length == 0) {
                    throw new ClassNotFoundException(name);
                }
                byte buffer[] = new byte[length];
                in.read(buffer);
                in.close();
                newClass = defineClass(name, buffer, 0, buffer.length);
                cat.info("Did load class: " + name);
            } catch (IOException e) {
                throw new ClassNotFoundException(e.getMessage());
            }
            return newClass;
        }

        public URL getResource(String name) {
            return ClassLoader.getSystemResource(name);
        }
    }
}
