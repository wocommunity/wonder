package er.attachment.metadata;

/**
 * IERMetadataDirectory provides the base interface for any metadata directory.
 *  
 * @author mschrag
 */
public interface IERMetadataDirectory {
  /**
   * The name for an EXIF directory.
   */
  public static final String EXIF = "EXIF";
  
  /**
   * The name for an IPTC directory.
   */
  public static final String IPTC = "IPTC";
  
  /**
   * The name for a TIFF directory.
   */
  public static final String TIFF = "TIFF";
  
  /**
   * The name for a JFIF directory.
   */
  public static final String JFIF = "JFIF";
  
  /**
   * The name for a PDF directory.
   */
  public static final String PDF = "PDF";

  /**
   * Returns the name of this directory.
   * 
   * @return the name of this directory
   */
  public String getDirectoryName();
}