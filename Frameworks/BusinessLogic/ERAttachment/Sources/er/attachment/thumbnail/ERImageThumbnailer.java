package er.attachment.thumbnail;

import java.io.File;
import java.io.IOException;

import er.attachment.utils.ERMimeType;

/**
 * An implementation of IERThumbnailer that hands off to ERImageProcessor.
 * 
 * @author mschrag
 */
public class ERImageThumbnailer implements IERThumbnailer {
	public boolean canThumbnail(ERMimeType mimeType) {
		return mimeType.isImage();
	}

	public void thumbnail(int resizeWidth, int resizeHeight, File inputFile, File outputFile) throws IOException {
		ERImageProcessor.imageProcessor().thumbnail(resizeWidth, resizeHeight, inputFile, outputFile);
	}

	public void thumbnail(int resizeWidth, int resizeHeight, File inputFile, File outputFile, ERMimeType outputMimeType) throws IOException {
		ERImageProcessor.imageProcessor().thumbnail(resizeWidth, resizeHeight, inputFile, outputFile, outputMimeType);
	}
}
