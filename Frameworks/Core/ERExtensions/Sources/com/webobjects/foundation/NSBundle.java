package com.webobjects.foundation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Locale;
import java.util.Properties;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

@SuppressWarnings("unchecked")
public class NSBundle {
  private static class SpecificResourceFilter implements FilenameFilter {
    private String name;

    public boolean accept(File dir, String aName) {

      boolean result = false;

      if (aName != null && aName.startsWith(name)) {

        result = true;
      }

      return result;
    }

    public SpecificResourceFilter(String aName) {
      name = (new StringBuilder()).append(aName).append(".").toString();
    }
  }

  private static class ResourceFilter implements FilenameFilter {
    private String extension;

    public boolean accept(File dir, String aName) {
      boolean result = false;
      if (aName != null) {
        File f = new File(dir, aName);
        if (f.isFile() && aName.endsWith(extension)) {
          result = true;
        }
      }
      return result;
    }

    public ResourceFilter(String anExtension) {
      extension = (new StringBuilder()).append(".").append(anExtension).toString();
    }
  }

  private static class ResourceDirectoryFilter implements FilenameFilter {
    private String extension;

    public boolean accept(File dir, String aName) {
      boolean result = false;
      if (aName != null) {
        File d = new File((new StringBuilder()).append(dir).append(File.separator).append(aName).toString());
        if (d.isDirectory() && aName.endsWith(extension)) {
          result = true;
        }
      }
      return result;
    }

    public ResourceDirectoryFilter(String anExtension) {
      extension = (new StringBuilder()).append(".").append(anExtension).toString();
    }
  }

  private static class OldResourceFilter implements FilenameFilter {
    private String extension;

    public boolean accept(File dir, String aName) {
      boolean result = false;
      if (aName != null && aName.endsWith(extension)) {
        result = true;
      }
      return result;
    }

    public OldResourceFilter(String anExtension) {
      extension = (new StringBuilder()).append(".").append(anExtension).toString();
    }
  }

  static class InfoDictFilter implements FilenameFilter {
    private static final String INFO_DICT_NAME = "Info.plist";

    public boolean accept(File dir, String aName) {
      boolean result = false;
      if (aName != null && aName.equals("Info.plist")) {
        result = true;
      }
      return result;
    }

    InfoDictFilter() {
    }
  }

  static class FilesFilter implements FilenameFilter {
    public boolean accept(File dir, String aName) {
      boolean result = false;
      if (aName != null) {
        File namedFile = new File(dir, aName);
        result = namedFile.isFile();
      }
      return result;
    }

    FilesFilter() {
    }
  }

  static class DirectoryFilter implements FilenameFilter {
    public boolean accept(File dir, String aName) {
      boolean result = false;
      if (aName != null) {
        File namedFile = new File(dir, aName);
        result = namedFile.isDirectory();
      }
      return result;
    }

    DirectoryFilter() {
    }
  }

  public static final String CFBUNDLESHORTVERSIONSTRINGKEY = "CFBundleShortVersionString";
  public static final String MANIFESTIMPLEMENTATIONVERSIONKEY = "Implementation-Version";
  public static final Class _CLASS = _NSUtilities._classWithFullySpecifiedName("com.webobjects.foundation.NSBundle");
  public static final String BundleDidLoadNotification = "NSBundleDidLoadNotification";
  public static final String LoadedClassesNotification = "NSLoadedClassesNotification";
  private static final String userDirPath;
  private static final String JSUFFIX;
  private static final String LPROJSUFFIX = ".lproj";
  private static final String MAIN_BUNDLE_NAME = "MainBundle";
  private static final String NONLOCALIZED_LOCALE = "Nonlocalized.lproj";
  private static final String NONLOCALIZED_LOCALE_PREFIX;
  private static final String RESOURCES = "Resources";
  private static final String RSUFFIX;
  private static final String RJSUFFIX;
  private static final String CONTENTS = "Contents";
  private static final String CSUFFIX;
  private static final String CRSUFFIX;
  private static final int NSBUNDLE = 1;
  private static final int CFBUNDLE = 2;
  private static final NSMutableArray AllBundles = new NSMutableArray(64);
  private static final NSMutableArray AllBundlesReally = new NSMutableArray(64);
  private static final NSMutableArray AllFrameworks = new NSMutableArray(64);
  private static final NSMutableDictionary BundlesClassesTable = new NSMutableDictionary(2048);
  private static NSArray ClassPath;
  private static final NSMutableDictionary BundlesNamesTable = new NSMutableDictionary(16);
  private static final NSMutableDictionary AppBundlesNamesTable = new NSMutableDictionary(1);
  private static NSBundle MainBundle;
  private static final _NSThreadsafeMutableDictionary OldResourceFilters = new _NSThreadsafeMutableDictionary(new NSMutableDictionary());
  private static boolean PrincipalClassLookupAllowed;
  private static final _NSThreadsafeMutableDictionary ResourceDirectoryFilters = new _NSThreadsafeMutableDictionary(new NSMutableDictionary());
  private static final _NSThreadsafeMutableDictionary ResourceFilters = new _NSThreadsafeMutableDictionary(new NSMutableDictionary());
  private static final DirectoryFilter TheDirectoryFilter = new DirectoryFilter();
  private static final FilesFilter TheFilesFilter = new FilesFilter();
  private static final InfoDictFilter TheInfoDictFilter = new InfoDictFilter();
  private static final _NSUtilities.JavaArchiveFilter TheJavaArchiveFilter = new _NSUtilities.JavaArchiveFilter();
  private static final _NSUtilities.JavaClassFilter TheJavaClassFilter = new _NSUtilities.JavaClassFilter();
  private static String ResourcesInfoPlist;
  private static String JarResourcesInfoPlist;
  private static String ResourcesProperties = "Resources/Properties";
  private static NSMutableDictionary TheFileDict = new NSMutableDictionary(1);
  private static boolean safeInvokeDeprecatedJarBundleAPI = false;
  private boolean isJar;
  private JarFile jarFile;
  private NSMutableArray jarFileEntries;
  private NSDictionary jarFileLayout;
  private String _bundleURLPrefix;
  private String bundlePath;
  private int bundleType;
  private boolean classesHaveBeenLoaded;
  private NSArray classNames;
  private NSDictionary infoDictionary;
  private boolean isFramework;
  private Properties properties;
  private String name;
  private NSArray packages;
  private Class principalClass;
  private NSMutableArray resourceBuckets;
  private String resourcePath;
  private String contentsPath;
  private String _resourceLocation;
  private static final String jarEndsWithString;

  private static final String __exctractStringFromURL(URL anURL) {
    String url2Path = null;
    try {
      String urlPath = URLDecoder.decode(anURL.getPath(), "UTF-8");
      if (urlPath.endsWith(NSBundle.jarEndsWithString)) {
        url2Path = urlPath.substring(0, urlPath.length() - NSBundle.JarResourcesInfoPlist.length());
        URL url2 = new URL(url2Path);
        url2Path = url2.getPath();
      }
    }
    catch (Exception urlException) {
    }
    return url2Path;
  }

  private NSBundle() {
  }

  /**
   * @deprecated Method allBundles is deprecated
   */
  @Deprecated
  public static synchronized NSArray allBundles() {
    return NSBundle.AllBundles.immutableClone();
  }

  /**
   * @deprecated Method allFrameworks is deprecated
   */
  @Deprecated
  public static NSArray allFrameworks() {
    return NSBundle.frameworkBundles();
  }

  public static synchronized NSBundle bundleForClass(Class aClass) {
    NSBundle bundle = null;
    if (aClass != null) {
      bundle = (NSBundle) NSBundle.BundlesClassesTable.objectForKey(aClass.getName());
    }
    return bundle;
  }

  /**
   * @deprecated Method bundleWithPath is deprecated
   */
  @Deprecated
  public static NSBundle bundleWithPath(String aPath) {
    return NSBundle._bundleWithPathShouldCreateIsJar(aPath, false, false);
  }

  public static NSBundle _bundleWithPathShouldCreateIsJar(String aPath, boolean shouldCreateBundle, boolean newIsJar) {
    NSBundle bundle = null;
    String normalizedPath = null;
    String cleanedPath = null;
    normalizedPath = NSBundle.NormalizeExistingBundlePath(aPath);
    bundle = NSBundle.LookupBundleWithPath(normalizedPath);
    if (bundle == null) {
      cleanedPath = NSBundle.CleanNormalizedBundlePath(normalizedPath);
      bundle = NSBundle.LookupBundleWithPath(cleanedPath);
    }
    if (bundle == null && shouldCreateBundle) {
      bundle = NSBundle.CreateBundleWithPath(newIsJar ? normalizedPath : cleanedPath, newIsJar);
    }
    return bundle;
  }

  public static synchronized NSBundle bundleForName(String aName) {
    NSBundle bundle = null;
    if (aName != null) {
      String fixedName;
      if (aName.endsWith(".framework")) {
        fixedName = NSPathUtilities.stringByDeletingPathExtension(aName);
      }
      else {
        fixedName = aName;
      }
      bundle = (NSBundle) NSBundle.BundlesNamesTable.objectForKey(fixedName);
    }
    return bundle;
  }

  public static synchronized NSBundle _appBundleForName(String aName) {
    NSBundle bundle = null;
    if (aName != null) {
      String fixedName;
      if (aName.endsWith(".woa")) {
        fixedName = NSPathUtilities.stringByDeletingPathExtension(aName);
      }
      else {
        fixedName = aName;
      }
      bundle = (NSBundle) NSBundle.AppBundlesNamesTable.objectForKey(fixedName);
    }
    return bundle;
  }

  public static synchronized NSArray frameworkBundles() {
    return NSBundle.AllFrameworks.immutableClone();
  }

  public static void _setMainBundle(NSBundle bundle) {
    NSBundle.MainBundle = bundle;
  }

  public static NSBundle mainBundle() {
    return NSBundle.MainBundle;
  }

