package er.attachment.thumbnail;

import com.webobjects.foundation.NSMutableArray;

import er.attachment.utils.ERMimeType;

public class ERThumbnailer {
	private static NSMutableArray<IERThumbnailer> _thumbnailers = new NSMutableArray<>();

	static {
		ERThumbnailer.registerThumbnailer(new ERImageThumbnailer());
	}

	public static void registerThumbnailer(IERThumbnailer thumbnailer) {
		ERThumbnailer._thumbnailers.add(thumbnailer);
	}

	public static IERThumbnailer thumbnailer(ERMimeType mimeType) {
		for (IERThumbnailer thumbnailer : _thumbnailers) {
			if (thumbnailer.canThumbnail(mimeType)) {
				return thumbnailer;
			}
		}
		return null;
	}
}
