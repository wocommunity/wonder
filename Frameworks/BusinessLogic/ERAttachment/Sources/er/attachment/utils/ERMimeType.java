package er.attachment.utils;

import com.webobjects.foundation.NSArray;

/**
 * <div class="en">
 * ERMimeType stores the metadata about a particular mime type.
 * </div>
 * 
 * <div class="ja">
 * ERMimeType はある Mime タイプのメタデータを保持します。
 * </div>
 * 
 * @author mschrag
 */
public class ERMimeType {
  private String _name;
  private String _mimeType;
  private String _uti;
  private NSArray<String> _extensions;

  /**
   * <div class="en">
   * Constructs an ERMimeType.
   * </div>
   * 
   * <div class="ja">
   * コンストラクタ
   * </div>
   * 
   * @param name <div class="en">the display name of the mime type ("Portable Network Graphics")</div>
   *             <div class="ja">Mime タイプの表示名称 ("Portable Network Graphics")</div>
   * @param mimeType <div class="en">the mime type string ("image/jpg")</div>
   *                 <div class="ja">Mime タイプ文字列表記 ("image/jpg")</div>
   * @param uti <div class="en">the universal type identifier that corresponds to this mime type ("public.jpeg")</div>
   *            <div class="ja">Mime タイプへの対応されている universal type identifier ("public.jpeg")</div>
   * @param extensions <div class="en">the array of file extensions for this mime type ("jpg", "jpeg", etc)</div>
   *                   <div class="ja">Mime タイプの拡張子配列 ("jpg", "jpeg", etc)</div>
   */
  public ERMimeType(String name, String mimeType, String uti, NSArray<String> extensions) {
    _name = name;
    _mimeType = mimeType;
    _uti = uti;
    _extensions = extensions;
  }

  /**
   * <div class="en">
   * Returns the glob type of this mime type (image/pdf=&gt;image/*).
   * </div>
   * 
   * <div class="ja">
   * Mime タイプの glob タイプを戻します。 (image/pdf=&gt;image/*)
   * </div>
   * 
   * @return <div class="en">the glob type of this mime type</div>
   *         <div class="ja">Mime タイプの glob タイプ</div>
   */
  public ERGlobMimeType globMimeType() {
    return new ERGlobMimeType(type() + "/*");
  }

  /**
   * <div class="en">
   * Returns true if this mime type exactly matches the other, meaning, the 
   * underlying mime type strings are identical.
   * </div>
   * 
   * <div class="ja">
   * Mime タイプが指定されている Mime タイプと完全一する場合 true を戻します。
   * Mime タイプの文字列表記が全く同様です。
   * </div>
   * 
   * @param mimeType <div class="en">the other mime type to compare</div>
   *                 <div class="ja">比較のために指定する Mime タイプ</div>
   * 
   * @return <div class="en">true if the mime type strings are identical</div>
   *         <div class="ja">全く同様であれば、 true が戻ります。</div>
   */
  public boolean matchesExactly(ERMimeType mimeType) {
    return _mimeType.equalsIgnoreCase(mimeType._mimeType);
  }

  /**
   * <div class="en">
   * Supports glob mime types for comparison, so image/* matches image/jpeg.
   * </div>
   * 
   * <div class="ja">
   * 比較の為の glob Mime タイプのサポートします。
   * image/* は image/jpeg をマッチします。
   * </div>
   * 
   * @param otherMimeType <div class="en">the other mime type to compare against</div>
   *                      <div class="ja">比較する為の Mime タイプ</div>
   * 
   * @return <div class="en">true if the mime types are compatible</div>
   *         <div class="ja">Mime タイプの互換あれば、 true が戻ります。</div>
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
   * <div class="en">
   * Returns the name of this mime type.
   * </div>
   * 
   * <div class="ja">
   * Mime タイプの表示名称を戻します。
   * </div>
   * 
   * @return <div class="en">the name of this mime type</div>
   *         <div class="ja">Mime タイプの表示名称</div>
   */
  public String name() {
    return _name;
  }

  /**
   * <div class="en">
   * Returns the mime type string representation.
   * </div>
   * 
   * <div class="ja">
   * Mime タイプ文字列表記を戻します。
   * </div>
   * 
   * @return <div class="en">the mime type string representation</div>
   *         <div class="ja">Mime タイプ文字列表記</div>
   */
  public String mimeType() {
    return _mimeType;
  }

  /**
   * <div class="en">
   * Returns the universal type identifier.
   * </div>
   * 
   * <div class="ja">
   * Mime タイプへの対応されている universal type identifier を戻します。
   * </div>
   * 
   * @return <div class="en">the universal type identifier</div>
   *         <div class="ja">Mime タイプへの対応されている universal type identifier</div>
   */
  public String uti() {
    return _uti;
  }

