package er.attachment.utils;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import com.webobjects.foundation.NSArray;

import er.extensions.ERXProperties;

/**
 * ERMimeTypeManager provides an interface to looking up mime type metadata.
 * 
 * @author mschrag
 */
public class ERMimeTypeManager {
  private static ERMimeTypeManager INSTANCE;

  /**
   * Returns the singleton mime type manager.  See the top level documentation on information
   * about configuring custom mime types.
   *  
   * @return the singleton mime type manager
   */
  public static synchronized ERMimeTypeManager mimeTypeManager() {
    if (ERMimeTypeManager.INSTANCE == null) {
      ERMimeTypeManager mimeTypeManager = new ERMimeTypeManager();
      
      NSArray<String> mimeTypes = ERXProperties.componentsSeparatedByString("er.attachment.mimeTypes", ",");
      NSArray<String> additionalMimeTypes = ERXProperties.componentsSeparatedByString("er.attachment.additionalMimeTypes", ",");
      if (additionalMimeTypes != null) {
        mimeTypes = mimeTypes.arrayByAddingObjectsFromArray(additionalMimeTypes);
      }
      
      for (String mimeType : mimeTypes) {
        String name = ERXProperties.stringForKeyWithDefault("er.attachment.mimeType." + mimeType + ".name", mimeType);
        String uti = ERXProperties.stringForKeyWithDefault("er.attachment.mimeType." + mimeType + ".uti", null);
        NSArray<String> extensions = ERXProperties.componentsSeparatedByString("er.attachment.mimeType." + mimeType + ".extensions", ",");
        mimeTypeManager.addMimeType(new ERMimeType(name, mimeType, uti, extensions));
      }

      ERMimeTypeManager.INSTANCE = mimeTypeManager;
    }
    return ERMimeTypeManager.INSTANCE;
  }

  /**
   * Returns the primary extension for the given mime type.
   * 
   * @param mimeTypeStr the mime type string to lookup
   * @return the primary extension or null if there is no mime type in the system that matches
   */
  public static String primaryExtension(String mimeTypeStr) {
    ERMimeType mimeType = ERMimeTypeManager.mimeTypeManager().mimeTypeForMimeTypeString(mimeTypeStr, false);
    String extension = null;
    if (mimeType != null) {
      extension = mimeType.primaryExtension();
    }
    return extension;
  }

  private List<ERMimeType> _mimeTypes;

  private ERMimeTypeManager() {
    _mimeTypes = new LinkedList<ERMimeType>();
  }

  /**
   * Removes all the mime types from this manager.
   */
  public void clearMimeTypes() {
    _mimeTypes.clear();
  }
  
  /**
   * Removes the given mime type definition from this manager.
   * 
   * @param mimeType the mime type to remove
   */
  public void removeMimeType(ERMimeType mimeType) {
    _mimeTypes.remove(mimeType);
  }
  
  /**
   * Adds a mime type definition to the manager.
   * 
   * @param mimeType the mime type to add
   */
  public void addMimeType(ERMimeType mimeType) {
    _mimeTypes.add(mimeType);
  }

  /**
   * Returns the ERMimeType for the given mime type string, optionally throwing an exception
   * if the type isn't found.
   * 
   * @param mimeType the mime type string to lookup
   * @param exceptionIfNotFound if true, a NoSuchElementException exception is thrown if the mime type isn't found
   * @return the matching ERMimeType
   */
  public ERMimeType mimeTypeForMimeTypeString(String mimeType, boolean exceptionIfNotFound) {
    ERMimeType matchingMimeType = null;
    if (mimeType != null) {
      Iterator<ERMimeType> mimeTypesIter = _mimeTypes.iterator();
      while (matchingMimeType == null && mimeTypesIter.hasNext()) {
        ERMimeType possibleMatchingMimeType = mimeTypesIter.next();
        if (possibleMatchingMimeType.mimeType().equals(mimeType)) {
          matchingMimeType = possibleMatchingMimeType;
        }
      }
      if (mimeType != null && mimeType.indexOf("*") != -1) {
        matchingMimeType = new ERGlobMimeType(mimeType);
      }
      if (exceptionIfNotFound && matchingMimeType == null) {
        throw new NoSuchElementException("There is no registered mime type for the type " + mimeType + ".");
      }
    }
    return matchingMimeType;
  }

  /**
   * Returns the ERMimeType for the given file extension, optionally throwing an exception
   * if the type isn't found.
   * 
   * @param extension the file extension to lookup
   * @param exceptionIfNotFound if true, a NoSuchElementException exception is thrown if the mime type isn't found
   * @return the matching ERMimeType
   */
  public ERMimeType mimeTypeForExtension(String extension, boolean exceptionIfNotFound) {
    ERMimeType matchingMimeType = null;
    if (extension != null) {
      String lowercaseExtension = extension.toLowerCase();
      Iterator<ERMimeType> mimeTypesIter = _mimeTypes.iterator();
      while (matchingMimeType == null && mimeTypesIter.hasNext()) {
        ERMimeType mimeType = mimeTypesIter.next();
        if (mimeType.isRepresentedByExtension(lowercaseExtension)) {
          matchingMimeType = mimeType;
        }
      }
    }
    if (exceptionIfNotFound && matchingMimeType == null) {
      throw new NoSuchElementException("There is no registered mime type for the extension " + extension + ".");
    }
    return matchingMimeType;
  }
}
