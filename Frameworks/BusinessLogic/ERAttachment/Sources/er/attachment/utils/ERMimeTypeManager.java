package er.attachment.utils;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import com.webobjects.foundation.NSArray;

import er.extensions.foundation.ERXProperties;

/**
 * <span class="en">
 * ERMimeTypeManager provides an interface to looking up mime type metadata.
 * </span>
 * 
 * <span class="ja">
 * ERMimeTypeManager は MIME タイプのメタデータをルックアップするインタフェースを提供します。
 * </span>
 * 
 * @property er.attachment.mimeTypes
 * @property er.attachment.additionalMimeTypes
 * @property er.attachment.mimeType.[mimeType].name
 * @property er.attachment.mimeType.[mimeType].uti
 * @property er.attachment.mimeType.[mimeType].extensions
 *
 * @author mschrag
 */
public class ERMimeTypeManager {
  private static ERMimeTypeManager INSTANCE;

  /**
   * <span class="en">
   * Returns the singleton mime type manager.  See the top level documentation on information
   * about configuring custom mime types.
   *  
   * @return the singleton mime type manager
   * </span>
   * 
   * <span class="ja">
   * MIME タイプ・マネージャのシングルトンを戻します。
   *  
   * @return MIME タイプ・マネージャのシングルトン
   * </span>
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
   * <span class="en">
   * Returns the primary extension for the given mime type.
   * 
   * @param mimeTypeStr the mime type string to lookup
   * 
   * @return the primary extension or null if there is no mime type in the system that matches
   * </span>
   * 
   * <span class="ja">
   * 指定 MIME タイプの優先される拡張子を戻します。
   * 
   * @param mimeTypeStr - ルックアップする MIME タイプ
   * 
   * @return 優先する拡張子、まだはマッチしなければ null
   * </span>
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
   * <span class="en">
   * Removes all the mime types from this manager.
   * </span>
   * 
   * <span class="ja">
   * マネージャよりすべての MIME タイプを削除します。
   * </span>
   */
  public void clearMimeTypes() {
    _mimeTypes.clear();
  }
  
  /**
   * <span class="en">
   * Removes the given mime type definition from this manager.
   * 
   * @param mimeType the mime type to remove
   * </span>
   * 
   * <span class="ja">
   * 指定の MIME タイプをマネージャより削除します。
   * 
   * @param mimeType - 削除する MIME タイプ
   * </span>
  */
  public void removeMimeType(ERMimeType mimeType) {
    _mimeTypes.remove(mimeType);
  }
  
  /**
   * <span class="en">
   * Adds a mime type definition to the manager.
   * 
   * @param mimeType the mime type to add
   * </span>
   * 
   * <span class="ja">
   * 指定の MIME タイプをマネージャに追加します。
   * 
   * @param mimeType - 追加する MIME タイプ
   * </span>
   */
  public void addMimeType(ERMimeType mimeType) {
    _mimeTypes.add(mimeType);
  }

  /**
   * <span class="en">
   * Returns the extension for the given filename.
   * 
   * @param fileName the filename
   * 
   * @return the extension of the filename (or null)
   * </span>
   * 
   * <span class="ja">
   * 指定ファイル名の拡張子を戻します。
   * 
   * @param fileName - ファイル名
   * 
   * @return ファイルの拡張子、又は null
   * </span>
   */
  public String extensionForFileName(String fileName) {
    String extension = null;
    if (fileName != null) {
      int dotIndex = fileName.lastIndexOf('.');
      if (dotIndex != -1) {
        extension = fileName.substring(dotIndex + 1);
      }
    }
    return extension;
  }
  
  /**
   * <span class="en">
   * Returns the ERMimeType for the given file name, optionally throwing an exception
   * if the type isn't found.
   * 
   * @param fileName the file name to lookup
   * @param exceptionIfNotFound if true, a NoSuchElementException exception is thrown if the mime type isn't found
   * 
   * @return the matching ERMimeType
   * </span>
   * 
   * <span class="ja">
   * 指定ファイル名の ERMimeType を戻します。
   * オプション指定により、見つからない場合にはエラーを発生させます。
   * 
   * @param fileName - ルックアップするファイル名
   * @param exceptionIfNotFound - true の場合には見つからない時に NoSuchElementException を発生させます。
   * 
   * @return マッチされた ERMimeType
   * </span>
   */
  public ERMimeType mimeTypeForFileName(String fileName, boolean exceptionIfNotFound) {
    return mimeTypeForExtension(extensionForFileName(fileName), exceptionIfNotFound);
  }
  
