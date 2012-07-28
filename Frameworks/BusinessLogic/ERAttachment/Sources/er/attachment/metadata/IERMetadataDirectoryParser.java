package er.attachment.metadata;

/**
 * <span class="en">
 * IERMetadataDirectoryParser provides an interface for converting an unparsed metadata directory
 * into a parsed metadata directory.
 *  </span>
 *  
 * <span class="ja">
 * IERMetadataDirectoryParser はパースされていないメタデータ・ディレクトリを
 * パースされているメタデータ・ディレクトリへ変換するインタフェースを提供します。
 * </span>
 * 
 * @author mschrag
 */
public interface IERMetadataDirectoryParser {
  
  /**
   * <span class="en">
   * Parses the unparsed metadata directory.
   * 
   * @param unparsedMetadata the unparsed metadata directory
   * 
   * @return a parsed metadata directory, or null if this parser can't process the given metadata
   * </span>
   * 
   * <span class="ja">
   * パースされていないメタデータ・ディレクトリをパースします。
   * 
   * @param unparsedMetadata - パースされていないメタデータ・ディレクトリ
   * 
   * @return パースされているメタデータ・ディレクトリ、又はパースできない場合には null
   * </span>
   */
  public ERParsedMetadataDirectory parseMetadata(ERUnparsedMetadataDirectory unparsedMetadata);
}
