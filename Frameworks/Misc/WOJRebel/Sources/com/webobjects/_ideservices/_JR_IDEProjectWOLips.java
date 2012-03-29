/*jadclipse*/package com.webobjects._ideservices;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSBundle;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSPathUtilities;

public class _JR_IDEProjectWOLips implements _IDEProject {
  private static final String LANGUAGE_RESOURCE_SEPARATOR = "_";
  private volatile _JR_WOLipsProject _wolipsProject;

  private String _languageFromKey(String s) {

    String s1 = "";
    String s2 = null;
    if (s.endsWith("WEBSERVER_RESOURCES")) {
      s2 = "WEBSERVER_RESOURCES";
    }
    else if (s.endsWith("WOAPP_RESOURCES")) {
      s2 = "WOAPP_RESOURCES";
    }
    else if (s.endsWith("OTHER_RESOURCES")) {
      s2 = "OTHER_RESOURCES";
    }
    else if (s.endsWith("WO_COMPONENTS")) {
      s2 = "WO_COMPONENTS";
    }
    if (s2 != null) {
      s1 = s.substring(0, s.indexOf(s2));
    }

    int i = s1.indexOf("_");
    if (i > 0) {
      s1 = s1.substring(0, i);
    }
    return s1;
  }

  public static _JR_WOLipsProject wolipsProjectFromEclipseProject(String bundlePath) {
    try {
      _JR_WOLipsProject project = null;

      File bundleFolder = new File(bundlePath);
      File buildFolder = bundleFolder.getParentFile();
      if (buildFolder != null && buildFolder.exists()) {
        File projectFolder = buildFolder.getParentFile();
        if (projectFolder != null && projectFolder.exists()) {
          File eclipseProjectFile = new File(projectFolder, ".project");
          if (eclipseProjectFile.exists()) {
            project = new _JR_WOLipsProject(bundleFolder);

            String bundleName = bundleFolder.getName();
//            project.setAppHelpFileForOSType(bundlePath, i);
//            project.setAppIconFileForOSType(bundlePath, i);
//            project.setApplicationClass(bundlePath);
//            project.setBundleExtension(bundlePath);
//            project.setLanguageName(bundlePath);
//            project.setMainNibFileForOSType(bundlePath, i);
//            project.setProjectDir(bundlePath);
//            project.setProjectName(bundlePath);
//            project.setProjectVersion(bundlePath);
//            project.setRememberFileAttributes(false);
//            project.setShouldGenerateMain(bundlePath);
//            project.setTouched(l);
//            project.setVersionNb(bundlePath);
            if (bundleName.endsWith(".framework")) {
              project.setProjectType("JavaWebObjectsFramework");
            }
            else {
              project.setProjectType("JavaWebObjectsApplication");
            }

            project.setProjectName(bundleName.substring(0, bundleName.lastIndexOf('.')));
            project.setProjectDir(bundleFolder.getAbsolutePath());
            project.setProjectVersion("2.8");
          }
        }
      }

      return project;
    }
    catch (Throwable e) {
      throw new NSForwardException(e);
    }
  }

  public static _JR_IDEProjectWOLips wolipsProjectAtPath(String bundlePath) {
	if (bundlePath == null)
		return null;
	
	boolean isJar = bundlePath.endsWith(".jar");
	if (isJar) {
		// Can we do anything more useful here?
		return null;
	}
	  
    _JR_WOLipsProject project = _JR_IDEProjectWOLips.wolipsProjectFromEclipseProject(bundlePath);

    _JR_IDEProjectWOLips ideProjectWOLips = null;
    if (project != null) {
      ideProjectWOLips = new _JR_IDEProjectWOLips(project);
    }
    
    return ideProjectWOLips;
  }

  public _JR_IDEProjectWOLips(_JR_WOLipsProject wolipsProject) {
    _wolipsProject = wolipsProject;
  }

  public String projectDir() {

    return NSPathUtilities.stringByNormalizingExistingPath(_wolipsProject.projectDir());
  }

