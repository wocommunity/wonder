package er.attachment.metadata;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import er.extensions.foundation.ERXExceptionUtilities;

public class ERMetadataParser {
  private static final Logger log = LoggerFactory.getLogger(ERMetadataParser.class);

  private static ERMetadataParser _metadataParser;

  /**
   * Sets the singleton metadata parser implementation to use.
   * 
   * @param metadataParser the metadata parser implementation to use
   */
  public static synchronized void setMetadataProcessor(ERMetadataParser metadataParser) {
    ERMetadataParser._metadataParser = metadataParser;
  }

  /**
   * Returns an ERMetadataParser that is best suited for your environment.
   * 
   * @return an ERMetadataParser that is best suited for your environment
   */
  public static synchronized ERMetadataParser metadataParser() {
    if (ERMetadataParser._metadataParser == null) {
      ERMetadataParser._metadataParser = new ERMetadataParser();
      ERMetadataParser._metadataParser.addMetadataDirectoryParser(new DrewMetadataDirectoryParser());

      IERMetadataParser metadataParser = null;
      if (metadataParser == null) {
        // Try ImageIO ...
        try {
          metadataParser = new ImageIOMetadataParser();
        }
        catch (Throwable t) {
          // ... failure in the constructor means the lib doesn't exist
          log.warn("Cannot use ImageIOMetadataParser: {}", ERXExceptionUtilities.toParagraph(t));
        }

        // Try ImageMagick ...
        if (metadataParser == null) {
          try {
            metadataParser = ImageMagickCommandlineMetadataParser.imageMagickMetadataParser();
          }
          catch (Throwable t) {
            log.warn("Cannot use ImageMagickCommandlineMetadataParser: {}", ERXExceptionUtilities.toParagraph(t));
          }
        }

        // ... and the fallback to Java2D
        if (metadataParser == null) {
          metadataParser = new JAIMetadataParser();
        }

        ERMetadataParser._metadataParser.addMetadataParser(metadataParser);
      }
    }
    return ERMetadataParser._metadataParser;
  }

  private List<IERMetadataParser> _metadataParsers;
  private List<IERMetadataDirectoryParser> _metadataDirectoryParsers;

  /**
   * Constructs a new ERMetadataParser.
   */
  public ERMetadataParser() {
    _metadataParsers = new LinkedList<>();
    _metadataDirectoryParsers = new LinkedList<>();
  }

  /**
   * Adds a metadata parser implementation.
   * 
   * @param metadataParser a metadata parser implementation
   */
  public void addMetadataParser(IERMetadataParser metadataParser) {
    _metadataParsers.add(metadataParser);
  }

  /**
   * Adds a metadata directory parser implementation.
   * 
   * @param metadataDirectoryParser a metadata directory parser implementation
   */
  public void addMetadataDirectoryParser(IERMetadataDirectoryParser metadataDirectoryParser) {
    _metadataDirectoryParsers.add(metadataDirectoryParser);
  }

  /**
   * Parses the metadata from the given file, converts any unparsed directories to parsed directories,
   * and removes any leftover unparsed directories.
   * 
   * @param importFile the file to parser
   * @return a metadata directory set of parsed metadata
   * @throws ERMetadataParserException if metadata parsing fails
   */
  public ERMetadataDirectorySet parseMetadata(File importFile) throws ERMetadataParserException {
    ERMetadataDirectorySet fullMetadataDirectorySet = new ERMetadataDirectorySet();
    for (IERMetadataParser metadataParser : _metadataParsers) {
      ERMetadataDirectorySet tempMetadataDirectorySet = metadataParser.parseMetadata(importFile);
      if (tempMetadataDirectorySet != null) {
        fullMetadataDirectorySet.add(tempMetadataDirectorySet);
      }
    }

    if (fullMetadataDirectorySet.isEmpty()) {
      log.info("No metadata handler for '{}'.", importFile.getAbsolutePath());
    }
    else {
      for (IERMetadataDirectoryParser metadataDirectoryParser : _metadataDirectoryParsers) {
        fullMetadataDirectorySet.parseUnparsedDirectoriesWith(metadataDirectoryParser);
      }
      List<IERMetadataDirectory> unparsedMetadataDirectories = fullMetadataDirectorySet.removeUnparsedDirectories();
      if (!unparsedMetadataDirectories.isEmpty()) {
        for (IERMetadataDirectory unparsedMetadataDirectory : unparsedMetadataDirectories) {
          log.info("No metadata handler for '{}' in '{}'.", unparsedMetadataDirectory.getDirectoryName(), importFile.getAbsolutePath());
        }
      }
    }

    return fullMetadataDirectorySet;
  }
}