  private static File getProjectWOFolder(String aPath) {
	  File projectWOFolder = null;
	  
	  File path = new File(aPath);
	  File projectFolder = path.getParentFile();
	  File projectFile = new File(projectFolder, ".project");
	  if (projectFile.exists()) {
		  File buildFolder = new File(projectFolder, "build");
		  if (!buildFolder.exists()) {
			  buildFolder = new File(projectFolder, "dist");
		  }
		  String projectName = projectFolder.getName();
		  File frameworkFolder = new File(buildFolder, projectName + ".framework");
		  if (frameworkFolder.exists()) {
			  projectWOFolder = frameworkFolder;
		  }
		  else {
			  File woaFolder = new File(buildFolder, projectName + ".woa");
			  if (woaFolder.exists()) {
				  projectWOFolder = woaFolder;
			  }
		  }
	  }
	  return projectWOFolder;
  }
  
  private static boolean couldBeABundle(String aPath) {
    boolean isBundle = false;
    NSMutableArray resourcesSubdirs = new NSMutableArray();
    int index = aPath.lastIndexOf(NSBundle.RSUFFIX);
    if (index == -1) {
      resourcesSubdirs.addObject(aPath.concat(NSBundle.RSUFFIX));
      resourcesSubdirs.addObject(aPath.concat(NSBundle.CSUFFIX));
    }
    else {
      resourcesSubdirs.addObject(aPath.substring(0, index + NSBundle.RSUFFIX.length()));
    }
    Iterator iterator = resourcesSubdirs.iterator();
    do {
      if (!iterator.hasNext()) {
        break;
      }
      File file = new File((String) iterator.next());
      if (!file.exists() || !file.isDirectory()) {
        continue;
      }
      String files[] = file.list(NSBundle.TheInfoDictFilter);
      if (files.length <= 0) {
        continue;
      }
      isBundle = true;
      break;
    } while (true);
    return isBundle;
  }

  private static void transferPropertiesFromSourceToDest(Properties sourceProps, Properties destProps) {
    if (sourceProps != null) {
      destProps.putAll(sourceProps);
    }
  }

  private static void LoadUserAndBundleProperties() {
    NSArray allBundles = NSBundle.allFrameworks();
    Enumeration bundleEn = allBundles.objectEnumerator();
    Properties nextProps = null;
    Properties bundleProps = new Properties();
    Properties userProps = null;
    Properties oldSysProps = NSProperties._getProperties();
    //System.out.println("NSBundle.LoadUserAndBundleProperties: " + oldSysProps);
    String userhome = System.getProperty("user.home");
    for (; bundleEn.hasMoreElements(); NSBundle.transferPropertiesFromSourceToDest(nextProps, bundleProps)) {
      NSBundle nextBundle = (NSBundle) bundleEn.nextElement();
      nextProps = nextBundle.properties();
    }
    if (NSBundle.mainBundle() != null) {
      nextProps = NSBundle.mainBundle().properties();
    }
    NSBundle.transferPropertiesFromSourceToDest(nextProps, bundleProps);
    nextProps = bundleProps;
    if (userhome != null && userhome.length() > 0) {
      try {
        File propertiesFile = new File(userhome, "WebObjects.properties");
        InputStream is = propertiesFile.toURL().openStream();
        if (is != null) {
          userProps = NSBundle.initPropertiesFromInputStreamWithParent(is, bundleProps);
        }
      }
      catch (Exception e) {
      }
      if (userProps != null) {
        nextProps = userProps;
      }
    }
    Properties sysProps = new Properties(nextProps);
    NSBundle.transferPropertiesFromSourceToDest(oldSysProps, sysProps);
    NSProperties._setProperties(sysProps);
  }

  private static void InitMainBundle() {
    try {
      String mainBundleName = NSProperties._mainBundleName();
      if (mainBundleName != null) {
        NSBundle.MainBundle = NSBundle.bundleForName(mainBundleName);
      }
    }
    catch (Exception e) {
    }
    if (NSBundle.MainBundle == null && NSBundle.couldBeABundle(NSBundle.userDirPath)) {
      NSBundle.MainBundle = NSBundle._bundleWithPathShouldCreateIsJar(NSBundle.userDirPath, true, false);
    }
    if (NSBundle.MainBundle == null) {
      NSBundle.MainBundle = NSBundle.bundleForName("JavaFoundation");
    }
    if (NSBundle.MainBundle != null && (NSBundle.MainBundle.infoDictionary == null || NSBundle.MainBundle.name != (String) NSBundle.MainBundle.infoDictionary.objectForKey("NSExecutable"))) {
      NSBundle.MainBundle.name = "MainBundle";
    }
  }

  /**
   * @deprecated Method _setPrincipalClassWarningSuppressed is deprecated
   */
  public static void _setPrincipalClassWarningSuppressed(boolean flag1) {
  }

  private static void InitPrincipalClasses() {
    int count = NSBundle.AllBundlesReally.count();
    for (int i = 0; i < count; i++) {
      ((NSBundle) NSBundle.AllBundlesReally.objectAtIndex(i)).initPrincipalClass();
    }
    NSBundle.PrincipalClassLookupAllowed = true;
  }

  private static void LoadBundlesFromJars(NSArray array) {
    Enumeration en = array.objectEnumerator();
    do {
      if (!en.hasMoreElements()) {
        break;
      }
      NSBundle b = NSBundle._bundleWithPathShouldCreateIsJar(en.nextElement().toString(), true, true);
      if (b != null) {
        b.postNotification();
      }
    } while (true);
  }

  private static void LoadBundlesFromClassPath(NSArray array) {
	Enumeration en = array.objectEnumerator();
    do {
      if (!en.hasMoreElements()) {
        break;
      }
      NSBundle b = null;
      String nextPathComponent = (String) en.nextElement();
      File projectWOFolder = getProjectWOFolder(nextPathComponent);
      if (projectWOFolder != null) {
  		File javaFolder = new File(new File(new File(projectWOFolder, "Contents"), "Resources"), "Java");
  		if (!javaFolder.exists()) {
  			javaFolder = new File(new File(projectWOFolder, "Resources"), "Java");
  		}
  		if (javaFolder.exists()) {
  	    	try {
  				String frameworkJavaPathComponent = javaFolder.getCanonicalPath();
  				if (!NSBundle.ClassPath.containsObject(frameworkJavaPathComponent)) {
	  	    		int classPathIndex = NSBundle.ClassPath.indexOfObject(nextPathComponent);
	  	    		if (classPathIndex != -1) {
		  	  			NSMutableArray<String> mutableClassPath = NSBundle.ClassPath.mutableClone();
	  	    			mutableClassPath.replaceObjectAtIndex(frameworkJavaPathComponent, classPathIndex);
		  	  			NSBundle.ClassPath = mutableClassPath.immutableClone();
	  	    		}
	  	  			nextPathComponent = frameworkJavaPathComponent;
  				}
  			}
  			catch (IOException e) {
  				System.out.println("NSBundle.LoadBundlesFromClassPath: skipping " + projectWOFolder);
  			}
  		}
      }
      if (NSBundle.couldBeABundle(nextPathComponent)) {
        b = NSBundle._bundleWithPathShouldCreateIsJar(nextPathComponent, true, false);
      }
      if (b != null) {
        b.postNotification();
      }
    } while (true);
  }

  private static String DefaultLocalePrefix() {
    String defaultLang = "English";
    String defaultLocaleLang = Locale.getDefault().getLanguage();
    if (defaultLocaleLang.equals("de")) {
      defaultLang = "German";
    }
    else if (defaultLocaleLang.equals("es")) {
      defaultLang = "Spanish";
    }
    else if (defaultLocaleLang.equals("fr")) {
      defaultLang = "French";
    }
    else if (defaultLocaleLang.equals("ja")) {
      defaultLang = "Japanese";
    }
    return defaultLang.concat(".lproj");
  }

  private static synchronized NSBundle LookupBundleWithPath(String aPath) {
    NSBundle bundle = null;
    if (aPath == null) {
      return null;
    }
    Enumeration en = NSBundle.AllBundlesReally.objectEnumerator();
    do {
      if (!en.hasMoreElements() || bundle != null) {
        break;
      }
      NSBundle nextBundle = (NSBundle) en.nextElement();
      if (nextBundle.bundlePath().equals(aPath)) {
        bundle = nextBundle;
      }
    } while (true);
    return bundle;
  }

  public static NSArray _allBundlesReally() {
    return NSBundle.AllBundlesReally;
  }

  private static synchronized NSBundle CreateBundleWithPath(String aPath, boolean newIsJar) {
    NSBundle bundle = null;
    if (aPath == null) {
      return null;
    }
    bundle = new NSBundle();
    bundle.initIsJar(newIsJar);
    bundle.initBundlePath(aPath);
    bundle.initBundleURLPrefix();
    bundle.initBundleType();
    bundle.initJarFileLayout();
    bundle.initContentsPath();
    bundle.initResourcePath();
    bundle.initResourceBuckets();
    bundle.initInfoDictionary();
    bundle.initName();
    if (bundle.couldBeAFramework()) {
      NSBundle possibleBundle = NSBundle.bundleForName(bundle.name);
      if (possibleBundle == null) {
        NSBundle.BundlesNamesTable.setObjectForKey(bundle, bundle.name);
      }
      else {
        System.out.println("NSBundle.CreateBundleWithPath: There were multiple declarations of the bundle '" + bundle.name() + "'.  Skipping '" + aPath + "' in favor of '" + possibleBundle.bundlePath() + "'.");
        return null;
        //throw new IllegalStateException((new StringBuilder()).append("<").append(_CLASS.getName()).append("> warning: There is already a unique instance for Bundle named '").append(bundle.name).append("'.  Use NSBundle.bundleForName(").append(bundle.name).append(") to access it: ").append(possibleBundle.toString()).toString());
      }
      bundle.isFramework = true;
      NSBundle.AllFrameworks.addObject(bundle);
    }
    else {
      NSBundle.AppBundlesNamesTable.setObjectForKey(bundle, bundle.name);
      NSBundle.MainBundle = bundle;
      bundle.isFramework = false;
      NSBundle.AllBundles.addObject(bundle);
    }
    NSBundle.AllBundlesReally.addObject(bundle);
    bundle.initProperties();
    bundle.initClassNames();
    bundle.initPackages();
    if (NSBundle.PrincipalClassLookupAllowed) {
      bundle.initPrincipalClass();
    }
    return bundle;
  }

