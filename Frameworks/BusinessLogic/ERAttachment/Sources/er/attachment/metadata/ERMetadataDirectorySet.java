package er.attachment.metadata;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * <span class="en">
 * An ERMetadataDirectorySet contains generic metadata (like width/height) as well a set of
 * metadata directories (like EXIF or IPTC).
 * </span>
 * 
 * <span class="ja">
 * ERMetadataDirectorySet は一般メタデータを含みます。 (例 width/height)
 * 又はメタデータ・ディレクトリも含めるのです。 (例 EXIF や IPTC).
 * </span>
 * 
 * @author mschrag
 */
public class ERMetadataDirectorySet {
  private List<IERMetadataDirectory> _metadataDirectories;

  private int _width;
  private int _height;
  private boolean _rotated;
  private String _caption;

  public ERMetadataDirectorySet() {
    _width = -1;
    _height = -1;
    _caption = null;
    _metadataDirectories = new LinkedList<IERMetadataDirectory>();
  }

  /**
   * <span class="ja">Empty かどうかをチェックします。</span>
   */
  public boolean isEmpty() {
    return _width == -1 && _height == -1 && _metadataDirectories.isEmpty();
  }

  /**
   * <span class="en">
   * Sets the width of the image.
   * 
   * @param width the width of the image
   * </span>
   * 
   * <span class="ja">
   * イメージ幅をセットします。
   * 
   * @param width - イメージ幅
   * </span>
   */
  public void setWidth(int width) {
    _width = width;
  }

  /**
   * <span class="en">
   * Returns the width of the image.
   * 
   * @return width of the image
   * </span>
   * 
   * <span class="ja">
   * イメージ幅を戻します
   * 
   * @return イメージ幅
   * </span>
   */
  public int getWidth() {
    return _width;
  }

  /**
   * <span class="en">
   * Sets the height of the image.
   * 
   * @param height the height of the image
   * </span>
   * 
   * <span class="ja">
   * イメージ高をセットします。
   * 
   * @param height - イメージ高
   * </span>
   */
  public void setHeight(int height) {
    _height = height;
  }

  /**
   * <span class="en">
   * Returns the height of the image.
   * 
   * @return height of the image
   * </span>
   * 
   * <span class="ja">
   * イメージ高を戻します。
   * 
   * @return イメージ高
   * </span>
   */
  public int getHeight() {
    return _height;
  }

  /**
   * <span class="en">
   * Sets whether or not the given image was rotated.
   * 
   * @param rotated whether or not the given image was rotated
   * </span>
   * 
   * <span class="ja">
   * イメージが回転されている場合には true をセットします。
   * 
   * @param rotated - イメージが回転されている場合には true をセット
   * </span>
   */
  public void setRotated(boolean rotated) {
    _rotated = rotated;
  }

  /**
   * <span class="en">
   * Returns whether or not the image was rotated.
   * 
   * @return true if the image was rotated
   * </span>
   * 
   * <span class="ja">
   * イメージが回転されている場合には true が戻ります。
   * 
   * @return イメージが回転されている場合には true
   * </span>
   */
  public boolean isRotated() {
    return _rotated;
  }

  /**
   * <span class="en">
   * Returns the caption for the image.
   * 
   * @return the caption for the image
   * </span>
   * 
   * <span class="ja">
   * イメージのキャプションを戻します。
   * 
   * @return イメージのキャプション
   * </span>
   */
  public String getCaption() {
    return _caption;
  }

  /**
   * <span class="en">
   * Sets the caption for the image.
   * 
   * @param caption
   * </span>
   * 
   * <span class="ja">
   * イメージのキャプションをセットします。
   * 
   * @param caption - イメージのキャプション
   * </span>
   */
  public void setCaption(String caption) {
    _caption = caption;
  }

  /**
   * <span class="en">
   * Returns the metadata directory of the given type (IERMetadataDirectory.EXIF, IERMetadataDirectory.IPTC, etc).
   * 
   * @param directoryName the metadata directory name
   * 
   * @return the matching meatdata directory, or null if one does not exist
   * </span>
   * 
   * <span class="ja">
   * 指定タイプのメタデータ・ディレクトリを戻します。 (IERMetadataDirectory.EXIF, IERMetadataDirectory.IPTC, 等).
   * 
   * @param directoryName - メタデータ・ディレクトリ名
   * 
   * @return マッチするメタデータ・ディレクトリ、なければ null
   * </span>
   */
  public IERMetadataDirectory _getDirectoryNamed(String directoryName) {
    IERMetadataDirectory matchingMetadataDirectory = null;
    Iterator directoriesIter = _metadataDirectories.iterator();
    while (matchingMetadataDirectory == null && directoriesIter.hasNext()) {
      IERMetadataDirectory metadataDirectory = (IERMetadataDirectory) directoriesIter.next();
      if (directoryName.equals(metadataDirectory.getDirectoryName())) {
        matchingMetadataDirectory = metadataDirectory;
      }
    }
    return matchingMetadataDirectory;
  }