  /**
   * <div class="en">
   * Returns the list of extensions that map to this mime type.
   * </div>
   * 
   * <div class="ja">
   * Mime タイプの拡張子配列を戻します。
   * </div>
   * 
   * @return <div class="en">the list of extensions that map to this mime type</div>
   *         <div class="ja">Mime タイプの拡張子配列</div>
   */
  public NSArray<String> extensions() {
    return _extensions;
  }

  /**
   * <div class="en">
   * Returns true if this mime type represents a file of the given extension.
   * </div>
   * 
   * <div class="ja">
   * 指定されている拡張子は Mime タイプ文字列表記の一部である時に true を戻します。
   * </div>
   * 
   * @param extension <div class="en">the extension to lookup</div>
   *                  <div class="ja">ルックアップされる拡張子</div>
   * 
   * @return <div class="en">true if the extension matches one of the extensions in this mime type</div>
   *         <div class="ja">Mime タイプの拡張子の一つがマッチする時には true が戻ります。</div>
   */
  public boolean isRepresentedByExtension(String extension) {
    boolean representedByExtension = false;
    for (int i = 0; !representedByExtension && i < _extensions.count(); i++) {
      representedByExtension = _extensions.objectAtIndex(i).equalsIgnoreCase(extension);
    }
    return representedByExtension;
  }

  /**
   * <div class="en">
   * Returns the "primary" extension for this mime type.  The primary extension is
   * the first extension in the list, and generally should be considered the most
   * common extension to use for the type.
   * </div>
   * 
   * <div class="ja">
   * Mime タイプの「優先」拡張子を戻します。優先される拡張子はリスト内の最初のエレメントです。
   * 一番よく使用されるべきの拡張子が最初に来るはずです。
   * </div>
   * 
   * @return <div class="en">the primary extension (or "" if there are no extensions)</div>
   *         <div class="ja">優先される拡張子 (又は、拡張子がない場合には "")</div>
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
   * <div class="en">
   * Returns the part of the mime type before the "/".
   * </div>
   * 
   * <div class="ja">
   * "/" の前にある Mime タイプを戻します。
   * </div>
   * 
   * @return <div class="en">the type of the mime type</div>
   *         <div class="ja">Mime タイプの親分類を戻します</div>
   */
  public String type() {
    return _mimeType.substring(0, _mimeType.indexOf('/'));
  }

  /**
   * <div class="en">
   * Returns the part of the mime type after the "/".
   * </div>
   * 
   * <div class="ja">
   * "/" の後に続く Mime タイプを戻します。
   * </div>
   * 
   * @return <div class="en">the subtype of the mime type</div>
   *         <div class="ja">Mime タイプの子分類を戻します</div>
   */
  public String subtype() {
    return _mimeType.substring(_mimeType.indexOf('/') + 1);
  }

  /**
   * <div class="en">
   * Returns true for image/&lt;whatever&gt; mime types.
   * </div>
   * 
   * <div class="ja">
   * image/&lt;whatever&gt;　を含む場合には true が戻ります。
   * </div>
   * 
   * @return <div class="en">true for image/&lt;whatever&gt; mime types</div>
   *         <div class="ja">image/&lt;whatever&gt;　を含む場合には true</div>
   */
  public boolean isImage() {
    return _mimeType.startsWith("image/");
  }

  /**
   * <div class="en">
   * Returns true for video/&lt;whatever&gt; mime types.
   * </div>
   * 
   * <div class="ja">
   * video/&lt;whatever&gt;　を含む場合には true が戻ります。
   * </div>
   * 
   * @return <div class="en">true for video/&lt;whatever&gt; mime types</div>
   *         <div class="ja">video/&lt;whatever&gt;　を含む場合には true</div>
   */
  public boolean isVideo() {
    return _mimeType.startsWith("video/");
  }

  /**
   * <div class="en">
   * Returns true for audio/&lt;whatever&gt; mime types.
   * </div>
   * 
   * <div class="ja">
   * audio/&lt;whatever&gt;　を含む場合には true が戻ります。
   * </div>
   * 
   * @return <div class="en">true for audio/&lt;whatever&gt; mime types</div>
   *         <div class="ja">audio/&lt;whatever&gt;　を含む場合には true</div>
   */
  public boolean isAudio() {
    return _mimeType.startsWith("audio/");
  }
  
  @Override
  public String toString() {
    return "[ERMimeType: mimeType=" + _mimeType + "]";
  }
}
