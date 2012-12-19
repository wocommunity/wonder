package er.attachment.utils;

import com.webobjects.foundation.NSArray;

/**
 * <span class="en">
 * ERMimeType stores the metadata about a particular mime type.
 * </span>
 * 
 * <span class="ja">
 * ERMimeType はある Mime タイプのメタデータを保持します。
 * </span>
 * 
 * @author mschrag
 */
public class ERMimeType {
  private String _name;
  private String _mimeType;
  private String _uti;
  private NSArray<String> _extensions;

  /**
   * <span class="en">
   * Constructs an ERMimeType.
   * 
   * @param name the display name of the mime type ("Portable Network Graphics")
   * @param mimeType the mime type string ("image/jpg")
   * @param uti the universal type identifier that corresponds to this mime type ("public.jpeg")
   * @param extensions the array of file extensions for this mime type ("jpg", "jpeg", etc)
   * </span>
   * 
   * <span class="ja">
   * コンストラクタ
   * 
   * @param name - Mime タイプの表示名称 ("Portable Network Graphics")
   * @param mimeType - Mime タイプ文字列表記 ("image/jpg")
   * @param uti - Mime タイプへの対応されている universal type identifier ("public.jpeg")
   * @param extensions - Mime タイプの拡張子配列 ("jpg", "jpeg", etc)
   * </span>
   */
  public ERMimeType(String name, String mimeType, String uti, NSArray<String> extensions) {
    _name = name;
    _mimeType = mimeType;
    _uti = uti;
    _extensions = extensions;
  }

  /**
   * <span class="en">
   * Returns the glob type of this mime type (image/pdf=>image/*).
   * 
   * @return the glob type of this mime type
   * </span>
   * 
   * <span class="ja">
   * Mime タイプの glob タイプを戻します。 (image/pdf=>image/*)
   * 
   * @return Mime タイプの glob タイプ
   * </span>
   */
  public ERGlobMimeType globMimeType() {
    return new ERGlobMimeType(type() + "/*");
  }

  /**
   * <span class="en">
   * Returns true if this mime type exactly matches the other, meaning, the 
   * underlying mime type strings are identical.
   * 
   * @param mimeType the other mime type to compare
   * 
   * @return true if the mime type strings are identical
   * </span>
   * 
   * <span class="ja">
   * Mime タイプが指定されている Mime タイプと完全一する場合 true を戻します。
   * Mime タイプの文字列表記が全く同様です。
   * 
   * @param mimeType - 比較のために指定する Mime タイプ
   * 
   * @return 全く同様であれば、 true が戻ります。
   * </span>
   */
  public boolean matchesExactly(ERMimeType mimeType) {
    return _mimeType.equalsIgnoreCase(mimeType._mimeType);
  }

  /**
   * <span class="en">
   * Supports glob mime types for comparison, so image/* matches image/jpeg.
   *  
   * @param otherMimeType the other mime type to compare against
   * 
   * @return true if the mime types are compatible
   * </span>
   * 
   * <span class="ja">
   * 比較の為の glob Mime タイプのサポートします。
   * image/* は image/jpeg をマッチします。
   *  
   * @param otherMimeType - 比較する為の Mime タイプ
   * 
   * @return Mime タイプの互換あれば、 true が戻ります。
   * </span>
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
      String otherBase = otherMimeTypeStr.substring(0, otherSlashIndex);
      if (base.equals(otherBase)) {
        String type = mimeTypeStr.substring(slashIndex + 1);
        String otherType = otherMimeTypeStr.substring(otherSlashIndex + 1);
        matches = "*".equals(type) || "*".equals(otherType) || type.equals(otherType);
      }
      else {
        matches = false;
      }
    }
    return matches;
  }

  /**
   * <span class="en">
   * Returns the name of this mime type.
   *  
   * @return the name of this mime type
   * </span>
   * 
   * <span class="ja">
   * Mime タイプの表示名称を戻します。
   *  
   * @return Mime タイプの表示名称
   * </span>
   */
  public String name() {
    return _name;
  }

  /**
   * <span class="en">
   * Returns the mime type string representation.
   * 
   * @return the mime type string representation
   * </span>
   * 
   * <span class="ja">
   * Mime タイプ文字列表記を戻します。
   * 
   * @return Mime タイプ文字列表記
   * </span>
   */
  public String mimeType() {
    return _mimeType;
  }