  private static String NormalizeExistingBundlePath(String aPath) {
    String standardizedPath = null;
    if (aPath != null) {
      File fileAtPath = new File(aPath);
      if (fileAtPath.exists()) {
        standardizedPath = NSPathUtilities.stringByNormalizingExistingPath(aPath);
      }
    }
    return standardizedPath;
  }

  private static String CleanNormalizedBundlePath(String standardizedPath) {
    String bundlePath = standardizedPath;
    if (bundlePath != null) {
      String allDirectoriesInPath = null;
      int i = -1;
      File fileAtPath = new File(bundlePath);
      if (!fileAtPath.isDirectory()) {
        allDirectoriesInPath = _NSStringUtilities.stringByDeletingLastComponent(bundlePath, File.separatorChar);
      }
      else {
        allDirectoriesInPath = bundlePath;
      }
      if (allDirectoriesInPath != null) {
        i = allDirectoriesInPath.lastIndexOf(NSBundle.RJSUFFIX);
        if (i == -1 || i == 0) {
          i = allDirectoriesInPath.lastIndexOf(NSBundle.RSUFFIX);
        }
        if (i == -1 || i == 0) {
          bundlePath = allDirectoriesInPath;
        }
        else {
          bundlePath = allDirectoriesInPath.substring(0, i);
          if (NSPathUtilities.lastPathComponent(NSPathUtilities.stringByDeletingLastPathComponent(bundlePath)).equals("Versions")) {
            bundlePath = NSPathUtilities.stringByDeletingLastPathComponent(NSPathUtilities.stringByDeletingLastPathComponent(bundlePath));
          }
        }
      }
    }
    return bundlePath;
  }

  private static OldResourceFilter OldResourceFilterForExtension(String anExtension) {
    OldResourceFilter rf = null;
    if (anExtension == null) {
      throw new IllegalArgumentException("Illegal resource search: cannot search using a null extension");
    }
    String correctedExtension = anExtension.startsWith(".") ? anExtension.substring(1) : anExtension;
    rf = (OldResourceFilter) NSBundle.OldResourceFilters.objectForKey(correctedExtension);
    if (rf == null) {
      rf = new OldResourceFilter(correctedExtension);
      NSBundle.OldResourceFilters.setObjectForKey(rf, correctedExtension);
    }
    return rf;
  }

  private static ResourceDirectoryFilter ResourceDirectoryFilterForExtension(String anExtension) {
    ResourceDirectoryFilter rdf = null;
    if (anExtension == null) {
      throw new IllegalArgumentException("Illegal resource search: cannot search using a null extension");
    }
    String correctedExtension = anExtension.startsWith(".") ? anExtension.substring(1) : anExtension;
    rdf = (ResourceDirectoryFilter) NSBundle.ResourceDirectoryFilters.objectForKey(correctedExtension);
    if (rdf == null) {
      rdf = new ResourceDirectoryFilter(correctedExtension);
      NSBundle.ResourceDirectoryFilters.setObjectForKey(rdf, correctedExtension);
    }
    return rdf;
  }

  private static ResourceFilter ResourceFilterForExtension(String anExtension) {
    ResourceFilter rf = null;
    if (anExtension == null) {
      throw new IllegalArgumentException("Illegal resource search: cannot search using a null extension");
    }
    String correctedExtension = anExtension.startsWith(".") ? anExtension.substring(1) : anExtension;
    rf = (ResourceFilter) NSBundle.ResourceFilters.objectForKey(correctedExtension);
    if (rf == null) {
      rf = new ResourceFilter(correctedExtension);
      NSBundle.ResourceFilters.setObjectForKey(rf, correctedExtension);
    }
    return rf;
  }

  static Class searchAllBundlesForClassWithName(String className) {
    if (NSLog._debugLoggingAllowedForLevelAndGroups(2, 32L)) {
      NSLog.debug.appendln((new StringBuilder()).append("NSBundle.searchAllBundlesForClassWithName(\"").append(className).append("\") was invoked.\n\t**This affects performance very badly.**").toString());
      if (NSLog.debug.allowedDebugLevel() > 2) {
        NSLog.debug.appendln(new RuntimeException("NSBundle.searchAllBundlesForClassWithName was invoked."));
      }
    }
    Class result = null;
    result = NSBundle.searchForClassInBundles(className, NSBundle.allBundles(), true);
    if (result == null) {
      result = NSBundle.searchForClassInBundles(className, NSBundle.allFrameworks(), true);
    }
    return result;
  }

  private static Class searchForClassInBundles(String className, NSArray bundles, boolean registerPackageOnHit) {
    int count = bundles.count();
    for (int i = 0; i < count; i++) {
      NSBundle bundle = (NSBundle) bundles.objectAtIndex(i);
      NSArray<String> packages = bundle.bundleClassPackageNames();
      Class result = _NSUtilities._searchForClassInPackages(className, packages, registerPackageOnHit, false);
      if (result != null) {
        return result;
      }
    }
    return null;
  }

  public NSArray bundleClassPackageNames() {
    return packages;
  }

  /**
   * @deprecated Method bundlePath is deprecated
   */
  public String bundlePath() {
    return bundlePath;
  }

  public URL bundlePathURL() {
    return NSPathUtilities._URLWithPath(bundlePath);
  }

  public String _bundleURLPrefix() {
    return _bundleURLPrefix;
  }

  public byte[] bytesForResourcePath(String aResourcePath) {
    InputStream is = inputStreamForResourcePath(aResourcePath);
    byte b[] = null;
    if (is == null) {
      b = new byte[0];
    }
    else {
      try {
        b = _NSStringUtilities.bytesFromInputStream(is);
      }
      catch (Exception e) {
        throw NSForwardException._runtimeExceptionForThrowable(e);
      }
    }
    return b;
  }

  public NSArray bundleClassNames() {
    return classNames;
  }

  /**
   * @deprecated Method infoDictionary is deprecated
   */
  public NSDictionary infoDictionary() {
    return infoDictionary;
  }

  public NSDictionary _infoDictionary() {
    return infoDictionary;
  }

  public URL pathURLForResourcePath(String aResourcePath) {
    return _pathURLForResourcePath(aResourcePath, true);
  }

  public URL _pathURLForResourcePath(String aResourcePath, boolean returnDirectories) {
    URL url = null;
    if (aResourcePath != null && aResourcePath.length() > 0 && resourceBuckets.indexOfIdenticalObject(_resourceLocation) != -1) {
      boolean isLocalized = true;
      if (aResourcePath.startsWith("Nonlocalized.lproj")) {
        isLocalized = false;
      }
      String realPath;
      if (isLocalized) {
        realPath = aResourcePath;
      }
      else {
        realPath = aResourcePath.substring("Nonlocalized.lproj".length());
      }
      if (isJar) {
        if (!realPath.startsWith("/")) {
          realPath = "/".concat(realPath);
        }
        ZipEntry ze = jarFile.getEntry(_resourceLocation.concat(realPath));
        if (ze != null && (returnDirectories || !ze.isDirectory())) {
          try {
            url = new URL(_bundleURLPrefix.concat(ze.getName()));
          }
          catch (Exception e) {
            throw NSForwardException._runtimeExceptionForThrowable(e);
          }
        }
      }
      else {
        if (!realPath.startsWith(File.separator)) {
          realPath = File.separator.concat(realPath);
        }
        try {
          File f = new File(resourcePath.concat(realPath));
          if (f.exists() && (f.isFile() || returnDirectories)) {
            url = NSPathUtilities._URLWithPath(f.getCanonicalPath());
          }
        }
        catch (Exception exception) {
          throw NSForwardException._runtimeExceptionForThrowable(exception);
        }
      }
    }
    return url;
  }

  public InputStream inputStreamForResourcePath(String aResourcePath) {
    InputStream is = null;
    URL url = _pathURLForResourcePath(aResourcePath, false);
    if (url != null) {
      try {
        is = url.openStream();
      }
      catch (IOException ioe) {
        throw NSForwardException._runtimeExceptionForThrowable(ioe);
      }
    }
    return is;
  }

  public boolean isFramework() {
    return isFramework;
  }

  public boolean _isCFBundle() {
    return bundleType == 2;
  }

  public boolean isJar() {
    return isJar;
  }

  public JarFile _jarFile() {
    return jarFile;
  }

  public NSDictionary _jarFileLayout() {
    return jarFileLayout;
  }

  /**
   * @deprecated Method load is deprecated
   */
  public boolean load() {
    return classesHaveBeenLoaded;
  }

  public String name() {
    return name;
  }

  public boolean _directoryExistsInJar(String path) {
    if (path == null) {
      return false;
    }
    if (path.length() == 0) {
      return true;
    }
    if (isJar) {
      String aPath = path;
      if (!aPath.endsWith("/")) {
        aPath = aPath.concat("/");
      }
      if (File.separatorChar != '/') {
        aPath = aPath.replace(File.separatorChar, '/');
      }
      return jarFile.getEntry(aPath) != null;
    }
    else {
      return false;
    }
  }

  /**
   * @deprecated Method pathForResource is deprecated
   */
  public String pathForResource(String aName, String anExtension) {
    return pathForResource(aName, anExtension, null);
  }

