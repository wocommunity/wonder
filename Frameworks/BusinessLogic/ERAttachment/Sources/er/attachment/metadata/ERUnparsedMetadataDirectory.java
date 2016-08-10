package er.attachment.metadata;

import java.io.File;

/**
 * <span class="en">
 * An ERUnparsedMetadataDirectory represents a byte stream of metadata from an
 * image file that needs to be processed to be converted into real metadata.
 * </span>
 * 
 * <span class="ja">
 * ERUnparsedMetadataDirectory　は処理するイメージ・ファイルのメタデータのバイト・ストリームを表現します。
 * </span>
 * 
 * @author mschrag
 */
public class ERUnparsedMetadataDirectory implements IERMetadataDirectory {
  private File _file;
  
  private String _directoryName;

  private byte[] _metadata;

  /**
   * <span class="en">
   * Constructs a new unparsed metadata stream.
   * 
   * @param file the original file
   * @param directoryName the name of the directory
   * @param metadata the metadata stream
   * </span>
   * 
   * <span class="en">
	 * 新パースされていないメタデータ・ストリームを作成します。
	 * 
	 * @param file - オリジナル・ファイル
	 * @param directoryName - ディレクトリ名
	 * @param metadata - メタデータ・ストリーム
	 * </span>
   */
  public ERUnparsedMetadataDirectory(File file, String directoryName, byte[] metadata) {
    _file = file;
    _directoryName = directoryName;
    _metadata = metadata;
  }

  /**
   * <span class="en">
   * Returns the original image file.
   * 
   * @return the original image file
   * </span>
   * 
   * <span class="ja">
	 * オリジナル・イメージ・ファイルを戻します。
	 * 
	 * @return オリジナル・イメージ・ファイル
	 * </span>
   */
  public File getFile() {
    return _file;
  }
  
  /**
   * <span class="en">
   * Returns the name of the directory that this byte stream corresponds to.
   *  
   * @return the name of the directory that this byte stream corresponds to
   * </span>
   * 
   * <span class="ja">
	 * バイト・ストリームが該当しているディレクトリ名を戻します。
	 *  
	 * @return バイト・ストリームが該当しているディレクトリ名
	 * </span>
   */
  public String getDirectoryName() {
    return _directoryName;
  }

  /**
   * <span class="en">
   * Returns the metadata byte stream
   *  
   * @return the metadata byte stream
   * </span>
   * 
   * <span class="ja">
	 * メタデータ・バイト・ストリームを戻します。
	 *  
	 * @return メタデータ・バイト・ストリーム
	 * </span>
   */
  public byte[] getMetadata() {
    return _metadata;
  }

  @Override
  public String toString() {
    return "[UnparsedMetadataDirectory: directoryName = " + _directoryName + "]";
  }
}