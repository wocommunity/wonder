package er.attachment.metadata;

import java.io.File;

/**
 * An ERUnparsedMetadataDirectory represents a byte stream of metadata from an
 * image file that needs to be processed to be converted into real metadata.
 * 
 * @author mschrag
 */
public class ERUnparsedMetadataDirectory implements IERMetadataDirectory {
  private File _file;
  
  private String _directoryName;

  private byte[] _metadata;

  /**
   * Constructs a new unparsed metadata stream.
   * 
   * @param file the original file
   * @param directoryName the name of the directory
   * @param metadata the metadata stream
   */
  public ERUnparsedMetadataDirectory(File file, String directoryName, byte[] metadata) {
    _file = file;
    _directoryName = directoryName;
    _metadata = metadata;
  }

  /**
   * Returns the original image file.
   * 
   * @return the original image file
   */
  public File getFile() {
    return _file;
  }
  
  /**
   * Returns the name of the directory that this byte stream corresponds to.
   *  
   * @return the name of the directory that this byte stream corresponds to
   */
  public String getDirectoryName() {
    return _directoryName;
  }

  /**
   * Returns the metadata byte stream
   *  
   * @return the metadata byte stream
   */
  public byte[] getMetadata() {
    return _metadata;
  }

  @Override
  public String toString() {
    return "[UnparsedMetadataDirectory: directoryName = " + _directoryName + "]";
  }
}