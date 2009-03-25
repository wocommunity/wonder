package er.attachment.thumbnail;

import java.io.File;
import java.io.IOException;

public interface IERThumbnailer {
  public void thumbnail(int resizeWidth, int resizeHeight, File inputFile, File outputFile) throws IOException;
}
