package er.attachment.metadata;

/**
 * <span class="en">
 * IERMetadataDirectory provides the base interface for any metadata directory.
 * </span>
 * 
 * <span class="ja">
 * IERMetadataDirectory はメタデータ・ディレクトリの為の基本インタフェースを提供します。
 * </span>
 *  
 * @author mschrag
 */
public interface IERMetadataDirectory {
  
  /**
   * <span class="en">The name for an EXIF directory.</span>
   * <span class="ja">EXIF ディレクトリの名前</span>
   */
  public static final String EXIF = "EXIF";
  
  /**
   * <span class="en">The name for an IPTC directory.</span>
   * <span class="ja">IPTC ディレクトリの名前</span>
   */
  public static final String IPTC = "IPTC";
  
  /**
   * <span class="en">The name for a TIFF directory.</span>
   * <span class="ja">TIFF ディレクトリの名前</span>
   */
  public static final String TIFF = "TIFF";
  
  /**
   * <span class="en">The name for a JFIF directory.</span>
   * <span class="ja">JFIF ディレクトリの名前</span>
   */
  public static final String JFIF = "JFIF";
  
  /**
   * <span class="en">The name for a PDF directory.</span>
   * <span class="ja">PDF ディレクトリの名前</span>
   */
  public static final String PDF = "PDF";

  /**
   * <span class="en">
   * Returns the name of this directory.
   * 
   * @return the name of this directory
   * </span>
   * 
   * <span class="ja">
   *  このディレクトリの名前を戻します。
   * 
   * @return このディレクトリの名前
   * </span>
   */
  public String getDirectoryName();
}