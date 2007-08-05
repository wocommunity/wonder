package er.attachment.model;

import java.io.File;
import java.io.IOException;

import er.extensions.ERXProperties;

public class ERAttachmentManager {
  public static File tempFile(String entityName, String attributeName) throws IOException {
    String tempFolderPath = ERXProperties.stringForKey("er.attachment." + entityName + "." + attributeName + ".tempFolder");
    if (tempFolderPath == null) {
      tempFolderPath = ERXProperties.stringForKey("er.attachment.tempFolder");
    }
    File tempFile;
    if (tempFolderPath == null) {
      tempFile = File.createTempFile("ERAttachmentUpload", "tmp");
    }
    else {
      tempFile = File.createTempFile("ERAttachmentUpload", "tmp", new File(tempFolderPath));
    }

    File tempFolder = tempFile.getParentFile();
    if (!tempFolder.exists() && !tempFolder.mkdirs()) {
      throw new IOException("Failed to create temp folder '" + tempFolder + "'.");
    }
    
    return tempFile;
  }

  public static long maxSize(String entityName, String attributeName) {
    long maxSize = ERXProperties.longForKey("er.attachment." + entityName + "." + attributeName + ".maxSize");
    if (maxSize == 0) {
      maxSize = ERXProperties.longForKeyWithDefault("er.attachment.maxSize", Long.MAX_VALUE);
    }
    return maxSize;
  }
}