  /**
   * <span class="en">
   * Returns the universal type identifier.
   * 
   * @return the universal type identifier
   * </span>
   * 
   * <span class="ja">
   * Mime タイプへの対応されている universal type identifier を戻します。
   * 
   * @return Mime タイプへの対応されている universal type identifier
   * </span>
   */
  public String uti() {
    return _uti;
  }

  /**
   * <span class="en">
   * Returns the list of extensions that map to this mime type.
   * 
   * @return the list of extensions that map to this mime type
   * </span>
   * 
   * <span class="ja">
   * Mime タイプの拡張子配列を戻します。
   * 
   * @return Mime タイプの拡張子配列
   * </span>
   */
  public NSArray<String> extensions() {
    return _extensions;
  }

  /**
   * <span class="en">
   * Returns true if this mime type represents a file of the given extension.
   * 
   * @param extension the extension to lookup
   * 
   * @return true if the extension matches one of the extensions in this mime type
   * </span>
   * 
   * <span class="ja">
   * 指定されている拡張子は Mime タイプ文字列表記の一部である時に true を戻します。
   * 
   * @param extension - ルックアップされる拡張子
   * 
   * @return Mime タイプの拡張子の一つがマッチする時には true が戻ります。
   * </span>
   */
  public boolean isRepresentedByExtension(String extension) {
    boolean representedByExtension = false;
    for (int i = 0; !representedByExtension && i < _extensions.count(); i++) {
      representedByExtension = _extensions.objectAtIndex(i).equalsIgnoreCase(extension);
    }
    return representedByExtension;
  }

  /**
   * <span class="en">
   * Returns the "primary" extension for this mime type.  The primary extension is
   * the first extension in the list, and generally should be considered the most
   * common extension to use for the type.
   * 
   * @return the primary extension (or "" if there are no extensions)
   * </span>
   * 
   * <span class="ja">
   * Mime タイプの「優先」拡張子を戻します。優先される拡張子はリスト内の最初のエレメントです。
   * 一番よく使用されるべきの拡張子が最初に来るはずです。
   * 
   * @return 優先される拡張子 (又は、拡張子がない場合には "")
   * </span>
  */
  public String primaryExtension() {
    String extension;
    if (_extensions != null && _extensions.count() > 0) {
      extension = _extensions.objectAtIndex(0);
    }
    else {
      extension = "";
    }
    return extension;
  }

  /**
   * <span class="en">
   * Returns the part of the mime type before the "/".
   * 
   * @return the type of the mime type
   * </span>
   * 
   * <span class="ja">
   * "/" の前にある Mime タイプを戻します。
   * 
   * @return Mime タイプの親分類を戻します
   * </span>
   */
  public String type() {
    return _mimeType.substring(0, _mimeType.indexOf('/'));
  }

  /**
   * <span class="en">
   * Returns the part of the mime type after the "/".
   * 
   * @return the subtype of the mime type
   * </span>
   * 
   * <span class="ja">
   * "/" の後に続く Mime タイプを戻します。
   * 
   * @return Mime タイプの子分類を戻します
   * </span>
   */
  public String subtype() {
    return _mimeType.substring(_mimeType.indexOf('/') + 1);
  }

  /**
   * <span class="en">
   * Returns true for image/<whatever> mime types.
   * 
   * @return true for image/<whatever> mime types
   * </span>
   * 
   * <span class="ja">
   * image/<whatever>　を含む場合には true が戻ります。
   * 
   * @return image/<whatever>　を含む場合には true
   * </span>
   */
  public boolean isImage() {
    return _mimeType.startsWith("image/");
  }

  /**
   * <span class="en">
   * Returns true for video/<whatever> mime types.
   * 
   * @return true for video/<whatever> mime types
   * </span>
   * 
   * <span class="ja">
   * video/<whatever>　を含む場合には true が戻ります。
   * 
   * @return video/<whatever>　を含む場合には true
   * </span>
   */
  public boolean isVideo() {
    return _mimeType.startsWith("video/");
  }

  /**
   * <span class="en">
   * Returns true for audio/<whatever> mime types.
   * 
   * @return true for audio/<whatever> mime types
   * </span>
   * 
   * <span class="ja">
   * audio/<whatever>　を含む場合には true が戻ります。
   * 
   * @return audio/<whatever>　を含む場合には true
   * </span>
   */
  public boolean isAudio() {
    return _mimeType.startsWith("audio/");
  }
  
  @Override
  public String toString() {
    return "[ERMimeType: mimeType=" + _mimeType + "]";
  }
}