  /**
   * @deprecated Method pathForResource is deprecated
   */
  public String pathForResource(String aName, String anExtension, String aSubDirPath) {
    if (isJar) {
      if (NSBundle.safeInvokeDeprecatedJarBundleAPI) {
        return null;
      }
      else {
        throw new IllegalStateException("pathsForResoures cannot be invoked on a jar-based NSBundle");
      }
    }
    String path = null;
    if (aName != null) {
      Enumeration en = resourceBuckets.objectEnumerator();
      String localePrefix = NSBundle.DefaultLocalePrefix();
      String pathFragments[] = new String[2];
      String pathPrefix = null;
      String fileName;
      if (anExtension == null) {
        fileName = aName;
      }
      else if (anExtension.startsWith(".") || aName.endsWith(".")) {
        fileName = (new StringBuilder()).append(aName).append(anExtension).toString();
      }
      else {
        fileName = (new StringBuilder()).append(aName).append(".").append(anExtension).toString();
      }
      if (aSubDirPath == null) {
        pathFragments[0] = "";
        pathFragments[1] = localePrefix;
      }
      else {
        pathFragments[0] = aSubDirPath;
        pathFragments[1] = (new StringBuilder()).append(aSubDirPath).append(File.separator).append(localePrefix).toString();
      }
      while (en.hasMoreElements() && path == null) {
        String nextDir = (String) en.nextElement();
        if (nextDir.equals("")) {
          pathPrefix = bundlePath;
        }
        else {
          pathPrefix = (new StringBuilder()).append(bundlePath).append(File.separator).append(nextDir).toString();
        }
        int i = 0;
        while (i < pathFragments.length && path == null) {
          String possiblePath;
          if (pathFragments[i].equals("")) {
            possiblePath = (new StringBuilder()).append(pathPrefix).append(File.separator).append(fileName).toString();
          }
          else {
            possiblePath = (new StringBuilder()).append(pathPrefix).append(File.separator).append(pathFragments[i]).append(File.separator).append(fileName).toString();
          }
          try {
            File possibleResource = new File(possiblePath);
            if (possibleResource.exists()) {
              path = possibleResource.getCanonicalPath();
            }
          }
          catch (Exception exception) {
            throw NSForwardException._runtimeExceptionForThrowable(exception);
          }
          i++;
        }
      }
      if (path == null && anExtension == null) {
        SpecificResourceFilter srf = new SpecificResourceFilter(aName);
        for (en = resourceBuckets.objectEnumerator(); en.hasMoreElements() && path == null;) {
          String nextDir = (String) en.nextElement();
          if (nextDir.equals("")) {
            pathPrefix = bundlePath;
          }
          else {
            pathPrefix = (new StringBuilder()).append(bundlePath).append(File.separator).append(nextDir).toString();
          }
          int i = 0;
          while (i < pathFragments.length && path == null) {
            String possiblePath;
            if (pathFragments[i].equals("")) {
              possiblePath = pathPrefix;
            }
            else {
              possiblePath = (new StringBuilder()).append(pathPrefix).append(pathFragments[i]).toString();
            }
            File possibleResourceDir = new File(possiblePath);
            if (possibleResourceDir.exists() && possibleResourceDir.isDirectory()) {
              String fileNames[] = possibleResourceDir.list(srf);
              if (fileNames.length > 0) {
                try {
                  path = (new StringBuilder()).append(possibleResourceDir.getCanonicalPath()).append(File.separator).append(fileNames[0]).toString();
                }
                catch (IOException e) {
                  throw NSForwardException._runtimeExceptionForThrowable(e);
                }
              }
            }
            i++;
          }
        }
      }
    }
    return path;
  }

  /**
   * @deprecated Method pathsForResources is deprecated
   */
  public NSArray pathsForResources(String anExtension, String aSubDirPath) {
    if (isJar) {
      if (NSBundle.safeInvokeDeprecatedJarBundleAPI) {
        return NSArray.EmptyArray;
      }
      else {
        throw new IllegalStateException("pathsForResources cannot be invoked on a jar-based NSBundle");
      }
    }
    Enumeration en = resourceBuckets.objectEnumerator();
    NSMutableArray fileArray = new NSMutableArray();
    String localePrefix = NSBundle.DefaultLocalePrefix();
    String pathFragments[] = new String[2];
    OldResourceFilter rf = null;
    if (anExtension != null && !anExtension.equals("")) {
      rf = NSBundle.OldResourceFilterForExtension(anExtension);
    }
    if (aSubDirPath == null) {
      pathFragments[0] = "";
      pathFragments[1] = localePrefix;
    }
    else {
      pathFragments[0] = aSubDirPath;
      pathFragments[1] = (new StringBuilder()).append(aSubDirPath).append(File.separator).append(localePrefix).toString();
    }
    while (en.hasMoreElements()) {
      String nextDir = (String) en.nextElement();
      String pathPrefix;
      if (nextDir.equals("")) {
        pathPrefix = bundlePath;
      }
      else {
        pathPrefix = (new StringBuilder()).append(bundlePath).append(File.separator).append(nextDir).toString();
      }
      int i = 0;
      while (i < pathFragments.length) {
        String possiblePath;
        if (pathFragments[i].equals("")) {
          possiblePath = pathPrefix;
        }
        else {
          possiblePath = (new StringBuilder()).append(pathPrefix).append(File.separator).append(pathFragments[i]).toString();
        }
        File possibleResourceDir = new File(possiblePath);
        if (possibleResourceDir.exists() && possibleResourceDir.isDirectory()) {
          String resourceNames[];
          if (rf == null) {
            resourceNames = possibleResourceDir.list();
          }
          else {
            resourceNames = possibleResourceDir.list(rf);
          }
          if (resourceNames.length > 0) {
            String basePath;
            try {
              basePath = possibleResourceDir.getCanonicalPath();
            }
            catch (IOException e) {
              throw NSForwardException._runtimeExceptionForThrowable(e);
            }
            for (int j = 0; j < resourceNames.length; j++) {
              fileArray.addObject((new StringBuilder()).append(basePath).append(File.separator).append(resourceNames[j]).toString());
            }
          }
        }
        i++;
      }
    }
    return fileArray;
  }

  public Class principalClass() {
    return principalClass;
  }

  public Properties properties() {
    return properties;
  }

  /**
   * @deprecated Method resourcePath is deprecated
   */
  public String resourcePath() {
    return resourcePath;
  }

  public URL _urlForRelativePath(String path) {
    URL retVal = null;
    if (path != null && path.length() > 0) {
      if (isJar) {
        String aPath = path;
        if (aPath.startsWith("/")) {
          aPath = aPath.substring(1, aPath.length());
        }
        ZipEntry ze = jarFile.getEntry(aPath);
        if (ze == null && !aPath.endsWith("/")) {
          aPath = aPath.concat("/");
          ze = jarFile.getEntry(aPath);
        }
        if (ze != null) {
          try {
            retVal = new URL(_bundleURLPrefix.concat(aPath));
          }
          catch (MalformedURLException mue) {
          }
        }
      }
      else {
        File f = new File(_NSStringUtilities.concat(bundlePath(), File.separator, path));
        if (f.exists()) {
          try {
            retVal = f.toURL();
          }
          catch (MalformedURLException mue) {
          }
        }
      }
    }
    return retVal;
  }

  public String resourcePathForLocalizedResourceNamed(String aName, String aSubDirPath) {
    String path = null;
    if (aName != null) {
      String FileSeparator = isJar ? "/" : File.separator;
      Enumeration en = resourceBuckets.objectEnumerator();
      String localePrefix = NSBundle.DefaultLocalePrefix().concat(FileSeparator);
      String pathFragments[] = new String[2];
      if (aSubDirPath == null || aSubDirPath.length() == 0) {
        pathFragments[0] = localePrefix;
        pathFragments[1] = "";
      }
      else {
        pathFragments[0] = _NSStringUtilities.concat(localePrefix, aSubDirPath, FileSeparator);
        pathFragments[1] = aSubDirPath.concat(FileSeparator);
      }
      while (en.hasMoreElements() && path == null) {
        String nextDir = (String) en.nextElement();
        String pathPrefix;
        if (nextDir.equals("")) {
          pathPrefix = bundlePath.concat(FileSeparator);
        }
        else {
          pathPrefix = _NSStringUtilities.concat(bundlePath, FileSeparator, nextDir, FileSeparator);
        }
        int i = 0;
        while (i < pathFragments.length && path == null) {
          if (isJar) {
            String possiblePath = _NSStringUtilities.concat(FileSeparator, pathFragments[i], aName);
            String comparisonPath = nextDir.concat(possiblePath);
            ZipEntry ze = jarFile.getEntry(comparisonPath);
            if (ze != null) {
              path = pathFragments[i].concat(aName);
              if (!pathFragments[i].startsWith(NSBundle.DefaultLocalePrefix())) {
                path = "Nonlocalized.lproj".concat(possiblePath);
              }
            }
          }
          else {
            String possiblePath;
            if (pathFragments[i].equals("")) {
              possiblePath = pathPrefix.concat(aName);
            }
            else {
              possiblePath = _NSStringUtilities.concat(pathPrefix, pathFragments[i], aName);
            }
            File possibleResource = new File(possiblePath);
            File possibleResourcePrefix = new File(pathPrefix);
            if (possibleResource.exists() && possibleResourcePrefix.exists() && possibleResourcePrefix.isDirectory()) {
              try {
                path = possibleResource.getCanonicalPath();
                String absolutePathPrefix = possibleResourcePrefix.getCanonicalPath();
                if (!absolutePathPrefix.endsWith(File.separator)) {
                  absolutePathPrefix = (new StringBuilder()).append(absolutePathPrefix).append(File.separator).toString();
                }
                if (path.startsWith(absolutePathPrefix)) {
                  path = path.substring(absolutePathPrefix.length());
                  if (!pathFragments[i].startsWith(NSBundle.DefaultLocalePrefix())) {
                    path = NSBundle.NONLOCALIZED_LOCALE_PREFIX.concat(path);
                  }
                }
                else {
                  throw new IllegalArgumentException((new StringBuilder()).append("<").append(com.webobjects.foundation.NSBundle.class.getName()).append("> May not pass relative paths that reference resources outside of the bundle! (").append(aName).append(",").append(aSubDirPath).append(")").toString());
                }
              }
              catch (IOException e) {
                throw NSForwardException._runtimeExceptionForThrowable(e);
              }
            }
          }
          i++;
        }
      }
    }
    return path;
  }

