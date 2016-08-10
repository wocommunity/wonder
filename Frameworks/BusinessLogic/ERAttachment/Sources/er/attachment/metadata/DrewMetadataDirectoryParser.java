package er.attachment.metadata;

import com.drew.lang.ByteArrayReader;
import com.drew.lang.SequentialByteArrayReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifReader;
import com.drew.metadata.iptc.IptcReader;

public class DrewMetadataDirectoryParser implements IERMetadataDirectoryParser {
  public ERParsedMetadataDirectory parseMetadata(ERUnparsedMetadataDirectory unparsedMetadata) {
    ERParsedMetadataDirectory parsedMetadataDirectory = null;
    
    Metadata metadata = new Metadata();
    String directoryName = unparsedMetadata.getDirectoryName();
    if (directoryName.equalsIgnoreCase(IERMetadataDirectory.EXIF)) {
      ByteArrayReader reader = new ByteArrayReader(unparsedMetadata.getMetadata());
      new ExifReader().extract(reader, metadata);
      parsedMetadataDirectory = new ERParsedMetadataDirectory(directoryName);
    }
    else if (directoryName.equalsIgnoreCase(IERMetadataDirectory.IPTC)) {
      new IptcReader().extract(new SequentialByteArrayReader(unparsedMetadata.getMetadata()), metadata, unparsedMetadata.getMetadata().length);
      parsedMetadataDirectory = new ERParsedMetadataDirectory(directoryName);
    }
    
    if (parsedMetadataDirectory != null) {
      for (Directory directory : metadata.getDirectories()) {
        DrewMetadataParser.fillInParsedMetadataDirectoryFromDrewMetadata(parsedMetadataDirectory, directory);
      }
    }
    
    return parsedMetadataDirectory;
  }
}
