package er.attachment.metadata;

import java.io.File;

/**
 * <span class="en">
 * IERMetadataParser provides an interface to return the metadata from a File. The directory
 * can contain parsed or unparsed metadata.
 * </span>
 * 
 * <span class="ja">
 * IERMetadataParser はファイルのメタデータを戻すインタフェースを提供します。
 * ディレクトリはメタデータをパースとアンパースできます。
 * </span>
 * 
 * @author mschrag
 */
public interface IERMetadataParser {
  
  /**
   * <span class="en">
   * Parses the metadata from the given file.
   * 
   * @param importFile the file to parse metadata file
   * 
   * @return a metadata directory set of the metadata from this file
   * 
   * @throws ERMetadataParserException if metadata parsing fails
   * </span>
   * 
   * <span class="ja">
   * 指定ファイルのメタデータをパースします。
   * 
   * @param importFile - パースするファイル
   * 
   * @return ファイルのメタデータ・ディレクトリ
   * 
   * @throws ERMetadataParserException - パスが失敗した場合
   * </span>
   */
  public ERMetadataDirectorySet parseMetadata(File importFile) throws ERMetadataParserException;
}