  public NSArray resourcePathsForDirectories(String extension, String aSubDirPath) {
    NSArray list = null;
    if (resourceBuckets.indexOfIdenticalObject(_resourceLocation) != -1) {
      if (isJar) {
        String anExtension = fixExtension(extension);
        if (aSubDirPath == null || aSubDirPath.length() == 0) {
          NSMutableArray allPaths = new NSMutableArray(resourcePathsForDirectoriesInDirectoryInJar(_resourceLocation, anExtension, false));
          NSArray lProjDirs = resourcePathsForDirectoriesInDirectoryInJar(_resourceLocation, ".lproj", false);
          int count = lProjDirs.count();
          for (int i = 0; i < count; i++) {
            String lProjDir = (String) lProjDirs.objectAtIndex(i);
            allPaths.addObjectsFromArray(resourcePathsForDirectoriesInDirectoryInJar(_NSStringUtilities.concat(_resourceLocation, "/", lProjDir), anExtension, false));
          }
          list = allPaths;
        }
        else {
          String startPath = _NSStringUtilities.concat(_resourceLocation, "/", aSubDirPath);
          list = resourcePathsForDirectoriesInDirectoryInJar(startPath, anExtension, false);
        }
      }
      else {
        FilenameFilter rdf;
        if (extension == null) {
          rdf = NSBundle.TheDirectoryFilter;
        }
        else {
          rdf = NSBundle.ResourceDirectoryFilterForExtension(extension);
        }
        if (aSubDirPath == null) {
          NSMutableArray allPaths = new NSMutableArray(resourcePathsForDirectoriesInDirectory(resourcePath, rdf, false));
          NSArray lProjDirs = resourcePathsForDirectoriesInDirectory(resourcePath, NSBundle.ResourceDirectoryFilterForExtension(".lproj"), false);
          int count = lProjDirs.count();
          for (int i = 0; i < count; i++) {
            String lProjDir = (String) lProjDirs.objectAtIndex(i);
            allPaths.addObjectsFromArray(resourcePathsForDirectoriesInDirectory(_NSStringUtilities.concat(resourcePath, File.separator, lProjDir), rdf, false));
          }
          list = allPaths;
        }
        else {
          String absolutePath = NSPathUtilities.stringByNormalizingExistingPath(_NSStringUtilities.concat(resourcePath, File.separator, aSubDirPath));
          if (absolutePath.startsWith(resourcePath.concat(File.separator))) {
            list = resourcePathsForDirectoriesInDirectory(absolutePath, rdf, false);
          }
        }
      }
    }
    if (list == null || list.count() == 0) {
      return NSArray.emptyArray();
    }
    else {
      return list;
    }
  }

  public NSArray resourcePathsForLocalizedResources(String extension, String aSubDirPath) {
    NSMutableArray localizedPaths = null;
    NSArray returnPaths = null;
    String anExtension = extension;
    if (resourceBuckets.indexOfIdenticalObject(_resourceLocation) != -1) {
      if (isJar) {
        String localePrefix = NSBundle.DefaultLocalePrefix();
        String lpSuffix = "/".concat(localePrefix);
        anExtension = fixExtension(anExtension);
        if (aSubDirPath == null || aSubDirPath.length() == 0) {
          localizedPaths = new NSMutableArray(resourcePathsForResourcesInDirectoryInJar(_NSStringUtilities.concat(_resourceLocation, lpSuffix), anExtension, false));
        }
        else {
          String startPath = _NSStringUtilities.concat(_resourceLocation, lpSuffix, "/", aSubDirPath);
          localizedPaths = new NSMutableArray(resourcePathsForResourcesInDirectoryInJar(startPath, anExtension, false));
        }
        if (aSubDirPath == null || aSubDirPath.length() == 0) {
          NSMutableArray dirNames = new NSMutableArray();
          NSMutableArray fileNames = new NSMutableArray();
          _simplePathsInDirectoryInJar(_resourceLocation, "", dirNames, anExtension, fileNames);
          int dirNamesCount = dirNames.count();
          int fileNamesCount = fileNames.count();
          NSMutableArray nlPaths = new NSMutableArray();
          for (int i = 0; i < fileNamesCount; i++) {
            boolean identicalPath = false;
            String nextName = _NSStringUtilities.concat(localePrefix, "/", (String) fileNames.objectAtIndex(i));
            if (localizedPaths.indexOfObject(nextName) != -1) {
              identicalPath = true;
            }
            if (!identicalPath) {
              localizedPaths.addObject(_NSStringUtilities.concat("Nonlocalized.lproj", "/", (String) fileNames.objectAtIndex(i)));
            }
          }
          for (int i = 0; i < dirNamesCount; i++) {
            boolean useThisDir = !((String) dirNames.objectAtIndex(i)).toString().endsWith(".lproj");
            if (useThisDir) {
              nlPaths.addObjectsFromArray(resourcePathsForResourcesInDirectoryInJar(_NSStringUtilities.concat(_resourceLocation, "/", (String) dirNames.objectAtIndex(i)), anExtension, true));
            }
          }
          int nlPathsCount = nlPaths.count();
          for (int i = 0; i < nlPathsCount; i++) {
            boolean identicalPath = false;
            String nextName = localePrefix.concat(((String) nlPaths.objectAtIndex(i)).substring("Nonlocalized.lproj".length()));
            if (localizedPaths.indexOfObject(nextName) != -1) {
              identicalPath = true;
            }
            if (!identicalPath) {
              localizedPaths.addObject(nlPaths.objectAtIndex(i));
            }
          }
        }
      }
      else {
        String localePrefix = NSBundle.DefaultLocalePrefix();
        String lpSuffix = File.separator.concat(localePrefix);
        FilenameFilter rf;
        if (anExtension == null) {
          rf = NSBundle.TheFilesFilter;
        }
        else {
          rf = NSBundle.ResourceFilterForExtension(anExtension);
        }
        if (aSubDirPath == null) {
          localizedPaths = new NSMutableArray(resourcePathsForResourcesInDirectory(resourcePath.concat(lpSuffix), rf, false));
        }
        else {
          String absolutePath = NSPathUtilities.stringByNormalizingExistingPath(_NSStringUtilities.concat(resourcePath, lpSuffix, File.separator, aSubDirPath));
          if (absolutePath.startsWith(resourcePath.concat(File.separator))) {
            localizedPaths = new NSMutableArray(resourcePathsForDirectoriesInDirectory(absolutePath, rf, false));
          }
        }
        File nlSubdir = new File(resourcePath);
        if (localizedPaths != null && aSubDirPath == null && nlSubdir.exists() && nlSubdir.isDirectory()) {
          String dirNames[] = nlSubdir.list(NSBundle.TheDirectoryFilter);
          int dirNamesCount = dirNames.length;
          String fileNames[] = nlSubdir.list(rf);
          int fileNamesCount = fileNames.length;
          NSMutableArray nlPaths = new NSMutableArray();
          for (int i = 0; i < fileNamesCount; i++) {
            boolean identicalPath = false;
            String nextName = _NSStringUtilities.concat(localePrefix, File.separator, fileNames[i]);
            if (localizedPaths.indexOfObject(nextName) != -1) {
              identicalPath = true;
            }
            if (!identicalPath) {
              localizedPaths.addObject(NSBundle.NONLOCALIZED_LOCALE_PREFIX.concat(fileNames[i]));
            }
          }
          for (int i = 0; i < dirNamesCount; i++) {
            boolean useThisDir = !dirNames[i].endsWith(".lproj");
            if (useThisDir) {
              nlPaths.addObjectsFromArray(resourcePathsForResourcesInDirectory(_NSStringUtilities.concat(resourcePath, File.separator, dirNames[i]), rf, true));
            }
          }
          int nlPathsCount = nlPaths.count();
          for (int i = 0; i < nlPathsCount; i++) {
            boolean identicalPath = false;
            String nextName = localePrefix.concat(((String) nlPaths.objectAtIndex(i)).substring("Nonlocalized.lproj".length()));
            if (localizedPaths.indexOfObject(nextName) != -1) {
              identicalPath = true;
            }
            if (!identicalPath) {
              localizedPaths.addObject(nlPaths.objectAtIndex(i));
            }
          }
        }
      }
      if (localizedPaths == null || localizedPaths.count() == 0) {
        returnPaths = NSArray.emptyArray();
      }
      else {
        returnPaths = localizedPaths;
      }
    }
    return returnPaths;
  }

  public NSArray resourcePathsForResources(String extension, String aSubDirPath) {
    NSArray list = null;
    String anExtension = extension;
    if (resourceBuckets.indexOfIdenticalObject(_resourceLocation) != -1) {
      if (isJar) {
        anExtension = fixExtension(anExtension);
        if (aSubDirPath == null || aSubDirPath.length() == 0) {
          list = resourcePathsForResourcesInDirectoryInJar(_resourceLocation, anExtension, false);
        }
        else {
          String startPath = _NSStringUtilities.concat(_resourceLocation, "/", aSubDirPath);
          boolean prependNonLocalizedLproj = aSubDirPath.indexOf(".lproj") == -1;
          list = resourcePathsForResourcesInDirectoryInJar(startPath, anExtension, prependNonLocalizedLproj);
        }
      }
      else {
        FilenameFilter rf;
        if (anExtension == null) {
          rf = NSBundle.TheFilesFilter;
        }
        else {
          rf = NSBundle.ResourceFilterForExtension(anExtension);
        }
        if (aSubDirPath == null) {
          list = resourcePathsForResourcesInDirectory(resourcePath, rf, false);
        }
        else {
          String absolutePath = NSPathUtilities.stringByNormalizingExistingPath(_NSStringUtilities.concat(resourcePath, File.separator, aSubDirPath));
          if (absolutePath.startsWith(resourcePath.concat(File.separator))) {
            boolean prependNonLocalizedLproj = aSubDirPath.indexOf(".lproj") == -1;
            list = resourcePathsForResourcesInDirectory(absolutePath, rf, prependNonLocalizedLproj);
          }
        }
      }
    }
    if (list == null || list.count() == 0) {
      return NSArray.emptyArray();
    }
    else {
      return list;
    }
  }

