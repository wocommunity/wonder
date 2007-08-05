package er.attachment.utils;

import com.webobjects.foundation.NSArray;

/**
 * ERMimeType stores the metadata about a particular mime type.
 * 
 * @author mschrag
 */
public class ERMimeType {
  private String _name;
  private String _mimeType;
  private String _uti;
  private NSArray<String> _extensions;

  /**
   * Constructs an ERMimeType.
   * 
   * @param name the display name of the mime type ("Portable Network Graphics")
   * @param mimeType the mime type string ("image/jpg")
   * @param uti the universal type identifier that corresponds to this mime type ("public.jpeg")
   * @param extensions the array of file extensions for this mime type ("jpg", "jpeg", etc)
   */
  public ERMimeType(String name, String mimeType, String uti, NSArray<String> extensions) {
    _name = name;
    _mimeType = mimeType;
    _uti = uti;
    _extensions = extensions;
  }
  
  /**
   * Returns true if this mime type exactly matches the other, meaning, the 
   * underlying mime type strings are identical.
   * 
   * @param mimeType the other mime type to compare
   * @return true if the mime type strings are identical
   */
  public boolean matchesExactly(ERMimeType mimeType) {
    return _mimeType.equalsIgnoreCase(mimeType._mimeType);
  }
  
  /**
   * Supports glob mime types for comparison, so image/* matches image/jpeg.
   *  
   * @param otherMimeType the other mime type to compare against
   * @return true if the mime types are compatible
   */
  public boolean matches(ERMimeType otherMimeType) {
    boolean matches;
    if ("*".equals(_mimeType) || (otherMimeType != null && "*".equals(otherMimeType._mimeType))) {
      matches = true;
    }
    else if (otherMimeType == null) {
      matches = false;
    }
    else {
      String mimeTypeStr = _mimeType;
      String otherMimeTypeStr = otherMimeType._mimeType;
      int slashIndex = mimeTypeStr.indexOf('/');
      int otherSlashIndex = otherMimeTypeStr.indexOf('/');
      String base = mimeTypeStr.substring(0, slashIndex);
      String otherBase = otherMimeTypeStr.substring(0, slashIndex);
      if (base.equals(otherBase)) {
        String type = mimeTypeStr.substring(slashIndex + 1);
        String otherType = otherMimeTypeStr.substring(slashIndex + 1);
        matches = "*".equals(type) || "*".equals(otherType) || type.equals(otherType);
      }
      else {
        matches = false;
      }
    }
    return matches;
  }

  /**
   * Returns the name of this mime type.
   *  
   * @return the name of this mime type
   */
  public String name() {
    return _name;
  }

  /**
   * Returns the mime type string representation.
   * 
   * @return the mime type string representation
   */
  public String mimeType() {
    return _mimeType;
  }

  /**
   * Returns the universal type identifier.
   * 
   * @return the universal type identifier
   */
  public String uti() {
    return _uti;
  }

  /**
   * Returns the list of extensions that map to this mime type.
   * 
   * @return the list of extensions that map to this mime type
   */
  public NSArray<String> extensions() {
    return _extensions;
  }

  /**
   * Returns true if this mime type represents a file of the given extension.
   * 
   * @param extension the extension to lookup
   * @return true if the extension matches one of the extensions in this mime type
   */
  public boolean isRepresentedByExtension(String extension) {
    boolean representedByExtension = false;
    for (int i = 0; !representedByExtension && i < _extensions.count(); i++) {
      representedByExtension = _extensions.objectAtIndex(i).equalsIgnoreCase(extension);
    }
    return representedByExtension;
  }

  /**
   * Returns the "primary" extension for this mime type.  The primary extension is
   * the first extension in the list, and generally should be considered the most
   * common extension to use for the type.
   * 
   * @return the primary extension (or "" if there are no extensions)
   */
  public String primaryExtension() {
    String extension;
    if (_extensions.count() > 0) {
      extension = _extensions.objectAtIndex(0);
    }
    else {
      extension = "";
    }
    return extension;
  }

  /**
   * Returns the part of the mime type before the "/".
   * 
   * @return the type of the mime type
   */
  public String type() {
    return _mimeType.substring(0, _mimeType.indexOf('/'));
  }
  
  /**
   * Returns the part of the mime type after the "/".
   * 
   * @return the subtype of the mime type
   */
  public String subtype() {
    return _mimeType.substring(_mimeType.indexOf('/') + 1);
  }
  
  /**
   * Returns true for image/<whatever> mime types.
   * 
   * @return true for image/<whatever> mime types
   */
  public boolean isImage() {
    return _mimeType.startsWith("image/");
  }

  /**
   * Returns true for video/<whatever> mime types.
   * 
   * @return true for video/<whatever> mime types
   */
  public boolean isVideo() {
    return _mimeType.startsWith("video/");
  }

  /**
   * Returns true for audio/<whatever> mime types.
   * 
   * @return true for audio/<whatever> mime types
   */
  public boolean isAudio() {
    return _mimeType.startsWith("audio/");
  }
}