  public String projectDirNotNormalized() {

    return _wolipsProject.projectDir();
  }

  public String languageDir() {

    return _wolipsProject.languageDir();
  }

  public String projectName() {

    return _wolipsProject.projectName();
  }

  public String projectTypeName() {

    return _wolipsProject.projectTypeName();
  }

  public String languageName() {

    return _wolipsProject.languageName();
  }

  public NSMutableArray fileListForKey(String s, boolean flag) {

    return _wolipsProject.fileListForKey(s, flag);
  }

  public String pathForFrameworkNamed(String s) {
    return "unimplemented";
  }

  public void setPathForFramework(String s, String s1) {
  }

  public void addComponent(String s, String s1) {

    addFileKey(s1, "CLASSES");
    addFileKey(s, "WO_COMPONENTS");
  }

  public String pathToBucket(String s) {
    if (s.equals("EJB_META_INFO")) {
      return "";
    }
    if (s.equals("EJB_SERVER_CLASSES")) {
      return "EJBServer.subproj";
    }
    if (s.equals("EJB_CLIENT_CLASSES")) {
      return "EJBClient.subproj";
    }
    if (s.equals("EJB_COMMON_CLASSES")) {
      return "";
    }
    return "";
  }

  public void addFileKey(String s, String s1) {

    _JR_WOLipsProject _lpbproject = _wolipsProject;
    String s2 = NSPathUtilities.lastPathComponent(_lpbproject.projectDir());

    String s3 = s1;
    if (s1.equals("WO_COMPONENTS")) {
      s3 = "WEBCOMPONENTS";
    }
    else

    if (s1.equals("EJB_META_INFO")) {
      s3 = "RESOURCES";
    }
    else if (s1.equals("EJB_SERVER_CLASSES")) {
      s2 = s2 + "/EJBServer";
      s3 = "CLASSES";
    }
    else if (s1.equals("EJB_CLIENT_CLASSES")) {
      s2 = s2 + "/EJBClient";
      s3 = "CLASSES";
    }
    else if (s1.equals("EJB_COMMON_CLASSES")) {
      s3 = "CLASSES";
    }

    _JR_WOLipsProject _tmp = _lpbproject;
    _JR_WOLipsProject.addFileToPBBucket(s2, s, s3);
  }

  public void openFile(String s, int i, String s1) {

    _JR_WOLipsProject _tmp = _wolipsProject;
    _JR_WOLipsProject.openFile(s, i, s1);
  }

  public void extractFilesIntoWOProject(_WOProject _pwoproject) {

    extractFilesFromProjectIntoWOProject(_wolipsProject, _pwoproject);
  }

  public void extractFilesFromProjectIntoWOProject(_JR_WOLipsProject _ppbproject, _WOProject _pwoproject) {

    if (_ppbproject == null) {
      return;
    }
    extractFilesForKeyFromProjectIntoWOProject("H_FILES", _ppbproject, _pwoproject);
    extractFilesForKeyFromProjectIntoWOProject("WOAPP_RESOURCES", _ppbproject, _pwoproject);
    extractFilesForKeyFromProjectIntoWOProject("CLASSES", _ppbproject, _pwoproject);
    extractFilesForKeyFromProjectIntoWOProject("OTHER_LINKED", _ppbproject, _pwoproject);

    if (_pwoproject.includeFrameworks()) {
      extractFrameworksFromProjectIntoWOProject(_ppbproject, _pwoproject);
    }

    extractResourcesFromProjectIntoWOProject(_ppbproject, _pwoproject);

    extractEOModelsFromProjectIntoWOProject(_ppbproject, _pwoproject);
    NSArray nsarray;

    if ((nsarray = _ppbproject.parseSubprojects()) != null) {

      int i = 0;
      for (int j = nsarray.count(); i < j; i++) {

        _JR_WOLipsProject _lpbproject = (_JR_WOLipsProject) nsarray.objectAtIndex(i);
        extractFilesFromProjectIntoWOProject(_lpbproject, _pwoproject);
      }
    }
  }