  public String toString() {
    int count = 0;
    if (classNames != null) {
      count = classNames.count();
    }
    return (new StringBuilder()).append("<").append(getClass().getName()).append(" name:'").append(name).append("' bundlePath:'").append(bundlePath).append("' packages:'").append(packages).append("' ").append(count).append(" classes >").toString();
  }

  private void addResourceBucket(String aBundleSubDirPath) {
    if (aBundleSubDirPath != null && resourceBuckets.indexOfObject(aBundleSubDirPath) == -1) {
      if (isJar) {
        ZipEntry ze = jarFile.getEntry(aBundleSubDirPath.concat("/"));
        if (ze != null && ze.isDirectory()) {
          resourceBuckets.addObject(aBundleSubDirPath);
        }
        if (aBundleSubDirPath.length() == 0) {
          resourceBuckets.addObject(aBundleSubDirPath);
        }
      }
      else {
        String resourceDirPath = _NSStringUtilities.concat(bundlePath, File.separator, aBundleSubDirPath);
        File resourceDir = new File(resourceDirPath);
        if (resourceDir.exists() && resourceDir.isDirectory()) {
          resourceBuckets.addObject(aBundleSubDirPath);
        }
      }
    }
  }

  public Class _classWithName(String className) {
    Class objectClass = null;
    if (className == null) {
      throw new IllegalArgumentException("Class name cannot be null.");
    }
    objectClass = _NSUtilities._classWithPartialName(className, false);
    if (objectClass != null) {
      return objectClass;
    }
    else {
      NSArray thePackages = bundleClassPackageNames();
      objectClass = _NSUtilities._searchForClassInPackages(className, thePackages, true, false);
      return objectClass == null ? _NSUtilities._classWithPartialName(className, true) : objectClass;
    }
  }

  private NSArray classNamesFromDirectory(File aDirectory) {
    String classes[] = aDirectory.list(NSBundle.TheJavaClassFilter);
    NSMutableArray theClassNames = new NSMutableArray();
    String directories[] = aDirectory.list(NSBundle.TheDirectoryFilter);
    if (classes != null) {
      int l = classes.length;
      for (int i = 0; i < l; i++) {
        String className;
        try {
          className = _NSStringUtilities.concat(aDirectory.getCanonicalPath(), File.separator, classes[i]);
        }
        catch (IOException e) {
          throw NSForwardException._runtimeExceptionForThrowable(e);
        }
        if (resourcePath == bundlePath) {
          className = className.substring(resourcePath.length() + 1, className.lastIndexOf('.'));
        }
        else {
          className = className.substring(resourcePath.length() + NSBundle.JSUFFIX.length() + 1, className.lastIndexOf('.'));
        }
        theClassNames.addObject(className.replace(File.separatorChar, '.'));
      }
    }
    if (directories != null) {
      int l = directories.length;
      for (int i = 0; i < l; i++) {
        File f;
        try {
          f = new File(_NSStringUtilities.concat(aDirectory.getCanonicalPath(), File.separator, directories[i]));
        }
        catch (IOException e) {
          throw NSForwardException._runtimeExceptionForThrowable(e);
        }
        theClassNames.addObjectsFromArray(classNamesFromDirectory(f));
      }
    }
    if (theClassNames.count() == 0) {
      return NSArray.emptyArray();
    }
    else {
      return theClassNames;
    }
  }

  private boolean couldBeAFramework() {
    if (infoDictionary != null) {
      if (infoDictionary.objectForKey("Has_WOComponents") != null) {
        isFramework = true;
      }
      else {
        String value = (String) infoDictionary.objectForKey("CFBundlePackageType");
        if (value != null && value.equalsIgnoreCase("FMWK")) {
          isFramework = true;
        }
      }
    }
    return isFramework;
  }

  private void initIsJar(boolean newIsJar) {
    isJar = newIsJar;
  }

  private void initBundlePath(String newBundlePath) {
    bundlePath = newBundlePath;
  }

  private void initBundleURLPrefix() {
    if (isJar) {
      _bundleURLPrefix = _NSStringUtilities.concat("jar:", NSPathUtilities._fileURLPrefix, bundlePath, "!/");
    }
    else {
      _bundleURLPrefix = _NSStringUtilities.concat(NSPathUtilities._fileURLPrefix, bundlePath, "/");
    }
  }

  private void initJarFileLayout() {
    if (!isJar) {
      return;
    }
    NSMutableDictionary root = new NSMutableDictionary();
    for (Enumeration e = jarFile.entries(); e.hasMoreElements();) {
      ZipEntry ze = (ZipEntry) e.nextElement();
      String zePath = ze.getName();
      NSArray zeArray = NSArray.componentsSeparatedByString(zePath, "/");
      NSMutableDictionary currentDict = root;
      Enumeration e2 = zeArray.objectEnumerator();
      while (e2.hasMoreElements()) {
        String element = (String) e2.nextElement();
        if (element.length() > 0) {
          NSMutableDictionary aDict = (NSMutableDictionary) currentDict.objectForKey(element);
          if (aDict == null) {
            if (e2.hasMoreElements()) {
              NSMutableDictionary newDict = new NSMutableDictionary();
              currentDict.setObjectForKey(newDict, element);
              currentDict = newDict;
            }
            else {
              currentDict.setObjectForKey(NSBundle.TheFileDict, element);
            }
          }
          else if (aDict != NSBundle.TheFileDict) {
            currentDict = aDict;
          }
        }
      }
    }
    jarFileLayout = root;
  }

  private void initBundleType() {
    if (isJar) {
      bundleType = 1;
      try {
        URL url = new URL(_bundleURLPrefix.concat(NSBundle.ResourcesInfoPlist));
        JarURLConnection jarConnection = (JarURLConnection) url.openConnection();
        jarFile = jarConnection.getJarFile();
        jarFileEntries = new NSMutableArray();
        for (Enumeration e = jarFile.entries(); e.hasMoreElements(); jarFileEntries.addObject(e.nextElement())) {
        }
      }
      catch (Exception e) {
        throw NSForwardException._runtimeExceptionForThrowable(e);
      }
    }
    else {
      File contentsDir = new File(bundlePath.concat(NSBundle.CSUFFIX));
      if (contentsDir.exists()) {
        bundleType = 2;
      }
      else {
        bundleType = 1;
      }
      jarFile = null;
    }
  }

  private void initClassNames() {
    NSMutableSet classes = new NSMutableSet();
    classNames = new NSMutableArray();
    if (isJar) {
      if (jarFile != null) {
        Enumeration e = jarFileEntries.objectEnumerator();
        do {
          if (!e.hasMoreElements()) {
            break;
          }
          ZipEntry entry = (ZipEntry) e.nextElement();
          String path = entry.getName();
          if (path.endsWith(".class") && !path.startsWith("WebServerResources") && !path.startsWith("Resources")) {
            String nextClassName = path.substring(0, path.lastIndexOf('.'));
            nextClassName = nextClassName.replace('/', '.');
            nextClassName = nextClassName.intern();
            classes.addObject(nextClassName);
          }
        } while (true);
      }
    }
    else {
      Enumeration en = NSBundle.ClassPath.objectEnumerator();
      do {
        if (!en.hasMoreElements()) {
          break;
        }
        String nextPath = (String) en.nextElement();
        if (nextPath.startsWith(resourcePath)) {
          File f = new File(nextPath);
          if (f.isDirectory()) {
            try {
              if (bundlePath.equals(NSBundle.userDirPath) || f.getCanonicalPath().endsWith(NSBundle.RJSUFFIX)) {
                classes.addObjectsFromArray(classNamesFromDirectory(f));
              }
            }
            catch (IOException e) {
            }
          }
          else if (NSBundle.TheJavaArchiveFilter.accept(null, nextPath)) {
            classes.addObjectsFromArray(_NSUtilities.classNamesFromArchive(f));
          }
        }
      } while (true);
    }
    if (classes.count() == 0) {
      classesHaveBeenLoaded = false;
      classNames = NSArray.emptyArray();
    }
    else {
      classesHaveBeenLoaded = true;
      setClassNames(classes.allObjects());
    }
  }

  private void initInfoDictionary() {
    infoDictionary = NSDictionary.EmptyDictionary;
    String infoPlistPath = _bundleURLPrefix.concat(bundleType != 2 ? NSBundle.ResourcesInfoPlist : "Contents/Info.plist");
    try {
      URL infoDictURL = new URL(infoPlistPath);
      infoDictionary = (NSDictionary) NSPropertyListSerialization.propertyListWithPathURL(infoDictURL);
    }
    catch (Exception e) {
      NSLog.err.appendln((new StringBuilder()).append("Failed to load ").append(infoPlistPath).append(". Treating as empty. ").append(e).toString());
    }
  }

  private void initName() {
    String newName = null;
    if (infoDictionary != null) {
      newName = (String) infoDictionary.objectForKey("NSExecutable");
    }
    if (newName == null) {
      newName = NSPathUtilities.lastPathComponent(bundlePath);
      if (newName.length() > 3) {
        newName = NSPathUtilities.stringByDeletingPathExtension(newName);
      }
    }
    name = newName;
  }

