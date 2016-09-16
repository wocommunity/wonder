package er.attachment.metadata;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * <span class="en">
 * ERParsedMetadataDirectory represents a metadata directory that has been properly parsed.
 * </span>
 * 
 * <span class="ja">
 * ERParsedMetadataDirectory はパースを成功しているメタデータ・ディレクトリ
 * </span>
 * 
 * @author mschrag
 */
public class ERParsedMetadataDirectory implements IERMetadataDirectory {
  private String _directoryName;

  private List<ERMetadataEntry> _metadataEntries;

  /**
   * <span class="en">
   * Constructs a new parsed metadata directory.
   *  
   * @param directoryName the directory name
   * </span>
   * 
   * <span class="ja">
   * 新パース済みのメタデータ・ディレクトリを作成します。
   *  
   * @param directoryName - ディレクトリ名
   * </span>
   */
  public ERParsedMetadataDirectory(String directoryName) {
    _directoryName = directoryName;
    _metadataEntries = new LinkedList<>();
  }

  /**
   * <span class="en">
   * Returns the name of this metadata directory (EXIF, IPTC, etc).
   * 
   * @return the name of this metadata directory
   * </span>
   * 
   * <span class="ja">
   * メタデータ・ディレクトリの名前を戻します。(EXIF, IPTC, 等).
   * 
   * @return メタデータ・ディレクトリの名前
   * </span>
   */
  public String getDirectoryName() {
    return _directoryName;
  }

  /**
   * <span class="en">
   * Adds a metadata entry to this directory.
   * 
   * @param entry the entry to add
   * </span>
   * 
   * <span class="ja">
   * このディレクトリのメタデータ・エントリーを追加します。
   * 
   * @param entry - 追加するメタデータ・エントリー
   * </span>
   */
  public void addMetadataEntry(ERMetadataEntry entry) {
    _metadataEntries.add(entry);
  }

  /**
   * <span class="en">
   * Returns a metadata entry for the given type.
   * 
   * @param type the type to lookup
   * 
   * @return a metadata entry for the given type
   * </span>
   * 
   * <span class="ja">
   * 指定タイプのメタデータ・エントリーを戻します。
   * 
   * @param type - ルックアップするタイプ
   * 
   * @return 指定タイプのメタデータ・エントリー
   * </span>
   */
  public ERMetadataEntry getMetadataEntryByType(int type) {
    ERMetadataEntry matchingEntry = null;
    Iterator metadataEntriesIter = _metadataEntries.iterator();
    while (matchingEntry == null && metadataEntriesIter.hasNext()) {
      ERMetadataEntry entry = (ERMetadataEntry) metadataEntriesIter.next();
      if (entry.getType() == type) {
        matchingEntry = entry;
      }
    }
    return matchingEntry;
  }

  /**
   * <span class="en">
   * Returns a metadata entry for the given name.
   * 
   * @param name the name to lookup
   * 
   * @return a metadata entry for the given name
   * </span>
   * 
   * <span class="ja">
   * 指定名前のメタデータ・エントリーを戻します。
   * 
   * @param name - ルックアップする名前
   * 
   * @return 指定名前のメタデータ・エントリー
   * </span>
   */
  public ERMetadataEntry getMetadataEntryByName(String name) {
    ERMetadataEntry matchingEntry = null;
    Iterator metadataEntriesIter = _metadataEntries.iterator();
    while (matchingEntry == null && metadataEntriesIter.hasNext()) {
      ERMetadataEntry entry = (ERMetadataEntry) metadataEntriesIter.next();
      if (name.equalsIgnoreCase(entry.getName())) {
        matchingEntry = entry;
      }
    }
    return matchingEntry;
  }

  /**
   * <span class="en">
   * Returns the metadata entries from this directory.
   * 
   * @return the metadata entries from this directory
   * </span>
   * 
   * <span class="ja">
   * このディレクトリのメタデータ・エントリーを戻します。
   * 
   * @return ディレクトリのメタデータ・エントリー
   * </span>
   */
  public Iterator<ERMetadataEntry> getMetadataEntries() {
    return _metadataEntries.iterator();
  }

  @Override
  public String toString() {
    return "[ParsedMetadataDirectory: directoryName = " + _directoryName + "; metadata = " + _metadataEntries + "]";
  }
}