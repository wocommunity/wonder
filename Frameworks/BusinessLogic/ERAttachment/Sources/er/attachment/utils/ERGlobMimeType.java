package er.attachment.utils;

import com.webobjects.foundation.NSArray;

/**
 * <span class="en">
 * ERGlobMimeType represents an "image/*" style mime type.
 * </span>
 * 
 * <span class="ja">
 * ERGlobMimeType は "image/*" MIME タイプです。
 * </span>
 * 
 * @author mschrag
 */
public class ERGlobMimeType extends ERMimeType {
  
  /**
   * <span class="en">
   * Constructs an ERGlobMimeType.
   * 
   * @param _mimeType the glob mime type (i.e. image/*)
   * </span>
   * 
   * <span class="ja">
   * コンストラクタ
   * 
   * @param _mimeType - glob mime タイプ (例： image/*)
   * </span>
   */
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