  private void initPackages() {
    NSMutableArray thePackages = new NSMutableArray(packages);
    packages = new NSMutableArray();
    Enumeration en = classNames.objectEnumerator();
    do {
      if (!en.hasMoreElements()) {
        break;
      }
      String nextClass = (String) en.nextElement();
      String newPackage = _NSStringUtilities.stringByDeletingLastComponent(nextClass, '.');
      if (!thePackages.containsObject(newPackage)) {
        thePackages.addObject(newPackage);
      }
    } while (true);
    packages = thePackages.immutableClone();
  }

  private void initPrincipalClass() {
    String principalClassName = null;
    principalClass = null;
    if (infoDictionary != null) {
      principalClassName = (String) infoDictionary.objectForKey("NSPrincipalClass");
      if (principalClassName != null) {
        principalClass = _NSUtilities.classWithName(principalClassName);
        if (principalClass == null && _NSUtilities._principalClassLoadingWarningsNeeded) {
          NSLog.err.appendln((new StringBuilder()).append("Principal class '").append(principalClassName).append("' not found in bundle ").append(name).toString());
          if (NSLog.debugLoggingAllowedForLevelAndGroups(1, 32L)) {
            NSLog.debug.appendln(new ClassNotFoundException(principalClassName));
          }
        }
      }
    }
  }

  private static Properties initPropertiesFromInputStreamWithParent(InputStream is, Properties parent) throws IOException {
    Properties _prop = new Properties(parent);
    _prop.load(is);
    is.close();
    return _prop;
  }

  private void initProperties() {
    String propertiesPath = _bundleURLPrefix.concat(bundleType != 2 ? NSBundle.ResourcesProperties : (new StringBuilder()).append("Contents/").append(NSBundle.ResourcesProperties).toString());
    try {
      URL propertiesURL = new URL(propertiesPath);
      properties = NSBundle.initPropertiesFromInputStreamWithParent(propertiesURL.openStream(), null);
    }
    catch (FileNotFoundException fnfe) {
    }
    catch (Exception e) {
      NSLog.err.appendln((new StringBuilder()).append("Error reading properties file ").append(propertiesPath).append(". Exception was ").append(e).toString());
      NSLog.err.appendln("Ignoring this file.");
      properties = null;
    }
  }

  private void initResourceBuckets() {
    resourceBuckets = new NSMutableArray();
    if (isJar) {
      _resourceLocation = "Resources";
      if (jarFile.getEntry(_resourceLocation) != null) {
        addResourceBucket(_resourceLocation);
      }
    }
    else {
      String resourceLocation;
      switch (bundleType) {
      case 1:

        resourceLocation = "Resources";
        break;
      case 2:

        resourceLocation = _NSStringUtilities.concat("Contents", File.separator, "Resources");
        break;
      default:

        throw new IllegalStateException("Inconsistent Bundle type");
      }
      File rlFile = new File(_NSStringUtilities.concat(bundlePath, File.separator, resourceLocation));
      if (rlFile.exists()) {
        _resourceLocation = NSPathUtilities.stringByNormalizingExistingPath(_NSStringUtilities.concat(bundlePath, File.separator, resourceLocation)).substring(bundlePath.length() + 1);
        addResourceBucket(_resourceLocation);
      }
      else if (bundleType == 2) {
        throw new IllegalStateException((new StringBuilder()).append("Bundle at path \"").append(bundlePath).append("\" is a CFBundle, but is missing its \"Contents").append(File.separator).append("Resources\" subdirectory.").toString());
      }
    }
    addResourceBucket("");
  }

  private void initContentsPath() {
    if (isJar) {
      contentsPath = _bundleURLPrefix;
    }
    else {
      switch (bundleType) {
      case 1:

        contentsPath = bundlePath;
        break;
      case 2:

        File contentsDir = new File(bundlePath.concat(NSBundle.CSUFFIX));

        if (contentsDir.exists()) {
          try {
            contentsPath = contentsDir.getCanonicalPath();
          }
          catch (IOException e) {
            throw NSForwardException._runtimeExceptionForThrowable(e);
          }
        }
        else {
          contentsPath = bundlePath;
        }
        break;
      default:

        throw new IllegalStateException("Inconsistent Bundle type");
      }
    }
  }

  private void initResourcePath() {
    if (isJar) {
      resourcePath = contentsPath.concat("Resources");
    }
    else {
      switch (bundleType) {
      case 1: {
        File resourceDir = new File(bundlePath.concat(NSBundle.RSUFFIX));
        if (resourceDir.exists()) {
          try {
            resourcePath = resourceDir.getCanonicalPath();
          }
          catch (IOException e) {
            throw NSForwardException._runtimeExceptionForThrowable(e);
          }
        }
        else {
          resourcePath = bundlePath;
        }
        break;
      }
      case 2: {
        File resourceDir = new File(bundlePath.concat(NSBundle.CRSUFFIX));
        if (resourceDir.exists()) {
          try {
            resourcePath = resourceDir.getCanonicalPath();
          }
          catch (IOException e) {
            throw NSForwardException._runtimeExceptionForThrowable(e);
          }
        }
        else {
          resourcePath = bundlePath;
        }
        break;
      }
      default: {
        throw new IllegalStateException("Inconsistent Bundle type");
      }
      }
    }
  }

  boolean posted = false;
  private void postNotification() {
	  if(!posted) {
		  posted = true;
		  NSNotificationCenter.defaultCenter().postNotification("NSBundleDidLoadNotification", this, new NSDictionary(classNames, "NSLoadedClassesNotification"));
	  }
  }

  private boolean _prefixPathWithNonLocalizedPrefix(String aPath) {
    return aPath.equals(resourcePath);
  }

  private boolean _prefixPathWithNonLocalizedPrefixJar(String aPath) {
    return aPath.equals(_resourceLocation);
  }

  private String fixExtension(String anExtension) {
    return anExtension != null ? anExtension.startsWith(".") ? anExtension.substring(1) : anExtension : "";
  }

  public void _simplePathsInDirectoryInJar(String startPath, String dirExtension, NSMutableArray dirs, String fileExtension, NSMutableArray files) {
    if (startPath.length() == 0 || startPath.equals(".") || startPath.equals("/")) {
      Enumeration e = jarFileLayout.keyEnumerator();
      do {
        if (!e.hasMoreElements()) {
          break;
        }
        String key = (String) e.nextElement();
        NSDictionary value = (NSDictionary) jarFileLayout.objectForKey(key);
        if (value == NSBundle.TheFileDict) {
          if (key.endsWith(fileExtension) && files != null) {
            files.addObject(key);
          }
        }
        else if (key.endsWith(dirExtension) && dirs != null) {
          dirs.addObject(key);
        }
      } while (true);
    }
    else {
      String aPath = startPath.endsWith("/") ? startPath : startPath.concat("/");
      if (jarFile.getEntry(aPath) != null) {
        NSArray keyArray = NSArray.componentsSeparatedByString(aPath.substring(0, aPath.length() - 1), "/");
        NSDictionary filesDict = (NSDictionary) _NSUtilities.valueForKeyArray(jarFileLayout, keyArray);
        if (filesDict != null) {
          Enumeration e = filesDict.keyEnumerator();
          do {
            if (!e.hasMoreElements()) {
              break;
            }
            String key = (String) e.nextElement();
            NSDictionary value = (NSDictionary) filesDict.objectForKey(key);
            if (value == NSBundle.TheFileDict) {
              if (key.endsWith(fileExtension) && files != null) {
                files.addObject(key);
              }
            }
            else if (key.endsWith(dirExtension) && dirs != null) {
              dirs.addObject(key);
            }
          } while (true);
        }
      }
    }
  }

  private NSArray resourcePathsForDirectoriesInDirectoryInJar(String startPath, String anExtension, boolean prependNonlocalizedLProj) {
    NSMutableArray returnList = new NSMutableArray();
    NSMutableArray dirNames = new NSMutableArray();
    _simplePathsInDirectoryInJar(startPath, anExtension, dirNames, "", null);
    String prefix = _prefixPathWithNonLocalizedPrefixJar(startPath) ? "" : startPath.substring(_resourceLocation.concat("/").length());
    for (int i = 0; i < dirNames.count(); i++) {
      String dirName = (String) dirNames.objectAtIndex(i);
      if (prefix.length() == 0) {
        if (dirName.endsWith(".lproj")) {
          returnList.addObject(dirName);
        }
        else {
          returnList.addObject(_NSStringUtilities.concat("Nonlocalized.lproj", "/", dirName));
        }
        continue;
      }
      if (prependNonlocalizedLProj) {
        returnList.addObject(_NSStringUtilities.concat("Nonlocalized.lproj", "/", prefix, "/", dirName));
      }
      else {
        returnList.addObject(_NSStringUtilities.concat(prefix, "/", dirName));
      }
    }
    for (int i = 0; i < dirNames.count(); i++) {
      String dirName = (String) dirNames.objectAtIndex(i);
      if (prefix.length() == 0) {
        boolean endWithLPROJ = dirName.endsWith(".lproj");
        returnList.addObjectsFromArray(resourcePathsForDirectoriesInDirectoryInJar(_NSStringUtilities.concat(startPath, "/", dirName), anExtension, !endWithLPROJ));
      }
      else {
        returnList.addObjectsFromArray(resourcePathsForDirectoriesInDirectoryInJar(_NSStringUtilities.concat(startPath, "/", dirName), anExtension, prependNonlocalizedLProj));
      }
    }
    if (returnList.count() == 0) {
      return NSArray.emptyArray();
    }
    else {
      return returnList;
    }
  }