  /**
   * <span class="en">
   * Returns the ERMimeType for the given file, optionally throwing an exception
   * if the type isn't found.
   * 
   * @param file the file to lookup
   * @param exceptionIfNotFound if true, a NoSuchElementException exception is thrown if the mime type isn't found
   * 
   * @return the matching ERMimeType
   * </span>
   * 
   * <span class="ja">
   * 指定ファイルの ERMimeType を戻します。
   * オプション指定により、見つからない場合にはエラーを発生させます。
   * 
   * @param file - ルックアップするファイル
   * @param exceptionIfNotFound - true の場合には見つからない時に NoSuchElementException を発生させます。
   * 
   * @return マッチされた ERMimeType
   * </span>
   */
  public ERMimeType mimeTypeForFile(File file, boolean exceptionIfNotFound) {
    String extension = (file == null ? null : extensionForFileName(file.getName()));
    return mimeTypeForExtension(extension, exceptionIfNotFound);
  }
  
  /**
   * <span class="en">
   * Returns the ERMimeType for the given mime type string, optionally throwing an exception
   * if the type isn't found.
   * 
   * @param mimeType the mime type string to lookup
   * @param exceptionIfNotFound if true, a NoSuchElementException exception is thrown if the mime type isn't found
   * 
   * @return the matching ERMimeType
   * </span>
   * 
   * <span class="ja">
   * 指定 MIME タイプの ERMimeType を戻します。
   * オプション指定により、見つからない場合にはエラーを発生させます。
   * 
   * @param mimeType - ルックアップする MIME タイプ
   * @param exceptionIfNotFound - true の場合には見つからない時に NoSuchElementException を発生させます。
   * 
   * @return マッチされた ERMimeType
   * </span>
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
   * <span class="en">
   * Returns the ERMimeType for the given file extension, optionally throwing an exception
   * if the type isn't found.
   * 
   * @param extension the file extension to lookup
   * @param exceptionIfNotFound if true, a NoSuchElementException exception is thrown if the mime type isn't found
   * 
   * @return the matching ERMimeType
   * </span>
   * 
   * <span class="ja">
   * 指定拡張子の ERMimeType を戻します。
   * オプション指定により、見つからない場合にはエラーを発生させます。
   * 
   * @param extension - ルックアップする拡張子
   * @param exceptionIfNotFound - true の場合には見つからない時に NoSuchElementException を発生させます。
   * 
   * @return マッチされた ERMimeType
   * </span>
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

  /**
   * <span class="en">
   * Returns the ERMimeType for the given UTI, optionally throwing an exception
   * if the type isn't found.
   * 
   * @param uti the UTI to lookup
   * @param exceptionIfNotFound if true, a NoSuchElementException exception is thrown if the mime type isn't found
   * 
   * @return the matching ERMimeType
   * </span>
   * 
   * <span class="ja">
   * 指定 UTI の ERMimeType を戻します。
   * オプション指定により、見つからない場合にはエラーを発生させます。
   * 
   * @param uti - ルックアップする UTI
   * @param exceptionIfNotFound - true の場合には見つからない時に NoSuchElementException を発生させます。
   * 
   * @return マッチされた ERMimeType
   * </span>
   */
  public ERMimeType mimeTypeForUTI(String uti, boolean exceptionIfNotFound) {
    ERMimeType matchingMimeType = null;
    if (uti != null) {
      Iterator<ERMimeType> mimeTypesIter = _mimeTypes.iterator();
      while (matchingMimeType == null && mimeTypesIter.hasNext()) {
        ERMimeType mimeType = mimeTypesIter.next();
        if (uti.equals(mimeType.uti())) {
          matchingMimeType = mimeType;
        }
      }
    }
    if (exceptionIfNotFound && matchingMimeType == null) {
      throw new NoSuchElementException("There is no registered mime type for the uti '" + uti + "'.");
    }
    return matchingMimeType;
  }
}
