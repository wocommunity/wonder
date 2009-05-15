package er.attachment.metadata;

/**
 * IERMetadataDirectoryParser provides an interface for converting an unparsed metadata directory
 * into a parsed metadata directory.
 *  
 * @author mschrag
 */
public interface IERMetadataDirectoryParser {
  /**
   * Parses the unparsed metadata directory.
   * 
   * @param unparsedMetadata the unparsed metadata directory
   * @return a parsed metadata directory, or null if this parser can't process the given metadata
   */
  public ERParsedMetadataDirectory parseMetadata(ERUnparsedMetadataDirectory unparsedMetadata);
}
