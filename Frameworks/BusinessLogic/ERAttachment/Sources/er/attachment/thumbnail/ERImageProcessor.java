package er.attachment.thumbnail;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import er.attachment.utils.ERMimeType;
import er.attachment.utils.ERMimeTypeManager;
import er.extensions.foundation.ERXExceptionUtilities;
import er.extensions.foundation.ERXProperties;

/**
 * ImageProcessor is a common superclass of all IImageProcessor 
 * implementations.
 * 
 * @property er.attachment.thumbnail.imageProcessor
 *
 * @author mschrag
 */
public abstract class ERImageProcessor implements IERImageProcessor {
  public static final Logger log = LoggerFactory.getLogger(ERImageProcessor.class);

  public static volatile IERImageProcessor _imageProcessor;

  /**
   * Returns the best IImageProcesor to use in your environment.
   * 
   * @return the best IImageProcesor to use in your environment
   */
  public static IERImageProcessor imageProcessor() {
    IERImageProcessor imageProcessor = _imageProcessor;
    if (imageProcessor == null) {
      synchronized (IERImageProcessor.class) {
        if (imageProcessor == null) {
          String imageProcessorKey = ERXProperties.stringForKey("er.attachment.thumbnail.imageProcessor");
          if (imageProcessorKey != null) {
            // ... add a registry of these at some point
            if ("sips".equals(imageProcessorKey)) {
              imageProcessor = new SipsImageProcessor();
            }
            else if ("imageio".equals(imageProcessorKey)) {
              imageProcessor = ImageIOImageProcessor.imageIOImageProcessor();
            }
            else if ("imagemagick".equals(imageProcessorKey)) {
              try {
                imageProcessor = ImageMagickImageProcessor.imageMagickImageProcessor();
              }
              catch (Throwable t) {
                throw new RuntimeException("Failed to load ImageMagick image processor.", t);
              }
            }
            else if ("java".equals(imageProcessorKey)) {
              imageProcessor = new Java2DImageProcessor();
            }
            else {
              throw new IllegalArgumentException("Unknown image processor '" + imageProcessorKey + "'.");
            }
          }
          else {
            // Try ImageIO ...
            try {
              imageProcessor = ImageIOImageProcessor.imageIOImageProcessor();
            }
            catch (Throwable t) {
              // ... failure in the constructor means the lib doesn't exist
              log.warn("Cannot use ImageIOProcessor: " + ERXExceptionUtilities.toParagraph(t));
            }
  
            // Try ImageMagick ...
            if (imageProcessor == null) {
              try {
                imageProcessor = ImageMagickImageProcessor.imageMagickImageProcessor();
              }
              catch (Throwable t) {
                log.warn("Cannot use ImageMagickImageProcessor: " + ERXExceptionUtilities.toParagraph(t));
              }
            }
  
            // ... and the fallback to Java2D
            if (imageProcessor == null) {
              imageProcessor = new Java2DImageProcessor();
            }
          }

          _imageProcessor = imageProcessor;
        }
      }
    }

    return imageProcessor;
  }

  /**
   * Provides a shortcut to thumbnailing an image using some default values that produce
   * decent quality thumbnail outputs.
   * 
   * @param resizeWidth the maximum resize width
   * @param resizeHeight the maximum resize height
   * @param inputFile the input file to thumbnail
   * @param outputFile the output file to write the thumbnail into
   * @throws IOException if the thumbnailing fails
   */
  public void thumbnail(int resizeWidth, int resizeHeight, File inputFile, File outputFile) throws IOException {
    ERMimeType outputMimeType = ERMimeTypeManager.mimeTypeManager().mimeTypeForFile(outputFile, true);
    thumbnail(resizeWidth, resizeHeight, inputFile, outputFile, outputMimeType);
  }
  
  /**
   * Provides a shortcut to thumbnailing an image using some default values that produce
   * decent quality thumbnail outputs.
   * 
   * @param resizeWidth the maximum resize width
   * @param resizeHeight the maximum resize height
   * @param inputFile the input file to thumbnail
   * @param outputFile the output file to write the thumbnail into
   * @param outputMimeType the output mime type
   * @throws IOException if the thumbnailing fails
   */
  public void thumbnail(int resizeWidth, int resizeHeight, File inputFile, File outputFile, ERMimeType outputMimeType) throws IOException {
    processImage(resizeWidth, resizeHeight, null, -1, 2.5f, 0.35f, 0.0f, -1, -1, -1, -1, null, false, 0.9f, null, inputFile, outputFile, outputMimeType);
  }
}