  /**
   * <span class="en">
   * Returns the metadata directory of the given type (IERMetadataDirectory.EXIF, IERMetadataDirectory.IPTC, etc) and casts to an ERParsedMetadataDirectory.
   * 
   * @param directoryName the metadata directory name
   * 
   * @return the matching meatdata directory, or null if one does not exist
   * </span>
   * 
   * <span class="ja">
   * 指定タイプのメタデータ・ディレクトリを戻します。 (IERMetadataDirectory.EXIF, IERMetadataDirectory.IPTC, 等) 
   * さらに ERParsedMetadataDirectory へキャストします。
   * 
   * @param directoryName - メタデータ・ディレクトリ名
   * 
   * @return マッチするメタデータ・ディレクトリ、なければ null
   * </span>
   */
  public ERParsedMetadataDirectory getDirectoryNamed(String directoryName) {
    return (ERParsedMetadataDirectory) _getDirectoryNamed(directoryName);
  }

  /**
   * Adds a metadata directory to this set.
   * 
   * @param metadata the metadata directory to add
   */
  public void addMetadata(IERMetadataDirectory metadata) {
    _metadataDirectories.add(metadata);
  }

  /**
   * Returns the metadata directories from this set.
   */
  public List<IERMetadataDirectory> getMetadataDirectories() {
    return _metadataDirectories;
  }

  /**
   * <span class="en">
   * Adds the given raw directory set to this directory set.
   * 
   * @param rawAssetMetadata a raw directory set
   * </span>
   * 
   * <span class="ja">
   * 指定の raw ディレクトリをディクショナリーセットに追加します。
   * 
   * @param rawAssetMetadata - raw ディクショナリーセット
   * </span>
   */
  public void add(ERMetadataDirectorySet rawAssetMetadata) {
    if (rawAssetMetadata._width != -1) {
      _width = rawAssetMetadata._width;
    }
    if (rawAssetMetadata._height != -1) {
      _height = rawAssetMetadata._height;
    }
    _metadataDirectories.addAll(rawAssetMetadata._metadataDirectories);
  }

  protected List<IERMetadataDirectory> removeUnparsedDirectories() {
    List<IERMetadataDirectory> unparsedMetadataDirectories = new LinkedList<IERMetadataDirectory>();
    Iterator<IERMetadataDirectory> metadataDirectoriesIter = _metadataDirectories.iterator();
    while (metadataDirectoriesIter.hasNext()) {
      IERMetadataDirectory metadataDirectory = metadataDirectoriesIter.next();
      if (metadataDirectory instanceof ERUnparsedMetadataDirectory) {
        unparsedMetadataDirectories.add(metadataDirectory);
        metadataDirectoriesIter.remove();
      }
    }
    return unparsedMetadataDirectories;
  }

  protected void parseUnparsedDirectoriesWith(IERMetadataDirectoryParser metadataDirectoryParser) {
    Iterator<IERMetadataDirectory> metadataDirectoriesIter = new LinkedList<IERMetadataDirectory>(getMetadataDirectories()).iterator();
    while (metadataDirectoriesIter.hasNext()) {
      IERMetadataDirectory metadataDirectory = metadataDirectoriesIter.next();
      ERParsedMetadataDirectory parsedMetadataDirectory;
      if (metadataDirectory instanceof ERUnparsedMetadataDirectory) {
        ERUnparsedMetadataDirectory unparsedMetadataDirectory = (ERUnparsedMetadataDirectory) metadataDirectory;
        parsedMetadataDirectory = metadataDirectoryParser.parseMetadata(unparsedMetadataDirectory);
        if (parsedMetadataDirectory != null) {
          replaceUnparsedWithParsed(unparsedMetadataDirectory, parsedMetadataDirectory);
        }
      }
      else {
        parsedMetadataDirectory = (ERParsedMetadataDirectory) metadataDirectory;
      }
    }
  }

  protected void replaceUnparsedWithParsed(ERUnparsedMetadataDirectory oldMetadata, ERParsedMetadataDirectory newMetadata) {
    _metadataDirectories.remove(oldMetadata);
    if (newMetadata != null) {
      _metadataDirectories.add(newMetadata);
    }
  }

  @Override
  public String toString() {
    return "[MetadataDirectorySet: width = " + _width + "; height = " + _height + "; caption = " + _caption + "; metadataDirectories = " + _metadataDirectories + "]";
  }
}
