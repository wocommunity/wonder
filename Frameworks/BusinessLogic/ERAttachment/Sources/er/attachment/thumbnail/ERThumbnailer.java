package er.attachment.thumbnail;

import java.io.File;

import com.webobjects.foundation.NSMutableArray;

public class ERThumbnailer {
  private static NSMutableArray<IERThumbnailer> _thumbnailers = new NSMutableArray<IERThumbnailer>();

  public static void registerThumbnailer(IERThumbnailer thumbnailer) {
    ERThumbnailer.  _thumbnailers.add(thumbnailer);
  }

  public static IERThumbnailer thumbnailer(File inputFile) {
    return null;
  }
}
