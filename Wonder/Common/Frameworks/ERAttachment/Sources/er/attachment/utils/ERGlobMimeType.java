package er.attachment.utils;

import com.webobjects.foundation.NSArray;

public class ERGlobMimeType extends ERMimeType {
  public ERGlobMimeType(String _mimeType) {
    super(_mimeType, _mimeType, null, new NSArray<String>());
  }

  @Override
  public String uti() {
    throw new IllegalStateException("The glob mime type " + mimeType() + " cannot provide a UTI value.");
  }

  @Override
  public NSArray<String> extensions() {
    throw new IllegalStateException("The glob mime type " + mimeType() + " cannot provide an extension list.");
  }
}
