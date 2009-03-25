package er.attachment.metadata;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * ERParsedMetadataDirectory represents a metadata directory that has been properly parsed.
 * 
 * @author mschrag
 */
public class ERParsedMetadataDirectory implements IERMetadataDirectory {
  private String _directoryName;

  private List<ERMetadataEntry> _metadataEntries;

  /**
   * Constructs a new parsed metadata directory.
   *  
   * @param directoryName the directory name
   */
  public ERParsedMetadataDirectory(String directoryName) {
    _directoryName = directoryName;
    _metadataEntries = new LinkedList<ERMetadataEntry>();
  }

  /**
   * Returns the name of this metadata directory (EXIF, IPTC, etc).
   * 
   * @return the name of this metadata directory
   */
  public String getDirectoryName() {
    return _directoryName;
  }

  /**
   * Adds a metadata entry to this directory.
   * 
   * @param entry the entry to add
   */
  public void addMetadataEntry(ERMetadataEntry entry) {
    _metadataEntries.add(entry);
  }

  /**
   * Returns a metadata entry for the given type.
   * 
   * @param type the type to lookup
   * @return a metadata entry for the given type
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
   * Returns a metadata entry for the given name.
   * @param name the name to lookup
   * @return a metadata entry for the given name
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
   * Returns the metadata entries from this directory.
   * 
   * @return the metadata entries from this directory
   */
  public Iterator<ERMetadataEntry> getMetadataEntries() {
    return _metadataEntries.iterator();
  }

  @Override
  public String toString() {
    return "[ParsedMetadataDirectory: directoryName = " + _directoryName + "; metadata = " + _metadataEntries + "]";
  }
}