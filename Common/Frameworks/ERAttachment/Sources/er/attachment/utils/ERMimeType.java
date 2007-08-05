package er.attachment.utils;

import com.webobjects.foundation.NSArray;

public class ERMimeType {
  private String _name;
  private String _mimeType;
  private String _uti;
  private NSArray<String> _extensions;

  public ERMimeType(String name, String mimeType, String uti, NSArray<String> extensions) {
    _name = name;
    _mimeType = mimeType;
    _uti = uti;
    _extensions = extensions;
  }
  
  public boolean matchesExactly(ERMimeType mimeType) {
    return _mimeType.equalsIgnoreCase(mimeType._mimeType);
  }

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

  public String name() {
    return _name;
  }

  public String mimeType() {
    return _mimeType;
  }

  public String uti() {
    return _uti;
  }

  public NSArray<String> extensions() {
    return _extensions;
  }

  public boolean isRepresentedByExtension(String _extension) {
    boolean representedByExtension = false;
    for (int i = 0; !representedByExtension && i < _extensions.count(); i++) {
      representedByExtension = _extensions.objectAtIndex(i).equalsIgnoreCase(_extension);
    }
    return representedByExtension;
  }

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

  public String type() {
    return _mimeType.substring(0, _mimeType.indexOf('/'));
  }
  
  public String subtype() {
    return _mimeType.substring(_mimeType.indexOf('/') + 1);
  }
  
  public boolean isImage() {
    return _mimeType.startsWith("image/");
  }

  public boolean isVideo() {
    return _mimeType.startsWith("video/");
  }

  public boolean isAudio() {
    return _mimeType.startsWith("audio/");
  }
}