  public void extractFrameworksFromProjectIntoWOProject(_JR_WOLipsProject _ppbproject, _WOProject _pwoproject) {
    NSMutableArray nsmutablearray = _ppbproject.fileListForKey("FRAMEWORKS", false);

    if (nsmutablearray != null) {
      int i = 0;
      for (int j = nsmutablearray.count(); i < j; i++) {
        String s = (String) nsmutablearray.objectAtIndex(i);
        _pwoproject.extractFrameworkNamed(s);
      }
    }
  }

  public void extractEOModelsFromProjectIntoWOProject(_JR_WOLipsProject _ppbproject, _WOProject _pwoproject) {
    String as[] = { "OTHER_RESOURCES", "WOAPP_RESOURCES", null };
    for (int i = 0; as[i] != null; i++) {

      NSMutableArray nsmutablearray = _ppbproject.fileListForKey(as[i], false);
      //System.out.println("_IDEProjectPB.extractEOModelsFromProjectIntoWOProject: " + nsmutablearray + ", " + as[i] + ", " + _ppbproject.projectName());
      //System.out.println("_IDEProjectPB.extractEOModelsFromProjectIntoWOProject:   " + new File(_ppbproject.projectDir()).lastModified());
      if (nsmutablearray == null) {
        continue;
      }
      int j = 0;
      for (int k = nsmutablearray.count(); j < k; j++) {
        String s = (String) nsmutablearray.objectAtIndex(j);
        if (NSPathUtilities.pathIsEqualToString(NSPathUtilities.pathExtension(s), "eomodeld")) {
          String s1 = _ppbproject.contentsFolder().getAbsolutePath() + File.separator + "Resources" + File.separator + s;
          _pwoproject.addModelFilePath(s1);
        }
      }
    }
  }

  public void extractResourcesFromProjectIntoWOProject(_JR_WOLipsProject _ppbproject, _WOProject _pwoproject) {

    //System.out.println("_IDEProjectPB.extractResourcesFromProjectIntoWOProject: " + _ppbproject + ", " + _ppbproject.projectName());
    NSDictionary nsdictionary = _ppbproject.filesTable();
    NSArray nsarray = nsdictionary.allKeys();

    int i = 0;
    for (int j = nsarray.count(); i < j; i++) {

      String s = (String) nsarray.objectAtIndex(i);

      if (s.endsWith("WEBSERVER_RESOURCES") || s.endsWith("WOAPP_RESOURCES") || s.endsWith("OTHER_RESOURCES") || s.endsWith("WO_COMPONENTS")) {

        extractResourcesFromProjectWithKeyIntoWOProject(_ppbproject, s, _pwoproject);
      }
    }
  }

  public void extractResourcesFromProjectWithKeyIntoWOProject(_JR_WOLipsProject _ppbproject, String s, _WOProject _pwoproject) {
    String basePath;
    if ("WEBSERVER_RESOURCES".equals(s)) {
      basePath = _ppbproject.contentsFolder().getAbsolutePath() + File.separator + "WebServerResources";
    }
    else if ("WOAPP_RESOURCES".equals(s)) {
      basePath = _ppbproject.contentsFolder().getAbsolutePath() + File.separator + "Resources";
    }
    else if ("OTHER_RESOURCES".equals(s)) {
      basePath = _ppbproject.contentsFolder().getAbsolutePath() + File.separator + "Resources";
    }
    else if ("WO_COMPONENTS".equals(s)) {
      basePath = _ppbproject.contentsFolder().getAbsolutePath() + File.separator + "Resources";
    }
    else {
      basePath = _ppbproject.contentsFolder().getAbsolutePath();
    }
    
    NSMutableArray nsmutablearray = _ppbproject.fileListForKey(s, false);

    if (nsmutablearray != null) {

      String s1 = _languageFromKey(s);

      int i = 0;
      for (int j = nsmutablearray.count(); i < j; i++) {

        String s2 = (String) nsmutablearray.objectAtIndex(i);
        String s3 = _WOProject.resourcePathByAppendingLanguageFileName(basePath, s1, "");

        String s4 = s3 + File.separator + s2;
        //System.out.println("_IDEProjectPB.extractResourcesFromProjectWithKeyIntoWOProject:   >> " + s4);

        File file = new File(s4);

        if (NSPathUtilities.pathExtension(s2).length() <= 0 && file.exists() && file.isDirectory()) {
          //System.out.println("_IDEProjectPB.extractResourcesFromProjectWithKeyIntoWOProject: a");

          _pwoproject.extractResourcesFromPath(s3, s2);
        }
        else {
          //System.out.println("_IDEProjectPB.extractResourcesFromProjectWithKeyIntoWOProject: b " + s2 + ", " + s4);
          _pwoproject.addResource(s2, s4, s1);
        }
      }
    }
  }

