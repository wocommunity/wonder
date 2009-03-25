package er.attachment.metadata;

import java.io.File;

/**
 * IERMetadataParser provides an interface to return the metadata from a File. The directory
 * can contain parsed or unparsed metadata.
 * 
 * @author mschrag
 */
public interface IERMetadataParser {
  /**
   * Parses the metadata from the given file.
   * 
   * @param importFile the file to parse metadata file
   * @return a metadata directory set of the metadata from this file
   * @throws ERMetadataParserException if metadata parsing fails
   */
  public ERMetadataDirectorySet parseMetadata(File importFile) throws ERMetadataParserException;
}
