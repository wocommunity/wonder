package er.attachment.metadata;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * An ERMetadataDirectorySet contains generic metadata (like width/height) as well a set of
 * metadata directories (like EXIF or IPTC).
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

  public boolean isEmpty() {
    return _width == -1 && _height == -1 && _metadataDirectories.isEmpty();
  }

  /**
   * Sets the width of the image.
   * 
   * @param width the width of the image
   */
  public void setWidth(int width) {
    _width = width;
  }

  /**
   * Returns the width of the image.
   * 
   * @param width the width of the image
   */
  public int getWidth() {
    return _width;
  }

  /**
   * Sets the height of the image.
   * 
   * @param width the height of the image
   */
  public void setHeight(int height) {
    _height = height;
  }

  /**
   * Returns the height of the image.
   * 
   * @param width the height of the image
   */
  public int getHeight() {
    return _height;
  }

  /**
   * Sets whether or not the given image was rotated.
   * 
   * @param rotated whether or not the given image was rotated
   */
  public void setRotated(boolean rotated) {
    _rotated = rotated;
  }

  /**
   * Returns whether or not the image was rotated.
   * @return
   */
  public boolean isRotated() {
    return _rotated;
  }

  /**
   * Returns the caption for the image.
   * 
   * @return the caption for the image
   */
  public String getCaption() {
    return _caption;
  }

  /**
   * Sets the caption for the image.
   * 
   * @return the caption for the image
   */
  public void setCaption(String caption) {
    _caption = caption;
  }

  /**
   * Returns the metadata directory of the given type (IERMetadataDirectory.EXIF, IERMetadataDirectory.IPTC, etc).
   * 
   * @param directoryName the metadata directory name
   * @return the matching meatdata directory, or null if one does not exist
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
   * Returns the metadata directory of the given type (IERMetadataDirectory.EXIF, IERMetadataDirectory.IPTC, etc) and casts to an ERParsedMetadataDirectory.
   * 
   * @param directoryName the metadata directory name
   * @return the matching meatdata directory, or null if one does not exist
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
   * 
   * @param metadata the metadata directory to add
   */
  public List<IERMetadataDirectory> getMetadataDirectories() {
    return _metadataDirectories;
  }

  /**
   * Adds the given raw directory set to this directory set.
   * 
   * @param rawAssetMetadata a raw directory set
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
