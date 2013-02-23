package com.webobjects._ideservices;

import java.io.File;

import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;

public class _JR_WOLipsProject extends _PBProject {
  private File _bundleFolder;
  private File _contentsFolder;
  private File _versionFile;
  private long _lastModified;

  public _JR_WOLipsProject(File bundleFolder) {
    _bundleFolder = bundleFolder;
    if (bundleFolder.getName().endsWith(".framework")) {
      _contentsFolder = _bundleFolder;
    }
    else {
      _contentsFolder = new File(_bundleFolder, "Contents");
    }
    _versionFile = new File(_bundleFolder.getParentFile(), ".version");
    _dict = new NSMutableDictionary();
    _lastModified = -1;
  }

  public File contentsFolder() {
    return _contentsFolder;
  }
  
  protected void refreshIfNecessary() {
    if (_lastModified == -1 || _versionFile.exists()) {
      long lastModified = _versionFile.lastModified();
      if (lastModified != _lastModified) {
        _lastModified = lastModified;
        
        ((NSMutableDictionary) super.filesTable()).removeAllObjects();
        ((NSMutableDictionary) super.localFiles()).removeAllObjects();
        
        File resourcesFolder = new File(_contentsFolder, "Resources");
        if (resourcesFolder.exists()) {
          addResources(resourcesFolder, resourcesFolder.getAbsolutePath());
        }
        
        File webserverResourcesFolder = new File(_contentsFolder, "WebServerResources");
        if (webserverResourcesFolder.exists()) {
          addWebserverResources(webserverResourcesFolder, webserverResourcesFolder.getAbsolutePath());
        }
      }
    }
  }
  
  protected void addResources(File resourcesFolder, String basePath) {
    boolean addChildren = true;
    String name = resourcesFolder.getName();
    if (name.endsWith(".wo")) {
      _addRelativeFileKey(resourcesFolder.getAbsolutePath(), "WO_COMPONENTS", basePath);
      addChildren = false;
    }
    else if (name.endsWith(".eomodeld")) {
      _addRelativeFileKey(resourcesFolder.getAbsolutePath(), _PBProject.PB_WOAppResourcesKey, basePath);
      addChildren = false;
    }
    else if (name.equals("Java")) {
      // ignore Java folder ...
      addChildren = false;
    }
    
    if (addChildren) {
      File[] files = resourcesFolder.listFiles();
      if (files != null) {
        for (File file : files) {
          if (file.isDirectory()) {
            addResources(file, basePath);
          }
          else {
            String fileName = file.getName();
            if (fileName.endsWith(".api")) {
              // ignore API files ...
            }
            else {
              _addRelativeFileKey(file.getAbsolutePath(), _PBProject.PB_WOAppResourcesKey, basePath);
            }
          }
        }
      }
    }
  }
  
  protected void addWebserverResources(File webresourcesFolder, String basePath) {
    File[] files = webresourcesFolder.listFiles();
    if (files != null) {
      for (File file : files) {
        if (file.isDirectory()) {
          addWebserverResources(file, basePath);
        }
        else {
          _addRelativeFileKey(file.getAbsolutePath(), "WEBSERVER_RESOURCES", basePath);
        }
      }
    }
  }
  
  protected void _addRelativeFileKey(String absolutePath, String key, String basePath) {
    if (absolutePath.startsWith(basePath)) {
      String relativePath = absolutePath.substring(basePath.length() + 1);
      addFileKey(relativePath, key);
    }
    else {
      addFileKey(absolutePath, key);
    }
  }

  @Override
  public NSDictionary localFiles() {
    refreshIfNecessary();
    return super.localFiles();
  }

  @Override
  public NSMutableDictionary filesTable() {
    refreshIfNecessary();
    return super.filesTable();
  }
}