  private NSArray resourcePathsForDirectoriesInDirectory(String aPath, FilenameFilter aFilter, boolean prependNonlocalizedLProj) {
    String dirNames[] = (new File(aPath)).list(aFilter);
    if (dirNames == null) {
      return NSArray.emptyArray();
    }
    NSMutableArray list = new NSMutableArray();
    String prefix = _prefixPathWithNonLocalizedPrefix(aPath) ? "" : aPath.substring(resourcePath.concat(File.separator).length());
    for (int i = 0; i < dirNames.length; i++) {
      if (prefix.length() == 0) {
        if (dirNames[i].endsWith(".lproj")) {
          list.addObject(dirNames[i]);
        }
        else {
          list.addObject(NSBundle.NONLOCALIZED_LOCALE_PREFIX.concat(dirNames[i]));
        }
        continue;
      }
      if (prependNonlocalizedLProj) {
        list.addObject(_NSStringUtilities.concat(NSBundle.NONLOCALIZED_LOCALE_PREFIX, prefix, File.separator, dirNames[i]));
      }
      else {
        list.addObject(_NSStringUtilities.concat(prefix, File.separator, dirNames[i]));
      }
    }
    for (int i = 0; i < dirNames.length; i++) {
      if (prefix.length() == 0) {
        boolean endWithLPROJ = dirNames[i].endsWith(".lproj");
        list.addObjectsFromArray(resourcePathsForDirectoriesInDirectory(_NSStringUtilities.concat(aPath, File.separator, dirNames[i]), aFilter, !endWithLPROJ));
      }
      else {
        list.addObjectsFromArray(resourcePathsForDirectoriesInDirectory(_NSStringUtilities.concat(aPath, File.separator, dirNames[i]), aFilter, prependNonlocalizedLProj));
      }
    }
    if (list.count() == 0) {
      return NSArray.emptyArray();
    }
    else {
      return list;
    }
  }

  private NSArray resourcePathsForResourcesInDirectoryInJar(String aPath, String anExtension, boolean prependNonlocalizedLProj) {
    NSMutableArray returnList = new NSMutableArray();
    NSMutableArray fileNames = new NSMutableArray();
    NSMutableArray dirNames = new NSMutableArray();
    _simplePathsInDirectoryInJar(aPath, "", dirNames, anExtension, fileNames);
    String prefix = _prefixPathWithNonLocalizedPrefixJar(aPath) ? "" : aPath.substring(_resourceLocation.concat("/").length());
    for (int i = 0; i < fileNames.count(); i++) {
      String fileName = (String) fileNames.objectAtIndex(i);
      if (prefix.length() == 0) {
        returnList.addObject(_NSStringUtilities.concat("Nonlocalized.lproj", "/", fileName));
        continue;
      }
      if (prependNonlocalizedLProj) {
        returnList.addObject(_NSStringUtilities.concat("Nonlocalized.lproj", "/", prefix, "/", fileName));
      }
      else {
        returnList.addObject(_NSStringUtilities.concat(prefix, "/", fileName));
      }
    }
    for (int i = 0; i < dirNames.count(); i++) {
      String dirName = (String) dirNames.objectAtIndex(i);
      boolean prepend = prefix.length() != 0 ? prependNonlocalizedLProj : !dirName.endsWith(".lproj");
      returnList.addObjectsFromArray(resourcePathsForResourcesInDirectoryInJar(_NSStringUtilities.concat(aPath, "/", dirName), anExtension, prepend));
    }
    if (returnList.count() == 0) {
      return NSArray.emptyArray();
    }
    else {
      return returnList;
    }
  }

  private NSArray resourcePathsForResourcesInDirectory(String aPath, FilenameFilter aFilter, boolean prependNonlocalizedLProj) {
    File subdir = new File(aPath);
    String fileNames[] = subdir.list(aFilter);
    if (fileNames == null) {
      return NSArray.emptyArray();
    }
    String prefix = _prefixPathWithNonLocalizedPrefix(aPath) ? "" : aPath.substring(resourcePath.concat(File.separator).length());
    NSMutableArray list = new NSMutableArray();
    for (int i = 0; i < fileNames.length; i++) {
      if (prefix.length() == 0) {
        list.addObject(NSBundle.NONLOCALIZED_LOCALE_PREFIX.concat(fileNames[i]));
        continue;
      }
      if (prependNonlocalizedLProj) {
        list.addObject(_NSStringUtilities.concat(NSBundle.NONLOCALIZED_LOCALE_PREFIX, prefix, File.separator, fileNames[i]));
      }
      else {
        list.addObject(_NSStringUtilities.concat(prefix, File.separator, fileNames[i]));
      }
    }
    String dirNames[] = subdir.list(NSBundle.TheDirectoryFilter);
    for (int i = 0; i < dirNames.length; i++) {
      boolean prepend = prefix.length() != 0 ? prependNonlocalizedLProj : !dirNames[i].endsWith(".lproj");
      list.addObjectsFromArray(resourcePathsForResourcesInDirectory(_NSStringUtilities.concat(aPath, File.separator, dirNames[i]), aFilter, prepend));
    }
    if (list.count() == 0) {
      return NSArray.emptyArray();
    }
    else {
      return list;
    }
  }

  private void setClassNames(NSArray classes) {
    if (classes != null) {
      NSMutableArray theClasses = new NSMutableArray(classNames);
      Enumeration en = classes.objectEnumerator();
      do {
        if (!en.hasMoreElements()) {
          break;
        }
        String nextClassName = (String) en.nextElement();
        if (!theClasses.containsObject(nextClassName)) {
          theClasses.addObject(nextClassName);
          NSBundle bundle = (NSBundle) NSBundle.BundlesClassesTable.objectForKey(nextClassName);
          if (bundle == null) {
            NSBundle.BundlesClassesTable.setObjectForKey(this, nextClassName);
          }
        }
      } while (true);
      classNames = theClasses.immutableClone();
    }
  }

  static {
    JSUFFIX = (new StringBuilder()).append(File.separator).append("Java").toString();
    NONLOCALIZED_LOCALE_PREFIX = (new StringBuilder()).append("Nonlocalized.lproj").append(File.separator).toString();
    RSUFFIX = (new StringBuilder()).append(File.separator).append("Resources").toString();
    RJSUFFIX = (new StringBuilder()).append(NSBundle.RSUFFIX).append(File.separator).append("Java").toString();
    CSUFFIX = (new StringBuilder()).append(File.separator).append("Contents").toString();
    CRSUFFIX = (new StringBuilder()).append(NSBundle.CSUFFIX).append(NSBundle.RSUFFIX).toString();
    NSBundle.ResourcesInfoPlist = "Resources/Info.plist";
    NSBundle.JarResourcesInfoPlist = (new StringBuilder()).append("!/").append(NSBundle.ResourcesInfoPlist).toString();
    jarEndsWithString = ".jar".concat(NSBundle.JarResourcesInfoPlist);
    try {
      NSBundle.safeInvokeDeprecatedJarBundleAPI = NSPropertyListSerialization.booleanForString(NSProperties.getProperty("com.webobjects.safeInvokeDeprecatedJarBundleAPI"));
      String woUserDir = NSProperties.getProperty("webobjects.user.dir");
      if (woUserDir == null) {
        woUserDir = System.getProperty("user.dir");
      }
      userDirPath = (new File(woUserDir)).getCanonicalPath();
      NSMutableArray urlArray = new NSMutableArray();
      Enumeration e = com.webobjects.foundation.NSBundle.class.getClassLoader().getResources(NSBundle.ResourcesInfoPlist);
      do {
        if (!e.hasMoreElements()) {
          break;
        }
        String urlPath = NSBundle.__exctractStringFromURL((URL) e.nextElement());
        if (urlPath != null) {
          urlArray.addObject(urlPath);
        }
      } while (true);
      if (urlArray.count() == 0) {
        e = ClassLoader.getSystemResources(NSBundle.ResourcesInfoPlist);
        do {
          if (!e.hasMoreElements()) {
            break;
          }
          String urlPath = NSBundle.__exctractStringFromURL((URL) e.nextElement());
          if (urlPath != null) {
            urlArray.addObject(urlPath);
          }
        } while (true);
      }
      NSBundle.LoadBundlesFromJars(urlArray);
      NSArray classpath = NSArray._mutableComponentsSeparatedByString((new StringBuilder()).append(System.getProperty("java.class.path")).append(File.pathSeparator).append(NSProperties.getProperty("com.webobjects.classpath")).toString(), File.pathSeparator);
      NSMutableArray cleanedUpClassPath = new NSMutableArray();
      Iterator iterator = classpath.iterator();
      do {
        if (!iterator.hasNext()) {
          break;
        }
        String fixedComponent = NSPathUtilities.stringByNormalizingExistingPath((String) iterator.next());
        if (fixedComponent != null && fixedComponent.length() > 0) {
          cleanedUpClassPath.addObject(fixedComponent);
        }
      } while (true);
      for (int i = cleanedUpClassPath.count() - 1; i >= 0; i--) {
        String component = (String) cleanedUpClassPath.objectAtIndex(i);
        if (cleanedUpClassPath.indexOfObject(component) != i) {
          cleanedUpClassPath.removeObjectAtIndex(i);
        }
      }
      ClassPath = cleanedUpClassPath;
      NSBundle.LoadBundlesFromClassPath(NSBundle.ClassPath);
      NSBundle.InitMainBundle();
      NSBundle.LoadUserAndBundleProperties();
      NSBundle.InitPrincipalClasses();
      _NSUtilities._setResourceSearcher(new _NSUtilities._ResourceSearcher() {
        public Class _searchForClassWithName(String className) {
          return NSBundle.searchAllBundlesForClassWithName(className);
        }

        public URL _searchPathURLForResourceWithName(Class resourceClass, String resourceName, String extension) {
          URL url = null;
          NSBundle bundle = NSBundle.bundleForClass(resourceClass);
          if (bundle != null && resourceName != null) {
            String fileName = null;
            if (extension == null || extension.length() == 0) {
              fileName = resourceName;
            }
            else if (extension.startsWith(".") || resourceName.endsWith(".")) {
              fileName = resourceName.concat(extension);
            }
            else {
              fileName = _NSStringUtilities.concat(resourceName, ".", extension);
            }
            bundle.pathURLForResourcePath(bundle.resourcePathForLocalizedResourceNamed(fileName, ""));
          }
          return url;
        }
      });
    }
    catch (IOException e) {
      throw NSForwardException._runtimeExceptionForThrowable(e);
    }
      }
}