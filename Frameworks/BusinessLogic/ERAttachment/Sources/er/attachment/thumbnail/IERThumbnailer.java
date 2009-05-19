package er.attachment.thumbnail;

import java.io.File;
import java.io.IOException;

import er.attachment.utils.ERMimeType;

public interface IERThumbnailer {
  public boolean canThumbnail(ERMimeType mimeType);

	public void thumbnail(int resizeWidth, int resizeHeight, File inputFile, File outputFile) throws IOException;

	public void thumbnail(int resizeWidth, int resizeHeight, File inputFile, File outputFile, ERMimeType outputMimeType) throws IOException;
}