  public void extractFilesForKeyFromProjectIntoWOProject(String s, _JR_WOLipsProject _ppbproject, _WOProject _pwoproject) {

    NSMutableArray nsmutablearray = _ppbproject.fileListForKey(s, false);
    if (nsmutablearray != null) {

      int i = 0;
      for (int j = nsmutablearray.count(); i < j; i++) {
        String s1 = (String) nsmutablearray.objectAtIndex(i);

        if (NSPathUtilities.pathIsEqualToString(NSPathUtilities.pathExtension(s1), "java")) {
          String s2 = _ppbproject.projectDir() + File.separator + s1;
          _pwoproject.addInterfaceFilePath(s2);
        }
      }
    }
  }

  public String ideApplicationName() {

    return "unimplemented";
  }

  public String ideProjectPath() {
    return NSPathUtilities.stringByNormalizingExistingPath(_wolipsProject.projectDir());
  }

  @SuppressWarnings("unchecked")
  public NSArray frameworkBundlePaths() {
    NSMutableArray nsmutablearray = null;
    NSMutableArray nsmutablearray1 = _wolipsProject.fileListForKey("FRAMEWORKS", false);
    int i = nsmutablearray1.count();

    if (i > 0) {
      nsmutablearray = new NSMutableArray(i);

      for (int j = 0; j < i; j++) {
        NSBundle nsbundle = NSBundle.bundleForName((String) nsmutablearray1.objectAtIndex(j));

        if (nsbundle != null) {
          nsmutablearray.addObject(nsbundle.bundlePath());
        }
      }
    }

    if (i == 0 || nsmutablearray.count() == 0) {
      return NSArray.EmptyArray;
    }
    return nsmutablearray;
  }

  public void addFilenameExtensionToListOfKnowns(String s) {
  }

  public void refreshUnderlyingProjectCache() {
    _wolipsProject.refreshIfNecessary();
  }

  public String bundlePath() {

    String s = null;
    String s1 = _wolipsProject.projectTypeName();

    if (s1.equalsIgnoreCase("JavaWebObjectsFramework")) {
      s = NSBundle.bundleForName(_wolipsProject.projectName()).bundlePath();
    }
    else if (s1.equalsIgnoreCase("JavaWebObjectsApplication")) {
      s = NSBundle.mainBundle().bundlePath();
    }

    return NSPathUtilities.stringByNormalizingExistingPath(s);
  }

  public URL bundlePathURL() {
    try {
      return new File(bundlePath()).toURI().toURL();
    }
    catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }

  public NSArray frameworkBundlePathURLs() {
    NSMutableArray nsmutablearray = (NSMutableArray) frameworkBundlePaths();
    NSMutableArray urls = new NSMutableArray();
    Enumeration en = nsmutablearray.objectEnumerator();
    while (en.hasMoreElements()) {
      String path = (String) en.nextElement();
      try {
        urls.add(new File(path).toURI().toURL());
      }
      catch (MalformedURLException e) {
        throw new RuntimeException(e);
      }
    }
    return urls;
  }

  public boolean shouldPreloadResources() {
    return false;
  }
}